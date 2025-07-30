package com.ivan.gimnasio.presentation.controller;

import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.util.EstadoCuota;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
public class SocioCardController {

    @FXML
    private Label nombreLabel;

    @FXML
    private Label dniLabel;

    @FXML
    private Label fechaLabel;

    @FXML
    private Label telefonoLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label estadoCuotaLabel;

    @FXML
    private Label actividadesLabel;

    @FXML
    private ImageView fotoImageView;

    public void setSocio(Socio socio) {
        nombreLabel.setText(socio.getNombre() + " " + socio.getApellido());
        dniLabel.setText("DNI: " + socio.getDni());
        fechaLabel.setText("Alta: " + socio.getFechaInscripcion());

        if (socio.getTelefono() != null)
            telefonoLabel.setText("Teléfono: " + socio.getTelefono());
        if (socio.getEmail() != null)
            emailLabel.setText("Email: " + socio.getEmail());
        estadoCuotaLabel.setText("Estado de cuota: " +
                (socio.getEstadoCuota() == EstadoCuota.AL_DIA ? "Al día" : "Vencida"));
        if (socio.getMembresias() != null && !socio.getMembresias().isEmpty()) {
            String actividades = socio.getMembresias().stream()
                    .map(Membresia::getNombre)
                    .collect(Collectors.joining(", "));
            actividadesLabel.setText("Membresías: " + actividades);
        } else {
            actividadesLabel.setText("Membresías: Ninguna");
        }

        // Si querés mostrar imagen
//        if (socio.getFoto() != null) {
//            Image imagen = new Image(new ByteArrayInputStream(socio.getFoto()));
//            fotoImageView.setImage(imagen);
//        }
    }
}