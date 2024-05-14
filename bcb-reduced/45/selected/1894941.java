package org.nodevision.portal.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.poi.hdf.extractor.WordDocument;
import org.nodevision.portal.om.searchconfig.Search;
import org.nodevision.portal.repositories.RepositorySearch;
import org.pdfbox.searchengine.lucene.LucenePDFDocument;
import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;

final class NVSearch {

    private final ArrayList visitedPages = new ArrayList();

    private IndexWriter writer;

    private final String name;

    public NVSearch(final String name) {
        this.name = name;
    }

    public final void createIndex() {
        try {
            final Search search = RepositorySearch.getSearchesStatic().getSearch(name);
            deleteDir(new File(search.getPath() + "_temp"));
            writer = new IndexWriter(search.getPath() + "_temp", new StandardAnalyzer(), true);
            writer.maxFieldLength = 1000000;
            if ("filesystem".equalsIgnoreCase(search.getType())) {
                crawlFS(search);
            } else if ("crawler".equalsIgnoreCase(search.getType())) {
                crawlPage(search);
            }
            writer.optimize();
            writer.close();
            deleteDir(new File(search.getPath()));
            new File(search.getPath() + "_temp").renameTo(new File(search.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private void crawlPage(final Search search) {
        try {
            crawlHttp(search.getStart());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void crawlFS(final Search search) {
        try {
            crawlFSDir(new File(search.getStart()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void crawlHttp(final String url) throws Exception {
        final URL getUrl = new URL(url);
        URLConnection conn = getUrl.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        String line;
        final BufferedReader rdResponse = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        final StringBuffer content = new StringBuffer();
        while (null != (line = rdResponse.readLine())) {
            content.append(line);
        }
        rdResponse.close();
        visitedPages.add(new URL(url).getPath());
        final Source source = new Source(content.toString());
        try {
            final List elements = source.findAllStartTags("A");
            if (null != elements) {
                for (Iterator i = elements.iterator(); i.hasNext(); ) {
                    final StartTag startTag = (StartTag) i.next();
                    final Attributes attributes = startTag.getAttributes();
                    final Attribute hrefAttribute = attributes.get("href");
                    if (null != hrefAttribute) {
                        final URL oldUrl = new URL(url);
                        final URL newUrl = new URL(oldUrl, hrefAttribute.getValue());
                        if (newUrl.getHost().equalsIgnoreCase(oldUrl.getHost()) && !visitedPages.contains(newUrl.getPath())) {
                            crawlHttp(newUrl.toString());
                        }
                    }
                }
            }
            if (new URL(url).getPath().endsWith("html") || new URL(url).getPath().endsWith("htm") || new URL(url).getPath().endsWith("txt") || new URL(url).getPath().endsWith("jsp")) {
                indexHTML(content.toString(), url);
            } else if (new URL(url).getPath().endsWith("pdf")) {
                indexPDF(LucenePDFDocument.getDocument(new URL(url)));
            } else if (new URL(url).getPath().endsWith("doc")) {
                conn = getUrl.openConnection();
                final WordDocument d = new WordDocument(conn.getInputStream());
                final StringWriter wr = new StringWriter();
                d.writeAllText(wr);
                indexDoc(wr.toString(), url);
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    private void crawlFSDir(final File path) throws Exception {
        if (!path.isDirectory()) {
            indexFS(path.getAbsolutePath());
        } else {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    crawlFSDir(files[i]);
                } else {
                    indexFS(files[i].getAbsolutePath());
                }
            }
        }
    }

    private synchronized void indexFS(final String path) {
        if (path.endsWith("html") || path.endsWith("htm") || path.endsWith("txt")) {
            try {
                final BufferedReader in = new BufferedReader(new FileReader(path));
                String str;
                final StringBuffer content = new StringBuffer();
                while (null != (str = in.readLine())) {
                    content.append(str);
                }
                indexHTML(content.toString(), path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (path.endsWith("pdf")) {
            try {
                indexPDF(LucenePDFDocument.getDocument(new File(path)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (path.endsWith("doc")) {
            try {
                final WordDocument d = new WordDocument(path);
                final StringWriter wr = new StringWriter();
                d.writeAllText(wr);
                indexDoc(wr.toString(), path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void indexHTML(String content, final String url) throws Exception {
        String title = NVSearch.getTitle(content);
        content = NVSearch.extractText(content);
        content = NVSearch.eliminateWhitespaces(content);
        final Field contentField = new Field("contents", content.toLowerCase(), false, true, true);
        final Field urlField = new Field("url", url, true, true, true);
        final Field sizeField = new Field("size", String.valueOf(content.length()), true, false, false, false);
        int length = 500;
        if (500 > content.length()) {
            length = content.length();
        }
        final String summary = content.substring(0, length);
        final Field summaryField = new Field("summary", summary, true, false, false, false);
        if (null == title) {
            title = url;
        }
        final Field titleField = new Field("title", title, true, true, true);
        final Document document = new Document();
        document.add(contentField);
        document.add(urlField);
        document.add(sizeField);
        document.add(summaryField);
        document.add(titleField);
        writer.addDocument(document);
    }

    private synchronized void indexPDF(final Document document) {
        try {
            writer.addDocument(document);
        } catch (Exception e) {
        }
    }

    private synchronized void indexDoc(String content, final String url) throws Exception {
        content = NVSearch.eliminateWhitespaces(content);
        final Field contentField = new Field("contents", content.toLowerCase(), false, true, true);
        final Field urlField = new Field("url", url, true, true, true);
        final Field sizeField = new Field("size", String.valueOf(content.length()), true, false, false, false);
        int length = 500;
        if (500 > content.length()) {
            length = content.length();
        }
        final String summary = content.substring(0, length);
        final Field summaryField = new Field("summary", summary, true, false, false, false);
        final Field titleField = new Field("title", url, true, true, true);
        final Document document = new Document();
        document.add(contentField);
        document.add(urlField);
        document.add(sizeField);
        document.add(summaryField);
        document.add(titleField);
        writer.addDocument(document);
    }

    private static String eliminateWhitespaces(final String content) {
        final Pattern p = Pattern.compile("\\s{2,}");
        final Matcher matcher = p.matcher(content);
        final StringBuffer renderedContent = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(renderedContent, " ");
        }
        matcher.appendTail(renderedContent);
        return renderedContent.toString();
    }

    private static String extractText(final String content) {
        final Pattern p = Pattern.compile("<(.*?)>");
        final Matcher matcher = p.matcher(content);
        final StringBuffer renderedContent = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(renderedContent, "");
        }
        matcher.appendTail(renderedContent);
        return renderedContent.toString();
    }

    private static String getTitle(final String content) {
        try {
            final Source source = new Source(content);
            final List elements = source.findAllStartTags("title");
            if (null != elements) {
                final StartTag titleTag = (StartTag) elements.get(0);
                final EndTag endTag = titleTag.findEndTag();
                final Segment seg = new Segment(source, titleTag.getEnd(), endTag.getBegin());
                return seg.toString().trim();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public final HashMap search(final String queryString, final int start, int end) throws IOException, ParseException {
        final HashMap returnHits = new HashMap();
        final Search search = RepositorySearch.getSearchesStatic().getSearch(name);
        final Searcher searcher = new IndexSearcher(search.getPath());
        final Analyzer analyzer = new StandardAnalyzer();
        final Query query = MultiFieldQueryParser.parse(queryString, new String[] { "contents", "title", "url" }, analyzer);
        final Hits hits = searcher.search(query);
        final HashMap documents = new HashMap();
        if (hits.length() < end || -1 == end) {
            end = hits.length();
        }
        for (int i = start; i < end; i++) {
            final Document doc = hits.doc(i);
            final HashMap document = new HashMap();
            document.put("url", doc.get("url"));
            document.put("summary", doc.get("summary"));
            document.put("size", doc.get("size"));
            document.put("title", doc.get("title"));
            document.put("score", String.valueOf(Math.round(hits.score(i) * 100)));
            documents.put(doc.get("url"), document);
        }
        returnHits.put("hits", String.valueOf(hits.length()));
        returnHits.put("query", queryString);
        returnHits.put("documents", documents);
        searcher.close();
        return returnHits;
    }
}
