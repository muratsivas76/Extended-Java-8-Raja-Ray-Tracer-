public class SceneParser {
    public List<Object> parseSceneFile(String filename) throws IOException {
        List<Object> sceneObjects = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        StringBuilder currentObject = new StringBuilder();
        boolean inObject = false;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            if (line.isEmpty()) continue;
            
            // "---" ile biten satırı görünce nesneyi kaydet
            if (line.startsWith("---")) {
                if (currentObject.length() > 0) {
                    Object obj = parseObject(currentObject.toString());
                    if (obj != null) sceneObjects.add(obj);
                    currentObject.setLength(0);
                    inObject = false;
                }
                continue;
            }
            
            // "raja." ile başlayan satırı görünce yeni nesne başlıyor
            if (line.startsWith("raja.")) {
                if (currentObject.length() > 0) {
                    // Önceki nesneyi kaydet
                    Object obj = parseObject(currentObject.toString());
                    if (obj != null) sceneObjects.add(obj);
                    currentObject.setLength(0);
                }
                inObject = true;
            }
            
            if (inObject) {
                currentObject.append(line).append("\n");
            }
        }
        
        // Dosya sonunda kalan nesneyi de kaydet
        if (currentObject.length() > 0) {
            Object obj = parseObject(currentObject.toString());
            if (obj != null) sceneObjects.add(obj);
        }
        
        reader.close();
        return sceneObjects;
    }
    
    private Object parseObject(String objectDefinition) {
        try {
            StringReader stringReader = new StringReader(objectDefinition);
            ObjectReader reader = new ObjectReader(stringReader);
            
            if (objectDefinition.contains("AfricanKenteTexture")) {
                return AfricanKenteTexture.build(reader);
            } else if (objectDefinition.contains("MSphere")) {
                return MSphere.build(reader);
            } else if (objectDefinition.contains("MPlane")) {
                return MPlane.build(reader);
            } else if (objectDefinition.contains("PointLightSource")) {
                return PointLightSource.build(reader);
            } else if (objectDefinition.contains("BasicTexturedForm")) {
                return BasicTexturedForm.build(reader);
            }
            // ... diğer sınıfları ekle
            
        } catch (Exception e) {
            System.err.println("Parse error for: " + objectDefinition);
            e.printStackTrace();
            return null;
        }
    }
}