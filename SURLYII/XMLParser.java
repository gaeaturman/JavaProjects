import java.util.*;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLParser {
    private InputStream xmlFile;
    private DocumentBuilderFactory dbFactory;
    private DocumentBuilder dBuilder;
    private Document doc;

    public XMLParser(String xmlPath) {
        try {
            xmlFile = getClass().getResourceAsStream(xmlPath);
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Relation getRelation() {
        /* Variables to hold info for the relation */
        String relName = doc.getDocumentElement().getNodeName();
        relName = relName.toUpperCase();
        ArrayList<String> attributes = getAttrs();
        LinkedList<LinkedList<String>> rows = getRows();

        Relation rel = new Relation(relName, attributes);
        for (LinkedList<String> row : rows) {
            rel.insert(row);
        }

        return rel;
    }

    private ArrayList<String> getAttrs() {
        ArrayList<String> attributes = new ArrayList<>();

        /* Loop through all the attributes */
        NodeList nList = doc.getElementsByTagName("attribute");
        for (int index = 0; index < nList.getLength(); index++) {
            // Get the attribute node and its name
            Node attr = nList.item(index);
            Element eAttr = (Element) attr;
            String attrName = eAttr.getAttribute("name");

            // Get the contents of the node
            NodeList nlType = eAttr.getElementsByTagName("type");
            Node nType = nlType.item(0);
            String type = nType.getTextContent();

            NodeList nlSize = eAttr.getElementsByTagName("size");
            Node nSize = nlSize.item(0);
            String size = nSize.getTextContent();

            // Add the space-delimited attribute info to the list of attributes
            attributes.add(attrName + " " + type + " " + size);
        }

        return attributes;
    }

    private LinkedList<LinkedList<String>> getRows() {
        LinkedList<LinkedList<String>> rows = new LinkedList<>();

        /* Loop through all the rows */
        NodeList nList = doc.getElementsByTagName("row");
        for (int nindex = 0; nindex < nList.getLength(); nindex++) {
            Node curRow = nList.item(nindex);
            Element eCurRow = (Element) curRow;

            NodeList rowInfo = eCurRow.getElementsByTagName("el");
            LinkedList<String> row = new LinkedList<>();
            for (int elindex = 0; elindex < rowInfo.getLength(); elindex++) {
                Node info = rowInfo.item(elindex);
                Element eInfo = (Element) info;

                String elText = eInfo.getTextContent();
                row.add(elText);
            }

            rows.add(row);
        }

        return rows;
    }
}
