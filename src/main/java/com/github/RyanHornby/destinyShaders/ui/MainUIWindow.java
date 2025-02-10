package com.github.RyanHornby.destinyShaders.ui;

import com.github.RyanHornby.destinyShaders.model.Color;
import com.github.RyanHornby.destinyShaders.model.UpdateResponse;
import com.github.RyanHornby.destinyShaders.model.entity.ShaderEntity;
import com.github.RyanHornby.destinyShaders.model.exception.DestinyManifestException;
import com.github.RyanHornby.destinyShaders.model.exception.HttpException;
import com.github.RyanHornby.destinyShaders.model.exception.NetworkException;
import com.github.RyanHornby.destinyShaders.service.ShaderService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MainUIWindow extends Application {
    private GridPane imageGrid;
    private GridPane filterGrid;
    private List<ComboBox<String>> filterDropdowns;
    private List<Pair<ShaderEntity, Image>> allImages;
    private List<String> categories;
    private ShaderService shaderService;
    private boolean shouldRetry = false;

    @Override
    public void start(Stage primaryStage) {
        shaderService = (ShaderService) primaryStage.getUserData();

        Label label = new Label("");
        ProgressIndicator progressIndicator = new ProgressIndicator();

        VBox vBox = new VBox(10, label, progressIndicator);
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox, 750, 600);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Checking for shader updates... Please wait");
                if (shaderService.emptyDb()) {
                    updateMessage("Performing first time setup, this may take a while...");
                    shaderService.refreshDb(shaderService.checkForUpdates());
                } else {
                    UpdateResponse updateResponse = shaderService.checkForUpdates();
                    if (updateResponse.getUpdateNeeded()) {
                        updateMessage("Updating shaders...");
                        shaderService.refreshDb(updateResponse);
                    }
                }
                return null;
            }
        };

        label.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            label.textProperty().unbind();
            makeMainUI(primaryStage);
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof NetworkException) {
                errorPopup(primaryStage, "A network exception occurred, please ensure you are connected to the internet.");
            } else if (exception instanceof HttpException) {
                errorPopup(primaryStage, "A Destiny API returned HTTP code " + ((HttpException) exception).getHttpCode() + ".");
            } else if (exception instanceof DestinyManifestException) {
                errorPopup(primaryStage, "Destiny was unable to provide their manifest file with error code " + ((DestinyManifestException) exception).getErrorCode() + ".");
            } else {
                errorPopup(primaryStage, "An unexpected internal error occurred.");
            }

            if (shouldRetry) {
                primaryStage.close();
                Stage stage = new Stage();
                stage.setUserData(shaderService);
                start(stage);
            } else {
                makeMainUI(primaryStage);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        primaryStage.setTitle("Destiny Shader Finder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void errorPopup(Stage primaryStage, String errorMessage) {
        Stage stage = new Stage();
        stage.setTitle("Error");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);

        Label message = new Label(errorMessage);
        Button retry = new Button("Retry");
        Button close = new Button("Continue");

        retry.setOnAction(e -> {
            this.shouldRetry = true;
            stage.close();
        });
        close.setOnAction(e -> {
            this.shouldRetry = false;
            stage.close();
        });

        HBox buttons = new HBox(10, retry, close);
        buttons.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(10, message, buttons);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 450, 100);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void makeMainUI(Stage primaryStage) {
        List<String> filterTitles = List.of("Inner Center", "Outer Center", "Upper Trim", "Lower Trim",
                                            "Left", "Right", "Up", "Down");

        categories = new ArrayList<>();
        categories.add("Any");
        for (Color color : Color.values()) {
            categories.add(color.name());
        }

        filterGrid = new GridPane();
        filterGrid.setHgap(10);
        filterGrid.setVgap(10);

        filterDropdowns = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ComboBox<String> filterDropdown = new ComboBox<>();
            filterDropdown.getItems().addAll(categories);
            filterDropdown.setValue("Any");
            filterDropdown.setOnAction(e -> updateImageGrid());
            filterGrid.add(new Label(filterTitles.get(i)), 0, i);
            filterGrid.add(filterDropdown, 1, i);
            filterDropdowns.add(filterDropdown);
        }

        allImages = loadImages();

        imageGrid = new GridPane();
        imageGrid.setHgap(10);
        imageGrid.setVgap(10);
        imageGrid.setPrefWidth(575);
        updateImageGrid();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(imageGrid);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        HBox root = new HBox(10, filterGrid, scrollPane);
        Scene scene = new Scene(root, 750, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private List<Pair<ShaderEntity, Image>> loadImages() {
        List<ShaderEntity> entities = shaderService.findAll();
        List<Pair<ShaderEntity, Image>> images = new ArrayList<>();
        for (ShaderEntity entity : entities) {
            images.add(new Pair<>(entity, new Image("file:" + entity.getImagePath())));
        }
        return images;
    }

    private void updateImageGrid() {
        imageGrid.getChildren().clear();
        int row = 0, col = 0;
        for (Pair<ShaderEntity, Image> pair : allImages) {
            if (matches(pair.getKey())) {
                ImageView imageView = new ImageView(pair.getValue());
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                Tooltip.install(imageView, new Tooltip(pair.getKey().getName()));
                imageGrid.add(imageView, col, row);
                col++;
                if (col >= 5) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private boolean matches(ShaderEntity entity) {
        return (filterDropdowns.get(0).getValue().equals("Any") || filterDropdowns.get(0).getValue().equals(entity.getInnerCenter().name())) &&
                (filterDropdowns.get(1).getValue().equals("Any") || filterDropdowns.get(1).getValue().equals(entity.getOuterCenter().name())) &&
                (filterDropdowns.get(2).getValue().equals("Any") || filterDropdowns.get(2).getValue().equals(entity.getTrimUpper().name())) &&
                (filterDropdowns.get(3).getValue().equals("Any") || filterDropdowns.get(3).getValue().equals(entity.getTrimLower().name())) &&
                (filterDropdowns.get(4).getValue().equals("Any") || filterDropdowns.get(4).getValue().equals(entity.getLeft().name())) &&
                (filterDropdowns.get(5).getValue().equals("Any") || filterDropdowns.get(5).getValue().equals(entity.getRight().name())) &&
                (filterDropdowns.get(6).getValue().equals("Any") || filterDropdowns.get(6).getValue().equals(entity.getUp().name())) &&
                (filterDropdowns.get(7).getValue().equals("Any") || filterDropdowns.get(7).getValue().equals(entity.getDown().name()));
    }

    public static void main(ShaderService shaderService) throws Exception {
        MainUIWindow mainUIWindow = new MainUIWindow();
        mainUIWindow.init();

        Platform.startup(() -> {
            Stage stage = new Stage();
            stage.setUserData(shaderService);
            mainUIWindow.start(stage);
        });
    }
}
