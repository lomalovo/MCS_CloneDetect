package com.lowagie.text.pdf;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

public class MetaDataTest {

    @Test
    public void testProducer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Hello World"));
        document.close();
        PdfReader r = new PdfReader(baos.toByteArray());
        assertEquals("Producer", r.getInfo().get("Producer"), Document.getVersion());
    }
}
