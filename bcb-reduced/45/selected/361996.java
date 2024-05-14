package net.woodstock.rockapi.itext.test;

import java.io.FileOutputStream;
import junit.framework.TestCase;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class ItextTest extends TestCase {

    public void test1() throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream("D:/itext.pdf"));
        doc.open();
        Paragraph p1 = new Paragraph();
        p1.add(new Chunk("Um Chunk"));
        Paragraph p2 = new Paragraph("Teste2");
        Paragraph p3 = new Paragraph("Teste3");
        doc.add(p1);
        doc.add(p2);
        doc.add(p3);
        doc.close();
    }
}
