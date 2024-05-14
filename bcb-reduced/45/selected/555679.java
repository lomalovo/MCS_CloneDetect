package com.lowagie.examples.general.copystamp;

import java.io.FileOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Reads the pages of an existing PDF file and puts 2 pages from the existing doc
 * into one of the new doc.
 */
public class TwoOnOne {

    /**
     * Reads the pages of an existing PDF file and puts 2 pages from the existing doc
     * into one of the new doc.
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Import pages as images");
        try {
            PdfReader reader = new PdfReader("ChapterSection.pdf");
            int n = reader.getNumberOfPages();
            Rectangle psize = reader.getPageSize(1);
            float width = psize.getHeight();
            float height = psize.getWidth();
            Document document = new Document(new Rectangle(width, height));
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("2on1.pdf"));
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            int i = 0;
            int p = 0;
            while (i < n) {
                document.newPage();
                p++;
                i++;
                PdfImportedPage page1 = writer.getImportedPage(reader, i);
                cb.addTemplate(page1, .5f, 0, 0, .5f, 60, 120);
                if (i < n) {
                    i++;
                    PdfImportedPage page2 = writer.getImportedPage(reader, i);
                    cb.addTemplate(page2, .5f, 0, 0, .5f, width / 2 + 60, 120);
                }
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.beginText();
                cb.setFontAndSize(bf, 14);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "page " + p + " of " + ((n / 2) + (n % 2 > 0 ? 1 : 0)), width / 2, 40, 0);
                cb.endText();
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
