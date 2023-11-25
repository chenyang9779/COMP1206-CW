package uk.ac.soton.comp1206.scene;

import java.util.Calendar;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/** multiplayer scene */
public class MultiplayerScene extends ChallengeScene {


  private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
  protected Game game;
  private PieceBoard pieceBoard;
  private PieceBoard nextPieceBoard;
  private GameBoard board;
  private int currentX = 0;
  private int currentY = 0;

  private int x = 0;
  private int y = 0;

  /**
   * Create a new multiplayer Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /** Build the Challenge window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    // Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    var timerBar = new Rectangle(0, 0, gameWindow.getWidth(), 30);
    mainPane.setBottom(timerBar);

    var rightPane = new BorderPane();
    mainPane.setRight(rightPane);

    // add pieceBoard to the scene
    pieceBoard =
        new PieceBoard(game.currentPiece, gameWindow.getWidth() / 5, gameWindow.getWidth() / 5);
    nextPieceBoard =
        new PieceBoard(game.followingPiece, gameWindow.getWidth() / 10, gameWindow.getWidth() / 10);
    nextPieceBoard.setTranslateX(gameWindow.getWidth() / 10);

    // Add UI elements
    var currentScore = new Text();
    var currentLevel = new Text();
    var livesLeft = new Text();
    var highScore = new Text();

    // binding to the game properties
    currentScore.textProperty().bind(game.scoreProperty().asString());
    currentLevel.textProperty().bind(game.levelProperty().asString());
    livesLeft.textProperty().bind(game.livesProperty().asString());
    highScore.textProperty().bind(game.highScoreProperty().asString());

    // names for score, lives and levels
    var currentPieceTitle = new Text("Current Piece");
    var followingPieceTitle = new Text("Following Piece");
    var scoreName = new Text("Score: ");
    var levelName = new Text("Level: ");
    var livesName = new Text("Lives: ");
    var highScoreName = new Text("High Score: ");

    // set style for all texts
    currentPieceTitle.getStyleClass().add("pieceTitle");
    followingPieceTitle.getStyleClass().add("pieceTitle");
    currentScore.getStyleClass().add("score");
    currentLevel.getStyleClass().add("level");
    livesLeft.getStyleClass().add("lives");
    scoreName.getStyleClass().add("score");
    levelName.getStyleClass().add("level");
    livesName.getStyleClass().add("lives");
    highScore.getStyleClass().add("hiscore");
    highScoreName.getStyleClass().add("hiscore");

    // put information in HBox
    var scoreBox = new HBox(scoreName, currentScore);
    var levelBox = new HBox(levelName, currentLevel);
    var livesBox = new HBox(livesName, livesLeft);
    var highScoreBox = new HBox(highScoreName, highScore);

    // get all box in one VBox
    var pieceBoardBox =
        new VBox(
            (1080 - gameWindow.getWidth() / 2) / 5,
            currentPieceTitle,
            pieceBoard,
            followingPieceTitle,
            nextPieceBoard,
            highScoreBox,
            scoreBox,
            levelBox,
            livesBox);
    pieceBoardBox.setTranslateX(-(1080 - gameWindow.getWidth() / 2));
    pieceBoardBox.setTranslateY((1080 - gameWindow.getWidth() / 2) / 2);
    rightPane.setCenter(pieceBoardBox);

    // right-click on the mainBoard
    board.setOnRightClicked(
        (clicked) -> {
          game.rotateCurrentPiece();
          Multimedia.audioFile("rotate.wav");
        });

    // left-click on the current piece board
    pieceBoard.setOnBlockClick(
        (clicked) -> {
          game.rotateCurrentPiece();
          Multimedia.audioFile("rotate.wav");
        });

    // left-click on the next piece board to swap the following two pieces
    nextPieceBoard.setOnBlockClick(
        (clicked) -> {
          game.swapCurrentPiece();
          Multimedia.audioFile("transition.wav");
        });

    // call the listener to update currentPiece and followingPiece
    game.setNextPieceListener(
        (currentPiece, followingPiece) -> {
          pieceBoard.display(currentPiece);
          nextPieceBoard.display(followingPiece);
        });

    //call the listener when lines are cleared
    game.setLineClearedListener(
        (cleared) -> {
          board.clear(cleared);
        });

    //call the listener when game loop is finished
    game.setGameLoopListener(
        (e) -> {
          double currentTime = Calendar.getInstance().getTimeInMillis();

          // the initial ratio is 1
          double ratio = 1;

          // setter for the dimensions
          timerBar.setWidth(ratio * gameWindow.getWidth());

          // create a new timer
          AnimationTimer animationTimer =
              new AnimationTimer() {
                int timerDelay = game.getTimerDelay();
                double timeLeft = timerDelay;
                int red;
                int green;

                //for different time left on the timer, the timer boar will show different color
                @Override
                public void handle(long l) {
                  if (timeLeft > 0) {
                    timeLeft =
                        timerDelay - (Calendar.getInstance().getTimeInMillis() - currentTime);
                    double ratio = timeLeft / timerDelay;
                    timerBar.setWidth(ratio * gameWindow.getWidth());
                    if (ratio > 0.9) {
                      green = 128;
                    } else if (ratio <= 0.9 && ratio > 0.5) {
                      green = (int) (-317.5 * ratio + 413.75);
                      red = (int) (-637.5 * ratio + 573.75);
                    } else if (ratio > 0.1 && ratio < 0.5) {
                      green = (int) (637.5 * ratio - 63.75);
                    } else if (ratio < 0.1) {
                      green = 0;
                    }
                    timerBar.setFill(Color.rgb(red, green, 0));
                  } else {
                    stop();
                  }
                }
              };
          animationTimer.start();
        });

    //call lives listener when live is 0
    game.setLivesListener(
        () -> {
          game.timer.cancel();
          Multimedia.stop();
          Platform.runLater(() -> gameWindow.startScoresScene(game));
        });
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /** Setup the game object and model */
  public void setupGame() {
    logger.info("Starting a new challenge");

    // Start new game
    game = new Game(5, 5);
  }

