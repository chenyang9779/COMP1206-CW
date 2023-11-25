package uk.ac.soton.comp1206.event;

/** The Game Loop Listener is used to pass on the time for the current game loop */
public interface GameLoopListener {

  /**
   * Handle when game loop is finished
   *
   * @param time how long time is the next game loop
   */
  public void setOnGameLoop(int time);
}
