package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUISearchWindow;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Messages;
import org.jpedal.utils.SwingWorker;

/**provides interactive search Window and search capabilities*/
public class SwingSearchWindow extends JFrame implements GUISearchWindow {

    /**flag to stop multiple listeners*/
    private boolean isSetup = false;

    String defaultMessage = "Enter your text here";

    JTextField searchText = null;

    JCheckBox searchAll;

    JTextField searchCount;

    DefaultListModel listModel;

    SearchList results;

    MouseListener ML;

    ActionListener AL = null;

    WindowListener WL;

    KeyListener KL;

    /**swing thread to search in background*/
    SwingWorker searcher = null;

    /**flag to show searching taking place*/
    public boolean isSearch = false;

    JButton searchButton = null;

    /**number fo search items*/
    private int itemFoundCount = 0;

    /**used when fiding text to highlight on page*/
    Map textPages = new HashMap();

    Map textRectangles = new HashMap();

    final JPanel nav = new JPanel();

    Values commonValues;

    SwingGUI currentGUI;

    PdfDecoder decode_pdf;

    /**deletes message when user starts typing*/
    private boolean deleteOnClick;

    public SwingSearchWindow(Values commonValues, SwingGUI currentGUI, PdfDecoder decode_pdf) {
        this.commonValues = commonValues;
        this.currentGUI = currentGUI;
        this.decode_pdf = decode_pdf;
    }

