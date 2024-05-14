package questions.tables;

import java.awt.Color;
import java.io.FileOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class AddTableAsHeaderFooter extends PdfPageEventHelper {

    public static final String RESULT = "results/questions/tables/header_footer_table.pdf";

    public static void main(String[] args) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 100);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
            writer.setPageEvent(new AddTableAsHeaderFooter());
            document.open();
            String text = "Lots of text. ";
            for (int i = 0; i < 5; i++) text += text;
            for (int i = 0; i < 20; i++) document.add(new Paragraph(text));
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    protected PdfTemplate tpl;

    protected BaseFont helv;

    protected PdfPTable headerTable;

    public void onOpenDocument(PdfWriter writer, Document document) {
        try {
            tpl = writer.getDirectContent().createTemplate(150, 18);
            Rectangle rect = new Rectangle(0, 0, 150, 18);
            rect.setBackgroundColor(Color.GRAY);
            tpl.setBoundingBox(rect);
            tpl.rectangle(rect);
            helv = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);
            headerTable = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Paragraph("Header Text"));
            headerTable.addCell(cell);
            headerTable.setTotalWidth(document.right() - document.left());
            headerTable.setLockedWidth(true);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public void onEndPage(PdfWriter writer, Document document) {
        try {
            headerTable.writeSelectedRows(0, -1, document.leftMargin(), document.top() + headerTable.getTotalHeight(), writer.getDirectContent());
            PdfPTable footerTable = new PdfPTable(2);
            PdfPCell cell1 = new PdfPCell(new Phrase("page " + writer.getPageNumber()));
            footerTable.addCell(cell1);
            PdfPCell cell2 = new PdfPCell(Image.getInstance(tpl));
            footerTable.addCell(cell2);
            footerTable.setTotalWidth(document.right() - document.left());
            footerTable.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.getDirectContent());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public void onCloseDocument(PdfWriter writer, Document document) {
        tpl.beginText();
        tpl.setFontAndSize(helv, 12);
        tpl.setTextMatrix(2, 4);
        tpl.showText("Number of pages = " + (writer.getPageNumber() - 1));
        tpl.endText();
    }
}
