package com.sitechasia.webx.core.utils.commons;

import java.awt.Color;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sitechasia.webx.core.exception.FrameworkException;

/**
 * 数据转换工具类,该类定义为final类型<br>
 * 工具类不允许继承,所有方法均为类的静态方法,供用户直接调用<br>
 * <p>
 * --- 功 能 描 述 ---
 * </p>
 * 1.Java Bean与微软的OLE Excel文档xls的数据转换;<br>
 * 2.Java Bean转换为标准的PDF格式文档.<br>
 * <p>
 * excel文档格式规则约定：
 * </p>
 * 1.<b>excel文档的第一行为标题,从第二行开始为所需转换的数据</b>;<br>
 * 2.<b>标题与数据之间、各行数据之间不要产生空行或无效的数据</b>。
 * <p>
 * --- Refactory Log ---
 * <p>
 *
 * @author Administrator
 * @author todd
 * @version 1.2 , 2008/5/7
 * @since JDK1.5
 */
public final class DataConversionUtils {

    private static final String PDF_FONT_STYLE = "STSong-Light";

    private static final String PDF_FONT_ENCODING = "UniGB-UCS2-H";

    private static final Font PDF_FONT_CHINESE;

    static {
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont(PDF_FONT_STYLE, PDF_FONT_ENCODING, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bfChinese != null) {
            PDF_FONT_CHINESE = new Font(bfChinese);
        } else {
            PDF_FONT_CHINESE = new Font();
        }
    }

    private DataConversionUtils() {
    }

    /**
	 * 将java bean转换为excel文档<br>
	 * 每一个bean对应excel文档的一行数据,每一个bean属性对应一行数据的一个cell<br>
	 *
	 * @param <T>
	 *            指定的bean的类型
	 * @param titlebeanNameMap
	 *            bean属性名与excel列标题的对应map
	 * @param beans
	 *            bean容器
	 * @param excelFilePath
	 *            excel文档的绝对路径
	 * @throws FrameworkException
	 *
	 *
	 * <pre>
	 * Map titlebeanNameMap = new HashMap();
	 * titlebeanNameMap.put(&quot;name&quot;, &quot;姓名&quot;);
	 * titlebeanNameMap.put(&quot;age&quot;, &quot;年龄&quot;);
	 * List beans = new ArrayList();
	 * FooBean bean1 = new FooBean();
	 * bean1.setName(&quot;张三&quot;);
	 * bean1.setAge(&quot;27&quot;);
	 * beans.add(0, bean1);
	 * FooBean bean2 = new FooBean();
	 * bean2.setName(&quot;李四&quot;);
	 * bean2.setAge(&quot;25&quot;);
	 * beans.add(1, bean2);
	 * try {
	 * 	DataExchangeUtils.beans2excelFile(titlebeanNameMap, beans, &quot;/foo/foo.xls&quot;);
	 * } catch (FrameworkException frwke) {
	 * 	//处理异常
	 * }
	 * </pre>
	 */
    public static <T> void beans2excelFile(Map<String, String> titlebeanNameMap, List<T> beans, String excelFilePath) throws FrameworkException {
        if (titlebeanNameMap == null || titlebeanNameMap.size() == 0) {
            throw new FrameworkException("创建excel文档时参数titlebeanNameMap为空");
        }
        if (beans == null || beans.size() == 0) {
            throw new FrameworkException("创建excel文档时参数beans为空");
        }
        if (excelFilePath == null || excelFilePath.length() == 0) {
            throw new FrameworkException("创建excel文档时参数excelFilePath为空");
        }
        HSSFWorkbook wb = creatWorkbook((String) null);
        buildExcelDocument(wb, titlebeanNameMap, beans);
        createExcelFile(wb, excelFilePath);
    }

