package org.gerhardb.lib.print;

import java.awt.Color;
import java.io.FileOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class ITextDemoText {

    public static void main(String[] args) {
        String output = "d:/trash/hello.pdf";
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            HeaderFooter event = new HeaderFooter();
            writer.setPageEvent(event);
            BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
            BaseFont bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
            int y_line1 = 650;
            int y_line2 = y_line1 - 50;
            int y_line3 = y_line2 - 50;
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(0f);
            cb.moveTo(250, y_line3 - 100);
            cb.lineTo(250, y_line1 + 100);
            cb.moveTo(50, y_line1);
            cb.lineTo(400, y_line1);
            cb.moveTo(50, y_line2);
            cb.lineTo(400, y_line2);
            cb.moveTo(50, y_line3);
            cb.lineTo(400, y_line3);
            cb.stroke();
            cb.beginText();
            cb.setFontAndSize(bf_helv, 12);
            String text = "Sample text for alignment";
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, text + " Center", 250, y_line1, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, text + " Right", 250, y_line2, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text + " Left", 250, y_line3, 0);
            cb.endText();
            document.newPage();
            Paragraph par = new Paragraph("bold paragraph");
            par.getFont().setStyle(Font.BOLD);
            document.add(par);
            par = new Paragraph("italic paragraph");
            par.getFont().setStyle(Font.ITALIC);
            document.add(par);
            par = new Paragraph("underlined and strike-through paragraph");
            par.getFont().setStyle(Font.UNDERLINE | Font.STRIKETHRU);
            document.add(par);
            PdfPTable table = new PdfPTable(3);
            table.setSpacingBefore(20);
            table.getDefaultCell().setPadding(5);
            PdfPCell cell = new PdfPCell(new Phrase("header"));
            cell.setPadding(5);
            cell.setColspan(3);
            table.addCell(cell);
            table.setHeaderRows(1);
            cell = new PdfPCell(new Phrase("example cell with rowspan 2 and red border"));
            cell.setPadding(5);
            cell.setRowspan(2);
            cell.setBorderColor(new BaseColor(new Color(255, 0, 0)));
            table.addCell(cell);
            table.addCell("1.1");
            table.addCell("2.1");
            table.addCell("1.2");
            table.addCell("2.2");
            cell = new PdfPCell(new Phrase("align center"));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("rotated cell"));
            cell.setPadding(5);
            cell.setRowspan(2);
            cell.setColspan(2);
            cell.setRotation(90);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("align right"));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            document.add(table);
            cb.beginText();
            cb.setFontAndSize(bf_times, 14);
            cb.setTextMatrix(100, 300);
            cb.showText("Text at position 100, 300.");
            cb.endText();
            PdfTemplate template = cb.createTemplate(300, 300);
            template.beginText();
            template.setFontAndSize(bf_times, 14);
            template.showText("Rotated text at position 400, 200.");
            template.endText();
            float rotate = 90;
            float x = 400;
            float y = 200;
            float angle = (float) (-rotate * (Math.PI / 180));
            float xScale = (float) Math.cos(angle);
            float yScale = (float) Math.cos(angle);
            float xRot = (float) -Math.sin(angle);
            float yRot = (float) Math.sin(angle);
            cb.addTemplate(template, xScale, xRot, yRot, yScale, x, y);
            document.close();
            Runtime.getRuntime().exec("\"C:/Program Files (x86)/Adobe/Reader 10.0/Reader/AcroRd32.exe\" " + output);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /** Inner class to add a header and a footer. */
    static class HeaderFooter extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            switch(writer.getPageNumber() % 2) {
                case 0:
                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("even header"), rect.getRight(), rect.getTop(), 0);
                    break;
                case 1:
                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, new Phrase("odd header"), rect.getLeft(), rect.getTop(), 0);
                    break;
            }
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(String.format("page %d", new Integer(writer.getPageNumber()))), (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() - 18, 0);
        }
    }
}
