package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/** The Next Piece Listener is called when the piece on piece boards are updated */
public interface NextPieceListener {

  /**
   * Handle when pieces are updated
   *
   * @param currentPiece new current piece
   * @param followingPiece new following piece
   */
  public void nextPiece(GamePiece currentPiece,GamePiece followingPiece);

}
