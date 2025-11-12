package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class AnisotropicMetalTexture implements Texture, java.io.Serializable, Writable {
  private RGB metalColor;
  private double anisotropy;
  private double roughnessX;
  private double roughnessY;
  
  // Default constructor
  public AnisotropicMetalTexture() {
    this(new RGB(0.7, 0.7, 0.75),    // Silver-gray metal
      0.8,                        // Anisotropy strength
      0.1,                        // Roughness in X direction
    0.4);                       // Roughness in Y direction
  }
  
  // Custom constructor
  public AnisotropicMetalTexture(RGB metalColor, double anisotropy, double roughnessX, double roughnessY) {
    this.metalColor = metalColor;
    this.anisotropy = anisotropy;
    this.roughnessX = roughnessX;
    this.roughnessY = roughnessY;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Anisotropic effect - brush strokes pattern
    double brushPattern = Math.sin(p.x * 50) * anisotropy;
    double anisotropicEffect = (brushPattern + 1) * 0.5;
    
    RGB surfaceColor = metalColor.multiply(0.8 + anisotropicEffect * 0.4);
    
    return new LocalTexture(
      surfaceColor,
      surfaceColor.multiply(0.3),  // Medium ambient
      new RGB(0.8, 0.8, 0.85),     // High metallic specular
      80,                          // High shininess
      1
    );
  }
  
  // IO methods
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("metalColor", new RGB(0.7, 0.7, 0.75));
    map.put("anisotropy", 0.8);
    map.put("roughnessX", 0.1);
    map.put("roughnessY", 0.4);
    
    reader.readFields(map);
    
    return new AnisotropicMetalTexture(
      (RGB) map.get("metalColor"),
      ((Number) map.get("anisotropy")).doubleValue(),
      ((Number) map.get("roughnessX")).doubleValue(),
      ((Number) map.get("roughnessY")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String ANISOTROPIC_STR = "Constructor: AnisotropicMetalTexture(RGB metalColor, double anisotropy, double roughnessX, double roughnessY)\n" +
    "Examples:\n" +
    "-1                             # Default anisotropic metal\n" +
    "0.7,0.7,0.75,  0.8,  0.1,   0.4      # Brushed silver\n" +
    "0.9,0.8,0.5,   0.6,  0.05,  0.2      # Brushed gold\n" +
    "Enter values after ###\n###\n";
    return ANISOTROPIC_STR;
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
    
    if (str.equals("-1")) return new AnisotropicMetalTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 6) return texture;
    
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      double anisotropy = Double.parseDouble(split[3]);
      double roughX = Double.parseDouble(split[4]);
      double roughY = Double.parseDouble(split[5]);
      
      texture = new AnisotropicMetalTexture(
        new RGB(r, g, b),
        anisotropy,
        roughX,
        roughY
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
      {"metalColor", metalColor},
      {"anisotropy", anisotropy},
      {"roughnessX", roughnessX},
      {"roughnessY", roughnessY}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getMetalColor() { return metalColor; }
  public double getAnisotropy() { return anisotropy; }
  public double getRoughnessX() { return roughnessX; }
  public double getRoughnessY() { return roughnessY; }
}
