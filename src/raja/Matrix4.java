// Murat Inan
package raja;

/**
 * Represents a 4x4 matrix for 3D transformations (translation, rotation, scaling).
 */
public class Matrix4 implements java.io.Serializable, raja.io.Writable {
  public final double[][] m; // Matrix elements
  
  private double trnsX, trnsY, trnsZ, rotaX, rotaY, rotaZ, scleX, scleY, scleZ;
  
  /**
   * Constructs an identity Matrix4.
   */
  public Matrix4() {
    m = new double[4][4];
    m[0][0] = 1.0; m[0][1] = 0.0; m[0][2] = 0.0; m[0][3] = 0.0;
    m[1][0] = 0.0; m[1][1] = 1.0; m[1][2] = 0.0; m[1][3] = 0.0;
    m[2][0] = 0.0; m[2][1] = 0.0; m[2][2] = 1.0; m[2][3] = 0.0;
    m[3][0] = 0.0; m[3][1] = 0.0; m[3][2] = 0.0; m[3][3] = 1.0;
  }
  
  /**
   * Constructs a Matrix4 with the specified elements.
   */
  public Matrix4(double m00, double m01, double m02, double m03,
    double m10, double m11, double m12, double m13,
    double m20, double m21, double m22, double m23,
    double m30, double m31, double m32, double m33) {
    m = new double[4][4];
    this.m[0][0] = m00; this.m[0][1] = m01; this.m[0][2] = m02; this.m[0][3] = m03;
    this.m[1][0] = m10; this.m[1][1] = m11; this.m[1][2] = m12; this.m[1][3] = m13;
    this.m[2][0] = m20; this.m[2][1] = m21; this.m[2][2] = m22; this.m[2][3] = m23;
    this.m[3][0] = m30; this.m[3][1] = m31; this.m[3][2] = m32; this.m[3][3] = m33;
  }
  
  /**
   * Constructs a new Matrix4 by copying an existing matrix.
   * @param other The Matrix4 object to copy.
   */
  public Matrix4(Matrix4 other) {
    this(other.m[0][0], other.m[0][1], other.m[0][2], other.m[0][3],
      other.m[1][0], other.m[1][1], other.m[1][2], other.m[1][3],
      other.m[2][0], other.m[2][1], other.m[2][2], other.m[2][3],
    other.m[3][0], other.m[3][1], other.m[3][2], other.m[3][3]);
  }
  
  /**
   * Returns an identity (unit) 4x4 matrix.
   * An identity matrix has 1s on the main diagonal and 0s elsewhere.
   * It represents no translation, rotation, or scaling.
   *
   * @return A new 4x4 identity matrix.
   */
  public static Matrix4 identity() {
    return new Matrix4(); // The default constructor creates an identity matrix
  }
  
  /**
   * Returns the rotation component of this transformation matrix by removing translation and scaling.
   * This extracts only the rotational part, normalizing the basis vectors to remove scale effects.
   * Useful for transforming normal vectors without distorting their length.
   *
   * @return A new Matrix4 containing only the rotation component
   */
  public Matrix4 getRotationMatrix() {
    // Extract the 3x3 upper-left submatrix (rotation/scale part)
    Vector3D col0 = new Vector3D(m[0][0], m[1][0], m[2][0]); // First column
    Vector3D col1 = new Vector3D(m[0][1], m[1][1], m[2][1]); // Second column
    Vector3D col2 = new Vector3D(m[0][2], m[1][2], m[2][2]); // Third column
    
    // Normalize each column vector to remove scaling
    col0 = col0.normalization();
    col1 = col1.normalization();
    col2 = col2.normalization();
    
    // Create new matrix with normalized rotation and zero translation
    return new Matrix4(
      col0.x, col1.x, col2.x, 0.0,  // First row: normalized X basis, no translation
      col0.y, col1.y, col2.y, 0.0,  // Second row: normalized Y basis, no translation
      col0.z, col1.z, col2.z, 0.0,  // Third row: normalized Z basis, no translation
      0.0,    0.0,    0.0,    1.0   // Fourth row: homogeneous coordinates
    );
  }
  
