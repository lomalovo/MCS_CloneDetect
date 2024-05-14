package com.vlee.servlet.inventory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.math.BigDecimal;
import java.io.*;
import com.vlee.servlet.main.*;
import com.vlee.bean.inventory.*;
import com.vlee.util.*;
import com.vlee.ejb.user.*;
import com.vlee.ejb.inventory.*;
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.lowagie.text.*;
import com.lowagie.text.rtf.*;
import com.lowagie.text.pdf.*;
import com.lowagie.tools.*;

public class DoPrintInventoryLabel extends HttpServlet {

    private String strClassName = "DoBOMCreate";

    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String formName = req.getParameter("formName");
        if (formName.equals("print-label-itemcode-ean-serial-by-grn")) {
            Log.printVerbose("printing label by grn...");
            try {
                fnPrintLabelItemcodeEanSerialByGRN(req, res);
            } catch (Exception ex) {
                ex.printStackTrace();
                req.setAttribute("errMsg", ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (formName.equals("print-label-itemcode-ean-serial-unique")) {
            try {
                fnPrintLabelItemcodeEanSerialUnique(req, res);
            } catch (Exception ex) {
                ex.printStackTrace();
                req.setAttribute("errMsg", ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (formName.equals("print-label-code128-itemname-price-by-grn")) {
            Log.printVerbose("printing label by grn...");
            try {
                fnPrintLabelCode128ItemnamePriceByGRN(req, res);
            } catch (Exception ex) {
                ex.printStackTrace();
                req.setAttribute("errMsg", ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (formName.equals("print-label-code128-itemname-price-unique")) {
            try {
                fnPrintLabelCode128ItemnamePriceUnique(req, res);
            } catch (Exception ex) {
                ex.printStackTrace();
                req.setAttribute("errMsg", ex.getMessage());
                ex.printStackTrace();
            }
        }
        if (formName.equals("print-label-code39-itemname-price-unique")) {
            try {
                fnPrintLabelCode39ItemnamePriceUnique(req, res);
            } catch (Exception ex) {
                ex.printStackTrace();
                req.setAttribute("errMsg", ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void fnPrintLabelCode39ItemnamePriceUnique(HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "filename=label.pdf");
            Integer numberOfLabel = new Integer(req.getParameter("numberOfLabel"));
            String itemCode = req.getParameter("itemCode");
            String itemName = req.getParameter("itemName");
            String itemCurrency = req.getParameter("itemCurrency");
            String itemPrice = req.getParameter("itemPrice");
            BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
            Rectangle pageSize = new Rectangle(400, 200);
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfContentByte contentByte = writer.getDirectContent();
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setFixedHeight(90);
            String labelString = itemName;
            labelString += "\n" + itemCurrency + " " + itemPrice;
            for (int cnt1 = 0; cnt1 < numberOfLabel.intValue(); cnt1++) {
                try {
                    PdfPTable table2 = new PdfPTable(1);
                    table2.setWidthPercentage(100);
                    table2.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    table2.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
                    Barcode39 code39 = new Barcode39();
                    code39.setCode(itemCode);
                    code39.setStartStopText(false);
                    Image image39 = code39.createImageWithBarcode(contentByte, null, null);
                    PdfPCell shipment = new PdfPCell(new Phrase(new Chunk(image39, 0, 0)));
                    shipment.setFixedHeight(code39.getBarcodeSize().height() + 2f);
                    shipment.setPaddingTop(1f);
                    shipment.setPaddingBottom(1f);
                    shipment.setVerticalAlignment(Element.ALIGN_TOP);
                    shipment.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table2.addCell(new Phrase("All IT Hypermarket Sdn Bhd", FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                    table2.addCell(shipment);
                    table2.addCell(new Phrase(itemName, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                    table2.addCell(new Phrase(itemCurrency + " " + itemPrice, FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL)));
                    PdfPCell theCell = new PdfPCell(table2);
                    theCell.setBorder(Rectangle.BOX);
                    table.addCell(theCell);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            document.add(table);
            document.close();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fnPrintLabelCode128ItemnamePriceUnique(HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "filename=label.pdf");
            Integer numberOfLabel = new Integer(req.getParameter("numberOfLabel"));
            String itemCode = req.getParameter("itemCode");
            String itemName = req.getParameter("itemName");
            String itemCurrency = req.getParameter("itemCurrency");
            String itemPrice = req.getParameter("itemPrice");
            BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfContentByte contentByte = writer.getDirectContent();
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setFixedHeight(76);
            String labelString = itemName;
            labelString += "\n" + itemCurrency + " " + itemPrice;
            for (int cnt1 = 0; cnt1 < numberOfLabel.intValue(); cnt1++) {
                try {
                    PdfPTable table2 = new PdfPTable(1);
                    table2.setWidthPercentage(100);
                    table2.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    table2.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
                    Barcode128 shipBarCode = new Barcode128();
                    shipBarCode.setX(0.75f);
                    shipBarCode.setN(1.5f);
                    shipBarCode.setChecksumText(true);
                    shipBarCode.setGenerateChecksum(true);
                    shipBarCode.setSize(10f);
                    shipBarCode.setTextAlignment(Element.ALIGN_LEFT);
                    shipBarCode.setBaseline(10f);
                    shipBarCode.setCode(itemCode);
                    shipBarCode.setBarHeight(50f);
                    Image imgShipBarCode = shipBarCode.createImageWithBarcode(contentByte, null, null);
                    PdfPCell shipment = new PdfPCell(new Phrase(new Chunk(imgShipBarCode, 0, 0)));
                    shipment.setFixedHeight(shipBarCode.getBarcodeSize().height() + 16f);
                    shipment.setPaddingTop(5f);
                    shipment.setPaddingBottom(10f);
                    shipment.setBorder(Rectangle.BOX);
                    shipment.setVerticalAlignment(Element.ALIGN_TOP);
                    shipment.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table2.addCell(new Phrase("All IT Hypermarket Sdn Bhd", FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                    table2.addCell(shipment);
                    table2.addCell(new Phrase(itemName, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                    table2.addCell(new Phrase(itemCurrency + " " + itemPrice, FontFactory.getFont(FontFactory.COURIER, 15, Font.NORMAL)));
                    PdfPCell theCell = new PdfPCell(table2);
                    table.addCell(theCell);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            document.add(table);
            document.close();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fnPrintLabelCode128ItemnamePriceByGRN(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Log.printVerbose("printing label by grn...2");
        try {
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "filename=label.pdf");
            String strGRN = req.getParameter("grnId");
            Log.printVerbose("... GRN NO:" + strGRN);
            GoodsReceivedNoteObject grnObj = GoodsReceivedNoteNut.getObject(new Long(strGRN));
            BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setFixedHeight(76);
            for (int cnt1 = 0; cnt1 < grnObj.vecGRNItems.size(); cnt1++) {
                try {
                    GoodsReceivedNoteItemObject grnItmObj = (GoodsReceivedNoteItemObject) grnObj.vecGRNItems.get(cnt1);
                    ItemObject itemObj = ItemNut.getObject(grnItmObj.mItemId);
                    Log.printVerbose(" ITEM " + cnt1 + 1 + "... inside GRN..");
                    Log.printVerbose(" ITEM CODE/EAN:" + itemObj.code + " , " + itemObj.eanCode);
                    String labelString = "ITEMCODE:" + itemObj.code;
                    labelString += "\nEAN/UPC:" + itemObj.eanCode;
                    PdfPCell theCell = new PdfPCell();
                    if (itemObj.serialized == false) {
                        Integer nQty = new Integer(CurrencyFormat.strInt(grnItmObj.mTotalQty));
                        for (int cnt2 = 0; cnt2 < nQty.intValue(); cnt2++) {
                            table.addCell(new Phrase(labelString, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                        }
                    } else {
                        QueryObject query = new QueryObject(new String[] { SerialNumberDeltaBean.DOC_KEY + " = '" + grnItmObj.mPkid.toString() + "' ", SerialNumberDeltaBean.DOC_TABLE + " = '" + GoodsReceivedNoteItemBean.TABLENAME + "' " });
                        query.setOrder(" ORDER BY " + SerialNumberDeltaBean.SERIAL + " ");
                        Vector vecSN = new Vector(SerialNumberDeltaNut.getObjects(query));
                        for (int cnt5 = 0; cnt5 < vecSN.size(); cnt5++) {
                            SerialNumberDeltaObject sndObj = (SerialNumberDeltaObject) vecSN.get(cnt5);
                            Log.printVerbose(" SERIAL :" + sndObj.serialNumber);
                            String buffer = labelString + "\nSN:" + sndObj.serialNumber;
                            table.addCell(new Phrase(buffer, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            document.add(table);
            document.close();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fnPrintLabelItemcodeEanSerialUnique(HttpServletRequest req, HttpServletResponse res) throws Exception {
        try {
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "filename=label.pdf");
            Integer numberOfLabel = new Integer(req.getParameter("numberOfLabel"));
            String itemCode = req.getParameter("itemCode");
            String itemEAN = req.getParameter("itemEAN");
            String itemSerial = req.getParameter("itemSerial");
            BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setFixedHeight(76);
            String labelString = "ITEMCODE:" + itemCode;
            labelString += "\nEAN/UPC:" + itemEAN;
            if (itemSerial != null && itemSerial.length() > 3) {
                labelString += "\nSN#:" + itemSerial;
            }
            for (int cnt1 = 0; cnt1 < numberOfLabel.intValue(); cnt1++) {
                try {
                    PdfPCell theCell = new PdfPCell();
                    table.addCell(new Phrase(labelString, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            document.add(table);
            document.close();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fnPrintLabelItemcodeEanSerialByGRN(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Log.printVerbose("printing label by grn...2");
        try {
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "filename=label.pdf");
            String strGRN = req.getParameter("grnId");
            Log.printVerbose("... GRN NO:" + strGRN);
            GoodsReceivedNoteObject grnObj = GoodsReceivedNoteNut.getObject(new Long(strGRN));
            BufferedOutputStream outputStream = new BufferedOutputStream(res.getOutputStream());
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setFixedHeight(76);
            for (int cnt1 = 0; cnt1 < grnObj.vecGRNItems.size(); cnt1++) {
                try {
                    GoodsReceivedNoteItemObject grnItmObj = (GoodsReceivedNoteItemObject) grnObj.vecGRNItems.get(cnt1);
                    ItemObject itemObj = ItemNut.getObject(grnItmObj.mItemId);
                    Log.printVerbose(" ITEM " + cnt1 + 1 + "... inside GRN..");
                    Log.printVerbose(" ITEM CODE/EAN:" + itemObj.code + " , " + itemObj.eanCode);
                    String labelString = "ITEMCODE:" + itemObj.code;
                    labelString += "\nEAN/UPC:" + itemObj.eanCode;
                    PdfPCell theCell = new PdfPCell();
                    if (itemObj.serialized == false) {
                        Integer nQty = new Integer(CurrencyFormat.strInt(grnItmObj.mTotalQty));
                        for (int cnt2 = 0; cnt2 < nQty.intValue(); cnt2++) {
                            table.addCell(new Phrase(labelString, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                        }
                    } else {
                        QueryObject query = new QueryObject(new String[] { SerialNumberDeltaBean.DOC_KEY + " = '" + grnItmObj.mPkid.toString() + "' ", SerialNumberDeltaBean.DOC_TABLE + " = '" + GoodsReceivedNoteItemBean.TABLENAME + "' " });
                        query.setOrder(" ORDER BY " + SerialNumberDeltaBean.SERIAL + " ");
                        Vector vecSN = new Vector(SerialNumberDeltaNut.getObjects(query));
                        for (int cnt5 = 0; cnt5 < vecSN.size(); cnt5++) {
                            SerialNumberDeltaObject sndObj = (SerialNumberDeltaObject) vecSN.get(cnt5);
                            Log.printVerbose(" SERIAL :" + sndObj.serialNumber);
                            String buffer = labelString + "\nSN:" + sndObj.serialNumber;
                            table.addCell(new Phrase(buffer, FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL)));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            document.add(table);
            document.close();
            outputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
