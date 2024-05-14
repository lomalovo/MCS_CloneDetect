package ch.skyguide.tools.requirement.hmi;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import ch.skyguide.fdp.common.AbstractObjectFilter;
import ch.skyguide.fdp.common.IMutableObjectListModel;
import ch.skyguide.fdp.common.IObjectListModel;
import ch.skyguide.fdp.common.ObjectListModel;
import ch.skyguide.fdp.common.hmi.AbortWindowClosingException;
import ch.skyguide.fdp.common.hmi.ChainedFrame;
import ch.skyguide.fdp.common.hmi.IChainedFrameClosingListener;
import ch.skyguide.fdp.common.hmi.framework.table.ObjectSelectionModel;
import ch.skyguide.fdp.common.hmi.framework.table.ObjectTableModel;
import ch.skyguide.fdp.common.hmi.framework.table.editor.CalendarEditor;
import ch.skyguide.fdp.common.hmi.framework.table.filtering.MutableDynamicTableFilter;
import ch.skyguide.fdp.common.hmi.framework.table.sorting.MutableDynamicTableSorter;
import ch.skyguide.fdp.common.hmi.memento.MementoContainer;
import ch.skyguide.fdp.common.hmi.tree.AutoScrollingJTree;
import ch.skyguide.fdp.common.hmi.tree.TreeBasedListModel;
import ch.skyguide.fdp.common.hmi.util.JTableSelectionStabilizer;
import ch.skyguide.fdp.common.hmi.util.JTreeKeyListener;
import ch.skyguide.fdp.common.hmi.util.PopupMenuMouseController;
import ch.skyguide.fdp.common.hmi.util.TabbedPaneTableController;
import ch.skyguide.fdp.common.hmi.util.draganddrop.FileDropAdapter;
import ch.skyguide.fdp.common.hmi.util.draganddrop.IFileDropReceiver;
import ch.skyguide.fdp.common.hmi.util.splitpane.AbstractMultipleSplitPaneLayout;
import ch.skyguide.fdp.common.hmi.util.splitpane.HorizontalMultipleSplitPaneLayout;
import ch.skyguide.fdp.common.hmi.util.splitpane.MultipleSplitPane;
import ch.skyguide.fdp.common.hmi.util.splitpane.VerticalMultipleSplitPaneLayout;
import ch.skyguide.tools.autosave.RecoveryDialog;
import ch.skyguide.tools.relnotes.ReleaseNotesAdapter;
import ch.skyguide.tools.relnotes.SplashScreen;
import ch.skyguide.tools.repository.data.ConnectionSettings;
import ch.skyguide.tools.repository.server.RequirementProjectException;
import ch.skyguide.tools.requirement.autosave.Autosaver;
import ch.skyguide.tools.requirement.autosave.RecoveryFileTableModel;
import ch.skyguide.tools.requirement.data.AbstractRequirement;
import ch.skyguide.tools.requirement.data.AbstractTestResult;
import ch.skyguide.tools.requirement.data.ChangeHistory;
import ch.skyguide.tools.requirement.data.LocalChangeHistory;
import ch.skyguide.tools.requirement.data.ManualTestResult;
import ch.skyguide.tools.requirement.data.ManualTestStep;
import ch.skyguide.tools.requirement.data.Preferences;
import ch.skyguide.tools.requirement.data.Requirement;
import ch.skyguide.tools.requirement.data.RequirementDomain;
import ch.skyguide.tools.requirement.data.RequirementProject;
import ch.skyguide.tools.requirement.data.Roles;
import ch.skyguide.tools.requirement.hmi.action.AboutRequirementToolAction;
import ch.skyguide.tools.requirement.hmi.action.AbstractExportTestAction;
import ch.skyguide.tools.requirement.hmi.action.AddUserAction;
import ch.skyguide.tools.requirement.hmi.action.AllTestsExecutionAction;
import ch.skyguide.tools.requirement.hmi.action.BrowseToLogFileAction;
import ch.skyguide.tools.requirement.hmi.action.CollapseAllAction;
import ch.skyguide.tools.requirement.hmi.action.ExpandAllAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTestIterationReportToDocAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTestIterationReportToPdfAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTestToDocAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTestToPdfAction;
import ch.skyguide.tools.requirement.hmi.action.ExportToDocAction;
import ch.skyguide.tools.requirement.hmi.action.ExportToPdfAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTraceMatrixToDocAction;
import ch.skyguide.tools.requirement.hmi.action.ExportTraceMatrixToPdfAction;
import ch.skyguide.tools.requirement.hmi.action.FullScreenAction;
import ch.skyguide.tools.requirement.hmi.action.GenerateTraceabilityMatrixAction;
import ch.skyguide.tools.requirement.hmi.action.GotoAction;
import ch.skyguide.tools.requirement.hmi.action.GotoProjectAction;
import ch.skyguide.tools.requirement.hmi.action.InsertVersionAction;
import ch.skyguide.tools.requirement.hmi.action.MarkApprovalAction;
import ch.skyguide.tools.requirement.hmi.action.MarkProposalAction;
import ch.skyguide.tools.requirement.hmi.action.MarkReleaseAction;
import ch.skyguide.tools.requirement.hmi.action.MarkReviewAction;
import ch.skyguide.tools.requirement.hmi.action.NewAction;
import ch.skyguide.tools.requirement.hmi.action.OpenAction;
import ch.skyguide.tools.requirement.hmi.action.OpenTestSessionAction;
import ch.skyguide.tools.requirement.hmi.action.PageSetupAction;
import ch.skyguide.tools.requirement.hmi.action.PreferencesAction;
import ch.skyguide.tools.requirement.hmi.action.QuitAction;
import ch.skyguide.tools.requirement.hmi.action.SaveAction;
import ch.skyguide.tools.requirement.hmi.action.SaveAsAction;
import ch.skyguide.tools.requirement.hmi.action.SaveAsTemplateAction;
import ch.skyguide.tools.requirement.hmi.action.SearchAction;
import ch.skyguide.tools.requirement.hmi.action.SearchNextAction;
import ch.skyguide.tools.requirement.hmi.action.SearchPreviousAction;
import ch.skyguide.tools.requirement.hmi.action.ShowSvnVersionsAction;
import ch.skyguide.tools.requirement.hmi.action.StatisticsAction;
import ch.skyguide.tools.requirement.hmi.action.TestExecutionAction;
import ch.skyguide.tools.requirement.hmi.dnd.TreeDragSource;
import ch.skyguide.tools.requirement.hmi.dnd.TreeDropTarget;
import ch.skyguide.tools.requirement.hmi.enabler.EditingStateManager;
import ch.skyguide.tools.requirement.hmi.model.BeanManagerAndTableModelFactory;
import ch.skyguide.tools.requirement.hmi.model.IssueListModel;
import ch.skyguide.tools.requirement.hmi.model.PastTestResultModel;
import ch.skyguide.tools.requirement.hmi.openoffice.FilterEnum;
import ch.skyguide.tools.requirement.hmi.openoffice.OpenOfficeContext;
import ch.skyguide.tools.requirement.hmi.openoffice.OpenOfficeException;
import ch.skyguide.tools.requirement.hmi.openoffice.OpenOfficeManager;
import ch.skyguide.tools.requirement.hmi.search.AbstractElementSeeker;
import ch.skyguide.tools.requirement.hmi.search.NodeTextSeeker;
import ch.skyguide.tools.requirement.hmi.search.SearchContext;
import ch.skyguide.tools.requirement.plugin.AuxiliaryDataPanel;
import ch.skyguide.tools.requirement.plugin.IAuxiliaryDataDescriptor;
import ch.skyguide.tools.requirement.util.LoggingHelper;
import ch.skyguide.tools.requirement.util.TestRunner;
import ch.skyguide.tools.usermgt.UserDescription;
import ch.skyguide.tools.usermgt.UserRoster;
import ch.skyguide.tools.usermgt.hmi.UserTableModel;

