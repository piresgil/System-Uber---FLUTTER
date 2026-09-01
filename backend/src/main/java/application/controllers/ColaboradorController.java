package application.controllers;

import application.model.Colaborador;
import application.model.TipoColaborador;
import application.services.ColaboradorService;
import application.util.AlertUtils;
import application.util.FormUtils;
import application.util.LogUtils;
import application.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Controlador JavaFX responsável pela gestão de Colaboradores.
 * Permite registar, editar, eliminar e listar colaboradores.
 */
@Component
public class ColaboradorController {

    @FXML
    private AnchorPane apPrincipal;

    @FXML
    private Button btRegistar, btEditar, btGravar, btEliminar;

    @FXML
    private TextField txtNome, txtEmail, txtTelefone;

    @FXML
    private ComboBox<TipoColaborador> cbTipo; // NOVO CAMPO

    @FXML
    private TableView<Colaborador> tableView;

    @FXML
    private TableColumn<Colaborador, String> nomeColumn, emailColumn, telefoneColumn;

    @FXML
    private TableColumn<Colaborador, TipoColaborador> tipoColumn; // NOVA COLUNA

    private final ObservableList<Colaborador> listaColaborador = FXCollections.observableArrayList();

    private Colaborador selecionado;
    private boolean editMode;

    private final ColaboradorService colaboradorService;

    @Autowired
    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @FXML
    public void initialize() {
        configurarComboBox();
        configurarTabela();
        carregarColaboradores();
        resetarBotoes();
        configurarListenersDeValidacao(); // NOVO
    }

    /**
     * Preenche o ComboBox com os valores do enum TipoColaborador.
     */
    private void configurarComboBox() {
        cbTipo.setItems(FXCollections.observableArrayList(TipoColaborador.values()));
    }

    /**
     * Configura as colunas da tabela e listeners.
     */
    private void configurarTabela() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telefoneColumn.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        FormUtils.configurarListenerSelecao(tableView, btEditar);
        tableView.setItems(listaColaborador);
    }

    /**
     * Regista um novo colaborador.
     */
    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;

        Colaborador colaborador = FormUtils.capturarEntidadeDoFormulario(
                new Colaborador(), entidade()
        );

        if (colaborador == null) return;

        colaborador.setTipo(cbTipo.getValue()); // NOVO

        String erros = ValidationUtils.validarEntidade(colaborador);
        if (erros != null) {
            AlertUtils.showErrorAlert("Erro", erros);
            return;
        }

        if (ValidationUtils.isEmailDuplicado(colaborador.getEmail(), colaboradorService::existsByEmail)) {
            AlertUtils.showErrorAlert("Erro", "Este e-mail já está em uso.");
            return;
        }

        try {
            Colaborador salvo = colaboradorService.insert(colaborador);
            listaColaborador.add(salvo);

            AlertUtils.showSuccessAlert("Sucesso", "Colaborador registado com sucesso!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao registar colaborador", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Entra ou sai do modo de edição.
     */
    @FXML
    protected void onclickOnEditar() {
        if (editMode) {
            cancelarEdicao();
            return;
        }

        selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        txtNome.setText(selecionado.getNome());
        txtEmail.setText(selecionado.getEmail());
        txtTelefone.setText(selecionado.getTelefone());
        cbTipo.setValue(selecionado.getTipo()); // NOVO

        editMode = true;
        btEditar.setText("Voltar");
        btGravar.setDisable(false);
        btRegistar.setDisable(true);
        btEliminar.setDisable(false);
    }

    /**
     * Grava alterações no colaborador selecionado.
     */
    @FXML
    protected void onclickOnGravar() {
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Editar Colaborador",
                "Deseja realmente editar este colaborador?"
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            selecionado = FormUtils.capturarEntidadeDoFormulario(selecionado, entidade());
            selecionado.setTipo(cbTipo.getValue()); // NOVO

            if (ValidationUtils.isEmailDuplicado(selecionado.getEmail(), selecionado.getId(), colaboradorService::existsByEmailAndIdNot)) {
                AlertUtils.showErrorAlert("Erro", "Este e-mail já está em uso.");
                return;
            }

            colaboradorService.update(selecionado.getId(), selecionado);
            FormUtils.atualizarListaNaTabela(listaColaborador, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Colaborador atualizado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao atualizar colaborador", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Elimina o colaborador selecionado.
     */
    @FXML
    protected void onclickOnEliminar() {
        Colaborador selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Eliminar Colaborador",
                "Deseja realmente eliminar este colaborador?"
        );

        if (!confirmar) return;

        try {
            colaboradorService.delete(selecionado.getId());
            listaColaborador.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Colaborador eliminado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao eliminar colaborador", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Carrega todos os colaboradores do serviço.
     */
    private void carregarColaboradores() {
        listaColaborador.setAll(colaboradorService.findAll());
    }

    /**
     * Cancela a edição e limpa o formulário.
     */
    private void cancelarEdicao() {
        selecionado = null;
        editMode = false;

        tableView.getSelectionModel().clearSelection();
        carregarColaboradores();
        tableView.refresh();

        resetarBotoes();
    }

    /**
     * Restaura o estado inicial dos botões e limpa o formulário.
     */
    private void resetarBotoes() {
        btGravar.setDisable(true);
        btEliminar.setDisable(true);
        btRegistar.setDisable(true);
        verificarCamposPreenchidos();
        btEditar.setText("Editar");
        editMode = false;

        FormUtils.limparFormulario(apPrincipal);
        cbTipo.getSelectionModel().clearSelection(); // NOVO

        colaboradorService.limparCache();
    }

    /**
     * Mapeia os campos do formulário para a entidade.
     */
    private Map<String, Control> entidade() {
        return Map.of(
                "nome", txtNome,
                "email", txtEmail,
                "telefone", txtTelefone,
                "tipo", cbTipo // NOVO
        );
    }

    private void verificarCamposPreenchidos() {
        boolean preenchido =
                !txtNome.getText().isBlank() &&
                        !txtEmail.getText().isBlank() &&
                        !txtTelefone.getText().isBlank() &&
                        cbTipo.getValue() != null;

        btRegistar.setDisable(!preenchido);

        // Se estiveres em modo edição, também podes ativar o Gravar
        if (editMode) {
            btGravar.setDisable(!preenchido);
        }
    }
    private void configurarListenersDeValidacao() {
        txtNome.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtEmail.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtTelefone.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        cbTipo.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
    }


}
