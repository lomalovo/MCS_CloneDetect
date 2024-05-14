package part1.chapter04;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import com.lowagie.database.DatabaseConnection;
import com.lowagie.database.HsqldbConnection;
import com.lowagie.filmfestival.FilmFonts;
import com.lowagie.filmfestival.PojoFactory;
import com.lowagie.filmfestival.Movie;
import com.lowagie.filmfestival.PojoToElementFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class MovieCompositeMode {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part1/chapter04/movie_composite_mode.pdf";

    /** Path to the resources. */
    public static final String RESOURCE = "resources/posters/%s.jpg";

    /**
     * Main method.
     *
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException 
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, DocumentException, SQLException {
        new MovieCompositeMode().createPdf(RESULT);
    }

    /**
     * Creates a PDF with information about the movies
     * @param    filename the name of the PDF file that will be created.
     * @throws    DocumentException 
     * @throws    IOException 
     * @throws    SQLException
     */
    public void createPdf(String filename) throws IOException, DocumentException, SQLException {
        DatabaseConnection connection = new HsqldbConnection("filmfestival");
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        document.add(new Paragraph("Movies:"));
        java.util.List<Movie> movies = PojoFactory.getMovies(connection);
        List list;
        PdfPCell cell;
        for (Movie movie : movies) {
            PdfPTable table = new PdfPTable(new float[] { 1, 7 });
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            cell = new PdfPCell(Image.getInstance(String.format(RESOURCE, movie.getImdb())), true);
            cell.setBorder(PdfPCell.NO_BORDER);
            table.addCell(cell);
            cell = new PdfPCell();
            Paragraph p = new Paragraph(movie.getTitle(), FilmFonts.BOLD);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingBefore(5);
            p.setSpacingAfter(5);
            cell.addElement(p);
            cell.setBorder(PdfPCell.NO_BORDER);
            if (movie.getOriginalTitle() != null) {
                p = new Paragraph(movie.getOriginalTitle(), FilmFonts.ITALIC);
                p.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(p);
            }
            list = PojoToElementFactory.getDirectorList(movie);
            list.setIndentationLeft(30);
            cell.addElement(list);
            p = new Paragraph(String.format("Year: %d", movie.getYear()), FilmFonts.NORMAL);
            p.setIndentationLeft(15);
            p.setLeading(24);
            cell.addElement(p);
            p = new Paragraph(String.format("Run length: %d", movie.getDuration()), FilmFonts.NORMAL);
            p.setLeading(14);
            p.setIndentationLeft(30);
            cell.addElement(p);
            list = PojoToElementFactory.getCountryList(movie);
            list.setIndentationLeft(40);
            cell.addElement(list);
            table.addCell(cell);
            document.add(table);
        }
        document.close();
        connection.close();
    }
}
