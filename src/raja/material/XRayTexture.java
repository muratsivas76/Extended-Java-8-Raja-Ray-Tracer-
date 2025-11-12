// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

public class XRayTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor;
  private double transparency;
  private int reflectivity;
  private int plane; // 0: XY, 1: XZ, 2: YZ (opsiyonel, ama tutarlılık için)
  
  public XRayTexture() {
    // Varsayılan: açık mavi, yarı şeffaf
    this(new RGB(0.15, 0.6, 1.0), 0.92, 5);
  }
  
  public XRayTexture(RGB baseColor, double transparency, int reflectivity) {
    this(baseColor, transparency, reflectivity, 0);
  }
  
  public XRayTexture(RGB baseColor, double transparency, int reflectivity, int plane) {
    this.baseColor = baseColor;
    this.transparency = Math.min(1.0, Math.max(0.0, transparency));
    this.reflectivity = reflectivity;
    this.plane = plane;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // X-ray efekti: nesnenin merkezine olan mesafeye göre solma
    double depth = p.length(); // orijine olan Öklid mesafesi
    double intensity = Math.exp(-depth * 0.5);
    
    // Sadece mavi ve yeşil kanalları solmaya bırak (tipik X-ray estetiği)
    RGB color = new RGB(
      baseColor.getR(),                    // Kırmızı sabit (genelde düşük)
      baseColor.getG() * intensity,        // Yeşil soluyor
      baseColor.getB() * intensity         // Mavi soluyor
    );
    
    // Ambient = diffuse = color (basit, fakat etkili)
    // Specular çok düşük — X-ray parlak değil
    RGB ambient = color.multiply(0.3);
    RGB diffuse = color;
    RGB specular = new RGB(0.1, 0.1, 0.1); // çok hafif
    
    int shininess = 10; // düşük parlaklık
    
    return new LocalTexture(
      diffuse,
      ambient,
      specular,
      shininess,
      reflectivity
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String XRAY_STR="Constructor is: XRayTexture(RGB baseColor, double transparency, int reflectivity, int plane);\nExample:\n1.0,0.3,0.1,  0.85,  3,  0|1|2(xy/xz/yz)\n-1 returns empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return XRAY_STR;
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
    
    if (str.equals("-1")) return new XRayTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //XRayTexture(RGB baseColor, double transparency, int reflectivity, int plane)
    //1.0,0.3,0.1,  0.85,  3,  0
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      double tr = Double.parseDouble(split[3]);
      
      int rf = Integer.parseInt(split[4]);
      int pl = Integer.parseInt(split[5]);
      
      texture = new XRayTexture(new RGB(r, g, b), tr, rf, pl);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  // --- IO Desteği ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("baseColor", null);
    map.put("transparency", 0.92);
    map.put("reflectivity", 0.05);
    map.put("plane", 0);
    
    reader.readFields(map);
    
    return new XRayTexture(
      (RGB) map.get("baseColor"),
      ((Number) map.get("transparency")).doubleValue(),
      ((Number) map.get("reflectivity")).intValue(),
      ((Number) map.get("plane")).intValue()
    );
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"baseColor", baseColor},
      {"transparency", transparency},
      {"reflectivity", reflectivity},
      {"plane", plane}
    };
    writer.writeFields(fields);
  }
  
  // --- Getter'lar ---
  public RGB getBaseColor() { return baseColor; }
  public double getTransparency() { return transparency; }
  public double getReflectivity() { return reflectivity; }
  public int getPlane() { return plane; }
  
}

/**
// Standart X-ray görünümü
Texture xray = new XRayTexture();

// Özel renkli X-ray (örneğin: kızılötesi tarzı)
Texture thermalXray = new XRayTexture(
new RGB(1.0, 0.3, 0.1), // Turuncu-kırmızı
0.85,                   // Şeffaflık
3,                      // Çok düşük yansıma
0                       // XY düzlemi
);

// Duvar içi boru taraması gibi
Texture pipeScan = new XRayTexture(
new RGB(0.0, 0.8, 1.0),
0.9,
4,
1 // XZ düzlemi
);
 */
