package org.equanda.tapestry.components.select.equandaSelectResults;

import org.equanda.persistence.om.EquandaPersistenceException;
import org.equanda.persistence.om.EquandaProxy;
import org.equanda.reporting.servlet.ServletResolver;
import org.equanda.tapestry.components.EquandaBaseComponent;
import org.equanda.tapestry.components.select.SelectObject;
import org.equanda.tapestry.components.shared.ConfigurationKeys;
import org.equanda.tapestry.components.shared.DescriptionFactory;
import org.equanda.tapestry.components.equandaView.EquandaView;
import org.equanda.tapestry.model.GMField;
import org.equanda.tapestry.model.GMSelect;
import org.equanda.tapestry.model.GMTable;
import org.equanda.tapestry.navigation.HasProxyParameter;
import org.equanda.tapestry.navigation.HasSelectParameter;
import org.equanda.tapestry.pages.EquandaBasePage;
import org.equanda.tapestry.pages.edit.EditPage;
import org.equanda.tapestry.pages.shared.AddViewEditManipulation;
import org.equanda.tapestry.pages.view.ViewPage;
import org.equanda.tapestry.rights.AccessRight;
import org.equanda.tapestry.useradmin.cache.LoaderType;
import org.equanda.tapestry.util.ResourceUtil;
import org.equanda.tapestry.util.EquandaProxyAccessor;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.tapestry.IAsset;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Asset;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.components.ForBean;
import org.apache.tapestry.util.ContentType;
import org.apache.tapestry.web.WebResponse;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 * Component to display the results of a select.
 *
 * @author <a href="mailto:andrei@paragon-software.ro">Andrei Chiritescu</a>
 */
