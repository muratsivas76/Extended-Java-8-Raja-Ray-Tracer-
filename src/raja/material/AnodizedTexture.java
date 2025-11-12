// Murat Inan
package raja.material;

import java.util.HashMap;

import raja.*;
import raja.shape.*;
import raja.io.*;

public class AnodizedTexture implements Texture, java.io.Serializable, Writable
{
  private RGB baseColor;
  private double reflectivity;
  private double shininess;
  private int ns, nt;
  private double patternScale;
  
  public AnodizedTexture(RGB baseColor, double reflectivity, double shininess,
  int ns, int nt, double patternScale)
  {
    this.baseColor = baseColor;
    this.reflectivity = reflectivity;
    this.shininess = shininess;
    this.ns = ns;
    this.nt = nt;
    this.patternScale = patternScale;
  }
  
  // Basit constructor - varsayılan değerlerle
  public AnodizedTexture(RGB baseColor)
  {
    this(baseColor, 0.85, 120.0, 600, 1, 0.8);
  }
  
  // Varsayılan constructor
  public AnodizedTexture()
  {
    this(new RGB(0.15, 0.25, 0.75), 0.85, 120.0, 600, 1, 0.8);
  }
  
  // Predefined anodized colors - OPTIMIZED VALUES
  public static AnodizedTexture createAnodizedBlue()
  {
    // Classic anodized blue - deep rich color
    return new AnodizedTexture(new RGB(0.15, 0.25, 0.75), 0.85, 120.0, 600, 1, 0.8);
  }
  
  public static AnodizedTexture createAnodizedRed()
  {
    // Deep anodized red with slight purple undertone
    return new AnodizedTexture(new RGB(0.75, 0.12, 0.18), 0.82, 110.0, 550, 1, 0.9);
  }
  
  public static AnodizedTexture createAnodizedGreen()
  {
    // Rich emerald green
    return new AnodizedTexture(new RGB(0.12, 0.58, 0.25), 0.83, 115.0, 580, 1, 0.85);
  }
  
  public static AnodizedTexture createAnodizedPurple()
  {
    // Deep purple with blue hints
    return new AnodizedTexture(new RGB(0.48, 0.15, 0.72), 0.84, 125.0, 620, 1, 0.75);
  }
  
  public static AnodizedTexture createAnodizedGold()
  {
    // Warm champagne gold
    return new AnodizedTexture(new RGB(0.88, 0.72, 0.18), 0.90, 140.0, 700, 1, 0.6);
  }
  
  public static AnodizedTexture createAnodizedTitanium()
  {
    // Cool titanium gray with subtle rainbow
    return new AnodizedTexture(new RGB(0.42, 0.44, 0.48), 0.88, 130.0, 650, 1, 1.0);
  }
  
  public static AnodizedTexture createAnodizedBlack()
  {
    // Matte black anodized
    return new AnodizedTexture(new RGB(0.08, 0.08, 0.10), 0.75, 90.0, 450, 1, 1.2);
  }
  
  public static AnodizedTexture createAnodizedPink()
  {
    // Rose gold / pink anodized
    return new AnodizedTexture(new RGB(0.82, 0.45, 0.52), 0.86, 125.0, 600, 1, 0.7);
  }
  
  @Override
  public LocalTexture getLocalTexture(Point3D p)
  {
    // Enhanced thin-film interference pattern
    double filmThickness = calculateFilmThickness(
      p.x * patternScale,
      p.y * patternScale,
      p.z * patternScale
    );
    
    // Apply enhanced interference effect
    RGB anodizedColor = applyEnhancedInterference(baseColor, filmThickness);
    
    RGB kr = anodizedColor.multiply(reflectivity);
    
    return new LocalTexture(anodizedColor, kr, new RGB(0.0, 0.0, 0.0), ns, nt);
  }
  
  private double calculateFilmThickness(double x, double y, double z)
  {
    // Multi-scale smooth noise for realistic oxide layer variation
    // Frequencies optimized for natural-looking anodized surface
    
    // Large-scale variation (main color zones)
    double large = Math.sin(x * 0.6) * Math.cos(y * 0.55) * Math.sin(z * 0.5);
    
    // Medium-scale variation (secondary patterns)
    double medium = Math.sin(x * 1.3 + y * 1.1) *
    Math.cos(z * 1.5 - x * 0.8) *
    Math.sin(y * 1.4 + z * 0.9);
    
    // Fine-scale variation (micro detail)
    double fine = Math.sin(x * 2.8 + z * 2.5) *
    Math.cos(y * 3.1 - z * 2.2) *
    Math.sin(x * 2.9 + y * 2.6);
    
    // Very fine detail (adds texture richness)
    double veryFine = Math.sin(x * 4.5 + y * 4.2 + z * 3.8) *
    Math.cos(x * 3.9 - y * 4.1);
    
    // Weighted combination for natural appearance
    double thickness = large * 0.45 +
    medium * 0.30 +
    fine * 0.18 +
    veryFine * 0.07;
    
    // Apply smoothstep for more organic transitions
    thickness = thickness * 0.5 + 0.5; // [0, 1]
    return smoothstep(0.0, 1.0, thickness);
  }
  
