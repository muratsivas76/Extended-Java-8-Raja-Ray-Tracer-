/* $Id: DiadicSampler.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
 * Copyright (C) 2000 E. Fleury & G. Sutre
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
import java.awt.image.BufferedImage;
import javax.swing.BoundedRangeModel;
import java.util.Vector;


public class DiadicSampler implements Sampler
{
  public void compute(Camera camera, RayTracer rt, BoundedRangeModel model, BufferedImage image)
  {
    if (image == null) {
      throw new IllegalArgumentException("null image");
    }
    
    if (model == null) {
      compute(camera, rt, image);
    }
    
    model.setMinimum(1);
    model.setMaximum(image.getWidth() * image.getHeight());
    model.setValue(0);
    
    int width = image.getWidth();
    int height = image.getHeight();
    
    int[] currentXpix = {0, width - 1};
    int[] currentYpix = {0, height - 1};
    
    computePixels(camera, rt, model, image, currentXpix, currentYpix, width, height);
    
    while(true)
    {
      int[] newXpix = holes(currentXpix);
      int[] newYpix = holes(currentYpix);
      
      if ((newXpix.length == 0) && (newYpix.length == 0)) {
        break;
      }
      
      computePixels(camera, rt, model, image, newXpix, newYpix, width, height);
      computePixels(camera, rt, model, image, currentXpix, newYpix, width, height);
      computePixels(camera, rt, model, image, newXpix, currentYpix, width, height);
      
      currentXpix = merge(currentXpix, newXpix);
      currentYpix = merge(currentYpix, newYpix);
    }
  }
  public void compute(Camera camera, RayTracer rt, BufferedImage image)
  {
    if (image == null) {
      throw new IllegalArgumentException("null image");
    }
    
    int width = image.getWidth();
    int height = image.getHeight();
    
    int[] currentXpix = {0, width - 1};
    int[] currentYpix = {0, height - 1};
    
    computePixels(camera, rt, image, currentXpix, currentYpix, width, height);
    
    while(true)
    {
      int[] newXpix = holes(currentXpix);
      int[] newYpix = holes(currentYpix);
      
      if ((newXpix.length == 0) && (newYpix.length == 0)) {
        break;
      }
      
      computePixels(camera, rt, image, newXpix, newYpix, width, height);
      computePixels(camera, rt, image, currentXpix, newYpix, width, height);
      computePixels(camera, rt, image, newXpix, currentYpix, width, height);
      
      currentXpix = merge(currentXpix, newXpix);
      currentYpix = merge(currentYpix, newYpix);
    }
  }
  private void computePixels(Camera camera, RayTracer rt, BoundedRangeModel model, BufferedImage image, int [] xPix, int [] yPix, int width, int height)
  {
    for(int k = 0 ; k < xPix.length ; k++)
    {
      for(int l = 0 ; l < yPix.length ; l++)
      {
        int i = xPix[k];
        int j = yPix[l];
        
        Ray ray = camera.getRay(((double) i + 0.5) / width,
        ((double) j + 0.5) / height);
        RGB light = rt.getLight(ray);
        image.setRGB(i, j, light.getColor().getRGB());
        model.setValue(model.getValue() + 1);
      }
    }
  }
  private void computePixels(Camera camera, RayTracer rt, BufferedImage image, int [] xPix, int [] yPix, int width, int height)
  {
    for(int k = 0 ; k < xPix.length ; k++)
    {
      for(int l = 0 ; l < yPix.length ; l++)
      {
        int i = xPix[k];
        int j = yPix[l];
        
        Ray ray = camera.getRay(((double) i + 0.5) / width,
        ((double) j + 0.5) / height);
        RGB light = rt.getLight(ray);
        image.setRGB(i, j, light.getColor().getRGB());
      }
    }
  }
  private int[] holes(int[] t)
  {
    Vector holes = new Vector();
    
    for(int i = 1 ; i < t.length ; i++)
    {
      if (t[i] > t[i-1] + 1) {
        holes.add(new Integer(t[i-1] + (t[i] - t[i-1]) / 2));
      }
    }
    
    int[] result = new int[holes.size()];
    
    for(int i = 0 ; i < result.length ; i++)
    {
      result[i] = ((Integer) holes.elementAt(i)).intValue();
    }
    
    return result;
  }
  private int[] merge(int[] t1, int[] t2)
  {
    int[] result = new int[t1.length + t2.length];
    
    int k1, k2, i;
    
    for(k1 = 0, k2 = 0, i = 0 ; (k1 < t1.length) && (k2 < t2.length) ; i++)
    {
      if (t1[k1] <= t2[k2]) {
        result[i] = t1[k1++];
      }
      else {
        result[i] = t2[k2++];
      }
    }
    
    if (k1 < t1.length) {
      for(; i < result.length ; k1++, i++)
      {
        result[i] = t1[k1];
      }
    }
    else if (k2 < t2.length) {
      for(; i < result.length ; k2++, i++)
      {
        result[i] = t2[k2];
      }
    }
    
    return result;
  }
}
