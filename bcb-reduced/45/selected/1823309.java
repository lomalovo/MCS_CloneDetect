package com.lowagie.examples.directcontent.colors;

import java.awt.Color;
import java.io.FileOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfSpotColor;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SpotColor;

/**
 * Demonstrates the use of spotcolors.
 */
public class SpotColors {

    /**
     * Demonstrates the use of spotcolors.
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Pantone example : Spot Color");
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("spotcolor.pdf"));
            BaseFont bf = BaseFont.createFont("Helvetica", "winansi", BaseFont.NOT_EMBEDDED);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfSpotColor spc_cmyk = new PdfSpotColor("PANTONE 280 CV", 0.25f, new CMYKColor(0.9f, .2f, .3f, .1f));
            PdfSpotColor spc_rgb = new PdfSpotColor("PANTONE 147", 0.9f, new Color(114, 94, 38));
            PdfSpotColor spc_g = new PdfSpotColor("PANTONE 100 CV", 0.5f, new GrayColor(0.9f));
            cb.setColorStroke(spc_cmyk, .5f);
            cb.setLineWidth(10f);
            cb.rectangle(100, 700, 100, 100);
            cb.moveTo(100, 700);
            cb.lineTo(200, 800);
            cb.stroke();
            cb.setColorFill(spc_cmyk, spc_cmyk.getTint());
            cb.rectangle(250, 700, 100, 100);
            cb.fill();
            cb.setColorStroke(spc_rgb, spc_rgb.getTint());
            cb.setLineWidth(5f);
            cb.circle(150f, 500f, 100f);
            cb.stroke();
            cb.setColorFill(spc_rgb, spc_rgb.getTint());
            cb.circle(150f, 500f, 50f);
            cb.fill();
            cb.setColorFill(spc_g, spc_g.getTint());
            cb.moveTo(100f, 200f);
            cb.lineTo(200f, 250f);
            cb.lineTo(400f, 150f);
            cb.fill();
            document.newPage();
            String text = "Some text to show";
            document.add(new Paragraph(text, new Font(Font.HELVETICA, 24, Font.NORMAL, new SpotColor(spc_cmyk))));
            document.add(new Paragraph(text, new Font(Font.HELVETICA, 24, Font.NORMAL, new SpotColor(spc_cmyk, 0.5f))));
            PdfTemplate t = cb.createTemplate(500f, 500f);
            t.setColorStroke(new SpotColor(spc_cmyk, .5f));
            t.setLineWidth(10f);
            t.rectangle(100, 10, 100, 100);
            t.moveTo(100, 10);
            t.lineTo(200, 100);
            t.stroke();
            t.setColorFill(spc_g, spc_g.getTint());
            t.rectangle(100, 125, 100, 100);
            t.fill();
            t.beginText();
            t.setFontAndSize(bf, 20f);
            t.setTextMatrix(1f, 0f, 0f, 1f, 10f, 10f);
            t.showText("Template text upside down");
            t.endText();
            t.rectangle(0, 0, 499, 499);
            t.stroke();
            t.sanityCheck();
            cb.addTemplate(t, -1.0f, 0.00f, 0.00f, -1.0f, 550f, 550f);
            cb.sanityCheck();
        } catch (Exception de) {
            de.printStackTrace();
        }
        document.close();
    }
}
