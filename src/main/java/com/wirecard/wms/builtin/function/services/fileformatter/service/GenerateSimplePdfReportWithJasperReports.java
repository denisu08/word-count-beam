package com.wirecard.wms.builtin.function.services.fileformatter.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public class GenerateSimplePdfReportWithJasperReports {

    public static void main(String[] args) {
        String reportPath = "rawdata/jasperReport/myreport.jrxml";
        String dataPath = "rawData/jasperReport/data.json";
        String outputReportPath = "rawData/jasperReport/reportResult.png";
        String type = "Image"; // PDF or Image

        try {
            JasperDesign design = JRXmlLoader.load(reportPath);
            JasperReport jasperReport = JasperCompileManager.compileReport(design);

            // It is possible to generate Jasper reports from a JSON source.
            JsonDataSource jsonDataSource = new JsonDataSource(new FileInputStream(new File(dataPath)));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), jsonDataSource);

            ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
            if(type.equalsIgnoreCase("pdf")) {
                JRPdfExporter exporter = new JRPdfExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReportStream));
                exporter.exportReport();
            } else {
                DefaultJasperReportsContext.getInstance();
                JasperPrintManager printManager = JasperPrintManager.getInstance(DefaultJasperReportsContext.getInstance());
                BufferedImage rendered_image = null;
                rendered_image = (BufferedImage)printManager.printPageToImage(jasperPrint, 0,1.6f);
                ImageIO.write(rendered_image, "png", pdfReportStream);
            }

            IOUtils.write(pdfReportStream.toByteArray(), new FileOutputStream(outputReportPath));

//            response.setContentType("application/pdf");
//            response.setHeader("Content-Length", String.valueOf(pdfReportStream.size()));
//            response.addHeader("Content-Disposition", "inline; filename=jasper.pdf;");

//            OutputStream responseOutputStream = response.getOutputStream();
//            responseOutputStream.write(pdfReportStream.toByteArray());
//            responseOutputStream.close();
            pdfReportStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}