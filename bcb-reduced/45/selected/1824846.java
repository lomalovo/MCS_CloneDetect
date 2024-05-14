package bouttime.report.bracketsheet;

import bouttime.configuration.PositionOnPage;
import bouttime.dao.Dao;
import bouttime.mainview.BoutTimeApp;
import bouttime.model.Group;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.apache.log4j.Logger;

/**
 * A class to generate a PDF file of Bracket Sheets.
 */
public class BracketSheetReport {

    static Logger logger = Logger.getLogger(BracketSheetReport.class);

    private static final String outputDirectory;

    private static final String outputFilePath;

    private static final int PAGE_ERROR = 0;

    private static final int PAGE_BRACKET = 1;

    private static final int PAGE_ROUNDROBIN = 2;

    static {
        Application app = Application.getInstance(BoutTimeApp.class);
        ApplicationContext appCtx = app.getContext();
        ResourceMap map = appCtx.getResourceMap(BracketSheetReport.class);
        outputDirectory = map.getString("report.dir");
        outputFilePath = String.format("%s/%s", outputDirectory, map.getString("report.filename"));
    }

    public static String getoutputFilePath() {
        return outputFilePath;
    }

    public static boolean generateReport(Dao dao, List<Group> list, boolean doBoutNumbers, boolean doTimestamp) {
        return generateReport(dao, list, null, doBoutNumbers, doTimestamp);
    }

