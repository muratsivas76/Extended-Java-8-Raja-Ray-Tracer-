/* $Id: Resolution.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
 * Copyright (C) 1999-2000 E. Fleury & G. Sutre
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

package raja.renderer;

public class Resolution implements java.io.Serializable
{
  /**
   * The width resolution.
   *
   * @serial
   */
  public int width;
  
  /**
   * The height resolution.
   *
   * @serial
   */
  public int height;
  
  /**
   * Constructs a Resolution and initializes it to the specified width and
   * specified height.
   * @param width the specified width resolution
   * @param height the specified height resolution
   */
  public Resolution(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  /**
   * Returns a textual <code>String</code> representation of this
   * <code>Resolution</code> object.
   */
  public String toString()
  {
    return width + "x" + height;
  }
}
