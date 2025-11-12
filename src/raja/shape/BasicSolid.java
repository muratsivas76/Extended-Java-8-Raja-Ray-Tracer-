/* $Id: BasicSolid.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import raja.*;
import raja.io.*;
import java.util.HashMap;


public class BasicSolid implements Solid, java.io.Serializable, Writable
{
  private TexturedForm texturedForm;
  private Volume volume;
  
  public BasicSolid(TexturedForm f, Volume v)
  {
    texturedForm = f;
    volume = v;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("texturedForm", null);
    map.put("volume", new IsotropicVolume(1.0));
    
    /* Parsing */
    reader.readFields(map);
    
    return new BasicSolid((TexturedForm) map.get("texturedForm"),
    (Volume) map.get("volume"));
  }
  
  public SolidLocalGeometry intersection(Ray r)
  {
    TexturedLocalGeometry intersection = texturedForm.intersection(r);
    
    if (intersection == null) {
      return null;
    }
    else {
      SolidLocalGeometry result = new SolidLocalGeometry(intersection);
      result.setInVolume(volume);
      return result;
    }
  }
  public boolean intersects(LightRay r)
  {
    return texturedForm.intersects(r);
  }
  public boolean contains(Point3D p)
  {
    return texturedForm.contains(p);
  }
  public boolean strictlyContains(Point3D p)
  {
    return texturedForm.strictlyContains(p);
  }
  public double refractiveIndex(Point3D p)
  {
    return volume.refractiveIndex(p);
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "texturedForm", texturedForm },
    { "volume", volume } };
    writer.writeFields(fields);
  }
}
