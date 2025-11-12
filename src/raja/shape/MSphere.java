/* $Id: MSphere.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
 * Copyright (C) 1999-2000 E. Fleury & G. Sutre
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

/**
 * Represents a sphere defined by a center point and radius.
 * Supports arbitrary position, orientation, and scaling via a 4x4 transformation matrix.
 * Non-uniform scaling can create ellipsoidal shapes.
 */
public class MSphere extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D center;
  protected double radius;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  
  /**
   * Constructs a sphere with the specified center and radius.
   *
   * @param radius the radius of the sphere
   */
  public MSphere(double radius)
  {
    this.center = new Point3D(0, 0, 0);
    this.radius = radius;
  }
  
  /**
   * Builds an MSphere instance from ObjectReader data.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    map.put("radius",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new MSphere(((Number) map.get("radius")).doubleValue());
  }
  
  /**
   * Sets the transformation matrix and updates derived data.
   *
   * @param transform the transformation matrix to apply
   */
  @Override
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    Matrix4 inv = transform.inverse();
    this.inverseTransform = (inv != null) ? inv : Matrix4.identity();
  }
  
  /**
   * Gets the current transformation matrix.
   *
   * @return the current transformation matrix
   */
  @Override
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  /**
   * Gets the inverse transformation matrix.
   *
   * @return the inverse transformation matrix
   */
  public Matrix4 getInverseTransform() {
    return this.inverseTransform;
  }
  
  /**
   * Computes the intersection point between a ray and the sphere/ellipsoid.
   * Transforms the ray to local space for accurate intersection calculations.
   *
   * @param r the ray to test for intersection
   * @return the intersection point in world coordinates, or null if no intersection
   */
  @Override
  public Point3D computeIntersection(Ray r)
  {
    // Transform ray to local space
    Point3D localOrigin = inverseTransform.transformPoint(r.origin);
    Vector3D localDirection = inverseTransform.transformVector(r.direction).normalization();
    Ray localRay = new Ray(localOrigin, localDirection);
    
    // Sphere intersection in local space
    // Vektör: ray origin'den sphere center'a
    Vector3D oc = new Vector3D(localRay.origin, center);
    
    double a = Vector3D.dotProduct(localRay.direction, localRay.direction);
    double b = -2.0 * Vector3D.dotProduct(localRay.direction, oc);
    double c = Vector3D.dotProduct(oc, oc) - radius * radius;
    
    double discriminant = b*b - 4*a*c;
    
    if (discriminant < 0) {
      return null; // No intersection
    }
    
    double sqrtDiscriminant = Math.sqrt(discriminant);
    double t1 = (-b - sqrtDiscriminant) / (2*a);
    double t2 = (-b + sqrtDiscriminant) / (2*a);
    
    // Find the closest positive intersection
    double t = -1;
    if (t1 > 1e-9) {
      t = t1;
      } else if (t2 > 1e-9) {
      t = t2;
    }
    
    if (t < 0) {
      return null; // No valid intersection
    }
    
    // Local intersection point
    Point3D localIntersection = new Point3D(
      localRay.origin,
      Vector3D.product(localRay.direction, t)
    );
    
    // Transform back to world space
    Point3D worldIntersection = transform.transformPoint(localIntersection);
    
    // Check light group after transformation
    if (hasLG(worldIntersection)) {
      return null;
    }
    
    return worldIntersection;
  }
  
  /**
   * Checks if a point is inside or on the surface of the sphere/ellipsoid.
   *
   * @param p the point to test in world coordinates
   * @return true if the point is inside or on the surface, false otherwise
   */
  @Override
  public boolean exactlyContains(Point3D p)
  {
    // Transform point to local space for accurate check
    Point3D localPoint = inverseTransform.transformPoint(p);
    return (Point3D.distance(localPoint, center) <= radius);
  }
  
  /**
   * Checks if a point is strictly inside the sphere/ellipsoid (not on surface).
   *
   * @param p the point to test in world coordinates
   * @return true if the point is strictly inside, false otherwise
   */
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    // Transform point to local space for accurate check
    Point3D localPoint = inverseTransform.transformPoint(p);
    return (Point3D.distance(localPoint, center) < radius);
  }
  
  /**
   * Computes the surface normal at the given point on the sphere/ellipsoid.
   *
   * @param p the point on the surface in world coordinates
   * @return the normalized surface normal vector
   */
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    Point3D localPoint = inverseTransform.transformPoint(p);
    Vector3D localNormal = new Vector3D(center, localPoint);
    
    // Normal transformation: (M^-1)^T
    Matrix4 normalMatrix = inverseTransform.transpose();
    Vector3D worldNormal = normalMatrix.transformVector(localNormal);
    
    return worldNormal.normalization();
  }
  
  /**
   * Gets the local center point (before transformation).
   *
   * @return the local center point
   */
  public Point3D getLocalCenter() {
    return center;
  }
  
  /**
   * Gets the local radius (before transformation).
   *
   * @return the local radius
   */
  public double getLocalRadius() {
    return radius;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String SPHERE_STR = "Constructor is: MSphere(double radius);\nExample -last value is volume-:\n0.7,  1\nEnter your values after three diyez symbol\n###\n";
    return SPHERE_STR;
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
      
      bform = new MSphere(rd);
      double vl = Double.parseDouble(split[1]);
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
  
  /**
   * Returns a string representation of the sphere.
   *
   * @return string representation
   */
  @Override
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  /**
   * Writes the sphere data to an ObjectWriter.
   *
   * @param writer the ObjectWriter to write to
   * @throws java.io.IOException if writing fails
   */
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "radius", new Double(radius) }
    };
    writer.writeFields(fields);
  }
  
}
