/* $Id: StereoCamera.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class StereoCamera extends Camera
{
  private Camera camera;
  private Vector3D trans;
  
  public StereoCamera(Camera camera, Vector3D trans)
  {
    super();
    this.camera = camera;
    this.trans = trans;
  }
  
  public StereoCamera(Point3D origin, Vector3D direction, double focal, double screenWidth, double screenHeight, double step, boolean cross)
  {
    this(new HorizontalCamera(origin, direction, focal, screenWidth, screenHeight),
      cross ? Vector3D.product(Vector3D.normalization(new Vector3D(direction.y, -direction.x, 0)), step / 2)
    : Vector3D.product(Vector3D.normalization(new Vector3D(-direction.y, direction.x, 0)), step / 2));
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
    map.put("pixelwidth",null);
    map.put("pixelheight",null);
    map.put("step",null);
    map.put("cross",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new StereoCamera((Point3D) map.get("origin"),
      (Vector3D) map.get("direction"),
      ((Number) map.get("focal")).doubleValue(),
      ((Number) map.get("screenWidth")).doubleValue(),
      ((Number) map.get("screenHeight")).doubleValue(),
      ((Number) map.get("step")).doubleValue(),
    false);
  }
  
  Ray getRay(double x, double y)
  {
    Ray r;
    
    if (x < 0.5) {
      r = camera.getRay(2*x, y);
      r.origin.translate(trans);
    }
    else {
      r = camera.getRay(2*x - 1, y);
      r.origin.translate(Vector3D.opposite(trans));
    }
    
    return r;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String STEREO_CAMERA_STR = "Constructor is: StereoCamera(Point3D origin, Vector3D direction, double focal, double screenWidth, double screenHeight, double step, boolean cross);\nExample:\n-5,0,0,  1,0,0,  1.8, 2, 1.5, 2.0, true\nEnter your values after three diyez symbol\n###\n";
    return STEREO_CAMERA_STR;
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
      
      double step = Double.parseDouble (split[9]);
      boolean cross = Boolean.parseBoolean (split[10]);
      
      xcam = new StereoCamera (
        new Point3D(px, py, pz), new Vector3D(dx, dy, dz),
        focal, scrw, scrh, step, cross
      );
      return xcam;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return xcam;
    }
  }
  ////////////////
}
