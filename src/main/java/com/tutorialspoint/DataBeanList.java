package com.tutorialspoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataBeanList {
    public ArrayList<DataBean> getDataBeanList() {

        // Create sub report data
        SubReportBean subBean1 = new SubReportBean();
        subBean1.setCity("Mumbai");
        subBean1.setStreet("M.G.Road");
        SubReportBean subBean2 = new SubReportBean();
        subBean2.setCity("New York");
        subBean2.setStreet("Park Street");
        SubReportBean subBean3 = new SubReportBean();
        subBean3.setCity("San Fransisco");
        subBean3.setStreet("King Street");

        ArrayList<DataBean> dataBeanList = new ArrayList<DataBean>();

        // Create master report data
        dataBeanList.add(produce("Manisha", "India", Arrays.asList(subBean1)));
        dataBeanList.add(produce("Dennis Ritchie", "USA", Arrays.asList(subBean2)));
        dataBeanList.add(produce("V.Anand", "India", Arrays.asList(subBean1)));
        dataBeanList.add(produce("Shrinath", "California", Arrays.asList(subBean3)));

        return dataBeanList;
    }

    /*
     * This method returns a DataBean object,
     * with name, country and sub report
     * bean data set in it.
     */
    private DataBean produce(String name, String country,
                             List<SubReportBean> subBean) {
        DataBean dataBean = new DataBean();

        dataBean.setName(name);
        dataBean.setCountry(country);
        dataBean.setSubReportBeanList(subBean);

        return dataBean;
    }
}