package org.jpedal.examples.text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Int;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Sample code showing how jpedal library can be used with
 * pdf files  to extract text from a specified Rectangle as a table 
 * structure <br>Scope:<b>(Ent only)</b>
 * 
 * Debugging tip: Set verbose=true in LogWriter to see what is going on.
 */
public class ExtractTextTableFromZones {

    /**output where we put files*/
    private String user_dir = System.getProperty("user.dir");

    /**correct separator for OS */
    String separator = System.getProperty("file.separator");

    /**the decoder object which decodes the pdf and returns a data object*/
    PdfDecoder decodePdf = null;

    /**flag to show if we display messages*/
    public static boolean showMessages = false;

    /**location output files written to*/
    private String outputDir = "";

    /**user-supplied co-ords*/
    private static int defX1 = -1, defX2, defY1, defY2;

    /**option to set output as Xxml or CSV*/
    private boolean isCSV = true;

    /**sample file which can be setup - substitute your own. 
	 * If a directory is given, all the files in the directory will be processed*/
    private static String testFile = "C:/Tables/test.pdf";

    private static String xml_testFile = "C:/Tables/test.xml";

    public ExtractTextTableFromZones() {
    }

    /**example method to open a file and extract the raw text*/
    public ExtractTextTableFromZones(String file_name) {
        String xmlFlag = System.getProperty("xml");
        if (xmlFlag != null) {
            this.isCSV = false;
        }
        if (file_name == null) file_name = testFile;
        if (user_dir.endsWith(separator) == false) user_dir = user_dir + separator;
        File output_path = new File(outputDir);
        if (output_path.exists() == false) output_path.mkdirs();
        if (file_name.toLowerCase().endsWith(".pdf")) {
            decodeFile(file_name);
        } else {
            String[] files = null;
            File inputFiles = null;
            if (!file_name.endsWith(separator)) file_name = file_name + separator;
            try {
                inputFiles = new File(file_name);
                if (!inputFiles.isDirectory()) {
                    System.err.println(file_name + " is not a directory. Exiting program");
                }
                files = inputFiles.list();
            } catch (Exception ee) {
                LogWriter.writeLog("Exception trying to access file " + ee.getMessage());
            }
            long fileCount = files.length;
            for (int i = 0; i < fileCount; i++) {
                if (showMessages) System.out.println(i + "/ " + fileCount + " " + files[i]);
                if (files[i].toLowerCase().endsWith(".pdf")) {
                    if (showMessages) System.out.println(file_name + files[i]);
                    decodeFile(file_name + files[i]);
                }
            }
        }
    }

    public ExtractTextTableFromZones(String pdf, String xml, String output) {
        outputDir = output;
        String xmlFlag = System.getProperty("xml");
        if (xmlFlag != null) {
            this.isCSV = false;
        }
        if (pdf == null) pdf = testFile;
        if (user_dir.endsWith(separator) == false) user_dir = user_dir + separator;
        File output_path = new File(outputDir);
        if (output_path.exists() == false) output_path.mkdirs();
        if (pdf.toLowerCase().endsWith(".pdf")) {
            decodeFile(pdf, xml, outputDir);
        } else {
            String[] files = null;
            File inputFiles = null;
            if (!pdf.endsWith(separator)) pdf = pdf + separator;
            try {
                inputFiles = new File(pdf);
                if (!inputFiles.isDirectory()) {
                    System.err.println(pdf + " is not a directory. Exiting program");
                }
                files = inputFiles.list();
            } catch (Exception ee) {
                LogWriter.writeLog("Exception trying to access file " + ee.getMessage());
            }
            long fileCount = files.length;
            for (int i = 0; i < fileCount; i++) {
                if (showMessages) System.out.println(i + "/ " + fileCount + " " + files[i]);
                if (files[i].toLowerCase().endsWith(".pdf")) {
                    if (showMessages) System.out.println(pdf + files[i]);
                    decodeFile(pdf + files[i], xml, outputDir);
                }
            }
        }
    }

