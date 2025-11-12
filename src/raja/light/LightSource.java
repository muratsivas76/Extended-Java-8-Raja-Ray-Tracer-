/* $Id: LightSource.java,v 1.2 2001/02/25 01:31:46 gregoire Exp $
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

package raja.light;

import raja.*;


/**
 * The interface for objects which represent light sources.
 *
 * @see LightRay
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public interface LightSource
{
  /**
   * Returns the <code>LightRay</code> from the specified <code>Point3D</code> to the <code>LightSource</code>.
   * If the <code>LightSource</code> does not directly illuminate the specified <code>Point3D</code>, then this method
   * returns <code>null</code>.
   * @param p a specified <code>Point3D</code>.
   * @return the <code>LightRay</code> from the specified <code>Point3D</code> to the <code>LightSource</code> if
   *         the <code>LightSource</code> directly illuminates this point;
   *	       <code>null</code> otherwise.
   */
  public LightRay getLightRay(Point3D p);
  
  public RGB getMax();
  
  // ADDED by Murat Inan
  abstract public String getUsageInformation();
  abstract public LightSource getInstance(String info);
  abstract public String toExampleString();
  /////////
}
