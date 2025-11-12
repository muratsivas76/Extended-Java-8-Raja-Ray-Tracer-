package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class StripesTexture implements Texture, java.io.Serializable, Writable {
  private RGB stripeColor1;
  private RGB stripeColor2;
  private boolean horizontal;
  private double stripeWidth;
  
  // Default constructor
  public StripesTexture() {
    this(new RGB(1, 1, 1), new RGB(0, 0, 0), true, 0.2);
  }
  
  // Custom constructor
  public StripesTexture(RGB stripeColor1, RGB stripeColor2, boolean horizontal, double stripeWidth) {
    this.stripeColor1 = stripeColor1;
    this.stripeColor2 = stripeColor2;
    this.horizontal = horizontal;
    this.stripeWidth = stripeWidth;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double coord = horizontal ? p.y : p.x;
    double position = Math.abs(coord % (stripeWidth * 2));
    RGB surfaceColor = position < stripeWidth ? stripeColor1 : stripeColor2;
    
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
    map.put("stripeColor1", new RGB(1,1,1));
    map.put("stripeColor2", new RGB(0,0,0));
    map.put("horizontal", true);
    map.put("stripeWidth", 0.2);
    
    reader.readFields(map);
    
    return new StripesTexture(
      (RGB) map.get("stripeColor1"),
      (RGB) map.get("stripeColor2"),
      (Boolean) map.get("horizontal"),
      ((Number) map.get("stripeWidth")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String STRIPES_STR = "Constructor: StripesTexture(RGB stripeColor1, RGB stripeColor2, boolean horizontal, double stripeWidth)\n" +
    "Examples:\n" +
    "-1                              # Black-white horizontal stripes\n" +
    "1,0,0,  0,0,1,  true,   0.3     # Red-blue horizontal stripes\n" +
    "0,1,0,  1,0,1,  false,  0.15    # Green-purple vertical stripes\n" +
    "Enter values after ###\n###\n";
    return STRIPES_STR;
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
    
    if (str.equals("-1")) return new StripesTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 8) return texture;
    
    try {
      double r1 = Double.parseDouble(split[0]);
      double g1 = Double.parseDouble(split[1]);
      double b1 = Double.parseDouble(split[2]);
      double r2 = Double.parseDouble(split[3]);
      double g2 = Double.parseDouble(split[4]);
      double b2 = Double.parseDouble(split[5]);
      boolean horiz = Boolean.parseBoolean(split[6]);
      double width = Double.parseDouble(split[7]);
      
      texture = new StripesTexture(new RGB(r1, g1, b1), new RGB(r2, g2, b2), horiz, width);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"stripeColor1", stripeColor1},
      {"stripeColor2", stripeColor2},
      {"horizontal", horizontal},
      {"stripeWidth", stripeWidth}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getStripeColor1() { return stripeColor1; }
  public RGB getStripeColor2() { return stripeColor2; }
  public boolean isHorizontal() { return horizontal; }
  public double getStripeWidth() { return stripeWidth; }
}