    /**
     * find text on page
     */
    public void find() {
        if (isSetup) {
            searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + " " + itemFoundCount);
            searchText.selectAll();
            searchText.grabFocus();
        } else {
            isSetup = true;
            defaultMessage = Messages.getMessage("PdfViewerSearchGUI.DefaultMessage");
            searchText = new JTextField(defaultMessage);
            searchButton = new JButton(Messages.getMessage("PdfViewerSearch.Button"));
            nav.setLayout(new BorderLayout());
            WL = new WindowListener() {

                public void windowOpened(WindowEvent arg0) {
                }

                public void windowClosing(WindowEvent arg0) {
                    removeSearchWindow(true);
                }

                public void windowClosed(WindowEvent arg0) {
                }

                public void windowIconified(WindowEvent arg0) {
                }

                public void windowDeiconified(WindowEvent arg0) {
                }

                public void windowActivated(WindowEvent arg0) {
                }

                public void windowDeactivated(WindowEvent arg0) {
                }
            };
            this.addWindowListener(WL);
            nav.add(searchButton, BorderLayout.EAST);
            nav.add(searchText, BorderLayout.CENTER);
            searchAll = new JCheckBox();
            searchAll.setSelected(true);
            searchAll.setText(Messages.getMessage("PdfViewerSearch.CheckBox"));
            nav.add(searchAll, BorderLayout.NORTH);
            itemFoundCount = 0;
            textPages.clear();
            textRectangles.clear();
            listModel = null;
            searchCount = new JTextField(Messages.getMessage("PdfViewerSearch.ItemsFound") + " " + itemFoundCount);
            searchCount.setEditable(false);
            nav.add(searchCount, BorderLayout.SOUTH);
            listModel = new DefaultListModel();
            results = new SearchList(listModel, textPages);
            results.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            ML = new MouseListener() {

                public void mouseClicked(MouseEvent arg0) {
                    if (!commonValues.isProcessing()) {
                        float scaling = currentGUI.getScaling();
                        int inset = currentGUI.getPDFDisplayInset();
                        int id = results.getSelectedIndex();
                        decode_pdf.setFoundTextAreas(null);
                        if (id != -1) {
                            Integer key = new Integer(id);
                            Object newPage = textPages.get(key);
                            if (newPage != null) {
                                int nextPage = ((Integer) newPage).intValue();
                                Rectangle highlight = (Rectangle) textRectangles.get(key);
                                if (commonValues.getCurrentPage() != nextPage) {
                                    commonValues.setCurrentPage(nextPage);
                                    currentGUI.resetStatusMessage(Messages.getMessage("PdfViewer.LoadingPage") + " " + commonValues.getCurrentPage());
                                    decode_pdf.setPageParameters(scaling, commonValues.getCurrentPage());
                                    currentGUI.decodePage(false);
                                    decode_pdf.invalidate();
                                }
                                int scrollInterval = decode_pdf.getScrollInterval();
                                int x = (int) ((highlight.x - currentGUI.cropX) * scaling) + inset;
                                int y = (int) ((currentGUI.cropH - (highlight.y - currentGUI.cropY)) * scaling) + inset;
                                int w = (int) (highlight.width * scaling);
                                int h = (int) (highlight.height * scaling);
                                Rectangle scrollto = new Rectangle(x - scrollInterval, y - h - scrollInterval, w + scrollInterval * 2, h + scrollInterval * 2);
                                decode_pdf.scrollRectToVisible(scrollto);
                                decode_pdf.setFoundTextArea(highlight);
                                decode_pdf.invalidate();
                                decode_pdf.repaint();
                            }
                        }
                    }
                }

                public void mousePressed(MouseEvent arg0) {
                }

                public void mouseReleased(MouseEvent arg0) {
                }

                public void mouseEntered(MouseEvent arg0) {
                }

                public void mouseExited(MouseEvent arg0) {
                }
            };
            results.addMouseListener(ML);
            AL = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (!isSearch) {
                        try {
                            searchText();
                        } catch (Exception e1) {
                            System.out.println("Exception " + e1);
                            e1.printStackTrace();
                        }
                    } else {
                        searcher.interrupt();
                        isSearch = false;
                        searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
                    }
                }
            };
            searchButton.addActionListener(AL);
            searchText.selectAll();
            deleteOnClick = true;
            KL = new KeyListener() {

                public void keyTyped(KeyEvent e) {
                    if (deleteOnClick) {
                        deleteOnClick = false;
                        searchText.setText("");
                    }
                    int id = e.getID();
                    if (id == KeyEvent.KEY_TYPED) {
                        char key = e.getKeyChar();
                        if (key == '\n') {
                            try {
                                searchText();
                            } catch (Exception e1) {
                                System.out.println("Exception " + e1);
                                e1.printStackTrace();
                            }
                        }
                    }
                }

                public void keyPressed(KeyEvent arg0) {
                }

                public void keyReleased(KeyEvent arg0) {
                }
            };
            searchText.addKeyListener(KL);
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().add(results);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(80);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(80);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            getContentPane().add(nav, BorderLayout.NORTH);
            Container frame;
            if (commonValues.getModeOfOperation() == Values.RUNNING_APPLET) {
                frame = currentGUI.getFrame().getContentPane();
            } else {
                frame = currentGUI.getFrame();
            }
            int w = 230;
            int h = frame.getHeight();
            int x1 = frame.getLocationOnScreen().x;
            int x = frame.getWidth() + x1;
            int y = frame.getLocationOnScreen().y;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int width = d.width;
            if (x + w > width) {
                x = width - w;
                frame.setSize(x - x1, frame.getHeight());
            }
            setSize(w, h);
            setLocation(x, y);
            searchAll.setFocusable(false);
            searchText.grabFocus();
        }
        show();
    }

    public void removeSearchWindow(boolean justHide) {
        hide();
        setVisible(false);
        if (searcher != null) searcher.interrupt();
        if (isSetup && !justHide) {
            if (listModel != null) listModel.clear();
            itemFoundCount = 0;
            isSearch = false;
        }
    }

    private void searchText() throws Exception {
        if ((searcher != null)) searcher.interrupt();
        searchButton.setText(Messages.getMessage("PdfViewerSearchButton.Stop"));
        searchButton.invalidate();
        searchButton.repaint();
        isSearch = true;
        searchCount.setText(Messages.getMessage("PdfViewerSearch.Scanning1"));
        searchCount.repaint();
        searcher = new SwingWorker() {

            public Object construct() {
                try {
                    listModel.removeAllElements();
                    results.repaint();
                    int listCount = 0;
                    textPages.clear();
                    textRectangles.clear();
                    itemFoundCount = 0;
                    decode_pdf.setFoundTextAreas(null);
                    String textToFind = searchText.getText();
                    PdfPageData pageSize = decode_pdf.getPdfPageData();
                    int x1, y1, x2, y2;
                    int startPage = 1;
                    int endPage = commonValues.getPageCount() + 1;
                    if (!searchAll.isSelected()) {
                        startPage = commonValues.getCurrentPage();
                        endPage = startPage + 1;
                    }
                    for (int i = startPage; i < endPage; i++) {
                        if (Thread.interrupted()) throw new InterruptedException();
                        PdfGroupingAlgorithms currentGrouping = null;
                        try {
                            if (i == commonValues.getCurrentPage()) currentGrouping = decode_pdf.getGroupingObject(); else {
                                decode_pdf.decodePageInBackground(i);
                                currentGrouping = decode_pdf.getBackgroundGroupingObject();
                            }
                            currentGrouping.generateTeasers();
                            x1 = pageSize.getMediaBoxX(i);
                            x2 = pageSize.getMediaBoxWidth(i);
                            y1 = pageSize.getMediaBoxY(i);
                            y2 = pageSize.getMediaBoxHeight(i);
                            float[] co_ords = currentGrouping.findTextInRectangle(x1, y2, x2 + x1, y1, i, textToFind, false, true);
                            float[] endPoints = currentGrouping.getEndPoints();
                            final String[] teasers = currentGrouping.getTeasers();
                            if (Thread.interrupted()) throw new InterruptedException();
                            if ((co_ords != null) && (teasers != null)) {
                                itemFoundCount = itemFoundCount + teasers.length;
                                int count = co_ords.length, next = 0;
                                for (int ii = 0; ii < count; ii = ii + 2) {
                                    int wx1 = (int) co_ords[ii];
                                    int wy1 = (int) co_ords[ii + 1];
                                    int wx2 = (int) endPoints[ii];
                                    int wy2 = (int) endPoints[ii + 1];
                                    final String tease = teasers[ii / 2];
                                    Runnable setTextRun = new Runnable() {

                                        public void run() {
                                            listModel.addElement(tease);
                                        }
                                    };
                                    SwingUtilities.invokeAndWait(setTextRun);
                                    Integer key = new Integer(listCount);
                                    listCount++;
                                    textRectangles.put(key, new Rectangle(wx1, wy2, wx2 - wx1, wy1 - wy2));
                                    textPages.put(key, new Integer(i));
                                    next++;
                                }
                            }
                            if ((co_ords != null) | ((i % 16) == 0)) {
                                searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + " " + itemFoundCount + " " + Messages.getMessage("PdfViewerSearch.Scanning") + i);
                                searchCount.invalidate();
                                searchCount.repaint();
                            }
                        } catch (PdfException e1) {
                        }
                    }
                    searchCount.setText(Messages.getMessage("PdfViewerSearch.ItemsFound") + " " + itemFoundCount + "  " + Messages.getMessage("PdfViewerSearch.Done"));
                    results.invalidate();
                    results.repaint();
                    isSearch = false;
                    searchButton.setText(Messages.getMessage("PdfViewerSearch.Button"));
                } catch (Exception e) {
                }
                return null;
            }
        };
        searcher.start();
    }

    public void grabFocusInInput() {
        searchText.grabFocus();
    }

    public boolean isSearchVisible() {
        return this.isVisible();
    }
}
