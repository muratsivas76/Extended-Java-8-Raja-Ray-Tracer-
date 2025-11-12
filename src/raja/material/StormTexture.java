package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class StormTexture implements Texture, java.io.Serializable, Writable {
  private double time = 0.0;
  
  public StormTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double cloud = Math.sin(time + p.x * 2 + p.y * 3) * 0.2;
    double lightning = Math.random() > 0.98 ? 1.0 : 0.0;
    
    RGB stormColor = new RGB(
      0.3 + cloud * 0.1,
      0.3 + cloud * 0.1,
      0.4 + cloud * 0.1
    );
    
    RGB emissive = new RGB(
      lightning * 0.8,
      lightning * 0.9,
      lightning * 1.0
    );
    
    return new LocalTexture(
      stormColor,
      new RGB(0.1, 0.1, 0.2),
      emissive,
      30, 10
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: StormTexture();\nPress Set button please.";
  }
  
  private String exampleString = "null";
  
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public Texture getInstance(String info) {
    this.exampleString = info;
    return new StormTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new StormTexture();
  }
}
