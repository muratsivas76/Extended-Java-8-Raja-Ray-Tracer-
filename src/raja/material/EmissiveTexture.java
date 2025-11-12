// Murat Inan
package raja.material;

import java.util.HashMap;

import raja.*;
import raja.shape.*;
import raja.io.*;

public class EmissiveTexture implements Texture, java.io.Serializable, Writable
{
  private RGB emissiveColor;
  private double emissiveStrength;
  
  public EmissiveTexture(RGB color, double emissiveStrength)
  {
    this.emissiveColor = color;
    this.emissiveStrength = emissiveStrength;
  }
  
  public EmissiveTexture(RGB color)
  {
    this(color, 2.0);
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    // Base color (normal shading için)
    RGB baseColor = emissiveColor;
    
    // Emissive boost (parlak ama shape'i bozmayacak kadar)
    double r = Math.min(1.0, emissiveColor.getR() * emissiveStrength);
    double g = Math.min(1.0, emissiveColor.getG() * emissiveStrength);
    double b = Math.min(1.0, emissiveColor.getB() * emissiveStrength);
    
    RGB brightColor = new RGB(r, g, b);
    
    // Normal shading ile emissive'i dengele
    return new LocalTexture(brightColor,
      new RGB(0.1, 0.1, 0.1),  // biraz reflection (shading için)
      new RGB(0.0, 0.0, 0.0),
      50,   // ns: düzgün shading
    1);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("emissiveColor", null);
    map.put("emissiveStrength", new Double(2.0));
    reader.readFields(map);
    return new EmissiveTexture(
      (RGB) map.get("emissiveColor"),
      ((Number) map.get("emissiveStrength")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //EmissiveTexture(RGB color, double emissiveStrength)
    String EMISSIVE_STR="Constructor is: EmissiveTexture(RGB color, double emissiveStrength);\nExample:\n0,1,0,  2.0\nEnter your values after three diyez symbol\n###\n";
    return EMISSIVE_STR;
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
    
    //if (str.equals("-1")) return new EmissiveTexture(RGB.green);
    
    String [] split = str.split (",");
    if (split == null) return texture;
    //0.85,0.85,0.0,  2.0
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      double st = Double.parseDouble(split[3]);
      
      texture = new EmissiveTexture(new RGB(r, g, b), st);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "emissiveColor", emissiveColor },
      { "emissiveStrength", new Double(emissiveStrength) }
    };
    writer.writeFields(fields);
  }
  
}
