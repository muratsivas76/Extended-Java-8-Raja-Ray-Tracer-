// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

/**
 * VoidTexture simulates a black hole or cosmic void effect: a dark,
 * non-reflective center that absorbs light, surrounded by a subtle
 * radial gradient fading to near-black. Designed for dramatic,
 * low-emission environments like space scenes or surreal void portals.
 *
 * The texture is radially symmetric around the origin in the XY plane
 * and works best on flat or spherical surfaces facing the viewer.
 */
public class VoidTexture implements Texture, java.io.Serializable, Writable {
  private RGB outerColor;
  private double scale;
  private double falloff;
  
  // Extremely low Phong values to simulate light absorption
  private static final double AMBIENT_FACTOR = 0.05;
  private static final double SPECULAR_FACTOR = 0.01;
  private static final RGB SPECULAR_COLOR = new RGB(0.1, 0.1, 0.15);
  private static final int REFLECTIVITY = 0;
  private static final int SHININESS = 5;
  
  /**
   * Default constructor: deep cosmic void with near-total blackness.
   */
  public VoidTexture() {
    this(
      new RGB(0.08, 0.06, 0.12),  // very dark blue-black
      4.0,
      3.0
    );
  }
  
  /**
   * Constructs a VoidTexture with custom parameters.
   *
   * @param outerColor Color at the outer edge of the void (still very dark).
   * @param scale      Controls the apparent size of the void (higher = larger).
   * @param falloff    Sharpness of the darkening toward the center (higher = steeper).
   */
  public VoidTexture(RGB outerColor, double scale, double falloff) {
    this.outerColor = outerColor;
    this.scale = Math.max(0.1, scale);
    this.falloff = Math.max(0.5, falloff);
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Radial distance in XY plane (void centered at origin)
    double r = Math.sqrt(p.x * p.x + p.y * p.y) * scale;
    
    // Exponential falloff toward center → near-zero at r=0
    double intensity = Math.pow(Math.min(1.0, r), falloff);
    RGB color = new RGB(
      outerColor.getR() * intensity,
      outerColor.getG() * intensity,
      outerColor.getB() * intensity
    );
    
    // Clamp to prevent negative or NaN values
    color = new RGB(
      Math.max(0.0, color.getR()),
      Math.max(0.0, color.getG()),
      Math.max(0.0, color.getB())
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
    map.put("outerColor", null);
    map.put("scale", 4.0);
    map.put("falloff", 3.0);
    
    reader.readFields(map);
    
    return new VoidTexture(
      (RGB) map.get("outerColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("falloff")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //VoidTexture(RGB outerColor, double scale, double falloff)
    String VOID_STR="Constructor is: VoidTexture(RGB outerColor, double scale, double falloff);\nExample:\n0.1,0.01,0.06,  4.0,  3.0\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return VOID_STR;
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
    
    if (str.equals("-1")) return new VoidTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //VoidTexture(RGB outerColor, double scale, double falloff)
    //0.1,0.01,0.06,  4.0,  3.0
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      double sc = Double.parseDouble(split[3]);
      double fl = Double.parseDouble(split[4]);
      
      texture = new VoidTexture(new RGB(r, g, b), sc, fl);
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
      {"outerColor", outerColor},
      {"scale", scale},
      {"falloff", falloff}
    };
    writer.writeFields(fields);
  }
  
  // --- Getters ---
  public RGB getOuterColor() { return outerColor; }
  public double getScale() { return scale; }
  public double getFalloff() { return falloff; }
  
}

/**
VoidTexture voidMat = new VoidTexture();

VoidTexture voidMat = new VoidTexture(
new RGB(0.05, 0.03, 0.1),   // colder, deeper void
6.0,                        // larger apparent size
5.0                         // sharper drop to black
);
 */
