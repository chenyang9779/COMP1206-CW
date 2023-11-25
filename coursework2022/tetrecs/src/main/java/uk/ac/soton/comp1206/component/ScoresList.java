package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;

/**
 * container for scores boxes to display all scores
 *
 * <p> Extends VBox
 *
 */
public class ScoresList extends VBox {

  private static final Logger logger = LogManager.getLogger(ScoresList.class);

  /** List property containing scores. */
  protected final SimpleListProperty<Pair<String, Integer>> scoresList = new SimpleListProperty<>();

  /** Container of display boxes. */
  protected ArrayList<HBox> items = new ArrayList<>();

  /** Container of score items. */
  protected VBox itemContainer = new VBox();



  /**
   * Constructor for new custom ScoresList component.
   *
   * @param text : provides text for the title.
   */
  public ScoresList(String text) {
    Text title = new Text(text);
    title.getStyleClass().add("hiscore-title");
    VBox.setMargin(title, new Insets(0, 0, 10, 0));

    setMaxWidth(400);
    setMaxHeight(400);

    itemContainer.setMaxHeight(350);

    getChildren().addAll(title, itemContainer);
  }


  /** Displays scores in the list. */
  public void display() {
    logger.info("Displaying scores...");
    int colourCounter = 0;

    logger.info("Setting up score items...");
    for (Pair<String, Integer> l : scoresList) {

      //separate the name and score
      var name = new Text(l.getKey());
      var score = new Text(l.getValue().toString());

      //
      name.getStyleClass().add("scorelist");
      score.getStyleClass().add("scorelist");

      // Setting colours
      if (colourCounter == 10) {
        colourCounter = 0;
      }
      name.setFill(colours[colourCounter]);
      score.setFill(colours[colourCounter]);
      colourCounter++;

      //add name and score in to one hBox
      var displayBox = new HBox();
      displayBox.setAlignment(Pos.CENTER);
      displayBox.getChildren().addAll(name, score);
      displayBox.setOpacity(0);
      HBox.setMargin(score, new Insets(0,0,0,8));
      items.add(displayBox);
      itemContainer.getChildren().add(displayBox);
    }

    //reveal the scores in animation
    for(HBox h: items){
      FadeTransition fade = new FadeTransition();
      fade.setFromValue(0);
      fade.setToValue(100);
      fade.setDuration(Duration.millis(1000));
      fade.setNode(h);
      fade.play();
    }

  }

  /**
   * Returns list property for score data.
   *
   * @return scoresList.
   */
  public SimpleListProperty<Pair<String, Integer>> scoreListProperty() {
    return scoresList;
  }


  /** Contains colours of score items. */
  protected static final Color[] colours = {
    Color.RED,
    Color.ORANGE,
    Color.YELLOW,
    Color.LIME,
    Color.GREEN,
    Color.DARKTURQUOISE,
    Color.AQUA,
    Color.BLUE,
    Color.MEDIUMPURPLE,
    Color.PURPLE
  };
}