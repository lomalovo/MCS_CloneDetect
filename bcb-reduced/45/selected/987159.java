package com.patientis.framework.itext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.examples.simpleviewer.utils.FileFilterer;
import com.patientis.framework.controls.ISButton;
import com.patientis.framework.controls.ISPanel;
import com.patientis.framework.controls.ISScrollPane;
import com.patientis.framework.controls.forms.ISDialog;
import com.patientis.framework.controls.forms.ISFrame;
import com.patientis.framework.controls.listeners.ISActionListener;
import com.patientis.framework.locale.ImageUtil;
import com.patientis.framework.utility.PrintUtility;

public class PDFSimpleViewer extends ISDialog {

    private String viewerTitle = "Jpanel Demo";

    /**the actual JPanel/decoder object*/
    private PdfDecoder pdfDecoder;

    /**name of current PDF file*/
    private String currentFile = null;

    /**current page number (first page is 1)*/
    private int currentPage = 1;

    private final JLabel pageCounter1 = new JLabel("Page ");

    private JTextField pageCounter2 = new JTextField(4);

    private JLabel pageCounter3 = new JLabel("of");

    private int printPixelWidth = 0;

    private int printPixelHeight = 0;

    private boolean landscape = false;

    /**
   * 
   */
    private ISPanel contentPanel = new ISPanel();

    /**
   * construct a pdf viewer, passing in the full file name
   */
    public PDFSimpleViewer(Frame frameOrDialog, File pdfFile, boolean hideViewer) throws Exception {
        super(frameOrDialog);
        init(pdfFile, hideViewer);
    }

    /**
   * construct a pdf viewer, passing in the full file name
   */
    public PDFSimpleViewer(ISDialog frameOrDialog, File pdfFile, boolean hideViewer) throws Exception {
        super(frameOrDialog);
        init(pdfFile, hideViewer);
    }

