// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CheckerboardTexture implements Texture, java.io.Serializable, Writable
{
  private RGB kd1, kd2, kr, kt;
  private int ns, nt;
  private double step, x0, y0, z0;
  
  public CheckerboardTexture(RGB kd1, RGB kd2, RGB kr, RGB kt,
    int ns, int nt,
  double step, double x0, double y0, double z0)
  {
    this.kd1 = kd1;
    this.kd2 = kd2;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.step = step;
    this.x0 = x0;
    this.y0 = y0;
    this.z0 = z0;
  }
  
  /**
   * Builds the object from ObjectReader.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("kdUn", null);
    map.put("kdDeux", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    map.put("step", null);
    map.put("x", null);
    map.put("y", null);
    map.put("z", null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new CheckerboardTexture(
      (RGB) map.get("kdUn"),
      (RGB) map.get("kdDeux"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("step")).doubleValue(),
      ((Number) map.get("x")).doubleValue(),
      ((Number) map.get("y")).doubleValue(),
      ((Number) map.get("z")).doubleValue()
    );
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    // 3D checkerboard pattern - works on all surfaces including boxes
    double i = Math.floor((p.x - x0) / step);
    double j = Math.floor((p.y - y0) / step);
    double k = Math.floor((p.z - z0) / step);
    
    if ((i + j + k) % 2 == 0) {
      return new LocalTexture(kd1, kr, kt, ns, nt);
      } else {
      return new LocalTexture(kd2, kr, kt, ns, nt);
    }
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String CHECKERBOARD_STR="Constructor is: CheckerboardTexture(RGB kd1, RGB kd2, RGB kr, RGB kt, int ns, int nt, double step, double x0, double y0, double z0);\nExample:\n1.0,1.0,1.0,  0.0,0.0,0.0,  0.0,1.0,0.0,  0.5,0.5,0.5,  100, 10, 4.5, 0.0, 0.0, 0.0\nEnter your values after three diyez symbol\n###\n";
    return CHECKERBOARD_STR;
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
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //1.0,1.0,1.0,  0.0,0.0,0.0,  0.0,1.0,0.0,  0.5,0.5,0.5,
    //100, 10,
    //0.5, 0.0, 0.0, 0.0, true
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
      
      int ns = Integer.parseInt(split[12]);
      int nt = Integer.parseInt(split[13]);
      
      double step = Double.parseDouble(split[14]);
      double ud = Double.parseDouble(split[15]);
      double vd = Double.parseDouble(split[16]);
      double zd = Double.parseDouble(split[17]);
      
      texture = new CheckerboardTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
        new RGB(c4r, c4g, c4b),
      ns, nt, step, ud, vd, zd);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "kdUn", kd1 },
      { "kdDeux", kd2 },
      { "kr", kr },
      { "kt", kt },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) },
      { "step", new Double(step) },
      { "x", new Double(x0) },
      { "y", new Double(y0) },
      { "z", new Double(z0) },
    };
    writer.writeFields(fields);
  }
  
}
