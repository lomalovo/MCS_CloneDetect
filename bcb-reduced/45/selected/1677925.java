package net.aa3sd.SMT.ui;

import javax.swing.JPanel;
import net.aa3sd.SMT.SMTProperties;
import net.aa3sd.SMT.SMTSingleton;
import net.aa3sd.SMT.interfaces.TaskListListener;
import net.aa3sd.SMT.search.ResourceList;
import org.apache.log4j.Logger;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * @author Paul J. Morris
 *
 */
public class ResourcePanel extends JPanel implements TaskListListener {

    private static final long serialVersionUID = 1574596860526213441L;

    private static final Logger log = Logger.getLogger(ResourcePanel.class);

    private JTable table;

    public ResourcePanel() {
        setLayout(new BorderLayout(0, 0));
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        table = new JTable();
        table.setModel(new ResourceList());
        scrollPane.setViewportView(table);
        SMTSingleton.getSingletonInstance().getCurrentSearch().getTasks().registerTaskListListener(this);
        JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);
        JMenu mnNewMenu = new JMenu("Resources");
        menuBar.add(mnNewMenu);
        mnNewMenu.setMnemonic(KeyEvent.VK_R);
        JMenuItem mntmPrintTaskAssignments = new JMenuItem("Print Operational Planning Worksheet (ICS 215)");
        mntmPrintTaskAssignments.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Document document = new Document();
                if ("A4".equals(SMTSingleton.getSingletonInstance().getProperties().getProperties().get(SMTProperties.KEY_PAPERSIZE))) {
                    document.setPageSize(PageSize.A4);
                } else {
                    document.setPageSize(PageSize.LETTER);
                }
                try {
                    String filename = "OperationalPlanningWorksheet_ICS-215.pdf";
                    PdfWriter.getInstance(document, new FileOutputStream(filename));
                    document.open();
                    ResourceList tasks = ((ResourceList) table.getModel());
                    tasks.writeToPdf(document);
                    log.debug("Printing: " + filename);
                    document.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (DocumentException ex) {
                    ex.printStackTrace();
                }
            }
        });
        mnNewMenu.add(mntmPrintTaskAssignments);
    }

    @Override
    public void taskListChanged() {
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
    }
}
