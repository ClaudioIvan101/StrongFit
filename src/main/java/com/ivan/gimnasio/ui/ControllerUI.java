package com.ivan.gimnasio.ui;

//////////////////////////////////////////////////////////
//                 IMPORTACIONES
//////////////////////////////////////////////////////////

import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.presentation.controller.ListarSociosController;
import com.ivan.gimnasio.presentation.controller.SocioCardController;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import com.ivan.gimnasio.util.AlertaUtil;
import com.ivan.gimnasio.util.EstadoCuota;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import java.net.URL;
import java.util.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;


//////////////////////////////////////////////////////////
//                 CONTROLADOR PRINCIPAL
//////////////////////////////////////////////////////////

@Controller
public class ControllerUI {

    //////////////////////////////////////////////////////////
    //        INYECCIÓN DE DEPENDENCIAS Y FXML
    //////////////////////////////////////////////////////////

    @FXML private StackPane contentPane;               // Área donde se cargan vistas
    @FXML private AnchorPane principal;                // Panel raíz de la vista
    @FXML private StackPane contenedorCentrado;        // Donde se cargan tarjetas con info

    @FXML public TextField txtNombre;
    @FXML public TextField txtDni;
    @FXML public TextField txtEmail;
    @FXML public TextField txtTelefono;
    @FXML public TextField txtApellido;
    @FXML private ListView<Socio> listaResultados;
    @FXML private TextField buscarSocioPorDni;
    @FXML private TextField buscarSocioPorFicha;
    @FXML private ListView<Membresia> listViewMembresias;
    @FXML
    private BorderPane rootPane;
    AlertaUtil alertaUtil;
    @Autowired private SpringFXMLLoader springFXMLLoader;
    @Autowired private ApplicationContext context;
    @Autowired private IMembresiaService membresiaService;

    private final ISocioService socioService;
    private StackPane vistaAnterior;

    // Constructor con inyección manual de ISocioService
    public ControllerUI(ISocioService socioService) {
        this.socioService = socioService;
    }
    //////////////////////////////////////////////////////////
    //              ACCIONES DE BOTONES
    //////////////////////////////////////////////////////////

