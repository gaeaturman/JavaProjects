/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;
import java.util.regex.*;
import java.io.File;

/* Execute RELATION, INSERT, PRINT commands by parsing cleaned up file. */
public class CommandExecutor {

    public static Database database = Database.getInstance();
    private final String ACCEPTEDCOMMANDS = "RELATION INSERT PRINT DESTROY DELETE";
    private final String CATALOG = "CATALOG";

    ArrayList<String> commands;
    public CommandExecutor(ArrayList<String> commands) {
        this.commands = commands;
    }

    //take in arraylist of commands (properly formatted)
    //send off to appropriate methods for RELATION, INSERT, PRINT
    public void parse() {
        String curCommand;
        String splitCommand[];

        for (int i = 0; i < commands.size(); i++) {
            curCommand = commands.get(i);
            splitCommand = curCommand.split(" ");

            if (!ACCEPTEDCOMMANDS.contains(splitCommand[0])) {
                System.err.println("Unrecognized command: " + splitCommand[0]);
                continue;
            }

            switch (splitCommand[0].toUpperCase()) {
                case "RELATION":
                    handleRelation(curCommand);
                    break;

                case "INSERT":
                    handleInsert(curCommand);
                    break;

                case "PRINT":
                    handlePrint(curCommand);
                    break;

                case "DESTROY":
                    handleDestroy(curCommand);
                    break;
                case "DELETE":
                    handleDelete(curCommand);
                    break;
            }
        }
    }


    //do RELATION, check for malformatted syntax using regex
    public void handleRelation(String command) {
        String relName = command.split(" ")[1]; // Does not handle multiple spaces after RELATION

        if (relName.equalsIgnoreCase(CATALOG)) {
            System.err.println("Cannot create relation CATALOG!");
            return;
        }

        if (Database.getCatalogEntry(relName) != null) {
            System.err.println("Relation " + relName + " already exists in Catalog.");
            return;
        }

        String regex = "^[\\S]+[ ]+[\\S]+[ ]+(\\(.*\\)\\;)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }
        String attrs = matcher.group(1).substring(1, matcher.group(1).length()-2);

        ArrayList<String> attributes = getRelAttrs(attrs);

        if (checkAttrFormat(attrs.split(" "))) {
            System.out.println("Creating " + relName + " with " + attributes.size() + " attributes.");
            Relation curRelation = new Relation(relName, attributes);
            Database.create(curRelation);
        } else {
            System.err.println("Error: Improper formatting  " + command);
        }
    }

    /* Check that there are 3 arguments per attribute */
    private boolean checkAttrFormat(String[] splitAttrs) {
        List<String> delimitedAttrs = new ArrayList<String>(Arrays.asList(splitAttrs));

        for (int i = 0; i < delimitedAttrs.size(); i++) {
            if (delimitedAttrs.get(i).equals("")) {
                delimitedAttrs.remove(i);
                i--;
            }
        }

        /* Check that the attribute is in the format:
         *     <name> <type> <length>
         */
        if (delimitedAttrs.size() % 3 != 0) {
            return false;
        }

        return true;
    }

    /* Return a list of attributes given a string of attributes */
    private ArrayList<String> getRelAttrs(String attrs) {
        String attrRegex = "[a-zA-Z]+[ ]+(CHAR|NUM)+[ ]+\\d+";
        Pattern attrPattern = Pattern.compile(attrRegex);
        Matcher attrMatcher = attrPattern.matcher(attrs);

        ArrayList<String> attributes = new ArrayList<String>();
        int attrCount = 0;
        int start, end;
        String attribute;
        while (attrMatcher.find()) {
            attrCount++;
            start = attrMatcher.start();
            end = attrMatcher.end();
            attribute = attrs.substring(start, end);
            attributes.add(attribute);
        }

        return attributes;
    }

    // do INSERT, check for malformatted syntax using regex
    public void handleInsert(String command) {
        /* Check that the command line is formatted correctly */
        String relName = command.split(" ")[1]; // won't handle multiple spaces after command INSERT
        /* Confirms INSERT_space_relName_space_(anything)_endlineChar */
        String regex = "^[\\S]+[ ]+[a-zA-z]+[ ]+.*\\;$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        if (!Database.contains(relName)) {
            System.err.println("No existing relation of type " + relName + ". Cannot insert to uncreated type.");
            return;
        }

        Relation curRel = Database.getRelation(relName);

