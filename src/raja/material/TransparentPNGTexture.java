// Murat Inan
package raja.material;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import javax.imageio.ImageIO;

import raja.*;
import raja.shape.*;
import raja.io.*;

public class TransparentPNGTexture implements Texture, java.io.Serializable, Writable
{
  private Matrix4 inverseTransform = null;
  
  private boolean useZForV = false;
  
  private String imagePath="textures/turkeyFlag.png";
  
  private BufferedImage texture;
  private BufferedImage rotatedTexture; // Döndürülmüş texture cache'i
  private RGB kd, kr, kt;
  private final double KTV; //0.0-1.0 (and 1.0 is full transparen)
  private int ns, nt;
  private double billboardWidth, billboardHeight;
  private double uOffset = 0.0;
  private double vOffset = 0.0;
  private double rotX = 0.0;
  private double rotY = 0.0;
  private double rotZ = 0.0;
  
  // Ana constructor - tüm parametrelerle
  public TransparentPNGTexture(BufferedImage texture,
    RGB kd, RGB kr, RGB kt,
    int ns, int nt,
    double KTV,
    double uOffset, double vOffset,
  double rotX, double rotY, double rotZ)
  {
    this.texture = texture;
    this.kd = kd;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.KTV = KTV;
    this.uOffset = uOffset;
    this.vOffset = vOffset;
    this.rotX = rotX;
    this.rotY = rotY;
    this.rotZ = rotZ;
    
    // Texture'ı rotate et
    applyRotation();
  }
  
