package com.group5.hrms.service;

import com.group5.hrms.model.HrmReportData;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.OutputStream;
import java.util.Map;

public class HrmReportExportService {
    public void writePdf(HrmReportData data, OutputStream output) {
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        document.add(new Paragraph("PeopleFlow HRMS - " + data.getReportTitle(), title));
        document.add(new Paragraph("Department: " + data.getDepartmentLabel()));
        document.add(new Paragraph("Generated at: " + data.getGeneratedAt()));
        document.add(new Paragraph(" "));

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        for (Map.Entry<String, String> entry : data.getSummaryMetrics().entrySet()) {
            summary.addCell(new Phrase(entry.getKey()));
            summary.addCell(new Phrase(entry.getValue()));
        }
        document.add(summary);
        document.add(new Paragraph(" "));

        if (data.isEmpty()) {
            document.add(new Paragraph("No data available for the selected period and department."));
            document.close();
            return;
        }

        for (Map<String, String> row : data.getTableRows()) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            for (Map.Entry<String, String> entry : row.entrySet()) {
                table.addCell(new Phrase(entry.getKey()));
                table.addCell(new Phrase(entry.getValue()));
            }
            document.add(table);
            document.add(new Paragraph(" "));
        }
        document.close();
    }
}
