package org.apache.beam.examples.jsonwriter;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.Row;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

public class SampleJSONWriter {
    public static void main(final String[] args) {

//        final Schema schema = Schema.builder()
//                .addInt16Field("age")
//                .addStringField("name")
//                .addStringField("id")
//                .build();
//        final Row person = Row.withSchema(schema)
//                .addValues((short) 2, "beam", "1").build()

        final Pipeline pipeline = Pipeline.create(PipelineOptionsFactory.fromArgs(args).create());

        // CoderRegistry cr = pipeline.getCoderRegistry();

        pipeline.apply("ReadCSV", TextIO.read().from("people.txt"))
                .apply("MapToPerson", ParDo.of(new DoFn<String, Person>() {
                    @DoFn.ProcessElement
                    public void onElement(
                            @Element final String file,
                            final OutputReceiver<Person> emitter) {
                        final String[] parts = file.split(";");
                        emitter.output(new Person(parts[0], parts[1], Integer.parseInt(parts[2])));
                    }
                }))
                .apply("MapToJson", ParDo.of(new DoFn<Person, String>() {
                    private transient Jsonb jsonb;

                    @ProcessElement
                    public void onElement(
                            @Element final Person person,
                            final OutputReceiver<String> emitter) {
                        if (jsonb == null) {
                            jsonb = JsonbBuilder.create();
                        }
                        emitter.output(jsonb.toJson(person));
                    }

                    @Teardown
                    public void onTearDown() {
                        if (jsonb != null) {
                            try {
                                jsonb.close();
                            } catch (final Exception e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }
                }))
                .apply("WriteToJson", TextIO.write().to("output.json").withoutSharding());


        pipeline.run().waitUntilFinish();
    }
}
