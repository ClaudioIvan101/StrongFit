package com.ivan.gimnasio.presentation.controller;

import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import com.ivan.gimnasio.util.AlertaUtil;
import com.ivan.gimnasio.util.EstadoCuota;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
@Scope("prototype")
public class ListarSociosController implements Initializable {
    // Referencias a la tabla y columnas de la vista
    @FXML
    private TableView<Socio> tablaSocios;
    @FXML
    private TableColumn<Socio, String> colNombre;
    @FXML
    private TableColumn<Socio,String> colApellido;
    @FXML
    private TableColumn<Socio, String> colDni;
    @FXML
    private TableColumn<Socio, String> colEstadoCuota;
    @FXML
    private TableColumn<Socio, Void> colAcciones;
    @FXML
    private TableColumn<Socio, String> colActividad;
    AlertaUtil alertaUtil;
    @Autowired
    private ISocioService socioService;
    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    private Membresia membresiaFiltrada;

    private EstadoCuota filtroEstadoCuota;

    public void setFiltroEstadoCuota(EstadoCuota estado) {
        this.filtroEstadoCuota = estado;
    }
    // Permite filtrar la lista de socios por membresía
    public void setMembresiaFiltrada(Membresia membresia) {
        this.membresiaFiltrada = membresia;
        cargarSocios();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuración de columnas básicas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

        colActividad.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    Socio socio = getTableView().getItems().get(getIndex());
                    String actividad = "Sin asignar";
                    String color = "white";
                    Tooltip tooltip = null;

                    if (socio.getMembresias() != null && !socio.getMembresias().isEmpty()) {
                        Membresia m = socio.getMembresias().iterator().next();
                        actividad = m.getNombre();
                        if (!m.isActivo()) {
                            color = "red";
                            tooltip = new Tooltip("Esta membresía fue dada de baja");
                        }
                    }

                    setText(actividad);
                    setStyle("-fx-text-fill: " + color + ";");
                    setTooltip(tooltip);
                }
            }
        });

        // Muestra la primera membresía del socio o "Sin asignar"
        colActividad.setCellValueFactory(cellData -> {
            Socio socio = cellData.getValue();
            String actividad = "Sin asignar";
            if (socio.getMembresias() != null && !socio.getMembresias().isEmpty()) {
                actividad = socio.getMembresias().iterator().next().getNombre();
            }
            return new SimpleStringProperty(actividad);
        });

        // Estado de la cuota (al día o vencida)
        colEstadoCuota.setCellValueFactory(cellData -> {
            Socio socio = cellData.getValue();
            String estado = (socio.getEstadoCuota() == EstadoCuota.AL_DIA) ? "Cuota al día" : "Cuota vencida";
            return new SimpleStringProperty(estado);
        });

        // Botones de acciones (modificar, listar, eliminar, ficha)
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnModificar = crearBotonConIcono("editar.png", "Modificar socio");
            private final Button btnListarMiembros = crearBotonConIcono("lista-de-verificacion.png", "Ver miembros");
            private final Button btnEliminar = crearBotonConIcono("borrar.png", "Eliminar socio");
            private final Button btnFicha = crearBotonConIcono("iconPerson.png", "Ver ficha");

            private final HBox hbox = new HBox(8, btnModificar, btnListarMiembros, btnEliminar, btnFicha);


            {
                btnEliminar.setOnAction(e -> {
                    Socio s = getTableView().getItems().get(getIndex());
                    // Confirmación antes de eliminar
                   if(alertaUtil.confirmar("Eliminar Socio", "¿Estás seguro de eliminar a " + s.getNombre() + " " + s.getApellido() + "?")) {
                        socioService.eliminarSocio(s);
                        cargarSocios();
                    }
                });
                btnFicha.setOnAction(e -> {
                    Socio s = getTableView().getItems().get(getIndex());
                    mostrarFichaSocio(s);
                });
                hbox.setAlignment(Pos.CENTER_LEFT);
                colAcciones.setMinWidth(300);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Carga inicial de socios
        if (membresiaFiltrada == null) {
            cargarSocios();
        }

        // Debug: imprime los socios en consola
        List<Socio> sociosDebug = socioService.listarSocios();
        sociosDebug.forEach(socio -> {
            System.out.println(socio.getNombre() + " " + socio.getApellido());
            System.out.println(socio.getDni());
        });
        System.out.println("-----------");
    }

    // Carga la lista de socios en la tabla, filtrando si corresponde
    public void cargarSocios() {
        List<Socio> socios;
        if (filtroEstadoCuota != null) {
            socios = socioService.listarSociosConCuotaVencida();
        } else if (membresiaFiltrada != null) {
            socios = socioService.listarSociosPorMembresia(membresiaFiltrada);
        } else {
            socios = socioService.listarSocios();
        }
        tablaSocios.getItems().setAll(socios);
    }

    // Muestra la ficha del socio en una ventana modal
    private void mostrarFichaSocio(Socio socio) {
        try {
            FXMLLoader loader = springFXMLLoader.load("/fxml/SocioCard.fxml");
            Parent root = loader.load();

            SocioCardController controller = loader.getController();
            controller.setSocio(socio);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ficha de: " + socio.getNombre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Button crearBotonConIcono(String iconPath, String tooltipText) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/img/" + iconPath)));
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Button boton = new Button();
        boton.setGraphic(icon);
        boton.getStyleClass().add("table-button");
        boton.setTooltip(new Tooltip(tooltipText));
        boton.setMinSize(32, 32);
        boton.setPrefSize(32, 32);

        return boton;
    }

}
