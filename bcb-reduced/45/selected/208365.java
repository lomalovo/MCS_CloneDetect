package bouttime.report.award;

import bouttime.dao.Dao;
import bouttime.mainview.BoutTimeApp;
import bouttime.model.Group;
import bouttime.model.Wrestler;
import bouttime.sort.WrestlerPlaceSort;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

/**
 * A class to generate a report for the awards.
 */
public class AwardReport {

    static Logger logger = Logger.getLogger(AwardReport.class);

    private static String outputDirectory;

    private static String outputFilePath;

    static {
        Application app = Application.getInstance(BoutTimeApp.class);
        ApplicationContext appCtx = app.getContext();
        ResourceMap map = appCtx.getResourceMap(AwardReport.class);
        outputDirectory = map.getString("report.dir");
        outputFilePath = String.format("%s/%s", outputDirectory, map.getString("report.filename"));
        logger.debug(String.format("outputDirectory=%s  outputFilePath=%s", outputDirectory, outputFilePath));
    }

    public static String getoutputFilePath() {
        return outputFilePath;
    }

    /**
     * Generate an award report for a given session.
     * @param dao Dao object to use to retrieve data.
     * @param session Session to generate the report for.
     * @return True if the report was generated.
     */
    public static boolean doBySession(Dao dao, String session) {
        if (session == null) {
            return false;
        }
        return doReport(dao, session, null);
    }

    /**
     * Generate an award report for a given group.
     * @param dao Dao object to use to retrieve data.
     * @param group Group to generate the report for.
     * @return True if the report was generated.
     */
    public static boolean doByGroup(Dao dao, Group group) {
        if (group == null) {
            return false;
        }
        return doReport(dao, null, group);
    }

    /**
     * Generate an award report for the entire tournament.
     * @param dao Dao object to use to retrieve data.
     * @return True if the report was generated.
     */
    public static boolean doAll(Dao dao) {
        return doReport(dao, null, null);
    }

    /**
     * Generate an award report.
     * @param dao Dao object to use to retrieve data.
     * @param session Session to generate the report for.
     * @param group Group to generate report for.  This takes precedence, so
     * if not null, then the report will be generated for this group.
     * @return True if the report was generated.
     */
    private static boolean doReport(Dao dao, String session, Group group) {
        if (!dao.isOpen()) {
            return false;
        }
        Document document = new Document();
        try {
            FileOutputStream fos = createOutputFile();
            if (fos == null) {
                return false;
            }
            PdfWriter.getInstance(document, fos);
            document.open();
            Paragraph p1 = new Paragraph(new Paragraph(String.format("%s    %s %s, %s", dao.getName(), dao.getMonth(), dao.getDay(), dao.getYear()), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(p1);
            Paragraph p2 = new Paragraph(new Paragraph("Award Report", FontFactory.getFont(FontFactory.HELVETICA, 14)));
            p2.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p2);
            Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Font detailFont = new Font(Font.FontFamily.TIMES_ROMAN, 10);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(3);
            headerCell.setBorderWidth(2);
            List<Group> groups;
            if (group != null) {
                groups = new ArrayList<Group>();
                groups.add(group);
            } else if (session != null) {
                groups = dao.getGroupsBySession(session);
            } else {
                groups = dao.getAllGroups();
            }
            for (Group g : groups) {
                PdfPTable datatable = new PdfPTable(4);
                int colWidths[] = { 30, 30, 30, 10 };
                datatable.setWidths(colWidths);
                datatable.setWidthPercentage(100);
                datatable.getDefaultCell().setPadding(3);
                datatable.getDefaultCell().setBorderWidth(2);
                datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPhrase(new Phrase(g.toString(), headerFont));
                headerCell.setColspan(4);
                datatable.addCell(headerCell);
                datatable.setHeaderRows(1);
                datatable.getDefaultCell().setBorderWidth(1);
                datatable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                List<Wrestler> wList = getSortedAwardList(g);
                int i = 0;
                for (Wrestler w : wList) {
                    if ((i++ % 2) == 0) {
                        datatable.getDefaultCell().setGrayFill(0.9f);
                    } else {
                        datatable.getDefaultCell().setGrayFill(1);
                    }
                    datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    datatable.addCell(new Phrase(w.getFirstName(), detailFont));
                    datatable.addCell(new Phrase(w.getLastName(), detailFont));
                    datatable.addCell(new Phrase(w.getTeamName(), detailFont));
                    datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    Integer place = w.getPlace();
                    String placeStr = (place == null) ? "" : place.toString();
                    datatable.addCell(new Phrase(placeStr, detailFont));
                }
                datatable.setSpacingBefore(5f);
                datatable.setSpacingAfter(15f);
                document.add(datatable);
            }
        } catch (DocumentException de) {
            logger.error("Document Exception", de);
            return false;
        }
        document.close();
        return true;
    }

    /**
     * Get the list of wrestlers receiving awards for the given group.
     * The list is sorted by their award (or place).
     * @param g The group to use.
     * @return Sorted list of wrestlers.
     */
    private static List<Wrestler> getSortedAwardList(Group g) {
        List<Wrestler> list = new ArrayList<Wrestler>();
        for (Wrestler w : g.getWrestlers()) {
            Integer p = w.getPlace();
            if ((p != null) && (!p.toString().isEmpty())) {
                list.add(w);
            }
        }
        Collections.sort(list, new WrestlerPlaceSort());
        return list;
    }

    /**
     * Create the file to write the report to.
     * @return FileOutputStream to write the report to.
     */
    private static FileOutputStream createOutputFile() {
        File tmpDir = new File(outputDirectory);
        if (!tmpDir.exists() || !tmpDir.isDirectory()) {
            if (!tmpDir.mkdir()) {
                logger.warn("Unable to create directory : " + tmpDir);
                return null;
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFilePath);
        } catch (java.io.FileNotFoundException ex) {
            logger.error("File Not Found Exception", ex);
            fos = null;
        }
        return fos;
    }
}
