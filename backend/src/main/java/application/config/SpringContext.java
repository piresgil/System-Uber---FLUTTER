package application.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

/**
 * Classe responsável por integrar o contexto do Spring com JavaFX.
 * Permite carregar FXML com controladores gerenciados pelo Spring.
 */
@Component
public class SpringContext {
    private static SpringContext instance;
    private final ApplicationContext context;

    /**
     * Construtor que recebe o contexto do Spring e armazena a instância da classe.
     */
    public SpringContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
        instance = this; // Armazena a instância para uso estático
    }

    /**
     * Retorna a instância estática do contexto.
     */
    public static SpringContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SpringContext ainda não foi inicializado!");
        }
        return instance;
    }

    /**
     * Retorna o contexto do Spring.
     */
    public ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Obtém um bean gerenciado pelo Spring, dado o seu tipo.
     */
    public static <T> T getBean(Class<T> beanClass) {
        return getInstance().context.getBean(beanClass);
    }

    /**
     * Carrega um arquivo FXML a partir de uma URL e injeta os controladores do Spring.
     */
    public Parent load(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }

    /**
     * Carrega um arquivo FXML a partir de um caminho de String e injeta os controladores do Spring.
     */
    public Parent load(String fxmlPath) throws IOException {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("Arquivo FXML não encontrado: " + fxmlPath);
        }
        return load(url);
    }
}