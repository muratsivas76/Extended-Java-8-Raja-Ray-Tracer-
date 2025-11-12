package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class AuroraCeramicTexture implements Texture, java.io.Serializable, Writable {
  private RGB baseColor = new RGB(0.96, 0.96, 0.86);    // Açık bej seramik
  private RGB auroraColor = new RGB(0.0, 1.0, 0.5);     // Yeşil aurora
  private double auroraIntensity = 0.45;
  private double time = 0.0;
  
  public AuroraCeramicTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double x = p.x * 7.0;
    double y = p.y * 7.0;
    double z = p.z * 7.0;
    
    // Aurora akış pattern'leri
    double flow1 = Math.sin(x * 1.2 + Math.cos(y * 0.8) + Math.sin(z * 1.5));
    double flow2 = Math.cos(x * 0.7 + Math.sin(y * 1.3) + Math.cos(z * 0.9));
    double flow3 = Math.sin(x * 2.1 + y * 1.7 + z * 0.5);
    
    double auroraPattern = (flow1 * 0.4 + flow2 * 0.3 + flow3 * 0.3);
    double normalizedPattern = (auroraPattern + 1.0) * 0.5;
    
    // Zaman efekti
    double timeEffect = Math.sin(time * 2.0 + y * 0.5) * 0.2 + 0.8;
    
    RGB surfaceColor;
    if (normalizedPattern < auroraIntensity) {
      // Aurora glow efekti
      double intensity = normalizedPattern / auroraIntensity * timeEffect;
      surfaceColor = createAuroraGlow(baseColor, auroraColor, intensity);
      } else {
      // Seramik base with subtle glow
      double intensity = (normalizedPattern - auroraIntensity) / (1.0 - auroraIntensity);
      surfaceColor = addCeramicGlow(baseColor, auroraColor, intensity * 0.3 * timeEffect);
    }
    
    return new LocalTexture(
      surfaceColor,
      new RGB(0.25, 0.25, 0.25), // Orta reflection
      new RGB(0.0, 0.0, 0.0),    // Işık yok
      55, 10              // Seramik parlaklık
    );
  }
  
  private RGB createAuroraGlow(RGB base, RGB glow, double intensity) {
    return new RGB(
      base.getR() * (1 - intensity) + glow.getR() * intensity + glow.getR() * intensity * 0.5,
      base.getG() * (1 - intensity) + glow.getG() * intensity + glow.getG() * intensity * 0.5,
      base.getB() * (1 - intensity) + glow.getB() * intensity + glow.getB() * intensity * 0.5
    );
  }
  
  private RGB addCeramicGlow(RGB base, RGB glow, double intensity) {
    return new RGB(
      Math.min(1.0, base.getR() + glow.getR() * intensity * 0.3),
      Math.min(1.0, base.getG() + glow.getG() * intensity * 0.4),
      Math.min(1.0, base.getB() + glow.getB() * intensity * 0.2)
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: AuroraCeramicTexture();\nPress Set button please.";
  }
  
  @Override
  public String toExampleString() {
    return "AuroraCeramicTexture";
  }
  
  @Override
  public Texture getInstance(String info) {
    return new AuroraCeramicTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"baseColor", baseColor},
      {"auroraColor", auroraColor},
      {"auroraIntensity", Double.valueOf(auroraIntensity)},
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new AuroraCeramicTexture();
  }
}
