package pdf;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import objects.KanjiCard;
import reading.ReadingFile;
import sql.ReaderSQL;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import config.Config;

public class ToKanjiPDF {

    private Document document = null;

    BaseFont baseJapFont = null;

    Font kanjiFont = null;

    Font furiganaFont = null;

    Font[] japFonts = null;

    Font normalFont = null;

    LinkedList<PdfPCell> rectoCells = null;

    LinkedList<PdfPCell> versoCells = null;

    LinkedList<PdfPCell> newVersoCells = null;

    public ToKanjiPDF() {
        rectoCells = new LinkedList<PdfPCell>();
        versoCells = new LinkedList<PdfPCell>();
        newVersoCells = new LinkedList<PdfPCell>();
        document = new Document(PageSize.A4);
        document.setMargins(0, 0, 0, 0);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(Config.PATH_TO_OUTPUT_FILES + Config.NAME_OF_KANJI_PDF_FILE));
            baseJapFont = BaseFont.createFont(Config.PATH_TO_JAP_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            normalFont = new Font(Font.HELVETICA, 14);
            kanjiFont = new Font(baseJapFont, Config.KANJI_FONT_SIZE);
            furiganaFont = new Font(baseJapFont, 14);
            japFonts = new Font[Config.MAX_SIZES.length];
            for (int i = 0; i < japFonts.length; i++) {
                japFonts[i] = new Font(baseJapFont, Config.MAX_SIZES[i]);
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (DocumentException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Lance la production d'un fichier PDF.
	 * @param args pas utilisé
	 */
    public static void main(String[] args) {
        ToKanjiPDF tkpdf = new ToKanjiPDF();
        tkpdf.writeKanjiCards();
    }

    /**
	 * Produit un PDF à partir d'une liste de kanji et leurs définitions sur une ligne.
	 */
    private void writeKanji() {
        document.open();
        try {
            ReadingFile readMachine = new ReadingFile();
            readMachine.readKanji();
            LinkedList<String> words = readMachine.getFinalList();
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            boolean nowKanji = false;
            short count = 0;
            for (String str : words) {
                if (count % 9 == 0) {
                    count = 0;
                    nowKanji = !nowKanji;
                }
                Paragraph para = null;
                if (nowKanji) {
                    para = new Paragraph(str, kanjiFont);
                } else {
                    str = str.replaceAll("\\|", "\n\n");
                    para = new Paragraph(str, furiganaFont);
                }
                PdfPCell cell = new PdfPCell(para);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setFixedHeight((float) (842.0 / 3));
                table.addCell(cell);
                count++;
            }
            document.add(table);
            document.close();
            System.out.println("Finished !");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Produit un PDF à partir d'une liste de {@link objects.KanjiCard}.
	 */
    private void writeKanjiCards() {
        ReaderSQL rsql = new ReaderSQL();
        rsql.readKanjiFromDB();
        LinkedList<KanjiCard> kanjiCardList = rsql.getKanjiCardsList();
        document.open();
        createCells(kanjiCardList);
        invertCells();
        PdfPTable table = addCells();
        try {
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        System.out.println("Finished !");
    }

    /**
	 * Remplit les deux listes recto et verso.
	 * @param kanjiCardList
	 */
    private void createCells(LinkedList<KanjiCard> kanjiCardList) {
        for (KanjiCard kc : kanjiCardList) {
            Paragraph recto = null;
            Paragraph verso = null;
            recto = new Paragraph(kc.getKanji(), kanjiFont);
            System.out.println(kc.getKanji());
            verso = new Paragraph();
            String kcKunyomi = kc.getKunyomi();
            String kcOnyomi = kc.getOnyomi();
            Chunk kunyomi = new Chunk();
            Chunk onyomi = new Chunk();
            if (kcKunyomi != null) {
                kunyomi = new Chunk(kcKunyomi.replaceAll("\\|", "     ") + "\n\n", furiganaFont);
            }
            if (kcOnyomi != null) {
                onyomi = new Chunk(kc.getOnyomi().replaceAll("\\|", "    ") + "\n\n", furiganaFont);
            }
            switch(kc.getMostUsed()) {
                case Config.MOST_USED_KUNYOMI:
                    kunyomi.setBackground(Color.LIGHT_GRAY);
                    verso.add(kunyomi);
                    verso.add(onyomi);
                    break;
                case Config.MOST_USED_ONYOMI:
                    onyomi.setBackground(Color.LIGHT_GRAY);
                    verso.add(onyomi);
                    verso.add(kunyomi);
                    break;
                case Config.MOST_USED_BOTH:
                    kunyomi.setBackground(Color.LIGHT_GRAY);
                    onyomi.setBackground(Color.LIGHT_GRAY);
                    verso.add(kunyomi);
                    verso.add(onyomi);
                    break;
                default:
                    verso.add(kunyomi);
                    verso.add(onyomi);
                    break;
            }
            verso.add(new Chunk(kc.getDefinition() + "\n\n\n", normalFont));
            String[] examples = kc.getExamples().split("） |\\|");
            for (int i = 0; i < examples.length; i++) {
                Chunk chunk = null;
                if (i % 2 == 0) {
                    chunk = new Chunk(examples[i] + "） ", furiganaFont);
                } else {
                    chunk = new Chunk(examples[i] + "\n\n", normalFont);
                }
                verso.add(chunk);
            }
            rectoCells.add(new KanjiCell(recto));
            versoCells.add(new KanjiCell(verso));
        }
    }

    /**
	 * Inverse les premières et troisièmes colonnes.
	 */
    private void invertCells() {
        int reste = versoCells.size() % 9;
        if (reste != 0) {
            for (int i = 0; i < 9 - reste; i++) {
                rectoCells.add(new KanjiCell());
                versoCells.add(new KanjiCell());
            }
        }
        while (newVersoCells.size() < versoCells.size()) {
            for (int i = 0; i < versoCells.size(); i = i + 9) {
                for (int j = i + 2; j < i + 9; j = j + 3) {
                    for (int k = j; k > j - 3; k--) {
                        newVersoCells.add(versoCells.get(k));
                    }
                }
            }
        }
    }

    /**
	 * Ajoute les cellules à un tableau.
	 * @return le tableau avec toutes les cellules.
	 */
    private PdfPTable addCells() {
        boolean nowKanji = false;
        short count = 0;
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        while (!newVersoCells.isEmpty()) {
            if (count % 9 == 0) {
                count = 0;
                nowKanji = !nowKanji;
            }
            if (nowKanji) {
                table.addCell(rectoCells.removeFirst());
            } else {
                table.addCell(newVersoCells.removeFirst());
            }
            count++;
        }
        return table;
    }
}
