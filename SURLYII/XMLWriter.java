import java.util.*;
import java.io.InputStream;
import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class XMLWriter {
    private InputStream xmlFile;
    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private Document doc;

    public XMLWriter(Relation rel) {
        String relName = rel.getName().toLowerCase();

        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();

            // Create root element
            Element root = doc.createElement(relName);
            doc.appendChild(root);

            // Add attributes to root
            Element attributes = createAttrs(rel);
            root.appendChild(attributes);

            // Add rows to root
            Element rows = createRows(rel);
            root.appendChild(rows);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(relName+".xml"));

            transformer.transform(domSource, streamResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element createAttrs(Relation rel) {
        Element attributes = doc.createElement("attributes");

        ArrayList<Attribute> attrs = rel.getAttributes();
        for (Attribute attr : attrs) {
            String name = attr.getName();
            String type = attr.getType();
            String size = Integer.toString(attr.getSize());

            Element attribute = doc.createElement("attribute");
            Attr attrName = doc.createAttribute("name");
            attrName.setValue(name);
            attribute.setAttributeNode(attrName);

            Element eType = doc.createElement("type");
            eType.appendChild(doc.createTextNode(type));
            attribute.appendChild(eType);

            Element eSize = doc.createElement("size");
            eSize.appendChild(doc.createTextNode(size));
            attribute.appendChild(eSize);

            attributes.appendChild(attribute);
        }

        return attributes;
    }

    private Element createRows(Relation rel) {
        Element rows = doc.createElement("rows");

        LinkedList<LinkedList<String>> relRows = rel.getRows();
        for (LinkedList<String> relRow : relRows) {
            Element row = doc.createElement("row");

            for (String value : relRow) {
                Element el = doc.createElement("el");
                el.appendChild(doc.createTextNode(value));
                row.appendChild(el);
            }

            rows.appendChild(row);
        }

        return rows;
    }
}
