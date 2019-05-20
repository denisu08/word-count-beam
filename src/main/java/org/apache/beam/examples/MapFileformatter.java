package org.apache.beam.examples;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class MapFileformatter {

    public static class ReverseWords
            extends PTransform<PCollection<String>, PCollection<String>> {
        @Override
        public PCollection<String> expand(PCollection<String> lines) {

            // Convert lines of text into individual words.
            PCollection<String> words = lines.apply(ParDo.of(new WordCount.ExtractWordsFn()));

            // Count the number of times each word occurs.
//            PCollection<String> wordCounts = words.apply(Count.perElement());
//            return wordCounts;
            return words;
        }
    }

    public interface MapFileformatterOptions extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
        String getInputFile();

        void setInputFile(String value);

        /** Set this required option to specify where to write the output. */
        @Description("Path of the file to write to")
        @Validation.Required
        String getOutput();

        void setOutput(String value);
    }

    static void runReverse(MapFileformatterOptions options) {
        Pipeline p = Pipeline.create(options);

        // Concepts #2 and #3: Our pipeline applies the composite CountWords transform, and passes the
        // static FormatAsTextFn() to the ParDo transform.
        p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
                .apply(new ReverseWords())
                .apply("WriteReverseWords", TextIO.write().to(options.getOutput()));

        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        MapFileformatterOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(MapFileformatterOptions.class);

        runReverse(options);
    }
}
