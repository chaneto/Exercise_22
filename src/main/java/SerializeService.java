import org.json.simple.parser.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface SerializeService<T> {

    String serialize(T t, DataType type) throws IllegalAccessException, IOException, ParserConfigurationException, TransformerException;
    T deserialize(String data, DataType type) throws IOException, ParseException, IllegalAccessException;
}
