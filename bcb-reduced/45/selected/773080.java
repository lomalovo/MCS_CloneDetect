package in_action.chapterX;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PushbuttonField;

/**
 * This example was written by Bruno Lowagie.
 * It is an extra example for the book 'iText in Action' by Manning Publications.
 * ISBN: 1932394796
 * http://www.1t3xt.com/docs/book.php
 * http://www.manning.com/lowagie/
 */
public class TooltipExample3 extends PdfPageEventHelper {

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("results/in_action/chapterX/tooltip3.pdf"));
            document.open();
            writer.setPageEvent(new TooltipExample3());
            Paragraph p = new Paragraph("Hello World ");
            Chunk c = new Chunk("tooltip");
            c.setGenericTag("This is my tooltip.");
            p.add(c);
            document.add(p);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }

    /**
	 * (non-Javadoc)
	 * @see com.lowagie.text.pdf.PdfPageEventHelper#onGenericTag(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document, com.lowagie.text.Rectangle, java.lang.String)
	 */
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
        try {
            PdfAnnotation annotation = PdfAnnotation.createText(writer, rect, "tooltip", text, false, null);
            annotation.put(PdfName.NM, new PdfString("mytooltip"));
            float[] red = { 1, 0, 0 };
            annotation.put(PdfName.C, new PdfArray(red));
            annotation.put(PdfName.F, new PdfNumber(PdfAnnotation.FLAGS_READONLY | PdfAnnotation.FLAGS_NOVIEW));
            PdfAnnotation popup = PdfAnnotation.createPopup(writer, new Rectangle(rect.getLeft(), rect.getBottom() - 80, rect.getRight() + 100, rect.getBottom()), null, false);
            popup.put(PdfName.PARENT, annotation.getIndirectReference());
            annotation.put(PdfName.POPUP, popup.getIndirectReference());
            writer.addAnnotation(annotation);
            writer.addAnnotation(popup);
            PushbuttonField field = new PushbuttonField(writer, rect, "mywidget");
            PdfAnnotation widget = field.getField();
            PdfDictionary dict = new PdfDictionary();
            String js1 = "var t = this.getAnnot(this.pageNum, 'mytooltip'); t.popupOpen = true; var w = this.getField('mywidget'); w.setFocus();";
            PdfAction enter = PdfAction.javaScript(js1, writer);
            dict.put(PdfName.E, enter);
            String js2 = "var t = this.getAnnot(this.pageNum, 'mytooltip'); t.popupOpen = false;";
            PdfAction exit = PdfAction.javaScript(js2, writer);
            dict.put(PdfName.X, exit);
            widget.put(PdfName.AA, dict);
            writer.addAnnotation(widget);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
