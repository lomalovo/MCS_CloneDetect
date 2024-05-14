package screencompare.screen;

import java.io.FileOutputStream;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

public class ScreenReport {

    private String _filename;

    private int _margin = 30;

    public ScreenReport(String filename) {
        _filename = filename;
    }

    public void create(Screen... screens) {
        try {
            int width = 0, height = 2 * _margin;
            for (Screen screen : screens) {
                if (screen._img.getWidth() > width) {
                    width = screen._img.getWidth();
                }
                height += screen._img.getHeight() + 80;
            }
            width += 2 * _margin;
            Document pdf = new Document(new Rectangle(width, height), _margin, _margin, _margin, _margin);
            PdfWriter.getInstance(pdf, new FileOutputStream(_filename));
            pdf.open();
            for (int i = 0; i < screens.length; i++) {
                if (i > 0) {
                    pdf.add(new Paragraph("\n\n"));
                }
                _addScreen(pdf, screens[i]);
            }
            pdf.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void _addScreen(Document pdf, Screen screen) {
        try {
            if (screen.hasInfo()) {
                StringBuffer info = new StringBuffer();
                if (screen.getInfo().getTest() != null) {
                    info.append("Test: " + screen.getInfo().getTest() + "\n");
                }
                info.append("Screen: " + screen.getInfo().getUrl() + " (");
                if (screen.getInfo().getBrowserName() != null) {
                    info.append(screen.getInfo().getBrowserName() + ", ");
                }
                info.append(screen._img.getWidth() + "x" + screen._img.getHeight());
                info.append(")");
                pdf.add(new Paragraph(info.toString()));
            }
            Image img = Image.getInstance(screen._img, null);
            img.setBorderWidth(1f);
            img.setBorderColor(BaseColor.BLACK);
            img.setBorder(Rectangle.BOX);
            pdf.add(img);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
