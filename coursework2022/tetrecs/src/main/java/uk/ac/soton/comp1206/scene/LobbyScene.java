package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.LobbyList;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/** The lobby scene. Display channel, players and chat */
public class LobbyScene extends BaseScene {

  private static Logger logger = LogManager.getLogger(LobbyScene.class);

  /** observable lists for channels, players and message */
  private ArrayList<Text> observableChannels = new ArrayList<>();

  private SimpleListProperty<Text> currentChannels =
      new SimpleListProperty<>(FXCollections.observableArrayList(observableChannels));
  private ArrayList<String> observablePlayers = new ArrayList<>();
  private SimpleListProperty<String> currentPlayers =
      new SimpleListProperty<>(FXCollections.observableArrayList(observablePlayers));
  private ArrayList<String> observableMessages = new ArrayList<>();
  private SimpleListProperty<String> messages =
      new SimpleListProperty<>(FXCollections.observableArrayList(observableMessages));

  /** bind the communicator */
  private Communicator communicator = gameWindow.getCommunicator();

  /** Set up timer */
  private Timer listTimer = new Timer();

  private Timer userTimer = new Timer();

  /** host and in channel boolean property */
  private SimpleBooleanProperty host = new SimpleBooleanProperty(false);

  private SimpleBooleanProperty inChannel = new SimpleBooleanProperty(false);

  /** pane field property */
  private BorderPane mainPane;

  private BorderPane rightPane;
  private BorderPane chatPane;

  /** hBox for start and leave buttons */
  private HBox startLeaveBox = new HBox(20);

  /** lobby list field for channel, player and messages */
  private LobbyList channelList;

