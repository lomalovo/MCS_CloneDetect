package de.beas.explicanto.client.rcp.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import de.bea.services.vidya.client.datasource.UserNames;
import de.beas.explicanto.client.ExplicantoClientPlugin;
import de.beas.explicanto.client.rcp.dialogs.ExplicantoMessageDialog;
import de.beas.explicanto.client.rcp.editor.MainEditor;
import de.beas.explicanto.version.ExplicantoVersion;

/**
 * 
 * ExportPDFAction
 *
 * @author alexandru.georgescu
 * @version 1.0
 *
 */
public class ExportPDFAction extends GenericAction implements IRunnableWithProgress {

    protected Document pdfDoc = null;

    protected PdfWriter writer;

    protected File tempFile;

    protected Image theImage;

    protected MainEditor editor;

    private PDFPageListener pageListener = new PDFPageListener();

    private String fileName;

    public void run(IAction action) {
        editor = (MainEditor) window.getActivePage().getActiveEditor();
        String filtrer[] = { "*.pdf" };
        FileDialog exportDlg = new FileDialog(window.getShell(), SWT.SAVE);
        exportDlg.setText(translate("dialog.exportPDF.title"));
        exportDlg.setFilterExtensions(filtrer);
        fileName = null;
        File file = null;
        boolean ok = false;
        do {
            fileName = exportDlg.open();
            if (fileName == null) return;
            file = new File(fileName);
            if (file.exists()) ok = askForOverwrite(); else ok = true;
        } while (!ok);
        try {
            window.run(true, false, this);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * Writes the pdf file to the specified path
     *
     * @param fileName
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    protected void writePDF(String fileName) throws FileNotFoundException, DocumentException, Exception {
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                theImage = getModelImage();
            }
        });
        if (theImage == null) throw new Exception("Error creating model image");
        if (theImage.width() <= theImage.height()) pdfDoc = new Document(PageSize.A4); else pdfDoc = new Document(PageSize.A4.rotate());
        writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(fileName));
        pdfDoc.addAuthor(UserNames.getLoggedInUser().getUsername());
        String creator = translate("aboutDlg.blurb");
        creator = creator.substring(0, creator.indexOf("\n")).trim();
        creator += " " + ExplicantoVersion.getVersion() + " " + ExplicantoVersion.getBuild();
        pdfDoc.addCreator(creator);
        pdfDoc.addProducer();
        pdfDoc.addTitle(editor.getPartName());
        writer.setPageEvent(pageListener);
        pdfDoc.open();
        createTitlePage();
        createModelLayoutPage();
        pdfDoc.close();
        tempFile.delete();
    }

    /**
     * Creates the title page
     *  
     * @throws DocumentException
     */
    private void createTitlePage() throws DocumentException {
        pdfDoc.newPage();
        String title = editor.getPartName();
        Paragraph titleprgf = new Paragraph("\n\n" + title, new Font(Font.TIMES_ROMAN, 48, Font.BOLD));
        titleprgf.setAlignment(Paragraph.ALIGN_CENTER);
        pdfDoc.add(titleprgf);
    }

    private void createModelLayoutPage() throws DocumentException {
        pdfDoc.newPage();
        Rectangle pageSize = pdfDoc.getPageSize();
        float realWidht = pageSize.width() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
        float realHeight = pageSize.height() - pdfDoc.topMargin() - pdfDoc.bottomMargin() - 50;
        if ((theImage.width() > realWidht) || (theImage.height() > realHeight)) {
            theImage.scaleToFit(realWidht, realHeight);
        }
        float sw = theImage.scaledWidth();
        float xPosition = pdfDoc.leftMargin();
        if (sw < realWidht) xPosition += (realWidht - sw) / 2;
        PdfPTable imgTable = new PdfPTable(1);
        PdfPCell imgCell = new PdfPCell(theImage);
        imgCell.setBorder(PdfPCell.NO_BORDER);
        imgCell.setPadding(0);
        imgTable.addCell(imgCell);
        imgTable.setTotalWidth(theImage.scaledWidth());
        imgTable.writeSelectedRows(0, 1, xPosition, pageSize.height() - pdfDoc.topMargin() - 15, writer.getDirectContent());
    }

    public Image getModelImage() {
        ImageData iData = editor.getJPEGDiagram();
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { iData };
        tempFile = new File("/temp.jpeg");
        try {
            loader.save(new FileOutputStream(tempFile), SWT.IMAGE_JPEG);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return null;
        }
        try {
            Image image = Image.getInstance(tempFile.getAbsolutePath());
            return image;
        } catch (BadElementException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Asks the user if he wants to overwrite the file
     *
     * @return
     */
    private boolean askForOverwrite() {
        return ExplicantoMessageDialog.openQuestion(window.getShell(), translate("misc.fileAlreadyExists"));
    }

    protected String getTranslatedName() {
        return translate("actions.exportPDF.title");
    }

    protected String getTranslatedToolTip() {
        return translate("actions.exportPDF.tooltip");
    }

    private class PDFPageListener extends PdfPageEventHelper {

        /**
         * This method is called by the PdfWrites for every page and writes the
         * header and footer
         */
        public void onEndPage(PdfWriter writer, Document document) {
            System.out.println("Page end");
            Rectangle page = document.getPageSize();
            PdfPTable head = new PdfPTable(1);
            head.setWidthPercentage(100);
            Image headImg = null;
            try {
                headImg = null;
                Image.getInstance("\\icons\\logotemp.png");
                System.out.println("The image" + headImg.width() + " " + headImg.height());
            } catch (BadElementException e1) {
            } catch (MalformedURLException e1) {
            } catch (IOException e1) {
                headImg = null;
            }
            if (headImg == null) {
                try {
                    headImg = Image.getInstance("\\plugins\\CSDEApplication_1.0.0\\icons\\logotemp.png");
                    System.out.println("The image" + headImg.width() + " " + headImg.height());
                } catch (BadElementException e1) {
                } catch (MalformedURLException e1) {
                } catch (IOException e1) {
                    headImg = null;
                }
            }
            PdfPCell headCell = new PdfPCell();
            headCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            headCell.addElement(headImg);
            headCell.setBorder(PdfPCell.NO_BORDER);
            head.addCell(headCell);
            head.setTotalWidth(100);
            head.writeSelectedRows(0, -1, document.leftMargin(), page.height() - document.topMargin() + 20, writer.getDirectContent());
            PdfPTable foot = new PdfPTable(1);
            PdfPCell cell = new PdfPCell();
            Paragraph footPrgf = new Paragraph(translate("pdfExport.footer"), new Font(Font.TIMES_ROMAN, 10, Font.NORMAL));
            footPrgf.setAlignment(Paragraph.ALIGN_CENTER);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.addElement(footPrgf);
            foot.addCell(cell);
            foot.setTotalWidth(page.width() - document.leftMargin() - document.rightMargin());
            foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() + 5, writer.getDirectContent());
        }
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            monitor.beginTask(translate("pdfExport.message"), 7);
            monitor.worked(4);
            writePDF(fileName);
            monitor.worked(6);
            monitor.done();
        } catch (Exception e) {
            monitor.done();
            ExplicantoClientPlugin.handleException(e, null);
        }
    }
}
