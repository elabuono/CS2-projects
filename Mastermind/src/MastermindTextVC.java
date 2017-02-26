/**
 * Author:  Aaron Deever
 * File:  MastermindTextVC.java
 */

import java.util.Observable;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Observer;

/**
 * Class definition for textual interface for Mastermind.
 * This class can be used to play a text-based version
 * of the game.
 *
 * @author Aaron Deever
 */
public class MastermindTextVC implements Observer {

    /**
     * The underlying model.
     */
    private MastermindModel model;

    /**
     * Construct a MastermindTextVC object
     */
    public MastermindTextVC() {
        this.model = new MastermindModel();
        this.model.addObserver(this);
    }

    /**
     * Play the Mastermind game by initiating command loop.
     */
    private void playGame() {
        commandLoop();
    }

    /**
     * Read a command and execute loop.
     */
    private void commandLoop() {
        Scanner in = new Scanner(System.in);
        displayHelp();
        while(true) {
            String line = in.nextLine();
            String[] words = line.split("\\s+");
            if (words.length > 0) {
                if (words[0].equals("quit")) {
                    break;
                } else if (words[0].equals("reset")) {
                    this.model.reset();
                } else if (words[0].equals("peek")) {
                    this.model.peek();
                } else if (words[0].equals("guess")) {
                    ArrayList<Integer> fullGuess = new ArrayList<>();
                    if(words.length != MastermindModel.CODE_LENGTH + 1) {
                        displayHelp();
                    }
                    else {
                        for (int i = 1; i <= MastermindModel.CODE_LENGTH; i++) {
                            int val;
                            try {
                                val = Integer.parseInt(words[i]);
                            } catch (NumberFormatException e) {
                                displayHelp();
                                break;
                            }
                            if (val >= 1 && val <= MastermindModel.UNIQUE_SYMBOLS) {
                                fullGuess.add(val);
                            } else {
                                displayHelp();
                                break;
                            }
                        }
                        if(fullGuess.size() == MastermindModel.CODE_LENGTH) {
                            this.model.setFullGuessRow(fullGuess);
                            this.model.makeGuess();
                        }
                    }
                } else {
                    displayHelp();
                }
            }
            else {
                displayHelp();
            }
        }
        in.close();
    }

    /**
     * Update method causes the text user interface
     * to be re-displayed.  The help information
     * is not included in the display.
     *
     * @param o the observable object (not used)
     * @param arg an argument passed to the notifyObservers method (not used)
     */
    public void update(Observable o, Object arg) {
        displayGame();
    }

    /**
     * Textual display of Mastermind game state.
     */
    private void displayGame() {
        displayBoard();
        displayMessage();
        displayPrompt();
    }

    /**
     * Display the solution row, the guess rows, and the clues.
     * Clues are output as a sequence of characters to the left of the guess:
     * each B indicates a correct symbol and location,
     * each W indicates a correct symbol but incorrect location.
     * Each guess appears as the numerical symbol values chosen.
     * Guesses not yet made are represented with "X" characters.
     * The solution is represented by "X" characters, unless it
     * is visible, in which case it is the numerical symbol values.
     *
     * This display is flexible for up to 9 different symbols.
     */
    private void displayBoard() {
        ArrayList<Integer> sol = this.model.getSolution();
        ArrayList<Character> clues = this.model.getClueData();
        ArrayList<Integer> guesses = this.model.getGuessData();


        System.out.println();
        for(int i = 0; i < MastermindModel.CODE_LENGTH + 1; i++) {
            System.out.print(" ");
        }
        for(int i = 0; i < MastermindModel.CODE_LENGTH; i++) {
            if (sol.get(i) == 0) {
                System.out.print("X ");
            } else {
                System.out.print(sol.get(i) + " ");
            }
        }
        System.out.println();

        for(int i = 0; i < MastermindModel.CODE_LENGTH + 1; i++) {
            System.out.print(" ");
        }
        for(int i = 0; i < 2*MastermindModel.CODE_LENGTH - 1; i++) {
            System.out.print("-");
        }
        System.out.println();
        for(int i = 0; i < MastermindModel.MAX_GUESSES; i++) {
            // need to reverse top-to-bottom order of clues/guesses
            int loc = MastermindModel.CODE_LENGTH *
                    (MastermindModel.MAX_GUESSES - i - 1);
            for (int j = 0; j < MastermindModel.CODE_LENGTH; j++) {
                System.out.print(clues.get(loc + j));
            }
            System.out.print(" ");
            for (int j = 0; j < MastermindModel.CODE_LENGTH; j++) {
                if (guesses.get(loc + j) == 0) {
                    System.out.print("X ");
                } else {
                    System.out.print(guesses.get(loc + j) + " ");
                }
            }
            System.out.println("  Guess: " + (MastermindModel.MAX_GUESSES - i));
        }
    }

    /** Displays one of three messages: winning message, losing message,
     * or number of guessing remaining message.
     */
    private void displayMessage() {
        System.out.println();
        if (this.model.getVictoryStatus()) {
            System.out.println("You won the game!!");
        } else if (this.model.getRemainingGuesses() == 0) {
            System.out.println("You lost the game!!");
        } else {
            System.out.print("You have " + this.model.getRemainingGuesses());
            System.out.println(" guesses remaining.");
        }
    }

    /** Display prompt for next command
     */
    private void displayPrompt() {
        System.out.print("Enter command: ");
    }

    /**
     * Print out help for the game.
     * Follow this up with re-displaying the current game.
     */
    private void displayHelp() {
        System.out.println("\nChoose numbers from 1-" +
                MastermindModel.UNIQUE_SYMBOLS + " for each guess component.");
        System.out.println("Each 'B' in your clue indicates a match.");
        System.out.print("Each 'W' in your clue indicates a correct symbol");
        System.out.println(" but incorrect location.\n");
        System.out.println("Available commands:");
        System.out.println("guess a b c d ... -- make next guess");
        System.out.println("quit              -- quit the game");
        System.out.println("reset             -- start a new game");
        System.out.println("peek              -- toggle solution visibility");

        displayBoard();
    }

    /**
     * The main method used to play a game.
     *
     * @param args Command line arguments -- unused
     */
    public static void main(String[] args) {
        MastermindTextVC game = new MastermindTextVC();
        game.playGame();
    }
}
