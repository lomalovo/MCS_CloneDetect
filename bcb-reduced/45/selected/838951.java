package orcajo.azada.itext.handlers.internal;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.HashMap;
import orcajo.azada.itext.preferences.PDFSetting;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Cell;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.tonbeller.jpivot.table.CellInfo;
import de.kupzog.ktable.KTableModel;

public class OlapTableITextWriter {

    private static Font f_headerCol = null;

    private static Font f_headerRow = null;

    private static Font f_table = null;

    static {
        try {
            BaseFont helveticagr = BaseFont.createFont("ARIAL.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            f_headerCol = new Font(helveticagr, 7, Font.NORMAL);
            f_headerRow = new Font(helveticagr, 7, Font.NORMAL);
            f_table = new Font(helveticagr, 8, Font.NORMAL);
        } catch (Exception e) {
            f_headerCol = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.BOLD);
            f_headerRow = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.BOLD);
            f_table = FontFactory.getFont(FontFactory.HELVETICA, 8);
        }
    }

    private KTableModel tm;

    private File fileOut;

    PDFSetting setting;

    private OlapTableITextWriter(File fileOut, KTableModel kmodel) {
        super();
        this.fileOut = fileOut;
        this.tm = kmodel;
        setting = PDFSetting.getInstance();
    }

    public static Document write(File fileOut, KTableModel kmodel, String title) throws Exception {
        return new OlapTableITextWriter(fileOut, kmodel).buid(title);
    }

    private Document buid(String title) throws Exception {
        Rectangle r = PDFSetting.getInstance().getPageSize();
        Document document = new Document(r);
        if (!PDFSetting.getInstance().isVertical()) {
            document.setPageSize(document.getPageSize().rotate());
        }
        if (fileOut != null) {
            PdfWriter.getInstance(document, new FileOutputStream(fileOut));
        } else {
            throw new Exception("File is null");
        }
        document.open();
        document.addAuthor("LA_Azada");
        document.addSubject("Table");
        if (title != null && !title.trim().equals("")) {
            document.addTitle(title);
        }
        Table table;
        table = buildTable();
        if (table != null) {
            document.add(table);
        }
        document.close();
        return document;
    }

    private Table buildTable() throws Exception {
        int nRows = tm.getRowCount();
        int nRowsHeader = tm.getFixedHeaderRowCount();
        int nCols = tm.getColumnCount();
        int nFixedCols = tm.getFixedHeaderColumnCount();
        Table table = new Table(nCols);
        CellInfo c;
        Object value = null;
        Map<Object, Point> used = new HashMap<Object, Point>();
        for (int i = 0; i < nRowsHeader; i++) {
            for (int j = 0; j < nCols; j++) {
                value = tm.getContentAt(j, i);
                if (!used.containsKey(value)) {
                    Point span = getSpan(value, new Point(j, i));
                    used.put(value, new Point(j, i));
                    Cell cell;
                    Color color = getColor(setting.getHeaderColNormal());
                    if (value != null && value instanceof CellInfo) {
                        c = (CellInfo) value;
                        cell = new Cell(new Phrase(c.getCaption(), f_headerCol));
                        if (setting.isShowHeaderColColor()) {
                            if (CellInfo.Style.EVEN.equals(c.getStyle())) color = getColor(setting.getHeaderColEvent()); else if (CellInfo.Style.SPAN.equals(c.getStyle())) color = getColor(setting.getHeaderColSpan());
                        }
                    } else {
                        String s = (value != null) ? value.toString() : "";
                        cell = new Cell(new Phrase(s, f_headerRow));
                    }
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(color);
                    if (span.x > 1) cell.setColspan(span.x);
                    if (span.y > 1) cell.setRowspan(span.y);
                    table.addCell(cell);
                }
            }
        }
        used.clear();
        for (int i = nRowsHeader; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                Color color = getColor(setting.getHeaderRowNormal());
                c = (CellInfo) tm.getContentAt(j, i);
                Cell cell = null;
                if (j < nFixedCols) {
                    if (!used.containsKey(c)) {
                        used.put(c, new Point(j, i));
                        cell = new Cell(new Phrase(c.getCaption(), f_headerRow));
                        cell.setBorder(Rectangle.TOP | Rectangle.LEFT);
                    } else {
                        cell = new Cell("");
                        Point p = used.get(c);
                        cell.setBorder(Rectangle.NO_BORDER);
                        if (p.y == i) {
                            cell.setBorder(Rectangle.TOP);
                        } else if (p.x == j) {
                            cell.setBorder(cell.getBorder() | Rectangle.LEFT);
                        }
                    }
                    if (setting.isShowHeaderRowColor()) {
                        if (CellInfo.Style.EVEN.equals(c.getStyle())) color = getColor(setting.getHeaderRowEvent()); else if (CellInfo.Style.SPAN.equals(c.getStyle())) color = getColor(setting.getHeaderRowSpan());
                    }
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                } else {
                    cell = new Cell(new Phrase(c.getCaption(), f_table));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    if (setting.isShowHeaderRowColor()) {
                        if (CellInfo.Style.EVEN.equals(c.getStyle())) color = getColor(setting.getHeaderRowEvent());
                        if (CellInfo.Style.EVEN.equals(c.getStyle())) {
                            color = getColor(setting.getHeaderRowEvent());
                        } else if (CellInfo.Style.GREEN.equals(c.getStyle())) {
                            color = Color.green;
                        } else if (CellInfo.Style.RED.equals(c.getStyle())) {
                            color = Color.RED;
                        } else if (CellInfo.Style.YELLOW.equals(c.getStyle())) {
                            color = Color.YELLOW;
                        }
                    }
                }
                cell.setBackgroundColor(color);
                table.addCell(cell);
            }
        }
        used.clear();
        return table;
    }

    private Point getSpan(Object value, Point p) {
        Point result = new Point(1, 1);
        int nRows = tm.getRowCount();
        int nCols = tm.getColumnCount();
        for (int i = p.y + 1; i < nRows; i++) {
            Object actualVal = tm.getContentAt(p.x, i);
            if (value != null) {
                if (!value.equals(actualVal)) break;
            } else {
                if (actualVal != null) break;
            }
            result.y++;
        }
        for (int i = p.x + 1; i < nCols; i++) {
            Object actualVal = tm.getContentAt(i, p.y);
            if (value != null) {
                if (!value.equals(actualVal)) break;
            } else {
                if (actualVal != null) break;
            }
            result.x++;
        }
        return result;
    }

    private Color getColor(RGB rgb) {
        return new Color(rgb.red, rgb.green, rgb.blue);
    }
}
