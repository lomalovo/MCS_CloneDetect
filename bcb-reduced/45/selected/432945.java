package part4.chapter14;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class GraphicsStateOperators {

    /** The resulting PDF. */
    public static final String RESULT = "results/part4/chapter14/graphics_state.pdf";

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
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        for (int i = 25; i > 0; i--) {
            canvas.setLineWidth((float) i / 10);
            canvas.moveTo(50, 806 - (5 * i));
            canvas.lineTo(320, 806 - (5 * i));
            canvas.stroke();
        }
        canvas.restoreState();
        canvas.moveTo(350, 800);
        canvas.lineTo(350, 750);
        canvas.moveTo(540, 800);
        canvas.lineTo(540, 750);
        canvas.stroke();
        canvas.saveState();
        canvas.setLineWidth(8);
        canvas.setLineCap(PdfContentByte.LINE_CAP_BUTT);
        canvas.moveTo(350, 790);
        canvas.lineTo(540, 790);
        canvas.stroke();
        canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
        canvas.moveTo(350, 775);
        canvas.lineTo(540, 775);
        canvas.stroke();
        canvas.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);
        canvas.moveTo(350, 760);
        canvas.lineTo(540, 760);
        canvas.stroke();
        canvas.restoreState();
        canvas.saveState();
        canvas.setLineWidth(8);
        canvas.setLineJoin(PdfContentByte.LINE_JOIN_MITER);
        canvas.moveTo(387, 700);
        canvas.lineTo(402, 730);
        canvas.lineTo(417, 700);
        canvas.stroke();
        canvas.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);
        canvas.moveTo(427, 700);
        canvas.lineTo(442, 730);
        canvas.lineTo(457, 700);
        canvas.stroke();
        canvas.setLineJoin(PdfContentByte.LINE_JOIN_BEVEL);
        canvas.moveTo(467, 700);
        canvas.lineTo(482, 730);
        canvas.lineTo(497, 700);
        canvas.stroke();
        canvas.restoreState();
        canvas.saveState();
        canvas.setLineWidth(3);
        canvas.moveTo(50, 660);
        canvas.lineTo(320, 660);
        canvas.stroke();
        canvas.setLineDash(6, 0);
        canvas.moveTo(50, 650);
        canvas.lineTo(320, 650);
        canvas.stroke();
        canvas.setLineDash(6, 3);
        canvas.moveTo(50, 640);
        canvas.lineTo(320, 640);
        canvas.stroke();
        canvas.setLineDash(15, 10, 5);
        canvas.moveTo(50, 630);
        canvas.lineTo(320, 630);
        canvas.stroke();
        float[] dash1 = { 10, 5, 5, 5, 20 };
        canvas.setLineDash(dash1, 5);
        canvas.moveTo(50, 620);
        canvas.lineTo(320, 620);
        canvas.stroke();
        float[] dash2 = { 9, 6, 0, 6 };
        canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
        canvas.setLineDash(dash2, 0);
        canvas.moveTo(50, 610);
        canvas.lineTo(320, 610);
        canvas.stroke();
        canvas.restoreState();
        PdfTemplate hooks = canvas.createTemplate(300, 120);
        hooks.setLineWidth(8);
        hooks.moveTo(46, 50);
        hooks.lineTo(65, 80);
        hooks.lineTo(84, 50);
        hooks.stroke();
        hooks.moveTo(87, 50);
        hooks.lineTo(105, 80);
        hooks.lineTo(123, 50);
        hooks.stroke();
        hooks.moveTo(128, 50);
        hooks.lineTo(145, 80);
        hooks.lineTo(162, 50);
        hooks.stroke();
        hooks.moveTo(169, 50);
        hooks.lineTo(185, 80);
        hooks.lineTo(201, 50);
        hooks.stroke();
        hooks.moveTo(210, 50);
        hooks.lineTo(225, 80);
        hooks.lineTo(240, 50);
        hooks.stroke();
        canvas.saveState();
        canvas.setMiterLimit(2);
        canvas.addTemplate(hooks, 300, 600);
        canvas.restoreState();
        canvas.saveState();
        canvas.setMiterLimit(2.1f);
        canvas.addTemplate(hooks, 300, 550);
        canvas.restoreState();
        document.close();
    }

    /**
     * Main method.
     *
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new GraphicsStateOperators().createPdf(RESULT);
    }
}
