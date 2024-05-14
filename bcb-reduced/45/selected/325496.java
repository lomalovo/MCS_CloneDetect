package org.moviereport.core.export.pdf.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moviereport.core.MDFAssertion;
import org.moviereport.core.export.pdf.ExportPdfConfiguration;
import org.moviereport.core.export.pdf.MovieDescriptionToPdf.MDFToPdfException;
import org.moviereport.core.export.pdf.MovieDescriptionToPdf.MDFToPdfException.ERROR_TYPE;
import org.moviereport.core.model.MovieCollection;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class DVDCover2Sided implements MovieReport {

    private Logger logger = Logger.getLogger(DVDCover2Sided.class.getName());

    private final String reportDescription;

    private final String reportName;

    private final float leftAndRightSideWidth;

    private final float middleWidth;

    /**
	 * 72points = 1inch = 2,54cm => 72/2,54 = 1cm<br>
	 * 12.8cm - 1.4cm - 12.8cm
	 */
    public DVDCover2Sided(String reportDescription, String reportName, float leftAndRightSideWidth, float middleWidth) {
        this.reportDescription = reportDescription;
        this.reportName = reportName;
        this.leftAndRightSideWidth = leftAndRightSideWidth;
        this.middleWidth = middleWidth;
    }

    public InputStream createReport(MovieCollection movieCollection, ExportPdfConfiguration exportConfiguration) throws MDFToPdfException {
        try {
            Document document = new Document(PageSize.A4.rotate(), 0f, 0f, 20f, 0f);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font boxTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 25f, Font.BOLD, CommonReportElements.TITLE_COLOR);
            Font collectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 15f, Font.BOLD, CommonReportElements.TITLE_COLOR);
            Font movieTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 7f, Font.BOLD);
            Font movieDescriptionFont = FontFactory.getFont(FontFactory.HELVETICA, 6f, Font.NORMAL);
            Font movieMetaDataFont = FontFactory.getFont(FontFactory.HELVETICA, 5f, Font.NORMAL, BaseColor.DARK_GRAY);
            FontConfiguration fontConfiguration = new FontConfiguration();
            fontConfiguration.setBoxTitleFont(boxTitleFont);
            fontConfiguration.setCollectionTitleFont(collectionTitleFont);
            fontConfiguration.setMovieDescriptionFont(movieDescriptionFont);
            fontConfiguration.setMovieMetaDataFont(movieMetaDataFont);
            fontConfiguration.setMovieTitleFont(movieTitleFont);
            Paragraph paragraph = new Paragraph();
            paragraph.add(getMainTable(movieCollection, fontConfiguration));
            document.add(paragraph);
            document.close();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
            return byteArrayInputStream;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Could not create the specified File for writing the PDF");
            throw new MDFToPdfException(ERROR_TYPE.ERROR_WRITING_PDF_FILE, e);
        } catch (DocumentException e) {
            logger.log(Level.SEVERE, "Could not create the specified File for writing the PDF");
            throw new MDFToPdfException(ERROR_TYPE.ERROR_WRITING_PDF_FILE, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not create the specified File for writing the PDF");
            throw new MDFToPdfException(ERROR_TYPE.ERROR_WRITING_PDF_FILE, e);
        }
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public String getReportName() {
        return reportName;
    }

    private PdfPTable getMainTable(MovieCollection movieCollection, FontConfiguration fontConfiguration) throws MDFToPdfException, DocumentException {
        MDFAssertion.preConditionNotNull(movieCollection);
        MDFAssertion.preConditionNotNull(movieCollection.getMovieCollections());
        MDFAssertion.preCondition(movieCollection.getMovieCollections().size() > 0);
        PdfPTable table = new PdfPTable(3);
        table.setLockedWidth(true);
        table.setTotalWidth(new float[] { leftAndRightSideWidth, middleWidth, leftAndRightSideWidth });
        PdfPCell leftCell = null;
        PdfPCell rightCell = null;
        if (movieCollection.getMovieCollections().size() > 0) {
            rightCell = new PdfPCell(CommonReportElements.getMovieCollectionTable(movieCollection.getMovieCollections().get(0), fontConfiguration));
        } else {
            rightCell = new PdfPCell(CommonReportElements.getMovieCollectionTable(null, fontConfiguration));
        }
        if (movieCollection.getMovieCollections().size() > 1) {
            leftCell = new PdfPCell(CommonReportElements.getMovieCollectionTable(movieCollection.getMovieCollections().get(1), fontConfiguration));
        } else {
            leftCell = new PdfPCell(CommonReportElements.getMovieCollectionTable(null, fontConfiguration));
        }
        float height = 530f;
        leftCell.setFixedHeight(height);
        leftCell.setBorderColor(CommonReportElements.BORDER_COLOR);
        table.addCell(leftCell);
        PdfPCell middleCell = new PdfPCell(CommonReportElements.getMovieCollectionTitleTable(movieCollection, fontConfiguration));
        middleCell.setFixedHeight(height);
        middleCell.setBorderColor(CommonReportElements.BORDER_COLOR);
        table.addCell(middleCell);
        rightCell.setFixedHeight(height);
        rightCell.setBorderColor(CommonReportElements.BORDER_COLOR);
        table.addCell(rightCell);
        return table;
    }
}
