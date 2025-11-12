package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class SandTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseSandColor;
  private RGB darkSandColor;
  private double grainSize;
  private double roughness;
  
  // Default constructor
  public SandTexture() {
    this(new RGB(0.76, 0.70, 0.50),   // Light sand color
      new RGB(0.65, 0.58, 0.38),   // Dark sand color
      0.05,                        // Grain size
    0.8);                        // Roughness
  }
  
  // Custom constructor
  public SandTexture(RGB baseSandColor, RGB darkSandColor, double grainSize, double roughness) {
    this.baseSandColor = baseSandColor;
    this.darkSandColor = darkSandColor;
    this.grainSize = grainSize;
    this.roughness = roughness;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Simple noise-like sand pattern
    double noise = improvedNoise(p.x * 10, p.y * 10, p.z * 10);
    double grainPattern = Math.sin(p.x * 100 * grainSize) * Math.cos(p.y * 100 * grainSize);
    
    RGB surfaceColor = interpolateColor(baseSandColor, darkSandColor, (noise + grainPattern) * 0.5);
    
    return new LocalTexture(
      surfaceColor,
      surfaceColor.multiply(0.5),  // High ambient - soft shadows
      new RGB(0.1, 0.1, 0.1),      // Low specular
      5,                           // Low shininess
      1
    );
  }
  
  private double improvedNoise(double x, double y, double z) {
    // Simple perlin noise-like function
    return (Math.sin(x * 12.9898 + y * 78.233 + z * 45.164) * 43758.5453) % 1.0;
  }
  
  private RGB interpolateColor(RGB color1, RGB color2, double factor) {
    factor = Math.max(0, Math.min(1, (factor + 1) * 0.5));
    return new RGB(
      color1.getR() * (1 - factor) + color2.getR() * factor,
      color1.getG() * (1 - factor) + color2.getG() * factor,
      color1.getB() * (1 - factor) + color2.getB() * factor
    );
  }
  
  // IO methods
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseSandColor", new RGB(0.76, 0.70, 0.50));
    map.put("darkSandColor", new RGB(0.65, 0.58, 0.38));
    map.put("grainSize", 0.05);
    map.put("roughness", 0.8);
    
    reader.readFields(map);
    
    return new SandTexture(
      (RGB) map.get("baseSandColor"),
      (RGB) map.get("darkSandColor"),
      ((Number) map.get("grainSize")).doubleValue(),
      ((Number) map.get("roughness")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String SAND_STR = "Constructor: SandTexture(RGB baseSandColor, RGB darkSandColor, double grainSize, double roughness)\n" +
    "Examples:\n" +
    "-1                             # Default sand texture\n" +
    "0.76,0.70,0.50,  0.65,0.58,0.38,  0.05,  0.8  # Golden sand\n" +
    "0.9,0.85,0.7,    0.8,0.75,0.6,    0.03,  0.9  # White beach sand\n" +
    "Enter values after ###\n###\n";
    return SAND_STR;
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
    
    if (str.equals("-1")) return new SandTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 8) return texture;
    
    try {
      double r1 = Double.parseDouble(split[0]);
      double g1 = Double.parseDouble(split[1]);
      double b1 = Double.parseDouble(split[2]);
      double r2 = Double.parseDouble(split[3]);
      double g2 = Double.parseDouble(split[4]);
      double b2 = Double.parseDouble(split[5]);
      double grainSize = Double.parseDouble(split[6]);
      double roughness = Double.parseDouble(split[7]);
      
      texture = new SandTexture(
        new RGB(r1, g1, b1),
        new RGB(r2, g2, b2),
        grainSize,
        roughness
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
      {"baseSandColor", baseSandColor},
      {"darkSandColor", darkSandColor},
      {"grainSize", grainSize},
      {"roughness", roughness}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getBaseSandColor() { return baseSandColor; }
  public RGB getDarkSandColor() { return darkSandColor; }
  public double getGrainSize() { return grainSize; }
  public double getRoughness() { return roughness; }
}
