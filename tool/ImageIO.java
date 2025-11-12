/* $Id: ImageIO.java,v 1.2 2001/02/16 19:08:58 gregoire Exp $
 * Copyright (C) 2001 E. Fleury & G. Sutre
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.io;

import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Map;
import java.util.HashMap;


/**
 * A class allowing the creation of image readers and writers.
 *
 * @see ImageReader
 * @see ImageWriter
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public final class ImageIO
{
    /**
     * This <code>Map</code> associates codec names to usual file suffixes.
     */
    private static Map suffix2Codec = new HashMap();

    static
    {
        // Initialize the suffix2Codec map
        suffix2Codec.put("png",  "PNG");
        suffix2Codec.put("jpg",  "JPEG");
        suffix2Codec.put("jpeg", "JPEG");
        suffix2Codec.put("tif",  "TIFF");
        suffix2Codec.put("tiff", "TIFF");
        suffix2Codec.put("pnm",  "PNM");
        suffix2Codec.put("bmp",  "BMP");
        suffix2Codec.put("gif",  "GIF");
        suffix2Codec.put("tga",  "TGA");
        suffix2Codec.put("pcx",  "PCX");
        suffix2Codec.put("xpm",  "XPM");
    }

    /**
     * This flag indicates whether the JAI Codec package is available.  It
     * is <code>true</code> when the JAI Codec package has already been
     * detected.  A <code>false</code> value actually means
     * <i>don't know</i>.
     */
    private static boolean isJAICodecAvailable = false;

    /**
     * This flag indicates whether the JAI Util package is available.  It
     * is <code>true</code> when the JAI Util package has already been
     * detected.  A <code>false</code> value actually means
     * <i>don't know</i>.
     */
    private static boolean isJAIUtilAvailable = false;

    /**
     * Don't let anyone instantiate this class.
     */
    private ImageIO()
    {
    }

    /**
     * Returns the codec name denoted by the suffix of the given file name.
     * If a codec name cannot be deduced from this file suffix then this
     * method returns <code>null</code>.
     *
     * @param fileName the file name whose suffix to deduce the codec name
     *                 from.
     *
     * @return the codec name denoted by the suffix of the given file name.
     */
    public static String suffix2Codec(String fileName)
    {
        String suffix = raja.util.FileHelper.getSuffix(fileName);

        if (suffix == null)
        {
            return null;
        }

        return (String) suffix2Codec.get(suffix);
    }

    /**
     * Returns the codec name denoted by the name of the given file.  If a
     * codec name cannot be deduced from this file's name then this method
     * returns <code>null</code>.
     *
     * @param f the file whose name to deduce the codec from.
     *
     * @return the codec name denoted by the given file's name.
     */
    public static String suffix2Codec(File f)
    {
        return suffix2Codec(f.getName());
    }

    /**
     * Checks whether the JAI Codec package is available.  This method
     * performs a really superficial test, since it only checks that a class
     * named com.sun.media.jai.codec.ImageCodec can be loaded, but this test
     * should be sufficient.
     *
     * @return <code>true</code> if the JAI Codec package is (precisely:
     *                           could be) available;
     *         <code>false</code> otherwise.
     */
    private static boolean isJAICodecAvailable()
    {
        if (isJAICodecAvailable)
        {
            return true;
        }

        try
        {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            cl.loadClass("com.sun.media.jai.codec.ImageCodec");
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }

        isJAICodecAvailable = true;
        return true;
    }

    /**
     * Checks whether the JAI Util package is available.  This method
     * performs a really superficial test, since it only checks that a class
     * named com.sun.media.jai.util.PixelData can be loaded, but this test
     * should be sufficient.
     *
     * @return <code>true</code> if the JAI Util package is (precisely:
                                 could be) available;
     *         <code>false</code> otherwise.
     */
    private static boolean isJAIUtilAvailable()
    {
        if (isJAIUtilAvailable)
        {
            return true;
        }

        try
        {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            cl.loadClass("com.sun.media.jai.util.PixelData");
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }

        isJAIUtilAvailable = true;
        return true;
    }

    /**
     * Returns an <code>ImageReader</code> object suitable for decoding
     * images in the given codec from the supplied <code>InputStream</code>,
     * or <code>null</code> if reading in this codec is not supported.
     *
     * @param codec the codec name in which to decode images.
     * @param is the <code>InputStream</code> to read from.
     *
     * @return an instance of <code>ImageReader</code> suitable for decoding
     *         in the given codec, or <code>null</code>.
     */
    public static ImageReader createImageReader(String codec,
                                                InputStream is) throws IOException
    {
        // The JAI's PNG, TIFF, JPEG and BMP image decoders need the JAI
        // Codec Package
        // The JAI's PNM image decoder need the JAI Codec Package and the
        // JAI Util Package
        if (isJAICodecAvailable() & (codec.equals("PNG")  ||
                                     codec.equals("TIFF") ||
                                     codec.equals("JPEG") ||
             (isJAIUtilAvailable() & codec.equals("PNM")) ||
                                     codec.equals("BMP")))
        {
            return new JAIImageReaderWrapper(codec, is);
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns an <code>ImageWriter</code> object suitable for encoding
     * images in the given codec to the supplied <code>OutputStream</code>,
     * or <code>null</code> if writing in this codec is not supported.
     *
     * @param codec the codec name in which to encode images.
     * @param os the <code>OutputStream</code> to write to.
     *
     * @return an instance of <code>ImageWriter</code> suitable for encoding
     *         in the given codec, or <code>null</code>.
     */
    public static ImageWriter createImageWriter(String codec,
                                                OutputStream os) throws IOException
    {
        // The JAI's PNG, TIFF, JPEG and BMP image encoders need the JAI
        // Codec Package
        // The JAI's PNM image encoder need the JAI Codec Package and the
        // JAI Util Package
        if (isJAICodecAvailable() & (codec.equals("PNG")  ||
                                     codec.equals("TIFF") ||
                                     codec.equals("JPEG") ||
             (isJAIUtilAvailable() & codec.equals("PNM")) ||
                                     codec.equals("BMP")))
        {
            return new JAIImageWriterWrapper(codec, os);
        }
        else
        {
            return null;
        }
    }
}
