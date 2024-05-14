package org.in4ama.documentengine;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FopFactory;
import org.apache.log4j.Logger;
import org.in4ama.datasourcemanager.cfg.DataSetConfiguration;
import org.in4ama.datasourcemanager.util.DocumentHelper;
import org.in4ama.documentautomator.AcroFieldBinding;
import org.in4ama.documentengine.cfg.ProjectConfigMgr;
import org.in4ama.documentengine.evaluator.EvaluableContent;
import org.in4ama.documentengine.evaluator.EvaluationContext;
import org.in4ama.documentengine.evaluator.Evaluator;
import org.in4ama.documentengine.exception.EvaluationException;
import org.in4ama.documentengine.generator.GeneratorContext;
import org.in4ama.documentengine.util.Concatenator;
import org.in4ama.documentengine.util.FOUtils;
import org.in4ama.documentengine.util.IntermediateConverter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.Barcode;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.BarcodeCodabar;
import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.BarcodeInter25;
import com.lowagie.text.pdf.BarcodePostnet;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

/** A class for manipulating documents */
public class DocumentEngine {

    /** Logger */
    private static Logger log = Logger.getLogger(DocumentEngine.class);

    /** Default relative path to the FOP configuration file */
    private static final String DEFAULT_FOP_CONFIG_FILE = "fopconfig" + File.separator + "fop.xml";

    /** Prefix of the relative path to the FOP configuration file */
    private String fopConfigPath = "";

    /** Evaluator object used by this document engine */
    private Evaluator evaluator;

    private static final String[] CHECKBOX_ON_VALUES = { "on", "true", "yes", "1" };

    private static final String[] CHECKBOX_OFF_VALUES = { "off", "false", "no", "0" };

    private static final String CHECKBOX_DEFAULT_ON = "Yes";

    private static final String CHECKBOX_DEFAULT_OFF = "Off";

    public void setFopConfigPath(String fopConfigPath) {
        this.fopConfigPath = (fopConfigPath != null) ? fopConfigPath : "";
    }

    public String getFopConfigPath() {
        return fopConfigPath;
    }

    /**
	 * Sets the evaluator object which should be used by this document engine.
	 * 
	 * @param evaluator
	 *            the Evaluator object.
	 */
    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
	 * Gets the evaluator object used by the document engine.;
	 * 
	 * @return the Evaluator object.
	 */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
	 * Gets a helper array indicating which pages should receive a background.
	 * 
	 * @param pages
	 *            array containing numbers of pages on which to include the
	 *            background.
	 * @param numPages
	 *            the number of pages.
	 * @return array of booleans specifying the pages with background.
	 */
    private static boolean[] getIncludeBackground(int[] pages, int numPages) {
        boolean[] bt = new boolean[numPages];
        boolean all = (pages == null) || (pages.length == 0);
        for (int i = 0; i < numPages; i++) bt[i] = all;
        if (!all) for (int i = 0; i < pages.length; i++) bt[pages[i]] = true;
        return bt;
    }

    /**
	 * Adds a background to the specified PDF document.
	 * 
	 * @param mainDocument
	 *            the PDF document which is to "receive" the background.
	 * @param backgroundDocument
	 *            the PDF document containing a background.
	 * @return a PDF document with included background.
	 */
    public InputStream addBackground(InputStream mainDocument, InputStream backgroundDocument) throws Exception {
        return addBackground(mainDocument, backgroundDocument, null);
    }

