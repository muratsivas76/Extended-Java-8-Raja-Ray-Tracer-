// Murat Inan
package net.murat.rajaext;

import javax.swing.filechooser.*;

/**
 * Simple class for showing obj files
 *
 * @author Murat iNAN
 */
public 	class 	FilterOBJ
extends FileFilter
implements java.io.Serializable
{
  
  /**
   * @param f All files
   * @return If is directory or is au file true
   *         otherwise false
   */
  public boolean accept (java.io.File f)
  {
    if (f.isDirectory ())
    {
      return true;
    }
    
    String name = f.getName ();
    
    if (name != null)
    {
      name = name.toLowerCase ();
    }
    
    if (name.endsWith (".txt"))
    {
      return true;
    }
    
    return false;
  }
  
  /** @return "text" files*/
  public String getDescription ()
  {
    return "Plain TEXT files";
  }
  
}
