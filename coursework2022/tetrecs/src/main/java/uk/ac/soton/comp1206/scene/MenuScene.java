package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/** The main menu of the game. Provides a gateway to the rest of the game. */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /** check boxes for music and audio to mute them*/
  private CheckBox muteMusic;
  private CheckBox muteAudio;

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /** Build the menu layout */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    // new title with all animation
    var title = getImage("/images/TetrECS.png");
    title.setFitWidth(500);
    title.setFitHeight(215);
    title.isPreserveRatio();
    title.setTranslateY(240);
    var scaleTransition = new ScaleTransition(Duration.millis(5000), title);
    scaleTransition.setToX(2);
    scaleTransition.play();
    var fadeTransition = new FadeTransition(Duration.millis(5000), title);
    fadeTransition.setFromValue(0);
    fadeTransition.setToValue(1);
    fadeTransition.play();
    var rotateTransition = new RotateTransition(Duration.millis(5000), title);
    rotateTransition.setFromAngle(-15);
    rotateTransition.setToAngle(15);
    rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
    rotateTransition.setAutoReverse(true);
    rotateTransition.play();

    // add title to the pane
    var titleBox = new HBox(title);
    titleBox.setAlignment(Pos.TOP_CENTER);
    mainPane.setCenter(titleBox);

    // buttons for different modes
    var singlePlayer = new Text("Singleplayer Mode");
    var multiplayer = new Text("Multiplayer Mode");
    var howToPlay = new Text("How To Play");
    var exit = new Text("Exit");
    singlePlayer.getStyleClass().add("menuItem");
    multiplayer.getStyleClass().add("menuItem");
    howToPlay.getStyleClass().add("menuItem");
    exit.getStyleClass().add("menuItem");

    var menu = new VBox(20, singlePlayer, multiplayer, howToPlay, exit);
    menu.setAlignment(Pos.TOP_CENTER);
    menu.setTranslateY(-20);
    mainPane.setBottom(menu);

    // Bind the button action to the startGame method in the menu
    singlePlayer.setOnMouseClicked(
        (clicked) -> {
          if (!muteMusic.isSelected()) {
            startSinglePlayer(clicked);
          }
          if (muteMusic.isSelected()) {
            startSinglePlayer(clicked);
            Multimedia.backgroundMusic("game_start.wav");
          }
        });
    multiplayer.setOnMouseClicked(this::openLobby);
    howToPlay.setOnMouseClicked(this::openInstruction);
    exit.setOnMouseClicked(this::exit);

    // bind the checkbox actions to mute the music and audio
    muteMusic = new CheckBox("Enable music");
    muteMusic.setSelected(true);
    muteMusic.getStyleClass().add("check-box");
    muteAudio = new CheckBox("Enable audio");
    muteAudio.getStyleClass().add("check-box");
    muteAudio.selectedProperty().bindBidirectional(Multimedia.audioEnabledProperty());
    var muteBox = new VBox(10, muteMusic, muteAudio);
    muteBox.setAlignment(Pos.CENTER_RIGHT);
    mainPane.setTop(muteBox);

    muteMusic.setOnMouseClicked(
        (clicked) -> {
          if (!muteMusic.isSelected()) {
            Multimedia.stop();
          }
          if (muteMusic.isSelected()) {
            Multimedia.backgroundMusic("dong.mp3");
          }
        });
  }

  /** Initialise the menu */
  @Override
  public void initialise() {}

  /**
   * Handle when the Start Game button is pressed
   *
   * @param mouseEvent event
   */
  private void startSinglePlayer(MouseEvent mouseEvent) {
    Multimedia.start();
    Multimedia.stop();
    gameWindow.startChallenge();
  }

  /**
   * Handle when the instruction button is pressed
   *
   * @param mouseEvent event
   */
  private void openInstruction(MouseEvent mouseEvent) {
    gameWindow.displayInstruction();
  }

  /**
   * Handle when the multiplayer button is pressed
   *
   * @param mouseEvent event
   */
  public void openLobby(MouseEvent mouseEvent) {
    gameWindow.startLobby();
  }

  /**
   * Handle when the exist button is pressed
   *
   * @param mouseEvent event
   */
  private void exit(MouseEvent mouseEvent) {
    System.exit(0);
  }

  /**
   * get the image to view into external form
   *
   * @param s : image location
   * @return imageView
   */
  public ImageView getImage(String s) {
    return new ImageView(this.getClass().getResource(s).toExternalForm());
  }
}
