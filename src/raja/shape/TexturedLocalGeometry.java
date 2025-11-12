/* $Id: TexturedLocalGeometry.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

package raja.shape;

import raja.Vector3D;


public abstract class TexturedLocalGeometry extends LocalGeometry
{
  private LocalGeometry lg;
  
  protected TexturedLocalGeometry(LocalGeometry lg)
  {
    super(lg);
    this.lg = lg;
  }
  
  AcneCorrection getAcneCorrection()
  {
    return lg.getAcneCorrection();
  }
  public Vector3D getNormal()
  {
    return lg.getNormal();
  }
  public abstract LocalTexture getInLocalTexture();
  public abstract LocalTexture getOutLocalTexture();
}
