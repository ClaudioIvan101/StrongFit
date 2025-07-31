package com.ivan.gimnasio.ui;

import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.SocioRepository;
import com.ivan.gimnasio.presentation.controller.SocioCardController;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import com.ivan.gimnasio.util.AlertaUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class SocioControllerUI {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private ListView<Membresia> listViewMembresias;
    AlertaUtil alertaUtil;
    @Autowired
    private ISocioService socioService;
    @Autowired
    private IMembresiaService membresiaService;
    @Autowired private SpringFXMLLoader springFXMLLoader;
    private ControllerUI controllerUI;
    Socio socio;
    public void setControllerUI(ControllerUI controllerUI) {
        this.controllerUI = controllerUI;
    }
    private ControllerUI mainController;

    public void setMainController(ControllerUI controller) {
        this.mainController = controller;
    }

    @FXML
    private void cerrarFicha() {
        if (mainController != null) {
            mainController.cerrarPanelEmergente();
        }
    }
    @FXML
    public void initialize() {
        List<Membresia> membresias = membresiaService.obtenerTodas()
                .stream()
                .filter(Membresia::isActivo)
                .collect(Collectors.toList());

        listViewMembresias.setItems(FXCollections.observableArrayList(membresias));
    }

    public SocioControllerUI(ISocioService socioService) {
        this.socioService = socioService;
    }

    @FXML
    void registrarSocio(ActionEvent event) throws IOException {
        String name = txtNombre.getText();
        String username = txtApellido.getText();
        String dni = txtDni.getText();
        String phone = txtTelefono.getText();
        LocalDate date = LocalDate.now();
        String email = txtEmail.getText();
        if (name.isBlank() || username.isBlank() || dni.isBlank()) {
            alertaUtil.mostrarError("Por favor, completá todos los campos obligatorios.");
            return;
        }
        // Validar nombre: solo letras y espacios
        if (!name.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            alertaUtil.mostrarError("El nombre debe contener solo letras y espacios.");
            return;
        }
        // Validar DNI: solo números
        if (!dni.matches("\\d+")) {
            alertaUtil.mostrarError("El DNI debe contener solo números.");
            return;
        }
        Optional<Socio> existente = socioService.buscarSocioPorDniOptional(dni);
        if(existente.isPresent()) {
            Socio socioExistente = existente.get();
            if(!socioExistente.isActivo()) {
                boolean restaurar = alertaUtil.confirmar("Socio dado de baja",
                        "Este socio fue dado de baja. ¿Deseás restaurarlo?");
                if (restaurar) {
                    socioService.restaurarSocio(socioExistente);
                    alertaUtil.mostrarInfo("Socio restaurado con éxito.");
                    controllerUI.mostrarFichaSocio(socioExistente);
                    controllerUI.volverAVistaAnterior();
                }
                return;
            }else {
                alertaUtil.mostrarError("Ya existe un socio con el DNI proporcionado.");
                FXMLLoader loader = springFXMLLoader.load("/fxml/SocioCard.fxml");
                Parent root = loader.load();

                SocioCardController controller = loader.getController();
                controller.setSocio(socioExistente);

                // cargar el modal

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Ficha de Socio: " + socioExistente.getNombre() + " " + socioExistente.getApellido());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();;
            }
            } else {
            if(alertaUtil.confirmar("Registrar socio", "¿Estás seguro de que deseas registrar al socio?")) {
                Socio socio = new Socio();
                socio.setNombre(name);
                socio.setApellido(username);
                socio.setDni(dni);
                socio.setEmail(email);
                socio.setTelefono(phone);
                socio.setFechaInscripcion(date);
                Socio socioGuardado = socioService.registrarSocio(socio);
                ObservableList<Membresia> membresiasSeleccionadas = listViewMembresias.getSelectionModel().getSelectedItems();
                List<Long> idsMembresias = membresiasSeleccionadas.stream()
                        .map(Membresia::getId).collect(Collectors.toList());
                socioService.asignarMembresiasASocio(socioGuardado.getId(), idsMembresias);
                alertaUtil.mostrarInfo("Socio registrado con éxito.");
                controllerUI.volverAVistaAnterior();
            }
        }
    }
    @FXML
    void mostrarVistaAnterior(ActionEvent event) {
        controllerUI.volverAVistaAnterior();
    }

}
