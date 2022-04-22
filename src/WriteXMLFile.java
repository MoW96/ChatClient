import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;

public class WriteXMLFile {

    public void writeSettingsToXML(String username) {
        try {
            Element settings = new Element("settings");
            Document document = new Document(settings);

            settings.addContent(new Element("username").setText(username));

            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat());
            xmlOutputter.output(document,new FileWriter("C:\\Users\\mwegn\\OneDrive\\Dokumente\\ChatClient\\ChatClientSettinigs.xml"));

        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
    }
}
