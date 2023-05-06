package com.narukoshin.crawler;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class BrainStorm {
    /**
     * Status message mostly it shows if the crawler is running.
     * */
    @FXML
    private Label statusMessage;

    /**
     * Main stage
     * */
    private Stage mainStage;

    /**
     * Options button
     * */
    @FXML
    private Button optionsButton;

    /**
     * Table collection
     * */
    @FXML
    public TableView<TableCollection> TableCollection;

    /**
     * ID column of the table.
     * */
    @FXML
    public TableColumn<TableCollection, Integer> idColumn;

    /**
     * URL Column of the table.
     * */
    @FXML
    public TableColumn<TableCollection, String> urlColumn;

    private Thread CrawlerThread = null;

    /**
     * Counting the number of found websites with readable .git folder
     * */
    public int count = 0;

    /**
     * Initialize function where some table cell value factory is defined.
     * */
    public void initialize() {
        // Connecting to the database
        Database db = new Database();
        // Initializing some configuration
        Config c = new Config();
        // Setting a static config instance
        // So all other classes can access all the settings and methods
        Config.setInstance(c);
        c.setDatabase(db);
        // Loading the settings from the database.
        try {
            c.createSettings();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        // There we will load all the found websites from the database if there are any.
        try {
            // Requesting websites from the database.
            List<?> websites = c.getWebsites();
            for (Object element : websites) {
                count++;
                // Adding data to the table.
                TableCollection data = new TableCollection(count, (String) element);
                TableCollection.getItems().add(data);
                TableCollection.refresh();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * When the user clicks the start button, we are starting crawler in another thread
     * to run it in the background.
     *
     * */
    public void onStartClick(){
        // If the crawler is already running, then we are not re-running it again.
        if (Config.isRunning) return;
        // Setting running status to true to avoid multiple launches.
        Config.setRunning(true);
        CrawlerThread = new Thread(() -> {
            Crawler c = new Crawler();
            c.createReference(BrainStorm.this);
            c.StartCrawler(Config.isRunning);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                stopCrawlerExecution();
                System.out.println("Something very bad happened");
                e.printStackTrace();
            }
        });
        CrawlerThread.setDaemon(true);
        CrawlerThread.start();
        // Setting the status message to Running to indicate that the app is processing the task.
        changeStatus("Running...");
    }


    /**
     * When a user clicks the stop button, we are stopping the crawler.
     * */
    public void onStopClick(){
        stopCrawlerExecution();
    }

    /**
     * A function for stopping the crawler.
     * It changes the status message to null and the isRunning boolean value to false.
     * */
    public void stopCrawlerExecution() {
        // If the crawler is not running, then we are not executing the following code.
        if (!Config.isRunning) return;
        Config.setRunning(false);
        changeStatus("");
        CrawlerThread.stop();
        // Clearing the current status message to indicate that the app stopped processing the task.
    }

    /**
     * Change the status message.
     * @param message   the new status message
     * */
    public void changeStatus(String message){
        System.out.println("Changing status message");
        Platform.runLater(() -> statusMessage.setText(message));
    }

    /**
     * When a user clicks the option button, an option window will open.
     * There user can edit some settings, for example, change the cookie value
     * or edit some parameters of the Google query.
     *
     * @throws Exception throws any exception that might be thrown on clicking the option button.
     * */
    public void onOptionsClick() throws Exception {
        Stage optionsStage = new Stage();
        FXMLLoader loader = new FXMLLoader(App.class.getResource("options.fxml"));

        try {
            Parent root = loader.load();
            Scene optionsScene = new Scene(root);
            // Setting ownership and Modality to ensure that user can't click anywhere
            // while the option window is open
            optionsStage.initModality(Modality.APPLICATION_MODAL);
            optionsStage.initOwner(mainStage);
            optionsStage.setTitle("Options");
            optionsStage.setResizable(false);
            // making the main window as the owner,
            // if the main window is closed, options will be closed too.
            optionsStage.setScene(optionsScene);
            optionsStage.show();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    /**
     * When a user clicks the save button, it will save all the results from the table in the database
     * and also the current settings, cookie value, etc.
     * */
    public void onSaveClick(){
        for (TableCollection item : TableCollection.getItems()) {
            String url = item.getUrl().strip();
            Config c = Config.getInstance();
            try {
                c.addWebsite(url);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Getting the value of the main stage to initialize ownership to the option stage.
     * This is used to avoid bugs like if the main window is closed, options will be closed too.
     * */
    public void setMainStage(Stage stage) {
        mainStage = stage;
    }
}