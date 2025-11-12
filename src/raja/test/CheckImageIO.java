/* $Id: CheckImageIO.java,v 1.2 2001/02/18 08:29:24 gregoire Exp $
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
import raja.io.*;


/*
 * Program to check Raja's I/O image functionnality.
 *
 * Usage: java raja.test.CheckImageIO [--indexed] tmpfile
 *
 * The codec to check I/O for is deduced from the tmpfile extension.  When
 * the --indexed option is used, an indexed image is generated ; otherwise
 * an RGB image is generated.
 *
 * WARNING: This class is not part of the core Raja API.  It is primarily
 * used by Raja developpers to validate their implementations.  It does not
 * contain much error handling.
 */

class CheckImageIO
{
    public static void main(String[] argv) throws IOException
    {
        // Parse command line arguments
        boolean indexed = false;
        int index = 0;

        if (argv[index].equals("--indexed"))
        {
            indexed = true;
            index++;
        }

        String tmpFileName = argv[index++];


        // Deduce codec from temporary file name extension
        String codec = ImageIO.suffix2Codec(tmpFileName);

        if (codec == null)
        {
            System.err.println("Could not deduce codec for file: " + tmpFileName);
            System.err.println("CheckImageIO aborted.");
            System.exit(1);
        }


        // Set maximum width and height
        int maxWidth = 2048;
        int maxHeight = 2048;


        // Create a new random test image
        RenderedImage testImage = createRandomImage(maxWidth, maxHeight, indexed);
        System.out.println("done.\n");


        // Write test image in output file
        System.out.println("Writing " + codec + " file: " + tmpFileName + "...");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFileName));
        ImageWriter writer = ImageIO.createImageWriter(codec, os);

        if (writer == null)
        {
            System.err.println("Could not find an image writer for this codec: " +
                               codec);
            System.err.println("CheckImageIO aborted.");
            System.exit(1);
        }

        Object writeParam = writer.createDefaultImageWriteParam(testImage);
        writer.setImageWriteParam(writeParam);
        writer.write(testImage);


        // Close the output stream
        os.close();
        System.out.println("done.\n");


        // Read input image
        System.out.println("Reading " + codec + " file: " + tmpFileName + "...");
        InputStream is = new BufferedInputStream(new FileInputStream(tmpFileName));
        ImageReader reader = ImageIO.createImageReader(codec, is);

        if (reader == null)
        {
            System.err.println("Could not find an image reader for this codec: " +
                               codec);
            System.err.println("CheckImageIO aborted.");
            System.exit(1);
        }

        RenderedImage readImage = reader.read();
        System.out.println("done.\n");


        // Diff the original test image and the encoded-decoded version
        System.out.println("Diffing images...");
        DiffImages.diff(testImage, readImage, false);
        System.out.println("done.");


        // Close the input stream
        // Remark: trying to access the input image after closing the input
        // stream may fail, since the input image is decoded as needed
        is.close();
    }

    static RenderedImage createRandomImage(int maxWidth, int maxHeight,
                                           boolean indexed)
    {
        int width  = Math.round((float) (maxWidth * Math.random()));
        int height = Math.round((float) (maxWidth * Math.random()));

        int imageType;

        if (indexed)
        {
            imageType = BufferedImage.TYPE_BYTE_INDEXED;
            System.out.println("Creating a random INDEXED " +
                               width + "x" + height + " image...");
        }
        else
        {
            imageType = BufferedImage.TYPE_3BYTE_BGR;
            System.out.println("Creating a random 3BYTE_BGR " +
                               width + "x" + height + " image...");
        }

        BufferedImage image = new BufferedImage(width, height, imageType);

        for (int i = 0 ; i < width ; i++)
        {
            for (int j = 0 ; j < height ; j++)
            {
                long random_pos_long = Math.round(Long.MAX_VALUE * Math.random());
                int random_int = (int) (random_pos_long >> 16);
                int rgb = 0xFF000000 | random_int;
                image.setRGB(i, j, rgb);
            }
        }

        return image;
    }
}
