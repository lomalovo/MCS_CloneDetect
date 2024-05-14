package input_Output;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import language.Messages;
import main.ISettings;
import user_personalInformation.Patient;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * 
 * @author debous
 * @Create 17/03/2011
 * @lastUpdate 02/04/2011
 * 
 * The Create PDF class
 */
public class CreatePDF implements ISettings {

    private Patient patient;

    private Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 25, Font.BOLD);

    private Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);

    private Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);

    private Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, BaseColor.BLUE);

    private Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12);

    private static final String PREFACE_TITLE = Messages.getInstance().getString("CreatePDF.0");

    private static final String PREFACE_NOTE = Messages.getInstance().getString("CreatePDF.1") + " " + Messages.getInstance().getString("CreatePDF.2") + Messages.getInstance().getString("CreatePDF.3") + Messages.getInstance().getString("CreatePDF.4");

    private static final String EXPORT_DATE = Messages.getInstance().getString("CreatePDF.5");

    private static final String PREFACE_FOOTER = Messages.getInstance().getString("CreatePDF.6") + " " + Messages.getInstance().getString("CreatePDF.7") + Messages.getInstance().getString("CreatePDF.8") + " " + Messages.getInstance().getString("CreatePDF.9") + " " + Messages.getInstance().getString("CreatePDF.10") + Messages.getInstance().getString("CreatePDF.11");

    private static final String PERSONAL_INFORMATION_TITLE = Messages.getInstance().getString("CreatePDF.12");

    private static final String PATIENT_FIRSTNAME = Messages.getInstance().getString("CreatePDF.13");

    private static final String PATIENT_LASTNAME = Messages.getInstance().getString("CreatePDF.14");

    private static final String PATIENT_SOCIALSECURITY_NUMBER = Messages.getInstance().getString("CreatePDF.15");

    private static final String PATIENT_BLOOD_GROUP = Messages.getInstance().getString("CreatePDF.16");

    private static final String PATIENT_GENDER = Messages.getInstance().getString("CreatePDF.17");

    private static final String PATIENT_DOB = Messages.getInstance().getString("CreatePDF.18");

    private static final String PATIENT_HEIGHT = Messages.getInstance().getString("CreatePDF.19");

    private static final String PATIENT_WEIGHT = Messages.getInstance().getString("CreatePDF.20");

    private static final String PATIENT_ADDRESS = Messages.getInstance().getString("CreatePDF.21");

    private static final String PATIENT_EMERGENCY_NUMBER = Messages.getInstance().getString("CreatePDF.22");

    private static final String PATIENT_PATHOLOGY_TITLE = Messages.getInstance().getString("CreatePDF.23");

    private static final String PATIENT_TREATMENTS_TITLE = Messages.getInstance().getString("CreatePDF.24");

    private static final String PATIENT_ALLERGIES_TITLE = Messages.getInstance().getString("CreatePDF.25");

    private static final String PATIENT_VACCINES_TITLE = Messages.getInstance().getString("CreatePDF.26");

    private static final String PATIENT_MEDICAL_NOTE_TITLE = Messages.getInstance().getString("CreatePDF.27");

    private static final String[] VACCINES_STATUS = { Messages.getInstance().getString("PatientWindow_vaccinesTab.0"), Messages.getInstance().getString("PatientWindow_vaccinesTab.1"), Messages.getInstance().getString("PatientWindow_vaccinesTab.2") };

    private static final String[] ALLERGIES_STATUS = { Messages.getInstance().getString("PatientWindow_allergiesTab.25"), Messages.getInstance().getString("PatientWindow_allergiesTab.26"), Messages.getInstance().getString("PatientWindow_allergiesTab.27"), Messages.getInstance().getString("PatientWindow_allergiesTab.28"), Messages.getInstance().getString("PatientWindow_allergiesTab.29"), Messages.getInstance().getString("PatientWindow_allergiesTab.30") };

    private static final String[] DIAGNOSTIC_STATUS = { Messages.getInstance().getString("PatientWindow_pathologiesTab.3"), Messages.getInstance().getString("PatientWindow_pathologiesTab.4"), Messages.getInstance().getString("PatientWindow_pathologiesTab.5") };

    /**
	 * Title list
	 */
    private static final String[] TITLE_LIST = { Messages.getInstance().getString("PatientWindow_InfoTab.0"), Messages.getInstance().getString("PatientWindow_InfoTab.1"), Messages.getInstance().getString("PatientWindow_InfoTab.2") };

    /**
	 * Patient Gender
	 */
    private static final String[] SEXE = { Messages.getInstance().getString("PatientWindow_InfoTab.11"), Messages.getInstance().getString("PatientWindow_InfoTab.12") };

    /**
	 * Pathology title
	 */
    private static final String PATHOLOGY_TITLE = Messages.getInstance().getString("CreatePDF.28");

    /**
	 * Diagnostic title
	 */
    private static final String DIAGNOSTIC_TITLE = Messages.getInstance().getString("CreatePDF.29");

    /**
	 * Description title
	 */
    private static final String DESCRIPTION_TITLE = Messages.getInstance().getString("CreatePDF.30");

    /**
	 * Renal pathology title
	 */
    private static final String RENAL_PATHOLOGY = Messages.getInstance().getString("CreatePDF.31");

    /**
	 * Glaucoma pathology title
	 */
    private static final String GLAUCOMA_PATHOLOGY = Messages.getInstance().getString("CreatePDF.32");

    /**
	 * Liver failure pathologye title
	 */
    private static final String LIVERFAILURE_PATHOLOGY = Messages.getInstance().getString("CreatePDF.33");

    /**
	 * Chronic hepatitis pathology title
	 */
    private static final String CHRONICHEPATITIS_PATHOLOGY = Messages.getInstance().getString("CreatePDF.34");

    /**
	 * Epilepsy pathologye title
	 */
    private static final String EPILEPSY_PATHOLOGY = Messages.getInstance().getString("CreatePDF.35");

    /**
	 * Chronic respiratory pathology title
	 */
    private static final String CHRONICRESPIRATORYFAILURE_PATHOLOGY = Messages.getInstance().getString("CreatePDF.36");

    /**
	 * Pepti ucler disease pathology title
	 */
    private static final String PEPTIULCERDISEASE_PATHOLOGY = Messages.getInstance().getString("CreatePDF.37");

    /**
	 * New pathology title
	 */
    private static final String NEW_PATHOLOGY_TITLE = Messages.getInstance().getString("CreatePDF.38");

    /**
	 * Drug name label
	 */
    private static final String DRUG_NAME = Messages.getInstance().getString("CreatePDF.39");

    /**
	 * Morning label
	 */
    private static final String MORNING = Messages.getInstance().getString("CreatePDF.40");

    /**
	 * Noon label
	 */
    private static final String NOON = Messages.getInstance().getString("CreatePDF.41");

    /**
	 * Evening label
	 */
    private static final String EVENING = Messages.getInstance().getString("CreatePDF.42");

    /**
	 * Sleep label
	 */
    private static final String SLEEP = Messages.getInstance().getString("CreatePDF.43");

    /**
	 * Start label
	 */
    private static final String START = Messages.getInstance().getString("CreatePDF.44");

    /**
	 * Duration label
	 */
    private static final String DURATION = Messages.getInstance().getString("CreatePDF.45");

    /**
	 * Allergen title
	 */
    private static final String ALLERGEN_TITLE = Messages.getInstance().getString("CreatePDF.46");

    /**
	 * Sensibility title
	 */
    private static final String SENSIBILITY_TITLE = Messages.getInstance().getString("CreatePDF.47");

    /**
	 * Description title
	 */
    private static final String DESCRIPTIONS_TITLE = Messages.getInstance().getString("CreatePDF.48");

    /**
	 * Antibiotics title
	 */
    private static final String ANTIBIOTICS_TITLE = Messages.getInstance().getString("CreatePDF.49");

    /**
	 * AINS title
	 */
    private static final String AINS_TITLE = Messages.getInstance().getString("CreatePDF.50");

    /**
	 * Paracetamol title
	 */
    private static final String PARACETAMOL_TITLE = Messages.getInstance().getString("CreatePDF.51");

    /**
	 * Sulfamide title
	 */
    private static final String SULFAMIDES_TITLE = Messages.getInstance().getString("CreatePDF.52");

    /**
	 * Morphiniue title
	 */
    private static final String MORPHINIQUE_TITLE = Messages.getInstance().getString("CreatePDF.53");

    /**
	 * Aspirine title
	 */
    private static final String ASPIRINE_TITLE = Messages.getInstance().getString("CreatePDF.54");

    /**
	 * Latex title
	 */
    private static final String LATEX_TITLE = Messages.getInstance().getString("CreatePDF.55");

    /**
	 * Iodine title
	 */
    private static final String IODINE_TITLE = Messages.getInstance().getString("CreatePDF.56");

    /**
	 * New Allergy title
	 */
    private static final String NEW_ALLERGY_TITLE = Messages.getInstance().getString("CreatePDF.57");

    /**
	 * Strain title
	 */
    private static final String STRAIN_TITLE = Messages.getInstance().getString("CreatePDF.58");

    /**
	 * Strain status title
	 */
    private static final String STATUS_TITLE = Messages.getInstance().getString("CreatePDF.59");

    /**
	 * Date title
	 */
    private static final String DATE_TITLE = Messages.getInstance().getString("CreatePDF.60");

    /**
	 * BCG title
	 */
    private static final String BCG_TITLE = Messages.getInstance().getString("CreatePDF.61");

    /**
	 * DT title
	 */
    private static final String DT_TITLE = Messages.getInstance().getString("CreatePDF.62");

    /**
	 * Papillomavirus title
	 */
    private static final String PAPILLOMAVIRUS_TITLE = Messages.getInstance().getString("CreatePDF.63");

    /**
	 * Pertussis title
	 */
    private static final String PERTUSSIS_TITLE = Messages.getInstance().getString("CreatePDF.64");

    /**
	 * HIB title
	 */
    private static final String HIB_TITLE = Messages.getInstance().getString("CreatePDF.65");

    /**
	 * Influenza title
	 */
    private static final String INFLUENZA_TITLE = Messages.getInstance().getString("CreatePDF.66");

    /**
	 * Pneumococcus title
	 */
    private static final String PNEUMOCOCCUS_TITLE = Messages.getInstance().getString("CreatePDF.67");

    /**
	 * Hepatitits B title
	 */
    private static final String HEPATITIS_B_TITLE = Messages.getInstance().getString("CreatePDF.68");

    /**
	 * Diphteria title
	 */
    private static final String ROR_TITLE = Messages.getInstance().getString("CreatePDF.69");

    /**
	 * Typhoid title
	 */
    private static final String TYPHOID_TITLE = Messages.getInstance().getString("CreatePDF.70");

    private static final String MEDICAL_CERTIFICATION = Messages.getInstance().getString("CreatePDF.76");

    /**
	 * Constructor
	 * @param patient The patient
	 * @param path The PDF file path
	 */
    public CreatePDF(Patient patient, File path) {
        this.patient = patient;
        try {
            Document document = new Document(PageSize.A4, 20, 20, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            addMetaData(document);
            addTitlePage(document);
            addContent(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMetaData(Document document) {
        document.addTitle(PREFACE_TITLE + patient.getPersonFirstName() + patient.getPersonLastName().toUpperCase());
        document.addSubject(PREFACE_TITLE);
        document.addAuthor(Messages.getInstance().getString("CreatePDF.71"));
        document.addCreator(Messages.getInstance().getString("CreatePDF.72"));
        document.getPageNumber();
    }

    private void addTitlePage(Document document) throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(PREFACE_TITLE, titleFont));
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(TITLE_LIST[Integer.parseInt(patient.getPatientTitle())] + " " + patient.getPersonFirstName() + " " + patient.getPersonLastName().toUpperCase(), catFont));
        addEmptyLine(preface, 3);
        preface.add(new Paragraph(PREFACE_NOTE, redFont));
        preface.add(new Paragraph(EXPORT_DATE + new Date(), smallBold));
        addEmptyLine(preface, 8);
        preface.add(new Paragraph(PREFACE_FOOTER, redFont));
        document.add(preface);
        document.newPage();
    }

    private void addContent(Document document) throws DocumentException {
        Anchor chap1 = new Anchor(PERSONAL_INFORMATION_TITLE, catFont);
        chap1.setName(PERSONAL_INFORMATION_TITLE);
        Paragraph subPara = new Paragraph(PATIENT_FIRSTNAME, subFont);
        Chapter catPart = new Chapter(new Paragraph(chap1), 1);
        Section subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPersonFirstName(), smallBold));
        subPara = new Paragraph(PATIENT_LASTNAME, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPersonLastName(), smallBold));
        subPara = new Paragraph(PATIENT_SOCIALSECURITY_NUMBER, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getSocialSecurityNumber(), smallBold));
        subPara = new Paragraph(PATIENT_BLOOD_GROUP, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatientBloodGroup(), smallBold));
        subPara = new Paragraph(PATIENT_GENDER, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(SEXE[Integer.parseInt(patient.getPatientSexe())], smallBold));
        subPara = new Paragraph(PATIENT_DOB, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatienttDateOfBirth(), smallBold));
        subPara = new Paragraph(PATIENT_HEIGHT, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatientHeight(), smallBold));
        subPara = new Paragraph(PATIENT_WEIGHT, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatientWeight(), smallBold));
        subPara = new Paragraph(PATIENT_ADDRESS, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatientAddress(), smallBold));
        subPara = new Paragraph(PATIENT_EMERGENCY_NUMBER, subFont);
        subCatPart = catPart.addSection(subPara);
        subCatPart.add(new Paragraph(patient.getPatientEmergencyPhoneNumber(), smallBold));
        document.add(catPart);
        Anchor chap2 = new Anchor(PATIENT_PATHOLOGY_TITLE, catFont);
        chap2.setName(PATIENT_PATHOLOGY_TITLE);
        catPart = new Chapter(new Paragraph(chap2), 2);
        subPara = new Paragraph();
        Section subCatPart2 = catPart.addSection(subPara);
        createPatholgiesTable(subCatPart2);
        document.add(catPart);
        Anchor chap3 = new Anchor(PATIENT_TREATMENTS_TITLE, catFont);
        chap3.setName(PATIENT_TREATMENTS_TITLE);
        catPart = new Chapter(new Paragraph(chap3), 3);
        subPara = new Paragraph();
        Section subCatPart3 = catPart.addSection(subPara);
        createTreatmentsTable(subCatPart3);
        document.add(catPart);
        Anchor chap4 = new Anchor(PATIENT_ALLERGIES_TITLE, catFont);
        chap4.setName(PATIENT_ALLERGIES_TITLE);
        catPart = new Chapter(new Paragraph(chap4), 4);
        subPara = new Paragraph();
        Section subCatPart4 = catPart.addSection(subPara);
        createAllergiesTable(subCatPart4);
        document.add(catPart);
        Anchor chap5 = new Anchor(PATIENT_VACCINES_TITLE, catFont);
        chap5.setName(PATIENT_VACCINES_TITLE);
        catPart = new Chapter(new Paragraph(chap5), 5);
        subPara = new Paragraph();
        Section subCatPart5 = catPart.addSection(subPara);
        createVaccinesTable(subCatPart5);
        document.add(catPart);
        Anchor chap6 = new Anchor(PATIENT_MEDICAL_NOTE_TITLE, catFont);
        chap6.setName(PATIENT_MEDICAL_NOTE_TITLE);
        catPart = new Chapter(new Paragraph(chap6), 6);
        subPara = new Paragraph();
        Section subCatPart6 = catPart.addSection(subPara);
        subCatPart6.add(new Paragraph(patient.getAllPatientMedicalNote(), smallBold));
        Anchor chap7 = new Anchor(MEDICAL_CERTIFICATION, catFont);
        chap7.setName(MEDICAL_CERTIFICATION);
        Paragraph subPara7 = new Paragraph(MEDICAL_CERTIFICATION, subFont);
        Chapter catPart7 = new Chapter(new Paragraph(chap7), 7);
        Section subCatPart7 = catPart7.addSection(subPara7);
        subCatPart7.add(new Paragraph(Messages.getInstance().getString("CreatePDF.73") + " " + TITLE_LIST[Integer.parseInt(patient.getPatientTitle())] + " " + patient.getPersonFirstName() + " " + patient.getPersonLastName().toUpperCase() + " " + Messages.getInstance().getString("CreatePDF.74") + Messages.getInstance().getString("CreatePDF.75"), smallBold));
        document.add(catPart7);
    }

    private void createPatholgiesTable(Section subCatPart) throws BadElementException {
        PdfPTable table = new PdfPTable(3);
        PdfPCell c1 = new PdfPCell(new Phrase(PATHOLOGY_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(DIAGNOSTIC_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(DESCRIPTION_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);
        table.addCell(new Phrase(RENAL_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(0).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(0).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(GLAUCOMA_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(1).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(1).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(LIVERFAILURE_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(2).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(2).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(CHRONICHEPATITIS_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(3).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(3).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(EPILEPSY_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(4).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(4).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(CHRONICRESPIRATORYFAILURE_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(5).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(5).split(SPLIT_CHARACTER)[1])]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(PEPTIULCERDISEASE_PATHOLOGY, subFont));
        table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(6).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientPathologies().get(6).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        if (!patient.getAllPatientPathologies().get(7).split(SPLIT_CHARACTER)[0].equals(NEW_PATHOLOGY_TITLE)) {
            table.addCell(new Phrase(patient.getAllPatientPathologies().get(7).split(SPLIT_CHARACTER)[0] + ": ", subFont));
            table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(7).split(SPLIT_CHARACTER)[1])]);
            try {
                table.addCell(patient.getAllPatientPathologies().get(7).split(SPLIT_CHARACTER)[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                table.addCell("");
            }
        }
        if (!patient.getAllPatientPathologies().get(8).split(SPLIT_CHARACTER)[0].equals(NEW_PATHOLOGY_TITLE)) {
            table.addCell(new Phrase(patient.getAllPatientPathologies().get(8).split(SPLIT_CHARACTER)[0] + ": ", subFont));
            table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(8).split(SPLIT_CHARACTER)[1])]);
            try {
                table.addCell(patient.getAllPatientPathologies().get(8).split(SPLIT_CHARACTER)[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                table.addCell("");
            }
        }
        if (!patient.getAllPatientPathologies().get(9).split(SPLIT_CHARACTER)[0].equals(NEW_PATHOLOGY_TITLE)) {
            table.addCell(new Phrase(patient.getAllPatientPathologies().get(9).split(SPLIT_CHARACTER)[0] + ": ", subFont));
            table.addCell(DIAGNOSTIC_STATUS[Integer.parseInt(patient.getAllPatientPathologies().get(9).split(SPLIT_CHARACTER)[1])]);
            try {
                table.addCell(patient.getAllPatientPathologies().get(9).split(SPLIT_CHARACTER)[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                table.addCell("");
            }
        }
        subCatPart.add(table);
    }

    private void createTreatmentsTable(Section subCatPart) throws BadElementException {
        PdfPTable table = new PdfPTable(7);
        PdfPCell c1 = new PdfPCell(new Phrase(DRUG_NAME, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(MORNING, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(NOON, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(EVENING, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(SLEEP, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(START, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(DURATION, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);
        for (int i = 0; i < patient.getAllPatientTreatments().size(); ++i) {
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[0]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[1]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[2]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[3]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[4]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[5]);
            table.addCell(patient.getAllPatientTreatments().get(i).split(SPLIT_CHARACTER)[6]);
        }
        subCatPart.add(table);
    }

    private void createAllergiesTable(Section subCatPart) throws BadElementException {
        PdfPTable table = new PdfPTable(3);
        PdfPCell c1 = new PdfPCell(new Phrase(ALLERGEN_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(SENSIBILITY_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(DESCRIPTIONS_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);
        table.addCell(new Phrase(ANTIBIOTICS_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(0).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(0).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(AINS_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(1).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(1).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(PARACETAMOL_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(2).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(2).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(SULFAMIDES_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(3).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(3).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(MORPHINIQUE_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(4).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(4).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(ASPIRINE_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(5).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(5).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(LATEX_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(6).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(6).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(IODINE_TITLE, subFont));
        table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(7).split(SPLIT_CHARACTER)[0])]);
        try {
            table.addCell(patient.getAllPatientAllergies().get(7).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        if (!patient.getAllPatientAllergies().get(8).split(SPLIT_CHARACTER)[0].equals(NEW_ALLERGY_TITLE)) {
            table.addCell(new Phrase(patient.getAllPatientAllergies().get(8).split(SPLIT_CHARACTER)[0] + ": ", subFont));
            table.addCell(ALLERGIES_STATUS[Integer.parseInt(patient.getAllPatientAllergies().get(8).split(SPLIT_CHARACTER)[1])]);
            try {
                table.addCell(patient.getAllPatientAllergies().get(8).split(SPLIT_CHARACTER)[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                table.addCell("");
            }
        }
        subCatPart.add(table);
    }

    private void createVaccinesTable(Section subCatPart) throws BadElementException {
        PdfPTable table = new PdfPTable(3);
        PdfPCell c1 = new PdfPCell(new Phrase(STRAIN_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(STATUS_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        c1 = new PdfPCell(new Phrase(DATE_TITLE, subFont));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);
        table.addCell(new Phrase(BCG_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(0).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(0).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(DT_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(1).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(1).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(PAPILLOMAVIRUS_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(2).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(2).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(PERTUSSIS_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(3).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(3).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(TYPHOID_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(4).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(4).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(ROR_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(5).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(5).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(HIB_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(6).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(6).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(HEPATITIS_B_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(7).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(7).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(PNEUMOCOCCUS_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(8).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(8).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        table.addCell(new Phrase(INFLUENZA_TITLE, subFont));
        try {
            table.addCell(VACCINES_STATUS[Integer.parseInt(patient.getAllPatientVaccines().get(9).split(SPLIT_CHARACTER)[0])]);
            table.addCell(patient.getAllPatientVaccines().get(9).split(SPLIT_CHARACTER)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            table.addCell("");
        }
        subCatPart.add(table);
    }

    /**
	 * Add empty line to a paragraph
	 * @param paragraph the paragraph to add an empty line
	 * @param number the number of empty line
	 */
    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