    /**
	 * Adds a background to the specified PDF document.
	 * 
	 * @param mainDocument
	 *            the PDF document which is to "receive" the background.
	 * @param backgroundDocument
	 *            the PDF document containing a background.
	 * @param pages
	 *            numbers of pages which should "receive" the background, null
	 *            means that the background should be added to each page.
	 * @return a PDF document with included background.
	 */
    public InputStream addBackground(InputStream mainDocument, InputStream backgroundDocument, int[] pages) throws Exception {
        log.debug("Adding a background.");
        PdfReader mainReader = new PdfReader(mainDocument);
        PdfReader backgroundReader = new PdfReader(backgroundDocument);
        com.lowagie.text.Document document = new com.lowagie.text.Document(backgroundReader.getPageSizeWithRotation(1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfImportedPage backgroundPage = writer.getImportedPage(backgroundReader, 1);
        int numMainPages = mainReader.getNumberOfPages();
        boolean[] include = getIncludeBackground(pages, numMainPages);
        for (int i = 1; i <= numMainPages; i++) {
            document.newPage();
            if (include[i - 1]) cb.addTemplate(backgroundPage, 0, 0);
            PdfImportedPage mainPage = writer.getImportedPage(mainReader, i);
            cb.addTemplate(mainPage, 0, 0);
        }
        document.close();
        backgroundReader.close();
        mainReader.close();
        out.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
	 * Fill the ACROFIELDS with relevant data in the specified PDF document.
	 * 
	 * @param documentInput
	 *            the PDF document's content.
	 * @return the InputStream "containing" the required PDF document.
	 */
    public InputStream createDocumentFromPdf(InputStream documentInput, Map<String, AcroFieldBinding> fieldBindings, EvaluationContext job, boolean flatten) throws Exception {
        log.debug("Creating a PDF acrofield document.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(documentInput);
        PdfStamper stamper = new PdfStamper(reader, out);
        Set<String> fieldNames = fieldBindings.keySet();
        for (String fieldName : fieldNames) {
            log.debug("Evaluating an acrofield: " + fieldName);
            AcroFieldBinding binding = fieldBindings.get(fieldName);
            if (binding == null) continue;
            handleBinding(job, binding, stamper);
        }
        stamper.setFormFlattening(flatten);
        stamper.close();
        reader.close();
        out.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static Barcode getBarcode(String type) {
        Barcode code = null;
        if ("EAN-13".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.EAN13);
        } else if ("EAN-8".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.EAN8);
        } else if ("UPC-A".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.UPCA);
        } else if ("UPC-E".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.UPCE);
        } else if ("supplemental 2".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.SUPP2);
        } else if ("supplemental 5".equals(type)) {
            code = new BarcodeEAN();
            code.setCodeType(Barcode.SUPP5);
        } else if ("Code 128".equals(type)) {
            code = new Barcode128();
            code.setCodeType(Barcode.CODE128);
        } else if ("Code 128 raw".equals(type)) {
            code = new Barcode128();
            code.setCodeType(Barcode.CODE128_RAW);
        } else if ("UCC/EAN-128".equals(type)) {
            code = new Barcode128();
            code.setCodeType(Barcode.CODE128_UCC);
        } else if ("Code 39".equals(type)) {
            code = new Barcode39();
            code.setExtended(false);
        } else if ("Code 39 extended".equals(type)) {
            code = new Barcode39();
            code.setExtended(false);
        } else if ("Codabar".equals(type)) {
            code = new BarcodeCodabar();
        } else if ("Interleaved 2".equals(type)) {
            code = new BarcodeInter25();
            code.setN(2);
        } else if ("Interleaved 5".equals(type)) {
            code = new BarcodeInter25();
            code.setN(5);
        } else if ("Postnet".equals(type)) {
            code = new BarcodePostnet();
        }
        return code;
    }

    /**
	 * Processes the specified barcode binding.
	 * 
	 * @param fieldValue
	 *            the value from which the barcode is to be generated.
	 * @param binding
	 *            object describing the barcode properties.
	 * @param stamper
	 *            PdfStamper object.
	 * @throws Exception
	 */
    private void handleBindingBarcode(EvaluationContext job, AcroFieldBinding binding, PdfStamper stamper) throws Exception {
        try {
            String fieldValue = evaluator.evaluate((String) binding.getValue(), job);
            log.debug("Binding acrofield (barcode) '" + binding.getName() + "' to value: '" + fieldValue + "'.");
            AcroFields acroFields = stamper.getAcroFields();
            String fieldName = binding.getName();
            float[] positions = acroFields.getFieldPositions(fieldName);
            int pageNr = (int) positions[0];
            Rectangle rect = new Rectangle(positions[1], positions[2], positions[3], positions[4]);
            String type = (String) binding.getProperty(TYPE);
            Barcode code = getBarcode(type);
            if (code == null) {
                String msg = "Unknown barcode type: " + type + ".";
                log.error(msg);
                throw new EvaluationException(msg);
            }
            boolean hasGuardBars = Boolean.valueOf((String) binding.getProperty(GUARD_LINES));
            code.setGuardBars(hasGuardBars);
            code.setBarHeight(rect.getHeight());
            code.setX(rect.getWidth() - 10);
            try {
                code.setCode(fieldValue);
            } catch (Exception ex) {
                String msg = "Barcode type '" + type + "' doesn't support the format of '" + fieldValue + "' value.";
                log.error(msg);
                throw new EvaluationException(msg);
            }
            Color backgroundColor = Color.WHITE;
            Color foregroundColor = Color.BLACK;
            String rgbS = null;
            try {
                rgbS = (String) binding.getProperty(BACKGROUND_COLOR);
                backgroundColor = new Color(Integer.parseInt(rgbS));
            } catch (Exception ex) {
                log.warn("Unable to create the barcode's background color from the RGB value: " + rgbS);
            }
            try {
                rgbS = (String) binding.getProperty(FOREGROUND_COLOR);
                foregroundColor = new Color(Integer.parseInt(rgbS));
            } catch (Exception ex) {
                log.warn("Unable to create the barcode's foreground color from the RGB value: " + rgbS);
            }
            java.awt.Image im = code.createAwtImage(foregroundColor, backgroundColor);
            Image image = Image.getInstance(im, null, false);
            String rotS = (String) binding.getProperty(ROTATION);
            if (rotS != null) {
                float deg = (float) getDeg(rotS);
                image.setRotationDegrees(deg);
            }
            image.scaleToFit(rect.getWidth(), rect.getHeight());
            float posX = positions[1] + (rect.getWidth() - image.getScaledWidth()) / 2;
            float posY = positions[2] + (rect.getHeight() - image.getScaledHeight()) / 2;
            image.setAbsolutePosition(posX, posY);
            acroFields.removeField(fieldName);
            PdfContentByte cb = stamper.getOverContent(pageNr);
            cb.addImage(image);
        } catch (Exception ex) {
            log.error("Unable to create a barcode: " + binding.getName());
        }
    }

