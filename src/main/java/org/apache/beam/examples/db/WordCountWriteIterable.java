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
package org.apache.beam.examples.db;

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
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.io.Serializable;
import java.sql.PreparedStatement;

/**
 * An example project for inserting data into Google Cloud Spanner using JDBC
 * and Apache Beam. This project relies on a new feature of Apache Beam's
 * JdbcIO: The ability to write iterable of rows (i.e. the input can be
 * PCollection<Iterable<T>>).
 */
public class WordCountWriteIterable
{
	/**
	 * \p{L} denotes the category of Unicode letters, so this pattern will match
	 * on everything that is not a letter.
	 *
	 * <p>
	 * It is used for tokenizing strings in the wordcount examples.
	 */
	public static final String TOKENIZER_PATTERN = "[^\\p{L}]+";

	/**
	 * Group words together based on the first letter to allow these to be
	 * written in groups.
	 *
	 * @author loite
	 *
	 */
	static class FirstLetterAsKey extends DoFn<String, KV<String, String>> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@ProcessElement
		public void processElement(ProcessContext context) throws Exception
		{
			context.output(KV.of(context.element().substring(0, 1).toLowerCase(), context.element()));
		}
	}

	/**
	 * Extract the values from a key-value pair of first-letter-keys and list of
	 * words.
	 *
	 * @author loite
	 *
	 */
	static class GetValues extends DoFn<KV<String, Iterable<String>>, Iterable<String>> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@ProcessElement
		public void processElement(ProcessContext context) throws Exception
		{
			context.output(context.element().getValue());
		}
	}

	/**
	 * Concept #2: You can make your pipeline assembly code less verbose by
	 * defining your DoFns statically out-of-line. This DoFn tokenizes lines of
	 * text into individual words; we pass it to a ParDo in the pipeline.
	 */
	static class ExtractWordsFn extends DoFn<String, String>
	{
		private static final long serialVersionUID = 1L;
		private final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");
		private final Distribution lineLenDist = Metrics.distribution(ExtractWordsFn.class, "lineLenDistro");

		@ProcessElement
		public void processElement(ProcessContext c)
		{
			lineLenDist.update(c.element().length());
			if (c.element().trim().isEmpty())
			{
				emptyLines.inc();
			}

			// Split the line into words.
			String[] words = c.element().split(TOKENIZER_PATTERN);

			// Output each word encountered into the output PCollection.
			for (String word : words)
			{
				if (!word.isEmpty())
				{
					c.output(word);
				}
			}
		}
	}

	/**
	 * A SimpleFunction that converts a Word and Count into a printable string.
	 */
	public static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(KV<String, Long> input)
		{
			return input.getKey() + ": " + input.getValue();
		}
	}

	/**
	 * A PTransform that converts a PCollection containing lines of text into a
	 * PCollection of formatted word counts.
	 *
	 * <p>
	 * Concept #3: This is a custom composite transform that bundles two
	 * transforms (ParDo and Count) as a reusable PTransform subclass. Using
	 * composite transforms allows for easy reuse, modular testing, and an
	 * improved monitoring experience.
	 */
	public static class CountWords extends PTransform<PCollection<String>, PCollection<KV<String, Long>>>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public PCollection<KV<String, Long>> expand(PCollection<String> lines)
		{

			// Convert lines of text into individual words.
			PCollection<String> words = lines.apply(ParDo.of(new ExtractWordsFn()));

			// Count the number of times each word occurs.
			PCollection<KV<String, Long>> wordCounts = words.apply(Count.<String> perElement());

			return wordCounts;
		}
	}

	static class WordCountPreparedStatementSetter implements JdbcIO.PreparedStatementSetter<String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void setParameters(String element, PreparedStatement preparedStatement) throws Exception
		{
			preparedStatement.setString(1, element);
		}
	}

	/**
	 * Options supported by {@link WordCountWriteIterable}.
	 *
	 * <p>
	 * Concept #4: Defining your own configuration options. Here, you can add
	 * your own arguments to be processed by the command-line parser, and
	 * specify default values for them. You can then access the options values
	 * in your pipeline code.
	 *
	 * <p>
	 * Inherits standard configuration options.
	 */
	public interface WordCountOptions extends PipelineOptions
	{

		/**
		 * By default, this example reads from a public dataset containing the
		 * all the works of Shakespare
		 */
		@Description("Path of the file to read from")
		@Default.String("gs://apache-beam-samples/shakespeare/*")
		String getInputFile();

		void setInputFile(String value);

		@Description("JDBC Driver to use for writing the data")
		@Default.String("nl.topicus.jdbc.CloudSpannerDriver")
		String getJdbcDriver();

		void setJdbcDriver(String driver);

		/**
		 * The default configuration uses the Google Cloud Spanner emulator
		 * (https://emulator.googlecloudspanner.com). You should supply your own
		 * configuration using an input parameter, or change the default
		 * configuration to point to your own Cloud Spanner instance.
		 *
		 * Also note how the key file can be a file stored in the Google Cloud
		 * Storage.
		 *
		 * @return The JDBC URL to connect to
		 */
		@Description("JDBC url to write the data to")
		@Default.String("jdbc:cloudspanner:https://emulator.googlecloudspanner.com:8443;UseCustomHost=true;Project=static-test-project;Instance=static-test-instance;Database=static-test-database;PvtKeyPath=gs://spanner-beam-example/config/cloudspanner-emulator-key.json")
		String getURL();

		void setURL(String url);
	}

	/**
	 * INSERT_OR_UPDATE statement for inserting the word count values. The 'ON
	 * DUPLICATE KEY UPDATE' part makes it an UPSERT statement.
	 */
	private static final String INSERT_OR_UPDATE_SQL = "INSERT INTO WordCount (word) VALUES (?) ON DUPLICATE KEY UPDATE";

	public static void main(String[] args)
	{
		WordCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(WordCountOptions.class);
		Pipeline p = Pipeline.create(options);

		// Concepts #2 and #3: Our pipeline applies the composite CountWords
		// transform, and passes the
		// static FormatAsTextFn() to the ParDo transform.
		p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
				// Count words in input file(s)
				.apply(new CountWords())
				// Format as text
				.apply(MapElements.via(new FormatAsTextFn()))
				// Make key-value pairs with the first letter as the key
				.apply(ParDo.of(new FirstLetterAsKey()))
				// Group the words by first letter
				.apply(GroupByKey.<String, String> create())
				// Get a PCollection of only the values, discarding the keys
				.apply(ParDo.of(new GetValues()));
				// Write the words to the database
//				.apply(JdbcIO.<String> writeIterable()
//						.withDataSourceConfiguration(
//								JdbcIO.DataSourceConfiguration.create(options.getJdbcDriver(), options.getURL()))
//						.withStatement(INSERT_OR_UPDATE_SQL)
//						.withPreparedStatementSetter(new WordCountPreparedStatementSetter()));

		p.run().waitUntilFinish();
	}
}
