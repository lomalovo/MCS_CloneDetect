package eu.pisolutions.ocelot.document.annotation;

import eu.pisolutions.ocelot.document.PdfNameConstants;
import eu.pisolutions.ocelot.document.action.Action;
import eu.pisolutions.ocelot.document.action.AdditionalActions;
import eu.pisolutions.ocelot.document.io.DocumentNodeFactory;
import eu.pisolutions.ocelot.document.io.DocumentNodeRegistry;
import eu.pisolutions.ocelot.document.io.DocumentNodeRegistryHelper;
import eu.pisolutions.ocelot.document.io.DocumentPdfObjectCreationContext;
import eu.pisolutions.ocelot.document.io.DocumentPdfObjectCreationContextHelper;
import eu.pisolutions.ocelot.document.io.DocumentReadingContext;
import eu.pisolutions.ocelot.document.io.DocumentReadingContextHelper;
import eu.pisolutions.ocelot.object.PdfDictionaryObject;
import eu.pisolutions.ocelot.object.PdfObject;
import eu.pisolutions.ocelot.version.PdfVersion;
import eu.pisolutions.ocelot.version.RequiredPdfVersionHelper;

/**
 * Additional actions for {@link Annotation}s.
 *
 * <dl>
 * <dt><b>Specification:</b></dt>
 * <dd>PDF 1.7, 8.5.2</dd>
 * </dl>
 *
 * @author Laurent Pireyn
 */
public final class AnnotationAdditionalActions extends AdditionalActions {

    public enum Factory implements DocumentNodeFactory<AnnotationAdditionalActions> {

        INSTANCE;

        public AnnotationAdditionalActions createNode(DocumentReadingContext context, boolean indirect, PdfObject object) {
            return new AnnotationAdditionalActions(indirect);
        }
    }

    private Action onEnterAction;

    private Action onExitAction;

    private Action onMouseDownAction;

    private Action onMouseUpAction;

    private Action onFocusAction;

    private Action onBlurAction;

    private Action onPageOpenAction;

    private Action onPageCloseAction;

    private Action onPageVisibleAction;

    private Action onPageNotVisibleAction;

    public AnnotationAdditionalActions() {
        super();
    }

    private AnnotationAdditionalActions(boolean indirect) {
        super(indirect);
    }

    public Action getOnEnterAction() {
        return this.onEnterAction;
    }

    public void setOnEnterAction(Action onEnterAction) {
        this.onEnterAction = this.setProperty(this.onEnterAction, onEnterAction);
    }

    public Action getOnExitAction() {
        return this.onExitAction;
    }

    public void setOnExitAction(Action onExitAction) {
        this.onExitAction = this.setProperty(this.onExitAction, onExitAction);
    }

    public Action getOnMouseDownAction() {
        return this.onMouseDownAction;
    }

    public void setOnMouseDownAction(Action onMouseDownAction) {
        this.onMouseDownAction = this.setProperty(this.onMouseDownAction, onMouseDownAction);
    }

    public Action getOnMouseUpAction() {
        return this.onMouseUpAction;
    }

    public void setOnMouseUpAction(Action onMouseUpAction) {
        this.onMouseUpAction = this.setProperty(this.onMouseUpAction, onMouseUpAction);
    }

    public Action getOnFocusAction() {
        return this.onFocusAction;
    }

    public void setOnFocusAction(Action onFocusAction) {
        this.onFocusAction = this.setProperty(this.onFocusAction, onFocusAction);
    }

    public Action getOnBlurAction() {
        return this.onBlurAction;
    }

    public void setOnBlurAction(Action onBlurAction) {
        this.onBlurAction = this.setProperty(this.onBlurAction, onBlurAction);
    }

    public Action getOnPageOpenAction() {
        return this.onPageOpenAction;
    }

    public void setOnPageOpenAction(Action onPageOpenAction) {
        this.onPageOpenAction = this.setProperty(this.onPageOpenAction, onPageOpenAction);
    }

    public Action getOnPageCloseAction() {
        return this.onPageCloseAction;
    }

    public void setOnPageCloseAction(Action onPageCloseAction) {
        this.onPageCloseAction = this.setProperty(this.onPageCloseAction, onPageCloseAction);
    }

    public Action getOnPageVisibleAction() {
        return this.onPageVisibleAction;
    }

    public void setOnPageVisibleAction(Action onPageVisibleAction) {
        this.onPageVisibleAction = this.setProperty(this.onPageVisibleAction, onPageVisibleAction);
    }

    public Action getOnPageNotVisibleAction() {
        return this.onPageNotVisibleAction;
    }

    public void setOnPageNotVisibleAction(Action onPageNotVisibleAction) {
        this.onPageNotVisibleAction = this.setProperty(this.onPageNotVisibleAction, onPageNotVisibleAction);
    }

