package com.divosa.eformulieren.web.core.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import com.divosa.eformulieren.util.helper.FileHelper;
import com.divosa.eformulieren.web.core.exception.EFormulierenException;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class ITextPDFMerge {

    private static Logger LOGGER = Logger.getLogger(ITextPDFMerge.class);

    public static void concatPDFs(List<CustomPdfReader> readers, OutputStream outputStream, boolean paginate) {
        Document document = new Document();
        try {
            int totalPages = 0;
            Iterator<CustomPdfReader> iteratorPDFs = readers.iterator();
            while (iteratorPDFs.hasNext()) {
                PdfReader pdf = iteratorPDFs.next();
                totalPages += pdf.getNumberOfPages();
            }
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            int currentPageNumber = 0;
            int pageOfCurrentReaderPDF = 0;
            Iterator<CustomPdfReader> iteratorPDFReader = readers.iterator();
            while (iteratorPDFReader.hasNext()) {
                CustomPdfReader pdfReader = iteratorPDFReader.next();
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;
                    page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                    cb.addTemplate(page, 0, 0);
                    if (paginate) {
                        cb.beginText();
                        cb.setFontAndSize(bf, 9);
                        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + currentPageNumber + " of " + totalPages, 520, 5, 0);
                        cb.endText();
                    }
                    if (pdfReader.getTemplateFile() != null) {
                        PdfImportedPage headerPage = writer.getImportedPage(pdfReader.getTemplateFile(), 1);
                        writer.getDirectContentUnder().addTemplate(headerPage, 0, 0);
                    }
                }
                pageOfCurrentReaderPDF = 0;
            }
            outputStream.flush();
            document.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) document.close();
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static CustomPdfReader loadPdf(String filename) throws IOException, EFormulierenException {
        CustomPdfReader reader = null;
        if (filename != null && !filename.isEmpty()) {
            InputStream is = FileHelper.loadInputStreamFromClasspathInJar(filename + ".pdf");
            if (is != null) {
                reader = new CustomPdfReader(is);
            } else {
                throw new EFormulierenException("Bestand met naam " + filename + " niet gevonden.");
            }
        }
        return reader;
    }
}
