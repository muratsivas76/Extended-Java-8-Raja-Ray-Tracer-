// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class TurkishTileTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor;
  private RGB patternColor;
  private double tileSize;
  private int plane; // 0: XY, 1: XZ, 2: YZ
  
  // Phong/seramik malzeme sabitleri
  private static final double AMBIENT_FACTOR = 0.4;
  private static final double SPECULAR_FACTOR = 0.8;
  private static final RGB SPECULAR_COLOR = new RGB(1.0, 1.0, 1.0);
  private static final int REFLECTIVITY = 3;
  private static final int SHININESS = 80;
  
  // Varsayılan kurucu
  public TurkishTileTexture() {
    this(
      new RGB(0.0, 0.4, 0.8),   // Mavi
      new RGB(1.0, 1.0, 1.0),   // Beyaz
      2.0,                      // Varsayılan tile boyutu
      0                         // XY düzlemi
    );
  }
  
  public TurkishTileTexture(RGB baseColor, RGB patternColor, double tileSize) {
    this(baseColor, patternColor, tileSize, 0);
  }
  
  public TurkishTileTexture(RGB baseColor, RGB patternColor, double tileSize, int plane) {
    this.baseColor = baseColor;
    this.patternColor = patternColor;
    this.tileSize = Math.max(0.5, tileSize);
    this.plane = plane;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double u, v;
    
    // Düzlem seçimi
    switch (plane) {
      case 1: // XZ - duvar
        u = p.x;
      v = p.z;
      break;
      case 2: // YZ - yan duvar
        u = p.y;
      v = p.z;
      break;
      default: // XY - zemin
        u = p.x;
      v = p.y;
      break;
    }
    
    // Tile büyüklüğünü ayarla (NorwegianRose ile tutarlı)
    u = u / tileSize;
    v = v / tileSize;
    
    // Tile içi koordinatlar [0,1)
    double tileU = u - Math.floor(u);
    double tileV = v - Math.floor(v);
    
    // Desen rengini hesapla
    RGB surfaceColor = createCleanTurkishPattern(tileU, tileV);
    
    // Seramik malzeme özellikleri
    RGB ambient = surfaceColor.multiply(AMBIENT_FACTOR);
    RGB diffuse = surfaceColor;
    RGB specular = SPECULAR_COLOR.multiply(SPECULAR_FACTOR);
    
    return new LocalTexture(
      diffuse,
      ambient,
      specular,
      SHININESS,
      REFLECTIVITY
    );
  }
  
  private RGB createCleanTurkishPattern(double u, double v) {
    // 1. Kenarlık (10%)
    double border = 0.1;
    if (u < border || u > 1.0 - border || v < border || v > 1.0 - border) {
      return patternColor;
    }
    
    // 2. Merkezde sekiz yapraklı çiçek
    double centerX = 0.5;
    double centerY = 0.5;
    double dx = u - centerX;
    double dy = v - centerY;
    double dist = Math.sqrt(dx * dx + dy * dy);
    
    if (dist < 0.2) {
      if (dist < 0.05) {
        return patternColor; // Katı merkez
      }
      double angle = Math.atan2(dy, dx);
      double petal = Math.abs(Math.sin(4 * angle)); // 8 yaprak
      if (petal > 0.7 && dist > 0.05 && dist < 0.18) {
        return patternColor;
      }
    }
    
    // 3. Keskin geometrik çizgiler
    if (Math.abs(u - v) < 0.02 || Math.abs(u + v - 1.0) < 0.02) {
      return patternColor; // Çaprazlar
    }
    if (Math.abs(u - 0.5) < 0.015 || Math.abs(v - 0.5) < 0.015) {
      return patternColor; // Dikey/yatay eksenler
    }
    
    // 4. Köşe süslemeleri (basitleştirilmiş)
    double[][] corners = {
      {0.2, 0.2}, {0.8, 0.2},
      {0.2, 0.8}, {0.8, 0.8}
    };
    
    for (double[] corner : corners) {
      double cx = corner[0];
      double cy = corner[1];
      double cdx = u - cx;
      double cdy = v - cy;
      double cornerDist = Math.sqrt(cdx * cdx + cdy * cdy);
      
      if (cornerDist < 0.08) {
        double angle = Math.atan2(cdy, cdx);
        double star = Math.abs(Math.sin(4 * angle));
        if (star > 0.8) {
          return patternColor;
        }
      }
    }
    
    return baseColor;
  }
  
  // --- IO Desteği ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", null);
    map.put("patternColor", null);
    map.put("tileSize", 2.0);
    map.put("plane", 0);
    
    reader.readFields(map);
    
    return new TurkishTileTexture(
      (RGB) map.get("baseColor"),
      (RGB) map.get("patternColor"),
      ((Number) map.get("tileSize")).doubleValue(),
      ((Number) map.get("plane")).intValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //TurkishTileTexture(RGB baseColor, RGB patternColor, double tileSize, int plane)
    //0.0,0.4,0.8,  1,1,1,  0.2,  0|1|2(xyPlane/xz/yzSphere)
    String TURKISHTILE_STR="Constructor is: TurkishTileTexture(RGB baseColor, RGB patternColor, double tileSize, int plane);\nExample:\n0.0,0.4,0.8,  1,1,1,  20.0, 0|1|2(xy/xz/yz)\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return TURKISHTILE_STR;
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
    
    if (str.equals("-1")) return new TurkishTileTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //TurkishTileTexture(RGB baseColor, RGB patternColor, double tileSize, int plane)
    //0.0,0.4,0.8,  1,1,1,  20.0, 0|1|2(xy/xz/yz)
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      
      int pl = Integer.parseInt(split[7]);
      
      texture = new TurkishTileTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, pl);
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
      {"patternColor", patternColor},
      {"tileSize", tileSize},
      {"plane", plane}
    };
    writer.writeFields(fields);
  }
  
  // --- Getter'lar ---
  public RGB getBaseColor() { return baseColor; }
  public RGB getPatternColor() { return patternColor; }
  public double getTileSize() { return tileSize; }
  public int getPlane() { return plane; }
  
}

/**
// Klasik mavi-beyaz Türk seramik zemini
Texture floor = new TurkishTileTexture(
new RGB(0.0, 0.4, 0.8),   // Mavi
new RGB(1.0, 1.0, 1.0),   // Beyaz
2.0,                      // Tile boyutu
0                         // XY düzlemi
);

// Büyük desenli zemin
Texture largeTile = new TurkishTileTexture(
new RGB(0.0, 0.4, 0.8),
new RGB(1.0, 1.0, 1.0),
4.0                       // Daha büyük tile
);

// Duvara uygulamak için
Texture wall = new TurkishTileTexture(
new RGB(0.8, 0.2, 0.2),   // Kırmızı
new RGB(1.0, 1.0, 1.0),   // Beyaz
1.5,                      // Küçük desenler
1                         // XZ düzlemi
);
 */
