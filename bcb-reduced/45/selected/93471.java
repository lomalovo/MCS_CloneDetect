package com.lowagie.tools.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPageLabels;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.tools.arguments.FileArgument;
import com.lowagie.tools.arguments.PdfFilter;
import com.lowagie.tools.arguments.ToolArgument;

/**
 * Converts a Tiff file to a PDF file.
 * Inspired by a comp.text.pdf question by Sebastian Schubert
 * and an answer by Hans-Werner Hilse.
 */
public class PhotoAlbum extends AbstractTool {

    static {
        addVersion("$Id: PhotoAlbum.java 2057 2005-11-29 21:05:22Z blowagie $");
    }

    /**
	 * Constructs a PhotoAlbum object.
	 */
    public PhotoAlbum() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, "srcdir", "The directory containing the image files", false));
        arguments.add(new FileArgument(this, "destfile", "The file to which the converted TIFF has to be written", true, new PdfFilter()));
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#createFrame()
	 */
    protected void createFrame() {
        internalFrame = new JInternalFrame("PhotoAlbum", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== PhotoAlbum OPENED ===");
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#execute()
	 */
    public void execute() {
        try {
            if (getValue("srcdir") == null) throw new InstantiationException("You need to choose a source directory");
            File directory = (File) getValue("srcdir");
            if (directory.isFile()) directory = directory.getParentFile();
            if (getValue("destfile") == null) throw new InstantiationException("You need to choose a destination file");
            File pdf_file = (File) getValue("destfile");
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf_file));
            writer.setViewerPreferences(PdfWriter.PageModeUseThumbs);
            PdfPageLabels pageLabels = new PdfPageLabels();
            int dpiX, dpiY;
            float imgWidthPica, imgHeightPica;
            TreeSet images = new TreeSet();
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) images.add(files[i]);
            }
            File image;
            for (Iterator i = images.iterator(); i.hasNext(); ) {
                image = (File) i.next();
                System.out.println("Testing image: " + image.getName());
                try {
                    Image img = Image.getInstance(image.getAbsolutePath());
                    dpiX = img.getDpiX();
                    if (dpiX == 0) dpiX = 72;
                    dpiY = img.getDpiY();
                    if (dpiY == 0) dpiY = 72;
                    imgWidthPica = (72 * img.plainWidth()) / dpiX;
                    imgHeightPica = (72 * img.plainHeight()) / dpiY;
                    img.scaleAbsolute(imgWidthPica, imgHeightPica);
                    document.setPageSize(new Rectangle(imgWidthPica, imgHeightPica));
                    if (document.isOpen()) {
                        document.newPage();
                    } else {
                        document.open();
                    }
                    img.setAbsolutePosition(0, 0);
                    document.add(img);
                    pageLabels.addPageLabel(writer.getPageNumber(), PdfPageLabels.EMPTY, image.getName());
                    System.out.println("Added image: " + image.getName());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            if (document.isOpen()) {
                writer.setPageLabels(pageLabels);
                document.close();
            } else {
                System.err.println("No images were found in directory " + directory.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
        }
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#valueHasChanged(com.lowagie.tools.arguments.ToolArgument)
	 */
    public void valueHasChanged(ToolArgument arg) {
        if (internalFrame == null) {
            return;
        }
    }

    /**
     * Converts a tiff file to PDF.
     * @param args
     */
    public static void main(String[] args) {
        PhotoAlbum tool = new PhotoAlbum();
        if (args.length < 2) {
            System.err.println(tool.getUsage());
        }
        tool.setArguments(args);
        tool.execute();
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#getDestPathPDF()
	 */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue("destfile");
    }
}
