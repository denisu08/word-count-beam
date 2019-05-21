/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.examples;

import org.apache.beam.examples.common.ExampleUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.sql.PreparedStatement;
import java.util.Date;

public class CountLog {

    // The Filter class
    static class FilterCSVHeaderFn extends DoFn<String, String> {
        String headerFilter;

        public FilterCSVHeaderFn(String headerFilter) {
            this.headerFilter = headerFilter;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String row = c.element();
            // Filter out elements that match the header
            if (!row.equals(this.headerFilter)) {
                c.output(row);
            }
        }
    }

    static class WordCountPreparedStatementSetter implements JdbcIO.PreparedStatementSetter<String>
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void setParameters(String element, PreparedStatement preparedStatement) throws Exception
        {
            preparedStatement.setString(1, element);
            preparedStatement.setString(2, element);
        }
    }

    public interface WordCountOptions extends PipelineOptions {

        @Description("Path of the file to read from")
        @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
        String getInputFile();

        void setInputFile(String value);

        @Description("Path of the file to write to")
        @Required
        @Default.String("car_output")
        String getOutput();

        @Description("JDBC url to write the data to")
        @Default.String("jdbc:mysql://localhost:3306/test")
        String getURL();
        void setURL(String url);

        @Description("JDBC Driver to use for writing the data")
        @Default.String("com.mysql.cj.jdbc.Driver")
        String getJdbcDriver();
        void setJdbcDriver(String driver);

        void setOutput(String value);
    }

    private static final String INSERT_OR_UPDATE_SQL = "INSERT INTO WordCount (word) VALUES (?) ON DUPLICATE KEY UPDATE word=?";

    static void runSalesCount(WordCountOptions options) {
        Pipeline p = Pipeline.create(options);

        // Concepts #2 and #3: Our pipeline applies the composite CountWords transform, and passes the
        // static FormatAsTextFn() to the ParDo transform.
        String header = "id,brand_name,model_name,sales_number";
        p.apply(TextIO.read().from("car_sales_log"))
                .apply(ParDo.of(new FilterCSVHeaderFn(header)))
                .apply("ParseAndConvertToKV", MapElements.via(
                        new SimpleFunction<String, KV<String, Integer>>() {
                            @Override
                            public KV<String, Integer> apply(String input) {
                                String[] split = input.split(",");
                                if (split.length < 4) {
                                    return null;
                                }
                                String key = split[1];
                                Integer value = Integer.valueOf(split[3]);
                                return KV.of(key, value);
                            }
                        }
                ))
                .apply(GroupByKey.<String, Integer>create())
                .apply("SumUpValuesByKey", ParDo.of(new DoFn<KV<String, Iterable<Integer>>, String>() {
                    @ProcessElement
                    public void processElement(ProcessContext context) {
                        Integer totalSales = 0;
                        String brand = context.element().getKey();
                        Iterable<Integer> sales = context.element().getValue();
                        for (Integer amount : sales) {
                            totalSales += amount;
                        }
                        context.output(brand + ": " + totalSales);
                    }
                }))
                // .apply(TextIO.write().to("car_sales_output").withoutSharding().withHeader("#brand_name: totalCount"));
                .apply(JdbcIO.<String> write()
                        .withDataSourceConfiguration(
                                JdbcIO.DataSourceConfiguration.create(options.getJdbcDriver(), options.getURL())
                                        .withUsername("root").withPassword(""))
                        .withStatement(INSERT_OR_UPDATE_SQL)
                        .withPreparedStatementSetter(new WordCountPreparedStatementSetter()));
        // id,brand_name,model_name,sales_number
        // brand_name: totalCount

        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        System.out.println("startDate: " + new Date());
        WordCountOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(WordCountOptions.class);
        runSalesCount(options);
        System.out.println("finishedDate: " + new Date());
    }
}
