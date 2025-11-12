// FinalTest.java - Tüm transform tipleri
import raja.*;
import raja.shape.*;

public class FinalTest {
    public static void main(String[] args) {
        System.out.println("=== FINAL TRANSFORM TEST ===");
        
        Torus torus = new Torus(1.0, 0.3);
        
        // Test 1: Translate
        torus.setTransform(Matrix4.translate(3, 0, 0));
        System.out.println("Translate: " + (torus.intersection(new Ray(new Point3D(3,0,-5), new Vector3D(0,0,1))) != null));
        
        // Test 2: Rotate
        torus.setTransform(Matrix4.rotateY(45));
        System.out.println("Rotate: " + (torus.intersection(new Ray(new Point3D(0,0,-5), new Vector3D(0,0,1))) != null));
        
        // Test 3: Scale
        torus.setTransform(Matrix4.scale(1.5, 1.5, 1.5));
        System.out.println("Scale: " + (torus.intersection(new Ray(new Point3D(0,0,-5), new Vector3D(0,0,1))) != null));
        
        // Test 4: Kombine
        torus.setTransform(Matrix4.rotateY(30).multiply(Matrix4.scale(1.2, 1.2, 1.2)).multiply(Matrix4.translate(2, 0, 0)));
        System.out.println("Kombine: " + (torus.intersection(new Ray(new Point3D(1.73,0,-1), new Vector3D(0.866,0,0.5))) != null));
        
        System.out.println("=== TRANSFORM SİSTEMİ TAMAM! ===");
    }
    
}