    /**
   * 
   * @param frameOrDialog
   * @param pdfFile
   * @param hideViewer
   * @throws Exception
   */
    public void init(File pdfFile, boolean hideViewer) throws Exception {
        pdfDecoder = new PdfDecoder();
        currentFile = pdfFile.getAbsolutePath();
        try {
            pdfDecoder.openPdfFile(currentFile);
            pdfDecoder.decodePage(currentPage);
            pdfDecoder.setPageParameters(1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeViewer(pdfFile);
        if (!hideViewer) {
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(700, 700);
            setLocationRelativeTo(null);
            setVisible(true);
            pageCounter2.setText(String.valueOf(currentPage));
            pageCounter3.setText("of " + pdfDecoder.getPageCount());
        }
    }

    /**
   * construct an empty pdf viewer and pop up the open window
   */
    public PDFSimpleViewer() throws Exception {
        setTitle(viewerTitle);
        pdfDecoder = new PdfDecoder();
        initializeViewer(null);
    }

    /**
   * opens a chooser and allows user to select a pdf file and opens it
   */
    private void selectFile() {
        JFileChooser open = new JFileChooser(".");
        open.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String[] pdf = new String[] { "pdf" };
        open.addChoosableFileFilter(new FileFilterer(pdf, "Pdf (*.pdf)"));
        int resultOfFileSelect = JFileChooser.ERROR_OPTION;
        while (resultOfFileSelect == JFileChooser.ERROR_OPTION) {
            resultOfFileSelect = open.showOpenDialog(this);
            if (resultOfFileSelect == JFileChooser.ERROR_OPTION) System.err.println("JFileChooser error");
            if (resultOfFileSelect == JFileChooser.APPROVE_OPTION) {
                currentFile = open.getSelectedFile().getAbsolutePath();
                currentPage = 1;
                try {
                    pdfDecoder.closePdfFile();
                    pdfDecoder.openPdfFile(currentFile);
                    if (!checkEncryption()) {
                        resultOfFileSelect = JFileChooser.CANCEL_OPTION;
                    }
                    pdfDecoder.decodePage(currentPage);
                    pdfDecoder.setPageParameters(1, 1);
                    pdfDecoder.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pageCounter2.setText(String.valueOf(currentPage));
                pageCounter3.setText("of " + pdfDecoder.getPageCount());
                setTitle(viewerTitle + " - " + currentFile);
                repaint();
            }
        }
    }

    /**
   * check if encryption present and acertain password, return true if content accessable
   */
    private boolean checkEncryption() {
        if (pdfDecoder.isEncrypted()) {
            while (!pdfDecoder.isFileViewable()) {
                String password = JOptionPane.showInputDialog(this, "Please enter password");
                if (password != null) {
                    try {
                        pdfDecoder.setEncryptionPassword(password);
                    } catch (PdfException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
        return true;
    }

    /**
   * setup the viewer and its components
   */
    private void initializeViewer(final File pdfFile) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        Component[] itemsToAdd = initChangerPanel();
        JPanel topBar = new JPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        for (int i = 0; i < itemsToAdd.length; i++) {
            topBar.add(itemsToAdd[i]);
        }
        ISButton button = new ISButton("Print");
        button.setIcon(ImageUtil.getIcon("common/print.gif", ""));
        button.addActionListener(new ISActionListener() {

            @Override
            public void actionExecuted(ActionEvent e) throws Exception {
                if (printPixelWidth > 0 && printPixelHeight > 0) {
                    PrintPDF.printPDF(pdfFile, printPixelWidth, printPixelHeight, landscape);
                } else {
                    PrintPDF.printPDF(pdfFile);
                }
            }
        });
        topBar.add(button);
        contentPanel.add(topBar, BorderLayout.NORTH);
        JScrollPane display = initPDFDisplay();
        contentPanel.add(new ISScrollPane(display), BorderLayout.CENTER);
        contentPane.add((contentPanel), BorderLayout.CENTER);
    }

    /**
   * returns the open button with listener
   */
    private JButton initOpenBut() {
        JButton open = new JButton();
        open.setIcon(new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/open.gif")));
        open.setText("Open");
        open.setToolTipText("Open a file");
        open.setBorderPainted(false);
        open.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
        return open;
    }

    /**
   * returns the scrollpane with pdfDecoder set as the viewport
   */
    private JScrollPane initPDFDisplay() {
        JScrollPane currentScroll = new JScrollPane();
        currentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        currentScroll.setViewportView(pdfDecoder);
        return currentScroll;
    }

    /**
   * setup the page display and changer panel and return it 
   */
    private Component[] initChangerPanel() {
        Component[] list = new Component[11];
        JButton start = new JButton();
        start.setBorderPainted(false);
        URL startImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/start.gif");
        start.setIcon(new ImageIcon(startImage));
        start.setToolTipText("Rewind to page 1");
        list[0] = start;
        start.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage != 1) {
                    currentPage = 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back to page 1");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        JButton fback = new JButton();
        fback.setBorderPainted(false);
        URL fbackImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/fback.gif");
        fback.setIcon(new ImageIcon(fbackImage));
        fback.setToolTipText("Rewind 10 pages");
        list[1] = fback;
        fback.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage > 10) {
                    currentPage -= 10;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back 10 pages");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        JButton back = new JButton();
        back.setBorderPainted(false);
        URL backImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/back.gif");
        back.setIcon(new ImageIcon(backImage));
        back.setToolTipText("Rewind one page");
        list[2] = back;
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage > 1) {
                    currentPage -= 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("back 1 page");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        pageCounter2.setEditable(true);
        pageCounter2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent a) {
                String value = pageCounter2.getText().trim();
                int newPage;
                try {
                    newPage = Integer.parseInt(value);
                    if ((newPage > pdfDecoder.getPageCount()) | (newPage < 1)) {
                        return;
                    }
                    currentPage = newPage;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e) {
                        System.err.println("page number entered");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, '>' + value + "< is Not a valid Value.\nPlease enter a number between 1 and " + pdfDecoder.getPageCount());
                }
            }
        });
        list[3] = pageCounter1;
        list[4] = new JPanel();
        list[5] = pageCounter2;
        list[6] = new JPanel();
        list[7] = pageCounter3;
        JButton forward = new JButton();
        forward.setBorderPainted(false);
        URL fowardImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/forward.gif");
        forward.setIcon(new ImageIcon(fowardImage));
        forward.setToolTipText("forward 1 page");
        list[8] = forward;
        forward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount()) {
                    currentPage += 1;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward 1 page");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        JButton fforward = new JButton();
        fforward.setBorderPainted(false);
        URL ffowardImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/fforward.gif");
        fforward.setIcon(new ImageIcon(ffowardImage));
        fforward.setToolTipText("Fast forward 10 pages");
        list[9] = fforward;
        fforward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount() - 9) {
                    currentPage += 10;
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward 10 pages");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        JButton end = new JButton();
        end.setBorderPainted(false);
        URL endImage = getClass().getResource("/org/jpedal/examples/simpleviewer/res/end.gif");
        end.setIcon(new ImageIcon(endImage));
        end.setToolTipText("Fast forward to last page");
        list[10] = end;
        end.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (currentFile != null && currentPage < pdfDecoder.getPageCount()) {
                    currentPage = pdfDecoder.getPageCount();
                    try {
                        pdfDecoder.decodePage(currentPage);
                        pdfDecoder.invalidate();
                        repaint();
                    } catch (Exception e1) {
                        System.err.println("forward to last page");
                        e1.printStackTrace();
                    }
                    pageCounter2.setText(String.valueOf(currentPage));
                }
            }
        });
        return list;
    }

    /**
	 * @return the contentPanel
	 */
    public ISPanel getContentPanel() {
        return contentPanel;
    }

    /**
	 * @param contentPanel the contentPanel to set
	 */
    public void setContentPanel(ISPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    /**
	 * @return the printPixelWidth
	 */
    public int getPrintPixelWidth() {
        return printPixelWidth;
    }

    /**
	 * @param printPixelWidth the printPixelWidth to set
	 */
    public void setPrintPixelWidth(int printPixelWidth) {
        this.printPixelWidth = printPixelWidth;
    }

    /**
	 * @return the printPixelHeight
	 */
    public int getPrintPixelHeight() {
        return printPixelHeight;
    }

    /**
	 * @param printPixelHeight the printPixelHeight to set
	 */
    public void setPrintPixelHeight(int printPixelHeight) {
        this.printPixelHeight = printPixelHeight;
    }

    /**
	 * @return the landscape
	 */
    public boolean isLandscape() {
        return landscape;
    }

    /**
	 * @param landscape the landscape to set
	 */
    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }
}
