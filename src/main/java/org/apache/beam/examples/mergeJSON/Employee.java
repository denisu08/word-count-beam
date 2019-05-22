package org.apache.beam.examples.mergeJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Employee implements Serializable {

    protected String id;
    protected String name;
    protected String dept;
    protected String country;
    protected String gender;
    protected String birth_year;
    protected String emp_salary;
    protected String departmentId;

    public Employee() {
    }

    public Employee(String id, String name, String dept, String country, String gender, String birth_year, String emp_salary, String departmentId) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.country = country;
        this.gender = gender;
        this.birth_year = birth_year;
        this.emp_salary = emp_salary;
        this.departmentId = departmentId;
    }

    @JsonProperty("emp_id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("emp_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("emp_country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("emp_gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @JsonProperty("emp_birth_year")
    public String getBirth_year() {
        return birth_year;
    }

    public void setBirth_year(String birth_year) {
        this.birth_year = birth_year;
    }

    @JsonProperty("emp_salary")
    public String getEmp_salary() {
        return emp_salary;
    }

    public void setEmp_salary(String emp_salary) {
        this.emp_salary = emp_salary;
    }

    @JsonProperty("emp_dept")
    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public ExtendedEmployee extendWith(Department dept) {
        ExtendedEmployee extendedEmployee = new ExtendedEmployee();
        extendedEmployee.setEmployeeId(this.id);
        extendedEmployee.setDepartmentName(dept.getName());
        extendedEmployee.setDepartmentStartYear(dept.getStart_year());
        extendedEmployee.setEmployeeSalary(this.emp_salary);
        return extendedEmployee;
    }

    public static Employee of(String id, String name, String dept, String country, String gender, String birth_year, String emp_salary, String departmentId) {
        Employee employee = new Employee();
        employee.id = id;
        employee.name = name;
        employee.dept = dept;
        employee.country = country;
        employee.gender = gender;
        employee.birth_year = birth_year;
        employee.emp_salary = emp_salary;
        employee.departmentId = departmentId;
        return employee;
    }
}
