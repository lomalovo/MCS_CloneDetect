package com.itextpdf.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.itextpdf.text.error_messages.MessageLocalization;
import com.itextpdf.text.Image;

/**
 * <CODE>PdfImage</CODE> is a <CODE>PdfStream</CODE> containing an image-<CODE>Dictionary</CODE> and -stream.
 */
public class PdfImage extends PdfStream {

    static final int TRANSFERSIZE = 4096;

    /** This is the <CODE>PdfName</CODE> of the image. */
    protected PdfName name = null;

    /**
     * Constructs a <CODE>PdfImage</CODE>-object.
     *
     * @param image the <CODE>Image</CODE>-object
     * @param name the <CODE>PdfName</CODE> for this image
     * @throws BadPdfFormatException on error
     */
    public PdfImage(Image image, String name, PdfIndirectReference maskRef) throws BadPdfFormatException {
        super();
        if (name == null) generateImgResName(image); else this.name = new PdfName(name);
        put(PdfName.TYPE, PdfName.XOBJECT);
        put(PdfName.SUBTYPE, PdfName.IMAGE);
        put(PdfName.WIDTH, new PdfNumber(image.getWidth()));
        put(PdfName.HEIGHT, new PdfNumber(image.getHeight()));
        if (image.getLayer() != null) put(PdfName.OC, image.getLayer().getRef());
        if (image.isMask() && (image.getBpc() == 1 || image.getBpc() > 0xff)) put(PdfName.IMAGEMASK, PdfBoolean.PDFTRUE);
        if (maskRef != null) {
            if (image.isSmask()) put(PdfName.SMASK, maskRef); else put(PdfName.MASK, maskRef);
        }
        if (image.isMask() && image.isInverted()) put(PdfName.DECODE, new PdfLiteral("[1 0]"));
        if (image.isInterpolation()) put(PdfName.INTERPOLATE, PdfBoolean.PDFTRUE);
        InputStream is = null;
        try {
            if (image.isImgRaw()) {
                int colorspace = image.getColorspace();
                int transparency[] = image.getTransparency();
                if (transparency != null && !image.isMask() && maskRef == null) {
                    StringBuilder s = new StringBuilder("[");
                    for (int k = 0; k < transparency.length; ++k) s.append(transparency[k]).append(" ");
                    s.append("]");
                    put(PdfName.MASK, new PdfLiteral(s.toString()));
                }
                bytes = image.getRawData();
                put(PdfName.LENGTH, new PdfNumber(bytes.length));
                int bpc = image.getBpc();
                if (bpc > 0xff) {
                    if (!image.isMask()) put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
                    put(PdfName.BITSPERCOMPONENT, new PdfNumber(1));
                    put(PdfName.FILTER, PdfName.CCITTFAXDECODE);
                    int k = bpc - Image.CCITTG3_1D;
                    PdfDictionary decodeparms = new PdfDictionary();
                    if (k != 0) decodeparms.put(PdfName.K, new PdfNumber(k));
                    if ((colorspace & Image.CCITT_BLACKIS1) != 0) decodeparms.put(PdfName.BLACKIS1, PdfBoolean.PDFTRUE);
                    if ((colorspace & Image.CCITT_ENCODEDBYTEALIGN) != 0) decodeparms.put(PdfName.ENCODEDBYTEALIGN, PdfBoolean.PDFTRUE);
                    if ((colorspace & Image.CCITT_ENDOFLINE) != 0) decodeparms.put(PdfName.ENDOFLINE, PdfBoolean.PDFTRUE);
                    if ((colorspace & Image.CCITT_ENDOFBLOCK) != 0) decodeparms.put(PdfName.ENDOFBLOCK, PdfBoolean.PDFFALSE);
                    decodeparms.put(PdfName.COLUMNS, new PdfNumber(image.getWidth()));
                    decodeparms.put(PdfName.ROWS, new PdfNumber(image.getHeight()));
                    put(PdfName.DECODEPARMS, decodeparms);
                } else {
                    switch(colorspace) {
                        case 1:
                            put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
                            if (image.isInverted()) put(PdfName.DECODE, new PdfLiteral("[1 0]"));
                            break;
                        case 3:
                            put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                            if (image.isInverted()) put(PdfName.DECODE, new PdfLiteral("[1 0 1 0 1 0]"));
                            break;
                        case 4:
                        default:
                            put(PdfName.COLORSPACE, PdfName.DEVICECMYK);
                            if (image.isInverted()) put(PdfName.DECODE, new PdfLiteral("[1 0 1 0 1 0 1 0]"));
                    }
                    PdfDictionary additional = image.getAdditional();
                    if (additional != null) putAll(additional);
                    if (image.isMask() && (image.getBpc() == 1 || image.getBpc() > 8)) remove(PdfName.COLORSPACE);
                    put(PdfName.BITSPERCOMPONENT, new PdfNumber(image.getBpc()));
                    if (image.isDeflated()) put(PdfName.FILTER, PdfName.FLATEDECODE); else {
                        flateCompress(image.getCompressionLevel());
                    }
                }
                return;
            }
            String errorID;
            if (image.getRawData() == null) {
                is = image.getUrl().openStream();
                errorID = image.getUrl().toString();
            } else {
                is = new java.io.ByteArrayInputStream(image.getRawData());
                errorID = "Byte array";
            }
            switch(image.type()) {
                case Image.JPEG:
                    put(PdfName.FILTER, PdfName.DCTDECODE);
                    switch(image.getColorspace()) {
                        case 1:
                            put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
                            break;
                        case 3:
                            put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                            break;
                        default:
                            put(PdfName.COLORSPACE, PdfName.DEVICECMYK);
                            if (image.isInverted()) {
                                put(PdfName.DECODE, new PdfLiteral("[1 0 1 0 1 0 1 0]"));
                            }
                    }
                    put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    if (image.getRawData() != null) {
                        bytes = image.getRawData();
                        put(PdfName.LENGTH, new PdfNumber(bytes.length));
                        return;
                    }
                    streamBytes = new ByteArrayOutputStream();
                    transferBytes(is, streamBytes, -1);
                    break;
                case Image.JPEG2000:
                    put(PdfName.FILTER, PdfName.JPXDECODE);
                    if (image.getColorspace() > 0) {
                        switch(image.getColorspace()) {
                            case 1:
                                put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
                                break;
                            case 3:
                                put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                                break;
                            default:
                                put(PdfName.COLORSPACE, PdfName.DEVICECMYK);
                        }
                        put(PdfName.BITSPERCOMPONENT, new PdfNumber(image.getBpc()));
                    }
                    if (image.getRawData() != null) {
                        bytes = image.getRawData();
                        put(PdfName.LENGTH, new PdfNumber(bytes.length));
                        return;
                    }
                    streamBytes = new ByteArrayOutputStream();
                    transferBytes(is, streamBytes, -1);
                    break;
                case Image.JBIG2:
                    put(PdfName.FILTER, PdfName.JBIG2DECODE);
                    put(PdfName.COLORSPACE, PdfName.DEVICEGRAY);
                    put(PdfName.BITSPERCOMPONENT, new PdfNumber(1));
                    if (image.getRawData() != null) {
                        bytes = image.getRawData();
                        put(PdfName.LENGTH, new PdfNumber(bytes.length));
                        return;
                    }
                    streamBytes = new ByteArrayOutputStream();
                    transferBytes(is, streamBytes, -1);
                    break;
                default:
                    throw new BadPdfFormatException(MessageLocalization.getComposedMessage("1.is.an.unknown.image.format", errorID));
            }
            put(PdfName.LENGTH, new PdfNumber(streamBytes.size()));
        } catch (IOException ioe) {
            throw new BadPdfFormatException(ioe.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ee) {
                }
            }
        }
    }

    /**
     * Returns the <CODE>PdfName</CODE> of the image.
     *
     * @return		the name
     */
    public PdfName name() {
        return name;
    }

    static void transferBytes(InputStream in, OutputStream out, int len) throws IOException {
        byte buffer[] = new byte[TRANSFERSIZE];
        if (len < 0) len = 0x7fff0000;
        int size;
        while (len != 0) {
            size = in.read(buffer, 0, Math.min(len, TRANSFERSIZE));
            if (size < 0) return;
            out.write(buffer, 0, size);
            len -= size;
        }
    }

    protected void importAll(PdfImage dup) {
        name = dup.name;
        compressed = dup.compressed;
        compressionLevel = dup.compressionLevel;
        streamBytes = dup.streamBytes;
        bytes = dup.bytes;
        hashMap = dup.hashMap;
    }

    /**
     * Called when no resource name is provided in our constructor.  This generates a 
     * name that is required to be unique within a given resource dictionary.
     * @since 5.0.1
     */
    private void generateImgResName(Image img) {
        name = new PdfName("img" + Long.toHexString(img.getMySerialId()));
    }
}
