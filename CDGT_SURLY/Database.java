/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;

/* class for DATABASE */
public class Database {
    private static Database dbObject = null;

    private static LinkedList<Relation> database;
    private static LinkedList<CatalogEntry> catalog;

    private Database() {
    }

    public static LinkedList<Relation> getDatabase() {
        return database;
    }

    public LinkedList<CatalogEntry> getCatalog() {
        return catalog;
    }

    public static Relation getRelation(String relName) {
        Relation curRel;

        for (int i = 0; i < database.size(); i++) {
            curRel = database.get(i);
            if (curRel.getName().equalsIgnoreCase(relName)) {
                return curRel;
            }
        }
        return null;
    }

    public static CatalogEntry getCatalogEntry(String entryName) {
        CatalogEntry curEntry;
        for (int i = 0; i < catalog.size(); i++) {
            curEntry = catalog.get(i);
            if (curEntry.getName().equalsIgnoreCase(entryName)) {
                return curEntry;
            }
        }
        return null;
    }

    public static void create(Relation relation) {
        database.add(relation);
        CatalogEntry catEntry = new CatalogEntry(relation.getName(), relation.getAttributes());
        catalog.add(catEntry);
    }

    public static void insert(Relation rel, LinkedList<String> row) {
        rel.insert(row);
    }

    public static Boolean contains(String relName) {
        Relation curRel;
        for (int i = 0; i < database.size(); i++) {
            curRel = database.get(i);
            if (curRel.getName().equalsIgnoreCase(relName)) {
                return true;
            }
        }
        return false;
    }

    public static Database getInstance() {
        if (dbObject == null) {
            dbObject = new Database();
            database = new LinkedList<Relation>();
            catalog = new LinkedList<CatalogEntry>();
        }
        return dbObject;
    }

    public String toString() {
        String outStr = "";
        Relation curRel;
        LinkedList<String> curRow;
        int i = 0;
        int j = 0;
        while (i < database.size() && (curRel = database.get(i)) != null) {
            outStr += "RELATION " + curRel.getName() + "\n";
            while ((curRow = curRel.getRow(j)) != null) {
                outStr += curRow.toString() + "\n";
                j++;
            }
            outStr += "\n";
            j = 0;
            i++;
        }

        return outStr;
    }

    public static void destroyRelation(Relation relation) {
        database.remove(relation);
    }

    public static void destroyCatEntry(CatalogEntry catEntry) {
        catalog.remove(catEntry);
    }

}
