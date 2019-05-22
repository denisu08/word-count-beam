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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.*;

import java.io.IOException;
import java.util.Map;

public class MergeJSONByKey {

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
                        @ProcessElement
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
                        @ProcessElement
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
                        .apply("getKey", MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(Department.class)))
                                .via(dept -> KV.of(dept.getId(), dept)));
        // alternatve
                        /*.apply("getKey", MapElements.via(
                            new SimpleFunction<Department, KV<String, Department>>() {
                              public KV<String, Department> apply(Department dept) {
                                  return KV.of(dept.getId(), dept);
                              }
                            }
                        ));*/

        // Because we will perform a join, employees also need to be put into
        // key-value pairs, where their key is their *department id*.
        PCollectionView<Map<String, Department>> departmentSideInput =
                departments.apply("ToMapSideInput", View.<String, Department>asMap());

        // We load the PCollection of employees
        PCollection<KV<String, Employee>> employees = p.apply(new LoadEmployees())
                .apply("getKey", MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(Employee.class)))
                        .via(emp -> KV.of(emp.getDepartmentId(), emp)));

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

        // The syntax for a CoGroupByKey operation is a bit verbose.
        // In this step we define a TupleTag, which serves as identifier for a
        // PCollection.
        final TupleTag<Employee> employeesTag = new TupleTag<>();
        final TupleTag<Department> departmentsTag = new TupleTag<>();

        // We define a DoFn that is able to join a single department with multiple
        // employees.
        class JoinEmployeesWithDepartments extends DoFn<KV<String, CoGbkResult>, ExtendedEmployee> {
            @ProcessElement
            public void processElement(ProcessContext c) {
                // KV<...> elm = c.element();
                // We assume one department with the same ID, and assume that
                // employees always have a department available.
                Department dept = c.element().getValue().getOnly(departmentsTag);
                Iterable<Employee> employees = c.element().getValue().getAll(employeesTag);

                for (Employee empl : employees) {
                    ExtendedEmployee result = empl.extendWith(dept);
                    c.output(result);
                }
            }
        }

        // We use the PCollection tuple-tags to join the two PCollections.
        PCollection<KV<String, CoGbkResult>> results =
                KeyedPCollectionTuple.of(departmentsTag, departments)
                        .and(employeesTag, employees)
                        .apply(CoGroupByKey.create());

        // Finally, we convert the joined PCollections into a kind that
        // we can use: ExtendedEmployee.
        PCollection<ExtendedEmployee> extendedEmployees = results.apply("ExtendInformation", ParDo.of(new JoinEmployeesWithDepartments()));
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
                }))
                .apply("WriteCounts", TextIO.write().to("rawdata/merge/extendedEmployee2.json").withoutSharding());

        p.run().waitUntilFinish();
    }

    public static void main(String[] args) {
        AppOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(AppOptions.class);
        runMergeJSON(options);
    }
}
