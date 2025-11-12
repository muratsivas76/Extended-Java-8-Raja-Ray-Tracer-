package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class ColorWheelTexture implements Texture, java.io.Serializable, Writable {
  private boolean showColorNames;
  private double wheelSize;
  
  // Default constructor
  public ColorWheelTexture() {
    this(true, 0.8);
  }
  
  // Custom constructor
  public ColorWheelTexture(boolean showColorNames, double wheelSize) {
    this.showColorNames = showColorNames;
    this.wheelSize = wheelSize;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double x = p.x - 0.5;
    double y = p.y - 0.5;
    double distance = Math.sqrt(x * x + y * y);
    double angle = Math.atan2(y, x);
    
    if (distance > wheelSize) {
      return new LocalTexture(
        new RGB(0.9, 0.9, 0.9),
        new RGB(0.36, 0.36, 0.36),
        new RGB(0.2, 0.2, 0.2),
        10,
        1
      );
    }
    
    // Convert angle to hue (0 to 1)
    double hue = (angle + Math.PI) / (2 * Math.PI);
    RGB color = hueToRGB(hue);
    
    return new LocalTexture(
      color,
      color.multiply(0.4),
      new RGB(0.2, 0.2, 0.2),
      10,
      1
    );
  }
  
  private RGB hueToRGB(double hue) {
    double r = Math.abs(hue * 6 - 3) - 1;
    double g = 2 - Math.abs(hue * 6 - 2);
    double b = 2 - Math.abs(hue * 6 - 4);
    
    return new RGB(
      Math.max(0, Math.min(1, r)),
      Math.max(0, Math.min(1, g)),
      Math.max(0, Math.min(1, b))
    );
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("showColorNames", true);
    map.put("wheelSize", 0.8);
    
    reader.readFields(map);
    
    return new ColorWheelTexture(
      (Boolean) map.get("showColorNames"),
      ((Number) map.get("wheelSize")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String COLORWHEEL_STR = "Constructor: ColorWheelTexture(boolean showColorNames, double wheelSize)\n" +
    "Examples:\n" +
    "-1             # Default color wheel with names\n" +
    "true,  0.9       # Large wheel with names\n" +
    "false,  0.7      # Smaller wheel without names\n" +
    "Enter values after ###\n###\n";
    return COLORWHEEL_STR;
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
    
    if (str.equals("-1")) return new ColorWheelTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 2) return texture;
    
    try {
      boolean showNames = Boolean.parseBoolean(split[0]);
      double size = Double.parseDouble(split[1]);
      
      texture = new ColorWheelTexture(showNames, size);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"showColorNames", showColorNames},
      {"wheelSize", wheelSize}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public boolean isShowColorNames() { return showColorNames; }
  public double getWheelSize() { return wheelSize; }
  
}
