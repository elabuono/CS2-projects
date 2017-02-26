/**
 * GUI Version of Mastermind game
 * @author Erica LaBuono
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class MastermindGraphicalVC extends Application implements Observer {

    private MastermindModel model;

    private int guessNumber = 0;

    private List<List<Node>> hints;
    private List<List<Node>> game;

    private Button guessButton;
    private Button resetButton;
    private Button peekButton;

    private Text statusText;

    @Override
    public void start(Stage stage) throws Exception {
        this.model = new MastermindModel();
        this.model.addObserver(this);

        stage.setTitle("Mastermind");

        BorderPane mainPane = new BorderPane();
        // the height and width below were empirically developed...
        mainPane.setPrefHeight( 600 );
        mainPane.setPrefWidth( 500 );

        // right side holds the display and control buttons.
        BorderPane controls = new BorderPane();
        controls.setCenter( this.makeControlPane() );
        controls.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );

        BorderPane game = new BorderPane();
        game.setCenter( this.makeGamePane() );
        game.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );

        BorderPane hints = new BorderPane();
        hints.setCenter( this.makeHintsPane() );
        hints.setMaxSize( Double.MAX_VALUE, Double.MAX_VALUE );

        BorderPane status = new BorderPane();
        statusText = new Text("You have 10 guesses remaining.");
        status.setCenter(statusText);

        mainPane.setRight( controls );
        mainPane.setCenter( game );
        mainPane.setLeft( hints );
        mainPane.setTop( status );

        Scene scene = new Scene( mainPane );
        stage.setScene( scene );
        stage.show();
    }


    /**
     * Makes the grid pane of buttons with events, holds the guesses
     * @return gridpane: center pane of the game
     */
    private Node makeGamePane() {
        game = new ArrayList<>();
        GridPane gamePane = new GridPane();

        for ( int r = 0; r < model.MAX_GUESSES + 1; r++) {

            GridPane guessPane = new GridPane();
            List<Node> nodeList = new ArrayList<>();
            guessPane.setHgap(2.5);

            for ( int c = 0; c < model.CODE_LENGTH; c++ ) {

                Button btn = new Button( "" );
                btn.setMinWidth(50);
                btn.setMinHeight(50);
                btn.setMaxHeight(50);
                btn.setMaxWidth(50);;

                final int finalR = r;
                final int finalC = c;

                btn.addEventHandler(ActionEvent.ANY, event -> pressed(finalR, finalC));

                guessPane.add( btn, c, 0 );
                nodeList.add(btn);
            }

            game.add(nodeList);
            gamePane.add(guessPane, 0, r);
        }

        gamePane.setVgap(2.5);
        gamePane.setPadding(new Insets(10, 20, 10, 20));
        return gamePane;
    }

    /**
     * Generates the hint grid pane, populates with Circles
     * @return gridpane of hints
     */
    private Node makeHintsPane() {
        hints = new ArrayList<>();
        GridPane hintsGrid = new GridPane();

        for ( int r = 0; r < model.MAX_GUESSES; r++) {
            GridPane hintGrid = new GridPane();
            List<Node> nodeList = new ArrayList<>();

            for(int innerR = 0; innerR < 2; innerR++) {
                for(int innerC = 0; innerC < 2; innerC++) {
                    Circle shape = new Circle();
                    shape.setRadius(12.5);
                    shape.setFill(Color.GRAY);
                    hintGrid.add(shape, innerC, innerR);
                    nodeList.add(shape);
                }
            }

            hints.add(nodeList);
            hintsGrid.add(hintGrid, 0, r);
        }

        hintsGrid.setHgap(2.5);
        hintsGrid.setVgap(2.5);
        hintsGrid.setPadding(new Insets(10, 0, 10, 20));
        return hintsGrid;
    }

    /**
     * Generates the box containing the buttons for guess, reset, and peeking
     * @return a Vbox of buttons
     */
    private Node makeControlPane() {
        guessButton = new Button("Guess");
        guessButton.setDisable(true);

        guessButton.addEventHandler(ActionEvent.ANY, event -> model.makeGuess());

        resetButton = new Button("Reset");
        resetButton.addEventHandler(ActionEvent.ANY, event -> model.reset());

        peekButton = new Button("(Un)Peek");
        peekButton.addEventHandler(ActionEvent.ANY, event -> model.peek());

        VBox box = new VBox();
        box.getChildren().addAll(guessButton, resetButton, peekButton);
        return box;
    }

    /**
     * Calls choose on modified guess, item
     * @param guess int representing guess num
     * @param item int representing item num
     */
    private void pressed(int guess, int item) {
        model.choose(guess + 1, item + 1);
    }

    @Override
    public void update(Observable o, Object arg) {

        this.model = (MastermindModel) o;

        List<Integer> guessData = model.getGuessData();
        List<Character> clueData = model.getClueData();

        // update game and hints
        for(int r = 0; r < model.MAX_GUESSES; r++ ) {
            for(int c = 0; c < MastermindModel.CODE_LENGTH; c++) {
                Node n = game.get(r).get(c);
                Node hint = hints.get(r).get(c);
                n.setStyle(styleForSymbol(guessData.get(oneD2D(r, c))));
                ((Circle) hint).setFill(styleForHintSymbol(clueData.get(oneD2D(r, c))));
            }
        }

        drawSolution();

        if(model.getVictoryStatus()) {
            statusText.setText("You cracked the code!");
            guessButton.setDisable(true);
        } else {
            if(model.getRemainingGuesses() == 0) {
                statusText.setText("You ran out of guesses!");
                guessButton.setDisable(true);
            } else {
                statusText.setText("You have " + model.getRemainingGuesses() + " guesses remaining.");
                if(model.canGuess()) {
                    guessButton.setDisable(false);
                } else {
                    guessButton.setDisable(true);
                }
            }

        }
    }

    /**
     * Produces the solution for the GUI
     */
    private void drawSolution() {
        List<Integer> solution = model.getSolution();
        List<Node> nodes = game.get(MastermindModel.MAX_GUESSES);
        for(int i = 0; i < MastermindModel.CODE_LENGTH; i++) {
            nodes.get(i).setStyle(styleForSymbol(solution.get(i)));
        }
    }

    /**
     * Generates colors of game pieces from symbols.
     * @param symbol an integer representing a to-be color
     * @return the color
     */
    public String styleForSymbol(int symbol) {
        // black, white, red, green, yellow, blue
        switch(symbol) {
            case 0: return "-fx-background-color: -fx-inner-border, -fx-outer-border, -fx-body-color;";
            case 1: return "-fx-background-color: -fx-inner-border, -fx-outer-border, black;";
            case 2: return "-fx-background-color: -fx-inner-border, -fx-outer-border, white;";
            case 3: return "-fx-background-color: -fx-inner-border, -fx-outer-border, red;";
            case 4: return "-fx-background-color: -fx-inner-border, -fx-outer-border, green;";
            case 5: return "-fx-background-color: -fx-inner-border, -fx-outer-border, yellow;";
            case 6: return "-fx-background-color: -fx-inner-border, -fx-outer-border, blue;";
            default: return "";
        }
    }

    /**
     * Generates colors of hint pieces from symbols
     * @param symbol a char representing a hint
     * @return Color for the hint circles
     */
    public Color styleForHintSymbol(char symbol) {
        // black, white, red, green, yellow, blue
        switch(symbol) {
            case ' ': return Color.GRAY;
            case 'B': return Color.BLACK;
            case 'W': return Color.WHITE;
            default: return Color.GRAY;
        }
    }

    /**
     * Conversion for 2D to 1D location
     * @param row int representing row num
     * @param col int representing col num
     * @return int converted from 2D to 1D
     */
    public int oneD2D(int row, int col) {
        return MastermindModel.CODE_LENGTH*row + col;
    }

    /**
     * Launches the GUI.
     * @param args
     */
    public static void main(String[] args) {
        MastermindGraphicalVC.launch(args);
    }
}
