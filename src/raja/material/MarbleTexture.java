// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class MarbleTexture implements Texture, java.io.Serializable, Writable {
  private RGB veinColor;
  private RGB baseColor;
  private double scale;
  private int turbulenceLayers;
  private double turbulenceScale;
  
  // Phong parameters for polished stone
  private static final double AMBIENT_FACTOR = 0.3;
  private static final double SPECULAR_FACTOR = 0.7;
  private static final RGB SPECULAR_COLOR = new RGB(1.0, 1.0, 1.0);
  private static final int REFLECTIVITY = 4;
  private static final int SHININESS = 200;
  
  public MarbleTexture() {
    this(
      new RGB(0.9, 0.9, 0.9),   // base
      new RGB(0.3, 0.3, 0.4),   // vein
      4.0,
      4,
      2.0
    );
  }
  
  public MarbleTexture(RGB baseColor, RGB veinColor, double scale, int turbulenceLayers, double turbulenceScale) {
    this.baseColor = baseColor;
    this.veinColor = veinColor;
    this.scale = Math.max(0.1, scale);
    this.turbulenceLayers = Math.max(1, turbulenceLayers);
    this.turbulenceScale = Math.max(0.1, turbulenceScale);
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double u = p.x * scale;
    double v = p.y * scale;
    double w = p.z * scale;
    
    // Turbulence (simple noise approximation)
    double t = turbulence(u, v, w);
    
    // Sinus wave + turbulence -> veines
    double marble = Math.sin(u + t) * 0.5 + 0.5;
    
    // clear veines
    RGB color;
    if (marble < 0.3) {
      color = veinColor;
      } else if (marble < 0.7) {
      double blend = (marble - 0.3) / 0.4;
      color = RGB.interpolate(veinColor, baseColor, blend);
      } else {
      color = baseColor;
    }
    
    return new LocalTexture(
      color,
      color.multiply(AMBIENT_FACTOR),
      SPECULAR_COLOR.multiply(SPECULAR_FACTOR),
      SHININESS,
      REFLECTIVITY
    );
  }
  
  private double turbulence(double x, double y, double z) {
    double value = 0.0;
    double scale = 1.0;
    for (int i = 0; i < turbulenceLayers; i++) {
      value += noise(x * scale, y * scale, z * scale) / scale;
      scale *= turbulenceScale;
    }
    return value;
  }
  
  //Simple noise function
  private double noise(double x, double y, double z) {
    int X = (int) Math.floor(x) & 255;
    int Y = (int) Math.floor(y) & 255;
    int Z = (int) Math.floor(z) & 255;
    
    // Deterministic "random" value
    long hash = (X * 73856093L) ^ (Y * 19349663L) ^ (Z * 83492791L);
    return (double)(hash % 256) / 256.0;
  }
  
  // --- IO Support ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", null);
    map.put("veinColor", null);
    map.put("scale", 4.0);
    map.put("turbulenceLayers", 4);
    map.put("turbulenceScale", 2.0);
    
    reader.readFields(map);
    
    return new MarbleTexture(
      (RGB) map.get("baseColor"),
      (RGB) map.get("veinColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("turbulenceLayers")).intValue(),
      ((Number) map.get("turbulenceScale")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //MarbleTexture(RGB baseColor, RGB veinColor, double scale, int turbulenceLayers, double turbulenceScale)
    String MARBLE_STR="Constructor is: MarbleTexture(RGB baseColor, RGB veinColor, double scale, int turbulenceLayers, double turbulenceScale);\nExample:\n0.85,0.9,0.85,  0.2,0.4,0.3,  6.0,  5,  2.5\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return MARBLE_STR;
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
    
    if (str.equals("-1")) return new MarbleTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //MarbleTexture(RGB baseColor, RGB veinColor, double scale,
    //int turbulenceLayers, double turbulenceScale);
    //Example:\n0.85,0.9,0.85,  0.2,0.4,0.3,  6.0,  5,  2.5
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      
      int tl = Integer.parseInt(split[7]);
      
      double ts = Double.parseDouble(split[8]);
      
      texture = new MarbleTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, tl, ts);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"baseColor", baseColor},
      {"veinColor", veinColor},
      {"scale", scale},
      {"turbulenceLayers", turbulenceLayers},
      {"turbulenceScale", turbulenceScale}
    };
    writer.writeFields(fields);
  }
  
  // --- Getters ---
  public RGB getBaseColor() { return baseColor; }
  public RGB getVeinColor() { return veinColor; }
  public double getScale() { return scale; }
  public int getTurbulenceLayers() { return turbulenceLayers; }
  public double getTurbulenceScale() { return turbulenceScale; }
  
}

/**
Texture whiteMarble = new MarbleTexture();

Texture greenMarble = new MarbleTexture(
new RGB(0.85, 0.9, 0.85),
new RGB(0.2, 0.4, 0.3),
6.0, 5, 2.5
);
 */
