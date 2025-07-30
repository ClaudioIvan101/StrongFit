package com.ivan.gimnasio.config.app;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;

@Component
public class SpringFXMLLoader {
    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    public FXMLLoader load(String fxmlPath) {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(context::getBean);
        return loader;
    }
}
