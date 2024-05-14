package in_action.chapterF;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This example was written by Bruno Lowagie. It is part of the book 'iText in
 * Action' by Manning Publications. 
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php 
 * http://www.manning.com/lowagie/
 */
public class HelloWorldXmpMetadata2 {

    /**
	 * Generates a PDF, RTF and HTML file with the text 'Hello World' with some
	 * metadata.
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("AppendixF: example HelloWorldXmpMetadata2");
        System.out.println("-> Creates a PDF with the text 'Hello World'");
        System.out.println("   and adds XMP metadata automatically, based");
        System.out.println("   on the PDF metadata");
        System.out.println("-> jars needed: iText.jar");
        System.out.println("-> files generated in /results subdirectory:");
        System.out.println("   HelloWorldXmpMetadata2.pdf");
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapterF/HelloWorldXmpMetadata2.pdf"));
            document.addTitle("Hello World example");
            document.addSubject("This example shows how to add metadata & XMP");
            document.addKeywords("Metadata, iText, step 3");
            document.addCreator("My program using \'iText'");
            document.addAuthor("Bruno Lowagie & Paulo Soares");
            writer.createXmpMetadata();
            document.open();
            document.add(new Paragraph("Hello World"));
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
