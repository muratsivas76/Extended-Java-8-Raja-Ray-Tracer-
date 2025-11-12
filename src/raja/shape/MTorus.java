// Murat Inan
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

/**
 * Represents a torus defined by a major radius (distance from center to tube center)
 * and a minor radius (radius of the tube itself).
 * Supports arbitrary position and orientation via a 4x4 transformation matrix.
 * Uses Ray.EPSILON2 for consistent epsilon values throughout the implementation.
 */
public class MTorus extends BasicForm implements java.io.Serializable, Writable {
  private static final long serialVersionUID = 1L;
  private static final double MAX_DISTANCE = 50.0;
  private static final int MAX_ITERATIONS = 100;
  private static final double SURFACE_THRESHOLD = 0.1;
  
  private double majorRadius;
  private double minorRadius;
  
  private Matrix4 transform = Matrix4.identity();
  private Matrix4 inverseTransform = Matrix4.identity();
  
  // Orientation basis vectors derived from the transform
  private Vector3D ox = new Vector3D(1, 0, 0); // Local X-axis
  private Vector3D oy = new Vector3D(0, 1, 0); // Local Y-axis
  private Vector3D oz = new Vector3D(0, 0, 1); // Local Z-axis
  
  /**
   * Constructs a torus centered at the origin with given radii.
   *
   * @param majorRadius the distance from center to tube center, must be positive
   * @param minorRadius the radius of the tube itself, must be positive and smaller than major radius
   * @throws IllegalArgumentException if radii are invalid
   */
  public MTorus(double majorRadius, double minorRadius) {
    if (majorRadius <= Ray.EPSILON2 || minorRadius <= Ray.EPSILON2) {
      throw new IllegalArgumentException("Radii must be positive");
    }
    if (minorRadius >= majorRadius) {
      throw new IllegalArgumentException("Minor radius must be smaller than major radius");
    }
    this.majorRadius = majorRadius;
    this.minorRadius = minorRadius;
    updateTransforms();
  }
  
  /**
   * Builds an MTorus instance from ObjectReader data.
   */
  public static Object build(ObjectReader reader) throws java.io.IOException {
    HashMap<String, Object> map = new HashMap<>();
    map.put("majorRadius", null);
    map.put("minorRadius", null);
    reader.readFields(map);
    return new MTorus((Double) map.get("majorRadius"), (Double) map.get("minorRadius"));
  }
  
  /**
   * Sets the transformation matrix and updates derived data (inverse and orientation).
   *
   * @param transform the transformation matrix to apply
   */
  public void setTransform(Matrix4 transform) {
    this.transform = transform;
    updateTransforms();
    updateOrientationVectors();
  }
  
  /**
   * Gets the current transformation matrix.
   *
   * @return the current transformation matrix
   */
  public Matrix4 getTransform() {
    return this.transform;
  }
  
  /**
   * Updates the inverse transformation matrix.
   * Falls back to identity matrix if inversion fails.
   */
  private void updateTransforms() {
    Matrix4 inv = this.transform.inverse();
    this.inverseTransform = (inv != null) ? inv : Matrix4.identity();
  }
  
  /**
   * Extracts the local orientation basis vectors from the current transform matrix.
   * These vectors represent the local coordinate system of the torus.
   */
  private void updateOrientationVectors() {
    this.ox = transform.transformVector(new Vector3D(1, 0, 0)).normalization();
    this.oy = transform.transformVector(new Vector3D(0, 1, 0)).normalization();
    this.oz = transform.transformVector(new Vector3D(0, 0, 1)).normalization();
  }
  
  /**
   * Computes the signed distance function (SDF) in local space.
   * Positive: outside, Negative: inside, Zero: on surface.
   *
   * @param P the point in local space to evaluate
   * @return the signed distance from the point to the torus surface
   */
  private double calculateSDFLocal(Point3D P) {
    // Optimize edilmiş SDF hesaplama
    double dx = -P.x;
    double dy = -P.y;
    double dz = -P.z;
    
    // Daha hızlı projection
    double projX = dx * ox.x + dy * ox.y + dz * ox.z;
    double projY = dx * oy.x + dy * oy.y + dz * oy.z;
    
    double radialDist = Math.sqrt(projX * projX + projY * projY);
    double axialDist = Math.sqrt(dx*dx + dy*dy + dz*dz - projX*projX - projY*projY);
    
    // Daha kararlı hesaplama
    return Math.sqrt((radialDist - majorRadius) * (radialDist - majorRadius) + axialDist * axialDist) - minorRadius;
  }
  
