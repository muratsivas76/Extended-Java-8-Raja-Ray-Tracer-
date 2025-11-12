import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class ConvertUnicode {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Kullanım: java ConvertUnicode <src.txt> <dst.txt>");
            return;
        }
        
        String srcFile = args[0];
        String dstFile = args[1];
        
        try {
            convertUnicodeFile(srcFile, dstFile);
            System.out.println("✓ Unicode conversion tamamlandı: " + dstFile);
        } catch (Exception e) {
            System.err.println("✗ Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void convertUnicodeFile(String srcFile, String dstFile) throws IOException {
        // Kaynak dosyayı oku
        String content = new String(Files.readAllBytes(Paths.get(srcFile)));
        
        // Unicode escape sequence'leri decode et
        String convertedContent = decodeUnicodeEscapes(content);
        
        // Hedef dosyaya UTF-8 formatında yaz
        Files.write(Paths.get(dstFile), convertedContent.getBytes("UTF-8"));
    }
    
    private static String decodeUnicodeEscapes(String input) {
        // Regex pattern: \uXXXX formatındaki unicode escape'leri bul
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            // Unicode hex değerini al
            String hexValue = matcher.group(1);
            
            // Hex'i integer'a çevir
            int unicodeValue = Integer.parseInt(hexValue, 16);
            
            // Unicode karaktere dönüştür
            char unicodeChar = (char) unicodeValue;
            
            // Orijinal \uXXXX yerine gerçek karakteri koy
            matcher.appendReplacement(result, String.valueOf(unicodeChar));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
}