package com.generatescape.views;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import com.generatescape.actions.AddBookmarkAction;
import com.generatescape.actions.EmailAction;
import com.generatescape.actions.OpenFileAction;
import com.generatescape.baseobjects.CONSTANTS;
import com.generatescape.baseobjects.SearchHolder;
import com.generatescape.com.feedutils.TreeExpandFeedParser;
import com.generatescape.dnd.CanalDragListener;
import com.generatescape.dnd.CanalTransfer;
import com.generatescape.dnd.CanalTreeDropAdapter;
import com.generatescape.feedwriter.FeedWriter;
import com.generatescape.filehandling.SafeSaveDialog;
import com.generatescape.htmlutils.RSSAutoDetector;
import com.generatescape.library.Library;
import com.generatescape.mediaminder.SearchCatcher;
import com.generatescape.newtreemodel.CanalGraph;
import com.generatescape.newtreemodel.NewCanalNode;
import com.generatescape.pdf.PDFDocumentFactory;
import com.generatescape.preferences.PrefPageOne;
import com.generatescape.threading.JobHandler;
import com.generatescape.treesystem.CanalContentProvider;
import com.generatescape.treesystem.CanalTreeLabelProvider;
import com.generatescape.utils.Writer;
import com.lowagie.text.DocumentException;
import framework.BrowserApp;

/*******************************************************************************
 * Copyright (c) 2005, 2007 GenerateScape and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the GNU General Public License which accompanies this distribution, and is
 * available at http://www.gnu.org/copyleft/gpl.html
 * 
 * @author kentgibson : http://www.bigblogzoo.com
 * 
 ******************************************************************************/
public class AutoDetectView extends ViewPart {

    static Logger log = Logger.getLogger(AutoDetectView.class.getName());

    private static Action followHitsAction;

    private static Action refreshAction;

    private static boolean followHits = false;

    private static CanalContentProvider ccp = new CanalContentProvider();

    private static CheckboxTreeViewer treeViewer;

    private static Action reagg;

    private static Action pdfcreate;

    private static Action pdfdesc;

    private static Composite parent;

    private static Tree tree;

    private static boolean isDirty = false;

    private static AutoDetectView theview;

    private static final String cleanPartName = "SearchView";

    private static final String dirtyPartName = "*SearchView";

    private Action saveAction, saveAsAction, openAction, emailThis;

    private static Combo textBoxAtTop;

    private CanalTreeLabelProvider labelProvider;

    private Action detectAction;

    private Action doubleClickAction;

    private TreeEditor editor;

    private GridData layoutData;

    private Action removeAllHits, removeAHit, addBookmark;

    private static SearchHolder algorithmns[];

    private static Action typesOfSearch[];

    /** Remember all done searches in the running session */
    private static final Vector<?> lastSearches = new Vector();

    private CanalTreeDropAdapter dropAdapter;

    private CanalDragListener cdl;

    private IPartListener ipl = null;

    public static final String ID = "com.generatescape.views.AutoDetectView";

    private static String currentfile = "";

    static {
        currentfile = ccp.initAutoDetectView();
    }

    /**
   * @param name
   */
    public void refreshname(String name) {
        currentfile = name;
        if (isDirty) {
            theview.setPartName(dirtyPartName + " - " + name);
        } else {
            theview.setPartName(cleanPartName + " - " + name);
        }
    }

