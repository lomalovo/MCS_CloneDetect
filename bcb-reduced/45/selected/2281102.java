package edu.asu.vogon.pdf.viewer.ui.control;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.icepdf.core.pobjects.PageTree;
import edu.asu.vogon.model.GeneralTextTerm;
import edu.asu.vogon.model.SpecificTextTerm;
import edu.asu.vogon.model.Text;
import edu.asu.vogon.pdf.viewer.Activator;
import edu.asu.vogon.pdf.viewer.Constants;
import edu.asu.vogon.pdf.viewer.icepdf.VogonSwingController;
import edu.asu.vogon.util.properties.PropertyHandler;
import edu.asu.vogon.util.properties.PropertyHandlerRegistry;

public class WordTextHighlightRunnable implements IRunnableWithProgress {

    private VogonSwingController controller;

    private String filePath;

    private Text pdfText;

    public WordTextHighlightRunnable(VogonSwingController controller, String filePath, Text pdfText) {
        this.controller = controller;
        this.filePath = filePath;
        this.pdfText = pdfText;
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        controller.openDocument(filePath);
        PageTree pageTree = controller.getPageTree();
        PropertyHandler handler = PropertyHandlerRegistry.REGISTRY.getPropertyHandler(Activator.PLUGIN_ID, Constants.PROPERTIES_FILE);
        monitor.beginTask(handler.getProperty("_open_pdf_progress_opening"), pageTree.getNumberOfPages());
        List<GeneralTextTerm> results = pdfText.getResults();
        List<SpecificTextTerm> resultsCopy = new ArrayList<SpecificTextTerm>();
        for (GeneralTextTerm r : results) {
            if (r instanceof SpecificTextTerm) resultsCopy.add((SpecificTextTerm) r);
        }
        TextHighlighter highlighter = new TextHighlighter(pageTree, controller);
        try {
            highlighter.highlightResults(resultsCopy, monitor);
        } catch (InterruptedException e) {
            controller.closeDocument();
            throw new InterruptedException(handler.getProperty("_open_pdf_progress_cancel"));
        }
        monitor.done();
    }
}
