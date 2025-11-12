import java.io.*;
import java.util.*;

public class ClassNamesCollector {
    public static void main(String[] args) {
        String srcDir = "/home/muratsivas76/istasyon/java/raja_extended/src";
        String outFile = "/home/muratsivas76/istasyon/java/raja_extended/tool/classNames.txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            processJavaFiles(new File(srcDir), writer, srcDir);
            System.out.println("All Java class names collected successfully to tool/classNames.txt!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void processJavaFiles(File dir, BufferedWriter writer, String basePath) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processJavaFiles(file, writer, basePath);
            } else if (file.getName().endsWith(".java")) {
                writeFileContent(file, writer, basePath);
				System.out.println ("Added: "+(file.getAbsolutePath ())+"");
            }
        }
    }

    private static void writeFileContent(File javaFile, BufferedWriter writer, String basePath) throws IOException {
        StringBuffer sb = new StringBuffer();
        
        String s = javaFile.getName().replace(".java", "");
        sb.append("\t" + s + " " + s.toLowerCase() + "_variable = null;\n");
        
        writer.write(sb.toString());
    }
	
}
