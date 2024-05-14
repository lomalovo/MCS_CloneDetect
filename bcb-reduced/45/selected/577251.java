package bouttime.utility.boutsheet;

import bouttime.report.bracketsheet.BracketSheetUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFrame;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 *
 */
public class BoutSheetMaker {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter writer;
            if (args.length >= 1) {
                writer = PdfWriter.getInstance(document, new FileOutputStream(args[0]));
            } else {
                System.err.println("ERROR : Must specify output file.");
                return;
            }
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED);
            float pageWidth = cb.getPdfDocument().getPageSize().getWidth();
            float midPage = pageWidth / 2;
            drawBout(cb, bf, 35, midPage - 35);
            drawBout(cb, bf, midPage + 35, pageWidth - 35);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
            return;
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            return;
        }
        document.close();
        if ((args.length == 3) && (Boolean.parseBoolean(args[2]))) {
            showPDF(args[0]);
        }
    }

    private static void drawBout(PdfContentByte cb, BaseFont bf, float xStart, float xEnd) throws DocumentException, IOException {
        float midRange = xStart + ((xEnd - xStart) / 2);
        float x = xStart;
        float y = 560;
        float widthUnit = (xEnd - xStart) / 4;
        float fontSize = 10;
        String name = "Jeff's Tournament    July 1, 2009";
        BracketSheetUtil.drawString(cb, bf, x, y, fontSize, name);
        float width = widthUnit;
        float height = 20;
        y -= 30;
        float matX = x + (width / 2);
        fontSize = 12;
        BracketSheetUtil.drawStringCentered(cb, bf, matX, y + 5, fontSize, "Mat", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 5, fontSize, "5", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        float boutX = x + (width / 2);
        BracketSheetUtil.drawStringCentered(cb, bf, boutX, y + 5, fontSize, "Bout", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 5, fontSize, "254", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        String groupName = "Open    Div 5    135-140";
        fontSize = 16;
        y -= 25;
        BracketSheetUtil.drawStringCentered(cb, bf, midRange, y, fontSize, groupName, 0);
        y -= 20;
        fontSize = 8;
        BracketSheetUtil.drawStringCentered(cb, bf, midRange, y, fontSize, "Judge's Score Sheet", 0);
        width = width * 2;
        height = 20;
        y -= 23;
        x = xStart;
        float redX = x + (width / 2);
        fontSize = 12;
        BracketSheetUtil.drawStringCentered(cb, bf, redX, y + 5, fontSize, "RED", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        float greenX = x + (width / 2);
        BracketSheetUtil.drawStringCentered(cb, bf, greenX, y + 5, fontSize, "GREEN", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        fontSize = 10;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 4, fontSize, "Chris Wilson");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 4, fontSize, "Mike Elswick");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 4, fontSize, "VR");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 4, fontSize, "WestLake");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        fontSize = 8;
        BracketSheetUtil.drawStringCentered(cb, bf, midRange, y + 6, fontSize, "Points and Cautions", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width * 2, height, 1, 0);
        height = height * 3;
        y -= height;
        x = xStart;
        fontSize = 8;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 1");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 1");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 2");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 2");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 3/OT");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + height - 10, fontSize, "Period 3/OT");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        height = height / 3;
        y -= height;
        x = xStart;
        fontSize = 12;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 5, fontSize, "Total");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 5, fontSize, "Total");
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        height = height * 2;
        y = y - height - 10;
        x = xStart;
        fontSize = 10;
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 15, fontSize, "Winner's");
        BracketSheetUtil.drawString(cb, bf, x + 2, y + 2, fontSize, "Signature:");
        BracketSheetUtil.drawRectangle(cb, x, y, width * 2, height, 1, 0);
        height = height / 2;
        y -= height;
        x = xStart;
        width = width / 2;
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        y -= height;
        x = xStart;
        fontSize = 8;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 8, fontSize, "Fall Time", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 8, fontSize, "Tech Fall Time", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 8, fontSize, "Decision Score", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x += width;
        BracketSheetUtil.drawStringCentered(cb, bf, x + (width / 2), y + 8, fontSize, "Injury Default Time", 0);
        BracketSheetUtil.drawRectangle(cb, x, y, width, height, 1, 0);
        x = xStart;
        y -= 60;
        BracketSheetUtil.drawHorizontalLine(cb, x, y, width * 4, 1, 0);
        y -= 10;
        fontSize = 10;
        BracketSheetUtil.drawString(cb, bf, x, y, fontSize, "Judge's Signature");
    }

    public static void showPDF(String filename) {
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JFrame frame = factory.buildViewerFrame();
        controller.openDocument(filename);
        frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        frame.addWindowListener(new WindowListener() {

            public void windowClosed(WindowEvent arg0) {
            }

            public void windowActivated(WindowEvent arg0) {
            }

            public void windowClosing(WindowEvent arg0) {
            }

            public void windowDeactivated(WindowEvent arg0) {
                System.exit(0);
            }

            public void windowDeiconified(WindowEvent arg0) {
            }

            public void windowIconified(WindowEvent arg0) {
            }

            public void windowOpened(WindowEvent arg0) {
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
