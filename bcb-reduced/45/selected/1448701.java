package com.itextpdf.text.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.List;
import com.itextpdf.text.api.WriterOperation;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.pdf.collection.PdfCollection;
import com.itextpdf.text.pdf.draw.DrawInterface;
import com.itextpdf.text.pdf.internal.PdfAnnotationsImp;
import com.itextpdf.text.pdf.internal.PdfViewerPreferencesImp;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * <CODE>PdfDocument</CODE> is the class that is used by <CODE>PdfWriter</CODE>
 * to translate a <CODE>Document</CODE> into a PDF with different pages.
 * <P>
 * A <CODE>PdfDocument</CODE> always listens to a <CODE>Document</CODE>
 * and adds the Pdf representation of every <CODE>Element</CODE> that is
 * added to the <CODE>Document</CODE>.
 *
 * @see		com.itextpdf.text.Document
 * @see		com.itextpdf.text.DocListener
 * @see		PdfWriter
 * @since	2.0.8 (class was package-private before)
 */
public class PdfDocument extends Document {

    /**
     * <CODE>PdfInfo</CODE> is the PDF InfoDictionary.
     * <P>
     * A document's trailer may contain a reference to an Info dictionary that provides information
     * about the document. This optional dictionary may contain one or more keys, whose values
     * should be strings.<BR>
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.10 (page 120-121)
     * @since	2.0.8 (PdfDocument was package-private before)
     */
    public static class PdfInfo extends PdfDictionary {

        /**
         * Construct a <CODE>PdfInfo</CODE>-object.
         */
        PdfInfo() {
            super();
            addProducer();
            addCreationDate();
        }

        /**
         * Constructs a <CODE>PdfInfo</CODE>-object.
         *
         * @param		author		name of the author of the document
         * @param		title		title of the document
         * @param		subject		subject of the document
         */
        PdfInfo(final String author, final String title, final String subject) {
            this();
            addTitle(title);
            addSubject(subject);
            addAuthor(author);
        }

