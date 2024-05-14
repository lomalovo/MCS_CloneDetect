package com.eip.yost.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import com.eip.yost.dto.AffectationDTO;
import com.eip.yost.dto.SouscriptionDTO;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;

public class ReportWriter extends AbstractDocumentWriter {

    public ReportWriter() {
    }

    public String generate(SouscriptionDTO pSouscription, Locale pCurrentLocale) throws Exception {
        SimpleDateFormat vFormat = new SimpleDateFormat("yyyyMMdd");
        String vDirectory = new StringBuilder(FactoryBundle.getInstance().getProperty("root.directory")).append("resources/invoices/").append(pSouscription.getClient().getIdclient()).append("/").toString();
        String vFile = new StringBuilder(vDirectory).append(pSouscription.getFormule().getNom()).append("_").append(vFormat.format(pSouscription.getDateSouscription())).append("-").append(vFormat.format(pSouscription.getDateResiliation())).append(".pdf").toString();
        if (!new File(vDirectory).exists()) {
            new File(vDirectory).mkdirs();
        }
        FileOutputStream buffer = new FileOutputStream(vFile);
        startDocument(buffer, PageSize.A4);
        createAndAddHeader(new StringBuilder(pSouscription.getClient().getPrenom()).append(" ").append(pSouscription.getClient().getNom()).append("\r\n").append(pSouscription.getClient().getEmail()).append("\r\n").append(pSouscription.getClient().getAdresse()).toString(), document);
        if (pCurrentLocale.equals(Locale.ENGLISH)) {
            createAndAddFooter(new StringBuilder(FactoryBundle.getInstance().getProperty("corp.name")).append(" - ").append(getStrDate(new Date())).toString(), document);
            document.open();
            publish(pSouscription);
        } else if (pCurrentLocale.equals(Locale.FRANCE)) {
            createAndAddFooter(new StringBuilder(FactoryBundle.getInstance().getProperty("corp.name")).append(" - Le ").append(getStrDateFr(new Date())).toString(), document);
            document.open();
            publishFr(pSouscription);
        }
        endDocument(document);
        buffer.close();
        return vFile;
    }

    public void publish(SouscriptionDTO pSouscription) throws Exception {
        addImageToContainer(Image.getInstance(FactoryBundle.getInstance().getProperty("pdf.image.header")), document);
        addNewParagraph("Invoice for package " + pSouscription.getFormule().getNom(), fontBigBold, document, new Integer(Element.ALIGN_CENTER));
        GregorianCalendar vSouscription = new GregorianCalendar(pSouscription.getDateSouscription().getYear(), pSouscription.getDateSouscription().getMonth(), pSouscription.getDateSouscription().getDay());
        GregorianCalendar vResiliation = new GregorianCalendar(pSouscription.getDateResiliation().getYear(), pSouscription.getDateResiliation().getMonth(), pSouscription.getDateResiliation().getDay());
        Long vDelai = vResiliation.getTimeInMillis() - vSouscription.getTimeInMillis();
        GregorianCalendar vDifference = new GregorianCalendar();
        vDifference.setTimeInMillis(vDelai);
        Integer vYear = vDifference.get(Calendar.YEAR) - 1970;
        Integer vMonth = vDifference.get(Calendar.MONTH);
        Integer vDiff = ((vMonth > 0 && vMonth < 11 && vYear <= 0)) ? vYear * 12 + vMonth : vYear * 12 + vMonth + 1;
        addNewParagraph("Period from " + getStrDate(pSouscription.getDateSouscription()) + " to " + getStrDate(pSouscription.getDateResiliation()) + " (" + vDiff + " month(s))\r\n\r\n", fontMediumBold, document, new Integer(Element.ALIGN_CENTER));
        PdfPTable t0 = getNewDefaultPTable(2);
        t0.setTotalWidth(new float[] { 75, 25 });
        addCellToTable(t0, "Modules", fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
        addCellToTable(t0, "Price", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        for (AffectationDTO vAffectation : pSouscription.getAffectationList()) {
            addCellToTable(t0, vAffectation.getModule().getNom(), fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
            addCellToTable(t0, " ", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        }
        addCellToTable(t0, "Total", fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
        addCellToTable(t0, (pSouscription.getFormule().getPrix() * vDiff) + "€", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        addTableToContainer(t0, document);
        addNewParagraph("\r\n", fontPlain, document);
    }

    public void publishFr(SouscriptionDTO pSouscription) throws Exception {
        addImageToContainer(Image.getInstance(FactoryBundle.getInstance().getProperty("pdf.image.header")), document);
        addNewParagraph("Facture pour la formule " + pSouscription.getFormule().getNom(), fontBigBold, document, new Integer(Element.ALIGN_CENTER));
        GregorianCalendar vSouscription = new GregorianCalendar(pSouscription.getDateSouscription().getYear(), pSouscription.getDateSouscription().getMonth(), pSouscription.getDateSouscription().getDay());
        GregorianCalendar vResiliation = new GregorianCalendar(pSouscription.getDateResiliation().getYear(), pSouscription.getDateResiliation().getMonth(), pSouscription.getDateResiliation().getDay());
        Long vDelai = vResiliation.getTimeInMillis() - vSouscription.getTimeInMillis();
        GregorianCalendar vDifference = new GregorianCalendar();
        vDifference.setTimeInMillis(vDelai);
        Integer vYear = vDifference.get(Calendar.YEAR) - 1970;
        Integer vMonth = vDifference.get(Calendar.MONTH);
        Integer vDiff = ((vMonth > 0 && vMonth < 11 && vYear <= 0)) ? vYear * 12 + vMonth : vYear * 12 + vMonth + 1;
        addNewParagraph("Période du " + getStrDateFr(pSouscription.getDateSouscription()) + " au " + getStrDateFr(pSouscription.getDateResiliation()) + " (" + vDiff + " mois)\r\n\r\n", fontMediumBold, document, new Integer(Element.ALIGN_CENTER));
        PdfPTable t0 = getNewDefaultPTable(2);
        t0.setTotalWidth(new float[] { 75, 25 });
        addCellToTable(t0, "Modules", fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
        addCellToTable(t0, "Prix", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        for (AffectationDTO vAffectation : pSouscription.getAffectationList()) {
            addCellToTable(t0, vAffectation.getModule().getNom(), fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
            addCellToTable(t0, " ", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        }
        addCellToTable(t0, "Total", fontPlain, new Integer(Element.ALIGN_LEFT), Rectangle.BOX);
        addCellToTable(t0, (pSouscription.getFormule().getPrix() * vDiff) + "€", fontPlain, new Integer(Element.ALIGN_RIGHT), Rectangle.BOX);
        addTableToContainer(t0, document);
        addNewParagraph("\r\n", fontPlain, document);
    }
}
