package net.murat.rajaext;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import javax.swing.*;

// raja
import raja.*;
import raja.material.*;
import raja.ui.*;
import raja.util.*;
import raja.light.*;
import raja.shape.*;
import raja.renderer.*;

public class RajaRayTracerGUI extends JPanel implements Serializable, ActionListener {
  private final PaintPanel paintPanel = new PaintPanel();
  
  private int width = 800;
  private int height = 600;
  
  private BufferedImage bimg = new BufferedImage(width, height,
  BufferedImage.TYPE_INT_ARGB);
  
  private final java.util.LinkedList<TexturedForm> txFormList = new java.util.LinkedList<TexturedForm>();
  private final java.util.LinkedList<String> txFormNamesList = new java.util.LinkedList<String>();
  //private final java.util.LinkedList<String> txFormFullNamesList = new java.util.LinkedList<String>();
  //private final java.util.LinkedList<String> cameraInfoList = new java.util.LinkedList<String>();
  //private final java.util.LinkedList<String> lightsInfoList = new java.util.LinkedList<String>();
  //private final java.util.LinkedList<String> texturesInfoList = new java.util.LinkedList<String>();
  
  private final Aggregate solids = new Aggregate();
  private final raja.util.List lights = new raja.util.List();
  
  private final JFileChooser jfc = new JFileChooser (new File ("."));
  private final JFileChooser mfc = new JFileChooser (new File ("."));
  
  final private JTextArea SHAPEAREA=getTextArea (9, 55);
  final private JScrollPane JSPSHAPE=new JScrollPane (SHAPEAREA);
  
  final private JTextArea MATERIALAREA=getTextArea (12, 55);
  final private JScrollPane JSPMATERIAL=new JScrollPane (MATERIALAREA);
  
  final private JTextArea LIGHTAREA=getTextArea (8, 55);
  final private JScrollPane JSPLIGHT=new JScrollPane (LIGHTAREA);
  
  final private JTextArea CAMERAAREA=getTextArea (8, 55);
  final private JScrollPane JSPCAMERA=new JScrollPane (CAMERAAREA);
  
  final private JTextArea UNIINTAREA=getTextArea (12, 55);
  final private JScrollPane JSPUNIINT=new JScrollPane (UNIINTAREA);
  
  private final JTextField torderField = getField("TRS");
  private final JTextField txField = getField("0.0");
  private final JTextField tyField = getField("0.0");
  private final JTextField tzField = getField("0.0");
  private final JTextField rxField = getField("0.0");
  private final JTextField ryField = getField("0.0");
  private final JTextField rzField = getField("0.0");
  private final JTextField sxField = getField("1.0");
  private final JTextField syField = getField("1.0");
  private final JTextField szField = getField("1.0");
  
  private JComboBox<String> samplerBox = null;
  private JComboBox<String> cameraBox = null;
  private JComboBox<String> lgBox = null;
  private JComboBox shapesBox = null;
  private JList<String> materialesBox = null;
  
  private final String[] OPTVALS = new String[] {"Set", "Cancel"};
  
  //private final int MILS = 500;
  
  private JButton createButton = new JButton("");
  private final JProgressBar bar = new JProgressBar ();
  
  private Camera camera = new HorizontalCamera(
    new Point3D(-3.0, 0.0, 0.0),    // Point
    new Vector3D(1.0, 0.0, 0.0),   // X eksenine bak
    1.8, 2.0, 1.5                  // Focal, screen width/height
  );
  
  private final Camera DEFAULTCAM = camera;
  private final Texture DEFAULT_TEXTURE = new LambertianTexture(new RGB(0.2, 0.2, 0.7));
  
  private final LightSource DEFAULTLS = new PointLightSource(
    new Point3D(-5.0, 0.0, 5.0),
    new RGB(1.0, 1.0, 1.0)
  );
  
  private World world = null;
  private RGB ambientShadow = new RGB(0.1, 0.1, 0.1);
  private RGB bgColor = new RGB(0.0, 0.0, 0.7);
  
  private int depth = 3;
  private Volume volume = new IsotropicVolume(0.5);
  
  private Sampler sampler = new DiadicSampler();
  private int alias = 2;
  
  private boolean processing = false;
  
