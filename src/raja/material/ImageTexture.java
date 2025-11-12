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

public class ImageTexture implements Texture, java.io.Serializable, Writable
{
  public String imagePath = "textures/turkeyFlag.png";
  
  private double uOffset = 0.0;
  private double vOffset = 0.0;
  private double tileSizeU = 1.0;
  private double tileSizeV = 1.0;
  private double rotX = 0.0;
  private double rotY = 0.0;
  private double rotZ = 0.0;
  
  private BufferedImage texture;
  private BufferedImage rotatedTexture; // Döndürülmüş texture cache'i
  private RGB kd, kr, kt;
  private int ns, nt;
  private double uScale, vScale;
  
  // Ana constructor - tüm parametrelerle
  public ImageTexture(BufferedImage texture,
    RGB kd, RGB kr, RGB kt,
    int ns, int nt,
    double uScale, double vScale,
    double uOffset, double vOffset,
    double tileSizeU, double tileSizeV,
  double rotX, double rotY, double rotZ)
  {
    this.texture = texture;
    this.kd = kd;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.uScale = uScale;
    this.vScale = vScale;
    this.uOffset = uOffset;
    this.vOffset = vOffset;
    this.tileSizeU = tileSizeU;
    this.tileSizeV = tileSizeV;
    this.rotX = rotX;
    this.rotY = rotY;
    this.rotZ = rotZ;
    
    // Texture'ı rotate et
    applyRotation();
  }
  
