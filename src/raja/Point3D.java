/* $Id: Point3D.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

package raja;

import raja.io.*;


/**
 * The <code>Point3D</code> class defines a high precision point in the 3
 * dimensional space, given by its 3D coordinates.  This class provides
 * most of the classical operations on points (translation, distance...).
 *
 * @see java.awt.geom.Point2D
 * @see Vector3D
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public class Point3D implements java.io.Serializable, Writable
{
  /**
   * The <i>x</i> coordinate.
   * @serial
   */
  public double x;
  
  /**
   * The <i>y</i> coordinate.
   * @serial
   */
  public double y;
  
  /**
   * The <i>z</i> coordinate.
   * @serial
   */
  public double z;
  
  /**
   * Creates a <code>Point3D</code> object initialized with the specified
   * 3D coordinates.
   *
   * @param x,&nbsp;y,&nbsp;z the coordinates to which to set the newly
   *                          constructed <code>Point3D</code>.
   */
  public Point3D(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  /**
   * Initializes a newly created <code>Point3D</code> object so that it
   * represents the same 3 dimensional point as the argument.  In other
   * words, the newly created <code>Point3D</code> is a copy of the
   * specified <code>Point3D</code>.
   *
   * @param p a <code>Point3D</code> object.
   */
  public Point3D(Point3D p)
  {
    this(p.x, p.y, p.z);
  }
  
  /**
   * Initializes a newly created <code>Point3D</code> object so that it
   * represents the <i>translation</i> of the specified
   * <code>Point3D</code> by the specified <code>Vector3D</code>.
   *
   * @param p a <code>Point3D</code> object.
   * @param v a <code>Vector3D</code> object.
   */
  public Point3D(Point3D p, Vector3D v)
  {
    this(p.x + v.x,
      p.y + v.y,
    p.z + v.z);
  }
  
  /**
   * Creates a <code>Point3D</code> object from the specified
   * <code>ObjectReader</code>.
   *
   * @param reader the <code>ObjectReader</code> to read the
   *        fields from.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    Number[] params = reader.readNumbers(3);
    
    return new Point3D(params[0].doubleValue(),
      params[1].doubleValue(),
    params[2].doubleValue());
  }
  
  /**
   * Translates this <code>Point3D</code> by the specified
   * <code>Vector3D</code>.
   *
   * @param v the <code>Vector3D</code> to translate this
   *          <code>Point3D</code> by.
   */
  public void translate(Vector3D v)
  {
    x += v.x;
    y += v.y;
    z += v.z;
  }
  
  /**
   * Computes the <i>distance</i> between the two specified
   * <code>Point3D</code>.
   *
   * @param p1,&nbsp;p2 the two <code>Point3D</code> to compute the
   *                    distance between.
   * @return the distance between the two specified <code>Point3D</code>.
   */
  public static double distance(Point3D p1, Point3D p2)
  {
    return Math.sqrt(distanceSq(p1, p2));
  }
  
  /**
   * Computes the <i>square of the distance</i> between the two specified
   * <code>Point3D</code>.
   *
   * @param p1,&nbsp;p2 the two <code>Point3D</code> to compute the
   *                    square of the distance between.
   * @return the square of the distance between the two specified
   *         <code>Point3D</code>.
   */
  public static double distanceSq(Point3D p1, Point3D p2)
  {
    double dx, dy, dz;
    
    dx = p1.x - p2.x;
    dy = p1.y - p2.y;
    dz = p1.z - p2.z;
    
    return (dx*dx + dy*dy + dz*dz);
  }
  
  ///////////////////////
  // ADDED by Murat Inan
  /**
   * Computes the <i>length</i> of this <code>Point3D</code> from origin (0,0,0).
   *
   * @return the length of this <code>Point3D</code> from origin.
   */
  public double length()
  {
    return Math.sqrt(x*x + y*y + z*z);
  }
  
  /**
   * Computes the <i>square of the length</i> of this <code>Point3D</code> from origin (0,0,0).
   *
   * @return the square of the length of this <code>Point3D</code> from origin.
   */
  public double lengthSq()
  {
    return x*x + y*y + z*z;
  }
  //////////////
  
  /**
   * Returns a textual <code>String</code> representation of this
   * <code>Point3D</code> object.
   */
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  /**
   * Writes the contents of this <code>Point3D</code> object to the
   * specified <code>ObjectWriter</code>.
   *
   * @param writer the <code>ObjectWriter</code> to write the
   *        fields to.
   */
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Number[] fields = { new Double(x), new Double(y), new Double(z) };
    writer.writeFields(fields);
  }
}
