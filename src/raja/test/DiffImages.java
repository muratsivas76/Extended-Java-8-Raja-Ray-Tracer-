/* $Id: DiffImages.java,v 1.2 2001/02/18 08:29:24 gregoire Exp $
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

import java.io.*;
import java.awt.image.*;
import raja.io.*;


/*
 * Program to diff images.  The maximum and average differences between
 * pixels (RGBcomponentwise) are reported.
 *
 * Usage: java raja.test.DiffImages [-v] file1 file2
 *
 * The -v option turns verbosity on: all differing pixels will be reported.
 *
 * WARNING: This class is not part of the core Raja API.  It is primarily
 * used by Raja developpers to validate their implementations.  It does not
 * contain much error handling.
 */

class DiffImages
{
    public static void main(String[] argv) throws java.io.IOException
    {
        // Parse command line arguments
        boolean verbose = false;
        int index = 0;

        if (argv[index].equals("-v"))
        {
            verbose = true;
            index++;
        }

        String fileName1 = argv[index++];
        String fileName2 = argv[index++];


        // Deduce input codecs from file name extensions
        String codec1 = ImageIO.suffix2Codec(fileName1);

        if (codec1 == null)
        {
            System.err.println("Could not deduce codec for file: " + fileName1);
            System.err.println("DiffImages aborted.");
            System.exit(1);
        }

        String codec2 = ImageIO.suffix2Codec(fileName2);

        if (codec2 == null)
        {
            System.err.println("Could not deduce codec for file: " + fileName2);
            System.err.println("DiffImages aborted.");
            System.exit(1);
        }


        // Read input images
        System.out.println("Reading " + codec1 + " file: " + fileName1 + "...");
        InputStream is1 = new BufferedInputStream(new FileInputStream(fileName1));
        ImageReader reader1 = ImageIO.createImageReader(codec1, is1);

        if (reader1 == null)
        {
            System.err.println("Could not find an image reader for this codec: " +
                               codec1);
            System.err.println("DiffImages aborted.");
            System.exit(1);
        }

        RenderedImage image1 = reader1.read();
        System.out.println("done.\n");

        System.out.println("Reading " + codec2 + " file: " + fileName2 + "...");
        InputStream is2 = new BufferedInputStream(new FileInputStream(fileName2));
        ImageReader reader2 = ImageIO.createImageReader(codec2, is2);

        if (reader2 == null)
        {
            System.err.println("Could not find an image reader for this codec: " +
                               codec2);
            System.err.println("DiffImages aborted.");
            System.exit(1);
        }

        RenderedImage image2 = reader2.read();
        System.out.println("done.\n");


        System.out.println("Image 1 is a " + codec1 + " file: " + fileName1);
        System.out.println("Image 2 is a " + codec2 + " file: " + fileName2);
        System.out.println();


        // Diff the images
        System.out.println("Diffing images...");
        diff(image1, image2, verbose);
        System.out.println("done.");


        // Close the input and output streams
        // Remark: trying to access the input image after closing the input
        // stream may fail, since the input image is decoded as needed
        is1.close();
        is2.close();
    }

    static void diff(RenderedImage image1, RenderedImage image2, boolean verbose)
    {
        int width = image1.getWidth();
        int height = image2.getHeight();

        if ((image2.getWidth() != width) || (image2.getHeight() != height))
        {
            System.out.println("The 2 images have a different resolution!");
            System.out.println("DiffImages aborted.");
            return;
        }

        System.out.println("Resolution: " + width + " x " + height);

        Raster r1 = image1.getData();
        Raster r2 = image2.getData();
        ColorModel cm1 = image1.getColorModel();
        ColorModel cm2 = image2.getColorModel();

        int[] max = {0, 0, 0};
        double[] sum = {0, 0, 0};

        int[] pix1 = new int[3];
        int[] pix2 = new int[3];

        for (int i = 0 ; i < width ; i++)
        {
            for (int j = 0 ; j < height ; j++)
            {
                int rgb1 = cm1.getRGB(r1.getDataElements(i, j, null));
                int rgb2 = cm2.getRGB(r2.getDataElements(i, j, null));

                pix1[0] = (rgb1 >> 16) & 0xFF;   // red
                pix1[1] = (rgb1 >> 8)  & 0xFF;   // green
                pix1[2] = (rgb1 >> 0)  & 0xFF;   // blue

                pix2[0] = (rgb2 >> 16) & 0xFF;   // red
                pix2[1] = (rgb2 >> 8)  & 0xFF;   // green
                pix2[2] = (rgb2 >> 0)  & 0xFF;   // blue

                if ((pix1[0] != pix2[0]) ||
                    (pix1[1] != pix2[1]) ||
                    (pix1[2] != pix2[2]))
                {
                    if (verbose)
                    {
                        System.out.println("Difference at pixel (" +
                                           i + ", " + j + ")");
                        System.out.println("   image 1 : [" +
                                           pix1[0] + ", " +
                                           pix1[1] + ", " +
                                           pix1[2] + "]");
                        System.out.println("   image 2 : [" +
                                           pix2[0] + ", " +
                                           pix2[1] + ", " +
                                           pix2[2] + "]");
                        System.out.println();
                    }

                    for(int k = 0 ; k < 3 ; k++)
                    {
                        max[k] = Math.max(max[k], Math.abs(pix1[k] - pix2[k]));
                        sum[k] = sum[k] + Math.abs(pix1[k] - pix2[k]);
                    }
                }
            }
        }

        System.out.println("Maximum difference: [" +
                           max[0] + ", " +
                           max[1] + ", " +
                           max[2] + "]");
        System.out.println("Average difference: [" +
                           (sum[0] / (width*height)) + ", " +
                           (sum[1] / (width*height)) + ", " +
                           (sum[2] / (width*height)) + "]");
    }
}
