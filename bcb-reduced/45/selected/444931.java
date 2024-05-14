package questions.images;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class CustomizedTitleBar {

    public static final String RESOURCE = "resources/questions/img/button.jpg";

    public static final String RESULT = "results/questions/images/customized_titlebars.pdf";

    public static void main(String[] args) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        writer.setInitialLeading(18);
        document.open();
        Image button = Image.getInstance(RESOURCE);
        document.add(getTitleBar(writer, button, "My first title"));
        for (int i = 0; i < 20; i++) document.add(new Paragraph("Some text: " + i));
        document.add(getTitleBar(writer, button, "My second title"));
        for (int i = 0; i < 10; i++) document.add(new Paragraph("Some text: " + i));
        document.newPage();
        document.add(getTitleBar(writer, button, "My third title"));
        for (int i = 0; i < 10; i++) document.add(new Paragraph("Some text: " + i));
        document.close();
    }

    public static Image getTitleBar(PdfWriter writer, Image background, String title) throws DocumentException, IOException {
        float width = background.getWidth();
        float height = background.getHeight();
        PdfTemplate tmp = writer.getDirectContent().createTemplate(width, height);
        tmp.addImage(background, width, 0, 0, height, 0, 0);
        BaseFont font = BaseFont.createFont();
        tmp.beginText();
        tmp.setGrayFill(1);
        tmp.setFontAndSize(font, 18);
        tmp.showTextAligned(Element.ALIGN_CENTER, title, width / 2, 4, 0);
        tmp.endText();
        return Image.getInstance(tmp);
    }
}
