/**
 * Controlador responsável pela gestão de usuários na aplicação.
 * Permite registrar, editar, excluir e visualizar usuários.
 *
 * @author Daniel Gil
 */
package application.controllers;

import application.model.User;
import application.services.UserService;
import application.util.AlertUtils;
import application.util.FormUtils;
import application.util.LogUtils;
import application.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component // Define o controlador como um componente do Spring
public class UserController {
    // AnchorPane principal
    @FXML
    public AnchorPane apPrincipal;

    // Botões do formulário
    @FXML
    private Button btRegistar, btEditar, btGravar, btEliminar;

    // Campos de entrada
    @FXML
    private TextField txtNome, txtEmail, txtPassword;

    // Tabela
    @FXML
    private TableView<User> tableView;

    // Lista observável para atualizar a tabela dinamicamente
    private final ObservableList<User> listaUser = FXCollections.observableArrayList();

    // Variável para armazenar o usuário em edição
    private User selecionado;

    // Injeção de dependências para serviços do usuário e criptografia de senha
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // variavel de control para modo ediçaão
    private boolean editMode;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @FXML
    public void initialize() {
        carregarDadosIniciais();
        configurarComponentes();
        // Desativa botões que não devem ser usados inicialmente
        resetarBotoes();
    }

    private void carregarDadosIniciais() {
        btRegistar.setDisable(true);
        btEditar.setDisable(true);
        editMode = false;

        FormUtils.configurarListeners(() -> editMode, btRegistar, txtNome, txtEmail, txtPassword);

    }

    private void configurarComponentes() {
        // Configuração das colunas da tabela
        FormUtils.configurarTabela(tableView, List.of("nome", "email", "password"), User.class);
        // Configurar listeners para os campos
        FormUtils.configurarListenerSelecao(tableView, btEditar);
        // Carrega os usuários do banco de dados
        carregarUsuarios();
        tableView.setItems(listaUser);
    }

    /**
     * Regista um novo User no sistema.
     */
    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;
        User user = FormUtils.capturarEntidadeDoFormulario(
                new User(), entidade());

        if (user == null) return;

        // Validação usando Bean Validation
        String errosValidacao = ValidationUtils.validarEntidade(user);
        if (errosValidacao != null) {
            AlertUtils.showErrorAlert("Erro", errosValidacao);
            return;
        }

        // Verifica se o e-mail já está registrado
        if (ValidationUtils.isEmailDuplicado(user.getEmail(), userService::existsByEmail)) {
            AlertUtils.showErrorAlert("Erro", "Este e-mail já está em uso.");
            return;
        }

