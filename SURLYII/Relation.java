/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;
import java.util.regex.*;

/* class for RELATION */
public class Relation {
    String name;
    ArrayList<Attribute> attributes;
    int numAttrs;
    LinkedList<LinkedList<String>> rows;
    boolean isTemp;

    private final String FORMAT_REGEX = "([\\S\' ]+ (<|>|<=|>=|=|!=) [\\S\' ]+ AND |[\\S\' ]+ (<|>|<=|>=|=|!=) [\\S\' ]+ OR |[\\S\' ]+ (<|>|<=|>=|=|!=) [\\S\' ]+;)+";
    private final String COND_REGEX = "(AND|OR|\\S+ +(<|>|<=|>=|=|!=) +(('([^']*?)')+|[a-zA-Z0-9]+))";

    public Relation(String name, ArrayList<String> attributes) {
        this.name = name;
        this.attributes = convertAttrs(attributes);
        this.rows = new LinkedList<LinkedList<String>>();
        numAttrs = attributes.size();
        isTemp = false;
    }

    public Relation(String name, ArrayList<Attribute> attributes, LinkedList<LinkedList<String>> rows) {
        this.name = name;
        this.attributes = attributes;
        this.rows = rows;
        numAttrs = attributes.size();
        isTemp = true;
    }

    public void insert(LinkedList<String> row) {
        rows.add(row);
    }

    //TODO: Handle quoted strings in where conditions
    public LinkedList<LinkedList<String>> where(String conditions) {
        /* If there is no where clause, return all the rows */
        if (conditions.isEmpty()) return getRows();

        conditions = conditions.toUpperCase();
        ArrayList<String> condList = condSplit(conditions);
        LinkedList<LinkedList<String>> returnRows = new LinkedList<LinkedList<String>>();

        if (condList == null) {
            System.err.println("Malformatted WHERE conditions.");
            return null;
        }

        for (LinkedList<String> row : rows) {

            ArrayList<String> condCopy = new ArrayList<String>(condList) ;
            if (!condList.contains("AND") && !condList.contains("OR")) {
                if (evalCond(condList.get(0), row)) {
                    returnRows.add(row);
                }
            } else if (whereHelper(row, condCopy)) {
                    returnRows.add(row);
            }
        }

        return returnRows;
    }

    private boolean whereHelper(LinkedList<String> row, ArrayList<String> conditions) {

        conditions = whereHelperHelp(row, conditions, "and");
        conditions = whereHelperHelp(row, conditions, "or");

        boolean ret = conditions.get(0) == "true" ? true : false;
        return ret;
    }

    private ArrayList<String> whereHelperHelp(LinkedList<String> row, ArrayList<String> conditions, String op) {
        for (int i = 0; i < conditions.size(); i++) {
            String cond = conditions.remove(i);
            if (cond.equalsIgnoreCase(op)) {
                String prev = conditions.remove(i-1);
                String next = conditions.remove(i-1);
                String result = evalConds(prev, cond, next, row);
                conditions.add(i-1, result);
                i--;
            } else {
                conditions.add(i, cond);
            }

        }

        return conditions;
    }

    private boolean evalCond(String cond, LinkedList<String> row) {

        if (cond.equals("true")) {
            return true;
        } else if (cond.equals("false")) {
            return false;
        }

        cond = cond.replace("'", "");
        String[] condParts = cond.split(" ");
        if (condParts.length > 3) {
            String[] temp = new String[3];
            String quotedThing = "";
            for (int i = 2; i < condParts.length; i++) {
                quotedThing += condParts[i] + " ";
            }
            quotedThing = quotedThing.trim();

            temp[0] = condParts[0];
            temp[1] = condParts[1];
            temp[2] = quotedThing;

            condParts = temp;
        }

        Attribute attr = null;
        int counter = 0;

        for (Attribute attribute : attributes) {
            if (attribute.getName().equalsIgnoreCase(condParts[0])) {
                attr = attribute;
                break;
            }
            counter+=1;
        }

        if (attr != null) {
            String attrVal = row.get(counter).toUpperCase();
            String condVal = condParts[2].toUpperCase();

            switch (condParts[1]) {
                case "=":
                    if (attrVal.equals(condVal)) return true;
                    break;
                case "!=":
                    if (!attrVal.equals(condVal)) return true;
                    break;
                case ">=":
                    if (attrVal.compareTo(condVal) >= 0) return true;
                    break;
                case "<=":
                    if (attrVal.compareTo(condVal) <= 0) return true;
                    break;
                case ">":
                    if (attrVal.compareTo(condVal) > 0) return true;
                    break;
                case "<":
                    if (attrVal.compareTo(condVal) < 0) return true;
                    break;
            }
        }

        return false;
    }

