package org.apache.beam.examples;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;

public class WordMongo {

    public interface WordCountOptions extends PipelineOptions {

        @Description(" input Collection")  //参数描述
        @Default.String("file_upld_result")
        String getInPath();                //set和get方法
        void setInPath(String value);

        @Description(" output Collection")
        @Default.String("file_upld_result_new")
        String getOutPath();
        void setOutPath(String value);
    }

    static void runWordCount(WordCountOptions options) {
        MongoDbIO.Read readData = MongoDbIO.read()
                .withUri("mongodb://root:mongo@10.10.230.177:27017/demo_heri?authSource=admin")
                .withDatabase("demo_heri")
                .withCollection(options.getInPath());
        MongoDbIO.Write writeData = MongoDbIO.write()
                .withUri("mongodb://root:mongo@10.10.230.177:27017/demo_heri?authSource=admin")
                .withDatabase("demo_heri")
                .withCollection(options.getOutPath());

        Pipeline p = Pipeline.create(options);

        PCollection<Document> inputData = p.apply(readData);
        // PCollection<Document> newData = inputData.apply(ParDo.of(new DistinctTransform()));
        PCollection<Document> newData = inputData.apply(new DistinctTransform());
        newData.apply(writeData);

        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        WordCountOptions options =
                PipelineOptionsFactory.fromArgs(args).as(WordCountOptions.class);

        runWordCount(options);
    }
}

class DistinctTransform extends PTransform<PCollection<Document>, PCollection<Document>>{

    @Override
    public PCollection<Document> expand(PCollection<Document> documentPCollection) {
        PCollection<Document> distinctData = documentPCollection.apply(Distinct.withRepresentativeValueFn(new distinctValues()));
        PCollection<String> tempData = distinctData.apply(ParDo.of(new DocToString()));
        return tempData.apply(ParDo.of(new StringToDoc()));
    }

    public class distinctValues implements SerializableFunction<Document, String> {
        @Override
        public String apply(Document s) {
            return String.valueOf(s.get("structure"));
        }
    }
}

//Document转String
class DocToString extends DoFn<Document,String>{
    @ProcessElement
    public void processElement(ProcessContext c){
        String jsonString = c.element().toJson();
        c.output(jsonString);
    }
}
//String转Document
class StringToDoc extends DoFn<String,Document>{
    @ProcessElement
    public void processElement(ProcessContext c){
        String jsonString = c.element();
        c.output(Document.parse(jsonString));
    }
}