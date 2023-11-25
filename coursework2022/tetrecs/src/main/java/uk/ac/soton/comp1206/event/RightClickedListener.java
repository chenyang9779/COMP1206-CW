package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBoard;

/** The RightClicked Listener is called when the right mouse button is triggered */
public interface RightClickedListener {

  /**
   * Handle when mouse is right clicked
   *
   * @param board passes the GameBoard that the mouse clicked on
   */
  public void setOnRightClicked(GameBoard board);
}
