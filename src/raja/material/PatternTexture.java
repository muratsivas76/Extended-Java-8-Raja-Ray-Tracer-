package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;

import java.util.HashMap;

public class PatternTexture implements Texture, java.io.Serializable, Writable {
  
  private RGB primaryColor = new RGB(1.0, 0.84, 0.0);
  private RGB secondaryColor = new RGB(1.0, 1.0, 0.59);
  private RGB borderColor = new RGB(0.0, 0.0, 0.0);
  private RGB kr = new RGB(0.0, 0.0, 0.0);
  private RGB kt = new RGB(0.5, 0.5, 0.5);
  private double cellSize = 0.5;
  private double borderWidth = 0.05;
  private int ns = 100;
  private int nt = 10;
  
  public PatternTexture(RGB primary, RGB secondary, RGB borderColor, RGB kt,
    double cellSize, double borderWidth, int ns, int nt) {
    this.primaryColor = primary;
    this.secondaryColor = secondary;
    this.borderColor = borderColor;
    this.kr = borderColor;
    this.kt = kt;
    this.cellSize = Math.max(0.05, cellSize);
    this.borderWidth = Math.max(0, Math.min(0.5, borderWidth));
    this.ns = ns;
    this.nt = nt;
  }
  
  public PatternTexture(RGB primary, RGB secondary, double cellSize, double borderWidth) {
    this(primary, secondary, new RGB(0.0, 0.0, 0.0), new RGB(0.5, 0.5, 0.5),
    cellSize, borderWidth, 100, 10);
  }
  
  public PatternTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double angle = Math.toRadians(30.0);
    double cosA = Math.cos(angle);
    double sinA = Math.sin(angle);
    
    double px = p.x;
    double py = p.y;
    double pz = p.z;
    
    double x = (px * cosA - pz * sinA) / cellSize;
    double z = (px * sinA + pz * cosA) / cellSize;
    double y = py / cellSize * 0.5;
    
    x += Math.sin(y * Math.PI) * 0.25;
    z += Math.cos(y * Math.PI * 0.5) * 0.25;
    
    double sqrt3 = Math.sqrt(3.0);
    double q = (2.0 / 3.0) * x;
    double r = (-1.0 / 3.0) * x + (sqrt3 / 3.0) * z;
    
    double cubeX = q;
    double cubeZ = r;
    double cubeY = -cubeX - cubeZ;
    
    int rx = (int) Math.round(cubeX);
    int ry = (int) Math.round(cubeY);
    int rz = (int) Math.round(cubeZ);
    
    double xDiff = Math.abs(rx - cubeX);
    double yDiff = Math.abs(ry - cubeY);
    double zDiff = Math.abs(rz - cubeZ);
    
    if (xDiff > yDiff && xDiff > zDiff) {
      rx = -ry - rz;
      } else if (yDiff > zDiff) {
      ry = -rx - rz;
      } else {
      rz = -rx - ry;
    }
    
    double hexCenterX = (3.0 / 2.0) * rx;
    double hexCenterZ = (sqrt3 / 2.0) * rx + sqrt3 * rz;
    
    double dx = x - hexCenterX;
    double dz = z - hexCenterZ;
    double distance = Math.sqrt(dx * dx + dz * dz);
    
    double hexRadius = 1.0;
    boolean isBorder = distance > (hexRadius - borderWidth * 2);
    boolean isEven = ((rx - rz) % 2 == 0);
    RGB surfaceColor = isBorder ? borderColor : (isEven ? primaryColor : secondaryColor);
    
    return new LocalTexture(surfaceColor, kr, kt, ns, nt);
  }
  
  @Override
  public String getUsageInformation() {
    return "Constructor: PatternTexture(RGB primary, RGB secondary, RGB borderColor, RGB kt, " +
    "double cellSize, double borderWidth, int ns, int nt);\n" +
    "-1 returns default constructor.\n" +
    "Example:\n1.0,0.84,0.0,  1.0,1.0,0.59,  0.0,0.0,0.0,  0.5,0.5,0.5,  0.5,  0.05,  100,  10\n" +
    "Mark parameters after ###\n";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    String str = info.trim();
    int idx = str.lastIndexOf("###");
    if (idx < 0) return new PatternTexture();
    
    str = str.substring(idx + 3).replaceAll("[\\n\\s]", "");
    if (str.equals("-1")) return new PatternTexture();
    
    String[] s = str.split(",");
    if (s.length < 16) return new PatternTexture();
    
    try {
      double pR = Double.parseDouble(s[0]);
      double pG = Double.parseDouble(s[1]);
      double pB = Double.parseDouble(s[2]);
      double sR = Double.parseDouble(s[3]);
      double sG = Double.parseDouble(s[4]);
      double sB = Double.parseDouble(s[5]);
      double bR = Double.parseDouble(s[6]);
      double bG = Double.parseDouble(s[7]);
      double bB = Double.parseDouble(s[8]);
      double ktR = Double.parseDouble(s[9]);
      double ktG = Double.parseDouble(s[10]);
      double ktB = Double.parseDouble(s[11]);
      double cS = Double.parseDouble(s[12]);
      double bW = Double.parseDouble(s[13]);
      int ns = Integer.parseInt(s[14]);
      int nt = Integer.parseInt(s[15]);
      
      return new PatternTexture(
        new RGB(pR, pG, pB),
        new RGB(sR, sG, sB),
        new RGB(bR, bG, bB),
        new RGB(ktR, ktG, ktB),
        cS, bW, ns, nt
      );
      } catch (Exception e) {
      e.printStackTrace();
      return new PatternTexture();
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    writer.write("// Pattern Texture\n");
    Object[][] fields = {
      {"primaryColor", primaryColor},
      {"secondaryColor", secondaryColor},
      {"borderColor", borderColor},
      {"kr", kr},
      {"kt", kt},
      {"cellSize", Double.valueOf(cellSize)},
      {"borderWidth", Double.valueOf(borderWidth)},
      {"ns", Integer.valueOf(ns)},
      {"nt", Integer.valueOf(nt)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("primaryColor", null);
    map.put("secondaryColor", null);
    map.put("borderColor", null);
    map.put("kr", null);
    map.put("kt", null);
    map.put("cellSize", Double.valueOf(0.5));
    map.put("borderWidth", Double.valueOf(0.05));
    map.put("ns", Integer.valueOf(100));
    map.put("nt", Integer.valueOf(10));
    
    reader.readFields(map);
    
    return new PatternTexture(
      (RGB) map.get("primaryColor"),
      (RGB) map.get("secondaryColor"),
      (RGB) map.get("borderColor"),
      (RGB) map.get("kt"),
      ((Number) map.get("cellSize")).doubleValue(),
      ((Number) map.get("borderWidth")).doubleValue(),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue()
    );
  }
  
}