  /**
   * Sets the value at the specified row and column.
   * @param row The row index (0-3)
   * @param col The column index (0-3)
   * @param value The value to set
   * @throws IndexOutOfBoundsException if row or col is not in [0, 3]
   */
  public void set(int row, int col, double value) {
    if (row < 0 || row >= 4 || col < 0 || col >= 4) {
      throw new IndexOutOfBoundsException("Matrix4 indices out of bounds: [" + row + "][" + col + "]");
    }
    
    this.m[row][col] = value;
  }
  
  /**
   * Gets the X-axis scale factor from this transformation matrix.
   * This is calculated as the magnitude of the X basis vector.
   * @return The X scale factor
   */
  public double getScaleX() {
    return Math.sqrt(m[0][0] * m[0][0] + m[1][0] * m[1][0] + m[2][0] * m[2][0]);
  }
  
  /**
   * Gets the Y-axis scale factor from this transformation matrix.
   * This is calculated as the magnitude of the Y basis vector.
   * @return The Y scale factor
   */
  public double getScaleY() {
    return Math.sqrt(m[0][1] * m[0][1] + m[1][1] * m[1][1] + m[2][1] * m[2][1]);
  }
  
  /**
   * Gets the Z-axis scale factor from this transformation matrix.
   * This is calculated as the magnitude of the Z basis vector.
   * @return The Z scale factor
   */
  public double getScaleZ() {
    return Math.sqrt(m[0][2] * m[0][2] + m[1][2] * m[1][2] + m[2][2] * m[2][2]);
  }
  
  public Ray transformRay(Ray ray) {
    Point3D newOrigin = this.transformPoint(ray.getOrigin());
    Vector3D newDirection = this.transformVector(ray.getDirection()).normalization();
    return new Ray(newOrigin, newDirection);
  }
  
  /**
   * Transforms a direction vector by this matrix.
   * Unlike points, vectors are not affected by translation.
   * Only the rotational and scaling components are applied.
   *
   * This is used for transforming normal vectors, ray directions, etc.
   *
   * @param v The direction vector to transform
   * @return A new transformed Vector3
   */
  public Vector3D transformDirection(Vector3D v) {
    double x = m[0][0] * v.x + m[0][1] * v.y + m[0][2] * v.z;
    double y = m[1][0] * v.x + m[1][1] * v.y + m[1][2] * v.z;
    double z = m[2][0] * v.x + m[2][1] * v.y + m[2][2] * v.z;
    return new Vector3D(x, y, z);
  }
  
  /**
   * Provides access to a specific element of the matrix.
   * @param row The row index (0-3).
   * @param col The column index (0-3).
   * @return The matrix element at the specified position.
   * @throws IndexOutOfBoundsException If the row or column index is invalid.
   */
  public double get(int row, int col) {
    if (row < 0 || row >= 4 || col < 0 || col >= 4) {
      throw new IndexOutOfBoundsException("Matrix4 indices out of bounds: [" + row + "][" + col + "]");
    }
    return m[row][col];
  }
  
  /**
   * Normal vektörü dönüştürür (normal transformasyonu için).
   * Normal vektörlerin doğru dönüşümü için matrisin ters transpozu kullanılır.
   * @param normal Dönüştürülecek normal vektör
   * @return Dönüştürülmüş normal vektör (normalize edilmiş)
   */
  public Vector3D transformNormal(Vector3D normal) {
    // Matrisin ters transpozu alınır
    Matrix4 normalMatrix = this.inverseTransposeForNormal();
    
    if (normalMatrix == null) {
      return new Vector3D(0, 0, 0); // Geçersiz dönüşüm durumu
    }
    
    // Vektörü dönüştür (w=0 varsayarak, sadece 3x3 kısım kullanılır)
    double x = normal.x;
    double y = normal.y;
    double z = normal.z;
    
    double newX = normalMatrix.m[0][0] * x + normalMatrix.m[0][1] * y + normalMatrix.m[0][2] * z;
    double newY = normalMatrix.m[1][0] * x + normalMatrix.m[1][1] * y + normalMatrix.m[1][2] * z;
    double newZ = normalMatrix.m[2][0] * x + normalMatrix.m[2][1] * y + normalMatrix.m[2][2] * z;
    
    return new Vector3D(newX, newY, newZ).normalization();
  }
  
