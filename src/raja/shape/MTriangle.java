// Murat Inan
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class MTriangle extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D v0, v1, v2;  // Üç köşe noktası
  protected Vector3D normal;     // Önceden hesaplanmış normal
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  private Point3D worldV0, worldV1, worldV2; // Transform edilmiş köşeler
  private Vector3D worldNormal; // Transform edilmiş normal
  
  public MTriangle(Point3D v0, Point3D v1, Point3D v2)
  {
    this.v0 = v0;
    this.v1 = v1;
    this.v2 = v2;
    
    // Normal hesapla (saat yönünün tersine - counter clockwise)
    Vector3D edge1 = new Vector3D(v0, v1);
    Vector3D edge2 = new Vector3D(v0, v2);
    this.normal = Vector3D.normalization(Vector3D.crossProduct(edge1, edge2));
    
    updateTransformedVertices();
  }
  
  public MTriangle(double x0, double y0, double z0,
    double x1, double y1, double z1,
  double x2, double y2, double z2)
  {
    this(new Point3D(x0, y0, z0),
      new Point3D(x1, y1, z1),
    new Point3D(x2, y2, z2));
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("v0", null);
    map.put("v1", null);
    map.put("v2", null);
    
    reader.readFields(map);
    
    return new MTriangle((Point3D) map.get("v0"),
      (Point3D) map.get("v1"),
    (Point3D) map.get("v2"));
  }
  
  /**
   * Transform matrix'ini set et ve köşeleri güncelle
   */
  @Override
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    Matrix4 inv = transform.inverse();
    this.inverseTransform = (inv != null) ? inv : Matrix4.identity();
    updateTransformedVertices();
  }
  
  @Override
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  /**
   * Transform edilmiş köşeleri ve normali güncelle
   */
  private void updateTransformedVertices() {
    this.worldV0 = transform.transformPoint(v0);
    this.worldV1 = transform.transformPoint(v1);
    this.worldV2 = transform.transformPoint(v2);
    
    // World space'de normal hesapla
    Vector3D edge1 = new Vector3D(worldV0, worldV1);
    Vector3D edge2 = new Vector3D(worldV0, worldV2);
    this.worldNormal = Vector3D.normalization(Vector3D.crossProduct(edge1, edge2));
  }
  
  /**
   * Möller-Trumbore intersection algorithm (world coordinates)
   */
  @Override
  public Point3D computeIntersection(Ray r)
  {
    // World coordinates kullan
    Vector3D edge1 = new Vector3D(worldV0, worldV1);
    Vector3D edge2 = new Vector3D(worldV0, worldV2);
    
    Vector3D h = Vector3D.crossProduct(r.direction, edge2);
    double a = Vector3D.dotProduct(edge1, h);
    
    // Ray parallel to triangle?
    if (Math.abs(a) < 1e-12) {
      return null;
    }
    
    double f = 1.0 / a;
    Vector3D s = new Vector3D(worldV0, r.origin);
    double u = f * Vector3D.dotProduct(s, h);
    
    // u outside [0,1]?
    if (u < 0.0 || u > 1.0) {
      return null;
    }
    
    Vector3D q = Vector3D.crossProduct(s, edge1);
    double v = f * Vector3D.dotProduct(r.direction, q);
    
    // v outside [0,1] or u+v > 1?
    if (v < 0.0 || u + v > 1.0) {
      return null;
    }
    
    // At this stage we can compute t to find out where the intersection point is on the line
    double t = f * Vector3D.dotProduct(edge2, q);
    
    if (t > 1e-6) { // Ray intersection
      if (hasLG(r.origin)) {
        return null;
      }
      return new Point3D(r.origin, Vector3D.product(r.direction, t));
    }
    
    // There is a line intersection but not a ray intersection
    return null;
  }
  
  @Override
  public boolean exactlyContains(Point3D p)
  {
    // World coordinates kullan
    Vector3D v0p = new Vector3D(worldV0, p);
    Vector3D v0v1 = new Vector3D(worldV0, worldV1);
    Vector3D v0v2 = new Vector3D(worldV0, worldV2);
    
    double dot00 = Vector3D.dotProduct(v0v2, v0v2);
    double dot01 = Vector3D.dotProduct(v0v2, v0v1);
    double dot02 = Vector3D.dotProduct(v0v2, v0p);
    double dot11 = Vector3D.dotProduct(v0v1, v0v1);
    double dot12 = Vector3D.dotProduct(v0v1, v0p);
    
    double invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01);
    double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
    
    // Check if point is in triangle
    return (u >= 0) && (v >= 0) && (u + v <= 1);
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    // World coordinates kullan
    Vector3D v0p = new Vector3D(worldV0, p);
    Vector3D v0v1 = new Vector3D(worldV0, worldV1);
    Vector3D v0v2 = new Vector3D(worldV0, worldV2);
    
    double dot00 = Vector3D.dotProduct(v0v2, v0v2);
    double dot01 = Vector3D.dotProduct(v0v2, v0v1);
    double dot02 = Vector3D.dotProduct(v0v2, v0p);
    double dot11 = Vector3D.dotProduct(v0v1, v0v1);
    double dot12 = Vector3D.dotProduct(v0v1, v0p);
    
    double invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01);
    double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
    double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
    
    // Check if point is strictly inside triangle (not on edges)
    return (u > 0) && (v > 0) && (u + v < 1);
  }
  
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    // World normal dön
    return worldNormal;
  }
  
  /**
   * Local köşelere erişim için getter'lar
   */
  public Point3D getLocalV0() { return v0; }
  public Point3D getLocalV1() { return v1; }
  public Point3D getLocalV2() { return v2; }
  
  /**
   * World köşelere erişim için getter'lar
   */
  public Point3D getWorldV0() { return worldV0; }
  public Point3D getWorldV1() { return worldV1; }
  public Point3D getWorldV2() { return worldV2; }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String TRIANGLE_STR = "Constructor is: MTriangle(double x0, double y0, double z0,double x1, double y1, double z1, double x2, double y2, double z2);\nExample -last value is volume-:\n-2,-2,0,  2,-2,0,  0,0,3,  1\nEnter your values after three diyez symbol\n###\n";
    return TRIANGLE_STR;
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
    
    //-2,-2,0,  2,-2,0,  0,0,3,  0.5
    try {
      double p1x = Double.parseDouble(split[0]);
      double p1y = Double.parseDouble(split[1]);
      double p1z = Double.parseDouble(split[2]);
      
      double p2x = Double.parseDouble(split[3]);
      double p2y = Double.parseDouble(split[4]);
      double p2z = Double.parseDouble(split[5]);
      
      double p3x = Double.parseDouble(split[6]);
      double p3y = Double.parseDouble(split[7]);
      double p3z = Double.parseDouble(split[8]);
      
      bform = new MTriangle(
        new Point3D(p1x, p1y, p1z),
        new Point3D(p2x, p2y, p2z),
      new Point3D(p3x, p3y, p3z));
      double vl = Double.parseDouble(split[9]);
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
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "v0", v0 },
      { "v1", v1 },
      { "v2", v2 }
    };
    writer.writeFields(fields);
  }
  
}
