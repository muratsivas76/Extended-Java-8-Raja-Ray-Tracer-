// Murat Inan
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class MBox extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D min;
  protected Point3D max;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  private Point3D worldMin, worldMax; // Transform edilmiş köşeler
  
  public MBox() {
    this(new Point3D(-0.5, -0.5, -0.5), new Point3D(0.5, 0.5, 0.5));
  }
  
  public MBox(Point3D min, Point3D max)
  {
    this.min = new Point3D(Math.min(min.x, max.x),
      Math.min(min.y, max.y),
    Math.min(min.z, max.z));
    this.max = new Point3D(Math.max(min.x, max.x),
      Math.max(min.y, max.y),
    Math.max(min.z, max.z));
    updateTransformedVertices();
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("min", null);
    map.put("max", null);
    reader.readFields(map);
    return new MBox((Point3D) map.get("min"),
    (Point3D) map.get("max"));
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
   * Transform edilmiş köşeleri güncelle
   */
  private void updateTransformedVertices() {
    this.worldMin = transform.transformPoint(min);
    this.worldMax = transform.transformPoint(max);
    
    // Transform sonrası min/max'ı yeniden hesapla (rotation için)
    double minX = Math.min(worldMin.x, worldMax.x);
    double minY = Math.min(worldMin.y, worldMax.y);
    double minZ = Math.min(worldMin.z, worldMax.z);
    double maxX = Math.max(worldMin.x, worldMax.x);
    double maxY = Math.max(worldMin.y, worldMax.y);
    double maxZ = Math.max(worldMin.z, worldMax.z);
    
    this.worldMin = new Point3D(minX, minY, minZ);
    this.worldMax = new Point3D(maxX, maxY, maxZ);
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    // World coordinates kullan
    double tmin = Double.NEGATIVE_INFINITY;
    double tmax = Double.POSITIVE_INFINITY;
    
    // X slab
    if (Math.abs(r.direction.x) < 1e-12) {
      if (r.origin.x < worldMin.x || r.origin.x > worldMax.x) return null;
      } else {
      double tx1 = (worldMin.x - r.origin.x) / r.direction.x;
      double tx2 = (worldMax.x - r.origin.x) / r.direction.x;
      if (tx1 > tx2) { double tmp = tx1; tx1 = tx2; tx2 = tmp; }
        tmin = Math.max(tmin, tx1);
      tmax = Math.min(tmax, tx2);
      if (tmin > tmax) return null;
    }
    
    // Y slab
    if (Math.abs(r.direction.y) < 1e-12) {
      if (r.origin.y < worldMin.y || r.origin.y > worldMax.y) return null;
      } else {
      double ty1 = (worldMin.y - r.origin.y) / r.direction.y;
      double ty2 = (worldMax.y - r.origin.y) / r.direction.y;
      if (ty1 > ty2) { double tmp = ty1; ty1 = ty2; ty2 = tmp; }
        tmin = Math.max(tmin, ty1);
      tmax = Math.min(tmax, ty2);
      if (tmin > tmax) return null;
    }
    
    // Z slab
    if (Math.abs(r.direction.z) < 1e-12) {
      if (r.origin.z < worldMin.z || r.origin.z > worldMax.z) return null;
      } else {
      double tz1 = (worldMin.z - r.origin.z) / r.direction.z;
      double tz2 = (worldMax.z - r.origin.z) / r.direction.z;
      if (tz1 > tz2) { double tmp = tz1; tz1 = tz2; tz2 = tmp; }
        tmin = Math.max(tmin, tz1);
      tmax = Math.min(tmax, tz2);
      if (tmin > tmax) return null;
    }
    
    if (tmax < 0) return null;
    
    double t = (tmin >= 0) ? tmin : tmax;
    return new Point3D(r.origin, Vector3D.product(r.direction, t));
  }
  
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    double epsilon = 1e-6;
    
    // World coordinates kullan
    double dxMin = Math.abs(p.x - worldMin.x);
    double dxMax = Math.abs(p.x - worldMax.x);
    double dyMin = Math.abs(p.y - worldMin.y);
    double dyMax = Math.abs(p.y - worldMax.y);
    double dzMin = Math.abs(p.z - worldMin.z);
    double dzMax = Math.abs(p.z - worldMax.z);
    
    double minDist = Math.min(Math.min(Math.min(dxMin, dxMax),
      Math.min(dyMin, dyMax)),
    Math.min(dzMin, dzMax));
    
    if (Math.abs(minDist - dxMin) < epsilon) return new Vector3D(-1, 0, 0);
    if (Math.abs(minDist - dxMax) < epsilon) return new Vector3D( 1, 0, 0);
    if (Math.abs(minDist - dyMin) < epsilon) return new Vector3D( 0,-1, 0);
    if (Math.abs(minDist - dyMax) < epsilon) return new Vector3D( 0, 1, 0);
    if (Math.abs(minDist - dzMin) < epsilon) return new Vector3D( 0, 0,-1);
    if (Math.abs(minDist - dzMax) < epsilon) return new Vector3D( 0, 0, 1);
    
    return new Vector3D(0, 0, 1);
  }
  
  @Override
  public boolean exactlyContains(Point3D p)
  {
    // World coordinates kullan
    return (p.x >= worldMin.x && p.x <= worldMax.x &&
      p.y >= worldMin.y && p.y <= worldMax.y &&
    p.z >= worldMin.z && p.z <= worldMax.z);
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    // World coordinates kullan
    return (p.x > worldMin.x && p.x < worldMax.x &&
      p.y > worldMin.y && p.y < worldMax.y &&
    p.z > worldMin.z && p.z < worldMax.z);
  }
  
  /**
   * Local köşelere erişim için getter'lar
   */
  public Point3D getLocalMin() { return min; }
  public Point3D getLocalMax() { return max; }
  
  /**
   * World köşelere erişim için getter'lar
   */
  public Point3D getWorldMin() { return worldMin; }
  public Point3D getWorldMax() { return worldMax; }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String BOX_STR = "Constructor is: MBox(Point3D min, Point3D max);\nExample -last value is volume-:\n-0.5,-0.5,-0.5,  0.5,0.5,0.5,  1\nEnter your values after three diyez symbol\n###\n";
    return BOX_STR;
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
    
    //-0.5,-0.5,-0.5,  0.5,0.5,0.5, 1.0
    try {
      double p1x = Double.parseDouble(split[0]);
      double p1y = Double.parseDouble(split[1]);
      double p1z = Double.parseDouble(split[2]);
      
      double p2x = Double.parseDouble(split[3]);
      double p2y = Double.parseDouble(split[4]);
      double p2z = Double.parseDouble(split[5]);
      
      bform = new MBox(new Point3D(p1x, p1y, p1z), new Point3D(p2x, p2y, p2z));
      double vl = Double.parseDouble(split[6]);
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
    Object[][] fields = { { "min", min },
    { "max", max } };
    writer.writeFields(fields);
  }
  
}
