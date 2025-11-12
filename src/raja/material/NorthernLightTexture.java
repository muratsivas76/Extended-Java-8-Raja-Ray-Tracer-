package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class NorthernLightTexture implements Texture, java.io.Serializable, Writable {
  private RGB primaryAurora = new RGB(0.0, 1.0, 0.5);   // Yeşil aurora
  private RGB secondaryAurora = new RGB(0.0, 0.75, 1.0); // Mavi aurora
  private double time = 0.0;
  
  public NorthernLightTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Basit aurora efekti - orijinalin sadeleştirilmiş hali
    double x = p.x * 8.0;
    double y = p.y * 12.0;
    
    // Aurora dalga pattern'i
    double curtain1 = Math.sin(x * 0.7 + Math.sin(y * 1.2) * 3.0);
    double curtain2 = Math.cos(y * 1.5 + Math.sin(x * 0.9) * 2.5);
    
    double auroraPattern = (curtain1 * 0.6 + curtain2 * 0.4);
    double normalizedPattern = (auroraPattern + 1.0) * 0.5;
    
    // Zaman efekti (basit)
    double timeEffect = Math.sin(time + y * 0.3) * 0.2 + 0.8;
    
    RGB auroraColor;
    if (normalizedPattern < 0.6) {
      // Yeşil aurora
      double ratio = normalizedPattern / 0.6;
      auroraColor = new RGB(
        primaryAurora.getR() * ratio * timeEffect,
        primaryAurora.getG() * ratio * timeEffect,
        primaryAurora.getB() * ratio * timeEffect
      );
      } else {
      // Mavi aurora
      double ratio = (normalizedPattern - 0.6) / 0.4;
      auroraColor = new RGB(
        secondaryAurora.getR() * ratio * timeEffect,
        secondaryAurora.getG() * ratio * timeEffect,
        secondaryAurora.getB() * ratio * timeEffect
      );
    }
    
    // Işık yayan aurora efekti
    return new LocalTexture(
      new RGB(0.1, 0.1, 0.2),     // Koyu diffuse
      new RGB(0.0, 0.0, 0.0),     // Reflection yok
      auroraColor,                 // Işık yayan aurora
      100, 50
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: NorthernLightTexture();\nPress Set button please.";
  }
  
  @Override
  public String toExampleString() {
    return "NorthernLightTexture";
  }
  
  @Override
  public Texture getInstance(String info) {
    return new NorthernLightTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"primaryAurora", primaryAurora},
      {"secondaryAurora", secondaryAurora},
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new NorthernLightTexture();
  }
}
