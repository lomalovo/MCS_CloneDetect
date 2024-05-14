package com.lowagie.text.pdf.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author kevin
 */
public class PdfTextExtractorEncodingsTest {

    /** Basic Latin characters, with Unicode values less than 128 */
    private static final String TEXT1 = "AZaz09*!";

    /** Latin-1 characters */
    private static final String TEXT2 = "'`¤¦";

    @BeforeClass
    public static void initializeFontFactory() {
        FontFactory.registerDirectories();
    }

    protected static Font getSomeTTFont(String encoding, boolean embedded, float size) {
        String fontNames[] = { "arial" };
        for (String name : fontNames) {
            Font foundFont = FontFactory.getFont(name, encoding, embedded, size);
            if (foundFont != null) {
                switch(foundFont.getBaseFont().getFontType()) {
                    case BaseFont.FONT_TYPE_TT:
                    case BaseFont.FONT_TYPE_TTUNI:
                        return foundFont;
                }
            }
        }
        throw new IllegalArgumentException("Unable to find TrueType font to test with - add the name of a TT font on the system to the fontNames array in this method");
    }

    private static byte[] createPdf(final Font font) throws Exception {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final Document document = new Document();
        PdfWriter.getInstance(document, byteStream);
        document.open();
        document.add(new Paragraph(TEXT1, font));
        document.newPage();
        document.add(new Paragraph(TEXT2, font));
        document.close();
        final byte[] pdfBytes = byteStream.toByteArray();
        return pdfBytes;
    }

    /**
     * Used for testing only if we need to open the PDF itself
     * @param bytes
     * @param file
     * @throws Exception
     */
    private void saveBytesToFile(byte[] bytes, File file) throws Exception {
        final FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes);
        outputStream.close();
        System.out.println("PDF dumped to " + file.getAbsolutePath());
    }

    /**
     * Test parsing a document which uses a standard non-embedded font.
     * 
     * @throws Exception any exception will cause the test to fail
     */
    @Test
    public void testStandardFont() throws Exception {
        Font font = new Font(Font.TIMES_ROMAN, 12);
        byte[] pdfBytes = createPdf(font);
        if (false) {
            saveBytesToFile(pdfBytes, new File("test.pdf"));
        }
        checkPdf(pdfBytes);
    }

    /**
     * Test parsing a document which uses a font encoding which creates a /Differences
     * PdfArray in the PDF.
     * 
     * @throws Exception any exception will cause the test to fail
     */
    @Test
    public void testEncodedFont() throws Exception {
        Font font = getSomeTTFont("ISO-8859-1", BaseFont.EMBEDDED, 12);
        byte[] pdfBytes = createPdf(font);
        checkPdf(pdfBytes);
    }

    /**
     * Test parsing a document which uses a Unicode font encoding which creates a /ToUnicode
     * PdfArray.
     * 
     * @throws Exception any exception will cause the test to fail
     */
    @Test
    public void testUnicodeFont() throws Exception {
        Font font = getSomeTTFont(BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        byte[] pdfBytes = createPdf(font);
        checkPdf(pdfBytes);
    }

    private void checkPdf(final byte[] pdfBytes) throws Exception {
        final PdfReader pdfReader = new PdfReader(pdfBytes);
        final PdfTextExtractor textExtractor = new PdfTextExtractor(pdfReader);
        Assert.assertEquals(TEXT1, textExtractor.getTextFromPage(1));
        Assert.assertEquals(TEXT2, textExtractor.getTextFromPage(2));
    }
}
