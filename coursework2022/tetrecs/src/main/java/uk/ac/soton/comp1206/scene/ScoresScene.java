package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoreFileHandler;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.io.*;
import java.util.ArrayList;

/** Scene to display scores at the end of the game. */
public class ScoresScene extends BaseScene {

  private static Logger logger = LogManager.getLogger(ScoresScene.class);

  /** Displays local names and scores on screen. */
  private ScoresList scoreListLocal;

  /** Displays online names and scores on screen. */
  private ScoresList scoreListOnline;

  /** Game object containing score data. */
  private Game game;

  /** Gets scores from file. */
  private ScoreFileHandler scoreFileHandler = new ScoreFileHandler();

  private Boolean higherScore = false;

  /** Local scores list property. */
  private ArrayList<Pair<String, Integer>> observableRemoteScores = new ArrayList<>();

  private SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>();
  /** Remote scores list property. */
  private SimpleListProperty<Pair<String, Integer>> remoteScores =
      new SimpleListProperty<>(FXCollections.observableArrayList(observableRemoteScores));

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    scoreListLocal = new ScoresList("Local Scores");
    scoreListOnline = new ScoresList("Online Scores");
  }

  /** Initialise this scene. Called after creation */
  @Override
  public void initialise() {
    logger.info("Initialising scene...");

    scene.setOnKeyPressed(
        (keyEvent) -> {
          switch (keyEvent.getCode()) {
            case ESCAPE:
              Multimedia.stop();
              gameWindow.startMenu();
              break;
          }
        });
  }

  /** Build the layout of the scene */
  @Override
  public void build() {
    logger.info("Building scene...");
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("menu-background");
    root.getChildren().add(scorePane);

    var pane = new BorderPane();
    scorePane.getChildren().add(pane);

    var title = new Text("High Scores List");
    title.getStyleClass().add("hiscore-title");
    var titleBox = new HBox(title);
    titleBox.setTranslateY(150);
    titleBox.setAlignment(Pos.CENTER);
    pane.setTop(titleBox);

    var scoreListContainer = new HBox(50, scoreListLocal, scoreListOnline);
    scoreListContainer.setAlignment(Pos.CENTER);
    try {
      localScores = scoreFileHandler.loadScores();
      scoreListLocal.scoreListProperty().bind(localScores);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Checks if score is a new high score.
    logger.info("Checking scores..." + game.getScore());
    for (Pair<String, Integer> l : localScores) {
      if (game.getScore() >= l.getValue()) {
        higherScore = true;
      }
    }

    // if score is higher than scores in file ask for name and update it
    if (higherScore) {
      logger.info("True");
      HBox container = new HBox();

      TextField nameEntry = new TextField();
      nameEntry.setPromptText("Enter nickname...");
      nameEntry.setMaxHeight(50);

      Button submit = new Button("Submit");
      submit.getStyleClass().add("lobby-button");
      submit.setMaxHeight(50);

      submit.setOnAction(
          actionEvent -> {
            String name = nameEntry.getText();
            int score = game.getScore();
            // Insert into local scores.
            for (int i = 0; i < localScores.size(); i++) {
              if (score >= localScores.get(i).getValue()) {
                localScores.add(i, new Pair<>(name, score));
                break;
              }
            }
            localScores.remove(localScores.size() - 1);
            try {
              scoreFileHandler.writeScores(localScores.get());
            } catch (IOException e) {
              e.printStackTrace();
            }

            // Send to server.
            writeOnlineScore(name, score);
            loadOnlineScores();

            pane.setCenter(scoreListContainer);
            scoreListLocal.display();
          });

      nameEntry.setOnKeyPressed(
          entered -> {
            if (entered.getCode() == KeyCode.ENTER) {
              String name = nameEntry.getText();
              int score = game.getScore();
              // Insert into local scores.
              for (int i = 0; i < localScores.size(); i++) {
                if (score >= localScores.get(i).getValue()) {
                  localScores.add(i, new Pair<>(name, score));
                  break;
                }
              }
              localScores.remove(localScores.size() - 1);
              try {
                scoreFileHandler.writeScores(localScores.get());
              } catch (IOException e) {
                e.printStackTrace();
              }

              // Send to server.
              writeOnlineScore(name, score);
              loadOnlineScores();

              pane.setCenter(scoreListContainer);
              scoreListLocal.display();
            }
          });

      container.setAlignment(Pos.CENTER);
      container.getChildren().addAll(nameEntry, submit);
      pane.setCenter(container);
    } else {
      loadOnlineScores();
      pane.setCenter(scoreListContainer);
      scoreListLocal.display();
    }

    // Displays online scores.
    scoreListOnline.scoreListProperty().bind(remoteScores);
  }

  /** Loads scores from server. */
  private void loadOnlineScores() {
    Communicator communicator = gameWindow.getCommunicator();
    logger.info("Loading online scores...");

    // Sends request to server.
    communicator.send("HISCORES UNIQUE");

    // Adds listener to server.
    communicator.addListener(
        communication -> {
          if (!(communication.startsWith("HISCORES"))) return;

          communication = communication.replace("HISCORES", "");

          String[] scores = communication.split("\n");

          remoteScores.clear();
          // Adds scores to list.
          for (int i = 0; i < 10; i++) {
            String[] split = scores[i].split(":");
            remoteScores.add(new Pair<String, Integer>(split[0], Integer.parseInt(split[1])));
          }
          logger.info("DISPLAY ONLINE SCORES...");

          System.out.println(remoteScores.size());

          Platform.runLater(() -> scoreListOnline.display());
        });
  }

  /**
   * Sends new high score to server
   *
   * @param name name
   * @param score score
   */
  private void writeOnlineScore(String name, int score) {
    Communicator comm = gameWindow.getCommunicator();

    comm.send("HISCORE " + name + ":" + score);
  }
}
