/*
* Recursive Fibonacci Program No. 2
* Author: Gaea Turman
*/
import java.io.InvalidClassException;
import java.util.Scanner;

// Fibonacci1 returns the nth number in a Fibonacci sequence (input by user)
class Fibonacci2 {

  // initializes int n accessable by all interior functions
  // initializes Scanner input

  static int n;
  static Scanner input;

  //Uses handleArguments to validate command line arguments
  //Prints out message and nth fibonacci number after has gone through...
  //recursive function fib

  public static void main(String[] args) throws InvalidClassException {
    if (handleArguments(args)) {
      Scanner input = new Scanner(System.in);
    }
    System.out.print("Fib ("+n+") is "+fib(n));
    System.out.println();
  }

  static final String enterthis = "Please enter a non-negitive integer between 0 and 46.";

  /*
  * Validate the command line arguments and do setup based on the
  * arguments. One command line argument is expected:
  *   1. A positive integer between 0-46
  *
  * Return true if processing was sucessful and false otherwise.
  */
  static boolean handleArguments(String[] args) {
    // Check for correct number of arguments
    if (args.length != 1) {
      System.out.println("Wrong number of command line arguments.");
      System.out.println(enterthis);
      return false;
    }
    try {
      n = Integer.parseInt(args[0]);
    } catch (NumberFormatException ex) {
      System.out.println("Number must be an integer.");
      System.out.println(enterthis);
      return false;
    }
    return true;
  }

  // Scanner user_input = new Scanner(System.in); //creates new scanner which takes input from user
  // System.out.println("Please enter a non-negitive integer between 0 and 46: ");
  // prints message prompting user for desired parameters
  // if (n > 46 || n < 0) {
  // System.out.println("Invalid integer input, please enter a non-negitive integer between 0 and 46.")
  //

  public static int fib(int numb){
    if (numb == 0){
      return 0;
    }
    else{
      IntPair numberset = fibaux(numb);
      return numberset.getfirst();
    }
  }
  //function fib that takes in an integer numb
  //if - base case, if number == 0 returns 0 and does not call fibaux again
  //else - recursive case, initializes new IntPair numberset
  //which calls fibaux on numb
  //returns IntPair numberset first - is set in fibaux

  public static IntPair fibaux(int numb){
    if (numb == 1 || numb == 2){
      return new IntPair(1, 1);
    }
    else{
      IntPair X = fibaux(numb-1);
      int f1 = X.getfirst();
      int f2 = X.getsecond();
      return new IntPair(f1+f2, f1);
    }
  }
  //function fibaux that takes in an integer numb
  //if - base case, if number == 1 or 2 (fibonacci nth for 2 is also 1)
  //returns new IntPair(1,1);
  //else - recursive case, initializes new IntPair X
  //which calls fibaux on (numb-1)
  //initializes f1/f2 = as first and second of IntPair respectively
  //returns new IntPair with(f1+f2, f1) - set to first, second
}
