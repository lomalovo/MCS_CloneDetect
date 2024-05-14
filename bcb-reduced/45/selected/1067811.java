package part1.chapter04;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TableHeight extends MyFirstTable {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part1/chapter04/table_height.pdf";

    /**
     * Creates a PDF with information about the movies
     * @param    filename the name of the PDF file that will be created.
     * @throws    DocumentException 
     * @throws    IOException
     */
    public void createPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        PdfPTable table = createFirstTable();
        document.add(new Paragraph(String.format("Table height before document.add(): %f", table.getTotalHeight())));
        document.add(new Paragraph(String.format("Height of the first row: %f", table.getRowHeight(0))));
        document.add(table);
        document.add(new Paragraph(String.format("Table height after document.add(): %f", table.getTotalHeight())));
        document.add(new Paragraph(String.format("Height of the first row: %f", table.getRowHeight(0))));
        table = createFirstTable();
        document.add(new Paragraph(String.format("Table height before setTotalWidth(): %f", table.getTotalHeight())));
        document.add(new Paragraph(String.format("Height of the first row: %f", table.getRowHeight(0))));
        table.setTotalWidth(50);
        table.setLockedWidth(true);
        document.add(new Paragraph(String.format("Table height after setTotalWidth(): %f", table.getTotalHeight())));
        document.add(new Paragraph(String.format("Height of the first row: %f", table.getRowHeight(0))));
        document.add(table);
        document.close();
    }

    /**
     * Main method.
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new TableHeight().createPdf(RESULT);
    }
}
