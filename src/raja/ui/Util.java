/* $Id: Util.java,v 1.4 2001/02/25 01:28:40 gregoire Exp $
 * Copyright (C) 2000 E. Fleury & G. Sutre
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.ui;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.swing.filechooser.FileFilter;

import raja.renderer.*;
import raja.io.*;


/**
 * Utility class providing static methods used by the Console UI and the Mini GUI.
 */
class Util
{
  static final String rajaVersion = "0.4.0-pre4";
  
  static final FileFilter rajaInputFileFilter = new FileFilter() {
    // Accept all directories and all .raj files.
    public boolean accept(File f)
    {
      if (f.isDirectory())
      {
        return true;
      }
      
      String suffix = raja.util.FileHelper.getSuffix(f);
      return ((suffix != null) && suffix.equals("raj"));
    }
    
    // The description of this filter
    public String getDescription()
    {
      return "Raja input files (*.raj)";
    }
  };
  
  static final FileFilter imageFileFilter = new FileFilter() {
    // Accept all directories and all .png, .tiff, .jpg, .pnm and
    // .bmp files.
    public boolean accept(File f)
    {
      if (f.isDirectory())
      {
        return true;
      }
      
      String codec = ImageIO.suffix2Codec(f);
      
      if (codec != null)
      {
        return (codec.equals("PNG")  ||
          codec.equals("TIFF") ||
          codec.equals("JPEG") ||
          codec.equals("PNM")  ||
        codec.equals("BMP"));
      }
      return false;
    }
    
    // The description of this filter
    public String getDescription()
    {
      return "Image files";
    }
  };
  
  /**
   * Returns a string representing the given elapsed time given.
   */
  static String getTime(long totalMilliSeconds)
  {
    long milliSeconds = totalMilliSeconds % 1000;
    long totalSeconds = totalMilliSeconds / 1000;
    long seconds = totalSeconds % 60;
    long totalMinutes = totalSeconds / 60;
    long minutes = totalMinutes % 60;
    long totalHours = totalMinutes / 60;
    
    boolean display = false;
    String elapsedTime = "";
    
    if (totalHours > 0)
    {
      elapsedTime += totalHours + "h ";
      display = true;
    }
    if ((minutes > 0) || display)
    {
      elapsedTime += minutes + "m ";
      display = true;
    }
    if ((seconds > 0) || display)
    {
      elapsedTime += seconds + "s ";
      display = true;
    }
    
    elapsedTime += milliSeconds + "ms";
    
    return elapsedTime;
  }
  
  /**
   * Returns a renderer intialized with the given arguments.
   */
  static Renderer getRenderer(Scene scene,
    Resolution resolution,
    boolean exact,
    int depth,
    boolean diadic,
  int antialiasLevel)
  {
    RayTracer rayTracer;
    
    if (exact) {
      rayTracer = new AdvancedRayTracer(scene.getWorld(), depth, 0);
    }
    else {
      rayTracer = new AdvancedRayTracer(scene.getWorld(), depth);
    }
    
    Sampler sampler;
    
    if (diadic) {
      sampler = new DiadicSampler();
    }
    else if (antialiasLevel > 0) {
      sampler = new NaiveSuperSampler(antialiasLevel);
    }
    else {
      sampler = new BasicSampler();
    }
    
    Renderer renderer = new BasicRenderer(scene.getCamera(),
      resolution,
      rayTracer,
      sampler,
    BufferedImage.TYPE_3BYTE_BGR);
    return renderer;
  }
}
