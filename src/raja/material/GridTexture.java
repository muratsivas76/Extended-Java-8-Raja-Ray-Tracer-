package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class GridTexture implements Texture, java.io.Serializable, Writable {
  private RGB backgroundColor;
  private RGB lineColor;
  private double cellSize;
  private double lineThickness;
  
  // Default constructor
  public GridTexture() {
    this(new RGB(1, 1, 1), new RGB(0, 0, 0), 0.5, 0.05);
  }
  
  // Custom constructor
  public GridTexture(RGB backgroundColor, RGB lineColor, double cellSize, double lineThickness) {
    this.backgroundColor = backgroundColor;
    this.lineColor = lineColor;
    this.cellSize = cellSize;
    this.lineThickness = lineThickness;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double xPos = Math.abs(p.x % cellSize);
    double yPos = Math.abs(p.y % cellSize);
    
    boolean isLine = xPos < lineThickness || xPos > cellSize - lineThickness ||
    yPos < lineThickness || yPos > cellSize - lineThickness;
    
    RGB surfaceColor = isLine ? lineColor : backgroundColor;
    
    return new LocalTexture(
      surfaceColor,
      surfaceColor.multiply(0.4),
      new RGB(0.2, 0.2, 0.2),
      10,
      1
    );
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("backgroundColor", new RGB(1,1,1));
    map.put("lineColor", new RGB(0,0,0));
    map.put("cellSize", 0.5);
    map.put("lineThickness", 0.05);
    
    reader.readFields(map);
    
    return new GridTexture(
      (RGB) map.get("backgroundColor"),
      (RGB) map.get("lineColor"),
      ((Number) map.get("cellSize")).doubleValue(),
      ((Number) map.get("lineThickness")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String GRID_STR = "Constructor: GridTexture(RGB backgroundColor, RGB lineColor, double cellSize, double lineThickness)\n" +
    "Examples:\n" +
    "-1                             # White background, black lines\n" +
    "1,1,1,  0,0,0,  0.3,  0.02          # White background, black lines, smaller grid\n" +
    "0.9,0.9,1,  0.2,0.2,0.8,  0.4,  0.03 # Light blue background, blue lines\n" +
    "Enter values after ###\n###\n";
    return GRID_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    
    Texture texture = null;
    String str = info.trim();
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return texture;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "").replaceAll(" ", "");
    
    if (str.equals("-1")) return new GridTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 8) return texture;
    
    try {
      double br = Double.parseDouble(split[0]);
      double bg = Double.parseDouble(split[1]);
      double bb = Double.parseDouble(split[2]);
      double lr = Double.parseDouble(split[3]);
      double lg = Double.parseDouble(split[4]);
      double lb = Double.parseDouble(split[5]);
      double cellSize = Double.parseDouble(split[6]);
      double thickness = Double.parseDouble(split[7]);
      
      texture = new GridTexture(new RGB(br, bg, bb), new RGB(lr, lg, lb), cellSize, thickness);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"backgroundColor", backgroundColor},
      {"lineColor", lineColor},
      {"cellSize", cellSize},
      {"lineThickness", lineThickness}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getBackgroundColor() { return backgroundColor; }
  public RGB getLineColor() { return lineColor; }
  public double getCellSize() { return cellSize; }
  public double getLineThickness() { return lineThickness; }
}
