package es.aeat.eett.rubik.export.pdf;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.table.TableModel;
import org.apache.commons.logging.LogFactory;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import es.aeat.eett.rubik.core.cell.RubikCell;
import es.aeat.eett.rubik.tableRubik.MultiHeader;
import es.aeat.eett.rubik.tableRubik.TableRubik;

/**
 * <p>
 * en: Exports the TableRubik to PDF format.
 * </p>
 *
 * <p>
 * es: Esta clase exporta a PDF una TableRubik.
 * </p>
 *
 * @author f00992
 */
class PDFTableRubik implements ModelChangeListener {

    private TableModel dataModel = null;

    private TableModel fixedModel = null;

    private MultiHeader fixedHeader = null;

    private MultiHeader headerData = null;

    private String nameFile = null;

    private static Font f_headerCol = null;

    private static Font f_headerRow = null;

    private static Font f_table = null;

    private Color b_c_HeaderEven = null;

    private Color b_c_HeaderNormal = null;

    private Color b_c_HeaderSpan = null;

    boolean dirty = true;

    private ConfiPdf confiPdf;

    static {
        try {
            BaseFont helveticagr = BaseFont.createFont("ARIAL.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            f_headerCol = new Font(helveticagr, 7, Font.NORMAL);
            f_headerRow = new Font(helveticagr, 7, Font.NORMAL);
            f_table = new Font(helveticagr, 8, Font.NORMAL);
        } catch (Exception e) {
            LogFactory.getLog(PDFTableRubik.class).error(e);
            f_headerCol = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.BOLD);
            f_headerRow = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.BOLD);
            f_table = FontFactory.getFont(FontFactory.HELVETICA, 8);
        }
    }

    protected PDFTableRubik(TableRubik tr, ConfiPdf confiPdf, String nameFile) {
        this.nameFile = nameFile;
        this.confiPdf = confiPdf;
        getTableModels(tr);
    }

    /**
	 * Este el el metodo principal que hay que llamar para crear un pdf.
	 * @param tr
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
    public static Document build(TableRubik tr, ConfiPdf confiPdf) throws FileNotFoundException, DocumentException {
        return new PDFTableRubik(tr, confiPdf, null).build();
    }

    /**
	 * Este el el metodo principal que hay que llamar para crear un pdf.
	 * @param tr
	 * @param confiPdf
	 * @param nameFile
	 * @return Document
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
    public static Document build(TableRubik tr, ConfiPdf confiPdf, String nameFile) throws FileNotFoundException, DocumentException {
        return new PDFTableRubik(tr, confiPdf, nameFile).build();
    }

    private Document build() throws FileNotFoundException, DocumentException {
        Rectangle r = confiPdf.getPaperSizesRectangles()[confiPdf.getPaperSize()];
        if (confiPdf.getOrientation() > 0) r = r.rotate();
        Document document = new Document(r);
        if (nameFile != null) {
            PdfWriter.getInstance(document, new FileOutputStream(nameFile));
        }
        if (!confiPdf.getTitle().trim().equals("")) {
            document.addTitle(confiPdf.getTitle());
        }
        document.addSubject("Export pdf");
        document.open();
        if (!confiPdf.getTitle().trim().equals("")) {
            Phrase tituloPhrase = new Phrase(confiPdf.getTitle(), new Font(Font.TIMES_ROMAN, 14, Font.BOLD));
            document.add(tituloPhrase);
        }
        Table datatable;
        try {
            datatable = buildTable();
        } catch (BadElementException e) {
            document.close();
            return null;
        }
        if (datatable != null) {
            document.add(buildTable());
            document.close();
            return document;
        } else {
            document.close();
            return null;
        }
    }

    private Table buildTable() throws BadElementException {
        int nRowsHeader = (fixedHeader.getRowCount());
        int nRcols = (fixedHeader.getColumnCount() + dataModel.getColumnCount());
        int nRcolsHeaderRows = fixedModel.getColumnCount();
        Table datatable = new Table(nRcols);
        datatable.setWidth(confiPdf.getTableWidth());
        datatable.setPadding(2);
        setColorHeader(confiPdf.isColorearHeaderCol());
        for (int i = 0; i < nRowsHeader; i++) {
            for (int j = 0; j < nRcols; j++) {
                if (dirty) return null;
                RubikCell c = null;
                if (j < nRcolsHeaderRows) {
                    c = (RubikCell) fixedHeader.getValueAt(i, j);
                    if (c != null && fixedHeader.isVisibleValueAt(i, j)) printCellHeaderCol(datatable, c);
                } else {
                    c = (RubikCell) headerData.getValueAt(i, j - nRcolsHeaderRows);
                    if (c != null && headerData.isVisibleValueAt(i, j - nRcolsHeaderRows)) printCellHeaderCol(datatable, c);
                }
            }
        }
        setColorHeader(confiPdf.isColorearHeaderRow());
        for (int i = 0; i < fixedModel.getRowCount(); i++) {
            for (int j = 0; j < nRcols; j++) {
                if (dirty) return null;
                RubikCell c = null;
                if (j < nRcolsHeaderRows) {
                    c = (RubikCell) fixedModel.getValueAt(i, j);
                    if (c != null) printCellHeaderRow(datatable, c);
                } else {
                    c = (RubikCell) dataModel.getValueAt(i, j - nRcolsHeaderRows);
                    if (c != null) printCellData(datatable, c, RubikCell.STYLE_ODD);
                }
            }
        }
        return datatable;
    }

    private void printCellHeaderCol(Table datatable, RubikCell c) throws BadElementException {
        Cell cell = new Cell(new Phrase(c.getCaption(), f_headerCol));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (c.getColspan() > 1) cell.setColspan(c.getColspan());
        if (c.getRowspan() > 1) cell.setRowspan(c.getRowspan());
        Color color = b_c_HeaderNormal;
        if (c.getStyle() == RubikCell.STYLE_EVEN) color = b_c_HeaderEven; else if (c.getStyle() == RubikCell.STYLE_SPAN) color = b_c_HeaderSpan;
        cell.setBackgroundColor(color);
        datatable.addCell(cell);
    }

    private void printCellHeaderRow(Table datatable, RubikCell c) throws BadElementException {
        Cell cell = new Cell(new Phrase(c.getCaption(), f_headerRow));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (c.getColspan() > 1) cell.setColspan(c.getColspan());
        if (c.getRowspan() > 1) cell.setRowspan(c.getRowspan());
        Color color = b_c_HeaderNormal;
        if (c.getStyle() == RubikCell.STYLE_EVEN) color = b_c_HeaderEven; else if (c.getStyle() == RubikCell.STYLE_SPAN) color = b_c_HeaderSpan;
        cell.setBackgroundColor(color);
        datatable.addCell(cell);
    }

    private void printCellData(Table datatable, RubikCell c, int even) throws BadElementException {
        Cell cell = new Cell(new Phrase(c.getCaption(), f_table));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        datatable.addCell(cell);
    }

    private void getTableModels(TableRubik tablaRubik) {
        dirty = false;
        dataModel = tablaRubik.getDataTable().getModel();
        fixedModel = tablaRubik.getFixedTable().getModel();
        fixedHeader = (MultiHeader) tablaRubik.getFixedTable().getTableHeader();
        headerData = (MultiHeader) tablaRubik.getDataTable().getTableHeader();
    }

    private void setColorHeader(boolean colorear) {
        if (colorear) {
            b_c_HeaderEven = new Color(200, 235, 255);
            b_c_HeaderNormal = new Color(240, 250, 240);
            b_c_HeaderSpan = new Color(240, 250, 200);
        } else {
            b_c_HeaderEven = Color.white;
            b_c_HeaderNormal = Color.white;
            b_c_HeaderSpan = Color.white;
        }
    }

    /**
	 * @see com.tonbeller.jpivot.core.ModelChangeListener#modelChanged(com.tonbeller.jpivot.core.ModelChangeEvent)
	 */
    public void modelChanged(ModelChangeEvent e) {
        this.dirty = true;
    }

    /**
	 * @see com.tonbeller.jpivot.core.ModelChangeListener#structureChanged(com.tonbeller.jpivot.core.ModelChangeEvent)
	 */
    public void structureChanged(ModelChangeEvent e) {
        this.dirty = true;
    }
}
