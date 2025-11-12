/* $Id: Scene.java,v 1.2 2001/02/25 01:25:18 gregoire Exp $
 * Copyright (C) 1999-2001 E. Fleury & G. Sutre
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

package raja.renderer;

import raja.io.*;

import java.util.HashMap;
import java.util.Iterator;


public class Scene implements java.io.Serializable, Writable
{
  private World world;
  private Camera camera;
  
  public Scene(World world, Camera camera)
  {
    this.world = world;
    this.camera = camera;
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("world", null);
    map.put("camera", null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Scene((World) map.get("world"),
    (Camera) map.get("camera"));
  }
  
  public World getWorld()
  {
    return world;
  }
  public Camera getCamera()
  {
    return camera;
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "world", world },
    { "camera", camera } };
    writer.writeFields(fields);
  }
}
