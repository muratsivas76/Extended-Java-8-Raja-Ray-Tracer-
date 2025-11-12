/* $Id: BasicForm.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public abstract class BasicForm implements Form
{
  protected abstract Point3D computeIntersection(Ray r);
  protected abstract Vector3D computeNormal(Point3D p);
  protected abstract boolean exactlyContains(Point3D p);
  protected abstract boolean exactlyStrictlyContains(Point3D p);
  
  // ADDED by Murat Inan
  public abstract void setTransform(Matrix4 mtrx);
  public abstract Matrix4 getTransform();
  public abstract String getUsageInformation();
  public abstract BasicForm getInstance(String info);
  public abstract String toExampleString();
  public abstract double getVolumeValue();
  public abstract void setVolumeValue(double vlm);
  ////////////////////
  
  public LocalGeometry intersection(Ray r)
  {
    class BasicLocalGeometry extends LocalGeometry
    {
      BasicForm form;
      
      BasicLocalGeometry(Point3D p, BasicForm f)
      {
        super(p);
        this.form = f;
      }
      
      AcneCorrection getAcneCorrection()
      {
        return new AcneCorrection() {
          BasicForm getBasicForm()
          {
            return form;
          }
        };
      }
      public Vector3D getNormal()
      {
        return form.computeNormal(this);
      }
    }
    
    Point3D intersection = computeIntersection(r);
    
    if (intersection == null) {
      return null;
    }
    else {
      return new BasicLocalGeometry(intersection, this);
    }
  }
  
  public boolean intersects(LightRay r)
  {
    Point3D intersection = computeIntersection(r);
    if (intersection != null) {
      double d = Point3D.distance(r.origin, intersection);
      if (d < r.distance) {
        return true;
      }
    }
    return false;
  }
  
  public boolean contains(Point3D p)
  {
    if (hasLG(p)) {
      return true;
    }
    return exactlyContains(p);
  }
  public boolean strictlyContains(Point3D p)
  {
    if (hasLG(p)) {
      return false;
    }
    return exactlyStrictlyContains(p);
  }
  
  /**
   * Returns the point on the ray corresponding to the smallest positive solution
   * to the equation a*X^2 + b*X + c = 0.
   * @param a, b, c the coefficients of the equation.
   * @param r the ray.
   * @return the point r.origin + t*r.direction where t is the smallest X > 0 such that a*X^2 + b*X + c = 0 if there is any;
   *	       <code>null</code> otherwise.
   */
  protected Point3D solve2ndOrder(double a, double b, double c, Ray r)
  {
    if (a == 0) {
      if (b != 0) {
        double t = (- c) / b;
        if (t > 0) {
          Point3D intersection = new Point3D(r.origin, Vector3D.product(r.direction, t));
          if (isValid(intersection, r)) {
            return intersection;
          }
        }
      }
      return null;
    }
    
    double det = b*b - 4 * a *c;
    
    if (det < 0) {
      return null;
    }
    
    double sqrtDet = Math.sqrt(det);
    double tMin, tMax;
    
    if (a < 0) {
      tMin = (-b + Math.sqrt(det)) / (2 * a);
      tMax = (-b - Math.sqrt(det)) / (2 * a);
    }
    else {
      tMin = (-b - Math.sqrt(det)) / (2 * a);
      tMax = (-b + Math.sqrt(det)) / (2 * a);
    }
    if (tMax <= 0) {
      return null;
    }
    
    if (tMin > 0) {
      Point3D intersection = new Point3D(r.origin, Vector3D.product(r.direction, tMin));
      if (isValid(intersection, r)) {
        return intersection;
      }
    }
    
    Point3D intersection = new Point3D(r.origin, Vector3D.product(r.direction, tMax));
    if (isValid(intersection, r)) {
      return intersection;
    }
    return null;
  }
  
  private boolean isValid(Point3D intersection, Ray r)
  {
    if (r.origin instanceof LocalGeometry) {
      LocalGeometry lg = (LocalGeometry) r.origin;
      if (lg.getAcneCorrection().getBasicForm() == this) {
        double dot1 = Vector3D.dotProduct(computeNormal(lg), r.direction);
        double dot2 = Vector3D.dotProduct(computeNormal(intersection), r.direction);
        if ((dot1 * dot2) > 0) {
          return false;
        }
      }
    }
    return true;
  }
  
  protected boolean hasLG(Point3D p)
  {
    if (p instanceof LocalGeometry) {
      if (((LocalGeometry) p).getAcneCorrection().getBasicForm() == this) {
        return true;
      }
    }
    return false;
  }
}
