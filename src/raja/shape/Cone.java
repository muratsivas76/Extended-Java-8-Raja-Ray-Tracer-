/* $Id: Cone.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class Cone extends BasicForm implements java.io.Serializable, Writable
{
  protected Point3D origin;
  protected Vector3D direction;   // normed vector
  protected double angle;   // Faut qu'on trouve un truc pour virer cette daube qui ne sert a rien
  protected double k;   // cos(angle)
  
  public Cone(Point3D origin, Vector3D direction, double angle)
  {
    this.origin = origin;
    this.direction = Vector3D.normalization(direction);
    this.angle = angle;
    this.k = Math.cos(angle);
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("origin",null);
    map.put("direction",null);
    map.put("angle",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Cone((Point3D) map.get("origin"),
      (Vector3D) map.get("direction"),
    ((Number) map.get("angle")).doubleValue());
  }
  
  @Override
  public Point3D computeIntersection(Ray r)
  {
    double a, b, c;
    
    Vector3D alpha = new Vector3D(origin, r.origin);
    Vector3D beta = r.direction;
    
    double scAlpha = Vector3D.dotProduct(alpha, direction);
    double scBeta = Vector3D.dotProduct(beta, direction);
    double scAlphaBeta = Vector3D.dotProduct(alpha, beta);
    double kSquare = k*k;
    
    a = scBeta*scBeta - kSquare;
    b = 2 * (scAlpha*scBeta - (kSquare * scAlphaBeta));
    c = scAlpha*scAlpha - (kSquare * alpha.normSq());
    
    return solve2ndOrder(a, b, c, r);
  }
  @Override
  public boolean exactlyContains(Point3D p)
  {
    Vector3D originP = new Vector3D(origin, p);
    return (Math.abs(Vector3D.dotProduct(originP, direction)) >= (k * originP.norm()));
  }
  @Override
  public boolean exactlyStrictlyContains(Point3D p)
  {
    Vector3D originP = new Vector3D(origin, p);
    return (Math.abs(Vector3D.dotProduct(originP, direction)) > (k * originP.norm()));
  }
  @Override
  public Vector3D computeNormal(Point3D p)
  {
    double b = - Math.sqrt(1 / (1 - k*k));
    Vector3D u = Vector3D.normalization(new Vector3D(origin, p));
    double a = - (b * k);
    
    if (Vector3D.dotProduct(u, direction) < 0) {
      b = -b;
    }
    
    return Vector3D.sum(Vector3D.product(u, a),
    Vector3D.product(direction, b));
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
    return new Cone(new Point3D(0, 0, 0), new Vector3D(0, 0, 1), 1.0);
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
    { "angle", new Double(angle) } };
    writer.writeFields(fields);
  }
}
