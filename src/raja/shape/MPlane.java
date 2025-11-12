/* $Id: MPlane.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

public class MPlane extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D center;
  protected Vector3D normal;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  private Matrix4 normalTransform = Matrix4.identity();
  
  // MEVCUT Constructor - sadece normal
  public MPlane(Vector3D normal)
  {
    this.center = new Point3D(0, 0, 0);
    this.normal = Vector3D.normalization(normal);
    updateTransforms();
  }
  
  // YENİ Constructor - center + normal
  public MPlane(Point3D center, Vector3D normal)
  {
    this.center = center;
    this.normal = Vector3D.normalization(normal);
    updateTransforms();
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("normal", null);
    
    reader.readFields(map);
    
    return new MPlane((Vector3D) map.get("normal"));
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
    
    // Işını yerel uzaya al
    Point3D localOrigin = this.inverseTransform.transformPoint(r.origin);
    Vector3D localDirection = this.inverseTransform.transformVector(r.direction);
    
    // Yerel düzlem: always passes through (0,0,0) with normal `this.normal`
    double denom = Vector3D.dotProduct(this.normal, localDirection);
    if (Math.abs(denom) < Ray.EPSILON2) return null;
    
    double tLocal = -Vector3D.dotProduct(this.normal, new Vector3D(new Point3D(0,0,0), localOrigin)) / denom;
    if (tLocal < Ray.EPSILON2) return null;
    
    // tLocal, yerel ışın parametresidir ama dünya t'siyle aynı (çünkü transform lineer)
    // Dolayısıyla doğrudan dünya ışınıyla t'yi kullanabiliriz
    return new Point3D(r.origin, Vector3D.product(r.direction, tLocal));
  }
  
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    // Use normal transform matrix (inverse transpose) for proper normal transformation
    return normalTransform.transformVector(normal).normalization();
  }
  
  @Override
  public boolean exactlyContains(Point3D p)
  {
    Vector3D worldNormal = computeNormal(null);
    Point3D worldPlaneOrigin = transform.transformPoint(this.center);
    
    double distance = Vector3D.dotProduct(worldNormal, new Vector3D(worldPlaneOrigin, p));
    return Math.abs(distance) <= Ray.EPSILON2;
  }
  
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    Vector3D worldNormal = computeNormal(null);
    Point3D worldPlaneOrigin = transform.transformPoint(this.center);
    
    double distance = Vector3D.dotProduct(worldNormal, new Vector3D(worldPlaneOrigin, p));
    return distance < -Ray.EPSILON2;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String PLANE_STR = "Constructor is: MPlane(Vector3D normal)\n" +
    "Examples - last value is volume:\n" +
    "0,0,1,  0\n" +
    "Enter your values after three diyez symbol\n###\n";
    return PLANE_STR;
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
      if (split.length == 4) {
        // Sadece normal: dx,dy,dz,volume
        double dx = Double.parseDouble(split[0]);
        double dy = Double.parseDouble(split[1]);
        double dz = Double.parseDouble(split[2]);
        
        bform = new MPlane(new Vector3D(dx, dy, dz));
        double vl = Double.parseDouble(split[3]);
        bform.setVolumeValue(vl);
        } else if (split.length == 7) {
        // Center + normal: cx,cy,cz, dx,dy,dz,volume
        double cx = Double.parseDouble(split[0]);
        double cy = Double.parseDouble(split[1]);
        double cz = Double.parseDouble(split[2]);
        double dx = Double.parseDouble(split[3]);
        double dy = Double.parseDouble(split[4]);
        double dz = Double.parseDouble(split[5]);
        
        bform = new MPlane(new Point3D(cx, cy, cz), new Vector3D(dx, dy, dz));
        double vl = Double.parseDouble(split[6]);
        bform.setVolumeValue(vl);
      }
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
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "normal", normal }
    };
    writer.writeFields(fields);
  }
  
}

/**
// Floor (only normal):
MPlane floor = new MPlane(new Vector3D(0, 0, 1));

// Backwall (center + normal):
MPlane wall = new MPlane(new Point3D(0, 0, 10), new Vector3D(0, 0, -1));
wall.setTransform(Matrix4.translation(0, 0, 10));
 */
