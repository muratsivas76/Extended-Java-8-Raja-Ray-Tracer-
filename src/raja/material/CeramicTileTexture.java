// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class CeramicTileTexture implements Texture, java.io.Serializable, Writable {
  private RGB tileColor1;  // Örneğin: Beyaz
  private RGB tileColor2;  // Örneğin: Siyah
  private RGB groutColor;
  private double tileSize;
  private double groutWidth;
  private int plane; // 0:XY, 1:XZ, 2:YZ
  
  // Damier (iki renkli) versiyon
  public CeramicTileTexture(RGB tileColor1, RGB tileColor2, RGB groutColor,
    double tileSize, double groutWidth, int plane) {
    this.tileColor1 = tileColor1;
    this.tileColor2 = tileColor2;
    this.groutColor = groutColor;
    this.tileSize = tileSize;
    this.groutWidth = groutWidth;
    this.plane = plane;
  }
  
  // Zemin için (XY düzlemi) - iki renkli
  public CeramicTileTexture(RGB tileColor1, RGB tileColor2, RGB groutColor,
    double tileSize, double groutWidth) {
    this(tileColor1, tileColor2, groutColor, tileSize, groutWidth, 0);
  }
  
  // Eski tek-renkli kullanım için uyumluluk (opsiyonel)
  public CeramicTileTexture(RGB tileColor, RGB groutColor, double tileSize, double groutWidth, int plane) {
    this(tileColor, tileColor, groutColor, tileSize, groutWidth, plane); // Aynı renk iki kez → tek renk
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double u, v;
    
    switch (plane) {
      case 0: // XY düzlemi - ZEMİN
        u = p.x;
      v = p.y;
      break;
      case 1: // XZ düzlemi - DUVAR
        u = p.x;
      v = p.z;
      break;
      case 2: // YZ düzlemi - YAN DUVAR
        u = p.y;
      v = p.z;
      break;
      default:
        u = p.x;
      v = p.y;
    }
    
    //  DOĞRU KARE İNDEKSİ HESAPLAMASI
    int kareU = (int) Math.floor(u / tileSize);
    int kareV = (int) Math.floor(v / tileSize);
    
    // Kare içindeki yer (0 ile tileSize arası)
    double localU = u - kareU * tileSize;
    double localV = v - kareV * tileSize;
    
    // Derz kontrolü — kenarlarda kesintisiz çizgi için
    boolean isGrout = (localU <= groutWidth) ||
    (localU >= tileSize - groutWidth) ||
    (localV <= groutWidth) ||
    (localV >= tileSize - groutWidth);
    
    RGB baseColor;
    if (isGrout) {
      baseColor = groutColor;
      } else {
      // Damier desen
      baseColor = ((kareU + kareV) % 2 == 0) ? tileColor1 : tileColor2;
    }
    
    return new LocalTexture(
      baseColor,
      baseColor.multiply(0.4),
      new RGB(0.0, 0.0, 0.0),
      isGrout ? 100 : 500,
      1
    );
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("tileColor1", null);
    map.put("tileColor2", null);
    map.put("groutColor", null);
    map.put("tileSize", 0.5);
    map.put("groutWidth", 0.03);
    map.put("plane", 0);
    
    reader.readFields(map);
    
    return new CeramicTileTexture(
      (RGB) map.get("tileColor1"),
      (RGB) map.get("tileColor2"),
      (RGB) map.get("groutColor"),
      ((Number) map.get("tileSize")).doubleValue(),
      ((Number) map.get("groutWidth")).doubleValue(),
      ((Number) map.get("plane")).intValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String CERAMICTILE_STR = "Constructor is: CeramicTileTexture (RGB tileColor1, RGB tileColor2, RGB groutColor, double tileSize, double groutWidth, int plane);\nExample:\n0.95,0.95,0.98,  0.1,0.1,0.1,  0.3,0.3,0.3,  0.4,  0.01,  0|1|2(xy/xz/yz)\nEnter your values after three diyez symbol\n###\n";
    return CERAMICTILE_STR;
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
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    // CeramicTileTexture(RGB tileColor1, RGB tileColor2, RGB groutColor,
    // double tileSize, double groutWidth, int plane)
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double c3r = Double.parseDouble(split[6]);
      double c3g = Double.parseDouble(split[7]);
      double c3b = Double.parseDouble(split[8]);
      
      double ts = Double.parseDouble(split[9]);
      double gw = Double.parseDouble(split[10]);
      
      int pl = Integer.parseInt(split[11]);
      
      texture = new CeramicTileTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
      ts, gw, pl);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "tileColor1", tileColor1 },
      { "tileColor2", tileColor2 },
      { "groutColor", groutColor },
      { "tileSize", tileSize },
      { "groutWidth", groutWidth },
      { "plane", plane }
    };
    writer.writeFields(fields);
  }
  
  // Getter'lar
  public RGB getTileColor1() { return tileColor1; }
  public RGB getTileColor2() { return tileColor2; }
  public RGB getGroutColor() { return groutColor; }
  public double getTileSize() { return tileSize; }
  public double getGroutWidth() { return groutWidth; }
  public int getPlane() { return plane; }
  
}

/**
// Beyaz-Siyah satranç zemin (klasik damier)
Texture floor = new CeramicTileTexture(
new RGB(0.95, 0.95, 0.98),  // Açık beyaz
new RGB(0.1, 0.1, 0.12),    // Siyah
new RGB(0.3, 0.3, 0.35),    // Gri derz
0.4,                        // 40 cm kare
0.01,                       // 1 cm derz
0                           // XY düzlemi
);

// Kırmızı-Beyaz mutfak duvarı
Texture wall = new CeramicTileTexture(
new RGB(0.9, 0.3, 0.3),     // Kırmızı
new RGB(0.95, 0.95, 0.98),  // Beyaz
new RGB(0.15, 0.15, 0.15),  // Siyah derz
0.3,                        // 30 cm
0.008,                      // 8 mm ince derz
1                           // XZ düzlemi (duvar)
);

// Büyük tile, belirgin derz
new CeramicTileTexture(
new RGB(1,1,1),
new RGB(0,0,0),
new RGB(0.2,0.2,0.2),
1.0,    // 1m x 1m kareler
0.05,   // 5 cm derz → çok belirgin
1       // XZ duvar
);
 */

