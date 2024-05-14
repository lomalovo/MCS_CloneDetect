package com.jesyre.test.collaboration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.jesyre.collaboration.exception.CollaborationException;
import com.jesyre.collaboration.helper.CollaborationFilter;
import com.jesyre.collaboration.helper.JPAHelper;
import com.jesyre.collaboration.helper.CollaborationFilter.Cadenza;
import com.jesyre.collaboration.object.Agenda;
import com.jesyre.collaboration.object.Attivita;
import com.jesyre.collaboration.object.Categoria;
import com.jesyre.collaboration.object.Evento;
import com.jesyre.collaboration.object.Luogo;
import com.jesyre.collaboration.object.Non_conformita;
import com.jesyre.collaboration.object.Non_conformita_azioni;
import com.jesyre.collaboration.object.Personale;
import com.jesyre.collaboration.object.Personale_curriculum;
import com.jesyre.collaboration.object.Piani_azioni_correttive;
import com.jesyre.collaboration.object.Piani_azioni_preventive;
import com.jesyre.collaboration.object.Piano_obiettivi;
import com.jesyre.collaboration.object.Progetti;
import com.jesyre.collaboration.object.Proprieta_task;
import com.jesyre.collaboration.object.Ruoli;
import com.jesyre.collaboration.object.Task;
import com.jesyre.collaboration.object.Tipo_attivita;
import com.jesyre.collaboration.object.Tipo_task;
import com.jesyre.collaboration.object.Progetti.Stato;
import com.jesyre.collaboration.util.Constant;
import com.jesyre.collaboration.util.chart.AbstractChart;
import com.jesyre.collaboration.util.chart.AllocazioneChart;
import com.jesyre.collaboration.util.chart.FinanziamentoProgettiChart;
import com.jesyre.collaboration.util.chart.OreChart;
import com.jesyre.collaboration.util.chart.ProposteProgettiChart;
import com.jesyre.collaboration.util.chart.TrasfertaChart;
import com.jesyre.collaboration.util.report.ReportManager;
import com.jesyre.framework.util.ByteArrayDataSource;
import com.jesyre.framework.util.Configure;
import com.jesyre.framework.util.General;
import com.jesyre.framework.util.date.DateTime;
import com.jesyre.framework.util.date.DateTime.YearOutOfRangeException;
import com.jesyre.framework.util.file.FileSystem;
import com.jesyre.framework.util.smtp.Mailer;

public class TestJPA {

    private static final SimpleDateFormat titleFrmt = new SimpleDateFormat("MMMM yyyy", Locale.ITALY);

    private static final String TEST_CONNECTION = "JSRCollaborationLocal";

    private static int actualYear = 2010;

    private static int actualMonth = Calendar.MAY;

    private static String reportPath = "I:\\tmp\\";

