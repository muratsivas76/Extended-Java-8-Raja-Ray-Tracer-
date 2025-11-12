// RotationTest.java
import raja.*;
import raja.shape.*;

public class RotationTest {
    public static void main(String[] args) {
        System.out.println("=== ROTATION TEST START ===");
        
        // 1. Torus'u merkezde oluştur
        Torus torus = new Torus(1.0, 0.5);
        
        // 2. Y ekseninde 45 derece döndür
        Matrix4 transform = Matrix4.rotateY(45); // 45 derece
        torus.setTransform(transform);
        
        // 3. Farklı yönlerden ray'ler ile test
        Ray[] testRays = {
            new Ray(new Point3D(0, 0, -5), new Vector3D(0, 0, 1)),    // Önden
            new Ray(new Point3D(-3, 0, 0), new Vector3D(1, 0, 0)),    // Soldan
            new Ray(new Point3D(3, 0, 0), new Vector3D(-1, 0, 0)),    // Sağdan
            new Ray(new Point3D(0, 3, 0), new Vector3D(0, -1, 0))     // Yukarıdan
        };
        
        for (int i = 0; i < testRays.length; i++) {
            LocalGeometry hit = torus.intersection(testRays[i]);
            System.out.println("Rotation test ray " + i + ": " + (hit != null ? "HIT" : "NO HIT"));
        }
        
        System.out.println("=== ROTATION TEST END ===");
    }
    
}
