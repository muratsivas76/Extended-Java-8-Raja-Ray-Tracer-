/* $Id: List.java,v 1.1.1.1 2001/01/08 23:10:15 gregoire Exp $
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

import raja.io.*;
import java.util.*;


public class List extends LinkedList implements java.io.Serializable, Writable
{
  public List()
  {
    super();
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Iterator iter = iterator();
    writer.write("[\n");
    writer.incrementIndent();
    int maxIndex = size() - 1;
    for (int i = 0; i <= maxIndex; i++) {
      Object obj = iter.next();
      writer.writeIndent();
      writer.writeObject(obj);
      if (i < maxIndex)
      {
        writer.write(",");
      }
      writer.write("\n");
    }
    writer.decrementIndent();
    writer.writeIndent();
    writer.write("]");
  }
}
