// Murat Inan
package raja.light;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class SpotLightSource implements LightSource, java.io.Serializable, Writable
{
  private RGB light;
  private Point3D origin;
  private Vector3D direction;
  private double cutoffAngle; // in degrees
  private double exponent;    // falloff exponent
  
  public SpotLightSource(Point3D origin, Vector3D direction, double cutoffAngle, double exponent, RGB light)
  {
    this.origin = origin;
    this.direction = direction.normalization();
    this.cutoffAngle = cutoffAngle;
    this.exponent = exponent;
    this.light = light;
  }
  
  public SpotLightSource(double x, double y, double z,
    double dx, double dy, double dz,
  double cutoffAngle, double exponent, RGB light)
  {
    this.origin = new Point3D(x, y, z);
    this.direction = new Vector3D(dx, dy, dz).normalization();
    this.cutoffAngle = cutoffAngle;
    this.exponent = exponent;
    this.light = light;
  }
  
  public static Object build(ObjectReader reader) throws java.io.IOException
  {
    HashMap map = new HashMap();
    map.put("origin", null);
    map.put("direction", null);
    map.put("cutoffAngle", null);
    map.put("exponent", null);
    map.put("light", null);
    
    reader.readFields(map);
    
    return new SpotLightSource((Point3D) map.get("origin"),
      (Vector3D) map.get("direction"),
      (Double) map.get("cutoffAngle"),
      (Double) map.get("exponent"),
    (RGB) map.get("light"));
  }
  
  public LightRay getLightRay(Point3D p)
  {
    Vector3D toLight = new Vector3D(p, origin);
    double distance = toLight.magnitude();
    Vector3D toLightNormalized = toLight.normalization();
    
    // Calculate angle between spotlight direction and vector to point
    double cosTheta = Vector3D.dotProduct(Vector3D.opposite(direction), toLightNormalized);
    double theta = Math.acos(cosTheta) * 180.0 / Math.PI;
    
    // If point is outside cutoff angle, no light
    if (theta > cutoffAngle) {
      return null;
    }
    
    // Calculate attenuation based on angle
    double cosCutoff = Math.cos(cutoffAngle * Math.PI / 180.0);
    double attenuation = Math.pow(cosTheta, exponent);
    
    // Apply attenuation to light intensity
    RGB attenuatedLight = new RGB(
      light.getR() * attenuation,
      light.getG() * attenuation,
      light.getB() * attenuation
    );
    
    return new LightRay(p, origin, attenuatedLight);
  }
  
  public RGB getMax()
  {
    return light;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String SPOT_LIGHT_STR = "Constructor is: SpotLightSource(double x, double y, double z, double dx, double dy, double dz, double cutoffAngle, double exponent, RGB light);\nExample:\n-3,0,5,  0,1,0,  5.0, 25,  1.0, 1.0, 1.0\nEnter your values after three diyez symbol\n###\n";
    return SPOT_LIGHT_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public LightSource getInstance(String info)
  {
    this.exampleString = info;
    
    LightSource ls = new SpotLightSource(
      -3.0, 0.0, 5.0,
      0, 1, 0,
      5.0, 25.0,
      RGB.white
    );
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return ls;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    if (str.length () < 11) return ls;
    
    String [] split = str.split (",");
    if (split == null) return ls;
    if (split.length < 6) return ls;
    
    try {
      double px = Double.parseDouble(split[0]);
      double py = Double.parseDouble(split[1]);
      double pz = Double.parseDouble(split[2]);
      
      double dx = Double.parseDouble(split[3]);
      double dy = Double.parseDouble(split[4]);
      double dz = Double.parseDouble(split[5]);
      
      double ca = Double.parseDouble(split[6]);
      double ep = Double.parseDouble(split[7]);
      
      double r = Double.parseDouble(split[8]);
      double g = Double.parseDouble(split[9]);
      double b = Double.parseDouble(split[10]);
      
      ls = new SpotLightSource(px, py, pz, dx, dy, dz, ca, ep, new RGB(r, g, b));
      return ls;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return ls;
    }
  }
  ////////////////
  
  public String toString()
  {
    return ObjectWriter.toString(this);
  }
  
  public void write(ObjectWriter writer) throws java.io.IOException
  {
    Object[][] fields = {
      { "origin", origin },
      { "direction", direction },
      { "cutoffAngle", cutoffAngle },
      { "exponent", exponent },
      { "light", light }
    };
    writer.writeFields(fields);
  }
  
}
