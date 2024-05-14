package com.xenoage.zong.print;

import static com.xenoage.util.error.Err.err;
import static com.xenoage.util.iterators.It.it;
import java.awt.Graphics2D;
import java.io.OutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.xenoage.util.Units;
import com.xenoage.util.error.ErrorLevel;
import com.xenoage.util.iterators.It;
import com.xenoage.util.logging.Log;
import com.xenoage.util.math.Size2f;
import com.xenoage.zong.layout.Layout;
import com.xenoage.zong.layout.Page;
import com.xenoage.zong.renderer.RenderingFormat;
import com.xenoage.zong.renderer.Voc;
import com.xenoage.zong.renderer.context.AWTGraphicsContext;
import com.xenoage.zong.renderer.targets.AWTPageLayoutRenderer;

/**
 * This class allows the user to print out
 * the current score into a PDF file.
 * 
 * The printing functions are the same as for printing out with
 * Java2D, but the target is iText instead of the printer driver.
 *
 * @author Andreas Wenger
 */
public final class PDFPrinter {

    /**
   * Prints the given {@link Layout} into the given PDF output stream.
   */
    public static void print(Layout layout, OutputStream out) {
        Document document = new Document();
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, out);
        } catch (Exception e) {
            err().report(ErrorLevel.Warning, Voc.Error_CouldNotSaveDocument);
        }
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        AWTPageLayoutRenderer renderer = AWTPageLayoutRenderer.getInstance();
        It<Page> pages = it(layout.pages);
        for (Page page : pages) {
            Size2f pageSize = page.format.size;
            float width = Units.mmToPx(pageSize.width, 1);
            float height = Units.mmToPx(pageSize.height, 1);
            document.newPage();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
            Log.message("Printing page " + pages.getIndex() + "...");
            renderer.paint(layout, pages.getIndex(), new AWTGraphicsContext(g2d, 1f, RenderingFormat.Vector));
            g2d.dispose();
            cb.addTemplate(tp, 0, 0);
        }
        document.close();
    }
}
