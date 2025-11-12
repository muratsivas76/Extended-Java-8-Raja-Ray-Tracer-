import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AddUsageInstanceInformation {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Kullanım: java AddUsageInstanceInformation <input.java> <output_info.txt>");
            return;
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        
        try {
            processJavaFile(inputFile, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void processJavaFile(String javaFile, String outputFile) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(javaFile)));
        String className = extractClassName(content);
        ConstructorInfo constructor = extractLongestConstructor(content, className);
        
        generateExactOutput(className, constructor, outputFile);
    }
    
    private static String extractClassName(String content) {
        // class XPlainTexture implements Texture şeklini ara
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "class\\s+(\\w+)(?:\\s+extends|\\s+implements|\\s*\\{)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "UnknownClass";
    }
    
    private static ConstructorInfo extractLongestConstructor(String content, String className) {
        // Tüm constructor'ları bul
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "public\\s+" + className + "\\s*\\(([^)]*)\\)\\s*\\{",
            java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        ConstructorInfo longestConstructor = new ConstructorInfo();
        int maxParams = -1;
        
        while (matcher.find()) {
            String paramsStr = matcher.group(1).trim();
            ConstructorInfo constructor = parseConstructorParams(paramsStr);
            
            if (constructor.parameterTypes != null && constructor.parameterTypes.length > maxParams) {
                longestConstructor = constructor;
                maxParams = constructor.parameterTypes.length;
            }
        }
        
        return longestConstructor;
    }
    
    private static ConstructorInfo parseConstructorParams(String paramsStr) {
        ConstructorInfo info = new ConstructorInfo();
        
        if (paramsStr.isEmpty()) return info;
        
        // Newline ve multiple space'leri temizle
        paramsStr = paramsStr.replaceAll("\\s+", " ").trim();
        
        // Parametreleri ayır
        List<String> params = new ArrayList<>();
        StringBuilder currentParam = new StringBuilder();
        int bracketCount = 0;
        
        for (char c : paramsStr.toCharArray()) {
            if (c == '<') bracketCount++;
            if (c == '>') bracketCount--;
            
            if (c == ',' && bracketCount == 0) {
                params.add(currentParam.toString().trim());
                currentParam = new StringBuilder();
            } else {
                currentParam.append(c);
            }
        }
        params.add(currentParam.toString().trim());
        
        info.parameterTypes = new String[params.size()];
        info.parameterNames = new String[params.size()];
        
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            // Son boşluktan öncesi type, sonrası name
            int lastSpace = param.lastIndexOf(' ');
            if (lastSpace != -1) {
                info.parameterTypes[i] = param.substring(0, lastSpace).trim();
                info.parameterNames[i] = param.substring(lastSpace + 1).trim();
            } else {
                info.parameterTypes[i] = param;
                info.parameterNames[i] = "arg" + i;
            }
        }
        
        return info;
    }
    
    private static void generateExactOutput(String className, ConstructorInfo constructor, String outputFile) throws IOException {
        StringBuilder output = new StringBuilder();
        
        output.append("\t// ADDED by Murat Inan\n");
        
        // getUsageInformation - TAM XPlainTexture STİLİNDE
        output.append("\t@Override\n");
        output.append("\tpublic String getUsageInformation() {\n");
        output.append("\t\t//").append(className).append("(");
        
        if (constructor.parameterTypes != null) {
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(constructor.parameterTypes[i]).append(" ").append(constructor.parameterNames[i]);
                if (i < constructor.parameterTypes.length - 1) output.append(", ");
            }
        }
        output.append(")\n");
        
        // Örnek satırı - XPlainTexture'deki gibi
        output.append("\t\t//");
        if (constructor.parameterTypes != null) {
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(getSmartExampleValue(constructor.parameterTypes[i], constructor.parameterNames[i]));
                if (i < constructor.parameterTypes.length - 1) output.append(",  ");
            }
        }
        output.append("\n");
        
        output.append("\t\tString ").append(className.toUpperCase()).append("_STR=\"Constructor is: ").append(className).append("(");
        
        if (constructor.parameterTypes != null) {
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(constructor.parameterTypes[i]).append(" ").append(constructor.parameterNames[i]);
                if (i < constructor.parameterTypes.length - 1) output.append(", ");
            }
        }
        output.append(");\\nExample:\\n");
        
        // Örnek değerler - XPlainTexture'deki gibi formatlı
        if (constructor.parameterTypes != null) {
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(getSmartExampleValue(constructor.parameterTypes[i], constructor.parameterNames[i]));
                if (i < constructor.parameterTypes.length - 1) output.append(",  ");
            }
        }
        output.append("\\n-1 returns empty constructor.\\nEnter your values after three diyez symbol\\n###\\n\";\n");
        output.append("\t\treturn ").append(className.toUpperCase()).append("_STR;\n");
        output.append("\t}\n\n");
        
        // getInstance - TAM XPlainTexture STİLİNDE
        output.append("\t@Override\n");
        output.append("    public Texture getInstance(String info) {\n");
        output.append("\t\tTexture texture = null;\n");
        output.append("\t\t\n");
        output.append("\t\tString str = info.trim();\n");
        output.append("\t\t\n");
        output.append("\t\tint diyezIndex = str.lastIndexOf(\"###\");\n");
        output.append("\t\tif (diyezIndex < 0) return texture;\n");
        output.append("\t\t\n");
        output.append("\t\tstr = str.substring(diyezIndex+3);\t\n");
        output.append("\t\tstr = str.replaceAll(\"\\n\", \"\");\n");
        output.append("\t\tstr = str.replaceAll(\" \", \"\");\n");
        output.append("\t\t\n");
        output.append("\t\tif (str.equals(\"-1\")) return new ").append(className).append("();\n");
        output.append("\t\t\n");
        
        if (constructor.parameterTypes != null && constructor.parameterTypes.length > 0) {
            output.append("\t\tString [] split = str.split (\",\");\n");
            output.append("\t\tif (split == null) return texture;\n\n");
            
            // Yorum satırı - XPlainTexture'deki gibi
            output.append("\t\t//").append(className).append("(");
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(constructor.parameterTypes[i]).append(" ").append(constructor.parameterNames[i]);
                if (i < constructor.parameterTypes.length - 1) output.append(", ");
            }
            output.append(")\n");
            
            // Örnek yorum satırı
            output.append("\t\t//");
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                output.append(getSmartExampleValue(constructor.parameterTypes[i], constructor.parameterNames[i]));
                if (i < constructor.parameterTypes.length - 1) output.append(",  ");
            }
            output.append("\n");
            
            output.append("\t\ttry {\n");
            
            // Değişken tanımlamaları - XPlainTexture'deki gibi c1r, c1g, c1b formatında
            int currentIndex = 0;
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                String type = constructor.parameterTypes[i];
                String name = constructor.parameterNames[i];
                
                if (type.equals("RGB")) {
                    output.append("\t\t\tdouble c").append(i+1).append("r = Double.parseDouble(split[").append(currentIndex++).append("]);\n");
                    output.append("\t\t\tdouble c").append(i+1).append("g = Double.parseDouble(split[").append(currentIndex++).append("]);\n");
                    output.append("\t\t\tdouble c").append(i+1).append("b = Double.parseDouble(split[").append(currentIndex++).append("]);\n");
                } 
                // MEVCUT PRIMITIVE TÜRLER
                else if (type.equals("int")) {
                    output.append("\t\t\tint ").append(name).append(" = Integer.parseInt(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("double")) {
                    output.append("\t\t\tdouble ").append(name).append(" = Double.parseDouble(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("float")) {
                    output.append("\t\t\tfloat ").append(name).append(" = Float.parseFloat(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("boolean")) {
                    output.append("\t\t\tboolean ").append(name).append(" = Boolean.parseBoolean(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("String")) {
                    output.append("\t\t\tString ").append(name).append(" = split[").append(currentIndex++).append("];\n");
                
                // YENİ EKLENEN PRIMITIVE TÜRLER
                } else if (type.equals("long")) {
                    output.append("\t\t\tlong ").append(name).append(" = Long.parseLong(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("short")) {
                    output.append("\t\t\tshort ").append(name).append(" = Short.parseShort(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("byte")) {
                    output.append("\t\t\tbyte ").append(name).append(" = Byte.parseByte(split[").append(currentIndex++).append("]);\n");
                } else if (type.equals("char")) {
                    output.append("\t\t\tchar ").append(name).append(" = split[").append(currentIndex++).append("].charAt(0);\n");
                
                // YENİ EKLENEN OBJECT TÜRLERİ
                } else if (type.equals("java.awt.Color")) {
                    output.append("\t\t\tjava.awt.Color ").append(name).append(" = new java.awt.Color(");
                    output.append("Float.parseFloat(split[").append(currentIndex++).append("]), ");
                    output.append("Float.parseFloat(split[").append(currentIndex++).append("]), ");
                    output.append("Float.parseFloat(split[").append(currentIndex++).append("]), ");
                    output.append("Float.parseFloat(split[").append(currentIndex++).append("]));\n");
                } else if (type.equals("Ray")) {
                    output.append("\t\t\tRay ").append(name).append(" = new Ray(");
                    output.append("new Point3D(Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("])), ");
                    output.append("new Vector3D(Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("])));\n");
                } else if (type.equals("Matrix4")) {
                    output.append("\t\t\tMatrix4 ").append(name).append(" = new Matrix4();\n");
                    currentIndex++; // Matrix4 için 1 slot ayır
                } else if (type.equals("FloatColor")) {
                    output.append("\t\t\tFloatColor ").append(name).append(" = new FloatColor(");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]));\n");
                
                // DİĞER OBJECT TÜRLERİ (mevcut)
                } else if (type.equals("Color")) {
                    output.append("\t\t\tColor ").append(name).append(" = new Color(");
                    output.append("Integer.parseInt(split[").append(currentIndex++).append("]), ");
                    output.append("Integer.parseInt(split[").append(currentIndex++).append("]), ");
                    output.append("Integer.parseInt(split[").append(currentIndex++).append("]));\n");
                } else if (type.equals("Point3D")) {
                    output.append("\t\t\tPoint3D ").append(name).append(" = new Point3D(");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]));\n");
                } else if (type.equals("Vector3D")) {
                    output.append("\t\t\tVector3D ").append(name).append(" = new Vector3D(");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]), ");
                    output.append("Double.parseDouble(split[").append(currentIndex++).append("]));\n");
                
                // BİLİNMEYEN TÜRLER
                } else {
                    output.append("\t\t\tObject ").append(name).append(" = null; // ").append(type).append(" tipi manual handle edilmeli\n");
                    currentIndex++; // Bilinmeyen tür için 1 slot
                }
            }
            
            output.append("\n\t\t\ttexture = new ").append(className).append("(\n\t\t\t\t");
            
            // Constructor parametreleri - XPlainTexture'deki gibi
            for (int i = 0; i < constructor.parameterTypes.length; i++) {
                String type = constructor.parameterTypes[i];
                String name = constructor.parameterNames[i];
                
                if (type.equals("RGB")) {
                    output.append("new RGB(c").append(i+1).append("r, c").append(i+1).append("g, c").append(i+1).append("b)");
                } else if (type.equals("String") && (name.toLowerCase().contains("3d") || name.toLowerCase().contains("use"))) {
                    output.append(name).append(".toLowerCase()");
                } else {
                    output.append(name);
                }
                
                if (i < constructor.parameterTypes.length - 1) {
                    output.append(", ");
                    if ((i + 1) % 3 == 0) output.append("\n\t\t\t\t");
                }
            }
            
            output.append(");\n");
            output.append("\t\t\treturn texture;\n");
            output.append("\t\t} catch (NumberFormatException nfe) {\n");
            output.append("\t\t\tnfe.printStackTrace();\n");
            output.append("\t\t\treturn null;\n");
            output.append("\t\t}\n");
        }
        
        output.append("\t}\n");
        output.append("\t////////////////\n\n");
        
        // toString metodu - BOX FORMATINDA EKLENİYOR
        output.append("\t// toString metodu - Box formatında\n");
        output.append("\t@Override\n");
        output.append("\tpublic String toString() {\n");
        output.append("\t\tStringBuilder sb = new StringBuilder();\n");
        output.append("\t\tsb.append(\"").append(className).append(" {\");\n");
        
        if (constructor.parameterNames != null) {
            for (int i = 0; i < constructor.parameterNames.length; i++) {
                String fieldName = constructor.parameterNames[i];
                output.append("\t\tsb.append(\"\\\\n    ").append(fieldName).append(" = \");\n");
                output.append("\t\tsb.append(this.").append(fieldName).append(");\n");
                output.append("\t\tsb.append(\";\");\n");
            }
        }
        
        output.append("\t\tsb.append(\"\\\\n};\");\n");
        output.append("\t\treturn sb.toString();\n");
        output.append("\t}\n");
        
        Files.write(Paths.get(outputFile), output.toString().getBytes());
        System.out.println("✓ " + className + " için TAM ISTEDİĞİN ÇIKTI + toString " + outputFile + " dosyasına yazıldı!");
    }
    
    private static String getSmartExampleValue(String type, String paramName) {
        String lowerName = paramName.toLowerCase();
        
        if (type.equals("RGB")) {
            if (lowerName.contains("kd") || lowerName.equals("color")) return "0.0,0.0,0.0";
            if (lowerName.contains("krl")) return "0.0,1.0,0.0";
            if (lowerName.contains("krg")) return "0.0,0.0,1.0";
            if (lowerName.contains("ktl")) return "0.3,0.3,0.3";
            if (lowerName.contains("ktg")) return "0.1,0.1,0.1";
            return "0.5,0.5,0.5";
        } else if (type.equals("int")) {
            if (lowerName.contains("ns")) return "100";
            if (lowerName.contains("nt")) return "10";
            return "40";
        } else if (type.equals("double")) {
            if (lowerName.contains("step")) return "4.5";
            return "0.0";
        } else if (type.equals("String")) {
            return "true";
        } else if (type.equals("boolean")) {
            return "true";
        
        // YENİ EKLENEN TÜRLER İÇİN ÖRNEK DEĞERLER
        } else if (type.equals("long")) {
            return "123456789";
        } else if (type.equals("short")) {
            return "100";
        } else if (type.equals("byte")) {
            return "64";
        } else if (type.equals("char")) {
            return "A";
        } else if (type.equals("java.awt.Color")) {
            return "1.0,0.5,0.0,1.0"; // RGBA
        } else if (type.equals("Ray")) {
            return "0.0,0.0,0.0,1.0,0.0,0.0"; // origin(x,y,z) + direction(x,y,z)
        } else if (type.equals("Matrix4")) {
            return "identity"; // Varsayılan matrix
        } else if (type.equals("FloatColor")) {
            return "1.0,0.0,0.0,1.0"; // RGBA double
        } else if (type.equals("float")) {
            return "1.0f";
        } else if (type.equals("Color")) {
            return "255,0,0"; // RGB
        } else if (type.equals("Point3D")) {
            return "0.0,0.0,0.0";
        } else if (type.equals("Vector3D")) {
            return "1.0,0.0,0.0";
        }
        
        return "null";
    }
    
    static class ConstructorInfo {
        String[] parameterTypes;
        String[] parameterNames;
    }
    
}
