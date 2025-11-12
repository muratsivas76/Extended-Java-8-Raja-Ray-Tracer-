/* $Id: ImageWriter.java,v 1.1 2001/02/16 01:52:10 gregoire Exp $
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

import java.io.IOException;
import java.awt.image.RenderedImage;


/**
 * An interface specifying objects that can write images.  Images are
 * encoded as instances of <code>RenderedImage</code>.
 *
 * @see ImageReader
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public interface ImageWriter
{
  /**
   * Encodes the given <code>RenderedImage</code>.
   *
   * @param image the <code>RenderedImage</code> to be written.
   *
   * @exception IOException if an I/O error occurs
   */
  public void write(RenderedImage image) throws IOException;
  
  /**
   * Returns the current writing parameters.  Concrete implementations of
   * this interface should return an instance of the appropriate subclass
   * or subinterface.
   *
   * @return the current writing parameters.
   */
  public Object getImageWriteParam();
  
  /**
   * Sets the current writing parameters.  Concrete implementations of
   * this interface may throw a <code>RuntimeException</code> if the param
   * argument is not an instance of the appropriate subclass or
   * subinterface.
   *
   * @param param the writing parameters to use.
   */
  public void setImageWriteParam(Object param);
  
  /**
   * Returns the default writing parameters appropriate to encode the
   * given <code>RenderedImage</code>.  Concrete implementations of this
   * interface should return an instance of the appropriate subclass or
   * subinterface.
   *
   * @param image the <code>RenderedImage</code> to be encoded.
   *
   * @return the default writing parameters to encoded the given image.
   */
  public Object createDefaultImageWriteParam(RenderedImage image);
}
