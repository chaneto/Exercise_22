import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.*;

public class SerializeServiceImpl implements SerializeService {

    @Override
    public String serialize(Object object, DataType type) throws IllegalAccessException, IOException, ParserConfigurationException, TransformerException {
        String result = "";
        JSONObject jsonObject = new JSONObject();
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Class personReflection = object.getClass();
        Element className = document.createElement(personReflection.getSimpleName().toLowerCase());
        String csv = "";
        Field[] fields = personReflection.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (isForSerialization(field)) {
                if (type.name().equals("JSON")) {
                    jsonObject.put(field.getName(), field.get(object));
                } else if (type.name().equals("XML")) {
                    Element element = document.createElement(field.getName());
                    element.appendChild(document.createTextNode(String.valueOf(field.get(object))));
                    className.appendChild(element);
                }else if (type.name().equals("CSV")) {
                    csv += field.getName() + "," + field.get(object) + " ";
                }

            }
        }
        if (type.name().equals("JSON")) {
            writeFile("src/main/resources/files/json", jsonObject.toJSONString());
            result = jsonObject.toJSONString();
        } else if (type.name().equals("XML")) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StreamResult output = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(className);
            transformer.transform(source, output);
            String xmlString = output.getWriter().toString();
            writeFile("src/main/resources/files/xml", xmlString);
            result = xmlString;
        } else if (type.name().equals("CSV")) {
            String output = "";
            String[] values = csv.split(" ");
            List<String> filedNames = new ArrayList<>();
            List<String> fieldValues = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                for(String item: values){
                    String[] fieldData = item.split(",");
                    if(i == 0){
                        filedNames.add(fieldData[0]);
                    }else {
                        fieldValues.add(fieldData[1]);
                   }

                }
            }
            output += String.join(",", filedNames) + "\n" + String.join(",", fieldValues);

            writeFile("src/main/resources/files/csv", output);

            result = output;
        }


        return result;
    }

    @Override
    public Object deserialize(String data, DataType type) throws IOException, ParseException, IllegalAccessException {
        Person person = new Person();
        String dataFile = getDataFromFile(data);
        if (type.name().equals("JSON")) {
            String[] jsonArray = dataFile.substring(1, dataFile.length() - 1).split(",");
            for (String item : jsonArray) {
                String[] values = item.split(":");
                String fieldName = values[0].substring(1, values[0].length() - 1);
                String value = values[1];
                if (value.contains("\"")) {
                    value = values[1].substring(1, values[1].length() - 1);
                }
                setFieldsValues(person, fieldName, value);

            }

        } else if (type.name().equals("XML")) {
            List<String> xmlFields = new ArrayList<>();
            String[] xmlValues = dataFile.split("<");
            for (int i = 2; i < xmlValues.length; i++) {
                String[] hhh = xmlValues[i].split("\\/");
                if (hhh[0].equals("")) {
                    continue;
                }
                xmlFields.add(hhh[0]);
            }
            for (String item : xmlFields) {
                String[] values = item.split(">");
                String fieldName = values[0];
                String value = values[1];
                setFieldsValues(person, fieldName, value);
            }
        } else if(type.name().equals("CSV")) {
            String[] lines = dataFile.split("\n");
            List<String> values = new ArrayList<>();
            for (int i = 0; i < lines.length; i++) {
                String[] lineValues = lines[i].split(",");
                for (int j = 0; j < lineValues.length; j++) {
                    if(i == 0){
                        values.add(lineValues[j]);
                    }else {
                        values.set(j ,values.get(j).trim() + "," + lineValues[j]);
                    }
                }
            }

            for (int i = 0; i < values.size(); i++) {
                String[] fieldValues = values.get(i).split(",");
                System.out.println(values.get(i));
               setFieldsValues(person, fieldValues[0], fieldValues[1]);
            }
        }
        return person;
    }

    public boolean isForSerialization(Field field) {
        boolean result = false;
        for (Annotation item : field.getAnnotations()) {
            if (item.annotationType().getSimpleName().equals("SerializableField")) {
                result = true;
            }
        }
        return result;
    }

    public void writeFile(String filePath, String file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        OutputStreamWriter out = new OutputStreamWriter(fileOutputStream, UTF_8);
        out.write(file);
        out.close();
        fileOutputStream.close();
    }

    public String getDataFromFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath))
                .stream()
                .filter(f -> !f.isEmpty())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public void setFieldsValues(Object object, String fieldName, String value) throws IllegalAccessException {
        Class personReflection = object.getClass();
        Field[] fields = personReflection.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (isForSerialization(field)) {
                if (fieldName.equals(field.getName())) {
                    if (field.getType().getSimpleName().equals("Integer")) {
                        field.set(object, Integer.parseInt(value));
                    } else if (field.getType().getSimpleName().equals("Long")) {
                        field.set(object, Long.valueOf(value));
                    } else {
                        field.set(object, value);
                    }

                }
            }
        }
    }
}
