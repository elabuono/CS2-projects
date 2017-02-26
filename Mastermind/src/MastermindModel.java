/**
 * Author:  Aaron Deever
 * Author: Erica LaBuono
 * File:  MastermindModel.java
 */

import java.util.Observable;
import java.util.ArrayList;
import java.util.Random;

/**
 * The model for the Mastermind game.
 *
 * @author Aaron Deever
 */
public class MastermindModel extends Observable {

    /** The standard version allows 10 guesses.  However,
     * the game plays the same way for any number of allowed guesses.
     */
    public static final int MAX_GUESSES = 10;

    /**
     * The standard version of the game uses 6 unique symbols
     * (pegs of color: black, white, red, green, yellow, blue).
     * However, the game plays the same for any number
     * of unique symbols.
     */
    public static final int UNIQUE_SYMBOLS = 6;

    /**
     * The standard game has a code of length 4 that must be cracked.
     * However, the game plays the same for any length code that
     * needs to be cracked.
     */
    public static final int CODE_LENGTH = 4;

    /**
     * Number of guessing remaining to crack the code.
     */
    private int guessesRemaining;

    /**
     * clues is an ArrayList<Character>
     * containing CODE_LENGTH*MAX_GUESSES entries.
     * Each time a guess is made, CODE_LENGTH elements can be
     * filled. There are three
     * possible values for the elements of the clues ArrayList:
     * <br>
     *     ' ' indicates either the corresponding guess
     *     has not been made yet or the guess has been made
     *     and this represents an incorrect symbol guess.
     *     <br>
     *     'B' indicates one of the components of a guess is
     *     both the correct symbol and correct location.
     *     (Corresponds to 'B'lack clue peg in the board game.)
     *     <br>
     *     'W' indicates one of the components of a guess is
     *     the correct symbol but incorrect location.
     *     (Corresponds to 'W'hite clue peg in the board game.)
     * <br>
     *     The first CODE_LENGTH elements of the clues ArrayList correspond
     *     to Guess 1, the next CODE_LENGTH elements correspond to Guess 2,
     *     and so forth.
     */
    private ArrayList<Character> clues;

    /** solution is an ArrayList containing CODE_LENGTH entries.  The entries
     * represent the symbols (often displayed as colors)
     * of the CODE_LENGTH components of the solution,
     * in left to right order.  Each element has an integer value
     * in the range [1...UNIQUE_SYMBOLS] indicating the numerical value
     * (symbol index) of that component of the solution.
     */
    private ArrayList<Integer> solution;

    /** guesses is an ArrayList containing CODE_LENGTH*MAX_GUESSES entries.
     * Entries have a value in the range [0...UNIQUE_SYMBOLS].
     * A value of 0 indicates that there is no information for this
     * particular entry.  A value in the range [1...UNIQUE_SYMBOLS]
     * indicates a guess for a particular entry.
     * <br>
     *     The first CODE_LENGTH elements of the guesses ArrayList correspond
     *     to Guess 1, in left to right order.  The next CODE_LENGTH elements
     *     correspond to Guess 2, and so forth.
     */
    private ArrayList<Integer> guesses;

    /** indicates whether a peek request has been made to display the solution.
     */
    private boolean isPeeking;

    /**
     * indicates whether solution is visible.  The solution is visible in
     * three scenarios:  the game has been won, the game has been lost,
     * the player has peeked.
     */
    private boolean solutionVisible;

    /** status:  false if not yet a win. */
    private boolean hasWon;

    /**
     * Constructor for a MatermindModel object.
     */
    public MastermindModel() {
        guessesRemaining = MAX_GUESSES;
        clues = new ArrayList<>();
        solution = new ArrayList<>();
        guesses = new ArrayList<>();
        isPeeking = false;
        hasWon = false;
        solutionVisible = false;

        // populate the ArrayLists with appropriate initial values.

        // generate random symbol values for the solution
        // in the range [1...UNIQUE_SYMBOLS]
        Random rand = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            solution.add(rand.nextInt(UNIQUE_SYMBOLS) + 1);
        }

