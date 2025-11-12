/* $Id: RGB.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import java.awt.Color;


/**
 * A class to encapsulate a high precision wavelength-dependent nonnegative
 * value.
 * The wavelength-dependent nonnegative value is defined by its
 * samples at the <b>R</b>ed, <b>G</b>reen and <b>B</b>lue wavelengths.
 * The samples can be any nonnegative double value; in particular,
 * {@link Double#POSITIVE_INFINITY} is allowed.  Note that for a color
 * the samples should range from 0 to 1 (see {@link #getColor}).
 *
 * @see Color
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public class RGB implements java.io.Serializable, Writable
{
  /**
   * The <i>red</i> sample.
   * @serial
   */
  private double r;
  
  /**
   * The <i>green</i> sample.
   * @serial
   */
  private double g;
  
  /**
   * The <i>blue</i> sample.
   * @serial
   */
  private double b;
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * black color.
   */
  public static final RGB black   = new RGB(0, 0, 0);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * blue color.
   */
  public static final RGB blue    = new RGB(0, 0, 1);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * cyan color.
   */
  public static final RGB cyan    = new RGB(0, 1, 1);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * green color.
   */
  public static final RGB green   = new RGB(0, 1, 0);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * magenta color.
   */
  public static final RGB magenta = new RGB(1, 0, 1);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * red color.
   */
  public static final RGB red     = new RGB(1, 0, 0);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * yellow color.
   */
  public static final RGB yellow  = new RGB(1, 1, 0);
  
  /**
   * The <code>RGB</code> wavelength-dependent value corresponding to the
   * white color.
   */
  public static final RGB white   = new RGB(1, 1, 1);
  
  /**
   * Creates an <code>RGB</code> wavelength-dependent value initialized
   * with the specified red, green, and blue samples.
   *
   * @param r a nonnegative double value corresponding to
   *          the red sample.
   * @param g a nonnegative double value corresponding to
   *          the green sample.
   * @param b a nonnegative double value corresponding to
   *          the blue sample.
   */
  public RGB(double r, double g, double b)
  {
    super();
    
    if ((r < 0) || (g < 0) || (b < 0) ||
    Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b))
    {
      throw new IllegalArgumentException("RGB parameter outside of expected range");
    }
    
    this.r = r;
    this.g = g;
    this.b = b;
  }
  
  /**
   * Creates an <code>RGB</code> constant value initialized with the
   * specified double value.
   *
   * @param c a nonnegative double value.
   */
  public RGB(double c)
  {
    this(c, c, c);
  }
  
  /**
   * Creates an <code>RGB</code> wavelength-dependent value corresponding
   * to the specified <code>Color</code>.
   *
   * @param c the color to be represented by the <code>RGB</code> object.
   * @see #getColor
   */
  public RGB(Color c)
  {
    this(((double) c.getRed()) / 255,
      ((double) c.getGreen()) / 255,
    ((double) c.getBlue()) / 255);
  }
  
  /**
   * Creates an <code>RGB</code> object from the specified
   * <code>ObjectReader</code>.
   *
   * @param reader the <code>ObjectReader</code> to read the
   *        fields from.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    Number[] params = reader.readNumbers(3);
    
    return new RGB(params[0].doubleValue(),
      params[1].doubleValue(),
    params[2].doubleValue());
  }
  
  /**
   * Compares this object to the specified object.
   * The result is <code>true</code> if and only if the argument is not
   * <code>null</code> and is an <code>RGB</code> object that represents
   * the same wavelength-dependent value as this object (i.e. their
   * samples are the same).
   *
   * @param  obj the object to compare with.
   * @return <code>true</code> if the objects are the same;
   *         <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof RGB))
    {
      RGB rgb = (RGB) obj;
      return ((r == rgb.r) && (g == rgb.g) && (b == rgb.b));
    }
    return false;
  }
  
  /**
   * Computes the <i>componentwise sum</i> of the two specified
   * <code>RGB</code> wavelength-dependent values.
   *
   * @param rgb1,&nbsp;rgb2 the <code>RGB</code> wavelength-dependent
   *                        values to sum.
   * @return an instance of <code>RGB</code> that is the componentwise
   *         sum of the two specified <code>RGB</code>
   *         wavelength-dependent values.
   */
  public static RGB sum(RGB rgb1, RGB rgb2)
  {
    return new RGB(rgb1.r + rgb2.r,
      rgb1.g + rgb2.g,
    rgb1.b + rgb2.b);
  }
  
  /**
   * Computes the <i>componentwise product</i> of the two specified
   * <code>RGB</code> wavelength-dependent values.
   *
   * @param rgb1,&nbsp;rgb2 the <code>RGB</code> wavelength-dependent
   *                        values to product.
   * @return an instance of <code>RGB</code> that is the componentwise
   *         product of the two specified <code>RGB</code>
   *         wavelength-dependent values.
   */
  public static RGB product(RGB rgb1, RGB rgb2)
  {
    return new RGB(rgb1.r * rgb2.r,
      rgb1.g * rgb2.g,
    rgb1.b * rgb2.b);
  }
  
  /**
   * Computes the <i>product</i> of the specified <code>RGB</code>
   * wavelength-dependent value with the specified double factor.
   *
   * @param rgb the <code>RGB</code> wavelength-dependent value
   *            to product with.
   * @param f   the double factor.
   * @return an instance of <code>RGB</code> that is the product of the
   *         specified <code>RGB</code> wavelength-dependent value with
   *         the specified double factor.
   */
  public static RGB product(RGB rgb, double f)
  {
    return new RGB(f * rgb.r,
      f * rgb.g,
    f * rgb.b);
  }
  
  /**
   * Computes the <i>componentwise max</i> of the two specified
   * <code>RGB</code> wavelength-dependent values.
   *
   * @param rgb1,&nbsp;rgb2 the <code>RGB</code> values to compare.
   * @return an instance of <code>RGB</code> that is the componentwise max
   *         of the two specified <code>RGB</code> wavelength-dependent
   *         values.
   */
  public static RGB max(RGB rgb1, RGB rgb2)
  {
    return new RGB(Math.max(rgb1.r, rgb2.r),
      Math.max(rgb1.g, rgb2.g),
    Math.max(rgb1.b, rgb2.b));
  }
  
  //////// ADDED by Murat Inan ///////////
  // Skaler ile çarpma
  public RGB multiply(double scalar) {
    return new RGB(
      this.r * scalar,
      this.g * scalar,
      this.b * scalar
    );
  }
  
  // Başka bir RGB ile çarpma (component-wise)
  public RGB multiply(RGB other) {
    return new RGB(
      this.r * other.getR(),
      this.g * other.getG(),
      this.b * other.getB()
    );
  }
  
  // Skaler ile çarpma ve mevcut nesneyi değiştirme (isteğe bağlı)
  public RGB multiplySelf(double scalar) {
    this.r *= scalar;
    this.g *= scalar;
    this.b *= scalar;
    return this;
  }
  
  /**
   * Computes the componentwise sum of this RGB and the specified RGB.
   *
   * @param other the RGB to add to this RGB
   * @return a new RGB instance that is the componentwise sum of this RGB and the specified RGB
   */
  public RGB add(RGB other) {
    return new RGB(this.r + other.getR(),
      this.g + other.getG(),
    this.b + other.getB());
  }
  
  /**
   * İki RGB rengi arasında interpolasyon yapar
   * @param color1 İlk renk
   * @param color2 İkinci renk
   * @param factor Interpolasyon faktörü (0.0 = color1, 1.0 = color2)
   * @return İnterpolasyon sonucu oluşan yeni RGB
   */
  public static RGB interpolate(RGB color1, RGB color2, double factor) {
    // Factor'ı [0,1] aralığına clamp et
    double t = Math.max(0.0, Math.min(1.0, factor));
    
    // Lineer interpolasyon
    double r = color1.getR() + (color2.getR() - color1.getR()) * t;
    double g = color1.getG() + (color2.getG() - color1.getG()) * t;
    double b = color1.getB() + (color2.getB() - color1.getB()) * t;
    
    return new RGB(r, g, b);
  }
  
  /**
   * Instance method olarak da ekleyelim (opsiyonel)
   * @param other Diğer renk
   * @param factor Interpolasyon faktörü (0.0 = this, 1.0 = other)
   * @return İnterpolasyon sonucu
   */
  public RGB interpolate(RGB other, double factor) {
    return RGB.interpolate(this, other, factor);
  }
  /////////////////////
  
  private static double inverse(double d)
  {
    if (d == 0)
    {
      return Double.POSITIVE_INFINITY;
    }
    else
    {
      return 1/d;
    }
  }
  
  /**
   * Computes the <i>componentwise inverse</i> of the specified
   * <code>RGB</code> wavelength-dependent value.
   * The inverse of a zero sample is {@link Double#POSITIVE_INFINITY}.
   *
   * @param rgb the <code>RGB</code> value to inverse.
   * @return an instance of <code>RGB</code> that is the componentwise
   *         inverse of the specified <code>RGB</code>
   *         wavelength-dependent value.
   */
  public static RGB inverse(RGB rgb)
  {
    return new RGB(inverse(rgb.r),
      inverse(rgb.g),
    inverse(rgb.b));
  }
  
  /**
   * Compares this <code>RGB</code> wavelength-dependent value with the
   * specified <code>RGB</code> wavelength-dependent value.
   *
   * @param rgb the <code>RGB</code> value to compare with.
   * @return <code>true</code> if this <code>RGB</code>
   *         wavelength-dependent value is smaller than the specified
   *         <code>RGB</code> wavelength-dependent value;
   *         <code>false</code> otherwise.
   */
  public boolean isSmaller(RGB rgb)
  {
    return (r <= rgb.r) && (g <= rgb.g) && (b <= rgb.b);
  }
  
  /**
   * Returns the <code>Color</code> corresponding to this <code>RGB</code>
   * wavelength-dependent value.
   * Each sample greater than 1 is truncated to 1 as this is the maximal
   * value for a <code>Color</code>'s component.
   *
   * @return a <code>Color</code> object corresponding to this
   *         <code>RGB</code> wavelength-dependent value.
   */
  public Color getColor()
  {
    return new Color(Math.min(1, (float) r),
      Math.min(1, (float) g),
    Math.min(1, (float) b));
  }
  
  // ADDED by Murat Inan
  public double getR() {
    return this.r;
  }
  
  public double getG() {
    return this.g;
  }
  
  public double getB() {
    return this.b;
  }
  
  public String toParametersString() {
    String s = ("" + r + ", " + g + ", " + b + "");
    return s;
  }
  /////////////////////
  
  /**
   * Returns a textual <code>String</code> representation of this
   * <code>RGB</code> object.
   */
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  /**
   * Writes the contents of this <code>RGB</code> object to the specified
   * <code>ObjectWriter</code>.
   *
   * @param writer the <code>ObjectWriter</code> to write the
   *        fields to.
   */
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    writer.write(" " + r + " ; " + g + " ; " + b + " ");
  }
}
