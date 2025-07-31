package com.ivan.gimnasio.ui;

//////////////////////////////////////////////////////////
//                 IMPORTACIONES
//////////////////////////////////////////////////////////

import com.ivan.gimnasio.config.app.SpringFXMLLoader;
import com.ivan.gimnasio.persistence.entity.Membresia;
import com.ivan.gimnasio.persistence.entity.Socio;
import com.ivan.gimnasio.presentation.controller.ListarSociosController;
import com.ivan.gimnasio.presentation.controller.SocioCardController;
import com.ivan.gimnasio.service.interfaces.IAsistenciaService;
import com.ivan.gimnasio.service.interfaces.IMembresiaService;
import com.ivan.gimnasio.service.interfaces.ISocioService;
import com.ivan.gimnasio.util.AlertaUtil;
import com.ivan.gimnasio.util.EstadoCuota;
import com.ivan.gimnasio.util.VentanaUtil;
import javafx.animation.FadeTransition;
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
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @FXML private BorderPane rootPane;
    @FXML private VBox subMenuTarjetas;
    @FXML private VBox subMenuSocios;
    AlertaUtil alertaUtil;
    @Autowired private SpringFXMLLoader springFXMLLoader;
    @Autowired private ApplicationContext context;
    @Autowired private IMembresiaService membresiaService;
    @Autowired private IAsistenciaService asistenciaService;
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
                try {
                    asistenciaService.registrarAsistencia(socioSeleccionado);
                    AlertaUtil.mostrarInfo("✅ Asistencia registrada correctamente.");
                    Socio socioActualizado = socioService.buscarSocioPorDni(socioSeleccionado.getDni());
                    mostrarFichaSocio(socioActualizado);
                } catch (IllegalStateException e) {
                    AlertaUtil.mostrarError("⚠️ " + e.getMessage());
                }
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
        try {
            asistenciaService.registrarAsistencia(socio);
            AlertaUtil.mostrarInfo("✅ Asistencia registrada correctamente.");
            Socio socioActualizado = socioService.buscarSocioPorDni(socio.getDni());
            mostrarFichaSocio(socioActualizado);
        } catch (IllegalStateException e) {
            AlertaUtil.mostrarError("⚠️ " + e.getMessage());
        }
// Y lo usás
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
    void mostrarAsistencias(ActionEvent event) throws IOException {
        // Abre la ventana de listado de socios en una nueva Stage
        VentanaUtil.abrirModal(springFXMLLoader, "/fxml/ListarAsistencias.fxml", "Asistencias de Socios", null);
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
            VentanaUtil.abrirModal(springFXMLLoader, "/fxml/SocioCard.fxml", "Ficha de Socio", controller -> {
                ((SocioCardController) controller).setSocio(socio);
            });
        }catch (Exception e) {
            AlertaUtil.mostrarError("Error al cargar la ficha del socio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void mostrarSocios(ActionEvent event) throws IOException {
        // Abre la ventana de listado de socios en una nueva Stage
        VentanaUtil.abrirModal(springFXMLLoader, "/fxml/ListarSocios.fxml", "Listado de Socios", null);
    }
    @FXML
    public void listarSociosConCuotaVencida() throws IOException {
        VentanaUtil.abrirModal(springFXMLLoader, "/fxml/ListarSocios.fxml", "Socios con cuota vencida", controller -> {
            ListarSociosController listarController = (ListarSociosController) controller;
            listarController.setFiltroEstadoCuota(EstadoCuota.VENCIDA);
            listarController.cargarSocios();
        });
    }
    @FXML
    public void mostrarTarjeta() throws IOException {
       toggleSubMenu(subMenuTarjetas);
    }
    @FXML
    public void mostrarMenuSocios() throws IOException {
        toggleSubMenu(subMenuSocios);
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
        LocalDate inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime desdeSemana = inicioSemana.atStartOfDay();
        LocalDateTime hastaSemana = LocalDate.now().atTime(LocalTime.MAX);

        List<LocalDate> diasAsistidos = asistenciaService.obtenerDiasAsistidosSemana(socio.getDni(), desdeSemana, hastaSemana);
        int asistenciasSemana = diasAsistidos.size();
        Set<Membresia> membresiasParaContar = socio.getMembresias();
        int limiteSemanal = membresiasParaContar.stream()
                .mapToInt(Membresia::getLimiteSemanal)
                .max()
                .orElse(5);

        int asistenciasHoy = asistenciaService.contarAsistenciasHoy(socio.getDni());
        Label asistenciasSemanaLabel = new Label("Asistencias esta semana: " + asistenciasSemana + "/" + limiteSemanal);
        asistenciasSemanaLabel.getStyleClass().add("label-info");
        info.getChildren().add(asistenciasSemanaLabel);
        if (asistenciasHoy > 1) {
            Label asistenciasHoyLabel = new Label("Asistencias hoy: " + asistenciasHoy);
            asistenciasHoyLabel.getStyleClass().add("label-info");
            info.getChildren().add(asistenciasHoyLabel);
        }
        info.getChildren().add(estadoLabel);
        contenedorCentrado.getChildren().add(card);
    }
    private void toggleSubMenu(VBox subMenu) {
        boolean visible = subMenu.isVisible();
        FadeTransition fade = new FadeTransition(Duration.millis(150), subMenu);

        if (visible) {
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                subMenu.setVisible(false);
                subMenu.setManaged(false);
            });
        } else {
            subMenu.setManaged(true);
            subMenu.setVisible(true);
            fade.setFromValue(0);
            fade.setToValue(1);
        }

        fade.play();
    }
    
    //////////////////////////////////////////////////////////
    //              BEAN SPRING DE PRUEBA                  ///
    //////////////////////////////////////////////////////////

    @Bean
    String titulo() {
        return "StrongFit";
    }
}
