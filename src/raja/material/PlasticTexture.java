package raja.material;

import raja.*;
import raja.io.*;
import raja.shape.*;
import java.util.HashMap;

public class PlasticTexture implements Texture, java.io.Serializable, Writable {
  private RGB plasticColor;
  private double shininess;
  private double glossiness;
  
  // Default constructor
  public PlasticTexture() {
    this(new RGB(0.8, 0.8, 0.8), 50.0, 0.3);
  }
  
  // Custom constructor
  public PlasticTexture(RGB plasticColor, double shininess, double glossiness) {
    this.plasticColor = plasticColor;
    this.shininess = shininess;
    this.glossiness = glossiness;
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p) {
    // Simple plastic material with specular highlights
    return new LocalTexture(
      plasticColor,
      plasticColor.multiply(0.3),
      new RGB(glossiness, glossiness, glossiness),
      (int) shininess,
      1
    );
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("plasticColor", new RGB(0.8, 0.8, 0.8));
    map.put("shininess", 50.0);
    map.put("glossiness", 0.3);
    
    reader.readFields(map);
    
    return new PlasticTexture(
      (RGB) map.get("plasticColor"),
      ((Number) map.get("shininess")).doubleValue(),
      ((Number) map.get("glossiness")).doubleValue()
    );
  }
  
  @Override
  public String getUsageInformation() {
    String PLASTIC_STR = "Constructor: PlasticTexture(RGB plasticColor, double shininess, double glossiness)\n" +
    "Examples:\n" +
    "-1                   # Default gray plastic\n" +
    "1,0,0,    80,  0.5   # Red shiny plastic\n" +
    "0,0.5,1,  30,  0.2   # Blue matte plastic\n" +
    "Enter values after ###\n###\n";
    return PLASTIC_STR;
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
    str = str.replaceAll("\n", "").replaceAll(" ", "");
    
    if (str.equals("-1")) return new PlasticTexture();
    
    String[] split = str.split(",");
    if (split == null || split.length != 5) return texture;
    
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      double shine = Double.parseDouble(split[3]);
      double gloss = Double.parseDouble(split[4]);
      
      texture = new PlasticTexture(new RGB(r, g, b), shine, gloss);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      {"plasticColor", plasticColor},
      {"shininess", shininess},
      {"glossiness", glossiness}
    };
    writer.writeFields(fields);
  }
  
  // Getters
  public RGB getPlasticColor() { return plasticColor; }
  public double getShininess() { return shininess; }
  public double getGlossiness() { return glossiness; }
}
