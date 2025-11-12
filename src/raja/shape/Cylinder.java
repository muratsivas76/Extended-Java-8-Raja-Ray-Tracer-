/* $Id: Cylinder.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class Cylinder extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D origin;
  protected Vector3D direction;   // normed vector
  protected double radius;
  
  public Cylinder(Vector3D direction, Point3D origin, double radius)
  {
    this.direction = Vector3D.normalization(direction);
    this.origin = origin;
    this.radius = radius;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("origin",null);
    map.put("direction",null);
    map.put("radius",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Cylinder((Vector3D) map.get("direction"),
      (Point3D) map.get("origin"),
    ((Number) map.get("radius")).doubleValue());
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    double a, b, c;
    
    Vector3D alpha = Vector3D.projection(new Vector3D(origin, r.origin), direction);
    Vector3D beta = Vector3D.projection(r.direction, direction);
    
    a = beta.normSq();
    b = 2 * Vector3D.dotProduct(alpha, beta);
    c = alpha.normSq() - radius*radius;
    
    return solve2ndOrder(a, b, c, r);
  }
  @Override
  public boolean exactlyContains(Point3D p)
  {
    Vector3D v = Vector3D.projection(new Vector3D(origin, p), direction);
    return (v.norm() <= radius);
  }
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    Vector3D v = Vector3D.projection(new Vector3D(origin, p), direction);
    return (v.norm() < radius);
  }
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    Vector3D v = Vector3D.projection(new Vector3D(origin, p), direction);
    return Vector3D.normalization(v);
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
  
  //ADDED by Murat Inan
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
    return new Cylinder(new Vector3D(0, 0, 1), new Point3D(0, 0, 0), 1.0);
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
    Object[][] fields = { { "origin", origin },
      { "direction", direction },
    { "radius", new Double(radius) } };
    writer.writeFields(fields);
  }
}
