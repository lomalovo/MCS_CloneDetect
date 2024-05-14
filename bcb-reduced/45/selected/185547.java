package questions.images;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

public class TransparentEllipse1 {

    public static final String RESULT = "results/questions/images/transparent_ellipse1.pdf";

    public static final String RESOURCE = "resources/questions/img/bruno_original.jpg";

    public static void main(String[] args) {
        Document document = new Document(PageSize.POSTCARD);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Image img = Image.getInstance(RESOURCE);
            byte[] bytes = new byte[2500];
            int byteCount = 0;
            int bitCount = 6;
            for (int x = -50; x < 50; x++) {
                for (int y = -50; y < 50; y++) {
                    double rSquare = Math.pow(x, 2) + Math.pow(y, 2);
                    if (rSquare <= 1600) {
                        bytes[byteCount] += (3 << bitCount);
                    } else if (rSquare <= 2025) {
                        bytes[byteCount] += (2 << bitCount);
                    } else if (rSquare <= 2500) {
                        bytes[byteCount] += (1 << bitCount);
                    }
                    bitCount -= 2;
                    if (bitCount < 0) {
                        bitCount = 6;
                        byteCount++;
                    }
                }
            }
            Image smask = Image.getInstance(100, 100, 1, 2, bytes);
            smask.makeMask();
            img.setImageMask(smask);
            img.setAbsolutePosition(0, 0);
            img.scaleToFit(PageSize.POSTCARD.getWidth(), PageSize.POSTCARD.getHeight());
            writer.getDirectContent().addImage(img);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
