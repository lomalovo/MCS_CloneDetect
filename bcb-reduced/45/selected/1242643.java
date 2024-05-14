package part3.chapter11;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class Ligatures1 {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part3/chapter11/ligatures_1.pdf";

    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws DocumentException 
     * @throws IOException 
     * @throws    DocumentException 
     * @throws    IOException
     */
    public void createPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.CP1252, BaseFont.EMBEDDED);
        Font font = new Font(bf, 12);
        document.add(new Paragraph("Movie title: Love at First Hiccough (Denmark)", font));
        document.add(new Paragraph("directed by Tomas Villum Jensen", font));
        document.add(new Paragraph("Kærlighed ved første hik", font));
        document.add(new Paragraph(ligaturize("Kaerlighed ved f/orste hik"), font));
        document.close();
    }

    /**
     * Method that makes the ligatures for the combinations 'a' and 'e'
     * and for '/' and 'o'.
     * @param s a String that may have the combinations ae or /o
     * @return a String where the combinations are replaced by a unicode character
     */
    public String ligaturize(String s) {
        int pos;
        while ((pos = s.indexOf("ae")) > -1) {
            s = s.substring(0, pos) + 'æ' + s.substring(pos + 2);
        }
        while ((pos = s.indexOf("/o")) > -1) {
            s = s.substring(0, pos) + 'ø' + s.substring(pos + 2);
        }
        return s;
    }

    /**
     * Main method.
     *
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new Ligatures1().createPdf(RESULT);
    }
}
