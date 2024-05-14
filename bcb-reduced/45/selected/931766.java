package corner.orm.tapestry.pdf.components;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.tapestry.BaseComponentTestCase;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;
import corner.orm.tapestry.pdf.PdfEntityPage;
import corner.orm.tapestry.pdf.PdfOutputPageEvent;
import corner.orm.tapestry.pdf.PdfWriterDelegate;
import corner.orm.tapestry.pdf.service.FieldCreator;
import corner.orm.tapestry.pdf.service.IFieldCreator;

/**
 * @author <a href=mailto:Ghostbb@bjmaxinfo.com>Ghostbb</a>
 * @version $Revision: 3677 $
 * @since 2.3.7
 */
public class PdfTextTest extends BaseComponentTestCase {

    @Test(groups = "pdf")
    public void test_pdfText() throws Exception {
        int pageNum = 2;
        byte[] templateData = createPdfTemplateData(pageNum);
        FileCopyUtils.copy(templateData, new File("target/PdfTextTest_tmp.pdf"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfReader reader1 = new PdfReader(templateData);
        assertEquals(reader1.getNumberOfPages(), pageNum);
        PdfOutputPageEvent event = new PdfOutputPageEvent(reader1);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(event);
        PdfWriterDelegate d = new PdfWriterDelegate(writer);
        document.open();
        PdfEntityPage page = (PdfEntityPage) newInstance(PdfEntityPage.class, "template", "test.pdf in memory");
        IFieldCreator creator = new FieldCreator();
        replay();
        for (int i = 0; i < pageNum; i++) {
            PdfText pdfText = (PdfText) newInstance(PdfText.class, "value", "test pdf text" + i, "page", page, "clientId", "clientId", "id", "name" + i, "fieldCreator", creator);
            pdfText.setRectangle("100,400,200,500");
            pdfText.renderPdf(d, document);
            document.newPage();
        }
        document.close();
        byte[] bs = baos.toByteArray();
        FileCopyUtils.copy(bs, new File("target/PdfTextTest_out.pdf"));
        verify();
        PdfReader reader2 = new PdfReader(bs);
        assertEquals(reader2.getNumberOfPages(), pageNum);
        for (int i = 0; i < pageNum; i++) {
            assertEquals(reader2.getAcroFields().getField("name" + i), "test pdf text" + i);
        }
    }

    private byte[] createPdfTemplateData(int pageNum) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        for (int i = 0; i < pageNum; i++) {
            document.add(new Phrase("page:" + (i + 1)));
            TextField tf = new TextField(writer, new Rectangle(100, 400, 200, 500), "name" + i);
            tf.setText("value=asdf");
            writer.addAnnotation(tf.getTextField());
            document.newPage();
        }
        document.close();
        writer.close();
        byte[] bs = baos.toByteArray();
        return bs;
    }
}
