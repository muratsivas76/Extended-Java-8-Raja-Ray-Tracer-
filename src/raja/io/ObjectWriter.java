/* $Id: ObjectWriter.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

package raja.io;

import java.io.*;
import java.util.*;


public class ObjectWriter extends BufferedWriter
{
  private static final String INDENT_BLOCK = "        ";
  private Map labels;
  private int indent;
  
  public ObjectWriter(Writer out)
  {
    super(out);
    labels = new HashMap();
    indent = 0;
  }
  
  public void incrementIndent()
  {
    indent++;
  }
  public void decrementIndent()
  {
    indent--;
  }
  
  public static String toString(Writable obj)
  {
    StringWriter stringWriter = new StringWriter();
    
    try
    {
      ObjectWriter w = new ObjectWriter(stringWriter);
      w.writeObject(obj);
      w.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return stringWriter.toString();
  }
  
  public void writeObject(Object obj) throws IOException
  {
    if (! (obj instanceof Writable))
    {
      throw new NotWritableException(obj);
    }
    
    if (labels.containsKey(obj))
    {
      String label = (String) labels.get(obj);
      
      if (label == null)
      {
        throw new IllegalArgumentException("Raja Internal Error !!!!");
      }
      
      write(label);
    }
    else
    {
      String className = obj.getClass().getName();
      
      if ((obj instanceof raja.util.List) || (obj instanceof raja.util.DirectedGraph) || (obj instanceof raja.util.DirectedGraph.Edge))
      {
        ((Writable) obj).write(this);
      }
      else
      {
        String label = "@" + className + "_" + Integer.toHexString(obj.hashCode());
        labels.put(obj, label);
        write(className + " (" + label + ") {");
        
        incrementIndent();
        ((Writable) obj).write(this);
        decrementIndent();
        
        write("}");
      }
    }
  }
  
  private void writeField(String fieldName, Object fieldValue) throws IOException
  {
    write(fieldName + " = ");
    //ADDED by Murat Inan - Support for Boolean, String and Character types
    if (fieldValue instanceof Number)
    {
      write(fieldValue.toString());
    }
    else if (fieldValue instanceof Boolean)
    {
      write(fieldValue.toString());
    }
    else if (fieldValue instanceof String)
    {
      write("\"" + fieldValue.toString() + "\"");
    }
    else if (fieldValue instanceof Character)
    {
      write("'" + fieldValue.toString() + "'");
    }
    else
    {
      writeObject(fieldValue);
    }
  }
  
  public void writeFields(Object[][] fields) throws IOException
  {
    write("\n");
    for(int i = 0 ; i < fields.length ; i++)
    {
      writeIndent();
      writeField(((String) fields[i][0]), fields[i][1]);
      write("\n");
    }
    writeIndent(-1);
  }
  
  public void writeFields(Number[] fields) throws IOException
  {
    write(" ");
    for(int i = 0 ; i < (fields.length - 1) ; i++)
    {
      write(fields[i].toString() + " ; ");
    }
    write(fields[fields.length - 1].toString() + " ");
  }
  
  public void writeIndent() throws IOException
  {
    writeIndent(0);
  }
  private void writeIndent(int n) throws IOException
  {
    for(int i = 0 ; i < (indent + n) ; i++)
    {
      write(INDENT_BLOCK);
    }
  }
  
}
