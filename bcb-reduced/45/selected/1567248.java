package in_action.chapter02;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This example was written by Bruno Lowagie. It is part of the book 'iText in
 * Action' by Manning Publications. 
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php 
 * http://www.manning.com/lowagie/
 */
public class HelloWorldLandscape {

    /**
	 * Generates a PDF file with the text 'Hello World'. The page size is
	 * LETTER; the orientation is Landscape.
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("Chapter 2: example HelloWorldLandscape");
        System.out.println("-> Creates a PDF file with the text 'Hello World';");
        System.out.println("   PageSize.LETTER was used for the page size,");
        System.out.println("   the orientation was set to landscape.");
        System.out.println("-> jars needed: iText.jar");
        System.out.println("-> files generated in /results subdirectory:");
        System.out.println("   HelloWorldLandscape.pdf");
        Document document = new Document(PageSize.LETTER.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapter02/HelloWorldLandscape.pdf"));
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
