package com.ivan.gimnasio.ui;

import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class SocioControllerUI {
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDni;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtApellido;
    private ISocioService socioService;

    public SocioControllerUI(ISocioService socioService) {
        this.socioService = socioService;
    }

    @FXML
    private void registrarSocio() {
        String nombre = txtNombre.getText();
        String dni = txtDni.getText();

        LocalDate date = LocalDate.now();
        String phone = txtTelefono.getText();
        Socio socio = new Socio();
        socio.setNombre(nombre);
        socio.setDni(dni);
        socio.setFechaInscripcion(date);


        socioService.registrarSocio(socio);
        System.out.println("Socio registrado con éxito");
    }
}
