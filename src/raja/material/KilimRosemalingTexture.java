package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class KilimRosemalingTexture implements Texture, java.io.Serializable, Writable {
  private RGB kilimColor = new RGB(0.77, 0.0, 0.0);      // Kırmızı kilim
  private RGB rosemalingColor = new RGB(0.0, 0.39, 0.39); // Turkuaz rosemaling
  private RGB accentColor = new RGB(1.0, 0.84, 0.0);     // Altın sarısı aksan
  private double time = 0.0;
  
  public KilimRosemalingTexture() {}
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    double x = p.x * 12.0;
    double y = p.y * 12.0;
    double z = p.z * 12.0;
    
    // Basitleştirilmiş kültürel pattern'ler
    double kilimPattern1 = Math.abs(Math.sin(x * 2.0) + Math.cos(y * 2.0));
    double kilimPattern2 = (Math.floor(x * 1.2) + Math.floor(y * 1.2)) % 2.0;
    
    double rosePattern1 = Math.sin(x * 1.5) * Math.cos(y * 1.5 + Math.sin(z * 0.8));
    double rosePattern2 = Math.abs(Math.sin(x * 2.5 + y * 1.8) + Math.cos(y * 2.2));
    
    // Kültürel fusion pattern
    double combinedPattern = (kilimPattern1 * 0.3 + kilimPattern2 * 0.2 +
    rosePattern1 * 0.3 + rosePattern2 * 0.2);
    double normalizedPattern = (combinedPattern + 1.0) * 0.5;
    
    RGB surfaceColor;
    if (normalizedPattern < 0.3) {
      // Kilim arkaplan
      surfaceColor = kilimColor;
      } else if (normalizedPattern < 0.6) {
      // Rosemaling çiçek desenleri
      double intensity = (normalizedPattern - 0.3) / 0.3;
      surfaceColor = blendColors(rosemalingColor, lightenColor(rosemalingColor, 0.2), intensity);
      } else if (normalizedPattern < 0.8) {
      // Altın aksan detaylar
      surfaceColor = accentColor;
      } else {
      // Kenar bordürler
      double intensity = (normalizedPattern - 0.8) / 0.2;
      RGB borderColor = blendColors(kilimColor, rosemalingColor, 0.5);
      surfaceColor = darkenColor(borderColor, intensity * 0.4);
    }
    
    return new LocalTexture(
      surfaceColor,
      new RGB(0.1, 0.1, 0.1), // Düşük reflection
      new RGB(0.0, 0.0, 0.0), // Işık yok
      25,
      10
    );
  }
  
  // Basit color utility fonksiyonları
  private RGB blendColors(RGB color1, RGB color2, double ratio) {
    return new RGB(
      color1.getR() * (1 - ratio) + color2.getR() * ratio,
      color1.getG() * (1 - ratio) + color2.getG() * ratio,
      color1.getB() * (1 - ratio) + color2.getB() * ratio
    );
  }
  
  private RGB lightenColor(RGB color, double amount) {
    return new RGB(
      Math.min(1.0, color.getR() + amount),
      Math.min(1.0, color.getG() + amount),
      Math.min(1.0, color.getB() + amount)
    );
  }
  
  private RGB darkenColor(RGB color, double amount) {
    return new RGB(
      Math.max(0.0, color.getR() - amount),
      Math.max(0.0, color.getG() - amount),
      Math.max(0.0, color.getB() - amount)
    );
  }
  
  public void updateTime(double t) { this.time = t; }
  
  @Override
  public String getUsageInformation() {
    return "Constructor is: KilimRosemalingTexture();\nPress Set button please.";
  }
  
  @Override
  public String toExampleString() {
    return "KilimRosemalingTexture";
  }
  
  @Override
  public Texture getInstance(String info) {
    return new KilimRosemalingTexture();
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"kilimColor", kilimColor},
      {"rosemalingColor", rosemalingColor},
      {"accentColor", accentColor},
      {"time", Double.valueOf(time)}
    };
    writer.writeFields(fields);
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    return new KilimRosemalingTexture();
  }
}
