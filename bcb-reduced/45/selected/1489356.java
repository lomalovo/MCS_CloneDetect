package questions.metadata;

import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

public class ReplaceXMP {

    public static final String ORIGINAL = "results/questions/metadata/xmp_original.pdf";

    public static final String RESULT1 = "results/questions/metadata/xmp_altered1.pdf";

    public static final String RESULT2 = "results/questions/metadata/xmp_altered2.pdf";

    public static void main(String[] args) {
        createOriginal();
        alterXmp1();
        alterXmp2();
    }

    @SuppressWarnings("unchecked")
    public static void createOriginal() {
        Document document = new Document();
        try {
            document.addAuthor("Bruno Lowagie");
            document.addKeywords("Hello World, XMP, Metadata");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(ORIGINAL));
            writer.createXmpMetadata();
            document.open();
            document.add(new Paragraph("Hello World"));
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }

    public static void alterXmp1() {
        try {
            PdfReader reader = new PdfReader(ORIGINAL);
            PdfDictionary catalog = reader.getCatalog();
            PdfObject obj = catalog.get(PdfName.METADATA);
            PRStream stream = (PRStream) PdfReader.getPdfObject(obj);
            String metadata = new String(PdfReader.getStreamBytes(stream));
            metadata = metadata.replaceAll("Hello World", "Hello Universe");
            stream.setData(metadata.getBytes(), false);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(RESULT1));
            stamper.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static void alterXmp2() {
        try {
            PdfReader reader = new PdfReader(ORIGINAL);
            String metadata = new String(reader.getMetadata());
            metadata = metadata.replaceAll("Hello World", "Hello Universe");
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(RESULT2));
            stamper.setXmpMetadata(metadata.getBytes());
            stamper.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
