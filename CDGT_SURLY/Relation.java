/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;

/* class for RELATION */
public class Relation {
    String name;
    ArrayList<Attribute> attributes;
    int numAttrs;
    LinkedList<LinkedList<String>> rows;

    public Relation(String name, ArrayList<String> attributes) {
        this.name = name;
        this.attributes = convertAttrs(attributes);
        this.rows = new LinkedList<LinkedList<String>>();
        numAttrs = attributes.size();
    }

    public void insert(LinkedList<String> row) {
        rows.add(row);
    }

    public void clearRelation() {
        rows.clear();
    }

    public Boolean isEmpty() {
        if (rows.size() == 0) {
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public LinkedList<String> getRow(int i) {
        if (i < rows.size()) {
            return rows.get(i);
        } else {
            return null;
        }
    }

    public int getNumAttrs() {
        return numAttrs;
    }

    private ArrayList<Attribute> convertAttrs(ArrayList<String> attributes) {
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        for (int i = 0; i < attributes.size(); i++) {
            String curAttr = attributes.get(i);
            String[] splitAttrs = curAttr.split(" ");
            String attrName = splitAttrs[0];
            String attrType = splitAttrs[1];
            int attrSize = Integer.parseInt(splitAttrs[2]);
            Attribute newAttr = new Attribute(attrName, attrType, attrSize);
            attrs.add(newAttr);
        }
        return attrs;
    }

    // Constructs a string representation of the relation
    public String toString() {
        String outString = "";
        String temp = "";
        int[] formatWidths = getColumnWidths();

        outString += formatPrintHeader(formatWidths);

        for (LinkedList<String> curRow : rows) {
            outString += "| ";
            for (int j = 0; j < curRow.size(); j++) {
                String curAttr = curRow.get(j);
                curAttr = String.format("%-" + formatWidths[j] + "s", curAttr);
                if (j < curRow.size() - 1) {
                    outString += curAttr + " | ";
                } else {
                    outString += curAttr + " |\n";
                }
            }
        }
        outString += addDashes(getTableWidth()) + "\n";

        return outString;
    }

    // Formats the top of a table print statement with the relation name
    // and its column names
    private String formatPrintHeader(int[] formatWidths) {
        String header = "";

        header += addDashes(getTableWidth()) + "\n";

        int titleWidth = getTableWidth() - 3;
        header += "| " + String.format("%-" + titleWidth + "s|\n", name);
        header += "|" + addDashes(getTableWidth() - 2) + "|\n";
        header += "| ";
        for (int i = 0; i < attributes.size(); i++) {
            String attrName = attributes.get(i).getName();
            attrName = String.format("%-" + formatWidths[i] + "s", attrName);
            if (i < attributes.size() - 1) {
                header += attrName + " | ";
            } else {
                header += attrName + " |\n";
            }
        }
        header += "|" + addDashes(getTableWidth() - 2) + "|\n";
        return header;
    }

    // Adds a horizontal line of dashes to a given string
    // Assumes that there was a newline character before passing to addDashes()
    private String addDashes(int numDashes) {
        String dashes = "";
        for (int i = 0; i < numDashes; i++) {
            dashes += "-";
        }
        return dashes;
    }

    // Returns the width of a given relation for printing purposes
    private int getTableWidth() {
        int tableWidth = 3*(numAttrs-1) + 4;
        for (int width : getColumnWidths()) {
            tableWidth += width;
        }
        return tableWidth;
    }

    // Returns an array of max widths for each attribute for printing purposes
    private int[] getColumnWidths() {
        int[] widths = new int[numAttrs];
        // Initialize widths with length of attribute names
        for (int c = 0; c < attributes.size(); c++) {
            widths[c] = attributes.get(c).getName().length();
        }

        // Go through all the rows and make the columns the correct width
        for (int i = 0; i < rows.size(); i++) {
            LinkedList<String> curRow = rows.get(i);
            for (int j = 0; j < numAttrs; j++) {
                int length = curRow.get(j).length();
                widths[j] = Math.max(widths[j], length);
            }
        }

        return widths;
    }
}
