package application.controllers;

import application.model.Cartao;
import application.model.Carro;
import application.model.TipoCartao;
import application.services.CartaoService;
import application.services.CarroService;
import application.util.AlertUtils;
import application.util.FormUtils;
import application.util.LogUtils;
import application.util.ValidationUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Controller JavaFX responsável pela gestão dos Cartões.
 * Segue o mesmo padrão profissional do CarroController.
 */
@Component
public class CartaoController {

    @FXML
    private AnchorPane apPrincipal;

    @FXML
    private ComboBox<TipoCartao> comboBoxTipo;
    @FXML
    private TextField txtCartaoNr;
    @FXML
    private TextField txtContratoNr;
    @FXML
    private TextField txtNome;
    @FXML
    private ComboBox<Carro> comboBoxCarro;

    @FXML
    private Button btRegistar;
    @FXML
    private Button btEditar;
    @FXML
    private Button btGravar;
    @FXML
    private Button btEliminar;

    @FXML
    private TableView<Cartao> tableView;
    @FXML
    private TableColumn<Cartao, String> tipoColumn;
    @FXML
    private TableColumn<Cartao, String> cartaoNrColumn;
    @FXML
    private TableColumn<Cartao, String> contratoColumn;
    @FXML
    private TableColumn<Cartao, String> nomeColumn;
    @FXML
    private TableColumn<Cartao, String> carroColumn;

    private final ObservableList<Cartao> listaCartoes = FXCollections.observableArrayList();
    private final ObservableList<Carro> listaCarros = FXCollections.observableArrayList();

    private Cartao selecionado;
    private boolean editMode;

    private final CartaoService cartaoService;
    private final CarroService carroService;

    @Autowired
    public CartaoController(CartaoService cartaoService, CarroService carroService) {
        this.cartaoService = cartaoService;
        this.carroService = carroService;
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarCombos();
        carregarCartoes();
        resetarBotoes();
        configurarListenersDeValidacao();

        // Listener que ativa o botão Editar quando há seleção
        FormUtils.configurarListenerSelecao(tableView, btEditar);

    }

    /**
     * Configura as colunas da tabela.
     */
    private void configurarTabela() {

        tipoColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getTipo().name()));

        cartaoNrColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getNumero()));

        contratoColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getContrato()));

        nomeColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getNome()));

        carroColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        c.getValue().getCarro() != null ? c.getValue().getCarro().getMatricula() : ""
                ));

        tableView.setItems(listaCartoes);
    }

    /**
     * Carrega os valores dos ComboBox.
     */
    private void carregarCombos() {
        comboBoxTipo.setItems(FXCollections.observableArrayList(TipoCartao.values()));
        listaCarros.setAll(carroService.findAll());
        comboBoxCarro.setItems(listaCarros);
    }

    /**
     * Carrega os cartões do banco.
     */
    private void carregarCartoes() {
        listaCartoes.setAll(cartaoService.findAll());
    }

    /**
     * Regista um novo cartão.
     */
    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;

        Cartao cartao = FormUtils.capturarEntidadeDoFormulario(new Cartao(), entidade());
        if (cartao == null) return;

        cartao.setCarro(comboBoxCarro.getValue());

        String erros = ValidationUtils.validarEntidade(cartao);
        if (erros != null) {
            AlertUtils.showErrorAlert("Erro", erros);
            return;
        }

        try {
            Cartao salvo = cartaoService.insert(cartao);
            listaCartoes.add(salvo);

            AlertUtils.showSuccessAlert("Sucesso", "Cartão registado com sucesso!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao registar cartão", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Entra no modo de edição.
     */
    @FXML
    protected void onclickOnEditar() {
        if (editMode) {
            cancelarEdicao();
            return;
        }

        selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        comboBoxTipo.setValue(selecionado.getTipo());
        txtCartaoNr.setText(selecionado.getNumero());
        txtContratoNr.setText(selecionado.getContrato());
        txtNome.setText(selecionado.getNome());
        comboBoxCarro.setValue(selecionado.getCarro());

        editMode = true;
        btEditar.setText("Voltar");
        btGravar.setDisable(false);
        btRegistar.setDisable(true);
        btEliminar.setDisable(false);
    }

    /**
     * Grava alterações no cartão selecionado.
     */
    @FXML
    protected void onclickOnGravar() {
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Editar Cartão",
                "Deseja realmente editar este cartão?"
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            selecionado = FormUtils.capturarEntidadeDoFormulario(selecionado, entidade());
            selecionado.setCarro(comboBoxCarro.getValue());

            cartaoService.update(selecionado.getId(), selecionado);
            FormUtils.atualizarListaNaTabela(listaCartoes, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Cartão atualizado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao atualizar cartão", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Elimina um cartão.
     */
    @FXML
    protected void onclickOnEliminar() {
        Cartao selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Eliminar Cartão",
                "Deseja realmente eliminar este cartão?"
        );

        if (!confirmar) return;

        try {
            cartaoService.delete(selecionado.getId());
            listaCartoes.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Cartão eliminado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao eliminar cartão", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    /**
     * Cancela o modo de edição.
     */
    private void cancelarEdicao() {
        selecionado = null;
        editMode = false;

        tableView.getSelectionModel().clearSelection();
        carregarCartoes();
        tableView.refresh();

        resetarBotoes();
    }

    /**
     * Reseta botões e limpa o formulário.
     */
    private void resetarBotoes() {
        btGravar.setDisable(true);
        btEliminar.setDisable(true);
        btRegistar.setDisable(true);

        // O botão Editar só deve ficar ativo quando houver seleção na tabela
        btEditar.setDisable(true);
        btEditar.setText("Editar");

        editMode = false;

        FormUtils.limparFormulario(apPrincipal);

        verificarCamposPreenchidos();
    }


    /**
     * Listeners para ativar/desativar botões conforme preenchimento.
     */
    private void configurarListenersDeValidacao() {
        txtCartaoNr.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtContratoNr.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtNome.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());

        comboBoxTipo.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        comboBoxCarro.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
    }

    /**
     * Verifica se todos os campos obrigatórios estão preenchidos.
     */
    private void verificarCamposPreenchidos() {
        boolean preenchido =
                comboBoxTipo.getValue() != null &&
                        !txtCartaoNr.getText().isBlank() &&
                        !txtContratoNr.getText().isBlank() &&
                        !txtNome.getText().isBlank() &&
                        comboBoxCarro.getValue() != null;

        btRegistar.setDisable(!preenchido);

        if (editMode) {
            btGravar.setDisable(!preenchido);
        }
    }

    /**
     * Mapeia os campos do formulário para o FormUtils.
     */
    private Map<String, Control> entidade() {
        return Map.of(
                "tipo", comboBoxTipo,
                "numero", txtCartaoNr,
                "contrato", txtContratoNr,
                "nome", txtNome
        );
    }
}
