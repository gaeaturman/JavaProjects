/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */

 // Program to parse various commands and respond accordingly, checks input syntax and throws errors as appropriate.

/* Main.java, just starts up the program using LexicalAnalyzer. */
public class Main {

    public static void main(String args[]) {
        InputHandler ih = new InputHandler(args);
        ih.parseArgs(args);
        return;
    }

}
