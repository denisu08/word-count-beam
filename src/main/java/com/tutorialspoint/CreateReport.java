package com.tutorialspoint;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CreateReport {

    public static void main(String[] args) {
//        ArrayNode
        String masterReportFileName =  "rawData/jasperReport/master.jrxml";
        String subReportFileName = "rawData/jasperReport/subReport.jrxml";
        String destFileName = "rawData/jasperReport/master_subreport_output.pdf";
        String dataPath = "rawData/jasperReport/dataSubReport.json";

//        DataBeanList DataBeanList = new DataBeanList();
//        ArrayList<DataBean> dataList = DataBeanList.getDataBeanList();
//        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);

        try {
            JsonDataSource jsonDataSource = new JsonDataSource(new FileInputStream(new File(dataPath)));

            /* Compile the master and sub report */
            JasperDesign design = JRXmlLoader.load(masterReportFileName);
            JasperDesign subdesign = JRXmlLoader.load(subReportFileName);
            JasperReport jasperMasterReport = JasperCompileManager.compileReport(design);
            JasperReport jasperSubReport = JasperCompileManager.compileReport(subdesign);

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SUBREPORT_KEY", jasperSubReport);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperMasterReport, parameters, jsonDataSource);

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