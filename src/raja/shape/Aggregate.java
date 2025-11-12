/* $Id: Aggregate.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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
import raja.util.DirectedGraph;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Comparator;


public class Aggregate implements Solid, java.io.Serializable, Writable
{
  private List solids;
  private DirectedGraph priorities;
  
  public Aggregate()
  {
    solids = new List();
    priorities = new DirectedGraph();
  }
  
  // ADDED by Murat Inan
  public raja.util.List getList() {
    return solids;
  }
  
  public Aggregate(List solids, DirectedGraph priorities)
  {
    for(Iterator iterEdges = priorities.edges() ; iterEdges.hasNext() ;)
    {
      DirectedGraph.Edge currentEdge = (DirectedGraph.Edge) iterEdges.next();
      
      if ((currentEdge.getSource() == currentEdge.getDest()) ||
      priorities.hasEdge(currentEdge.getDest(), currentEdge.getSource()))
      {
        //throw new IllegalArgumentException(priorities.toString());
      }
    }
    
    this.solids = solids;
    this.priorities = priorities;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("solids", null);
    map.put("priorities", null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Aggregate((List) map.get("solids"),
    (DirectedGraph) map.get("priorities"));
  }
  
  public void addSolid(Solid solid)
  {
    solids.add(solid);
  }
  public void addPriority(Solid major, Solid minor)
  {
    if ((major == minor) ||
    priorities.hasEdge(minor, major))
    {
      //throw new IllegalArgumentException();
    }
    priorities.addEdge(major, minor);
  }
  
  private boolean hasPriority(Solid major, Solid minor)
  {
    return priorities.hasEdge(major, minor);
  }
  private boolean hasPriority(Solid major, Set minors)
  {
    return priorities.hasEdges(major, minors);
  }
  private Solid mainSolid(Set set)
  {
    if (set.isEmpty()) {
      return null;
    }
    
    Iterator iterSolid = set.iterator();
    Solid main = (Solid) iterSolid.next();
    
    while (iterSolid.hasNext())
    {
      Solid currentSolid = (Solid) iterSolid.next();
      if (hasPriority(currentSolid, main)) {
        main = currentSolid;
      }
    }
    
    set.remove(main);
    
    if (hasPriority(main, set)) {
      set.add(main);
      return main;
    }
    else
    {
      return main;
      //throw new IllegalArgumentException("Could not determine main solid of: " + set  + " with priorities: " + priorities);
    }
  }
  
  public SolidLocalGeometry intersection(Ray r)
  {
    class Point3DComparator implements Comparator
    {
      private Point3D p;
      
      Point3DComparator(Point3D p)
      {
        this.p = p;
      }
      
      public int compare(Object o1, Object o2)
      {
        SolidLocalGeometry slg1 = (SolidLocalGeometry) o1;
        SolidLocalGeometry slg2 = (SolidLocalGeometry) o2;
        
        double d1 = Point3D.distanceSq(p, slg1);
        double d2 = Point3D.distanceSq(p, slg2);
        
        if (d1 < d2) {
          return -1;
        }
        else if (d1 > d2) {
          return 1;
        }
        return (slg1.hashCode() - slg2.hashCode());
      }
    }
    
    TreeMap Intersections = new TreeMap(new Point3DComparator(r.origin));
    
    for(Iterator iterSolid = solids.iterator() ; iterSolid.hasNext() ;)
    {
      Solid currentSolid = (Solid) iterSolid.next();
      SolidLocalGeometry currentIntersection = currentSolid.intersection(r);
      if (currentIntersection != null) {
        Intersections.put(currentIntersection, currentSolid);
      }
    }
    
    if (Intersections.isEmpty()) {
      return null;
    }
    
    SolidLocalGeometry first = (SolidLocalGeometry) Intersections.firstKey();
    
    Set formsIn = new HashSet();
    
    for(Iterator iterSolid = solids.iterator() ; iterSolid.hasNext() ;)
    {
      Solid currentSolid = (Solid) iterSolid.next();
      if (currentSolid.strictlyContains(first)) {
        formsIn.add(currentSolid);
      }
    }
    
    Solid firstSolid = (Solid) Intersections.get(first);
    
    if (! firstSolid.strictlyContains(first)) {
      if (Vector3D.dotProduct(r.direction, first.getNormal()) > 0) {
        formsIn.add(firstSolid);
      }
    }
    
    if (formsIn.isEmpty()) {
      return first;
    }
    
    Solid mainSolid = mainSolid(formsIn);
    
    SolidLocalGeometry validIntersection = null;
    
    for(Iterator iterIntersections = Intersections.entrySet().iterator() ; iterIntersections.hasNext() ;)
    {
      Map.Entry currentEntry = (Map.Entry) iterIntersections.next();
      SolidLocalGeometry currentIntersection = (SolidLocalGeometry) currentEntry.getKey();
      Solid currentSolid = (Solid) currentEntry.getValue();
      
      if (currentSolid == mainSolid) {
        
        validIntersection = currentIntersection;
        
        if (validIntersection.inVolumeUndefined()) {
          throw new Error("Error in Aggregate: inVolume is undefined");
        }
        
        if (validIntersection.outVolumeUndefined()) {
          // The ray leaves mainSolid at this intersection
          formsIn.remove(mainSolid);
          
          Set newFormsIn = new HashSet();
          
          for(Iterator iterSolid = formsIn.iterator() ; iterSolid.hasNext() ;)
          {
            Solid newCurrentSolid = (Solid) iterSolid.next();
            if (newCurrentSolid.strictlyContains(validIntersection)) {
              newFormsIn.add(newCurrentSolid);
            }
          }
          
          if (! newFormsIn.isEmpty()) {
            Solid newMainSolid = mainSolid(newFormsIn);
            validIntersection.setOutVolume(newMainSolid);
          }
        }
        break;
      }
      else if (hasPriority(currentSolid, mainSolid)) {
        // The ray enters currentSolid at this intersection
        
        validIntersection = currentIntersection;
        
        if (validIntersection.inVolumeUndefined()) {
          throw new Error("Error in Aggregate: inVolume is undefined");
        }
        
        if (! validIntersection.outVolumeUndefined()) {
          throw new Error("Error in Aggregate: outVolume should be undefined");
        }
        
        validIntersection.setOutVolume(mainSolid);
        break;
      }
      else if (! hasPriority(mainSolid, currentSolid)) {
        //    throw new IllegalArgumentException("Unknown priority between " + mainSolid + " and " + currentSolid);
      }
      else
      {
        formsIn.add(currentSolid);
      }
    }
    
    return validIntersection;
  }
  
  public boolean intersects(LightRay r)
  {
    Point3D intersection = intersection(r);
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
    for(Iterator iterSolid = solids.iterator() ; iterSolid.hasNext() ;)
    {
      Solid currentSolid = (Solid) iterSolid.next();
      if (currentSolid.contains(p)) {
        return true;
      }
    }
    return false;
  }
  public boolean strictlyContains(Point3D p)
  {
    for(Iterator iterSolid = solids.iterator() ; iterSolid.hasNext() ;)
    {
      Solid currentSolid = (Solid) iterSolid.next();
      if (currentSolid.strictlyContains(p)) {
        return true;
      }
    }
    return false;
  }
  
  public double refractiveIndex(Point3D p)
  {
    Set formsIn = new HashSet();
    
    for(Iterator iterSolid = solids.iterator() ; iterSolid.hasNext() ;)
    {
      Solid currentSolid = (Solid) iterSolid.next();
      if (currentSolid.contains(p)) {
        formsIn.add(currentSolid);
      }
    }
    
    Solid mainSolid = mainSolid(formsIn);
    return mainSolid.refractiveIndex(p);
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "solids", solids },
    { "priorities", priorities } };
    writer.writeFields(fields);
  }
}
