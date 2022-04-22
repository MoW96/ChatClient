import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

public class ReadXMLFile {
    private String username;

    public ReadXMLFile() {
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File("C:\\Users\\mwegn\\OneDrive\\Dokumente\\ChatClient\\ChatClientSettinigs.xml");

        try {
            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            username  = rootNode.getChildText("username");
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomEx) {
            System.out.println(jdomEx.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}
