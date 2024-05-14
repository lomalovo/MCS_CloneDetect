package questions.compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class CompressionLevelsWriter {

    public static final String RESOURCE = "resources/questions/txt/caesar.txt";

    public static final String[] RESULT = { "results/questions/compression/writer_default_compression.pdf", "results/questions/compression/writer_no_compression.pdf", "results/questions/compression/writer_best_speed.pdf", "results/questions/compression/writer_level_2.pdf", "results/questions/compression/writer_level_3.pdf", "results/questions/compression/writer_level_4.pdf", "results/questions/compression/writer_level_5.pdf", "results/questions/compression/writer_level_6.pdf", "results/questions/compression/writer_level_7.pdf", "results/questions/compression/writer_level_8.pdf", "results/questions/compression/writer_best_compression.pdf" };

    public static void main(String[] args) {
        File file;
        for (int i = -1; i < 10; i++) {
            long before = new Date().getTime();
            createPdf(i);
            long after = new Date().getTime();
            file = new File(RESULT[i + 1]);
            System.out.println(file.getName() + "; time: " + (after - before) + "ms; length: " + file.length() + " bytes");
        }
    }

    public static void createPdf(int compressionLevel) {
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT[compressionLevel + 1]));
            writer.setCompressionLevel(compressionLevel);
            document.open();
            BufferedReader reader = new BufferedReader(new FileReader(RESOURCE));
            String line;
            Paragraph p;
            while ((line = reader.readLine()) != null) {
                p = new Paragraph(line);
                p.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(p);
                document.add(Chunk.NEWLINE);
            }
            reader.close();
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
