/* $Id: JAIImageWriterWrapper.java,v 1.1 2001/02/16 01:52:10 gregoire Exp $
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
import com.sun.media.jai.codec.*;


/**
 * A wrapper class to use JAI encoders as instances of
 * <code>ImageWriter</code>.
 *
 * @see ImageWriter
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
class JAIImageWriterWrapper implements ImageWriter
{
    private static final float DEFAULT_JPEG_QUALITY = (float) 0.95;
    private ImageEncoder enc;
    private String codec;

    JAIImageWriterWrapper(String codec, OutputStream os) throws IOException
    {
        this.codec = codec;
        this.enc = ImageCodec.createImageEncoder(codec, os, null);
    }

    public void write(RenderedImage image) throws IOException
    {
        enc.encode(image);
    }

    public Object createDefaultImageWriteParam(RenderedImage image)
    {
        if (codec.equals("PNG"))
        {
            return PNGEncodeParam.getDefaultEncodeParam(image);
        }
        else if (codec.equals("TIFF"))
        {
            return new TIFFEncodeParam();
        }
        else if (codec.equals("JPEG"))
        {
            JPEGEncodeParam encParam = new JPEGEncodeParam();
            encParam.setQuality(DEFAULT_JPEG_QUALITY);
            return encParam;
        }
        else if (codec.equals("PNM"))
        {
            return new PNMEncodeParam();
        }
        else if (codec.equals("BMP"))
        {
            return new BMPEncodeParam();
        }
        else
        {
            // This should not happen
            throw new IllegalArgumentException("Unsupported writing codec: " + codec);
        }
    }

    public Object getImageWriteParam()
    {
        return enc.getParam();
    }

    public void setImageWriteParam(Object param)
    {
        if (codec.equals("PNG") || codec.equals("JPEG") ||codec.equals("BMP"))
        {
            // Work around the setParam() bug in the PNG, JPEG and BMP Encoders
            // Indeed, setting a new ImageEncodeParam with setParam() does not
            // change the encoded file
            // So we create a new encoder instead

            enc = ImageCodec.createImageEncoder(codec,
                                                enc.getOutputStream(),
                                                (ImageEncodeParam) param);
        }
        else
        {
            enc.setParam((ImageEncodeParam) param);
        }
    }
}
