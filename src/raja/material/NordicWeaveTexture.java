package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class NordicWeaveTexture implements Texture, java.io.Serializable, Writable {
  private RGB primaryColor = new RGB(0.55, 0.27, 0.07);   // Kahverengi
  private RGB secondaryColor = new RGB(0.0, 0.39, 0.39);  // Turkuaz
  private RGB accentColor = new RGB(0.86, 0.86, 0.86);    // Açık gri
  private double patternScale = 4.0;
  private double time = 0.0;
  
  public NordicWeaveTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double x = p.x * patternScale;
    double y = p.y * patternScale;
    double z = p.z * patternScale;
    
    // Viking + Türk dokuma pattern'leri
    double pattern1 = Math.sin(x * 2.0) * Math.cos(y * 2.0);
    double pattern2 = Math.abs(Math.sin(x * 3.0 + y * 3.0));
    double pattern3 = Math.floor(x * 0.5) + Math.floor(y * 0.5);
    
    double combinedPattern = (pattern1 + pattern2 + pattern3 % 2.0) % 3.0;
    
    RGB surfaceColor;
    if (combinedPattern < 1.0) {
      surfaceColor = primaryColor;    // Kahverengi dokuma
      } else if (combinedPattern < 2.0) {
      surfaceColor = secondaryColor;  // Turkuaz dokuma
      } else {
      surfaceColor = accentColor;     // Gümüş gri aksan
    }
    
    return new LocalTexture(
      surfaceColor,
      new RGB(0.08, 0.08, 0.08),     // Çok düşük reflection
      new RGB(0.0, 0.0, 0.0),        // Işık yok
      15,
      10
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: NordicWeaveTexture();\nPress Set button please.";
  }
  
  @Override
  public String toExampleString() {
    return "NordicWeaveTexture";
  }
  
  @Override
  public Texture getInstance(String info) {
    return new NordicWeaveTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"primaryColor", primaryColor},
      {"secondaryColor", secondaryColor},
      {"accentColor", accentColor},
      {"patternScale", Double.valueOf(patternScale)},
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new NordicWeaveTexture();
  }
}
