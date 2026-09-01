package application.views;

import application.AppLauncher;
import application.config.SpringContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;


public class InicioFXML extends Application {

    private static ConfigurableApplicationContext context;


    @Override
    public void init() {
        context = new SpringApplicationBuilder(AppLauncher.class).run();
    }


    @Override
    public void start(Stage primaryStage) throws IOException {
        SpringContext loader = context.getBean(SpringContext.class);
        Stage sistemaStage = new Stage();

        // Configura o evento de fechamento da janela
        sistemaStage.setOnCloseRequest(event -> {
            System.out.println("Fechando o sistema e liberando recursos...");
            stop(); // Método para fechar conexões e liberar recursos
            primaryStage.show(); // Volta ao menu inicial
        });

        System.out.println("Abrir Sistema");

        Pane root = (Pane) loader.load(getClass().getResource("/views/inicio.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("System Uber");
        primaryStage.show();
    }

    @Override
    public void stop() {

        // Libera outros recursos, se necessário
        System.out.println("Recursos liberados com sucesso.");
        // Fecha o contexto do Spring Boot ao fechar o JavaFX
        context.close();
    }
}