@ComponentClass(allowInformalParameters = true, allowBody = true)
public abstract class EquandaSelectResults extends EquandaBaseComponent {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EquandaSelectResults.class);

    @Parameter(name = "selectObject", required = true)
    public abstract SelectObject getSelectObject();

    public abstract void setSelectObject(SelectObject obj);

    @Parameter(name = "selectDescription", required = true)
    public abstract GMSelect getSelectDescription();

    @Parameter(name = "pageSize", required = true)
    public abstract int getPageSize();

    @Parameter(name = "displayedPageNumber", defaultValue = "0")
    public abstract int getDisplayedPageNumber();

    public abstract void setDisplayedPageNumber(int no);

    @Parameter(name = "autoRunSelect", defaultValue = "false")
    public abstract boolean isAutoRunSelect();

    public abstract void setAutoRunSelect(boolean isSubmitted);

    @Parameter(name = "sortField")
    public abstract String getSortField();

    public abstract void setSortField(String fieldName);

    @Parameter(name = "sortAscending", defaultValue = "false")
    public abstract boolean isSortAscending();

    public abstract void setSortAscending(boolean type);

    public abstract EquandaProxy getRowProxy();

    public abstract Object getEachMultipleValue();

    public abstract GMField getFieldDescription();

    public abstract int getIndex();

    public abstract int getSize();

    public abstract void setSize(int size);

    public abstract int getPageCount();

    public abstract void setPageCount(int count);

    public abstract boolean isPrevious();

    public abstract void setPrevious(boolean hasPrevious);

    public abstract boolean isNext();

    public abstract void setNext(boolean hasNext);

    public abstract List<EquandaProxy> getResultList();

    public abstract void setResultList(List<EquandaProxy> list);

    public abstract String getError();

    public abstract void setError(String s);

    @Asset(value = "/org/equanda/tapestry/components/select/equandaSelectResults/EquandaSelectResults.html")
    public abstract IAsset get$template();

    @Asset(value = "/images/arrow-up.gif")
    public abstract IAsset getImageUp();

    @Asset(value = "/images/arrow-down.gif")
    public abstract IAsset getImageDown();

    @Asset(value = "/images/select-view.gif")
    public abstract IAsset getImageView();

    @Asset(value = "/images/select-edit.gif")
    public abstract IAsset getImageEdit();

    @Asset(value = "/images/select-remove.gif")
    public abstract IAsset getImageRemove();

    @Asset(value = "/images/select-select.gif")
    public abstract IAsset getImageSelect();

    @Component(type = "For", id = "foreachRow", bindings = { "source       = getResultList()", "value        = RowProxy", "index        = index", "volatile     = true" })
    public abstract ForBean getForeachRowComponent();

    @Component(type = "For", id = "foreachHeaderColumn", bindings = { "source       = getDisplayedColumns(true)", "value        = FieldDescription", "index        = index", "volatile     = true" })
    public abstract ForBean getForeachHeaderColumnComponent();

    @Component(type = "For", id = "foreachValueColumn", bindings = { "source       = getDisplayedColumns(true)", "value        = FieldDescription", "index        = index", "volatile     = true" })
    public abstract ForBean getForeachValueColumnComponent();

    @Component(type = "For", id = "foreachMultipleValueColumn", bindings = { "source       = getColumnValue()", "value        = EachMultipleValue", "index        = index", "volatile     = true" })
    public abstract ForBean getForeachMultipleValueColumnComponent();

    @Component(type = "EquandaView", id = "equandaView", bindings = { "tableDescription    = getTable()", "proxy               = getPage().getPageParameters().getProxy()" })
    public abstract EquandaView getEquandaViewComponent();

    public GMTable getTable() {
        if (!getSelectObject().isUseCustomQuery()) {
            return getSelectDescription().getTable();
        } else {
            return DescriptionFactory.getTableDescription(getSelectObject().getTableName());
        }
    }

    @Override
    protected void renderComponent(IMarkupWriter iMarkupWriter, IRequestCycle iRequestCycle) {
        if (!iRequestCycle.isRewinding() && isAutoRunSelect()) {
            select(getDisplayedPageNumber());
        }
        super.renderComponent(iMarkupWriter, iRequestCycle);
    }

    public String getTableName() {
        return getTable().getName();
    }

    public void select(int pageNumber) {
        List<EquandaProxy> allList;
        try {
            allList = getSelectObject().select();
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException(), e.getTargetException());
            setError(getTranslatedExceptionMessage(e.getTargetException()));
            return;
        } catch (Exception e) {
            log.error(e, e);
            setError(getTranslatedExceptionMessage(e));
            return;
        }
        int size = allList.size();
        setPageCount(calcPageCount(size));
        setSize(size);
        int indexStart = pageNumber * getPageSize();
        int indexEnd = (pageNumber + 1) * getPageSize();
        if (indexEnd > size) {
            if (indexStart > size) {
                indexStart = Math.max(0, size - getPageSize());
            }
            indexEnd = size;
            pageNumber = getPageCount() - 1;
        }
        if (pageNumber < 0) pageNumber = 0;
        if (log.isDebugEnabled()) {
            log.debug("EquandaSelectResults.select - start index = " + indexStart + " end index = " + indexEnd);
        }
        List<EquandaProxy> resultList = new ArrayList<EquandaProxy>(indexEnd - indexStart);
        for (int i = indexStart; i < indexEnd; i++) resultList.add(allList.get(i));
        if (getSortField() != null) {
            Collections.sort(resultList, new EquandaProxyFieldComparator(DescriptionFactory.getFieldDescription(getSelectObject().getTableName(), getSortField()), isSortAscending()));
        }
        setResultList(resultList);
        setDisplayedPageNumber(pageNumber);
        setPrevious(pageNumber > 0);
        setNext(pageNumber + 1 < getPageCount());
        setAutoRunSelect(true);
        if (size == 1) {
            ((HasProxyParameter) getNavPage().getPageParameters()).setProxy(resultList.get(0));
        }
    }

    public void changePage(int pageNumber) {
        if (log.isDebugEnabled()) log.debug("EquandaSelectResults.changePage - get page no = " + pageNumber);
        select(pageNumber);
    }

    public void sortBy(String fieldName, boolean ascending) {
        if (log.isDebugEnabled()) {
            log.debug("EquandaSelectResults.sortBy - sort by field = " + fieldName + "  order ascending : " + ascending);
        }
        setSortField(fieldName);
        setSortAscending(ascending);
        select(getDisplayedPageNumber());
    }

    public void remove(String uoid) {
        if (log.isDebugEnabled()) log.debug("EquandaSelectResults.remove - remove proxy uoid = " + uoid);
        try {
            EquandaProxyAccessor.selectUoid(getTableName(), uoid).removeEntityBean();
        } catch (EquandaPersistenceException upe) {
            log.error(upe, upe);
            setError(getTranslatedExceptionMessage(upe));
        }
        select(getDisplayedPageNumber());
    }

    public void edit(String uoid) throws Exception {
        boolean isThisTheReturnPage = !getNavigationManager().hasReturnPage(getNavPage()) || !hasSelect();
        EditPage page = (EditPage) getNavigationManager().getForwardPage(getNavPage(), "EditPage", isThisTheReturnPage);
        page.getPageParameters().setTableName(getTableName());
        page.getPageParameters().setProxy(EquandaProxyAccessor.selectUoid(getTableName(), uoid));
        page.getPageParameters().setShowSelect(hasSelect());
        getNavigationManager().forward(page);
    }

    public void view(String uoid) throws Exception {
        viewLink(uoid, getTableName());
    }

    public void viewLink(String uoid, String tableName) throws Exception {
        boolean isThisTheReturnPage = !getNavigationManager().hasReturnPage(getNavPage()) || !hasSelect();
        ViewPage page = (ViewPage) getNavigationManager().getForwardPage(getNavPage(), "ViewPage", isThisTheReturnPage);
        page.getPageParameters().setTableName(tableName);
        page.getPageParameters().setProxy(EquandaProxyAccessor.selectUoid(tableName, uoid));
        page.getPageParameters().setShowSelect(hasSelect());
        getNavigationManager().forward(page);
    }

    public void select(String uoid) throws Exception {
        ((HasProxyParameter) getNavPage().getPageParameters()).setProxy(EquandaProxyAccessor.selectUoid(getTableName(), uoid));
        AddViewEditManipulation.select(getNavPage(), null, getTableName());
    }

    private float[] getReportPercents(List<GMField> fields) {
        float[] percents = new float[fields.size()];
        int noPercentFields = 0;
        int percentLeft = 100;
        for (int i = 0; i < fields.size(); i++) {
            float percent = getSessionUserAdmin().getReportPercentage(fields.get(i).getId());
            if (percent == 0) {
                noPercentFields++;
            } else {
                percents[i] = percent;
                percentLeft -= percent;
            }
        }
        if (noPercentFields > 0) {
            if (percentLeft <= 0) percentLeft = 1;
            int percentForEachField = percentLeft / noPercentFields;
            for (int i = 0; i < fields.size(); i++) {
                if (percents[i] == 0) percents[i] = percentForEachField;
            }
        }
        return percents;
    }

    public void printReport(boolean portrait) {
        WebResponse response = this.getWebResponse();
        OutputStream out;
        try {
            out = response.getOutputStream(new ContentType("application/pdf"));
            response.setHeader("Content-disposition", "inline; filename=Report.pdf");
        } catch (Exception e) {
            log.error(e, e);
            return;
        }
        Document document = new Document(portrait ? PageSize.A4 : PageSize.A4.rotate(), 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(document, out);
            ServletResolver resolver = new ServletResolver(((EquandaBasePage) getPage()).getServletContext());
            Phrase top = new Phrase();
            Image img = Image.getInstance(resolver.getImageRealPath(ResourceUtil.getConfigString(ConfigurationKeys.LOGO_FILE)));
            img.scaleToFit(300, 15);
            Chunk chk = new Chunk(img, 0, 0);
            top.add(chk);
            top.add(getMessages().getMessage("Table") + " " + getMessages().getMessage("table." + getSelectDescription().getTable().getName() + ".label") + " " + getMessages().getMessage("Report") + " " + getMessages().getMessage("select." + getSelectDescription().getTable().getName() + "." + getSelectDescription().getName() + ".label"));
            HeaderFooter header = new HeaderFooter(top, false);
            header.setAlignment(Element.ALIGN_LEFT);
            document.setHeader(header);
            document.setFooter(header);
            document.open();
            List<GMField> displayedFields = getDisplayedColumns(false);
            float[] percents = getReportPercents(displayedFields);
            PdfPTable table = new PdfPTable(percents);
            for (GMField field : displayedFields) {
                if (field instanceof FakeGmField) {
                    table.addCell("UOID");
                } else {
                    table.addCell(field.getName());
                }
            }
            table.setHeaderRows(1);
            List<EquandaProxy> results = getSelectObject().select();
            for (EquandaProxy proxy : results) {
                for (GMField field : displayedFields) {
                    if (field instanceof FakeGmField) {
                        table.addCell(proxy.getUOID().getId());
                    } else {
                        String str = "";
                        Object obj = EquandaProxyAccessor.getField(proxy, field.getName());
                        if (obj != null) {
                            int i = 0;
                            if (obj instanceof Collection) {
                                for (Object el : ((Collection) obj)) {
                                    if (el instanceof EquandaProxy) {
                                        str += EquandaProxyAccessor.getDisplay((EquandaProxy) el);
                                    } else {
                                        str += el.toString();
                                    }
                                    if (i < ((Collection) obj).size() - 1) str += ", ";
                                    i++;
                                }
                            } else if (obj instanceof EquandaProxy) {
                                str = EquandaProxyAccessor.getDisplay((EquandaProxy) obj);
                            } else if (obj instanceof Boolean) {
                                str = getMessages().getMessage(((Boolean) obj) ? "report_yes" : "report_no");
                            } else {
                                str = obj.toString();
                            }
                        } else {
                            str = "";
                        }
                        table.addCell(str);
                    }
                }
            }
            document.add(table);
        } catch (Exception e) {
            log.error(e, e);
        }
        try {
            if (document.isOpen()) document.close();
            out.flush();
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public String getTableLabel() {
        return getMessages().getMessage("table." + getSelectDescription().getTable().getName() + ".label");
    }

    private int calcPageCount(int size) {
        double count = (double) size / getPageSize();
        return (int) (Math.ceil(count));
    }

    public boolean isColumnSortable() {
        return !(getFieldDescription() instanceof FakeGmField || getFieldDescription().isMultiple() || getFieldDescription().getFieldType().isLink());
    }

    public boolean isFakeColumn() {
        return getFieldDescription() instanceof FakeGmField;
    }

    public boolean isViewColumn() {
        return isFakeColumn() && ((FakeGmField) getFieldDescription()).isType(FakeGmField.Type.VIEW);
    }

    public boolean isEditColumn() {
        return isFakeColumn() && ((FakeGmField) getFieldDescription()).isType(FakeGmField.Type.EDIT);
    }

    public boolean isRemoveColumn() {
        return isFakeColumn() && ((FakeGmField) getFieldDescription()).isType(FakeGmField.Type.REMOVE);
    }

    public boolean isUoidColumn() {
        return isFakeColumn() && ((FakeGmField) getFieldDescription()).isType(FakeGmField.Type.UOID);
    }

    public boolean isSelectColumn() {
        return isFakeColumn() && ((FakeGmField) getFieldDescription()).isType(FakeGmField.Type.SELECT);
    }

    public boolean isLinkColumn() {
        return getFieldDescription().getFieldType().isLink() && getFieldDescription().getUpdateField() == null;
    }

    public boolean isMultipleColumn() {
        return getFieldDescription().isMultiple();
    }

    public boolean isSortedColumn() {
        return getSortField() != null && getFieldDescription().getName().equals(getSortField());
    }

    public IAsset getSortingImageAsset() {
        return isSortAscending() ? getImageDown() : getImageUp();
    }

    public Object getColumnMultipleValue() {
        return getEachMultipleValue();
    }

    public Object getDisplay(EquandaProxy proxy) {
        try {
            return EquandaProxyAccessor.getDisplay(proxy, this);
        } catch (Exception e) {
            log.error(e, e);
        }
        return proxy != null ? proxy : "Not available";
    }

    public Object getColumnWidth() {
        if (isFakeColumn()) return "*px";
        return getSessionUserAdmin().getListWidth(getFieldDescription().getId());
    }

    public Object getColumnValue() {
        GMField field = getFieldDescription();
        if (field instanceof FakeGmField) {
            FakeGmField fake = (FakeGmField) field;
            if (fake.isType(FakeGmField.Type.EQUANDA_TYPE)) {
                return getRowProxy().getEquandaName();
            } else if (fake.isType(FakeGmField.Type.UOID)) {
                return getRowProxy().getUOID().getId();
            }
            return "";
        } else {
            try {
                return EquandaProxyAccessor.getField(getRowProxy(), field.getName());
            } catch (Exception e) {
                log.error(e, e);
                return "not available";
            }
        }
    }

    public String getColumnHeader() {
        GMField field = getFieldDescription();
        if (field instanceof FakeGmField) {
            FakeGmField fake = (FakeGmField) field;
            if (fake.isType(FakeGmField.Type.EQUANDA_TYPE)) {
                return "EquandaType";
            } else if (fake.isType(FakeGmField.Type.UOID)) {
                return "UOID";
            }
            return "";
        } else {
            String label = "field." + field.getTableName() + "." + field.getName() + ".label";
            return getMessages().getMessage(label);
        }
    }

    public List<GMField> getDisplayedColumns(boolean forSelect) {
        GMTable tableDesc = getTable();
        List<GMField> allFields = tableDesc.getFields();
        List<GMField> displayedFields = new ArrayList<GMField>();
        boolean hasPrefsInDB = false;
        for (GMField field : allFields) {
            AccessRight accessR;
            if (forSelect) {
                accessR = getSessionUserAdmin().lookupAccess(LoaderType.USER_LIST_PREF, field.getId());
            } else {
                accessR = getSessionUserAdmin().lookupAccess(LoaderType.USER_REPORT_PREF, field.getId());
            }
            if (accessR != null) hasPrefsInDB = true;
            if (getSessionUserAdmin().hasAccess(field.getId(), AccessRight.EDITABLE_ACCESS_VIEW) && accessR == AccessRight.TABLE_PREFERENCE_DISPLAY) {
                displayedFields.add(field);
            }
        }
        if (displayedFields.isEmpty() && !hasPrefsInDB) {
            for (GMField field : allFields) {
                if (field.isDisplayed() && getSessionUserAdmin().hasAccess(field.getId(), AccessRight.EDITABLE_ACCESS_VIEW)) {
                    displayedFields.add(field);
                }
            }
        }
        if (displayedFields.isEmpty() && !hasPrefsInDB) {
            for (GMField field : allFields) {
                if (field.isUnique() && getSessionUserAdmin().hasAccess(field.getId(), AccessRight.EDITABLE_ACCESS_VIEW)) {
                    displayedFields.add(field);
                }
            }
        }
        if (displayedFields.isEmpty()) {
            displayedFields.add(new FakeGmField(FakeGmField.Type.UOID));
            if (tableDesc.getTypes().size() > 1) {
                displayedFields.set(0, new FakeGmField(FakeGmField.Type.EQUANDA_TYPE));
            }
        }
        if (forSelect) {
            displayedFields.add(new FakeGmField(FakeGmField.Type.VIEW));
            displayedFields.add(new FakeGmField(FakeGmField.Type.EDIT));
            if (getSessionUserAdmin().isTableRemoveable(tableDesc)) {
                displayedFields.add(new FakeGmField(FakeGmField.Type.REMOVE));
            }
            Boolean select = hasSelect();
            if (select != null && select) {
                displayedFields.add(new FakeGmField(FakeGmField.Type.SELECT));
            }
        }
        return displayedFields;
    }

    private boolean hasSelect() {
        if (getNavPage().getPageParameters() instanceof HasSelectParameter) {
            Boolean showSelect = ((HasSelectParameter) getNavPage().getPageParameters()).isShowSelect();
            return showSelect != null && showSelect;
        }
        return false;
    }
}

class FakeGmField extends GMField {

    enum Type {

        UOID, EQUANDA_TYPE, VIEW, EDIT, REMOVE, SELECT
    }

    Type type;

    FakeGmField(Type type) {
        this.type = type;
    }

    boolean isType(Type type_) {
        return type == type_;
    }
}

class EquandaProxyFieldComparator implements Comparator<EquandaProxy> {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EquandaProxyFieldComparator.class);

    private GMField field;

    private boolean sortAscending;

    public EquandaProxyFieldComparator(GMField field, boolean sortAscending) {
        this.field = field;
        this.sortAscending = sortAscending;
    }

    public int compare(EquandaProxy o1, EquandaProxy o2) {
        try {
            int result = 0;
            if (!field.getFieldType().isLink() && !field.isMultiple()) {
                Object val1 = EquandaProxyAccessor.getField(o1, field.getName());
                Object val2 = EquandaProxyAccessor.getField(o2, field.getName());
                if (val1 == null && val2 == null) {
                    result = 0;
                } else if (val1 == null) {
                    result = -1;
                } else if (val2 == null) {
                    result = 1;
                } else if (val1 instanceof Comparable) result = ((Comparable) val1).compareTo(val2);
            }
            return sortAscending ? result : (result * -1);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e);
        }
    }
}
