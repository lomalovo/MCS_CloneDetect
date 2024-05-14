package org.tockit.docco.documenthandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.ArrayList;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;
import org.tockit.docco.filefilter.DoccoFileFilter;
import org.tockit.docco.filefilter.ExtensionFileFilterFactory;
import org.tockit.docco.indexer.DocumentSummary;
import org.tockit.plugin.Plugin;

public class PdfDocumentHandler implements DocumentHandler, Plugin {

    /**
	 * Pretty much copy and paste code from the PDFbox LucenePDFDocument class.
	 */
    public DocumentSummary parseDocument(URL url) throws IOException, DocumentHandlerException {
        DocumentSummary docSummary = new DocumentSummary();
        PDDocument pdfDocument = null;
        try {
            PDFParser parser = new PDFParser(url.openStream());
            parser.parse();
            pdfDocument = parser.getPDDocument();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText(pdfDocument, writer);
            writer.close();
            byte[] contents = out.toByteArray();
            docSummary.contentReader = new InputStreamReader(new ByteArrayInputStream(contents));
            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            if (info.getAuthor() != null) {
                docSummary.authors = new ArrayList();
                docSummary.authors.add(info.getAuthor());
            }
            if (info.getKeywords() != null) {
                docSummary.keywords = new ArrayList();
                docSummary.keywords.add(info.getKeywords());
            }
            if (info.getModificationDate() != null) {
                Date date = info.getModificationDate().getTime();
                if (date.getTime() >= 0) {
                    docSummary.modificationDate = date;
                }
            }
            if (info.getTitle() != null) {
                docSummary.title = info.getTitle();
            }
        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
        }
        return docSummary;
    }

    public String getDisplayName() {
        return "PDF using pdfbox";
    }

    public DoccoFileFilter getDefaultFilter() {
        return new ExtensionFileFilterFactory().createNewFilter("pdf");
    }

    public void load() {
        DocumentHandlerRegistry.registerDocumentHandler(this);
    }
}
