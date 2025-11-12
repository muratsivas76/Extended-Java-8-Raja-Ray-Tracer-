import java.awt.*;
import java.awt.image.*;

import java.io.*;

import javax.imageio.*;

//raja
import raja.*;
import raja.test.*;
import raja.light.*;
import raja.shape.*;
import raja.renderer.*;

final
public class DrawCristal
     extends Object
  implements Serializable
{

    private DrawCristal ()
    {
        System.exit (-1);
    }
    
    private static final void draw ()
    throws Exception
    {
        Aggregate solids = new Aggregate ();
        raja.util.List lights=new raja.util.List ();
        
        BufferedImage bimg=new BufferedImage (
            800, 600, BufferedImage.TYPE_3BYTE_BGR);
            
        Camera camera=new HorizontalCamera (
            new Point3D (0, 0, 130),
            new Vector3D (0.7071, 0, -0.70),
            1.8, 2.0, 1.5);
  
        Form form=new Sphere (new Point3D (140, -50, 30), 20);
        LocalTexture lt=new LocalTexture (
            new raja.RGB (0,0,0), new raja.RGB (.5,.5,.5), 
            new raja.RGB (new Color (0x7d5efb0f)),
            new raja.RGB (0,0,0), new raja.RGB (.90,.82,.78), 50, 1);
        Texture texture=new PlainTexture (lt);
        TexturedForm tform=new BasicTexturedForm (form, texture);
        Volume volume=new IsotropicVolume (1.5);
        Solid solida=new BasicSolid (tform, volume);
        solids.addSolid (solida);

        form=new Cone (new Point3D (140, -50, 30), new Vector3D (0,0,1), .5235);
        lt=new LocalTexture (
            new raja.RGB (new Color (0x7d5efb0f)), new raja.RGB (.5,.5,.5), 
            new raja.RGB (new Color (0x7d5efb0f)),
            new raja.RGB (0,0,0), new raja.RGB (.78,.90,.82), 50, 1);
        texture=new PlainTexture (lt);
        tform=new BasicTexturedForm (form, texture);
        volume=new IsotropicVolume (0);
        Solid solidb=new BasicSolid (tform, volume);
        solids.addSolid (solidb);

        form=new Plane (new Point3D (140, -50, 30), new Vector3D (0,0,-1));
        lt=new LocalTexture (
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)),
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)), 49, 0);
        texture=new PlainTexture (lt);
        tform=new BasicTexturedForm (form, texture);
        volume=new IsotropicVolume (1.5);
        Solid solidc=new BasicSolid (tform, volume);
        solids.addSolid (solidc);

        form=new Plane (new Point3D (140, -50, 30), new Vector3D (0,0,1));
        lt=new LocalTexture (
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)),
            new raja.RGB (new Color (0x6552fb0f)), 
            new raja.RGB (new Color (0x6552fb0f)), 50, 0);
        texture=new PlainTexture (lt);
        tform=new BasicTexturedForm (form, texture);
        volume=new IsotropicVolume (0);
        Solid solidd=new BasicSolid (tform, volume);
        solids.addSolid (solidd);

        form=new Plane (new Point3D (0, 0, 0), new Vector3D (0,0,1));
        texture=new DamierTexture (
            new raja.RGB (1,1,1), 
            new raja.RGB (new Color (0x7d5efb0f)),
            new raja.RGB (new Color (0x7d5efb0f)), 
            new raja.RGB (new Color (0x7d5efb0f)), 
            1, 1, 10, 0, 0);
        tform=new BasicTexturedForm (form, texture);
        volume=new IsotropicVolume (0);
        Solid solide=new BasicSolid (tform, volume);
        solids.addSolid (solide);

        form=new Cylinder (new Vector3D (140, 50, 10), 
           new Point3D (0,0,1), 18);
        lt=new LocalTexture (
            new raja.RGB (new Color (0x7d5efb0f)), new raja.RGB (.5,.5,.5), 
            new raja.RGB (new Color (0x7d5efb0f)),
            new raja.RGB (0,0,0), new raja.RGB (.78,.90,.82), 50, 1);
        texture=new PlainTexture (lt);
        tform=new BasicTexturedForm (form, texture);
        volume=new IsotropicVolume (0);
        Solid solidf=new BasicSolid (tform, volume);
        solids.addSolid (solidf);

//        solids.addPriority (solidb, solida);
        solids.addPriority (solidc, solidb);
        solids.addPriority (solidd, solidc);
        solids.addPriority (solida, solidc);
        solids.addPriority (solidf, solida);
        
        LightSource ls=new DirectionalLightSource (
           new Vector3D (1, 0, -.5),new raja.RGB (new Color (0x9122fb0f)));
           
        lights.add (ls);
        
        World world=new World (solids, lights,
            new raja.RGB (.4, .6, 1), new raja.RGB (.3, .3, .3),
            new IsotropicVolume (1));
        
        RayTracer tracer=new AdvancedRayTracer (world, 1);
        Sampler sampler=new DiadicSampler ();
        
        javax.swing.JProgressBar bar=new javax.swing.JProgressBar ();
        
        sampler.compute (camera, tracer, bar.getModel (), bimg);
        
        ImageIO.write (bimg, "PNG", new File ("cristal.png"));
        
        return;
    }
    
    public static final void main (final String [] args)
    throws Exception
    {
        draw ();
        
        System.out.println ("cristal.png is ready!");
        System.exit (0x00);
    }
    
}//class end

/*************
Sampler sampler=tool.getBasicSampler ();
sampler.compute (camera, tracer, bar.getModel (), bimg);
*************/
