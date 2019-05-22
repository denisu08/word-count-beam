package org.apache.beam.examples.mergeJSON;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Department implements Serializable {

    protected String id;
    protected String name;
    protected String start_year;

    public Department() {}

    public Department(String id, String name, String start_year) {
        this.id = id;
        this.name = name;
        this.start_year = start_year;
    }

    @JsonProperty("dept_id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("dept_name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("dept_start_year")
    public String getStart_year() {
        return start_year;
    }
    public void setStart_year(String start_year) {
        this.start_year = start_year;
    }

    public static Department of(String id, String name, String start_year) {
        Department department = new Department();
        department.id = id;
        department.name = name;
        department.start_year = start_year;
        return department;
    }
}
