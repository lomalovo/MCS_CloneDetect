package net.aa3sd.SMT.ui;

import javax.swing.JMenu;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTable;
import net.aa3sd.SMT.SMTProperties;
import net.aa3sd.SMT.SMTSingleton;
import net.aa3sd.SMT.interfaces.TaskListListener;
import net.aa3sd.SMT.resources.Preplan;
import net.aa3sd.SMT.search.Task;
import net.aa3sd.SMT.search.TaskList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPopupMenu;
import java.awt.Component;
import javax.swing.JMenuItem;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import org.apache.log4j.Logger;
import java.awt.event.KeyEvent;

/**
 * @author mole
 *
 */
public class TaskListPanel extends JPanel implements TaskListListener {

    private static final long serialVersionUID = 6997707841421093808L;

    private static final Logger log = Logger.getLogger(TaskListPanel.class);

    private JTable table;

    /**
	 * Create the panel.
	 */
    public TaskListPanel() {
        setLayout(new BorderLayout(0, 0));
        JScrollPane scrollPane = new JScrollPane();
        table = new JTable();
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
        table.setModel(SMTSingleton.getSingletonInstance().getCurrentSearch().getTasks());
        scrollPane.setViewportView(table);
        add(scrollPane, BorderLayout.CENTER);
        JPopupMenu popupMenu = new JPopupMenu();
        addPopup(table, popupMenu);
        JMenuItem mntmPrintTaskAssignment = new JMenuItem("Print Task Assignment Form");
        mntmPrintTaskAssignment.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                String taskid = ((TaskList) table.getModel()).getTaskAt(row).getIdentifier();
                ((TaskList) table.getModel()).getTaskAt(row).writeToPdf(taskid.replace(' ', '_') + "_ICS-204a-OS" + ".pdf");
            }
        });
        JMenuItem mntmEditTask = new JMenuItem("Edit Task");
        mntmEditTask.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                TaskDialog d = new TaskDialog(((TaskList) table.getModel()).getTaskAt(row));
                d.setVisible(true);
            }
        });
        popupMenu.add(mntmEditTask);
        popupMenu.add(mntmPrintTaskAssignment);
        popupMenu.addSeparator();
        JMenuItem mntmPrintTaskAssignments = new JMenuItem("Print All Task Assignment Forms");
        mntmPrintTaskAssignments.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Document document = new Document();
                if ("A4".equals(SMTSingleton.getSingletonInstance().getProperties().getProperties().get(SMTProperties.KEY_PAPERSIZE))) {
                    document.setPageSize(PageSize.A4);
                } else {
                    document.setPageSize(PageSize.LETTER);
                }
                try {
                    String filename = "TaskList_ICS-204_with_Appendixes.pdf";
                    PdfWriter.getInstance(document, new FileOutputStream(filename));
                    document.open();
                    TaskList tasks = ((TaskList) table.getModel());
                    tasks.writeToPdf(document);
                    for (int i = 0; i < tasks.getRowCount(); i++) {
                        tasks.getTaskAt(i).writeToPdf(document);
                    }
                    document.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (DocumentException ex) {
                    ex.printStackTrace();
                }
            }
        });
        popupMenu.add(mntmPrintTaskAssignments);
        JMenuItem mntmPrintTaskAssignments2 = new JMenuItem("Print All Task Assignment Forms");
        mntmPrintTaskAssignments2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Document document = new Document();
                if ("A4".equals(SMTSingleton.getSingletonInstance().getProperties().getProperties().get(SMTProperties.KEY_PAPERSIZE))) {
                    document.setPageSize(PageSize.A4);
                } else {
                    document.setPageSize(PageSize.LETTER);
                }
                try {
                    String filename = "TaskList_ICS-204_with_Appendixes.pdf";
                    PdfWriter.getInstance(document, new FileOutputStream(filename));
                    document.open();
                    TaskList tasks = ((TaskList) table.getModel());
                    tasks.writeToPdf(document);
                    for (int i = 0; i < tasks.getRowCount(); i++) {
                        tasks.getTaskAt(i).writeToPdf(document);
                    }
                    document.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (DocumentException ex) {
                    ex.printStackTrace();
                }
            }
        });
        JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);
        menuBar.add(getTaskCreationMenu());
        menuBar.add(mntmPrintTaskAssignments2);
    }

    @Override
    public void taskListChanged() {
        table.setModel(SMTSingleton.getSingletonInstance().getCurrentSearch().getTasks());
        table.validate();
    }

    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    public JMenu getTaskCreationMenu() {
        JMenu mnNewMenu = new JMenu("Add Task");
        mnNewMenu.setMnemonic(KeyEvent.VK_T);
        String[] taskSummaries = Preplan.getTaskSummaries();
        for (int i = 0; i < taskSummaries.length; i++) {
            JMenuItem mntmTask = new JMenuItem(taskSummaries[i]);
            mntmTask.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    log.debug(((JMenuItem) e.getSource()).getText());
                    Task t = new Task();
                    t.setAssignmentSummary(((JMenuItem) e.getSource()).getText());
                    SMTSingleton.getSingletonInstance().getCurrentSearch().getTasks().addTask(t);
                }
            });
            mnNewMenu.add(mntmTask);
        }
        return mnNewMenu;
    }
}
