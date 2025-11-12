/*
 * Murat Inan
 *
 * MCone.java - Corrected Ray Tracing Cone implementation
 */

package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class MCone extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D apex;
  protected Vector3D axis;
  protected double height;
  protected double baseRadius;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  
  public MCone(Point3D apex, Vector3D axis, double height, double baseRadius)
  {
    this.apex = apex;
    this.axis = Vector3D.normalization(axis);
    this.height = height;
    this.baseRadius = baseRadius;
    updateTransforms();
  }
  
  public MCone(Point3D apex, Vector3D axis, double angle)
  {
    this.apex = apex;
    this.axis = Vector3D.normalization(axis);
    this.height = 10.0;
    this.baseRadius = this.height * Math.tan(angle); // Fixed: use this.height
    updateTransforms();
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("apex", null);
    map.put("axis", null);
    map.put("height", null);
    map.put("baseRadius", null);
    
    reader.readFields(map);
    
    return new MCone((Point3D) map.get("apex"),
      (Vector3D) map.get("axis"),
      ((Double) map.get("height")).doubleValue(),
    ((Double) map.get("baseRadius")).doubleValue());
  }
  
  @Override
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    updateTransforms();
  }
  
  @Override
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  private void updateTransforms() {
    Matrix4 inv = this.transform.inverse();
    this.inverseTransform = (inv != null) ? inv : Matrix4.identity();
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    Point3D localOrigin = inverseTransform.transformPoint(r.origin);
    Vector3D localDirection = inverseTransform.transformVector(r.direction).normalization();
    
    Point3D baseCenter = new Point3D(apex, Vector3D.product(axis, height));
    
    Point3D coneIntersection = computeConeSurfaceIntersection(localOrigin, localDirection);
    Point3D baseIntersection = computeBaseIntersection(localOrigin, localDirection, baseCenter);
    
    return findClosestIntersection(coneIntersection, baseIntersection, localOrigin);
  }
  
  private Point3D computeConeSurfaceIntersection(Point3D origin, Vector3D direction) {
    // Ray: P = O + t*D
    // Cone: ((P-A)·V)² = cos²α * |P-A|²
    // where cos²α = h²/(h²+r²), and we use k = r/h for convenience
    
    Vector3D co = new Vector3D(apex, origin);
    double k = baseRadius / height;
    double cosAlphaSq = 1.0 / (1.0 + k * k);
    
    double dDotV = Vector3D.dotProduct(direction, axis);
    double coDotV = Vector3D.dotProduct(co, axis);
    double coDotCo = Vector3D.dotProduct(co, co);
    double dDotCo = Vector3D.dotProduct(direction, co);
    
    // Quadratic: a*t² + b*t + c = 0
    double a = dDotV * dDotV - cosAlphaSq;
    double b = 2.0 * (dDotV * coDotV - dDotCo * cosAlphaSq);
    double c = coDotV * coDotV - coDotCo * cosAlphaSq;
    
    double discriminant = b * b - 4.0 * a * c;
    
    if (discriminant < Ray.EPSILON2) {
      return null;
    }
    
    double sqrtDisc = Math.sqrt(discriminant);
    double t1 = (-b - sqrtDisc) / (2.0 * a);
    double t2 = (-b + sqrtDisc) / (2.0 * a);
    
    // Check both intersections for validity
    Point3D result1 = checkConeIntersection(origin, direction, t1);
    Point3D result2 = checkConeIntersection(origin, direction, t2);
    
    if (result1 != null && result2 != null) {
      return (t1 < t2) ? result1 : result2;
    }
    return (result1 != null) ? result1 : result2;
  }
  
  private Point3D checkConeIntersection(Point3D origin, Vector3D direction, double t) {
    if (t < Ray.EPSILON2) {
      return null;
    }
    
    Point3D p = new Point3D(origin, Vector3D.product(direction, t));
    
    // Verify point is within cone height bounds
    Vector3D toApex = new Vector3D(apex, p);
    double h = Vector3D.dotProduct(toApex, axis);
    
    if (h >= -Ray.EPSILON2 && h <= height + Ray.EPSILON2) {
      return p;
    }
    
    return null;
  }
  
  private Point3D computeBaseIntersection(Point3D origin, Vector3D direction, Point3D baseCenter) {
    // Plane equation: (P - C)·N = 0, where N = axis
    double denom = Vector3D.dotProduct(axis, direction);
    
    if (Math.abs(denom) < Ray.EPSILON2) {
      return null;
    }
    
    Vector3D oc = new Vector3D(origin, baseCenter);
    double t = Vector3D.dotProduct(oc, axis) / denom;
    
    if (t < Ray.EPSILON2) {
      return null;
    }
    
    Point3D intersection = new Point3D(origin, Vector3D.product(direction, t));
    
    // Check if point is within base circle
    Vector3D toCenter = new Vector3D(baseCenter, intersection);
    if (toCenter.norm() <= baseRadius + Ray.EPSILON2) {
      return intersection;
    }
    
    return null;
  }
  
  private Point3D findClosestIntersection(Point3D coneIntersection, Point3D baseIntersection, Point3D origin) {
    Point3D closest = null;
    double minDistSq = Double.POSITIVE_INFINITY;
    
    if (coneIntersection != null) {
      Vector3D v = new Vector3D(origin, coneIntersection);
      double distSq = Vector3D.dotProduct(v, v);
      if (distSq < minDistSq) {
        minDistSq = distSq;
        closest = coneIntersection;
      }
    }
    
    if (baseIntersection != null) {
      Vector3D v = new Vector3D(origin, baseIntersection);
      double distSq = Vector3D.dotProduct(v, v);
      if (distSq < minDistSq) {
        closest = baseIntersection;
      }
    }
    
    return closest != null ? transform.transformPoint(closest) : null;
  }
  
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    Point3D localPoint = inverseTransform.transformPoint(p);
    Point3D baseCenter = new Point3D(apex, Vector3D.product(axis, height));
    
    Vector3D toBase = new Vector3D(baseCenter, localPoint);
    double distToBasePlane = Math.abs(Vector3D.dotProduct(toBase, axis));
    
    Vector3D normal;
    
    // Check if point is on base (within epsilon of base plane)
    if (distToBasePlane < Ray.EPSILON2 && toBase.norm() <= baseRadius + Ray.EPSILON2) {
      normal = Vector3D.product(axis, -1.0);
      } else {
      // Point is on cone surface
      Vector3D toApex = new Vector3D(apex, localPoint);
      double h = Vector3D.dotProduct(toApex, axis);
      
      // Project point onto axis to find nearest axis point
      Point3D axisPoint = new Point3D(apex, Vector3D.product(axis, h));
      Vector3D radial = new Vector3D(axisPoint, localPoint);
      double radialLen = radial.norm();
      
      if (radialLen < Ray.EPSILON2) {
        // At or very near the apex
        normal = radial.normalization();
        } else {
        // Cone surface normal: combine radial and axial components
        double k = baseRadius / height;
        Vector3D radialUnit = Vector3D.product(radial, 1.0 / radialLen);
        normal = Vector3D.sum(radialUnit, Vector3D.product(axis, -k)).normalization();
      }
    }
    
    return transform.transformVector(normal).normalization();
  }
  
  @Override
  public boolean exactlyContains(Point3D p)
  {
    Point3D localPoint = inverseTransform.transformPoint(p);
    
    Vector3D toApex = new Vector3D(apex, localPoint);
    double h = Vector3D.dotProduct(toApex, axis);
    
    if (h < -Ray.EPSILON2 || h > height + Ray.EPSILON2) {
      return false;
    }
    
    // At height h, the radius is: r(h) = (h/height) * baseRadius
    double currentRadius = (h / height) * baseRadius;
    Point3D axisPoint = new Point3D(apex, Vector3D.product(axis, h));
    double distanceToAxis = new Vector3D(axisPoint, localPoint).norm();
    
    return distanceToAxis <= currentRadius + Ray.EPSILON2;
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    Point3D localPoint = inverseTransform.transformPoint(p);
    
    Vector3D toApex = new Vector3D(apex, localPoint);
    double h = Vector3D.dotProduct(toApex, axis);
    
    if (h <= 0 || h >= height) {
      return false;
    }
    
    double currentRadius = (h / height) * baseRadius;
    Point3D axisPoint = new Point3D(apex, Vector3D.product(axis, h));
    double distanceToAxis = new Vector3D(axisPoint, localPoint).norm();
    
    return distanceToAxis < currentRadius;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String CONE_STR = "Constructor is: MCone(Point3D apex, Vector3D axis, double height, double baseRadius);\nExample -last value is volume-:\n0,0,0,  0,0,-1,  8.0,  3.0,  1\nEnter your values after three diyez symbol\n###\n";
    return CONE_STR;
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
    
    //0,0,0,  0,0,-1,  8.0, 3.0,  0.5
    try {
      double px = Double.parseDouble(split[0]);
      double py = Double.parseDouble(split[1]);
      double pz = Double.parseDouble(split[2]);
      
      double dx = Double.parseDouble(split[3]);
      double dy = Double.parseDouble(split[4]);
      double dz = Double.parseDouble(split[5]);
      
      double hg = Double.parseDouble(split[6]);
      double rd = Double.parseDouble(split[7]);
      
      bform = new MCone(new Point3D(px, py, pz), new Vector3D(dx, dy, dz), hg, rd);
      double vl = Double.parseDouble(split[8]);
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
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "apex", apex },
      { "axis", axis },
      { "height", new Double(height) },
      { "baseRadius", new Double(baseRadius) }
    };
    writer.writeFields(fields);
  }
  
}
