/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;

/* class for Attribute */
public class Attribute {
    String name;
    String type;
    int size;

    public Attribute(String name, String type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        return name + " " + type + " " + size;
    }
}
