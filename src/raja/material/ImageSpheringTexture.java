// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class ImageSpheringTexture implements Texture, java.io.Serializable, Writable {
  private BufferedImage texture;
  private String imagePath;
  private RGB kd, kr, kt;
  private int ns, nt;
  private double reflectivity;
  private double uOffset = 0.0;
  private double vOffset = 0.0;
  private double uScale = 1.0;
  private double vScale = 1.0;
  
  // EKLENEN SATIRLAR
  private transient Point3D sphereWorldCenter;
  private transient Matrix4 inverseTransform;
  
  public ImageSpheringTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt,
    int ns, int nt, double reflectivity,
    double uOffset, double vOffset, double uScale, double vScale) {
    this.texture = texture;
    this.kd = kd;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.reflectivity = reflectivity;
    this.uOffset = uOffset;
    this.vOffset = vOffset;
    this.uScale = uScale;
    this.vScale = vScale;
  }
  
  public ImageSpheringTexture(BufferedImage texture) {
    this(texture, new RGB(0.5, 0.5, 0.5), new RGB(0.2, 0.2, 0.2),
    new RGB(0.0, 0.0, 0.0), 50, 10, 0.1, 0.0, 0.0, 1.0, 1.0);
  }
  
  // EKLENEN METOD
  public void setSphereInfo(Point3D worldCenter, Matrix4 inverseTransform) {
    this.sphereWorldCenter = worldCenter;
    this.inverseTransform = inverseTransform;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    if (texture == null) {
      return new LocalTexture(kd, kr, kt, ns, nt);
    }
    
    // EKLENEN KOD: World → Local dönüşüm
    Point3D localPoint;
    if (inverseTransform != null) {
      localPoint = inverseTransform.transformPoint(p);
      } else {
      localPoint = p;
    }
    
    // Normalize
    double len = Math.sqrt(localPoint.x * localPoint.x + localPoint.y * localPoint.y + localPoint.z * localPoint.z);
    double nx = localPoint.x / len;
    double ny = localPoint.y / len;
    double nz = localPoint.z / len;
    
    // DÜZ PROJEKSİYON - Y ve Z koordinatları (90 derece düzeltme)
    double u = 0.5 - (ny * 0.5);  // Y yukarı-aşağı → sağ-sol
    double v = 0.5 - (nz * 0.5);  // Z sağ-sol → yukarı-aşağı
    
    // Offset ve scale
    u = (u + uOffset) * uScale;
    v = (v + vOffset) * vScale;
    
    // Sınır kontrolü
    u = Math.max(0.0, Math.min(1.0, u));
    v = Math.max(0.0, Math.min(1.0, v));
    
    // Texture pixel
    int x = (int) (u * (texture.getWidth() - 1));
    int y = (int) (v * (texture.getHeight() - 1));
    
    x = Math.max(0, Math.min(texture.getWidth() - 1, x));
    y = Math.max(0, Math.min(texture.getHeight() - 1, y));
    
    int argb = texture.getRGB(x, y);
    int alpha = (argb >> 24) & 0xFF;
    
    // DEĞİŞEN KOD: KTV transparency kontrolü
    if (alpha < 5) {
      // KTV değerine göre transparency ayarla
      // KTV = 1.0 → tam transparent, KTV = 0.1 → hafif transparent
      RGB transparentKt = new RGB(kt.getR(), kt.getG(), kt.getB()); // Orijinal KTV değerini kullan
      
      return new LocalTexture(
        new RGB(0.0, 0.0, 0.0),     // diffuse siyah
        new RGB(0.0, 0.0, 0.0),     // reflection yok
        transparentKt,               // KTV değeri transparency'yi kontrol eder
        5, 1
      );
    }
    
    int red = (argb >> 16) & 0xFF;
    int green = (argb >> 8) & 0xFF;
    int blue = argb & 0xFF;
    
    RGB textureColor = new RGB(red / 255.0, green / 255.0, blue / 255.0);
    RGB finalKr = kr.multiply(reflectivity);
    
    return new LocalTexture(textureColor, finalKr, kt, ns, nt);
  }
  
  public void setImagePath(String path) {
    this.imagePath = path;
  }
  
  public String getImagePath() {
    return this.imagePath;
  }
  
  public void setTexture(BufferedImage texture) {
    this.texture = texture;
  }
  
  // Offset ve Scale setter'ları
  public void setOffset(double uOffset, double vOffset) {
    this.uOffset = uOffset;
    this.vOffset = vOffset;
  }
  
  public void setScale(double uScale, double vScale) {
    this.uScale = uScale;
    this.vScale = vScale;
  }
  
  // --- IO Support ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("kd", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    map.put("reflectivity", 0.1);
    map.put("uOffset", 0.0);
    map.put("vOffset", 0.0);
    map.put("uScale", 1.0);
    map.put("vScale", 1.0);
    
    reader.readFields(map);
    
    return new ImageSpheringTexture(
      null,
      (RGB) map.get("kd"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("reflectivity")).doubleValue(),
      ((Number) map.get("uOffset")).doubleValue(),
      ((Number) map.get("vOffset")).doubleValue(),
      ((Number) map.get("uScale")).doubleValue(),
      ((Number) map.get("vScale")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: ImageSpheringTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt, double reflectivity, double uOffset, double vOffset, double uScale, double vScale);\n" +
    "Example:\n\"textures/sun.png\", 0.5,0.5,0.5, 0.2,0.2,0.2, 0.0,0.0,0.0, 50, 10, 0.1, 0.0, 0.0, 1.0, 1.0\n" +
    "RGB kt value:\n" +
    "  - 1.0,1.0,1.0 --> full transparent background\n" +
    "  - 0.0,0.0,0.0 --> opaque background\n" +
    "Enter your values after three diyez symbol\n###\n";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    
    String str = info.trim();
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return null;
    
    str = str.substring(diyezIndex + 3);
    str = str.replaceAll("\n", "").replaceAll(" ", "");
    
    String[] split = str.split(",");
    if (split == null) return null;
    
    try {
      String imagePath = split[0].replaceAll("\"", "");
      BufferedImage img = ImageIO.read(new File(imagePath));
      
      double kdR = Double.parseDouble(split[1]);
      double kdG = Double.parseDouble(split[2]);
      double kdB = Double.parseDouble(split[3]);
      
      double krR = Double.parseDouble(split[4]);
      double krG = Double.parseDouble(split[5]);
      double krB = Double.parseDouble(split[6]);
      
      double ktR = Double.parseDouble(split[7]);
      double ktG = Double.parseDouble(split[8]);
      double ktB = Double.parseDouble(split[9]);
      
      int ns = Integer.parseInt(split[10]);
      int nt = Integer.parseInt(split[11]);
      double reflectivity = Double.parseDouble(split[12]);
      double uOffset = Double.parseDouble(split[13]);
      double vOffset = Double.parseDouble(split[14]);
      double uScale = Double.parseDouble(split[15]);
      double vScale = Double.parseDouble(split[16]);
      
      ImageSpheringTexture texture = new ImageSpheringTexture(
        img, new RGB(kdR, kdG, kdB), new RGB(krR, krG, krB),
        new RGB(ktR, ktG, ktB), ns, nt, reflectivity,
        uOffset, vOffset, uScale, vScale
      );
      texture.setImagePath(imagePath);
      
      return texture;
      
      } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    writer.write("// ImageSpheringTexture Path: " + imagePath + "\n");
    
    Object[][] fields = {
      {"kd", kd},
      {"kr", kr},
      {"kt", kt},
      {"ns", Integer.valueOf(ns)},
      {"nt", Integer.valueOf(nt)},
      {"reflectivity", Double.valueOf(reflectivity)},
      {"uOffset", Double.valueOf(uOffset)},
      {"vOffset", Double.valueOf(vOffset)},
      {"uScale", Double.valueOf(uScale)},
      {"vScale", Double.valueOf(vScale)}
    };
    writer.writeFields(fields);
  }
  
}

/**
// Dünya haritası için
Texture earth = new ImageSpheringTexture(
earthImage,
new RGB(0.5, 0.5, 0.5), // diffuse
new RGB(0.3, 0.3, 0.3), // reflection
new RGB(0.0, 0.0, 0.0), // transparency (KTV = 0,0,0 → opak background)
100, 10, 0.2,            // shininess, transparency, reflectivity
0.0, 0.0,
1.0, 1.0
);

// Transparent background için
Texture transparentSphere = new ImageSpheringTexture(
transparentImage,
new RGB(0.5, 0.5, 0.5), // diffuse
new RGB(0.3, 0.3, 0.3), // reflection
new RGB(1.0, 1.0, 1.0), // KTV = 1,1,1 → tam transparent background
100, 10, 0.2,
0.0, 0.0,
1.0, 1.0
);

// Hafif transparent background için
Texture semiTransparentSphere = new ImageSpheringTexture(
semiTransparentImage,
new RGB(0.5, 0.5, 0.5), // diffuse
new RGB(0.3, 0.3, 0.3), // reflection
new RGB(0.1, 0.1, 0.1), // KTV = 0.1,0.1,0.1 → hafif transparent background
100, 10, 0.2,
0.0, 0.0,
1.0, 1.0
);
 */
