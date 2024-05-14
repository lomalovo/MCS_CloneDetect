package questions.separators;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.VerticalPositionMark;

public class PositionedMarks extends VerticalPositionMark {

    public static final String RESULT = "results/questions/separators/begin_end.pdf";

    protected boolean start;

    protected BaseFont bf;

    public PositionedMarks(boolean start) throws DocumentException, IOException {
        this.start = start;
        bf = BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Paragraph p = new Paragraph(StarSeparators.TEXT);
            p.add(0, new PositionedMarks(true));
            p.add(new PositionedMarks(false));
            ColumnText column = new ColumnText(writer.getDirectContent());
            for (int i = 0; i < 5; i++) {
                column.addElement(p);
            }
            column.setSimpleColumn(36, 36, 275, 806);
            column.go();
            column.setSimpleColumn(320, 36, 559, 806);
            column.go();
            document.newPage();
            for (int i = 0; i < 10; i++) {
                document.add(p);
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public void draw(PdfContentByte canvas, float llx, float lly, float urx, float ury, float y) {
        canvas.beginText();
        canvas.setFontAndSize(bf, 12);
        if (start) {
            canvas.showTextAligned(Element.ALIGN_CENTER, String.valueOf((char) 220), llx - 10, y, 0);
        } else {
            canvas.showTextAligned(Element.ALIGN_CENTER, String.valueOf((char) 220), urx + 10, y + 8, 180);
        }
        canvas.endText();
    }
}
