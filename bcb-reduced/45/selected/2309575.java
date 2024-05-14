package edutex.swt;

import java.io.File;
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import edutex.NLSMessages;
import edutex.config.EdutexConfig;
import edutex.util.ImageManager;
import edutex.util.EdutexFileFilter;

public class DirsTree {

    private Tree tree;

    public static final String TREE_FILE = "TreeItem.File";

    private static final String TREE_FILE_TYPE = "TreeItem.FileType";

    private static final Integer FILE = 0;

    private static final Integer DIR = 1;

    private static final Integer ROOT = 2;

    private Image closedRootImage, openRootImage, closedFolderImage, openFolderImage, fileTexImage, filePxpImage, filePdfImage, filePsImage, fileDviImage;

    private EdutexFileFilter edutexFilter;

    private File root;

    private final TreeEditor editor;

    /**
	 * Constructeur
	 * @param parent
	 */
    public DirsTree(Composite parent, EdutexConfig config) {
        tree = new Tree(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
        this.editor = new TreeEditor(this.tree);
        this.edutexFilter = new EdutexFileFilter(config);
        this.closedRootImage = ImageManager.image(this.tree.getDisplay(), "icon_ClosedDrive");
        this.openRootImage = ImageManager.image(this.tree.getDisplay(), "icon_OpenDrive");
        this.closedFolderImage = ImageManager.image(this.tree.getDisplay(), "icon_ClosedFolder");
        this.openFolderImage = ImageManager.image(this.tree.getDisplay(), "icon_OpenFolder");
        this.fileTexImage = ImageManager.image(this.tree.getDisplay(), "image_tex");
        this.filePxpImage = ImageManager.image(this.tree.getDisplay(), "image_pxp");
        this.filePdfImage = ImageManager.image(this.tree.getDisplay(), "image_pdf");
        this.filePsImage = ImageManager.image(this.tree.getDisplay(), "image_ps");
        this.fileDviImage = ImageManager.image(this.tree.getDisplay(), "image_dvi");
        tree.addTreeListener(new TreeAdapter() {

            @Override
            public void treeExpanded(TreeEvent event) {
                final TreeItem item = (TreeItem) event.item;
                Integer type = (Integer) item.getData(TREE_FILE_TYPE);
                if (type == ROOT) item.setImage(openRootImage);
                if (type == DIR) item.setImage(openFolderImage);
                expand(item);
            }

            @Override
            public void treeCollapsed(TreeEvent event) {
                final TreeItem item = (TreeItem) event.item;
                Integer type = (Integer) item.getData(TREE_FILE_TYPE);
                if (type == ROOT) item.setImage(closedRootImage);
                if (type == DIR) item.setImage(closedFolderImage);
            }
        });
    }

    /**
	 * Retourne la racine
	 * @return
	 */
    public File getRoot() {
        return root;
    }

    /**
	 * Retourne l'arborescence
	 * @return
	 */
    public Tree getTree() {
        return tree;
    }

    /**
	 * Nouveau dossier
	 */
    public final void newFolder() {
        TreeItem selected = this.getSelectedTreeItem();
        if (selected == null) return;
        final File current = (File) selected.getData(TREE_FILE);
        if (current != null && current.isDirectory()) {
            final File newFolder = new File(current.getAbsolutePath() + File.separator + NLSMessages.getString("DirsTree.NewFolder"));
            newFolder.mkdir();
            ArrayList<TreeItem> items = this.expand(selected);
            selected = null;
            for (TreeItem it : items) {
                if (newFolder.equals(it.getData(TREE_FILE))) {
                    selected = it;
                    break;
                }
            }
            if (selected == null) return;
            final TreeItem sel = selected;
            this.rename(sel);
        }
    }

    /**
	 * Nouveau fichier
	 */
    public final void newFile(String ext) {
        TreeItem selected = this.getSelectedTreeItem();
        if (selected == null) return;
        final File current = (File) selected.getData(TREE_FILE);
        if (current != null && current.isDirectory()) {
            final File newFile = new File(current.getAbsolutePath() + File.separator + NLSMessages.getString("DirsTree.NewFile") + "." + ext);
            try {
                newFile.createNewFile();
            } catch (Exception err) {
                err.printStackTrace();
                return;
            }
            ArrayList<TreeItem> items = this.expand(selected);
            selected = null;
            for (TreeItem it : items) {
                if (newFile.equals(it.getData(TREE_FILE))) {
                    selected = it;
                    break;
                }
            }
            if (selected == null) return;
            final TreeItem sel = selected;
            this.rename(sel);
        }
    }

    /**
	 * Renomme un fichier
	 * 
	 * @param sel
	 */
    public final void rename() {
        this.rename(this.getSelectedTreeItem());
    }

    /**
	 * Supprimer un fichier
	 * 
	 * @param sel
	 */
    public final void delete() {
        TreeItem selected = this.getSelectedTreeItem();
        File file = (File) selected.getData(TREE_FILE);
        if (file != null && file.isFile()) {
            MessageBox box = new MessageBox(this.tree.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            box.setMessage(NLSMessages.getString("DirsTree.ConfirmDeleteFile", file.getName()));
            int valid = box.open();
            if (valid == SWT.YES) {
                file.delete();
                this.expand(selected.getParentItem());
            }
        }
        if (file != null && file.isDirectory()) {
            MessageBox box = new MessageBox(this.tree.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            box.setMessage(NLSMessages.getString("DirsTree.ConfirmeDeleteFolder", file.getName()));
            int valid = box.open();
            if (valid == SWT.YES) {
                file.delete();
                this.expand(selected.getParentItem());
            }
        }
    }

    /**
	 * Renomme un fichier
	 * 
	 * @param sel
	 */
    public final void rename(final TreeItem sel) {
        if (sel == null) return;
        final File newFolder = (File) sel.getData(TREE_FILE);
        final Composite composite = new Composite(tree, SWT.NONE);
        final Text textItem = new Text(composite, SWT.NONE);
        final int inset = 1;
        composite.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event e) {
                Rectangle rect = composite.getClientArea();
                textItem.setBounds(rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
            }
        });
        Listener textListener = new Listener() {

            public void handleEvent(final Event e) {
                switch(e.type) {
                    case SWT.FocusOut:
                        sel.setText(textItem.getText());
                        newFolder.renameTo(new File(newFolder.getParentFile().getAbsolutePath() + File.separator + sel.getText()));
                        expand(sel.getParentItem());
                        composite.dispose();
                        break;
                    case SWT.Traverse:
                        switch(e.detail) {
                            case SWT.TRAVERSE_RETURN:
                                {
                                    sel.setText(textItem.getText());
                                    newFolder.renameTo(new File(newFolder.getParentFile().getAbsolutePath() + File.separator + sel.getText()));
                                    expand(sel.getParentItem());
                                }
                            case SWT.TRAVERSE_ESCAPE:
                                composite.dispose();
                                e.doit = false;
                        }
                        break;
                }
            }
        };
        textItem.addListener(SWT.FocusOut, textListener);
        textItem.addListener(SWT.Traverse, textListener);
        this.editor.setEditor(composite, sel);
        textItem.setText(sel.getText());
        textItem.selectAll();
        textItem.setFocus();
        editor.horizontalAlignment = SWT.RIGHT;
        Rectangle itemRect = sel.getBounds(), rect = tree.getClientArea();
        editor.minimumWidth = Math.max(100, itemRect.width) + inset * 2;
        int left = itemRect.x, right = rect.x + rect.width;
        editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
        editor.minimumHeight = 16 + inset * 2;
        editor.layout();
    }

    /**
	 * Expand to dir
	 */
    public final void expandToDir(File dir) {
        if (dir == null || dir.isFile()) return;
        ArrayList<File> parentDirs = new ArrayList<File>();
        File parent = dir;
        while (parent != null) {
            parentDirs.add(parent);
            parent = parent.getParentFile();
        }
        if (parentDirs.size() == 0) return;
        TreeItem[] roots = this.tree.getItems();
        TreeItem r = null;
        int indexRoot = -1;
        for (TreeItem it : roots) {
            if (parentDirs.contains(it.getData(TREE_FILE))) {
                r = it;
                indexRoot = parentDirs.indexOf(it.getData(TREE_FILE));
                break;
            }
        }
        if (r == null) {
            this.tree.setTopItem(this.tree.getItem(0));
            for (TreeItem it : roots) it.setExpanded(false);
            return;
        }
        ArrayList<TreeItem> items = null;
        items = this.expand(r);
        r.setExpanded(true);
        for (int i = indexRoot - 1; i >= 0; i--) {
            File current = parentDirs.get(i);
            r = null;
            for (TreeItem it : items) {
                if (it.getData(TREE_FILE).equals(current)) {
                    r = it;
                    break;
                }
            }
            if (r == null) break; else {
                items = this.expand(r);
                r.setExpanded(true);
                if (i == 0) {
                    this.tree.setSelection(r);
                    this.tree.setTopItem(r);
                }
            }
        }
    }

    /**
	 * Expand an item
	 * 
	 * @param item the TreeItem to fill in
	 */
    private final ArrayList<TreeItem> expand(TreeItem item) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>();
        item.removeAll();
        final File dir = (File) item.getData(TREE_FILE);
        File[] files = dir.listFiles(edutexFilter);
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    File[] ffs = f.listFiles();
                    TreeItem it = new TreeItem(item, SWT.NONE);
                    it.setText(f.getName());
                    it.setImage(this.closedFolderImage);
                    it.setData(TREE_FILE, f);
                    it.setData(TREE_FILE_TYPE, DIR);
                    if (ffs != null && ffs.length > 0) new TreeItem(it, SWT.NONE);
                    items.add(it);
                } else if (f.isFile()) {
                    TreeItem it = new TreeItem(item, SWT.NONE);
                    it.setText(f.getName());
                    if (f.getName().endsWith("tex")) it.setImage(this.fileTexImage); else if (f.getName().endsWith("pxp")) it.setImage(this.filePxpImage); else if (f.getName().endsWith("pdf")) it.setImage(this.filePdfImage); else if (f.getName().endsWith("ps")) it.setImage(this.filePsImage); else if (f.getName().endsWith("dvi")) it.setImage(this.fileDviImage);
                    it.setData(TREE_FILE, f);
                    it.setData(TREE_FILE_TYPE, FILE);
                    items.add(it);
                }
            }
        }
        return items;
    }

    /**
	 * D�finit les r�pertoires racine
	 * @param dirs
	 */
    public final void setRoot(File dirs) {
        if (dirs.isDirectory()) {
            this.root = dirs;
            this.setRoots(new File[] { dirs });
        }
    }

    /**
	 * D�finit les r�pertoires racine
	 * @param dirs
	 */
    public final void setRoots(File[] dirs) {
        for (File dir : dirs) {
            if (dir.isDirectory()) {
                TreeItem newItem = new TreeItem(this.tree, SWT.NONE);
                newItem.setText(dir.getName());
                newItem.setData(TREE_FILE, dir);
                newItem.setData(TREE_FILE_TYPE, ROOT);
                newItem.setImage(this.closedRootImage);
                new TreeItem(newItem, SWT.NONE);
            }
        }
    }

    /**
	 * Ajoute un volume
	 * @param volume
	 */
    public final void addVolume(File volume) {
        if (volume.isDirectory()) {
            TreeItem newItem = new TreeItem(this.tree, SWT.NONE);
            newItem.setText(volume.getPath());
            newItem.setData(TREE_FILE, volume);
            newItem.setData(TREE_FILE_TYPE, ROOT);
            newItem.setImage(this.closedRootImage);
            new TreeItem(newItem, SWT.NONE);
        }
    }

    /**
	 * Retourne le fichier s�lectionn�
	 * @return
	 */
    public final File getSelectedFile() {
        TreeItem[] sel = this.tree.getSelection();
        if (sel != null && sel.length == 1) {
            return (File) sel[0].getData(TREE_FILE);
        }
        return null;
    }

    /**
	 * Retourne le fichier s�lectionn�
	 * @return
	 */
    public final TreeItem getSelectedTreeItem() {
        TreeItem[] sel = this.tree.getSelection();
        if (sel != null && sel.length == 1) {
            return sel[0];
        }
        return null;
    }

    /**
	 * Ajoute un volume
	 * @param volume
	 */
    public final void setVolumes(File[] volumes) {
        for (File vol : volumes) {
            this.addVolume(vol);
        }
    }
}
