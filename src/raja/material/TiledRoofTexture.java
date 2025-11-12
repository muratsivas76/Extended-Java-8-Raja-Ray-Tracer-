// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

/**
 * TiledRoofTexture simulates traditional curved clay roof tiles arranged in
 * staggered horizontal rows. The pattern uses smooth color transitions between
 * tile and gap regions, with subtle ambient/specular properties to mimic sun-baked clay.
 *
 * This texture is designed for vertical surfaces (e.g., facades) or roofs aligned
 * with the XZ plane. It assumes the Y-axis points upward.
 */
public class TiledRoofTexture implements Texture, java.io.Serializable, Writable {
  private RGB tileColor;
  private RGB gapColor;
  private double scale;
  private double tileHeightRatio;
  
  // Phong lighting parameters for matte, non-reflective clay
  private static final double AMBIENT_FACTOR = 0.4;
  private static final double SPECULAR_FACTOR = 0.1;
  private static final RGB SPECULAR_COLOR = new RGB(0.8, 0.75, 0.7);
  private static final int REFLECTIVITY = 5;
  private static final int SHININESS = 10;
  
  /**
   * Constructs a default TiledRoofTexture with warm terracotta tiles.
   */
  public TiledRoofTexture() {
    this(
      new RGB(0.7, 0.3, 0.25),   // classic red-orange clay
      new RGB(0.5, 0.25, 0.2),   // slightly darker gaps
      12.0,
      0.6
    );
  }
  
  /**
   * Constructs a TiledRoofTexture with custom parameters.
   *
   * @param tileColor        Main color of the roof tiles.
   * @param gapColor         Color of the gaps between tiles (mortar or shadow).
   * @param scale            Controls tile density (higher = more tiles per unit).
   * @param tileHeightRatio  Ratio of tile height to total row height (0.0–1.0).
   */
  public TiledRoofTexture(RGB tileColor, RGB gapColor, double scale, double tileHeightRatio) {
    this.tileColor = tileColor;
    this.gapColor = gapColor;
    this.scale = Math.max(0.1, scale);
    this.tileHeightRatio = Math.min(1.0, Math.max(0.1, tileHeightRatio));
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Work in XZ plane: tiles run horizontally along X, stacked along Z (Y is up in world)
    double x = p.x * scale;
    double z = p.z * scale;
    
    // Row index (each row is one tile height + gap)
    double row = Math.floor(z);
    double localZ = z - row;
    
    // Alternate offset every other row for staggered layout
    double xOffset = (row % 2 == 0) ? 0.0 : 0.5;
    double localX = (x + xOffset) % 1.0;
    
    // Define tile region: occupies [0, tileHeightRatio) in Z and full width in X
    double blend = 1.0;
    if (localZ < tileHeightRatio) {
      // Inside a tile row
      blend = 1.0;
      } else {
      // In the gap between rows
      blend = 0.0;
    }
    
    // Smooth transition at tile/gap boundary (optional soft edge)
    double edge = 0.02;
    if (localZ > tileHeightRatio - edge && localZ < tileHeightRatio + edge) {
      double t = (localZ - (tileHeightRatio - edge)) / (2 * edge);
      blend = 1.0 - Math.min(1.0, Math.max(0.0, t));
    }
    
    RGB color = RGB.interpolate(gapColor, tileColor, blend);
    
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
    map.put("tileColor", null);
    map.put("gapColor", null);
    map.put("scale", 12.0);
    map.put("tileHeightRatio", 0.6);
    
    reader.readFields(map);
    
    return new TiledRoofTexture(
      (RGB) map.get("tileColor"),
      (RGB) map.get("gapColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("tileHeightRatio")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //TiledRoofTexture(RGB tileColor, RGB gapColor, double scale, double tileHeightRatio)
    //0.7,0.3,0.2,  0.4,0.2,0.1,  10.0, 0.6
    String TILEDROOF_STR="Constructor is: TiledRoofTexture(RGB tileColor, RGB gapColor, double scale, double tileHeightRatio);\nExample:\n0.7,0.3,0.2,  0.4,0.2,0.1,  10.0, 0.6\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return TILEDROOF_STR;
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
    
    if (str.equals("-1")) return new TiledRoofTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //TiledRoofTexture(RGB tileColor, RGB gapColor, double scale, double tileHeightRatio)
    //0.7,0.3,0.2,  0.4,0.2,0.1,  10.0, 0.6
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      double th = Double.parseDouble(split[7]);
      
      texture = new TiledRoofTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, th);
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
      {"tileColor", tileColor},
      {"gapColor", gapColor},
      {"scale", scale},
      {"tileHeightRatio", tileHeightRatio}
    };
    writer.writeFields(fields);
  }
  
  // --- Getters ---
  public RGB getTileColor() { return tileColor; }
  public RGB getGapColor() { return gapColor; }
  public double getScale() { return scale; }
  public double getTileHeightRatio() { return tileHeightRatio; }
  
}

/**
TiledRoofTexture trt = new TiledRoofTexture();

TiledRoofTexture trt = new TiledRoofTexture(
new RGB(0.75, 0.35, 0.2),   // tile color (warm terracotta)
new RGB(0.45, 0.25, 0.15),  // gap color (darker mortar)
10.0,                       // scale (tiles per unit)
0.65                        // tile height ratio
);
 */
