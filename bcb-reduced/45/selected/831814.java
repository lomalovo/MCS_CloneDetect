package in_action.chapter06;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This example was written by Bruno Lowagie. It is part of the book 'iText in
 * Action' by Manning Publications. 
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php 
 * http://www.manning.com/lowagie/
 */
public class PdfPTableCellHeights {

    /**
	 * Generates a PDF file with a table.
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("Chapter 6: example PdfPTableCellHeights");
        System.out.println("-> Creates a PDF file with a PdfPTable.");
        System.out.println("-> jars needed: iText.jar");
        System.out.println("-> resulting PDF: pdfptable_cellheights.pdf");
        Document document = new Document(PageSize.A5.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapter06/pdfptable_cellheights.pdf"));
            document.open();
            PdfPTable table = new PdfPTable(2);
            table.setExtendLastRow(true);
            PdfPCell cell;
            cell = new PdfPCell(new Paragraph("blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah"));
            table.addCell("wrap");
            cell.setNoWrap(false);
            table.addCell(cell);
            table.addCell("no wrap");
            cell.setNoWrap(true);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("1. blah blah\n2. blah blah blah\n3. blah blah"));
            table.addCell("fixed height (more than sufficient)");
            ;
            cell.setFixedHeight(72f);
            table.addCell(cell);
            table.addCell("fixed height (not sufficient)");
            cell.setFixedHeight(36f);
            table.addCell(cell);
            table.addCell("minimum height");
            cell = new PdfPCell(new Paragraph("blah blah"));
            cell.setMinimumHeight(36f);
            table.addCell(cell);
            table.addCell("extend last row");
            cell = new PdfPCell(new Paragraph("almost no content, but the row is extended"));
            table.addCell(cell);
            document.add(table);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
