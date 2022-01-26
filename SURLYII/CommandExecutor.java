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
    private final String ACCEPTEDCOMMANDS = "RELATION INSERT PRINT DESTROY DELETE IMPORT EXPORT";
    private final String ASSIGNCOMMANDS = "SELECT PROJECT JOIN";
    private final String CATALOG = "CATALOG";

    private final String RELATION_REGEX = "^[\\S]+[ ]+[\\S]+[ ]+(\\(.*\\)\\;)$";
    private final String TEMP_REL_REGEX = "^\\S+ +=.*;$";
    private final String INSERT_REGEX = "^[\\S]+[ ]+[a-zA-z]+[ ]+.*\\;$";
    private final String ATTRIBUTE_REGEX = "([^\\']\\S*|\\'.+\\')\\s*";
    private final String SYNTAX_REGEX = "^\\S+ ([<>=! ]|[a-zA-Z0-9&\\']|[a-zA-Z0-9&] |[a-zA-Z0-9&] *, *[a-zA-Z0-9&])+;$";
    private final String JOIN_REGEX = "^JOIN [a-zA-Z0-9]+ [a-zA-Z0-9]+ ON [a-zA-Z0-9.]+ = [a-zA-Z0-9.]+;$";
    private final String IO_REGEX = "^(IMPORT|EXPORT) +\\S+;$";

    ArrayList<String> commands;
    public CommandExecutor(ArrayList<String> commands) {
        this.commands = commands;
    }

    //take in arraylist of commands (properly formatted)
    //send off to appropriate methods for RELATION, INSERT, PRINT, DELETE, and DESTROY
    public void parse() {
        String curCommand;
        String splitCommand[];

        for (int i = 0; i < commands.size(); i++) {
            curCommand = commands.get(i);
            splitCommand = curCommand.split(" ");

            if (checkForAssignment(curCommand)) {
                parseAssignment(curCommand);
                continue;
            }

            if (!ACCEPTEDCOMMANDS.contains(splitCommand[0].toUpperCase())) {
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

                case "IMPORT":
                    handleImport(curCommand);
                    break;

                case "EXPORT":
                    handleExport(curCommand);
                    break;
            }
        }
    }

    /* If the current command is assigning a temporary relation, handle those
     * commands for SELECT, PROJECT, and JOIN */
    private void parseAssignment(String command) {
        String[] splitCommand = command.split(" ");

        if (!ASSIGNCOMMANDS.contains(splitCommand[2].toUpperCase())) {
            System.err.println("Unrecognized command: " + splitCommand[2]);
            return;
        }

        String varName = splitCommand[0];
        String assignmentCommand = "";
        for (int i = 2; i < splitCommand.length; i++) {
            assignmentCommand += splitCommand[i];
            if (i != splitCommand.length - 1) assignmentCommand += " ";
        }

        switch (splitCommand[2].toUpperCase()) {
            case "SELECT":
                handleSelect(assignmentCommand, varName);
                break;

            case "PROJECT":
                handleProject(assignmentCommand, varName);
                break;

            case "JOIN":
                handleJoin(assignmentCommand, varName);
                break;
        }
    }

    /* Check if the current command is attempting to assign a temporary relation */
    private boolean checkForAssignment(String command) {
        Pattern pattern = Pattern.compile(TEMP_REL_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            return true;
        }
        return false;
    }


    /* do RELATION, check for malformatted syntax using regex */
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

        Pattern pattern = Pattern.compile(RELATION_REGEX);
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

    /* do INSERT, check for malformatted syntax using regex */
    public void handleInsert(String command) {
        /* Check that the command line is formatted correctly */
        String relName = command.split(" ")[1]; // won't handle multiple spaces after command INSERT
        /* Confirms INSERT_space_relName_space_(anything)_endlineChar */
        Pattern pattern = Pattern.compile(INSERT_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        if (!Database.contains(relName, false)) {
            System.err.println("No existing relation of type " + relName + ". Cannot insert to uncreated type.");
            return;
        }

        Relation curRel = Database.getRelation(relName, false);

        String attrRegex = regexBuilder(curRel.getAttributes());
        String regex = "^\\S+ +[a-zA-Z]+ +"+attrRegex+" *\\;$"; //second/real regex check specific to relation attributes

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
     * takes in a string of a relations attribute number and types */
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

        Pattern pattern = Pattern.compile(ATTRIBUTE_REGEX);
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
        Pattern pattern = Pattern.compile(SYNTAX_REGEX);
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
                return;
            }

            Relation curRel = Database.getRelation(printRels[i], true);

            if (curRel == null) {
                System.err.println("No existing relation of type " + printRels[i] + ". Cannot print uncreated type.");
                return;
            }

            System.out.println(Database.getRelation(printRels[i], true).toString());
        }
    }

    /* Destroy a relation specified by the command from the database
     * and the catalog */
    public void handleDestroy(String command) {
        Pattern pattern = Pattern.compile(SYNTAX_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        relName = relName.replace(";",""); //get rid of pesky endline char

        /* Ensure relation exists in database */
        if (!Database.contains(relName, false)) {
            System.err.println("No existing relation of type " + relName + ". Cannot destroy to uncreated type.");
            return;
        }

        Relation deadRelation = Database.getRelation(relName, false);
        CatalogEntry deadCatEntry = Database.getCatalogEntry(relName);
        Database.destroyRelation(deadRelation);
        Database.destroyCatEntry(deadCatEntry);
        System.out.println("Destroyed relation "+relName+".");
    }

    /* Deletes all tuples from a relation in the database */
    public void handleDelete(String command) {
        Pattern pattern = Pattern.compile(SYNTAX_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        /* Ensure relation exists in database */
        if (!Database.contains(relName.replace(";", ""), false)) {
            System.err.println("No existing relation of type " + relName + ". Cannot delete to uncreated type.");
            return;
        }

        command = command.toUpperCase();
        ArrayList<String> splitCom = new ArrayList<String>(Arrays.asList(command.split(" ")));

        if (splitCom.contains("WHERE")) {
            String condsOnly = getWhereConds(splitCom);
            Relation curRel = Database.getRelation(relName, false);
            LinkedList<LinkedList<String>> matchedRows = curRel.where(condsOnly);
            int deletedRows = curRel.deleteRows(matchedRows);
            System.out.println("Deleted " + deletedRows + " rows from relation " + relName + ".");
        } else {

            relName = relName.replace(";",""); //get rid of pesky endline char
            Relation rel = Database.getRelation(relName, false);
            rel.clearRelation();
            System.out.println("Deleted all rows from relation "+relName+".");
        }
    }

    /* Parse through the command to find which relation we're selecting from
     * then get the where conditions if applicable. Assigns the rows restricted
     * by the WHERE condition to the specified variable. */
    private void handleSelect(String command, String tempVar) {
        Pattern pattern = Pattern.compile(SYNTAX_REGEX);
        Matcher matcher = pattern.matcher(command);
        Relation tempRel, curRel;

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        command = command.toUpperCase();
        ArrayList<String> splitCom = new ArrayList<String>(Arrays.asList(command.split(" ")));
        String relName = splitCom.get(1);
        curRel = Database.getRelation(relName, true);

        if (curRel == null) {
            System.err.println("Cannot SELECT from a relation that does not exist: " + relName);
            return;
        }

        String whereConds = getWhereConds(splitCom);

        ArrayList<Attribute> attrs = curRel.getAttributes();
        LinkedList<LinkedList<String>> rows = curRel.where(whereConds);
        tempRel = new Relation(tempVar, attrs, rows);

        Database.createTemp(tempRel);

    }

    /* Given an arraylist representing each word of a command, return a String
     * representing only the conditions after "WHERE" */
    private String getWhereConds(ArrayList<String> splitCom) {
        String condsOnly = "";
        if (splitCom.size() <= 2) return condsOnly;

        for (int i = 3; i < splitCom.size(); i++) {
            String curItem = splitCom.get(i);

            if (curItem.contains("'")) {
                for (int j = i+1; j < splitCom.size(); j++) {
                    String temp = splitCom.get(j);
                    curItem += " " + temp;
                    if (temp.contains("'")) {
                        i = j;
                        break;
                    }
                }
            }
            condsOnly += curItem + " ";
        }
        condsOnly = condsOnly.trim();

        return condsOnly;
    }

    /* Parse through the command to find the relation from which we are projecting
     * and the specific attributes to keep. Assigns the returned attributes as rows
     * to a new relation specified by the commmand. */
    private void handleProject(String command, String tempVar) {
        Pattern pattern = Pattern.compile(SYNTAX_REGEX);
        Matcher matcher = pattern.matcher(command);
        Relation tempRel, curRel;

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        command = command.toUpperCase();
        ArrayList<String> splitCom = new ArrayList<String>(Arrays.asList(command.split(" ")));
        String relName = splitCom.get(splitCom.size()-1);
        curRel = Database.getRelation(relName, true);

        if (curRel == null) {
            System.err.println("Cannot SELECT from a relation that does not exist: " + relName);
            return;
        }

        ArrayList<String> projAttrStrings = getProjectAttrs(splitCom);
        ArrayList<Attribute> projAttrs = curRel.getAttributes(projAttrStrings);
        LinkedList<LinkedList<String>> projRows = curRel.getRows(projAttrs);
        tempRel = new Relation(tempVar, projAttrs, projRows);

        Database.createTemp(tempRel);
    }

    /* Given an arraylist representing each word of a command, return a String
     * representing only the attributes to project */
    private ArrayList<String> getProjectAttrs(ArrayList<String> command) {
        ArrayList<String> attributes = new ArrayList<>();

        for (int i = 1; i < command.size() - 2; i++) {
            String attribute = command.get(i);
            attribute = attribute.replace(",", "");
            attributes.add(attribute);
        }

        return attributes;
    }

    private void handleJoin(String command, String tempVar) {
        Pattern pattern = Pattern.compile(JOIN_REGEX);
        Matcher matcher = pattern.matcher(command);
        Relation tempRel, curRel1, curRel2;
        int[] attrsIndicies = new int[2];

        //ArrayList<Attribute> attrs = combineAttrs();

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        command = command.toUpperCase();
        ArrayList<String> splitCom = new ArrayList<String>(Arrays.asList(command.split(" ")));

        String relName1  = splitCom.get(1).replace(",", "");
        String relName2  = splitCom.get(2).replace(",", "");
        curRel1 = Database.getRelation(relName1, true);
        curRel2 = Database.getRelation(relName2, true);

        String attrString = "";
        for (int i = 4; i < splitCom.size(); i++) {
            attrString += splitCom.get(i)+" ";
        }
        attrString.trim();

        if (curRel1 == null || curRel2 == null) {
            System.err.println("Cannot JOIN relations if one or more does not exist.");
            return;
        }

        attrsIndicies = getAttrIndicies(curRel1, curRel2, attrString);

        if (attrsIndicies == null) {
            System.err.println("Cannot JOIN - Attributes do not exist in specified relations.");
            return;
        }
        ArrayList<Attribute> attrs1 = curRel1.getAttributes();
        ArrayList<Attribute> attrs2 = curRel2.getAttributes();

        LinkedList<LinkedList<String>> tempRows = curRel1.join(curRel2, attrsIndicies, "=");
        ArrayList<Attribute> tempAttrs = constructJoinAttrs(attrs1, attrs2, attrsIndicies[1]);

        tempRel = new Relation(tempVar, tempAttrs, tempRows);
        Database.createTemp(tempRel);
    }


    private int[] getAttrIndicies(Relation rel1, Relation rel2, String attrs) {
        String[] splitAttrs = attrs.split(" ");

        String attr1 = splitAttrs[0];
        String attr2 = splitAttrs[2].replace(";", "");
        int[] attrsIndicies = new int[2];

        if (attr1.contains(".")) {
            String[] splitAttr1 = attr1.split("\\.");
            attr1 = splitAttr1[1];
        }

        if (attr2.contains(".")) {
            String[] splitAttr2 = attr2.split("\\.");
            attr2 = splitAttr2[1];
        }

        attrsIndicies[0] = rel1.indexOfAttr(attr1);
        attrsIndicies[1] = rel2.indexOfAttr(attr2);

        if (attrsIndicies[0] == -1 || attrsIndicies[1] == -1) {
            return null;
        }
        return attrsIndicies;
    }

    private ArrayList<Attribute> constructJoinAttrs(ArrayList<Attribute> attrs1, ArrayList<Attribute> attrs2, int toRemove) {
        ArrayList<Attribute> combinedAttrs = new ArrayList<>();

        for (Attribute attr : attrs1) {
            combinedAttrs.add(attr);
        }
        for (Attribute attr : attrs2) {
            combinedAttrs.add(attr);
        }
        combinedAttrs.remove(attrs1.size() + toRemove);

        return combinedAttrs;
    }

    private void handleExport(String command) {
        Pattern pattern = Pattern.compile(IO_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        relName = relName.replace(";",""); //get rid of pesky endline char

        /* Ensure relation exists in database */
        if (!Database.contains(relName, true)) {
            System.err.println("No existing relation of type " + relName + ". Cannot export uncreated type.");
            return;
        }

        Relation rel = Database.getRelation(relName, true);

        XMLWriter writer = new XMLWriter(rel);
        System.out.println("Relation " + relName + " exported as " + relName.toLowerCase() + ".xml.");
    }

    private void handleImport(String command) {
        Pattern pattern = Pattern.compile(IO_REGEX);
        Matcher matcher = pattern.matcher(command);

        if (!matcher.matches()) {
            System.err.println("Malformatted command: "+command);
            return;
        }

        String relName = command.split(" ")[1];
        relName = relName.replace(";",""); //get rid of pesky endline char

        /* Ensure relation exists in database */
        if (Database.contains(relName, true)) {
            System.err.println("Relation with name " + relName + " already exists. Cannot import!");
            return;
        }

        String filename = relName.toLowerCase() + ".xml";
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("File for relation (" +filename+ ") could not be found. Cannot import!");
            return;
        }

        XMLParser reader = new XMLParser(filename);
        Relation importedRel = reader.getRelation();
        Database.create(importedRel);

        System.out.println("Relation " + relName + " imported from " + filename);
    }
}
