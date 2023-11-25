package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * The Visual User Interface component displaying the pieces
 *
 * <p>Extends GameBoard to display the blocks
 */
public class PieceBoard extends GameBoard {

  /**
   * Constructor for new piece board component.
   *
   * @param gamePiece : game piece to be displayed.
   * @param height : height of the piece board
   * @param width : width of the piece board
   */
  public PieceBoard(GamePiece gamePiece, int width, int height) {
    super(new Grid(3, 3), width, height);
    display(gamePiece);
  }

  /**
   * Takes in a GamePiece and display it every time its called
   *
   * @param gamePiece : gamePiece that need to be displayed
   */
  public void display(GamePiece gamePiece) {
    // display the next piece in the pieceBoard
    int[][] blocks = gamePiece.getBlocks();
    for (int i = 0; i < blocks.length; i++) {
      for (int j = 0; j < blocks[i].length; j++) {
        if (blocks[i][j] != 0) {
          grid.set(i, j, gamePiece.getValue());
        } else {
          grid.set(i, j, 0);
        }
      }
    }
  }
}
