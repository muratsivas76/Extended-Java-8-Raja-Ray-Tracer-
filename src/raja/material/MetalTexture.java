// Murat Inan
package raja.material;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import raja.*;
import raja.shape.*;
import raja.io.*;

public class MetalTexture implements Texture, java.io.Serializable, Writable
{
  private RGB kd, kr, kt;
  private int ns, nt;
  private double step, x0, y0;
  
  // Ana constructor - metal rengi ve özellikleri ile
  public MetalTexture(RGB metalColor, double reflectivity, double shininess)
  {
    // Metal için otomatik renk ayarları
    this.kd = metalColor.multiply(0.3);  // Diffuse kısım koyu
    this.kr = metalColor.multiply(reflectivity);  // Reflection metal renginde
    this.kt = new RGB(0.0, 0.0, 0.0);  // Metals are not transparent
    
    // Shininess ayarı - yüksek shininess = parlak metal
    this.ns = (int)(shininess * 1000);
    this.nt = 1;
    
    // Damier pattern için varsayılan değerler
    this.step = 0.5;
    this.x0 = 0.0;
    this.y0 = 0.0;
  }
  
  // Basit constructor - sadece metal rengi
  public MetalTexture(RGB metalColor)
  {
    this(metalColor, 0.8, 0.9);  // Varsayılan: yüksek reflectivity ve shininess
  }
  
  // RGB ve pattern parametreleri ile constructor
  public MetalTexture(RGB kd, RGB kr, RGB kt, int ns, int nt, double step, double x0, double y0)
  {
    this.kd = kd;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.step = step;
    this.x0 = x0;
    this.y0 = y0;
  }
  
  // Predefined metal types - kolay kullanım için static factory methods
  public static MetalTexture createGold()
  {
    RGB goldColor = new RGB(1.0, 0.84, 0.0);  // Altın rengi
    return new MetalTexture(goldColor, 0.9, 0.95);
  }
  
  public static MetalTexture createSilver()
  {
    RGB silverColor = new RGB(0.75, 0.75, 0.75);  // Gümüş rengi
    return new MetalTexture(silverColor, 0.95, 0.98);
  }
  
  public static MetalTexture createCopper()
  {
    RGB copperColor = new RGB(0.72, 0.45, 0.20);  // Bakır rengi
    return new MetalTexture(copperColor, 0.85, 0.9);
  }
  
  public static MetalTexture createBronze()
  {
    RGB bronzeColor = new RGB(0.55, 0.47, 0.14);  // Bronz rengi
    return new MetalTexture(bronzeColor, 0.8, 0.85);
  }
  
  public static MetalTexture createSteel()
  {
    RGB steelColor = new RGB(0.65, 0.65, 0.67);  // Çelik rengi
    return new MetalTexture(steelColor, 0.7, 0.8);
  }
  
  public static MetalTexture createAluminum()
  {
    RGB aluminumColor = new RGB(0.77, 0.77, 0.80);  // Alüminyum rengi
    return new MetalTexture(aluminumColor, 0.9, 0.92);
  }
  
  public static MetalTexture createPlatinum()
  {
    RGB platinumColor = new RGB(0.90, 0.89, 0.89);  // Platin rengi
    return new MetalTexture(platinumColor, 0.92, 0.96);
  }
  
  /**
   * Builds the object LocalTexture from a StreamLexer.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("kd", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    map.put("step", null);
    map.put("x", null);
    map.put("y", null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new MetalTexture((RGB) map.get("kd"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("step")).doubleValue(),
      ((Number) map.get("x")).doubleValue(),
    ((Number) map.get("y")).doubleValue());
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    // TAMAMEN DÜZ METAL
    return new LocalTexture(kd, kr, kt, ns, nt);
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String METAL_STR="Constructor is: MetalTexture(RGB kd, RGB kr, RGB kt, int ns, int nt, double step, double x0, double y0);\nExample:\n1.0,1.0,0.0,  1.0,0.0,0.0,  0.3,0.3,0.3,  100, 10, 4.5, 0.0, 0.0\nEnter your values after three diyez symbol\n###\n";
    return METAL_STR;
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
    
    //1.0,1.0,0.0,  1.0,0.0,0.0,  0.3,0.3,0.3,  100, 10, 4.5, 0.0, 0.0
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
      
      int ns = Integer.parseInt(split[9]);
      int nt = Integer.parseInt(split[10]);
      
      double step = Double.parseDouble(split[11]);
      double xd = Double.parseDouble(split[12]);
      double yd = Double.parseDouble(split[13]);
      
      texture = new MetalTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
      ns, nt, step, xd, yd);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "kd", kd },
      { "kr", kr },
      { "kt", kt },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) },
      { "step", new Double(step) },
      { "x", new Double(x0) },
    { "y", new Double(y0) } };
    writer.writeFields(fields);
  }
  
  // Getter methods
  public RGB getKd() { return kd; }
  public RGB getKr() { return kr; }
  public RGB getKt() { return kt; }
  public int getNs() { return ns; }
  public int getNt() { return nt; }
  public double getStep() { return step; }
  public double getX0() { return x0; }
  public double getY0() { return y0; }
  
  // Setter methods
  public void setStep(double step) { this.step = step; }
  public void setX0(double x0) { this.x0 = x0; }
  public void setY0(double y0) { this.y0 = y0; }
  
}
