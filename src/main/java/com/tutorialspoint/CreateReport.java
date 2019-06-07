package com.tutorialspoint;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.io.IOUtils;

public class CreateReport {

    public static void main(String[] args) {
        String masterReportFileName =  "rawData/jasperReport/jasper_report_template.jrxml";
        String subReportFileName = "rawData/jasperReport/address_report_template.jrxml";
        String destFileName = "rawData/jasperReport/jasper_report_template.pdf";

        DataBeanList DataBeanList = new DataBeanList();
        ArrayList<DataBean> dataList = DataBeanList.getDataBeanList();
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);

        try {
            /* Compile the master and sub report */
            JasperDesign design = JRXmlLoader.load(masterReportFileName);
            JasperDesign subdesign = JRXmlLoader.load(subReportFileName);
            JasperReport jasperMasterReport = JasperCompileManager.compileReport(design);
            JasperReport jasperSubReport = JasperCompileManager.compileReport(subdesign);

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("subreportParameter", jasperSubReport);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMasterReport, parameters, beanColDataSource);

            ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReportStream));
            exporter.exportReport();
            IOUtils.write(pdfReportStream.toByteArray(), new FileOutputStream(destFileName));

        } catch (Exception e) {

            e.printStackTrace();
        }
        System.out.println("Done filling!!! ...");
    }
}