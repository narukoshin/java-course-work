package com.narukoshin.crawler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.sql.SQLException;

public class OptionsController {
    @FXML
    private TextField queryString;

    @FXML
    private TextField cookieValue;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField additionalParameters;

    public void initialize(){
        Config c = Config.getInstance();
        try {
            queryString.setText(c.getSetting("queryString"));
            cookieValue.setText(c.getSetting("cookieValue"));
            additionalParameters.setText(c.getSetting("additionalParameters"));
        } catch (SQLException e) {
            // Setting default query string config
            queryString.setText(Config.queryString);
            // Setting default cookie value config
            cookieValue.setText(Config.cookieValue);
            // setting default search parameter
            additionalParameters.setText(Config.additionalParameters);
        }
    }

    /**
     * When a user clicks the save button, it will save the settings in the database.
     * If a crawler is running, saving changes will be impossible till it's stopped.
     * */
    public void onSaveSettings(){
        // Updating config
        if (Config.isRunning) {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Cannot save settings while running");
            return;
        }

        Config c = Config.getInstance();
        try {
            // Updating settings in the database.
            c.updateSetting("queryString", queryString.getText());
            c.updateSetting("cookieValue", cookieValue.getText());
            c.updateSetting("additionalParameters", additionalParameters.getText());
            System.out.println("Updated");
        } catch (SQLException e) {
            // updating default settings
            Config.queryString = queryString.getText();
            Config.cookieValue = cookieValue.getText();
            Config.additionalParameters = additionalParameters.getText();
            e.printStackTrace();
        }

        // Showing the message that the settings were saved.
        errorLabel.setText("Settings saved");
    }
}