        // generate all ' ' and 0's to start off for clues and guesses
        for (int i = 0; i < CODE_LENGTH * MAX_GUESSES; i++) {
            clues.add(' ');
            guesses.add(0);
        }
    }

    /**
     * Make a guess.  A guess is only processed if all
     * components of the current guess are populated
     * with non-zero values (i.e. they can't be the
     * initial "empty" value).  A guess request is
     * not processed if the game's hasWon status already
     * indicates a "win", or if there are no
     * remaining guesses.
     *
     *<br>
     *     Processing a guess involves producing
     *     clues for that guess, as well as updating
     *     the number of remaining guesses and possibly
     *     updating the hasWon status.
     */
    public void makeGuess() {
        if(hasWon || guessesRemaining == 0) {
            return;
        }

        // make sure all components of current guess populated
        int currGuess = MAX_GUESSES - guessesRemaining; // 0-based
        int guessIdx = currGuess * CODE_LENGTH;
        for(int i = 0; i < CODE_LENGTH; i++) {
            if (guesses.get(guessIdx + i) == 0) {
                return;
            }
        }

        // valid data, so process the guess request
        int clueIdx = currGuess*CODE_LENGTH;

        // work with copies of solution and guess to avoid
        // corrupting actual data
        ArrayList<Integer> solCopy = new ArrayList<>(solution);
        ArrayList<Integer> guessCopy = new ArrayList<>();
        for(int i = 0; i < CODE_LENGTH; i++) {
            guessCopy.add(guesses.get(guessIdx + i));
        }

        // first look for and remove matches that are in the
        // correct location
        int i = 0;
        int matches = 0;
        while(i < solCopy.size()) {
            if (solCopy.get(i).equals(guessCopy.get(i))) {
                clues.set(clueIdx, 'B');
                clueIdx++;
                matches++;
                solCopy.remove(i);
                guessCopy.remove(i);
            } else {
                i++;
            }
        }

        // stop here if winning guess
        if(matches == CODE_LENGTH) {
            hasWon = true;
            guessesRemaining--;
            solutionVisible = true;  // make sure solution visible
            setChanged();
            notifyObservers();
            return;
        }

        // now do search for remaining matches, which
        // must be in the wrong location.  Iterate through
        // guessCopy, and remove matches
        // from solCopy as they are detected.
        // Removal is necessary to avoid double-counting
        // when there are repeat symbols in the guess
        // that match a single solution symbol.
        for(i = 0; i < guessCopy.size(); i++) {
            int guessVal = guessCopy.get(i);
            int matchIdx = -1;
            for (int j = 0; j < solCopy.size(); j++) {
                if (guessVal == solCopy.get(j)) {
                    matchIdx = j;
                    break;
                }
            }
            if (matchIdx > -1) { // found, now remove from solCopy
                solCopy.remove(matchIdx);
                clues.set(clueIdx, 'W');
                clueIdx++;
            }
        }

        // update remaining guesses, and notify observers
        guessesRemaining--;
        if(guessesRemaining == 0) {
            solutionVisible = true; // game over - lost
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Update a complete single guess at once.
     * If any data is out of range, this
     * function does not change the model state.
     * Also, if the game has already been won, or there
     * are no guesses remaining, this function does not
     * change the model state.
     *
     * @param fullGuess contains an entire guess
     */
    public void setFullGuessRow(ArrayList<Integer> fullGuess) {
        if (hasWon) {
            return;
        }

        if (guessesRemaining == 0) {
            return;
        }

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (fullGuess.get(i) <= 0 || fullGuess.get(i) > UNIQUE_SYMBOLS) {
                return;
            }
        }

        // data is valid
        int cell = CODE_LENGTH * (MAX_GUESSES - guessesRemaining);
        for (int i = 0; i < CODE_LENGTH; i++) {
            guesses.set(cell + i, fullGuess.get(i));
        }

        // call makeGuess.  It will notify observers
        makeGuess();
    }

    /**
     * Increments the symbol index of the
     * guess component selected (wrapping back around to index 0
     * after reaching the last unique symbol index), but only
     * if that guess component belongs to the current guess.  That is,
     * a request corresponding to a previous or future guess is ignored.
     * The request is also ignored if the game is already won.
     *
     * @param guessNum the guess number selected.  In range [1...MAX_GUESSES]
     * @param guessComp the guess component selected.
     *                  In range [1...CODE_LENGTH].
     */
    public void choose(int guessNum, int guessComp) {
        // do nothing if game already won
        if(hasWon) {
            return;
        }

        int currGuess = MAX_GUESSES - guessesRemaining + 1;
        // do nothing if selected guess not current one.
        if(currGuess != guessNum) {
            return;
        }

        // otherwise cycle forward one symbol for the selected
        // guess component
        int cell = (guessNum-1) * CODE_LENGTH + (guessComp-1);
        int val = guesses.get(cell);
        int newVal = val + 1;
        if (newVal > UNIQUE_SYMBOLS) {
            newVal = 0;
        }

        guesses.set(cell, newVal);
        setChanged();
        notifyObservers();
    }

    /**
     * Toggle isPeeking.  A peek request
     * is ignored if the game is over - either won
     * or lost.
     */
    public void peek() {
        if(hasWon || guessesRemaining == 0) {
            return;
        }

        isPeeking = !isPeeking;
        solutionVisible = !solutionVisible;
        setChanged();
        notifyObservers();
    }

    /**
     * Start a new game.
     */
    public void reset() {
        guessesRemaining = MAX_GUESSES;
        isPeeking = false;
        hasWon = false;
        solutionVisible = false;

        // populate the ArrayLists with appropriate initial values.

        // generate random symbol values for the solution
        Random rand = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            solution.set(i, rand.nextInt(UNIQUE_SYMBOLS) + 1);
        }

        // generate all ' ' to start off for clues and 0's for guesses
        for (int i = 0; i < CODE_LENGTH * MAX_GUESSES; i++) {
            clues.set(i,' ');
            guesses.set(i,0);
        }

        setChanged();
        notifyObservers();
    }

    /**
     * Returns current victory status.
     * @return true if game has been won, false if not yet won.
     */
    public boolean getVictoryStatus() {
        return hasWon;
    }

    /**
     * Returns number of remaining guesses.
     * @return number of remaining guesses.
     */
    public int getRemainingGuesses() {
        return guessesRemaining;
    }


    /**
     * Return an ArrayList containing all clue data.
     * @return a copy of the model's ArrayList of clue data.
     */
    public ArrayList<Character> getClueData() {
        return new ArrayList<>(clues);
    }

    /**
     * Returns visible solution data.  This is an ArrayList of all 0's
     * if the solution is not showing.  Otherwise, it is (a copy of) the
     * actual solution data.
     * @return ArrayList of visible solution data.
     */
    public ArrayList<Integer> getSolution() {
        if(solutionVisible) {
            return new ArrayList<>(solution);
        }
        else {
            // create and return a blank solution for display
            ArrayList<Integer> blankSol = new ArrayList<>();
            for (int i = 0; i < CODE_LENGTH; i++) {
                blankSol.add(0);
            }
            return blankSol;
        }
    }

    /**
     * Returns guess data.  This is a copy of the model's
     * ArrayList of all guess data.
     * @return ArrayList containing all guess data.
     */
    public ArrayList<Integer> getGuessData() {
        return new ArrayList<>(guesses);
    }

    /**
     * Is the guess row valid to guess?
     * @return true if valid to guess.
     */
    public boolean canGuess() {
        int currGuess = MAX_GUESSES - guessesRemaining; // 0-based
        int guessIdx = currGuess * CODE_LENGTH;
        for(int i = 0; i < CODE_LENGTH; i++) {
            if (guesses.get(guessIdx + i) == 0) {
                return false;
            }
        }
        return true;
    }
}
