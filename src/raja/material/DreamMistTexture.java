package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class DreamMistTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor;
  private RGB fogColor;
  private double transparency;
  private double glowIntensity;
  
  // Default constructor
  public DreamMistTexture() {
    this(new RGB(0.9, 0.95, 1.0),    // Light blue-white
      new RGB(0.8, 0.9, 1.0),     // Fog color
      0.7,                        // 70% transparency
    0.3);                       // Glow intensity
  }
  
  // Custom constructor
  public DreamMistTexture(RGB baseColor, RGB fogColor, double transparency, double glowIntensity) {
    this.baseColor = baseColor;
    this.fogColor = fogColor;
    this.transparency = transparency;
    this.glowIntensity = glowIntensity;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Dreamy mist effect with depth-based fog
    double depthEffect = Math.sin(p.x * 5) * Math.cos(p.y * 5) * 0.3 + 0.7;
    RGB surfaceColor = interpolateColor(baseColor, fogColor, depthEffect);
    
    // Add glow effect
    RGB glow = new RGB(glowIntensity, glowIntensity, glowIntensity);
    RGB finalColor = surfaceColor.add(glow.multiply(0.2));
    
    return new LocalTexture(
      finalColor,
      finalColor.multiply(0.6),    // High ambient for dreamy look
      new RGB(0.3, 0.3, 0.4),      // Soft specular
      15,                          // Medium shininess
      1
    );
  }
  
  private RGB interpolateColor(RGB color1, RGB color2, double factor) {
    factor = Math.max(0, Math.min(1, factor));
    return new RGB(
      color1.getR() * (1 - factor) + color2.getR() * factor,
      color1.getG() * (1 - factor) + color2.getG() * factor,
      color1.getB() * (1 - factor) + color2.getB() * factor
    );
  }
  
  // IO methods
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", new RGB(0.9, 0.95, 1.0));
    map.put("fogColor", new RGB(0.8, 0.9, 1.0));
    map.put("transparency", 0.7);
    map.put("glowIntensity", 0.3);
    
    reader.readFields(map);
    
    return new DreamMistTexture(
      (RGB) map.get("baseColor"),
      (RGB) map.get("fogColor"),
      ((Number) map.get("transparency")).doubleValue(),
      ((Number) map.get("glowIntensity")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String DREAM_MIST_STR = "Constructor: DreamMistTexture(RGB baseColor, RGB fogColor, double transparency, double glowIntensity)\n" +
    "Examples:\n" +
    "-1                             # Default dream mist\n" +
    "0.9,0.95,1.0,  0.8,0.9,1.0,   0.7,  0.3  # Blue dream mist\n" +
    "1.0,0.95,0.9,  0.9,0.85,0.8,  0.8,  0.4  # Warm golden mist\n" +
    "Enter values after ###\n###\n";
    return DREAM_MIST_STR;
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
    
    if (str.equals("-1")) return new DreamMistTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 8) return texture;
    
    try {
      double r1 = Double.parseDouble(split[0]);
      double g1 = Double.parseDouble(split[1]);
      double b1 = Double.parseDouble(split[2]);
      double r2 = Double.parseDouble(split[3]);
      double g2 = Double.parseDouble(split[4]);
      double b2 = Double.parseDouble(split[5]);
      double transparency = Double.parseDouble(split[6]);
      double glow = Double.parseDouble(split[7]);
      
      texture = new DreamMistTexture(
        new RGB(r1, g1, b1),
        new RGB(r2, g2, b2),
        transparency,
        glow
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
      {"fogColor", fogColor},
      {"transparency", transparency},
      {"glowIntensity", glowIntensity}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getBaseColor() { return baseColor; }
  public RGB getFogColor() { return fogColor; }
  public double getTransparency() { return transparency; }
  public double getGlowIntensity() { return glowIntensity; }
}
