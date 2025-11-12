/* $Id: CommandLineUI.java,v 1.5 2001/02/25 01:28:40 gregoire Exp $
 * Copyright (C) 2000-2001 E. Fleury & G. Sutre
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
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.*;

import raja.renderer.Scene;
import raja.renderer.Renderer;
import raja.renderer.Resolution;
import raja.io.ObjectReader;
import raja.io.ImageIO;
import raja.io.ImageWriter;

import gnu.getopt.*;


/**
 * Main class of this Command Line User Interface.
 */
public class CommandLineUI
{
  private static final int SHOW_NOT_REQUESTED = 0;
  private static final int SHOW_HELP = 1;
  private static final int SHOW_VERSION = 2;
  
  private static final String NO_PROGRESS = "none";
  private static final String X_PROGRESS = "X";
  private static final String TXT_PROGRESS = "txt";
  
  private static final String progNameLong = "Raja's Command Line UI";
  private static final String progName = "raja";
  
  private static final String usage =
  "Usage: " + progName + " [OPTIONS] INPUT_FILE\n" +
  "\n" +
  "Options:\n" +
  "\n" +
  "  -h, --help\t\t\tdisplay this help\n" +
  "  -q, --quiet, --silent\t\tbe really quiet\n" +
  "  -v, --verbose[=LEVEL]\t\tincrease verbosity, or set to LEVEL\n" +
  "  -V, --version\t\t\tdisplay version\n" +
  "  -x, --xview\t\t\tshow image during its computation\n" +
  "  -p, --progress={X, txt}\tturns progress display on and\n" +
  "                         \tchose between graphical or textual display\n" +
  "\n" +
  "  -r, --resolution=RES\t\tset resolution to RES (default 256x192)\n" +
  "  -d, --depth=DEPTH\t\tset recursivity depth (default 5)\n" +
  "  -e, --exact\t\t\tdisable optimization mode\n" +
  "  -a, --antialias=LEVEL\t\tuse antialiasing with level LEVEL\n" +
  "  -D, --diadic\t\t\tuse diadic sampling\n" +
  "\n" +
  "  -o, --output=FILE\t\tsave image into FILE\n" +
  "  -c, --codec=CODEC\t\tset encoding codec to CODEC\n" +
  "                   \t\tavailable codecs: PNG, TIFF, JPEG, PNM, BMP";
  
  private static final String fullVersion =
  progNameLong + " version " + Util.rajaVersion + "\n" +
  "\n" +
  "(C) 1999-2001 Emmanuel Fleury, Grégoire Sutre\n" +
  "\n" +
  "This program is free software; It is distributed in the hope that\n" +
  "it will be useful, but WITHOUT ANY WARRANTY; without even the implied\n" +
  "warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n" +
  "See the GNU General Public License for more details.\n" +
  "\n" +
  "Written by Emmanuel Fleury <efleury@users.sourceforge.net>\n" +
  "       and Grégoire Sutre  <gregoire@users.sourceforge.net>\n" +
  "\n" +
  "For news, updates and documentation, visit http://raja.sourceforge.net";
  
  /**
   * Main method to run this program.
   */
  public static void main(String[] argv)
  {
    long starttime = 0, endtime = 0;
    
    // Set parameters to default values
    boolean diadic = false;
    boolean exact = false;
    int showInfo = SHOW_NOT_REQUESTED;
    boolean xview = false;
    int antialiasLevel = 0;
    int depth = 5;
    String codec = null;
    String inputFile = null;
    String outputFile = null;
    String progress = NO_PROGRESS;
    Resolution resolution = new Resolution(256, 192);
    MessageWriter msgWriter = new MessageWriter(System.out,
      System.err,
    MessageWriter.VERBOSITY_NORMAL);
    
    
    
    // Parsing command line --------------------------------------------
    
    // Init LongOpt array for Getopt initialization
    LongOpt[] longopts = new LongOpt[14];
    
    // Init flag options
    longopts[0]  = new LongOpt("diadic",  LongOpt.NO_ARGUMENT, null, 'D');
    longopts[1]  = new LongOpt("exact",   LongOpt.NO_ARGUMENT, null, 'e');
    longopts[2]  = new LongOpt("help",    LongOpt.NO_ARGUMENT, null, 'h');
    longopts[3]  = new LongOpt("quiet",   LongOpt.NO_ARGUMENT, null, 'q');
    longopts[4]  = new LongOpt("silent",  LongOpt.NO_ARGUMENT, null, 'q');
    longopts[5]  = new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'V');
    longopts[6]  = new LongOpt("xview",   LongOpt.NO_ARGUMENT, null, 'x');
    
