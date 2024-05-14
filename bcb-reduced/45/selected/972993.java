package com.isa.jump.plugin;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.InstallScaleBarPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;

/**
 * This class installs a File->Print menu option and
 * performs basic vector or raster printing of the current 
 * Layer View Panel or the area within the fence, if present.
 * The vertical extent may be modified to fit the paper size.
 * @author Larry Becker
 */
public class PrintPlugIn extends AbstractPlugIn {

    public final int INCH = 72;

    public final int HALF_INCH = 36;

    private final String PRINT_MENU = "Print...";

    private final String REMOVE_TRANSPARENCY = "Remove Transparency";

    private final String REMOVE_BASIC_FILLS = "Remove Fills from Basic Style";

    private final String REMOVE_THEME_FILLS = "Remove Fills from Color Themes";

    private final String CHANGE_LINE_WIDTH = "Change Line Width";

    private final String LINE_WIDTH = "Line Width Percent";

    private final String LINE_WIDTH_TOOLTIP = "0 - 300";

    private final String PDF_PAGE_TOOLTIP = "Inches";

    private final String PRINT_BORDER = "Print Page Border";

    private final String PRINT_OPTIONS = "Print Options";

    private final String PRINTER_NOT_FOUND = "Printer not found";

    private final String RESOLUTION_MULTIPLIER = "Double Image Resolution";

    private final String EXPAND_TO_FIT = "Expand to Fit Page (Vertical)";

    private final String PRINT_AREA_IN_FENCE = "Print Area in Fence";

    private final String PRINT_AREA_IN_BOUNDS = "Print Area in selection bounds";

    private final String OUT_OF_RANGE = "out of range";

    private final String FINISHED_MESSAGE = "Print finished";

    private final String PRINT_TO_PDF = "Print to PDF";

    private final String PDF_PAGE_WIDTH = "PDF Page Width";

    private final String PDF_PAGE_HEIGHT = "PDF Page Height";

    private boolean printToPDF = false;

    private PlugInContext pluginContext;

    private PrintService printService = null;

    ;

    private LayerViewPanel printPanel = null;

    private int resolutionFactor = 1;

    private boolean removeTransparency = false;

    private boolean removeThemeFills = false;

    private boolean removeBasicFills = false;

    private boolean changeLineWidth = true;

    private double lineWidthPercent = 25.0f;

    private float lineWidthMultiplier = 0.0f;

    private boolean printBorder = false;

    private boolean expandToFit = true;

    private boolean printFenceArea = false;

    private boolean printBoundsArea = true;

    private boolean doubleImageResolution = false;

    private Envelope printEnvelope;

    PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();

    private ArrayList printLayerables;

    private Envelope windowEnvelope = null;

    private Geometry fence = null;

    private double pdfPageWidth = 17;

    private double pdfPageHeight = 22;

    public PrintRequestAttributeSet getPrintRequestAttributeSet() {
        return attributeSet;
    }

    public void setPrintRequestAttributeSet(PrintRequestAttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem(this, new String[] { MenuNames.FILE }, PRINT_MENU, false, null, PrintPlugIn.createEnableCheck(context.getWorkbenchContext()));
    }

