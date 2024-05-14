package in_action.chapter04;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This example was written by Bruno Lowagie. It is part of the book 'iText in
 * Action' by Manning Publications. 
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php 
 * http://www.manning.com/lowagie/
 */
public class FoxDogParagraph {

    /**
	 * Generates a PDF file with the text 'Quick brown fox jumps over the lazy
	 * dog'
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("Chapter 4: example FoxDogParagraph");
        System.out.println("-> Creates a PDF file with the text");
        System.out.println("   'Quick brown fox jumps over the lazy dog';");
        System.out.println("   the text is added using Paragraph objects.");
        System.out.println("-> jars needed: iText.jar");
        System.out.println("-> resulting PDF: fox_dog_paragraph.pdf");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapter04/fox_dog_paragraph.pdf"));
            document.open();
            Chunk space = new Chunk(' ');
            String text = "Quick brown fox jumps over the lazy dog.";
            Phrase phrase1 = new Phrase(text);
            Phrase phrase2 = new Phrase(new Chunk(text, new Font(Font.TIMES_ROMAN)));
            Phrase phrase3 = new Phrase(text, new Font(Font.COURIER));
            Paragraph paragraph = new Paragraph();
            paragraph.add(phrase1);
            paragraph.add(space);
            paragraph.add(phrase2);
            paragraph.add(space);
            paragraph.add(phrase3);
            document.add(paragraph);
            document.add(paragraph);
            paragraph.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(paragraph);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(paragraph);
            paragraph.setSpacingBefore(10);
            document.add(paragraph);
            paragraph.setSpacingBefore(0);
            paragraph.setSpacingAfter(10);
            document.add(paragraph);
            paragraph.setIndentationLeft(20);
            document.add(paragraph);
            paragraph.setIndentationRight(20);
            document.add(paragraph);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
