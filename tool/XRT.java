//package net.murat.rajaext;

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

// raja
import raja.*;
//import raja.io.*;
import raja.material.*;
//import raja.ui.*;
import raja.util.*;
import raja.light.*;
import raja.shape.*;
import raja.renderer.*;

public class XRT
{
  private static BufferedImage bimg = null;
  private static final Aggregate solids = new Aggregate();
  private static final raja.util.List lights = new raja.util.List();

  public static void main(final String[] args)
  {
    try
    {
      produce(args);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }
	
  private static final void forCompileNames() {
AdvancedRayTracer advancedraytracer_variable = null;
AfricanKenteTexture africankentetexture_variable = null;
Aggregate aggregate_variable = null;
AnodizedTexture anodizedtexture_variable = null;
BasicForm basicform_variable = null;
BasicRenderer basicrenderer_variable = null;
BasicSampler basicsampler_variable = null;
BasicSolid basicsolid_variable = null;
BasicTexturedForm basictexturedform_variable = null;
Billboard billboard_variable = null;
Camera camera_variable = null;
CeramicTileTexture ceramictiletexture_variable = null;
CheckerboardTexture checkerboardtexture_variable = null;
Complement complement_variable = null;
CompositeForm compositeform_variable = null;
Cone cone_variable = null;
CrystalCaveTexture crystalcavetexture_variable = null;
Cylinder cylinder_variable = null;
raja.test.DamierTexture damiertexture_variable = null;
DiadicSampler diadicsampler_variable = null;
DielectricTexture dielectrictexture_variable = null;
DirectedGraph directedgraph_variable = null;
DirectionalLightSource directionallightsource_variable = null;
EmissiveTexture emissivetexture_variable = null;
FileHelper filehelper_variable = null;
Form form_variable = null;
raja.test.HamierTexture hamiertexture_variable = null;
HokusaiTexture hokusaitexture_variable = null;
HorizontalCamera horizontalcamera_variable = null;
ImageTexture imagetexture_variable = null;
Intersection intersection_variable = null;
IsotropicVolume isotropicvolume_variable = null;
LambertianTexture lambertiantexture_variable = null;
LightRay lightray_variable = null;
LightSource lightsource_variable = null;
LocalGeometry localgeometry_variable = null;
LocalTexture localtexture_variable = null;
raja.test.MamierTexture mamiertexture_variable = null;
MarbleTexture marbletexture_variable = null;
Matrix3 matrix3_variable = null;
Matrix4 matrix4_variable = null;
MBox mbox_variable = null;
MCone mcone_variable = null;
MCylinder mcylinder_variable = null;
MetalTexture metaltexture_variable = null;
MPlane mplane_variable = null;
MSphere msphere_variable = null;
MTorus mtorus_variable = null;
MTriangle mtriangle_variable = null;
NaiveSuperSampler naivesupersampler_variable = null;
NorwegianRoseTexture norwegianrosetexture_variable = null;
PlainTexture plaintexture_variable = null;
Plane plane_variable = null;
Point2D point2d_variable = null;
Point3D point3d_variable = null;
PointLightSource pointlightsource_variable = null;
Ray ray_variable = null;
RayTracer raytracer_variable = null;
Renderer renderer_variable = null;
Resolution resolution_variable = null;
RGB rgb_variable = null;
Sampler sampler_variable = null;
Scene scene_variable = null;
Solid solid_variable = null;
SolidLocalGeometry solidlocalgeometry_variable = null;
Sphere sphere_variable = null;
SpotLightSource spotlightsource_variable = null;
StereoCamera stereocamera_variable = null;
Texture texture_variable = null;
TexturedForm texturedform_variable = null;
TexturedLocalGeometry texturedlocalgeometry_variable = null;
TiledRoofTexture tiledrooftexture_variable = null;
TransparentPNGTexture transparentpngtexture_variable = null;
TurkishTileTexture turkishtiletexture_variable = null;
Union union_variable = null;
Vector3D vector3d_variable = null;
VoidTexture voidtexture_variable = null;
Volume volume_variable = null;
WoodTexture woodtexture_variable = null;
World world_variable = null;
XDamierTexture xdamiertexture_variable = null;
XPlainTexture xplaintexture_variable = null;
XRayTexture xraytexture_variable = null;
  }
  
  private static void produce(final String[] args) throws Exception
  {
	///////////////
    forCompileNames();
    //////////////
    
    final int width = 800;
    final int height = 600;

    bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

    Camera camera = new HorizontalCamera(
      new Point3D(0.0, 0.0, 0.0),    // Origin
      new Vector3D(1.0, 0.0, 0.0),   // X eksenine bak
      1.8, 2.0, 1.5                  // Focal, screen width/height
    );

    //Camera camera = new StereoCamera(
    //  new Point3D(0.0, 0.0, 0.0),    // Origin
    //  new Vector3D(1.0, 0.0, 0.0),   // X eksenine bak
    //  1.8, 2.0, 1.5,                 // Focal, screen width/height
    //  8, false                        // step, isCross
    //);
    
////////////
Matrix4 transform = null;

// FLOOR PLANE - X ekseninde
BasicForm floorForm = new MPlane(
  new Point3D(0.0, 0.0,  0.0),  
  new Vector3D(0.0, 0.0, 1.0)    //  normal
);

transform = Matrix4.translate(0.0, 0.0, -15.0);//.multiply(Matrix4.rotateX(40));
floorForm.setTransform(transform);

Texture floorTexture = null;

//floorTexture = new CeramicTileTexture(
//    new RGB(1,1,1),
//    new RGB(0.9,0.9,0.9),
//    new RGB(0.8,0.82,0.8),
//    4.0,    // xm * xm kareler
//    0.005,   // x cm derz
//    0       // plane
//);

floorTexture = new XDamierTexture(
    new RGB(1.0, 0.0, 0.0),  // kd1: TAM KIRMIZI
    new RGB(1.0, 1.0, 1.0),  // kd2: BEYAZ
    new RGB(0.0, 0.3, 0.0),  // kr: yansıma yok (mat)
    new RGB(0.2, 0.0, 0.0),  // kt: iletim yok
    100,                       // ns: düşük parlaklık
    10,                       // nt: iletim parlaklığı (önemsiz)
    4.0,                     // step: her 2 birimde desen tekrarlar
    0.0,                     // x0: x ofseti
    0.0                      // y0: y ofseti
);

Solid floorSolid = new BasicSolid(new BasicTexturedForm(floorForm, floorTexture), new IsotropicVolume(1.0));
solids.addSolid(floorSolid);
    
System.out.println("Floor plane added.");

////////////
// Küçük bir triangle - kameranın sağ üstünde
//BasicForm triangle = new MTriangle(
//    new Point3D(-2.0, -2.0, 0.0),   // sol alt köşe - Y:sol, Z:aşağı
//    new Point3D(2.0, -2.0, 0.0),    // sağ alt köşe - Y:sağ, Z:aşağı  
//    new Point3D(0.0, 0.0, 3.0)      // üst tepe - Y:orta, Z:yukarı
//);

// Sadece X'de ileriye taşı (kameraya yakın)
// Karmaşık transform: ölçekle, döndür, taşı
//transform = Matrix4.translate(3, 0, 0)        // X=3 ileri (öncelikle taşı)
//                          .multiply(Matrix4.rotateZ(45))   // Z ekseninde 45° döndür
//                          .multiply(Matrix4.rotateY(30))   // Y ekseninde 30° döndür  
//                          .multiply(Matrix4.scale(2, 1.5, 1)); // X:2x, Y:1.5x, Z:1x ölçekle
//
//triangle.setTransform(transform);

//LocalTexture triangleTexture = new LocalTexture(
//    new RGB(1.0, 0.0, 0.0),       // kd: TAM KIRMIZI (1.0) - plane gibi
//    new RGB(0.0, 0.3, 0.0),       // kr: plane ile aynı
//    new RGB(0.2, 0.0, 0.0),       // krg: plane ile aynı  
//    new RGB(0.0, 0.0, 0.0),       // kt
//    new RGB(0.0, 0.0, 0.0),       // ktg
//    100,                          // ns: plane ile aynı
//    10                            // nt: plane ile aynı
//);

//Texture triangleTexture = new XDamierTexture(
//    new RGB(0.0, 0.0, 0.0),  // kd1: first color
//    new RGB(1.0, 1.0, 1.0),  // kd2: second color
//    new RGB(0.0, 0.3, 0.0),  // kr: yansıma yok (mat)
//    new RGB(0.2, 0.0, 0.0),  // kt: iletim yok
//    100,                       // ns: düşük parlaklık
//    10,                       // nt: iletim parlaklığı (önemsiz)
//    4.0,                     // step: her 2 birimde desen tekrarlar
//    3.0,                     // x0: x ofseti
//    0.0                      // y0: y ofseti
//);

////Texture trianglePlainTexture = new PlainTexture(triangleTexture);
//TexturedForm texturedTriangle = new BasicTexturedForm(triangle, triangleTexture);
//Solid triangleSolid = new BasicSolid(texturedTriangle, new IsotropicVolume(1.0));
//solids.addSolid(triangleSolid);
//
//System.out.println("Triangle added.");
////////////
// Ray marching torus - bu sefer çalışacak!
// 1. Torus'u ORİJİNDE oluştur
//BasicForm torus = new MTorus(0.9, 0.4);

// 2. Transform'u DOĞRU sırayla uygula:
//transform = Matrix4.translate(3.0, 3.0, 2.0).multiply(Matrix4.rotateY(60))
//     .multiply(Matrix4.rotateY(75))
//     .multiply(Matrix4.scale(1.0, 1.0, 1.0));
//torus.setTransform(transform);

//LocalTexture torusTexture = new LocalTexture(
//    new RGB(0.8, 0.8, 0.1),        // kd: color
//    new RGB(0.3, 0.3, 0.1),        // kr
//    new RGB(0.2, 0.2, 0.1),        // krg  
//    new RGB(0.0, 0.0, 0.0),        // kt
//    new RGB(0.0, 0.0, 0.0),        // ktg
//    100,                            // ns
//    10                              // nt
//);

//Texture torusPlainTexture = new PlainTexture(torusTexture);
//TexturedForm texturedTorus = new BasicTexturedForm(torus, torusPlainTexture);
//Solid torusSolid = new BasicSolid(texturedTorus, new IsotropicVolume(1.0));
//solids.addSolid(torusSolid);

//System.out.println("Torus added!");
/////////
// AYNI texture'ı Sphere'e uygula
BasicForm testSphere = new MSphere(new Point3D(0.0, 0.0, 0.0), 0.7);
// Transform uygulanmış sphere için texture
transform = Matrix4.translate(3.0, 0.0, 0.50);
                   //.multiply(Matrix4.rotateXYZ(0.0, 0.0, 0.0))
                   //.multiply(Matrix4.scale(1.0, 1.0, 1.0));
                   //.multiply(Matrix4.rotateY(45));
testSphere.setTransform(transform);
//
LocalTexture sphereTexture = new LocalTexture(
    new RGB(0.7, 0.1, 0.1),        // kd: AYNI renk
    new RGB(0.3, 0.3, 0.1),         // kr: AYNI yansıma
    new RGB(0.3, 0.2, 0.1),         // krg: AYNI  
    new RGB(1.0, 1.0, 1.0),         // kt
    new RGB(0.0, 0.0, 1.0),         // ktg
    10,                            // ns: AYNI parlama
    10                              // nt
);

Texture spherePlainTexture = new PlainTexture(sphereTexture);
TexturedForm texturedSphere = new BasicTexturedForm(testSphere, spherePlainTexture);
Solid sphereSolid = new BasicSolid(texturedSphere, new IsotropicVolume(0.5));
solids.addSolid(sphereSolid);

System.out.println("Sphere added.");
//////////
/**
BasicForm sphere = new MSphere(new Point3D(0.0, 0.0, 0.0), 5);
// Transform uygulanmış sphere için texture
transform = Matrix4.translate(60.0, -21.4, 19.5)
                   .multiply(Matrix4.rotateXYZ(130.0, 30.0, 90.0))
                   .multiply(Matrix4.scale(1.0, 1.0, 1.0));
sphere.setTransform(transform);

Texture colorGlow = new EmissiveTexture(new RGB(1.0, 1.0, 0.0), 0.35);
TexturedForm texturedEmsSphere = new BasicTexturedForm(sphere, colorGlow);
Solid sphereEmsSolid = new BasicSolid(texturedEmsSphere, new IsotropicVolume(1.0));
solids.addSolid(sphereEmsSolid);

System.out.println("Sphere emissive added.");
*/
//////////
// Orijinal üreticinin parametreleriyle cone
//BasicForm testCone = new MCone(
//    new Point3D(0.0, 0.0, 0.0),     // Tepe noktası orijinde
//    new Vector3D(0.0, 0.0, -1.0),   // AŞAĞI yön (Z ekseninde aşağı)
//    8.0,                           // x birim yükseklik
//    3.5                            // x birim taban yarıçapı
//);

// Transform - X'te 10 birim, Z'de 3 birim hareket
//transform = Matrix4.translate(7.0, 8.0, 6.0).multiply(Matrix4.rotateY(-25));
//testCone.setTransform(transform);

// Orijinal texture parametreleri
//LocalTexture coneTexture = new LocalTexture(
//    new RGB(0.6, 0.0, 0.0),        // kd: Koyu kırmızı
//    new RGB(0.4, 0.4, 0.4),        // krl: yansıma
//    new RGB(0.0, 0.7, 0.2),        // krg: Aynı
//    new RGB(0.0, 0.0, 0.0),        // ktl: Şeffaflık yok
//    new RGB(0.0, 0.0, 0.0),        // ktg: Şeffaflık yok
//    100,                            // ns: Düşük parlaklık
//    10                              // nt
//);

//Texture conePlainTexture = new PlainTexture(coneTexture);
//TexturedForm texturedCone = new BasicTexturedForm(testCone, conePlainTexture);
//Solid coneSolid = new BasicSolid(texturedCone, new IsotropicVolume(0.9));
//solids.addSolid(coneSolid);

//System.out.println("Cone added.");
//////////
// MBox kullanım örneği - bizim koordinat sistemimize göre:
// X: ileri/geri, Y: sol/sağ, Z: yukarı/aşağı

// Küçük bir kutu oluştur (local coordinates)
//BasicForm box = new MBox(
//    new Point3D(-0.5, -0.5, -0.5),  // sol-arkası-alt köşe
//    new Point3D(0.5, 0.5, 0.5)      // sağ-önü-üst köşe
//);

// Transform: büyüt, döndür, ileri taşı
//transform = transform = Matrix4.translate(2, 0, 1)
//                          .multiply(Matrix4.rotateZ(45))
//                          .multiply(Matrix4.scale(2, 1, 0.5));
//box.setTransform(transform);

// Material
// RGB constructor'ını kullan
//Texture boxTexture = new CheckerboardTexture(
//    new RGB(1.0, 0.0, 0.0),  // kd1: TAM KIRMIZI
//    new RGB(0.0, 1.0, 0.0),  // kd2: TAM YEŞİL
//    new RGB(0.5, 0.5, 0.5),  // kr: YÜKSEK YANSIMA
//    new RGB(0.0, 0.0, 0.0),  // kt
//    150,                      // ns: ÇOK YÜKSEK
//    10,                       // nt
//    0.3,                      // step
//    0.0, 0.0, 0.0,            // x0, y0, z0
//    true                      // use3D
//);

//Texture boxPlainTexture = new PlainTexture(boxTexture);
//TexturedForm texturedBox = new BasicTexturedForm(box, boxTexture);
//Solid boxSolid = new BasicSolid(texturedBox, new IsotropicVolume(1.0));
//solids.addSolid(boxSolid);

//System.out.println("Box added.");
//////////
// Dikey silindir
//BasicForm cylinder = new MCylinder(1.5, 2.0); // radius=0.5, height=3.0

// Yatay silindir yap
//transform = Matrix4.translate(4, -2.5, 1)
//                   .multiply(Matrix4.rotateX(-45))
//                   .multiply(Matrix4.scale(1.0, 2.5, 1.0));
//transform = Matrix4.rotateX(10)  // Y eksenini X'e çevir
//                   .multiply(Matrix4.translate(3, 4, 1));
//cylinder.setTransform(transform);

//Texture cylinderTexture = new CheckerboardTexture(
//    new RGB(1.0, 0.0, 0.0),  // kd1: TAM KIRMIZI
//    new RGB(0.0, 1.0, 0.0),  // kd2: TAM YEŞİL
//    new RGB(0.0, 0.0, 0.0),  // kr:  YANSIMA
//    new RGB(0.0, 0.0, 0.0),  // kt
//    100,                      // ns: shining
//    10,                       // nt
//    0.3,                      // step
//    0.0, 0.0, 0.0,            // x0, y0, z0
//    true                      // use3D
//);
//
//TexturedForm texturedSphere = new BasicTexturedForm(cylinder, cylinderTexture);
//Solid cylinderSolid = new BasicSolid(texturedSphere, new IsotropicVolume(1.5));
//solids.addSolid(cylinderSolid);
//
//System.out.println("Cylinder added.");
//////////
/**
// Dikdörtgen billboard
Billboard billboard = new Billboard(15.0, 6.0, false);

// Transform ile konumlandır
transform = Matrix4.translate(20, 0, 1.7)
                   .multiply(Matrix4.rotateY(-60));
billboard.setTransform(transform);

//Texture billboardTexture = new CheckerboardTexture(
//    new RGB(0.0, 0.0, 1.0),  // kd1: Color 1
//    new RGB(0.0, 1.0, 0.0),  // kd2: Color 2
//    new RGB(0.0, 0.0, 0.1),  // kr:  Reflection
//    new RGB(0.1, 0.0, 0.0),  // kt
//    100,                      // ns: shining
//    10,                       // nt
//    0.3,                      // step
//    0.0, 0.0, 0.0,            // x0, y0, z0
//    true                      // use3D
//);

BufferedImage pngImage = new BufferedImage(10, 10, 10);

try {
	pngImage = ImageIO.read(new File("textures/turkeyFlag.png"));
}
catch (IOException ioe) {
	ioe.printStackTrace();
}

Texture billboardTexture = new TransparentPNGTexture(
    pngImage,
    new RGB(0.5, 0.5, 0.5),  // kd: fallback color
    new RGB(0.0, 0.0, 0.0),  // kr: reflection YOK - canlılık için
    new RGB(0.0, 0.0, 0.0),  // kt: transmission YOK
    30,                      // ns: düşük shininess
    10,                      // nt: düşük
    0.90,                     // KTV: 0-1, and 1.0 is full transparent 
    0.0,                     // uOffset: no offset
    0.0,                     // vOffset: no offset  
    0.0,                     // rotX: no X rotation
    0.0,                     // rotY: no Y rotation
    40.0                     // rotZ: no Z rotation
);

((TransparentPNGTexture)billboardTexture).setBillboardDimensions(15.0, 6.0);
((TransparentPNGTexture)billboardTexture).setInverseTransform(transform.inverse());

TexturedForm texturedBillboard = new BasicTexturedForm(billboard, billboardTexture);
Solid billboardSolid = new BasicSolid(texturedBillboard, new IsotropicVolume(1.0));
solids.addSolid(billboardSolid);

System.out.println("Billboard added.");
*/
//////////
// Sphere'e uygula
BasicForm sphereMaterial = new MSphere(new Point3D(0,0,0), 1.5);

transform = Matrix4.translate(7, 0, 0);
transform = transform.multiply(Matrix4.rotateZ(0.0));
transform = transform.multiply(Matrix4.scale(1.0, 1.0, 1.0));
sphereMaterial.setTransform(transform);

// Cam dielectric
//Texture material = new DielectricTexture(
//    new RGB(0.0, 0.0, 0.8),   // white-like-blue
//    0.7,                        // reflection
//    0.4,                        // transparency  
//    200                         //shininess
//);
//BufferedImage image = new BufferedImage(10, 10, 1);

//try {
//	image = ImageIO.read(new File("textures/turkeyFlag.png"));
//} catch (IOException ioe) {
//	ioe.printStackTrace();
//}

//Texture material = new ImageTexture(
//    image,
//    new RGB(0.5, 0.5, 0.5),  // kd - Fallback color
//    new RGB(0.1, 0.1, 0.1),  // kr - Low reflection
//    new RGB(0.0, 0.0, 0.0),  // kt - No transparency
//    50,                       // ns - Material property
//    10,                       // nt - Material property
//    1.0,                      // uScale - x horizontal scale
//    1.0,                      // vScale - x vertical scale
//    0.0,                      // uOffset - No horizontal offset
//    0.0,                      // vOffset - No vertical offset
//    1.5,                      // tileSizeU - Normal tile size U
//    1.5,                      // tileSizeV - Normal tile size V
//    0.0,                      // rotX - No X rotation
//    0.0,                      // rotY - No Y rotation
//    -270.0                       // rotZ - No Z rotation
//);

Texture material = null;
//material = MetalTexture.createGold();
//material = AnodizedTexture.createAnodizedRed();
//material = new AfricanKenteTexture();
//material = new CrystalCaveTexture();
//material = new HokusaiTexture();
//material = new XRayTexture();
//material = new WoodTexture();
//material = new VoidTexture();
//material = new LambertianTexture(RGB.green);
//material = new MarbleTexture();
//material = new NorwegianRoseTexture();
//material = new TiledRoofTexture();
material = new TurkishTileTexture(
    new RGB(0.0, 0.4, 0.8),
    new RGB(1.0, 1.0, 1.0),
    0.5,
    2);
TexturedForm materialSphere = new BasicTexturedForm(sphereMaterial, material);
Solid sphereMaterialSolid = new BasicSolid(materialSphere, new IsotropicVolume(0.5));
//solids.addSolid(sphereMaterialSolid);

//System.out.println("Sphere xmaterial added.");
//////////
    // IŞIK KAMERANIN OLDUĞU YERDE
    LightSource pointLight = new PointLightSource(
      new Point3D(-5.0, 0.0, 5.0), // 
      new RGB(1.0, 1.0, 1.0)      // Renk ışık
    );
    lights.add(pointLight);
    
    System.out.println("Light source1 added.");

	// Dolgu ışığı
	LightSource frontLight = new PointLightSource(
		new Point3D(12.0, 0.0, 14.0),
		new RGB(1.0, 1.0, 1.0)  // Biraz overexpose bile edebilirsin
	);
	lights.add(frontLight);
	
	System.out.println("Light source2 added.");

	// Mevcut ışıkların yanına ekle:
//	LightSource directionalLight = new DirectionalLightSource(
//		new Vector3D(-1.0, -0.5, -1.0),  // Işık yönü (normalize edilecek)
//		new RGB(0.8, 0.8, 0.8)            // Güçlü beyaz ışık
//	);
//	lights.add(directionalLight);

//	System.out.println("Directional light added.");

	// Dar spot ışığı
//	LightSource spotLight = new SpotLightSource(
//    new Point3D(0.0, 0.0, 8.0),     // Yukarıdan tam tepede
//    new Vector3D(0, 0, -1),         // Direkt AŞAĞI doğru
//    15,                             // ÇOK DAR açı (sadece üçgen)
//    15,                             // ÇOK SERT kenar
//    new RGB(8.0, 8.0, 6.0)          // ÇOK GÜÇLÜ beyaz ışık
//);
//	lights.add(spotLight);
//	
//	System.out.println("Light source3 spot added.");
	
    // World setup
    World world = new World(
      solids,
      lights,
      new RGB(0.0, 0.0, 0.7),  // Background
      new RGB(0.1, 0.1, 0.1),  // Ambient-Shadow
      new IsotropicVolume(0.5)
    );

    System.out.println("Rendering started...");
    
    // BasicSampler kullan - daha basit
    // Others: DiadicSampler, NaiveSuperSampler(int alias)
    Sampler sampler = new BasicSampler();
    RayTracer tracer = new AdvancedRayTracer(world, 3);
    sampler.compute(camera, tracer, bimg);

    if (bimg != null)
    {
      File ff = new File("mini_test_scene.png");
      ImageIO.write(bimg, "PNG", ff);
      System.out.println("Ready: " + ff.getName());
    }
  }
  
}