    // Init optional valued options
    longopts[7]  = new LongOpt("verbose", LongOpt.OPTIONAL_ARGUMENT, null, 'v');
    
    // Init valued options
    longopts[8]  = new LongOpt("antialias",  LongOpt.REQUIRED_ARGUMENT, null, 'a');
    longopts[9]  = new LongOpt("codec",      LongOpt.REQUIRED_ARGUMENT, null, 'c');
    longopts[10] = new LongOpt("depth",      LongOpt.REQUIRED_ARGUMENT, null, 'd');
    longopts[11] = new LongOpt("output",     LongOpt.REQUIRED_ARGUMENT, null, 'o');
    longopts[12] = new LongOpt("progress",   LongOpt.REQUIRED_ARGUMENT, null, 'p');
    longopts[13] = new LongOpt("resolution", LongOpt.REQUIRED_ARGUMENT, null, 'r');
    
    // Init Getopt object to parse options
    Getopt g = new Getopt(progName, argv, "+:DehqVxv::a:c:d:o:p:r:", longopts);
    
    // Get options
    int c;
    String arg;
    
    while ((c = g.getopt()) != -1)
    {
      switch (c)
      {
        case 'D':
          diadic = true;
        break;
        
        case 'e':
          exact = true;
        break;
        
        case 'h':
          showInfo = SHOW_HELP;
        break;
        
        case 'q':
          msgWriter.setVerbosity(MessageWriter.VERBOSITY_NONE);
        break;
        
        case 'V':
          showInfo = SHOW_VERSION;
        break;
        
        case 'x':
          xview = true;
        break;
        
        case 'v':
          arg = g.getOptarg();
        if (arg != null)
        {
          try
          {
            msgWriter.setVerbosity(parseNonNegativeInt(arg));
          }
          catch(NumberFormatException err)
          {
            printInvalidArgumentError(msgWriter, "verbose", arg);
            System.exit(1);
          }
        }
        else
        {
          msgWriter.increaseVerbosity();
        }
        break;
        
        case 'a':
          arg = g.getOptarg();
        try
        {
          antialiasLevel = parseNonNegativeInt(arg);
        }
        catch(NumberFormatException err)
        {
          printInvalidArgumentError(msgWriter, "antialias", arg);
          System.exit(1);
        }
        break;
        
        case 'c':
          arg = g.getOptarg();
        codec = arg.toUpperCase();
        break;
        
        case 'd':
          arg = g.getOptarg();
        try
        {
          depth = parseNonNegativeInt(arg);
        }
        catch(NumberFormatException err)
        {
          printInvalidArgumentError(msgWriter, "depth", arg);
          System.exit(1);
        }
        break;
        
        case 'o':
          arg = g.getOptarg();
        outputFile = arg;
        break;
        
        case 'p':
          arg = g.getOptarg();
        if (arg.equals("X"))
        {
          progress = X_PROGRESS;
        }
        else if (arg.equals("txt"))
        {
          progress = TXT_PROGRESS;
        }
        else
        {
          printInvalidArgumentError(msgWriter, "progress", arg);
          System.exit(1);
        }
        break;
        
        case 'r':
          arg = g.getOptarg();
        int sep = arg.indexOf('x');
        
        if (sep == -1)
        {
          printInvalidArgumentError(msgWriter, "resolution", arg);
          System.exit(1);
        }
        
        try
        {
          int width = parseNonNegativeInt(arg.substring(0, sep));
          int height = parseNonNegativeInt(arg.substring(sep + 1));
          resolution = new Resolution(width, height);
        }
        catch(NumberFormatException err)
        {
          printInvalidArgumentError(msgWriter, "resolution", arg);
          System.exit(1);
        }
        break;
        
        case ':':
        case '?':
        msgWriter.message(MessageWriter.MSG_ERROR,
        "Try `" + progName + " --help' for more information.");
        System.exit(1);
        
        default:
          // This shall not happen
        throw new RuntimeException(progNameLong + " internal error !!!!");
      }
    }
    
