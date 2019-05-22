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
package org.apache.beam.examples.mergeJSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.values.PCollectionView;

import java.io.IOException;
import java.util.Map;

public class MergeJSON {

    public interface AppOptions extends PipelineOptions {

        @Default.String("rawdata/merge/department.json")
        String getInputDepartmentFile();

        void setInputDepartmentFile(String value);

        @Default.String("rawdata/merge/employee.json")
        String getInputEmployeeFile();

        void setInputEmployeeFile(String value);

        /**
         * Set this required option to specify where to write the output.
         */
        @Default.String("rawdata/merge/mergeJSON")
        String getOutput();

        void setOutput(String value);
    }

    static public class LoadDepts extends PTransform<PBegin, PCollection<Department>> {

        private transient ObjectMapper om;

        @Override
        public PCollection<Department> expand(PBegin input) {
            return input.apply(TextIO.read().from("rawdata/merge/department.json"))
                    .apply("MapToDepartment", ParDo.of(new DoFn<String, Department>() {
                        @DoFn.ProcessElement
                        public void onElement(@Element final String file, final OutputReceiver<Department> emitter) throws IOException {
                            if (om == null) {
                                om = new ObjectMapper();
                            }
                            Department department = om.readValue(file, Department.class);
                            emitter.output(department);
                        }
                    }));
        }
    }

    static public class LoadEmployees extends PTransform<PBegin, PCollection<Employee>> {

        private transient ObjectMapper om;

        @Override
        public PCollection<Employee> expand(PBegin input) {
            return input.apply(TextIO.read().from("rawdata/merge/employee.json"))
                    .apply("MapToEmployee", ParDo.of(new DoFn<String, Employee>() {
                        @DoFn.ProcessElement
                        public void onElement(@Element final String file, final OutputReceiver<Employee> emitter) throws IOException {
                            if (om == null) {
                                om = new ObjectMapper();
                            }
                            Employee employee = om.readValue(file, Employee.class);
                            emitter.output(employee);
                        }
                    }));
        }
    }

    static void runMergeJSON(AppOptions options) {
        // First we want to load all departments, and put them into a PCollection
        // of key-value pairs, where the Key is their identifier. We assume that it's String-type.

        Pipeline p = Pipeline.create(options);
        PCollection<KV<String, Department>> departments =
                p.apply(new LoadDepts())
                        // .apply("getKey", MapElements.via((Department dept) -> KV.of(dept.getId(), dept)));
                        .apply("getKey", MapElements.via(
                                new SimpleFunction<Department, KV<String, Department>>() {
                                    public KV<String, Department> apply(Department dept) {
                                        return KV.of(dept.getId(), dept);
                                    }
                                }
                        ));

        // We then convert this PCollection into a map-type PCollectionView.
        // We can access this map directly within a ParDo.
        PCollectionView<Map<String, Department>> departmentSideInput =
                departments.apply("ToMapSideInput", View.<String, Department>asMap());

        // We load the PCollection of employees
        PCollection<Employee> employees = p.apply(new LoadEmployees());

        // Let's suppose that we will *extend* an employee's information with their
        // Department information. I have assumed the existence of an ExtendedEmployee
        // class to represent an employee extended with department information.
        class JoinDeptEmployeeDoFn extends DoFn<Employee, ExtendedEmployee> {

            @ProcessElement
            public void processElement(ProcessContext c) {
                // We obtain the Map-type side input with department information.
                Map<String, Department> departmentMap = c.sideInput(departmentSideInput);
                Employee empl = c.element();
                Department dept = departmentMap.get(empl.getDepartmentId());
                if (dept == null) return;

                ExtendedEmployee result = empl.extendWith(dept);
                c.output(result);
            }

        }

        // We apply the ParDo to extend the employee with department information
        // and specify that it takes in a departmentSideInput.
        PCollection<ExtendedEmployee> extendedEmployees =
                employees.apply(
                        ParDo.of(new JoinDeptEmployeeDoFn()).withSideInputs(departmentSideInput));

        extendedEmployees.apply("MapToJson", ParDo.of(new DoFn<ExtendedEmployee, String>() {
            private transient ObjectMapper om;

            @ProcessElement
            public void onElement(
                    @Element final ExtendedEmployee person,
                    final OutputReceiver<String> emitter) throws JsonProcessingException {
                if (om == null) {
                    om = new ObjectMapper();
                }
                emitter.output(om.writeValueAsString(person));
            }

            @Teardown
            public void onTearDown() {
                om = null;
            }
        })).apply("WriteCounts", TextIO.write().to("rawdata/merge/extendedEmployee.json").withoutSharding());

        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        AppOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(AppOptions.class);
        runMergeJSON(options);
    }
}
