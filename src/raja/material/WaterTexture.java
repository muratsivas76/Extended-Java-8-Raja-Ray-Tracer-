package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class WaterTexture implements Texture, java.io.Serializable, Writable {
  private double time = 0.0;
  
  public WaterTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double wave = Math.sin(time * 2 + p.x * 3 + p.z * 2) * 0.1;
    double ripple = Math.cos(time * 3 + p.y * 4) * 0.05;
    
    RGB waterColor = new RGB(
      0.2 + wave * 0.1,
      0.4 + ripple * 0.2,
      0.8 + wave * 0.1
    );
    
    return new LocalTexture(
      waterColor,
      new RGB(0.7, 0.8, 0.9),
      new RGB(0.1, 0.2, 0.3),
      200, 100
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: WaterTexture();\nPress Set button please.";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    return new WaterTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new WaterTexture();
  }
}
