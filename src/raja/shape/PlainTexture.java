/* $Id: PlainTexture.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import raja.Point3D;
import raja.io.*;
import java.util.HashMap;


public class PlainTexture implements Texture, java.io.Serializable, Writable
{
  LocalTexture localTexture;
  
  public PlainTexture(LocalTexture t)
  {
    localTexture = t;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("localTexture",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new PlainTexture((LocalTexture) map.get("localTexture"));
  }
  
  public LocalTexture getLocalTexture(Point3D p)
  {
    return localTexture;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    return "";
  }
  
  @Override
  public String toExampleString() {
    return "null";
  }
  
  @Override
  public Texture getInstance(String info) {
    LocalTexture l = new LocalTexture (0.1, 0.3, 0.1, 10, 1, raja.RGB.red);
    return new PlainTexture(l);
  }
  ////////////////
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "localTexture", localTexture } };
    writer.writeFields(fields);
  }
}
