/* $Id: BasicRenderer.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
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

import java.awt.image.BufferedImage;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;


public class BasicRenderer implements Renderer
{
  private Camera camera;
  private RayTracer rt;
  private Sampler sampler;
  private BufferedImage image;
  private BoundedRangeModel model;
  
  public BasicRenderer(Camera camera, Resolution res, RayTracer rt, Sampler sampler, int imageType)
  {
    this.camera = camera;
    this.rt = rt;
    this.sampler = sampler;
    
    image = new BufferedImage(res.width, res.height, imageType);
    model = new DefaultBoundedRangeModel();
  }
  public BufferedImage getImage()
  {
    return image;
  }
  public BoundedRangeModel getModel()
  {
    return model;
  }
  public void run()
  {
    sampler.compute(camera, rt, model, image);
  }
}
