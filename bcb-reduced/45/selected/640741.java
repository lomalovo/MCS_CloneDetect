package questions.forms;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;

public class MultipleChoice {

    public static final String RESULT = "results/questions/forms/multiplechoice.pdf";

    public static void main(final String[] args) throws IOException, DocumentException {
        createPdf();
        readList();
    }

    public static void createPdf() throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        document.open();
        TextField field = new TextField(writer, new Rectangle(36, 750, 144, 806), "iText");
        field.setFontSize(9);
        String[] list_options = { "JAVA", "C", "CS", "VB", "PHP" };
        field.setChoiceExports(list_options);
        String[] list_values = { "Java", "C/C++", "C#", "VB", "PHP" };
        field.setChoices(list_values);
        PdfFormField f = field.getListField();
        f.setFieldFlags(PdfFormField.FF_MULTISELECT);
        f.put(PdfName.I, new PdfArray(new int[] { 0, 2 }));
        writer.addAnnotation(f);
        document.close();
    }

    public static void readList() throws IOException {
        PdfReader reader = new PdfReader(RESULT);
        AcroFields form = reader.getAcroFields();
        String[] selection = form.getListSelection("iText");
        for (int i = 0; i < selection.length; i++) {
            System.out.println(selection[i]);
        }
    }
}
