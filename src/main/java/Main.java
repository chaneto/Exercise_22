import org.json.simple.parser.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IllegalAccessException, IOException, ParseException, ParserConfigurationException, TransformerException {
        Person person = new Person(3, "Petko", "Bulgaria");
        SerializeService serializeService = new SerializeServiceImpl();
        String  serializeToJson = serializeService.serialize(person, DataType.JSON);
        System.out.println("serializeToJson: \n" + serializeToJson + "\n");
        Person deserializeFromJson = (Person) serializeService.deserialize("src/main/resources/files/json", DataType.JSON);
        System.out.println("deserializeFromJson:");
        System.out.println(deserializeFromJson.getAge());
        System.out.println(deserializeFromJson.getName());
        System.out.println(deserializeFromJson.getCountry() + "\n");

        String  serializeToXml = serializeService.serialize(person, DataType.XML);
        System.out.println("serializeToXml: \n" + serializeToXml + "\n");
        Person deserializeFromXml = (Person) serializeService.deserialize("src/main/resources/files/xml", DataType.XML);
        System.out.println("deserializeFromXml:");
        System.out.println(deserializeFromXml.getAge());
        System.out.println(deserializeFromXml.getName());
        System.out.println(deserializeFromXml.getCountry() + "\n");

        String  serializeToCsv = serializeService.serialize(person, DataType.CSV);
        System.out.println("serializeToCsv: \n" + serializeToCsv + "\n");
        Person deserializeFromCsv = (Person) serializeService.deserialize("src/main/resources/files/csv", DataType.CSV);
        System.out.println("deserializeFromCsv:");
        System.out.println(deserializeFromCsv.getAge());
        System.out.println(deserializeFromCsv.getName());
        System.out.println(deserializeFromCsv.getCountry() + "\n");
    }

}
