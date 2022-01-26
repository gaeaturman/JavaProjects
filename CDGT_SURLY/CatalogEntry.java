/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;

/* class for CatalogEntry */
public class CatalogEntry {
    String name;
    ArrayList<Attribute> attributes;

    public CatalogEntry(String name, ArrayList<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public String toString() {
        String outStr = "";
        outStr += name + " (";
        for (int i = 0; i < attributes.size(); i++) {
            outStr += attributes.get(i).toString() + ", ";
        }
        outStr = outStr.substring(0, outStr.length() - 2);
        outStr += ")";

        return outStr;
    }
}
