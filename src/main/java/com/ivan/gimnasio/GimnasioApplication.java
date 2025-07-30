package com.ivan.gimnasio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GimnasioApplication extends Application {

	public static void main(String[] args) {
		System.out.println("SpringBoot inicio correctamente");
		launch(args);
			}

	@Override
	public void start(Stage stage) throws Exception {
var context = SpringApplication.run(GimnasioApplication.class);
var fxml = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
fxml.setControllerFactory(context::getBean);
Parent root = fxml.load();
var scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/Main.css").toExternalForm());
String titulo = context.getBean("titulo", String.class);
stage.setScene(scene);
stage.setTitle(titulo);
stage.show();
	}
}
