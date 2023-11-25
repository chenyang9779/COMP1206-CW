package uk.ac.soton.comp1206.event;

import java.util.Set;

/** The Line Cleared Listener is called when a line is cleared and pass them to fade them out */
public interface LineClearedListener {

  /**
   * Handle when one or more lines are cleared
   *
   * @param gameBlockCoordinates all the coordinates that the blocks are cleared
   */
  public void lineCleared(Set<int[]> gameBlockCoordinates);
}
