package com.eris4.benchdb.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import com.eris4.benchdb.core.util.Resource;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class Printer {

    public static final int LIST_LEADING = 30;

    public static final int PARAGRAPH_SPACE_BEFORE = 20;

    public static final int DATA_SPACE_BEFORE = 30;

    public static final int TITLE_SPACE_BEFORE = 500;

    public static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 36);

    public static final Font CHAPTER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 24, Font.BOLDITALIC);

    public static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);

    ;

    public static final String TITLE = "Database Benchmark Results";

    public static final String INTRO = "This benchmark do..... The aim is ... Some other general information.";

    public static final float IMAGE_WIDTH = 500;

    public void print(List<Test> tests, List<Database> databases) throws FileNotFoundException, DocumentException, URISyntaxException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(Resource.getNewFile("benchmark.pdf")));
        document.open();
        document.addTitle("Database Benchmark Report");
        document.addAuthor("Valerio Barbagallo");
        document.addSubject("A report for the benchmark");
        document.addKeywords("test, benchmark, in-memory, database, main memory, embedded");
        document.addCreator("The benchmark application");
        Paragraph title = new Paragraph(TITLE, TITLE_FONT);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingBefore(TITLE_SPACE_BEFORE);
        Paragraph date = new Paragraph(new Date().toString());
        date.setAlignment(Paragraph.ALIGN_CENTER);
        date.setSpacingBefore(DATA_SPACE_BEFORE);
        document.add(title);
        document.add(date);
        Paragraph intro = new Paragraph(INTRO);
        intro.setSpacingBefore(PARAGRAPH_SPACE_BEFORE);
        document.add(intro);
        document.add(getTestDescription(tests));
        document.add(getDatabaseDescription(databases));
        int chapterNumber = 1;
        for (Test test : tests) {
            Paragraph chapterName = new Paragraph(test.getName(), CHAPTER_FONT);
            Chapter chapter = new Chapter(chapterName, chapterNumber);
            test.print(chapter);
            document.add(chapter);
            chapterNumber++;
        }
        document.close();
    }

    private Element getDatabaseDescription(List<Database> databases) {
        Paragraph result = new Paragraph();
        result.add("The followings are the databases on which the tests have been executed:");
        com.lowagie.text.List list = new com.lowagie.text.List(true, LIST_LEADING);
        for (Database database : databases) {
            list.add(database.getClass().getSimpleName());
        }
        result.add(list);
        result.setSpacingBefore(PARAGRAPH_SPACE_BEFORE);
        return result;
    }

    private Element getTestDescription(List<Test> tests) {
        Paragraph result = new Paragraph();
        result.add("The followings are the tests executed:");
        com.lowagie.text.List list = new com.lowagie.text.List(true, LIST_LEADING);
        for (Test test : tests) {
            list.add(test.getName());
        }
        result.add(list);
        result.setSpacingBefore(PARAGRAPH_SPACE_BEFORE);
        return result;
    }
}