  // Basit constructor
  public TransparentPNGTexture(BufferedImage texture,
    RGB kd, RGB kr, RGB kt,
  int ns, int nt)
  {
    this(texture, kd, kr, kt, ns, nt, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
  }
  
  public void setImagePath(String npath) {
    this.imagePath = npath;
  }
  
  public String getImagePath() {
    return this.imagePath;
  }
  
  // Texture rotation uygula
  private void applyRotation() {
    if (texture == null || (rotX == 0.0 && rotY == 0.0 && rotZ == 0.0)) {
      rotatedTexture = texture;
      return;
    }
    
    // Toplam rotation açısı (derece)
    double totalRotation = rotZ; // Z-ekseni ana rotation
    
    // Rotation matrisi oluştur
    AffineTransform transform = new AffineTransform();
    
    // Merkez etrafında döndür
    transform.rotate(Math.toRadians(totalRotation),
      texture.getWidth() / 2.0,
    texture.getHeight() / 2.0);
    
    // Yeni image oluştur - ARGB type
    rotatedTexture = new BufferedImage(texture.getWidth(), texture.getHeight(),
    BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = rotatedTexture.createGraphics();
    
    // Önce tüm background'u transparent yap
    g2d.setComposite(java.awt.AlphaComposite.Clear);
    g2d.fillRect(0, 0, texture.getWidth(), texture.getHeight());
    
    // Sonra resmi çiz
    g2d.setComposite(java.awt.AlphaComposite.SrcOver);
    g2d.setTransform(transform);
    g2d.drawImage(texture, 0, 0, null);
    g2d.dispose();
  }
  
  public void setBillboardDimensions(double width, double height) {
    this.billboardWidth = width;
    this.billboardHeight = height;
  }
  
  public void setInverseTransform(Matrix4 inverseTransform) {
    this.inverseTransform = inverseTransform;
  }
  
  public void setUseZForV(boolean useZ) {
    this.useZForV = useZ;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    BufferedImage currentTexture = (rotatedTexture != null) ? rotatedTexture : texture;
    
    if (currentTexture == null || billboardWidth <= 0 || billboardHeight <= 0) {
      return new LocalTexture(kd, kr, kt, ns, nt);
    }
    
    Point3D localP = p;
    if (inverseTransform != null) {
      localP = inverseTransform.transformPoint(p);
    }
    
    double u = 0.0;
    double v = 0.0;
    
    if (useZForV) {
      u = (localP.y / billboardWidth) + 0.5;     // Y --> sol/sağ
      v = 0.5 - (localP.z / billboardHeight);    // Z --> yukarı --> PNG üstüne uyarla
      } else {
      u = (localP.y / billboardWidth) + 0.5;
      v = 0.5 - (localP.x / billboardHeight);    // billboard için de aynı mantık
    }
    
    // Offset uygula
    u = (u + uOffset) % 1.0;
    v = (v + vOffset) % 1.0;
    
    // Pozitif koordinatları garanti et
    if (u < 0) u += 1.0;
    if (v < 0) v += 1.0;
    
    u = 1.0 - u;
    
    u = Math.max(0.0, Math.min(1.0, u));
    v = Math.max(0.0, Math.min(1.0, v));
    
    int px = (int) (u * (currentTexture.getWidth() - 1));
    int py = (int) (v * (currentTexture.getHeight() - 1));
    
    px = Math.max(0, Math.min(currentTexture.getWidth() - 1, px));
    py = Math.max(0, Math.min(currentTexture.getHeight() - 1, py));
    
    int argb = currentTexture.getRGB(px, py);
    int alpha = (argb >> 24) & 0xFF;
    
    // ÖBÜR PROJEDEKİ GİBİ: alpha < 5 ise TAMAMEN TRANSPARENT
    if (alpha > 5) {
      int red = (argb >> 16) & 0xFF;
      int green = (argb >> 8) & 0xFF;
      int blue = argb & 0xFF;
      
      RGB textureColor = new RGB(red / 255.0, green / 255.0, blue / 255.0);
      
      // OPAQUE - kt = 0 (transparency yok)
      return new LocalTexture(textureColor,
        new RGB(0.0, 0.0, 0.0),
        new RGB(0.0, 0.0, 0.0), // kt = 0
      5, 1);
      } else {
      // TRANSPARENT - kt = 1.0 (tamamen transparent)
      return new LocalTexture(new RGB(0.0, 0.0, 0.0), // kd = siyah
        new RGB(0.0, 0.0, 0.0), // kr = 0
        new RGB(KTV, KTV, KTV), // kt = 1.0 - TAM TRANSPARENT
      5, 1);
    }
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("kd", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    map.put("KTV", new Double(1.0));
    map.put("uOffset", new Double(0.0));
    map.put("vOffset", new Double(0.0));
    map.put("rotX", new Double(0.0));
    map.put("rotY", new Double(0.0));
    map.put("rotZ", new Double(0.0));
    reader.readFields(map);
    
    BufferedImage texture = null;
    return new TransparentPNGTexture(texture,
      (RGB) map.get("kd"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("KTV")).doubleValue(),
      ((Number) map.get("uOffset")).doubleValue(),
      ((Number) map.get("vOffset")).doubleValue(),
      ((Number) map.get("rotX")).doubleValue(),
      ((Number) map.get("rotY")).doubleValue(),
    ((Number) map.get("rotZ")).doubleValue());
  }
  
  public void setTexture(BufferedImage texture) {
    this.texture = texture;
    applyRotation(); // Yeni texture için rotation uygula
  }
  
  public BufferedImage getTexture() {
    return texture;
  }
  
  // Rotation değerlerini değiştirmek için method
  public void setRotation(double rotX, double rotY, double rotZ) {
    this.rotX = rotX;
    this.rotY = rotY;
    this.rotZ = rotZ;
    applyRotation(); // Rotation'ı yeniden uygula
  }
  
  // Offset değerlerini değiştirmek için method
  public void setOffset(double uOffset, double vOffset) {
    this.uOffset = uOffset;
    this.vOffset = vOffset;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //TransparentPNGTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt, double KTV, double uOffset, double vOffset, double rotX, double rotY, double rotZ)
    //"textures/texture.png",  0.5,0.5,0.5,  0,0,0,  0,0,0,  30,  10,  1.0, 0, 0, 0, 0, 0
    String TRANSPARENTPNG_STR="Constructor is: TransparentPNGTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt, double KTV, double uOffset, double vOffset, double rotX, double rotY, double rotZ);\nExample:\n\"textures/texture.png\",  0,0,0,  0,0,0,  0,0,0,  0,  0,  1.0, 0, 0, 0, 0, 0\nEnter your values after three diyez symbol\n###\n";
    return TRANSPARENTPNG_STR;
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
    
    //if (str.equals("-1")) return new ....();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //TransparentPNGTexture(BufferedImage texture,
    //                        RGB kd, RGB kr, RGB kt,
    //                        int ns, int nt,
    //                        double KTV,
    //                        double uOffset, double vOffset,
    //                        double rotX, double rotY, double rotZ)
    
    //"textures/texture.png",
    //0.5,0.5,0.5,  0,0,0,  0,0,0,
    //30,  10,
    //1.0,
    //0, 0,
    //0, 0, 0
    
    try {
      String tstr = split[0];
      tstr = tstr.trim();
      tstr = tstr.replaceAll("\"", "");
      BufferedImage timg = ImageIO.read(new File(tstr));
      
      double c1r = Double.parseDouble(split[1]);
      double c1g = Double.parseDouble(split[2]);
      double c1b = Double.parseDouble(split[3]);
      
      double c2r = Double.parseDouble(split[4]);
      double c2g = Double.parseDouble(split[5]);
      double c2b = Double.parseDouble(split[6]);
      
      double c3r = Double.parseDouble(split[7]);
      double c3g = Double.parseDouble(split[8]);
      double c3b = Double.parseDouble(split[9]);
      
      int ns = Integer.parseInt(split[10]);
      int nt = Integer.parseInt(split[11]);
      
      double kv = Double.parseDouble(split[12]);
      
      double uo = Double.parseDouble(split[13]);
      double vo = Double.parseDouble(split[14]);
      
      double rx = Double.parseDouble(split[15]);
      double ry = Double.parseDouble(split[16]);
      double rz = Double.parseDouble(split[17]);
      
      texture = new TransparentPNGTexture(
        timg,
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
        ns, nt,
        kv,
        uo, vo,
      rx, ry, rz);
      ((TransparentPNGTexture)(texture)).setImagePath(tstr);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
      } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "kd", kd },
      { "kr", kr },
      { "kt", kt },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) },
      { "KTV", new Double(KTV) },
      { "uOffset", new Double(uOffset) },
      { "vOffset", new Double(vOffset) },
      { "rotX", new Double(rotX) },
      { "rotY", new Double(rotY) },
      { "rotZ", new Double(rotZ) }
    };
    writer.writeFields(fields);
  }
  
  // Getter methods
  public double getUOffset() { return uOffset; }
  public double getVOffset() { return vOffset; }
  public double getRotX() { return rotX; }
  public double getRotY() { return rotY; }
  public double getRotZ() { return rotZ; }
  
}
