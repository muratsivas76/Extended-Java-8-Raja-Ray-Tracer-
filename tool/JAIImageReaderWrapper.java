/* $Id: JAIImageReaderWrapper.java,v 1.1 2001/02/16 01:52:10 gregoire Exp $
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
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import com.sun.media.jai.codec.*;


/**
 * A wrapper class to use JAI decoders as instances of
 * <code>ImageReader</code>.
 *
 * @see ImageReader
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
class JAIImageReaderWrapper implements ImageReader
{
    private ImageDecoder dec;
    private String codec;

    JAIImageReaderWrapper(String codec, InputStream is) throws IOException
    {
        this.codec = codec;
        this.dec = ImageCodec.createImageDecoder(codec, is, null);
    }

    public RenderedImage read() throws IOException
    {
        RenderedImage image = dec.decodeAsRenderedImage();

        if (! codec.equals("TIFF"))
        {
            return image;
        }


        // Work around the TIFF Decoder bug
        // Indeed, the image decoded by the TIFF Decoder has multiple tiles
        // and its getData() method returns a raster where only the first
        // tile has been copied

        // Create a new writable raster of the same size
        WritableRaster newRas = image.getData().createCompatibleWritableRaster();

        // Copy each tile of the decoded image in this new raster
        for(int i = image.getMinTileX() ; i < image.getNumXTiles() ; i++)
        {
            for(int j = image.getMinTileY() ; j < image.getNumYTiles() ; j++)
            {
                newRas.setRect(image.getTile(i, j));
            }
        }

        return new BufferedImage(image.getColorModel(), newRas, false, null);
    }

    public Object getImageReadParam()
    {
        return dec.getParam();
    }

    public void setImageReadParam(Object param)
    {
        dec.setParam((ImageDecodeParam) param);
    }
}