  private RGB applyEnhancedInterference(RGB base, double thickness)
  {
    double r = base.getR();
    double g = base.getG();
    double b = base.getB();
    
    // Enhanced thin-film interference simulation
    // Phase shifts optimized for realistic anodized appearance
    
    // Red wavelength interference (~650nm)
    double redPhase = thickness * Math.PI * 2.2;
    double redInterference = 0.5 + 0.5 * Math.cos(redPhase);
    
    // Green wavelength interference (~550nm)
    double greenPhase = thickness * Math.PI * 2.6;
    double greenInterference = 0.5 + 0.5 * Math.cos(greenPhase);
    
    // Blue wavelength interference (~450nm)
    double bluePhase = thickness * Math.PI * 3.1;
    double blueInterference = 0.5 + 0.5 * Math.cos(bluePhase);
    
    // Apply interference with enhanced modulation depth
    double interferenceStrength = 0.35; // Increased for more visible effect
    r *= 0.75 + interferenceStrength * redInterference;
    g *= 0.75 + interferenceStrength * greenInterference;
    b *= 0.75 + interferenceStrength * blueInterference;
    
    // Subtle hue shift based on oxide thickness
    // Creates the characteristic anodized color travel
    double t1 = smoothstep(0.0, 0.5, thickness);
    double t2 = smoothstep(0.5, 1.0, thickness);
    
    // Color journey: base -> warmer mid-tones -> cooler highlights
    r *= 0.85 + 0.25 * t2 + 0.10 * Math.sin(thickness * Math.PI);
    g *= 0.90 + 0.15 * t1 - 0.10 * t2;
    b *= 0.95 + 0.20 * (1.0 - t1) + 0.15 * Math.cos(thickness * Math.PI * 1.5);
    
    // Add subtle saturation boost in mid-tones
    double saturationBoost = 1.0 + 0.15 * Math.sin(thickness * Math.PI);
    double avg = (r + g + b) / 3.0;
    r = avg + (r - avg) * saturationBoost;
    g = avg + (g - avg) * saturationBoost;
    b = avg + (b - avg) * saturationBoost;
    
    // Clamp to valid range
    r = Math.max(0.0, Math.min(1.0, r));
    g = Math.max(0.0, Math.min(1.0, g));
    b = Math.max(0.0, Math.min(1.0, b));
    
    return new RGB(r, g, b);
  }
  
  // Enhanced smoothstep with more natural curve
  private double smoothstep(double edge0, double edge1, double x)
  {
    double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
    // Smootherstep (Ken Perlin's improved version)
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
  }
  
  public static Object build(ObjectReader reader)
  throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("baseColor", null);
    map.put("reflectivity", new Double(0.85));
    map.put("shininess", new Double(120.0));
    map.put("ns", new Integer(600));
    map.put("nt", new Integer(1));
    map.put("patternScale", new Double(0.8));
    
    reader.readFields(map);
    
    return new AnodizedTexture((RGB) map.get("baseColor"),
      ((Number) map.get("reflectivity")).doubleValue(),
      ((Number) map.get("shininess")).doubleValue(),
      ((Number) map.get("ns")).intValue(),
      ((Number) map.get("nt")).intValue(),
    ((Number) map.get("patternScale")).doubleValue());
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation() {
    String ANODIZED_STR = "Constructor is: AnodizedTexture(RGB baseColor, double reflectivity, double shininess, int ns, int nt, double patternScale);\nExample:\n0.15,0.25,0.75,  0.85,  120.0,  600,  1,  0.8\n-1 return empty constructor.\nEnter your values after three diyez symbol\n###\n";
    return ANODIZED_STR;
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
    
    if (str.equals("-1")) return new AnodizedTexture();
    
    String [] split = str.split (",");
    if (split == null) return texture;
    
    //0.15,0.25,0.75,  0.85,  120.0,  600,  1,  0.8
    try {
      double r = Double.parseDouble(split[0]);
      double g = Double.parseDouble(split[1]);
      double b = Double.parseDouble(split[2]);
      
      double rf = Double.parseDouble(split[3]);
      double sh = Double.parseDouble(split[4]);
      
      int ns = Integer.parseInt(split[5]);
      int nt = Integer.parseInt(split[6]);
      
      double pt = Double.parseDouble(split[7]);
      
      texture = new AnodizedTexture(new RGB(r, g, b), rf, sh, ns, nt, pt);
      return texture;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  ////////////////
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "baseColor", baseColor },
      { "reflectivity", new Double(reflectivity) },
      { "shininess", new Double(shininess) },
      { "ns", new Integer(ns) },
      { "nt", new Integer(nt) },
      { "patternScale", new Double(patternScale) }
    };
    writer.writeFields(fields);
  }
  
  // Getter methods
  public RGB getBaseColor() { return baseColor; }
  public double getReflectivity() { return reflectivity; }
  public double getShininess() { return shininess; }
  public int getNs() { return ns; }
  public int getNt() { return nt; }
  public double getPatternScale() { return patternScale; }
  
  // Setter methods
  public void setBaseColor(RGB baseColor) { this.baseColor = baseColor; }
  public void setReflectivity(double reflectivity) { this.reflectivity = reflectivity; }
  public void setShininess(double shininess) { this.shininess = shininess; }
  public void setNs(int ns) { this.ns = ns; }
  public void setNt(int nt) { this.nt = nt; }
  public void setPatternScale(double patternScale) { this.patternScale = patternScale; }
  
}