    // Check whether more information about this program is requested
    if (showInfo == SHOW_HELP)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_CRUCIAL,
      usage);
      System.exit(0);
    }
    else if (showInfo == SHOW_VERSION)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_CRUCIAL,
      fullVersion);
      System.exit(0);
    }
    
    // Check that there remains exactly one argument
    int index = g.getOptind();
    if (index > (argv.length - 1))
    {
      msgWriter.message(MessageWriter.MSG_ERROR,
      "Missing input file.");
      msgWriter.message(MessageWriter.MSG_ERROR,
      "Try `" + progName + " --help' for more information.");
      System.exit(1);
    }
    else if (index < (argv.length - 1))
    {
      msgWriter.message(MessageWriter.MSG_ERROR,
      "Too many input files.");
      msgWriter.message(MessageWriter.MSG_ERROR,
      "Try `" + progName + " --help' for more information.");
      System.exit(1);
    }
    
    // Init input file
    inputFile = argv[index];
    
    // [End of] Parsing command line -----------------------------------
    
    
    
    // Report parsed parameters ----------------------------------------
    
    // Display program name and version
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
    progNameLong + " version " + Util.rajaVersion + "\n");
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "------------ Reporting parsed parameters ---------------------------------" +
    "\n");
    
    // Display input file
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_LOW,
    "Input file: " + inputFile + "\n");
    
    // Display selected options
    String selectedOptions =
    "Selected options:" + "\n" +
    "   xview:\t" + xview + "\n" +
    "   progress:\t" + progress + "\n" +
    "   resolution:\t" + resolution + "\n" +
    "   depth:\t" + depth + "\n" +
    "   antialias:\t" + ((antialiasLevel == 0) ?
      "none" :
    "enabled with level " + antialiasLevel) + "\n" +
    "   exact:\t" + exact + "\n" +
    "   diadic:\t" + diadic + "\n" +
    "   output:\t" + ((outputFile == null) ?
      "none" :
    outputFile) + "\n" +
    "   codec:\t" + ((codec == null) ?
      "unspecified" :
    codec);
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_LOW,
    selectedOptions + "\n");
    
    if ((! xview) && (outputFile == null))
    {
      // Warn user that image will be lost
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
        "WARNING: you didn't select option `--xview' nor `--output'" + "\n" +
      "         image will be lost after rendering !!!" + "\n");
    }
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "--- [End of] Reporting parsed parameters ---------------------------------" +
    "\n");
    
    // [End of] Report parsed parameters -------------------------------
    
    
    
    // Check capability to save ouput file  ----------------------------
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "------------ Checking capability to save ouput file ----------------------" +
    "\n");
    
    // Deduce codec if not set
    if ((codec == null) && (outputFile != null))
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "Encoding codec was not specified." + "\n" +
      "Trying to deduce codec from output file suffix...");
      
      codec = ImageIO.suffix2Codec(outputFile);
      
      if (codec == null)
      {
        msgWriter.message(MessageWriter.MSG_ERROR,
          "Error: Could not deduce codec for file: " + outputFile + "\n" +
          "       Please use a supported image file suffix or explicitly set the" + "\n" +
        "       encoding codec with the --codec option");
        System.exit(1);
      }
      
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "done." +
      "\n");
      
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_LOW,
      "Using codec: " + codec + "\n");
    }
    
    // Check that we will be able to save the image
    if (outputFile != null)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "Checking capability to write using codec: " + codec +
      " into file: " + outputFile + "...");
      
      try
      {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        
        if (ImageIO.createImageWriter(codec, fileOutputStream) == null)
        {
          msgWriter.message(MessageWriter.MSG_ERROR,
          "Error: Could not find an image writer for this codec: " + codec);
          System.exit(1);
        }
        
        fileOutputStream.close();
      }
      catch (IOException err)
      {
        msgWriter.message(MessageWriter.MSG_ERROR,
        "I/O Error: " + err.getMessage());
        System.exit(1);
      }
      
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "done." +
      "\n");
    }
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "--- [End of] Checking capability to save ouput file ----------------------" +
    "\n");
    
    // [End of] Check capability to save ouput file  -------------------
    
    
    
    // Pre-rendering initializations -----------------------------------
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "------------ Pre-rendering initializations -------------------------------" +
    "\n");
    
    // Parse input file
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
    "Parsing file: " + inputFile + "...");
    Scene scene = null;
    try
    {
      Reader in = new InputStreamReader(new FileInputStream(inputFile));
      ObjectReader reader = new ObjectReader(in);
      starttime = System.currentTimeMillis();
      scene = (Scene) reader.readObject();
      endtime = System.currentTimeMillis();
    }
    catch (IOException err)
    {
      msgWriter.message(MessageWriter.MSG_ERROR,
      "I/O Error: " + err.getMessage());
      System.exit(1);
    }
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
    "Parsing finished.");
    
    // Display parsing time
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_LOW,
    "Parsing time: " + Util.getTime(endtime - starttime));
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL, "");
    
    // Init renderer
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
    "Initializing renderer...");
    Renderer renderer = Util.getRenderer(scene,
      resolution,
      exact,
      depth,
      diadic,
    antialiasLevel);
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "done." +
    "\n");
    
    // Get a reference to the image to be filled by the renderer
    BufferedImage image = renderer.getImage();
    
    // Init image frame and show it if necessary
    ImageFrame imageFrame = null;
    
    if (xview)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "Initializing image showing frame...");
      
      imageFrame = new ImageFrame("Raja's image", image, 100);
      imageFrame.setLocation(300, 300);
      imageFrame.pack();
      imageFrame.setUpdating(true);
      imageFrame.setVisible(true);
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "done." +
      "\n");
    }
    
    // Init progress display
    BoundedRangeModel model = renderer.getModel();
    JFrame progressFrame = null;
    
    if (progress == TXT_PROGRESS)
    {
      // Textual progress display
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "Initializing textual progress display...");
      model.addChangeListener(new TextualProgressBar(model, System.out));
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "done." +
      "\n");
    }
    else if (progress == X_PROGRESS)
    {
      // Graphical progress display
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "Initializing graphical progress display...");
      progressFrame = new GraphicalProgressBarFrame(model, progNameLong);
      progressFrame.setLocation(300, 200);
      progressFrame.pack();
      progressFrame.setVisible(true);
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "done." +
      "\n");
    }
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "--- [End of] Pre-rendering initializations -------------------------------" +
    "\n");
    
    // [End of] Pre-rendering initializations --------------------------
    
    
    
    // Rendering stage -------------------------------------------------
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "------------ Rendering stage ---------------------------------------------" +
    "\n");
    
    // Init rendering thread
    Thread rendering = new Thread(renderer);
    
    // Tune thread priority
    rendering.setPriority(Thread.MIN_PRIORITY);
    
    // Start rendering
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
    "Rendering...");
    
    starttime = System.currentTimeMillis();
    rendering.start();
    try
    {
      rendering.join();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    endtime = System.currentTimeMillis();
    
    // Rendering finished
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
    "Rendering finished.");
    
    // Display rendering time
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_LOW,
    "Rendering time: " + Util.getTime(endtime - starttime));
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n" +
    "--- [End of] Rendering stage ---------------------------------------------");
    
    // [End of] Rendering ----------------------------------------------
    
    
    
    // Post-rendering tasks --------------------------------------------
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "\n" +
    "------------ Post-rendering tasks ----------------------------------------");
    
    // Update image frame and stop progress display
    if (xview)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "\n" +
      "Stopping updating image showing frame...");
      imageFrame.setUpdating(false);
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "done.");
    }
    if (progress == X_PROGRESS)
    {
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
        "\n" +
      "Disposing of graphical progress display...");
      progressFrame.dispose();
      msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "done.");
    }
    
    // Save image
    if (outputFile != null)
    {
      try
      {
        msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL, "");
        
        msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
        "Writing " + codec + " file: " + outputFile + "...");
        
        OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
        ImageWriter writer = ImageIO.createImageWriter(codec, os);
        
        if (writer == null)
        {
          // This shall not happen, since we already checked the
          // writing ability for this codec
          throw new RuntimeException(progNameLong + " internal error !!!!");
        }
        
        Object writeParam = writer.createDefaultImageWriteParam(image);
        writer.setImageWriteParam(writeParam);
        writer.write(image);
        os.close();
        
        msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_NORMAL,
        "done.");
      }
      catch (IOException err)
      {
        msgWriter.message(MessageWriter.MSG_ERROR,
        "I/O Error: " + err.getMessage());
        System.exit(1);
      }
    }
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n" +
    "--- [End of] Post-rendering tasks ----------------------------------------");
    
    // [End of] Post-rendering tasks -----------------------------------
    
    
    
    // Exit gracefully -------------------------------------------------
    
    msgWriter.message(MessageWriter.MSG_SIGNIFICANCE_VERY_LOW,
      "\n\n" +
      "\n" +
    "------------ Exiting gracefully ------------------------------------------");
    
    if ((! xview) || (! imageFrame.isShowing())) {
      // Exit if no image frame was requested or
      // if the image frame has been closed
      System.exit(0);
    }
    else
    {
      // add a listener to the image frame in order to exit when
      // the frame is closed
      imageFrame.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent e)
          {
            System.exit(0);
          }
      });
    }
    
    // [End of] Exit gracefully ----------------------------------------
  }
  
  private static int parseNonNegativeInt(String s)
  {
    int result = Integer.parseInt(s);
    
    if (result < 0)
    {
      throw new NumberFormatException(s);
    }
    
    return result;
  }
  
  private static void printInvalidArgumentError(MessageWriter w,
    String opt,
  String arg)
  {
    w.message(MessageWriter.MSG_ERROR,
    "Invalid argument `" + arg + "' for option `--" + opt + "'.");
    w.message(MessageWriter.MSG_ERROR,
    "Try `" + progName + " --help' for more information.");
  }
}


