/* $Id: Sphere.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class Sphere extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D center;
  protected double radius;
  
  public Sphere(Point3D center, double radius)
  {
    this.center = center;
    this.radius = radius;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    map.put("center",null);
    map.put("radius",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Sphere((Point3D) map.get("center"),
    ((Number) map.get("radius")).doubleValue());
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    double a, b, c;
    
    Vector3D alpha = new Vector3D(center, r.origin);
    Vector3D beta = r.direction;
    
    a = beta.normSq();
    b = 2 * Vector3D.dotProduct(alpha, beta);
    c = alpha.normSq() - radius*radius;
    
    return solve2ndOrder(a, b, c, r);
  }
  @Override
  public boolean exactlyContains(Point3D p)
  {
    return (Point3D.distance(p, center) <= radius);
  }
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    return (Point3D.distance(p, center) < radius);
  }
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    Vector3D result = new Vector3D(center, p);
    return Vector3D.normalization(result);
  }
  
  // ADDED by Murat Inan
  @Override
  public void setTransform(Matrix4 mtrx) {
    return;
  }
  
  @Override
  public Matrix4 getTransform() {
    return new Matrix4();
  }
  ////////////////////
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    return "";
  }
  
  @Override
  public String toExampleString() {
    return "null";
  }
  
  @Override
  public BasicForm getInstance(String info)
  {
    return new Sphere(new Point3D(0, 0, 0), 1.0);
  }
  
  private double volumeValue = 1.0;
  public double getVolumeValue() {
    return this.volumeValue;
  }
  
  public void setVolumeValue(double vlm) {
    this.volumeValue = vlm;
  }
  ////////////////
  
  @Override
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "center", center },
    { "radius", new Double(radius) } };
    writer.writeFields(fields);
  }
}
