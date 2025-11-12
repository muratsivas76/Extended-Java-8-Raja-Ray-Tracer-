/* $Id: CompositeForm.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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
import raja.util.List;

import java.util.HashMap;
import java.util.Iterator;


public abstract class CompositeForm implements TexturedForm, java.io.Serializable, Writable
{
  private List texturedForms;
  
  public CompositeForm()
  {
    texturedForms = new List();
  }
  
  public CompositeForm(List texturedForms)
  {
    this.texturedForms = texturedForms;
  }
  
  public void addForm(TexturedForm f)
  {
    texturedForms.add(f);
  }
  protected Iterator formIterator()
  {
    return texturedForms.iterator();
  }
  
  public abstract TexturedLocalGeometry intersection(Ray r);
  public abstract boolean contains(Point3D p);
  public abstract boolean strictlyContains(Point3D p);
  
  public boolean intersects(LightRay r)
  {
    Point3D intersection = intersection(r);
    if (intersection != null) {
      double d = Point3D.distance(r.origin, intersection);
      if (d < r.distance) {
        return true;
      }
    }
    return false;
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "texturedForms", texturedForms } };
    writer.writeFields(fields);
  }
}
