package org.adempiere.webui.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.adempiere.webui.EnvWeb;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.VerticalBox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.desktop.IDesktop;
import org.adempiere.webui.process.WProcessInfo;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.adempiere.webui.window.SimplePDFViewer;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoUtil;
import org.compiere.util.CLogger;
import org.compiere.util.Ctx;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compierezk.util.CompiereHelper;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Html;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 *	Dialog to Start process or report.
 *	Displays information about the process
 *		and lets the user decide to start it
 *  	and displays results (optionally print them).
 *  Calls ProcessCtl to execute.
 *  @author 	Low Heng Sin
 *  @author     arboleda - globalqss
 *  - Implement ShowHelp option on processes and reports
 */
public class ProcessDialog extends Window implements EventListener {

    private static final long serialVersionUID = 1L;

    /**
	 * Dialog to start a process/report
	 * @param ctx
	 * @param parent
	 * @param title
	 * @param aProcess
	 * @param WindowNo
	 * @param AD_Process_ID
	 * @param tableId
	 * @param recordId
	 * @param autoStart
	 */
    public ProcessDialog(int AD_Process_ID, boolean isSOTrx) {
        log.info("Process=" + AD_Process_ID);
        m_ctx = EnvWeb.getCtx();
        ;
        m_WindowNo = SessionManager.getAppDesktop().registerWindow(this);
        this.setAttribute(IDesktop.WINDOWNO_ATTRIBUTE, m_WindowNo);
        m_AD_Process_ID = AD_Process_ID;
        EnvWeb.getCtx().setContext(m_WindowNo, "IsSOTrx", isSOTrx ? "Y" : "N");
        try {
            initComponents();
            init();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "", ex);
        }
    }

    private void initComponents() {
        VerticalBox vbox = new VerticalBox();
        vbox.setWidth("100%");
        vbox.setSpacing("10px");
        Div div = new Div();
        message = new Html();
        div.appendChild(message);
        vbox.appendChild(div);
        centerPanel = new Panel();
        vbox.appendChild(centerPanel);
        div = new Div();
        div.setAlign("center");
        Hbox hbox = new Hbox();
        String label = Msg.getMsg(EnvWeb.getCtx(), "Start");
        bOK = new Button(label.replaceAll("&", ""));
        bOK.setImage("/images/Ok16.png");
        bOK.setId("Ok");
        bOK.addEventListener(Events.ON_CLICK, this);
        hbox.appendChild(bOK);
        label = Msg.getMsg(EnvWeb.getCtx(), "Cancel");
        Button btn = new Button(label.replaceAll("&", ""));
        btn.setImage("/images/Cancel16.png");
        btn.setId("Cancel");
        btn.addEventListener(Events.ON_CLICK, this);
        hbox.appendChild(btn);
        div.appendChild(hbox);
        vbox.appendChild(div);
        this.appendChild(vbox);
    }

    private int m_WindowNo;

    private Ctx m_ctx;

    private int m_AD_Process_ID;

    private String m_Name = null;

    private boolean m_IsReport = false;

    private int[] m_ids = null;

    private StringBuffer m_messageText = new StringBuffer();

    private String m_ShowHelp = null;

    private Panel centerPanel = null;

    private Html message = null;

    private Button bOK = null;

    private boolean valid = true;

    /**	Logger			*/
    private static CLogger log = CLogger.getCLogger(ProcessDialog.class);

    private ProcessParameterPanel parameterPanel = null;

    private ProcessInfo m_pi = null;

    private boolean m_isLocked = false;

    private String initialMessage;

    /**
	 * 	Set Visible 
	 * 	(set focus to OK if visible)
	 * 	@param visible true if visible
	 */
    public boolean setVisible(boolean visible) {
        return super.setVisible(visible);
    }

    /**
	 *	Dispose
	 */
    public void dispose() {
        SessionManager.getAppDesktop().closeWindow(m_WindowNo);
        valid = false;
    }

    /**
	 *	Dynamic Init
	 *  @return true, if there is something to process (start from menu)
	 */
    public boolean init() {
        log.config("");
        boolean trl = !Env.isBaseLanguage(m_ctx, "AD_Process");
        String sql = "SELECT Name, Description, Help, IsReport " + "FROM AD_Process " + "WHERE AD_Process_ID=?";
        if (trl) sql = "SELECT t.Name, t.Description, t.Help, p.IsReport " + "FROM AD_Process p, AD_Process_Trl t " + "WHERE p.AD_Process_ID=t.AD_Process_ID" + " AND p.AD_Process_ID=? AND t.AD_Language=?";
        try {
            PreparedStatement pstmt = DB.prepareStatement(sql, null);
            pstmt.setInt(1, m_AD_Process_ID);
            if (trl) pstmt.setString(2, Env.getAD_Language(m_ctx));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                m_Name = rs.getString(1);
                m_IsReport = rs.getString(4).equals("Y");
                m_ShowHelp = "N";
                m_messageText.append("<b>");
                String s = rs.getString(2);
                if (rs.wasNull()) m_messageText.append(Msg.getMsg(m_ctx, "StartProcess?")); else m_messageText.append(s);
                m_messageText.append("</b>");
                s = rs.getString(3);
                if (!rs.wasNull()) m_messageText.append("<p>").append(s).append("</p>");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, sql, e);
            return false;
        }
        if (m_Name == null) return false;
        this.setTitle(m_Name);
        initialMessage = m_messageText.toString();
        message.setContent(initialMessage);
        bOK.setLabel(Msg.getMsg(EnvWeb.getCtx(), "Start"));
        m_pi = new WProcessInfo(m_Name, m_AD_Process_ID);
        m_pi.setAD_User_ID(m_ctx.getAD_User_ID());
        m_pi.setAD_Client_ID(m_ctx.getAD_Client_ID());
        parameterPanel = new ProcessParameterPanel(m_WindowNo, m_pi, "70%");
        centerPanel.getChildren().clear();
        if (parameterPanel.init()) {
            centerPanel.appendChild(parameterPanel);
        } else {
            if (m_ShowHelp != null && m_ShowHelp.equals("N")) {
                startProcess();
            }
        }
        if (m_ShowHelp != null && m_ShowHelp.equals("S")) {
            startProcess();
        }
        return true;
    }

    public void startProcess() {
        if (!getDesktop().isServerPushEnabled()) getDesktop().enableServerPush(true);
        this.lockUI(m_pi);
        Runnable runnable = new Runnable() {

            public void run() {
                org.zkoss.zk.ui.Desktop desktop = ProcessDialog.this.getDesktop();
                try {
                    Executions.activate(desktop);
                    try {
                        CompiereHelper.process(null, m_WindowNo, parameterPanel, m_pi, null);
                    } finally {
                        unlockUI(m_pi);
                        Executions.deactivate(desktop);
                    }
                } catch (DesktopUnavailableException e) {
                    log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, e.getLocalizedMessage(), e);
                }
            }
        };
        new Thread(runnable).start();
    }

    public void onEvent(Event event) {
        Component component = event.getTarget();
        if (component instanceof Button) {
            Button element = (Button) component;
            if ("Ok".equalsIgnoreCase(element.getId())) {
                if (element.getLabel().length() > 0) this.startProcess(); else this.dispose();
            } else if ("Cancel".equalsIgnoreCase(element.getId())) {
                this.dispose();
            }
        }
    }

    public void lockUI(ProcessInfo pi) {
        if (m_isLocked) return;
        m_isLocked = true;
        if (Executions.getCurrent() != null) Clients.showBusy("Processing...", true); else {
            try {
                Executions.activate(this.getDesktop());
                try {
                    Clients.showBusy("Processing...", true);
                } catch (Error ex) {
                    throw ex;
                } finally {
                    Executions.deactivate(this.getDesktop());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to lock UI.", e);
            }
        }
    }

    public void unlockUI(ProcessInfo pi) {
        if (!m_isLocked) return;
        m_isLocked = false;
        if (Executions.getCurrent() != null) {
            Clients.showBusy(null, false);
            updateUI(pi);
        } else {
            try {
                Executions.activate(this.getDesktop());
                try {
                    updateUI(pi);
                    Clients.showBusy(null, false);
                } catch (Error ex) {
                    throw ex;
                } finally {
                    Executions.deactivate(this.getDesktop());
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to update UI upon unloc.", e);
            }
        }
    }

    private void updateUI(ProcessInfo pi) {
        ProcessInfoUtil.setLogFromDB(pi);
        m_messageText.append("<p><font color=\"").append(pi.isError() ? "#FF0000" : "#0000FF").append("\">** ").append(pi.getSummary()).append("</font></p>");
        m_messageText.append(pi.getLogInfo(true));
        message.setContent(m_messageText.toString());
        bOK.setLabel("");
        m_ids = pi.getIDs();
        centerPanel.detach();
        invalidate();
        Clients.response(new AuEcho(this, "onAfterProcess", null));
    }

    public void onAfterProcess() {
        afterProcessTask();
        if (m_IsReport && !m_pi.isError()) this.dispose();
        if (m_ShowHelp != null && m_ShowHelp.equals("S")) this.dispose();
    }

    /**************************************************************************
	 *	Optional Processing Task
	 */
    private void afterProcessTask() {
        if (m_ids != null && m_ids.length > 0) {
            log.config("");
            if (m_AD_Process_ID == 119) printInvoices(); else if (m_AD_Process_ID == 118) printShipments();
        }
    }

    /**************************************************************************
	 *	Print Shipments
	 */
    private void printShipments() {
        if (m_ids == null) return;
        if (!FDialog.ask(m_WindowNo, this, "PrintShipments")) return;
        m_messageText.append("<p>").append(Msg.getMsg(EnvWeb.getCtx(), "PrintShipments")).append("</p>");
        message.setContent(m_messageText.toString());
        Clients.showBusy("Processing...", true);
        Clients.response(new AuEcho(this, "onPrintShipments", null));
    }

    public void onPrintShipments() {
        List<File> pdfList = new ArrayList<File>();
        for (int i = 0; i < m_ids.length; i++) {
            int M_InOut_ID = m_ids[i];
            ReportEngine re = ReportEngine.get(EnvWeb.getCtx(), ReportEngine.SHIPMENT, M_InOut_ID);
            pdfList.add(re.getPDF());
        }
        if (pdfList.size() > 1) {
            try {
                File outFile = File.createTempFile("PrintShipments", ".pdf");
                Document document = null;
                PdfWriter copy = null;
                for (File f : pdfList) {
                    String fileName = f.getAbsolutePath();
                    PdfReader reader = new PdfReader(fileName);
                    reader.consolidateNamedDestinations();
                    if (document == null) {
                        document = new Document(reader.getPageSizeWithRotation(1));
                        copy = PdfWriter.getInstance(document, new FileOutputStream(outFile));
                        document.open();
                    }
                    int pages = reader.getNumberOfPages();
                    PdfContentByte cb = copy.getDirectContent();
                    for (int i = 1; i <= pages; i++) {
                        document.newPage();
                        PdfImportedPage page = copy.getImportedPage(reader, i);
                        cb.addTemplate(page, 0, 0);
                    }
                }
                document.close();
                Clients.showBusy(null, false);
                Window win = new SimplePDFViewer(this.getTitle(), new FileInputStream(outFile));
                SessionManager.getAppDesktop().showWindow(win, "center");
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        } else if (pdfList.size() > 0) {
            Clients.showBusy(null, false);
            try {
                Window win = new SimplePDFViewer(this.getTitle(), new FileInputStream(pdfList.get(0)));
                SessionManager.getAppDesktop().showWindow(win, "center");
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    /**
	 *	Print Invoices
	 */
    private void printInvoices() {
        if (m_ids == null) return;
        if (!FDialog.ask(m_WindowNo, this, "PrintInvoices")) return;
        m_messageText.append("<p>").append(Msg.getMsg(EnvWeb.getCtx(), "PrintInvoices")).append("</p>");
        message.setContent(m_messageText.toString());
        Clients.showBusy("Processing...", true);
        Clients.response(new AuEcho(this, "onPrintInvoices", null));
    }

    public void onPrintInvoices() {
        List<File> pdfList = new ArrayList<File>();
        for (int i = 0; i < m_ids.length; i++) {
            int C_Invoice_ID = m_ids[i];
            ReportEngine re = ReportEngine.get(EnvWeb.getCtx(), ReportEngine.INVOICE, C_Invoice_ID);
            pdfList.add(re.getPDF());
        }
        if (pdfList.size() > 1) {
            try {
                File outFile = File.createTempFile("PrintInvoices", ".pdf");
                Document document = null;
                PdfWriter copy = null;
                for (File f : pdfList) {
                    PdfReader reader = new PdfReader(f.getAbsolutePath());
                    if (document == null) {
                        document = new Document(reader.getPageSizeWithRotation(1));
                        copy = PdfWriter.getInstance(document, new FileOutputStream(outFile));
                        document.open();
                    }
                    PdfContentByte cb = copy.getDirectContent();
                    int pages = reader.getNumberOfPages();
                    for (int i = 1; i <= pages; i++) {
                        document.newPage();
                        PdfImportedPage page = copy.getImportedPage(reader, i);
                        cb.addTemplate(page, 0, 0);
                    }
                }
                document.close();
                Clients.showBusy(null, false);
                Window win = new SimplePDFViewer(this.getTitle(), new FileInputStream(outFile));
                SessionManager.getAppDesktop().showWindow(win, "center");
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        } else if (pdfList.size() > 0) {
            try {
                Window win = new SimplePDFViewer(this.getTitle(), new FileInputStream(pdfList.get(0)));
                SessionManager.getAppDesktop().showWindow(win, "center");
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void executeASync(ProcessInfo pi) {
    }

    public boolean isUILocked() {
        return m_isLocked;
    }
}
