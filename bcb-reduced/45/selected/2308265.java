package questions.separators;

import java.awt.Color;
import java.io.FileOutputStream;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

public class LineSeparator1 {

    public static final String RESULT = "results/questions/separators/line_separator1.pdf";

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Paragraph separator = new Paragraph(0);
            separator.add(new Chunk(new LineSeparator(1, 80, Color.RED, Element.ALIGN_LEFT, -2)));
            ColumnText column = new ColumnText(writer.getDirectContent());
            for (int i = 0; i < 5; i++) {
                column.addElement(StarSeparators.TEXT);
                column.addElement(separator);
            }
            column.setSimpleColumn(36, 36, 295, 806);
            column.go();
            column.setSimpleColumn(300, 36, 559, 806);
            column.go();
            document.newPage();
            for (int i = 0; i < 10; i++) {
                document.add(StarSeparators.TEXT);
                document.add(separator);
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
