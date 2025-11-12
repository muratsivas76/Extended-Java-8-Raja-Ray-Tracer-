// Murat Inan - ANALİTİK TORUS INTERSECTION
package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;

public class Torus extends BasicForm implements java.io.Serializable, Writable {
    private double majorRadius;
    private double minorRadius;
    
    private Matrix4 transform = Matrix4.identity();
    private Matrix4 inverseTransform = Matrix4.identity();

    public Torus(double majorRadius, double minorRadius) {
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        updateTransforms();
    }

    public static Object build(ObjectReader reader) throws java.io.IOException {
        HashMap map = new HashMap();
        map.put("majorRadius", null);
        map.put("minorRadius", null);
        reader.readFields(map);
        return new Torus((Double) map.get("majorRadius"), (Double) map.get("minorRadius"));
    }
    
    public void setTransform(Matrix4 transform) {
        this.transform = transform;
        updateTransforms();
    }
    
    private void updateTransforms() {
        this.inverseTransform = this.transform.inverse();
        if (this.inverseTransform == null) {
            this.inverseTransform = Matrix4.identity();
        }
    }

public Point3D computeIntersection(Ray ray) {
    Point3D localOrigin = inverseTransform.transformPoint(ray.getOrigin());
    Vector3D localDirection = inverseTransform.transformVector(ray.getDirection()).normalization();
    
    double t = 0.0;
    Point3D p = localOrigin;
    
    for (int i = 0; i < 100; i++) {
        double dist = sdf(p);
        if (Math.abs(dist) < 0.001) {
            if (hasLG(ray.getOrigin())) return null;
            return transform.transformPoint(p);
        }
        t += dist;
        if (t > 50.0) return null;
        p = new Point3D(p.x + localDirection.x * dist, p.y + localDirection.y * dist, p.z + localDirection.z * dist);
    }
    return null;
}

private double sdf(Point3D p) {
    double x = p.x, y = p.y, z = p.z;
    double q = Math.sqrt(x*x + z*z) - majorRadius;
    return Math.sqrt(q*q + y*y) - minorRadius;
}

private double findSmallestPositiveRoot(double A, double B, double C, double D, double E) {
    double t = 0.0;
    double step = 0.1;
    double minT = 1000.0;
    boolean found = false;
    
    for (int i = 0; i < 10000; i++) {
        double value = A*t*t*t*t + B*t*t*t + C*t*t + D*t + E;
        
        if (Math.abs(value) < 0.001 && t > 0.001) {
            if (t < minT) {
                minT = t;
                found = true;
            }
        }
        
        t += step;
        if (t > 100.0) break;
    }
    
    return found ? minT : -1.0;
}

    // ANALİTİK NORMAL HESAPLAMA
    public Vector3D computeNormal(Point3D worldPoint) {
        Point3D localPoint = inverseTransform.transformPoint(worldPoint);
        
        double x = localPoint.x, y = localPoint.y, z = localPoint.z;
        double R = majorRadius, r = minorRadius;
        
        // Torus normal formülü: ∇f(x,y,z) where f(x,y,z) = (√(x²+z²)-R)² + y² - r²
        double s = Math.sqrt(x*x + z*z);
        
        if (s < 1e-10) {
            return new Vector3D(0, 1, 0); // Degenerate case
        }
        
        double nx = 4 * x * (s - R) / s;
        double ny = 2 * y;
        double nz = 4 * z * (s - R) / s;
        
        Vector3D localNormal = new Vector3D(nx, ny, nz).normalization();
        
        // World normal
        Matrix4 normalMatrix = inverseTransform.inverseTransposeForNormal();
        if (normalMatrix == null) {
            normalMatrix = Matrix4.identity();
        }
        
        return normalMatrix.transformVector(localNormal).normalization();
    }

    public boolean exactlyContains(Point3D p) {
        Point3D localPoint = inverseTransform.transformPoint(p);
        double x = localPoint.x, y = localPoint.y, z = localPoint.z;
        double s = Math.sqrt(x*x + z*z);
        return Math.sqrt((s - majorRadius)*(s - majorRadius) + y*y) <= minorRadius;
    }

    public boolean exactlyStrictlyContains(Point3D p) {
        Point3D localPoint = inverseTransform.transformPoint(p);
        double x = localPoint.x, y = localPoint.y, z = localPoint.z;
        double s = Math.sqrt(x*x + z*z);
        return Math.sqrt((s - majorRadius)*(s - majorRadius) + y*y) < minorRadius;
    }

    public String toString() {
        return ObjectWriter.toString(this);
    }
    
    public void write(ObjectWriter writer) throws java.io.IOException {
        Object[][] fields = { 
            { "majorRadius", majorRadius },
            { "minorRadius", minorRadius } 
        };
        writer.writeFields(fields);
    }
    
}
