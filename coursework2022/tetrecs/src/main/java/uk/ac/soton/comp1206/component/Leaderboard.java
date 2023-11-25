package uk.ac.soton.comp1206.component;

/**
 * The Visual User Interface component displaying the leaderboard
 *
 * <p>Extends ScoresList to display the high scores
 */
public class Leaderboard extends ScoresList {

  /**
   * Constructor for new leader board component.
   *
   * @param text : provides text for the title.
   */
  public Leaderboard(String text) {
    super(text);
  }
}
