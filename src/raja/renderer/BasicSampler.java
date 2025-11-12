/* $Id: BasicSampler.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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


public class BasicSampler implements Sampler
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
    model.setValue(1);
    
    int width = image.getWidth();
    int height = image.getHeight();
    
    for (int i = 0 ; i < width ; i++)
    {
      for (int j = 0 ; j < height ; j++)
      {
        Ray ray = camera.getRay(((double) i + 0.5) / width,
        ((double) j + 0.5) / height);
        RGB light = rt.getLight(ray);
        image.setRGB(i, j, light.getColor().getRGB());
        model.setValue(model.getValue() + 1);
      }
    }
  }
  public void compute(Camera camera, RayTracer rt, BufferedImage image)
  {
    if (image == null) {
      throw new IllegalArgumentException("null image");
    }
    
    int width = image.getWidth();
    int height = image.getHeight();
    
    for (int i = 0 ; i < width ; i++)
    {
      for (int j = 0 ; j < height ; j++)
      {
        Ray ray = camera.getRay(((double) i + 0.5) / width,
        ((double) j + 0.5) / height);
        RGB light = rt.getLight(ray);
        image.setRGB(i, j, light.getColor().getRGB());
      }
    }
  }
}
