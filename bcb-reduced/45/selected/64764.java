package questions.separators;

import java.awt.Color;
import java.io.FileOutputStream;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

public class SeparatedWords2 {

    public static final String RESULT = "results/questions/separators/separated_words.pdf";

    public static final String[] WORDS = { "hippopotamus", "dog", "baboon", "hamster", "moose", "lobster", "ox", "weasel", "platypus", "seal", "spider", "yak", "turtle", "raccoon", "cheetah", "penguin", "deer", "cow", "fox", "shark", "raven", "guinea pig", "rat", "zebra", "snake", "giraffe", "leopard", "mouse", "chicken", "rabbit", "goose", "otter", "hyena", "turkey", "kangaroo", "frog", "rhinoceros", "cat", "duck", "elephant" };

    public static void main(String[] args) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            document.open();
            Phrase p;
            Chunk separator = new Chunk(new LineSeparator(0.5f, 70, Color.RED, Element.ALIGN_CENTER, 3));
            for (int i = 0; i < 40; i++) {
                p = new Phrase("TEST");
                p.add(separator);
                p.add(new Chunk(String.valueOf(i)));
                p.add(separator);
                p.add(new Chunk(String.valueOf(i * 2)));
                p.add(separator);
                p.add(new Chunk(WORDS[39 - i]));
                p.add(separator);
                p.add(new Chunk(String.valueOf(i * 4)));
                p.add(separator);
                p.add(new Chunk(WORDS[i]));
                document.add(p);
                document.add(Chunk.NEWLINE);
            }
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
