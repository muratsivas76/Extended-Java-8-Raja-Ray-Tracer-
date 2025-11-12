package net.murat.rajaext;

final
public class PaintPanel
extends javax.swing.JPanel
{
  
  private java.awt.image.BufferedImage bimg = null;
  
  public PaintPanel ()
  {
    super (true);
    setPreferredSize (new java.awt.Dimension (640, 480));
  }
  
  final
  public java.awt.image.BufferedImage getBufferedImage ()
  {
    return bimg;
  }
  
  final
  public void setBufferedImage (java.awt.image.BufferedImage bi)
  {
    this.bimg = bi;
    this.repaint ();
  }
  
  public void paint (java.awt.Graphics g)
  {
    super.paint (g);
  }
  
  public void paintChildren (java.awt.Graphics g)
  {
    super.paintChildren (g);
  }
  
  public final void update (java.awt.Graphics g)
  {
    paintComponent (g);
  }
  
  protected final void paintComponent (java.awt.Graphics g)
  {
    super.paintComponent (g);
    
    java.awt.Dimension d = this.getSize ();
    int w = d.width;
    int h = d.height;
    
    g.setColor (java.awt.Color.white);
    g.fillRect (0, 0, w, h);
    
    if (bimg != null)
    {
      g.drawImage (bimg, 0, 0, this);
    }
    return;
  }
  
}
//class end