    /**
	 * 将excel文档转换为所需的java bean<br>
	 * excel数据的每一行对应一个指定类型的bean对象,每一行的一个cell对应bean的一个属性<br>
	 *
	 * @param <T>
	 *            指定的bean的类型
	 * @param beanAttrNames
	 *            bean属性名容器list, <b>该容器中的bean属性名的顺序与excel文档数据的显示顺序必须一致</b>
	 * @param beanClass
	 *            bean的Class对象
	 * @param excelFilePath
	 *            excel文档的绝对路径
	 * @return bean容器List
	 * @throws FrameworkException
	 *
	 *
	 * <pre>
	 * List attrs = new ArrayList();
	 * attrs.add(&quot;age&quot;);
	 * attrs.add(&quot;name&quot;);
	 * List beans = null;
	 * try {
	 * 	beans = DataExchangeUtils.excelFile2beans(attrs, FooBean.class, &quot;/foo/foo.xls&quot;);
	 * } catch (FrameworkException frwke) {
	 * 	//处理异常
	 * }
	 * </pre>
	 */
    public static <T> List<T> excelFile2beans(List<String> beanAttrNames, Class<T> beanClass, String excelFilePath) throws FrameworkException {
        if (beanAttrNames == null || beanAttrNames.size() == 0) {
            throw new FrameworkException("解析excel文档时参数beanAttrNames为空");
        }
        if (beanClass == null) {
            throw new FrameworkException("解析excel文档时参数beanClass为空");
        }
        if (excelFilePath == null || excelFilePath.length() == 0) {
            throw new FrameworkException("解析excel文档时参数excelFilePath为空");
        }
        HSSFWorkbook wb = creatWorkbook(excelFilePath);
        return parseExcelDocument(wb, beanAttrNames, beanClass);
    }

    /**
	 * 将java bean转换为pdf文档<br>
	 * 每一个bean对应pdf文档的一行数据,每一个bean属性对应一行数据的一个cell<br>
	 *
	 * @param <T>
	 *            指定的bean的类型
	 * @param titlebeanNameMap
	 *            bean属性名与pdf列标题的对应map
	 * @param beans
	 *            bean容器
	 * @param pdfFilePath
	 *            pdf文档的绝对路径
	 * @throws FrameworkException
	 *
	 *
	 * <pre>
	 * Map titlebeanNameMap = new HashMap();
	 * titlebeanNameMap.put(&quot;name&quot;, &quot;姓名&quot;);
	 * titlebeanNameMap.put(&quot;age&quot;, &quot;年龄&quot;);
	 * List beans = new ArrayList();
	 * FooBean bean1 = new FooBean();
	 * bean1.setName(&quot;张三&quot;);
	 * bean1.setAge(&quot;27&quot;);
	 * beans.add(0, bean1);
	 * FooBean bean2 = new FooBean();
	 * bean2.setName(&quot;李四&quot;);
	 * bean2.setAge(&quot;25&quot;);
	 * beans.add(1, bean2);
	 * try {
	 * 	DataExchangeUtils.beans2pdfFile(titlebeanNameMap, beans, &quot;/foo/foo.pdf&quot;);
	 * } catch (FrameworkException frwke) {
	 * 	//处理异常
	 * }
	 * </pre>
	 */
    public static <T> void beans2pdfFile(Map<String, String> titlebeanNameMap, List<T> beans, String pdfFilePath) throws FrameworkException {
        if (titlebeanNameMap == null || titlebeanNameMap.size() == 0) {
            throw new FrameworkException("创建pdf文档时参数titlebeanNameMap为空");
        }
        if (beans == null || beans.size() == 0) {
            throw new FrameworkException("创建pdf文档时参数beans为空");
        }
        if (pdfFilePath == null || pdfFilePath.length() == 0) {
            throw new FrameworkException("创建pdf文档时参数pdfFilePath为空");
        }
        Document document = creatPdfDocument(pdfFilePath);
        buildPdfDocument(document, titlebeanNameMap, beans);
    }

