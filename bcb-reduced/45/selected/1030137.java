package com.lowagie.examples.objects.tables.pdfptable;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfWriter;

/**
 * General example using TableEvents and CellEvents.
 */
public class FloatingBoxes implements PdfPCellEvent, PdfPTableEvent {

    /**
	 * @see com.lowagie.text.pdf.PdfPTableEvent#tableLayout(com.lowagie.text.pdf.PdfPTable,
	 *      float[][], float[], int, int, com.lowagie.text.pdf.PdfContentByte[])
	 */
    public void tableLayout(PdfPTable table, float[][] width, float[] height, int headerRows, int rowStart, PdfContentByte[] canvases) {
        float widths[] = width[0];
        float x1 = widths[0];
        float x2 = widths[widths.length - 1];
        float y1 = height[0];
        float y2 = height[height.length - 1];
        PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
        canvas.setRGBColorStroke(0x00, 0x00, 0xFF);
        canvas.rectangle(x1, y1, x2 - x1, y2 - y1);
        canvas.stroke();
        canvas.resetRGBColorStroke();
    }

    /**
	 * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell,
	 *      com.lowagie.text.Rectangle, com.lowagie.text.pdf.PdfContentByte[])
	 */
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        float x1 = position.getLeft() + 2;
        float x2 = position.getRight() - 2;
        float y1 = position.getTop() - 2;
        float y2 = position.getBottom() + 2;
        PdfContentByte canvas = canvases[PdfPTable.LINECANVAS];
        canvas.setRGBColorStroke(0xFF, 0x00, 0x00);
        canvas.rectangle(x1, y1, x2 - x1, y2 - y1);
        canvas.stroke();
        canvas.resetRGBColorStroke();
    }

    /**
	 * Example originally written by Wendy Smoak to generate a Table with
	 * 'floating boxes'. Adapted by Bruno Lowagie.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        FloatingBoxes floatingBoxes = new FloatingBoxes();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("FloatingBoxes.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(2);
            table.setTableEvent(floatingBoxes);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setCellEvent(floatingBoxes);
            table.getDefaultCell().setPadding(5f);
            table.addCell("value");
            table.addCell("name");
            table.addCell(new Paragraph("dog"));
            table.addCell(new Paragraph("cat"));
            table.addCell(new Paragraph("bird"));
            table.addCell(new Paragraph("horse"));
            document.add(table);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