        String attrRegex = regexBuilder(curRel.getAttributes());
        regex = "^\\S+ +[a-zA-Z]+ +"+attrRegex+" *\\;$"; //second/real regex check specific to relation attributes

        Pattern attrPattern = Pattern.compile(regex);
        Matcher attrMatcher = attrPattern.matcher(command);

        if (!attrMatcher.matches()) {
            System.err.println("Malformatted attribute(s): "+command);
            System.err.println("Relation syntax: "+ Database.getCatalogEntry(relName).toString());
            return;
        }

        /* Insert into database */
        LinkedList<String> relAttrs = separateAttrs(command);
        curRel.insert(relAttrs);
        System.out.println("Inserting "+ curRel.getNumAttrs() + " attributes to " + relName + ".");
    }

    /* Regex builder for using RELATION attribute types (e.g. CHAR 30), for
     * building string to check pattern against for proper formatting validation in handleInsert
     * takes in a string of a relations attribute number and types
     */
    private String regexBuilder(ArrayList<Attribute> attributes) {
        String regex = "";

        for (int i = 0; i < attributes.size(); i++) {
            Attribute curAttr = attributes.get(i);
            int maxLen = curAttr.getSize();
            if (curAttr.toString().contains("CHAR")) {
                regex+="(\\S{1," + maxLen + "}|\\'.{1," + maxLen + "}\\') +";
            } else if (curAttr.toString().contains("NUM")) {
                regex+="\\d{1," + maxLen + "} +";
            }
        }

        regex = regex.substring(0, regex.length()-2); //deletes the end spacer regex
        return regex;
    }


    /* Returns a list of separated attributes given a command string */
    private LinkedList<String> separateAttrs(String command) {
        String attr;
        LinkedList<String> attrs = new LinkedList<String>();

        command = command.substring(0, command.length()-1);

        String regex = "([^\\']\\S*|\\'.+\\')\\s*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        while (matcher.find()) {
            attr = matcher.group(1);
            attr = attr.replace("'", "");
            attrs.add(attr);
        }

        // Remove <RELATION> and <relation name>
        attrs.remove(0);
        attrs.remove(0);

        return attrs;
    }


    /* Print a given relation specified in the command */
    public void handlePrint(String command) {
        String regex = "^PRINT [\\S ]+\\;$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        /* Ensure correct formatting and existence of all relations listed */
        String[] printRels = command.split(" ");
        for (int i = 1; i < printRels.length; i++) {
            printRels[i] = printRels[i].replace(";", "");

            if (printRels[i].equalsIgnoreCase(CATALOG)) {
                LinkedList<CatalogEntry> cat = database.getCatalog();

                System.out.println("Catalog:\n--------");
                for (CatalogEntry ce : cat) {
                    System.out.println(ce.toString());
                }
                System.out.println();
                return;
            }

            Relation curRel = Database.getRelation(printRels[i]);
            if (curRel == null) {
                System.err.println("No existing relation of type " + printRels[i] + ". Cannot print uncreated type.");
                return;
            }

            System.out.println(Database.getRelation(printRels[i]).toString());
            System.out.println();
        }
    }

    /* Destroy a relation specified by the command from the database
     * and the catalog */
    public void handleDestroy(String command) {
        String regex = "^DESTROY [\\S]+\\;$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        relName = relName.replace(";",""); //get rid of pesky endline char

        /* Ensure relation exists in database */
        if (!Database.contains(relName)) {
            System.err.println("No existing relation of type " + relName + ". Cannot insert to uncreated type.");
            return;
        }

        Relation deadRelation = Database.getRelation(relName);
        CatalogEntry deadCatEntry = Database.getCatalogEntry(relName);
        Database.destroyRelation(deadRelation);
        Database.destroyCatEntry(deadCatEntry);
        System.out.println("Destroyed relation "+relName+".");
    }

    /* Deletes all tuples from a relation in the database */
    public void handleDelete(String command) {
        String regex = "^DELETE [\\S]+\\;$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        relName = relName.replace(";",""); //get rid of pesky endline char

        /* Ensure relation exists in database */
        if (!Database.contains(relName)) {
            System.err.println("No existing relation of type " + relName + ". Cannot insert to uncreated type.");
            return;
        }

        Relation rel = Database.getRelation(relName);
        rel.clearRelation();
        System.out.println("Deleted relation "+relName+".");
    }
}