    /**
	 * Processes the specified text binding.
	 * 
	 * @param fieldValue
	 *            the value to be shown.
	 * @param binding
	 *            the object describing the text properties.
	 * @param stamper
	 *            PdfStamper object.
	 * @throws Exception.
	 */
    private void handleBindingText(EvaluationContext job, AcroFieldBinding binding, PdfStamper stamper) throws Exception {
        String fieldValue = evaluator.evaluate((String) binding.getValue(), job);
        log.debug("Binding acrofield (text) '" + binding.getName() + "' to value: '" + fieldValue + "'.");
        stamper.getAcroFields().setField(binding.getName(), fieldValue);
    }

    /**
	 * Processes the specified checkbox binding.
	 * 
	 * @param fieldValue
	 *            the value indicating whether the checkbox should be enabled or
	 *            not.
	 * @param binding
	 *            the object describing the checkbox properties.
	 * @param stamper
	 *            PdfStamper object.
	 * @throws Exception
	 */
    private void handleBindingCheckBox(EvaluationContext job, AcroFieldBinding binding, PdfStamper stamper) throws Exception {
        String fieldValue = evaluator.evaluate((String) binding.getValue(), job);
        log.debug("Binding acrofield (checkbox) '" + binding.getName() + "' to value: '" + fieldValue + "'.");
        AcroFields acroFields = stamper.getAcroFields();
        String onValue = CHECKBOX_DEFAULT_ON, offValue = CHECKBOX_DEFAULT_OFF;
        String[] states = acroFields.getAppearanceStates(binding.getName());
        boolean found = false;
        for (int i = 0; i < CHECKBOX_ON_VALUES.length && !found; i++) {
            for (int j = 0; j < states.length && !found; j++) {
                if (CHECKBOX_ON_VALUES[i].equalsIgnoreCase(states[j])) {
                    onValue = states[j];
                    found = true;
                }
            }
        }
        found = false;
        for (int i = 0; i < CHECKBOX_OFF_VALUES.length && !found; i++) {
            for (int j = 0; j < states.length && !found; j++) {
                if (CHECKBOX_OFF_VALUES[i].equalsIgnoreCase(states[j])) {
                    offValue = states[j];
                    found = true;
                }
            }
        }
        fieldValue = fieldValue.trim();
        if (fieldValue.equalsIgnoreCase("true") || fieldValue.equalsIgnoreCase("on") || fieldValue.equalsIgnoreCase("yes")) fieldValue = onValue; else fieldValue = offValue;
        acroFields.setField(binding.getName(), fieldValue);
    }

    private static final String TYPE = "type";

    private static final String SHOW_TEXT = "show text";

    private static final String FOREGROUND_COLOR = "foreground color";

    private static final String BACKGROUND_COLOR = "background color";

    private static final String ROTATION = "rotation";

