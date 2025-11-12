/* $Id: World.java,v 1.1 2001/02/25 01:25:18 gregoire Exp $
 * Copyright (C) 2001 E. Fleury & G. Sutre
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

import raja.*;
import raja.light.LightSource;
import raja.shape.*;
import raja.io.*;
import raja.util.List;

import java.util.HashMap;
import java.util.Iterator;


public class World implements java.io.Serializable, Writable
{
  private static final long serialVersionUID = 123456789L;
  
  private Solid solid;
  private List lights;
  private RGB backgroundLight;
  private RGB ambiantLight;
  private Volume ambiantVolume;
  
  // ADDED by Murat Inan for easier writing to objectoutputStream
  private Camera camera = new HorizontalCamera(
    new Point3D(-3.0, 0.0, 0.0),
    new Vector3D(1.0, 0.0, 0.0),
    1.8, 2.0, 1.5
  );
  private int imageWidth = 800;
  private int imageHeight = 600;
  private int imageDepth = 3;
  //////////////////////
  
  public World(Solid solid)
  {
    this.solid = solid;
    lights = new List();
    backgroundLight = RGB.black;
    ambiantLight = RGB.black;
    ambiantVolume = new IsotropicVolume(1.0);
  }
  
  public World(Solid solid,
    List lights,
    RGB backgroundLight,
    RGB ambiantLight,
  Volume ambiantVolume)
  {
    this.solid = solid;
    this.lights = lights;
    this.backgroundLight = backgroundLight;
    this.ambiantLight = ambiantLight;
    this.ambiantVolume = ambiantVolume;
  }
  
  // ADDED by Murat Inan ////
  public Camera getCamera() {
    return this.camera;
  }
  
  public int getImageWidth() {
    return this.imageWidth;
  }
  
  public int getImageHeight() {
    return this.imageHeight;
  }
  
  public int getImageDepth() {
    return this.imageDepth;
  }
  
  public void setCamera(Camera c) {
    this.camera = c;
  }
  
  public void setImageWidth(int nw) {
    this.imageWidth = nw;
  }
  
  public void setImageHeight(int nh) {
    this.imageHeight = nh;
  }
  
  public void setImageDepth(int dp) {
    this.imageDepth = dp;
  }
  //////////////////////////
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("lights", new List());
    map.put("solid", null);
    map.put("ambiantLight", RGB.black);
    map.put("backgroundLight", RGB.black);
    map.put("ambiantVolume", new IsotropicVolume(1.0));
    
    /* Parsing */
    reader.readFields(map);
    
    return new World((Solid) map.get("solid"),
      (List) map.get("lights"),
      (RGB) map.get("backgroundLight"),
      (RGB) map.get("ambiantLight"),
    (Volume) map.get("ambiantVolume"));
  }
  
  public void addLightSource(LightSource light)
  {
    lights.add(light);
  }
  public Iterator lightIterator()
  {
    return lights.iterator();
  }
  public Solid getSolid()
  {
    return solid;
  }
  public void setBackgroundLight(RGB light)
  {
    backgroundLight = light;
  }
  public void setAmbiantLight(RGB light)
  {
    ambiantLight = light;
  }
  public void setAmbiantVolume(Volume v)
  {
    ambiantVolume = v;
  }
  public RGB getBackgroundLight()
  {
    return backgroundLight;
  }
  public RGB getAmbiantLight()
  {
    return ambiantLight;
  }
  public double getAmbiantRefractiveIndex(Point3D p)
  {
    return ambiantVolume.refractiveIndex(p);
  }
  
  public String toImportantParametersString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Width: "+Integer.toString(imageWidth)+"\n");
    sb.append("Height: "+Integer.toString(imageHeight)+"\n");
    sb.append("Depth: "+Integer.toString(imageDepth)+"\n");
    sb.append("Volume: "+Double.toString(ambiantVolume.refractiveIndex(null))+"\n\n");
    sb.append("BGColor: "+backgroundLight.toParametersString()+"\n");
    sb.append("Ambiant/Shadow Color: "+ambiantLight.toParametersString()+"\n");
    //sb.append("Camera: "+camera.toString()+"\n\n");
    
    return sb.toString();
  }
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "solid", solid },
      { "lights", lights },
      { "backgroundLight", backgroundLight },
      { "ambiantLight", ambiantLight },
    { "ambiantVolume", ambiantVolume } };
    writer.writeFields(fields);
  }
}
