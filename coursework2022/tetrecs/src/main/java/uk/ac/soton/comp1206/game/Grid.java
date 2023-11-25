package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.network.Multimedia;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 *
 * <p>Each value inside the Grid is an IntegerProperty can be bound to enable modification and
 * display of the contents of the grid.
 *
 * <p>The Grid contains functions related to modifying the model, for example, placing a piece
 * inside the grid.
 *
 * <p>The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  /** The number of columns in this grid */
  private final int cols;

  /** The number of rows in this grid */
  private final int rows;

  /** The grid is a 2D arrow with rows and columns of SimpleIntegerProperties. */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    // Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      // Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      // No such index
      return -1;
    }
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

  /**
   * check if the piece can be placed at the place
   *
   * @param gamePiece piece to place
   * @param x column
   * @param y row
   * @return true/false
   */
  public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
    int i = 0;
    for (int a = x - 1; a <= x + 1; a++) {
      int j = 0;
      for (int b = y - 1; b <= y + 1; b++) {
        if (gamePiece.getBlocks()[i][j] != 0 && get(a, b) != 0) {
          Multimedia.audioFile("fail.wav");
          return false;
        }
        j += 1;
      }
      i += 1;
    }
    Multimedia.audioFile("place.wav");
    return true;
  }

  /**
   * place the piece into the grid
   *
   * @param gamePiece piece to place
   * @param x column
   * @param y row
   */
  public void playPiece(GamePiece gamePiece, int x, int y) {
    int i = 0;
    for (int a = x - 1; a <= x + 1; a++) {
      int j = 0;
      for (int b = y - 1; b <= y + 1; b++) {
        if (gamePiece.getBlocks()[i][j] != 0) {
          set(a, b, gamePiece.getValue());
        }
        j += 1;
      }
      i += 1;
    }
  }
}
