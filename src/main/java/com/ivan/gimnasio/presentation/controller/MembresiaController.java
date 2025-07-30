package com.ivan.gimnasio.presentation.controller;
import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import com.ivan.gimnasio.ui.ControllerUI;
import com.ivan.gimnasio.util.AlertaUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class MembresiaController {

    @FXML
    private Button btnCrearMembresia;

    @FXML
    private TableView<Membresia> tablaMembresias;

    @FXML
    private TableColumn<Membresia, String> colNombre;

    @FXML
    private TableColumn<Membresia, Double> colPrecio;

    @FXML
    private TableColumn<Membresia, Void> colAcciones;
    @FXML
    private VBox panelCrearMembresia;

    @FXML
    private TextField txtNombreMembresia;

    @FXML
    private TextField txtPrecioMembresia;
   @FXML
   private Label lblTituloPanelMembresia;
   AlertaUtil alertaUtil;
    @Autowired
    private SpringFXMLLoader springFXMLLoader;
    @Autowired
    private ControllerUI controllerUI;
    @Autowired
    private IMembresiaService membresiaService;
    public void setMembresiaService(IMembresiaService service) {
        this.membresiaService = service;
    }
    public void setControllerUI(ControllerUI controllerUI) {
        this.controllerUI = controllerUI;
    }
    private Membresia membresiaEnEdicion = null;
    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnModificar = new Button("Modificar");
            private final Button btnListarMiembros = new Button("Miembros");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox hbox = new HBox(5, btnModificar, btnListarMiembros, btnEliminar);

            {
                // Icono editar
                ImageView iconEditar = new ImageView(new Image(getClass().getResourceAsStream("/img/editar.png")));
                iconEditar.setFitHeight(12);
                iconEditar.setFitWidth(16);
                btnModificar.setGraphic(iconEditar);

                // Icono usuarios
                ImageView iconUsuarios = new ImageView(new Image(getClass().getResourceAsStream("/img/lista-de-verificacion.png")));
                iconUsuarios.setFitHeight(12);
                iconUsuarios.setFitWidth(16);
                btnListarMiembros.setGraphic(iconUsuarios);

                // Icono eliminar
                ImageView iconEliminar = new ImageView(new Image(getClass().getResourceAsStream("/img/borrar.png")));
                iconEliminar.setFitHeight(12);
                iconEliminar.setFitWidth(16);
                btnEliminar.setGraphic(iconEliminar);

                hbox.setAlignment(Pos.CENTER);


                btnModificar.setOnAction(e -> {
                    Membresia seleccionada = getTableView().getItems().get(getIndex());
                    txtNombreMembresia.setText(seleccionada.getNombre());
                    txtPrecioMembresia.setText(String.valueOf(seleccionada.getPrecio()));
                    membresiaEnEdicion = seleccionada;
                    lblTituloPanelMembresia.setText("Actualizar Membresia");
                    panelCrearMembresia.setVisible(true);
                    panelCrearMembresia.setManaged(true);
                });


                btnListarMiembros.setOnAction(e -> {
                    Membresia m = getTableView().getItems().get(getIndex());
                    try {
                        abrirSociosDeMembresia(m);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                btnEliminar.setOnAction(e -> {
                    Membresia m = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmar Eliminación");
                    alert.setHeaderText(null);
                    alert.setContentText("¿Estás seguro de que quieres eliminar la membresía?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        dardeBajaMembresia(m);
                        cargarMembresias();
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
        cargarMembresias();
    }

    public void cargarMembresias(){
        List<Membresia> membresias = membresiaService.listarMembresiasActivas();
        ObservableList<Membresia> datos = FXCollections.observableArrayList(membresias);
        tablaMembresias.setItems(datos);
    }
    public void eliminarMembresia(Membresia membresia) {
        membresiaService.eliminar(membresia.getId());
    }
    public void dardeBajaMembresia(Membresia membresia) {
        membresiaService.darDeBajaMembresia(membresia.getId());
    }
   /// PANEL MEMBRESIA ///
    @FXML
    void mostrarPanelCrearMembresia(ActionEvent event) {
        membresiaEnEdicion = null;
        lblTituloPanelMembresia.setText("Nueva Membresía");
        panelCrearMembresia.setVisible(true);
        panelCrearMembresia.setManaged(true);
        txtNombreMembresia.clear();
        txtPrecioMembresia.clear();
    }

    @FXML
    void cancelarCrearMembresia(ActionEvent event) {
        panelCrearMembresia.setVisible(false);
        panelCrearMembresia.setManaged(false);
        txtNombreMembresia.clear();
        txtPrecioMembresia.clear();
        membresiaEnEdicion = null;
    }

    @FXML
    void guardarMembresia(ActionEvent event) {
        String nombre = txtNombreMembresia.getText();
        String precioTexto = txtPrecioMembresia.getText();

        if (nombre.isEmpty() || precioTexto.isEmpty()) return;

        try {
            double precio = Double.parseDouble(precioTexto);

            if (membresiaEnEdicion == null) {
                // Crear nueva
                if(alertaUtil.confirmar("Confirmar creación", "¿Estás seguro de que quieres crear una nueva membresía?")) {
                    Membresia nuevaMembresia = new Membresia();
                    nuevaMembresia.setNombre(nombre);
                    nuevaMembresia.setPrecio(precio);
                    nuevaMembresia.setFechaCreacion(LocalDate.now());
                    membresiaService.crearMembresia(nuevaMembresia);
                }
            } else {
                // Actualizar
                if(alertaUtil.confirmar("Confirmar actualización", "¿Estás seguro de que quieres actualizar la membresía?")) {
                    membresiaEnEdicion.setNombre(nombre);
                    membresiaEnEdicion.setPrecio(precio);
                    membresiaService.actualizar(membresiaEnEdicion);
                }
                membresiaEnEdicion = null;
            }

            cancelarCrearMembresia(null);
            cargarMembresias();
        } catch (NumberFormatException e) {
            System.out.println("Precio inválido");
        }
    }
    @FXML
    private void abrirSociosDeMembresia(Membresia membresia) throws IOException {
        FXMLLoader loader = springFXMLLoader.load("/fxml/ListarSocios.fxml");
        Parent root = loader.load();

        ListarSociosController controller = loader.getController();
        controller.setMembresiaFiltrada(membresia);
        controller.cargarSocios();

        Stage nuevoStage = new Stage();
        nuevoStage.setScene(new Scene(root));
        nuevoStage.setTitle("Socios de Membresía: " + membresia.getNombre());
        nuevoStage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana anterior
        nuevoStage.show();
    }
    @FXML
    void mostrarVistaAnterior(ActionEvent event) {
        controllerUI.volverAVistaAnterior();
    }

}
