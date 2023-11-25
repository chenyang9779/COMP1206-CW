package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerGame extends Game{

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

  private final GameWindow gameWindow;

  private GamePiece piece;

  private Communicator communicator;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows,GameWindow gameWindow) {
    super(cols, rows);
    this.gameWindow = gameWindow;
    communicator = gameWindow.getCommunicator();
    communicator.addListener((e)->{
      if(e.startsWith("PIECE")){
        e.replace("PIECE ","");
        piece = GamePiece.createPiece((Integer.parseInt(e)));
      }
    });
  }

  @Override
  public GamePiece spawnPiece(){
    communicator.send("PIECE");

    return piece;
  }
}
