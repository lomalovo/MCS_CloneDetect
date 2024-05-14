package listeners;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Thread;
import java.util.*;
import utils.Report.*;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestRunner;
import org.testng.internal.MethodHelper;
import org.testng.internal.Utils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import utils.Constantes;
import utils.controlTest.*;
import utils.compareSuites.*;
import utils.mail.ControlMail;
import utils.mail.InfoMail;
import org.outerj.daisy.diff.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.sql.Date;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.outerj.daisy.diff.helper.NekoHtmlParser;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This reporter is responsible for creating testng-failed.xml
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class Reporter extends TestListenerAdapter implements IReporter {

    public static final String TESTNG_FAILED_XML = "testng-failed.xml";

    private XmlSuite m_xmlSuite;

    public Reporter() {
    }

    public Reporter(XmlSuite xmlSuite) {
        m_xmlSuite = xmlSuite;
    }

    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        String fechaInicio = this.getTestContexts().get(0).getStartDate().toLocaleString().replace(':', '.');
        indexSuite oldSuite = (indexSuite) this.getTestContexts().get(0).getAttribute("oldSuite");
        indexSuite suiteNew = new indexSuite(this.getTestContexts().get(0).getStartDate(), this.getTestContexts().get(0).getSuite().getName());
        ;
        if (oldSuite != null) {
            compareSuites cp = new compareSuites(oldSuite, suiteNew, outputDirectory + "-library\\", false);
        }
        File oldDirectory;
        File newDirectory;
        oldDirectory = new File(outputDirectory);
        newDirectory = new File(outputDirectory + "-library" + "\\" + fechaInicio);
        oldDirectory.renameTo(newDirectory);
        generateReportHTML(oldSuite, suiteNew, outputDirectory + "-library" + "\\" + fechaInicio);
    }

    public void generateReportHTML(indexSuite suiteOld, indexSuite suiteNew, String outputDirectory) {
        Vector suiteNewBD = apiBD.listSuite(suiteNew);
        Vector suiteOldBD = null;
        if (suiteOld != null) suiteOldBD = apiBD.listSuite(suiteOld);
        Vector listMethods = null;
        Vector listSteps = null;
        Vector listValidations = null;
        String valuesTree = "";
        String test = "";
        boolean anyError = false;
        int iLastMethod = 0;
        int iLastStep = 0;
        int iLastValidation = 0;
        if (suiteOld == null) listMethods = apiBD.listMethods(suiteNew); else listMethods = apiBD.listMethodsComp(suiteOld, suiteNew);
        if (((HashMap) listMethods.get(0)).get("TEST" + Constantes.CONST_PREFIX_NEW) != null) test = (String) ((HashMap) listMethods.get(0)).get("TEST" + Constantes.CONST_PREFIX_NEW); else test = (String) ((HashMap) listMethods.get(0)).get("TEST" + Constantes.CONST_PREFIX_OLD);
        String reportHTML = "";
        reportHTML = reportHTML + "<html>\n";
        reportHTML = reportHTML + "<head>\n";
        reportHTML = reportHTML + "	<title>JQTreeTable</title>\n";
        reportHTML = reportHTML + "	<script type=\"text/javascript\" src=\"../../../static/js/jquery.min.js\"></script>\n";
        reportHTML = reportHTML + "	<script type=\"text/javascript\" src=\"../../../static/js/ReportLibrary.js\"></script>\n";
        reportHTML = reportHTML + "<script type=\"text/javascript\">\n";
        reportHTML = reportHTML + "$(function(){//Initialise the treetable\n";
        reportHTML = reportHTML + "  var map2=[@VALUES_TREE];\n";
        reportHTML = reportHTML + "  var options1 = {openImg: \"../../../static/images/tv-collapsable.gif\", shutImg: \"../../../static/images/tv-expandable.gif\", leafImg: \"../../../static/images/tv-item.gif\", lastOpenImg: \"../../../static/images/tv-collapsable-last.gif\", lastShutImg: \"../../../static/images/tv-expandable-last.gif\", lastLeafImg: \"../../../static/images/tv-item-last.gif\", vertLineImg: \"../../../static/images/vertline.gif\", blankImg: \"../../../static/images/blank.gif\", collapse: false, column: 1, striped: true, highlight: true, state:true};\n";
        reportHTML = reportHTML + "  $(\"#treet2\").jqTreeTable(map2, {openImg: \"../../../static/images/fopen.gif\", shutImg: \"../../../static/images/fshut.gif\", leafImg: \"../../../static/images/new.gif\", lastOpenImg: \"../../../static/images/fopen.gif\", lastShutImg: \"../../../static/images/fshut.gif\", lastLeafImg: \"../../../static/images/new.gif\", vertLineImg: \"../../../static/images/blank.gif\", blankImg: \"../../../static/images/blank.gif\", collapse: false, column: 1, striped: true, highlight: true, state:false});\n";
        reportHTML = reportHTML + "});\n";
        reportHTML = reportHTML + "</script>\n";
        reportHTML = reportHTML + "	<link href=\"../../../static/css/Report.css\" rel=\"stylesheet\" type=\"text/css\">\n";
        reportHTML = reportHTML + "</head>\n";
        reportHTML = reportHTML + "<body class=\"body\" onload=\"setBrowserType();setSizeTable();show_hide_column('tableMain','compare',false);\">\n";
        if (suiteOldBD != null) {
            reportHTML = reportHTML + "	<a id=\"compare\" href=\"javascript:show_hide_column('tableMain','compare',true);\" >Compare Report</a>";
            reportHTML = reportHTML + "<a id=\"comparedSuite\">(Suite Date: " + ((HashMap) suiteOldBD.get(0)).get("SUITE_DATE") + ")</a>\n";
        }
        reportHTML = reportHTML + "   <div id=\"divShow\">\n";
        reportHTML = reportHTML + "      <a href=\"javascript:show_hide_all('tableMain',false);\" id=\"linkShow\">Hide All</a>\n";
        reportHTML = reportHTML + "   </div>\n";
        reportHTML = reportHTML + "   <div class=\"divTestNG\">";
        reportHTML = reportHTML + "     <a href=\"..\\index.html\" target=\"_blank\" class=\"linkTestNG\">TestNG Report</a>";
        reportHTML = reportHTML + "   </div>";
        reportHTML = reportHTML + "   <div class=\"divTestNG\">";
        reportHTML = reportHTML + "     <a href=\"..\\emailable-report.html\" target=\"_blank\" class=\"linkTestNG\">Emailable Report</a>";
        reportHTML = reportHTML + "   </div>";
        reportHTML = reportHTML + "   <br>\n";
        reportHTML = reportHTML + "   <br>\n";
        reportHTML = reportHTML + "	<table id=\"tableMain\" class=\"tablemain\"><thead>\n";
        reportHTML = reportHTML + "	<tr>\n";
        reportHTML = reportHTML + "		<th colspan=\"1\" class=\"head\"></th>\n";
        reportHTML = reportHTML + "		<th colspan=\"21\" class=\"head\"><div id=\"titleReport\">" + ((HashMap) suiteNewBD.get(0)).get("SUITE") + " - " + test + " (Suite Date: " + ((HashMap) suiteNewBD.get(0)).get("SUITE_DATE") + ")" + "</div>";
        if (suiteOldBD != null) reportHTML = reportHTML + "<br class=\"clear\"><div id=\"subTitleReport\">" + ((HashMap) suiteOldBD.get(0)).get("SUITE") + " - " + test + " (Suite Date: " + ((HashMap) suiteOldBD.get(0)).get("SUITE_DATE") + ")</div></th>\n"; else reportHTML = reportHTML + "</th>\n";
        reportHTML = reportHTML + "	</tr>\n";
        reportHTML = reportHTML + "	<tr><th class=\"rowHead\" rowspan=\"2\">Type</th><th rowspan=\"2\">Element</th><th rowspan=\"2\">#Stp / #Val</th><th rowspan=\"2\">Result</th><th class=\"compare\" rowspan=\"2\">R-Old</th><th rowspan=\"2\">Init</th><th rowspan=\"2\">End</th><th rowspan=\"2\">Time</th><th class=\"compare\" rowspan=\"2\">T-Old</th><th rowspan=\"2\">HardCopy</th><th class=\"compare\" rowspan=\"2\">Ha-Old</th><th rowspan=\"2\">HTML</th><th class=\"compare\" rowspan=\"2\">H-Old</th><th class=\"compare\" rowspan=\"2\">HTML Compared</th><th class=\"compare\" colspan=\"4\">N� Diffs</th><th rowspan=\"2\">Static Data</th><th class=\"size20\" rowspan=\"2\">Action / Validation</th><th class=\"size15\" rowspan=\"2\">Result expected</th><th rowspan=\"2\">Class</th></tr>\n";
        reportHTML = reportHTML + "   <tr>\n";
        reportHTML = reportHTML + "       <th class=\"compare\">Exist</th>\n";
        reportHTML = reportHTML + "       <th class=\"compare\">Add</th>\n";
        reportHTML = reportHTML + "       <th class=\"compare\">Dlts</th>\n";
        reportHTML = reportHTML + "       <th class=\"compare\">Chan</th>\n";
        reportHTML = reportHTML + "   </tr>\n";
        reportHTML = reportHTML + "   </thead>\n";
        reportHTML = reportHTML + "	<tbody id=\"treet2\">\n";
        HashMap methodH = null;
        HashMap stepH = null;
        HashMap validationH = null;
        int countRows = 0;
        for (int iMethod = 0; iMethod < listMethods.size(); iMethod++) {
            methodH = ((HashMap) listMethods.get(iMethod));
            if (iMethod == 0) valuesTree = valuesTree + "0"; else valuesTree = valuesTree + ",0";
            countRows += 1;
            iLastMethod = countRows;
            int typeMeth = (Integer) methodH.get("TYPE");
            String typeMethLit = "";
            String typeMethCla = "";
            switch(typeMeth) {
                case Constantes.CONST_ONLY_OLD:
                    typeMethLit = Constantes.CONST_ONLY_OLD_LIT;
                    typeMethCla = "row" + Constantes.CONST_ONLY_OLD_LIT;
                    break;
                case Constantes.CONST_ONLY_NEW:
                    typeMethLit = Constantes.CONST_ONLY_NEW_LIT;
                    typeMethCla = "row" + Constantes.CONST_ONLY_NEW_LIT;
                    break;
                case Constantes.CONST_NEW_AND_OLD:
                    typeMethLit = Constantes.CONST_NEW_AND_OLD_LIT;
                    typeMethCla = "row" + Constantes.CONST_NEW_AND_OLD_LIT;
                    break;
            }
            String prefixMeth = "";
            String metodoMeth = "";
            String numberSteps = "";
            String resultNewMeth = "";
            String resultOldMeth = "";
            Date dateInitMeth = null;
            Date dateFinMeth = null;
            String timeOldMeth = "";
            String timeNewMeth = "";
            String classMeth = "";
            if (typeMeth == Constantes.CONST_ONLY_OLD || typeMeth == Constantes.CONST_NEW_AND_OLD) {
                metodoMeth = (String) methodH.get("METHOD" + Constantes.CONST_PREFIX_OLD);
                numberSteps = (String) methodH.get("NUMBER_STEPS" + Constantes.CONST_PREFIX_OLD);
                resultOldMeth = getResulMethodLit((String) methodH.get("RESULT_SCRIPT" + Constantes.CONST_PREFIX_OLD), (String) methodH.get("RESULT_TNG" + Constantes.CONST_PREFIX_OLD));
                dateInitMeth = new Date(Long.parseLong((String) methodH.get("INICIO" + Constantes.CONST_PREFIX_OLD)));
                dateFinMeth = new Date(Long.parseLong((String) methodH.get("FIN" + Constantes.CONST_PREFIX_OLD)));
                timeOldMeth = (String) methodH.get("TIME_MS" + Constantes.CONST_PREFIX_OLD);
                classMeth = (String) methodH.get("CLASS_SIGNATURE" + Constantes.CONST_PREFIX_OLD);
            }
            if (typeMeth == Constantes.CONST_ONLY_NEW || typeMeth == Constantes.CONST_NEW_AND_OLD) {
                metodoMeth = (String) methodH.get("METHOD" + Constantes.CONST_PREFIX_NEW);
                numberSteps = (String) methodH.get("NUMBER_STEPS" + Constantes.CONST_PREFIX_NEW);
                resultNewMeth = getResulMethodLit((String) methodH.get("RESULT_SCRIPT" + Constantes.CONST_PREFIX_NEW), (String) methodH.get("RESULT_TNG" + Constantes.CONST_PREFIX_NEW));
                if (resultNewMeth.compareTo("NOK") == 0) anyError = true;
                dateInitMeth = new Date(Long.parseLong((String) methodH.get("INICIO" + Constantes.CONST_PREFIX_NEW)));
                dateFinMeth = new Date(Long.parseLong((String) methodH.get("FIN" + Constantes.CONST_PREFIX_NEW)));
                timeNewMeth = (String) methodH.get("TIME_MS" + Constantes.CONST_PREFIX_NEW);
                classMeth = (String) methodH.get("CLASS_SIGNATURE" + Constantes.CONST_PREFIX_NEW);
            }
            reportHTML = reportHTML + "	<tr class=\"method" + typeMeth + "\">" + "<td class=\"" + typeMethCla + "\">" + typeMethLit + "</td> " + "<td class=\"nowrap\">" + metodoMeth + "</td>" + "<td>" + numberSteps + "</td>" + "<td><div class=\"result" + resultNewMeth + "\">" + resultNewMeth + "</div></td>" + "<td class=\"compare\"><div class=\"result" + resultOldMeth + "\">" + resultOldMeth + "</div></td>" + "<td>" + dateInitMeth.toLocaleString() + "</td>" + "<td>" + dateFinMeth.toLocaleString() + "</td>" + "<td>" + timeNewMeth + "</td>" + "<td class=\"compare\">" + timeOldMeth + "</td>" + "<td></td>" + "<td class=\"compare\"></td>" + "<td></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"><div class=\"diffs@DIFF_METH\">@DIFF_METH</td>" + "<td class=\"compare\">@ADDS_METH</td>" + "<td class=\"compare\">@DLTS_METH</td>" + "<td class=\"compare\">@CHAN_METH</td>" + "<td></td>" + "<td></td>" + "<td></td>" + "<td>" + classMeth + "</td></tr>\n";
            if (suiteOld == null || typeMeth == Constantes.CONST_ONLY_NEW || typeMeth == Constantes.CONST_ONLY_OLD) {
                indexSuite suiteTmp = suiteNew;
                prefixMeth = Constantes.CONST_PREFIX_NEW;
                if (typeMeth == Constantes.CONST_ONLY_OLD) {
                    suiteTmp = suiteOld;
                    prefixMeth = Constantes.CONST_PREFIX_OLD;
                }
                listSteps = apiBD.listSteps(suiteTmp, metodoMeth, prefixMeth);
            } else {
                listSteps = apiBD.listStepsComp(suiteOld, suiteNew, metodoMeth);
            }
            int countAddsStp = 0;
            int countDltsStp = 0;
            int countChanStp = 0;
            String diffStp = "No";
            for (int iStep = 0; iStep < listSteps.size(); iStep++) {
                stepH = ((HashMap) listSteps.get(iStep));
                countRows += 1;
                iLastStep = countRows;
                valuesTree = valuesTree + "," + iLastMethod;
                int typeStep = (Integer) stepH.get("TYPE");
                String typeStepLit = "";
                String typeStepCla = "";
                switch(typeStep) {
                    case Constantes.CONST_ONLY_OLD:
                        typeStepLit = Constantes.CONST_ONLY_OLD_LIT;
                        typeStepCla = "row" + Constantes.CONST_ONLY_OLD_LIT;
                        break;
                    case Constantes.CONST_ONLY_NEW:
                        typeStepLit = Constantes.CONST_ONLY_NEW_LIT;
                        typeStepCla = "row" + Constantes.CONST_ONLY_NEW_LIT;
                        break;
                    case Constantes.CONST_NEW_AND_OLD:
                        typeStepLit = Constantes.CONST_NEW_AND_OLD_LIT;
                        typeStepCla = "row" + Constantes.CONST_NEW_AND_OLD_LIT;
                        break;
                }
                Date dateInitSt = null;
                Date dateFinSt = null;
                String metodo = "";
                String stepNumber = "";
                String stepValidations = "";
                String resuOldStep = "";
                String resuNewStep = "";
                String timeOldStep = "";
                String timeNewStep = "";
                String PNGOldStep = "";
                String PNGNewStep = "";
                String litPNGOldStep = "";
                String litPNGNewStep = "";
                String HTMLOldStep = "";
                String HTMLNewStep = "";
                String litHTMLOldStep = "";
                String litHTMLNewStep = "";
                String HTMLCompStep = "";
                String litHTMLComp = "";
                String existDiffs = "";
                String adds = "";
                String deleteds = "";
                String changes = "";
                String description = "";
                String resExpected = "";
                String typePagOld = "";
                String typePagNew = "";
                String suiteNewLit = "";
                String suiteOldLit = "";
                String testNewLit = "";
                String testOldLit = "";
                if (typeStep == Constantes.CONST_NEW_AND_OLD) {
                    timeOldStep = (String) stepH.get("TIME_MS" + Constantes.CONST_PREFIX_OLD);
                    timeNewStep = (String) stepH.get("TIME_MS" + Constantes.CONST_PREFIX_NEW);
                    typePagOld = (String) stepH.get("TYPE_PAGE" + Constantes.CONST_PREFIX_OLD);
                    typePagNew = (String) stepH.get("TYPE_PAGE" + Constantes.CONST_PREFIX_NEW);
                    if (Integer.parseInt(typePagOld) == Constantes.CONST_HTML && Integer.parseInt(typePagNew) == Constantes.CONST_HTML && ((String) stepH.get("HTML" + Constantes.CONST_PREFIX_OLD)).compareTo("0") != 0 && ((String) stepH.get("HTML" + Constantes.CONST_PREFIX_NEW)).compareTo("0") != 0) {
                        litHTMLComp = "HTML Comp";
                        String nameFileCompared = suiteOld.getSuiteDate().toLocaleString().replace(':', '.') + "st" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD)) + ".html";
                        HTMLCompStep = ".\\" + (String) stepH.get("TEST" + Constantes.CONST_PREFIX_OLD) + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_OLD) + "\\" + nameFileCompared;
                        if (Integer.parseInt((String) stepH.get("DIFFERENCES")) == 1) existDiffs = "S�"; else existDiffs = "No";
                        adds = (String) stepH.get("NUM_ADDS");
                        deleteds = (String) stepH.get("NUM_DELETEDS");
                        changes = (String) stepH.get("NUM_CHANGES");
                        if (existDiffs.compareTo("S�") == 0) diffStp = "S�";
                        countAddsStp += Integer.parseInt(adds);
                        countDltsStp += Integer.parseInt(deleteds);
                        countChanStp += Integer.parseInt(changes);
                    }
                }
                if (typeStep == Constantes.CONST_ONLY_OLD || typeStep == Constantes.CONST_NEW_AND_OLD) {
                    stepValidations = (String) stepH.get("NUM_VALIDATIONS" + Constantes.CONST_PREFIX_OLD);
                    metodo = (String) ((HashMap) listMethods.get(iMethod)).get("METHOD" + Constantes.CONST_PREFIX_OLD);
                    stepNumber = (String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD);
                    dateInitSt = new Date(Long.parseLong((String) stepH.get("INICIO" + Constantes.CONST_PREFIX_OLD)));
                    dateFinSt = new Date(Long.parseLong((String) stepH.get("FIN" + Constantes.CONST_PREFIX_OLD)));
                    resuOldStep = getResulStepLit((String) stepH.get("RESULTADO" + Constantes.CONST_PREFIX_OLD), (String) stepH.get("EXCEPCION" + Constantes.CONST_PREFIX_OLD));
                    timeOldStep = (String) stepH.get("TIME_MS" + Constantes.CONST_PREFIX_OLD);
                    typePagOld = (String) stepH.get("TYPE_PAGE" + Constantes.CONST_PREFIX_OLD);
                    suiteOldLit = (String) stepH.get("SUITE" + Constantes.CONST_PREFIX_OLD);
                    testOldLit = (String) stepH.get("TEST" + Constantes.CONST_PREFIX_OLD);
                    if (Integer.parseInt(typePagOld) == Constantes.CONST_HTML) {
                        PNGOldStep = "..\\..\\" + suiteOld.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteOldLit + "\\" + testOldLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_OLD) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD)) + ".png";
                        litPNGOldStep = "PNG Page";
                    }
                    if (((String) stepH.get("HTML" + Constantes.CONST_PREFIX_OLD)).compareTo("0") != 0) {
                        switch(Integer.parseInt(typePagOld)) {
                            case Constantes.CONST_HTML:
                                HTMLOldStep = "..\\..\\" + suiteOld.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteOldLit + "\\" + testOldLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_OLD) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD)) + ".html";
                                litHTMLOldStep = "HTML Page";
                                break;
                            case Constantes.CONST_EXCEL:
                                litHTMLOldStep = "EXCEL Page";
                                HTMLOldStep = "..\\..\\" + suiteOld.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteOldLit + "\\" + testOldLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_OLD) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD)) + ".xls";
                                break;
                            case Constantes.CONST_PDF:
                                litHTMLOldStep = "PDF Page";
                                HTMLOldStep = "..\\..\\" + suiteOld.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteOldLit + "\\" + testOldLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_OLD) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_OLD)) + ".pdf";
                                break;
                        }
                    }
                    description = (String) stepH.get("DESCRIPTION" + Constantes.CONST_PREFIX_OLD);
                    resExpected = (String) stepH.get("RES_EXPECTED" + Constantes.CONST_PREFIX_OLD);
                }
                if (typeStep == Constantes.CONST_ONLY_NEW || typeStep == Constantes.CONST_NEW_AND_OLD) {
                    stepValidations = (String) stepH.get("NUM_VALIDATIONS" + Constantes.CONST_PREFIX_NEW);
                    metodo = (String) ((HashMap) listMethods.get(iMethod)).get("METHOD" + Constantes.CONST_PREFIX_NEW);
                    stepNumber = (String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_NEW);
                    dateInitSt = new Date(Long.parseLong((String) stepH.get("INICIO" + Constantes.CONST_PREFIX_NEW)));
                    dateFinSt = new Date(Long.parseLong((String) stepH.get("FIN" + Constantes.CONST_PREFIX_NEW)));
                    resuNewStep = getResulStepLit((String) stepH.get("RESULTADO" + Constantes.CONST_PREFIX_NEW), (String) stepH.get("EXCEPCION" + Constantes.CONST_PREFIX_NEW));
                    timeNewStep = (String) stepH.get("TIME_MS" + Constantes.CONST_PREFIX_NEW);
                    typePagNew = (String) stepH.get("TYPE_PAGE" + Constantes.CONST_PREFIX_NEW);
                    suiteNewLit = (String) stepH.get("SUITE" + Constantes.CONST_PREFIX_NEW);
                    testNewLit = (String) stepH.get("TEST" + Constantes.CONST_PREFIX_NEW);
                    if (Integer.parseInt(typePagNew) == Constantes.CONST_HTML) {
                        PNGNewStep = "..\\..\\" + suiteNew.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteNewLit + "\\" + testNewLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_NEW) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_NEW)) + ".png";
                        litPNGNewStep = "PNG Page";
                    }
                    if (((String) stepH.get("HTML" + Constantes.CONST_PREFIX_NEW)).compareTo("0") != 0) {
                        switch(Integer.parseInt(typePagNew)) {
                            case Constantes.CONST_HTML:
                                HTMLNewStep = "..\\..\\" + suiteNew.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteNewLit + "\\" + testNewLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_NEW) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_NEW)) + ".html";
                                litHTMLNewStep = "HTML Page";
                                break;
                            case Constantes.CONST_EXCEL:
                                litHTMLNewStep = "EXCEL Page";
                                HTMLNewStep = "..\\..\\" + suiteNew.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteNewLit + "\\" + testNewLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_NEW) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_NEW)) + ".xls";
                                break;
                            case Constantes.CONST_PDF:
                                litHTMLNewStep = "PDF Page";
                                HTMLNewStep = "..\\..\\" + suiteNew.getSuiteDate().toLocaleString().replace(':', '.') + "\\" + suiteNewLit + "\\" + testNewLit + "\\" + (String) stepH.get("METHOD" + Constantes.CONST_PREFIX_NEW) + "\\" + "Step-" + Integer.parseInt((String) stepH.get("STEP_NUMBER" + Constantes.CONST_PREFIX_NEW)) + ".pdf";
                                break;
                        }
                    }
                    description = (String) stepH.get("DESCRIPTION" + Constantes.CONST_PREFIX_NEW);
                    resExpected = (String) stepH.get("RES_EXPECTED" + Constantes.CONST_PREFIX_NEW);
                }
                reportHTML = reportHTML + "	<tr class=\"step" + typeStep + "\">" + "<td class=\"" + typeStepCla + "\">" + typeStepLit + "</td> " + "<td class=\"nowrap\">Step " + stepNumber + "</td>" + "<td>" + stepValidations + "</td>" + "<td><div class=\"result" + resuNewStep + "\">" + resuNewStep + "</div></td>" + "<td class=\"compare\"><div class=\"result" + resuOldStep + "\">" + resuOldStep + "</div></td>" + "<td>" + dateInitSt.toLocaleString() + "</td>" + "<td>" + dateFinSt.toLocaleString() + "</td>" + "<td>" + timeNewStep + "</td>" + "<td class=\"compare\">" + timeOldStep + "</td>" + "<td><a href=\"" + PNGNewStep + "\">" + litPNGNewStep + "</a></td>" + "<td class=\"compare\"><a href=\"" + PNGOldStep + "\">" + litPNGOldStep + "</a></td>" + "<td><a href=\"" + HTMLNewStep + "\">" + litHTMLNewStep + "</a></td>" + "<td class=\"compare\"><a href=\"" + HTMLOldStep + "\">" + litHTMLOldStep + "</a></td>" + "<td class=\"compare\"><a href=\"" + HTMLCompStep + "\">" + litHTMLComp + "</a></td>" + "<td class=\"compare\"><div class=\"diffs" + existDiffs + "\">" + existDiffs + "</td>" + "<td class=\"compare\">" + adds + "</td>" + "<td class=\"compare\">" + deleteds + "</td>" + "<td class=\"compare\">" + changes + "</td>" + "<td><a href=\"#\">Static Data</a></td>" + "<td>" + description + "</td>" + "<td>" + resExpected + "</td>" + "<td></td></tr>\n";
                listValidations = apiBD.listValidations(suiteNew, suiteOld, metodo, stepNumber, typeStep);
                for (int iValid = 0; iValid < listValidations.size(); iValid++) {
                    validationH = ((HashMap) listValidations.get(iValid));
                    countRows += 1;
                    iLastValidation = countRows;
                    valuesTree = valuesTree + "," + iLastStep;
                    int typeValid = (Integer) validationH.get("TYPE");
                    String typeValLit = "";
                    String typeValCla = "";
                    switch(typeValid) {
                        case Constantes.CONST_ONLY_OLD:
                            typeValLit = Constantes.CONST_ONLY_OLD_LIT;
                            typeValCla = "row" + Constantes.CONST_ONLY_OLD_LIT;
                            break;
                        case Constantes.CONST_ONLY_NEW:
                            typeValLit = Constantes.CONST_ONLY_NEW_LIT;
                            typeValCla = "row" + Constantes.CONST_ONLY_NEW_LIT;
                            break;
                        case Constantes.CONST_NEW_AND_OLD:
                            typeValLit = Constantes.CONST_NEW_AND_OLD_LIT;
                            typeValCla = "row" + Constantes.CONST_NEW_AND_OLD_LIT;
                            break;
                    }
                    String validationNumber = "";
                    String resultOld = "";
                    String resultNew = "";
                    String descriptValid = "";
                    if (typeStep == Constantes.CONST_ONLY_OLD) {
                        validationNumber = (String) validationH.get("VALIDATION_NUMBER" + Constantes.CONST_PREFIX_OLD);
                        resultOld = getResulValidLit((String) validationH.get("RESULTADO" + Constantes.CONST_PREFIX_OLD));
                        descriptValid = (String) validationH.get("DESCRIPTION" + Constantes.CONST_PREFIX_OLD);
                    }
                    if (typeStep == Constantes.CONST_ONLY_NEW || typeStep == Constantes.CONST_NEW_AND_OLD) {
                        validationNumber = (String) validationH.get("VALIDATION_NUMBER" + Constantes.CONST_PREFIX_NEW);
                        resultNew = getResulValidLit((String) validationH.get("RESULTADO" + Constantes.CONST_PREFIX_NEW));
                        descriptValid = (String) validationH.get("DESCRIPTION" + Constantes.CONST_PREFIX_NEW);
                    }
                    if (typeStep == Constantes.CONST_NEW_AND_OLD) {
                        resultOld = getResulValidLit((String) validationH.get("RESULTADO" + Constantes.CONST_PREFIX_OLD));
                    }
                    reportHTML = reportHTML + "	<tr class=\"validation" + typeStep + "\">" + "<td class=\"" + typeValCla + "\">" + typeValLit + "</td> " + "<td class=\"nowrap\">Validation " + validationNumber + "</td>" + "<td></td>" + "<td><div class=\"result" + resultNew + "\">" + resultNew + "</div></td>" + "<td class=\"compare\"><div class=\"result" + resultOld + "\">" + resultOld + "</div></td>" + "<td></td>" + "<td></td>" + "<td></td>" + "<td class=\"compare\"></td>" + "<td></td>" + "<td class=\"compare\"></td>" + "<td></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td class=\"compare\"></td>" + "<td></td>" + "<td>" + descriptValid + "</td>" + "<td></td>" + "<td></td></tr>\n";
                }
            }
            if (typeMeth == Constantes.CONST_NEW_AND_OLD) {
                reportHTML = reportHTML.replaceAll("@DIFF_METH", diffStp);
                reportHTML = reportHTML.replaceAll("@ADDS_METH", String.valueOf(countAddsStp));
                reportHTML = reportHTML.replaceAll("@DLTS_METH", String.valueOf(countDltsStp));
                reportHTML = reportHTML.replaceAll("@CHAN_METH", String.valueOf(countChanStp));
            } else {
                reportHTML = reportHTML.replaceAll("@DIFF_METH", "");
                reportHTML = reportHTML.replaceAll("@ADDS_METH", "");
                reportHTML = reportHTML.replaceAll("@DLTS_METH", "");
                reportHTML = reportHTML.replaceAll("@CHAN_METH", "");
            }
        }
        reportHTML = reportHTML + "	</tbody></table><br />\n";
        reportHTML = reportHTML + "</body>\n";
        reportHTML = reportHTML + "</html>\n";
        reportHTML = reportHTML.replaceAll("@VALUES_TREE", valuesTree);
        String file = outputDirectory + "\\" + ((HashMap) suiteNewBD.get(0)).get("SUITE") + "\\ReportTOTAL.html";
        FileWriter fichero = null;
        PrintWriter pw = null;
        try {
            fichero = new FileWriter(file);
            pw = new PrintWriter(fichero);
            pw.write(reportHTML);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fichero) fichero.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        String idTest = (String) ((HashMap) suiteNewBD.get(0)).get("SUITE_DATE");
        String idTestPrevio = "";
        if (suiteOldBD != null) idTestPrevio = (String) ((HashMap) suiteOldBD.get(0)).get("SUITE_DATE");
        sendMailResult(file, idTest, idTestPrevio, anyError);
    }

    public String getResulMethodLit(String result_script, String result_tng) {
        switch(Integer.parseInt(result_tng)) {
            case 2:
                return "NOK";
            case 3:
                return "SKIP";
            default:
                if (Integer.parseInt(result_script) == utils.Constantes.CONST_OK) return "OK"; else return "NOK";
        }
    }

    public String getResulStepLit(String result_script, String excepcion) {
        if (Integer.parseInt(result_script) == utils.Constantes.CONST_OK && Integer.parseInt(excepcion) == 0) return "OK"; else return "NOK";
    }

    public String getResulValidLit(String result) {
        if (Integer.parseInt(result) == utils.Constantes.CONST_OK) return "OK"; else return "NOK";
    }

    public void sendMailResult(String fileHTML, String idTest, String idTestPrevio, boolean anyError) {
        String SMTPHost = "webmail.caixapenedes.com";
        String from = "jorge.munoz@cast-info.es";
        String[] to = { "jorge.munoz@cast-info.es" };
        String[] CC = { "0920provesican@caixapenedes.com" };
        if (fileHTML.contains("D:\\")) fileHTML = fileHTML.replace("D:\\", "\\\\172.27.0.18\\"); else fileHTML = "\\\\172.27.0.18\\APPS\\Multicanal\\workspace\\Pruebas80\\build\\" + fileHTML;
        String resultado = "OK";
        if (anyError) resultado = "NOK";
        String subject = "[Pruebas80] Aviso pruebas realizadas (" + resultado + ")";
        String text = "Se ha ejecutado el script de pruebas del 80% de la operativa. Los datos asociados a la ejecuci�n son los siguientes:\n" + "\n" + "IDENTIFICADOR DEL TEST: " + idTest + "\n" + "\n" + "NAVEGADOR: " + this.getTestContexts().get(0).getAttribute("bpath") + "\n" + "\n" + "IDIOMA: " + this.getTestContexts().get(0).getAttribute("idioma") + "\n" + "\n" + "URL DE ACCESO: " + this.getTestContexts().get(0).getAttribute("appPath") + "\n" + "\n" + "RESULTADO: " + resultado + "\n" + "\n" + "REPORT HTML (Ubicado en m�quina de Hudson): " + fileHTML + "\n" + "\n";
        if (idTestPrevio.compareTo("") != 0) text += "COMPARATIVA CON TEST PREVIO: S� (" + idTestPrevio + ")\n"; else text += "COMPARATIVA CON TEST PREVIO: No \n";
        text += "\n" + "Un saludo, \n" + "Robot de pruebas.";
        String[] files = {};
        InfoMail infoMail = new InfoMail(SMTPHost, from, to, CC, subject, text);
        try {
            ControlMail.enviarMail(infoMail, "CAIXAPENEDES\\920167@caixapenedes.com", "jorge2008");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
