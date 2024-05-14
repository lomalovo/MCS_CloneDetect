package part1.chapter03;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfWriter;

public class ImageDirect {

    /** The resulting PDF. */
    public static final String RESULT = "results/part1/chapter03/image_direct.pdf";

    /** The movie poster. */
    public static final String RESOURCE = "resources/img/loa.jpg";

    public static void main(String[] args) throws IOException, DocumentException {
        Document document = new Document(PageSize.POSTCARD, 30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        writer.setCompressionLevel(0);
        document.open();
        Image img = Image.getInstance(RESOURCE);
        img.setAbsolutePosition((PageSize.POSTCARD.getWidth() - img.getScaledWidth()) / 2, (PageSize.POSTCARD.getHeight() - img.getScaledHeight()) / 2);
        writer.getDirectContent().addImage(img);
        Paragraph p = new Paragraph("Foobar Film Festival", new Font(FontFamily.HELVETICA, 22));
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
        document.close();
    }
}