        try {
            // Encripta a senha antes de salvar
            // user.setPassword(passwordEncoder.encode(user.getPassword()));

            User userSalvo = userService.insert(user);
            listaUser.add(userSalvo);

            AlertUtils.showSuccessAlert("Sucesso", "Utilizador " + userSalvo.getNome() + " registado com sucesso!");

            cancelarEdicao();

        } catch (Exception e) {
            LogUtils.logError("Erro ao registar utilizador", e); // Registra a exceção
            AlertUtils.showErrorAlert("Erro ao registar", e.getMessage());
        }
    }

    /**
     * Prepara a edição do User selecionado, carregando seus dados nos campos de entrada.
     */
    @FXML
    protected void onclickOnEditar() {
        if (editMode) {
            cancelarEdicao(); // Se já está editando, cancela a edição
            btEditar.setText("Editar"); // Volta ao estado original
        } else {
            selecionado = tableView.getSelectionModel().getSelectedItem();

            if (selecionado != null) {
                txtNome.setText(selecionado.getNome());
                txtEmail.setText(selecionado.getEmail());

                // Para evitar exibir a senha, futuramente usar esta linha:
                // txtPassword.clear();

                txtPassword.setText(selecionado.getPassword()); // Apenas para testes

                editMode = true;
                btEditar.setText("Voltar"); // Altera para cancelar
                btGravar.setDisable(false);
                btRegistar.setDisable(true); // Desativa Registar em modo de edição
                btEliminar.setDisable(false);

                FormUtils.configurarListeners(() -> editMode, btRegistar, txtNome, txtEmail, txtPassword);
            }
        }
    }

    /**
     * Salva as alterações feitas no user selecionado.
     */
    @FXML
    protected void onclickOnGravar() {

        if (!editMode) cancelarEdicao();

        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Editar Usuário " + selecionado.getNome(),
                "Tem certeza que deseja editar este usuário? " + selecionado.getNome()
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            selecionado = FormUtils.capturarEntidadeDoFormulario(selecionado, entidade());

            // Verifica se o e-mail já está registrado, ignorando o próprio usuário em edição
            assert selecionado != null;
            if (ValidationUtils.isEmailDuplicado(selecionado.getEmail(), selecionado.getId(), userService::existsByEmailAndIdNot)) {
                AlertUtils.showErrorAlert("Erro", "Este e-mail já está em uso.");
                return;
            }

            // Atualiza o usuário no serviço
            userService.update(selecionado.getId(), selecionado);

            // Atualiza a lista na tabela
            FormUtils.atualizarListaNaTabela(listaUser, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Utilizador " + selecionado.getNome() + " atualizado com sucesso!");

            resetarBotoes();

        } catch (DataIntegrityViolationException e) {
            LogUtils.logError("Erro ao atualizar utilizador (e-mail duplicado)", e); // Registra a exceção
            AlertUtils.showErrorAlert("Erro", "Este e-mail já está em uso.");
        } catch (Exception e) {
            LogUtils.logError("Erro ao atualizar utilizador", e); // Registra a exceção
            AlertUtils.showErrorAlert("Erro ao atualizar", e.getMessage());
        }
    }

    /**
     * Remove o user selecionado do sistema.
     */
    @FXML
    protected void onclickOnEliminar() {

        if (!editMode) cancelarEdicao();

        User selecionado = tableView.getSelectionModel().getSelectedItem();

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Eliminar Usuário " + selecionado.getNome(),
                "Tem certeza que deseja eliminar este usuário? " + selecionado.getNome()
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            userService.delete(selecionado.getId());
            listaUser.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Utilizador " + selecionado.getNome() + " eliminado com sucesso!");

            cancelarEdicao();

        } catch (Exception e) {
            LogUtils.logError("Erro ao eliminar utilizador", e); // Registra a exceção
            AlertUtils.showErrorAlert("Erro ao eliminar", e.getMessage());
        }
    }

    /**
     * Carrega todos os users para a tabela.
     */
    private void carregarUsuarios() {
        listaUser.setAll(userService.findAll());
    }

    /**
     * Cancela a edição do Colaborador, limpando a seleção e resetando os campos.
     * Também altera o botão Editar para o estado original.
     */
    private void cancelarEdicao() {
        selecionado = null; // Remove o colaborador em edição
        editMode = false; // Sai do modo de edição

        tableView.getSelectionModel().clearSelection(); // Limpa a seleção da tabela
        carregarUsuarios(); // 🔥 Garante que os dados são sincronizados com o repositório
        tableView.refresh(); // Atualiza a tabela

        resetarBotoes(); // Chama a função para restaurar o estado inicial
    }

    /**
     * Restaura os botões para o estado inicial após uma ação.
     */
    private void resetarBotoes() {
        btGravar.setDisable(true);
        btEliminar.setDisable(true);
        btRegistar.setDisable(true);
        btEditar.setText("Editar"); // Garante que o botão volte ao estado original
        editMode = false;
        FormUtils.limparFormulario(apPrincipal);
        selecionado = null;

        userService.limparCache();

        FormUtils.configurarListeners(() -> editMode, btRegistar, txtNome, txtEmail, txtPassword);
    }

    private Map<String, Control> entidade() {
        return Map.of(
                "nome", txtNome,
                "email", txtEmail,
                "password", txtPassword
        );
    }
}