  private RajaRayTracerGUI() {
    super(new BorderLayout());
    
    lights.add(DEFAULTLS);
    
    //cameraInfoList.add("Constructor is: HorizontalCamera(Point3D origin, Vector3D direction, double focal, double screenWidth, double screenHeight);\nExample:\n-5,0,0,  1,0,0,  1.8,  2,  1.5\nEnter your values after three diyez symbol\n###\n");
    //lightsInfoList.add("Constructor is: PointLightSource(double x, double y, double z, RGB light);\nExample:\n-3,0,5,  1.0, 1.0, 1.0\nEnter your values after three diyez symbol\n###\n");
    
    world = new World(
      solids,
      lights,
      new RGB(0.0, 0.0, 0.7),  // Background
      new RGB(0.1, 0.1, 0.1),  // Ambient-Shadow
      new IsotropicVolume(1.0)
    );
    
    mfc.addChoosableFileFilter (new FilterOBJ ());
    
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setForeground(new Color(0.1f, 0.1f, 0.9f));
    tabbedPane.setBackground(new Color(0.9f, 0.9f, 0.9f));
    tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 20));
    
    // Sekmeleri oluştur
    tabbedPane.addTab("Rendered Image", new JScrollPane(paintPanel));
    tabbedPane.addTab("Render Settings", createRenderSettingsPanel());
    tabbedPane.addTab("Camera & Lights", createCameraLightsPanel());
    tabbedPane.addTab("Shapes", createShapesPanel());
    
    add(tabbedPane, BorderLayout.CENTER);
    
    JPanel southPanel = new JPanel(new GridLayout(0, 3, 0, 5));
    
    createButton = getButton("Generate Image");
    createButton.addActionListener(this);
    createButton.setBackground(new Color(40, 167, 69)); // Yeşil tonu
    createButton.setForeground(Color.WHITE);
    createButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
    southPanel.add(createButton);
    
    JButton fromFileButton = getButton("Reproduce");
    fromFileButton.setBackground(new Color(90, 43, 83));
    fromFileButton.setForeground(Color.WHITE);
    fromFileButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
    fromFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          int rep=mfc.showOpenDialog (paintPanel);
          if (rep != JFileChooser.APPROVE_OPTION) return;
          
          File f=mfc.getSelectedFile ();
          
          try {
            InputStream fis = new FileInputStream(f);
            
            //BURASI IMPLEMENTS EDILECEK
            
            fis.close();
            } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
          }
        }
    });
    //southPanel.add (fromFileButton);
    
    JButton saveButton = getButton("Save");
    saveButton.setBackground(new Color(69, 53, 253)); // Mavi tonu
    saveButton.setForeground(Color.WHITE);
    saveButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
    saveButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (bimg == null) return;
          
          int rep=jfc.showSaveDialog (paintPanel);
          if (rep != JFileChooser.APPROVE_OPTION) return;
          
          File f=jfc.getSelectedFile ();
          
          String nm = f.getName();
          nm = nm.toLowerCase();
          
          String FORMAT = "PNG";
          
          if (nm.endsWith(".png")) {
            FORMAT = "PNG";
            } else if (nm.endsWith(".jpg")) {
            FORMAT = "JPG";
            } else if (nm.endsWith(".jpeg")) {
            FORMAT = "JPEG";
            } else {
            JOptionPane.showMessageDialog(paintPanel, "Only PNG/JPG/JPEG supported.");
            return;
          }
          
          try {
            ImageIO.write(bimg, FORMAT, f);
            
            //String fobjname = "test.txt";
            //String name = f.getName();
            //int index = name.lastIndexOf(".");
            //if (index > 0) {
            //  fobjname = (name.substring(0, index))+".txt";
            //}
            //File fscfile = new File("scenes");
            //if (fscfile.exists() == false) fscfile.mkdir();
            //OutputStream fos = new FileOutputStream("scenes/"+fobjname);
            //PrintStream ps = new PrintStream(fos, true, "UTF-8");
            
            //ps.println(world.toImportantParametersString());
            //ps.println(""+(camera.toString())+"\n");
            
            //ps.println("/** STARTS LIGHTS: */");
            
            //int size = lights.size();
            //for (int i = 0; i < size; i++) {
             // ps.println(lights.get(i));
            //  ps.println("");
            //}
            //ps.print("\b");
            //ps.println("/** ENDS LIGHTS */\n");
            
            //ps.println("/** STARTS GEOMETRIES/SHAPES/FORMS */");
            
            //size = txFormList.size();
            
            //int size2 = texturesInfoList.size();
            //if (size != size2) {
            //System.out.println("ERROR size != size2");
            //ps.flush();
            //ps.close();
            //fos.flush();
            //fos.close();
            //return;
            //}
            
           // for (int i = 0; i < size; i++) {
             // TexturedForm tfim = txFormList.get(i);
             // ps.println(tfim.toString());
             // if (tfim instanceof BasicTexturedForm) {
             //   BasicTexturedForm xtfim = (BasicTexturedForm)(tfim);
             //   Form forme = xtfim.getForm();
             //   if (forme instanceof BasicForm) {
             //     BasicForm bform = (BasicForm)forme;
             //     ps.println(bform.getTransform().toParametersString());
             //   }
                
               // Texture fiturIN = xtfim.getTextureIN ();
               // if (fiturIN instanceof TransparentPNGTexture) {
               //   String path = ((TransparentPNGTexture)(fiturIN)).getImagePath();
               //   ps.println("// TextureINPath: " + path + "");
               //   } else if (fiturIN instanceof ImageTexture) {
               //   String path = ((ImageTexture)(fiturIN)).getImagePath();
             //     ps.println("// TextureINPath: " + path + "");
              //    } else if (fiturIN instanceof ImageSpheringTexture) {
              //    String path = ((ImageSpheringTexture)(fiturIN)).getImagePath();
              //    ps.println("// TextureINPath: " + path + "");
              //    } else {
              //  }
             //   
              //  Texture fiturOUT = xtfim.getTextureOUT ();
              //  if (fiturOUT instanceof TransparentPNGTexture) {
              //    String path = ((TransparentPNGTexture)(fiturOUT)).getImagePath();
             //     ps.println("// TextureOUTPath: " + path + "");
               //   } else if (fiturOUT instanceof ImageTexture) {
              //    String path = ((ImageTexture)(fiturOUT)).getImagePath();
              //    ps.println("// TextureOUTPath: " + path + "");
              //    } else if (fiturOUT instanceof ImageSpheringTexture) {
             //     String path = ((ImageSpheringTexture)(fiturOUT)).getImagePath();
             //     ps.println("// TextureOUTPath: " + path + "");
             //     } else {
             //   }
            //    
            //  }
              //ps.println("%%%");
              //ps.println(texturesInfoList.get(i));
           //   ps.println("-----------------");
           // }
            
           // ps.println("END");
            
            //ps.flush();
            //ps.close();
            //fos.flush();
            //fos.close();
            JOptionPane.showMessageDialog(paintPanel, "Saved successfully: "+(f.getName()));
            return;
            } catch (IOException ioe) {
            JOptionPane.showMessageDialog(paintPanel, ioe.getMessage());
            return;
          }
        }
    });
    southPanel.add(saveButton);
    
    JButton exitButton = getButton("Exit");
    exitButton.setBackground(new Color(220, 53, 69)); // Kırmızı tonu
    exitButton.setForeground(Color.WHITE);
    exitButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
    exitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          System.exit(0);
        }
    });
    southPanel.add(exitButton);
    
    add(southPanel, BorderLayout.SOUTH);
  }
  
  private JPanel createRenderSettingsPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Main container for better centering
    JPanel mainPanel = new JPanel(new GridLayout(4, 1, 5, 5));
    
    // Resolution Settings
    JPanel resPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    resPanel.add(getLabel("Width:"));
    final JTextField widthField = getField("800");
    widthField.setColumns(6);
    resPanel.add(widthField);
    resPanel.add(getLabel("Height:"));
    final JTextField heightField = getField("600");
    heightField.setColumns(6);
    resPanel.add(heightField);
    mainPanel.add(resPanel);
    
    // Sampler Settings
    JPanel samplerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    samplerPanel.add(getLabel("Sampler:"));
    final String[] samplers = {"Diadic Sampler", "Basic Sampler", "Naive Super Sampler"};
    samplerBox = getComboBox(samplers);
    samplerPanel.add(samplerBox);
    samplerPanel.add(getLabel("Alias:"));
    final JTextField aliasField = getField("2");
    aliasField.setColumns(4);
    samplerPanel.add(aliasField);
    mainPanel.add(samplerPanel);
    
    // Render Quality Settings
    JPanel qualityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    qualityPanel.add(getLabel("Depth:"));
    final JTextField depthField = getField("3");
    depthField.setColumns(4);
    qualityPanel.add(depthField);
    qualityPanel.add(getLabel("Volume:"));
    final JTextField volumeField = getField("1"); // 1.5 is glass
    volumeField.setColumns(4);
    qualityPanel.add(volumeField);
    mainPanel.add(qualityPanel);
    
    // Render Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton ambientButton = getButton("Ambient/Shadow");
    ambientButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          Color col = JColorChooser.showDialog(paintPanel, "Ambient/Shadow Color:", Color.BLACK);
          if (col == null) return;
          
          int ri = col.getRed();
          int gi = col.getGreen();
          int bi = col.getBlue();
          
          double r = ((double)ri) / 255.0;
          double g = ((double)gi) / 255.0;
          double b = ((double)bi) / 255.0;
          
          ambientShadow = new RGB(r, g, b);
          
          return;
        }
    });
    buttonPanel.add(ambientButton);
    JButton worldBGButton = getButton("BG Color");
    worldBGButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          Color col = JColorChooser.showDialog(paintPanel, "World BG Color:", Color.BLACK);
          if (col == null) return;
          
          int ri = col.getRed();
          int gi = col.getGreen();
          int bi = col.getBlue();
          
          double r = ((double)ri) / 255.0;
          double g = ((double)gi) / 255.0;
          double b = ((double)bi) / 255.0;
          
          bgColor = new RGB(r, g, b);
          
          return;
        }
    });
    buttonPanel.add(worldBGButton);
    JButton setWorldButton = getButton("Set");
    setWorldButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          try {
            width = Integer.parseInt(widthField.getText());
            height = Integer.parseInt(heightField.getText());
            bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            alias = Integer.parseInt(aliasField.getText());
            depth = Integer.parseInt(depthField.getText());
            volume = new IsotropicVolume(Double.parseDouble(volumeField.getText()));
            
            int sindex = samplerBox.getSelectedIndex();
            
            if (sindex == 0) {
              sampler = new DiadicSampler();
              } else if (sindex == 1) {
              sampler = new BasicSampler();
              } else if (sindex == 2) {
              sampler = new NaiveSuperSampler(alias);
              } else {
              sampler = new BasicSampler();
            }
            
            world = new World(
              solids,
              lights,
              bgColor,
              ambientShadow,
              volume
            );
            } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(paintPanel, nfe.getMessage());
            return;
          }
          
          return;
        }
    });
    buttonPanel.add(setWorldButton);
    
    mainPanel.add(buttonPanel);
    
    // Center the main panel
    panel.add(mainPanel, BorderLayout.CENTER);
    
    return panel;
  }
  
  private JPanel createCameraLightsPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Main container for better centering
    JPanel mainPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    
    // Camera Settings
    JPanel camPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    camPanel.add(getLabel("Camera:"));
    final String[] cameras = {"Horizontal Camera", "Stereo Camera"};
    final Camera[] xcameras = {
      DEFAULTCAM,
      new StereoCamera(
        new Point3D(-3.0, 0.0, 0.0),
        new Vector3D(1.0, 0.0, 0.0),
        1.8, 2.0, 1.5, 5.0, true
      )
    };
    
    cameraBox = getComboBox(cameras);
    camPanel.add(cameraBox);
    JButton setCamButton = getButton("Set Camera");
    setCamButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          int cindex = cameraBox.getSelectedIndex();
          
          Camera aremac = xcameras[cindex];
          CAMERAAREA.setText(aremac.getUsageInformation());
          
          int intiline = JOptionPane.showOptionDialog(paintPanel, JSPCAMERA,
            "Change camera according lines below:",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            OPTVALS,
          OPTVALS[0]);
          
          if ((intiline < 0) || (intiline > 0)) return; // Cancel
            
          camera = aremac.getInstance(CAMERAAREA.getText());
          // cameraInfoList.clear();
          // cameraInfoList.add(aremac.toExampleString());
          // System.out.println("DEBUG: "+aremac.toExampleString());
          System.out.println(camera.toString());
          
          return;
        }
    });
    camPanel.add(setCamButton);
    mainPanel.add(camPanel);
    
    // Light Settings
    JPanel lightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    lightPanel.add(getLabel("Light Type:"));
    final String[] lghts = {"Point Light", "Directional Light", "Spot Light"};
    final LightSource[] lsources = {
      new PointLightSource(
        new Point3D(-3.0, 0.0, 5.0),
        new RGB(1.0, 1.0, 1.0)
      ),
      new DirectionalLightSource(
        new Vector3D(-5.0, 0.0, 5.0),
        new RGB(1.0, 1.0, 1.0)
      ),
      new SpotLightSource(
        -3.0, 0.0, 5.0,
        0, 1, 0,
        5.0, 25.0,
        RGB.white
      )
    };
    lgBox = getComboBox(lghts);
    lightPanel.add(lgBox);
    JButton addLightButton = getButton("Add Light");
    addLightButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          int cindex = lgBox.getSelectedIndex();
          
          LightSource lxs = lsources[cindex];
          LIGHTAREA.setText(lxs.getUsageInformation());
          
          int intiline = JOptionPane.showOptionDialog(paintPanel, JSPLIGHT,
            "Add light according lines below:",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            OPTVALS,
          OPTVALS[0]);
          
          if ((intiline < 0) || (intiline > 0)) return; // Cancel
            
          LightSource lsource = lxs.getInstance(LIGHTAREA.getText());
          System.out.println(lsource.toString());
          lights.add(lsource);
          // lightsInfoList.add(lxs.toExampleString());
          
          return;
        }
    });
    lightPanel.add(addLightButton);
    mainPanel.add(lightPanel);
    
    // Light Management
    JPanel lightMgmtPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton remLastLightButton = getButton("Remove Last Light");
    remLastLightButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (lights.size() < 1) return;
          System.out.println("Removing: " + (lights.getLast().toString()));
          lights.removeLast();
          // lightsInfoList.removeLast();
          return;
        }
    });
    lightMgmtPanel.add(remLastLightButton);
    JButton remAllLightsButton = getButton("Remove All Lights");
    remAllLightsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (lights.size() < 1) return;
          lights.clear();
          // lightsInfoList.clear();
          System.out.println("Removed all lights from list.");
          return;
        }
    });
    lightMgmtPanel.add(remAllLightsButton);
    mainPanel.add(lightMgmtPanel);
    
    // Center the main panel
    panel.add(mainPanel, BorderLayout.CENTER);
    
    return panel;
  }
  
  private JPanel createShapesPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Shape Selection
    JPanel shapeSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    shapeSelectPanel.add(getLabel("Shape:"));
    final String[] shapes = {
      "Plane", "Sphere", "Cylinder", "Cone",
      "Box", "Rectangle", "Torus", "Triangle",
      "Billboard", "Union", "Intersection"
    };
    final BasicForm[] xformes = {
      new MPlane(new Vector3D(0, 0, 1)),
      new MSphere(1.0),
      new MCylinder(1.0, 1.0),
      new MCone(new Point3D(0, 0, 0), new Vector3D(0, 0, 1), 0.5, 1.0),
      new MBox(new Point3D(-1, -1, -1), new Point3D(1, 1, 1)),
      new XRectangle(new Vector3D(0, 0, 1), 2, 1),
      new MTorus(1.0, 0.5),
      new MTriangle(new Point3D(-0.5, -0.5, -0.5), new Point3D(0.5, 0.5, 0.5), new Point3D(1, -1, 1)),
      new Billboard(10, 10, true)
    };
    shapesBox = getComboBox(shapes);
    shapeSelectPanel.add(shapesBox);
    
    shapeSelectPanel.add(getLabel("Material:"));
    final String[] materiales = {
      "Plain", "Damier", "Checkerboard", "Emissive",
      "Dielectric", "Image", "ImageSphering", "Lambertian",
      "Metal", "AfricanKente", "Anodized", "CeramicTile",
      "CrystalCave", "Hokusai", "Marble", "NorwegianRose",
      "TurkishTile", "TiledRoof", "Wood", "Void",
      "XRay", "TransparentPNG", "Hologram", "Prism",
      "Lava", "Water", "Storm", "AuroraCeramic",
      "Pattern", "KilimRosemaling", "NordicWeave", "NorthernLight",
      "ColorWheel", "Grid", "Plastic", "Stripes",
      "AnisotropicMetal", "DreamMist", "ObsidianHeart", "Sand"
    };
    final Texture[] xtextures = {
      new XPlainTexture(),
      new XDamierTexture(new RGB(1.0, 0.0, 0.0), new RGB(1.0, 1.0, 1.0), new RGB(0.0, 0.3, 0.0), new RGB(0.2, 0.0, 0.0), 100, 10, 4.0, 0.0, 0.0),
      new CheckerboardTexture(new RGB(1.0, 0.0, 0.0), new RGB(0.0, 1.0, 0.0), new RGB(0.5, 0.5, 0.5), new RGB(0.0, 0.0, 0.0), 150, 10, 0.3, 0.0, 0.0, 0.0),
      new EmissiveTexture(RGB.red, 0.35),
      new DielectricTexture(new RGB(0.0, 0.0, 0.8), 0.7, 0.4, 200),
      new ImageTexture(new BufferedImage(1, 1, 1), new RGB(0.5, 0.5, 0.5), new RGB(0.1, 0.1, 0.1), new RGB(0.0, 0.0, 0.0), 50, 10, 1.0, 1.0, 0.0, 0.0, 1.5, 1.5, 0.0, 0.0, 0.0),
      new ImageSpheringTexture(new BufferedImage(1, 1, 1), new RGB(0.5, 0.5, 0.5), new RGB(0.3, 0.3, 0.3), new RGB(0.0, 0.0, 0.0), 100, 10, 0.2, 0.0, 0.0, 1.0, 1.0),
      new LambertianTexture(),
      new MetalTexture(RGB.red),
      new AfricanKenteTexture(),
      new AnodizedTexture(),
      new CeramicTileTexture(new RGB(1,1,1), new RGB(0.9,0.9,0.9), new RGB(0.8,0.82,0.8), 4.0, 0.005, 0),
      new CrystalCaveTexture(),
      new HokusaiTexture(),
      new MarbleTexture(),
      new NorwegianRoseTexture(),
      new TurkishTileTexture(),
      new TiledRoofTexture(),
      new WoodTexture(),
      new VoidTexture(),
      new XRayTexture(),
      new TransparentPNGTexture(new BufferedImage(1, 1, 1), new RGB(0.5, 0.5, 0.5), new RGB(0.0, 0.0, 0.0), new RGB(0.0, 0.0, 0.0), 30, 10, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0),
      new HologramTexture(),
      new PrismTexture(),
      new LavaTexture(),
      new WaterTexture(),
      new StormTexture(),
      new AuroraCeramicTexture(),
      new PatternTexture(),
      new KilimRosemalingTexture(),
      new NordicWeaveTexture(),
      new NorthernLightTexture(),
      new ColorWheelTexture(),
      new GridTexture(),
      new PlasticTexture(),
      new StripesTexture(),
      new AnisotropicMetalTexture(),
      new DreamMistTexture(),
      new ObsidianHeartTexture(),
      new SandTexture()
    };
    
    materialesBox = new JList<String>(materiales);
    materialesBox.setForeground(new Color(0.8f, 0.1f, 0.1f));
    materialesBox.setFont(new Font("Serif", 1, 15));
    materialesBox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    materialesBox.setVisibleRowCount(20);
    JScrollPane materialesScroll = new JScrollPane(materialesBox);
    materialesScroll.setPreferredSize(new Dimension(200, 400));
    shapeSelectPanel.add(materialesScroll);
    
    JButton addShapeButton = getButton("Add Shape");
    addShapeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          BasicForm form = null;
          Texture texture = null;
          Matrix4 transform = null;
          
          BasicForm xfx = null;
          
          int cindex = shapesBox.getSelectedIndex();
          if (cindex == 8) { // union
            int size = txFormList.size();
            if (size < 2) return;
            
            Union union = new Union();
            StringBuffer ubuf = new StringBuffer("UNION: \n");
            for (int i = 0; i < size; i++) {
              ubuf.append("" + (i) + ": ");
              ubuf.append(txFormNamesList.get(i));
              ubuf.append("\n");
            }
            ubuf.append("Enter the numbers of selected shapes for union, separated with comma:###\n");
            
            UNIINTAREA.setText(ubuf.toString());
            
            int iline = JOptionPane.showOptionDialog (paintPanel, JSPUNIINT,
              "Enter selected shapes for union:",
              JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.PLAIN_MESSAGE,
              null,
              OPTVALS,
            OPTVALS [0]);
            
            if ((iline < 0) || (iline > 0)) return;//Cancel
              
            String hes = UNIINTAREA.getText();
            int hesindex = hes.lastIndexOf("###");
            hes = hes.substring(hesindex+3);
            hes = hes.replaceAll("\n", "");
            hes = hes.replaceAll(" ", "");
            
            String[] split = hes.split(",");
            int slen = split.length;
            
            int numTx = -1;
            for (int i = 0; i < slen; i++) {
              numTx = Integer.parseInt(split [i]);
              TexturedForm tm = txFormList.get(numTx);
              union.addForm(tm);
              //solids.getList().remove(numTx);
              //txFormList.remove(numTx);
              //txFormNamesList.remove(numTx);
            }
            int[] numbers=new int[slen];
            for (int i = 0; i < slen; i++) {
              numTx = Integer.parseInt(split[i]);
              numbers[i]=numTx;
            }
            java.util.Arrays.sort(numbers);
            for (int i = (slen-1); i >= 0; i--) {
              (solids.getList()).remove(numbers[i]);
              txFormList.remove(numbers[i]);
              txFormNamesList.remove(numbers[i]);
              //txFormFullNamesList.remove(numbers[i]);
              //texturesInfoList.remove(numbers[i]);
            }
            
            Solid usolid = new BasicSolid(union, volume);
            //solids.getList().clear();
            solids.addSolid(usolid);
            
            txFormList.add(union);
            txFormNamesList.add(union.toString());
            //txFormFullNamesList.add(union.toString());
            //texturesInfoList.add("UNION");
            
            return;
            } else if (cindex == 9) { // intersection
            int size = txFormList.size();
            if (size < 2) return;
            
            Intersection ise = new Intersection();
            StringBuffer ubuf = new StringBuffer("INTERSECTION: \n");
            for (int i = 0; i < size; i++) {
              ubuf.append("" + (i) + ": ");
              ubuf.append(txFormNamesList.get(i));
              ubuf.append("\n");
            }
            ubuf.append("Enter the numbers of selected shapes for intersection, separated with comma:###\n");
            
            UNIINTAREA.setText(ubuf.toString());
            
            int iline = JOptionPane.showOptionDialog (paintPanel, JSPUNIINT,
              "Enter selected shapes for intersection:",
              JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.PLAIN_MESSAGE,
              null,
              OPTVALS,
            OPTVALS [0]);
            
            if ((iline < 0) || (iline > 0)) return;//Cancel
              
            String hes = UNIINTAREA.getText();
            int hesindex = hes.lastIndexOf("###");
            hes = hes.substring(hesindex+3);
            hes = hes.replaceAll("\n", "");
            hes = hes.replaceAll(" ", "");
            
            String[] split = hes.split(",");
            int slen = split.length;
            
            int numTx = -1;
            for (int i = 0; i < slen; i++) {
              numTx = Integer.parseInt(split [i]);
              TexturedForm tm = txFormList.get(numTx);
              ise.addForm(tm);
              //solids.getList().remove(numTx);
              //txFormList.remove(numTx);
              //txFormNamesList.remove(numTx);
            }
            int[] numbers=new int[slen];
            for (int i = 0; i < slen; i++) {
              numTx = Integer.parseInt(split[i]);
              numbers[i]=numTx;
            }
            java.util.Arrays.sort(numbers);
            for (int i = (slen-1); i >= 0; i--) {
              (solids.getList()).remove(numbers[i]);
              txFormList.remove(numbers[i]);
              txFormNamesList.remove(numbers[i]);
              //txFormFullNamesList.remove(numbers[i]);
              //texturesInfoList.remove(numbers[i]);
            }
            
            Solid usolid = new BasicSolid(ise, volume);
            //solids.getList().clear();
            solids.addSolid(usolid);
            
            txFormList.add(ise);
            txFormNamesList.add(ise.toString());
            //txFormFullNamesList.add(ise.toString());
            //texturesInfoList.add("INTERSECTION");
            
            return;
            } else { //other basic shapes
            xfx = xformes[cindex];
            SHAPEAREA.setText(xfx.getUsageInformation());
          }
          
          int intiline = JOptionPane.showOptionDialog (paintPanel, JSPSHAPE,
            "Add shape according lines below:",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            OPTVALS,
          OPTVALS [0]);
          
          if ((intiline < 0) || (intiline > 0)) return;//Cancel
            
          if (xfx != null) {
            form = xfx.getInstance(SHAPEAREA.getText());
            System.out.println(form.toString());
            } else {
            form = null;
          }
          
          if (form == null) {
            JOptionPane.showMessageDialog(paintPanel, "Null form error!");
            return;
          }
          
          transform = getTransform();
          form.setTransform(transform);
          
          ////// TEXTURE IN //////////
          int[] cindexes = materialesBox.getSelectedIndices();
          final int cilen = cindexes.length;
          if (cilen == 0 || cilen > 2) {
            System.out.println("ERROR: You must select one or two options.");
            return;
          }
          
          boolean multiSelected = false;
          if (cilen == 2) {
            multiSelected = true;
          }
          
          Texture xtx = xtextures[cindexes[0]];
          MATERIALAREA.setText(xtx.getUsageInformation());
          
          intiline = JOptionPane.showOptionDialog (paintPanel, JSPMATERIAL,
            "Select material according lines below:",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            OPTVALS,
          OPTVALS [0]);
          
          if ((intiline < 0) || (intiline > 0)) return;//Cancel
            
          texture = xtx.getInstance(MATERIALAREA.getText());
          System.out.println(texture.toString());
          //System.out.println(xtx.toExampleString());
          
          if (texture == null) {
            texture = DEFAULT_TEXTURE;
          }
          
          //TransparentPNG - Billboard coherencia
          if (form instanceof Billboard &&
            texture instanceof TransparentPNGTexture) {
            ((TransparentPNGTexture)texture).setBillboardDimensions(
            ((Billboard)form).getWidth(), ((Billboard)form).getHeight());
            ((TransparentPNGTexture)texture).setInverseTransform(transform.inverse());
            ((TransparentPNGTexture)texture).setUseZForV(false);
          }
          
          if (form instanceof XRectangle &&
            texture instanceof TransparentPNGTexture) {
            ((TransparentPNGTexture)texture).setBillboardDimensions(
            ((XRectangle)form).getXRectangleWidth(), ((XRectangle)form).getXRectangleHeight());
            ((TransparentPNGTexture)texture).setInverseTransform(transform.inverse());
            ((TransparentPNGTexture)texture).setUseZForV(true);
          }
          
          if (form instanceof MSphere &&
            texture instanceof ImageSpheringTexture) {
            ((ImageSpheringTexture)texture).setSphereInfo(
              transform.transformPoint(((MSphere)form).getLocalCenter()),
              transform.inverse()
            );
          }
          
          Texture texture_in = texture;
          Texture texture_out = texture;
          ///////////// TEXTURE IN ENDS ///////
          
          //////////// TEXTURE OUT STARTS ////////////
          if (multiSelected) {
            xtx = xtextures[cindexes[1]];
            MATERIALAREA.setText(xtx.getUsageInformation());
            
            intiline = JOptionPane.showOptionDialog (paintPanel, JSPMATERIAL,
              "Select material according lines below:",
              JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.PLAIN_MESSAGE,
              null,
              OPTVALS,
            OPTVALS [0]);
            
            if ((intiline < 0) || (intiline > 0)) return;//Cancel
              
            Texture mexture = xtx.getInstance(MATERIALAREA.getText());
            System.out.println(mexture.toString());
            //System.out.println(xtx.toExampleString());
            
            if (mexture == null) {
              mexture = DEFAULT_TEXTURE;
            }
            
            //TransparentPNG - Billboard coherencia
            if (form instanceof Billboard &&
              texture instanceof TransparentPNGTexture) {
              ((TransparentPNGTexture)texture).setBillboardDimensions(
              ((Billboard)form).getWidth(), ((Billboard)form).getHeight());
              ((TransparentPNGTexture)texture).setInverseTransform(transform.inverse());
              ((TransparentPNGTexture)texture).setUseZForV(false);
            }
            
            if (form instanceof XRectangle &&
              texture instanceof TransparentPNGTexture) {
              ((TransparentPNGTexture)texture).setBillboardDimensions(
              ((XRectangle)form).getXRectangleWidth(), ((XRectangle)form).getXRectangleHeight());
              ((TransparentPNGTexture)texture).setInverseTransform(transform.inverse());
              ((TransparentPNGTexture)texture).setUseZForV(true);
            }
            
            if (form instanceof MSphere &&
              texture instanceof ImageSpheringTexture) {
              ((ImageSpheringTexture)texture).setSphereInfo(
                transform.transformPoint(((MSphere)form).getLocalCenter()),
                transform.inverse()
              );
            }
            
            texture_out = mexture;
          }
          /////////// TEXTURE OUT ENDS /////////////
          
          //texture = new LambertianTexture(new RGB(0.0, 0.7, 0.0));
          TexturedForm txForm = null;
          
          if (!multiSelected) {
            txForm = new BasicTexturedForm(form, texture_in);
            } else {
            boolean isRegular = true;
            
            String input = JOptionPane.showInputDialog(paintPanel,
            "<html><body><font color=\"red\" size=\"6\">r|e|y</font><font color=\"blue\" size=\"5\"> is regular order texture, other any letter is reverse.</font></body></hyml>");
            if (input == null) {
              isRegular = true;
              } else {
              if (input.length() < 1) {
                isRegular = true;
                } else {
                input = input.toLowerCase();
                final char chr = input.charAt(0);
                if ((chr == 'r') || (chr == 'e') || (chr == 'y')) {
                  isRegular = true;
                  } else {
                  isRegular = false;
                }
              }
            }
            
            if (!isRegular) {
              txForm = new BasicTexturedForm(form, texture_out, texture_in);
              } else {
              txForm = new BasicTexturedForm(form, texture_in, texture_out);
            }
          }
          
          //System.out.println("TXFRM: "+txForm);
          Solid solid = new BasicSolid(txForm, new IsotropicVolume(form.getVolumeValue()));
          solids.addSolid(solid);
          txFormList.add(txForm);
          txFormNamesList.add(purify(xfx.toExampleString()));
          //txFormFullNamesList.add(xfx.toExampleString());
          //texturesInfoList.add(xtx.toExampleString());
          
          System.out.println("Added: "+form.toString()+"\n#####\n"+texture.toString());
          
          return;
        }
    });
    shapeSelectPanel.add(addShapeButton);
    
    // Transform Settings - Shape panelin alt kısmında
    JPanel transformPanel = new JPanel(new GridLayout(4, 4, 5, 5));
    transformPanel.add(getLabel("Transform Order:"));
    torderField.setForeground(Color.RED);
    torderField.setColumns(6);
    transformPanel.add(torderField);
    transformPanel.add(getLabel(""));
    transformPanel.add(getLabel(""));
    transformPanel.add(getLabel("TranslateXYZ(+fwd/left/u):"));
    txField.setForeground(Color.MAGENTA);
    txField.setColumns(6);
    transformPanel.add(txField);
    tyField.setForeground(Color.MAGENTA);
    tyField.setColumns(6);
    transformPanel.add(tyField);
    tzField.setForeground(Color.MAGENTA);
    tzField.setColumns(6);
    transformPanel.add(tzField);
    
    transformPanel.add(getLabel("RotateXYZ:"));
    rxField.setForeground(Color.GREEN.darker());
    rxField.setColumns(6);
    transformPanel.add(rxField);
    ryField.setForeground(Color.GREEN.darker());
    ryField.setColumns(6);
    transformPanel.add(ryField);
    rzField.setForeground(Color.GREEN.darker());
    rzField.setColumns(6);
    transformPanel.add(rzField);
    
    transformPanel.add(getLabel("ScaleXYZ:"));
    sxField.setForeground(Color.RED);
    sxField.setColumns(6);
    transformPanel.add(sxField);
    syField.setForeground(Color.RED);
    syField.setColumns(6);
    transformPanel.add(syField);
    szField.setForeground(Color.RED);
    szField.setColumns(6);
    transformPanel.add(szField);
    
    //panel.add(transformPanel);
    
    // Shape Management
    JPanel shapeMgmtPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    
    JButton remSelectedShapesButton = getButton("Remove Selected Shapes");
    remSelectedShapesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (solids.getList().size() < 1) return;
          //(solids.getList()).removeLast();
          //txFormList.removeLast();
          //txFormNamesList.removeLast();
          final int size = txFormList.size();
          
          StringBuffer ubuf = new StringBuffer("SHAPES/GEOMETRIES: \n");
          for (int i = 0; i < size; i++) {
            ubuf.append("" + (i) + ": ");
            ubuf.append(txFormNamesList.get(i));
            ubuf.append("\n");
          }
          ubuf.append("Enter the numbers of shapes for remove, separated with comma:###\n");
          
          UNIINTAREA.setText(ubuf.toString());
          
          int iline = JOptionPane.showOptionDialog (paintPanel, JSPUNIINT,
            "Enter selected shapes for remove:",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            OPTVALS,
          OPTVALS [0]);
          
          if ((iline < 0) || (iline > 0)) return;//Cancel
            
          String hes = UNIINTAREA.getText();
          int hesindex = hes.lastIndexOf("###");
          hes = hes.substring(hesindex+3);
          hes = hes.replaceAll("\n", "");
          hes = hes.replaceAll(" ", "");
          
          String[] split = hes.split(",");
          int slen = split.length;
          
          int numTx = -1;
          
          int[] numbers=new int[slen];
          for (int i = 0; i < slen; i++) {
            numTx = Integer.parseInt(split[i]);
            numbers[i]=numTx;
          }
          java.util.Arrays.sort(numbers);
          for (int i = (slen-1); i >= 0; i--) {
            (solids.getList()).remove(numbers[i]);
            txFormList.remove(numbers[i]);
            txFormNamesList.remove(numbers[i]);
            //txFormFullNamesList.remove(numbers[i]);
            //texturesInfoList.remove(numbers[i]);
          }
          
          return;
        }
    });
    shapeMgmtPanel.add(remSelectedShapesButton);
    
    JButton remLastShapeButton = getButton("Remove Last Shape");
    remLastShapeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (solids.getList().size() < 1) return;
          System.out.println("Removing: "+(solids.getList().getLast().toString()));
          (solids.getList()).removeLast();
          txFormList.removeLast();
          txFormNamesList.removeLast();
          //texturesInfoList.removeLast();
          //txFormFullNamesList.removeLast();
          return;
        }
    });
    shapeMgmtPanel.add(remLastShapeButton);
    
    JButton remAllShapesButton = getButton("Remove All Shapes");
    remAllShapesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (solids.getList().size() < 1) return;
          (solids.getList()).clear();
          txFormList.clear();
          txFormNamesList.clear();
          //texturesInfoList.clear();
          //txFormFullNamesList.clear();
          System.out.println("Removed all solids from list.");
          return;
        }
    });
    shapeMgmtPanel.add(remAllShapesButton);
    
    // LAYOUT DÜZENİ:
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(transformPanel, BorderLayout.CENTER);
    
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(shapeMgmtPanel, BorderLayout.NORTH);
    centerPanel.add(shapeSelectPanel, BorderLayout.CENTER);
    
    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(centerPanel, BorderLayout.CENTER);
    
    return panel;
  }
  
  private final String purify(String descx) {
    String desc = descx;
    int index = desc.indexOf("(");
    desc = desc.substring(0, index);
    index = desc.indexOf(":");
    desc = desc.substring(index+1);
    desc = desc.trim();
    
    StringBuffer sb = new StringBuffer();
    sb.append(desc);
    sb.append("=");
    
    index = descx.lastIndexOf("###");
    String m = descx.substring(index+3);
    //m = m.replaceAll(" ", "");
    m = m.replaceAll("\n", "");
    
    sb.append(m);
    
    return sb.toString();
  }
  
  private final JLabel getLabel(String t) {
    JLabel label = new JLabel(t, JLabel.LEFT);
    label.setForeground(Color.GREEN.darker());
    label.setBackground(Color.BLACK);
    label.setFont(new Font("Serif", Font.BOLD, 14));
    return label;
  }
  
  private final JTextField getField(String t) {
    JTextField field = new JTextField(t);
    field.setForeground(Color.BLUE);
    field.setBackground(Color.WHITE);
    field.setFont(new Font("Serif", Font.BOLD, 16));
    return field;
  }
  
  private final JTextArea getTextArea(int u, int v) {
    JTextArea area = new JTextArea(u, v);
    area.setForeground(Color.BLUE);
    area.setBackground(Color.WHITE);
    area.setFont(new Font("Arial", Font.BOLD, 20));
    return area;
  }
  
  private final JButton getButton(String t) {
    JButton button = new JButton(t);
    button.setForeground(Color.BLUE);
    button.setBackground(new Color(0.9f, 0.9f, 0.9f));
    button.setFont(new Font("Serif", Font.BOLD, 14));
    return button;
  }
  
  private final JComboBox getComboBox(String[] strs) {
    JComboBox<String> box = new JComboBox(strs);
    box.setForeground(Color.BLUE);
    box.setBackground(Color.WHITE);
    box.setFont(new Font("Serif", Font.BOLD, 14));
    return box;
  }
  
  private Matrix4 getTransform() {
    try {
      double tx = Double.parseDouble(txField.getText());
      double ty = Double.parseDouble(tyField.getText());
      double tz = Double.parseDouble(tzField.getText());
      
      double rx = Double.parseDouble(rxField.getText());
      double ry = Double.parseDouble(ryField.getText());
      double rz = Double.parseDouble(rzField.getText());
      
      double sx = Double.parseDouble(sxField.getText());
      double sy = Double.parseDouble(syField.getText());
      double sz = Double.parseDouble(szField.getText());
      
      Matrix4 mtr = new Matrix4();
      
      String mtext = (torderField.getText()).trim();
      if (mtext.length () < 3) {
        JOptionPane.showMessageDialog(paintPanel, "Order must be like TRS.");
        return mtr;
      }
      
      mtext = mtext.toLowerCase();
      
      char chr = mtext.charAt(0);
      if (chr == 't') {
        mtr = Matrix4.translate(tx, ty, tz);
        } else if (chr == 'r') {
        if (rx != 0.0 || ry != 0.0 || rz != 0.0) {
          mtr = Matrix4.rotateXYZ(rx, ry, rz);
        }
        } else if (chr == 's') {
        if (sx != 1.0 || sy != 1.0 || sz != 1.0) {
          mtr = Matrix4.scale(sx, sy, sz);
        }
        } else {
      }
      
      chr = mtext.charAt(1);
      if (chr == 't') {
        mtr = mtr.multiply(Matrix4.translate(tx, ty, tz));
        } else if (chr == 'r') {
        if (rx != 0.0 || ry != 0.0 || rz != 0.0) {
          mtr = mtr.multiply(Matrix4.rotateXYZ(rx, ry, rz));
        }
        } else if (chr == 's') {
        if (sx != 1.0 || sy != 1.0 || sz != 1.0) {
          mtr = mtr.multiply(Matrix4.scale(sx, sy, sz));
        }
        } else {
      }
      
      chr = mtext.charAt(2);
      if (chr == 't') {
        mtr = mtr.multiply(Matrix4.translate(tx, ty, tz));
        } else if (chr == 'r') {
        if (rx != 0.0 || ry != 0.0 || rz != 0.0) {
          mtr = mtr.multiply(Matrix4.rotateXYZ(rx, ry, rz));
        }
        } else if (chr == 's') {
        if (sx != 1.0 || sy != 1.0 || sz != 1.0) {
          mtr = mtr.multiply(Matrix4.scale(sx, sy, sz));
        }
        } else {
      }
      
      System.out.println("Transform:\n"+mtr);
      return mtr;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return new Matrix4();
    }
  }
  
  private static final void forCompileNames() {
    AdvancedRayTracer advancedraytracer_variable = null;
    AfricanKenteTexture africankentetexture_variable = null;
    Aggregate aggregate_variable = null;
    AnisotropicMetalTexture anisootropicmetaltexture_variable = null;
    AnodizedTexture anodizedtexture_variable = null;
    AuroraCeramicTexture auroraceramictexture_variable = null;
    BasicForm basicform_variable = null;
    BasicRenderer basicrenderer_variable = null;
    BasicSampler basicsampler_variable = null;
    BasicSolid basicsolid_variable = null;
    BasicTexturedForm basictexturedform_variable = null;
    Billboard billboard_variable = null;
    Camera camera_variable = null;
    CeramicTileTexture ceramictiletexture_variable = null;
    CheckerboardTexture checkerboardtexture_variable = null;
    ColorWheelTexture colorwheeltexture_variable = null;
    Complement complement_variable = null;
    CompositeForm compositeform_variable = null;
    Cone cone_variable = null;
    CrystalCaveTexture crystalcavetexture_variable = null;
    Cylinder cylinder_variable = null;
    raja.test.DamierTexture damiertexture_variable = null;
    DiadicSampler diadicsampler_variable = null;
    DielectricTexture dielectrictexture_variable = null;
    DirectedGraph directedgraph_variable = null;
    DirectionalLightSource directionallightsource_variable = null;
    DreamMistTexture dreammisttexture_variable = null;
    EmissiveTexture emissivetexture_variable = null;
    FileHelper filehelper_variable = null;
    Form form_variable = null;
    GridTexture gridtexture_variable = null;
    raja.test.HamierTexture hamiertexture_variable = null;
    PatternTexture patterntexture_variable = null;
    HokusaiTexture hokusaitexture_variable = null;
    HologramTexture hologramtexture_variable = null;
    HorizontalCamera horizontalcamera_variable = null;
    ImageSpheringTexture imagespheringtexture_variable = null;
    ImageTexture imagetexture_variable = null;
    Intersection intersection_variable = null;
    IsotropicVolume isotropicvolume_variable = null;
    KilimRosemalingTexture kilimrosemalingtexture_variable = null;
    LambertianTexture lambertiantexture_variable = null;
    LavaTexture lavatexture_variable = null;
    LightRay lightray_variable = null;
    LightSource lightsource_variable = null;
    LocalGeometry localgeometry_variable = null;
    LocalTexture localtexture_variable = null;
    raja.test.MamierTexture mamiertexture_variable = null;
    MarbleTexture marbletexture_variable = null;
    Matrix3 matrix3_variable = null;
    Matrix4 matrix4_variable = null;
    MBox mbox_variable = null;
    MCone mcone_variable = null;
    MCylinder mcylinder_variable = null;
    MetalTexture metaltexture_variable = null;
    MPlane mplane_variable = null;
    MSphere msphere_variable = null;
    MTorus mtorus_variable = null;
    MTriangle mtriangle_variable = null;
    NaiveSuperSampler naivesupersampler_variable = null;
    NordicWeaveTexture nordicweavetexture_variable = null;
    NorthernLightTexture northernlighttexture_variable = null;
    NorwegianRoseTexture norwegianrosetexture_variable = null;
    raja.io.ObjectReader objectreader_variable = null;
    raja.io.ObjectWriter objectwriter_variable = null;
    ObsidianHeartTexture obsidianhearttexture_variable = null;
    PlainTexture plaintexture_variable = null;
    Plane plane_variable = null;
    PlasticTexture plastictexture_variable = null;
    Point2D point2d_variable = null;
    Point3D point3d_variable = null;
    PointLightSource pointlightsource_variable = null;
    PrismTexture prismtexture_variable = null;
    Ray ray_variable = null;
    RayTracer raytracer_variable = null;
    raja.renderer.Renderer renderer_variable = null;
    Resolution resolution_variable = null;
    RGB rgb_variable = null;
    Sampler sampler_variable = null;
    SandTexture sandtexture_variable = null;
    Scene scene_variable = null;
    Solid solid_variable = null;
    SolidLocalGeometry solidlocalgeometry_variable = null;
    Sphere sphere_variable = null;
    SpotLightSource spotlightsource_variable = null;
    StereoCamera stereocamera_variable = null;
    StormTexture stormtexture_variable = null;
    StripesTexture stripestexture_variable = null;
    Texture texture_variable = null;
    TexturedForm texturedform_variable = null;
    TexturedLocalGeometry texturedlocalgeometry_variable = null;
    TiledRoofTexture tiledrooftexture_variable = null;
    TransparentPNGTexture transparentpngtexture_variable = null;
    TurkishTileTexture turkishtiletexture_variable = null;
    Union union_variable = null;
    Vector3D vector3d_variable = null;
    VoidTexture voidtexture_variable = null;
    Volume volume_variable = null;
    WaterTexture watertexture_variable = null;
    WoodTexture woodtexture_variable = null;
    World world_variable = null;
    XDamierTexture xdamiertexture_variable = null;
    XPlainTexture xplaintexture_variable = null;
    XRayTexture xraytexture_variable = null;
    XRectangle xrectangle_variable = null;
  }
  
  // CREATE Button Action
  public void actionPerformed(ActionEvent evt) {
    Thread thr = new Thread() {
      public void run() {
        processing = true;
        createButton.setEnabled (false);
        
        try {
          drawScene(); // this too runs under his own thread
          } catch (Exception e) {
          e.printStackTrace ();
          processing = false;
          createButton.setEnabled (true);
        }
        
        createButton.setEnabled (true);
        processing = false;
      }
    };
    thr.start();
  }
  
  private final void drawScene() {
    processing = true;
    bimg = new BufferedImage(width, height,  BufferedImage.TYPE_INT_ARGB);
    paintPanel.setBufferedImage(bimg);
    
    createButton.setEnabled(false);
    Thread thr = new Thread() {
      public void run() {
        while(processing) {
          paintPanel.repaint();
          //try {
          //sleep (MILS);
          //} catch (InterruptedException ie) {
          //}
        }
      }
    };
    thr.start();
    
    bar.setValue (0);
    
    // For easy object output
    world.setCamera(camera);
    world.setImageWidth(width);
    world.setImageHeight(height);
    world.setImageDepth(depth);
    ///////
    final RayTracer tracer = new AdvancedRayTracer(world, depth);
    
    try {
      sampler.compute(camera, tracer, bar.getModel(), bimg);
    }
    catch (Exception e)
    {
      createButton.setEnabled (true);
      JOptionPane.showMessageDialog (paintPanel, "Error Computing!!!!");
      processing=false;
      bar.setValue (0);
      return;
    }
    
    processing = false;
    createButton.setEnabled(true);
    bar.setValue (0);
    
    return;
  }
  
  private static final void showScreen() {
    ////////
    forCompileNames();
    ////////
    
    JFrame fr = new JFrame("Raja Ray Tracer GUI");
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JComponent jc = new RajaRayTracerGUI();
    jc.setOpaque(true);
    fr.setContentPane(jc);
    
    fr.pack();
    fr.setSize(1360, 720);
    fr.setResizable(true);
    fr.setLocationRelativeTo(null);
    fr.setVisible(true);
    
    return;
  }
  
  public static final void main(final String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          showScreen();
        }
    });
  }
  
} // class end