/**
 * A Class for writing messages filtered by their significance.
 */
class MessageWriter
{
  static final int MSG_ERROR                 = -1;
  static final int MSG_SIGNIFICANCE_CRUCIAL  = 0;
  static final int MSG_SIGNIFICANCE_NORMAL   = 1;
  static final int MSG_SIGNIFICANCE_LOW      = 2;
  static final int MSG_SIGNIFICANCE_VERY_LOW = 3;
  
  static final int VERBOSITY_NONE      = 0;
  static final int VERBOSITY_NORMAL    = 1;
  static final int VERBOSITY_HIGH      = 2;
  static final int VERBOSITY_VERY_HIGH = 3;
  
  private PrintStream out;
  private PrintStream err;
  private int verbosity;
  
  MessageWriter(PrintStream out, PrintStream err, int verbosity)
  {
    this.out = out;
    this.err = err;
    this.verbosity = verbosity;
  }
  
  void setVerbosity(int verbosity)
  {
    this.verbosity = verbosity;
  }
  
  void increaseVerbosity()
  {
    verbosity++;
  }
  
  void message(int significance, String msg)
  {
    if (significance == MSG_ERROR)
    {
      err.println(msg);
    }
    else if (significance <= verbosity)
    {
      out.println(msg);
    }
  }
  
}


