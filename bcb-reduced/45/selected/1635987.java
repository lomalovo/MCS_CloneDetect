package net.sf.tidysaucer;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import net.sf.tidysaucer.actions.ZoomAction;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.DOMInspector;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xhtmlrenderer.util.Configuration;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserMenuBar extends JMenuBar {

    /**
     * Description of the Field
     */
    BrowserStartup root;

    /**
     * Description of the Field
     */
    JMenu file;

    /**
     * Description of the Field
     */
    JMenu edit;

    /**
     * Description of the Field
     */
    JMenu view;

    /**
     * Description of the Field
     */
    JMenu go;

    /**
     * Description of the Field
     */
    JMenuItem view_source;

    /**
     * Description of the Field
     */
    JMenu debug;

    private JMenu help;

    /**
     * Constructor for the BrowserMenuBar object
     *
     * @param root PARAM
     */
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }

    /**
     * Description of the Method
     */
    public void init() {
        file = new JMenu("Browser");
        file.setMnemonic('B');
        debug = new JMenu("Debug");
        debug.setMnemonic('U');
        view = new JMenu("View");
        view.setMnemonic('V');
        help = new JMenu("Help");
        help.setMnemonic('H');
        view_source = new JMenuItem("Page Source");
        view_source.setEnabled(false);
        view.add(root.actions.stop);
        view.add(root.actions.refresh);
        view.add(root.actions.reload);
        view.add(new JSeparator());
        JMenu text_size = new JMenu("Text Size");
        text_size.setMnemonic('T');
        text_size.add(root.actions.increase_font);
        text_size.add(root.actions.decrease_font);
        text_size.add(new JSeparator());
        text_size.add(root.actions.reset_font);
        view.add(text_size);
        go = new JMenu("Go");
        go.setMnemonic('G');
    }

    /**
     * Description of the Method
     */
    public void createLayout() {
        final ScalableXHTMLPanel panel = root.panel.view;
        file.add(root.actions.open_file);
        file.add(new JSeparator());
        file.add(root.actions.export_pdf);
        file.add(new JSeparator());
        file.add(root.actions.quit);
        add(file);
        JMenu zoom = new JMenu("Zoom");
        zoom.setMnemonic('Z');
        ScaleFactor[] factors = this.initializeScales();
        ButtonGroup zoomGroup = new ButtonGroup();
        for (int i = 0; i < factors.length; i++) {
            ScaleFactor factor = factors[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(new ZoomAction(panel, factor));
            if (factor.isNotZoomed()) item.setSelected(true);
            zoomGroup.add(item);
            zoom.add(item);
        }
        view.add(new JSeparator());
        view.add(zoom);
        view.add(new JSeparator());
        view.add(new JCheckBoxMenuItem(root.actions.print_preview));
        add(view);
        go.add(root.actions.forward);
        go.add(root.actions.backward);
        add(go);
        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');
        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));
        debugShow.add(new JCheckBoxMenuItem(new FontMetricsAction()));
        JMenu anti = new JMenu("Anti Aliasing");
        ButtonGroup anti_level = new ButtonGroup();
        addLevel(anti, anti_level, "None", -1);
        addLevel(anti, anti_level, "Low", 25).setSelected(true);
        addLevel(anti, anti_level, "Medium", 12);
        addLevel(anti, anti_level, "High", 0);
        debug.add(anti);
        debug.add(new ShowDOMInspectorAction());
        debug.add(new AbstractAction("Validation Console") {

            public void actionPerformed(ActionEvent evt) {
                if (root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    JFrame frame = root.validation_console;
                    JTextArea jta = new JTextArea();
                    root.error_handler.setTextArea(jta);
                    jta.setEditable(false);
                    jta.setLineWrap(true);
                    jta.setText("Validation Console: XML Parsing Error Messages");
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(new JScrollPane(jta), "Center");
                    JButton close = new JButton("Close");
                    frame.getContentPane().add(close, "South");
                    close.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {
                            root.validation_console.setVisible(false);
                        }
                    });
                    frame.pack();
                    frame.setSize(400, 300);
                }
                root.validation_console.setVisible(true);
            }
        });
        add(debug);
        help.add(root.actions.aboutPage);
        add(help);
    }

    private JRadioButtonMenuItem addLevel(JMenu menu, ButtonGroup group, String title, int level) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(new AntiAliasedAction(title, level));
        group.add(item);
        menu.add(item);
        return item;
    }

    /**
     * Description of the Method
     */
    public void createActions() {
        if (Configuration.isTrue("xr.use.listeners", true)) {
            List l = root.panel.view.getMouseTrackingListeners();
            for (Iterator i = l.iterator(); i.hasNext(); ) {
                FSMouseListener listener = (FSMouseListener) i.next();
                if (listener instanceof LinkListener) {
                    root.panel.view.removeMouseTrackingListener(listener);
                }
            }
            root.panel.view.addMouseTrackingListener(new CustomLinkListener() {

                public void onMouseOut(BasicPanel panel, Box box) {
                    root.panel.setStatus("");
                }

                public void onMouseOver(final BasicPanel panel, final Box box) {
                    final String uri = findLink(panel, box);
                    if (uri != null) {
                        final String url = panel.getSharedContext().getUac().resolveURI(uri);
                        root.panel.setStatus(url);
                    }
                }

                public void linkClicked(BasicPanel panel, String uri) {
                    root.panel.loadPage(uri);
                }
            });
        }
    }

    private ScaleFactor[] initializeScales() {
        ScaleFactor[] scales = new ScaleFactor[11];
        int i = 0;
        scales[i++] = new ScaleFactor(1.0d, "Normal (100%)");
        scales[i++] = new ScaleFactor(2.0d, "200%");
        scales[i++] = new ScaleFactor(1.5d, "150%");
        scales[i++] = new ScaleFactor(0.85d, "85%");
        scales[i++] = new ScaleFactor(0.75d, "75%");
        scales[i++] = new ScaleFactor(0.5d, "50%");
        scales[i++] = new ScaleFactor(0.33d, "33%");
        scales[i++] = new ScaleFactor(0.25d, "25%");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WIDTH, "Page width");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_HEIGHT, "Page height");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WHOLE, "Whole page");
        return scales;
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class ShowDOMInspectorAction extends AbstractAction {

        /**
         * Description of the Field
         */
        private DOMInspector inspector;

        /**
         * Description of the Field
         */
        private JFrame inspectorFrame;

        /**
         * Constructor for the ShowDOMInspectorAction object
         */
        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            if (inspectorFrame == null) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if (inspector == null) {
                inspector = new DOMInspector(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());
                inspectorFrame.getContentPane().add(inspector);
                inspectorFrame.pack();
                inspectorFrame.setSize(500, 600);
                inspectorFrame.show();
            } else {
                inspector.setForDocument(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());
            }
            inspectorFrame.show();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class BoxOutlinesAction extends AbstractAction {

        /**
         * Constructor for the BoxOutlinesAction object
         */
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_boxes(!root.panel.view.getSharedContext().debugDrawBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LineBoxOutlinesAction extends AbstractAction {

        /**
         * Constructor for the LineBoxOutlinesAction object
         */
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_line_boxes(!root.panel.view.getSharedContext().debugDrawLineBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class InlineBoxesAction extends AbstractAction {

        /**
         * Constructor for the InlineBoxesAction object
         */
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_inline_boxes(!root.panel.view.getSharedContext().debugDrawInlineBoxes());
            root.panel.view.repaint();
        }
    }

    class FontMetricsAction extends AbstractAction {

        /**
         * Constructor for the InlineBoxesAction object
         */
        FontMetricsAction() {
            super("Show Font Metrics");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_font_metrics(!root.panel.view.getSharedContext().debugDrawFontMetrics());
            root.panel.view.repaint();
        }
    }

    class AntiAliasedAction extends AbstractAction {

        int fontSizeThreshold;

        AntiAliasedAction(String text, int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
            root.panel.view.repaint();
        }
    }
}

/**
 * Description of the Class
 *
 * @author empty
 */
class EmptyAction extends AbstractAction {

    public EmptyAction(String name, Icon icon) {
        this(name, "", icon);
    }

    public EmptyAction(String name, String shortDesc, Icon icon) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, shortDesc);
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name  PARAM
     * @param accel PARAM
     */
    public EmptyAction(String name, int accel) {
        this(name);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name PARAM
     */
    public EmptyAction(String name) {
        super(name);
    }

    /**
     * Description of the Method
     *
     * @param evt PARAM
     */
    public void actionPerformed(ActionEvent evt) {
    }
}
