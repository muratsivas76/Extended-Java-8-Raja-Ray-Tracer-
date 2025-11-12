// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class WoodTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor;
  private RGB ringColor;
  private double scale;
  private double grainStrength;
  
  // Phong parameters for matte painted or natural wood
  private static final double AMBIENT_FACTOR = 0.3;
  private static final double SPECULAR_FACTOR = 0.2;
  private static final RGB SPECULAR_COLOR = new RGB(0.9, 0.9, 0.9);
  private static final int REFLECTIVITY = 1;
  private static final int SHININESS = 30;
  
  public WoodTexture() {
    this(
      new RGB(0.65, 0.45, 0.3),   // warm brown base
      new RGB(0.4, 0.25, 0.15),   // darker rings
      8.0,
      0.3
    );
  }
  
  public WoodTexture(RGB baseColor, RGB ringColor, double scale, double grainStrength) {
    this.baseColor = baseColor;
    this.ringColor = ringColor;
    this.scale = Math.max(0.1, scale);
    this.grainStrength = Math.min(1.0, Math.max(0.0, grainStrength));
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Use XY plane for standard wood flooring or paneling
    double u = p.x * scale;
    double v = p.y * scale;
    
    // Simulate annual rings using radial distance from origin (or pseudo-origin)
    // To avoid singularity at (0,0), add small offset
    double dx = p.x + 0.001;
    double dy = p.y + 0.001;
    double radial = Math.sqrt(dx * dx + dy * dy) * scale * 0.5;
    
    // Annual rings: sine wave along radial direction
    double rings = Math.sin(radial * 2.0 * Math.PI) * 0.5 + 0.5;
    
    // Wood fiber: subtle vertical grain using sine noise
    double fiber = Math.sin(u * 3.0) * 0.1 + Math.sin(v * 1.5) * 0.05;
    double grain = 1.0 + fiber * grainStrength;
    
    // Blend base and ring colors based on ring intensity
    RGB color;
    if (rings < 0.4) {
      color = ringColor;
      } else if (rings < 0.6) {
      double blend = (rings - 0.4) / 0.2;
      color = RGB.interpolate(ringColor, baseColor, blend);
      } else {
      color = baseColor;
    }
    
    // Apply grain modulation (slight brightness variation)
    color = new RGB(
      Math.min(1.0, Math.max(0.0, color.getR() * grain)),
      Math.min(1.0, Math.max(0.0, color.getG() * grain)),
      Math.min(1.0, Math.max(0.0, color.getB() * grain))
    );
    
    return new LocalTexture(
      color,
      color.multiply(AMBIENT_FACTOR),
      SPECULAR_COLOR.multiply(SPECULAR_FACTOR),
      SHININESS,
      REFLECTIVITY
    );
  }
  
  // --- IO Support ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", null);
    map.put("ringColor", null);
    map.put("scale", 8.0);
    map.put("grainStrength", 0.3);
    
    reader.readFields(map);
    
    return new WoodTexture(
      (RGB) map.get("baseColor"),
      (RGB) map.get("ringColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("grainStrength")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //WoodTexture(RGB baseColor, RGB ringColor, double scale, double grainStrength)
    //0.7,0.5,0.3,  0.4,0.3,0.2,  6.0, 0.25
    String WOOD_STR="Constructor is: WoodTexture(RGB baseColor, RGB ringColor, double scale, double grainStrength);\nExample:\n0.7,0.5,0.3,  0.4,0.3,0.2,  6.0, 0.25\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return WOOD_STR;
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
    
    if (str.equals("-1")) return new WoodTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //WoodTexture(RGB baseColor, RGB ringColor, double scale, double grainStrength)
    //0.7,0.5,0.3,  0.4,0.3,0.2,  6.0, 0.25
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      double gs = Double.parseDouble(split[7]);
      
      texture = new WoodTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, gs);
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
      {"ringColor", ringColor},
      {"scale", scale},
      {"grainStrength", grainStrength}
    };
    writer.writeFields(fields);
  }
  
  // --- Getters ---
  public RGB getBaseColor() { return baseColor; }
  public RGB getRingColor() { return ringColor; }
  public double getScale() { return scale; }
  public double getGrainStrength() { return grainStrength; }
  
}

/**
// Natural oak
Texture oak = new WoodTexture(
new RGB(0.7, 0.5, 0.35),
new RGB(0.45, 0.3, 0.2),
10.0,
0.4
);

// Dark walnut
Texture walnut = new WoodTexture(
new RGB(0.35, 0.25, 0.2),
new RGB(0.15, 0.1, 0.08),
6.0,
0.25
);
 */
