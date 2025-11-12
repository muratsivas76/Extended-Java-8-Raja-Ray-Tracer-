/* $Id: HorizontalCamera.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import raja.*;
import raja.io.*;
import java.util.HashMap;


public class HorizontalCamera extends Camera implements Writable
{
  private double screenWidth, screenHeight;
  private Vector3D direction;   // vecteur normé
  private double focal;
  private Point3D origin, screenTopLeft, screenTopRight, screenBottomLeft;
  
  public HorizontalCamera(Point3D origin, Vector3D direction, double focal, double screenWidth, double screenHeight)
  {
    super();
    this.origin = origin;
    this.direction = Vector3D.normalization(direction);
    this.focal = focal;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    
    
    // Computation of the screen's corners
    
    Vector3D d = this.direction;
    Point3D screenCenter = new Point3D(origin);
    screenCenter.translate(Vector3D.product(d, focal));
    
    Vector3D u = Vector3D.normalization(new Vector3D(-d.y, d.x, 0));
    Vector3D v = Vector3D.crossProduct(d, u);
    Vector3D uc = Vector3D.product(u, screenWidth / 2);
    Vector3D vc = Vector3D.product(v, screenHeight / 2);
    
    screenTopLeft = new Point3D(screenCenter, Vector3D.sum(uc, vc));
    screenTopRight = new Point3D(screenCenter, Vector3D.sum(Vector3D.opposite(uc), vc));
    screenBottomLeft = new Point3D(screenCenter, Vector3D.sum(uc, Vector3D.opposite(vc)));
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("screenWidth",null);
    map.put("screenHeight",null);
    map.put("focal",null);
    map.put("direction",null);
    map.put("origin",null);
    map.put("pixelWidth",null);
    map.put("pixelHeight",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new HorizontalCamera((Point3D) map.get("origin"),
      (Vector3D) map.get("direction"),
      ((Number) map.get("focal")).doubleValue(),
      ((Number) map.get("screenWidth")).doubleValue(),
    ((Number) map.get("screenHeight")).doubleValue());
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String HORIZONTAL_CAMERA_STR = "Constructor is: HorizontalCamera(Point3D origin, Vector3D direction, double focal, double screenWidth, double screenHeight);\nExample:\n-5,0,0,  1,0,0,  1.8,  2,  1.5\nEnter your values after three diyez symbol\n###\n";
    return HORIZONTAL_CAMERA_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Camera getInstance(String info)
  {
    this.exampleString = info;
    
    Camera xcam = new HorizontalCamera(
      new Point3D(-3.0, 0.0, 0.0),    // Point
      new Vector3D(1.0, 0.0, 0.0),   // X eksenine bak
      1.8, 2.0, 1.5                  // Focal, screen width/height
    );
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return xcam;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll(" ", "");
    str = str.replaceAll("\n", "");
    
    if (str.length () < 17) return xcam;
    
    String [] split = str.split (",");
    if (split == null) return xcam;
    if (split.length < 9) return xcam;
    
    try {
      double px = Double.parseDouble(split[0]);
      double py = Double.parseDouble(split[1]);
      double pz = Double.parseDouble(split[2]);
      
      double dx = Double.parseDouble(split[3]);
      double dy = Double.parseDouble(split[4]);
      double dz = Double.parseDouble(split[5]);
      
      double focal = Double.parseDouble(split[6]);
      double scrw = Double.parseDouble(split[7]);
      double scrh = Double.parseDouble(split[8]);
      
      xcam = new HorizontalCamera (
        new Point3D(px, py, pz), new Vector3D(dx, dy, dz), focal, scrw, scrh
      );
      return xcam;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return xcam;
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
      { "direction", direction },
      { "focal", new Double(focal) },
      { "screenWidth", new Double(screenWidth) },
    { "screenHeight", new Double(screenHeight) } };
    writer.writeFields(fields);
  }
  
  /**
  // ADDED by Murat Inan
  Ray getRay(double x, double y)
  {
  Point3D pixel = new Point3D(
  screenTopLeft.x + (x * (screenTopRight.x - screenTopLeft.x)) + (y * (screenBottomLeft.x - screenTopLeft.x)),
  screenTopLeft.y + (x * (screenTopRight.y - screenTopLeft.y)) + (y * (screenBottomLeft.y - screenTopLeft.y)),
  screenTopLeft.z + (x * (screenTopRight.z - screenTopLeft.z)) + (y * (screenBottomLeft.z - screenTopLeft.z))
  );
  
  Vector3D direct = new Vector3D(origin, pixel);
  
  // Ray kamera origin'den başlamalı, pixel'e doğru gitmeli
  return new Ray(origin, direct);  // Correct
  }
   */
  
  Ray getRay(double x, double y)
  {
    Point3D pixel = new Point3D(screenTopLeft.x + (x * (screenTopRight.x - screenTopLeft.x)) + (y * (screenBottomLeft.x - screenTopLeft.x)),
      screenTopLeft.y + (x * (screenTopRight.y - screenTopLeft.y)) + (y * (screenBottomLeft.y - screenTopLeft.y)),
    screenTopLeft.z + (x * (screenTopRight.z - screenTopLeft.z)) + (y * (screenBottomLeft.z - screenTopLeft.z)));
    
    Vector3D direct = new Vector3D(origin, pixel);
    return new Ray(pixel, direct);
  }
  
}
