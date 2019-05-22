package org.apache.beam.examples.mergeJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ExtendedEmployee implements Serializable {
    protected String employeeId;
    protected String departmentName;
    protected String departmentStartYear;
    protected String employeeSalary;

    public ExtendedEmployee() {
    }

    public ExtendedEmployee(String employeeId, String departmentName, String departmentStartYear, String employeeSalary) {
        this.employeeId = employeeId;
        this.departmentName = departmentName;
        this.departmentStartYear = departmentStartYear;
        this.employeeSalary = employeeSalary;
    }

    @JsonProperty("emp_id")
    public String getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @JsonProperty("emp_name")
    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @JsonProperty("emp_country")
    public String getDepartmentStartYear() {
        return departmentStartYear;
    }
    public void setDepartmentStartYear(String departmentStartYear) {
        this.departmentStartYear = departmentStartYear;
    }

    @JsonProperty("emp_salary")
    public String getEmployeeSalary() {
        return employeeSalary;
    }
    public void setEmployeeSalary(String employeeSalary) {
        this.employeeSalary = employeeSalary;
    }

    public static ExtendedEmployee of(String employeeId, String departmentName, String departmentStartYear, String employeeSalary) {
        ExtendedEmployee extendedEmployee = new ExtendedEmployee();
        extendedEmployee.employeeId = employeeId;
        extendedEmployee.departmentName = departmentName;
        extendedEmployee.departmentStartYear = departmentStartYear;
        extendedEmployee.employeeSalary = employeeSalary;
        return extendedEmployee;
    }
}
