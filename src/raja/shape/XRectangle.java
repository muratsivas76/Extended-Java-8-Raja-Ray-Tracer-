/* $Id: XRectangle.java,v 1.0 2025/10/27 12:00:00 murat Exp $
 * Copyright (C) 1999-2000 E. Fleury & G. Sutre
 * Modifications (C) 2025 Murat Inan
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

public class XRectangle extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D center;
  protected Vector3D normal;
  protected double width;   // extent in Y direction (left-right)
  protected double height;  // extent in Z direction (down-up)
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  private Matrix4 normalTransform = Matrix4.identity();
  
  // ONLY constructor: normal, width, height
  // Center is fixed at (0,0,0) in local space
  public XRectangle(Vector3D normal, double width, double height)
  {
    this.center = new Point3D(0, 0, 0);
    this.normal = Vector3D.normalization(normal);
    this.width = width;
    this.height = height;
    updateTransforms();
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("normal", null);
    map.put("width", Double.valueOf(1.0));
    map.put("height", Double.valueOf(1.0));
    
    reader.readFields(map);
    
    Vector3D normal = (Vector3D) map.get("normal");
    Double w = (Double) map.get("width");
    Double h = (Double) map.get("height");
    
    return new XRectangle(normal, w.doubleValue(), h.doubleValue());
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
    
    // Normal transform = (inverse)^T for proper normal transformation
    Matrix4 invTranspose = this.inverseTransform.transpose();
    this.normalTransform = (invTranspose != null) ? invTranspose : Matrix4.identity();
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    if (this.inverseTransform == null) return null;
    
    // Transform ray to local object space
    Point3D localOrigin = this.inverseTransform.transformPoint(r.origin);
    Vector3D localDirection = this.inverseTransform.transformVector(r.direction);
    
    // Local plane passes through (0,0,0) with given normal
    double denom = Vector3D.dotProduct(this.normal, localDirection);
    
    if (Math.abs(denom) < Ray.EPSILON2) {
      return null;
    }
    
    double tLocal = -Vector3D.dotProduct(this.normal, new Vector3D(new Point3D(0,0,0), localOrigin)) / denom;
    if (tLocal < Ray.EPSILON2) {
      return null;
    }
    
    // Compute hit point in local space
    Point3D hitLocal = new Point3D(localOrigin, Vector3D.product(localDirection, tLocal));
    
    // Check bounds in Y and Z (since rectangle lies in YZ plane when normal is along X)
    double y = hitLocal.y;
    double z = hitLocal.z;
    
    if (Math.abs(y) <= this.width / 2.0 && Math.abs(z) <= this.height / 2.0) {
      // Return world-space intersection point
      return new Point3D(r.origin, Vector3D.product(r.direction, tLocal));
    }
    
    return null;
  }
  
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    return normalTransform.transformVector(normal).normalization();
  }
  
  @Override
  public boolean exactlyContains(Point3D p)
  {
    if (this.inverseTransform == null) return false;
    Point3D localP = this.inverseTransform.transformPoint(p);
    
    double distToPlane = Vector3D.dotProduct(this.normal, new Vector3D(new Point3D(0,0,0), localP));
    if (Math.abs(distToPlane) > Ray.EPSILON2) return false;
    
    double y = localP.y;
    double z = localP.z;
    return (Math.abs(y) <= this.width / 2.0 + Ray.EPSILON2) &&
    (Math.abs(z) <= this.height / 2.0 + Ray.EPSILON2);
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    if (this.inverseTransform == null) return false;
    Point3D localP = this.inverseTransform.transformPoint(p);
    
    double distToPlane = Vector3D.dotProduct(this.normal, new Vector3D(new Point3D(0,0,0), localP));
    if (Math.abs(distToPlane) > Ray.EPSILON2) return false;
    
    double y = localP.y;
    double z = localP.z;
    return (Math.abs(y) < this.width / 2.0 - Ray.EPSILON2) &&
    (Math.abs(z) < this.height / 2.0 - Ray.EPSILON2);
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String RECT_STR = "Constructor is: XRectangle(Vector3D normal, double width, double height)\n" +
    "Example - last value is volume:\n" +
    "-1,0,0,  52.0,  17.0,  0   # Backwall transform is 45,0,10.25\n" +
    "Enter your values after three diyez symbol\n###\n";
    return RECT_STR;
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
    
    str = str.substring(diyezIndex + 3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    String[] split = str.split(",");
    if (split == null) return bform;
    
    try {
      if (split.length == 6) {
        double dx = Double.parseDouble(split[0]);
        double dy = Double.parseDouble(split[1]);
        double dz = Double.parseDouble(split[2]);
        double w  = Double.parseDouble(split[3]);
        double h  = Double.parseDouble(split[4]);
        double vl = Double.parseDouble(split[5]);
        
        bform = new XRectangle(new Vector3D(dx, dy, dz), w, h);
        bform.setVolumeValue(vl);
      }
      return bform;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  public double getXRectangleWidth() {
    return this.width;
  }
  
  public double getXRectangleHeight() {
    return this.height;
  }
  
  private double volumeValue = 1.0;
  public double getVolumeValue() {
    return this.volumeValue;
  }
  
  public void setVolumeValue(double vlm) {
    this.volumeValue = vlm;
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "normal", normal },
      { "width", Double.valueOf(width) },
      { "height", Double.valueOf(height) }
    };
    writer.writeFields(fields);
  }
  
}

/**
// Backwall: camera to X , wall X=5
XRectangle wall = new XRectangle(new Vector3D(-1, 0, 0), 52.0, 17.0);
wall.setTransform(Matrix4.translation(45, 0, 10.25));
 */
