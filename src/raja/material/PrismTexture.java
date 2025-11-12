package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class PrismTexture implements Texture, java.io.Serializable, Writable {
  private double time = 0.0;
  
  public PrismTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double r = (Math.sin(time + p.x * 5) + 1) * 0.5;
    double g = (Math.sin(time + p.y * 5 + 2) + 1) * 0.5;
    double b = (Math.sin(time + p.z * 5 + 4) + 1) * 0.5;
    
    RGB prismColor = new RGB(r, g, b);
    
    return new LocalTexture(
      prismColor,
      new RGB(r * 0.8, g * 0.8, b * 0.8),
      new RGB(0.0, 0.0, 0.0),
      300, 50
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: PrismTexture();\nPress Set button please.";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    return new PrismTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new PrismTexture();
  }
}
