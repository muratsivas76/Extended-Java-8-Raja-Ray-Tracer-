package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class ObsidianHeartTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor;
  private RGB glowColor;
  private double shininess;
  private double innerGlow;
  
  // Default constructor
  public ObsidianHeartTexture() {
    this(new RGB(0.02, 0.0, 0.0),    // Deep black with red tint
      new RGB(0.8, 0.0, 0.0),     // Red glow
      100.0,                      // High shininess
    0.5);                       // Inner glow intensity
  }
  
  // Custom constructor
  public ObsidianHeartTexture(RGB baseColor, RGB glowColor, double shininess, double innerGlow) {
    this.baseColor = baseColor;
    this.glowColor = glowColor;
    this.shininess = shininess;
    this.innerGlow = innerGlow;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Inner glow effect based on surface position
    double glowEffect = (Math.sin(p.x * 20) * Math.cos(p.y * 20) * Math.sin(p.z * 20) + 1) * 0.5;
    glowEffect *= innerGlow;
    
    RGB finalColor = baseColor.add(glowColor.multiply(glowEffect));
    
    return new LocalTexture(
      finalColor,
      finalColor.multiply(0.1),    // Very low ambient - deep shadows
      new RGB(0.9, 0.9, 0.9),      // High specular for glass-like appearance
      (int) shininess,              // Very high shininess
      1
    );
  }
  
  // IO methods
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", new RGB(0.02, 0.0, 0.0));
    map.put("glowColor", new RGB(0.8, 0.0, 0.0));
    map.put("shininess", 100.0);
    map.put("innerGlow", 0.5);
    
    reader.readFields(map);
    
    return new ObsidianHeartTexture(
      (RGB) map.get("baseColor"),
      (RGB) map.get("glowColor"),
      ((Number) map.get("shininess")).doubleValue(),
      ((Number) map.get("innerGlow")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String OBSIDIAN_STR = "Constructor: ObsidianHeartTexture(RGB baseColor, RGB glowColor, double shininess, double innerGlow)\n" +
    "Examples:\n" +
    "-1                             # Default obsidian heart\n" +
    "0.02,0.0,0.0,    0.8,0.0,0.0,  100.0,  0.5  # Red glowing obsidian\n" +
    "0.01,0.01,0.02,  0.0,0.6,0.8,  150.0,  0.3  # Blue glowing obsidian\n" +
    "Enter values after ###\n###\n";
    return OBSIDIAN_STR;
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
    
    if (str.equals("-1")) return new ObsidianHeartTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 8) return texture;
    
    try {
      double r1 = Double.parseDouble(split[0]);
      double g1 = Double.parseDouble(split[1]);
      double b1 = Double.parseDouble(split[2]);
      double r2 = Double.parseDouble(split[3]);
      double g2 = Double.parseDouble(split[4]);
      double b2 = Double.parseDouble(split[5]);
      double shininess = Double.parseDouble(split[6]);
      double innerGlow = Double.parseDouble(split[7]);
      
      texture = new ObsidianHeartTexture(
        new RGB(r1, g1, b1),
        new RGB(r2, g2, b2),
        shininess,
        innerGlow
      );
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"baseColor", baseColor},
      {"glowColor", glowColor},
      {"shininess", shininess},
      {"innerGlow", innerGlow}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getBaseColor() { return baseColor; }
  public RGB getGlowColor() { return glowColor; }
  public double getShininess() { return shininess; }
  public double getInnerGlow() { return innerGlow; }
}
