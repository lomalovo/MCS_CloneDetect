package cn.myapps.util.pdf;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.InputSource;
import com.lowagie.text.DocListener;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.html.HtmlPeer;
import com.lowagie.text.html.HtmlTagMap;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.SAXmyHtmlHandler;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PdfUtil {

    public static ObpmPdfDocument createDocument(String webFilename, String watermark) {
        return new ObpmPdfDocument(webFilename, watermark);
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<html>");
        stringBuffer.append("<body>");
        stringBuffer.append("这是一个表格66:");
        stringBuffer.append("<table border='1'  bgColor='#ffffff'>");
        stringBuffer.append("<tr style='background-color:#eeeeee'>");
        stringBuffer.append("<td bgcolor='#eeeeee' colSpan='2'>");
        stringBuffer.append("<table>");
        stringBuffer.append("<tr><td>表格嵌套2</td><td>表格嵌套2</td></tr>");
        stringBuffer.append("<tr><td>嵌套单元3</td><td>嵌套单元3</td></tr>");
        stringBuffer.append("</table>");
        stringBuffer.append("</td>");
        stringBuffer.append("<td>单元1</td>");
        stringBuffer.append("</tr>");
        stringBuffer.append("<tr>");
        stringBuffer.append("<td colSpan='2'>单元2</td><td>单元2</td>");
        stringBuffer.append("</tr>");
        stringBuffer.append("<tr>");
        stringBuffer.append("<td colSpan='2'>单元3</td><td>单元3</td>");
        stringBuffer.append("</tr>");
        stringBuffer.append("<tr>");
        stringBuffer.append("<td colSpan='2'><p>单元4</p></td><td>单元4</td>");
        stringBuffer.append("</tr>");
        stringBuffer.append("<tr>");
        stringBuffer.append("<td colSpan='2'>单元5</td><td>单元5</td>");
        stringBuffer.append("</tr>");
        stringBuffer.append("</table><br/>");
        stringBuffer.append("</body>");
        stringBuffer.append("</html>");
        System.out.println(stringBuffer.toString());
        PdfReader reader = new PdfReader("d:/test.pdf");
        PdfReader reader1 = new PdfReader("d:/test1.pdf");
        PdfReader reader2 = new PdfReader("d:/test2.pdf");
        Document document = new Document(reader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream("d:/marge.pdf"));
        document.open();
        int pageSize = reader.getNumberOfPages();
        for (int i = 1; i <= pageSize; i++) {
            PdfImportedPage page = copy.getImportedPage(reader, i);
            copy.addPage(page);
        }
        pageSize = reader1.getNumberOfPages();
        for (int i = 1; i <= pageSize; i++) {
            PdfImportedPage page = copy.getImportedPage(reader, i);
            copy.addPage(page);
        }
        pageSize = reader2.getNumberOfPages();
        for (int i = 1; i <= pageSize; i++) {
            PdfImportedPage page = copy.getImportedPage(reader, i);
            copy.addPage(page);
        }
        document.close();
    }

    /**
	 * Export the html text to a PDF file
	 * 
	 * @param html
	 *            the html source code.
	 * @param outputStream
	 *            the target output stream.
	 * @throws Exception
	 */
    public static void htmlToPDF(String html, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        try {
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new PageNumbersWatermark(bfChinese, "Teemlink"));
            document.open();
            StyleSheet st = new StyleSheet();
            st.loadTagStyle("body", ElementTags.FACE, "STSong-Light");
            st.loadTagStyle("body", ElementTags.ENCODING, "UniGB-UCS2-H");
            st.loadTagStyle("body", ElementTags.SIZE, "12f");
            HashMap props0 = new HashMap();
            props0.put("border", "1f");
            props0.put("width", "100%");
            props0.put("font-size", "12f");
            st.loadStyle("display_view-table", props0);
            HashMap props1 = new HashMap();
            props1.put("text-align", "left");
            props1.put("size", "12f");
            props1.put("bgcolor", "#eeeeee");
            props1.put("color", "#1268a5");
            st.loadStyle("column-head", props1);
            HashMap props2 = new HashMap();
            props2.put("text-align", "left");
            props2.put("size", "12f");
            st.loadStyle("column-td", props2);
            ArrayList p = HTMLWorker.parseToList(new StringReader(html), st);
            for (int k = 0; k < p.size(); ++k) {
                Element el = (Element) p.get(k);
                document.add(el);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}

/**
 * The inner class extend the com.lowagie.text.html.HtmlParser, its purpose is
 * to support the Chinese.
 */
class ITextSurportHtmlParser extends HtmlParser {

    public ITextSurportHtmlParser() {
        super();
    }

    public void goGB(DocListener document, InputStream is) {
        try {
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            HtmlTagMap myTags = new HtmlTagMap();
            HtmlPeer peer = new HtmlPeer(ElementTags.PARAGRAPH, HtmlTags.PARAGRAPH);
            myTags.remove(peer.getAlias());
            parser.parse(new InputSource(is), new SAXmyHtmlHandler(document, bfChinese));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
