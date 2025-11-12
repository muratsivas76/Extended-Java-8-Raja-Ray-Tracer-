// CorrectOrderTest.java
import raja.*;
import raja.shape.*;

public class CorrectOrderTest {
    public static void main(String[] args) {
        System.out.println("=== CORRECT ORDER TEST ===");
        
        // 1. Scale test - doğru sıra
        Torus torus = new Torus(1.0, 0.3);
        torus.setTransform(Matrix4.scale(2, 2, 2));
        
        Ray ray = new Ray(new Point3D(0, 0, -5), new Vector3D(0, 0, 1));
        LocalGeometry hit = torus.intersection(ray);
        System.out.println("Scale only: " + (hit != null ? "HIT" : "NO HIT"));
        
        // 2. Rotate + Translate - doğru sıra
        torus.setTransform(Matrix4.rotateY(45).multiply(Matrix4.translate(3, 0, 0)));
        Ray ray2 = new Ray(new Point3D(2.12, 0, -2.12), new Vector3D(0.707, 0, 0.707)); // 45 derece yön
        hit = torus.intersection(ray2);
        System.out.println("Rotate(45) * Translate(3,0,0): " + (hit != null ? "HIT" : "NO HIT"));
        
        // 3. Sadece rotate
        torus.setTransform(Matrix4.rotateY(25));
        Ray ray3 = new Ray(new Point3D(0, 0, -5), new Vector3D(0, 0, 1));
        hit = torus.intersection(ray3);
        System.out.println("Rotate(25) only: " + (hit != null ? "HIT" : "NO HIT"));
    }
    
}
