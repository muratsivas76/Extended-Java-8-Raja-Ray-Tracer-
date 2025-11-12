// Murat Inan
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class MCylinder extends BasicForm implements java.io.Serializable, Writable
{
  private double radius;
  private double height;
  private Matrix4 transform;
  private Matrix4 inverseTransform;
  
  public MCylinder(double radius, double height) {
    this.radius = radius;
    this.height = height;
    this.transform = Matrix4.identity();
    this.inverseTransform = Matrix4.identity();
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap map = new HashMap();
    map.put("radius", null);
    map.put("height", null);
    reader.readFields(map);
    return new MCylinder(((Number) map.get("radius")).doubleValue(),
    ((Number) map.get("height")).doubleValue());
  }
  
  @Override
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    this.inverseTransform = transform.inverse();
  }
  
  @Override
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  @Override
  public Point3D computeIntersection(Ray worldRay) {
    Ray localRay = new Ray(
      inverseTransform.transformPoint(worldRay.origin),
      inverseTransform.transformVector(worldRay.direction).normalization()
    );
    
    double ox = localRay.origin.x, oy = localRay.origin.y, oz = localRay.origin.z;
    double dx = localRay.direction.x, dy = localRay.direction.y, dz = localRay.direction.z;
    
    double t = Double.POSITIVE_INFINITY;
    
    double a = dx*dx + dz*dz;
    if (a > 1e-12) {
      double b = 2*(ox*dx + oz*dz);
      double c = ox*ox + oz*oz - radius*radius;
      double disc = b*b - 4*a*c;
      
      if (disc >= 0) {
        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2*a);
        double t2 = (-b + sqrtDisc) / (2*a);
        
        if (t1 > 1e-6 && isWithinHeight(oy + t1*dy)) t = Math.min(t, t1);
        if (t2 > 1e-6 && isWithinHeight(oy + t2*dy)) t = Math.min(t, t2);
      }
    }
    
    if (Math.abs(dy) > 1e-12) {
      double tBottom = -oy / dy;
      if (tBottom > 1e-6 && tBottom < t && isWithinDisk(ox + tBottom*dx, oz + tBottom*dz)) {
        t = tBottom;
      }
      
      double tTop = (height - oy) / dy;
      if (tTop > 1e-6 && tTop < t && isWithinDisk(ox + tTop*dx, oz + tTop*dz)) {
        t = tTop;
      }
    }
    
    if (t == Double.POSITIVE_INFINITY || hasLG(worldRay.origin)) return null;
    
    Point3D localHit = localRay.pointAt(t);
    return transform.transformPoint(localHit);
  }
  
  private boolean isWithinHeight(double y) {
    return y >= 0 && y <= height;
  }
  
  private boolean isWithinDisk(double x, double z) {
    return x*x + z*z <= radius*radius;
  }
  
  @Override
  public Vector3D computeNormal(Point3D worldPoint) {
    Point3D localPoint = inverseTransform.transformPoint(worldPoint);
    Vector3D localNormal;
    
    if (Math.abs(localPoint.y) < 1e-6) {
      localNormal = new Vector3D(0, -1, 0);
      } else if (Math.abs(localPoint.y - height) < 1e-6) {
      localNormal = new Vector3D(0, 1, 0);
      } else {
      localNormal = new Vector3D(localPoint.x, 0, localPoint.z).normalization();
    }
    
    return transform.transformVector(localNormal).normalization();
  }
  
  @Override
  public boolean exactlyContains(Point3D p) {
    Point3D local = inverseTransform.transformPoint(p);
    return isWithinDisk(local.x, local.z) && isWithinHeight(local.y);
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p) {
    Point3D local = inverseTransform.transformPoint(p);
    return (local.x*local.x + local.z*local.z < radius*radius) &&
    (local.y > 0 && local.y < height);
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String CYLINDER_STR = "Constructor is:  MCylinder(double radius, double height);\nExample -last value is volume-:\n1.25,  1.5,  1\nEnter your values after three diyez symbol\n###\n";
    return CYLINDER_STR;
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
      double rd = Double.parseDouble(split[0]);
      double hg = Double.parseDouble(split[1]);
      
      bform = new MCylinder(rd, hg);
      double vl = Double.parseDouble(split[2]);
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
      { "radius", new Double(radius) },
      { "height", new Double(height) }
    };
    writer.writeFields(fields);
  }
  
}
