package savi.spool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import savi.util.Fila;
import savi.util.Util;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author dausech
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConvertePDF {

    public static void converte(String arqp, String dirpdf) {
        Properties p = Util.lerPropriedades(arqp);
        String drive = arqp.substring(0, 2);
        if (drive.indexOf(":") == -1) drive = "";
        String arqRel = new String(drive + p.getProperty("arquivo"));
        File ar = new File(arqRel);
        if (ar.length() < 50) {
            File ap = new File(arqp);
            ap.delete();
            return;
        }
        String arqSaida = dirpdf.trim() + "/" + p.getProperty("usuario").trim() + "/" + p.getProperty("nome.pdf").trim() + ".pdf";
        String email_para = p.getProperty("email.para");
        String usuario = p.getProperty("usuario");
        Fila pagina = new Fila();
        String linha = new String("");
        String linaux = new String("");
        String lin_ant = new String("");
        boolean mesma = false;
        boolean primeira = true;
        boolean pronta = false;
        boolean sobrou = false;
        boolean terminou = false;
        boolean eof = false;
        int limite = 66;
        int me = 10;
        int md = 8;
        int ms = 24;
        int mi = 24;
        int espacamento = 12;
        int tamfonte = 10;
        int qtd_linhas = 0;
        int colunas = 0;
        final char esc = '';
        String lpp8 = new String("");
        lpp8 = esc + "0";
        arqSaida = Util.verificaExistencia(arqSaida);
        Document arqpdf = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(arqpdf, new FileOutputStream(arqSaida));
            BufferedReader arqtxt = new BufferedReader(new FileReader(arqRel));
            while (!terminou) {
                if (pronta) {
                    if (eof) terminou = true;
                    System.out.println("Linhas:" + qtd_linhas + "   Colunas:" + colunas + " Esp: " + espacamento);
                    qtd_linhas = 0;
                    pronta = false;
                    if (primeira) {
                        arqpdf.setMargins(me, md, ms, mi);
                        arqpdf.open();
                        primeira = false;
                    } else {
                        if (pagina.size() > 0) arqpdf.newPage();
                    }
                    if (colunas > 84) {
                        tamfonte = 7;
                    }
                    Font fonte = new Font(Font.COURIER, tamfonte, Font.BOLD);
                    sobrou = false;
                    while (pagina.size() > 0) {
                        linaux = pagina.get().toString();
                        if (linaux.startsWith("\f")) {
                            linaux = linaux.substring(1);
                            sobrou = true;
                            if (pagina.size() > 0) {
                                System.out.println("Nao pode ser verdadeiro aqui");
                                System.in.read();
                            }
                        } else {
                            if (colunas <= 84) linaux = "      " + linaux;
                            Paragraph pa = new Paragraph(new Phrase(linaux, fonte));
                            pa.setLeading(espacamento);
                            arqpdf.add(pa);
                            linaux = null;
                        }
                    }
                }
                if (sobrou) {
                    pagina.add(linaux);
                    qtd_linhas++;
                    lin_ant = linaux;
                    sobrou = false;
                }
                eof = ((linha = arqtxt.readLine()) != null);
                eof = !eof;
                if (!eof) {
                    if (primeira && (linha.indexOf(lpp8) != -1)) {
                        limite = 87;
                        espacamento = 9;
                        ms = 12;
                        mi = 12;
                    }
                    linaux = RemCC(linha.toString());
                    mesma = linaux.equals(lin_ant);
                    if (!mesma || (linaux.trim().length() == 0 && (qtd_linhas > 0))) {
                        pagina.add(linaux);
                        qtd_linhas++;
                        lin_ant = linaux;
                        if ((linaux.indexOf("\f") != -1) || (qtd_linhas > limite)) pronta = true;
                        if (linaux.length() > colunas && !linaux.startsWith("\f")) {
                            colunas = linaux.length();
                        }
                    }
                } else {
                    pronta = true;
                }
            }
            arqtxt.close();
            arqpdf.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        }
        File f = new File(arqp);
        if (!f.delete()) f.renameTo(new File(arqp.substring(0, arqp.length() - 1) + "_"));
        if (email_para.indexOf("@") != -1) {
            EnviaEmail e = new EnviaEmail(email_para, arqSaida, usuario);
            e.start();
        }
    }

    /**
	 *  Description of the Method
	 *
	 *@param  lin  Description of the Parameter
	 *@return      Description of the Return Value
	 */
    private static String RemCC(String lin) {
        final char esc = '';
        final char pri_car = ' ';
        final char ult_car = '©';
        char car;
        StringBuffer lin_ret = new StringBuffer(140);
        int i = 0;
        boolean flag;
        lin_ret.delete(0, 139);
        i = 0;
        flag = false;
        while (i < lin.length()) {
            car = lin.charAt(i);
            if (car == esc) {
                i += 2;
                flag = true;
            }
            if ((car >= pri_car && car <= ult_car) || (car == '\f')) {
                lin_ret.append(car);
                i++;
                flag = true;
            }
            if (!flag) i++;
            flag = false;
        }
        return lin_ret.toString();
    }
}
