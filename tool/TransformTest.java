// TransformTest.java
import raja.*;
import raja.shape.*;

public class TransformTest {
    public static void main(String[] args) {
        System.out.println("=== TRANSFORM TEST START ===");
        
        // 1. Basit torus (0,0,0)'da
        Torus torus = new Torus(1.0, 0.5);
        
        // 2. Merkeze bakan ray
        Ray ray = new Ray(new Point3D(0, 0, -5), new Vector3D(0, 0, 1));
        
        // 3. Intersection test (transform yok)
        LocalGeometry hit = torus.intersection(ray);
        System.out.println("Test 1 - No transform: " + (hit != null ? "HIT" : "NO HIT"));
        
        // 4. Translate uygula
        torus.setTransform(Matrix4.translate(4, 0, 0));
        
        // 5. Aynı ray ile test (torus artık (4,0,0)'da)
        hit = torus.intersection(ray);
        System.out.println("Test 2 - With translate, same ray: " + (hit != null ? "HIT" : "NO HIT"));
        
        // 6. Ray'i torus'un yeni yerine yönlendir
        Ray ray2 = new Ray(new Point3D(4, 0, -5), new Vector3D(0, 0, 1));
        hit = torus.intersection(ray2);
        System.out.println("Test 3 - With translate, new ray: " + (hit != null ? "HIT" : "NO HIT"));
        
        System.out.println("=== TRANSFORM TEST END ===");
    }
    
}
