/* $Id: LightRay.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


/**
 * A class to represent rays that originate from a point and head for a
 * light source. The <code>LightRay</code> class extends the
 * <code>Ray</code> class with two fields:
 * <ul>
 * <li> the light intensity corresponding to the illumination of the point
 *      by the light source.
 * <li> the distance between the point and the light source.
 * </ul>
 *
 * @see Ray
 * @see raja.light.LightSource
 *
 * @author Emmanuel Fleury
 * @author Grégoire Sutre
 */
public class LightRay extends Ray
{
  /**
   * The <i>light intensity</i> of the light ray.
   * @serial
   */
  public RGB light;
  
  /**
   * The <i>distance</i> between the ray's origin and the light source.
   * @serial
   */
  public double distance;
  
  /**
   * Creates a <code>LightRay</code> object initialized with the
   * specified origin, direction, light intensity and distance.
   *
   * @param origin    the origin of the newly constructed
   *                  <code>LightRay</code>.
   * @param direction the direction of the newly constructed
   *                  <code>LightRay</code>.
   * @param light     the light intensity of the newly constructed
   *                  <code>LightRay</code>.
   * @param distance  the distance between the ray's origin and the light
   *                  source it heads for.
   */
  public LightRay(Point3D origin, Vector3D direction,
  RGB light, double distance)
  {
    super(origin, direction);
    this.light = light;
    this.distance = distance;
  }
  
  /**
   * Creates a <code>LightRay</code> object initialized with the
   * specified origin, destination and light intensity.  The destination
   * should be the location of the light source.  The direction and
   * distance of the newly constructed <code>LightRay</code> are computed
   * from this location.
   *
   * @param origin      the origin of the newly constructed
   *                    <code>LightRay</code>.
   * @param destination the location of the light source.
   * @param light     the light intensity of the newly constructed
   *                  <code>LightRay</code>.
   */
  public LightRay(Point3D origin, Point3D destination, RGB light)
  {
    super(origin, destination);
    this.light = light;
    distance = Point3D.distance(origin, destination);
  }
}
