package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class LavaTexture implements Texture, java.io.Serializable, Writable {
  private double time = 0.0;
  
  public LavaTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double heat = (Math.sin(time * 3 + p.y * 5) + 1) * 0.5;
    double glow = (Math.cos(time * 2 + p.x * 4) + 1) * 0.3;
    
    RGB lavaColor = new RGB(
      0.8 + heat * 0.2,
      0.2 + glow * 0.3,
      0.1
    );
    
    return new LocalTexture(
      lavaColor,
      new RGB(0.3, 0.1, 0.0),
      new RGB(heat * 0.4, glow * 0.2, 0.0),
      50,
      10
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: LavaTexture();\nPress Set button please.";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    return new LavaTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new LavaTexture();
  }
}
