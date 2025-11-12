/* $Id: Complement.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class Complement implements TexturedForm, java.io.Serializable, Writable
{
  private TexturedForm texturedForm;
  
  public Complement(TexturedForm f)
  {
    texturedForm = f;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("texturedForm",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Complement((TexturedForm) map.get("texturedForm"));
  }
  
  public TexturedLocalGeometry intersection(Ray r)
  {
    class ComplementTexturedLocalGeometry extends TexturedLocalGeometry
    {
      TexturedLocalGeometry lgf;
      
      ComplementTexturedLocalGeometry(TexturedLocalGeometry lgf)
      {
        super(lgf);
        this.lgf = lgf;
      }
      
      AcneCorrection getAcneCorrection()
      {
        return lgf.getAcneCorrection();
      }
      public Vector3D getNormal()
      {
        return Vector3D.opposite(lgf.getNormal());
      }
      public LocalTexture getInLocalTexture()
      {
        return lgf.getOutLocalTexture();
      }
      public LocalTexture getOutLocalTexture()
      {
        return lgf.getInLocalTexture();
      }
    }
    
    TexturedLocalGeometry intersection = texturedForm.intersection(r);
    
    if (intersection == null) {
      return null;
    }
    else {
      return new ComplementTexturedLocalGeometry(intersection);
    }
  }
  public boolean intersects(LightRay r)
  {
    return texturedForm.intersects(r);
  }
  public boolean contains(Point3D p)
  {
    return (! texturedForm.strictlyContains(p));
  }
  public boolean strictlyContains(Point3D p)
  {
    return (! texturedForm.contains(p));
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "texturedForm", texturedForm } };
    writer.writeFields(fields);
  }
}
