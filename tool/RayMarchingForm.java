// Murat Inan
package raja.shape;

import raja.*;

public abstract class RayMarchingForm extends BasicForm
{
    // Ray marching parameters
    protected static final int MAX_MARCH_STEPS = 256;
    protected static final double HIT_THRESHOLD = 0.0001;
    protected static final double MAX_MARCH_DISTANCE = 1000.0;
    
    /**
     * Signed Distance Function - Implement this in subclasses
     * ÇALIŞMA NOTU: Bu metod LOCAL coordinates'ta çalışır!
     */
    protected abstract double sdf(Point3D p);
    
    /**
     * YENİ: Optimized ray marching intersection
     */
public Point3D computeIntersection(Ray r) {
    double totalDistance = 0.0;
    Point3D currentPos = r.origin;
    
    for (int i = 0; i < MAX_MARCH_STEPS; i++) {
        double dist = sdf(currentPos);
        
        // HIT - ama daha hassas
        if (Math.abs(dist) < HIT_THRESHOLD) {
            // BİR ADIM DAHA İLERİ GİT - daha doğru intersection için
            Point3D finalPos = new Point3D(
                currentPos.x + r.direction.x * dist * 0.5,
                currentPos.y + r.direction.y * dist * 0.5, 
                currentPos.z + r.direction.z * dist * 0.5
            );
            
            if (hasLG(r.origin)) return null;
            return finalPos;
        }
        
        if (totalDistance > MAX_MARCH_DISTANCE) return null;
        
        // MARCH
        currentPos = new Point3D(
            currentPos.x + r.direction.x * dist,
            currentPos.y + r.direction.y * dist,
            currentPos.z + r.direction.z * dist
        );
        totalDistance += Math.abs(dist); // ABS kullan
    }
    return null;
}
    
    /**
     * YENİ: Daha iyi normal hesaplama
     */
    public Vector3D computeNormal(Point3D p)
    {
        // p zaten world coordinates'ta geliyor, local'e çevir
        Point3D localPoint = hasTransform ? inverseTransform.transformPoint(p) : p;
        
        final double eps = 0.0005; // Daha küçük epsilon
        
        // Gradient hesapla
        double dx = sdf(new Point3D(localPoint.x + eps, localPoint.y, localPoint.z)) 
                  - sdf(new Point3D(localPoint.x - eps, localPoint.y, localPoint.z));
                  
        double dy = sdf(new Point3D(localPoint.x, localPoint.y + eps, localPoint.z)) 
                  - sdf(new Point3D(localPoint.x, localPoint.y - eps, localPoint.z));
                  
        double dz = sdf(new Point3D(localPoint.x, localPoint.y, localPoint.z + eps)) 
                  - sdf(new Point3D(localPoint.x, localPoint.y, localPoint.z - eps));
        
        Vector3D localNormal = new Vector3D(dx, dy, dz);
        localNormal = Vector3D.normalization(localNormal);
                    
        // Normal'i world coordinates'a çevir
        if (hasTransform) {
           return transformNormalToWorld(localNormal);
        }
        return localNormal;
    }
    
    // Default implementations
    public boolean exactlyContains(Point3D p) {
        Point3D localPoint = hasTransform ? inverseTransform.transformPoint(p) : p;
        return sdf(localPoint) <= 0;
    }
    
    public boolean exactlyStrictlyContains(Point3D p) {
        Point3D localPoint = hasTransform ? inverseTransform.transformPoint(p) : p;
        return sdf(localPoint) < 0;
    }
    
}
