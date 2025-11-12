/* $Id: PointLightSource.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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
import raja.io.*;
import java.util.HashMap;


public class PointLightSource implements LightSource, java.io.Serializable, Writable
{
  private RGB light;
  private Point3D origin;
  
  public PointLightSource(Point3D p, RGB light)
  {
    origin = p;
    this.light = light;
  }
  public PointLightSource(double x, double y, double z, RGB light)
  {
    origin = new Point3D(x, y, z);
    this.light = light;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("origin",null);
    map.put("light",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new PointLightSource((Point3D) map.get("origin"),
    (RGB) map.get("light"));
  }
  
  public LightRay getLightRay(Point3D p)
  {
    return new LightRay(p, origin, light);
  }
  public RGB getMax()
  {
    return light;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String POINT_LIGHT_STR = "Constructor is: PointLightSource(double x, double y, double z, RGB light);\nExample:\n-3,0,5,  1.0, 1.0, 1.0\nEnter your values after three diyez symbol\n###\n";
    return POINT_LIGHT_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public LightSource getInstance(String info)
  {
    this.exampleString = info;
    
    LightSource ls = new PointLightSource(
      new Point3D(-3.0, 0.0, 5.0),
      new RGB(1.0, 1.0, 1.0)
    );
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return ls;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    if (str.length () < 11) return ls;
    
    String [] split = str.split (",");
    if (split == null) return ls;
    if (split.length < 6) return ls;
    
    try {
      double pdx = Double.parseDouble(split[0]);
      double pdy = Double.parseDouble(split[1]);
      double pdz = Double.parseDouble(split[2]);
      
      double r = Double.parseDouble(split[3]);
      double g = Double.parseDouble(split[4]);
      double b = Double.parseDouble(split[5]);
      
      ls = new PointLightSource(pdx, pdy, pdz, new RGB(r, g, b));
      return ls;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return ls;
    }
  }
  ////////////////
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "origin", origin },
    { "light", light } };
    writer.writeFields(fields);
  }
}
