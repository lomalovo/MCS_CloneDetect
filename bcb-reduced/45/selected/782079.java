package net.sf.gateway.mef.viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.sf.gateway.mef.utils.PullXML;
import net.sf.gateway.mef.viewer.loaders.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfCreator {

    /**
     * Logging
     */
    private static final Log LOG = LogFactory.getLog(PdfCreator.class);

    private static final String fullPath = ".." + File.separator + "webapps" + File.separator + "viewer" + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "forms" + File.separator;

    private static final String[] files = { fullPath + "TEMP_IN111.pdf", fullPath + "TEMP_IN112.pdf", fullPath + "TEMP_IN113.pdf", fullPath + "TEMP_IN117.pdf", fullPath + "TEMP_IN119.pdf", fullPath + "TEMP_IN153.pdf", fullPath + "TEMP_IN154.pdf" };

    public static void createPdfs(String docid, String taxyear) {
        try {
            String s = PullXML.getXMLFromDocId(docid, "/xml/submission.xml");
            PdfLoader loader = null;
            switch(new Integer(taxyear)) {
                case 2009:
                    loader = new PdfLoader_2009();
                    break;
                case 2011:
                    loader = new PdfLoader_2011();
                    break;
                default:
                    break;
            }
            LOG.info("Loading PDFs called...");
            HashMap<String, Object> pdfMap = (HashMap<String, Object>) loader.loadPDFs(s, taxyear);
            LOG.info("Creating PDFs called...");
            createTempPDFs();
            LOG.info("filling PDFs called...");
            List<String> fileList = fillPDFs(pdfMap, taxyear);
            LOG.info("merge PDFs called...");
            mergePDFs(fileList);
            LOG.info("Finished!");
        } catch (SQLException e) {
            System.out.println("SQLException occured.");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException occured.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException occured.");
            e.printStackTrace();
        } catch (DocumentException e) {
            System.out.println("DocumentException occured.");
            e.printStackTrace();
        }
    }

    public static void createTempPDFs() throws DocumentException, FileNotFoundException, IOException {
        LOG.info("Creating PDFs ");
        for (int i = 0; i < files.length; i++) {
            LOG.info(files[i]);
            Document document = new Document();
            LOG.info("0");
            PdfWriter.getInstance(document, new FileOutputStream(files[i]));
            LOG.info("1");
            document.open();
            LOG.info("2");
            document.add(new Paragraph("Random Message Goes Here so PDF will happily create itself"));
            LOG.info("3");
            document.close();
            LOG.info("4");
        }
        LOG.info("Finished Creating PDFs");
    }

    public static void mergePDFs(List<String> list) throws FileNotFoundException, DocumentException, IOException {
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(fullPath + "master.pdf"));
        document.open();
        PdfReader reader;
        int n;
        for (int i = 0; i < list.size(); i++) {
            reader = new PdfReader(list.get(i), "Gu7ruc*YAWaStEbr".getBytes());
            n = reader.getNumberOfPages();
            for (int page = 0; page < n; ) {
                copy.addPage(copy.getImportedPage(reader, ++page));
            }
            copy.freeReader(reader);
        }
        document.close();
        copy.close();
    }

    public static void deletePDFs() {
        for (int x = 0; x < files.length; x++) {
            File file = new File(files[x]);
            LOG.info("delete file " + files[x]);
        }
        File file = new File(fullPath + "master.pdf");
    }

    public static List<String> fillPDFs(HashMap<String, Object> pdfMap, String taxyear) throws DocumentException, IOException {
        List<String> fileList = new ArrayList<String>();
        if (pdfMap.get("IN111") != null) {
            TaxForm in111 = (TaxForm) pdfMap.get("IN111");
            in111.fill(fullPath + "TY" + taxyear + "_IN111.pdf", files[0], "gwuser");
            fileList.add(fullPath + "TEMP_IN111.pdf");
        }
        if (pdfMap.get("IN112") != null) {
            TaxForm in112 = (TaxForm) pdfMap.get("IN112");
            in112.fill(fullPath + "TY" + taxyear + "_IN112.pdf", files[1], "gwuser");
            fileList.add(fullPath + "TEMP_IN112.pdf");
        }
        if (pdfMap.get("IN113") != null) {
            TaxForm in113 = (TaxForm) pdfMap.get("IN113");
            in113.fill(fullPath + "TY" + taxyear + "_IN113.pdf", files[2], "gwuser");
            fileList.add(fullPath + "TEMP_IN113.pdf");
        }
        if (pdfMap.get("IN117") != null) {
            TaxForm in117 = (TaxForm) pdfMap.get("IN117");
            in117.fill(fullPath + "TY" + taxyear + "_IN117.pdf", files[3], "gwuser");
            fileList.add(fullPath + "TEMP_IN117.pdf");
        }
        if (pdfMap.get("IN119") != null) {
            TaxForm in119 = (TaxForm) pdfMap.get("IN119");
            in119.fill(fullPath + "TY" + taxyear + "_IN119.pdf", files[4], "gwuser");
            fileList.add(fullPath + "TEMP_IN119.pdf");
        }
        if (pdfMap.get("IN153") != null) {
            TaxForm in153 = (TaxForm) pdfMap.get("IN153");
            in153.fill(fullPath + "TY" + taxyear + "_IN153.pdf", files[5], "gwuser");
            fileList.add(fullPath + "TEMP_IN153.pdf");
        }
        if (pdfMap.get("IN154") != null) {
            TaxForm in154 = (TaxForm) pdfMap.get("IN154");
            in154.fill(fullPath + "TY" + taxyear + "_IN154.pdf", files[6], "gwuser");
            fileList.add(fullPath + "TEMP_IN154.pdf");
        }
        return fileList;
    }
}
