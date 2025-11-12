// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class HokusaiTexture implements Texture, java.io.Serializable, Writable {
  public static final int PLANE_MODE = 0;
  public static final int SPHERE_MODE = 1;
  
  private RGB waterColor;
  private RGB foamColor;
  private double scale;
  private int mode; // 0: plane, 1: sphere
  private double radius; // sadece küre modunda kullanılır
  
  // Phong sabitleri (su/sıvı için optimize)
  private static final double AMBIENT_FACTOR = 0.2;
  private static final double SPECULAR_FACTOR = 0.9;
  private static final RGB SPECULAR_COLOR = new RGB(1.0, 1.0, 1.0);
  private static final int REFLECTIVITY = 6;
  private static final int SHININESS = 300; // parlak su
  
  // Varsayılan kurucu
  public HokusaiTexture() {
    this(new RGB(0.1, 0.2, 0.5),   // Koyu mavi su
      new RGB(1.0, 1.0, 1.0),   // Beyaz köpük
      4.0,                      // scale
      PLANE_MODE,
    1.0);                     // radius (küre için)
  }
  
  public HokusaiTexture(RGB waterColor, RGB foamColor, double scale, int mode, double radius) {
    this.waterColor = waterColor;
    this.foamColor = foamColor;
    this.scale = Math.max(0.1, scale);
    this.mode = (mode == SPHERE_MODE) ? SPHERE_MODE : PLANE_MODE;
    this.radius = Math.max(0.1, radius);
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double u, v;
    
    if (mode == SPHERE_MODE) {
      // Küre üzerinde enlem-boylam (latitude-longitude)
      double len = p.length();
      if (len < 1e-6) {
        u = 0; v = 0;
        } else {
        // Normalize et (küre varsayımı)
        double x = p.x / len;
        double y = p.y / len;
        double z = p.z / len;
        
        // Enlem (latitude): -π/2 → π/2
        double lat = Math.asin(Math.max(-1.0, Math.min(1.0, y)));
        // Boylam (longitude): -π → π
        double lon = Math.atan2(z, x);
        
        u = (lon / (2 * Math.PI)) + 0.5; // [0,1)
        v = (lat / Math.PI) + 0.5;       // [0,1)
      }
      } else {
      // Düzlem modu: XY düzlemi
      u = p.x * scale;
      v = p.y * scale;
    }
    
    // Tile içi koordinatlar
    u = u - Math.floor(u);
    v = v - Math.floor(v);
    
    // Hokusai dalga deseni
    RGB color = createHokusaiWave(u, v);
    
    // Su malzemesi özellikleri
    RGB ambient = color.multiply(AMBIENT_FACTOR);
    RGB diffuse = color;
    RGB specular = SPECULAR_COLOR.multiply(SPECULAR_FACTOR);
    
    return new LocalTexture(
      diffuse,
      ambient,
      specular,
      SHININESS,
      REFLECTIVITY
    );
  }
  
  private RGB createHokusaiWave(double u, double v) {
    // Dalga yönü: soldan sağa
    double waveX = u * 8.0;      // 8 tekrar
    double waveY = v * 2.0;      // dikeyde yavaş değişim
    
    // Temel dalga formu (Hokusai tarzı kavis)
    double baseWave = Math.sin(waveX - waveY * 0.5) * 0.5 + 0.5;
    
    // Köpük efekti: dalga tepeciklerinde
    double foam = 0.0;
    if (baseWave > 0.85) {
      // Dinamik köpük: dalga yüksekliği arttıkça köpük yoğunlaşır
      foam = (baseWave - 0.85) * 6.0;
      foam = Math.min(1.0, foam);
    }
    
    // Derinlik solması (alt kısım daha koyu)
    double depthFade = Math.exp(-v * 3.0); // alta doğru solma
    
    // Renk karışımı
    RGB water = waterColor.multiply(depthFade);
    RGB blended = RGB.interpolate(water, foamColor, foam);
    
    return blended;
  }
  
  // --- IO Desteği ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("waterColor", null);
    map.put("foamColor", null);
    map.put("scale", 4.0);
    map.put("mode", PLANE_MODE);
    map.put("radius", 1.0);
    
    reader.readFields(map);
    
    return new HokusaiTexture(
      (RGB) map.get("waterColor"),
      (RGB) map.get("foamColor"),
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("mode")).intValue(),
      ((Number) map.get("radius")).doubleValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //HokusaiTexture(RGB waterColor, RGB foamColor, double scale, int mode, double radius)		//0.4,0.2,0.1,  0.7,0.2,0.2,  4.0,  0|1|2(xy/xz/yz)
    //0.1,0.3,0.6,  0.9,0.9,1.0,  10.0,  0|1(plane/sphere),  2.5
    String HOKUSAI_STR="Constructor is: HokusaiTexture(RGB waterColor, RGB foamColor, double scale, int mode, double radius);\nExample:\n0.1,0.3,0.6,  0.9,0.9,1.0,  10.0,  0|1(plane/sphere),  2.5\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return HOKUSAI_STR;
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
    
    if (str.equals("-1")) return new HokusaiTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //HokusaiTexture(RGB waterColor, RGB foamColor, double scale, int mode, double radius)		//0.4,0.2,0.1,  0.7,0.2,0.2,  4.0,  0|1|2(xy/xz/yz)
    //0.1,0.3,0.6,  0.9,0.9,1.0,  10.0,  0|1(plane/sphere),  2.5
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double sc = Double.parseDouble(split[6]);
      
      int pl = Integer.parseInt(split[7]);
      
      double rd = Double.parseDouble(split[8]);
      
      texture = new HokusaiTexture(
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
      sc, pl, rd);
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
      {"waterColor", waterColor},
      {"foamColor", foamColor},
      {"scale", scale},
      {"mode", mode},
      {"radius", radius}
    };
    writer.writeFields(fields);
  }
  
  // --- Getter'lar ---
  public RGB getWaterColor() { return waterColor; }
  public RGB getFoamColor() { return foamColor; }
  public double getScale() { return scale; }
  public int getMode() { return mode; }
  public double getRadius() { return radius; }
  
}

/**
Texture oceanFloor = new HokusaiTexture(
new RGB(0.05, 0.15, 0.4),   // Koyu mavi
new RGB(1.0, 1.0, 1.0),     // Köpük
6.0,                        // Daha büyük dalgalar
HokusaiTexture.PLANE_MODE,
1.0
);

Texture waterPlanet = new HokusaiTexture(
new RGB(0.1, 0.3, 0.6),
new RGB(0.95, 0.95, 1.0),
10.0,                       // Kürede daha sık tekrar
HokusaiTexture.SPHERE_MODE,
2.5                         // Küre yarıçapı (RAJA'da nesne ölçeğiyle uyumlu)
);
 */
