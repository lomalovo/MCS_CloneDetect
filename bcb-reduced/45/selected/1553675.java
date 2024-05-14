package part1.chapter02;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.lowagie.database.DatabaseConnection;
import com.lowagie.database.HsqldbConnection;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;

/**
 * Writes a list of countries to a PDF file.
 */
public class CountryChunks {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part1/chapter02/country_chunks.pdf";

    /**
     * Main method.
     *
     * @param    args    no arguments needed
     * @throws DocumentException
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, DocumentException, SQLException {
        new CountryChunks().createPdf(RESULT);
    }

    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws    DocumentException
     * @throws    IOException
     * @throws    SQLException
     */
    public void createPdf(String filename) throws IOException, DocumentException, SQLException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename)).setInitialLeading(16);
        document.open();
        DatabaseConnection connection = new HsqldbConnection("filmfestival");
        Statement stm = connection.createStatement();
        ResultSet rs = stm.executeQuery("SELECT country, id FROM film_country ORDER BY country");
        Font font = new Font(FontFamily.HELVETICA, 6, Font.BOLD, BaseColor.WHITE);
        while (rs.next()) {
            document.add(new Chunk(rs.getString("country")));
            document.add(new Chunk(" "));
            Chunk id = new Chunk(rs.getString("id"), font);
            id.setBackground(BaseColor.BLACK, 1f, 0.5f, 1f, 1.5f);
            id.setTextRise(6);
            document.add(id);
            document.add(Chunk.NEWLINE);
        }
        stm.close();
        connection.close();
        document.close();
    }
}