    private static final String TEXT_LOCATION = "text location";

    private static final String TEXT_GAP = "text gap";

    private static final String GUARD_LINES = "guard lines";

    /**
	 * Tries to convert a specified object an InputStream.
	 * @param obj an object to be converted
	 * @return an InputStream object.
	 */
    private static InputStream toInputStream(Object obj) {
        InputStream in = null;
        if (obj instanceof Blob) {
            try {
                in = ((Blob) obj).getBinaryStream();
            } catch (Exception ex) {
                String msg = "Cannot convert retrieve the data from a Blob object.";
                throw new EvaluationException(msg, ex);
            }
        } else if (obj instanceof byte[]) {
            in = new ByteArrayInputStream((byte[]) obj);
        }
        return in;
    }

    /**
	 * Processes the image binding.
	 * 
	 * @param fieldValue
	 			  the value specifying the name of an image.
	 * @param binding
				  the object describing the image properties.
	 * @param stamper
	 *            PdfStamper object.
	 * @throws Exception
	 */
    private void handleBindingImage(EvaluationContext job, AcroFieldBinding binding, PdfStamper stamper) throws Exception {
        EvaluableContent evalContent = evaluator.getEvaluableContent((String) binding.getValue(), job);
        Image image = null;
        if ((evalContent.size() >= 2) && !(evalContent.getEvaluationUnit(1).getValue() instanceof String)) {
            InputStream in = toInputStream(evalContent.getEvaluationUnit(1).getValue());
            if (in == null) {
                String msg = "Unable to retrieve the binary content of an image.";
                throw new EvaluationException(msg);
            }
            image = Image.getInstance(ImageIO.read(in), null);
        } else {
            String fieldValue = evalContent.getAsText();
            image = Image.getInstance(fieldValue);
        }
        if (image == null) return;
        AcroFields acroFields = stamper.getAcroFields();
        String fieldName = binding.getName();
        float[] positions = acroFields.getFieldPositions(fieldName);
        int pageNr = (int) positions[0];
        Rectangle rect = new Rectangle(positions[1], positions[2], positions[3], positions[4]);
        String rotS = (String) binding.getProperty(ROTATION);
        if (rotS != null) {
            float deg = (float) getDeg(rotS);
            image.setRotationDegrees(deg);
        }
        image.scaleToFit(rect.getWidth(), rect.getHeight());
        float posX = positions[1] + (rect.getWidth() - image.getScaledWidth()) / 2;
        float posY = positions[2] + (rect.getHeight() - image.getScaledHeight()) / 2;
        image.setAbsolutePosition(posX, posY);
        acroFields.removeField(fieldName);
        PdfContentByte cb = stamper.getOverContent(pageNr);
        cb.addImage(image);
    }

    /**
	 * Binds the specified value to the specified field.
	 * 
	 * @param fieldValue
	 *            the value of the field
	 * @param binding
	 *            AcroFieldBinding object defining the binding properties.
	 * @param stamper
	 *            PdStamper to be used.
	 */
    private void handleBinding(EvaluationContext job, AcroFieldBinding binding, PdfStamper stamper) throws Exception {
        switch(binding.getType()) {
            case AcroFieldBinding.BINDING_TEXT:
                handleBindingText(job, binding, stamper);
                break;
            case AcroFieldBinding.BINDING_IMAGE:
                handleBindingImage(job, binding, stamper);
                break;
            case AcroFieldBinding.BINDING_BARCODE:
                handleBindingBarcode(job, binding, stamper);
                break;
            case AcroFieldBinding.BINDING_CHECKBOX:
                handleBindingCheckBox(job, binding, stamper);
                break;
        }
    }

    /**
	 * Evaluates the given expression in the context of the specified document
	 * generation job.
	 * 
	 * @param condition
	 *            the expression to be evaluated.
	 * @param job
	 *            DocumentGenerationJob object.
	 * @return true or false
	 */
    private boolean includeFragment(String condition, EvaluationContext job) {
        if ((condition == null) || (condition.length() == 0)) return true;
        Object ret = evaluator.evaluate(condition, job);
        if (ret instanceof Boolean) return ((Boolean) ret).booleanValue();
        if (ret instanceof String) return !"false".equalsIgnoreCase(((String) ret).trim());
        return true;
    }

