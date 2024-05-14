package part3.chapter09;

import java.io.FileOutputStream;
import java.io.IOException;
import part2.chapter08.Subscribe;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PushbuttonField;

public class JSForm {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part3/chapter09/javascript.pdf";

    /** Path to the resources. */
    public static final String JS1 = "resources/js/post_from_html.js";

    /** Path to the resources. */
    public static final String JS2 = "resources/js/post_to_html.js";

    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     */
    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfWriter writer = stamper.getWriter();
        PdfAction action = PdfAction.javaScript(Utilities.readFileToString(JS1), writer);
        writer.setOpenAction(action);
        PushbuttonField button1 = new PushbuttonField(stamper.getWriter(), new Rectangle(90, 660, 160, 690), "post");
        button1.setText("POST TO HTML");
        button1.setBackgroundColor(new GrayColor(0.7f));
        button1.setVisibility(PushbuttonField.VISIBLE_BUT_DOES_NOT_PRINT);
        PdfFormField submit1 = button1.getField();
        submit1.setAction(PdfAction.javaScript(Utilities.readFileToString(JS2), writer));
        stamper.addAnnotation(submit1, 1);
        stamper.close();
    }

    /**
     * Main method.
     *
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new Subscribe().createPdf(Subscribe.FORM);
        new JSForm().manipulatePdf(Subscribe.FORM, RESULT);
    }
}
