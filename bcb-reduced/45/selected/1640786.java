package com.diyfever.gui.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import com.diyfever.gui.IDrawingProvider;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Utility class that handles image exports to:
 * 
 * <ul>
 * <li>PDF</li>
 * <li>PNG</li>
 * <li>Printer</li>
 * </ul>
 * 
 * @author Branislav Stojkovic
 */
public class DrawingExporter {

    private static final double margin = 0;

    private static final int PDF_RESOLUTION = 72;

    private static final int BITMAP_SCALE_FACTOR = 3;

    private static final int SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();

    private static DrawingExporter instance;

    public static DrawingExporter getInstance() {
        if (instance == null) {
            instance = new DrawingExporter();
        }
        return instance;
    }

    private DrawingExporter() {
    }

    /**
	 * Prints the image and scales it down if needed.
	 * 
	 * @param plugInPort
	 * @throws PrinterException
	 */
    public void print(final IDrawingProvider plugInPort) throws PrinterException {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new Printable() {

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return (NO_SUCH_PAGE);
                } else {
                    Graphics2D g2d = (Graphics2D) graphics;
                    Dimension d = plugInPort.getSize();
                    double pageRatio = pageFormat.getWidth() / pageFormat.getHeight();
                    double imageRatio = d.getWidth() / d.getHeight();
                    double scale;
                    if (imageRatio > pageRatio) {
                        scale = (pageFormat.getWidth() - 2 * margin) / d.getWidth();
                    } else {
                        scale = (pageFormat.getHeight() - 2 * margin) / d.getHeight();
                    }
                    if (scale > 1) {
                        scale = 1d;
                    }
                    g2d.translate(pageFormat.getImageableX() + margin, pageFormat.getImageableY() + margin);
                    g2d.setFont(new Font("Tahoma", Font.PLAIN, 6));
                    FontMetrics metrics = g2d.getFontMetrics();
                    g2d.setColor(Color.gray);
                    if (scale < 1) {
                        String warningStr = "Note: image has been scaled down to fit the page.";
                        g2d.drawString(warningStr, 0, (int) (d.getHeight() * scale + metrics.getHeight()));
                    }
                    g2d.scale(scale, scale);
                    plugInPort.draw(g2d);
                    return (PAGE_EXISTS);
                }
            }
        });
        if (printJob.printDialog()) {
            printJob.print();
        }
    }

    /**
	 * Creates a PDF in the same size as the project.
	 * 
	 * @param plugInPort
	 * @param file
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 */
    public void exportPDF(IDrawingProvider plugInPort, File file) throws FileNotFoundException, DocumentException {
        Dimension d = plugInPort.getSize();
        float factor = 1f * PDF_RESOLUTION / SCREEN_RESOLUTION;
        float totalWidth = (float) (factor * (2 * margin + d.getWidth()));
        float totalHeight = (float) (factor * (2 * margin + d.getHeight()));
        Document document = new Document(new com.lowagie.text.Rectangle(totalWidth, totalHeight));
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(totalWidth, totalHeight);
        Graphics2D g2d = template.createGraphics((float) (factor * d.getWidth()), (float) (factor * d.getHeight()), new DefaultFontMapper());
        g2d.scale(factor, factor);
        plugInPort.draw(g2d);
        g2d.dispose();
        contentByte.addTemplate(template, (float) margin, (float) margin);
        document.close();
    }

    /**
	 * Renders the project into a PNG file.
	 * 
	 * @param plugInPort
	 * @param file
	 */
    public void exportPNG(IDrawingProvider plugInPort, File file) {
        try {
            Dimension d = plugInPort.getSize();
            BufferedImage image = new BufferedImage((int) d.getWidth() * BITMAP_SCALE_FACTOR, (int) d.getHeight() * BITMAP_SCALE_FACTOR, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.scale(BITMAP_SCALE_FACTOR, BITMAP_SCALE_FACTOR);
            plugInPort.draw(g2d);
            g2d.dispose();
            ImageIO.write(image, "PNG", file);
        } catch (Exception e) {
            System.out.println("Error exporting: " + e);
        }
    }
}