    /**
	 * 创建新的excel workbook
	 *
	 * @param path
	 * @return 创建新的excel workbook
	 * @throws FrameworkException
	 */
    private static HSSFWorkbook creatWorkbook(String path) throws FrameworkException {
        HSSFWorkbook wb = null;
        if (path == null || path.length() == 0) {
            wb = new HSSFWorkbook();
        } else {
            try {
                POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(path));
                wb = new HSSFWorkbook(fs);
            } catch (IOException ioe) {
                throw new FrameworkException("创建新的excel文档对象模型HSSFWorkbook时产生异常", ioe);
            }
        }
        return wb;
    }

    /**
	 * 填充excel文档数据
	 *
	 * @param <T>
	 * @param wb
	 * @param titlebeanNameMap
	 * @param beans
	 * @throws FrameworkException
	 */
    @SuppressWarnings("unchecked")
    private static <T> void buildExcelDocument(HSSFWorkbook wb, Map<String, String> titlebeanNameMap, List<T> beans) throws FrameworkException {
        try {
            List<String> titles = new ArrayList();
            List<String> beanAttrNames = new ArrayList();
            int cnt = 0;
            for (Iterator i = titlebeanNameMap.keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String value = titlebeanNameMap.get(key);
                beanAttrNames.add(cnt, key);
                titles.add(cnt, value);
                cnt++;
            }
            HSSFSheet sheet = wb.createSheet();
            HSSFRow row0 = sheet.createRow(0);
            for (int i = 0; i < titles.size(); i++) {
                HSSFCell cell = row0.createCell((short) i);
                setExcelCellText(cell, titles.get(i));
            }
            PropertyDescriptor[] props = getBeanInfo(beans.get(0).getClass()).getPropertyDescriptors();
            for (int i = 0; i < beans.size(); i++) {
                T bean = beans.get(i);
                HSSFRow row = sheet.createRow(i + 1);
                for (int j = 0; j < beanAttrNames.size(); j++) {
                    String beanAttrName = beanAttrNames.get(j);
                    for (int k = 0; k < props.length; k++) {
                        String propName = props[k].getName();
                        if (propName.equals(beanAttrName)) {
                            HSSFCell cell = row.createCell((short) j);
                            Object cellValue = callGetter(bean, props[k]);
                            if (cellValue == null) {
                                cellValue = "";
                            }
                            setExcelCellText(cell, cellValue.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new FrameworkException("生成excel文档时,填充excel文档数据产生异常", e);
        }
    }

    /**
	 * 解析excel文档数据
	 *
	 * @param <T>
	 * @param wb
	 * @param beanAttrNames
	 * @param beanClass
	 * @return 解析excel文档数据
	 * @throws FrameworkException
	 */
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseExcelDocument(HSSFWorkbook wb, List<String> beanAttrNames, Class<T> beanClass) throws FrameworkException {
        List<T> beans = new ArrayList();
        try {
            HSSFSheet sheet = wb.getSheetAt(0);
            PropertyDescriptor[] props = getBeanInfo(beanClass).getPropertyDescriptors();
            int beanCnt = sheet.getLastRowNum();
            for (int i = 1; i <= beanCnt; i++) {
                T bean = beanClass.newInstance();
                for (int j = 0; j < beanAttrNames.size(); j++) {
                    HSSFCell cell = getCell(sheet, i, j);
                    String cellValue = cell.getStringCellValue();
                    if (cellValue == null) {
                        cellValue = "";
                    }
                    String beanAttrName = beanAttrNames.get(j);
                    for (int k = 0; k < props.length; k++) {
                        String propName = props[k].getName();
                        if (propName.equals(beanAttrName)) {
                            callSetter(bean, props[k], cellValue);
                        }
                    }
                }
                beans.add(bean);
            }
        } catch (Exception e) {
            throw new FrameworkException("解析excel文档时产生异常", e);
        }
        return beans;
    }

    /**
	 * 生成excel file文档
	 *
	 * @param wb
	 * @param excelFilePath
	 * @throws FrameworkException
	 */
    private static void createExcelFile(HSSFWorkbook wb, String excelFilePath) throws FrameworkException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(excelFilePath);
            wb.write(fileOut);
        } catch (IOException ioe) {
            throw new FrameworkException("生成excel文档时产生异常", ioe);
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException ioe) {
                    throw new FrameworkException("关闭输出文件流时产生异常", ioe);
                }
            }
        }
    }

    /**
	 * 取得excel文档的cell内容
	 *
	 * @param sheet
	 * @param row
	 * @param col
	 * @return 取得excel文档的cell内容
	 */
    private static HSSFCell getCell(HSSFSheet sheet, int row, int col) {
        HSSFRow sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        HSSFCell cell = sheetRow.getCell((short) col);
        if (cell == null) {
            cell = sheetRow.createCell((short) col);
        }
        setCellTypeAndEncoding(cell);
        return cell;
    }

    /**
	 * 向cell中填充文档内容
	 *
	 * @param cell
	 * @param text
	 */
    private static void setExcelCellText(HSSFCell cell, String text) {
        setCellTypeAndEncoding(cell);
        cell.setCellValue(text);
    }

    /**
	 * 设置cell的内容为字符文本形式； 设置cell的字符集编码为双字节,支持中文
	 *
	 * @param cell
	 */
    private static void setCellTypeAndEncoding(HSSFCell cell) {
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell.setEncoding(HSSFCell.ENCODING_UTF_16);
    }

    /**
	 * 初始化该Bean的信息类BeanInfo实例
	 *
	 * @param beanClass
	 * @return 初始化该Bean的信息类BeanInfo实例
	 */
    private static BeanInfo getBeanInfo(Class beanClass) {
        try {
            return Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            return null;
        }
    }

    /**
	 * 得到当前bean实例的属性值
	 *
	 * @param target
	 * @param prop
	 * @return 得到当前bean实例的属性值
	 */
    private static Object callGetter(Object target, PropertyDescriptor prop) {
        Object o = null;
        if (prop.getReadMethod() != null) {
            try {
                o = prop.getReadMethod().invoke(target, (Object[]) null);
            } catch (Exception e) {
                o = null;
            }
        }
        return o;
    }

    /**
	 * 设置当前bean实例的属性值
	 *
	 * @param target
	 * @param prop
	 * @param value
	 */
    private static void callSetter(Object target, PropertyDescriptor prop, Object value) {
        if (prop.getWriteMethod() != null) {
            try {
                prop.getWriteMethod().invoke(target, new Object[] { value });
            } catch (Exception e) {
            }
        }
    }

    /**
	 * 创建pdf文档对象模型
	 *
	 * @param pdfFilePath
	 * @return 创建pdf文档对象模型
	 * @throws FrameworkException
	 */
    private static Document creatPdfDocument(String pdfFilePath) throws FrameworkException {
        Document document = null;
        try {
            document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFilePath));
        } catch (DocumentException de) {
            throw new FrameworkException("创建pdf文档时产生异常", de);
        } catch (IOException ioe) {
            throw new FrameworkException("创建pdf文档时产生异常", ioe);
        }
        return document;
    }

    /**
	 * 解析beans生成pdf文档内容
	 *
	 * @param <T>
	 * @param document
	 * @param titlebeanNameMap
	 * @param beans
	 * @throws FrameworkException
	 */
    @SuppressWarnings("unchecked")
    private static <T> void buildPdfDocument(Document document, Map<String, String> titlebeanNameMap, List<T> beans) throws FrameworkException {
        if (document == null) {
            throw new FrameworkException("pdf文档对象模型为null");
        }
        try {
            document.open();
            List<String> titles = new ArrayList();
            List<String> beanAttrNames = new ArrayList();
            int cnt = 0;
            for (Iterator i = titlebeanNameMap.keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String value = titlebeanNameMap.get(key);
                beanAttrNames.add(cnt, key);
                titles.add(cnt, value);
                cnt++;
            }
            PdfPTable table = new PdfPTable(titlebeanNameMap.size());
            for (int i = 0; i < titles.size(); i++) {
                setPdfTitleCellText(table, titles.get(i));
            }
            PropertyDescriptor[] props = getBeanInfo(beans.get(0).getClass()).getPropertyDescriptors();
            for (int i = 0; i < beans.size(); i++) {
                T bean = beans.get(i);
                for (int j = 0; j < beanAttrNames.size(); j++) {
                    String beanAttrName = beanAttrNames.get(j);
                    for (int k = 0; k < props.length; k++) {
                        String propName = props[k].getName();
                        if (propName.equals(beanAttrName)) {
                            Object cellValue = callGetter(bean, props[k]);
                            if (cellValue == null) {
                                cellValue = "";
                            }
                            setPdfDataCellText(table, cellValue.toString());
                        }
                    }
                }
            }
            document.add(table);
        } catch (DocumentException de) {
            throw new FrameworkException("创建pdf文档时产生异常", de);
        } finally {
            document.close();
        }
    }

    /**
	 * 设置pdf标题内容
	 *
	 * @param table
	 * @param value
	 * @throws FrameworkException
	 */
    private static void setPdfTitleCellText(PdfPTable table, String value) throws FrameworkException {
        Paragraph paragraph = new Paragraph(value, PDF_FONT_CHINESE);
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
	 * 设置pdf数据内容
	 *
	 * @param table
	 * @param value
	 */
    private static void setPdfDataCellText(PdfPTable table, String value) {
        Paragraph paragraph = new Paragraph(value, PDF_FONT_CHINESE);
        PdfPCell cell = new PdfPCell(paragraph);
        table.addCell(cell);
    }
}
