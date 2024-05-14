package questions.forms;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RadioCheckField;

public class RadioButtonsOnDifferentPages {

    public static final String RESULT = "results/questions/forms/radio_different_pages.pdf";

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            String[] languages = { "English", "French", "Dutch" };
            Rectangle rect;
            PdfFormField language = PdfFormField.createRadioButton(writer, true);
            language.setFieldName("language");
            language.setValueAsName(languages[0]);
            for (int i = 0; i < languages.length; i++) {
                rect = new Rectangle(40, 806 - i * 40, 60, 788 - i * 40);
                addRadioButton(writer, rect, language, languages[i], i == 0, writer.getPageNumber() + i);
            }
            writer.addAnnotation(language);
            for (int i = 0; i < languages.length; i++) {
                cb.beginText();
                cb.setFontAndSize(bf, 18);
                cb.showTextAligned(Element.ALIGN_LEFT, languages[i], 70, 790 - i * 40, 0);
                cb.endText();
                document.newPage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        document.close();
    }

    private static void addRadioButton(PdfWriter writer, Rectangle rect, PdfFormField radio, String name, boolean on, int page) throws IOException, DocumentException {
        RadioCheckField check = new RadioCheckField(writer, rect, null, name);
        check.setCheckType(RadioCheckField.TYPE_CIRCLE);
        check.setChecked(on);
        PdfFormField field = check.getRadioField();
        field.setPlaceInPage(page);
        radio.addKid(field);
    }
}