  /**
   * Multiplies this matrix by another matrix.
   * @param other The other Matrix4 to multiply with.
   * @return The resulting Matrix4.
   */
  public Matrix4 multiply(Matrix4 other) {
    Matrix4 result = new Matrix4();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        result.m[i][j] = 0;
        for (int k = 0; k < 4; k++) {
          result.m[i][j] += this.m[i][k] * other.m[k][j];
        }
      }
    }
    
    // Translation değerlerini koru
    result.trnsX = this.trnsX + other.trnsX;
    result.trnsY = this.trnsY + other.trnsY;
    result.trnsZ = this.trnsZ + other.trnsZ;
    
    // Rotation değerlerini koru
    result.rotaX = this.rotaX + other.rotaX;
    result.rotaY = this.rotaY + other.rotaY;
    result.rotaZ = this.rotaZ + other.rotaZ;
    
    // SCALE değerlerini MATRIX'TEN HESAPLA
    result.scleX = Math.sqrt(result.m[0][0] * result.m[0][0] + result.m[1][0] * result.m[1][0] + result.m[2][0] * result.m[2][0]);
    result.scleY = Math.sqrt(result.m[0][1] * result.m[0][1] + result.m[1][1] * result.m[1][1] + result.m[2][1] * result.m[2][1]);
    result.scleZ = Math.sqrt(result.m[0][2] * result.m[0][2] + result.m[1][2] * result.m[1][2] + result.m[2][2] * result.m[2][2]);
    
    return result;
  }
  
  /**
   * Transforms a Point3 by this matrix (includes translation).
   * For affine transformations, the W component of the transformed point should be 1.0.
   * @param point The Point3 to transform.
   * @return The transformed Point3.
   */
  public Point3D transformPoint(Point3D point) {
    double x = m[0][0] * point.x + m[0][1] * point.y + m[0][2] * point.z + m[0][3];
    double y = m[1][0] * point.x + m[1][1] * point.y + m[1][2] * point.z + m[1][3];
    double z = m[2][0] * point.x + m[2][1] * point.y + m[2][2] * point.z + m[2][3];
    return new Point3D(x, y, z);
  }
  
  /**
   * Transforms a Vector3 by this matrix (only rotation and scaling, no translation).
   * @param vector The Vector3 to transform.
   * @return The transformed Vector3.
   */
  public Vector3D transformVector(Vector3D vector) {
    double x = m[0][0] * vector.x + m[0][1] * vector.y + m[0][2] * vector.z;
    double y = m[1][0] * vector.x + m[1][1] * vector.y + m[1][2] * vector.z;
    double z = m[2][0] * vector.x + m[2][1] * vector.y + m[2][2] * vector.z;
    return new Vector3D(x, y, z);
  }
  
  /**
   * Returns the inverse of this matrix. Returns null if the matrix is non-invertible.
   * This method is designed for affine transformations (rotation, translation, uniform scaling).
   * Formula: [ R | t ]^-1 = [ R^-1 | -R^-1 * t ]
   * Where R is the upper-left 3x3 submatrix and t is the translation vector.
   * @return The inverse Matrix4 or null.
   */
  public Matrix4 inverse() {
    // Extract the upper 3x3 rotation/scale part
    Matrix3 upperLeft = new Matrix3(
      m[0][0], m[0][1], m[0][2],
      m[1][0], m[1][1], m[1][2],
      m[2][0], m[2][1], m[2][2]
    );
    Matrix3 invUpperLeft = upperLeft.inverse(); // This performs its own determinant check
    
    if (invUpperLeft == null) {
      System.err.println("Warning: Upper 3x3 part of Matrix4 is non-invertible, cannot compute inverse.");
      return null;
    }
    
    Matrix4 inv = new Matrix4(); // Resulting inverse matrix, initialized to identity
    
    // Set the upper-left 3x3 of the inverse matrix (R^-1)
    inv.m[0][0] = invUpperLeft.get(0,0); inv.m[0][1] = invUpperLeft.get(0,1); inv.m[0][2] = invUpperLeft.get(0,2);
    inv.m[1][0] = invUpperLeft.get(1,0); inv.m[1][1] = invUpperLeft.get(1,1); inv.m[1][2] = invUpperLeft.get(1,2);
    inv.m[2][0] = invUpperLeft.get(2,0); inv.m[2][1] = invUpperLeft.get(2,1); inv.m[2][2] = invUpperLeft.get(2,2);
    
    // Calculate the inverse translation part: -R^-1 * t
    Vector3D translation = new Vector3D(m[0][3], m[1][3], m[2][3]);
    Vector3D invTranslation = invUpperLeft.transform(translation).negation();
    
    inv.m[0][3] = invTranslation.x;
    inv.m[1][3] = invTranslation.y;
    inv.m[2][3] = invTranslation.z;
    
    // Bottom row remains [0, 0, 0, 1] for affine transformations
    inv.m[3][0] = 0.0; inv.m[3][1] = 0.0; inv.m[3][2] = 0.0; inv.m[3][3] = 1.0;
    
    return inv;
  }
  
  /**
   * Returns a new matrix that is the negation of this matrix.
   * Each element in the resulting matrix is the negative of the corresponding element in this matrix.
   *
   * @return A new Matrix4 representing the negation of this matrix
   */
  public Matrix4 negation() {
    return new Matrix4(
      -m[0][0], -m[0][1], -m[0][2], -m[0][3],
      -m[1][0], -m[1][1], -m[1][2], -m[1][3],
      -m[2][0], -m[2][1], -m[2][2], -m[2][3],
      -m[3][0], -m[3][1], -m[3][2], -m[3][3]
    );
  }
  
  /**
   * Computes the inverse transpose of the upper 3x3 part of this matrix.
   * This is typically used to transform normal vectors correctly when the
   * model matrix contains non-uniform scaling.
   * For pure rotations, the inverse is equal to the transpose.
   *
   * @return A new Matrix4 representing the inverse transpose of the 3x3 part,
   * with the translation components set to zero. Returns null if the
   * upper 3x3 part is non-invertible.
   */
  public Matrix4 inverseTransposeForNormal() {
    // Extract the upper 3x3 part
    Matrix3 upperLeft = new Matrix3(
      m[0][0], m[0][1], m[0][2],
      m[1][0], m[1][1], m[1][2],
      m[2][0], m[2][1], m[2][2]
    );
    
    // Compute its inverse
    Matrix3 invUpperLeft = upperLeft.inverse();
    
    if (invUpperLeft == null) {
      System.err.println("Warning: Upper 3x3 part of Matrix4 is non-invertible, cannot compute inverse transpose for normal.");
      return null;
    }
    
    // Transpose the inverse (this is the correct operation for normals)
    Matrix3 normalMatrix3 = invUpperLeft.transpose();
    
    // Construct a new Matrix4 from this 3x3, with translation part zeroed out
    return new Matrix4(
      normalMatrix3.get(0,0), normalMatrix3.get(0,1), normalMatrix3.get(0,2), 0,
      normalMatrix3.get(1,0), normalMatrix3.get(1,1), normalMatrix3.get(1,2), 0,
      normalMatrix3.get(2,0), normalMatrix3.get(2,1), normalMatrix3.get(2,2), 0,
      0, 0, 0, 1
    );
  }
  
  /**
   * Creates a translation matrix.
   * @param translation The translation vector.
   * @return The translation Matrix4.
   */
  public static Matrix4 translate(Vector3D translation) {
    Matrix4 result = new Matrix4(
      1, 0, 0, translation.x,
      0, 1, 0, translation.y,
      0, 0, 1, translation.z,
      0, 0, 0, 1
    );
    result.trnsX = translation.x;
    result.trnsY = translation.y;
    result.trnsZ = translation.z;
    
    return result;
  }
  
  public static Matrix4 translate(double x, double y, double z) {
    Matrix4 result = new Matrix4(
      1, 0, 0, x,
      0, 1, 0, y,
      0, 0, 1, z,
      0, 0, 0, 1
    );
    result.trnsX = x;
    result.trnsY = y;
    result.trnsZ = z;
    
    return result;
  }
  
  /**
   * Creates a rotation matrix around the X-axis.
   * @param angleDegrees The rotation angle in degrees.
   * @return The rotation Matrix4.
   */
  public static Matrix4 rotateX(double angleDegrees) {
    double angleRad = Math.toRadians(angleDegrees);
    double cosA = Math.cos(angleRad);
    double sinA = Math.sin(angleRad);
    Matrix4 result = new Matrix4(
      1,    0,     0, 0,
      0,  cosA, -sinA, 0,
      0,  sinA,  cosA, 0,
      0,    0,     0, 1
    );
    result.rotaX = angleDegrees;
    
    return result;
  }
  
  /**
   * Creates a rotation matrix around the Y-axis.
   * @param angleDegrees The rotation angle in degrees.
   * @return The rotation Matrix4.
   */
  public static Matrix4 rotateY(double angleDegrees) {
    double angleRad = Math.toRadians(angleDegrees);
    double cosA = Math.cos(angleRad);
    double sinA = Math.sin(angleRad);
    Matrix4 result = new Matrix4(
      cosA,  0, sinA, 0,
      0,     1,    0, 0,
      -sinA, 0, cosA, 0,
      0,     0,    0, 1
    );
    result.rotaY = angleDegrees;
    
    return result;
  }
  
  /**
   * Creates a rotation matrix around the Z-axis.
   * @param angleDegrees The rotation angle in degrees.
   * @return The rotation Matrix4.
   */
  public static Matrix4 rotateZ(double angleDegrees) {
    double angleRad = Math.toRadians(angleDegrees);
    double cosA = Math.cos(angleRad);
    double sinA = Math.sin(angleRad);
    Matrix4 result = new Matrix4(
      cosA, -sinA, 0, 0,
      sinA,  cosA, 0, 0,
      0,     0,    1, 0,
      0,     0,    0, 1
    );
    result.rotaZ = angleDegrees;
    
    return result;
  }
  
  /**
   * Creates a scaling matrix with the specified scale factors.
   * @param sx The X-axis scale factor.
   * @param sy The Y-axis scale factor.
   * @param sz The Z-axis scale factor.
   * @return The scaling Matrix4.
   */
  public static Matrix4 scale(double sx, double sy, double sz) {
    Matrix4 result = new Matrix4(
      sx, 0,  0, 0,
      0, sy,  0, 0,
      0,  0, sz, 0,
      0,  0,  0, 1
    );
    result.scleX = sx;
    result.scleY = sy;
    result.scleZ = sz;
    
    return result;
  }
  
  // Matrix4 sınıfına bu metodu ekleyin
  public Matrix4 transpose() {
    return new Matrix4(
      m[0][0], m[1][0], m[2][0], m[3][0],
      m[0][1], m[1][1], m[2][1], m[3][1],
      m[0][2], m[1][2], m[2][2], m[3][2],
      m[0][3], m[1][3], m[2][3], m[3][3]
    );
  }
  
  /**
   * Creates a rotation matrix around an arbitrary axis by the specified angle.
   * Uses Rodrigues' rotation formula.
   *
   * @param axis the rotation axis (will be normalized)
   * @param angle the rotation angle in radians
   * @return the rotation matrix
   */
  public static Matrix4 rotate(Vector3D axis, double angle) {
    Vector3D u = axis.normalization();
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    double oneMinusCos = 1.0 - cos;
    
    double ux = u.x, uy = u.y, uz = u.z;
    
    return new Matrix4(
      cos + ux*ux*oneMinusCos,     ux*uy*oneMinusCos - uz*sin, ux*uz*oneMinusCos + uy*sin, 0,
      uy*ux*oneMinusCos + uz*sin,  cos + uy*uy*oneMinusCos,     uy*uz*oneMinusCos - ux*sin, 0,
      uz*ux*oneMinusCos - uy*sin,  uz*uy*oneMinusCos + ux*sin, cos + uz*uz*oneMinusCos,     0,
      0,                           0,                           0,                           1
    );
  }
  
  /**
   * Creates a Matrix4 from a Matrix3 (typically to extend rotation matrices to 4x4).
   * @param m3 The Matrix3 to extend.
   * @return The created Matrix4.
   */
  public static Matrix4 fromMatrix3(Matrix3 m3) {
    return new Matrix4(
      m3.get(0,0), m3.get(0,1), m3.get(0,2), 0,
      m3.get(1,0), m3.get(1,1), m3.get(1,2), 0,
      m3.get(2,0), m3.get(2,1), m3.get(2,2), 0,
      0, 0, 0, 1
    );
  }
  
  /**
   * Creates a scaling matrix that scales uniformly along all axes.
   * @param s The uniform scale factor.
   * @return The scaling Matrix4.
   */
  public static Matrix4 scale(double s) {
    return scale(s, s, s);
  }
  
  /**
   * Creates a scaling matrix that scales only along the X-axis.
   * @param sx The X-axis scale factor.
   * @return The scaling Matrix4.
   */
  public static Matrix4 scaleX(double sx) {
    Matrix4 result = new Matrix4(
      sx, 0,  0, 0,
      0,  1,  0, 0,
      0,  0,  1, 0,
      0,  0,  0, 1
    );
    result.scleX = sx;
    
    return result;
  }
  
  /**
   * Creates a scaling matrix that scales only along the Y-axis.
   * @param sy The Y-axis scale factor.
   * @return The scaling Matrix4.
   */
  public static Matrix4 scaleY(double sy) {
    Matrix4 result = new Matrix4(
      1,  0,  0, 0,
      0, sy,  0, 0,
      0,  0,  1, 0,
      0,  0,  0, 1
    );
    result.scleY = sy;
    
    return result;
  }
  
  /**
   * Creates a scaling matrix that scales only along the Z-axis.
   * @param sz The Z-axis scale factor.
   * @return The scaling Matrix4.
   */
  public static Matrix4 scaleZ(double sz) {
    Matrix4 result = new Matrix4(
      1, 0,  0, 0,
      0, 1,  0, 0,
      0, 0, sz, 0,
      0, 0,  0, 1
    );
    result.scleZ = sz;
    
    return result;
  }
  
  /**
   * Creates a translation matrix that translates only along the X-axis.
   * @param x The translation amount along the X-axis.
   * @return The translation Matrix4.
   */
  public static Matrix4 translateX(double x) {
    Matrix4 result = new Matrix4(
      1, 0, 0, x,
      0, 1, 0, 0,
      0, 0, 1, 0,
      0, 0, 0, 1
    );
    result.trnsX = x;
    
    return result;
  }
  
  /**
   * Creates a translation matrix that translates only along the Y-axis.
   * @param y The translation amount along the Y-axis.
   * @return The translation Matrix4.
   */
  public static Matrix4 translateY(double y) {
    Matrix4 result = new Matrix4(
      1, 0, 0, 0,
      0, 1, 0, y,
      0, 0, 1, 0,
      0, 0, 0, 1
    );
    result.trnsY = y;
    
    return result;
  }
  
  /**
   * Creates a translation matrix that translates only along the Z-axis.
   * @param z The translation amount along the Z-axis.
   * @return The translation Matrix4.
   */
  public static Matrix4 translateZ(double z) {
    Matrix4 result = new Matrix4(
      1, 0, 0, 0,
      0, 1, 0, 0,
      0, 0, 1, z,
      0, 0, 0, 1
    );
    result.trnsZ = z;
    
    return result;
  }
  
  /**
   * Creates a rotation matrix that rotates around an arbitrary axis defined by a unit vector.
   * The input angle is in degrees; conversion to radians is handled internally.
   *
   * @param angleDegrees The rotation angle in degrees.
   * @param axis The unit vector representing the axis of rotation.
   * @return The rotation Matrix4.
   * @throws IllegalArgumentException if the axis vector is zero-length.
   */
  public static Matrix4 rotate(double angleDegrees, Vector3D axis) {
    if (axis.lengthSquared() == 0) {
      throw new IllegalArgumentException("Rotation axis must be a non-zero vector.");
    }
    
    // Normalize the axis to ensure it's a unit vector
    Vector3D u = axis.normalization();
    
    double angleRad = Math.toRadians(angleDegrees);
    double cosA = Math.cos(angleRad);
    double sinA = Math.sin(angleRad);
    double oneMinusCosA = 1.0 - cosA;
    
    double x = u.x;
    double y = u.y;
    double z = u.z;
    
    double m00 = cosA + x * x * oneMinusCosA;
    double m01 = x * y * oneMinusCosA - z * sinA;
    double m02 = x * z * oneMinusCosA + y * sinA;
    
    double m10 = y * x * oneMinusCosA + z * sinA;
    double m11 = cosA + y * y * oneMinusCosA;
    double m12 = y * z * oneMinusCosA - x * sinA;
    
    double m20 = z * x * oneMinusCosA - y * sinA;
    double m21 = z * y * oneMinusCosA + x * sinA;
    double m22 = cosA + z * z * oneMinusCosA;
    
    return new Matrix4(
      m00, m01, m02, 0,
      m10, m11, m12, 0,
      m20, m21, m22, 0,
      0,   0,   0,   1
    );
  }
  
  /**
   * Creates a rotation matrix by applying rotations around the X, Y, and Z axes in XYZ order.
   * Input angles are in degrees; conversion to radians is handled internally via rotateX/Y/Z.
   * This is equivalent to: R = Rz(z) * Ry(y) * Rx(x)
   * Note: Rotation order matters—this uses intrinsic Tait-Bryan angles (XYZ convention).
   *
   * @param xAngleDegrees Rotation angle around the X-axis in degrees.
   * @param yAngleDegrees Rotation angle around the Y-axis in degrees.
   * @param zAngleDegrees Rotation angle around the Z-axis in degrees.
   * @return The combined rotation Matrix4.
   */
  public static Matrix4 rotateXYZ(double xAngleDegrees, double yAngleDegrees, double zAngleDegrees) {
    // Each of rotateX/Y/Z already converts degrees to radians internally
    Matrix4 rx = rotateX(xAngleDegrees);
    Matrix4 ry = rotateY(yAngleDegrees);
    Matrix4 rz = rotateZ(zAngleDegrees);
    // Apply in order: X → Y → Z, so matrix multiplication is Rz * (Ry * Rx)
    Matrix4 result = rz.multiply(ry.multiply(rx));
    result.rotaX = xAngleDegrees;
    result.rotaY = yAngleDegrees;
    result.rotaZ = zAngleDegrees;
    
    return result;
  }
  
  public static Object build(raja.io.ObjectReader reader) throws java.io.IOException {
    /* Initialisation */
    java.util.HashMap map = new java.util.HashMap();
    
    // Matrix4 için 16 elemanlık bir dizi bekliyoruz
    map.put("m00", null); map.put("m01", null); map.put("m02", null); map.put("m03", null);
    map.put("m10", null); map.put("m11", null); map.put("m12", null); map.put("m13", null);
    map.put("m20", null); map.put("m21", null); map.put("m22", null); map.put("m23", null);
    map.put("m30", null); map.put("m31", null); map.put("m32", null); map.put("m33", null);
    
    /* Parsing */
    reader.readFields(map);
    
    return new Matrix4(
      ((Number)map.get("m00")).doubleValue(), ((Number)map.get("m01")).doubleValue(),
      ((Number)map.get("m02")).doubleValue(), ((Number)map.get("m03")).doubleValue(),
      ((Number)map.get("m10")).doubleValue(), ((Number)map.get("m11")).doubleValue(),
      ((Number)map.get("m12")).doubleValue(), ((Number)map.get("m13")).doubleValue(),
      ((Number)map.get("m20")).doubleValue(), ((Number)map.get("m21")).doubleValue(),
      ((Number)map.get("m22")).doubleValue(), ((Number)map.get("m23")).doubleValue(),
      ((Number)map.get("m30")).doubleValue(), ((Number)map.get("m31")).doubleValue(),
      ((Number)map.get("m32")).doubleValue(), ((Number)map.get("m33")).doubleValue()
    );
  }
  
  @Override
  public void write(raja.io.ObjectWriter writer) throws java.io.IOException {
    Number[] fields = {
      m[0][0], m[0][1], m[0][2], m[0][3],
      m[1][0], m[1][1], m[1][2], m[1][3],
      m[2][0], m[2][1], m[2][2], m[2][3],
      m[3][0], m[3][1], m[3][2], m[3][3]
    };
    writer.writeFields(fields);
  }
  
  public String toParametersString() {
    StringBuilder sb = new StringBuilder();
    sb.append("// translate(");
    sb.append(Double.toString(trnsX));
    sb.append(", ");
    sb.append(Double.toString(trnsY));
    sb.append(", ");
    sb.append(Double.toString(trnsZ));
    sb.append(") * rotate(");
    sb.append(Double.toString(rotaX));
    sb.append(", ");
    sb.append(Double.toString(rotaY));
    sb.append(", ");
    sb.append(Double.toString(rotaZ));
    sb.append(") * scale(");
    sb.append(Double.toString(scleX));
    sb.append(", ");
    sb.append(Double.toString(scleY));
    sb.append(", ");
    sb.append(Double.toString(scleZ));
    sb.append(");\n");
    
    return sb.toString();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      sb.append("| ");
      for (int j = 0; j < 4; j++) {
        sb.append(String.format("%8.4f", m[i][j])).append(" ");
      }
      sb.append("|\n");
    }
    return sb.toString();
  }
  
}
