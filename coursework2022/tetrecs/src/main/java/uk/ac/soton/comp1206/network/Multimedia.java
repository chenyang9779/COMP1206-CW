package uk.ac.soton.comp1206.network;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  private static final BooleanProperty audioEnabledProperty = new SimpleBooleanProperty(true);

  /**
   * media players
   */
  private static MediaPlayer audioPlayer;
  private static MediaPlayer musicPlayer;

  /**
   * Play audio file
   *
   * @param f filename
   */
  public static void audioFile(String f) {
    if (!getAudioEnabled()) return;
    String audio = Multimedia.class.getResource("/sounds/" + f).toExternalForm();
    logger.info("Starting to play audio" + audio);

    try {
      var toPlay = new Media(audio);
      audioPlayer = new MediaPlayer(toPlay);
      audioPlayer.play();
    } catch (Exception e) {
      setAudioEnabled(false);
      e.printStackTrace();
      logger.error("Unable to play audio file, disabling audio");
    }
  }

  /**
   * Play music file
   *
   * @param f filename
   */
  public static void backgroundMusic(String f) {
    if (!getAudioEnabled()) return;
    String music = Multimedia.class.getResource("/music/" + f).toExternalForm();
    logger.info("Starting to play music" + music);

    try {
      var toPlay = new Media(music);
      musicPlayer = new MediaPlayer(toPlay);
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      musicPlayer.play();
    } catch (Exception e) {
      setAudioEnabled(false);
      logger.error("Unable to play audio file, disabling audio");
      e.printStackTrace();
    }
  }

  /**
   * Method to top playing the music
   */
  public static void stop() {
    logger.info("Stopping the current music");
    musicPlayer.stop();
  }

  public static void pause() {
    musicPlayer.pause();
  }
  public static void start() {
    musicPlayer.play();
  }

  public static BooleanProperty audioEnabledProperty(){
    return audioEnabledProperty;
  }

  public static boolean getAudioEnabled() {
    return audioEnabledProperty().get();
  }

  public static void setAudioEnabled(boolean enabled) {
    logger.info("Audio enabled set to: " + enabled);
    audioEnabledProperty().set(enabled);
  }
}