        /**
         * Adds the title of the document.
         *
         * @param	title		the title of the document
         */
        void addTitle(final String title) {
            put(PdfName.TITLE, new PdfString(title, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the subject to the document.
         *
         * @param	subject		the subject of the document
         */
        void addSubject(final String subject) {
            put(PdfName.SUBJECT, new PdfString(subject, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds some keywords to the document.
         *
         * @param	keywords		the keywords of the document
         */
        void addKeywords(final String keywords) {
            put(PdfName.KEYWORDS, new PdfString(keywords, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the author to the document.
         *
         * @param	author		the name of the author
         */
        void addAuthor(final String author) {
            put(PdfName.AUTHOR, new PdfString(author, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the creator to the document.
         *
         * @param	creator		the name of the creator
         */
        void addCreator(final String creator) {
            put(PdfName.CREATOR, new PdfString(creator, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the producer to the document.
         */
        void addProducer() {
            put(PdfName.PRODUCER, new PdfString(getVersion()));
        }

        /**
         * Adds the date of creation to the document.
         */
        void addCreationDate() {
            PdfString date = new PdfDate();
            put(PdfName.CREATIONDATE, date);
            put(PdfName.MODDATE, date);
        }

        void addkey(final String key, final String value) {
            if (key.equals("Producer") || key.equals("CreationDate")) return;
            put(new PdfName(key), new PdfString(value, PdfObject.TEXT_UNICODE));
        }
    }

    /**
     * <CODE>PdfCatalog</CODE> is the PDF Catalog-object.
     * <P>
     * The Catalog is a dictionary that is the root node of the document. It contains a reference
     * to the tree of pages contained in the document, a reference to the tree of objects representing
     * the document's outline, a reference to the document's article threads, and the list of named
     * destinations. In addition, the Catalog indicates whether the document's outline or thumbnail
     * page images should be displayed automatically when the document is viewed and whether some location
     * other than the first page should be shown when the document is opened.<BR>
     * In this class however, only the reference to the tree of pages is implemented.<BR>
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.2 (page 67-71)
     */
    static class PdfCatalog extends PdfDictionary {

        /** The writer writing the PDF for which we are creating this catalog object. */
        PdfWriter writer;

        /**
         * Constructs a <CODE>PdfCatalog</CODE>.
         *
         * @param		pages		an indirect reference to the root of the document's Pages tree.
         * @param writer the writer the catalog applies to
         */
        PdfCatalog(final PdfIndirectReference pages, final PdfWriter writer) {
            super(CATALOG);
            this.writer = writer;
            put(PdfName.PAGES, pages);
        }

        /**
         * Adds the names of the named destinations to the catalog.
         * @param localDestinations the local destinations
         * @param documentLevelJS the javascript used in the document
         * @param documentFileAttachment	the attached files
         * @param writer the writer the catalog applies to
         */
        void addNames(final TreeMap<String, Destination> localDestinations, final HashMap<String, PdfObject> documentLevelJS, final HashMap<String, PdfObject> documentFileAttachment, final PdfWriter writer) {
            if (localDestinations.isEmpty() && documentLevelJS.isEmpty() && documentFileAttachment.isEmpty()) return;
            try {
                PdfDictionary names = new PdfDictionary();
                if (!localDestinations.isEmpty()) {
                    PdfArray ar = new PdfArray();
                    for (Map.Entry<String, Destination> entry : localDestinations.entrySet()) {
                        String name = entry.getKey();
                        Destination dest = entry.getValue();
                        if (dest.destination == null) continue;
                        PdfIndirectReference ref = dest.reference;
                        ar.add(new PdfString(name, null));
                        ar.add(ref);
                    }
                    if (ar.size() > 0) {
                        PdfDictionary dests = new PdfDictionary();
                        dests.put(PdfName.NAMES, ar);
                        names.put(PdfName.DESTS, writer.addToBody(dests).getIndirectReference());
                    }
                }
                if (!documentLevelJS.isEmpty()) {
                    PdfDictionary tree = PdfNameTree.writeTree(documentLevelJS, writer);
                    names.put(PdfName.JAVASCRIPT, writer.addToBody(tree).getIndirectReference());
                }
                if (!documentFileAttachment.isEmpty()) {
                    names.put(PdfName.EMBEDDEDFILES, writer.addToBody(PdfNameTree.writeTree(documentFileAttachment, writer)).getIndirectReference());
                }
                if (names.size() > 0) put(PdfName.NAMES, writer.addToBody(names).getIndirectReference());
            } catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }

        /**
         * Adds an open action to the catalog.
         * @param	action	the action that will be triggered upon opening the document
         */
        void setOpenAction(final PdfAction action) {
            put(PdfName.OPENACTION, action);
        }

        /**
         * Sets the document level additional actions.
         * @param actions   dictionary of actions
         */
        void setAdditionalActions(final PdfDictionary actions) {
            try {
                put(PdfName.AA, writer.addToBody(actions).getIndirectReference());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Constructs a new PDF document.
     */
    public PdfDocument() {
        super();
        addProducer();
        addCreationDate();
    }

    /** The <CODE>PdfWriter</CODE>. */
    protected PdfWriter writer;

    /**
     * Adds a <CODE>PdfWriter</CODE> to the <CODE>PdfDocument</CODE>.
     *
     * @param writer the <CODE>PdfWriter</CODE> that writes everything
     *                     what is added to this document to an outputstream.
     * @throws DocumentException on error
     */
    public void addWriter(final PdfWriter writer) throws DocumentException {
        if (this.writer == null) {
            this.writer = writer;
            annotationsImp = new PdfAnnotationsImp(writer);
            return;
        }
        throw new DocumentException(MessageLocalization.getComposedMessage("you.can.only.add.a.writer.to.a.pdfdocument.once"));
    }

    /** This is the PdfContentByte object, containing the text. */
    protected PdfContentByte text;

    /** This is the PdfContentByte object, containing the borders and other Graphics. */
    protected PdfContentByte graphics;

    /** This represents the leading of the lines. */
    protected float leading = 0;

    /**
     * Getter for the current leading.
     * @return	the current leading
     * @since	2.1.2
     */
    public float getLeading() {
        return leading;
    }

    /**
     * Setter for the current leading.
     * @param	leading the current leading
     * @since	2.1.6
     */
    void setLeading(final float leading) {
        this.leading = leading;
    }

    /** This represents the current alignment of the PDF Elements. */
    protected int alignment = Element.ALIGN_LEFT;

    /** This is the current height of the document. */
    protected float currentHeight = 0;

    /**
     * Signals that onParagraph is valid (to avoid that a Chapter/Section title is treated as a Paragraph).
     * @since 2.1.2
     */
    protected boolean isSectionTitle = false;

    /**
     * Signals that the current leading has to be subtracted from a YMark object when positive.
     * @since 2.1.2
     */
    protected int leadingCount = 0;

    /** The current active <CODE>PdfAction</CODE> when processing an <CODE>Anchor</CODE>. */
    protected PdfAction anchorAction = null;

    /**
     * Signals that an <CODE>Element</CODE> was added to the <CODE>Document</CODE>.
     *
     * @param element the element to add
     * @return <CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
     * @throws DocumentException when a document isn't open yet, or has been closed
     */
    @Override
    public boolean add(final Element element) throws DocumentException {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        try {
            switch(element.type()) {
                case Element.HEADER:
                    info.addkey(((Meta) element).getName(), ((Meta) element).getContent());
                    break;
                case Element.TITLE:
                    info.addTitle(((Meta) element).getContent());
                    break;
                case Element.SUBJECT:
                    info.addSubject(((Meta) element).getContent());
                    break;
                case Element.KEYWORDS:
                    info.addKeywords(((Meta) element).getContent());
                    break;
                case Element.AUTHOR:
                    info.addAuthor(((Meta) element).getContent());
                    break;
                case Element.CREATOR:
                    info.addCreator(((Meta) element).getContent());
                    break;
                case Element.PRODUCER:
                    info.addProducer();
                    break;
                case Element.CREATIONDATE:
                    info.addCreationDate();
                    break;
                case Element.CHUNK:
                    {
                        if (line == null) {
                            carriageReturn();
                        }
                        PdfChunk chunk = new PdfChunk((Chunk) element, anchorAction);
                        {
                            PdfChunk overflow;
                            while ((overflow = line.add(chunk)) != null) {
                                carriageReturn();
                                chunk = overflow;
                                chunk.trimFirstSpace();
                            }
                        }
                        pageEmpty = false;
                        if (chunk.isAttribute(Chunk.NEWPAGE)) {
                            newPage();
                        }
                        break;
                    }
                case Element.ANCHOR:
                    {
                        leadingCount++;
                        Anchor anchor = (Anchor) element;
                        String url = anchor.getReference();
                        leading = anchor.getLeading();
                        if (url != null) {
                            anchorAction = new PdfAction(url);
                        }
                        element.process(this);
                        anchorAction = null;
                        leadingCount--;
                        break;
                    }
                case Element.ANNOTATION:
                    {
                        if (line == null) {
                            carriageReturn();
                        }
                        Annotation annot = (Annotation) element;
                        Rectangle rect = new Rectangle(0, 0);
                        if (line != null) rect = new Rectangle(annot.llx(indentRight() - line.widthLeft()), annot.ury(indentTop() - currentHeight - 20), annot.urx(indentRight() - line.widthLeft() + 20), annot.lly(indentTop() - currentHeight));
                        PdfAnnotation an = PdfAnnotationsImp.convertAnnotation(writer, annot, rect);
                        annotationsImp.addPlainAnnotation(an);
                        pageEmpty = false;
                        break;
                    }
                case Element.PHRASE:
                    {
                        leadingCount++;
                        leading = ((Phrase) element).getTotalLeading();
                        element.process(this);
                        leadingCount--;
                        break;
                    }
                case Element.PARAGRAPH:
                    {
                        leadingCount++;
                        Paragraph paragraph = (Paragraph) element;
                        addSpacing(paragraph.getSpacingBefore(), leading, paragraph.getFont());
                        alignment = paragraph.getAlignment();
                        leading = paragraph.getTotalLeading();
                        carriageReturn();
                        if (currentHeight + line.height() + leading > indentTop() - indentBottom()) {
                            newPage();
                        }
                        indentation.indentLeft += paragraph.getIndentationLeft();
                        indentation.indentRight += paragraph.getIndentationRight();
                        carriageReturn();
                        PdfPageEvent pageEvent = writer.getPageEvent();
                        if (pageEvent != null && !isSectionTitle) pageEvent.onParagraph(writer, this, indentTop() - currentHeight);
                        if (paragraph.getKeepTogether()) {
                            carriageReturn();
                            PdfPTable table = new PdfPTable(1);
                            table.setWidthPercentage(100f);
                            PdfPCell cell = new PdfPCell();
                            cell.addElement(paragraph);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setPadding(0);
                            table.addCell(cell);
                            indentation.indentLeft -= paragraph.getIndentationLeft();
                            indentation.indentRight -= paragraph.getIndentationRight();
                            this.add(table);
                            indentation.indentLeft += paragraph.getIndentationLeft();
                            indentation.indentRight += paragraph.getIndentationRight();
                        } else {
                            line.setExtraIndent(paragraph.getFirstLineIndent());
                            element.process(this);
                            carriageReturn();
                            addSpacing(paragraph.getSpacingAfter(), paragraph.getTotalLeading(), paragraph.getFont());
                        }
                        if (pageEvent != null && !isSectionTitle) pageEvent.onParagraphEnd(writer, this, indentTop() - currentHeight);
                        alignment = Element.ALIGN_LEFT;
                        indentation.indentLeft -= paragraph.getIndentationLeft();
                        indentation.indentRight -= paragraph.getIndentationRight();
                        carriageReturn();
                        leadingCount--;
                        break;
                    }
                case Element.SECTION:
                case Element.CHAPTER:
                    {
                        Section section = (Section) element;
                        PdfPageEvent pageEvent = writer.getPageEvent();
                        boolean hasTitle = section.isNotAddedYet() && section.getTitle() != null;
                        if (section.isTriggerNewPage()) {
                            newPage();
                        }
                        if (hasTitle) {
                            float fith = indentTop() - currentHeight;
                            int rotation = pageSize.getRotation();
                            if (rotation == 90 || rotation == 180) fith = pageSize.getHeight() - fith;
                            PdfDestination destination = new PdfDestination(PdfDestination.FITH, fith);
                            while (currentOutline.level() >= section.getDepth()) {
                                currentOutline = currentOutline.parent();
                            }
                            PdfOutline outline = new PdfOutline(currentOutline, destination, section.getBookmarkTitle(), section.isBookmarkOpen());
                            currentOutline = outline;
                        }
                        carriageReturn();
                        indentation.sectionIndentLeft += section.getIndentationLeft();
                        indentation.sectionIndentRight += section.getIndentationRight();
                        if (section.isNotAddedYet() && pageEvent != null) if (element.type() == Element.CHAPTER) pageEvent.onChapter(writer, this, indentTop() - currentHeight, section.getTitle()); else pageEvent.onSection(writer, this, indentTop() - currentHeight, section.getDepth(), section.getTitle());
                        if (hasTitle) {
                            isSectionTitle = true;
                            add(section.getTitle());
                            isSectionTitle = false;
                        }
                        indentation.sectionIndentLeft += section.getIndentation();
                        element.process(this);
                        flushLines();
                        indentation.sectionIndentLeft -= section.getIndentationLeft() + section.getIndentation();
                        indentation.sectionIndentRight -= section.getIndentationRight();
                        if (section.isComplete() && pageEvent != null) if (element.type() == Element.CHAPTER) pageEvent.onChapterEnd(writer, this, indentTop() - currentHeight); else pageEvent.onSectionEnd(writer, this, indentTop() - currentHeight);
                        break;
                    }
                case Element.LIST:
                    {
                        List list = (List) element;
                        if (list.isAlignindent()) {
                            list.normalizeIndentation();
                        }
                        indentation.listIndentLeft += list.getIndentationLeft();
                        indentation.indentRight += list.getIndentationRight();
                        element.process(this);
                        indentation.listIndentLeft -= list.getIndentationLeft();
                        indentation.indentRight -= list.getIndentationRight();
                        carriageReturn();
                        break;
                    }
                case Element.LISTITEM:
                    {
                        leadingCount++;
                        ListItem listItem = (ListItem) element;
                        addSpacing(listItem.getSpacingBefore(), leading, listItem.getFont());
                        alignment = listItem.getAlignment();
                        indentation.listIndentLeft += listItem.getIndentationLeft();
                        indentation.indentRight += listItem.getIndentationRight();
                        leading = listItem.getTotalLeading();
                        carriageReturn();
                        line.setListItem(listItem);
                        element.process(this);
                        addSpacing(listItem.getSpacingAfter(), listItem.getTotalLeading(), listItem.getFont());
                        if (line.hasToBeJustified()) {
                            line.resetAlignment();
                        }
                        carriageReturn();
                        indentation.listIndentLeft -= listItem.getIndentationLeft();
                        indentation.indentRight -= listItem.getIndentationRight();
                        leadingCount--;
                        break;
                    }
                case Element.RECTANGLE:
                    {
                        Rectangle rectangle = (Rectangle) element;
                        graphics.rectangle(rectangle);
                        pageEmpty = false;
                        break;
                    }
                case Element.PTABLE:
                    {
                        PdfPTable ptable = (PdfPTable) element;
                        if (ptable.size() <= ptable.getHeaderRows()) break;
                        ensureNewLine();
                        flushLines();
                        addPTable(ptable);
                        pageEmpty = false;
                        newLine();
                        break;
                    }
                case Element.MULTI_COLUMN_TEXT:
                    {
                        ensureNewLine();
                        flushLines();
                        MultiColumnText multiText = (MultiColumnText) element;
                        float height = multiText.write(writer.getDirectContent(), this, indentTop() - currentHeight);
                        currentHeight += height;
                        text.moveText(0, -1f * height);
                        pageEmpty = false;
                        break;
                    }
                case Element.JPEG:
                case Element.JPEG2000:
                case Element.JBIG2:
                case Element.IMGRAW:
                case Element.IMGTEMPLATE:
                    {
                        add((Image) element);
                        break;
                    }
                case Element.YMARK:
                    {
                        DrawInterface zh = (DrawInterface) element;
                        zh.draw(graphics, indentLeft(), indentBottom(), indentRight(), indentTop(), indentTop() - currentHeight - (leadingCount > 0 ? leading : 0));
                        pageEmpty = false;
                        break;
                    }
                case Element.MARKED:
                    {
                        MarkedObject mo;
                        if (element instanceof MarkedSection) {
                            mo = ((MarkedSection) element).getTitle();
                            if (mo != null) {
                                mo.process(this);
                            }
                        }
                        mo = (MarkedObject) element;
                        mo.process(this);
                        break;
                    }
                case Element.WRITABLE_DIRECT:
                    if (null != writer) {
                        ((WriterOperation) element).write(writer, this);
                    }
                    break;
                default:
                    return false;
            }
            lastElementType = element.type();
            return true;
        } catch (Exception e) {
            throw new DocumentException(e);
        }
    }

    /**
     * Opens the document.
     * <P>
     * You have to open the document before you can begin to add content
     * to the body of the document.
     */
    @Override
    public void open() {
        if (!open) {
            super.open();
            writer.open();
            rootOutline = new PdfOutline(writer);
            currentOutline = rootOutline;
        }
        try {
            initPage();
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    /**
     * Closes the document.
     * <B>
     * Once all the content has been written in the body, you have to close
     * the body. After that nothing can be written to the body anymore.
     */
    @Override
    public void close() {
        if (close) {
            return;
        }
        try {
            boolean wasImage = imageWait != null;
            newPage();
            if (imageWait != null || wasImage) newPage();
            if (annotationsImp.hasUnusedAnnotations()) throw new RuntimeException(MessageLocalization.getComposedMessage("not.all.annotations.could.be.added.to.the.document.the.document.doesn.t.have.enough.pages"));
            PdfPageEvent pageEvent = writer.getPageEvent();
            if (pageEvent != null) pageEvent.onCloseDocument(writer, this);
            super.close();
            writer.addLocalDestinations(localDestinations);
            calculateOutlineCount();
            writeOutlines();
        } catch (Exception e) {
            throw ExceptionConverter.convertException(e);
        }
        writer.close();
    }

    protected int textEmptySize;

    /**
	 * Use this method to set the XMP Metadata.
	 * @param xmpMetadata The xmpMetadata to set.
	 * @throws IOException
	 */
    public void setXmpMetadata(final byte[] xmpMetadata) throws IOException {
        PdfStream xmp = new PdfStream(xmpMetadata);
        xmp.put(PdfName.TYPE, PdfName.METADATA);
        xmp.put(PdfName.SUBTYPE, PdfName.XML);
        PdfEncryption crypto = writer.getEncryption();
        if (crypto != null && !crypto.isMetadataEncrypted()) {
            PdfArray ar = new PdfArray();
            ar.add(PdfName.CRYPT);
            xmp.put(PdfName.FILTER, ar);
        }
        writer.addPageDictEntry(PdfName.METADATA, writer.addToBody(xmp).getIndirectReference());
    }

    /**
     * Makes a new page and sends it to the <CODE>PdfWriter</CODE>.
     *
     * @return a <CODE>boolean</CODE>
     */
    @Override
    public boolean newPage() {
        lastElementType = -1;
        if (isPageEmpty()) {
            setNewPageSizeAndMargins();
            return false;
        }
        if (!open || close) {
            throw new RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
        }
        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null) pageEvent.onEndPage(writer, this);
        super.newPage();
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
        try {
            flushLines();
            int rotation = pageSize.getRotation();
            if (writer.isPdfX()) {
                if (thisBoxSize.containsKey("art") && thisBoxSize.containsKey("trim")) throw new PdfXConformanceException(MessageLocalization.getComposedMessage("only.one.of.artbox.or.trimbox.can.exist.in.the.page"));
                if (!thisBoxSize.containsKey("art") && !thisBoxSize.containsKey("trim")) {
                    if (thisBoxSize.containsKey("crop")) thisBoxSize.put("trim", thisBoxSize.get("crop")); else thisBoxSize.put("trim", new PdfRectangle(pageSize, pageSize.getRotation()));
                }
            }
            pageResources.addDefaultColorDiff(writer.getDefaultColorspace());
            if (writer.isRgbTransparencyBlending()) {
                PdfDictionary dcs = new PdfDictionary();
                dcs.put(PdfName.CS, PdfName.DEVICERGB);
                pageResources.addDefaultColorDiff(dcs);
            }
            PdfDictionary resources = pageResources.getResources();
            PdfPage page = new PdfPage(new PdfRectangle(pageSize, rotation), thisBoxSize, resources, rotation);
            page.put(PdfName.TABS, writer.getTabs());
            page.putAll(writer.getPageDictEntries());
            writer.resetPageDictEntries();
            if (pageAA != null) {
                page.put(PdfName.AA, writer.addToBody(pageAA).getIndirectReference());
                pageAA = null;
            }
            if (annotationsImp.hasUnusedAnnotations()) {
                PdfArray array = annotationsImp.rotateAnnotations(writer, pageSize);
                if (array.size() != 0) page.put(PdfName.ANNOTS, array);
            }
            if (writer.isTagged()) page.put(PdfName.STRUCTPARENTS, new PdfNumber(writer.getCurrentPageNumber() - 1));
            if (text.size() > textEmptySize) text.endText(); else text = null;
            writer.add(page, new PdfContents(writer.getDirectContentUnder(), graphics, text, writer.getDirectContent(), pageSize));
            initPage();
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        return true;
    }

    /**
     * Sets the pagesize.
     *
     * @param pageSize the new pagesize
     * @return <CODE>true</CODE> if the page size was set
     */
    @Override
    public boolean setPageSize(final Rectangle pageSize) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextPageSize = new Rectangle(pageSize);
        return true;
    }

    /** margin in x direction starting from the left. Will be valid in the next page */
    protected float nextMarginLeft;

    /** margin in x direction starting from the right. Will be valid in the next page */
    protected float nextMarginRight;

    /** margin in y direction starting from the top. Will be valid in the next page */
    protected float nextMarginTop;

    /** margin in y direction starting from the bottom. Will be valid in the next page */
    protected float nextMarginBottom;

    /**
     * Sets the margins.
     *
     * @param	marginLeft		the margin on the left
     * @param	marginRight		the margin on the right
     * @param	marginTop		the margin on the top
     * @param	marginBottom	the margin on the bottom
     * @return	a <CODE>boolean</CODE>
     */
    @Override
    public boolean setMargins(final float marginLeft, final float marginRight, final float marginTop, final float marginBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextMarginLeft = marginLeft;
        nextMarginRight = marginRight;
        nextMarginTop = marginTop;
        nextMarginBottom = marginBottom;
        return true;
    }

    /**
     * @see com.itextpdf.text.DocListener#setMarginMirroring(boolean)
     */
    @Override
    public boolean setMarginMirroring(final boolean MarginMirroring) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroring(MarginMirroring);
    }

    /**
     * @see com.itextpdf.text.DocListener#setMarginMirroring(boolean)
     * @since	2.1.6
     */
    @Override
    public boolean setMarginMirroringTopBottom(final boolean MarginMirroringTopBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroringTopBottom(MarginMirroringTopBottom);
    }

    /**
     * Sets the page number.
     *
     * @param	pageN		the new page number
     */
    @Override
    public void setPageCount(final int pageN) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setPageCount(pageN);
    }

    /**
     * Sets the page number to 0.
     */
    @Override
    public void resetPageCount() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetPageCount();
    }

    /** Signals that OnOpenDocument should be called. */
    protected boolean firstPageEvent = true;

    /**
     * Initializes a page.
     * <P>
     * If the footer/header is set, it is printed.
     * @throws DocumentException on error
     */
    protected void initPage() throws DocumentException {
        pageN++;
        annotationsImp.resetAnnotations();
        pageResources = new PageResources();
        writer.resetContent();
        graphics = new PdfContentByte(writer);
        markPoint = 0;
        setNewPageSizeAndMargins();
        imageEnd = -1;
        indentation.imageIndentRight = 0;
        indentation.imageIndentLeft = 0;
        indentation.indentBottom = 0;
        indentation.indentTop = 0;
        currentHeight = 0;
        thisBoxSize = new HashMap<String, PdfRectangle>(boxSize);
        if (pageSize.getBackgroundColor() != null || pageSize.hasBorders() || pageSize.getBorderColor() != null) {
            add(pageSize);
        }
        float oldleading = leading;
        int oldAlignment = alignment;
        pageEmpty = true;
        try {
            if (imageWait != null) {
                add(imageWait);
                imageWait = null;
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        leading = oldleading;
        alignment = oldAlignment;
        carriageReturn();
        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null) {
            if (firstPageEvent) {
                pageEvent.onOpenDocument(writer, this);
            }
            pageEvent.onStartPage(writer, this);
        }
        firstPageEvent = false;
    }

    /** The line that is currently being written. */
    protected PdfLine line = null;

    /** The lines that are written until now. */
    protected ArrayList<PdfLine> lines = new ArrayList<PdfLine>();

    /**
     * Adds the current line to the list of lines and also adds an empty line.
     * @throws DocumentException on error
     */
    protected void newLine() throws DocumentException {
        lastElementType = -1;
        carriageReturn();
        if (lines != null && !lines.isEmpty()) {
            lines.add(line);
            currentHeight += line.height();
        }
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

    /**
     * If the current line is not empty or null, it is added to the arraylist
     * of lines and a new empty line is added.
     */
    protected void carriageReturn() {
        if (lines == null) {
            lines = new ArrayList<PdfLine>();
        }
        if (line != null && line.size() > 0) {
            if (currentHeight + line.height() + leading > indentTop() - indentBottom()) {
                PdfLine overflowLine = line;
                line = null;
                newPage();
                line = overflowLine;
            }
            currentHeight += line.height();
            lines.add(line);
            pageEmpty = false;
        }
        if (imageEnd > -1 && currentHeight > imageEnd) {
            imageEnd = -1;
            indentation.imageIndentRight = 0;
            indentation.imageIndentLeft = 0;
        }
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

    /**
     * Gets the current vertical page position.
     * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects
     *   for elements that do not terminate the lines they've started because those lines will get
     *   terminated.
     * @return The current vertical page position.
     */
    public float getVerticalPosition(final boolean ensureNewLine) {
        if (ensureNewLine) {
            ensureNewLine();
        }
        return top() - currentHeight - indentation.indentTop;
    }

    /** Holds the type of the last element, that has been added to the document. */
    protected int lastElementType = -1;

    /**
     * Ensures that a new line has been started.
     */
    protected void ensureNewLine() {
        try {
            if (lastElementType == Element.PHRASE || lastElementType == Element.CHUNK) {
                newLine();
                flushLines();
            }
        } catch (DocumentException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    /**
     * Writes all the lines to the text-object.
     *
     * @return the displacement that was caused
     * @throws DocumentException on error
     */
    protected float flushLines() throws DocumentException {
        if (lines == null) {
            return 0;
        }
        if (line != null && line.size() > 0) {
            lines.add(line);
            line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
        }
        if (lines.isEmpty()) {
            return 0;
        }
        Object currentValues[] = new Object[2];
        PdfFont currentFont = null;
        float displacement = 0;
        Float lastBaseFactor = new Float(0);
        currentValues[1] = lastBaseFactor;
        for (PdfLine l : lines) {
            float moveTextX = l.indentLeft() - indentLeft() + indentation.indentLeft + indentation.listIndentLeft + indentation.sectionIndentLeft;
            text.moveText(moveTextX, -l.height());
            if (l.listSymbol() != null) {
                ColumnText.showTextAligned(graphics, Element.ALIGN_LEFT, new Phrase(l.listSymbol()), text.getXTLM() - l.listIndent(), text.getYTLM(), 0);
            }
            currentValues[0] = currentFont;
            writeLineToContent(l, text, graphics, currentValues, writer.getSpaceCharRatio());
            currentFont = (PdfFont) currentValues[0];
            displacement += l.height();
            text.moveText(-moveTextX, 0);
        }
        lines = new ArrayList<PdfLine>();
        return displacement;
    }

    /** The characters to be applied the hanging punctuation. */
    static final String hangingPunctuation = ".,;:'";

    /**
     * Writes a text line to the document. It takes care of all the attributes.
     * <P>
     * Before entering the line position must have been established and the
     * <CODE>text</CODE> argument must be in text object scope (<CODE>beginText()</CODE>).
     * @param line the line to be written
     * @param text the <CODE>PdfContentByte</CODE> where the text will be written to
     * @param graphics the <CODE>PdfContentByte</CODE> where the graphics will be written to
     * @param currentValues the current font and extra spacing values
     * @param ratio
     * @throws DocumentException on error
     * @since 5.0.3 returns a float instead of void
     */
    float writeLineToContent(final PdfLine line, final PdfContentByte text, final PdfContentByte graphics, final Object currentValues[], final float ratio) throws DocumentException {
        PdfFont currentFont = (PdfFont) currentValues[0];
        float lastBaseFactor = ((Float) currentValues[1]).floatValue();
        PdfChunk chunk;
        int numberOfSpaces;
        int lineLen;
        boolean isJustified;
        float hangingCorrection = 0;
        float hScale = 1;
        float lastHScale = Float.NaN;
        float baseWordSpacing = 0;
        float baseCharacterSpacing = 0;
        float glueWidth = 0;
        float lastX = text.getXTLM() + line.getOriginalWidth();
        numberOfSpaces = line.numberOfSpaces();
        lineLen = line.getLineLengthUtf32();
        isJustified = line.hasToBeJustified() && (numberOfSpaces != 0 || lineLen > 1);
        int separatorCount = line.getSeparatorCount();
        if (separatorCount > 0) {
            glueWidth = line.widthLeft() / separatorCount;
        } else if (isJustified && separatorCount == 0) {
            if (line.isNewlineSplit() && line.widthLeft() >= lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1)) {
                if (line.isRTL()) {
                    text.moveText(line.widthLeft() - lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1), 0);
                }
                baseWordSpacing = ratio * lastBaseFactor;
                baseCharacterSpacing = lastBaseFactor;
            } else {
                float width = line.widthLeft();
                PdfChunk last = line.getChunk(line.size() - 1);
                if (last != null) {
                    String s = last.toString();
                    char c;
                    if (s.length() > 0 && hangingPunctuation.indexOf((c = s.charAt(s.length() - 1))) >= 0) {
                        float oldWidth = width;
                        width += last.font().width(c) * 0.4f;
                        hangingCorrection = width - oldWidth;
                    }
                }
                float baseFactor = width / (ratio * numberOfSpaces + lineLen - 1);
                baseWordSpacing = ratio * baseFactor;
                baseCharacterSpacing = baseFactor;
                lastBaseFactor = baseFactor;
            }
        } else if (line.alignment == Element.ALIGN_LEFT || line.alignment == Element.ALIGN_UNDEFINED) {
            lastX -= line.widthLeft();
        }
        int lastChunkStroke = line.getLastStrokeChunk();
        int chunkStrokeIdx = 0;
        float xMarker = text.getXTLM();
        float baseXMarker = xMarker;
        float yMarker = text.getYTLM();
        boolean adjustMatrix = false;
        float tabPosition = 0;
        for (Iterator<PdfChunk> j = line.iterator(); j.hasNext(); ) {
            chunk = j.next();
            BaseColor color = chunk.color();
            float fontSize = chunk.font().size();
            float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
            float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
            hScale = 1;
            if (chunkStrokeIdx <= lastChunkStroke) {
                float width;
                if (isJustified) {
                    width = chunk.getWidthCorrected(baseCharacterSpacing, baseWordSpacing);
                } else {
                    width = chunk.width();
                }
                if (chunk.isStroked()) {
                    PdfChunk nextChunk = line.getChunk(chunkStrokeIdx + 1);
                    if (chunk.isSeparator()) {
                        width = glueWidth;
                        Object[] sep = (Object[]) chunk.getAttribute(Chunk.SEPARATOR);
                        DrawInterface di = (DrawInterface) sep[0];
                        Boolean vertical = (Boolean) sep[1];
                        if (vertical.booleanValue()) {
                            di.draw(graphics, baseXMarker, yMarker + descender, baseXMarker + line.getOriginalWidth(), ascender - descender, yMarker);
                        } else {
                            di.draw(graphics, xMarker, yMarker + descender, xMarker + width, ascender - descender, yMarker);
                        }
                    }
                    if (chunk.isTab()) {
                        Object[] tab = (Object[]) chunk.getAttribute(Chunk.TAB);
                        DrawInterface di = (DrawInterface) tab[0];
                        tabPosition = ((Float) tab[1]).floatValue() + ((Float) tab[3]).floatValue();
                        if (tabPosition > xMarker) {
                            di.draw(graphics, xMarker, yMarker + descender, tabPosition, ascender - descender, yMarker);
                        }
                        float tmp = xMarker;
                        xMarker = tabPosition;
                        tabPosition = tmp;
                    }
                    if (chunk.isAttribute(Chunk.BACKGROUND)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.BACKGROUND)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        Object bgr[] = (Object[]) chunk.getAttribute(Chunk.BACKGROUND);
                        graphics.setColorFill((BaseColor) bgr[0]);
                        float extra[] = (float[]) bgr[1];
                        graphics.rectangle(xMarker - extra[0], yMarker + descender - extra[1] + chunk.getTextRise(), width - subtract + extra[0] + extra[2], ascender - descender + extra[1] + extra[3]);
                        graphics.fill();
                        graphics.setGrayFill(0);
                    }
                    if (chunk.isAttribute(Chunk.UNDERLINE)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.UNDERLINE)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        Object unders[][] = (Object[][]) chunk.getAttribute(Chunk.UNDERLINE);
                        BaseColor scolor = null;
                        for (int k = 0; k < unders.length; ++k) {
                            Object obj[] = unders[k];
                            scolor = (BaseColor) obj[0];
                            float ps[] = (float[]) obj[1];
                            if (scolor == null) scolor = color;
                            if (scolor != null) graphics.setColorStroke(scolor);
                            graphics.setLineWidth(ps[0] + fontSize * ps[1]);
                            float shift = ps[2] + fontSize * ps[3];
                            int cap2 = (int) ps[4];
                            if (cap2 != 0) graphics.setLineCap(cap2);
                            graphics.moveTo(xMarker, yMarker + shift);
                            graphics.lineTo(xMarker + width - subtract, yMarker + shift);
                            graphics.stroke();
                            if (scolor != null) graphics.resetGrayStroke();
                            if (cap2 != 0) graphics.setLineCap(0);
                        }
                        graphics.setLineWidth(1);
                    }
                    if (chunk.isAttribute(Chunk.ACTION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.ACTION)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        text.addAnnotation(new PdfAnnotation(writer, xMarker, yMarker + descender + chunk.getTextRise(), xMarker + width - subtract, yMarker + ascender + chunk.getTextRise(), (PdfAction) chunk.getAttribute(Chunk.ACTION)));
                    }
                    if (chunk.isAttribute(Chunk.REMOTEGOTO)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.REMOTEGOTO)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        Object obj[] = (Object[]) chunk.getAttribute(Chunk.REMOTEGOTO);
                        String filename = (String) obj[0];
                        if (obj[1] instanceof String) remoteGoto(filename, (String) obj[1], xMarker, yMarker + descender + chunk.getTextRise(), xMarker + width - subtract, yMarker + ascender + chunk.getTextRise()); else remoteGoto(filename, ((Integer) obj[1]).intValue(), xMarker, yMarker + descender + chunk.getTextRise(), xMarker + width - subtract, yMarker + ascender + chunk.getTextRise());
                    }
                    if (chunk.isAttribute(Chunk.LOCALGOTO)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALGOTO)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        localGoto((String) chunk.getAttribute(Chunk.LOCALGOTO), xMarker, yMarker, xMarker + width - subtract, yMarker + fontSize);
                    }
                    if (chunk.isAttribute(Chunk.LOCALDESTINATION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALDESTINATION)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        localDestination((String) chunk.getAttribute(Chunk.LOCALDESTINATION), new PdfDestination(PdfDestination.XYZ, xMarker, yMarker + fontSize, 0));
                    }
                    if (chunk.isAttribute(Chunk.GENERICTAG)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.GENERICTAG)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        Rectangle rect = new Rectangle(xMarker, yMarker, xMarker + width - subtract, yMarker + fontSize);
                        PdfPageEvent pev = writer.getPageEvent();
                        if (pev != null) pev.onGenericTag(writer, this, rect, (String) chunk.getAttribute(Chunk.GENERICTAG));
                    }
                    if (chunk.isAttribute(Chunk.PDFANNOTATION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.PDFANNOTATION)) subtract = 0;
                        if (nextChunk == null) subtract += hangingCorrection;
                        PdfAnnotation annot = PdfFormField.shallowDuplicate((PdfAnnotation) chunk.getAttribute(Chunk.PDFANNOTATION));
                        annot.put(PdfName.RECT, new PdfRectangle(xMarker, yMarker + descender, xMarker + width - subtract, yMarker + ascender));
                        text.addAnnotation(annot);
                    }
                    float params[] = (float[]) chunk.getAttribute(Chunk.SKEW);
                    Float hs = (Float) chunk.getAttribute(Chunk.HSCALE);
                    if (params != null || hs != null) {
                        float b = 0, c = 0;
                        if (params != null) {
                            b = params[0];
                            c = params[1];
                        }
                        if (hs != null) hScale = hs.floatValue();
                        text.setTextMatrix(hScale, b, c, 1, xMarker, yMarker);
                    }
                    if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
                        Float cs = (Float) chunk.getAttribute(Chunk.CHAR_SPACING);
                        text.setCharacterSpacing(cs.floatValue());
                    }
                    if (chunk.isImage()) {
                        Image image = chunk.getImage();
                        float matrix[] = image.matrix();
                        matrix[Image.CX] = xMarker + chunk.getImageOffsetX() - matrix[Image.CX];
                        matrix[Image.CY] = yMarker + chunk.getImageOffsetY() - matrix[Image.CY];
                        graphics.addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                        text.moveText(xMarker + lastBaseFactor + image.getScaledWidth() - text.getXTLM(), 0);
                    }
                }
                xMarker += width;
                ++chunkStrokeIdx;
            }
            if (chunk.font().compareTo(currentFont) != 0) {
                currentFont = chunk.font();
                text.setFontAndSize(currentFont.getFont(), currentFont.size());
            }
            float rise = 0;
            Object textRender[] = (Object[]) chunk.getAttribute(Chunk.TEXTRENDERMODE);
            int tr = 0;
            float strokeWidth = 1;
            BaseColor strokeColor = null;
            Float fr = (Float) chunk.getAttribute(Chunk.SUBSUPSCRIPT);
            if (textRender != null) {
                tr = ((Integer) textRender[0]).intValue() & 3;
                if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL) text.setTextRenderingMode(tr);
                if (tr == PdfContentByte.TEXT_RENDER_MODE_STROKE || tr == PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE) {
                    strokeWidth = ((Float) textRender[1]).floatValue();
                    if (strokeWidth != 1) text.setLineWidth(strokeWidth);
                    strokeColor = (BaseColor) textRender[2];
                    if (strokeColor == null) strokeColor = color;
                    if (strokeColor != null) text.setColorStroke(strokeColor);
                }
            }
            if (fr != null) rise = fr.floatValue();
            if (color != null) text.setColorFill(color);
            if (rise != 0) text.setTextRise(rise);
            if (chunk.isImage()) {
                adjustMatrix = true;
            } else if (chunk.isHorizontalSeparator()) {
                PdfTextArray array = new PdfTextArray();
                array.add(-glueWidth * 1000f / chunk.font.size() / hScale);
                text.showText(array);
            } else if (chunk.isTab()) {
                PdfTextArray array = new PdfTextArray();
                array.add((tabPosition - xMarker) * 1000f / chunk.font.size() / hScale);
                text.showText(array);
            } else if (isJustified && numberOfSpaces > 0 && chunk.isSpecialEncoding()) {
                if (hScale != lastHScale) {
                    lastHScale = hScale;
                    text.setWordSpacing(baseWordSpacing / hScale);
                    text.setCharacterSpacing(baseCharacterSpacing / hScale + text.getCharacterSpacing());
                }
                String s = chunk.toString();
                int idx = s.indexOf(' ');
                if (idx < 0) text.showText(s); else {
                    float spaceCorrection = -baseWordSpacing * 1000f / chunk.font.size() / hScale;
                    PdfTextArray textArray = new PdfTextArray(s.substring(0, idx));
                    int lastIdx = idx;
                    while ((idx = s.indexOf(' ', lastIdx + 1)) >= 0) {
                        textArray.add(spaceCorrection);
                        textArray.add(s.substring(lastIdx, idx));
                        lastIdx = idx;
                    }
                    textArray.add(spaceCorrection);
                    textArray.add(s.substring(lastIdx));
                    text.showText(textArray);
                }
            } else {
                if (isJustified && hScale != lastHScale) {
                    lastHScale = hScale;
                    text.setWordSpacing(baseWordSpacing / hScale);
                    text.setCharacterSpacing(baseCharacterSpacing / hScale + text.getCharacterSpacing());
                }
                text.showText(chunk.toString());
            }
            if (rise != 0) text.setTextRise(0);
            if (color != null) text.resetRGBColorFill();
            if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL) text.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
            if (strokeColor != null) text.resetRGBColorStroke();
            if (strokeWidth != 1) text.setLineWidth(1);
            if (chunk.isAttribute(Chunk.SKEW) || chunk.isAttribute(Chunk.HSCALE)) {
                adjustMatrix = true;
                text.setTextMatrix(xMarker, yMarker);
            }
            if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
                text.setCharacterSpacing(baseCharacterSpacing);
            }
        }
        if (isJustified) {
            text.setWordSpacing(0);
            text.setCharacterSpacing(0);
            if (line.isNewlineSplit()) lastBaseFactor = 0;
        }
        if (adjustMatrix) text.moveText(baseXMarker - text.getXTLM(), 0);
        currentValues[0] = currentFont;
        currentValues[1] = new Float(lastBaseFactor);
        return lastX;
    }

    protected Indentation indentation = new Indentation();

    /**
     * @since	2.0.8 (PdfDocument was package-private before)
     */
    public static class Indentation {

        /** This represents the current indentation of the PDF Elements on the left side. */
        float indentLeft = 0;

        /** Indentation to the left caused by a section. */
        float sectionIndentLeft = 0;

        /** This represents the current indentation of the PDF Elements on the left side. */
        float listIndentLeft = 0;

        /** This is the indentation caused by an image on the left. */
        float imageIndentLeft = 0;

        /** This represents the current indentation of the PDF Elements on the right side. */
        float indentRight = 0;

        /** Indentation to the right caused by a section. */
        float sectionIndentRight = 0;

        /** This is the indentation caused by an image on the right. */
        float imageIndentRight = 0;

        /** This represents the current indentation of the PDF Elements on the top side. */
        float indentTop = 0;

        /** This represents the current indentation of the PDF Elements on the bottom side. */
        float indentBottom = 0;
    }

    /**
     * Gets the indentation on the left side.
     *
     * @return	a margin
     */
    protected float indentLeft() {
        return left(indentation.indentLeft + indentation.listIndentLeft + indentation.imageIndentLeft + indentation.sectionIndentLeft);
    }

    /**
     * Gets the indentation on the right side.
     *
     * @return	a margin
     */
    protected float indentRight() {
        return right(indentation.indentRight + indentation.sectionIndentRight + indentation.imageIndentRight);
    }

    /**
     * Gets the indentation on the top side.
     *
     * @return	a margin
     */
    protected float indentTop() {
        return top(indentation.indentTop);
    }

    /**
     * Gets the indentation on the bottom side.
     *
     * @return	a margin
     */
    float indentBottom() {
        return bottom(indentation.indentBottom);
    }

    /**
     * Adds extra space.
     * This method should probably be rewritten.
     */
    protected void addSpacing(final float extraspace, final float oldleading, Font f) {
        if (extraspace == 0) return;
        if (pageEmpty) return;
        if (currentHeight + line.height() + leading > indentTop() - indentBottom()) return;
        leading = extraspace;
        carriageReturn();
        if (f.isUnderlined() || f.isStrikethru()) {
            f = new Font(f);
            int style = f.getStyle();
            style &= ~Font.UNDERLINE;
            style &= ~Font.STRIKETHRU;
            f.setStyle(style);
        }
        Chunk space = new Chunk(" ", f);
        space.process(this);
        carriageReturn();
        leading = oldleading;
    }

    /** some meta information about the Document. */
    protected PdfInfo info = new PdfInfo();

    /**
     * Gets the <CODE>PdfInfo</CODE>-object.
     *
     * @return	<CODE>PdfInfo</COPE>
     */
    PdfInfo getInfo() {
        return info;
    }

    /**
     * Gets the <CODE>PdfCatalog</CODE>-object.
     *
     * @param pages an indirect reference to this document pages
     * @return <CODE>PdfCatalog</CODE>
     */
    PdfCatalog getCatalog(final PdfIndirectReference pages) {
        PdfCatalog catalog = new PdfCatalog(pages, writer);
        if (rootOutline.getKids().size() > 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES);
            catalog.put(PdfName.OUTLINES, rootOutline.indirectReference());
        }
        writer.getPdfVersion().addToCatalog(catalog);
        viewerPreferences.addToCatalog(catalog);
        if (pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pageLabels.getDictionary(writer));
        }
        catalog.addNames(localDestinations, getDocumentLevelJS(), documentFileAttachment, writer);
        if (openActionName != null) {
            PdfAction action = getLocalGotoAction(openActionName);
            catalog.setOpenAction(action);
        } else if (openActionAction != null) catalog.setOpenAction(openActionAction);
        if (additionalActions != null) {
            catalog.setAdditionalActions(additionalActions);
        }
        if (collection != null) {
            catalog.put(PdfName.COLLECTION, collection);
        }
        if (annotationsImp.hasValidAcroForm()) {
            try {
                catalog.put(PdfName.ACROFORM, writer.addToBody(annotationsImp.getAcroForm()).getIndirectReference());
            } catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }
        return catalog;
    }

    /** This is the root outline of the document. */
    protected PdfOutline rootOutline;

    /** This is the current <CODE>PdfOutline</CODE> in the hierarchy of outlines. */
    protected PdfOutline currentOutline;

    /**
     * Adds a named outline to the document .
     * @param outline the outline to be added
     * @param name the name of this local destination
     */
    void addOutline(final PdfOutline outline, final String name) {
        localDestination(name, outline.getPdfDestination());
    }

    /**
     * Gets the root outline. All the outlines must be created with a parent.
     * The first level is created with this outline.
     * @return the root outline
     */
    public PdfOutline getRootOutline() {
        return rootOutline;
    }

    /**
     * Updates the count in the outlines.
     */
    void calculateOutlineCount() {
        if (rootOutline.getKids().size() == 0) return;
        traverseOutlineCount(rootOutline);
    }

    /**
     * Recursive method to update the count in the outlines.
     */
    void traverseOutlineCount(final PdfOutline outline) {
        ArrayList<PdfOutline> kids = outline.getKids();
        PdfOutline parent = outline.parent();
        if (kids.isEmpty()) {
            if (parent != null) {
                parent.setCount(parent.getCount() + 1);
            }
        } else {
            for (int k = 0; k < kids.size(); ++k) {
                traverseOutlineCount(kids.get(k));
            }
            if (parent != null) {
                if (outline.isOpen()) {
                    parent.setCount(outline.getCount() + parent.getCount() + 1);
                } else {
                    parent.setCount(parent.getCount() + 1);
                    outline.setCount(-outline.getCount());
                }
            }
        }
    }

    /**
     * Writes the outline tree to the body of the PDF document.
     */
    void writeOutlines() throws IOException {
        if (rootOutline.getKids().size() == 0) return;
        outlineTree(rootOutline);
        writer.addToBody(rootOutline, rootOutline.indirectReference());
    }

    /**
     * Recursive method used to write outlines.
     */
    void outlineTree(final PdfOutline outline) throws IOException {
        outline.setIndirectReference(writer.getPdfIndirectReference());
        if (outline.parent() != null) outline.put(PdfName.PARENT, outline.parent().indirectReference());
        ArrayList<PdfOutline> kids = outline.getKids();
        int size = kids.size();
        for (int k = 0; k < size; ++k) outlineTree(kids.get(k));
        for (int k = 0; k < size; ++k) {
            if (k > 0) kids.get(k).put(PdfName.PREV, kids.get(k - 1).indirectReference());
            if (k < size - 1) kids.get(k).put(PdfName.NEXT, kids.get(k + 1).indirectReference());
        }
        if (size > 0) {
            outline.put(PdfName.FIRST, kids.get(0).indirectReference());
            outline.put(PdfName.LAST, kids.get(size - 1).indirectReference());
        }
        for (int k = 0; k < size; ++k) {
            PdfOutline kid = kids.get(k);
            writer.addToBody(kid, kid.indirectReference());
        }
    }

    /** Contains the Viewer preferences of this PDF document. */
    protected PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();

    /** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences#setViewerPreferences(int) */
    void setViewerPreferences(final int preferences) {
        this.viewerPreferences.setViewerPreferences(preferences);
    }

    /** @see com.itextpdf.text.pdf.interfaces.PdfViewerPreferences#addViewerPreference(com.itextpdf.text.pdf.PdfName, com.itextpdf.text.pdf.PdfObject) */
    void addViewerPreference(final PdfName key, final PdfObject value) {
        this.viewerPreferences.addViewerPreference(key, value);
    }

    protected PdfPageLabels pageLabels;

    /**
     * Sets the page labels
     * @param pageLabels the page labels
     */
    void setPageLabels(final PdfPageLabels pageLabels) {
        this.pageLabels = pageLabels;
    }

    /**
     * Implements a link to other part of the document. The jump will
     * be made to a local destination with the same name, that must exist.
     * @param name the name for this link
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void localGoto(final String name, final float llx, final float lly, final float urx, final float ury) {
        PdfAction action = getLocalGotoAction(name);
        annotationsImp.addPlainAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, action));
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * @param name the name to jump to
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void remoteGoto(final String filename, final String name, final float llx, final float lly, final float urx, final float ury) {
        annotationsImp.addPlainAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, name)));
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * @param page the page to jump to
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void remoteGoto(final String filename, final int page, final float llx, final float lly, final float urx, final float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, page)));
    }

    /** Implements an action in an area.
     * @param action the <CODE>PdfAction</CODE>
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void setAction(final PdfAction action, final float llx, final float lly, final float urx, final float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, action));
    }

    /**
     * Stores the destinations keyed by name. Value is a <Code>Destination</Code>.
     */
    protected TreeMap<String, Destination> localDestinations = new TreeMap<String, Destination>();

    PdfAction getLocalGotoAction(final String name) {
        PdfAction action;
        Destination dest = localDestinations.get(name);
        if (dest == null) dest = new Destination();
        if (dest.action == null) {
            if (dest.reference == null) {
                dest.reference = writer.getPdfIndirectReference();
            }
            action = new PdfAction(dest.reference);
            dest.action = action;
            localDestinations.put(name, dest);
        } else {
            action = dest.action;
        }
        return action;
    }

    /**
     * The local destination to where a local goto with the same
     * name will jump to.
     * @param name the name of this local destination
     * @param destination the <CODE>PdfDestination</CODE> with the jump coordinates
     * @return <CODE>true</CODE> if the local destination was added,
     * <CODE>false</CODE> if a local destination with the same name
     * already existed
     */
    boolean localDestination(final String name, final PdfDestination destination) {
        Destination dest = localDestinations.get(name);
        if (dest == null) dest = new Destination();
        if (dest.destination != null) return false;
        dest.destination = destination;
        localDestinations.put(name, dest);
        if (!destination.hasPage()) destination.addPage(writer.getCurrentPage());
        return true;
    }

    /**
     * Stores a list of document level JavaScript actions.
     */
    int jsCounter;

    protected HashMap<String, PdfObject> documentLevelJS = new HashMap<String, PdfObject>();

    protected static final DecimalFormat SIXTEEN_DIGITS = new DecimalFormat("0000000000000000");

    void addJavaScript(final PdfAction js) {
        if (js.get(PdfName.JS) == null) throw new RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        try {
            documentLevelJS.put(SIXTEEN_DIGITS.format(jsCounter++), writer.addToBody(js).getIndirectReference());
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    void addJavaScript(final String name, final PdfAction js) {
        if (js.get(PdfName.JS) == null) throw new RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        try {
            documentLevelJS.put(name, writer.addToBody(js).getIndirectReference());
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    HashMap<String, PdfObject> getDocumentLevelJS() {
        return documentLevelJS;
    }

    protected HashMap<String, PdfObject> documentFileAttachment = new HashMap<String, PdfObject>();

    void addFileAttachment(String description, final PdfFileSpecification fs) throws IOException {
        if (description == null) {
            PdfString desc = (PdfString) fs.get(PdfName.DESC);
            if (desc == null) {
                description = "";
            } else {
                description = PdfEncodings.convertToString(desc.getBytes(), null);
            }
        }
        fs.addDescription(description, true);
        if (description.length() == 0) description = "Unnamed";
        String fn = PdfEncodings.convertToString(new PdfString(description, PdfObject.TEXT_UNICODE).getBytes(), null);
        int k = 0;
        while (documentFileAttachment.containsKey(fn)) {
            ++k;
            fn = PdfEncodings.convertToString(new PdfString(description + " " + k, PdfObject.TEXT_UNICODE).getBytes(), null);
        }
        documentFileAttachment.put(fn, fs.getReference());
    }

    HashMap<String, PdfObject> getDocumentFileAttachment() {
        return documentFileAttachment;
    }

    protected String openActionName;

    void setOpenAction(final String name) {
        openActionName = name;
        openActionAction = null;
    }

    protected PdfAction openActionAction;

    void setOpenAction(final PdfAction action) {
        openActionAction = action;
        openActionName = null;
    }

    protected PdfDictionary additionalActions;

    void addAdditionalAction(final PdfName actionType, final PdfAction action) {
        if (additionalActions == null) {
            additionalActions = new PdfDictionary();
        }
        if (action == null) additionalActions.remove(actionType); else additionalActions.put(actionType, action);
        if (additionalActions.size() == 0) additionalActions = null;
    }

    protected PdfCollection collection;

    /**
     * Sets the collection dictionary.
     * @param collection a dictionary of type PdfCollection
     */
    public void setCollection(final PdfCollection collection) {
        this.collection = collection;
    }

    PdfAnnotationsImp annotationsImp;

    /**
     * Gets the AcroForm object.
     * @return the PdfAcroform object of the PdfDocument
     */
    PdfAcroForm getAcroForm() {
        return annotationsImp.getAcroForm();
    }

    void setSigFlags(final int f) {
        annotationsImp.setSigFlags(f);
    }

    void addCalculationOrder(final PdfFormField formField) {
        annotationsImp.addCalculationOrder(formField);
    }

    void addAnnotation(final PdfAnnotation annot) {
        pageEmpty = false;
        annotationsImp.addAnnotation(annot);
    }

    protected int markPoint;

    int getMarkPoint() {
        return markPoint;
    }

    void incMarkPoint() {
        ++markPoint;
    }

    /** This is the size of the next page. */
    protected Rectangle nextPageSize = null;

    /** This is the size of the several boxes of the current Page. */
    protected HashMap<String, PdfRectangle> thisBoxSize = new HashMap<String, PdfRectangle>();

    /** This is the size of the several boxes that will be used in
     * the next page. */
    protected HashMap<String, PdfRectangle> boxSize = new HashMap<String, PdfRectangle>();

    void setCropBoxSize(final Rectangle crop) {
        setBoxSize("crop", crop);
    }

    void setBoxSize(final String boxName, final Rectangle size) {
        if (size == null) boxSize.remove(boxName); else boxSize.put(boxName, new PdfRectangle(size));
    }

    protected void setNewPageSizeAndMargins() {
        pageSize = nextPageSize;
        if (marginMirroring && (getPageNumber() & 1) == 0) {
            marginRight = nextMarginLeft;
            marginLeft = nextMarginRight;
        } else {
            marginLeft = nextMarginLeft;
            marginRight = nextMarginRight;
        }
        if (marginMirroringTopBottom && (getPageNumber() & 1) == 0) {
            marginTop = nextMarginBottom;
            marginBottom = nextMarginTop;
        } else {
            marginTop = nextMarginTop;
            marginBottom = nextMarginBottom;
        }
        text = new PdfContentByte(writer);
        text.reset();
        text.beginText();
        textEmptySize = text.size();
        text.moveText(left(), top());
    }

    /**
     * Gives the size of a trim, art, crop or bleed box, or null if not defined.
     * @param boxName crop, trim, art or bleed
     */
    Rectangle getBoxSize(final String boxName) {
        PdfRectangle r = thisBoxSize.get(boxName);
        if (r != null) return r.getRectangle();
        return null;
    }

    /** This checks if the page is empty. */
    private boolean pageEmpty = true;

    void setPageEmpty(final boolean pageEmpty) {
        this.pageEmpty = pageEmpty;
    }

    boolean isPageEmpty() {
        return writer == null || writer.getDirectContent().size() == 0 && writer.getDirectContentUnder().size() == 0 && (pageEmpty || writer.isPaused());
    }

    /**
     * Sets the display duration for the page (for presentations)
     * @param seconds   the number of seconds to display the page
     */
    void setDuration(final int seconds) {
        if (seconds > 0) writer.addPageDictEntry(PdfName.DUR, new PdfNumber(seconds));
    }

    /**
     * Sets the transition for the page
     * @param transition   the PdfTransition object
     */
    void setTransition(final PdfTransition transition) {
        writer.addPageDictEntry(PdfName.TRANS, transition.getTransitionDictionary());
    }

    protected PdfDictionary pageAA = null;

    void setPageAction(final PdfName actionType, final PdfAction action) {
        if (pageAA == null) {
            pageAA = new PdfDictionary();
        }
        pageAA.put(actionType, action);
    }

    void setThumbnail(final Image image) throws PdfException, DocumentException {
        writer.addPageDictEntry(PdfName.THUMB, writer.getImageReference(writer.addDirectImageSimple(image)));
    }

    /** This are the page resources of the current Page. */
    protected PageResources pageResources;

    PageResources getPageResources() {
        return pageResources;
    }

    /** Holds value of property strictImageSequence. */
    protected boolean strictImageSequence = false;

    /** Getter for property strictImageSequence.
     * @return Value of property strictImageSequence.
     *
     */
    boolean isStrictImageSequence() {
        return this.strictImageSequence;
    }

    /** Setter for property strictImageSequence.
     * @param strictImageSequence New value of property strictImageSequence.
     *
     */
    void setStrictImageSequence(final boolean strictImageSequence) {
        this.strictImageSequence = strictImageSequence;
    }

    /** This is the position where the image ends. */
    protected float imageEnd = -1;

    /**
	 * Method added by Pelikan Stephan
	 */
    public void clearTextWrap() {
        float tmpHeight = imageEnd - currentHeight;
        if (line != null) {
            tmpHeight += line.height();
        }
        if (imageEnd > -1 && tmpHeight > 0) {
            carriageReturn();
            currentHeight += tmpHeight;
        }
    }

    /** This is the image that could not be shown on a previous page. */
    protected Image imageWait = null;

    /**
     * Adds an image to the document.
     * @param image the <CODE>Image</CODE> to add
     * @throws PdfException on error
     * @throws DocumentException on error
     */
    protected void add(final Image image) throws PdfException, DocumentException {
        if (image.hasAbsoluteY()) {
            graphics.addImage(image);
            pageEmpty = false;
            return;
        }
        if (currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom()) {
            if (!strictImageSequence && imageWait == null) {
                imageWait = image;
                return;
            }
            newPage();
            if (currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom()) {
                imageWait = image;
                return;
            }
        }
        pageEmpty = false;
        if (image == imageWait) imageWait = null;
        boolean textwrap = (image.getAlignment() & Image.TEXTWRAP) == Image.TEXTWRAP && !((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE);
        boolean underlying = (image.getAlignment() & Image.UNDERLYING) == Image.UNDERLYING;
        float diff = leading / 2;
        if (textwrap) {
            diff += leading;
        }
        float lowerleft = indentTop() - currentHeight - image.getScaledHeight() - diff;
        float mt[] = image.matrix();
        float startPosition = indentLeft() - mt[4];
        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) startPosition = indentRight() - image.getScaledWidth() - mt[4];
        if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) startPosition = indentLeft() + (indentRight() - indentLeft() - image.getScaledWidth()) / 2 - mt[4];
        if (image.hasAbsoluteX()) startPosition = image.getAbsoluteX();
        if (textwrap) {
            if (imageEnd < 0 || imageEnd < currentHeight + image.getScaledHeight() + diff) {
                imageEnd = currentHeight + image.getScaledHeight() + diff;
            }
            if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
                indentation.imageIndentRight += image.getScaledWidth() + image.getIndentationLeft();
            } else {
                indentation.imageIndentLeft += image.getScaledWidth() + image.getIndentationRight();
            }
        } else {
            if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) startPosition -= image.getIndentationRight(); else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) startPosition += image.getIndentationLeft() - image.getIndentationRight(); else startPosition += image.getIndentationLeft();
        }
        graphics.addImage(image, mt[0], mt[1], mt[2], mt[3], startPosition, lowerleft - mt[5]);
        if (!(textwrap || underlying)) {
            currentHeight += image.getScaledHeight() + diff;
            flushLines();
            text.moveText(0, -(image.getScaledHeight() + diff));
            newLine();
        }
    }

    /** Adds a <CODE>PdfPTable</CODE> to the document.
     * @param ptable the <CODE>PdfPTable</CODE> to be added to the document.
     * @throws DocumentException on error
     */
    void addPTable(final PdfPTable ptable) throws DocumentException {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        if (ptable.getKeepTogether() && !fitsPage(ptable, 0f) && currentHeight > 0) {
            newPage();
        }
        if (currentHeight == 0) {
            ct.setAdjustFirstLine(false);
        }
        ct.addElement(ptable);
        boolean he = ptable.isHeadersInEvent();
        ptable.setHeadersInEvent(true);
        int loop = 0;
        while (true) {
            ct.setSimpleColumn(indentLeft(), indentBottom(), indentRight(), indentTop() - currentHeight);
            int status = ct.go();
            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                text.moveText(0, ct.getYLine() - indentTop() + currentHeight);
                currentHeight = indentTop() - ct.getYLine();
                break;
            }
            if (indentTop() - currentHeight == ct.getYLine()) ++loop; else loop = 0;
            if (loop == 3) {
                throw new DocumentException(MessageLocalization.getComposedMessage("infinite.table.loop"));
            }
            newPage();
        }
        ptable.setHeadersInEvent(he);
    }

    /**
     * Checks if a <CODE>PdfPTable</CODE> fits the current page of the <CODE>PdfDocument</CODE>.
     *
     * @param	table	the table that has to be checked
     * @param	margin	a certain margin
     * @return	<CODE>true</CODE> if the <CODE>PdfPTable</CODE> fits the page, <CODE>false</CODE> otherwise.
     */
    boolean fitsPage(final PdfPTable table, final float margin) {
        if (!table.isLockedWidth()) {
            float totalWidth = (indentRight() - indentLeft()) * table.getWidthPercentage() / 100;
            table.setTotalWidth(totalWidth);
        }
        ensureNewLine();
        Float spaceNeeded = table.isSkipFirstHeader() ? table.getTotalHeight() - table.getHeaderHeight() : table.getTotalHeight();
        return spaceNeeded + (currentHeight > 0 ? table.spacingBefore() : 0f) <= indentTop() - currentHeight - indentBottom() - margin;
    }

    /**
     * @since 5.0.1
     */
    public class Destination {

        public PdfAction action;

        public PdfIndirectReference reference;

        public PdfDestination destination;
    }
}
