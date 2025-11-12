// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

/**
 * CrystalCaveTexture simulates glowing amethyst-like crystal formations
 * growing inward from the surface. It uses procedural noise and radial
 * gradients to create deep purple crystalline structures with luminous cores.
 *
 * Best applied to interior cave walls or geodesic shapes where the surface
 * normal points outward (e.g., a sphere or inverted mesh).
 */
public class CrystalCaveTexture implements Texture, java.io.Serializable, Writable {
  private RGB crystalColor;
  private RGB glowColor;
  private double scale;
  private double density;
  
  // Phong parameters for semi-glossy, emissive-like crystals
  private static final double AMBIENT_FACTOR = 0.6;
  private static final double SPECULAR_FACTOR = 0.5;
  private static final RGB SPECULAR_COLOR = new RGB(0.95, 0.85, 1.0);
  private static final int REFLECTIVITY = 2;
  private static final int SHININESS = 80;
  
  /**
   * Default constructor: rich purple crystals with bright violet glow.
   */
  public CrystalCaveTexture() {
    this(
      new RGB(0.4, 0.1, 0.6),   // deep amethyst base
      new RGB(0.9, 0.5, 1.0),   // luminous glow core
      10.0,
      0.7
    );
  }
  
  /**
   * Constructs a CrystalCaveTexture with custom parameters.
   *
   * @param crystalColor Base color of the crystal facets.
   * @param glowColor    Bright inner glow (appears near crystal centers).
   * @param scale        Controls crystal size and repetition frequency.
   * @param density      How densely crystals cover the surface (0.0–1.0).
   */
  public CrystalCaveTexture(RGB crystalColor, RGB glowColor, double scale, double density) {
    this.crystalColor = crystalColor;
    this.glowColor = glowColor;
    this.scale = Math.max(0.5, scale);
    this.density = Math.min(1.0, Math.max(0.0, density));
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Use all three axes for 3D crystal distribution
    double x = p.x * scale;
    double y = p.y * scale;
    double z = p.z * scale;
    
    // Simple procedural "crystal centers" using modulo grid
    double gridX = x - Math.floor(x);
    double gridY = y - Math.floor(y);
    double gridZ = z - Math.floor(z);
    
    // Distance to nearest grid point (simulates crystal origin)
    double dx = Math.min(gridX, 1.0 - gridX);
    double dy = Math.min(gridY, 1.0 - gridY);
    double dz = Math.min(gridZ, 1.0 - gridZ);
    double distToCenter = Math.sqrt(dx * dx + dy * dy + dz * dz);
    
    // Invert: crystals grow from surface inward → closer to center = brighter
    double crystalIntensity = 1.0 - Math.min(1.0, distToCenter * 2.5);
    
    // Apply density mask: randomly suppress some crystals
    double noiseSeed = (Math.sin(x * 12.9898 + y * 78.233 + z * 45.164) * 43758.5453) % 1.0;
    if (noiseSeed > density) {
      crystalIntensity = 0.0;
    }
    
    // Blend between crystal base and glowing core
    RGB color = RGB.interpolate(crystalColor, glowColor, crystalIntensity);
    
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
    map.put("crystalColor", null);
    map.put("glowColor", null);
    map.put("scale", 10.0);
    map.put("density", 0.7);
    
    reader.readFields(map);
    
    return new CrystalCaveTexture(
      (RGB) map.get("crystalColor"),
      (RGB) map.get("glowColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("density")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String CRYSTALCAVE_TEXTURE="Constructor is: CrystalCaveTexture(RGB crystalColor, RGB glowColor, double scale, double density);\nExample:\n0.2,0.0,0.5,  0.8,0.3,1.0,  12.0,  0.5\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return CRYSTALCAVE_TEXTURE;
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
    
    if (str.equals("-1")) return new CrystalCaveTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    //CrystalCaveTexture(RGB crystalColor, RGB glowColor,
    //double scale, double density)
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      double dn = Double.parseDouble(split[7]);
      
      texture = new CrystalCaveTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, dn);
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
      {"crystalColor", crystalColor},
      {"glowColor", glowColor},
      {"scale", scale},
      {"density", density}
    };
    writer.writeFields(fields);
  }
  
  // --- Getters ---
  public RGB getCrystalColor() { return crystalColor; }
  public RGB getGlowColor() { return glowColor; }
  public double getScale() { return scale; }
  public double getDensity() { return density; }
  
}

/**
CrystalCaveTexture cave = new CrystalCaveTexture();

CrystalCaveTexture cave = new CrystalCaveTexture(
new RGB(0.2, 0.0, 0.5),   // darker crystal
new RGB(0.8, 0.3, 1.0),   // intense glow
12.0,                     // finer crystals
0.5                       // sparser coverage
);
 */
