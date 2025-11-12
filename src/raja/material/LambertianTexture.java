// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;
import java.util.HashMap;

public class LambertianTexture implements Texture, java.io.Serializable, Writable {
  private RGB albedo;
  
  public LambertianTexture() {
    this(new RGB(0.7, 0.7, 0.7));
  }
  
  public LambertianTexture(RGB albedo) {
    this.albedo = albedo;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Tamamen diffuse, tamamen mat
    return new LocalTexture(
      albedo,
      albedo.multiply(0.1),  // minimal ambient
      new RGB(0, 0, 0),      // NO specular
      1,                     // minimum shininess
      0                      // NO reflection
    );
  }
  
  // --- IO ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("albedo", null);
    reader.readFields(map);
    return new LambertianTexture((RGB) map.get("albedo"));
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String LAMBERTIAN_STR = "Constructor is: LambertianTexture(RGB albedo);\nExample:\n0.0, 0.7, 0.0\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return LAMBERTIAN_STR;
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
    
    if (str.equals("-1")) return new LambertianTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    //0.85,0.85,0.0
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      texture = new LambertianTexture(new RGB(r, g, b));
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    writer.writeFields(new Object[][] { {"albedo", albedo} });
  }
  
  public RGB getAlbedo() { return albedo; }
  
}
