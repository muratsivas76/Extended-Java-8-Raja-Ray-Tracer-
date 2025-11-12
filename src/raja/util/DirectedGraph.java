/* $Id: DirectedGraph.java,v 1.1.1.1 2001/01/08 23:10:15 gregoire Exp $
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

package raja.util;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import raja.io.*;


public class DirectedGraph implements java.io.Serializable, Writable
{
  public class Edge implements java.io.Serializable, Writable
  {
    private Object source;
    private Object dest;
    
    public Edge(Object source, Object dest)
    {
      this.source = source;
      this.dest = dest;
    }
    
    public Object getSource()
    {
      return source;
    }
    public Object getDest()
    {
      return dest;
    }
    
    public String toString()
    {
      return ObjectWriter.toString(this);
    }
    public void write(ObjectWriter writer) throws java.io.IOException
    {
      writer.writeIndent();
      writer.writeObject(source);
      writer.write(" >> ");
      writer.writeObject(dest);
    }
  }
  
  private Map postSets;
  
  public DirectedGraph()
  {
    postSets = new HashMap();
  }
  
  public void addEdge(Object source, Object dest)
  {
    Set post = (Set) postSets.get(source);
    
    if (post == null) {
      post = new HashSet();
      postSets.put(source, post);
    }
    post.add(dest);
  }
  public boolean hasEdge(Object source, Object dest)
  {
    Set post = (Set) postSets.get(source);
    return ((post != null) && post.contains(dest));
  }
  public boolean hasEdges(Object source, Collection dest)
  {
    if (dest.isEmpty())
    {
      return true;
    }
    
    Set post = (Set) postSets.get(source);
    return ((post != null) && post.containsAll(dest));
  }
  public Iterator edges()
  {
    class EdgeIterator implements Iterator
    {
      private boolean hasNext;
      private Object currentSource;
      Iterator iterPosts;
      Iterator iterSuccessors;
      
      EdgeIterator()
      {
        iterPosts = postSets.entrySet().iterator();
        init();
      }
      private void init()
      {
        hasNext = iterPosts.hasNext();
        
        if (hasNext)
        {
          Map.Entry currentPost = (Map.Entry) iterPosts.next();
          currentSource = currentPost.getKey();
          iterSuccessors = ((Set) currentPost.getValue()).iterator();
        }
      }
      
      public boolean hasNext()
      {
        return hasNext;
      }
      public Object next()
      {
        if (! hasNext)
        {
          throw new java.util.NoSuchElementException();
        }
        
        Edge e = new Edge(currentSource, iterSuccessors.next());
        if (! (iterSuccessors.hasNext()))
        {
          init();
        }
        return e;
      }
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    }
    
    return new EdgeIterator();
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    writer.write("[[\n");
    writer.incrementIndent();
    
    boolean putComma = false;
    
    for(Iterator iterEdges = edges() ; iterEdges.hasNext() ;)
    {
      Edge currentEdge = (Edge) iterEdges.next();
      if (putComma)
      {
        writer.write(",\n");
      }
      writer.writeIndent();
      writer.writeObject(currentEdge);
      putComma = true;
    }
    
    writer.decrementIndent();
    writer.writeIndent();
    writer.write("]]");
  }
}
