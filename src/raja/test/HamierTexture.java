package raja.test;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class HamierTexture implements Texture, java.io.Serializable, Writable
{
  private RGB kd1, kd2, kr, kt;
  private int ns, nt;
  private double step, x0, y0;
  
  public HamierTexture(Color c1, Color c2,
    int ns, int nt,
  double step, double x0, double y0)
  {
    BufferedImage bi=new BufferedImage (3, 3, 1);
    Graphics2D g2d=bi.createGraphics ();
    g2d.setBackground (Color.white);
    g2d.clearRect (0, 0, 3, 3);
    GradientPaint gp=new GradientPaint (0, 0, c1, 3, 3, c2, true);
    g2d.setPaint (gp);
    g2d.fillRect (0, 0, 3, 3);
    
    final double X255=255.0;
    
    int rgb=bi.getRGB (1, 1);
    Color ncol=new Color (rgb);
    
    double nr=((double)ncol.getRed ());
    double ng=((double)ncol.getGreen ());
    double nb=((double)ncol.getBlue ());
    
    nr=((1.0)/(X255))*nr;
    ng=((1.0)/(X255))*ng;
    nb=((1.0)/(X255))*nb;
    
    if (nr > 1.0) nr=1.0;
    if (nr < 0.0) nr=0.0;
    if (ng > 1.0) ng=1.0;
    if (ng < 0.0) ng=0.0;
    if (nb > 1.0) nb=1.0;
    if (nb < 0.0) nb=0.0;
    
    this.kd1=new RGB (nr, ng, nb);
    
    rgb=bi.getRGB (0, 0);
    ncol=new Color (rgb);
    
    nr=((double)ncol.getRed ());
    ng=((double)ncol.getGreen ());
    nb=((double)ncol.getBlue ());
    
    nr=((1.0)/(X255))*nr;
    ng=((1.0)/(X255))*ng;
    nb=((1.0)/(X255))*nb;
    
    if (nr > 1.0) nr=1.0;
    if (nr < 0.0) nr=0.0;
    if (ng > 1.0) ng=1.0;
    if (ng < 0.0) ng=0.0;
    if (nb > 1.0) nb=1.0;
    if (nb < 0.0) nb=0.0;
    
    RGB ofi=new RGB (nr, ng, nb);
    
    this.kd2=ofi;
    
    this.kr=ofi;
    
    rgb=bi.getRGB (0, 0);
    ncol=new Color (rgb);
    
    nr=((double)ncol.getRed ());
    ng=((double)ncol.getGreen ());
    nb=((double)ncol.getBlue ());
    
    nr=((1.0)/(X255))*nr;
    ng=((1.0)/(X255))*ng;
    nb=((1.0)/(X255))*nb;
    
    if (nr > 1.0) nr=1.0;
    if (nr < 0.0) nr=0.0;
    if (ng > 1.0) ng=1.0;
    if (ng < 0.0) ng=0.0;
    if (nb > 1.0) nb=1.0;
    if (nb < 0.0) nb=0.0;
    
    ofi=new RGB (nr, ng, nb);
    
    this.kt=ofi;
    
    this.ns=ns;
    this.nt=nt;
    
    this.step = step;
    this.x0 = x0;
    this.y0 = y0;
    
    g2d.dispose ();
    bi=null;
  }
  
  public HamierTexture(RGB kd1, RGB kd2, RGB kr, RGB kt, int ns, int nt, double step, double x0, double y0)
  {
    this.kd1 = kd1;
    this.kd2 = kd2;
    this.kr = kr;
    this.kt = kt;
    this.ns = ns;
    this.nt = nt;
    this.step = step;
    this.x0 = x0;
    this.y0 = y0;
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
    return null;
  }
  ////////////////
  
  /**
   * Builds the object LocalTexture from a StreamLexer.
   */
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    /* Initialisation */
    HashMap map = new HashMap();
    
    map.put("kdUn",null);
    map.put("kdDeux",null);
    map.put("kr",null);
    map.put("kt",null);
    map.put("ns",null);
    map.put("nt",null);
    map.put("step",null);
    map.put("x",null);
    map.put("y",null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new HamierTexture((RGB) map.get("kdUn"),
      (RGB) map.get("kdDeux"),
      (RGB) map.get("kr"),
      (RGB) map.get("kt"),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
      ((Number) map.get("step")).doubleValue(),
      ((Number) map.get("x")).doubleValue(),
    ((Number) map.get("y")).doubleValue());
  }
  
  public LocalTexture getLocalTexture(Point3D p)
  {
    double i = Math.floor((p.x - y0) / step);
    double j = Math.floor((p.y - x0) / step);
    
    if ((i-j) %3  == 1)
    {
      return new LocalTexture(kd1, kr, kt, ns, nt);
    }
    else
    {
      return new LocalTexture(kd2, kr, kt, ns, nt);
    }
  }
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = { { "kdUn", kd1 },
      { "kdDeux", kd2 },
      { "kr", kr },
      { "kt", kt },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) },
      { "step", new Double(step) },
      { "x", new Double(x0) },
    { "y", new Double(y0) } };
    writer.writeFields(fields);
  }
}
