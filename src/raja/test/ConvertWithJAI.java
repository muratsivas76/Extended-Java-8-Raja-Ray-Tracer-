/* $Id: ConvertWithJAI.java,v 1.2 2001/02/18 08:29:24 gregoire Exp $
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
 * Program to convert images, using JAI directly.
 *
 * Usage: java raja.test.ConvertWithJAI infile outfile
 *
 * WARNING: This class is not part of the core Raja API.  It is primarily
 * used by Raja developpers to validate their implementations.  It does not
 * contain much error handling.
 */

class ConvertWithJAI
{
    public static void main(String[] argv) throws IOException
    {
        // Parse command line arguments
        int index = 0;
        String inputFileName = argv[index++];
        String outputFileName = argv[index++];


        // Deduce input and output codecs from file name extensions
        String inputCodec = raja.io.ImageIO.suffix2Codec(inputFileName);

        if (inputCodec == null)
        {
            System.err.println("Could not deduce codec for file: " + inputFileName);
            System.err.println("ConvertWithJAI aborted.");
            System.exit(1);
        }

        String outputCodec = raja.io.ImageIO.suffix2Codec(outputFileName);

        if (outputCodec == null)
        {
            System.err.println("Could not deduce codec for file: " + outputFileName);
            System.err.println("ConvertWithJAI aborted.");
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
            System.err.println("ConvertWithJAI aborted.");
            System.exit(1);
        }

        RenderedImage image = dec.decodeAsRenderedImage();
        System.out.println("done.\n");


        // Write image in output file
        System.out.println("Writing " + outputCodec + " file: " + outputFileName + "...");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFileName));
        ImageEncoder enc = ImageCodec.createImageEncoder(outputCodec, os, null);

        if (enc == null)
        {
            System.err.println("Could not find an image encoder for this codec: " +
                               outputCodec);
            System.err.println("ConvertWithJAI aborted.");
            System.exit(1);
        }

        enc.encode(image);


        // Close the output stream
        os.close();
        System.out.println("done.");


        // Close the input stream
        // Remark: trying to access the input image after closing the input
        // stream may fail, since the input image is decoded as needed
        is.close();
    }
}