@SuppressWarnings("serial")
public class RequirementTool extends ChainedFrame implements Runnable, IFileDropReceiver {

    private static final boolean SHOW_VERSION_IN_TITLE = false;

    private static final Logger LOGGER = Logger.getLogger(RequirementTool.class.getName());

    public static final String ROLE_OWNER = "OWNER";

    public static final String ROLE_AUTHOR = "AUTHOR";

    public static final String ROLE_TEST_WRITER = "TEST_WRITER";

    public static final String ROLE_REVIEWER = "REVIEWER";

    public static final String ROLE_APPROVER = "APPROVER";

    public static final String ROLE_TESTER = "TESTER";

    public static final String ISSUE_MODEL = "ISSUE_MODEL";

    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }

    private class UnloadedFileDisposer implements ChangeListener, IChainedFrameClosingListener {

        private File previousSelectedFile = null;

        public void stateChanged(ChangeEvent _e) {
            File selectedFile = repositoryManager.getSelectedFile();
            if (selectedFile != previousSelectedFile) {
                previousSelectedFile = selectedFile;
            }
        }

        public void askPermissionToClose(WindowEvent _event) {
        }
    }

    private static class RequirementFilter extends AbstractObjectFilter<DefaultMutableTreeNode> {

        @Override
        public boolean accept(DefaultMutableTreeNode node) {
            if (node.isLeaf()) {
                final AbstractRequirement requirement = (AbstractRequirement) node.getUserObject();
                return !requirement.isInTrash();
            }
            return false;
        }
    }

    private static final String TITLE = "SLAM";

    private static final String CARD_NONE = "noSelection";

    private static final String CARD_REQUIREMENT = "requirementSelected";

    private static final String CARD_DOMAIN = "domainSelected";

    private static final String CARD_PROJECT = "projectSelected";

    private static final String CARD_TEST = "testSelected";

    private static final String TAB_HISTORY = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.History");

    private static final String TAB_LOCAL_HISTORY = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.LocalHistory");

    public static final String TAB_TESTS = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.Tests");

    private static final String TAB_PAST_TESTS = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.PastTests");

    public static final String TAB_REQUIREMENTS = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.Requirements");

    private static final String TAB_VERSIONS = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.Versions");

    private static final String TAB_USERS = BeanManagerAndTableModelFactory.getInstance().getTranslatedText("tab.Users");

    private static String logFilePath;

    private String releaseVersion;

    private final CursorManager cursorManager = new CursorManager(this);

    public static String getLogFilePath() {
        return logFilePath;
    }

    private String getReleaseVersion() {
        return releaseVersion;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        final RequirementTool tool = new RequirementTool();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                if (e instanceof AbortWindowClosingException) {
                    return;
                }
                System.out.print("Unexpected exception in thread ");
                System.out.println(t);
                e.printStackTrace();
                LOGGER.log(Level.WARNING, "Uncaught exception in thread " + t, e);
                String msg = e.getMessage();
                if (msg == null || msg.length() == 0) {
                    msg = e.toString();
                }
                JOptionPane.showMessageDialog(tool, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        logFilePath = initLogging();
        startSplashScreen(tool);
        initializeOpenOffice(tool);
        EventQueue.invokeLater(tool);
        if (args.length > 0) {
            int index = 0;
            if ("-open".equals(args[0]) && args.length > 1) {
                index = 1;
            }
            final File startFile = new File(args[index]);
            if (startFile.exists()) {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        final List<File> files = new ArrayList<File>(1);
                        files.add(startFile);
                        tool.receive(files);
                    }
                });
            }
        } else {
            tool.recoverLostFiles();
        }
    }

    private static void startSplashScreen(final RequirementTool tool) {
        ReleaseNotesAdapter adapter = null;
        SplashScreen splashScreen = null;
        try {
            adapter = new ReleaseNotesAdapter(RequirementTool.class.getClassLoader().getResourceAsStream("ch/skyguide/tools/requirement/ReleaseNotes.txt"), java.util.prefs.Preferences.userNodeForPackage(RequirementTool.class));
            if (adapter.hasVersionChanged()) {
                final JLabel label = new JLabel(BeanManagerAndTableModelFactory.getInstance().getTranslatedText("label.NewVersion"));
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLUE);
                splashScreen = new SplashScreen(adapter, label);
            } else {
                splashScreen = new SplashScreen(adapter);
            }
            Logger.getLogger(RequirementTool.class.getName()).log(Level.INFO, MessageFormat.format(TITLE + " version {0} started.", adapter.getCurrentVersion()));
            AboutRequirementToolAction.setupPanel(splashScreen);
            EventQueue.invokeLater(splashScreen);
        } catch (IOException e1) {
            Logger.getLogger(RequirementTool.class.getName()).log(Level.WARNING, "Unable to open the release notes");
        }
        if (adapter != null) {
            splashScreen.linkTo(tool);
            tool.setReleaseNotes(adapter);
        }
    }

    private static void initializeOpenOffice(final RequirementTool tool) {
        LOGGER.info("Initializing OpenOffice.org...");
        boolean isWriterInstalled = true;
        boolean isCalcInstalled = true;
        String errorMsg = "Mandatory application(s) are missing:";
        try {
            OpenOfficeManager.ensureWriterLoaded();
        } catch (OpenOfficeException e) {
            isWriterInstalled = false;
            errorMsg += "\nOpenOffice.org Writer";
        }
        try {
            OpenOfficeManager.ensureCalcLoaded();
        } catch (OpenOfficeException e) {
            isCalcInstalled = false;
            errorMsg += "\nOpenOffice.org Calc";
        }
        errorMsg += "\nExiting.";
        if (!isWriterInstalled || !isCalcInstalled) {
            JOptionPane.showMessageDialog(tool, errorMsg, "Fatal error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    private static String initLogging() {
        String filepath = LoggingHelper.initLogging();
        Logger.getLogger("ch.skyguide.tools.requirement.hmi.openoffice").setLevel(Level.FINER);
        Logger.getLogger("ch.skyguide.tools.requirement.persistence").setLevel(Level.FINER);
        return filepath;
    }

    private final MementoContainer mementos = new MementoContainer();

    private final EditingStateManager editingStateManager = new EditingStateManager();

    private final RequirementTreeModel requirementTreeModel = new RequirementTreeModel(this, mementos);

    private final JLabel emptyRequirementsView = new JLabel();

    private final JLabel emptyTestsView = new JLabel();

    private final RequirementDomainPanel domainPanel = new RequirementDomainPanel(this, editingStateManager);

    private final ProjectPanel projectPanel = new ProjectPanel(this, editingStateManager);

    private final RepositoryManager repositoryManager = new RepositoryManager(this, editingStateManager);

    private final NewAction newAction = new NewAction(requirementTreeModel, this);

    private final OpenAction loadAction = new OpenAction(repositoryManager);

    private final SaveAction saveAction = new SaveAction(repositoryManager, requirementTreeModel);

    private final ExportToPdfAction exportToPdfAction = new ExportToPdfAction(repositoryManager);

    private final ExportToDocAction exportToDocAction = new ExportToDocAction(repositoryManager);

    private final AbstractAction undoAction = requirementTreeModel.getUndoAction();

    private final AbstractAction redoAction = requirementTreeModel.getRedoAction();

    private final GotoAction gotoAction = new GotoAction(this);

    private final NodeTextSeeker seeker = new NodeTextSeeker(this);

    private final SearchAction searchAction = new SearchAction(seeker);

    private GotoProjectAction gotoProjectAction = new GotoProjectAction(this);

    private final SearchNextAction nextAction = new SearchNextAction(seeker);

    private final SearchPreviousAction previousAction = new SearchPreviousAction(seeker);

    private final GenerateTraceabilityMatrixAction generateTraceabilityMatrixAction = new GenerateTraceabilityMatrixAction(repositoryManager);

    private final ExportTraceMatrixToDocAction generateDocTraceabilityMatrixAction = new ExportTraceMatrixToDocAction(repositoryManager);

    private final ExportTraceMatrixToPdfAction generatePdfTraceabilityMatrixAction = new ExportTraceMatrixToPdfAction(repositoryManager);

    private final ExportTestIterationReportToPdfAction generatePdfIterationReportAction = new ExportTestIterationReportToPdfAction(repositoryManager);

    private final ExportTestIterationReportToDocAction generateDocIterationReportAction = new ExportTestIterationReportToDocAction(repositoryManager);

    private final StatusPane statusPane = new StatusPane(mementos);

    private final TestRunner testRunner = new TestRunner(this, statusPane);

    private final TestExecutionAction runTestAction = new TestExecutionAction(testRunner);

    private final OpenTestSessionAction openTestSessionAction = new OpenTestSessionAction(this);

    private final CollapseAllAction collapseAllAction = new CollapseAllAction(this);

    private final ExpandAllAction expandAllAction = new ExpandAllAction(this);

    private final RequirementFactory requirementFactory = new RequirementFactory(this, requirementTreeModel, editingStateManager);

    private final RequirementPanel requirementPanel = new RequirementPanel(this, requirementFactory, mementos, editingStateManager);

    private final TestScriptPanel testScriptPanel = new TestScriptPanel(this, mementos, editingStateManager);

    private final JTable testsTable;

    private final JTabbedPane tabbedPane;

    private final AllTestsExecutionAction allTestsExecutionAction;

    private final AbstractExportTestAction testExportToPdfAction;

    private final AbstractExportTestAction testExportToDocAction;

    private final JPopupMenu testPopupMenu = new JPopupMenu();

    private final JTree requirementTree = new AutoScrollingJTree(requirementTreeModel);

    private final IObjectListModel<AbstractRequirement> allRequirementsModel = new TreeBasedListModel<AbstractRequirement>(requirementTreeModel, new RequirementFilter());

    List<ChangeHistory> historyChangesPreview = null;

    private final VersionPanel versionPanel;

    private final AboutRequirementToolAction aboutAction = new AboutRequirementToolAction(this);

    private RecentFileManager recentFileManager;

    private IssueListModel issueListModel;

    protected IRequirementPanel lastShown;

    private List<IAuxiliaryDataDescriptor> auxiliaryDataDescriptors;

    private final FullScreenAction fullScreenAction = new FullScreenAction(this);

    private Map<AbstractRequirement, SingleRequirementFrame> requirementExternalFrames = new HashMap<AbstractRequirement, SingleRequirementFrame>();

    public RequirementTool() {
        super();
        ToolTipManager.sharedInstance().registerComponent(requirementTree);
        requirementTree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                final TreeNode node = (TreeNode) event.getPath().getLastPathComponent();
                resetExpansionHandles(node);
            }
        });
        UnloadedFileDisposer unloadedFileDisposer = new UnloadedFileDisposer();
        repositoryManager.addChangeListener(unloadedFileDisposer);
        addChainedFrameClosingListener(unloadedFileDisposer);
        editingStateManager.addAction(runTestAction);
        editingStateManager.addAction(openTestSessionAction);
        UserRoster.instance.setAvailableRoles(Roles.values());
        UserRoster.OWNER = Roles.OWNER;
        requirementTreeModel.setUserMonitor(new UserMonitor(this));
        testsTable = createTestTable();
        allTestsExecutionAction = new AllTestsExecutionAction(testRunner, getCurrentTestListModel());
        testExportToPdfAction = new ExportTestToPdfAction(repositoryManager);
        testExportToDocAction = new ExportTestToDocAction(repositoryManager);
        setJMenuBar(createJMenuBar());
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(createToolBar(), BorderLayout.NORTH);
        versionPanel = new VersionPanel(allRequirementsModel, requirementFactory, this, editingStateManager, mementos);
        requirementTreeModel.addTreeModelListener(versionPanel);
        tabbedPane = createContentPane();
        allTestsExecutionAction.setTabToShow(tabbedPane, TAB_TESTS);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.SOUTH);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        centerOnScreen(new Dimension(Math.min(screenSize.width, 1000), Math.min(screenSize.height, 830)));
        addChainedFrameClosingListener(requirementTreeModel);
        domainPanel.linkToFrame(this);
        requirementPanel.linkToFrame(this);
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                setStatusMessage("", false);
            }
        });
        buildTestMenu();
        requirementTreeModel.reset();
        if (IssueListModel.hasIssueTrackerServices()) {
            connectToJira();
        }
        setCloseOperator(EXIT_CLOSE_OPERATOR);
        repositoryManager.addChangeListener(statusPane);
        repositoryManager.addChangeListener(editingStateManager);
        new FileDropAdapter(this, this);
        final Autosaver autosaver = new Autosaver(this, 60 * Preferences.instance.getAutosaveDelay(), TimeUnit.SECONDS);
        mementos.addMementoEventListener(autosaver);
        addChainedFrameClosingListener(new IChainedFrameClosingListener() {

            public void askPermissionToClose(WindowEvent _event) {
                autosaver.terminate();
                try {
                    OpenOfficeManager.dispose();
                } catch (OpenOfficeException e) {
                    LOGGER.log(Level.WARNING, "Failed to dispose " + OpenOfficeManager.class.getSimpleName(), e);
                }
            }
        });
        checkOOoVersion();
    }

    private void checkOOoVersion() {
        String oooVersion;
        try {
            oooVersion = OpenOfficeManager.getContext().getProductVersion();
        } catch (OpenOfficeException e) {
            LOGGER.log(Level.WARNING, "Failed to query OpenOffice.org product version", e);
            oooVersion = null;
        }
        String text;
        if (oooVersion == null) {
            text = "Failed to query OpenOffice.org product version.";
        } else {
            if (!OpenOfficeContext.EXPECTED_PRODUCT_VERSION.equals(oooVersion)) {
                text = "This application requires OpenOffice.org version " + OpenOfficeContext.EXPECTED_PRODUCT_VERSION + ".\nYou have OpenOffice.org version " + oooVersion + " installed, which might cause unexpected problems.";
            } else {
                text = null;
            }
        }
        if (text != null) {
            JOptionPane.showMessageDialog(this, text, "OpenOffice.org version", JOptionPane.WARNING_MESSAGE);
        }
    }

    public RequirementTool(final File file) {
        this();
        repositoryManager.waitForLoad(file);
    }

    public void save() throws Exception {
        repositoryManager.waitForSave();
    }

    private JTabbedPane createContentPane() {
        final JTabbedPane pane = new JTabbedPane();
        pane.addTab(TAB_REQUIREMENTS, createRequirementsPanel());
        pane.addTab(TAB_HISTORY, new JScrollPane(createHistoryTable()));
        pane.addTab(TAB_LOCAL_HISTORY, createLocalHistoryPanel());
        pane.addTab(TAB_TESTS, createTestsPanel());
        pane.addTab(TAB_PAST_TESTS, createPastTestsPanel());
        pane.addTab(TAB_VERSIONS, versionPanel);
        for (IAuxiliaryDataDescriptor descriptor : getAuxiliaryDataDescriptors()) {
            addAuxiliaryData(pane, descriptor);
        }
        new TabbedPaneTableController(pane);
        final UserTableModel userTableModel = BeanManagerAndTableModelFactory.getInstance().createUserTableModel();
        final JTable table = new JTable(userTableModel);
        UserRoster.instance.addPropertyChangeListener(UserRoster.USER, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                final UserDescription user = (UserDescription) evt.getNewValue();
                final int index = UserRoster.instance.indexOf(user);
                if (index != -1) {
                    final ListSelectionModel selectionModel = table.getSelectionModel();
                    selectionModel.setSelectionInterval(index, index);
                }
            }
        });
        final JPopupMenu menu = new JPopupMenu();
        addMenuAction(menu, BeanManagerAndTableModelFactory.getInstance().createUserDeleteAction(table));
        addMenuAction(menu, new AddUserAction(this));
        menu.addSeparator();
        addMenuAction(menu, new MarkReviewAction(requirementTreeModel, repositoryManager, table));
        addMenuAction(menu, new MarkProposalAction(requirementTreeModel, repositoryManager, table));
        addMenuAction(menu, new MarkApprovalAction(requirementTreeModel, repositoryManager, table));
        addMenuAction(menu, new MarkReleaseAction(requirementTreeModel, repositoryManager, table));
        final PopupMenuMouseController controller = new PopupMenuMouseController(menu);
        table.addMouseListener(controller);
        editingStateManager.addUserTable(table);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addMouseListener(controller);
        pane.addTab(TAB_USERS, scrollPane);
        pane.addChangeListener(requirementFactory);
        pane.addChangeListener(fullScreenAction);
        return pane;
    }

    private Component createPastTestsPanel() {
        final JTable table = requirementTreeModel.getPastTestResultTable();
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    final int[] selectedRows = table.getSelectedRows();
                    if (selectedRows.length == 1) {
                        final PastTestResultModel tableModel = (PastTestResultModel) table.getModel();
                        displayTest(tableModel.getTestAtRow(selectedRows[0]));
                    }
                }
            }
        });
        return new JScrollPane(table);
    }

    private void addMenuAction(final JPopupMenu menu, final AbstractAction action) {
        menu.add(action);
        editingStateManager.addAction(action);
    }

    private JComponent createLocalHistoryPanel() {
        final JTable table = new JTable(requirementTreeModel.getLocalHistoryTableModel());
        BeanManagerAndTableModelFactory.getInstance().assignLocalHistoryTableEditors(table);
        new MutableDynamicTableFilter<LocalChangeHistory>(table);
        final JScrollPane scrollPane = new JScrollPane(table);
        final RequirementLocalHistoryNavigator navigator = new RequirementLocalHistoryNavigator(this, new ObjectSelectionModel<LocalChangeHistory>(table));
        table.addMouseListener(navigator);
        return scrollPane;
    }

    @SuppressWarnings("unchecked")
    private JTable createHistoryTable() {
        final ObjectTableModel<ChangeHistory> historyTableModel = BeanManagerAndTableModelFactory.getInstance().createHistoryTableModel(new ObjectListModel<ChangeHistory>());
        final JTable table = new JTable(historyTableModel);
        BeanManagerAndTableModelFactory.getInstance().assignHistoryTableEditors(table);
        new MutableDynamicTableSorter<ChangeHistory>(table);
        new MutableDynamicTableFilter<ChangeHistory>(table);
        new TableModelRoleEditingManager((ObjectTableModel<ChangeHistory>) table.getModel(), editingStateManager, Roles.AUTHOR);
        requirementTreeModel.setHistoryTableModel((ObjectTableModel<ChangeHistory>) table.getModel());
        final ObjectSelectionModel<ChangeHistory> selectionModel = new ObjectSelectionModel<ChangeHistory>(table);
        table.addMouseListener(new ChangeHistoryNavigator(this, selectionModel));
        final JPopupMenu menu = new JPopupMenu();
        menu.add(new InsertVersionAction(requirementTreeModel, selectionModel));
        table.addMouseListener(new PopupMenuMouseController(menu));
        return table;
    }

    @SuppressWarnings("unchecked")
    public JTable createHistoryTablePreview() {
        historyChangesPreview = getRequirementTreeModel().collectHistory();
        ObjectListModel<ChangeHistory> model = new ObjectListModel<ChangeHistory>();
        model.addAll(historyChangesPreview);
        final ObjectTableModel<ChangeHistory> historyTableModel = BeanManagerAndTableModelFactory.getInstance().createHistoryTableModel(model);
        final JTable table = new JTable(historyTableModel);
        BeanManagerAndTableModelFactory.getInstance().assignHistoryTableEditors(table);
        new MutableDynamicTableSorter<ChangeHistory>(table);
        new MutableDynamicTableFilter<ChangeHistory>(table);
        new TableModelRoleEditingManager((ObjectTableModel<ChangeHistory>) table.getModel(), editingStateManager, Roles.AUTHOR);
        return table;
    }

    private JComponent createRequirementsPanel() {
        new TreeDragSource(requirementTree, editingStateManager);
        new TreeDropTarget(this, requirementTree);
        requirementFactory.setTree(requirementTree);
        requirementTree.addTreeWillExpandListener(new TreeWillExpandListener() {

            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                final TreePath path = event.getPath();
                if (path.getLastPathComponent() == requirementTreeModel.getRoot()) {
                    throw new ExpandVetoException(event);
                }
            }

            public void treeWillExpand(TreeExpansionEvent event) {
            }
        });
        requirementTree.addMouseListener(requirementFactory);
        requirementTree.setCellRenderer(new RequirementTreeCellRenderer());
        final TreeSelectionModel selectionModel = requirementTree.getSelectionModel();
        requirementTreeModel.setTreeSelectionModel(selectionModel);
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        final CardLayout requirementViewLayout = new CardLayout();
        final JPanel panel = new JPanel(requirementViewLayout);
        panel.add(emptyRequirementsView, CARD_NONE);
        panel.add(projectPanel, CARD_PROJECT);
        panel.add(domainPanel, CARD_DOMAIN);
        panel.add(requirementPanel, CARD_REQUIREMENT);
        selectionModel.addTreeSelectionListener(seeker);
        selectionModel.addTreeSelectionListener(new TreeSelectionListener() {

            private void setLastShown(IRequirementPanel aPanel) {
                if (lastShown != null && lastShown != aPanel) {
                    lastShown.setDomain(null, null);
                }
                lastShown = aPanel;
                fullScreenAction.setEnabled(lastShown != null && lastShown.hasTextArea());
            }

            public void valueChanged(TreeSelectionEvent e) {
                if (selectionModel.getSelectionPath() == null) {
                    setLastShown(null);
                    requirementViewLayout.show(panel, CARD_NONE);
                } else {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionModel.getSelectionPath().getLastPathComponent();
                    if (node.getUserObject() instanceof Requirement) {
                        final AbstractRequirement requirement = (AbstractRequirement) node.getUserObject();
                        if (requirement.isInTrash()) {
                            setLastShown(domainPanel);
                            domainPanel.setDomain(node, requirement);
                            requirementViewLayout.show(panel, CARD_DOMAIN);
                        } else {
                            setLastShown(requirementPanel);
                            requirementPanel.setDomain(node, requirement);
                            requirementViewLayout.show(panel, CARD_REQUIREMENT);
                        }
                    } else if (node.getUserObject() instanceof RequirementProject) {
                        setLastShown(projectPanel);
                        final RequirementProject project = (RequirementProject) node.getUserObject();
                        projectPanel.setDomain(node, project);
                        requirementViewLayout.show(panel, CARD_PROJECT);
                    } else if (node.getUserObject() instanceof RequirementDomain) {
                        setLastShown(domainPanel);
                        final RequirementDomain domain = (RequirementDomain) node.getUserObject();
                        domainPanel.setDomain(node, domain);
                        requirementViewLayout.show(panel, CARD_DOMAIN);
                    } else {
                        setLastShown(null);
                        requirementViewLayout.show(panel, CARD_NONE);
                    }
                }
            }
        });
        final JScrollPane treeScrollPane = new JScrollPane(requirementTree);
        treeScrollPane.putClientProperty(AbstractMultipleSplitPaneLayout.RELATIVE_SIZE, new Integer(1));
        panel.putClientProperty(AbstractMultipleSplitPaneLayout.RELATIVE_SIZE, new Integer(4));
        final MultipleSplitPane pane = new MultipleSplitPane(new HorizontalMultipleSplitPaneLayout(), treeScrollPane, panel);
        pane.setDividerSize(5);
        selectionModel.addTreeSelectionListener(requirementFactory);
        requirementTree.addKeyListener(new JTreeKeyListener(requirementTree));
        Preferences.addUserPreferenceChangeListener(new PreferenceChangeListener() {

            public void preferenceChange(PreferenceChangeEvent event) {
                if (Preferences.DISPLAY_CODE.equals(event.getKey()) || Preferences.DISPLAY_HEADING.equals(event.getKey())) {
                    requirementTree.setCellRenderer(new RequirementTreeCellRenderer());
                }
            }
        });
        return pane;
    }

    private JComponent createTestsPanel() {
        final ObjectSelectionModel<AbstractTestResult> selectionModel = new ObjectSelectionModel<AbstractTestResult>(testsTable);
        runTestAction.setSelectionModel(selectionModel);
        testExportToPdfAction.setSelectionModel(selectionModel);
        testExportToDocAction.setSelectionModel(selectionModel);
        openTestSessionAction.setSelectionModel(selectionModel);
        testsTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        testsTable.setDefaultEditor(Date.class, new CalendarEditor());
        final CardLayout testViewLayout = new CardLayout();
        final JPanel panel = new JPanel(testViewLayout);
        panel.add(emptyTestsView, CARD_NONE);
        panel.add(testScriptPanel, CARD_TEST);
        final JScrollPane testScrollPane = new JScrollPane(testsTable);
        new JTableSelectionStabilizer(testScrollPane);
        final MouseAdapter mouseAdapter = new PopupMenuMouseController(testPopupMenu);
        testsTable.addMouseListener(mouseAdapter);
        testScrollPane.addMouseListener(mouseAdapter);
        editingStateManager.addTable(testsTable, Roles.AUTHOR, Roles.TEST_WRITER, Roles.TESTER);
        JComponent testRelatedRequirementPanel = createTestRelatedRequirementPanel(selectionModel);
        testRelatedRequirementPanel.setName("testRelatedRequirementPanel");
        final MultipleSplitPane tablesPane = new MultipleSplitPane(new VerticalMultipleSplitPaneLayout(), testScrollPane, testRelatedRequirementPanel);
        tablesPane.putClientProperty(AbstractMultipleSplitPaneLayout.RELATIVE_SIZE, new Integer(1));
        panel.putClientProperty(AbstractMultipleSplitPaneLayout.RELATIVE_SIZE, new Integer(2));
        final MultipleSplitPane pane = new MultipleSplitPane(new HorizontalMultipleSplitPaneLayout(), tablesPane, panel);
        pane.setDividerSize(5);
        selectionModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent _e) {
                if (selectionModel.getSelectionSize() == 1) {
                    final AbstractTestResult test = getCurrentTestListModel().getObjectAt(testsTable.getSelectedRow());
                    testScriptPanel.setTest(test);
                    testViewLayout.show(panel, CARD_TEST);
                } else {
                    testViewLayout.show(panel, CARD_NONE);
                }
            }
        });
        return pane;
    }

    private JComponent createTestRelatedRequirementPanel(final ObjectSelectionModel<AbstractTestResult> selectionModel) {
        final TestRelatedRequirementFilter filter = new TestRelatedRequirementFilter(selectionModel, requirementTreeModel.getAllTestsModel());
        final IObjectListModel<AbstractRequirement> model = new TreeBasedListModel<AbstractRequirement>(requirementTreeModel, filter);
        final ObjectTableModel<AbstractRequirement> tableModel = BeanManagerAndTableModelFactory.getInstance().createRequirementSummaryTableModel(model);
        final JTable table = new JTable(tableModel);
        table.setName("testRelatedRequirementTable");
        new MutableDynamicTableSorter<AbstractRequirement>(table);
        final RequirementNavigator navigator = new RequirementNavigator(this, new ObjectSelectionModel<AbstractRequirement>(table));
        table.addMouseListener(navigator);
        return new JScrollPane(table);
    }

    private JTable createTestTable() {
        final ObjectTableModel<AbstractTestResult> tableModel = BeanManagerAndTableModelFactory.getInstance().createTestTableModel(requirementTreeModel.getAllTestsModel());
        final JTable table = new JTable(tableModel);
        final MutableDynamicTableSorter<AbstractTestResult> sorter = new MutableDynamicTableSorter<AbstractTestResult>(table);
        sorter.setInitialSorting("Name");
        new MutableDynamicTableFilter<AbstractTestResult>(table);
        BeanManagerAndTableModelFactory.getInstance().assignTestResultTableEditors(table);
        return table;
    }

    public void run() {
        setVisible(true);
    }

    private JMenuBar createJMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createSWALCHMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createWindowMenu());
        return menuBar;
    }

    private JMenu createSWALCHMenu() {
        final JMenu menu = new JMenu(BeanManagerAndTableModelFactory.getInstance().getTranslatedText("menu.Documents"));
        menu.add(generateTraceabilityMatrixAction);
        menu.add(generateDocTraceabilityMatrixAction);
        menu.add(generatePdfTraceabilityMatrixAction);
        menu.add(generateDocIterationReportAction);
        menu.add(generatePdfIterationReportAction);
        return menu;
    }

    private JToolBar createToolBar() {
        final JToolBar bar = new JToolBar("ADM tools");
        bar.add(newAction);
        bar.addSeparator();
        bar.add(loadAction);
        bar.add(saveAction);
        bar.add(undoAction);
        bar.add(redoAction);
        bar.addSeparator();
        bar.add(fullScreenAction);
        bar.addSeparator();
        bar.add(gotoProjectAction);
        bar.add(gotoAction);
        bar.add(searchAction);
        bar.add(previousAction);
        bar.add(nextAction);
        bar.addSeparator();
        bar.add(exportToPdfAction);
        bar.add(exportToDocAction);
        return bar;
    }

    private JMenu createFileMenu() {
        final JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.add(newAction);
        menu.addSeparator();
        menu.add(loadAction);
        menu.addSeparator();
        menu.add(saveAction);
        menu.add(new SaveAsAction(repositoryManager));
        menu.add(new SaveAsTemplateAction(repositoryManager));
        menu.addSeparator();
        menu.add(new PageSetupAction(this));
        menu.add(exportToPdfAction);
        menu.add(exportToDocAction);
        recentFileManager = new RecentFileManager(repositoryManager, menu);
        menu.addSeparator();
        menu.add(new QuitAction(this));
        return menu;
    }

    private JMenu createEditMenu() {
        final JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(requirementFactory.getAddDomainAction());
        menu.add(requirementFactory.getAddRequirementAction());
        menu.add(requirementFactory.getRemoveAction());
        menu.add(requirementFactory.getActivateAction());
        menu.addSeparator();
        menu.add(requirementFactory.getAddAutomatedTestAction());
        menu.add(requirementFactory.getAddManualTestAction());
        menu.add(requirementFactory.getAssociateTestAction());
        menu.addSeparator();
        menu.add(undoAction);
        menu.add(redoAction);
        menu.addSeparator();
        menu.add(searchAction);
        menu.add(nextAction);
        menu.add(previousAction);
        menu.addSeparator();
        menu.add(gotoProjectAction);
        menu.add(gotoAction);
        return menu;
    }

    private JMenu createTestMenu() {
        final JMenu menu = new JMenu("Test");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.add(allTestsExecutionAction);
        return menu;
    }

    private JMenu createViewMenu() {
        final JMenu menu = new JMenu(BeanManagerAndTableModelFactory.getInstance().getTranslatedText("menu.View"));
        menu.setMnemonic(KeyEvent.VK_V);
        menu.add(fullScreenAction);
        menu.addSeparator();
        menu.add(new StatisticsAction(repositoryManager));
        menu.addSeparator();
        menu.add(expandAllAction);
        menu.add(collapseAllAction);
        return menu;
    }

    private JMenu createWindowMenu() {
        final JMenu menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menu.add(new PreferencesAction(this));
        menu.addSeparator();
        menu.add(new BrowseToLogFileAction());
        menu.add(new ShowSvnVersionsAction(this));
        menu.add(aboutAction);
        return menu;
    }

    public RequirementTreeModel getRequirementTreeModel() {
        return requirementTreeModel;
    }

    public AbstractRequirement getSelectedRequirement() {
        TreePath selectionPath = requirementTree.getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        Object userObject = node.getUserObject();
        return (AbstractRequirement) userObject;
    }

    @SuppressWarnings("unchecked")
    protected IMutableObjectListModel<AbstractTestResult> getCurrentTestListModel() {
        final ObjectTableModel<AbstractTestResult> tableModel = (ObjectTableModel<AbstractTestResult>) testsTable.getModel();
        return (IMutableObjectListModel<AbstractTestResult>) tableModel.getModel();
    }

    public void updateTitle(final File file) {
        final StringBuilder buffer = new StringBuilder(TITLE);
        if (SHOW_VERSION_IN_TITLE) {
            buffer.append(' ');
            buffer.append(getReleaseVersion());
        }
        if (file != null) {
            buffer.append(" [");
            buffer.append(file.getAbsolutePath());
            buffer.append(']');
            if (!file.canWrite()) {
                buffer.append(" (Read-Only)");
            }
        }
        setTitle(buffer.toString());
    }

    public void setStatusMessage(String _message, boolean _showProgressBar) {
        statusPane.setStatusMessage(_message, _showProgressBar);
    }

    public JProgressBar getStatusProgressBar() {
        return statusPane.getStatusProgressBar();
    }

    public TestRunner getTestRunner() {
        return testRunner;
    }

    private void buildTestMenu() {
        testPopupMenu.add(openTestSessionAction);
        testPopupMenu.add(testExportToPdfAction);
        testPopupMenu.add(testExportToDocAction);
        testPopupMenu.add(allTestsExecutionAction);
        testPopupMenu.add(runTestAction);
    }

    public void displayRequirement(AbstractRequirement requirement) {
        final DefaultMutableTreeNode node = requirementTreeModel.getNode(requirement);
        if (node != null) {
            displayNode(node);
        }
    }

    public void displayProject() {
        displayNode((DefaultMutableTreeNode) requirementTreeModel.getRoot());
    }

    public void displayNode(final DefaultMutableTreeNode node) {
        showTab(TAB_REQUIREMENTS);
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                final TreePath treePath = new TreePath(node.getPath());
                requirementTree.scrollPathToVisible(treePath);
                requirementTree.getSelectionModel().setSelectionPath(treePath);
            }
        });
    }

    public void showLocalHistory() {
        showTab(TAB_LOCAL_HISTORY);
    }

    public void showRequirements() {
        showTab(TAB_REQUIREMENTS);
    }

    @SuppressWarnings("unchecked")
    public Enumeration<DefaultMutableTreeNode> getNodes() {
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) requirementTreeModel.getRoot();
        return root.depthFirstEnumeration();
    }

    private void setReleaseNotes(ReleaseNotesAdapter adapter) {
        releaseVersion = adapter.getCurrentVersion();
        updateTitle(null);
        aboutAction.setAdapter(adapter);
    }

    public RecentFileManager getRecentFileManager() {
        return recentFileManager;
    }

    private void showTab(final String tabName) {
        final int tabIndex = tabbedPane.indexOfTab(tabName);
        if (tabbedPane.getSelectedIndex() != tabIndex) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }

    public void displayTest(AbstractTestResult testResult) {
        displayTest(testResult, false);
    }

    public void redisplayTest(AbstractTestResult testResult) {
        displayTest(testResult, true);
    }

    @SuppressWarnings("unchecked")
    private void displayTest(final AbstractTestResult testResult, final boolean forceSelection) {
        showTab(TAB_TESTS);
        final ObjectTableModel<AbstractTestResult> tableModel = (ObjectTableModel<AbstractTestResult>) testsTable.getModel();
        final int[] selectedRows = testsTable.getSelectedRows();
        if (forceSelection && selectedRows.length == 1) {
            if (tableModel.getObjectAt(selectedRows[0]) == testResult) {
                testsTable.clearSelection();
            }
        }
        final int index = tableModel.getModel().indexOf(testResult);
        if (index != -1) {
            testsTable.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    public void displayTestStep(ManualTestStep step) {
        displayTestStep(step, false);
    }

    public void redisplayTestStep(ManualTestStep step) {
        displayTestStep(step, true);
    }

    @SuppressWarnings("unchecked")
    public void displayTestStep(ManualTestStep step, final boolean forceSelection) {
        final ObjectTableModel<AbstractTestResult> tableModel = (ObjectTableModel<AbstractTestResult>) testsTable.getModel();
        for (AbstractTestResult test : tableModel.getModel()) {
            if (test.isManual()) {
                final ManualTestResult manualTest = (ManualTestResult) test;
                if (manualTest.contains(step)) {
                    displayTest(manualTest);
                    testScriptPanel.displayTestStep(this, step, forceSelection);
                }
            }
        }
    }

    public void redisplayRequirement(AbstractRequirement requirement) {
        final DefaultMutableTreeNode node = requirementTreeModel.getNode(requirement);
        if (node != null) {
            redisplayNode(node);
        }
    }

    public void redisplayNode(final DefaultMutableTreeNode node) {
        showTab(TAB_REQUIREMENTS);
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                final TreePath treePath = new TreePath(node.getPath());
                if (requirementTree.getSelectionCount() == 1) {
                    if (treePath.equals(requirementTree.getSelectionPath())) {
                        requirementTree.clearSelection();
                    }
                }
                requirementTree.scrollPathToVisible(treePath);
                requirementTree.getSelectionModel().setSelectionPath(treePath);
            }
        });
    }

    public static void assignEscapeKey(final JButton button) {
        InputMap inputMap = SwingUtilities.getUIInputMap(button, JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (inputMap == null) {
            inputMap = new ComponentInputMapUIResource(button);
            SwingUtilities.replaceUIInputMap(button, JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
        } else {
            inputMap.clear();
        }
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "released");
    }

    public void connectToJira() {
        final IssueListModel oldConnection = issueListModel;
        final String jiraURL = Preferences.instance.getJiraURL();
        if (jiraURL == null) {
            issueListModel = null;
        } else {
            final String username = Preferences.instance.getJiraUsername();
            final ConnectionSettings newConnection = new ConnectionSettings(username, Preferences.instance.getJiraPassword());
            newConnection.setAddress(jiraURL);
            try {
                issueListModel = new IssueListModel(this, newConnection);
            } catch (RequirementProjectException e) {
                LOGGER.log(Level.SEVERE, "JIRA access error", e);
                JOptionPane.showMessageDialog(this, MessageFormat.format(BeanManagerAndTableModelFactory.getInstance().getTranslatedText("error.JIRA.connection"), jiraURL, username), BeanManagerAndTableModelFactory.getInstance().getTranslatedText("error.message.console"), JOptionPane.ERROR_MESSAGE);
                issueListModel = null;
            }
        }
        if (issueListModel != oldConnection) {
            firePropertyChange(ISSUE_MODEL, oldConnection, issueListModel);
        }
    }

    public boolean hasPendingUpdates() {
        return lastShown != null && lastShown.hasPendingUpdates();
    }

    public IssueListModel getIssueListModel() {
        return issueListModel;
    }

    public void resetSelectedFile() {
        repositoryManager.resetSelectedFile();
    }

    public void updateActionsEnabledState() {
        requirementFactory.updateActionsEnabledState();
    }

    public void collectOpenOfficeChanges() {
        if (lastShown != null && lastShown.hasPendingUpdates()) {
            lastShown.savePendingUpdates();
        }
    }

    private void addAuxiliaryData(final JTabbedPane pane, final IAuxiliaryDataDescriptor descriptor) {
        pane.addTab(descriptor.getTabLabel(), new AuxiliaryDataPanel(this, descriptor));
    }

    public Iterable<IAuxiliaryDataDescriptor> getAuxiliaryDataDescriptors() {
        if (auxiliaryDataDescriptors == null) {
            auxiliaryDataDescriptors = new ArrayList<IAuxiliaryDataDescriptor>();
            final Iterator<?> providers = sun.misc.Service.providers(IAuxiliaryDataDescriptor.class, RequirementTool.class.getClassLoader());
            while (providers.hasNext()) {
                auxiliaryDataDescriptors.add((IAuxiliaryDataDescriptor) providers.next());
            }
        }
        return auxiliaryDataDescriptors;
    }

    public void redisplayTestsInRequirement(Requirement requirement, List<AbstractTestResult> tests) {
        redisplayRequirement(requirement);
        requirementPanel.redisplayTests(tests);
    }

    public void setFullScreen() {
        final int index = tabbedPane.getSelectedIndex();
        final String tabLabel = tabbedPane.getTitleAt(index);
        if (TAB_REQUIREMENTS.equals(tabLabel)) {
            lastShown.setFullScreen();
            getTreeScrollPane().setVisible(false);
        } else if (TAB_TESTS.equals(tabLabel)) {
            tabbedPane.invalidate();
            testScriptPanel.setFullScreen();
            final MultipleSplitPane splitPane = (MultipleSplitPane) tabbedPane.getComponentAt(index);
            splitPane.getComponent(0).setVisible(false);
            tabbedPane.revalidate();
            tabbedPane.repaint();
        }
    }

    public void setNonFullScreen() {
        final int index = tabbedPane.getSelectedIndex();
        final String tabLabel = tabbedPane.getTitleAt(index);
        if (TAB_REQUIREMENTS.equals(tabLabel)) {
            lastShown.setNonFullScreen();
            getTreeScrollPane().setVisible(true);
        } else if (TAB_TESTS.equals(tabLabel)) {
            tabbedPane.invalidate();
            testScriptPanel.setNonFullScreen();
            final MultipleSplitPane splitPane = (MultipleSplitPane) tabbedPane.getComponentAt(index);
            splitPane.getComponent(0).setVisible(true);
            tabbedPane.revalidate();
            tabbedPane.repaint();
        }
    }

    private Container getTreeScrollPane() {
        return SwingUtilities.getAncestorOfClass(JScrollPane.class, requirementTree);
    }

    public void export(File _file, FilterEnum _filter) {
        requirementFactory.export(repositoryManager, _file, _filter);
    }

    public void collapseAll() {
        collapse(requirementTreeModel.getProject());
    }

    public void collapse(RequirementDomain requirement) {
        final TreeNode node = requirementTreeModel.getNode(requirement);
        expandAll(requirementTree, new TreePath(requirementTreeModel.getPathToRoot(node)), false);
    }

    public void expandAll() {
        expand(requirementTreeModel.getProject());
    }

    public void expand(RequirementDomain requirement) {
        final TreeNode node = requirementTreeModel.getNode(requirement);
        expandAll(requirementTree, new TreePath(requirementTreeModel.getPathToRoot(node)), true);
    }

    public void showPopup(AbstractRequirement _requirement) {
        SingleRequirementFrame reqFrame = requirementExternalFrames.get(_requirement);
        if (reqFrame == null) {
            reqFrame = new SingleRequirementFrame(this, _requirement);
            reqFrame.getPanel().linkToFrame(this);
            requirementExternalFrames.put(_requirement, reqFrame);
        } else {
            reqFrame.refresh(_requirement);
        }
        reqFrame.setVisible(true);
        reqFrame.requestFocus();
    }

    @SuppressWarnings("unchecked")
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        final TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements(); ) {
                final TreeNode n = e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public boolean canReceive(List<File> files) {
        return files.size() == 1 && files.get(0).isFile();
    }

    public void receive(List<File> files) {
        if (files.size() == 1) {
            repositoryManager.loadGivenFile(files.get(0));
        }
    }

    public void displaySearchResult(final AbstractElementSeeker elementSeeker, final SearchContext searchContext) {
        showTab(TAB_REQUIREMENTS);
        final TreePath selectionPath = requirementTree.getSelectionModel().getSelectionPath();
        final DefaultMutableTreeNode node = elementSeeker.getNode();
        if (selectionPath != null && selectionPath.getLastPathComponent() == node) {
            elementSeeker.displayResult(lastShown, searchContext);
        } else {
            final TreePath treePath = new TreePath(node.getPath());
            requirementTree.scrollPathToVisible(treePath);
            requirementTree.getSelectionModel().setSelectionPath(treePath);
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    elementSeeker.displayResult(lastShown, searchContext);
                }
            });
        }
    }

    public CursorManager getCursorManager() {
        return cursorManager;
    }

    public boolean isSaved() {
        return mementos.isSaved();
    }

    public void resetExpansionHandles() {
        resetExpansionHandles((TreeNode) requirementTreeModel.getRoot());
    }

    protected void resetExpansionHandles(final TreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            final TreeNode child = node.getChildAt(i);
            if (child.getChildCount() == 0) {
                requirementTree.expandPath(new TreePath(requirementTreeModel.getPathToRoot(child)));
            }
        }
    }

    public void jumpToVersionField() {
        if (lastShown instanceof ProjectPanel) {
            ((ProjectPanel) lastShown).jumpToVersionField();
        }
    }

    public MementoContainer getMementos() {
        return mementos;
    }

    public void displayHistoryTable() {
        showTab(TAB_HISTORY);
    }

    public void updateProject() {
        projectPanel.setDomain((DefaultMutableTreeNode) requirementTreeModel.getRoot(), requirementTreeModel.getProject());
    }

    private void recoverLostFiles() {
        final RecoveryFileTableModel model = new RecoveryFileTableModel(this);
        final RecoveryDialog recoveryDialog = new RecoveryDialog(this, model, BeanManagerAndTableModelFactory.getInstance().getTranslatedText("hint.Recovery"));
        recoveryDialog.setTitle(BeanManagerAndTableModelFactory.getInstance().getTranslatedText("title.Recovery"));
        if (recoveryDialog.hasSelectedFile()) {
            recoveryDialog.setVisible(true);
        }
    }
}
