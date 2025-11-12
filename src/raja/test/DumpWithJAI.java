/* $Id: DumpWithJAI.java,v 1.2 2001/02/18 08:29:24 gregoire Exp $
 * Copyright (C) 2001 E. Fleury & G. Sutre
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.test;

import java.awt.image.*;
import java.io.*;
import com.sun.media.jai.codec.*;


/*
 * Program to dump images, using JAI directly.
 *
 * Usage: java raja.test.DumpWithJAI file
 *
 * WARNING: This class is not part of the core Raja API.  It is primarily
 * used by Raja developpers to validate their implementations.  It does not
 * contain much error handling.
 */

class DumpWithJAI
{
    public static void main(String[] argv) throws IOException
    {
        // Parse command line arguments
        int index = 0;
        String inputFileName = argv[index++];


        // Deduce input codec from file name extension
        String inputCodec = raja.io.ImageIO.suffix2Codec(inputFileName);

        if (inputCodec == null)
        {
            System.err.println("Could not deduce codec for file: " + inputFileName);
            System.err.println("DumpWithJAI aborted.");
            System.exit(1);
        }


        // Read input image
        System.out.println("Reading " + inputCodec + " file: " + inputFileName + "...");
        InputStream is = new BufferedInputStream(new FileInputStream(inputFileName));
        ImageDecoder dec = ImageCodec.createImageDecoder(inputCodec, is, null);

        if (dec == null)
        {
            System.err.println("Could not find an image decoder for this codec: " +
                               inputCodec);
            System.err.println("DumpWithJAI aborted.");
            System.exit(1);
        }

        RenderedImage image = dec.decodeAsRenderedImage();
        System.out.println("done.\n");


        // Dump Image
        dump(image);


        // Close the input stream
        // Remark: trying to access the input image after closing the input
        // stream may fail, since the input image is decoded as needed
        is.close();
    }

    static void dump(RenderedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        Raster ras = image.getData();
        ColorModel cm = image.getColorModel();

        System.out.println("image = " + image);
        System.out.println("Image has resolution: " + width + "x" + height);
        System.out.println("getData() = " + ras);
        System.out.println("getColorModel() = " + cm);
        System.out.println("getNumXTiles() = " + image.getNumXTiles());
        System.out.println("getNumYTiles() = " + image.getNumYTiles());
        System.out.println("getMinTileX() = " + image.getMinTileX());
        System.out.println("getMinTileY() = " + image.getMinTileY());
        System.out.println("getTileWidth() = " + image.getTileWidth());
        System.out.println("getTileHeight() = " + image.getTileHeight());
        System.out.println();

        for (int i = 0 ; i < width ; i++)
        {
            for (int j = 0 ; j < height ; j++)
            {
                int rgb = cm.getRGB(ras.getDataElements(i, j, null));

                int red   = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8)  & 0xFF;
                int blue  = (rgb >> 0)  & 0xFF;

                System.out.println("Pixel(" + i + ", " + j + ") : [" +
                                   red + ", " +
                                   green + ", " +
                                   blue + "]");
            }
        }
    }
}
