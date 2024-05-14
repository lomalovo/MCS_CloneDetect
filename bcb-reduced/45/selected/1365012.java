package in_action.chapter10;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This example was written by Bruno Lowagie. It is part of the book 'iText in
 * Action' by Manning Publications. 
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php 
 * http://www.manning.com/lowagie/
 */
public class PdfPTableEvents implements PdfPTableEvent {

    /**
	 * @see com.lowagie.text.pdf.PdfPTableEvent#tableLayout(com.lowagie.text.pdf.PdfPTable,
	 *      float[][], float[], int, int, com.lowagie.text.pdf.PdfContentByte[])
	 */
    public void tableLayout(PdfPTable table, float[][] width, float[] height, int headerRows, int rowStart, PdfContentByte[] canvas) {
        float widths[] = width[0];
        PdfContentByte cb = canvas[PdfPTable.TEXTCANVAS];
        cb.saveState();
        cb.setLineWidth(2);
        cb.setRGBColorStroke(255, 0, 0);
        cb.rectangle(widths[0], height[height.length - 1], widths[widths.length - 1] - widths[0], height[0] - height[height.length - 1]);
        cb.stroke();
        if (headerRows > 0) {
            cb.setRGBColorStroke(0, 0, 255);
            cb.rectangle(widths[0], height[headerRows], widths[widths.length - 1] - widths[0], height[0] - height[headerRows]);
            cb.stroke();
        }
        cb.restoreState();
        cb = canvas[PdfPTable.BASECANVAS];
        cb.saveState();
        cb.setLineWidth(.5f);
        for (int line = 0; line < height.length - 1; ++line) {
            widths = width[line];
            for (int col = 0; col < widths.length - 1; ++col) {
                if (line == 0 && col == 0) cb.setAction(new PdfAction("http://www.lowagie.com/iText/"), widths[col], height[line + 1], widths[col + 1], height[line]);
                cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
                cb.moveTo(widths[col], height[line]);
                cb.lineTo(widths[col + 1], height[line]);
                cb.stroke();
                cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
                cb.moveTo(widths[col], height[line]);
                cb.lineTo(widths[col], height[line + 1]);
                cb.stroke();
            }
        }
        cb.restoreState();
    }

    /**
	 * Generates a PDF file with a table.
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("Chapter 10: example PdfPTableEvents1");
        System.out.println("-> Creates a PDF file with a PdfPTable with table events.");
        System.out.println("-> jars needed: iText.jar");
        System.out.println("-> resulting PDF: pdfptable_events.pdf");
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapter10/pdfptable_events.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(4);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            for (int k = 0; k < 24; ++k) {
                if (k != 0) table.addCell(String.valueOf(k)); else table.addCell("This is an URL");
            }
            PdfPTableEvents event = new PdfPTableEvents();
            table.setTableEvent(event);
            document.add(table);
            table.setTotalWidth(300);
            table.writeSelectedRows(0, -1, 100, 600, writer.getDirectContent());
            document.newPage();
            table = new PdfPTable(4);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            for (int k = 0; k < 500 * 4; ++k) {
                if (k == 0) {
                    table.getDefaultCell().setColspan(4);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(new Phrase("This is an URL"));
                    table.getDefaultCell().setColspan(1);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    k += 3;
                } else table.addCell(new Phrase(String.valueOf(k)));
            }
            table.setTableEvent(event);
            table.setHeaderRows(3);
            document.add(table);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
