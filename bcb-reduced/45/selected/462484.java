package org.charleslab.tools.export;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * 
 * @author chao.cheng
 * @createrTime: 2008-8-19 上午10:17:49
 */
public class ExportPDF extends Export {

    public ExportPDF(HttpServletResponse httpServletResponse, Collection<?> collection, Class<?> c) {
        super(httpServletResponse, collection, c);
    }

    public void export() {
        Iterator<?> it = getCollection().iterator();
        try {
            Document document = new Document();
            OutputStream outputStream = getHttpServletResponse().getOutputStream();
            getHttpServletResponse().reset();
            getHttpServletResponse().setHeader("content-disposition", "attachment;filename=" + getC().getSimpleName() + ".pdf");
            getHttpServletResponse().setContentType("APPLICATION/pdf");
            PdfWriter.getInstance(document, outputStream);
            document.open();
            List<String> header = new ArrayList<String>();
            Method[] ms = getC().getMethods();
            for (int i = 0; i < ms.length; i++) {
                String name = ms[i].getName();
                if (name.startsWith("set")) {
                    header.add(name.substring(3));
                }
            }
            PdfPTable table = new PdfPTable(header.size());
            for (String s : header) {
                table.addCell(s);
            }
            while (it.hasNext()) {
                Object o = getC().cast(it.next());
                for (Method m : ms) {
                    String name = m.getName();
                    if (name.startsWith("get") && !name.startsWith("getClass")) {
                        table.addCell(m.invoke(o) == null ? ("") : (m.invoke(o).toString()));
                    }
                }
            }
            document.add(table);
            document.close();
            getHttpServletResponse().flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