    public static void main(String[] args) {
        EntityTransaction tx = null;
        try {
            EntityManager em = getEntityManager(TEST_CONNECTION);
            tx = em.getTransaction();
            tx.begin();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    private static JPAHelper helper;

    private static JPAHelper getHelper() {
        if (helper == null) {
            helper = new JPAHelper();
        }
        return helper;
    }

    private static EntityManager getEntityManager(String name) {
        return getHelper().getEntityManager(name);
    }

    public static List<Categoria> testSearchCategoria() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Categoria> results = getHelper().searchCategoria(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static void testCreateICalendar() {
        try {
            CollaborationFilter filter = new CollaborationFilter();
            FileOutputStream stream = new FileOutputStream("c:\\temp\\c.ics");
            getHelper().doICalendar(stream, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Tipo_attivita> testSearchTipoAttivita() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Tipo_attivita> results = getHelper().searchTipoAttivita(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Tipo_task> testSearchTipoTask() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Tipo_task> results = getHelper().searchTipoTask(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Luogo> testSearchLuogo() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Luogo> results = getHelper().searchLuogo(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Ruoli> testSearchRuoli() {
        CollaborationFilter filter = new CollaborationFilter(false);
        List<Ruoli> results = getHelper().searchRuoli(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Personale> testSearchPersonale() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setTesto("bini");
        List<Personale> results = getHelper().searchPersonale(filter);
        for (Personale obj : results) {
            System.out.println(obj.getCognome() + " " + obj.getNome());
            List<Task> tasksA = obj.getTasks2();
            if (General.isValidField(tasksA)) {
                for (Task task : tasksA) {
                    if (!task.isOk()) System.out.println("Task: " + task.getScadenza() + " " + task.getDescrizione());
                }
            }
            List<Evento> eventi = obj.getEventos();
            if (General.isValidField(eventi)) {
                for (Evento ev : eventi) {
                    if (ev.getData_inizio().after(DateTime.now())) System.out.println("Evento: " + ev.getData_inizio() + " " + ev.getDescrizione());
                }
            }
        }
        return results;
    }

    public static List<Progetti> testSearchProgetti() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Progetti> results = getHelper().searchProgetti(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Progetti> testSearchProgettiChiusi() {
        List<Calendar> l = buildCalendarStartEndMese(actualYear, Calendar.MARCH);
        List<Progetti> results = getHelper().searchProgettiChiusi(l.get(0).getTime(), l.get(1).getTime());
        for (Progetti prog : results) {
            System.out.println(prog);
        }
        return results;
    }

    public static List<Progetti> testSearchProgettiApertiChiusi() {
        List<Progetti> aperti = getHelper().searchProgettiAperti();
        List<Calendar> l = buildCalendarStartEndMese(actualYear, Calendar.MARCH);
        List<Progetti> chiusi = getHelper().searchProgettiChiusi(l.get(0).getTime(), l.get(1).getTime());
        List<Progetti> totali = new ArrayList<Progetti>();
        totali.addAll(aperti);
        totali.addAll(chiusi);
        for (Progetti prog : totali) {
            System.out.println(prog);
        }
        return totali;
    }

    public static <T> T testFind(Class<T> entityClass, Object primaryKey) {
        T obj = getHelper().find(entityClass, primaryKey);
        System.out.println(obj);
        return obj;
    }

    public static List<Attivita> testSearchAttivita() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setTesto(Constant.ATTIVITA_TRASFERTA.toLowerCase());
        List<Attivita> results = getHelper().searchAttivita(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Agenda> testSearchAgenda() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Personale> pers = getHelper().searchPersonale(filter);
        Personale personale = pers.get(0);
        System.out.println(personale);
        filter.setPersonale(personale);
        List<Agenda> results = getHelper().searchAgenda(filter);
        for (Agenda obj : results) {
            System.out.println(obj);
            List<Personale> l = obj.getPersonales();
            if (General.isValidField(l)) {
                for (Personale p : l) {
                    System.out.println(p);
                }
            }
        }
        return results;
    }

    public static List<Non_conformita> testSearchNonConformita() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Non_conformita> results = getHelper().searchNonConformita(filter);
        for (Non_conformita o : results) {
            List<Non_conformita_azioni> l = o.getNonConformitaAzionis();
            for (Non_conformita_azioni i : l) {
                System.out.println("nc: " + i.getAzione());
            }
        }
        return results;
    }

    public static List<Piani_azioni_correttive> testSearchPac() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Piani_azioni_correttive> results = getHelper().searchPac(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Piani_azioni_preventive> testSearchPap() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Piani_azioni_preventive> results = getHelper().searchPap(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Piano_obiettivi> testSearchPianoObiettivi() {
        CollaborationFilter filter = new CollaborationFilter();
        List<Piano_obiettivi> results = getHelper().searchPianoObiettivi(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static List<Task> testSearchTask() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.addNoTipo(Constant.TIPO_TASK_ALLOCAZIONE);
        filter.addNoTipo(Constant.TIPO_TASK_TRASFERTA);
        List<Task> results = getHelper().searchTask(filter);
        for (Task task : results) {
            System.out.println("result: " + task.getTipoTask().getNome());
        }
        return results;
    }

    public static void testReportXlsTask() {
        testReportXlsTask(actualYear, actualMonth);
    }

    public static void testReportXlsTask(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        CollaborationFilter filter = new CollaborationFilter();
        filter.setData_inizio(new Timestamp(start.getTimeInMillis()));
        filter.setData_fine(new Timestamp(end.getTimeInMillis()));
        filter.setTipo(Constant.TIPO_TASK_ALLOCAZIONE);
        List<Task> results = getHelper().searchTask(filter);
        List<Personale> personas = getHelper().searchPersonale(null);
        String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
        String path = FileSystem.getUnionPath(new String[] { reportPath, folderMese });
        if (!FileSystem.exist(path)) FileSystem.mkDir(path);
        String reportTitle = "Presenze " + titleFrmt.format(start.getTime());
        String reportName = reportTitle.replace(' ', '_') + ".xls";
        Map<Personale, Double> ret = ReportManager.getInstance().doTask2Excel(results, personas, start, end, reportTitle, FileSystem.getUnionPath(path, reportName));
        for (Personale pers : ret.keySet()) {
            int oreAllocabili = getOreLavorative(pers, start, end);
            System.out.println(pers.getCognome() + " " + pers.getNome() + " = " + ret.get(pers) + " su " + oreAllocabili);
        }
    }

    public static void testReportXlsTraferte() {
        testReportXlsTraferte(actualYear, actualMonth);
    }

    public static void testReportXlsTraferte(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        CollaborationFilter filter = new CollaborationFilter();
        List<Personale> personas = getHelper().searchPersonale(filter);
        List<Progetti> progetti = getHelper().searchProgettiAperti();
        FileOutputStream f = null;
        try {
            String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
            String path = FileSystem.getUnionPath(new String[] { reportPath, folderMese });
            if (!FileSystem.exist(path)) FileSystem.mkDir(path);
            String reportTitle = "Trasferte " + titleFrmt.format(start.getTime());
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            f = new FileOutputStream(FileSystem.getUnionPath(path, reportName));
            HSSFWorkbook wb = new HSSFWorkbook();
            for (Personale pers : personas) {
                if (!pers.getRuoli().getNome_ruolo().equalsIgnoreCase(Constant.RUOLO_ADMIN)) {
                    ReportManager.getInstance().createSheetTrasferta(getHelper(), progetti, pers, start, end, wb);
                }
            }
            wb.write(f);
        } catch (Exception e) {
            Configure.log(e);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException e) {
                Configure.log(e);
            }
        }
    }

    public static void testReportXlsTrasferteProgetti() {
        testReportXlsTrasferteProgetti(actualYear, actualMonth);
    }

    public static void testReportXlsTrasferteProgetti(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        List<Progetti> progetti = getHelper().searchProgettiAperti();
        for (Progetti prog : progetti) {
            CollaborationFilter taskFilter = new CollaborationFilter();
            taskFilter.setData_inizio(new Timestamp(start.getTimeInMillis()));
            taskFilter.setData_fine(new Timestamp(end.getTimeInMillis()));
            taskFilter.setTipo(Constant.TIPO_TASK_TRASFERTA);
            taskFilter.setProgetto(prog);
            taskFilter.addOrder("data_inizio");
            List<Task> taskList = getHelper().searchTask(taskFilter);
            String reportTitle = "Trasferte di " + prog.getAcronimo() + " " + titleFrmt.format(start.getTime());
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            FileOutputStream f = null;
            if (taskList.size() > 0) {
                try {
                    String folderProgetto = prog.getUid();
                    String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
                    String pathProgetto = FileSystem.getUnionPath(new String[] { reportPath, folderProgetto, folderMese });
                    if (!FileSystem.exist(pathProgetto)) FileSystem.mkDir(pathProgetto);
                    f = new FileOutputStream(FileSystem.getUnionPath(pathProgetto, reportName));
                    HSSFWorkbook wb = new HSSFWorkbook();
                    ReportManager.getInstance().createFileXslTrasferta(taskList, prog, wb);
                    wb.write(f);
                } catch (Exception e) {
                    Configure.log(e);
                } finally {
                    if (f != null) try {
                        f.close();
                    } catch (IOException e) {
                        Configure.log(e);
                    }
                }
            }
        }
    }

    public static void testReportXlsTrasfertePersonale() {
        testReportXlsTrasfertePersonale(actualYear, actualMonth);
    }

    public static void testReportXlsTrasfertePersonale(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        List<Personale> personas = getHelper().searchPersonale(null);
        for (Personale pers : personas) {
            CollaborationFilter taskFilter = new CollaborationFilter();
            taskFilter.setData_inizio(new Timestamp(start.getTimeInMillis()));
            taskFilter.setData_fine(new Timestamp(end.getTimeInMillis()));
            taskFilter.setTipo(Constant.TIPO_TASK_TRASFERTA);
            taskFilter.setA(pers);
            taskFilter.addOrder("id");
            List<Task> taskList = getHelper().searchTask(taskFilter);
            String reportTitle = "Trasferte di " + pers.getCognome() + " " + pers.getNome() + " " + titleFrmt.format(start.getTime());
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            FileOutputStream f = null;
            if (taskList.size() > 0) {
                try {
                    String folderPersonale = pers.getId() + "_" + pers.getCognome().toLowerCase() + "_" + pers.getNome().toLowerCase();
                    String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
                    String pathPersonale = FileSystem.getUnionPath(new String[] { reportPath, folderPersonale, folderMese });
                    if (!FileSystem.exist(pathPersonale)) FileSystem.mkDir(pathPersonale);
                    f = new FileOutputStream(FileSystem.getUnionPath(pathPersonale, reportName));
                    HSSFWorkbook wb = new HSSFWorkbook();
                    if (!pers.getRuoli().getNome_ruolo().equalsIgnoreCase(Constant.RUOLO_ADMIN)) {
                        ReportManager.getInstance().createFileXslTrasferta(taskList, pers, wb);
                    }
                    wb.write(f);
                    List<String> filesName = new ArrayList<String>();
                    filesName.add(FileSystem.getUnionPath(new String[] { folderPersonale, folderMese, reportName }));
                    for (Task task : taskList) {
                        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        String title = format.format(task.getData_inizio());
                        Document doc = new Document(PageSize.A4);
                        String filename = "autorizzazione_missione_" + title + ".pdf";
                        try {
                            String pdfPath = FileSystem.getUnionPath(pathPersonale, filename);
                            PdfWriter.getInstance(doc, new FileOutputStream(pdfPath));
                            doc.open();
                            String pathLogo = FileSystem.getUnionPath(new String[] { reportPath, "images" });
                            if (!pers.getRuoli().getNome_ruolo().equalsIgnoreCase(Constant.RUOLO_ADMIN)) {
                                ReportManager.getInstance().createFilePdfTrasferta(task, pers, doc, pathLogo);
                            }
                            filesName.add(FileSystem.getUnionPath(new String[] { folderPersonale, folderMese, filename }));
                            doc.close();
                        } catch (Exception e) {
                            Configure.log(e);
                        }
                    }
                } catch (Exception e) {
                    Configure.log(e);
                } finally {
                    if (f != null) try {
                        f.close();
                    } catch (IOException e) {
                        Configure.log(e);
                    }
                }
            }
        }
    }

    private static int getOreLavorative(Personale p, Calendar start, Calendar end) {
        int ret = 0;
        try {
            if (p == null) return 0;
            Calendar s = (Calendar) start.clone();
            ret = 0;
            while (true) {
                if (!(s.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) && !(s.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) && !(DateTime.isMondayEaster(s))) {
                    ret += p.getRisorse();
                }
                s.add(Calendar.DATE, 1);
                if (s.after(end)) break;
            }
        } catch (YearOutOfRangeException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static List<Calendar> buildCalendarStartEndMese(int year, int month) {
        Calendar start = GregorianCalendar.getInstance();
        start.set(Calendar.DATE, 1);
        start.set(Calendar.MONTH, month);
        start.set(Calendar.YEAR, year);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        end.set(Calendar.DATE, end.getActualMaximum(Calendar.DATE));
        List<Calendar> l = new ArrayList<Calendar>();
        l.add(start);
        l.add(end);
        return l;
    }

    private static List<Calendar> buildCalendarStartEndAnno(int year) {
        Calendar start = GregorianCalendar.getInstance();
        start.set(Calendar.DATE, 1);
        start.set(Calendar.MONTH, Calendar.JANUARY);
        start.set(Calendar.YEAR, year);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        end.set(Calendar.MONTH, Calendar.DECEMBER);
        end.set(Calendar.DATE, end.getActualMaximum(Calendar.DATE));
        List<Calendar> l = new ArrayList<Calendar>();
        l.add(start);
        l.add(end);
        return l;
    }

    public static void testReportProgettiXlsTask() {
        testReportProgettiXlsTask(actualYear, actualMonth);
    }

    public static void testReportProgettiXlsTask(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        List<Personale> personas = getHelper().searchPersonale(null);
        CollaborationFilter progFilter = new CollaborationFilter();
        progFilter.setStatoProgetto(Stato.APERTO);
        progFilter.setFlag(Constant.FLAG_PROGETTO);
        progFilter.addOrder("acronimo");
        List<Progetti> progetti = getHelper().searchProgetti(progFilter);
        FileOutputStream f = null;
        try {
            String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
            String path = FileSystem.getUnionPath(new String[] { reportPath, folderMese });
            if (!FileSystem.exist(path)) FileSystem.mkDir(path);
            String reportTitle = "Allocazione personale per progetto " + titleFrmt.format(start.getTime());
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            f = new FileOutputStream(FileSystem.getUnionPath(path, reportName));
            HSSFWorkbook wb = new HSSFWorkbook();
            for (Progetti prog : progetti) {
                CollaborationFilter filter = new CollaborationFilter();
                filter.setData_inizio(new Timestamp(start.getTimeInMillis()));
                filter.setData_fine(new Timestamp(end.getTimeInMillis()));
                filter.setTipo(Constant.TIPO_TASK_ALLOCAZIONE);
                filter.setProgetto(prog);
                List<Task> tasks = getHelper().searchTask(filter);
                ReportManager.getInstance().createSheet(tasks, personas, prog, start, end, wb);
            }
            wb.write(f);
        } catch (Exception e) {
            Configure.log(e);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException e) {
                Configure.log(e);
            }
        }
    }

    public static void testReportProgettiYearXlsTask() {
        testReportProgettiYearXlsTask(actualYear);
    }

    public static void testReportProgettiYearXlsTask(int year) {
        List<Calendar> l = buildCalendarStartEndAnno(year);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        List<Personale> personas = getHelper().searchPersonale(null);
        CollaborationFilter progFilter = new CollaborationFilter();
        progFilter.setStatoProgetto(Stato.APERTO);
        progFilter.setFlag(Constant.FLAG_PROGETTO);
        progFilter.addOrder("acronimo");
        List<Progetti> progetti = getHelper().searchProgetti(progFilter);
        FileOutputStream f = null;
        try {
            String reportTitle = "Allocazione progetti annuale " + start.get(Calendar.YEAR);
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            f = new FileOutputStream(FileSystem.getUnionPath(reportPath, reportName));
            HSSFWorkbook wb = new HSSFWorkbook();
            for (Progetti prog : progetti) {
                CollaborationFilter filter = new CollaborationFilter();
                filter.setData_inizio(new Timestamp(start.getTimeInMillis()));
                filter.setData_fine(new Timestamp(end.getTimeInMillis()));
                filter.setTipo(Constant.TIPO_TASK_ALLOCAZIONE);
                filter.setProgetto(prog);
                List<Task> tasks = getHelper().searchTask(filter);
                ReportManager.getInstance().createSheetYear(tasks, personas, prog, start, end, wb);
            }
            wb.write(f);
        } catch (Exception e) {
            Configure.log(e);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException e) {
                Configure.log(e);
            }
        }
    }

    public static void testReportPersonaleXlsTask() {
        testReportPersonaleXlsTask(actualYear, actualMonth);
    }

    public static void testReportPersonaleXlsTask(int year, int month) {
        List<Calendar> l = buildCalendarStartEndMese(year, month);
        Calendar start = l.get(0);
        Calendar end = l.get(1);
        List<Personale> personas = getHelper().searchPersonale(null);
        CollaborationFilter progFilter = new CollaborationFilter();
        progFilter.setStatoProgetto(Stato.APERTO);
        progFilter.setFlag(Constant.FLAG_PROGETTO);
        progFilter.addOrder("acronimo");
        List<Progetti> progetti = getHelper().searchProgetti(progFilter);
        FileOutputStream f = null;
        try {
            String folderMese = titleFrmt.format(start.getTime()).replace(' ', '_');
            String path = FileSystem.getUnionPath(new String[] { reportPath, folderMese });
            if (!FileSystem.exist(path)) FileSystem.mkDir(path);
            String reportTitle = "Allocazione progetti per personale " + titleFrmt.format(start.getTime());
            String reportName = reportTitle.replace(' ', '_') + ".xls";
            f = new FileOutputStream(FileSystem.getUnionPath(path, reportName));
            HSSFWorkbook wb = new HSSFWorkbook();
            for (Personale pers : personas) {
                if (!pers.getRuoli().getNome_ruolo().equalsIgnoreCase(Constant.RUOLO_ADMIN)) {
                    CollaborationFilter filter = new CollaborationFilter();
                    filter.setA(pers);
                    filter.setData_inizio(new Timestamp(start.getTimeInMillis()));
                    filter.setData_fine(new Timestamp(end.getTimeInMillis()));
                    filter.setTipo(Constant.TIPO_TASK_ALLOCAZIONE);
                    List<Task> tasks = getHelper().searchTask(filter);
                    ReportManager.getInstance().createSheet(tasks, progetti, pers, start, end, wb);
                }
            }
            wb.write(f);
        } catch (Exception e) {
            Configure.log(e);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException e) {
                Configure.log(e);
            }
        }
    }

    public static Object testSearchAllocazione() {
        CollaborationFilter filter = new CollaborationFilter();
        Progetti p = new Progetti();
        p.setId(34);
        filter.setProgetto(p);
        Personale per = new Personale();
        per.setId(12);
        filter.setA(per);
        filter.setData_inizio(new Timestamp(109, 10, 3, 0, 0, 0, 0));
        filter.setData_fine(new Timestamp(109, 10, 3, 23, 59, 59, 999000000));
        Object results = getHelper().searchAllocazione(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static int testGetNumeroProgettiProposte() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setFlag(Constant.FLAG_PROGETTO);
        filter.setAnno(2007);
        Integer int0 = getHelper().getNumeroProgettiProposte(filter);
        System.out.println("progetti: " + int0);
        filter.setFlag(Constant.FLAG_PROPOSTA);
        int0 = getHelper().getNumeroProgettiProposte(filter);
        System.out.println("proposte: " + int0);
        return int0;
    }

    public static List<Double> testGetFinanziamentoProgetti() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setAnno(2009);
        List<Double> l = getHelper().getFianziamentoProgetti(filter);
        System.out.println("finanz: " + l);
        return l;
    }

    public static Object testSearchTrasferta() {
        CollaborationFilter filter = new CollaborationFilter();
        Progetti p = new Progetti();
        p.setId(31);
        filter.setProgetto(p);
        Object results = getHelper().searchTrasferta(filter);
        System.out.println("results: " + results);
        return results;
    }

    public static void testAddCategoria() {
        Categoria obj = new Categoria();
        obj.setNome("scaramellozzi");
        obj.setDescrizione("my desc");
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddProprietaTask() {
        Proprieta_task obj = new Proprieta_task();
        Task t = testSearchTask().get(0);
        obj.setTask(t);
        obj.setDescrizione("my desc");
        obj.setTesto_1("testo_1");
        obj.setTesto_2("testo_2");
        obj.setTesto_3("testo_3");
        obj.setTesto_4("testo_4");
        obj.setTesto_5("testo_5");
        obj.setCosto_1(1.0);
        obj.setCosto_2(2.78);
        obj.setCosto_3(1456.76);
        obj.setCosto_4(10.0);
        obj.setCosto_5(15.01);
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddTipoTask() {
        Tipo_task obj = new Tipo_task();
        obj.setNome("prova tipo task");
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddTipoAttivita() {
        Tipo_attivita obj = new Tipo_attivita();
        obj.setNome("prova tipo attivit�");
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddProgetto() {
        Progetti obj = new Progetti();
        Categoria cat = testSearchCategoria().get(0);
        obj.setCategoria(cat);
        Luogo l = testSearchLuogo().get(0);
        obj.setLuogo(l);
        obj.setAcronimo("JSRTest2");
        obj.setData_inizio(new Timestamp(109, 11, 23, 0, 0, 0, 0));
        obj.setDescrizione("progetto di test");
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddAttivita() {
        Attivita obj = new Attivita();
        Progetti p = testSearchProgetti().get(0);
        obj.setProgetto(p);
        obj.setNome("Attivit� di test");
        obj.setData_inizio(new Timestamp(109, 9, 23, 0, 0, 0, 0));
        obj.setData_fine(new Timestamp(109, 11, 10, 0, 0, 0, 0));
        Tipo_attivita tipo = testSearchTipoAttivita().get(0);
        obj.setTipoAttivita(tipo);
        obj.setOre_uomo(2.5);
        obj.setNote("rrtterter tetertert tyertertrt");
        obj.setPersonales(testSearchPersonale());
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddPersonale() {
        Personale obj = new Personale();
        obj.setUsername("1");
        obj.setPwd("pwd");
        obj.setNome("Francesco");
        obj.setCognome("Bini");
        obj.setData_nascita(new Timestamp(75, 8, 14, 0, 0, 0, 0));
        obj.setTitolo_studio("Laurea Ingegneria delle Telecomunicazioni");
        obj.setAnno_titolo_studio(2002);
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testSendMail() throws IOException, MessagingException {
        String smtpServer = "mail";
        String to = "fbini75@gmail.com";
        String from = "test@etruriaiinovazione.it";
        String fromReal = "Test";
        String subject = "Test mailer";
        String text = "bla bla bla bla......" + Constant.A_CAPO + "bla bla bla bla......" + Constant.A_CAPO + Constant.A_CAPO + "bla bla bla bla......" + Constant.A_CAPO;
        String urlRel = "/project/nuovotask.jsf?id=28";
        String label = "Vai al task";
        text += Constant.A_CAPO + label + " " + urlRel;
        DataSource body = new ByteArrayDataSource(text + Constant.FOOTER_MESSAGE, "text/plain");
        Mailer.send(smtpServer, to, from, fromReal, subject, body, null, null);
    }

    public static void testAddCurriculum() {
        Personale obj = new Personale();
        obj.setUsername("1");
        obj.setPwd("pwd");
        obj.setNome("Mario");
        obj.setCognome("Rossi");
        obj.setData_nascita(new Timestamp(75, 8, 14, 0, 0, 0, 0));
        obj.setTitolo_studio("Laurea Ingegneria delle Telecomunicazioni");
        obj.setAnno_titolo_studio(2002);
        List<Personale_curriculum> l = new ArrayList<Personale_curriculum>();
        Personale_curriculum c = new Personale_curriculum();
        c.setOrganizzazione("org1");
        c.setPeriodo("per1");
        c.setPosizione("pos1");
        c.setPersonale(obj);
        l.add(c);
        c = new Personale_curriculum();
        c.setOrganizzazione("org2");
        c.setPeriodo("per2");
        c.setPosizione("pos2");
        c.setPersonale(obj);
        l.add(c);
        obj.setPersonaleCurriculums(l);
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAddTask() {
        Task obj = new Task();
        Progetti pro = testSearchProgetti().get(0);
        obj.setProgetto(pro);
        Tipo_task tipo = testSearchTipoTask().get(0);
        obj.setTipoTask(tipo);
        obj.setDescrizione("Task prova");
        obj.setData_scadenza(new Timestamp(109, 11, 23, 0, 0, 0, 0));
        obj.setData_inizio(DateTime.NOW());
        List<Personale> pers = testSearchPersonale();
        obj.setDa(pers.get(0));
        obj.setA(pers.get(1));
        getHelper().set(obj);
        System.out.println("JPA added " + obj);
    }

    public static void testAlertDaemon() {
        Timer actionTimer = new Timer(false);
        String timeRepeatStr = "5";
        String timeStartStr = null;
        String disabledStr = "false";
        String dayOfWeekStr = "1";
        String dayOfMonthStr = null;
        String monthStr = "10";
        String yearStr = "2009";
        String hourStr = "0";
        String minuteStr = "0";
        String secondStr = "0";
        String millisecondStr = "0";
        Long timeRepeat = Long.parseLong(timeRepeatStr);
        if (General.isValidField(timeStartStr)) {
            Long timeStart = Long.parseLong(timeStartStr);
            actionTimer.scheduleAtFixedRate(new TimerTask() {

                public int count = 0;

                public void run() {
                    System.out.println((count++) + "RUN!!!!!");
                }
            }, timeStart * 1000, timeRepeat * 1000);
        } else {
            Calendar c = GregorianCalendar.getInstance();
            if (General.isValidField(dayOfWeekStr)) {
                c.set(Calendar.DAY_OF_WEEK, Integer.parseInt(dayOfWeekStr));
            } else if (General.isValidField(dayOfMonthStr)) {
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayOfMonthStr));
                if (General.isValidField(monthStr)) c.set(Calendar.MONTH, Integer.parseInt(monthStr));
                if (General.isValidField(yearStr)) c.set(Calendar.YEAR, Integer.parseInt(yearStr));
            }
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourStr));
            c.set(Calendar.MINUTE, Integer.parseInt(minuteStr));
            c.set(Calendar.SECOND, Integer.parseInt(secondStr));
            c.set(Calendar.MILLISECOND, Integer.parseInt(millisecondStr));
            System.out.println(c.getTime());
            actionTimer.scheduleAtFixedRate(new TimerTask() {

                public int count = 0;

                public void run() {
                    System.out.println((count++) + "RUN!!!!!");
                }
            }, c.getTime(), timeRepeat * 1000);
        }
    }

    public static String testMakeMailPersonale() {
        int expireThreshold = 7;
        Calendar c = GregorianCalendar.getInstance(Locale.ITALY);
        c.add(Calendar.DAY_OF_MONTH, expireThreshold);
        Personale p = getHelper().find(Personale.class, new Integer(1));
        String text = Constant.makeTextMail(p, c.getTime(), false);
        System.out.println(text);
        return text;
    }

    public static void testBonciPregna() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setTesto("assenza");
        Progetti pro = getHelper().searchProgetti(filter).get(0);
        filter.setTesto("maternit");
        Attivita a = getHelper().searchAttivita(filter).get(0);
        filter.setTesto(Constant.TIPO_TASK_ALLOCAZIONE.toLowerCase());
        Tipo_task tipo = getHelper().searchTipoTask(filter).get(0);
        filter.setTesto("bonci");
        Personale pers = getHelper().searchPersonale(filter).get(0);
        Calendar c = GregorianCalendar.getInstance(Locale.ITALY);
        c.set(Calendar.YEAR, 2010);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 2);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Calendar end = GregorianCalendar.getInstance(Locale.ITALY);
        end.set(Calendar.YEAR, 2010);
        end.set(Calendar.MONTH, Calendar.MARCH);
        end.set(Calendar.DATE, 31);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        boolean pregna = true;
        System.out.println(pro.getAcronimo() + " " + a.getNome() + " " + pers.getCognome());
        while (pregna) {
            Task obj = new Task();
            obj.setProgetto(pro);
            obj.setAttivita(a);
            obj.setTipoTask(tipo);
            obj.setDescrizione("Allocazione " + pro.getAcronimo());
            obj.setData_inizio(new Timestamp(c.getTime().getTime()));
            obj.setData_fine(new Timestamp(c.getTime().getTime()));
            obj.setA(pers);
            obj.setDa(pers);
            obj.setStato(com.jesyre.collaboration.object.Task.Stato.FATTO);
            obj.setOre_uomo(pers.getRisorse());
            int numDay = c.get(Calendar.DAY_OF_WEEK);
            if ((numDay != Calendar.SUNDAY) && (numDay != Calendar.SATURDAY)) {
                System.out.println(c.getTime());
                getHelper().set(obj);
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
            if (c.after(end)) {
                pregna = false;
            }
        }
    }

    public static void testFerieEtruria() {
        CollaborationFilter filter = new CollaborationFilter();
        filter.setTesto("ferie");
        Progetti pro = getHelper().searchProgetti(filter).get(0);
        filter.setTesto("assente");
        Attivita a = getHelper().searchAttivita(filter).get(0);
        filter.setTesto(Constant.TIPO_TASK_ALLOCAZIONE.toLowerCase());
        Tipo_task tipo = getHelper().searchTipoTask(filter).get(0);
        List<Personale> l = getHelper().searchPersonale(null);
        String[] dates = new String[] { "16/08/2010", "17/08/2010", "18/08/2010", "19/08/2010", "20/08/2010", "22/12/2010", "23/12/2010", "24/12/2010", "27/12/2010", "28/12/2010", "29/12/2010", "30/12/2010", "31/12/2010" };
        for (Personale pers : l) {
            if (!pers.getRuoli().getNome_ruolo().equalsIgnoreCase(Constant.RUOLO_ADMIN)) {
                for (String s : dates) {
                    Calendar c = GregorianCalendar.getInstance(Locale.ITALY);
                    String[] values = s.split("/");
                    int day = Integer.parseInt(values[0]);
                    int month = Integer.parseInt(values[1]) - 1;
                    int year = Integer.parseInt(values[2]);
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DATE, day);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    Task obj = new Task();
                    obj.setProgetto(pro);
                    obj.setAttivita(a);
                    obj.setTipoTask(tipo);
                    obj.setDescrizione("Allocazione " + pro.getAcronimo());
                    obj.setData_inizio(new Timestamp(c.getTime().getTime()));
                    obj.setData_fine(new Timestamp(c.getTime().getTime()));
                    obj.setA(pers);
                    obj.setDa(pers);
                    obj.setStato(com.jesyre.collaboration.object.Task.Stato.FATTO);
                    obj.setOre_uomo(pers.getRisorse());
                    int numDay = c.get(Calendar.DAY_OF_WEEK);
                    if ((numDay != Calendar.SUNDAY) && (numDay != Calendar.SATURDAY)) {
                        System.out.println("Allocazione " + obj.getProgetto().getAcronimo() + " " + c.getTime() + " " + obj.getA().getCognome() + " " + obj.getA().getNome());
                    }
                }
            }
        }
    }

    public static void testOreChart() throws CollaborationException {
        String path1 = "I://tmp/testAnnuale.jpg";
        String path2 = "I://tmp/testMensile.jpg";
        String path3 = "I://tmp/testSempre.jpg";
        String path4 = "I://tmp/testMensile.xls";
        CollaborationFilter filter = new CollaborationFilter();
        Personale p = getHelper().find(Personale.class, new Integer(2));
        filter.setA(p);
        filter.setInizio(new Timestamp(109, 9, 3, 0, 0, 0, 0));
        filter.setCadenza(Cadenza.ANNUALE);
        AbstractChart.writeGraph(getHelper(), filter, path1, OreChart.class, p.getCognome() + " " + p.getNome());
        filter.setCadenza(Cadenza.MENSILE);
        AbstractChart.writeGraph(getHelper(), filter, path2, OreChart.class, p.getCognome() + " " + p.getNome());
        filter.setCadenza(Cadenza.SEMPRE);
        AbstractChart.writeGraph(getHelper(), filter, path3, OreChart.class, p.getCognome() + " " + p.getNome());
    }

    public static void testAllocazioneChart() throws CollaborationException {
        String path = "I://tmp/testAllocazione.jpg";
        CollaborationFilter filter = new CollaborationFilter();
        Progetti p = getHelper().find(Progetti.class, new Integer(92));
        filter.setProgetto(p);
        AbstractChart.writeGraph(getHelper(), filter, path, AllocazioneChart.class, p.getAcronimo());
    }

    public static void testProposteProgettiChart() throws CollaborationException {
        String path = "I://tmp/testProgettiProposte.jpg";
        AbstractChart.writeGraph(getHelper(), null, path, ProposteProgettiChart.class, "Proposte progetti");
    }

    public static void testFinanziamentoProgettiChart() throws CollaborationException {
        String path = "I://tmp/finanziamento_progetti.jpg";
        AbstractChart.writeGraph(getHelper(), null, path, FinanziamentoProgettiChart.class, "Finanziamenti annuali dei progetti");
    }

    public static void testTrasfertaChart() throws CollaborationException {
        String path1 = "I://tmp/testAnnuale.jpg";
        String path2 = "I://tmp/testMensile.jpg";
        String path3 = "I://tmp/testSempre.jpg";
        CollaborationFilter filter = new CollaborationFilter();
        Personale p = getHelper().find(Personale.class, new Integer(1));
        filter.setA(p);
        filter.setCadenza(Cadenza.ANNUALE);
        filter.setInizio(DateTime.now());
        AbstractChart.writeGraph(getHelper(), filter, path1, TrasfertaChart.class, p.getCognome() + " " + p.getNome());
        filter.setCadenza(Cadenza.MENSILE);
        filter.setInizio(DateTime.now());
        AbstractChart.writeGraph(getHelper(), filter, path2, TrasfertaChart.class, p.getCognome() + " " + p.getNome());
        filter.setCadenza(Cadenza.SEMPRE);
        AbstractChart.writeGraph(getHelper(), filter, path3, TrasfertaChart.class, p.getCognome() + " " + p.getNome());
    }
}
