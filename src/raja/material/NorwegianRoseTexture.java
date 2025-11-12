// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class NorwegianRoseTexture implements Texture, java.io.Serializable, Writable {
  private RGB woodColor;
  private RGB roseColor;
  private int plane; // 0: XY, 1: XZ, 2: YZ
  private double tileSize; // Tile büyüklüğü (varsayılan: 2.0)
  
  // Phong/boyalı ahşap sabitleri
  private static final double AMBIENT_FACTOR = 0.4;
  private static final double SPECULAR_FACTOR = 0.2;
  private static final RGB SPECULAR_COLOR = new RGB(220.0/255.0, 220.0/255.0, 220.0/255.0);
  private static final int REFLECTIVITY = 1;
  private static final int SHININESS = 30;
  
  private static final double TWO_PI = Math.PI * 2;
  private static final double THREE_PI = Math.PI * 3;
  
  // Varsayılan kurucu
  public NorwegianRoseTexture() {
    this(
      new RGB(101.0/255.0, 67.0/255.0, 33.0/255.0), // Kahverengi ahşap
      new RGB(200.0/255.0, 50.0/255.0, 50.0/255.0), // Kırmızı rosemål
      0,     // varsayılan: XY düzlemi
      2.0    // varsayılan tile boyutu
    );
  }
  
  public NorwegianRoseTexture(RGB woodColor, RGB roseColor) {
    this(woodColor, roseColor, 0, 2.0);
  }
  
  public NorwegianRoseTexture(RGB woodColor, RGB roseColor, int plane) {
    this(woodColor, roseColor, plane, 2.0);
  }
  
  public NorwegianRoseTexture(RGB woodColor, RGB roseColor, double tileSize, int plane) {
    this.woodColor = woodColor;
    this.roseColor = roseColor;
    this.tileSize = tileSize;
    this.plane = plane;
  }
  
  public NorwegianRoseTexture(RGB woodColor, RGB roseColor, int plane, double tileSize) {
    this.woodColor = woodColor;
    this.roseColor = roseColor;
    this.plane = plane;
    this.tileSize = tileSize;
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
      default: // XY - zemin/ahşap yüzey
        u = p.x;
      v = p.y;
      break;
    }
    
    // Tile büyüklüğünü ayarla (daha büyük = desen daha net)
    u = u / tileSize;
    v = v / tileSize;
    
    // Tile içi koordinatlar [0,1)
    double tileU = u - Math.floor(u);
    double tileV = v - Math.floor(v);
    
    // Ahşap doku + rosemål deseni
    RGB woodBase = addWoodGrain(woodColor, u, v);
    RGB surfaceColor = createRosemalPattern(woodBase, tileU, tileV);
    
    // Boyalı ahşap malzeme özellikleri
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
  
  private RGB addWoodGrain(RGB baseWood, double u, double v) {
    // Ahşap damar efekti
    double grain = Math.sin(u * 3.0) * 0.15 + Math.sin(v * 1.5) * 0.1;
    double factor = 0.85 + grain;
    
    return new RGB(
      Math.min(1.0, Math.max(0.0, baseWood.getR() * factor)),
      Math.min(1.0, Math.max(0.0, baseWood.getG() * factor)),
      Math.min(1.0, Math.max(0.0, baseWood.getB() * factor))
    );
  }
  
  private RGB createRosemalPattern(RGB woodBase, double tileU, double tileV) {
    // 1. Kenarlık
    if (tileU < 0.1 || tileU > 0.9 || tileV < 0.1 || tileV > 0.9) {
      return roseColor;
    }
    
    // 2. Merkez çiçek
    double dx = tileU - 0.5;
    double dy = tileV - 0.5;
    double distSq = dx * dx + dy * dy;
    
    if (distSq < 0.04) { // yarıçap ~0.2
      if (distSq > 0.01) { // iç boşluk
        double angle = Math.atan2(dy, dx);
        double petal = Math.sin(angle * 6) * 0.5 + 0.5;
        if (petal > 0.7) {
          return roseColor;
        }
      }
      // Merkez dolu değil → ahşap kalır
    }
    
    // 3. Dalgalı ve geometrik desenler
    double diffUV = Math.abs(tileU - tileV);
    double sumUV = Math.abs(tileU + tileV - 1.0);
    
    if (diffUV < 0.03 ||
      sumUV < 0.03 ||
      Math.sin(tileU * THREE_PI) > 0.8 ||
      Math.sin(tileV * THREE_PI) > 0.8 ||
      Math.cos(tileU * TWO_PI) > 0.7 ||
      Math.cos(tileV * TWO_PI) > 0.7) {
      return roseColor;
    }
    
    // 4. Köşe süslemeleri
    if ((Math.abs(tileU - 0.25) < 0.04 && Math.abs(tileV - 0.25) < 0.04) ||
      (Math.abs(tileU - 0.75) < 0.04 && Math.abs(tileV - 0.75) < 0.04) ||
      (Math.abs(tileU - 0.25) < 0.04 && Math.abs(tileV - 0.75) < 0.04) ||
      (Math.abs(tileU - 0.75) < 0.04 && Math.abs(tileV - 0.25) < 0.04)) {
      return roseColor;
    }
    
    return woodBase;
  }
  
  // --- IO Desteği ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("woodColor", null);
    map.put("roseColor", null);
    map.put("plane", 0);
    map.put("tileSize", 2.0);
    
    reader.readFields(map);
    
    return new NorwegianRoseTexture(
      (RGB) map.get("woodColor"),
      (RGB) map.get("roseColor"),
      ((Number) map.get("plane")).intValue(),
      ((Number) map.get("tileSize")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    // NorwegianRoseTexture(RGB woodColor, RGB roseColor, double tileSize, int plane)
    //0.4,0.2,0.1,  0.7,0.2,0.2,  0.2,  0|1|2(xyPlane/xz/yzSphere)
    String NORWEGIANROSE_STR="Constructor is: NorwegianRoseTexture(RGB woodColor, RGB roseColor, double tileSize, int plane);\nExample:\n0.4,0.2,0.1,  0.7,0.2,0.2,  4.0,  0|1|2(xy/xz/yz)\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return NORWEGIANROSE_STR;
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
    
    if (str.equals("-1")) return new NorwegianRoseTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    // NorwegianRoseTexture(RGB woodColor, RGB roseColor, double tileSize, int plane)
    //0.4,0.2,0.1,  0.7,0.2,0.2,  4.0,  0|1|2(xy/xz/yz)
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      
      int pl = Integer.parseInt(split[7]);
      
      texture = new NorwegianRoseTexture(
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
      {"woodColor", woodColor},
      {"roseColor", roseColor},
      {"plane", plane},
      {"tileSize", tileSize}
    };
    writer.writeFields(fields);
  }
  
  // --- Getter'lar ---
  public RGB getWoodColor() { return woodColor; }
  public RGB getRoseColor() { return roseColor; }
  public int getPlane() { return new Integer(plane); }
  public double getTileSize() { return new Double(tileSize); }
  
}

/**
// Varsayılan tile boyutu (2.0)
Texture norwegianFloor = new NorwegianRoseTexture(
new RGB(0.4, 0.26, 0.13),   // Ahşap kahverengisi
new RGB(0.78, 0.2, 0.2),    // Kırmızı rosemål
0                           // XY düzlemi
);

// Özel tile boyutu
Texture largeTile = new NorwegianRoseTexture(
new RGB(0.4, 0.26, 0.13),
new RGB(0.78, 0.2, 0.2),
0,                          // XY düzlemi
4.0                         // Büyük desenler
);

// Duvar paneli
Texture wallPanel = new NorwegianRoseTexture(
new RGB(0.5, 0.35, 0.2),
new RGB(0.2, 0.6, 0.8),     // Mavi rosemål alternatifi
1,                          // XZ düzlemi
1.5                         // Orta boy desenler
);
 */
