package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/** The instruction scene. Display the controls */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new instruction challenge scene
   *
   * @param gameWindow the Game Window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instruction Scene");
  }

  /** build instruction scene*/
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var instructionPane = new StackPane();
    instructionPane.setMaxWidth(gameWindow.getWidth());
    instructionPane.setMaxHeight(gameWindow.getHeight());
    instructionPane.getStyleClass().add("challenge-background");
    root.getChildren().add(instructionPane);

    var instructionBox = new VBox();

    var mainPane = new BorderPane();
    instructionPane.getChildren().add(mainPane);

    //set up the instruction image
    var instructionImage =
        new ImageView(this.getClass().getResource("/images/Instructions.png").toExternalForm());
    instructionImage.setPreserveRatio(true);
    instructionImage.setFitHeight(gameWindow.getHeight()*2.5/4);
    instructionImage.setFitWidth(gameWindow.getWidth()*2.5/4);
    instructionImage.setTranslateX((gameWindow.getWidth()-gameWindow.getWidth()*2.5/4)/2);
    mainPane.setTop(instructionImage);

    //set up an exit button
    var exitToMenu = new Text("Press here or ESC to exit");
    exitToMenu.getStyleClass().add("menuItem");
    var exitBox = new HBox(exitToMenu);
    exitBox.setAlignment(Pos.BOTTOM_RIGHT);
    exitBox.setTranslateY(-30);
    mainPane.setBottom(exitBox);
    exitToMenu.setOnMouseClicked(this::openMenu);

    //display all available game pieces
    Text pieces = new Text("Game Pieces");
    pieces.getStyleClass().add("heading");
    instructionBox.getChildren().add(pieces);

    //set up grid for pieces
    var gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER);
    instructionBox.getChildren().add(gridPane);
    instructionBox.setAlignment(Pos.CENTER);

    //display all 15 pieces
    int col = 0;
    int row = 0;
    for (int i = 0; i < 15; i++) {
      if (col  >= 5) {
        col = 0;
        row++;
      }
      PieceBoard piece = new PieceBoard(GamePiece.createPiece(i), 90, 90);
      gridPane.add(piece, col, row);
      gridPane.setMargin(piece, new Insets(10,10,10,10));
      col++;
    }

    mainPane.setCenter(instructionBox);
  }

  /** initialise the scene */
  @Override
  public void initialise() {
    //when ECS is pressed exit to menupage
    scene.setOnKeyPressed(
        (KeyEvent keyEvent) -> {
          switch (keyEvent.getCode()) {
            case ESCAPE:
              Multimedia.stop();
              gameWindow.startMenu();
          }
        });
  }

  /** open menu when pressed on button
   *
   * @param mouseEvent mouse is clicked
   */
  public void openMenu(MouseEvent mouseEvent) {
    gameWindow.startMenu();
  }
}