  // Orta seviye constructor
  public ImageTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt,
    double uScale, double vScale, double uOffset, double vOffset) {
    this(texture, kd, kr, kt, ns, nt, uScale, vScale, uOffset, vOffset, 1.0, 1.0, 0.0, 0.0, 0.0);
  }
  
  // Basit constructor
  public ImageTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt) {
    this(texture, kd, kr, kt, ns, nt, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0);
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
    
    // Yeni image oluştur
    rotatedTexture = new BufferedImage(texture.getWidth(), texture.getHeight(),
    BufferedImage.TYPE_INT_ARGB); // ARGB type
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
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    if (rotatedTexture == null) {
      return new LocalTexture(kd, kr, kt, ns, nt);
    }
    
    final int imgWidth = rotatedTexture.getWidth();
    final int imgHeight = rotatedTexture.getHeight();
    
    // UV koordinatlarını al
    double u = p.x;
    double v = p.y;
    
    // Tile boyutunu uygula
    u = (u * tileSizeU);
    v = (v * tileSizeV);
    
    // Texture transformations uygula
    u = ((u * uScale) + uOffset);
    v = ((v * vScale) + vOffset);
    
    // Tile repeating - mod işlemi
    u = u % 1.0;
    v = v % 1.0;
    
    // Pozitif koordinatları garanti et
    if (u < 0) u += 1.0;
    if (v < 0) v += 1.0;
    
    // V koordinatını flip et (image coordinate system için)
    v = 1.0 - v;
    
    // BİLİNEAR FILTERING
    double x = u * (imgWidth - 1);
    double y = v * (imgHeight - 1);
    
    int x0 = (int) Math.floor(x);
    int y0 = (int) Math.floor(y);
    int x1 = (int) Math.ceil(x);
    int y1 = (int) Math.ceil(y);
    
    // Wrap coordinates
    x0 = wrapCoordinate(x0, imgWidth);
    y0 = wrapCoordinate(y0, imgHeight);
    x1 = wrapCoordinate(x1, imgWidth);
    y1 = wrapCoordinate(y1, imgHeight);
    
    // Renkleri al ve interpolate et
    int argb00 = rotatedTexture.getRGB(x0, y0);
    int argb10 = rotatedTexture.getRGB(x1, y0);
    int argb01 = rotatedTexture.getRGB(x0, y1);
    int argb11 = rotatedTexture.getRGB(x1, y1);
    
    // Bilinear interpolation
    double tx = x - x0;
    double ty = y - y0;
    
    int red = bilinearInterpolate(
      (argb00 >> 16) & 0xFF, (argb10 >> 16) & 0xFF,
    (argb01 >> 16) & 0xFF, (argb11 >> 16) & 0xFF, tx, ty);
    int green = bilinearInterpolate(
      (argb00 >> 8) & 0xFF, (argb10 >> 8) & 0xFF,
    (argb01 >> 8) & 0xFF, (argb11 >> 8) & 0xFF, tx, ty);
    int blue = bilinearInterpolate(
      argb00 & 0xFF, argb10 & 0xFF,
    argb01 & 0xFF, argb11 & 0xFF, tx, ty);
    int alpha = bilinearInterpolate(
      (argb00 >> 24) & 0xFF, (argb10 >> 24) & 0xFF,
    (argb01 >> 24) & 0xFF, (argb11 >> 24) & 0xFF, tx, ty);
    
    if (alpha < 128) {
      return new LocalTexture(kd, kr, new RGB(0.9, 0.9, 0.9), ns, nt);
      } else {
      RGB textureColor = new RGB(red/255.0, green/255.0, blue/255.0);
      return new LocalTexture(textureColor, kr, kt, ns, nt);
    }
  }
  
  // Diğer projedeki wrapCoordinate methodu
  private int wrapCoordinate(int coord, int max) {
    if (coord < 0) return max - 1 - ((-coord - 1) % max);
    return coord % max;
  }
  
  // Diğer projedeki bilinear interpolation
  private int bilinearInterpolate(int c00, int c10, int c01, int c11, double tx, double ty) {
    double a = c00 * (1 - tx) * (1 - ty);
    double b = c10 * tx * (1 - ty);
    double c = c01 * (1 - tx) * ty;
    double d = c11 * tx * ty;
    return (int) (a + b + c + d);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("kd", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("ns", null);
    map.put("nt", null);
    map.put("uScale", new Double(1.0));
    map.put("vScale", new Double(1.0));
    map.put("uOffset", new Double(0.0));
    map.put("vOffset", new Double(0.0));
    map.put("tileSizeU", new Double(1.0));
    map.put("tileSizeV", new Double(1.0));
    map.put("rotX", new Double(0.0));
    map.put("rotY", new Double(0.0));
    map.put("rotZ", new Double(0.0));
    
    reader.readFields(map);
    
    BufferedImage texture = null;
    return new ImageTexture(texture,
      (RGB) map.get("kd"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("uScale")).doubleValue(),
      ((Number) map.get("vScale")).doubleValue(),
      ((Number) map.get("uOffset")).doubleValue(),
      ((Number) map.get("vOffset")).doubleValue(),
      ((Number) map.get("tileSizeU")).doubleValue(),
      ((Number) map.get("tileSizeV")).doubleValue(),
      ((Number) map.get("rotX")).doubleValue(),
      ((Number) map.get("rotY")).doubleValue(),
    ((Number) map.get("rotZ")).doubleValue());
  }
  
  public void setImagePath(String npath) {
    this.imagePath = npath;
  }
  
  public String getImagePath() {
    return this.imagePath;
  }
  
  public void setTexture(BufferedImage texture) {
    this.texture = texture;
    applyRotation(); // Yeni texture için rotation uygula
  }
  
  // Rotation değerlerini değiştirmek için method
  public void setRotation(double rotX, double rotY, double rotZ) {
    this.rotX = rotX;
    this.rotY = rotY;
    this.rotZ = rotZ;
    applyRotation(); // Rotation'ı yeniden uygula
  }
  
  // Tile boyutunu değiştirmek için method
  public void setTileSize(double tileSizeU, double tileSizeV) {
    this.tileSizeU = tileSizeU;
    this.tileSizeV = tileSizeV;
  }
  
  // Offset değerlerini değiştirmek için method
  public void setOffset(double uOffset, double vOffset) {
    this.uOffset = uOffset;
    this.vOffset = vOffset;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    //ImageTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt, double uScale, double vScale, double uOffset, double vOffset, double tileSizeU, double tileSizeV, double rotX, double rotY, double rotZ)
    //"textures/texture.png", new RGB(0.5, 0.5, 0.5), new RGB(0.1, 0.1, 0.1), new RGB(0.0, 0.0, 0.0), 50, 10, 1.0, 1.0, 0.0, 0.0, 1.5, 1.5, 0.0, 0.0, -270.0
    String IMAGE_STR="Constructor is: ImageTexture(BufferedImage texture, RGB kd, RGB kr, RGB kt, int ns, int nt, double uScale, double vScale, double uOffset, double vOffset, double tileSizeU, double tileSizeV, double rotX, double rotY, double rotZ);\nExample:\n\"textures/texture.png\",  0.5,0.5,0.5,  0.1,0.1,0.1,  0.0,0.0,0.0, 50, 10, 1.0, 1.0, 0.0, 0.0, 1.5, 1.5, 0.0, 0.0, 0.0\nEnter your values after three diyez symbol\n###\n";
    return IMAGE_STR;
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
    
    //ImageTexture(BufferedImage texture,
    //               RGB kd, RGB kr, RGB kt,
    //               int ns, int nt,
    //               double uScale, double vScale,
    //               double uOffset, double vOffset,
    //               double tileSizeU, double tileSizeV,
    //               double rotX, double rotY, double rotZ)
    
    //    texture,
    //    new RGB(0.5, 0.5, 0.5), new RGB(0.1, 0.1, 0.1), new RGB(0.0, 0.0, 0.0),
    //    50, 10,
    //    1.0, 1.0,
    //    0.0, 0.0,
    //    1.5, 1.5,
    //    0.0, 0.0, -270.0
    
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
      
      double us = Double.parseDouble(split[12]);
      double vs = Double.parseDouble(split[13]);
      
      double uo = Double.parseDouble(split[14]);
      double vo = Double.parseDouble(split[15]);
      
      double tu = Double.parseDouble(split[16]);
      double tv = Double.parseDouble(split[17]);
      
      double rx = Double.parseDouble(split[18]);
      double ry = Double.parseDouble(split[19]);
      double rz = Double.parseDouble(split[20]);
      
      texture = new ImageTexture(
        timg,
        new RGB(c1r, c1g, c1b),
        new RGB(c2r, c2g, c2b),
        new RGB(c3r, c3g, c3b),
        ns, nt,
        us, vs,
        uo, vo,
        tu, tv,
      rx, ry, rz);
      ((ImageTexture)(texture)).setImagePath(tstr);
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
      { "uScale", new Double(uScale) },
      { "vScale", new Double(vScale) },
      { "uOffset", new Double(uOffset) },
      { "vOffset", new Double(vOffset) },
      { "tileSizeU", new Double(tileSizeU) },
      { "tileSizeV", new Double(tileSizeV) },
      { "rotX", new Double(rotX) },
      { "rotY", new Double(rotY) },
      { "rotZ", new Double(rotZ) }
    };
    writer.writeFields(fields);
  }
  
  // Getter methods
  public double getUOffset() { return uOffset; }
  public double getVOffset() { return vOffset; }
  public double getTileSizeU() { return tileSizeU; }
  public double getTileSizeV() { return tileSizeV; }
  public double getRotX() { return rotX; }
  public double getRotY() { return rotY; }
  public double getRotZ() { return rotZ; }
  
}
