package net.community.apps.tools.itext.pdfconcat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingWorker;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 12:58:27 PM
 */
public class PDFConcatenator extends SwingWorker<Void, File> {

    private static final LoggerWrapper _logger = WrapperFactoryManager.getLogger(PDFConcatenator.class);

    private final MainFrame _f;

    public final MainFrame getMainFrameInstance() {
        return _f;
    }

    private final Collection<? extends File> _fl;

    public final Collection<? extends File> getInputFiles() {
        return _fl;
    }

    private final String _outFile;

    public final String getOutputFile() {
        return _outFile;
    }

    public PDFConcatenator(final MainFrame f, final Collection<? extends File> fl, final String outFile) {
        if ((null == (_f = f)) || (null == (_fl = fl)) || (fl.size() <= 0) || (null == (_outFile = outFile)) || (outFile.length() <= 0)) throw new IllegalArgumentException("Incomplete arguments");
    }

    @Override
    protected Void doInBackground() throws Exception {
        int pageOffset = 0;
        Document document = null;
        PdfCopy writer = null;
        List<Object> master = new ArrayList<Object>();
        final Collection<? extends File> fl = getInputFiles();
        final MainFrame parent = getMainFrameInstance();
        final String outFile = getOutputFile();
        for (final Iterator<? extends File> fi = ((null == fl) || (fl.size() <= 0)) ? null : fl.iterator(); (fi != null) && fi.hasNext() && (!isCancelled()); ) {
            final File f = fi.next();
            if (null == f) continue;
            publish(f);
            _logger.info("processing " + f + " start");
            final long pStart = System.currentTimeMillis();
            try {
                final PdfReader reader = new PdfReader(f.getAbsolutePath());
                reader.consolidateNamedDestinations();
                final int n = reader.getNumberOfPages();
                final List<?> bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset > 0) SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                    master.addAll(bookmarks);
                }
                pageOffset += n;
                if (null == document) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfCopy(document, new FileOutputStream(outFile));
                    document.open();
                    _logger.info("Opened output=" + outFile);
                }
                for (int i = 1; i <= n; i++) {
                    final PdfImportedPage page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                final PRAcroForm form = reader.getAcroForm();
                if (form != null) writer.copyAcroForm(reader);
            } catch (Exception e) {
                final long pEnd = System.currentTimeMillis(), pDuration = pEnd - pStart;
                _logger.error(e.getClass().getName() + " while handle input=" + f + " after " + pDuration + " msec.: " + e.getMessage(), e);
                BaseOptionPane.showMessageDialog(parent, e);
                break;
            }
            final long pEnd = System.currentTimeMillis(), pDuration = pEnd - pStart;
            _logger.info("processing " + f + " processed " + f.length() + " bytes in " + pDuration + " msec.");
        }
        try {
            if (!master.isEmpty()) writer.setOutlines(master);
            if (document != null) document.close();
            _logger.info("Closing output=" + outFile);
        } catch (Exception e) {
            _logger.error(e.getClass().getName() + " while finalize output to " + outFile + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(parent, e);
        }
        return null;
    }

    @Override
    protected void done() {
        final MainFrame f = getMainFrameInstance();
        if (null == f) return;
        f.done(this);
    }

    @Override
    protected void process(List<File> chunks) {
        if ((null == chunks) || (chunks.size() <= 0)) return;
        final MainFrame f = getMainFrameInstance();
        if (null == f) return;
        for (final File c : chunks) {
            final String p = (null == c) ? null : c.getAbsolutePath();
            if ((p != null) && (p.length() > 0)) f.updateStatusBar(p);
        }
    }
}
