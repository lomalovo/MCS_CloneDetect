package net.sourceforge.mords.client.util;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import com.tdcs.lords.obj.Prescriber;
import com.tdcs.lords.obj.Prescription;
import com.tdcs.util.OsActions;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 *
 * @author david
 */
public class RxDocumentMaker {

    public static byte[] createCustomDoc(Prescription rx, double width, double height) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int w = (int) width * 72;
        int h = (int) height * 72;
        Rectangle pageSize = new Rectangle(w, h);
        Document document = new Document(pageSize, 10, 10, 10, 10);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        Paragraph clinic = makeBoldParagraph("CLINIC NAME", 14);
        clinic.setAlignment(Element.ALIGN_CENTER);
        document.add(clinic);
        Paragraph street = makeParagraph("CLINIC ADDRESS", 8);
        street.setAlignment(Element.ALIGN_CENTER);
        document.add(street);
        Paragraph city = makeParagraph("CLINIC STATE/PROVINCE/POSTAL CODE", 8);
        city.setAlignment(Element.ALIGN_CENTER);
        document.add(city);
        Paragraph numbers = makeParagraph("Phone:  PHONE/FAX", 6);
        numbers.setAlignment(Element.ALIGN_CENTER);
        document.add(numbers);
        Paragraph spacer = makeParagraph("\n", 8);
        document.add(spacer);
        Paragraph name = makeBoldParagraph("Name:  " + rx.getLastName() + ", " + rx.getFirstName(), 8);
        Paragraph ssn = makeParagraph("SSN:  " + rx.getSsn(), 8);
        Paragraph phone = makeParagraph("Patient phone:  " + rx.getPhone(), 8);
        String addr = "Address:\n" + rx.getAddress() + "\n" + rx.getCity() + ", " + rx.getState() + "  " + rx.getZip();
        Paragraph ptAddr = makeParagraph(addr, 8);
        Paragraph dateString = makeParagraph("Prescription Date:  " + rx.getDateString(), 8);
        Paragraph doctor = makeBoldParagraph("Physician:  " + rx.getPhysician(), 8);
        Paragraph deanum = makeRedBoldParagraph("DEA Number:  " + rx.getDeaNum(), 8);
        Paragraph serial = makeSerialParagraph("Serial:  " + rx.getKey(), 8);
        Paragraph sigLine = makeBoldParagraph("______________________________________", 8);
        Paragraph diagnosis = makeParagraph("Diagnosis:  " + rx.getDiagnosis(), 8);
        Paragraph stamp = makeBoldParagraph("Valid only with original signature.", 8);
        Table body = new Table(4);
        float[] widths = { 10f, 30f, 50f, 10f };
        body.setWidths(widths);
        body.setWidth(100f);
        body.setAlignment(Element.ALIGN_CENTER);
        body.setBorderWidth(1);
        body.setPadding(1);
        body.addCell(makeBold_Cell("Qty"));
        body.addCell(makeBold_Cell("Medication"));
        body.addCell(makeBold_Cell("Directions"));
        body.addCell(makeBold_Cell("Refills"));
        body.endHeaders();
        body.addCell(makeCenterCell(rx.getQty()));
        body.addCell(makeCenterCell(rx.getMedication()));
        body.addCell(makeCell(rx.getDirections()));
        body.addCell(makeCenterCell(rx.getRefills()));
        document.add(name);
        document.add(ssn);
        document.add(phone);
        document.add(ptAddr);
        document.add(diagnosis);
        document.add(dateString);
        document.add(spacer);
        document.add(body);
        document.add(spacer);
        sigLine.setAlignment(Element.ALIGN_RIGHT);
        serial.setAlignment(Element.ALIGN_RIGHT);
        doctor.setAlignment(Element.ALIGN_RIGHT);
        deanum.setAlignment(Element.ALIGN_RIGHT);
        stamp.setAlignment(Element.ALIGN_CENTER);
        document.add(sigLine);
        document.add(doctor);
        document.add(deanum);
        document.add(serial);
        document.add(stamp);
        document.close();
        return baos.toByteArray();
    }

    public static byte[] createDoc(Prescription rx) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        Paragraph clinic = makeBoldParagraph("CLINIC NAME", 14);
        clinic.setAlignment(Element.ALIGN_CENTER);
        document.add(clinic);
        Paragraph street = makeParagraph("CLINIC ADDRESS", 8);
        street.setAlignment(Element.ALIGN_CENTER);
        document.add(street);
        Paragraph city = makeParagraph("CLINIC CITY/STATE/PROVINCE/POSTAL CODE", 8);
        city.setAlignment(Element.ALIGN_CENTER);
        document.add(city);
        Paragraph numbers = makeParagraph("Phone:  PHONE/FAX", 6);
        numbers.setAlignment(Element.ALIGN_CENTER);
        document.add(numbers);
        Paragraph spacer = makeParagraph("\n\n", 8);
        document.add(spacer);
        Paragraph name = makeBoldParagraph("Name:  " + rx.getLastName() + ", " + rx.getFirstName(), 8);
        Paragraph ssn = makeParagraph("SSN:  " + rx.getSsn(), 8);
        Paragraph phone = makeParagraph("Patient phone:  " + rx.getPhone(), 8);
        String addr = "Address:\n" + rx.getAddress() + "\n" + rx.getCity() + ", " + rx.getState() + "  " + rx.getZip();
        Paragraph ptAddr = makeParagraph(addr, 8);
        Paragraph dateString = makeParagraph("Prescription Date:  " + rx.getDateString(), 8);
        Paragraph doctor = makeBoldParagraph("Physician:  " + rx.getPhysician(), 8);
        Paragraph deanum = makeRedBoldParagraph("DEA Number:  " + rx.getDeaNum(), 8);
        Paragraph serial = makeSerialParagraph("Serial:  " + rx.getKey(), 8);
        Paragraph sigLine = makeBoldParagraph("______________________________________", 10);
        Paragraph diagnosis = makeParagraph("Diagnosis:  " + rx.getDiagnosis(), 8);
        Paragraph stamp = makeBoldParagraph("Valid only with original signature.", 8);
        Table body = new Table(4);
        float[] widths = { 10f, 30f, 50f, 10f };
        body.setWidths(widths);
        body.setWidth(75f);
        body.setAlignment(Element.ALIGN_CENTER);
        body.setBorderWidth(1);
        body.setPadding(1);
        body.addCell(makeBold_Cell("Qty"));
        body.addCell(makeBold_Cell("Medication"));
        body.addCell(makeBold_Cell("Directions"));
        body.addCell(makeBold_Cell("Refills"));
        body.endHeaders();
        body.addCell(makeCenterCell(rx.getQty()));
        body.addCell(makeCenterCell(rx.getMedication()));
        body.addCell(makeCell(rx.getDirections()));
        body.addCell(makeCenterCell(rx.getRefills()));
        document.add(name);
        document.add(ssn);
        document.add(phone);
        document.add(ptAddr);
        document.add(diagnosis);
        document.add(dateString);
        document.add(spacer);
        document.add(body);
        document.add(spacer);
        sigLine.setAlignment(Element.ALIGN_RIGHT);
        serial.setAlignment(Element.ALIGN_RIGHT);
        doctor.setAlignment(Element.ALIGN_RIGHT);
        deanum.setAlignment(Element.ALIGN_RIGHT);
        stamp.setAlignment(Element.ALIGN_CENTER);
        document.add(sigLine);
        document.add(doctor);
        document.add(deanum);
        document.add(serial);
        document.add(spacer);
        document.add(stamp);
        document.close();
        return baos.toByteArray();
    }

    private static Paragraph makeParagraph(String s, int font) {
        return new Paragraph(s, FontFactory.getFont(FontFactory.COURIER, font));
    }

    private static Paragraph makeBoldParagraph(String s, int font) {
        return new Paragraph(s, FontFactory.getFont(FontFactory.COURIER_BOLD, font));
    }

    private static Paragraph makeRedBoldParagraph(String s, int font) {
        return new Paragraph(s, FontFactory.getFont(FontFactory.COURIER, font, Font.BOLD, Color.RED));
    }

    private static Paragraph makeSerialParagraph(String s, int font) {
        return new Paragraph(s, FontFactory.getFont(FontFactory.COURIER, font, Font.BOLD, Color.BLUE));
    }

    private static Cell makeCell(String txt) throws BadElementException {
        Paragraph p = new Paragraph(txt, FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static Cell makeCenterCell(String txt) throws BadElementException {
        Paragraph p = new Paragraph(txt, FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static Cell makeRtAlignCell(String txt) throws BadElementException {
        Paragraph p = new Paragraph(txt, FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static Cell makeRtAlignBoldTopCell(String txt) throws BadElementException {
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8);
        font.setStyle(Font.BOLD);
        Paragraph p = new Paragraph(txt, font);
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.TOP);
        return cell;
    }

    private static Cell makeBoldCell(String txt) throws BadElementException {
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8);
        font.setStyle(Font.BOLD);
        Paragraph p = new Paragraph(txt, font);
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static Cell makeBold_Cell(String txt) throws BadElementException {
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8);
        font.setStyle(Font.BOLD);
        Paragraph p = new Paragraph(txt, font);
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }

    private static Cell makeBoldTopCell(String txt) throws BadElementException {
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
        font.setStyle(Font.BOLD);
        Paragraph p = new Paragraph(txt, font);
        Cell cell = new Cell(p);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(1);
        cell.setRowspan(1);
        cell.setBorder(Rectangle.TOP);
        return cell;
    }

    public static void main(String[] args) {
        try {
            Prescription rx = new Prescription();
            rx.setLastName("Days");
            rx.setFirstName("David");
            rx.setSsn("123-45-6789");
            rx.setPhone("778-1033");
            rx.setAddress("13973 Lyck Run Lyra");
            rx.setCity("South Webster");
            rx.setState("OH");
            rx.setZip("45682");
            rx.setDeaNum("DEANUM12345678");
            rx.setPhysician("DR. WHO");
            rx.setDiagnosis("Acute Gluteal Pain");
            rx.setQty("120");
            rx.setRefills("0");
            rx.setMedication("Tylenol-3");
            rx.setDirections("Take 1 pill four times daily.  Never use with alcohol.  Do not try to operate machinery until you know the affect this medicaiton will have upon your reflexes.");
            rx.setDate(new Date());
            rx.setKey(rx.calculateKey());
            System.out.println(rx.getDateString());
            byte[] data = createDoc(rx);
            FileOutputStream fos = new FileOutputStream("testrx.pdf");
            fos.write(data);
            fos.flush();
            fos.close();
            byte[] smalldata = createCustomDoc(rx, 4.0, 5.0);
            fos = new FileOutputStream("testCustom.pdf");
            fos.write(smalldata);
            fos.flush();
            fos.close();
            OsActions.execute(new File("testrx.pdf"));
            OsActions.execute(new File("testCustom.pdf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
