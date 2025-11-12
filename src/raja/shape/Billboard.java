// Murat Inan
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

/**
 * A 2D quad in 3D space for displaying images or transparent textures.
 * Always faces the camera (billboard effect). Supports both rectangular and oval shapes.
 */
public class Billboard extends BasicForm implements java.io.Serializable, Writable
{
  private double width;
  private double height;
  private boolean isRectangle;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  
  /**
   * Constructs a rectangular billboard with specified width and height.
   *
   * @param width the width of the billboard (X ekseni)
   * @param height the height of the billboard (Y ekseni)
   */
  public Billboard(double width, double height) {
    this.width = width;
    this.height = height;
    this.isRectangle = true;
  }
  
  public Billboard() {
    this(20.0, 10.0, false);
  }
  
  /**
   * Constructs a square billboard with specified size.
   *
   * @param size the size of the billboard (both width and height)
   */
  public Billboard(double size) {
    this(size, size);
  }
  
  /**
   * Constructs an oval/elliptical billboard with specified width and height.
   *
   * @param width the width of the oval (X ekseni)
   * @param height the height of the oval (Y ekseni)
   * @param isRectangle false for oval shape
   */
  public Billboard(double width, double height, boolean isRectangle) {
    this.width = width;
    this.height = height;
    this.isRectangle = isRectangle;
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("width", null);
    map.put("height", null);
    map.put("isRectangle", Double.valueOf(1.0)); // 1.0 = rectangle, 0.0 = oval
    
    reader.readFields(map);
    
    double width = ((Number) map.get("width")).doubleValue();
    double height = ((Number) map.get("height")).doubleValue();
    double rectFlag = ((Number) map.get("isRectangle")).doubleValue();
    boolean isRect = (rectFlag != 0.0);
    
    return new Billboard(width, height, isRect);
  }
  
  @Override
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    Matrix4 inv = transform.inverse();
    this.inverseTransform = (inv != null) ? inv : Matrix4.identity();
  }
  
  @Override
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  @Override
  public Point3D computeIntersection(Ray r) {
    Point3D localOrigin = inverseTransform.transformPoint(r.origin);
    Vector3D localDirection = inverseTransform.transformVector(r.direction).normalization();
    
    if (Math.abs(localDirection.z) < 1e-12) {
      return null;
    }
    
    double t = -localOrigin.z / localDirection.z;
    if (t < 1e-6) {
      return null;
    }
    
    // SWAP: X ve Y'yi değiştir
    Point3D localHit = new Point3D(
      localOrigin.y + localDirection.y * t,  // Y -> X (width)
      localOrigin.x + localDirection.x * t,  // X -> Y (height)
      0
    );
    
    boolean withinBounds;
    if (isRectangle) {
      double halfWidth = width / 2.0;
      double halfHeight = height / 2.0;
      // SWAP: Artık localHit.x = width, localHit.y = height
      withinBounds = Math.abs(localHit.x) <= halfWidth && Math.abs(localHit.y) <= halfHeight;
      } else {
      double rx = width / 2.0;
      double ry = height / 2.0;
      double nx = localHit.x / rx;
      double ny = localHit.y / ry;
      withinBounds = (nx * nx + ny * ny) <= 1.0;
    }
    
    if (!withinBounds) {
      return null;
    }
    
    if (hasLG(r.origin)) {
      return null;
    }
    
    // SWAP: Geri transform etmeden önce tekrar swap
    Point3D correctedHit = new Point3D(localHit.y, localHit.x, 0);
    return transform.transformPoint(correctedHit);
  }
  
  @Override
  public boolean exactlyContains(Point3D p) {
    Point3D localPoint = inverseTransform.transformPoint(p);
    
    // SWAP: X ve Y
    Point3D swappedPoint = new Point3D(localPoint.y, localPoint.x, localPoint.z);
    
    if (Math.abs(swappedPoint.z) > 1e-6) {
      return false;
    }
    
    if (isRectangle) {
      double halfWidth = width / 2.0;
      double halfHeight = height / 2.0;
      return Math.abs(swappedPoint.x) <= halfWidth && Math.abs(swappedPoint.y) <= halfHeight;
      } else {
      double rx = width / 2.0;
      double ry = height / 2.0;
      double nx = swappedPoint.x / rx;
      double ny = swappedPoint.y / ry;
      return (nx * nx + ny * ny) <= 1.0;
    }
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p) {
    Point3D localPoint = inverseTransform.transformPoint(p);
    
    // SWAP: X ve Y
    Point3D swappedPoint = new Point3D(localPoint.y, localPoint.x, localPoint.z);
    
    if (Math.abs(swappedPoint.z) > 1e-6) {
      return false;
    }
    
    if (isRectangle) {
      double halfWidth = width / 2.0;
      double halfHeight = height / 2.0;
      return Math.abs(swappedPoint.x) < halfWidth && Math.abs(swappedPoint.y) < halfHeight;
      } else {
      double rx = width / 2.0;
      double ry = height / 2.0;
      double nx = swappedPoint.x / rx;
      double ny = swappedPoint.y / ry;
      return (nx * nx + ny * ny) < 1.0;
    }
  }
  
  @Override
  public Vector3D computeNormal(Point3D p) {
    Vector3D localNormal = new Vector3D(0, 0, 1);
    return transform.transformVector(localNormal).normalization();
  }
  
  public double getWidth() {
    return width;
  }
  
  public double getHeight() {
    return height;
  }
  
  public boolean isRectangle() {
    return isRectangle;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String BILLBOARD_STR = "Constructor is: Billboard(double width, double height, boolean isRectangle);\nExample -last value is volume-:\n3.0,  1.5,  true,  1\nNote that this shape is convenient for TransparentPNGTexture.\nEnter your values after three diyez symbol\n###\n";
    return BILLBOARD_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public BasicForm getInstance(String info)
  {
    this.exampleString = info;
    
    BasicForm bform = null;
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return bform;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    String [] split = str.split (",");
    if (split == null) return bform;
    
    try {
      double wd = Double.parseDouble(split[0]);
      double hd = Double.parseDouble(split[1]);
      
      boolean ir = Boolean.parseBoolean(split[2]);
      
      bform = new Billboard(wd, hd, ir);
      double vl = Double.parseDouble(split[3]);
      bform.setVolumeValue(vl);
      return bform;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  private double volumeValue = 1.0;
  public double getVolumeValue() {
    return this.volumeValue;
  }
  
  public void setVolumeValue(double vlm) {
    this.volumeValue = vlm;
  }
  ////////////
  
  @Override
  public String toString() {
    return ObjectWriter.toString(this);
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "width", new Double(width) },
      { "height", new Double(height) },
      { "isRectangle", isRectangle ? Double.valueOf(1.0) : Double.valueOf(0.0) }
    };
    writer.writeFields(fields);
  }
  
}
