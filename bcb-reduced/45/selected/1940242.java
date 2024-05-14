package questions.objects;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.ChapterAutoNumber;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfWriter;

public class ChaptersAndMemory {

    public static final String RESULT = "results/questions/objects/numbered_chapters.pdf";

    /**
	 * Generates a PDF file with autonumbered chapters and an open bookmark tab
	 * 
	 * @param args
	 *            no arguments needed here
	 */
    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            document.open();
            Phrase text = new Phrase("Quick brown fox jumps over the lazy dog. ");
            ChapterAutoNumber chapter1 = new ChapterAutoNumber("This is a sample sentence:");
            chapter1.setBookmarkTitle("The fox");
            chapter1.setBookmarkOpen(false);
            chapter1.setComplete(false);
            Section section1 = chapter1.addSection("Quick");
            section1.add(text);
            section1.add(text);
            section1.add(text);
            document.add(chapter1);
            Section section2 = chapter1.addSection("Fox");
            section2.add(text);
            section2.setComplete(false);
            document.add(chapter1);
            section2.add(text);
            section2.add(text);
            section2.setComplete(true);
            chapter1.setComplete(true);
            document.add(chapter1);
            ChapterAutoNumber chapter2 = new ChapterAutoNumber("Jumps");
            chapter2.setComplete(false);
            Section section = chapter2.addSection("Over");
            section.setComplete(false);
            section.add(text);
            section.add(text);
            section.add(text);
            Section subsection1 = section.addSection("Lazy");
            subsection1.setIndentationLeft(30);
            subsection1.add(text);
            subsection1.setComplete(false);
            document.add(chapter2);
            subsection1.add(text);
            subsection1.add(text);
            subsection1.add(text);
            subsection1.add(text);
            subsection1.setComplete(true);
            Section subsection2 = section.addSection("Dog");
            subsection2.setIndentationRight(30);
            subsection2.add(text);
            subsection2.add(text);
            subsection2.add(text);
            subsection2.add(text);
            subsection2.add(text);
            Section subsection3 = section.addSection("Did you see it?");
            subsection3.setIndentation(50);
            subsection3.add(text);
            subsection3.add(text);
            subsection3.add(text);
            subsection3.add(text);
            subsection3.add(text);
            section.setComplete(true);
            document.add(chapter2);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