    @FXML
    public void initialize() {
        buscarSocioPorDni.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                listaResultados.getItems().clear();
                return;
            }
            List<Socio> resultados = socioService.buscarSociosPorCriterio(newVal);
            listaResultados.getItems().setAll(resultados);
        });

        listaResultados.setOnMouseClicked(event -> {
            Socio socioSeleccionado = listaResultados.getSelectionModel().getSelectedItem();
            if (socioSeleccionado != null) {
                mostrarFichaSocio(socioSeleccionado);
            }
        });
        listaResultados.setCellFactory(lv -> new ListCell<Socio>() {
            @Override
            protected void updateItem(Socio socio, boolean empty) {
                super.updateItem(socio, empty);
                if (empty || socio == null) {
                    setText(null);
                    getStyleClass().remove("celda-socio");
                } else {
                    setText(socio.getNombre() + " " + socio.getApellido() + " - " + socio.getDni());
                    if (!getStyleClass().contains("celda-socio")) {
                        getStyleClass().add("celda-socio");
                    }
                }
            }
        });
    }
    @FXML
    void buscarSocioPorDni(ActionEvent event) {
        String dni = buscarSocioPorDni.getText();
        Optional<Socio> socioOpt = Optional.ofNullable(socioService.buscarSocioPorDni(dni));

        if (socioOpt.isEmpty()) {
            AlertaUtil.mostrarError("No se encontró un socio con ese DNI.");
            return;
        }

        Socio socio = socioOpt.get();

        // Si está dado de baja
        if (!socio.isActivo()) {
            if(AlertaUtil.confirmar("Restaurar Membresía","El socio " + socio.getNombre() + " " + socio.getApellido() +
                    " está dado de baja. ¿Desea restaurar su membresía?")) {
                // Restaurar membresía
                socioService.restaurarSocio(socio);
                AlertaUtil.mostrarInfo("Membresía restaurada correctamente.");
            } else {
                return; // Si no se confirma, no se muestra la ficha
            }
        }
            mostrarFichaSocio(socio);     // Y lo usás
    }
    @FXML
    void restaurarMembresias(ActionEvent event) {
        membresiaService.restaurarTodasLasMembresias();
    }

    //////////////////////////////////////////////////////////
    //              MÉTODOS PARA CARGAR VISTAS
    //////////////////////////////////////////////////////////

    @FXML
    public void mostrarRegistrarSocio() throws IOException {
        cargarVista("RegistrarSocio.fxml");
    }

    @FXML
    void buscarSocioPorFicha(ActionEvent event) throws IOException {
        // Obtiene el socio seleccionado de la tabla
        String dni = buscarSocioPorFicha.getText();
        if (dni == null || dni.isBlank()) {
            AlertaUtil.mostrarError("Ingresá un DNI.");
            return;
        }
        Socio socio = socioService.buscarSocioPorDni(dni);
        if (socio == null) {
            AlertaUtil.mostrarError("No se encontró un socio con ese DNI.");
            return;
        }
        // Carga la vista de ficha de socio
        try {
            FXMLLoader loader = springFXMLLoader.load("/fxml/SocioCard.fxml");
            Parent root = loader.load();

            SocioCardController controller = loader.getController();
            controller.setSocio(socio);

            // cargar el modal

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ficha de Socio: " + socio.getNombre() + " " + socio.getApellido());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e) {
            AlertaUtil.mostrarError("Error al cargar la ficha del socio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void mostrarSocios(ActionEvent event) throws IOException {
        // Abre la ventana de listado de socios en una nueva Stage
        FXMLLoader loader = springFXMLLoader.load("/fxml/listarSocios.fxml");
        Parent root = loader.load(); // importante: llamar a .load() después
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Lista de Socios");
        stage.setResizable(false);
        stage.show();
    }
    @FXML
    public void listarSociosConCuotaVencida() throws IOException {
        FXMLLoader loader = springFXMLLoader.load("/fxml/ListarSocios.fxml");
        Parent root = loader.load();

        ListarSociosController controller = loader.getController();
        controller.setFiltroEstadoCuota(EstadoCuota.VENCIDA);
        controller.cargarSocios();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Socios con cuota vencida");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
    @FXML
    public void mostrarTarjeta() throws IOException {

    }

    @FXML
    public void mostrarListaSocios() throws IOException {
        cargarVista("ListarSocios.fxml");
    }
    @FXML
    public void mostrarMembresias() throws IOException {
        cargarVista("MembresiaViews.fxml");
    }

    // Carga vista en el StackPane `contentPane`
    public void cargarVista(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
        loader.setControllerFactory(context::getBean); // Inyección Spring
        Parent root = loader.load();

        // Vincula controladores secundarios si es necesario
        Object controller = loader.getController();
        if (controller instanceof SocioControllerUI) {
            ((SocioControllerUI) controller).setControllerUI(this);
        }

        // Guarda vista anterior para poder volver
        if (!contentPane.getChildren().isEmpty()) {
            vistaAnterior = new StackPane();
            vistaAnterior.getChildren().addAll(contentPane.getChildren());
        }
        contentPane.getChildren().setAll(root);
    }

    // Permite volver a la vista anterior
    public void volverAVistaAnterior() {
        if (vistaAnterior != null) {
            contentPane.getChildren().setAll(vistaAnterior.getChildren());
        }
    }

    // Versión alternativa para cargar una vista en el AnchorPane principal
    public void cargarVistaDos(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
        loader.setControllerFactory(context::getBean);
        Parent vista = loader.load();

        principal.getChildren().clear();
        principal.getChildren().setAll(vista);

        // Anclaje automático a todos los bordes
        AnchorPane.setTopAnchor(vista, 0.0);
        AnchorPane.setBottomAnchor(vista, 0.0);
        AnchorPane.setLeftAnchor(vista, 0.0);
        AnchorPane.setRightAnchor(vista, 0.0);
    }
    public void mostrarFichaSocio(Socio socio) {
        Socio socioCompleto = socioService.buscarSocioPorDni(socio.getDni());
        if (socioCompleto == null) {
            AlertaUtil.mostrarError("No se pudo cargar la información del socio.");
            return;
        }
        contenedorCentrado.getChildren().clear();

        Label nombre = new Label("Nombre: " + socio.getNombre() + " " + socio.getApellido());
        nombre.getStyleClass().add("label-nombre");

        Label dniLabel = new Label("DNI: " + socio.getDni());
        dniLabel.getStyleClass().add("label-info");

        Label fecha = new Label("Vence: " + socio.getFechaInscripcion().plusDays(30));
        fecha.getStyleClass().add("label-info");

        Set<Membresia> membresias = socio.getMembresias();
        String actividadesTexto = membresias.isEmpty()
                ? "Sin actividades"
                : membresias.stream().map(Membresia::getNombre).collect(Collectors.joining(", "));

        Label actividad = new Label("Actividades: " + actividadesTexto);
        actividad.getStyleClass().add("label-info");

        VBox info = new VBox(8, nombre, dniLabel, fecha, actividad);
        info.setAlignment(Pos.CENTER_LEFT);

        ImageView foto = new ImageView(new Image("/img/iconPerson.png"));
        foto.setFitWidth(100);
        foto.setFitHeight(100);

        HBox card = new HBox(20, foto, info);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(400);
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));
        card.getStyleClass().add("card");

        EstadoCuota estado = socio.getEstadoCuota();
        Label estadoLabel = new Label(estado == EstadoCuota.AL_DIA ? "✔ Cuota al día" : "✖ Cuota vencida");
        estadoLabel.getStyleClass().add("label-estado");

        if (estado == EstadoCuota.AL_DIA) {
            card.getStyleClass().add("card-al-dia");
            estadoLabel.getStyleClass().add("estado-al-dia");
        } else {
            card.getStyleClass().add("card-vencida");
            estadoLabel.getStyleClass().add("estado-vencida");
        }

        info.getChildren().add(estadoLabel);
        contenedorCentrado.getChildren().add(card);
    }

    //////////////////////////////////////////////////////////
    //              BEAN SPRING DE PRUEBA                  ///
    //////////////////////////////////////////////////////////

    @Bean
    String titulo() {
        return "StrongFit";
    }
}
