package com.sandboxws.chinook;

import com.google.api.services.bigquery.model.TableRow;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.sandboxws.beam.coders.TableRowCoder;
import com.sandboxws.chinook.beam.AppOptions;
import com.sandboxws.chinook.bigquery.schema.AlbumTableSchema;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO.RowMapper;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Albums batch pipeline.
 *
 * @author Ahmed El.Hussaini
 */
@SuppressWarnings("serial")
public class AlbumPipeline {

    public static void main(String[] args) {
        String tableName = "Album";
        String pkName = "AlbumId";

        // Prepare and parse pipeline options
        PipelineOptionsFactory.register(AppOptions.class);
        AppOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(AppOptions.class);

        // create pipeline
        Pipeline pipeline = Pipeline.create(options);

        PGSimpleDataSource pgDataSource = getPostgresDataSource(options);
        // Fetch all albums from database
        PCollection<HashMap<String, Object>> rows = pipeline.apply(
                "Read Albums from PG",
                JdbcIO.<HashMap<String, Object>>read()
                        .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(pgDataSource))
                        .withCoder(TableRowCoder.of())
                        // Map ResultSet row to a HashMap
                        .withRowMapper(new RowMapper<HashMap<String, Object>>() {
                            @Override
                            public HashMap<String, Object> mapRow(ResultSet resultSet) throws Exception {
                                return TableRowMapper.asMap(resultSet, tableName, pkName);
                            }
                        })
                        .withQuery("select * from public.\"Album\""));

        // Build a TableRow from each HashMap
        PCollection<TableRow> bqTableRows = rows.apply(
                "HashMap to TableRow",
                ParDo.of(new HashMapToTableRowFn())
        ).setCoder(TableRowJsonCoder.of());

        // Write to BigQuery
        bqTableRows.apply("Write to BigQuery",
                BigQueryIO.writeTableRows()
                        .to(options.getOutputTable()) // Passed as an argument from the command line
                        .withSchema(AlbumTableSchema.schema()) // The schema for the BigQuery table
                        .ignoreUnknownValues() // Ignore any values passed but not defined on the table schema
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND) // Append to the BigQuery table.
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED) // Create the BigQuery table if it doesn't exist
        );

        // Run the pipeline
        pipeline.run().waitUntilFinish();
    }

    private static class HashMapToTableRowFn extends DoFn<HashMap<String, Object>, TableRow> {
        static final long serialVersionUID = 1L;

        @ProcessElement
        public void processElement(final @Element HashMap<String, Object> map, final OutputReceiver<TableRow> receiver) {
            TableRow tableRow = new TableRow();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                tableRow.set(entry.getKey(), entry.getValue());
            }

            receiver.output(tableRow);
        }
    }

    private static PGSimpleDataSource getPostgresDataSource(AppOptions options) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName(options.getPgDatabase());
        dataSource.setServerName(options.getPgHost());
        dataSource.setPortNumber(options.getPgPort());
        dataSource.setUser(options.getPgUsername());
        dataSource.setPassword(options.getPgPassword());

        return dataSource;
    }

    private static MysqlDataSource getMySQLDataSource(AppOptions options) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setDatabaseName(options.getMyDatabase());
        dataSource.setServerName(options.getMyHost());
        dataSource.setPortNumber(options.getMyPort());
        dataSource.setUser(options.getMyUsername());
        dataSource.setPassword(options.getMyPassword());

        return dataSource;
    }
}