package application.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Classe utilitária para exibição de alertas na interface gráfica (JavaFX).
 * Essa classe contém métodos estáticos para mostrar mensagens de erro,
 * sucesso e confirmação ao utilizador.
 */
public class AlertUtils {

    /**
     * Exibe um alerta de erro com um título e uma mensagem.
     * @param title O título da janela do alerta.
     * @param message A mensagem de erro a ser exibida.
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Criar alerta do tipo erro
        alert.setTitle(title); // Define o título
        alert.setHeaderText(null); // Remove cabeçalho
        alert.setContentText(message); // Define o conteúdo da mensagem
        alert.showAndWait(); // Exibe o alerta e aguarda interação do utilizador
    }

    /**
     * Exibe um alerta de sucesso/informação com um título e uma mensagem.
     * @param title O título da janela do alerta.
     * @param message A mensagem informativa a ser exibida.
     */
    public static void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Criar alerta do tipo informação
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de confirmação para o utilizador e retorna a escolha.
     * @param title O título da janela do alerta.
     * @param header Texto do cabeçalho do alerta.
     * @param message A mensagem de confirmação a ser exibida.
     * @return true se o utilizador clicar em "OK", false caso contrário.
     */
    public static boolean showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Criar alerta do tipo confirmação
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        Optional<ButtonType> resultado = alert.showAndWait(); // Aguarda a resposta do utilizador
        return resultado.isPresent() && resultado.get() == ButtonType.OK; // Retorna true se o utilizador confirmar
    }
}
