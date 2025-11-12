/* $Id: IsotropicVolume.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import raja.Point3D;
import raja.io.*;
import java.util.HashMap;


public class IsotropicVolume implements Volume, java.io.Serializable, Writable
{
  double ri;
  
  public IsotropicVolume(double refractiveIndex)
  {
    ri = refractiveIndex;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Parsing */
    Number[] params = reader.readNumbers(1);
    
    return new IsotropicVolume(params[0].doubleValue());
  }
  
  public double refractiveIndex(Point3D p)
  {
    return ri;
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Number[] fields = { new Double(ri) };
    writer.writeFields(fields);
  }
}
