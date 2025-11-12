/* $Id: ImageFrame.java,v 1.2 2001/02/18 09:26:56 gregoire Exp $
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;

import raja.io.ImageIO;
import raja.io.ImageWriter;


/**
 * A Class defining a frame displaying an image under construction.
 */
class ImageFrame extends JFrame
{
  private BufferedImage image;
  private ImageDisplay display;
  private JFileChooser fc;
  private JCheckBoxMenuItem updatingMenuItem;
  
  ImageFrame(String name, BufferedImage im, long updatingRate)
  {
    super(name);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    
    image = im;
    
    // Init display
    display = new ImageDisplay(im, updatingRate, false);
    getContentPane().add(display);
    
    // Init Menus
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    JMenu menu = new JMenu("File");
    menuBar.add(menu);
    
    // Init updating menu item
    updatingMenuItem = new JCheckBoxMenuItem("Updating", false);
    updatingMenuItem.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e)
        {
          display.setUpdating(e.getStateChange() == ItemEvent.SELECTED);
        }
    });
    menu.add(updatingMenuItem);
    
    menu.addSeparator();
    
    // Init save menu item
    Action saveAction = new SaveAction();
    try
    {
      fc = new JFileChooser();
      fc.addChoosableFileFilter(Util.imageFileFilter);
      fc.setFileFilter(Util.imageFileFilter);
    }
    catch (java.security.AccessControlException e)
    {
      // No access to local disk
      // Maybe running in a restricted environnment?
      fc = null;
      // Disabling save action
      saveAction.setEnabled(false);
    }
    menu.add(saveAction);
    
    menu.addSeparator();
    
    // Init close menu item
    JMenuItem mi = new JMenuItem("Close");
    mi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
          setVisible(false);
          dispose();
        }
    });
    menu.add(mi);
  }
  
  public void dispose()
  {
    setUpdating(false);
    display.stopUpdating();
    super.dispose();
  }
  
  /**
   * Sets the updating behavior.
   */
  void setUpdating(boolean b)
  {
    display.setUpdating(b);
    updatingMenuItem.setSelected(b);
  }
  
  /**
   * Class defining an action to save the displayed image.
   */
  class SaveAction extends AbstractAction
  {
    SaveAction()
    {
      super("Save...", new ImageIcon(ImageFrame.this.getClass().getClassLoader().getResource("raja/ui/icons/file_save.gif")));
    }
    
    public void actionPerformed(ActionEvent evt)
    {
      int returnVal = fc.showSaveDialog(ImageFrame.this);
      
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        File file = fc.getSelectedFile();
        
        try
        {
          String codec = ImageIO.suffix2Codec(file);
          
          if (codec == null)
          {
            JOptionPane.showMessageDialog(ImageFrame.this,
              "Could not deduce codec for file: " + file,
              "Illegal file suffix",
            JOptionPane.ERROR_MESSAGE);
            return;
          }
          
          OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
          ImageWriter writer = ImageIO.createImageWriter(codec, os);
          
          if (writer == null)
          {
            JOptionPane.showMessageDialog(ImageFrame.this,
              "Could not find an image writer for this codec: " +
              codec,
              "Illegal codec",
            JOptionPane.ERROR_MESSAGE);
            return;
          }
          
          Object writeParam = writer.createDefaultImageWriteParam(image);
          writer.setImageWriteParam(writeParam);
          writer.write(image);
          os.close();
          JOptionPane.showMessageDialog(ImageFrame.this,
          codec + " file: " + file + " has been written succesfully");
        }
        catch (IOException err)
        {
          JOptionPane.showMessageDialog(ImageFrame.this,
            err.getMessage(),
            "I/O Error",
          JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
}


/**
 * A Class defining an area displaying an image under construction.
 */
class ImageDisplay extends JPanel implements Runnable
{
  private BufferedImage image;
  private Thread updatingThread;
  private long updatingRate;
  private boolean updatingRunning;
  
  ImageDisplay(BufferedImage image, long updatingRate, boolean updating)
  {
    super();
    this.image = image;
    this.updatingRate = updatingRate;
    this.updatingRunning = updating;
    
    updatingThread = new Thread(this);
    updatingThread.setPriority(Thread.MAX_PRIORITY);
    updatingThread.start();
  }
  
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    
    g.drawImage(image,
      (getWidth()  - image.getWidth() ) / 2,
      (getHeight() - image.getHeight()) / 2,
    this);
  }
  
  public Dimension getMaximumSize()
  {
    return getPreferredSize();
  }
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }
  public Dimension getPreferredSize()
  {
    return new Dimension(image.getWidth(), image.getHeight());
  }
  
  BufferedImage getImage()
  {
    return image;
  }
  
  public void run() {
    while(true) {
      try {
        Thread.currentThread().sleep(updatingRate);
        
        if (updatingRunning) {
          repaint();
        }
        else {
          synchronized (this) {
            while (! updatingRunning) {
              wait();
            }
          }
        }
      }
      catch (InterruptedException e) {
      }
    }
  }
  synchronized void setUpdating(boolean b)
  {
    if (updatingRunning && !b) {
      // To be sure that the latest version of the image is
      // displayed, we re-display this component
      repaint();
    }
    
    updatingRunning = b;
    
    notify();
  }
  synchronized public void stopUpdating()
  {
    updatingThread.stop();
  }
}