    public static boolean generateReport(Dao dao, List<Group> list, String outputFile, boolean doBoutNumbers, boolean doTimestamp) {
        if (list.isEmpty()) {
            return false;
        }
        Document document = new Document();
        try {
            FileOutputStream fos = createOutputFile(outputFile);
            if (fos == null) {
                return false;
            }
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            String timestamp = "";
            if (doTimestamp) {
                timestamp = DateFormat.getInstance().format(new Date());
            }
            int rv;
            int i = 0;
            int size = list.size();
            for (Group g : list) {
                rv = addBracket(cb, dao, g, doBoutNumbers);
                if (rv != PAGE_ERROR) {
                    boolean doWatermark = false;
                    String gClass = g.getClassification();
                    String wmValues = dao.getBracketsheetWatermarkValues();
                    if ((wmValues != null) && !wmValues.isEmpty()) {
                        String[] tokens = wmValues.split(",");
                        for (String s : tokens) {
                            if (s.trim().equalsIgnoreCase(gClass)) {
                                doWatermark = true;
                                break;
                            }
                        }
                    }
                    int rotation = (rv == PAGE_ROUNDROBIN) ? 45 : 135;
                    if (doWatermark) {
                        PdfContentByte ucb = writer.getDirectContentUnder();
                        BaseFont helv = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);
                        ucb.saveState();
                        ucb.setColorFill(BaseColor.LIGHT_GRAY);
                        ucb.beginText();
                        ucb.setFontAndSize(helv, 86);
                        ucb.showTextAligned(Element.ALIGN_CENTER, gClass, document.getPageSize().getWidth() / 2, document.getPageSize().getHeight() / 2, rotation);
                        ucb.endText();
                        ucb.restoreState();
                    }
                    if (doTimestamp) {
                        rotation -= 45;
                        float width = cb.getPdfWriter().getPageSize().getWidth();
                        int x = (rv == PAGE_ROUNDROBIN) ? 15 : (int) (width - 15);
                        int y = 15;
                        BracketSheetUtil.drawTimestamp(cb, null, x, y, 10, timestamp, rotation);
                    }
                    if (!doBoutNumbers && (dao.getBracketsheetAwardImage() != null) && !dao.getBracketsheetAwardImage().isEmpty()) {
                        Image image = Image.getInstance(Image.getInstance(dao.getBracketsheetAwardImage()));
                        image.setRotationDegrees((rv == PAGE_ROUNDROBIN) ? 0 : 90);
                        PositionOnPage positionOnPage = dao.getBracketsheetAwardImagePosition();
                        if (PositionOnPage.UPPER_RIGHT == positionOnPage) {
                            float x = (rv == PAGE_ROUNDROBIN) ? document.getPageSize().getWidth() - 10 - image.getWidth() : 10;
                            float y = document.getPageSize().getHeight() - 10 - image.getHeight();
                            image.setAbsolutePosition(x, y);
                            cb.addImage(image);
                        } else if (PositionOnPage.CENTER == positionOnPage) {
                            PdfContentByte ucb = writer.getDirectContentUnder();
                            float pageX = document.getPageSize().getWidth() / 2;
                            float pageY = document.getPageSize().getHeight() / 2;
                            float imageX = image.getWidth() / 2;
                            float imageY = image.getHeight() / 2;
                            image.setAbsolutePosition(pageX - imageX, pageY - imageY);
                            ucb.addImage(image);
                        }
                    }
                    if (++i < size) {
                        document.newPage();
                    }
                }
            }
        } catch (DocumentException de) {
            logger.error("Document Exception", de);
            return false;
        } catch (IOException ioe) {
            logger.error("IO Exception", ioe);
            return false;
        }
        document.close();
        return true;
    }

    private static FileOutputStream createOutputFile(String outputFile) {
        if (outputFile == null) {
            File tmpDir = new File(outputDirectory);
            if (!tmpDir.exists() || !tmpDir.isDirectory()) {
                if (!tmpDir.mkdir()) {
                    logger.warn("Unable to create directory : " + tmpDir);
                    return null;
                }
            }
        }
        FileOutputStream fos = null;
        try {
            String filename = (outputFile != null) ? outputFile : outputFilePath;
            fos = new FileOutputStream(filename);
        } catch (java.io.FileNotFoundException ex) {
            logger.error("File Not Found Exception", ex);
            fos = null;
        }
        return fos;
    }

    private static int addBracket(PdfContentByte cb, Dao dao, Group g, boolean doBoutNumbers) {
        Boolean roundRobinEnabled = dao.isRoundRobinEnabled();
        Integer roundRobinMax = dao.getRoundRobinMax();
        Boolean pageOK = false;
        int rv = PAGE_ERROR;
        switch(g.getNumWrestlers()) {
            case 1:
                Bracket2BracketSheetReport r2 = new Bracket2BracketSheetReport();
                pageOK = r2.doPage(cb, dao, g, doBoutNumbers);
                if (pageOK) {
                    rv = PAGE_ROUNDROBIN;
                }
                break;
            case 2:
                if (roundRobinEnabled && (roundRobinMax >= 2)) {
                    pageOK = RoundRobinBracketSheetReport.doPage(cb, dao, g);
                    if (pageOK) {
                        rv = PAGE_ROUNDROBIN;
                    }
                } else {
                    Bracket2BracketSheetReport report2 = new Bracket2BracketSheetReport();
                    pageOK = report2.doPage(cb, dao, g, doBoutNumbers);
                    if (pageOK) {
                        rv = PAGE_ROUNDROBIN;
                    }
                }
                break;
            case 3:
                if (roundRobinEnabled && (roundRobinMax >= 3)) {
                    pageOK = RoundRobinBracketSheetReport.doPage(cb, dao, g);
                    if (pageOK) {
                        rv = PAGE_ROUNDROBIN;
                    }
                } else {
                    Bracket4BracketSheetReport report4 = new Bracket4BracketSheetReport();
                    pageOK = report4.doPage(cb, dao, g, doBoutNumbers);
                    if (pageOK) {
                        rv = PAGE_BRACKET;
                    }
                }
                break;
            case 4:
                if (roundRobinEnabled && (roundRobinMax >= 4)) {
                    pageOK = RoundRobinBracketSheetReport.doPage(cb, dao, g);
                    if (pageOK) {
                        rv = PAGE_ROUNDROBIN;
                    }
                } else {
                    Bracket4BracketSheetReport report4 = new Bracket4BracketSheetReport();
                    pageOK = report4.doPage(cb, dao, g, doBoutNumbers);
                    if (pageOK) {
                        rv = PAGE_BRACKET;
                    }
                }
                break;
            case 5:
                if (roundRobinEnabled && (roundRobinMax >= 5)) {
                    pageOK = RoundRobinBracketSheetReport.doPage(cb, dao, g);
                    if (pageOK) {
                        rv = PAGE_ROUNDROBIN;
                    }
                } else {
                    Bracket8BracketSheetReport report8 = new Bracket8BracketSheetReport();
                    pageOK = report8.doPage(cb, dao, g, doBoutNumbers);
                    if (pageOK) {
                        rv = PAGE_BRACKET;
                    }
                }
                break;
            case 6:
            case 7:
            case 8:
                Bracket8BracketSheetReport report8 = new Bracket8BracketSheetReport();
                pageOK = report8.doPage(cb, dao, g, doBoutNumbers);
                if (pageOK) {
                    rv = PAGE_BRACKET;
                }
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                Bracket16BracketSheetReport report16 = new Bracket16BracketSheetReport();
                pageOK = report16.doPage(cb, dao, g, doBoutNumbers);
                if (pageOK) {
                    rv = PAGE_BRACKET;
                }
                break;
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                Bracket32BracketSheetReport report32 = new Bracket32BracketSheetReport();
                pageOK = report32.doPage(cb, dao, g, doBoutNumbers);
                if (pageOK) {
                    rv = PAGE_BRACKET;
                }
                break;
            default:
                logger.warn("BracketSheetReport : unsupported number " + "of wrestlers (" + g.getNumWrestlers() + ")");
                break;
        }
        return rv;
    }
}
