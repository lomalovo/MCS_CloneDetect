package com.lowagie.tools.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.tools.arguments.FileArgument;
import com.lowagie.tools.arguments.OptionArgument;
import com.lowagie.tools.arguments.PageSizeArgument;
import com.lowagie.tools.arguments.PdfFilter;
import com.lowagie.tools.arguments.ToolArgument;

/**
 * Converts a monospaced txt file to a PDF file.
 */
public class Txt2Pdf extends AbstractTool {

    /**
	 * Constructs a Tiff2Pdf object.
	 */
    public Txt2Pdf() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW | MENU_EXECUTE_PRINT_SILENT;
        arguments.add(new FileArgument(this, "srcfile", "The file you want to convert", false));
        arguments.add(new FileArgument(this, "destfile", "The file to which the converted text has to be written", true, new PdfFilter()));
        PageSizeArgument oa1 = new PageSizeArgument(this, "pagesize", "Pagesize");
        arguments.add(oa1);
        OptionArgument oa2 = new OptionArgument(this, "orientation", "Orientation of the page");
        oa2.addOption("Portrait", "PORTRAIT");
        oa2.addOption("Landscape", "LANDSCAPE");
        arguments.add(oa2);
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#createFrame()
	 */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Txt2Pdf", true, true, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
    }

    /**
	 * @see com.lowagie.tools.plugins.AbstractTool#execute()
	 */
    public void execute() {
        try {
            String line = null;
            Document document;
            Font f;
            Rectangle pagesize = (Rectangle) getValue("pagesize");
            if ("LANDSCAPE".equals(getValue("orientation"))) {
                f = FontFactory.getFont(FontFactory.COURIER, 10);
                document = new Document(pagesize.rotate(), 36, 9, 36, 36);
            } else {
                f = FontFactory.getFont(FontFactory.COURIER, 11);
                document = new Document(pagesize, 72, 36, 36, 36);
            }
            BufferedReader in = new BufferedReader(new FileReader((File) getValue("srcfile")));
            PdfWriter.getInstance(document, new FileOutputStream((File) getValue("destfile")));
            document.open();
            while ((line = in.readLine()) != null) {
                document.add(new Paragraph(12, line, f));
            }
            document.close();
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
        Txt2Pdf tool = new Txt2Pdf();
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
