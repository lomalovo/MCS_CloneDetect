package workflowSim;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import workflowSim.*;

/**
 * @version 0.1
 * @author Guoshen Kuang
 */
public class GUIMain extends JFrame implements ActionListener {

    GUIMain() {
        super("Workflow Simulator v0.1");
        init();
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        new GUIMain();
    }

    public void init() {
        Container c = getContentPane();
        c.setLayout(new GridLayout(4, 3));
        c.add(new JLabel("ѡ����Դ�����ļ���"));
        c.add(resourceFile);
        c.add(openResourceFile);
        c.add(new JLabel("������������ļ���"));
        c.add(pdFiles);
        c.add(addPDFile);
        c.add(new JLabel("�������ʱ�䣺"));
        c.add(endTime);
        c.add(new JLabel());
        c.add(new JLabel());
        c.add(begin);
        c.add(new JLabel());
        openResourceFile.addActionListener(this);
        openResourceFile.setActionCommand("openResourceFile");
        addPDFile.addActionListener(this);
        addPDFile.setActionCommand("addPDFile");
        begin.addActionListener(this);
        begin.setActionCommand("begin");
        this.pack();
        this.setLocation(400, 300);
        this.setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openResourceFile")) {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                resourceFile.setText(fc.getSelectedFile().getPath());
            }
        } else if (e.getActionCommand().equals("addPDFile")) {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                pdFiles.append(fc.getSelectedFile().getPath() + "\n");
                pdFileNames.add(fc.getSelectedFile().getPath());
            }
        } else if (e.getActionCommand().equals("begin")) {
            begin.setEnabled(false);
            String[] pdfs = new String[pdFileNames.size()];
            for (int i = 0; i < pdfs.length; ++i) {
                pdfs[i] = pdFileNames.get(i);
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(".\\output.xml"));
                EventLog log = new XMLEventLog(writer);
                Simulator simulator = new Simulator(pdfs, resourceFile.getText(), log);
                simulator.simulate(Double.parseDouble(endTime.getText()));
            } catch (Exception exp) {
                System.out.println(exp);
            }
            begin.setEnabled(true);
        }
    }

    private JTextField endTime = new JTextField("500");

    private JButton begin = new JButton("���з������");

    private JButton addPDFile = new JButton("���");

    private JTextArea pdFiles = new JTextArea();

    private JButton openResourceFile = new JButton("��");

    private JTextField resourceFile = new JTextField();

    private ArrayList<String> pdFileNames = new ArrayList<String>();
}