  /**
   * Computes the intersection point between the ray and the torus using sphere tracing.
   *
   * @param ray the ray to test for intersection
   * @return the intersection point in world coordinates, or null if no intersection
   */
  // This makes inner shadow too.
  @Override
  public Point3D computeIntersection(Ray ray) {
    Point3D localOrigin = inverseTransform.transformPoint(ray.getOrigin());
    Vector3D localDirection = inverseTransform.transformVector(ray.getDirection()).normalization();
    
    // Shadow ray'ler için daha agresif parametreler
    final double SHADOW_THRESHOLD = 0.01; // Daha yüksek threshold
    final int SHADOW_ITERATIONS = 50;     // Daha az iteration
    
    double t = 0.0;
    Point3D p = localOrigin;
    
    for (int i = 0; i < SHADOW_ITERATIONS; i++) {
      double dist = calculateSDFLocal(p);
      
      // Shadow ray'ler için daha liberal intersection test
      if (Math.abs(dist) < SHADOW_THRESHOLD) {
        if (hasLG(ray.getOrigin())) {
          return null;
        }
        Point3D localHit = new Point3D(
          p.x + localDirection.x * dist,
          p.y + localDirection.y * dist,
          p.z + localDirection.z * dist
        );
        return transform.transformPoint(localHit);
      }
      
      // Negatif distance durumunda (içerideysek) hemen return
      if (dist < 0) {
        Point3D localHit = new Point3D(
          p.x + localDirection.x * Math.abs(dist),
          p.y + localDirection.y * Math.abs(dist),
          p.z + localDirection.z * Math.abs(dist)
        );
        return transform.transformPoint(localHit);
      }
      
      t += Math.abs(dist);
      if (t > MAX_DISTANCE) break;
      
      p = new Point3D(
        p.x + localDirection.x * dist,
        p.y + localDirection.y * dist,
        p.z + localDirection.z * dist
      );
    }
    return null;
  }
  
  // This make semi-torus
  /**
  @Override
  public Point3D computeIntersection(Ray ray) {
  Point3D localOrigin = inverseTransform.transformPoint(ray.getOrigin());
  Vector3D localDirection = inverseTransform.transformVector(ray.getDirection()).normalization();
  
  double t = 0.0;
  Point3D p = localOrigin;
  
  for (int i = 0; i < MAX_ITERATIONS; i++) {
    double dist = calculateSDFLocal(p);
  if (Math.abs(dist) < SURFACE_THRESHOLD) {
    // Işın geldiği yüzeyin normali ile ışın yönünü karşılaştır
  Vector3D normal = computeNormal(transform.transformPoint(p));
  double dot = Vector3D.dotProduct(normal, ray.getDirection());
  
  // Sadece ön yüzeylerden gölge düşsün (back-face culling)
  if (dot > 0) { // Back face - gölge düşürme
    return null;
  }
  
  if (hasLG(ray.getOrigin())) {
    return null;
  }
  return transform.transformPoint(p);
  }
  t += dist;
  if (t > MAX_DISTANCE) return null;
  p = new Point3D(p.x + localDirection.x * dist, p.y + localDirection.y * dist, p.z + localDirection.z * dist);
  }
  return null;
  }
   */
  
  /**
   * Computes the surface normal at the given point on the torus.
   *
   * @param worldPoint the point on the surface in world coordinates
   * @return the normalized surface normal vector
   */
  @Override
  public Vector3D computeNormal(Point3D worldPoint) {
    // Basit ve garantili normal hesapla
    Point3D localPoint = inverseTransform.transformPoint(worldPoint);
    
    // Merkeze göre vektör
    Vector3D toCenter = new Vector3D(localPoint, new Point3D(0, 0, 0));
    
    // Düzlem projeksiyonu
    double projX = Vector3D.dotProduct(toCenter, ox);
    double projY = Vector3D.dotProduct(toCenter, oy);
    
    Vector3D radial = new Vector3D(
      ox.x * projX + oy.x * projY,
      ox.y * projX + oy.y * projY,
      ox.z * projX + oy.z * projY
    );
    
    Vector3D localNormal = new Vector3D(localPoint.x - radial.x,
      localPoint.y - radial.y,
    localPoint.z - radial.z).normalization();
    
    return transform.transformVector(localNormal).normalization();
  }
  
