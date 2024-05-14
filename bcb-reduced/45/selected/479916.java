package edu.ucsd.ncmir.jibber.core;

import edu.ucsd.ncmir.jibber.events.ErrorEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author spl
 */
public class Atlas extends HashSet<Section> implements Comparable<Atlas> {

    public static final String PREFIX = "atlases";

    private String _atlas_name;

    private String _space_name;

    /** Creates a new instance of Atlas */
    public Atlas(JarFile jar_file, JarEntry jar_entry) {
        String jarpath = jar_entry.toString();
        String[] elements = jarpath.split("/");
        this._atlas_name = elements[1];
        this.reader(jar_file, jarpath);
    }

    @Override
    public String toString() {
        return this._atlas_name;
    }

    public int compareTo(Atlas a) {
        return this.toString().compareTo(a.toString());
    }

    private void reader(JarFile jar_file, String jar_path) {
        int line = 0;
        for (Enumeration<JarEntry> entries = jar_file.entries(); entries.hasMoreElements(); ) {
            JarEntry je = entries.nextElement();
            String entry = je.toString();
            if (entry.startsWith(jar_path) && !entry.equals(jar_path)) {
                String[] s = entry.split("/");
                String sname = s[s.length - 1];
                if (je.isDirectory()) {
                    Section section = new Section(this, sname);
                    this.add(section);
                    ClassLoader class_loader = this.getClass().getClassLoader();
                    URL index_url = class_loader.getResource(entry + "index");
                    if (index_url != null) try {
                        InputStreamReader isr = new InputStreamReader(index_url.openStream());
                        BufferedReader br = new BufferedReader(isr);
                        String text;
                        while ((text = br.readLine()) != null) {
                            String[] tokens = text.split("[ \t]+");
                            line++;
                            if (tokens.length == 10) {
                                URL page_url = class_loader.getResource(entry + tokens[0]);
                                double atlas_x0 = Double.parseDouble(tokens[1]);
                                double atlas_y0 = Double.parseDouble(tokens[2]);
                                double atlas_x1 = Double.parseDouble(tokens[3]);
                                double atlas_y1 = Double.parseDouble(tokens[4]);
                                double pdf_x0 = Double.parseDouble(tokens[5]);
                                double pdf_y0 = Double.parseDouble(tokens[6]);
                                double pdf_x1 = Double.parseDouble(tokens[7]);
                                double pdf_y1 = Double.parseDouble(tokens[8]);
                                double offset = Double.parseDouble(tokens[9]);
                                Page page = new Page(section, page_url, line, atlas_x0, atlas_y0, atlas_x1, atlas_y1, pdf_x0, pdf_y0, pdf_x1, pdf_y1, offset);
                                section.put(tokens[9], page);
                            }
                        }
                        br.close();
                    } catch (Exception e) {
                        new ErrorEvent().send(e);
                    }
                } else if (sname.equals("space_name")) {
                    ClassLoader class_loader = this.getClass().getClassLoader();
                    URL space_name_url = class_loader.getResource(entry);
                    if (space_name_url != null) {
                        try {
                            InputStreamReader isr = new InputStreamReader(space_name_url.openStream());
                            BufferedReader br = new BufferedReader(isr);
                            this._space_name = br.readLine();
                            br.close();
                        } catch (Exception e) {
                            new ErrorEvent().send(e);
                        }
                    } else new ErrorEvent().send("Unable to open space_name");
                }
            }
        }
    }

    public Section[] getSections() {
        Section[] sections = this.toArray(new Section[this.size()]);
        Arrays.sort(sections);
        return sections;
    }

    public static Atlas[] atlasLoader() {
        Atlas[] atlas_list = null;
        try {
            ArrayList<Atlas> atlases = new ArrayList<Atlas>();
            ClassLoader cl = Atlas.class.getClassLoader();
            for (Enumeration<URL> e = cl.getResources(Atlas.PREFIX); e.hasMoreElements(); ) {
                URL url = e.nextElement();
                JarURLConnection jc = (JarURLConnection) url.openConnection();
                JarFile jf = jc.getJarFile();
                for (Enumeration<JarEntry> ej = jf.entries(); ej.hasMoreElements(); ) {
                    JarEntry je = ej.nextElement();
                    if (je.isDirectory() && je.toString().matches("^atlases/[A-Za-z0-9_]+/$")) atlases.add(new Atlas(jf, je));
                }
            }
            if (atlases.size() > 0) {
                atlas_list = atlases.toArray(new Atlas[atlases.size()]);
                if (atlas_list.length > 1) Arrays.sort(atlas_list);
            }
        } catch (IOException ioe) {
            new ErrorEvent().sendWait(ioe);
        }
        return atlas_list;
    }

    /**
     * @return the _space_name
     */
    public String getSpaceName() {
        return this._space_name;
    }
}
