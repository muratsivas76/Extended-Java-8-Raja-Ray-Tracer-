// Murat Inan
package raja.material;

import raja.*;
import raja.shape.*;
import raja.io.*;

import java.util.HashMap;

/**
 * AfricanKenteTexture generates a vibrant, high-contrast striped pattern
 * inspired by traditional West African Kente cloth. The design features
 * bold vertical bands of saturated colors with sharp transitions, suitable
 * for cultural, decorative, or stylized rendering contexts.
 *
 * The texture is mapped along the X-axis (vertical stripes) and repeats
 * uniformly in Y. It assumes the surface normal aligns with the Z-axis
 * for front-facing application (e.g., banners, walls, or clothing planes).
 */
public class AfricanKenteTexture implements Texture, java.io.Serializable, Writable {
  private RGB[] stripeColors;
  private double scale;
  private int phase;
  
  // Phong parameters for matte fabric (non-shiny, low reflectivity)
  private static final double AMBIENT_FACTOR = 0.5;
  private static final double SPECULAR_FACTOR = 0.05;
  private static final RGB SPECULAR_COLOR = new RGB(0.9, 0.9, 0.9);
  private static final int REFLECTIVITY = 1;
  private static final int SHININESS = 5;
  
  /**
   * Default constructor with a classic Kente-inspired palette:
   * red, yellow, green, black — symbolizing values like sacrifice, wealth,
   * growth, and spiritual strength.
   */
  public AfricanKenteTexture() {
    this(
      new RGB[]{
        new RGB(0.8, 0.1, 0.1),   // red
        new RGB(0.95, 0.9, 0.2),  // yellow
        new RGB(0.1, 0.6, 0.2),   // green
        new RGB(0.0, 0.0, 0.0)    // black
      },
      8.0,
      0
    );
  }
  
  /**
   * Constructs an AfricanKenteTexture with a custom stripe sequence.
   *
   * @param stripeColors Array of colors defining the repeating stripe pattern.
   * @param scale        Controls stripe density (higher = more stripes per unit).
   * @param phase        Offset to shift the starting stripe (useful for variation).
   */
  public AfricanKenteTexture(RGB[] stripeColors, double scale, int phase) {
    if (stripeColors == null || stripeColors.length == 0) {
      throw new IllegalArgumentException("Stripe color array must not be empty.");
    }
    this.stripeColors = stripeColors.clone();
    this.scale = Math.max(0.1, scale);
    this.phase = phase;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Map stripes along X (vertical bands when viewed front-on)
    double x = p.x * scale;
    int stripeIndex = ((int) Math.floor(x) + phase) % stripeColors.length;
    if (stripeIndex < 0) stripeIndex += stripeColors.length;
    
    RGB color = stripeColors[stripeIndex];
    
    return new LocalTexture(
      color,
      color.multiply(AMBIENT_FACTOR),
      SPECULAR_COLOR.multiply(SPECULAR_FACTOR),
      SHININESS,
      REFLECTIVITY
    );
  }
  
  // --- IO Support ---
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("stripeColors", null);
    map.put("scale", 8.0);
    map.put("phase", 0);
    
    reader.readFields(map);
    
    // List'ten RGB[]'ye çevir
    java.util.List<RGB> colorsList = (java.util.List<RGB>) map.get("stripeColors");
    RGB[] colorsArray = colorsList.toArray(new RGB[0]);
    
    return new AfricanKenteTexture(
      colorsArray,
      ((Number) map.get("scale")).doubleValue(),
      ((Number) map.get("phase")).intValue()
    );
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String AFRICANKENTE_STR = "Constructor is: AfricanKenteTexture(RGB[] stripeColors, double scale, int phase);\nExample:\n1,0,0,  0,1,0,  0,0,1,  6.0,  0\n-1 return empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return AFRICANKENTE_STR;
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
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    if (str.equals("-1")) return new AfricanKenteTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //1,0,0,  0,1,0,  0,0,1,  6.0,  0
    try {
      double c1r = Double.parseDouble(split[0]);
      double c1g = Double.parseDouble(split[1]);
      double c1b = Double.parseDouble(split[2]);
      
      double c2r = Double.parseDouble(split[3]);
      double c2g = Double.parseDouble(split[4]);
      double c2b = Double.parseDouble(split[5]);
      
      double c3r = Double.parseDouble(split[6]);
      double c3g = Double.parseDouble(split[7]);
      double c3b = Double.parseDouble(split[8]);
      
      RGB[] rgbs = new RGB[3];
      rgbs[0] = new RGB(c1r, c1g, c1b);
      rgbs[1] = new RGB(c2r, c2g, c2b);
      rgbs[2] = new RGB(c3r, c3g, c3b);
      
      double sc = Double.parseDouble(split[9]);
      int ph = Integer.parseInt(split[10]);
      
      texture = new AfricanKenteTexture(rgbs, sc, ph);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    // RGB array'ini manuel olarak yaz
    writer.write("stripeColors = [");
    for(int i = 0; i < stripeColors.length; i++) {
      writer.writeObject(stripeColors[i]);
      if(i < stripeColors.length - 1) writer.write(", ");
    }
    writer.write("]");
    
    // Diğer field'ları normal şekilde yaz
    Object[][] otherFields = {
      {"scale", scale},
      {"phase", phase}
    };
    writer.writeFields(otherFields);
  }
  
  // --- Getters ---
  public RGB[] getStripeColors() { return stripeColors.clone(); }
  public double getScale() { return scale; }
  public int getPhase() { return phase; }
  
}

/**
AfricanKenteTexture kente = new AfricanKenteTexture();

RGB[] myPalette = {
new RGB(1.0, 0.0, 0.0),   // kırmızı
new RGB(0.0, 0.0, 1.0),   // mavi
new RGB(1.0, 1.0, 0.0)    // sarı
};
AfricanKenteTexture kente = new AfricanKenteTexture(myPalette, 6.0, 0);
 */
