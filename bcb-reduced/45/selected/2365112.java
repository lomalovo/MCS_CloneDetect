package com.lowagie.examples.objects.chunk;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * How to change the color of a font.
 * 
 * @author blowagie
 */
public class ChunkColor {

    /**
	 * Changing Font colors
	 * 
	 * @param args no arguments needed here
	 */
    public static void main(String[] args) {
        System.out.println("FontColor");
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ChunkColor.pdf"));
            document.open();
            Font red = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.BOLD, new Color(0xFF, 0x00, 0x00));
            Font blue = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.ITALIC, new Color(0x00, 0x00, 0xFF));
            Paragraph p;
            p = new Paragraph("Roses are ");
            p.add(new Chunk("red", red));
            document.add(p);
            p = new Paragraph("Violets are ");
            p.add(new Chunk("blue", blue));
            document.add(p);
            BaseFont bf = FontFactory.getFont(FontFactory.COURIER).getCalculatedBaseFont(false);
            PdfContentByte cb = writer.getDirectContent();
            cb.beginText();
            cb.setColorFill(new Color(0x00, 0xFF, 0x00));
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "Grass is green", 250, 700, 0);
            cb.endText();
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
