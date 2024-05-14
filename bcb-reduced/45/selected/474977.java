package marla.ide.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import marla.ide.problem.MarlaException;
import marla.ide.problem.Problem;
import marla.ide.resource.Configuration;
import marla.ide.resource.Configuration.ConfigType;
import marla.ide.resource.Updater;

/**
 * The main frame of the stand-alone application.
 *
 * @author Alex Laird
 */
public class MainFrame extends JFrame {

    /** The minimum size the window frame is allowed to be.*/
    private final Dimension MINIMUM_WINDOW_SIZE = new Dimension(850, 400);

    /** The progress frame.*/
    public static ProgressFrame progressFrame;

    /** The panel that is added to the frame.*/
    private static ViewPanel viewPanel;

    /**
	 * Constructs the frame for the stand-alone application.
	 *
	 * @param progressFrame A reference to the progress frame.
	 */
    public MainFrame(ProgressFrame progressFrame) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MainFrame.progressFrame = progressFrame;
        viewPanel = new ViewPanel(this);
        add(viewPanel);
        initComponents();
        initMyComponents();
    }

    /**
	 * Set the configuration arguments and the visible state of the MainFrame.
	 *
	 * @param args Arguments for configuration.
	 * @param visible The visible state to set to.
	 */
    public void setVisible(final String[] args, boolean visible) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Domain.setProgressString("10%");
                Domain.setProgressValue(10);
                Domain.setProgressStatus("Loading configuration...");
                Configuration conf = Configuration.getInstance();
                List<ConfigType> missed = conf.configureAll(args);
                Domain.setProgressString("90%");
                Domain.setProgressValue(90);
                Domain.setProgressStatus("Validating configuration...");
                int currIndex = 0;
                while (currIndex < missed.size()) {
                    ConfigType curr = missed.get(currIndex);
                    boolean fixed = false;
                    try {
                        progressFrame.setAlwaysOnTop(false);
                        viewPanel.fileChooserDialog.setDialogTitle(Configuration.getName(curr));
                        viewPanel.fileChooserDialog.setDialogType(JFileChooser.OPEN_DIALOG);
                        viewPanel.fileChooserDialog.resetChoosableFileFilters();
                        viewPanel.fileChooserDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        int response = viewPanel.fileChooserDialog.showOpenDialog(Domain.getTopWindow());
                        if (response == JFileChooser.APPROVE_OPTION) {
                            conf.set(curr, viewPanel.fileChooserDialog.getSelectedFile().getPath());
                            fixed = true;
                        } else {
                            Domain.showErrorDialog(Domain.getTopWindow(), "The maRla IDE cannot run without these resources.", "Fatal Error");
                            System.exit(1);
                        }
                    } catch (MarlaException ex) {
                        System.out.println(ex.getMessage());
                        fixed = false;
                    }
                    if (fixed) currIndex++;
                }
                Domain.setProgressString("95%");
                Domain.setProgressValue(95);
                Domain.setProgressStatus("Initializating workspace...");
                try {
                    conf.save();
                } catch (MarlaException ex) {
                    System.out.println("Error saving configuration file: " + ex.getMessage());
                }
                try {
                    viewPanel.loadOperations();
                } catch (MarlaException ex) {
                    Domain.showWarningDialog(Domain.getTopWindow(), ex.getMessage(), Domain.prettyExceptionDetails(ex), "Load Error");
                }
                viewPanel.initLoading = false;
                viewPanel.newButton.setEnabled(true);
                viewPanel.openButton.setEnabled(true);
                viewPanel.settingsButton.setEnabled(true);
                progressFrame.setAlwaysOnTop(false);
                Domain.setProgressString("98%");
                Domain.setProgressValue(98);
                Domain.setProgressStatus("Checking for updates...");
                boolean isUpdate = Updater.checkForUpdates();
                Domain.setProgressString("100%");
                Domain.setProgressValue(100);
                Domain.setProgressStatus("Complete...");
                progressFrame.setVisible(false);
                setCursor(Cursor.getDefaultCursor());
                if (isUpdate) {
                    int response = Domain.showConfirmDialog(Domain.getTopWindow(), "An update for maRla is available.\nWould you like to go to the download page?", "Update Available", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        if (viewPanel.domain.desktop != null) {
                            try {
                                viewPanel.domain.desktop.browse(new URI("http://code.google.com/p/marla/downloads/list"));
                            } catch (IOException ex) {
                            } catch (URISyntaxException ex) {
                            }
                        }
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (args.length != 0 && args[args.length - 1].endsWith(".marla")) {
                            try {
                                viewPanel.domain.problem = Problem.load(args[args.length - 1]);
                                viewPanel.openProblem(false, false);
                            } catch (Exception ex) {
                                System.out.println("Unable to load file from command line: " + ex.getMessage());
                                System.out.println("Load through the GUI for more information.");
                            }
                        }
                    }
                });
            }
        }).start();
        progressFrame.setAlwaysOnTop(true);
        super.setVisible(visible);
        verifyBounds();
        progressFrame.setLocationRelativeTo(this);
    }

    /**
	 * Initializes the frame for the stand-alone application.
	 */
    private void initMyComponents() {
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        setLocationRelativeTo(null);
        setTitle(getDefaultTitle());
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                viewPanel.quit(true);
            }
        });
    }

    /**
	 * Verify that the application is within the screen resolution both in size
	 * and in location.
	 */
    private void verifyBounds() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = getWidth();
        int height = getHeight();
        if (width < MINIMUM_WINDOW_SIZE.width) {
            width = MINIMUM_WINDOW_SIZE.width;
        }
        if (height < MINIMUM_WINDOW_SIZE.height) {
            height = MINIMUM_WINDOW_SIZE.height;
        }
        if (width > screenSize.width) {
            width = screenSize.width;
        }
        if (height > screenSize.height - 30) {
            height = screenSize.height - 30;
        }
        int x = getX();
        int y = getY();
        if (x > screenSize.getWidth()) {
            x = (int) screenSize.getWidth() - width;
        }
        if (x < 0) {
            x = 0;
        }
        if (y > screenSize.getHeight()) {
            y = (int) screenSize.getHeight() - height;
        }
        if (y < 0) {
            y = 0;
        }
        setBounds(x, y, width, height);
    }

    /** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newProblemMenuItem = new javax.swing.JMenuItem();
        openProblemMenuItem = new javax.swing.JMenuItem();
        closeProblemMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        fileSeparator1 = new javax.swing.JPopupMenu.Separator();
        exportToPdfMenuItem = new javax.swing.JMenuItem();
        exportForLatexMenuItem = new javax.swing.JMenuItem();
        exportDataToCsvMenuItem = new javax.swing.JMenuItem();
        fileSeparator2 = new javax.swing.JPopupMenu.Separator();
        printMenuItem = new javax.swing.JMenuItem();
        fileSeparator3 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        problemMenu = new javax.swing.JMenu();
        editProblemMenuItem = new javax.swing.JMenuItem();
        editSubProblemsMenuItem = new javax.swing.JMenuItem();
        editConclusionMenuItem = new javax.swing.JMenuItem();
        problemSeparator1 = new javax.swing.JPopupMenu.Separator();
        newDataSetMenuItem = new javax.swing.JMenuItem();
        editDataSetsMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        operationEditorMenuItem = new javax.swing.JMenuItem();
        reloadOperationgsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        settingsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        onlineHelpMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource(Domain.IMAGES_DIR + "logo.png")).getImage());
        setName("mainFrame");
        getContentPane().setLayout(new java.awt.GridLayout(1, 1));
        fileMenu.setText("File");
        fileMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });
        newProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        newProblemMenuItem.setText("New Problem...");
        newProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProblemMenuItem);
        openProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        openProblemMenuItem.setText("Open Problem...");
        openProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openProblemMenuItem);
        closeProblemMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        closeProblemMenuItem.setText("Close Problem");
        closeProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProblemMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeProblemMenuItem);
        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);
        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        saveAsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(fileSeparator1);
        exportToPdfMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        exportToPdfMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exportToPdfMenuItem.setText("Export to PDF...");
        exportToPdfMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToPdfMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportToPdfMenuItem);
        exportForLatexMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.ALT_MASK));
        exportForLatexMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exportForLatexMenuItem.setText("Export for LaTeX...");
        exportForLatexMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportForLatexMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportForLatexMenuItem);
        exportDataToCsvMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_MASK));
        exportDataToCsvMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exportDataToCsvMenuItem.setText("Export Data to CSV...");
        exportDataToCsvMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDataToCsvMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportDataToCsvMenuItem);
        fileMenu.add(fileSeparator2);
        printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        printMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        printMenuItem.setText("Print...");
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(printMenuItem);
        fileMenu.add(fileSeparator3);
        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        editMenu.setText("Edit");
        editMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);
        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);
        menuBar.add(editMenu);
        problemMenu.setText("Problem");
        problemMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        problemMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                problemMenuMenuSelected(evt);
            }
        });
        editProblemMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editProblemMenuItem.setText("Edit Problem...");
        editProblemMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editProblemMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editProblemMenuItem);
        editSubProblemsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editSubProblemsMenuItem.setText("Edit Sub Problems...");
        editSubProblemsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editSubProblemsMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editSubProblemsMenuItem);
        editConclusionMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editConclusionMenuItem.setText("Edit Conclusion...");
        editConclusionMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editConclusionMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editConclusionMenuItem);
        problemMenu.add(problemSeparator1);
        newDataSetMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        newDataSetMenuItem.setText("Add Data Set...");
        newDataSetMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDataSetMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(newDataSetMenuItem);
        editDataSetsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        editDataSetsMenuItem.setText("Edit Data Sets...");
        editDataSetsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDataSetsMenuItemActionPerformed(evt);
            }
        });
        problemMenu.add(editDataSetsMenuItem);
        menuBar.add(problemMenu);
        toolsMenu.setText("Tools");
        toolsMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        toolsMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                toolsMenuMenuSelected(evt);
            }
        });
        operationEditorMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        operationEditorMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        operationEditorMenuItem.setText("Launch maRla Operation Editor");
        operationEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operationEditorMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(operationEditorMenuItem);
        reloadOperationgsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        reloadOperationgsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        reloadOperationgsMenuItem.setText("Reload Operations");
        reloadOperationgsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadOperationgsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(reloadOperationgsMenuItem);
        toolsMenu.add(jSeparator1);
        settingsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_MASK));
        settingsMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        settingsMenuItem.setText("Settings");
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(settingsMenuItem);
        menuBar.add(toolsMenu);
        helpMenu.setText("Help");
        helpMenu.setFont(new java.awt.Font("Verdana", 0, 12));
        helpMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                helpMenuMenuSelected(evt);
            }
        });
        onlineHelpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        onlineHelpMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        onlineHelpMenuItem.setText("Online Help");
        onlineHelpMenuItem.setEnabled(false);
        onlineHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlineHelpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(onlineHelpMenuItem);
        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        aboutMenuItem.setFont(new java.awt.Font("Verdana", 0, 12));
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        pack();
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.quit(true);
    }

    private void newProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblem();
    }

    private void openProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.load();
    }

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.save();
    }

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.saveAs();
    }

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        if (viewPanel.initLoading) {
            for (int i = 0; i < fileMenu.getMenuComponentCount(); ++i) {
                fileMenu.getMenuComponent(i).setEnabled(false);
            }
        } else {
            newProblemMenuItem.setEnabled(true);
            openProblemMenuItem.setEnabled(true);
            exitMenuItem.setEnabled(true);
            if (viewPanel.domain.problem != null) {
                exportForLatexMenuItem.setEnabled(true);
                exportToPdfMenuItem.setEnabled(true);
                if (viewPanel.domain.problem.getDataCount() > 0) {
                    exportDataToCsvMenuItem.setEnabled(true);
                } else {
                    exportDataToCsvMenuItem.setEnabled(false);
                }
                closeProblemMenuItem.setEnabled(true);
                printMenuItem.setEnabled(true);
                saveAsMenuItem.setEnabled(true);
                if (viewPanel.domain.problem.isChanged()) {
                    saveMenuItem.setEnabled(true);
                } else {
                    saveMenuItem.setEnabled(false);
                }
            } else {
                closeProblemMenuItem.setEnabled(false);
                printMenuItem.setEnabled(false);
                saveMenuItem.setEnabled(false);
                saveAsMenuItem.setEnabled(false);
                exportForLatexMenuItem.setEnabled(false);
                exportToPdfMenuItem.setEnabled(false);
                exportDataToCsvMenuItem.setEnabled(false);
            }
        }
    }

    private void problemMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        if (viewPanel.initLoading) {
            for (int i = 0; i < problemMenu.getMenuComponentCount(); ++i) {
                problemMenu.getMenuComponent(i).setEnabled(false);
            }
        } else {
            if (viewPanel.domain.problem != null) {
                editProblemMenuItem.setEnabled(true);
                editConclusionMenuItem.setEnabled(true);
                editSubProblemsMenuItem.setEnabled(true);
                newDataSetMenuItem.setEnabled(true);
                editDataSetsMenuItem.setEnabled(true);
            } else {
                editProblemMenuItem.setEnabled(false);
                editConclusionMenuItem.setEnabled(false);
                editSubProblemsMenuItem.setEnabled(false);
                newDataSetMenuItem.setEnabled(false);
                editDataSetsMenuItem.setEnabled(false);
            }
        }
    }

    private void toolsMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        if (viewPanel.initLoading) {
            for (int i = 0; i < toolsMenu.getMenuComponentCount(); ++i) {
                toolsMenu.getMenuComponent(i).setEnabled(false);
            }
        } else {
            for (int i = 0; i < toolsMenu.getMenuComponentCount(); ++i) {
                toolsMenu.getMenuComponent(i).setEnabled(true);
            }
        }
    }

    private void closeProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.closeProblem(false, false);
    }

    private void editProblemMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblemWizardDialog.editProblem();
    }

    private void newDataSetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblemWizardDialog.addDataSet();
    }

    private void reloadOperationgsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.reloadOperations();
    }

    private void exportToPdfMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.exportToPdf();
    }

    private void exportForLatexMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.exportForLatex();
    }

    private void editSubProblemsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblemWizardDialog.editSubProblems();
    }

    private void helpMenuMenuSelected(javax.swing.event.MenuEvent evt) {
        if (viewPanel.initLoading) {
            for (int i = 0; i < helpMenu.getMenuComponentCount(); ++i) {
                helpMenu.getMenuComponent(i).setEnabled(false);
            }
        } else {
            for (int i = 0; i < helpMenu.getMenuComponentCount(); ++i) {
                helpMenu.getMenuComponent(i).setEnabled(true);
            }
        }
    }

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.settingsDialog.initSettingsDialog();
        viewPanel.settingsDialog.launchSettingsDialog();
    }

    private void editConclusionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblemWizardDialog.editConclusion();
    }

    private void editDataSetsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.newProblemWizardDialog.editDataSet(null);
    }

    private void onlineHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (viewPanel.domain.desktop != null) {
            try {
                viewPanel.domain.desktop.browse(new URI("http://code.google.com/p/marla/wiki/HelpContents"));
            } catch (IOException ex) {
            } catch (URISyntaxException ex) {
            }
        }
    }

    private void operationEditorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.launchOperationEditor();
    }

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.undo();
    }

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.redo();
    }

    private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.workspacePanel.print();
    }

    private void exportDataToCsvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.domain.exportDataSet();
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        viewPanel.aboutDialog.goViewAbout();
    }

    /**
	 * Retrieves the default title, which is the program name with it's version number.
	 *
	 * @return The default title of the application.
	 */
    protected String getDefaultTitle() {
        String title = Domain.NAME;
        if (!Domain.PRE_RELEASE.equals("")) {
            title += " " + Domain.VERSION + " " + Domain.PRE_RELEASE;
        }
        return title;
    }

    private javax.swing.JMenuItem aboutMenuItem;

    private javax.swing.JMenuItem closeProblemMenuItem;

    private javax.swing.JMenuItem editConclusionMenuItem;

    private javax.swing.JMenuItem editDataSetsMenuItem;

    private javax.swing.JMenu editMenu;

    private javax.swing.JMenuItem editProblemMenuItem;

    private javax.swing.JMenuItem editSubProblemsMenuItem;

    private javax.swing.JMenuItem exitMenuItem;

    private javax.swing.JMenuItem exportDataToCsvMenuItem;

    private javax.swing.JMenuItem exportForLatexMenuItem;

    private javax.swing.JMenuItem exportToPdfMenuItem;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JPopupMenu.Separator fileSeparator1;

    private javax.swing.JPopupMenu.Separator fileSeparator2;

    private javax.swing.JPopupMenu.Separator fileSeparator3;

    private javax.swing.JMenu helpMenu;

    private javax.swing.JPopupMenu.Separator jSeparator1;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JMenuItem newDataSetMenuItem;

    private javax.swing.JMenuItem newProblemMenuItem;

    private javax.swing.JMenuItem onlineHelpMenuItem;

    private javax.swing.JMenuItem openProblemMenuItem;

    private javax.swing.JMenuItem operationEditorMenuItem;

    private javax.swing.JMenuItem printMenuItem;

    private javax.swing.JMenu problemMenu;

    private javax.swing.JPopupMenu.Separator problemSeparator1;

    protected javax.swing.JMenuItem redoMenuItem;

    private javax.swing.JMenuItem reloadOperationgsMenuItem;

    private javax.swing.JMenuItem saveAsMenuItem;

    private javax.swing.JMenuItem saveMenuItem;

    private javax.swing.JMenuItem settingsMenuItem;

    private javax.swing.JMenu toolsMenu;

    protected javax.swing.JMenuItem undoMenuItem;
}
