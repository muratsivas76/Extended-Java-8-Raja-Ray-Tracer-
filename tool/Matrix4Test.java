// Matrix4Test.java
import raja.*;
import raja.shape.*;

public class Matrix4Test {
    public static void main(String[] args) {
        System.out.println("=== MATRIX ORDER TEST ===");
        
        // 1. Scale test
        Torus torus1 = new Torus(1.0, 0.3);
        torus1.setTransform(Matrix4.scale(2, 2, 2));
        
        Ray ray1 = new Ray(new Point3D(0, 0, -5), new Vector3D(0, 0, 1));
        LocalGeometry hit1 = torus1.intersection(ray1);
        System.out.println("Scale test: " + (hit1 != null ? "HIT" : "NO HIT"));
        
        // 2. Rotate test - farklı sıralar deneyelim
        Torus torus2 = new Torus(1.0, 0.3);
        
        // Seçenek A: Önce translate, sonra rotate
        Matrix4 transformA = Matrix4.translate(3, 0, 0).multiply(Matrix4.rotateY(45));
        
        // Seçenek B: Önce rotate, sonra translate  
        Matrix4 transformB = Matrix4.rotateY(45).multiply(Matrix4.translate(3, 0, 0));
        
        torus2.setTransform(transformA);
        Ray ray2 = new Ray(new Point3D(3, 0, -5), new Vector3D(0, 0, 1));
        LocalGeometry hit2 = torus2.intersection(ray2);
        System.out.println("Transform A (T*R): " + (hit2 != null ? "HIT" : "NO HIT"));
        
        torus2.setTransform(transformB);
        LocalGeometry hit3 = torus2.intersection(ray2);
        System.out.println("Transform B (R*T): " + (hit3 != null ? "HIT" : "NO HIT"));
        
        // 3. Matrix çarpım sırasını kontrol et
        System.out.println("\n=== MATRIX MULTIPLICATION ORDER ===");
        Matrix4 T = Matrix4.translate(1, 0, 0);
        Matrix4 R = Matrix4.rotateY(45);
        
        Matrix4 TR = T.multiply(R); // T * R
        Matrix4 RT = R.multiply(T); // R * T
        
        Point3D testPoint = new Point3D(0, 0, 0);
        System.out.println("T*R on (0,0,0): " + TR.transformPoint(testPoint));
        System.out.println("R*T on (0,0,0): " + RT.transformPoint(testPoint));
    }
    
}