    @Override
    public PdfVersion getRequiredPdfVersion() {
        return new RequiredPdfVersionHelper(super.getRequiredPdfVersion()).requirePdfVersionIfNotNull(PdfVersion.VERSION_1_5, this.onPageOpenAction).requirePdfVersionIfNotNull(PdfVersion.VERSION_1_5, this.onPageCloseAction).requirePdfVersionIfNotNull(PdfVersion.VERSION_1_5, this.onPageVisibleAction).requirePdfVersionIfNotNull(PdfVersion.VERSION_1_5, this.onPageNotVisibleAction).getRequiredPdfVersion();
    }

    @Override
    public void registerNodes(DocumentNodeRegistry registry) {
        new DocumentNodeRegistryHelper(registry).registerNode(this.onEnterAction).registerNode(this.onExitAction).registerNode(this.onMouseDownAction).registerNode(this.onMouseUpAction).registerNode(this.onFocusAction).registerNode(this.onBlurAction).registerNode(this.onPageOpenAction, PdfVersion.VERSION_1_5).registerNode(this.onPageCloseAction, PdfVersion.VERSION_1_5).registerNode(this.onPageVisibleAction, PdfVersion.VERSION_1_5).registerNode(this.onPageNotVisibleAction, PdfVersion.VERSION_1_5);
    }

    @Override
    protected void readSpecificEntriesFrom(DocumentReadingContext context, PdfDictionaryObject dictionary) {
        final DocumentReadingContextHelper helper = new DocumentReadingContextHelper(context);
        this.onEnterAction = helper.getNode(dictionary.get(PdfNameConstants.E), Action.Factory.INSTANCE);
        this.onExitAction = helper.getNode(dictionary.get(PdfNameConstants.X), Action.Factory.INSTANCE);
        this.onMouseDownAction = helper.getNode(dictionary.get(PdfNameConstants.D), Action.Factory.INSTANCE);
        this.onMouseUpAction = helper.getNode(dictionary.get(PdfNameConstants.U), Action.Factory.INSTANCE);
        this.onFocusAction = helper.getNode(dictionary.get(PdfNameConstants.FO), Action.Factory.INSTANCE);
        this.onBlurAction = helper.getNode(dictionary.get(PdfNameConstants.BL), Action.Factory.INSTANCE);
        this.onPageOpenAction = helper.getNode(dictionary.get(PdfNameConstants.P_O), Action.Factory.INSTANCE);
        this.onPageCloseAction = helper.getNode(dictionary.get(PdfNameConstants.P_C), Action.Factory.INSTANCE);
        this.onPageVisibleAction = helper.getNode(dictionary.get(PdfNameConstants.P_V), Action.Factory.INSTANCE);
        this.onPageNotVisibleAction = helper.getNode(dictionary.get(PdfNameConstants.P_I), Action.Factory.INSTANCE);
    }

    @Override
    protected void addSpecificEntriesTo(DocumentPdfObjectCreationContext context, PdfDictionaryObject dictionary) {
        final DocumentPdfObjectCreationContextHelper helper = new DocumentPdfObjectCreationContextHelper(context);
        dictionary.put(PdfNameConstants.E, helper.getElement(this.onEnterAction));
        dictionary.put(PdfNameConstants.X, helper.getElement(this.onExitAction));
        dictionary.put(PdfNameConstants.D, helper.getElement(this.onMouseDownAction));
        dictionary.put(PdfNameConstants.U, helper.getElement(this.onMouseUpAction));
        dictionary.put(PdfNameConstants.FO, helper.getElement(this.onFocusAction));
        dictionary.put(PdfNameConstants.BL, helper.getElement(this.onBlurAction));
        if (this.onPageOpenAction != null && context.supportsPdfVersion(PdfVersion.VERSION_1_5)) {
            dictionary.put(PdfNameConstants.P_O, helper.getElement(this.onPageOpenAction));
        }
        if (this.onPageCloseAction != null && context.supportsPdfVersion(PdfVersion.VERSION_1_5)) {
            dictionary.put(PdfNameConstants.P_C, helper.getElement(this.onPageCloseAction));
        }
        if (this.onPageVisibleAction != null && context.supportsPdfVersion(PdfVersion.VERSION_1_5)) {
            dictionary.put(PdfNameConstants.P_V, helper.getElement(this.onPageVisibleAction));
        }
        if (this.onPageNotVisibleAction != null && context.supportsPdfVersion(PdfVersion.VERSION_1_5)) {
            dictionary.put(PdfNameConstants.P_I, helper.getElement(this.onPageNotVisibleAction));
        }
    }
}
