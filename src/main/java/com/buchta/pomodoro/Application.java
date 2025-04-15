package com.buchta.pomodoro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.util.Duration;

public class Application extends javafx.application.Application {

  private int workTimeInSeconds = 40 * 60; // default 25 minutes
  private int breakTimeInSeconds = 10 * 60; // default 10 minutes
  private boolean isWorkTime = true;
  private int remainingTime = workTimeInSeconds;
  private Timeline timeline;
  private Label timerLabel;
  private Label modeLabel;

  private List<HBox> taskItems = new ArrayList<>();
  private VBox tasksBox = new VBox(5);
  private final String TASKS_FILE = "tasks.txt";

  @Override
  public void start(Stage stage) {
    stage.setAlwaysOnTop(true);
    timerLabel = new Label(formatTime(remainingTime));
    timerLabel.setStyle("-fx-font-size: 48px;");

    modeLabel = new Label("Work Time");
    modeLabel.setStyle("-fx-font-size: 24px;");

    Button startButton = new Button("Start");
    Button pauseButton = new Button("Pause");
    Button resetButton = new Button("Reset");

    startButton.setOnAction(e -> startTimer());
    pauseButton.setOnAction(e -> pauseTimer());
    resetButton.setOnAction(e -> resetTimer());

    TextField taskInput = new TextField();
    taskInput.setPromptText("New task");

    Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, 5);
    prioritySpinner.setEditable(true);

    Button addTaskButton = new Button("Add Task");
    addTaskButton.setOnAction(e -> addTask(taskInput.getText(), prioritySpinner.getValue(), taskInput));

    Button clearCompletedButton = new Button("Clear Completed");
    clearCompletedButton.setOnAction(e -> clearCompletedTasks());

    Button configureTimesButton = new Button("Configure Times");
    configureTimesButton.setOnAction(e -> showSettingsWindow());

    Button saveTasksButton = new Button("Save Tasks Now");
    saveTasksButton.setOnAction(e -> saveTasksToFile());

    HBox taskButtons = new HBox(10, clearCompletedButton, configureTimesButton, saveTasksButton);
    taskButtons.setAlignment(javafx.geometry.Pos.CENTER);

    HBox taskInputBox = new HBox(10, taskInput, new Label("Priority:"), prioritySpinner, addTaskButton);
    taskInputBox.setAlignment(Pos.CENTER);

    HBox timerButtons = new HBox(10, startButton, pauseButton, resetButton);
    timerButtons.setAlignment(javafx.geometry.Pos.CENTER);

    VBox layout = new VBox(20, modeLabel, timerLabel, timerButtons,
        new Label("Tasks:"), taskInputBox, tasksBox, taskButtons);
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(10));

    loadTasksFromFile();

    stage.setOnCloseRequest(e -> saveTasksToFile());

    Scene scene = new Scene(layout, 500, 800);
    stage.setTitle("Pomodoro Timer");
    stage.setScene(scene);
    stage.show();
  }

  private void showSettingsWindow() {
    Stage settingsStage = new Stage();
    VBox settingsLayout = new VBox(10);
    settingsLayout.setPadding(new Insets(10));
    settingsLayout.setAlignment(Pos.CENTER);

    TextField workTimeField = new TextField(String.valueOf(workTimeInSeconds / 60));
    workTimeField.setPromptText("Work Time (minutes)");

    TextField breakTimeField = new TextField(String.valueOf(breakTimeInSeconds / 60));
    breakTimeField.setPromptText("Break Time (minutes)");

    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> {
      try {
        workTimeInSeconds = Integer.parseInt(workTimeField.getText()) * 60;
        breakTimeInSeconds = Integer.parseInt(breakTimeField.getText()) * 60;
        resetTimer();
        settingsStage.close();
      } catch (NumberFormatException ex) {
        new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.").showAndWait();
      }
    });

    settingsLayout.getChildren().addAll(new Label("Configure Times"), workTimeField, breakTimeField, saveButton);
    settingsStage.setScene(new Scene(settingsLayout, 300, 200));
    settingsStage.setTitle("Settings");
    settingsStage.show();
  }

  private void addTask(String taskText, int priority, TextField taskInput) {
    if (taskText.trim().isEmpty()) {
      return;
    }

    Label priorityLabel = new Label("Priority: " + priority);
    CheckBox taskCheckbox = new CheckBox(taskText);
    HBox taskItem = new HBox(10, taskCheckbox, priorityLabel);
    taskItem.setAlignment(Pos.CENTER_LEFT);
    taskItem.setUserData(priority);

    taskItems.add(taskItem);
    tasksBox.getChildren().add(taskItem);
    taskInput.clear();
  }

  private void clearCompletedTasks() {
    Iterator<HBox> iterator = taskItems.iterator();
    while (iterator.hasNext()) {
      HBox taskItem = iterator.next();
      CheckBox cb = (CheckBox) taskItem.getChildren().get(0);
      if (cb.isSelected()) {
        tasksBox.getChildren().remove(taskItem);
        iterator.remove();
      }
    }
  }

  private void startTimer() {
    if (timeline == null || timeline.getStatus() != Timeline.Status.RUNNING) {
      timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
        remainingTime--;
        timerLabel.setText(formatTime(remainingTime));

        if (remainingTime <= 0) {
          // playNotificationSound();
          timeline.stop();
          switchMode();
          startTimer();
        }
      }));
      timeline.setCycleCount(Timeline.INDEFINITE);
      timeline.play();
    }
  }

  // // Add this method somewhere in your class:
  // private void playNotificationSound() {
  //   try {
  //     String soundFile = "sound.mp3"; // or .wav — wav files usually have better compatibility
  //     AudioClip clip = new AudioClip(new File(soundFile).toURI().toString());
  //     clip.play();
  //   } catch (Exception e) {
  //     System.out.println("Error playing sound: " + e.getMessage());
  //   }
  // }

  private void pauseTimer() {
    if (timeline != null) {
      timeline.pause();
    }
  }

  private void resetTimer() {
    if (timeline != null) {
      timeline.stop();
    }
    isWorkTime = true;
    remainingTime = workTimeInSeconds;
    timerLabel.setText(formatTime(remainingTime));
    modeLabel.setText("Work Time");
  }

  private void switchMode() {
    isWorkTime = !isWorkTime;
    remainingTime = isWorkTime ? workTimeInSeconds : breakTimeInSeconds;
    modeLabel.setText(isWorkTime ? "Work Time" : "Break Time");
  }

  private String formatTime(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private void saveTasksToFile() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASKS_FILE))) {
      for (HBox taskItem : taskItems) {
        CheckBox cb = (CheckBox) taskItem.getChildren().get(0);
        Label priorityLabel = (Label) taskItem.getChildren().get(1);
        int priority = (int) taskItem.getUserData();
        writer.write(cb.isSelected() + ";" + cb.getText() + ";" + priority);
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void loadTasksFromFile() {
    File file = new File(TASKS_FILE);
    if (!file.exists()) {
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(";", 3);
        if (parts.length == 3) {
          CheckBox cb = new CheckBox(parts[1]);
          cb.setSelected(Boolean.parseBoolean(parts[0]));
          int priority = Integer.parseInt(parts[2]);
          Label priorityLabel = new Label("Priority: " + priority);
          HBox taskItem = new HBox(10, cb, priorityLabel);
          taskItem.setAlignment(Pos.CENTER_LEFT);
          taskItem.setUserData(priority);
          taskItems.add(taskItem);
          tasksBox.getChildren().add(taskItem);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
