package com.lowagie.examples.general.faq;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Demonstrates the use of newPage.
 * @author blowagie
 */
public class NewPage {

    /**
     * Creates a PDF document with different pages.
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("Using newPage()");
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("NewPage.pdf"));
            document.open();
            document.add(new Paragraph("This is the first page."));
            document.newPage();
            document.add(new Paragraph("This is a new page"));
            document.newPage();
            document.newPage();
            document.add(new Paragraph("We invoked new page twice, yet there was no blank page added. Between the second page and this one. This is normal behaviour."));
            document.newPage();
            writer.setPageEmpty(false);
            document.newPage();
            document.add(new Paragraph("We told the writer the page wasn't empty."));
            document.newPage();
            document.add(Chunk.NEWLINE);
            document.newPage();
            document.add(new Paragraph("You can also add something invisible if you want a blank page."));
            document.add(Chunk.NEXTPAGE);
            document.add(new Paragraph("Using Chunk.NEXTPAGE also jumps to the next page"));
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
