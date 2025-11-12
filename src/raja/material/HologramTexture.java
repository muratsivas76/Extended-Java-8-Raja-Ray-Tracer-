package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class HologramTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor = new RGB(0.3, 0.8, 1.0);
  private double time = 0.0;
  
  public HologramTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double pulse = (Math.sin(time * 5) + 1) * 0.3;
    RGB emissive = new RGB(baseColor.getR() + pulse, baseColor.getG() + pulse, baseColor.getB());
    
    return new LocalTexture(
      new RGB(0.1, 0.1, 0.1),  // kd
      new RGB(0.0, 0.0, 0.0),  // kr
      emissive,                 // kt
      100,                      // ns
      200                       // nt
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: HologramTexture();\nPress Set button please.";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    return new HologramTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"baseColor", baseColor},
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new HologramTexture();
  }
}
