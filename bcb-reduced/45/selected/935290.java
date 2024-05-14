package com.amwebexpert.experiments;

import java.io.FileOutputStream;
import junit.framework.TestCase;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

public class PdfTesting extends TestCase {

    public void testWrapping() throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("c:/temp/test.pdf"));
        document.open();
        Table table = new Table(2);
        table.setWidths(new int[] { 1, 2 });
        table.setBorder(Table.NO_BORDER);
        table.setPadding(1);
        table.setSpacing(0);
        table.setDefaultCellBorder(Table.NO_BORDER);
        table.setAutoFillEmptyCells(false);
        table.setTableFitsPage(true);
        Font pdfFont = FontFactory.getFont("arial");
        pdfFont.setSize(12);
        table.setDefaultCellBorder(Cell.BOX);
        table.addCell(new Paragraph("Test", pdfFont));
        Paragraph paragraph = new Paragraph("De concert avec les utilisateurs et les pilotes responsables, elle �labore l'ensemble des sp�cifications...", pdfFont);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        Cell pdfCell = new Cell(paragraph);
        pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfCell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(pdfCell);
        document.add(table);
        document.close();
    }
}