  private LobbyList playerList;
  private LobbyList messageList;
  private VBox lobbyLeftContainer;
  private Text newChannel;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);

    // Initialise lists and buttons and set their styles
    channelList = new LobbyList("Current Channels");
    playerList = new LobbyList("Players");
    messageList = new LobbyList("Messages");
    newChannel = new Text("New Channel");
    newChannel.setTextAlignment(TextAlignment.CENTER);
    newChannel.getStyleClass().add("lobby-item");
    messageList.setAlignment(Pos.TOP_LEFT);
    messageList.setMaxWidth(600);
    playerList.setAlignment(Pos.TOP_LEFT);
    playerList.setTranslateX(50);
    startLeaveBox.setAlignment(Pos.TOP_LEFT);
  }

  /** Initialise the scene */
  @Override
  public void initialise() {

    // update when clicked on newChannel
    newChannel.setOnMouseClicked(
        (clicked) -> {
          askChannel();
        });

    // binding ESC to exist to menu or leave channel
    scene.setOnKeyPressed(
        (keyEvent) -> {
          switch (keyEvent.getCode()) {
            case ESCAPE:
              if (inChannel.get()) {
                messages.clear();
                communicator.send("PART");
                if (host.getValue()) {
                  startLeaveBox.getChildren().remove(1);
                }
                host.set(false);
                lobbyLeftContainer.getChildren().clear();
                lobbyLeftContainer.getChildren().addAll(newChannel, channelList);
              } else {
                Multimedia.stop();
                communicator.send("PART");
                userTimer.cancel();
                listTimer.cancel();
                gameWindow.startMenu();
                break;
              }
          }
        });
  }

  /** Build the lobby window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    // call all listeners for incoming server updates
    listeners();

    // start timer and schedule fix rate sending LIST to server
    listTimer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            communicator.send("LIST");
          }
        },
        0,
        1000);

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    // set up a pane on the right
    rightPane = new BorderPane();
    rightPane.setMaxSize(640, 1080);
    mainPane.setCenter(rightPane);

    // set up a pane for chat
    chatPane = new BorderPane();
    chatPane.setMaxSize(640, 1080);
    mainPane.setRight(chatPane);

    var playerName = new Text("Players");
    playerName.setTextAlignment(TextAlignment.CENTER);
    playerName.getStyleClass().add("lobby-item");

    // container for channels
    lobbyLeftContainer = new VBox(30, newChannel, channelList);
    lobbyLeftContainer.setAlignment(Pos.TOP_CENTER);
    mainPane.setLeft(lobbyLeftContainer);
    lobbyLeftContainer.setAlignment(Pos.TOP_LEFT);

    // bind all properties
    channelList.channelListProperty().bind(currentChannels);
    playerList.playerListProperty().bind(currentPlayers);
    messageList.messageListProperty().bind(messages);

    // create start and leave button
    var start = new Text("Start");
    var leave = new Text("Leave");
    start.getStyleClass().add("menuItem");
    leave.getStyleClass().add("menuItem");

    // when start or leave are clicked do following procedures
    start.setOnMouseClicked(
        (e) -> {
          communicator.send("START");
        });
    leave.setOnMouseClicked(
        (e) -> {
          messages.clear();
          Platform.runLater(
              () -> {
                communicator.send("PART");
              });
          if (host.getValue()) {
            startLeaveBox.getChildren().remove(1);
          }
          host.set(false);
          lobbyLeftContainer.getChildren().clear();
          lobbyLeftContainer.getChildren().addAll(newChannel, channelList);
        });

    // add leave button to the box
    startLeaveBox.getChildren().add(leave);

    // If you are the host add the start button to the box
    host.addListener(
        (observableValue, aBoolean, t1) -> {
          if (t1) {
            Platform.runLater(
                () -> {
                  startLeaveBox.getChildren().add(start);
                });
          }
        });
  }

  /** set up all listeners for incoming commands from server */
  public void listeners() {
    communicator.addListener(
        communication -> {
          if (communication.startsWith("CHANNELS")) {
            logger.info("");
            communication = communication.replace("CHANNELS ", "");
            List<String> split = Arrays.asList(communication.split("\n"));
            List<Text> textList = new ArrayList<>();
            for (String s : split) {
              var text = new Text(s);
              text.setOnMouseClicked(
                  e -> {
                    communicator.send("JOIN " + s);
                  });
              textList.add(text);
            }
            if (split.size() == 0) {
              currentChannels.clear();
            } else if (currentChannels != textList) {
              currentChannels.clear();
              for (Text t : textList) {
                currentChannels.add(t);
              }
              Platform.runLater(() -> channelList.displayChannel());
            }

          } else if (communication.startsWith("JOIN")) {
            inChannel.set(true);
            Platform.runLater(
                () -> {
                  rightPane.setLeft(playerList);
                  rightPane.setTop(startLeaveBox);
                  setupChat();
                  lobbyLeftContainer.getChildren().clear();
                  lobbyLeftContainer.getChildren().addAll(channelList);
                });
            userTimer.scheduleAtFixedRate(
                new TimerTask() {
                  @Override
                  public void run() {
                    communicator.send("USERS");
                  }
                },
                0,
                1000);
          } else if (communication.startsWith("USERS")) {
            communication = communication.replace("USERS ", "");
            List<String> split = Arrays.asList(communication.split("\n"));
            if (split.size() == 0) {
              currentPlayers.clear();
            } else if (currentPlayers != split) {
              currentPlayers.clear();
              for (String s : split) {
                currentPlayers.add(s);
              }
              System.out.println(currentPlayers.size());
              Platform.runLater(() -> playerList.displayPlayer());
            }
          } else if (communication.startsWith("HOST")) {
            host.set(true);
          } else if (communication.startsWith("ERROR")) {

          } else if (communication.startsWith("MSG")) {
            communication = communication.replace("MSG ", "");
            logger.info("Message " + communication + " received...");
            messages.add(communication);
            Platform.runLater(() -> messageList.displayMessage());
          } else if (communication.startsWith("NICK")) {
            if (communication.contains(":")) {
              List<String> split = Arrays.asList(communication.split(":"));

            } else {

            }

          } else if (communication.startsWith("PARTED")) {
            userTimer.purge();
            userTimer.cancel();
            Platform.runLater(
                () -> {
                  rightPane.getChildren().clear();
                  chatPane.getChildren().clear();
                });
            inChannel.set(false);
          } else if (communication.startsWith("START")) {
            userTimer.cancel();
            listTimer.cancel();
            Multimedia.stop();
            Platform.runLater(
                () -> {
                  gameWindow.startMultiplayer();
                });
          }
        });
  }

  /** set up the chat pane and button to send message*/
  public void setupChat() {
    chatPane.setTop(messageList);
    var inputMessage = new HBox();
    var text = new TextField("");
    var button = new Button("Send");
    button.setOnAction(
        (e) -> {
          communicator.send("MSG " + text.getText());
          text.clear();
          text.requestFocus();
        });
    text.setOnKeyPressed(
        (e) -> {
          if (e.getCode() != KeyCode.ENTER) return;
          communicator.send("MSG " + text.getText());
          text.clear();
          text.requestFocus();
        });
    button.getStyleClass().add("green");
    inputMessage.getChildren().addAll(text, button);
    chatPane.setBottom(inputMessage);
  }

  /** set up for ask channel name and button to create own channel*/
  public void askChannel() {
    var askChannel = new VBox();
    var title = new Text("Channel Name");
    title.getStyleClass().add("");
    var name = new TextField("");
    var submit = new Button("Submit");
    submit.getStyleClass().add("green");
    askChannel.getChildren().addAll(name, submit);
    askChannel.setAlignment(Pos.CENTER);
    lobbyLeftContainer.getChildren().clear();
    lobbyLeftContainer.getChildren().addAll(askChannel, channelList);

    askChannel.setOnKeyPressed(
        e -> {
          if (e.getCode() == KeyCode.ENTER) {
            communicator.send("CREATE " + name.getText());
          }
        });

    submit.setOnAction(
        actionEvent -> {
          communicator.send("CREATE " + name.getText());
        });
  }
}