  /** Initialise the scene and start the game */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    keyBind();
    game.start();
  }

  private int getX() {
    return x;
  }

  private int getY() {
    return y;
  }

  /**
   * bind all key controls
   */
  private void keyBind() {
    scene.setOnKeyPressed(
        (keyEvent) -> {
          board.getBlock(currentX, currentY).paint();
          switch (keyEvent.getCode()) {
            case ESCAPE:
              game.timer.cancel();
              Multimedia.stop();
              gameWindow.startScoresScene(game);
              break;
            case ENTER:
              game.blockClicked(board.getBlock(currentX, currentY));
              break;
            case X:
              game.blockClicked(board.getBlock(currentX, currentY));
              break;
            case W:
              if (currentY > 0) {
                currentY--;
              }
              break;
            case UP:
              if (currentY > 0) {
                currentY--;
              }
              break;
            case S:
              if (currentY < game.getCols() - 1) {
                currentY++;
              }
              break;
            case DOWN:
              if (currentY < game.getCols() - 1) {
                currentY++;
              }
              break;
            case A:
              if (currentX > 0) {
                currentX--;
              }
              break;
            case LEFT:
              if (currentX > 0) {
                currentX--;
              }
              break;
            case D:
              if (currentX < game.getRows() - 1) {
                currentX++;
              }
              break;
            case RIGHT:
              if (currentX < game.getRows() - 1) {
                currentX++;
              }
              break;
            case SPACE:
              game.swapCurrentPiece();
              break;
            case R:
              game.swapCurrentPiece();
              break;
            case Q:
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              break;
            case Z:
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              break;
            case OPEN_BRACKET:
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              game.rotateCurrentPiece();
              break;
            case E:
              game.rotateCurrentPiece();
              break;
            case C:
              game.rotateCurrentPiece();
              break;
            case CLOSE_BRACKET:
              game.rotateCurrentPiece();
              break;
          }
          board.getBlock(currentX, currentY).paintHover();
        });
  }
}
