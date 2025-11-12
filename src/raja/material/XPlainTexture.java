/* $Id: XPlainTexture.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

package raja.material;

import raja.Point3D;
import raja.RGB;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;


public class XPlainTexture implements Texture, java.io.Serializable, Writable
{
  private RGB kd = RGB.red;
  private RGB krl = RGB.green;
  private RGB krg = RGB.blue;
  private RGB ktl = RGB.magenta;
  private RGB ktg = RGB.yellow;
  int ns = 40;
  int nt = 10;
  
  public XPlainTexture()
  {}
  
  public XPlainTexture(RGB kd, RGB krl, RGB krg, RGB ktl, RGB ktg, int ns, int nt)
  {
    this.kd = kd;
    this.krl = krl;
    this.krg = krg;
    this.ktl = ktl;
    this.ktg = ktg;
    this.ns = ns;
    this.nt = nt;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    return new XPlainTexture();
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    LocalTexture lcl = new LocalTexture(
      this.kd,
      this.krl,
      this.krg,
      this.ktl,
      this.ktg,
      this.ns,
      this.nt
    );
    
    return lcl;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //XPlainTexture(RGB kd, RGB krl, RGB krg, RGB ktl, RGB ktg, int ns, int nt)
    //0.0,0.0,0.0,  0.0,1.0,0.0,  0.0,0.0,1.0,  0.3,0.3,0.3,  0.1,0.1,0.1,  40, 10
    String PLAIN_STR="Constructor is: XPlainTexture(RGB kd, RGB krl, RGB krg, RGB ktl, RGB ktg, int ns, int nt);\nExample:\n0.0,0.0,0.0,  0.0,0.0,0.0,  0.0,0.0,0.0,  0.9,0.9,0.9,  0.9,0.9,0.9,  100, 10\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return PLAIN_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    
    Texture texture = null;
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return texture;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    if (str.equals("-1")) return new XPlainTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //XPlainTexture(RGB kd, RGB krl, RGB krg, RGB ktl, RGB ktg, int ns, int nt)
    //1.0,0.0,0.0,  0.0,1.0,0.0,  0.0,0.0,1.0,  0.3,0.3,0.3,  0.1,0.1,0.1,  40, 10
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double c3r = Double.parseDouble(split[6]);
      double c3g = Double.parseDouble(split[7]);
      double c3b = Double.parseDouble(split[8]);
      
      double c4r = Double.parseDouble(split[9]);
      double c4g = Double.parseDouble(split[10]);
      double c4b = Double.parseDouble(split[11]);
      
      double c5r = Double.parseDouble(split[12]);
      double c5g = Double.parseDouble(split[13]);
      double c5b = Double.parseDouble(split[14]);
      
      int ns = Integer.parseInt(split[15]);
      int nt = Integer.parseInt(split[16]);
      
      texture = new XPlainTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
        new RGB(c4r, c4g, c4b),
        new RGB(c5r, c5g, c5b),
      ns, nt);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public String toString()
  {
    return "";
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
  }
  
}
