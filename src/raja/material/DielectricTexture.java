// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;
import java.util.HashMap;

public class DielectricTexture implements Texture, java.io.Serializable, Writable
{
  private RGB kd, kr, kt;
  private int ns, nt;
  
  public DielectricTexture(RGB baseColor, double reflection, double transparency, int shininess)
  {
    this.kd = baseColor;
    this.kr = new RGB(reflection, reflection, reflection);
    this.kt = new RGB(transparency, transparency, transparency); // XDamier'deki gibi kt
    this.ns = shininess;
    this.nt = 12;
  }
  
  public DielectricTexture() {
    this.kd = new RGB(0.25, 0.35, 0.45);    // DAHA KOYU MAVİ - daha belirgin cam rengi
    this.kr = new RGB(0.65, 0.65, 0.65);    // REFLECTION biraz azaltıldı
    this.kt = new RGB(0.35, 0.35, 0.35);    // TRANSPARENCY biraz artırıldı
    this.ns = 220;                          // SHININESS korundu
    this.nt = 12;                           // NT biraz azaltıldı
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    return new LocalTexture(kd, kr, kt, ns, nt);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("kd", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    
    reader.readFields(map);
    
    return new DielectricTexture((RGB) map.get("kd"),
      ((Number) map.get("kr")).doubleValue(),
      ((Number) map.get("kt")).doubleValue(),
    ((Number) map.get("ns")).intValue());
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String DIELECTRIC_STR="Constructor is: DielectricTexture(RGB baseColor, double reflection, double transparency, int shininess);\nExample:\n0.25,0.35,0.45,  0.65,  0.35,  220\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return DIELECTRIC_STR;
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
    
    if (str.equals("-1")) return new DielectricTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    //0.25,0.35,0.45,  0.65,  0.35,  220
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      double rf = Double.parseDouble(split[3]);
      double tr = Double.parseDouble(split[4]);
      int sh = Integer.parseInt(split[5]);
      
      texture = new DielectricTexture(new RGB(r, g, b), rf, tr, sh);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "kd", kd },
      { "kr", kr },
      { "kt", kt },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) }
    };
    writer.writeFields(fields);
  }
  
}