    /**
 * routine to decode a file
 */
    private void decodeFile(String file_name) {
        String name = "demo";
        int pointer = file_name.lastIndexOf(separator);
        if (pointer != -1) name = file_name.substring(pointer + 1, file_name.length() - 4);
        outputDir = user_dir + "tables" + separator + name + separator;
        File page_path = new File(outputDir + separator);
        if (page_path.exists() == false) page_path.mkdirs();
        try {
            decodePdf = new PdfDecoder(false);
            decodePdf.setExtractionMode(PdfDecoder.TEXT);
            decodePdf.init(true);
            if (showMessages) System.out.println("Opening file :" + file_name);
            decodePdf.openPdfFile(file_name);
        } catch (Exception e) {
            System.err.println("Exception " + e + " in pdf code");
        }
        if (!decodePdf.isExtractionAllowed()) {
            System.out.println("Text extraction not allowed");
        } else if (decodePdf.isEncrypted() && !decodePdf.isPasswordSupplied()) {
            System.out.println("Encrypted settings");
            System.out.println("Please look at SimpleViewer for code sample to handle such files");
            System.out.println("Or get support/consultancy");
        } else {
            int start = 1, end = decodePdf.getPageCount();
            try {
                for (int page = start; page < end + 1; page++) {
                    decodePdf.decodePage(page);
                    PdfGroupingAlgorithms currentGrouping = decodePdf.getGroupingObject();
                    PdfPageData currentPageData = decodePdf.getPdfPageData();
                    int x1, y1, x2, y2;
                    if (defX1 == -1) {
                        x1 = currentPageData.getMediaBoxX(page);
                        x2 = currentPageData.getMediaBoxWidth(page) + x1;
                        y2 = currentPageData.getMediaBoxY(page);
                        y1 = currentPageData.getMediaBoxHeight(page) + y2;
                    } else {
                        x1 = defX1;
                        y1 = defY1;
                        x2 = defX2;
                        y2 = defY2;
                    }
                    if (showMessages) System.out.println("Extracting text from rectangle as table(" + x1 + "," + y1 + " " + x2 + "," + y2 + ")");
                    String ending = ".xml";
                    if (isCSV) {
                        if (showMessages) System.out.println("Table will be in CSV format");
                        ending = ".csv";
                    } else {
                        if (showMessages) System.out.println("Table will be in xml format");
                    }
                    Map tableContent = null;
                    String tableText = null;
                    try {
                        tableContent = currentGrouping.extractTextAsTable(x1, y1, x2, y2, page, isCSV, false, false, false, 0, false);
                        tableText = (String) tableContent.get("content");
                    } catch (PdfException e) {
                        decodePdf.closePdfFile();
                        System.err.println("Exception " + e.getMessage() + " with table extraction");
                    }
                    if (tableText == null) {
                        if (showMessages) System.out.println("No text found");
                    } else {
                        OutputStreamWriter output_stream = new OutputStreamWriter(new FileOutputStream(outputDir + page + ending), "UTF-8");
                        if (showMessages) System.out.println("Writing to " + outputDir + page + ending);
                        if (!isCSV) output_stream.write("<xml><BODY>\n\n");
                        output_stream.write(tableText);
                        if (!isCSV) output_stream.write("\n</body></xml>");
                        output_stream.close();
                    }
                    decodePdf.flushObjectValues(false);
                }
            } catch (Exception e) {
                decodePdf.closePdfFile();
                System.err.println("Exception " + e.getMessage());
                e.printStackTrace();
            }
            decodePdf.flushObjectValues(true);
            if (showMessages) System.out.println("Text read as table");
        }
        decodePdf.closePdfFile();
    }

