package uk.ac.soton.comp1206.component;

import java.util.Collections;
import java.util.Comparator;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;

/** handles score files */
public class ScoreFileHandler {

  /** Initiate the file */
  private File scoreFile = new File("scoreFile.txt");

  private static Logger logger = LogManager.getLogger(ScoreFileHandler.class);

  /**
   * Constructor for ScoreFileHandler class. Creates file and fills with default scores if it does
   * not exist.
   */
  public ScoreFileHandler() {
    if (!scoreFile.exists()) {
      try {
        fileExist();
        logger.info("File creates");
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      logger.info("File found");
    }
  }

  /**
   * Reads scores from file.
   *
   * @return list of scores.
   * @throws IOException : IO errors.
   */
  public SimpleListProperty<Pair<String, Integer>> loadScores() throws IOException {
    ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    // read the file
    BufferedReader bufferedReader = new BufferedReader(new FileReader(scoreFile));
    while (bufferedReader.ready()) {
      String[] split = bufferedReader.readLine().split(":");
      scores.add(new Pair<>(split[0], Integer.parseInt(split[1])));
    }
    bufferedReader.close();
    return new SimpleListProperty<>(FXCollections.observableArrayList(scores));
  }

  /**
   * Writes scores to file.
   *
   * @param score : scores to be written to file.
   * @throws IOException : IO errors.
   */
  public void writeScores(ObservableList<Pair<String, Integer>> score) throws IOException {

    // sorts the score to descending order
    Collections.sort(
        score,
        new Comparator<>() {
          public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            if (o1.getValue() > o2.getValue()) {
              return -1;
            } else if (o1.getValue() < o2.getValue()) {
              return 1;
            } else {
              return 0;
            }
          }
        });

    // write the score in to the file
    BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile));
    for (Pair<String, Integer> p : score) {
      writer.write(p.getKey() + ":" + p.getValue());
      writer.write("\n");
    }
    writer.close();
  }

  /** if file doesn't exist write these default score to it */
  public void fileExist() throws Exception {
    scoreFile.createNewFile();
    ArrayList<Pair<String, Integer>> defaultScores = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      defaultScores.add(new Pair<>("Level " + i, i * 1000));
    }
    writeScores(new SimpleListProperty<>(FXCollections.observableArrayList(defaultScores)));
  }
}
