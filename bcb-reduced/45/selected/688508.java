package questions.images;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

public class PostCard {

    public static final String RESULT = "results/questions/images/postcard.pdf";

    public static final String RESOURCE = "resources/questions/img/bruno_original.jpg";

    public static void main(String[] args) {
        Document document = new Document(PageSize.POSTCARD);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Image img = Image.getInstance(RESOURCE);
            img.scaleToFit(PageSize.POSTCARD.getWidth(), 10000);
            img.setAbsolutePosition(0, 0);
            document.add(img);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