    /**
 * routine to decode a file
 */
    private void decodeFile(String pdf, String xml, String output) {
        int itemSelectedCount = 0;
        int[] itemSelectedPage = null;
        Vector_Int itemSelectedX1;
        Vector_Int itemSelectedY1;
        Vector_Int itemSelectedWidth;
        Vector_Int itemSelectedHeight;
        int[] values = new int[5];
        File file = new File(xml);
        if (file != null && file.exists()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                Document doc = factory.newDocumentBuilder().parse(file);
                NodeList nodes = doc.getElementsByTagName("TablePositions");
                Element currentElement = (Element) nodes.item(0);
                NodeList catNodes = currentElement.getChildNodes();
                List catValues = new ArrayList();
                int items = catNodes.getLength();
                for (int i = 0; i < items; i++) {
                    Node next = catNodes.item(i);
                    if (next instanceof Element) catValues.add(next);
                }
                int size = catValues.size();
                int i = 0;
                Element next = (Element) catValues.get(i);
                String key = next.getNodeName();
                String value = next.getAttribute("value");
                if (key.endsWith("Count")) {
                    itemSelectedCount = Integer.parseInt(value);
                    itemSelectedPage = new int[itemSelectedCount];
                }
                itemSelectedPage = new int[itemSelectedCount];
                itemSelectedX1 = new Vector_Int(itemSelectedCount);
                itemSelectedY1 = new Vector_Int(itemSelectedCount);
                itemSelectedWidth = new Vector_Int(itemSelectedCount);
                itemSelectedHeight = new Vector_Int(itemSelectedCount);
                int pages = 0;
                for (i = 0; i < size; i++) {
                    next = (Element) catValues.get(i);
                    key = next.getNodeName();
                    value = next.getAttribute("value");
                    if (key.endsWith("page")) {
                        itemSelectedPage[pages] = Integer.parseInt(value);
                        pages++;
                    }
                    if (key.endsWith("x1")) itemSelectedX1.addElement(Integer.parseInt(value));
                    if (key.endsWith("y1")) itemSelectedY1.addElement(Integer.parseInt(value));
                    if (key.endsWith("x2")) itemSelectedWidth.addElement(Integer.parseInt(value));
                    if (key.endsWith("y2")) itemSelectedHeight.addElement(Integer.parseInt(value));
                }
                int[][] openValues = new int[itemSelectedCount][5];
                i = 0;
                int[] x1Array = itemSelectedX1.get();
                int[] x2Array = itemSelectedWidth.get();
                int[] y1Array = itemSelectedY1.get();
                int[] y2Array = itemSelectedHeight.get();
                int pageValue = 0;
                if (showMessages) System.out.println("\nOpening file :" + pdf);
                while (i < itemSelectedCount) {
                    openValues[i][0] = itemSelectedPage[i];
                    openValues[i][1] = x1Array[i];
                    openValues[i][2] = x2Array[i];
                    openValues[i][3] = y1Array[i];
                    openValues[i][4] = y2Array[i];
                    values = openValues[i];
                    i++;
                    String name = "demo";
                    int pointer = pdf.lastIndexOf(separator);
                    if (pointer != -1) name = pdf.substring(pointer + 1, pdf.length() - 4);
                    if (output == "" || output == null) outputDir = user_dir + "tables" + separator + name + separator; else outputDir = output + separator + "tables" + separator + name + separator;
                    File page_path = new File(outputDir + separator);
                    if (page_path.exists() == false) page_path.mkdirs();
                    try {
                        decodePdf = new PdfDecoder(false);
                        decodePdf.setExtractionMode(PdfDecoder.TEXT);
                        decodePdf.init(true);
                        decodePdf.openPdfFile(pdf);
                    } catch (Exception e) {
                        System.err.println("Exception " + e + " in pdf code");
                    }
                    if (!decodePdf.isExtractionAllowed()) {
                        System.out.println("Text extraction not allowed");
                    } else if (decodePdf.isEncrypted() && !decodePdf.isPasswordSupplied()) {
                        System.out.println("Encrypted settings");
                        System.out.println("Please look at SimpleViewer for code sample to handle such files");
                        System.out.println("Or get support/consultancy");
                    } else {
                        try {
                            for (int k = 0; k < 5; k++) {
                                int page = itemSelectedPage[pageValue];
                                pageValue++;
                                decodePdf.decodePage(page);
                                PdfGroupingAlgorithms currentGrouping = decodePdf.getGroupingObject();
                                int x1, y1, x2, y2;
                                if (defX1 == -1) {
                                    k++;
                                    x1 = values[k];
                                    k++;
                                    x2 = values[k];
                                    k++;
                                    y1 = values[k];
                                    k++;
                                    y2 = values[k];
                                } else {
                                    x1 = defX1;
                                    y1 = defY1;
                                    x2 = defX2;
                                    y2 = defY2;
                                }
                                if (showMessages) System.out.println("\nExtracting text from Page " + page + " in rectangle(" + x1 + "," + y1 + " " + x2 + "," + y2 + ")");
                                String ending = ".xml";
                                if (isCSV) {
                                    if (showMessages) System.out.println("Table will be in CSV format");
                                    ending = ".csv";
                                } else {
                                    if (showMessages) System.out.println("Table will be in xml format");
                                }
                                Map tableContent = null;
                                String tableText = null;
                                try {
                                    tableContent = currentGrouping.extractTextAsTable(x1, y1, x2, y2, page, isCSV, false, false, false, 0, false);
                                    tableText = (String) tableContent.get("content");
                                } catch (PdfException e) {
                                    decodePdf.closePdfFile();
                                    System.err.println("Exception " + e.getMessage() + " with table extraction");
                                }
                                if (tableText == null) {
                                    if (showMessages) System.out.println("No text found");
                                } else {
                                    OutputStreamWriter output_stream = new OutputStreamWriter(new FileOutputStream(outputDir + pageValue + ending), "UTF-8");
                                    if (showMessages) System.out.println("Writing to " + outputDir + pageValue + ending);
                                    if (!isCSV) output_stream.write("<xml><BODY>\n\n");
                                    output_stream.write(tableText);
                                    if (!isCSV) output_stream.write("\n</body></xml>");
                                    output_stream.close();
                                }
                                decodePdf.flushObjectValues(false);
                            }
                        } catch (Exception e) {
                            decodePdf.closePdfFile();
                            System.err.println("Exception " + e.getMessage());
                            e.printStackTrace();
                        }
                        decodePdf.flushObjectValues(true);
                        if (showMessages) System.out.println("Text read as table");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            decodePdf.closePdfFile();
        } else {
            System.out.println("File Not Found.");
        }
    }

    /**
	 * main routine which checks for any files passed and runs the demo
	 */
    public static void main(String[] args) {
        System.out.println("Simple demo to extract text objects as CSV or xml tables");
        String file_name = testFile;
        String xml_file_name = xml_testFile;
        String output = "";
        if (args.length == 1) {
            file_name = args[0];
            System.out.println("File :" + file_name);
        } else if (args.length == 3) {
            file_name = args[0];
            System.out.println("PDF File :" + file_name);
            xml_file_name = args[1];
            System.out.println("XML File :" + xml_file_name);
            output = args[2];
            System.out.println("Output Folder :" + output);
        } else if (args.length == 5) {
            file_name = args[0];
            System.out.println("File :" + file_name);
            System.out.println("User coordinates supplied");
            defX1 = Integer.parseInt(args[1]);
            defY1 = Integer.parseInt(args[2]);
            defX2 = Integer.parseInt(args[3]);
            defY2 = Integer.parseInt(args[4]);
        } else {
            System.out.println("Please call with either ");
            System.out.println("FileName");
            System.out.println("or");
            System.out.println("FileName x1 y1 x2 y2");
        }
        File pdf_file = new File(file_name);
        if (pdf_file.exists() == false) {
            System.out.println("File " + file_name + " not found");
        }
        if (args.length == 1) {
            ExtractTextTableFromZones text1 = new ExtractTextTableFromZones(file_name);
        }
        if (args.length == 3) {
            File xml_file = new File(xml_file_name);
            if (xml_file.exists() == false) {
                System.out.println("File " + file_name + " not found");
            }
            ExtractTextTableFromZones text1 = new ExtractTextTableFromZones(file_name, xml_file_name, output);
        }
    }
}