/**
 * A Class defining a frame displaying a graphic progress bar.
 */
class GraphicalProgressBarFrame extends JFrame
{
  GraphicalProgressBarFrame(BoundedRangeModel m, String title)
  {
    super(title);
    JPanel pane = new JPanel();
    setContentPane(pane);
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JProgressBar progressBar = new JProgressBar();
    progressBar.setModel(m);
    progressBar.setStringPainted(true);
    
    JLabel label = new JLabel("Rendering...");
    label.setAlignmentX((float) 0.5);
    label.setForeground(Color.black);
    
    pane.add(label);
    pane.add(Box.createRigidArea(new Dimension(0, 10)));
    pane.add(progressBar);
  }
}


/**
 * A Class defining a textual progress bar on an output print stream.
 */
class TextualProgressBar implements ChangeListener
{
  private BoundedRangeModel m;
  private PrintStream ps;
  private int percentComplete;
  private boolean printedHeader = false;
  
  TextualProgressBar(BoundedRangeModel m, PrintStream ps)
  {
    this.m = m;
    this.ps = ps;
    this.percentComplete = 0;
  }
  
  public void stateChanged(ChangeEvent e)
  {
    if (! printedHeader)
    {
      ps.print("\n   0% -> ");
      printedHeader = true;
    }
    
    int newPercentComplete = (100 * (m.getValue() - m.getMinimum())) /
    (m.getMaximum() - m.getMinimum());
    
    if (newPercentComplete > percentComplete)
    {
      if (newPercentComplete == 50)
      {
        ps.print(".\n  50% -> ");
      }
      else if (newPercentComplete == 100)
      {
        ps.print(".\n\n");
      }
      else if ((newPercentComplete / 10) > (percentComplete / 10))
      {
        ps.print(". ");
      }
      else
      {
        ps.print(".");
      }
    }
    percentComplete = newPercentComplete;
  }
}
