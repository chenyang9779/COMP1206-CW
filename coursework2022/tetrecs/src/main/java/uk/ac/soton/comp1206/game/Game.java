package uk.ac.soton.comp1206.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.LivesListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.network.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /** Number of rows */
  protected final int rows;

  /** Number of columns */
  protected final int cols;

  /** The grid model linked to the game */
  protected final Grid grid;

  /** Integer property for score */
  private IntegerProperty score = new SimpleIntegerProperty(0);

  /** Integer property for level */
  private IntegerProperty level = new SimpleIntegerProperty(0);

  /** Integer property for lives */
  private IntegerProperty lives = new SimpleIntegerProperty(3);

  /** Integer property for multiplier */
  private IntegerProperty multiplier = new SimpleIntegerProperty(1);

  /** Integer property for high score */
  private IntegerProperty highScore = new SimpleIntegerProperty(0);

  /** stores current piece */
  public GamePiece currentPiece;

  /** stores following piece and generate it by spawnPiece method */
  public GamePiece followingPiece = spawnPiece();

  /** Set up all listeners */
  private NextPieceListener nextPieceListener;

  private LineClearedListener lineClearedListener;
  private GameLoopListener gameLoopListener;
  private LivesListener livesListener;

  /** Set up timers */
  private int timerDelay = 12000;

  public Timer timer;
  private TimerTask timerTask;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);

    currentPiece = spawnPiece();
  }

  /** Start the game */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
    startTimer();
    try {
      setHighScore(Integer.parseInt(readHighScore()));
    } catch (Exception e) {
    }
  }

  /** Initialise a new game and set up anything that needs to be done at the start */
  public void initialiseGame() {
    logger.info("Initialising game");
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    // Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    // Get the new value for this block
    if (getGrid().canPlayPiece(currentPiece, x, y) == true) {
      getGrid().playPiece(currentPiece, x, y);
      afterPiece();
      nextPiece();
    }

    // Update the grid with the new value
    //      grid.set(x, y, newValue);
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /** Spawn a random piece from the database of pieces */
  public GamePiece spawnPiece() {
    return GamePiece.createPiece(new Random().nextInt(14));
  }

  /**
   * change the current piece to following piece and renew the following piece and call the listener
   * as they have been changed and start the timer
   */
  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    nextPieceListener.nextPiece(currentPiece, followingPiece);
    startTimer();
  }

  /**
   * After a piece is being placed, test to see if any roll or row are full, if yes add the
   * coordinates to the list
   */
  public void afterPiece() {
    Set<int[]> clearedBlocksSet = new HashSet<>();
    // get full rows in an array
    var rowArray = new ArrayList<Integer>();
    for (int i = 0; i < grid.getRows(); i++) {
      int count = 0;
      for (int j = 0; j < grid.getCols(); j++) {
        if (grid.get(i, j) != 0) {
          count++;
        }
      }
      if (count == grid.getCols()) {
        rowArray.add(i);
      }
    }

    // get full cols in an array
    var colArray = new ArrayList<Integer>();
    for (int j = 0; j < grid.getCols(); j++) {
      int count = 0;
      for (int i = 0; i < grid.getRows(); i++) {
        if (grid.get(i, j) != 0) {
          count++;
        }
      }
      if (count == grid.getRows()) {
        colArray.add(j);
      }
      timer.cancel();
    }

    int blocks = 0;
    for (Integer a : rowArray) {
      for (int j = 0; j < grid.getCols(); j++) {
        if (grid.get(a, j) != 0) {
          Multimedia.audioFile("clear.wav");
          grid.set(a, j, 0);
          int[] blockClearedCoordinates = new int[2];
          blockClearedCoordinates[0] = a;
          blockClearedCoordinates[1] = j;
          clearedBlocksSet.add(blockClearedCoordinates);
          blocks += 1;
        } else {
          blocks -= 1;
        }
      }
    }

    for (Integer a : colArray) {
      for (int i = 0; i < grid.getRows(); i++) {
        if (grid.get(i, a) != 0) {
          Multimedia.audioFile("clear.wav");
          grid.set(i, a, 0);
          int[] blockClearedCoordinates = new int[2];
          blockClearedCoordinates[0] = i;
          blockClearedCoordinates[1] = a;
          clearedBlocksSet.add(blockClearedCoordinates);
          blocks += 1;
        } else {
          blocks -= 1;
        }
      }
    }

    setScore(getScore() + score(rowArray.size() + colArray.size(), blocks));
    if (getScore() > getHighScore()) {
      setHighScore(getScore());
    }

    if (rowArray.size() != 0 || colArray.size() != 0) {
      setMultiplier(getMultiplier() + 1);
    } else {
      setMultiplier(1);
    }

    setLevel(getScore() / 1000);
    lineClearedListener.lineCleared(clearedBlocksSet);
  }

  /**
   * if any lines are cleared then add the scores according to the equation
   *
   * @param lines : lines cleared
   * @param blocks : number of blocks that are cleared
   */
  public int score(int lines, int blocks) {
    if (lines == 0) {
      return 0;
    } else {
      return lines * blocks * 10 * getMultiplier();
    }
  }

  /** rotate the current piece and call the listener */
  public void rotateCurrentPiece() {
    currentPiece.rotate();
    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }

  /** change the current piece with the following piece and call the listener */
  public void swapCurrentPiece() {
    GamePiece temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }

  /** set up listeners */
  public void setNextPieceListener(NextPieceListener nextPieceListener) {
    this.nextPieceListener = nextPieceListener;
  }

  public void setLineClearedListener(LineClearedListener lineClearedListener) {
    this.lineClearedListener = lineClearedListener;
  }

  public void setGameLoopListener(GameLoopListener gameLoopListener) {
    this.gameLoopListener = gameLoopListener;
  }

  public void setLivesListener(LivesListener livesListener) {
    this.livesListener = livesListener;
  }

  // get properties for score, level, lives and multiplier
  public IntegerProperty scoreProperty() {
    return score;
  }

  public IntegerProperty levelProperty() {
    return level;
  }

  public IntegerProperty livesProperty() {
    return lives;
  }

  public IntegerProperty highScoreProperty() {
    return highScore;
  }

  // getter and setter for score, level, lives and multiplier
  public int getScore() {
    return score.get();
  }

  public int getLevel() {
    return level.get();
  }

  public int getLives() {
    return lives.get();
  }

  public int getMultiplier() {
    return multiplier.get();
  }

  public int getTimerDelay() {
    return timerDelay;
  }

  public int getHighScore() {
    return highScore.get();
  }

  public void setScore(int score) {
    this.score.set(score);
  }

  public void setLevel(int level) {
    this.level.set(level);
  }

  public void setLives(int lives) {
    this.lives.set(lives);
  }

  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  public void setHighScore(int highScore) {
    this.highScore.set(highScore);
  }

  /** setter for time delay minimum time delay is 2500 */
  public void setTimerDelay() {
    timerDelay = 12000 - 500 * getLevel();
    if (timerDelay <= 2500) {
      timerDelay = 2500;
    }
    gameLoopListener.setOnGameLoop(timerDelay);
  }

  /**
   * game loop with the timer delay, when loop finishes, lives -1 and current piece update with new
   * ones
   */
  public void gameLoop() {
    if (getLives() <= 0) {
      timer.cancel();
      livesListener.noMoreLives();
      Multimedia.audioFile("fail.wav");
    } else {
      setMultiplier(1);
      setLives(getLives() - 1);
      Multimedia.audioFile("lifelose.wav");
      nextPiece();
    }
  }

  /** start the timer */
  public void startTimer() {
    timerTask =
        new TimerTask() {
          @Override
          public void run() {
            gameLoop();
          }
        };
    timer = new Timer("Timer");
    setTimerDelay();
    timer.schedule(timerTask, getTimerDelay());
  }

  /** read high score from the file */
  public String readHighScore() throws Exception {
    File scoreFile = new File("scoreFile.txt");
    ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    String[] split = null;
    if (scoreFile.exists() == true) {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(scoreFile));
      split = bufferedReader.readLine().split(":");
    }
    return split[1];
  }
}