    //TODO: Add evalCond function that checks a conditions like 'CNUM = CSCI123'
    // and make sure that it checks the existence and types of the attributes

    private String evalConds(String cond1, String op, String cond2, LinkedList<String> row) {

        boolean bool1 = evalCond(cond1, row);
        boolean bool2 = evalCond(cond2, row);

        if (op.equals("AND")) {
            if (bool1 && bool2) {
                return "true";
            }
        } else if (op.equals("OR")) {
            if (bool1 || bool2) {
                return "true";
            }
        }

        return "false";
    }

    private ArrayList<String> condSplit(String conditions) {
        Pattern format = Pattern.compile(FORMAT_REGEX);
        Pattern cond = Pattern.compile(COND_REGEX);
        Matcher matcher = format.matcher(conditions);
        if (!matcher.matches()) {
            return null;
        }

        conditions = conditions.substring(0, conditions.length()-1);
        ArrayList<String> condList = new ArrayList<String>();
        matcher = cond.matcher(conditions);
        while (matcher.find()) {
            condList.add(matcher.group());
        }

        return condList;
    }

    /* For debugging purposes to print a linked list of linked lists */
    private void printLL(LinkedList<LinkedList<String>> ll, String title) {
        System.out.println(title);
        for(LinkedList<String> lst : ll) {
            System.out.println(lst.toString());
        }
    }

    public LinkedList<LinkedList<String>> join(Relation rel2, int[] attrIndicies, String relop) {
        LinkedList<String> returnCurrRow = new LinkedList<String>();
        LinkedList<String> temp = new LinkedList<String>();
        LinkedList<LinkedList<String>> rows2 = rel2.getRows();
        LinkedList<LinkedList<String>> returnRows = new LinkedList<LinkedList<String>>();

        int numRows = rows.size();
        int numRows2 = rows2.size();

        int attrIndex1 = attrIndicies[0];
        int attrIndex2 = attrIndicies[1];

        for (int i = 0; i < numRows; i++) {
            LinkedList<String> curRow1 = rows.get(i);
            for (int j = 0; j < numRows2; j++) {
                LinkedList<String> curRow2 = rows2.get(j);

                String s1 = curRow1.get(attrIndex1);
                String s2 = curRow2.get(attrIndex2);

                if (s1.equalsIgnoreCase(s2)) {
                    returnRows.add(constructJoinRow(curRow1, curRow2, attrIndex2));
                }
            }
        }

        return returnRows;
    }

    private LinkedList<String> constructJoinRow(LinkedList<String> r1, LinkedList<String> r2, int indexToRemove) {
        LinkedList<String> retRow = new LinkedList<>();

        for (String s : r1) {
            retRow.add(s);
        }
        for (String s : r2) {
            retRow.add(s);
        }
        retRow.remove(r1.size() + indexToRemove);

        return retRow;
    }

    public int deleteRows(LinkedList<LinkedList<String>> toDelete) {
        int numRemoved = 0;
        for (LinkedList<String> cur : toDelete) {
            rows.remove(cur);
            numRemoved += 1;
        }
        return numRemoved;
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

    public ArrayList<Attribute> getAttributes(ArrayList<String> attrs) {
        ArrayList<Attribute> returnAttrs = new ArrayList<>();
        for (Attribute attr : attributes) {
            if (attrs.contains(attr.getName())) {
                returnAttrs.add(attr);
            }
        }
        return returnAttrs;
    }

    public LinkedList<String> getRow(int i) {
        if (i < rows.size()) {
            return rows.get(i);
        } else {
            return null;
        }
    }

    public LinkedList<LinkedList<String>> getRows() {
        return rows;
    }

    public LinkedList<LinkedList<String>> getRows(ArrayList<Attribute> attrs) {
        int[] indices = new int[attrs.size()];
        for (int i = 0; i < attrs.size(); i++) {
            indices[i] = attributes.indexOf(attrs.get(i));
        }

        LinkedList<LinkedList<String>> returnRows = new LinkedList<>();
        LinkedList<String> curRow = new LinkedList<>();

        for (LinkedList<String> row : rows) {
            curRow = new LinkedList<>();
            for (int i : indices) {
                curRow.add(row.get(i));
            }
            returnRows.add(curRow);
        }

        return returnRows;
    }

    public int getNumAttrs() {
        return numAttrs;
    }

    public int indexOfAttr(String attr) {
        String currAttr = "";
        for (int i = 0; i < attributes.size(); i++) {
            currAttr = attributes.get(i).getName();
            if (currAttr.equalsIgnoreCase(attr)) {
                return i;
            }
        }
        return -1;
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
