package com.charismaai.service;

import com.charismaai.model.PracticeSession;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public ByteArrayInputStream generateReport(List<PracticeSession> sessions) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("CharismaAI - Performance Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            // Create Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 3, 2, 2, 5 }); // Column widths

            // Header Cells
            String[] headers = { "Date", "Eye Contact", "Fluency", "Feedback" };
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data Rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

            for (PracticeSession session : sessions) {
                table.addCell(new Phrase(session.getCreatedDate().format(formatter)));
                table.addCell(new Phrase(session.getEyeContactScore() + "%"));
                table.addCell(new Phrase(session.getAudioScore() + "%"));
                table.addCell(new Phrase(session.getFeedback()));
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
