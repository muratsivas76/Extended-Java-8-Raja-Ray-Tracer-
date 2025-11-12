/* $Id: Intersection.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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
import raja.util.List;

import java.util.HashMap;
import java.util.Iterator;


public class Intersection extends CompositeForm implements java.io.Serializable
{
  public Intersection()
  {
    super();
  }
  
  public Intersection(List texturedForms)
  {
    super(texturedForms);
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("texturedForms",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Intersection((List) map.get("texturedForms"));
  }
  
  public TexturedLocalGeometry intersection(Ray r)
  {
    List formsToIntersect = new List();
    
    for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
    {
      TexturedForm currentForm = (TexturedForm) iterForm.next();
      if (! currentForm.contains(r.origin)) {
        formsToIntersect.add(currentForm);
      }
    }
    
    if (formsToIntersect.isEmpty()) {
      TexturedLocalGeometry intersection = null;
      double dist = Double.POSITIVE_INFINITY;
      
      for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
      {
        TexturedForm currentForm = (TexturedForm) iterForm.next();
        TexturedLocalGeometry currentIntersection = currentForm.intersection(r);
        
        if (currentIntersection != null) {
          double currentDist = Point3D.distanceSq(r.origin, currentIntersection);
          if (currentDist < dist) {
            intersection = currentIntersection;
            dist = currentDist;
          }
        }
      }
      
      if (intersection == null) {
        return null;
      }
      if (contains(intersection)) {
        return intersection;
      }
      
      List newFormsToIntersect = new List();
      
      for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
      {
        TexturedForm currentForm = (TexturedForm) iterForm.next();
        if (! currentForm.contains(intersection)) {
          newFormsToIntersect.add(currentForm);
        }
      }
      Ray newR = new Ray(intersection, r.direction);
      return recursiveIntersection(newR, newFormsToIntersect);
    }
    return recursiveIntersection(r, formsToIntersect);
  }
  private TexturedLocalGeometry recursiveIntersection(Ray r, List formsToIntersect)
  {
    TexturedLocalGeometry intersection = null;
    double dist = 0;
    
    for(Iterator iterForm = formsToIntersect.iterator() ; iterForm.hasNext() ;)
    {
      TexturedForm currentForm = (TexturedForm) iterForm.next();
      TexturedLocalGeometry currentIntersection = currentForm.intersection(r);
      
      if (currentIntersection == null)
      {
        return null;
      }
      else
      {
        double currentDist = Point3D.distanceSq(r.origin, currentIntersection);
        if (currentDist > dist)
        {
          intersection = currentIntersection;
          dist = currentDist;
        }
      }
    }
    
    List newFormsToIntersect = new List();
    
    for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
    {
      TexturedForm currentForm = (TexturedForm) iterForm.next();
      if (! currentForm.contains(intersection)) {
        newFormsToIntersect.add(currentForm);
      }
    }
    
    if (newFormsToIntersect.isEmpty()) {
      return intersection;
    }
    else {
      Ray newR = new Ray(intersection, r.direction);
      return recursiveIntersection(newR, newFormsToIntersect);
    }
  }
  
  public boolean intersects(LightRay r)
  {
    if (strictlyContains(r.origin)) {
      for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
      {
        TexturedForm currentForm = (TexturedForm) iterForm.next();
        if (currentForm.intersects(r)) {
          return true;
        }
      }
      return false;
    }
    else {
      return super.intersects(r);
    }
  }
  
  public boolean contains(Point3D p)
  {
    for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
    {
      TexturedForm currentForm = (TexturedForm) iterForm.next();
      if (! currentForm.contains(p)) {
        return false;
      }
    }
    return true;
  }
  public boolean strictlyContains(Point3D p)
  {
    for(Iterator iterForm = formIterator() ; iterForm.hasNext() ;)
    {
      TexturedForm currentForm = (TexturedForm) iterForm.next();
      if (! currentForm.strictlyContains(p)) {
        return false;
      }
    }
    return true;
  }
}