  /**
   * Computes normal using finite differences for numerical stability in degenerate cases.
   *
   * @param localPoint the point in local coordinates
   * @return the normalized normal vector
   */
  private Vector3D computeNormalFiniteDifference(Point3D localPoint) {
    final double DELTA = 1e-6;
    
    double sdfCenter = calculateSDFLocal(localPoint);
    
    double sdfX = calculateSDFLocal(new Point3D(localPoint.x + DELTA, localPoint.y, localPoint.z));
    double sdfY = calculateSDFLocal(new Point3D(localPoint.x, localPoint.y + DELTA, localPoint.z));
    double sdfZ = calculateSDFLocal(new Point3D(localPoint.x, localPoint.y, localPoint.z + DELTA));
    
    Vector3D gradient = new Vector3D(
      (sdfX - sdfCenter) / DELTA,
      (sdfY - sdfCenter) / DELTA,
      (sdfZ - sdfCenter) / DELTA
    );
    
    Vector3D localNormal = gradient.normalization();
    return transform.transformVector(localNormal).normalization();
  }
  
  /**
   * Checks if the point is inside or on the surface of the torus.
   *
   * @param p the point to test in world coordinates
   * @return true if the point is inside or on the surface, false otherwise
   */
  @Override
  public boolean exactlyContains(Point3D p) {
    Point3D localPoint = inverseTransform.transformPoint(p);
    return calculateSDFLocal(localPoint) <= Ray.EPSILON2;
  }
  
  /**
   * Checks if the point is strictly inside the torus (not on surface).
   *
   * @param p the point to test in world coordinates
   * @return true if the point is strictly inside, false otherwise
   */
  @Override
  public boolean exactlyStrictlyContains(Point3D p) {
    Point3D localPoint = inverseTransform.transformPoint(p);
    return calculateSDFLocal(localPoint) < -Ray.EPSILON2;
  }
  
  // ADDED by Murat Inan
  @Override
  public String getUsageInformation()
  {
    String TORUS_STR = "Constructor is: MTorus(double majorRadius, double minorRadius);\nExample -last value is volume-:\n0.9,  0.4,  1\nEnter your values after three diyez symbol\n###\n";
    return TORUS_STR;
  }
  
  private String exampleString = "null";
  @Override
  public String toExampleString() {
    return this.exampleString;
  }
  
  @Override
  public BasicForm getInstance(String info)
  {
    this.exampleString = info;
    
    BasicForm bform = null;
    
    String str = info.trim();
    
    int diyezIndex = str.lastIndexOf("###");
    if (diyezIndex < 0) return bform;
    
    str = str.substring(diyezIndex+3);
    str = str.replaceAll("\n", "");
    str = str.replaceAll(" ", "");
    
    String [] split = str.split (",");
    if (split == null) return bform;
    
    //0.9,  0.4,  0.5
    try {
      double mjr = Double.parseDouble(split[0]);
      double mnr = Double.parseDouble(split[1]);
      
      bform = new MTorus(mjr, mnr);
      double vl = Double.parseDouble(split[2]);
      bform.setVolumeValue(vl);
      return bform;
      } catch (NumberFormatException nfe) {
      nfe.printStackTrace();
      return null;
    }
  }
  
  private double volumeValue = 1.0;
  public double getVolumeValue() {
    return this.volumeValue;
  }
  
  public void setVolumeValue(double vlm) {
    this.volumeValue = vlm;
  }
  ////////////
  
  /**
   * Returns a string representation of the torus.
   *
   * @return string representation
   */
  @Override
  public String toString() {
    return ObjectWriter.toString(this);
  }
  
  /**
   * Writes the torus data to an ObjectWriter.
   *
   * @param writer the ObjectWriter to write to
   * @throws java.io.IOException if writing fails
   */
  @Override
  public void write(ObjectWriter writer) throws java.io.IOException {
    Object[][] fields = {
      { "majorRadius", majorRadius },
      { "minorRadius", minorRadius }
    };
    writer.writeFields(fields);
  }
  
}
