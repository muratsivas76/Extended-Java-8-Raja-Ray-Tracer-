/* $Id: BasicTexturedForm.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class BasicTexturedForm implements TexturedForm, java.io.Serializable, Writable
{
  private Form form;
  private Texture in, out;
  
  public BasicTexturedForm(Form f, Texture in, Texture out)
  {
    this.form = f;
    this.in   = in;
    this.out  = out;
  }
  
  public BasicTexturedForm(Form f, Texture texture)
  {
    this.form = f;
    this.in   = texture;
    this.out  = texture;
  }
  
  // ADDED by Murat Inan
  public Form getForm() {
    return this.form;
  }
  public Texture getTextureIN() {
    return this.in;
  }
  public Texture getTextureOUT() {
    return this.out;
  }
  ////////////////
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("form",null);
    map.put("texture",null);
    map.put("textureIn",null);
    map.put("textureOut",null);
    
    /* Parsing */
    reader.readFields(map);
    
    if (map.get("texture") != null)
    {
      return new BasicTexturedForm((Form) map.get("form"),
      (Texture) map.get("texture"));
    }
    else
    {
      return new BasicTexturedForm((Form) map.get("form"),
        (Texture) map.get("textureIn"),
      (Texture) map.get("textureOut"));
    }
  }
  
  public TexturedLocalGeometry intersection(Ray r)
  {
    class BasicTexturedLocalGeometry extends TexturedLocalGeometry
    {
      Texture in, out;
      
      BasicTexturedLocalGeometry(LocalGeometry lgf, Texture in, Texture out)
      {
        super(lgf);
        this.in  = in;
        this.out = out;
      }
      
      public LocalTexture getInLocalTexture()
      {
        return in.getLocalTexture(this);
      }
      public LocalTexture getOutLocalTexture()
      {
        return out.getLocalTexture(this);
      }
    }
    
    LocalGeometry intersection = form.intersection(r);
    
    if (intersection == null) {
      return null;
    }
    else {
      return new BasicTexturedLocalGeometry(intersection, in, out);
    }
  }
  public boolean intersects(LightRay r)
  {
    return form.intersects(r);
  }
  public boolean contains(Point3D p)
  {
    return form.contains(p);
  }
  public boolean strictlyContains(Point3D p)
  {
    return form.strictlyContains(p);
  }
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  // ADDED by Murat Inan for transform support
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields;
    
    // Form'un transform'unu al (BasicForm olduğunu biliyoruz ve null olamaz)
    Matrix4 transform = ((BasicForm)form).getTransform();
    
    if (in == out) {
      fields = new Object[3][2];
      fields[0][0] = "form";
      fields[0][1] = form;
      fields[1][0] = "texture";
      fields[1][1] = in;
      fields[2][0] = "transform";
      fields[2][1] = transform;
    }
    else {
      fields = new Object[4][2];
      fields[0][0] = "form";
      fields[0][1] = form;
      fields[1][0] = "textureIn";
      fields[1][1] = in;
      fields[2][0] = "textureOut";
      fields[2][1] = out;
      fields[3][0] = "transform";
      fields[3][1] = transform;
    }
    
    writer.writeFields(fields);
  }
  
  // Original write method
  /**
  public void write(ObjectWriter writer) throws java.io.IOException
  {
  Object[][] fields;
  
  if (in == out) {
    fields = new Object[2][2];
  fields[1][0] = "texture";
  fields[1][1] = in;
  }
  else {
    fields = new Object[3][2];
  fields[1][0] = "textureIn";
  fields[1][1] = in;
  fields[2][0] = "textureOut";
  fields[2][1] = out;
  }
  
  fields[0][0] = "form";
  fields[0][1] = form;
  
  writer.writeFields(fields);
  }
   */
}
