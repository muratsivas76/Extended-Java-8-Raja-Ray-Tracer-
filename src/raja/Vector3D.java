/* $Id: Vector3D.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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
 * The <code>Vector3D</code> class defines a high precision vector in the 3
 * dimensional space, given by its 3D coordinates.  This class provides
 * most of the classical operations on vectors (norm, dot product, cross
 * product, projection...).
 *
 * @see Point3D
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public class Vector3D implements java.io.Serializable, Writable
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
   * Creates a <code>Vector3D</code> object initialized with the
   * specified 3D coordinates.
   *
   * @param x,&nbsp;y,&nbsp;z the coordinates to which to set the newly
   *                          constructed <code>Vector3D</code>.
   */
  public Vector3D(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  /**
   * Initializes a newly created <code>Vector3D</code> object so that it
   * represents the same 3 dimensional vector as the argument.  In other
   * words, the newly created <code>Vector3D</code> is a copy of the
   * specified <code>Vector3D</code>.
   *
   * @param v a <code>Vector3D</code> object.
   */
  public Vector3D(Vector3D v)
  {
    this(v.x, v.y, v.z);
  }
  
  /**
   * Initializes a newly created <code>Vector3D</code> object so that it
   * represents the vector <i>connecting</i> the two specified 3D points.
   *
   * @param source the source <code>Point3D</code>.
   * @param dest the destination <code>Point3D</code>.
   */
  public Vector3D(Point3D source, Point3D dest)
  {
    this(dest.x - source.x,
      dest.y - source.y,
    dest.z - source.z);
  }
  
  /**
   * Creates a <code>Vector3D</code> object from the specified
   * <code>ObjectReader</code>.
   *
   * @param reader the <code>ObjectReader</code> to read the
   *        fields from.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    Number[] params = reader.readNumbers(3);
    
    return new Vector3D(params[0].doubleValue(),
      params[1].doubleValue(),
    params[2].doubleValue());
  }
  
  /**
   * Computes the <i>norm</i> of this <code>Vector3D</code>.
   *
   * @return the norm of this <code>Vector3D</code>.
   */
  public double norm()
  {
    return Math.sqrt(normSq());
  }
  
  /**
   * Computes the <i>square of the norm</i> of this <code>Vector3D</code>.
   *
   * @return the square of the norm of this <code>Vector3D</code>.
   */
  public double normSq()
  {
    return (x*x + y*y + z*z);
  }
  
  /**
   * Computes a normed version of the specified <code>Vector3D</code>.
   * The norm of the <code>Vector3D</code> argument is assumed to be non
   * zero.
   *
   * @param v a <code>Vector3D</code> object, whose norm is non zero.
   * @return a normed version of the specified <code>Vector3D</code>.
   */
  public static Vector3D normalization(Vector3D v)
  {
    return product(v, 1.0 / v.norm());
  }
  
  /**
   * ADDED by Murat Inan
   * Returns a new vector that is the negation of this vector.
   * Each component of the resulting vector is the negative of the corresponding component in this vector.
   *
   * @return A new Vector3D representing the negation of this vector
   */
  public Vector3D negation() {
    return new Vector3D(-x, -y, -z);
  }
  
  // ADDED by Murat Inan
  public static Vector3D difference(Vector3D v1, Vector3D v2) {
    return new Vector3D(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
  }
  
  /**
   * Returns the magnitude (length) of this vector.
   * This is equivalent to the norm of the vector.
   *
   * ADDED by Murat Inan
   * @return the magnitude of this vector.
   */
  public double magnitude() {
    return Math.sqrt(x * x + y * y + z * z);
  }
  
  // ADDED by Murat Inan, This give more transparan shapes
  public Vector3D normalization3()
  {
    double nm = 1.0 / norm();
    Vector3D vc = new Vector3D(nm * x, nm * y, nm * z);
    return product(vc, nm);
  }
  
  // ADDED by Murat Inan
  public Vector3D normalization()
  {
    double n = norm();
    if (n == 0) return new Vector3D(0, 0, 0); // avoid div by zero
      return new Vector3D(x / n, y / n, z / n);
  }
  
  // ADDED by Murat Inan
  public Vector3D normalization2()
  {
    double magnitude = norm();
    if (magnitude <= 1e-10) { // floating point tolerance
      return new Vector3D(0, 0, 0);
    }
    double scale = 1.0 / magnitude;
    return new Vector3D(x * scale, y * scale, z * scale);
  }
  
  /**
   * Computes the <i>dot product</i> of the two specified
   * <code>Vector3D</code>.
   *
   * @param v1,&nbsp;v2 the two <code>Vector3D</code> to dot product.
   * @return the dot product of the two specified
   * <code>Vector3D</code>.
   */
  public static double dotProduct(Vector3D v1, Vector3D v2)
  {
    return ((v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z));
  }
  
  /**
   * Computes the <i>product</i> of the specified <code>Vector3D</code>
   * with the specified double factor.
   *
   * @param v the <code>Vector3D</code> product with.
   * @param f the double factor.
   * @return an instance of <code>Vector3D</code> that is the product of
   * the specified <code>Vector3D</code> with the specified double factor.
   */
  public static Vector3D product(Vector3D v, double f)
  {
    return new Vector3D(f * v.x,
      f * v.y,
    f * v.z);
  }
  
  /**
   * Computes the <i>cross product</i> of the two specified
   * <code>Vector3D</code>.
   *
   * @param v1,&nbsp;v2 the two <code>Vector3D</code> to cross product.
   * @return an instance of <code>Vector3D</code> that is the cross
   * product of the two specified <code>Vector3D</code>.
   */
  public static Vector3D crossProduct(Vector3D v1, Vector3D v2)
  {
    return new Vector3D((v1.y * v2.z) - (v1.z * v2.y),
      (v1.z * v2.x) - (v1.x * v2.z),
    (v1.x * v2.y) - (v1.y * v2.x));
  }
  
  /**
   * Computes the <i>sum</i> of the two specified <code>Vector3D</code>.
   *
   * @param v1,&nbsp;v2 the two <code>Vector3D</code> to sum.
   * @return an instance of <code>Vector3D</code> that is the sum of the
   * two specified <code>Vector3D</code>.
   */
  public static Vector3D sum(Vector3D v1, Vector3D v2)
  {
    return new Vector3D((v1.x + v2.x),
      (v1.y + v2.y),
    (v1.z + v2.z));
  }
  
  /**
   * Computes the <i>opposite</i> of the specified <code>Vector3D</code>.
   *
   * @param v the <code>Vector3D</code> to compute the opposite of.
   * @return an instance of <code>Vector3D</code> that is the
   *         opposite of the specified <code>Vector3D</code>.
   */
  public static Vector3D opposite(Vector3D v)
  {
    return new Vector3D(- v.x,
      - v.y,
    - v.z);
  }
  
  /**
   * Computes the <i>projection</i> of the specified <code>Vector3D</code>
   * on the hyperplane given by the specified normal.
   *
   * @param v the <code>Vector3D</code> project.
   * @param n the normal of the hyperplane to project the specified
   *        <code>Vector3D</code> on.
   * @return an instance of <code>Vector3D</code> that is the projection
   * of the specified <code>Vector3D</code> on the hyperplane given by the
   * specified normal.
   */
  public static Vector3D projection(Vector3D v, Vector3D n)
  {
    double sc = dotProduct(v, n);
    return Vector3D.sum(v, Vector3D.product(n, -sc));
  }
  
  /**
   * Returns a textual <code>String</code> representation of this
   * <code>Vector3D</code> object.
   */
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  /**
   * Returns the squared length (norm squared) of this vector.
   * This is equivalent to x*x + y*y + z*z.
   *
   * ADDED by Murat Inan
   * @return the squared length of this vector.
   */
  public double lengthSquared() {
    return x * x + y * y + z * z;
  }
  
  /**
   * Writes the contents of this <code>Vector3D</code> object to the
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
