package de.psisystems.dmachinery.handler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfStamper;
import de.psisystems.dmachinery.io.IOUtil;
import de.psisystems.dmachinery.io.URLHelper;

/**
 * Created by IntelliJ IDEA.
 * User: stefanpudig
 * Date: Jul 31, 2009
 * Time: 12:33:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class DatamatrixHandler implements FieldHandler {

    public void setField(PdfStamper stamper, String name, String data, URL templateURL) throws DocumentException {
        if (data == null || data == "") return;
        AcroFields form = stamper.getAcroFields();
        float[] positions = form.getFieldPositions(name);
        OutputStream out = null;
        for (int k = 0; k < positions.length; k += 5) {
            Rectangle rectangle = new Rectangle(positions[k + 1], positions[k + 2], positions[k + 3], positions[k + 4]);
            Image img = null;
            DataMatrixBean bean = new DataMatrixBean();
            final int dpi = 150;
            bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi));
            bean.doQuietZone(false);
            try {
                URL imgURL = URLHelper.createByteCacheURL(name);
                URLConnection imConnection = imgURL.openConnection();
                imConnection.setDoOutput(true);
                out = imConnection.getOutputStream();
                BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/jpeg", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
                bean.generateBarcode(canvas, "123456");
                canvas.finish();
                out.close();
                img = Image.getInstance(imgURL);
                if (img != null) {
                    img.scaleToFit(rectangle.getWidth(), rectangle.getHeight());
                    img.setAbsolutePosition(positions[k + 1] + (rectangle.getWidth() - img.getScaledWidth()), positions[k + 2] + (rectangle.getHeight() - img.getScaledHeight()));
                    PdfContentByte cb = stamper.getOverContent((int) positions[k]);
                    cb.addImage(img);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.close(out);
            }
        }
    }
}
