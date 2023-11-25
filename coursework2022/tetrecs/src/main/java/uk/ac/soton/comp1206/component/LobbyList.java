package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component displaying the lobby components
 *
 * <p>Extends VBox to display components
 */
public class LobbyList extends VBox {

  private static final Logger logger = LogManager.getLogger(LobbyList.class);

  /** List property containing players, channels, messages. */
  protected final SimpleListProperty<String> playerList = new SimpleListProperty<>();

  protected final SimpleListProperty<Text> channelList = new SimpleListProperty<>();
  protected final SimpleListProperty<String> messageList = new SimpleListProperty<>();

  /** Arraylist for vBoxes containing items */
  protected ArrayList<VBox> items = new ArrayList<>();

  /** Container of items. */
  protected VBox itemContainer = new VBox();

  /**
   * Constructor for new lobby list component.
   *
   * @param text : provides text for the title.
   */
  public LobbyList(String text) {
    Text title = new Text(text);
    title.getStyleClass().add("hiscore-title");
    title.setTextAlignment(TextAlignment.CENTER);
    VBox.setMargin(title, new Insets(0, 0, 10, 0));

    setMaxWidth(960);
    setMaxHeight(1080);

    itemContainer.setMaxHeight(1500);

    getChildren().addAll(title, itemContainer);
  }

  /** display channels in channel list */
  public void displayChannel() {
    logger.info("Displaying channels...");

    // clears the container
    itemContainer.getChildren().clear();

    for (Text t : channelList) {
      t.getStyleClass().add("lobby-item");
      var displayBox = new VBox();
      displayBox.setAlignment(Pos.TOP_LEFT);
      displayBox.getChildren().addAll(t);
      VBox.setMargin(t, new Insets(0, 0, 0, 8));
      items.add(displayBox);
      itemContainer.getChildren().add(displayBox);
    }
  }

  /** display players in player list */
  public void displayPlayer() {
    logger.info("Displaying players...");
    // clears the container
    itemContainer.getChildren().clear();

    for (String s : playerList) {
      var text = new Text(s);
      text.getStyleClass().add("scorelist");
      var displayBox = new VBox();
      displayBox.setAlignment(Pos.TOP_LEFT);
      displayBox.getChildren().addAll(text);
      VBox.setMargin(text, new Insets(0, 0, 0, 8));
      items.add(displayBox);
      itemContainer.getChildren().add(displayBox);
    }
  }

  /** display new message */
  public void displayMessage() {
    logger.info("Displaying message...");
    // clears the container
    itemContainer.getChildren().clear();

    for (String s : messageList) {
      var text = new Text(s);
      text.getStyleClass().add("chat");
      var displayBox = new VBox();
      displayBox.setAlignment(Pos.TOP_LEFT);
      displayBox.getChildren().add(text);
      VBox.setMargin(text, new Insets(0, 0, 0, 8));
      items.add(displayBox);
      itemContainer.getChildren().add(displayBox);
    }
  }

  /**
   * player list property
   *
   * @return playerList.
   */
  public SimpleListProperty<String> playerListProperty() {
    return playerList;
  }

  /**
   * channel list property
   *
   * @return channelList.
   */
  public SimpleListProperty<Text> channelListProperty() {
    return channelList;
  }

  /**
   * message list property
   *
   * @return messageList.
   */
  public SimpleListProperty<String> messageListProperty() {
    return messageList;
  }
}
