package com.tutorialspoint;

import java.util.List;

public class DataBean {
    private String name;
    private String country;
    private List<SubReportBean> subReportBeanList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<SubReportBean> getSubReportBeanList() {
        return subReportBeanList;
    }

    public void setSubReportBeanList(List<SubReportBean> subReportBeanList) {
        this.subReportBeanList = subReportBeanList;
    }
}