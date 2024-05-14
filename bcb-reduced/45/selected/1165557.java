package com.antilia.export.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Map;
import com.antilia.common.util.ReflectionUtils;
import com.antilia.hibernate.context.IProgressReporter;
import com.antilia.web.beantable.model.IColumnModel;
import com.antilia.web.beantable.model.ITableModel;
import com.antilia.web.export.AbstractExportTask;
import com.antilia.web.navigator.IPageableNavigator;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * 
 *
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com)
 *
 */
public class ExportPdfTask<E extends Serializable> extends AbstractExportTask {

    private IPageableNavigator<E> pageableProvider;

    private ITableModel<E> tableModel;

    private Map<String, String> columnTranslations;

    public ExportPdfTask(IPageableNavigator<E> pageableProvider, ITableModel<E> tableModel, Map<String, String> columnTranslations) {
        super();
        this.pageableProvider = pageableProvider.duplicate();
        this.tableModel = tableModel;
        this.columnTranslations = columnTranslations;
    }

    @Override
    protected void doExport(IProgressReporter progressReporter) throws Exception {
        long total = getTotalTasks();
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(getFile()));
        document.open();
        int columns = tableModel.getColumns();
        float[] widths = new float[columns];
        int index = 0;
        java.util.Iterator<IColumnModel<E>> it = tableModel.getColumnModels();
        while (it.hasNext()) {
            IColumnModel<E> columnModel = it.next();
            widths[index] = columnModel.getWidth();
            index++;
        }
        PdfPTable table = new PdfPTable(widths);
        it = tableModel.getColumnModels();
        while (it.hasNext()) {
            IColumnModel<E> columnModel = it.next();
            PdfPCell cell = new PdfPCell(new Phrase(this.columnTranslations.get(columnModel.getPropertyPath())));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }
        document.add(table);
        for (long i = 1; i <= total; i++) {
            if (progressReporter == null || progressReporter.isCanceled()) break;
            if (progressReporter != null) {
                progressReporter.setCurrentTask(i);
                progressReporter.setMessage("Exporting record " + i + " of " + total);
            }
            Thread.sleep(1);
            table = new PdfPTable(widths);
            E bean = pageableProvider.current();
            pageableProvider.next();
            it = tableModel.getColumnModels();
            while (it.hasNext()) {
                IColumnModel<E> columnModel = it.next();
                Object value = ReflectionUtils.getPropertyValue(bean, columnModel.getPropertyPath());
                if (value != null) table.addCell(value.toString()); else table.addCell("");
            }
            document.add(table);
        }
        document.close();
    }

    @Override
    protected File getExportFile() throws Exception {
        return File.createTempFile("export", ".pdf");
    }

    @Override
    protected long getTotalTasks() {
        return pageableProvider.size();
    }

    @Override
    protected String getIntialMessage() {
        return "Exporting to PDF";
    }
}