    public boolean execute(final PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), PRINT_OPTIONS, true);
        fence = context.getLayerViewPanel().getFence();
        printFenceArea = (fence != null);
        dialog.addCheckBox(REMOVE_TRANSPARENCY, removeTransparency);
        dialog.addCheckBox(EXPAND_TO_FIT, expandToFit);
        dialog.addCheckBox(PRINT_AREA_IN_FENCE, printFenceArea);
        dialog.getCheckBox(PRINT_AREA_IN_FENCE).setEnabled(context.getLayerViewPanel().getFence() != null);
        dialog.addCheckBox(PRINT_AREA_IN_BOUNDS, printFenceArea);
        dialog.getCheckBox(PRINT_AREA_IN_BOUNDS).setEnabled(context.getLayerViewPanel().getSelectionManager().getSelectedItems().size() == 1);
        dialog.addCheckBox(PRINT_BORDER, printBorder);
        dialog.addCheckBox(REMOVE_BASIC_FILLS, removeBasicFills);
        dialog.addCheckBox(REMOVE_THEME_FILLS, removeThemeFills);
        dialog.addCheckBox(CHANGE_LINE_WIDTH, changeLineWidth);
        dialog.addDoubleField(LINE_WIDTH, lineWidthPercent, 4, LINE_WIDTH_TOOLTIP);
        dialog.addCheckBox(RESOLUTION_MULTIPLIER, doubleImageResolution);
        dialog.addCheckBox(PRINT_TO_PDF, printToPDF);
        dialog.addDoubleField(PDF_PAGE_WIDTH, pdfPageWidth, 4, PDF_PAGE_TOOLTIP);
        dialog.addDoubleField(PDF_PAGE_HEIGHT, pdfPageHeight, 4, PDF_PAGE_TOOLTIP);
        dialog.setVisible(true);
        if (dialog.wasOKPressed()) {
            removeTransparency = dialog.getBoolean(REMOVE_TRANSPARENCY);
            expandToFit = dialog.getBoolean(EXPAND_TO_FIT);
            printFenceArea = dialog.getBoolean(PRINT_AREA_IN_FENCE);
            printBoundsArea = dialog.getBoolean(PRINT_AREA_IN_BOUNDS);
            printBorder = dialog.getBoolean(PRINT_BORDER);
            removeBasicFills = dialog.getBoolean(REMOVE_BASIC_FILLS);
            removeThemeFills = dialog.getBoolean(REMOVE_THEME_FILLS);
            changeLineWidth = dialog.getBoolean(CHANGE_LINE_WIDTH);
            lineWidthPercent = dialog.getDouble(LINE_WIDTH);
            lineWidthMultiplier = (float) lineWidthPercent / 100f;
            doubleImageResolution = dialog.getBoolean(RESOLUTION_MULTIPLIER);
            if (doubleImageResolution) resolutionFactor = 2; else resolutionFactor = 1;
            printToPDF = dialog.getBoolean(PRINT_TO_PDF);
            pdfPageWidth = dialog.getDouble(PDF_PAGE_WIDTH);
            pdfPageHeight = dialog.getDouble(PDF_PAGE_HEIGHT);
            new Thread(new Runnable() {

                public void run() {
                    try {
                        if (printToPDF) pdfCurrentWindow(context); else printCurrentWindow(context);
                    } catch (PrinterException e) {
                        context.getErrorHandler().handleThrowable(e);
                    }
                    context.getLayerViewPanel().repaint();
                }
            }).start();
        }
        return true;
    }

    protected void pdfCurrentWindow(PlugInContext context) {
        JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
        fileChooser.setDialogTitle("Save PDF");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        FileFilter fileFilter1 = GUIUtil.createFileFilter("PDF Files", new String[] { "pdf" });
        fileChooser.addChoosableFileFilter(fileFilter1);
        fileChooser.setFileFilter(fileFilter1);
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yy_HHmm-ss");
        String dateStr = df.format(date);
        String suggestedFileName = context.getTask().getName() + "_" + dateStr + ".pdf";
        fileChooser.setSelectedFile(new File(suggestedFileName));
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(context.getLayerViewPanel())) return;
        FileUtil.PutSaveDirectory(context.getWorkbenchContext(), fileChooser.getCurrentDirectory());
        String pdfFileName = fileChooser.getSelectedFile().getPath();
        if (!(pdfFileName.toLowerCase().endsWith(".pdf"))) pdfFileName = pdfFileName + ".pdf";
        pluginContext = context;
        printLayerables = new ArrayList(context.getLayerManager().getLayerables(Layerable.class));
        Collections.reverse(printLayerables);
        ArrayList oldStyleList = PrinterDriver.optimizeForVectors(printLayerables, removeThemeFills, removeBasicFills, changeLineWidth, (float) lineWidthMultiplier, removeTransparency);
        final Throwable[] throwable = new Throwable[] { null };
        printPanel = createLayerPanel(context.getLayerManager(), throwable);
        PDFDriver.disableDoubleBuffering(printPanel);
        PDFDriver pdfDriver = new PDFDriver(context, printPanel);
        ScaleBarRenderer.setEnabled(ScaleBarRenderer.isEnabled(context.getLayerViewPanel()), printPanel);
        NorthArrowRenderer.setEnabled(NorthArrowRenderer.isEnabled(context.getLayerViewPanel()), printPanel);
        pdfDriver.setTaskFrame((TaskFrame) context.getWorkbenchFrame().getActiveInternalFrame());
        pdfDriver.setPrintBorder(printBorder);
        pdfDriver.setPrintLayerables(printLayerables);
        windowEnvelope = pluginContext.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        fence = pluginContext.getLayerViewPanel().getFence();
        try {
            try {
                Document document = new Document(new Rectangle((float) pdfPageWidth * INCH, (float) pdfPageHeight * INCH));
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
                writer.setCropBoxSize(new Rectangle(0, 0, ((float) pdfPageWidth * INCH), ((float) pdfPageHeight * INCH)));
                writer.setPdfVersion(PdfWriter.VERSION_1_5);
                writer.setViewerPreferences(PdfWriter.PageModeUseOC);
                document.open();
                DefaultFontMapper mapper = new DefaultFontMapper();
                FontFactory.registerDirectories();
                PageFormat pageFormat = new PageFormat();
                Paper paper = new Paper();
                double width = pdfPageWidth * INCH;
                double height = pdfPageHeight * INCH;
                paper.setSize(width, height);
                paper.setImageableArea(0, 0, width, height);
                pageFormat.setPaper(paper);
                double w = pageFormat.getImageableWidth();
                double h = pageFormat.getImageableHeight();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate((float) w, (float) h);
                Graphics2D g2 = tp.createGraphics((float) w, (float) h, mapper);
                tp.setWidth((float) w);
                tp.setHeight((float) h);
                pdfDriver.setCb(tp);
                pdfDriver.setWriter(writer);
                try {
                    initLayerViewPanel(pageFormat);
                    pdfDriver.setResolutionFactor(resolutionFactor);
                } catch (Exception e) {
                    String message = (e.getMessage() == null) ? e.toString() : e.getMessage();
                    System.err.println(message);
                }
                try {
                    pdfDriver.print(g2, pageFormat, 0);
                } catch (PrinterException e) {
                    String message = (e.getMessage() == null) ? e.toString() : e.getMessage();
                    System.err.println(message);
                }
                g2.dispose();
                tp.sanityCheck();
                cb.addTemplate(tp, 0, 0);
                cb.sanityCheck();
                document.close();
            } catch (DocumentException de) {
                System.err.println(de.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        } finally {
            if (oldStyleList != null) {
                boolean wasFiringEvents = printPanel.getLayerManager().isFiringEvents();
                printPanel.getLayerManager().setFiringEvents(false);
                int j = 0;
                for (Iterator i = printLayerables.iterator(); i.hasNext(); ) {
                    Object layerable = i.next();
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer) layerable;
                        layer.setStyles((Collection) oldStyleList.get(j++));
                    }
                }
                printPanel.getLayerManager().setFiringEvents(wasFiringEvents);
            }
            if (printPanel != null) {
                PrinterDriver.enableDoubleBuffering(printPanel);
                printPanel.dispose();
                printPanel = null;
            }
            context.getWorkbenchFrame().setStatusMessage(FINISHED_MESSAGE);
        }
    }

    protected void printCurrentWindow(PlugInContext context) throws PrinterException {
        pluginContext = context;
        final Throwable[] throwable = new Throwable[] { null };
        printPanel = createLayerPanel(context.getLayerManager(), throwable);
        PrinterDriver.disableDoubleBuffering(printPanel);
        windowEnvelope = pluginContext.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        fence = pluginContext.getLayerViewPanel().getFence();
        printLayerables = new ArrayList(context.getLayerManager().getLayerables(Layerable.class));
        Collections.reverse(printLayerables);
        PrinterDriver printerDriver = new PrinterDriver(context, printPanel);
        ScaleBarRenderer.setEnabled(ScaleBarRenderer.isEnabled(context.getLayerViewPanel()), printPanel);
        NorthArrowRenderer.setEnabled(NorthArrowRenderer.isEnabled(context.getLayerViewPanel()), printPanel);
        printerDriver.setTaskFrame((TaskFrame) context.getWorkbenchFrame().getActiveInternalFrame());
        printerDriver.setPrintBorder(printBorder);
        ArrayList oldStyleList = PrinterDriver.optimizeForVectors(printLayerables, removeThemeFills, removeBasicFills, changeLineWidth, (float) lineWidthMultiplier, removeTransparency);
        printerDriver.setPrintLayerables(printLayerables);
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            attributeSet.add(new JobName(context.getTask().getName(), null));
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            if (printService == null) {
                PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, attributeSet);
                if (services.length > 0) {
                    printService = services[0];
                } else {
                    context.getWorkbenchFrame().warnUser(PRINTER_NOT_FOUND);
                    return;
                }
            }
            printerJob.setPrintService(printService);
            if (printerJob.printDialog(attributeSet)) {
                PageFormat pageFormat = PrinterDriver.getPageFormat(attributeSet, printerJob);
                printerJob.setPrintable(printerDriver, pageFormat);
                try {
                    initLayerViewPanel(pageFormat);
                    printerDriver.setResolutionFactor(resolutionFactor);
                } catch (Exception e) {
                    String message = (e.getMessage() == null) ? e.toString() : e.getMessage();
                    System.err.println(message);
                    throw new PrinterException(message);
                }
                printerJob.print(attributeSet);
            }
            if (throwable[0] != null) {
                String message = (throwable[0].getMessage() == null) ? throwable[0].toString() : throwable[0].getMessage();
                System.err.println(message);
                context.getErrorHandler().handleThrowable((throwable[0] instanceof Exception) ? (Exception) throwable[0] : new Exception(message));
            }
            printService = printerJob.getPrintService();
        } finally {
            if (oldStyleList != null) {
                boolean wasFiringEvents = printPanel.getLayerManager().isFiringEvents();
                printPanel.getLayerManager().setFiringEvents(false);
                int j = 0;
                for (Iterator i = printLayerables.iterator(); i.hasNext(); ) {
                    Object layerable = i.next();
                    if (layerable instanceof Layer) {
                        Layer layer = (Layer) layerable;
                        layer.setStyles((Collection) oldStyleList.get(j++));
                    }
                }
                printPanel.getLayerManager().setFiringEvents(wasFiringEvents);
            }
            if (printPanel != null) {
                PrinterDriver.enableDoubleBuffering(printPanel);
                printPanel.dispose();
                printPanel = null;
            }
            printerDriver = null;
            context.getWorkbenchFrame().setStatusMessage(FINISHED_MESSAGE);
        }
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1));
    }

    /**
     * Construct a printing LayerViewPanel using the PlugInContext's LayerManager
     * @param context
     * @return new LayerViewPanel
     */
    protected LayerViewPanel createLayerPanel(LayerManager layerManager, final Throwable[] throwable) {
        LayerViewPanel layerViewPanel = new LayerViewPanel(layerManager, new LayerViewPanelContext() {

            public void setStatusMessage(String message) {
            }

            public void warnUser(String warning) {
            }

            public void handleThrowable(Throwable t) {
                throwable[0] = t;
            }
        });
        return layerViewPanel;
    }

    protected Envelope computePrintPageEnvelope(Envelope windowEnvelope, PageFormat pf) {
        double pageRatio = pf.getImageableHeight() / pf.getImageableWidth();
        double minX = windowEnvelope.getMinX();
        double maxX = windowEnvelope.getMaxX();
        double minY = windowEnvelope.getMinY();
        double maxY = windowEnvelope.getMaxY();
        if (expandToFit) {
            minY = windowEnvelope.getMaxY() - (windowEnvelope.getWidth() * pageRatio);
            maxY = windowEnvelope.getMaxY();
        }
        return new Envelope(minX, maxX, minY, maxY);
    }

    protected void initLayerViewPanel(PageFormat pageFormat) throws Exception {
        if ((printFenceArea) && (fence != null)) {
            printEnvelope = computePrintPageEnvelope(fence.getEnvelopeInternal(), pageFormat);
        } else {
            if (printBoundsArea) {
                printEnvelope = computePrintPageEnvelope(((Geometry) (pluginContext.getLayerViewPanel().getSelectionManager().getSelectedItems().iterator().next())).getEnvelopeInternal(), pageFormat);
            } else {
                printEnvelope = computePrintPageEnvelope(windowEnvelope, pageFormat);
            }
        }
        int extentInPixelsX = (int) (pageFormat.getImageableWidth() * resolutionFactor);
        int extentInPixelsY = (int) (pageFormat.getImageableHeight() * resolutionFactor);
        if (!expandToFit) {
            double ratio = (printEnvelope.getHeight() / printEnvelope.getWidth());
            extentInPixelsY = (int) Math.round((ratio * pageFormat.getImageableWidth()) * resolutionFactor);
        }
        printPanel.setSize(extentInPixelsX, extentInPixelsY);
        printPanel.getViewport().zoom(printEnvelope);
    }
}