    public void createPartControl(final Composite parent) {
        theview = this;
        AutoDetectView.parent = parent;
        refreshname(currentfile);
        ipl = new IPartListener() {

            public void partActivated(IWorkbenchPart part) {
            }

            public void partBroughtToTop(IWorkbenchPart part) {
            }

            public void partClosed(IWorkbenchPart part) {
                if (part instanceof AutoDetectView) {
                    readyToDie(true);
                }
            }

            public void partDeactivated(IWorkbenchPart part) {
            }

            public void partOpened(IWorkbenchPart part) {
            }
        };
        getSite().getWorkbenchWindow().getPartService().addPartListener(ipl);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 2;
        parent.setLayout(layout);
        textBoxAtTop = new Combo(parent, SWT.FLAT);
        FormData findcomboLData = new FormData();
        findcomboLData.height = 21;
        findcomboLData.width = 342;
        findcomboLData.left = new FormAttachment(193, 1000, 0);
        findcomboLData.right = new FormAttachment(965, 1000, 0);
        findcomboLData.top = new FormAttachment(77, 1000, 0);
        findcomboLData.bottom = new FormAttachment(190, 1000, 0);
        parent.setLayoutData(findcomboLData);
        parent.setFocus();
        treeViewer = new CheckboxTreeViewer(parent, SWT.MULTI);
        treeViewer.setUseHashlookup(false);
        treeViewer.setContentProvider(ccp);
        labelProvider = new CanalTreeLabelProvider();
        treeViewer.setLabelProvider(labelProvider);
        layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        textBoxAtTop.setLayoutData(layoutData);
        createTabTopActions();
        createToolbar();
        treeViewer.setInput("root");
        PopUpCreator popUp = new PopUpCreator();
        popUp.createPopUpMenu(parent, ccp, treeViewer, this, CONSTANTS.AUTODETECTVIEW, null);
        makeTreeViewActions();
        hookDoubleClickAction();
        tree = treeViewer.getTree();
        editor = new TreeEditor(tree);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabVertical = true;
        editor.grabHorizontal = true;
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        ArrayList<?> list = new ArrayList();
        list = ccp.getAllChildren(ccp.getRoot1(), list);
        list = ccp.getAllChildren(ccp.getRoot2(), list);
        list = ccp.getAllChildren(ccp.getRoot3(), list);
        if (list.size() > 0) {
            reagg.setEnabled(true);
            pdfcreate.setEnabled(true);
            pdfdesc.setEnabled(true);
        }
        String feeddesc = "";
        for (Iterator<?> iter = list.iterator(); iter.hasNext(); ) {
            NewCanalNode element = (NewCanalNode) iter.next();
            treeViewer.setChecked(element, element.isChecked());
            if (element.getType() == CONSTANTS.TYPE_FEED_DESCRIPTION) {
                feeddesc = element.getKey();
            } else if (element.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE || element.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                if (element.getData() == null) {
                    continue;
                }
                if (element.getKey() == null) {
                    continue;
                }
                if (element.getDescription() == null) {
                    continue;
                }
                SearchCatcher.addForAutoDetect(feeddesc, element.getData(), element.getKey(), new Date(element.getDate()), element.getDescription());
            }
        }
        list.clear();
        list = null;
        treeViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                NewCanalNode node = (NewCanalNode) event.getElement();
                if (event.getChecked()) {
                    treeViewer.setSubtreeChecked(event.getElement(), true);
                } else {
                    treeViewer.setSubtreeChecked(event.getElement(), false);
                }
                if (node.getType() == CONSTANTS.TYPE_FEED_DESCRIPTION) {
                    return;
                }
                if (node.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE) {
                    if (!node.isChecked()) {
                        NewCanalNode parent = (NewCanalNode) ccp.getParent(node);
                        NewCanalNode sibling = (NewCanalNode) ccp.getNextSibling(parent);
                        SearchCatcher.addForAutoDetect(sibling.getKey(), node.getData(), node.getKey(), new Date(node.getDate()), node.getDescription());
                    }
                    return;
                }
                if (node.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                    if (!node.isChecked()) {
                        NewCanalNode parent = (NewCanalNode) ccp.getParent(node);
                        NewCanalNode sibling = (NewCanalNode) ccp.getNextSibling(parent);
                        SearchCatcher.addForAutoDetect(sibling.getKey(), node.getData(), node.getKey(), new Date(node.getDate()), node.getDescription());
                    }
                    return;
                }
                reagg.setEnabled(true);
                pdfcreate.setEnabled(true);
                pdfdesc.setEnabled(true);
                ArrayList<?> list = new ArrayList();
                list = ccp.getAllChildren(node, list);
                for (Iterator<?> iter = list.iterator(); iter.hasNext(); ) {
                    NewCanalNode element = (NewCanalNode) iter.next();
                    if (element.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE || element.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        if (!node.isChecked()) {
                            NewCanalNode parent = (NewCanalNode) ccp.getParent(element);
                            NewCanalNode sibling = (NewCanalNode) ccp.getNextSibling(parent);
                            SearchCatcher.addForAutoDetect(sibling.getKey(), element.getData(), element.getKey(), new Date(element.getDate()), element.getDescription());
                        }
                    }
                }
            }
        });
        initDND();
        textBoxAtTop.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A')) textBoxAtTop.setSelection(new Point(0, textBoxAtTop.getText().length()));
            }
        });
        if (algorithmns != null) {
            textBoxAtTop.addListener(SWT.DefaultSelection, new Listener() {

                public void handleEvent(Event event) {
                    for (int a = lastSearches.size() - 1; a >= 0; a--) {
                        textBoxAtTop.add((String) lastSearches.get(a));
                    }
                    doSearch();
                }
            });
        }
    }

    /** Does the search
   * 
   */
    private void doSearch() {
        int counter = 0;
        boolean passed = false;
        final int len = typesOfSearch.length;
        for (int i = 0; i < len; i++) {
            if (typesOfSearch[i].isChecked()) {
                passed = true;
            }
        }
        int numberofjobs = 0;
        for (int i = 0; i < len; i++) {
            if (typesOfSearch[i].isChecked()) {
                if (algorithmns[i].getEmbeddedXMLName()) {
                    numberofjobs++;
                }
            }
        }
        if (!passed) {
            MessageDialog.openInformation(parent.getShell(), "Search", "Please Choose A Search Engine To Use");
            return;
        }
        BrowserView.donotprogress = true;
        ArrayList urls = new ArrayList();
        ArrayList algols = new ArrayList();
        ArrayList videotype = new ArrayList();
        for (int i = 0; i < len; i++) {
            if (typesOfSearch[i].isChecked()) {
                final boolean xmlchannel = algorithmns[i].isXmlChannel();
                final String url = algorithmns[i].getGetURL(textBoxAtTop.getText());
                final String algo = algorithmns[i].getAlgorithm();
                if (xmlchannel) {
                    final String toshow = algorithmns[i].getGetSecondaryURL(textBoxAtTop.getText());
                    BrowserView.setUrl(toshow, null);
                    final ArrayList urlList = TreeExpandFeedParser.getFeedsWithSources(url, parent);
                    if (urlList == null) {
                        Display.getDefault().asyncExec(new Runnable() {

                            public void run() {
                                MessageDialog.openInformation(parent.getShell(), "Auto Detect", "No Channels found on this page");
                                return;
                            }
                        });
                    } else {
                        if (urlList != null) {
                            String[] urlListStr = new String[urlList.size()];
                            urlList.toArray(urlListStr);
                            JobHandler.scheduleAutoDetectPodcastSearch(this, urlListStr, algorithmns[i].getAlgorithm());
                        }
                    }
                } else {
                    if (counter == 0) {
                        boolean highlightstatus = PrefPageOne.getBoolValue(CONSTANTS.PREF_HIGHLIGHT_HITS);
                        if (highlightstatus) {
                            String texttohighlight = textBoxAtTop.getText();
                            BrowserView.setUrl(url, texttohighlight);
                        } else {
                            BrowserView.setUrl(url, null);
                        }
                    } else {
                        boolean highlightstatus = PrefPageOne.getBoolValue(CONSTANTS.PREF_HIGHLIGHT_HITS);
                        if (highlightstatus) {
                            String texttohighlight = textBoxAtTop.getText();
                            BrowserView.setUrlInNewTab(url, texttohighlight, false);
                        } else {
                            BrowserView.setUrlInNewTab(url, null, true);
                        }
                    }
                    counter++;
                    if (algorithmns[i].getEmbeddedXMLName()) {
                        urls.add(url);
                        algols.add(algo);
                    }
                    if (algorithmns[i].isVideoFormat()) {
                        videotype.add(new Boolean(true));
                    } else {
                        videotype.add(new Boolean(false));
                    }
                }
            }
        }
        if (urls.size() > 0) {
            String[] urlsStr = new String[urls.size()];
            urls.toArray(urlsStr);
            String[] algolsStr = new String[algols.size()];
            algols.toArray(algolsStr);
            Boolean[] vidformats = new Boolean[videotype.size()];
            videotype.toArray(vidformats);
            JobHandler.scheduleAutoDetectSearch(urlsStr, this, algolsStr, followHits, treeViewer, vidformats);
        }
        JobHandler.scheduleSearchUpdateJob(textBoxAtTop.getText(), this);
        return;
    }

    /** Initialises the Drag and Drop
   * 
   */
    private void initDND() {
        int opsdrag = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[] { CanalTransfer.getInstance() };
        CanalTransfer.getInstance().setGraph(ccp.getCanalGraph());
        cdl = new CanalDragListener(treeViewer, ccp, true);
        treeViewer.addDragSupport(opsdrag, transfers, cdl);
        dropAdapter = new CanalTreeDropAdapter(treeViewer, ccp.getCanalGraph());
        treeViewer.addDropSupport(opsdrag, transfers, dropAdapter);
    }

    /**
   * @return
   */
    public static CanalContentProvider getCcp() {
        return ccp;
    }

    private void hookDoubleClickAction() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
   * @return Returns the isDirty.
   */
    public static boolean isDirty() {
        return isDirty;
    }

    /**
   * @param isDirty
   *          The isDirty to set.
   */
    private static void setDirty(final boolean isDirty, final String newname) {
        AutoDetectView.isDirty = isDirty;
        currentfile = newname;
        if (theview != null) {
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    if (isDirty) {
                        theview.setPartName(dirtyPartName + " - " + newname);
                    } else {
                        theview.setPartName(cleanPartName + " - " + newname);
                    }
                }
            });
        }
    }

    /**
   * @param isDirty
   */
    public static void setDirty(final boolean isDirty) {
        AutoDetectView.isDirty = isDirty;
        if (theview != null) {
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    if (isDirty) {
                        theview.setPartName(dirtyPartName + " - " + currentfile);
                    } else {
                        theview.setPartName(cleanPartName + " - " + currentfile);
                    }
                }
            });
        }
    }

    /**
   * Whether or not to follow the hits
   */
    protected static void followHits() {
        if (followHits == false) {
            followHits = true;
            followHitsAction.setImageDescriptor(CONSTANTS.followHitsImageDescriptorWithBack);
        } else {
            followHits = false;
            followHitsAction.setImageDescriptor(CONSTANTS.followHitsImageDescriptor);
        }
        return;
    }

    /**
   * @param item
   * @param list
   * @param type
   * @return
   */
    public ArrayList getAllEndNodesArbitraryType(TreeItem item, ArrayList list, int type) {
        Object[] children = item.getItems();
        if (children == null) {
            return list;
        }
        int len = children.length;
        for (int i = 0; i < len; i++) {
            TreeItem ti = (TreeItem) children[i];
            NewCanalNode ncn = (NewCanalNode) ti.getData();
            if (ncn == null) {
                continue;
            }
            if (ncn.getType() == type) {
                list.add(ti);
            }
            getAllEndNodesArbitraryType(ti, list, type);
        }
        return list;
    }

    private void createTabTopActions() {
        refreshAction = new Action("Refresh Channels") {

            public void run() {
                TreeItem[] roots = treeViewer.getTree().getItems();
                ArrayList list = new ArrayList();
                for (int i = 0; i < roots.length; i++) {
                    list = getAllEndNodesArbitraryType(roots[i], list, CONSTANTS.TYPE_FEED_URL);
                }
                for (int i = 0; i < roots.length; i++) {
                    list = getAllEndNodesArbitraryType(roots[i], list, CONSTANTS.TYPE_PODCAST_URL);
                }
                for (int i = 0; i < roots.length; i++) {
                    list = getAllEndNodesArbitraryType(roots[i], list, CONSTANTS.TYPE_VIDEO_URL);
                }
                final ArrayList newlist = list;
                final int[] nextId = new int[1];
                Runnable longJob = new Runnable() {

                    boolean done = false;

                    public void run() {
                        Thread thread = new Thread(new Runnable() {

                            public void run() {
                                nextId[0]++;
                                AutoDetectView.parent.getDisplay().syncExec(new Runnable() {

                                    public void run() {
                                        for (Iterator iter = newlist.iterator(); iter.hasNext(); ) {
                                            TreeItem element = (TreeItem) iter.next();
                                            TreeItem[] siblings = element.getItems();
                                            for (int i = 0; i < siblings.length; i++) {
                                                siblings[i].dispose();
                                            }
                                            NewCanalNode elementNCN = (NewCanalNode) element.getData();
                                            ArrayList kids = new ArrayList();
                                            kids = ccp.getAllChildren(elementNCN, kids);
                                            for (Iterator iterator = kids.iterator(); iterator.hasNext(); ) {
                                                NewCanalNode kid = (NewCanalNode) iterator.next();
                                                ccp.removeChild(kid);
                                            }
                                            TreeExpandFeedParser.getFeeds(element.getText(), parent, false);
                                            String[] urlList = TreeExpandFeedParser.getUrls();
                                            String[] titleList = TreeExpandFeedParser.getTitles();
                                            if (titleList == null) {
                                                return;
                                            }
                                            if (urlList == null) {
                                                return;
                                            }
                                            for (int i = 0; i < titleList.length; i++) {
                                                TreeItem ti = new TreeItem(element, SWT.NONE);
                                                if (Library.haveIReadThisArticle(urlList[i])) {
                                                    NewCanalNode cn = new NewCanalNode(titleList[i], CONSTANTS.TYPE_FEED_READ_ARTICLE);
                                                    cn.setData(urlList[i]);
                                                    ti.setImage(CONSTANTS.readArticle);
                                                    ti.setData(cn);
                                                    ti.setText(titleList[i]);
                                                    ccp.addChild((NewCanalNode) element.getData(), cn);
                                                } else {
                                                    NewCanalNode cn = new NewCanalNode(titleList[i], CONSTANTS.TYPE_FEED_UNREAD_ARTICLE);
                                                    cn.setData(urlList[i]);
                                                    ti.setImage(CONSTANTS.unReadArticle);
                                                    ti.setData(cn);
                                                    ti.setText(titleList[i]);
                                                    ccp.addChild((NewCanalNode) element.getData(), cn);
                                                }
                                            }
                                            treeViewer.expandToLevel(element.getData(), 1);
                                        }
                                    }
                                });
                                done = true;
                                AutoDetectView.parent.getDisplay().wake();
                            }
                        });
                        thread.start();
                        while (!done && !AutoDetectView.parent.getShell().isDisposed()) {
                            if (!AutoDetectView.parent.getDisplay().readAndDispatch()) AutoDetectView.parent.getDisplay().sleep();
                        }
                    }
                };
                BusyIndicator.showWhile(AutoDetectView.parent.getDisplay(), longJob);
                treeViewer.refresh();
            }
        };
        refreshAction.setEnabled(true);
        refreshAction.setToolTipText("Refresh Channels");
        refreshAction.setImageDescriptor(CONSTANTS.refeshDesc);
        followHitsAction = new Action("Follow Hits") {

            public void run() {
                followHits();
            }
        };
        followHitsAction.setEnabled(true);
        followHitsAction.setToolTipText("Follow Hits");
        followHitsAction.setImageDescriptor(CONSTANTS.followHitsImageDescriptor);
        openAction = new OpenFileAction(treeViewer, parent, ccp, CONSTANTS.AUTODETECTVIEW, this);
        openAction.setToolTipText("Open File");
        openAction.setImageDescriptor(CONSTANTS.openImageDescriptor);
        openAction = new OpenFileAction(treeViewer, parent, ccp, CONSTANTS.AUTODETECTVIEW, this);
        openAction.setToolTipText("Open File");
        openAction.setImageDescriptor(CONSTANTS.openImageDescriptor);
        emailThis = new EmailAction(treeViewer, parent, ccp, false, false);
        emailThis.setToolTipText("Email This");
        emailThis.setImageDescriptor(CONSTANTS.emailDesc);
        saveAction = new Action("Save") {

            public void run() {
                final int[] nextId = new int[1];
                Runnable longJob = new Runnable() {

                    boolean done = false;

                    public void run() {
                        Thread thread = new Thread(new Runnable() {

                            public void run() {
                                nextId[0]++;
                                AutoDetectView.parent.getDisplay().syncExec(new Runnable() {

                                    public void run() {
                                        boolean result = Writer.writeTree(ccp.getCanalGraph(), CanalContentProvider.getDefaultAutoDetectFile());
                                        if (result) {
                                            MessageDialog.openInformation(parent.getShell(), "Save", "Searches Saved");
                                            setDirty(false);
                                        } else {
                                            MessageDialog.openInformation(parent.getShell(), "Problem With Saving", "Changes Not Saved - Please try again");
                                            return;
                                        }
                                    }
                                });
                                done = true;
                                AutoDetectView.parent.getDisplay().wake();
                            }
                        });
                        thread.start();
                        while (!done && !AutoDetectView.parent.getShell().isDisposed()) {
                            if (!AutoDetectView.parent.getDisplay().readAndDispatch()) AutoDetectView.parent.getDisplay().sleep();
                        }
                    }
                };
                BusyIndicator.showWhile(AutoDetectView.parent.getDisplay(), longJob);
            }
        };
        saveAction.setToolTipText("Save Changes");
        saveAction.setImageDescriptor(BrowserApp.getImageDescriptor("save.gif"));
        saveAsAction = new Action("Save As") {

            public void run() {
                final int[] nextId = new int[1];
                Runnable longJob = new Runnable() {

                    boolean done = false;

                    public void run() {
                        Thread thread = new Thread(new Runnable() {

                            public void run() {
                                nextId[0]++;
                                AutoDetectView.parent.getDisplay().syncExec(new Runnable() {

                                    public void run() {
                                        SafeSaveDialog ssd = new SafeSaveDialog(parent.getShell(), CONSTANTS.AUTODETECTVIEW);
                                        String outFile = ssd.open();
                                        if (outFile == null) {
                                            return;
                                        }
                                        boolean test = Writer.writeTreeSaveAs(ccp.getCanalGraph(), outFile);
                                        if (test) {
                                            MessageDialog.openInformation(parent.getShell(), "Save", "All Changes Saved To Searches");
                                            setDirty(false, outFile);
                                            CanalContentProvider.setDefaultAutoDetectFile(outFile);
                                        } else {
                                            MessageDialog.openInformation(parent.getShell(), "Problem With Saving", "Changes Not Saved");
                                            return;
                                        }
                                    }
                                });
                                done = true;
                                AutoDetectView.parent.getDisplay().wake();
                            }
                        });
                        thread.start();
                        while (!done && !AutoDetectView.parent.getShell().isDisposed()) {
                            if (!AutoDetectView.parent.getDisplay().readAndDispatch()) AutoDetectView.parent.getDisplay().sleep();
                        }
                    }
                };
                BusyIndicator.showWhile(AutoDetectView.parent.getDisplay(), longJob);
            }
        };
        saveAsAction.setToolTipText("Save As");
        saveAsAction.setImageDescriptor(BrowserApp.getImageDescriptor("saveas.gif"));
        detectAction = new Action("Detect New Channels") {

            public void run() {
                final String currentURL = BrowserView.getURL();
                Job customJob = new Job("Auto Detect New Channels") {

                    protected IStatus run(IProgressMonitor monitor) {
                        monitor.beginTask("Scanning - One Moment Please", IProgressMonitor.UNKNOWN);
                        Set feedURLS = null;
                        try {
                            feedURLS = RSSAutoDetector.autoDetect(currentURL, false, null);
                        } catch (IllegalStateException ex) {
                            Display.getDefault().asyncExec(new Runnable() {

                                public void run() {
                                    MessageDialog.openInformation(parent.getShell(), "Auto Detect", "No Channels found on this page : Unsupported File Type");
                                }
                            });
                        }
                        if (feedURLS.size() == 0) {
                            Display.getDefault().asyncExec(new Runnable() {

                                public void run() {
                                    MessageDialog.openInformation(parent.getShell(), "Auto Detect", "No Channels found on this page");
                                }
                            });
                        } else {
                            for (Iterator iter = feedURLS.iterator(); iter.hasNext(); ) {
                                String url = ((NewCanalNode) iter.next()).getKey();
                                addFeedForAutoDetect(url, null, null, false, monitor, -1);
                            }
                        }
                        monitor.done();
                        return new Status(IStatus.OK, "framework", IStatus.OK, "Job Completed Fine", null);
                    }
                };
                JobHandler.scheduleJob(customJob, AutoDetectView.this, true, 0);
            }
        };
        detectAction.setToolTipText("Detect Channels");
        detectAction.setImageDescriptor(BrowserApp.getImageDescriptor("radar.png"));
        removeAllHits = new Action("Remove All Hits") {

            public void run() {
                removeAllHits();
            }
        };
        removeAllHits.setToolTipText("Remove All Hits");
        removeAllHits.setImageDescriptor(BrowserApp.getImageDescriptor("removeAll2.gif"));
        addBookmark = new AddBookmarkAction(parent, treeViewer);
        addBookmark.setToolTipText("Add Bookmark");
        addBookmark.setImageDescriptor(CONSTANTS.addbookmarkDesc);
        removeAHit = new Action("Remove Selected Hit") {

            public void run() {
                removeSelected();
            }
        };
        removeAHit.setToolTipText("Remove Selected Hit");
        removeAHit.setImageDescriptor(CONSTANTS.remove);
        reagg = new Action("Reaggregate Selected Hits") {

            public void run() {
                Object[] items = treeViewer.getCheckedElements();
                ArrayList validNodes = new ArrayList();
                for (int i = 0; i < items.length; i++) {
                    NewCanalNode node = (NewCanalNode) items[i];
                    if (node.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE || node.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        validNodes.add(node);
                    }
                }
                NewCanalNode[] newnodes = new NewCanalNode[validNodes.size()];
                validNodes.toArray(newnodes);
                SafeSaveDialog ssd = new SafeSaveDialog(parent.getShell(), CONSTANTS.XMLFILEVIEW);
                String outFile = ssd.open();
                boolean result = FeedWriter.writeFeedWithDataKey(newnodes, SearchCatcher.getUrlToArticleVector(), outFile, PrefPageOne.getIntValue(CONSTANTS.PREF_SORT_ORDER));
                if (result) {
                    MessageDialog.openInformation(parent.getShell(), "Save", outFile + " Saved ");
                    BrowserView.setUrl(outFile, null);
                } else {
                    MessageDialog.openInformation(parent.getShell(), "Problem With Saving", "Changes Not Saved - Please try again");
                }
            }
        };
        reagg.setToolTipText("Reaggregate Selected Hits");
        reagg.setImageDescriptor(CONSTANTS.reagg);
        reagg.setEnabled(false);
        pdfcreate = new Action("Generate PDF From Selected Hits") {

            public void run() {
                Object[] items = treeViewer.getCheckedElements();
                ArrayList validNodes = new ArrayList();
                boolean withfeeddesc = false;
                for (int i = 0; i < items.length; i++) {
                    NewCanalNode node = (NewCanalNode) items[i];
                    if (node.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE || node.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        validNodes.add(node);
                    } else if (node.getType() == CONSTANTS.TYPE_FEED_DESCRIPTION) {
                        withfeeddesc = true;
                    }
                }
                if (items.length == 0) {
                    MessageDialog.openInformation(parent.getShell(), "Generate PDF From Selected Hits", "Please check at least one article to create a pdf.");
                    return;
                }
                NewCanalNode[] newnodes = new NewCanalNode[validNodes.size()];
                validNodes.toArray(newnodes);
                SafeSaveDialog ssd = new SafeSaveDialog(parent.getShell(), CONSTANTS.PDFFILEVIEW);
                String outFile = ssd.open();
                ArrayList list = FeedWriter.writePDFFeed(newnodes, SearchCatcher.getUrlToArticleVector(), outFile, PrefPageOne.getIntValue(CONSTANTS.PREF_SORT_ORDER));
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(outFile);
                    PDFDocumentFactory dg = new PDFDocumentFactory(fos, true, withfeeddesc);
                    dg.createPDF(list);
                    BrowserView.setUrlInNewTab(outFile.toString(), null, true);
                } catch (FileNotFoundException e) {
                    MessageDialog.openInformation(parent.getShell(), "Generate PDF From Selected Hits", e.getMessage());
                    return;
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        };
        pdfcreate.setToolTipText("Generate PDF From Selected Hits");
        pdfcreate.setImageDescriptor(CONSTANTS.pdfDescriptor);
        pdfcreate.setEnabled(false);
        pdfdesc = new Action("Article Description") {

            public void run() {
                IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
                if (selection.isEmpty()) {
                    MessageDialog.openInformation(parent.getShell(), "Article Description", "Unable to preview, please select a search result");
                    return;
                }
                ArrayList validNodes = new ArrayList();
                for (Iterator iterator = selection.iterator(); iterator.hasNext(); ) {
                    NewCanalNode domain = (NewCanalNode) iterator.next();
                    if (domain.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE || domain.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        validNodes.add(domain);
                    }
                }
                NewCanalNode[] newnodes = new NewCanalNode[validNodes.size()];
                validNodes.toArray(newnodes);
                String defaultpdf = null;
                String temp = System.getProperty("java.io.tmpdir");
                Format formatter = new SimpleDateFormat("'Time'_HH_mm_ss_'Day'_dd_MM_yyyy");
                Date date = new Date();
                String s = formatter.format(date);
                defaultpdf = temp + s + ".pdf";
                ArrayList list = FeedWriter.writePDFFeed(newnodes, SearchCatcher.getUrlToArticleVector(), defaultpdf, PrefPageOne.getIntValue(CONSTANTS.PREF_SORT_ORDER));
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(defaultpdf);
                    PDFDocumentFactory dg = new PDFDocumentFactory(fos, false, true);
                    dg.createPDF(list);
                    fos.close();
                    BrowserView.setUrlInNewTab(defaultpdf, null, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pdfdesc.setToolTipText("Article Description");
        pdfdesc.setImageDescriptor(CONSTANTS.pdfDescDescriptor);
        pdfdesc.setEnabled(false);
    }

    /**
   * Removes selected hits
   * 
   */
    private void removeSelected() {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        if (selection.isEmpty()) {
            MessageDialog.openInformation(parent.getShell(), "Delete", "Unable to delete, nothing selected");
            return;
        }
        for (Iterator iterator = selection.iterator(); iterator.hasNext(); ) {
            NewCanalNode domain = (NewCanalNode) iterator.next();
            if (domain.getParent() == null) {
                MessageDialog.openInformation(parent.getShell(), "Delete", "Unable to delete, please select a search result");
                return;
            }
            ccp.getCanalGraph().removeChild(domain);
        }
        setDirty(true);
        treeViewer.refresh();
    }

    /**
   * Removes all of the hits
   * 
   */
    private void removeAllHits() {
        ArrayList list = new ArrayList();
        list = ccp.getAllChildren(ccp.getRoot1(), list);
        list = ccp.getAllChildren(ccp.getRoot2(), list);
        list = ccp.getAllChildren(ccp.getRoot3(), list);
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            NewCanalNode element = (NewCanalNode) iter.next();
            ccp.removeChild(element);
        }
        reagg.setEnabled(false);
        pdfcreate.setEnabled(false);
        pdfdesc.setEnabled(false);
        setDirty(true);
        treeViewer.refresh();
    }

    /**
   * Creates the tool bar
   * 
   */
    private void createToolbar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(emailThis);
        toolbarManager.add(openAction);
        toolbarManager.add(saveAction);
        toolbarManager.add(saveAsAction);
        toolbarManager.add(detectAction);
        toolbarManager.add(removeAHit);
        toolbarManager.add(removeAllHits);
        toolbarManager.add(reagg);
        toolbarManager.add(pdfcreate);
        toolbarManager.add(pdfdesc);
        toolbarManager.add(followHitsAction);
        toolbarManager.add(refreshAction);
        toolbarManager.add(addBookmark);
    }

    public void setFocus() {
    }

    /**
   * Makes the actions for the tree
   * 
   */
    private void makeTreeViewActions() {
        doubleClickAction = new Action() {

            public void run() {
                final TreeItem item = tree.getSelection()[0];
                NewCanalNode ncn = (NewCanalNode) item.getData();
                if ((ncn.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) || (ncn.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE)) {
                    Library.readThisArticle(ncn.getData());
                    boolean highlightstatus = PrefPageOne.getBoolValue(CONSTANTS.PREF_HIGHLIGHT_HITS);
                    if (highlightstatus) {
                        NewCanalNode parent = ccp.getCanalGraph().getParent(ncn);
                        int type = parent.getType();
                        if (type == CONSTANTS.TYPE_PODCAST_URL || type == CONSTANTS.TYPE_VIDEO_URL) {
                            BrowserView.setUrlInNewTab(ncn.getData(), textBoxAtTop.getText(), true);
                        } else {
                            BrowserView.setUrl(ncn.getData(), textBoxAtTop.getText());
                        }
                    } else {
                        NewCanalNode parent = ccp.getCanalGraph().getParent(ncn);
                        int type = parent.getType();
                        if (type == CONSTANTS.TYPE_PODCAST_URL || type == CONSTANTS.TYPE_VIDEO_URL) {
                            BrowserView.setUrlInNewTab(ncn.getData(), null, true);
                        } else {
                            BrowserView.setUrl(ncn.getData(), null);
                        }
                    }
                    item.setImage(CONSTANTS.readArticle);
                    if (ncn.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        ncn.setType(CONSTANTS.TYPE_FEED_READ_ARTICLE);
                        setDirty(true);
                    }
                } else if ((ncn.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) || (ncn.getType() == CONSTANTS.TYPE_FEED_READ_ARTICLE)) {
                    Library.readThisArticle(ncn.getData());
                    boolean highlightstatus = PrefPageOne.getBoolValue(CONSTANTS.PREF_HIGHLIGHT_HITS);
                    if (highlightstatus) {
                        BrowserView.setUrl(ncn.getData(), textBoxAtTop.getText());
                    } else {
                        BrowserView.setUrl(ncn.getData(), null);
                    }
                    item.setImage(CONSTANTS.readArticle);
                    if (ncn.getType() == CONSTANTS.TYPE_FEED_UNREAD_ARTICLE) {
                        ncn.setType(CONSTANTS.TYPE_FEED_READ_ARTICLE);
                        setDirty(true);
                    }
                } else if (ncn.getType() == CONSTANTS.TYPE_FEED_URL) {
                    TreeItem[] siblings = item.getItems();
                    for (int i = 0; i < siblings.length; i++) {
                        siblings[i].dispose();
                    }
                    final int[] nextId = new int[1];
                    Runnable longJob = new Runnable() {

                        boolean done = false;

                        public void run() {
                            Thread thread = new Thread(new Runnable() {

                                public void run() {
                                    nextId[0]++;
                                    AutoDetectView.parent.getDisplay().syncExec(new Runnable() {

                                        public void run() {
                                            TreeExpandFeedParser.getFeeds(item.getText(), parent, false);
                                        }
                                    });
                                    done = true;
                                    AutoDetectView.parent.getDisplay().wake();
                                }
                            });
                            thread.start();
                            while (!done && !AutoDetectView.parent.getShell().isDisposed()) {
                                if (!AutoDetectView.parent.getDisplay().readAndDispatch()) AutoDetectView.parent.getDisplay().sleep();
                            }
                        }
                    };
                    BusyIndicator.showWhile(AutoDetectView.parent.getDisplay(), longJob);
                    String[] urlList = TreeExpandFeedParser.getUrls();
                    String[] titleList = TreeExpandFeedParser.getTitles();
                    if (titleList == null) {
                        return;
                    }
                    if (urlList == null) {
                        return;
                    }
                    for (int i = 0; i < titleList.length; i++) {
                        TreeItem ti = new TreeItem(item, SWT.NONE);
                        if (Library.haveIReadThisArticle(urlList[i])) {
                            NewCanalNode cn = new NewCanalNode(urlList[i], CONSTANTS.TYPE_FEED_READ_ARTICLE);
                            cn.setData(urlList[i]);
                            ti.setImage(CONSTANTS.readArticle);
                            ti.setData(cn);
                            ti.setText(titleList[i]);
                        } else {
                            NewCanalNode cn = new NewCanalNode(urlList[i], CONSTANTS.TYPE_FEED_UNREAD_ARTICLE);
                            cn.setData(urlList[i]);
                            ti.setImage(CONSTANTS.unReadArticle);
                            ti.setData(cn);
                            ti.setText(titleList[i]);
                        }
                    }
                    treeViewer.expandToLevel(item.getData(), 1);
                } else {
                    boolean expandedState = treeViewer.getExpandedState(ncn);
                    if (expandedState) {
                        treeViewer.collapseToLevel(ncn, 1);
                        treeViewer.refresh();
                        return;
                    }
                    treeViewer.expandToLevel(ncn, 1);
                    treeViewer.refresh();
                }
            }
        };
    }

    /**
   * @param results
   * @param url
   * @param algo
   * @param monitor
   * @param jobnumber
   */
    public static void addFeedForPodcast(String[][] results, String url, String algo, IProgressMonitor monitor, int jobnumber) {
        if (url == null || url.equals("")) {
            return;
        }
        AutoDetectView.setDirty(true);
        String title = "No Title Available";
        String desc = "No Description Available";
        title = results[0][0];
        desc = results[1][0];
        if (monitor.isCanceled()) {
            return;
        }
        if (algo != null) {
            title = title + " (" + algo + ")";
        }
        final NewCanalNode newNode = new NewCanalNode(title, CONSTANTS.TYPE_FEED);
        newNode.setLocation(CONSTANTS.AUTODETECTVIEW);
        final NewCanalNode newURL = new NewCanalNode(url, CONSTANTS.TYPE_PODCAST_URL);
        NewCanalNode newDesc = new NewCanalNode(desc, CONSTANTS.TYPE_FEED_DESCRIPTION);
        CanalGraph cg = ccp.getCanalGraph();
        String[] titleList = results[3];
        if (titleList == null) {
            return;
        }
        cg.addChild(ccp.getCanalGraph().getTop2(), newNode);
        cg.addChild(newNode, newDesc);
        cg.addChild(newNode, newURL);
        String[] urlList = results[4];
        String[] descList = results[2];
        if (monitor.isCanceled()) {
            return;
        }
        for (int i = 0; i < titleList.length; i++) {
            NewCanalNode cn = null;
            String temptitle = titleList[i];
            if (Library.haveIReadThisArticle(urlList[i])) {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_READ_ARTICLE);
            } else {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_UNREAD_ARTICLE);
            }
            cn.setDescription(descList[i]);
            cn.setData(urlList[i]);
            cg.addChild(newURL, cn);
            SearchCatcher.addForAutoDetect(title, urlList[i], titleList[i], new Date(System.currentTimeMillis()), descList[i]);
        }
        if (monitor.isCanceled()) {
            return;
        }
        reagg.setEnabled(true);
        pdfcreate.setEnabled(true);
        pdfdesc.setEnabled(true);
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                treeViewer.refresh();
                treeViewer.expandToLevel(newNode, 1);
            }
        });
        return;
    }

    /**
   * @param url
   * @param algo
   * @param set
   * @param withenclosures
   * @param withsources
   * @return
   */
    public static HashSet addFeedForAutoDetect(String url, String algo, HashSet set, boolean withenclosures, IProgressMonitor monitor, int jobnumber) {
        if (url == null || url.equals("")) {
            return set;
        }
        String[][] results;
        AutoDetectView.setDirty(true);
        String title = "No Title Available";
        String desc = "No Description Available";
        if (withenclosures) {
            results = TreeExpandFeedParser.getFeedsWithEnclosures(url, null, monitor);
            if (results == null) {
                return set;
            }
            title = results[0][0];
            desc = results[1][0];
        } else {
            results = TreeExpandFeedParser.getFeedsWithDescription(url, null, monitor, jobnumber);
            if (results == null) {
                return set;
            }
            title = results[0][0];
            desc = results[1][0];
        }
        if (monitor.isCanceled()) {
            return set;
        }
        if (algo != null) {
            title = title + " (" + algo + ")";
        }
        final NewCanalNode newNode = new NewCanalNode(title, CONSTANTS.TYPE_FEED);
        newNode.setLocation(CONSTANTS.AUTODETECTVIEW);
        final NewCanalNode newURL = new NewCanalNode(url, CONSTANTS.TYPE_FEED_URL);
        NewCanalNode newDesc = new NewCanalNode(desc, CONSTANTS.TYPE_FEED_DESCRIPTION);
        CanalGraph cg = ccp.getCanalGraph();
        String[] titleList = results[3];
        if (titleList == null) {
            return set;
        } else {
            System.out.println("titleList was OK withenclosures: " + withenclosures);
        }
        if (algo == null && set == null) {
            cg.addChild(ccp.getCanalGraph().getTop(), newNode);
        } else {
            cg.addChild(ccp.getCanalGraph().getTop2(), newNode);
        }
        cg.addChild(newNode, newDesc);
        cg.addChild(newNode, newURL);
        String[] urlList = results[4];
        String[] descList = results[2];
        if (monitor.isCanceled()) {
            return set;
        }
        for (int i = 0; i < titleList.length; i++) {
            NewCanalNode cn = null;
            String temptitle = titleList[i];
            if (Library.haveIReadThisArticle(urlList[i])) {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_READ_ARTICLE);
            } else {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_UNREAD_ARTICLE);
            }
            cn.setDescription(descList[i]);
            cn.setData(urlList[i]);
            cg.addChild(newURL, cn);
            SearchCatcher.addForAutoDetect(title, urlList[i], titleList[i], new Date(System.currentTimeMillis()), descList[i]);
        }
        if (monitor.isCanceled()) {
            return set;
        }
        reagg.setEnabled(true);
        pdfcreate.setEnabled(true);
        pdfdesc.setEnabled(true);
        if (set != null) {
            for (int i = 0; i < urlList.length; i++) {
                set.add(urlList[i]);
            }
        }
        monitor.worked(1);
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                treeViewer.refresh();
                treeViewer.expandToLevel(newNode, 1);
            }
        });
        return set;
    }

    /**
   * @param kids
   * @param algo
   * @return
   */
    private static NewCanalNode dirpresent(NewCanalNode[] kids, String algo) {
        for (int i = 0; i < kids.length; i++) {
            if (kids[i].getKey().equals(algo)) {
                return kids[i];
            }
        }
        return null;
    }

    /**
   * @param url
   * @param algo
   * @param monitor
   * @param spiderfeed
   * @param jobnumber
   * @param vidformat
   * @return
   */
    public static String[][] addFeed(String url, String algo, IProgressMonitor monitor, boolean spiderfeed, int jobnumber, boolean vidformat) {
        String[][] results;
        AutoDetectView.setDirty(true);
        String title = "No Title Available";
        String desc = "No Description Available";
        results = TreeExpandFeedParser.getFeedsWithDescription(url, null, monitor, jobnumber);
        if (results == null) {
            return null;
        }
        title = results[0][0];
        desc = results[1][0];
        if (title == null) {
            title = "No Title";
        }
        if (desc == null) {
            desc = "No Description";
        }
        if (monitor.isCanceled()) {
            return null;
        }
        if (!spiderfeed && algo != null) {
            title = title + " (" + algo + ")";
        }
        int type = CONSTANTS.TYPE_FEED;
        final NewCanalNode newNode = new NewCanalNode(title, type);
        newNode.setLocation(CONSTANTS.AUTODETECTVIEW);
        int feedtype = CONSTANTS.TYPE_FEED_URL;
        if (vidformat) {
            feedtype = CONSTANTS.TYPE_VIDEO_URL;
        }
        final NewCanalNode newURL = new NewCanalNode(url, feedtype);
        NewCanalNode newDesc = new NewCanalNode(desc, CONSTANTS.TYPE_FEED_DESCRIPTION);
        CanalGraph cg = ccp.getCanalGraph();
        String[] titleList = results[3];
        if (titleList == null) {
            return null;
        }
        if (algo == null) {
            cg.addChild(ccp.getCanalGraph().getTop(), newNode);
        } else if (spiderfeed) {
            NewCanalNode spiderchannel = cg.getTop3();
            NewCanalNode[] kids = cg.getChildren(spiderchannel);
            if (kids != null) {
                NewCanalNode dir = dirpresent(kids, algo);
                if (dir == null) {
                    NewCanalNode newDirNode = new NewCanalNode(algo, CONSTANTS.TYPE_DIRECTORY);
                    cg.addChild(cg.getTop3(), newDirNode);
                    cg.addChild(newDirNode, newNode);
                } else {
                    cg.addChild(dir, newNode);
                }
            } else {
                NewCanalNode newDirNode = new NewCanalNode(algo, CONSTANTS.TYPE_DIRECTORY);
                cg.addChild(cg.getTop3(), newDirNode);
                cg.addChild(newDirNode, newNode);
            }
        } else {
            cg.addChild(ccp.getCanalGraph().getTop2(), newNode);
        }
        cg.addChild(newNode, newDesc);
        cg.addChild(newNode, newURL);
        String[] urlList = results[4];
        String[] descList = results[2];
        if (monitor.isCanceled()) {
            System.out.println("Monitor cancelled");
            return null;
        }
        for (int i = 0; i < titleList.length; i++) {
            System.out.println(url + " titleList : " + i);
            NewCanalNode cn = null;
            String temptitle = titleList[i];
            if (Library.haveIReadThisArticle(urlList[i])) {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_READ_ARTICLE);
            } else {
                cn = new NewCanalNode(temptitle, CONSTANTS.TYPE_FEED_UNREAD_ARTICLE);
            }
            cn.setDescription(descList[i]);
            cn.setData(urlList[i]);
            cg.addChild(newURL, cn);
            SearchCatcher.addForAutoDetect(title, urlList[i], titleList[i], new Date(System.currentTimeMillis()), descList[i]);
        }
        if (monitor.isCanceled()) {
            return null;
        }
        reagg.setEnabled(true);
        pdfcreate.setEnabled(true);
        pdfdesc.setEnabled(true);
        System.out.println("Finished");
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                System.out.println("Trying refresh");
                treeViewer.refresh();
                treeViewer.expandToLevel(newNode, 1);
            }
        });
        return results;
    }

    /**
   * @param enable
   */
    public static void toggleReAgg(boolean enable) {
        reagg.setEnabled(enable);
    }

    /**
   * @param nochoice
   * @return
   */
    public static boolean readyToDie(boolean nochoice) {
        if (parent == null) {
            return true;
        }
        if (parent.isDisposed()) {
            return true;
        }
        if (isDirty()) {
            int questions;
            if (nochoice) {
                questions = SWT.ICON_QUESTION | SWT.YES | SWT.NO;
            } else {
                questions = SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL;
            }
            if (parent.isDisposed()) {
                return true;
            }
            MessageBox box = new MessageBox(parent.getShell(), questions);
            box.setText("Save SearchView");
            box.setMessage("The SearchView has changed. \n Do you want to save the changes?");
            int rc = box.open();
            if (rc == SWT.CANCEL) {
                return false;
            }
            if (rc == SWT.YES) {
                boolean test = Writer.writeTree(ccp.getCanalGraph(), CanalContentProvider.getDefaultAutoDetectFile());
                if (test) {
                    MessageDialog.openInformation(parent.getShell(), "Save", "All Changes Saved To Searches");
                } else {
                    MessageDialog.openInformation(parent.getShell(), "Problem With Saving", "Changes Not Saved - Please try Again");
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    /**
   * @return
   */
    public static Tree getTree() {
        return treeViewer.getTree();
    }

    /**
   * @return
   */
    public static TreeViewer getTreeViewer() {
        return treeViewer;
    }

    /**
   * @return
   */
    public static SearchHolder[] getAlgorithmns() {
        return algorithmns;
    }

    /**
   * @param algorithmns
   */
    public static void setAlgorithmns(SearchHolder[] algorithmns) {
        AutoDetectView.algorithmns = algorithmns;
    }

    /**
   * @return
   */
    public static Action[] getTypesOfSearch() {
        return typesOfSearch;
    }

    /**
   * @param typesOfSearch
   */
    public static void setTypesOfSearch(Action[] typesOfSearch) {
        AutoDetectView.typesOfSearch = typesOfSearch;
    }
}
