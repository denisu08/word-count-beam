package org.apache.beam.examples;

import org.apache.beam.examples.common.ExampleUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.beam.sdk.extensions.sorter.*;

public class SortedWordCount {

    public static void main(String[] args) {
        System.out.println("start date: " + new Date());
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline p = Pipeline.create(options);

        BufferedExternalSorter.Options options1 = BufferedExternalSorter.options();

        p.apply(TextIO.read().from("test.txt"))
                .apply("ExtractWords", ParDo.of(new DoFn<String, String>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        for (String word : c.element().split(ExampleUtils.TOKENIZER_PATTERN)) {
                            if (!word.isEmpty()) {
                                c.output(word);
                            }
                        }
                    }
                }))
                .apply(Count.perElement())
                .apply(ParDo.of(new DoFn<KV<String, Long>, KV<String, Long>>() {
                    @ProcessElement
                    public void processElement(ProcessContext c){
                        KV<String, Long> element = c.element();
                        if(element.getKey().length() > 2) {
                            c.output(element);
                        }
                    }
                }))
                .apply("CreateKey", MapElements.via(new SimpleFunction<KV<String, Long>, KV<String, KV<String, Long>>>() {
                    public KV<String, KV<String, Long>> apply(KV<String, Long> input) {
                        return KV.of("sort", KV.of(input.getKey().toLowerCase(), input.getValue()));
                    }
                }))
                .apply(GroupByKey.create())
                .apply(SortValues.create(options1))
                .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Iterable<KV<String, Long>>>, String>() {
                    @Override
                    public String apply(KV<String, Iterable<KV<String, Long>>> input) {
                        return StreamSupport.stream(input.getValue().spliterator(), false)
                                .map(value -> String.format("%20s: %s", value.getKey(), value.getValue()))
                                .collect(Collectors.joining(String.format("%n")));
                    }
                }))
                .apply(TextIO.write().to("bible.txt"));
        // Run the pipeline.
        p.run().waitUntilFinish();

        System.out.println("finished date: " + new Date());
    }
}