/* $Id: FileHelper.java,v 1.1 2001/02/16 00:57:03 gregoire Exp $
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

package raja.util;

import java.io.File;


public final class FileHelper
{
  /**
   * Don't let anyone instantiate this class.
   */
  private FileHelper()
  {
  }
  
  /**
   * Returns the suffix of the given file name.  This method returns
   * <code>null</code> if the given file name has no suffix.
   */
  public static String getSuffix(String fileName)
  {
    String suffix = null;
    int i = fileName.lastIndexOf('.');
    
    if ((i > 0) && (i < (fileName.length() - 1)))
    {
      suffix = fileName.substring(i+1).toLowerCase();
    }
    
    return suffix;
  }
  
  /**
   * Returns the suffix of the given file's name.  This method returns
   * <code>null</code> if the given file's name has no suffix.
   */
  public static String getSuffix(File f)
  {
    return getSuffix(f.getName());
  }
}