    /**
	 * Creates a PDF document from the specified fragments. The content of the
	 * fragment list may be changed.
	 * 
	 * @param fragments
	 *            list of Strings containing the fragments.
	 * @param conditions
	 *            list of conditions indicating which fragments should be
	 *            included.
	 * @param job
	 *            DocumentGenerationJob object.
	 * @return the InputStream containing the generated PDF document.
	 */
    public InputStream createDocumentFromFop(List<String> fragments, List<String> conditions, EvaluationContext job) throws Exception {
        log.debug("Creating a PDF document from FOP fragments");
        InputStream in = null;
        try {
            List<String> evalFragments = new ArrayList<String>();
            int numFragments = fragments.size();
            for (int i = 0; i < numFragments; i++) if ((conditions == null) || includeFragment(conditions.get(i), job)) evalFragments.add(evaluator.evaluateFop(fragments.get(i), job));
            fragments = null;
            numFragments = evalFragments.size();
            FopFactory fopFactory = FopFactory.newInstance();
            String realPath = fopConfigPath + DEFAULT_FOP_CONFIG_FILE;
            File fopConfigFile = new File(realPath);
            fopFactory.setUserConfig(fopConfigFile);
            String baseUrl = fopConfigPath.replace(" ", "%20");
            if (!baseUrl.startsWith("file:")) {
                baseUrl = "file:" + baseUrl;
            }
            ProjectConfigMgr cfgMgr = GeneratorContext.getInstance().getProjectConfigurationManager();
            String projectURL = cfgMgr.getProjectPath();
            fopFactory.setBaseURL("file:/" + projectURL.replace('\\', '/'));
            fopFactory.setFontBaseURL(baseUrl);
            for (int i = 0; i < numFragments; i++) {
                InputSource inSrc = new InputSource(new ByteArrayInputStream(evalFragments.get(i).getBytes()));
                SAXSource src = new SAXSource(inSrc);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IntermediateConverter.convertToIntermediate(fopFactory, src, null, os);
                evalFragments.set(i, os.toString());
            }
            Document document = FOUtils.mergeFoDocs(evalFragments);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DocumentHelper.outputXML(document, os);
            StreamSource streamSource = new StreamSource(new ByteArrayInputStream(os.toByteArray()));
            StreamSource[] streamSourcePages = new StreamSource[] { streamSource };
            os = new ByteArrayOutputStream();
            Concatenator.concatToPDF(fopFactory, streamSourcePages, os);
            in = new ByteArrayInputStream(os.toByteArray());
        } catch (Exception ex) {
            String msg = "Error while creating a PDF document.";
            throw new EvaluationException(msg, ex);
        }
        return in;
    }

    /** Retrieves parameter names from the specified XHTML */
    public Set<String> getParametersFromXHtml(String content) {
        Set<String> paramSet = new HashSet<String>();
        paramSet.addAll(evaluator.getParameters(content));
        return paramSet;
    }

    /** Retrieve the parameter names from the specified list of FOP fragments */
    public Set<String> getParametersFromFop(List<String> fragments) {
        Set<String> paramSet = new HashSet<String>();
        for (String fragment : fragments) paramSet.addAll(evaluator.getParameters(fragment));
        return paramSet;
    }

    /**
	 * Retrieves the parameter names from the specified list of
	 * DataSetConfiguration objects
	 */
    public Set<String> getParametersFromDataSetConfig(Collection<DataSetConfiguration> dataSetConfigList) {
        Set<String> paramSet = new HashSet<String>();
        for (DataSetConfiguration dataSetConfig : dataSetConfigList) paramSet.addAll(evaluator.getParameters(dataSetConfig));
        return paramSet;
    }

    /** Retrieves the parameter names from the specified ACROFIELD bindings */
    public Set<String> getParametersFromBinding(Map<String, AcroFieldBinding> binding) {
        Set<String> paramSet = new HashSet<String>();
        Collection<AcroFieldBinding> values = binding.values();
        for (AcroFieldBinding fieldBinding : values) paramSet.addAll(evaluator.getParameters((String) fieldBinding.getValue()));
        return paramSet;
    }

    /** Retrieves the parameter names from the specified conditions */
    public Set<String> getParametersFromConditions(Hashtable conditions) {
        Set<String> paramSet = new HashSet<String>();
        Collection<String> values = conditions.values();
        for (String value : values) paramSet.addAll(evaluator.getParameters(value));
        return paramSet;
    }

    private static int getDeg(String txt) {
        String deg[] = txt.split(" ");
        if (deg.length > 0) return Integer.parseInt(deg[0]); else return 0;
    }
}
