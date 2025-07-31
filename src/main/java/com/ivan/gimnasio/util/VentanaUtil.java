package com.ivan.gimnasio.util;

import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class VentanaUtil {
    public static void abrirModal(SpringFXMLLoader springLoader, String fxmlPath, String titulo, Consumer<Object> controllerConfig) {
        try {
            FXMLLoader loader = springLoader.load(fxmlPath);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controllerConfig != null) {
                controllerConfig.accept(controller);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AlertaUtil.mostrarError("Error al cargar la ventana: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
