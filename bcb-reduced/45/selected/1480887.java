package com.compomics.icelogo.gui.forms;

import com.compomics.icelogo.core.data.MainInformationFeeder;
import com.compomics.icelogo.gui.interfaces.Savable;
import com.compomics.util.sun.SwingWorker;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA. User: niklaas Date: 2-mrt-2009 Time: 14:46:01 To change this template use File | Settings |
 * File Templates.
 */
public class GraphableSaverForm {

    private JPanel jpanContent;

    private JButton saveButton;

    private JPanel savablePanel;

    private Vector<SavableLine> lSavableLines = new Vector<SavableLine>();

    private MainInformationFeeder iInfoFeeder = MainInformationFeeder.getInstance();

    public GraphableSaverForm(Vector<Savable> aSavables) {
        savablePanel.setLayout(new BoxLayout(savablePanel, BoxLayout.Y_AXIS));
        savablePanel.add(Box.createVerticalStrut(5));
        for (int i = 0; i < aSavables.size(); i++) {
            SavableLine lLine = new SavableLine(aSavables.get(i));
            lSavableLines.add(lLine);
            savablePanel.add(lLine.getContentPane());
            savablePanel.add(Box.createVerticalStrut(5));
        }
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
    }

    public JPanel getContentPane() {
        return this.jpanContent;
    }

    public void save() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showSaveDialog(new JFrame());
        String fileLocation = fc.getSelectedFile().getAbsolutePath();
        if (fileLocation.indexOf(".") != -1) {
            fileLocation = fileLocation.substring(0, fileLocation.indexOf("."));
        }
        final String lPDFFileLocation = fileLocation + ".pdf";
        final String lCSVFileLocation = fileLocation + ".csv";
        SwingWorker lPdfSaver = new SwingWorker() {

            public Boolean construct() {
                try {
                    Document document = new Document(new Rectangle(iInfoFeeder.getGraphableWidth(), iInfoFeeder.getGraphableHeight()));
                    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(lPDFFileLocation));
                    document.open();
                    PdfContentByte cb = writer.getDirectContent();
                    Graphics2D g2;
                    for (int i = 0; i < lSavableLines.size(); i++) {
                        if (lSavableLines.get(i).isSelected()) {
                            Savable lSavable = lSavableLines.get(i).getSavable();
                            if (lSavable.isSvg()) {
                                UserAgent userAgent = new UserAgentAdapter();
                                DocumentLoader loader = new DocumentLoader(userAgent);
                                BridgeContext ctx = new BridgeContext(userAgent, loader);
                                GVTBuilder builder = new GVTBuilder();
                                ctx.setDynamicState(BridgeContext.DYNAMIC);
                                PdfTemplate map = cb.createTemplate(iInfoFeeder.getGraphableWidth(), iInfoFeeder.getGraphableHeight());
                                g2 = map.createGraphics(iInfoFeeder.getGraphableWidth(), iInfoFeeder.getGraphableHeight(), new DefaultFontMapper());
                                GraphicsNode graphicsToPaint = builder.build(ctx, lSavableLines.get(i).getSavable().getSVG());
                                graphicsToPaint.paint(g2);
                                g2.dispose();
                                cb.addTemplate(map, 0, 0);
                                document.newPage();
                            } else if (lSavable.isChart()) {
                                g2 = cb.createGraphicsShapes(iInfoFeeder.getGraphableWidth(), iInfoFeeder.getGraphableHeight());
                                lSavableLines.get(i).getSavable().getContentPanel().paintAll(g2);
                                g2.dispose();
                                document.newPage();
                            } else if (lSavable.isText()) {
                                File lFile = new File(lCSVFileLocation);
                                if (lFile.exists() == false) {
                                    lFile.createNewFile();
                                }
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lFile)));
                                String lContent = lSavable.getText();
                                bw.write(lContent);
                                bw.flush();
                                bw.close();
                            }
                        }
                    }
                    document.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            public void finished() {
                JOptionPane.showMessageDialog(new JFrame(), "Saving done", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        lPdfSaver.start();
    }

    {
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder("Save"));
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        savablePanel = new JPanel();
        savablePanel.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(savablePanel);
        saveButton = new JButton();
        saveButton.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(saveButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }

    /**
     * Created by IntelliJ IDEA. User: niklaas Date: 3-mrt-2009 Time: 12:20:22 To change this template use File |
     * Settings | File Templates.
     */
    private class SavableLine {

        private JPanel jpanContent;

        private JCheckBox chbDescription;

        private Savable iSavable;

        public SavableLine(Savable aSavable) {
            build();
            this.iSavable = aSavable;
            chbDescription.setText(aSavable.getDescription());
        }

        public JPanel getContentPane() {
            return jpanContent;
        }

        public Savable getSavable() {
            return this.iSavable;
        }

        public boolean isSelected() {
            return chbDescription.isSelected();
        }

        /**
         * Construct the GUI
         */
        private void build() {
            jpanContent = new JPanel();
            jpanContent.setLayout(new GridBagLayout());
            chbDescription = new JCheckBox();
            chbDescription.setText("Description");
            chbDescription.setSelected(true);
            GridBagConstraints gbc;
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            jpanContent.add(chbDescription, gbc);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            final JPanel spacer1 = new JPanel();
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            jpanContent.add(spacer1, gbc);
        }

        public JComponent getRootComponent() {
            return jpanContent;
        }
    }
}
