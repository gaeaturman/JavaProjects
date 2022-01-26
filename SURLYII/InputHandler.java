/*
 * CSCI 330
 * Gaea Turman
 * Chris Daw
 */
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

/* Checks input from command line and cleans file; removes comments, puts directives (RELATION, INPUT, PRINT) on one line. */
public class InputHandler {

    String[] args;
    public InputHandler(String[] args) {
        this.args = args;
    }

    public void parseArgs(String[] args) {
        if (args.length > 1) {
            System.err.println("Too many arguments!");
            System.err.println("Usage: java Main filename");
            return;
        } else if (args.length == 0) {
            System.err.println("Too few arguments!");
            System.err.println("Usage: java Main filename");
        }

        String filename = args[0];
        handleFile(filename);
        return;
    }

    /* Reads from given filename and uses CommandExecutor to run the sql */
    public void handleFile(String filename) {
        File file = new File(filename);
        ArrayList<String> formattedCommands;

        try { //attempt to handleFile, if not found throw exception
            Scanner scanner = new Scanner(file);
            formattedCommands = format(scanner);

            CommandExecutor commandExecutor = new CommandExecutor(formattedCommands);
            commandExecutor.parse();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }



    /* Format the input file. Makes an arraylist of command lines consisting
     * only of valid syntax skips over commented out portions.
     */
    public ArrayList<String> format(Scanner scanner) {
        List<String> line = new ArrayList<String>();
        ArrayList<String> formattedCommands = new ArrayList<String>();
        String curElement, temp, curCommand;
        boolean foundEOL;

        while(scanner.hasNextLine()) {
            line = getNextLine(scanner);
            foundEOL = false;
            curCommand = "";

            for (int i = 0; i < line.size(); i++) {
                curElement = line.get(i);
                /* Handle multiline or inline comments */
                if (curElement.contains("/*")) {
                    int j = i;
                    while (j < line.size() || line.size() == 0) {
                        temp = line.get(j);
                        if (temp.startsWith("*/") && temp.length() > 2) { //if starts w/ end comment but has more to it
                            temp = temp.replace("*/", ""); //take out the comment symbol to get the part we want
                            line.set(j, temp); //replace temp w/ noncomment segment
                            i = j-1; //goes back over, since doesn't contain "*/" doesn't skip segment noncomment part of good segment
                            break;
                        }
                        if (temp.contains("*/")) {
                            i = j; // Set i to skip over everything in the comment
                            break;
                        }

                        // Reached the end of the line without finding closing comment
                        if (j == line.size() - 1) {
                            if (scanner.hasNextLine()) {
                                line = getNextLine(scanner);
                                while (line.size() == 0) {
                                    line = getNextLine(scanner);
                                }
                                j = 0;
                                continue;
                            } else {
                                break;
                            }
                        }
                        j++;
                    }
                } else {
                    /* Append to the current line */
                    if (curCommand.equals("")) {
                        curCommand += curElement;
                    } else {
                        curCommand += " " + curElement;
                    }
                }

                if (curElement.contains(";")) {
                    foundEOL = true;
                }

                /* If we haven't found the end of line character before we get
                * to the end of the line, we continue in the loop with the next line. */
                if (i == (line.size() - 1) && foundEOL == false) {
                    if (scanner.hasNextLine()) {
                        i = -1; // -1 so that next loop it gets incremented to 0
                        line = getNextLine(scanner);
                    } else {
                        break;
                    }
                }
            }

            if (!curCommand.equals("") && foundEOL) {
                formattedCommands.add(curCommand);
            }
        }

        return formattedCommands;
    }

    /* getNextLine takes the Scanner object and returns the space delimited
    * List representation of the line with spaces removed */
    public List<String> getNextLine(Scanner scanner) {
        String line = scanner.nextLine();

        /* The following eases the ignoring of comments */
        line = line.replace("/*", " /*");
        line = line.replace("*/", "*/ ");
        line = line.replace(",", "");

        String[] lineStrs = line.split(" ");
        List<String> delimitedLine = new ArrayList<String>(Arrays.asList(lineStrs));

        for (int i = 0; i < delimitedLine.size(); i++) {
            if (delimitedLine.get(i).equals("")) {
                delimitedLine.remove(i);
                i--;
            }
        }

        return delimitedLine;
    }
}
