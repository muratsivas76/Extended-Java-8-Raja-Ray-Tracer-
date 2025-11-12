/* $Id: ReduceImage.java,v 1.3 2001/02/18 08:29:24 gregoire Exp $
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
 * Program to reduce images by performing average computations.
 *
 * Usage: java raja.test.ReduceImage factor infile outfile
 *
 * WARNING: This class is not part of the core Raja API.  It is primarily
 * used by Raja developpers to validate their implementations.  It does not
 * contain much error handling.
 */

class ReduceImage
{
    public static void main(String[] argv) throws IOException
    {
        // Parse command line arguments
        int index = 0;
        int reduceFactor = Integer.parseInt(argv[index++]);
        String inputFileName = argv[index++];
        String outputFileName = argv[index++];


        // Deduce input and output codecs from file name extensions
        String inputCodec = ImageIO.suffix2Codec(inputFileName);

        if (inputCodec == null)
        {
            System.err.println("Could not deduce codec for file: " + inputFileName);
            System.err.println("ReduceImage aborted.");
            System.exit(1);
        }

        String outputCodec = ImageIO.suffix2Codec(outputFileName);

        if (outputCodec == null)
        {
            System.err.println("Could not deduce codec for file: " + outputFileName);
            System.err.println("ReduceImage aborted.");
            System.exit(1);
        }


        // Read input image
        System.out.println("Reading " + inputCodec + " file: " + inputFileName + "...");
        InputStream is = new BufferedInputStream(new FileInputStream(inputFileName));
        ImageReader reader = ImageIO.createImageReader(inputCodec, is);

        if (reader == null)
        {
            System.err.println("Could not find an image reader for this codec: " +
                               inputCodec);
            System.err.println("ReduceImage aborted.");
            System.exit(1);
        }

        RenderedImage inputImage = reader.read();
        System.out.println("done.\n");


        // Get input image resolution
        int inputWidth = inputImage.getWidth();
        int inputHeight = inputImage.getHeight();


        // Compute output image resolution
        int outputWidth = inputWidth / reduceFactor;
        int outputHeight = inputHeight / reduceFactor;

        System.out.println("Resolution of input image:  " + inputWidth +
                           " x " + inputHeight);
        System.out.println("Resolution of output image: " + outputWidth +
                           " x " + outputHeight);
        System.out.println();


        // Initialise output image
        Raster inputRas = inputImage.getData();
        WritableRaster outputRas = inputRas.createCompatibleWritableRaster(outputWidth,
                                                                           outputHeight);
        ColorModel colorModel = inputImage.getColorModel();
        BufferedImage outputImage = new BufferedImage(colorModel,
                                                      outputRas,
                                                      false,
                                                      null);


        // Compute output image
        System.out.println("Computing reduced image...");
        long reduceFactorSquare = reduceFactor * reduceFactor;

        for (int i = 0 ; i < outputWidth ; i++)
        {
            for (int j = 0 ; j < outputHeight ; j++)
            {
                double red   = 0;
                double green = 0;
                double blue  = 0;

                for (int h = 0 ; h < reduceFactor ; h++)
                {
                    for (int k = 0 ; k < reduceFactor ; k++)
                    {
                        Object data = inputRas.getDataElements(reduceFactor*i + h,
                                                               reduceFactor*j + k,
                                                               null);
                        int inputPixelRGB = colorModel.getRGB(data);

                        red   += (inputPixelRGB >> 16) & 0xFF;
                        green += (inputPixelRGB >> 8)  & 0xFF;
                        blue  += (inputPixelRGB >> 0)  & 0xFF;
                    }
                }

                red   /= reduceFactorSquare;
                green /= reduceFactorSquare;
                blue  /= reduceFactorSquare;

                int red_int   = Math.round((float) red);
                int green_int = Math.round((float) green);
                int blue_int  = Math.round((float) blue);

                int outputPixelRGB =               (0xFF << 24) |
                                       ((red_int & 0xFF) << 16) |
                                     ((green_int & 0xFF) << 8)  |
                                      ((blue_int & 0xFF) << 0);

                outputImage.setRGB(i, j, outputPixelRGB);
            }
        }

        System.out.println("done.\n");


        // Close the input stream
        // Remark: trying to access the input image after closing the input
        // stream may fail, since the input image is decoded as needed
        is.close();


        // Write reduced image in output file
        System.out.println("Writing " + outputCodec + " file: " + outputFileName + "...");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFileName));
        ImageWriter writer = ImageIO.createImageWriter(outputCodec, os);

        if (writer == null)
        {
            System.err.println("Could not find an image writer for this codec: " +
                               outputCodec);
            System.err.println("ReduceImage aborted.");
            System.exit(1);
        }

        Object writeParam = writer.createDefaultImageWriteParam(outputImage);
        writer.setImageWriteParam(writeParam);
        writer.write(outputImage);


        // Close the output stream
        os.close();
        System.out.println("done.");
    }
}
