package questions.separators;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.DrawInterface;

public class StarSeparators implements DrawInterface {

    public static final String RESULT = "results/questions/separators/stars.pdf";

    public static final Paragraph TEXT = new Paragraph("It was the best of times, it was the worst of times, " + "it was the age of wisdom, it was the age of foolishness, " + "it was the epoch of belief, it was the epoch of incredulity, " + "it was the season of Light, it was the season of Darkness, " + "it was the spring of hope, it was the winter of despair, " + "we had everything before us, we had nothing before us, " + "we were all going direct to Heaven, we were all going direct " + "the other way—in short, the period was so far like the present " + "period, that some of its noisiest authorities insisted on its " + "being received, for good or for evil, in the superlative degree " + "of comparison only.");

    public static final BaseFont FONT;

    static {
        TEXT.setAlignment(Element.ALIGN_JUSTIFIED);
        try {
            FONT = BaseFont.createFont();
        } catch (DocumentException e) {
            throw new ExceptionConverter(e);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Paragraph stars = new Paragraph(20);
            stars.add(new Chunk(new StarSeparators()));
            stars.setSpacingAfter(30);
            ColumnText column = new ColumnText(writer.getDirectContent());
            for (int i = 0; i < 5; i++) {
                column.addElement(TEXT);
                column.addElement(stars);
            }
            column.setSimpleColumn(36, 36, 295, 806);
            column.go();
            column.setSimpleColumn(300, 36, 559, 806);
            column.go();
            document.newPage();
            for (int i = 0; i < 50; i++) {
                document.add(TEXT);
                document.add(stars);
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
        float middle = (llx + urx) / 2;
        canvas.beginText();
        canvas.setFontAndSize(FONT, 10);
        canvas.showTextAligned(Element.ALIGN_CENTER, "*", middle, y, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, "*  *", middle, y - 10, 0);
        canvas.endText();
    }
}
