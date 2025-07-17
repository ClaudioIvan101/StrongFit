package com.ivan.gimnasio.ui;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;

@Component
public class ControllerUI {
    @FXML
    private AnchorPane contentPane;

    @Autowired
    private ApplicationContext context;

    public TextField txtNombre;
    public TextField txtDni;
    public TextField txtEmail;
    public TextField txtTelefono;
    public TextField txtApellido;
    private ISocioService socioService;
    public ControllerUI(ISocioService socioService) {
        this.socioService = socioService;
    }
    @FXML
    void registrarSocio(ActionEvent event) {
        String name = txtNombre.getText();
        String username = txtApellido.getText();
        String dni = txtDni.getText();
        String phone = txtTelefono.getText();
        LocalDate date = LocalDate.now();
        String email = txtEmail.getText();
        if (name.isBlank() || username.isBlank() || dni.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Validacion");
            alert.setHeaderText(null);
            alert.setContentText("⚠Por favor, completá todos los campos obligatorios.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #f8d7da; -fx-border-color: #f5c6cb;");
            dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-text-fill: #721c24;");
            alert.showAndWait();
            return;
        }
        Socio socio = new Socio();
        socio.setNombre(name);
        socio.setApellido(username);
        socio.setDni(dni);
        socio.setEmail(email);
        socio.setTelefono(phone);
        socio.setFechaInscripcion(date);
        socioService.registrarSocio(socio);
        System.out.println("Socio registrado con éxito");
    }

    @FXML
    private void mostrarRegistrarSocio() throws IOException {
        cargarVista("RegistrarSocio.fxml");
    }

    private void cargarVista(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
        loader.setControllerFactory(context::getBean); // inyección con Spring
        Parent root = loader.load();
        contentPane.getChildren().setAll(root);
    }
    @Bean
    String titulo() {
    return "StrongFit";
    }
}
