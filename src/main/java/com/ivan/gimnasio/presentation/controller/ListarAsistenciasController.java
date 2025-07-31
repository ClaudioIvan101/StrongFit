package com.ivan.gimnasio.presentation.controller;

import com.ivan.gimnasio.persistence.entity.Asistencia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.persistence.repository.AsistenciaRepository;
import com.ivan.gimnasio.service.interfaces.IAsistenciaService;
import com.ivan.gimnasio.util.EstadoCuota;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeSet;
@Controller
@Scope("prototype")
public class ListarAsistenciasController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<Asistencia> tablaAsistencias;
    @FXML private TableColumn<Asistencia,String> colSocio;
    @FXML private TableColumn<Asistencia, LocalDateTime> colFecha;
    @FXML private TableColumn<Asistencia, String> colActividad;
    @FXML private TableColumn<Asistencia, String> colEstadoCuota;
    @FXML private Label labelContador;
    @FXML private ComboBox<String> comboBox;
    private List<Asistencia> asistenciasDelDia = new ArrayList<>();
    @Autowired
    private IAsistenciaService asistenciaService;
    @FXML
    public void initialize() {
        datePicker.setOnAction(event -> cargarAsistenciasPorFecha(datePicker.getValue()));
        colSocio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSocio().getNombre() + " " + data.getValue().getSocio().getApellido()));
        colFecha.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFechaHora()));
        colActividad.setCellValueFactory(cellData -> {
            Socio socio = cellData.getValue().getSocio();
            String actividad = "Sin asignar";
            if (socio.getMembresias() != null && !socio.getMembresias().isEmpty()) {
                actividad = socio.getMembresias().iterator().next().getNombre();
            }
            return new SimpleStringProperty(actividad);
        });
        colEstadoCuota.setCellValueFactory(cellData -> {
            Socio socio = cellData.getValue().getSocio();
            String estado = (socio.getEstadoCuota() == EstadoCuota.AL_DIA) ? "Cuota al día" : "Cuota vencida";
            return new SimpleStringProperty(estado);
        });
        comboBox.setOnAction(event -> {
                String actividadSeleccionada = comboBox.getValue();
                aplicarFiltroActividad(actividadSeleccionada);
            });

        datePicker.setValue(LocalDate.now());
        cargarAsistenciasPorFecha(LocalDate.now());
    }
    private void cargarAsistenciasPorFecha(LocalDate fecha) {
        // Aquí puedes implementar la lógica para cargar las asistencia
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(LocalTime.of(23, 59, 59));
        asistenciasDelDia = asistenciaService.buscarConSocioYMiembrosEntreFechas(desde, hasta);
        Set<String> actividades = asistenciasDelDia.stream()
                .map(a -> {
                    Socio socio = a.getSocio();
                    return (socio.getMembresias() != null && !socio.getMembresias().isEmpty())
                            ? socio.getMembresias().iterator().next().getNombre()
                            : "Sin asignar";
                })
                .collect(Collectors.toCollection(TreeSet::new));
        actividades.add("Todas"); // opción general
        comboBox.setItems(FXCollections.observableArrayList(actividades));
        comboBox.setValue("Todas");
        aplicarFiltroActividad("Todas");
        labelContador.setText(String.valueOf(asistenciasDelDia.size()));// o repo
        tablaAsistencias.setItems(FXCollections.observableArrayList(asistenciasDelDia));
    }
    private void aplicarFiltroActividad(String actividadSeleccionada) {
        List<Asistencia> filtradas = asistenciasDelDia.stream()
                .filter(a -> {
                    Socio socio = a.getSocio();
                    String actividad = (socio.getMembresias() != null && !socio.getMembresias().isEmpty())
                            ? socio.getMembresias().iterator().next().getNombre()
                            : "Sin asignar";
                    return actividadSeleccionada.equals("Todas") || actividad.equals(actividadSeleccionada);
                })
                .toList();
        long cantidadSociosUnicos = asistenciasDelDia.stream()
                .map(a -> a.getSocio().getDni())
                .distinct()
                .count();
        tablaAsistencias.setItems(FXCollections.observableArrayList(filtradas));
        labelContador.setText(String.valueOf(cantidadSociosUnicos));
    }

}
