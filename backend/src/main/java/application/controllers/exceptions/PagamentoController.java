package application.controllers;

import application.model.*;
import application.services.ColaboradorService;
import application.services.PagamentoService;
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

import java.time.LocalDate;
import java.util.Map;

@Component
public class PagamentoController {

    @FXML private AnchorPane apPrincipal;

    @FXML private ComboBox<Colaborador> comboBoxColaborador;
    @FXML private ComboBox<TipoPagamento> comboBoxTipoPagamento;
    @FXML private ComboBox<Plataforma> comboBoxPlataforma;

    @FXML private TextField txtValor;
    @FXML private DatePicker datePickerData;

    @FXML private Button btRegistar;
    @FXML private Button btEditar;
    @FXML private Button btGravar;
    @FXML private Button btEliminar;

    @FXML private TableView<Pagamento> tableView;
    @FXML private TableColumn<Pagamento, String> colaboradorColumn;
    @FXML private TableColumn<Pagamento, String> plataformaColumn;
    @FXML private TableColumn<Pagamento, String> dataColumn;
    @FXML private TableColumn<Pagamento, String> valorColumn;
    @FXML private TableColumn<Pagamento, String> tipoColumn;

    private final ObservableList<Pagamento> listaPagamentos = FXCollections.observableArrayList();
    private final ObservableList<Colaborador> listaColaboradores = FXCollections.observableArrayList();

    private Pagamento selecionado;
    private boolean editMode;

    private final PagamentoService pagamentoService;
    private final ColaboradorService colaboradorService;

    @Autowired
    public PagamentoController(PagamentoService pagamentoService,
                               ColaboradorService colaboradorService) {
        this.pagamentoService = pagamentoService;
        this.colaboradorService = colaboradorService;
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarCombos();
        carregarPagamentos();
        resetarBotoes();
        configurarListenersDeValidacao();

        FormUtils.configurarListenerSelecao(tableView, btEditar);
    }

    private void configurarTabela() {

        colaboradorColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        c.getValue().getColaborador() != null ? c.getValue().getColaborador().getNome() : ""
                ));

        plataformaColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getPlataforma().name()));

        dataColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getData().toString()));

        valorColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.valueOf(c.getValue().getValor())));

        tipoColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getTipoPagamento().name()));

        tableView.setItems(listaPagamentos);
    }

    private void carregarCombos() {
        listaColaboradores.setAll(colaboradorService.findAll());

        comboBoxColaborador.setItems(listaColaboradores);
        comboBoxTipoPagamento.setItems(FXCollections.observableArrayList(TipoPagamento.values()));
        comboBoxPlataforma.setItems(FXCollections.observableArrayList(Plataforma.values()));
    }

    private void carregarPagamentos() {
        listaPagamentos.setAll(pagamentoService.findAll());
    }

    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;

        Pagamento pagamento = new Pagamento();
        pagamento.setColaborador(comboBoxColaborador.getValue());
        pagamento.setPlataforma(comboBoxPlataforma.getValue());
        pagamento.setTipoPagamento(comboBoxTipoPagamento.getValue());
        pagamento.setData(datePickerData.getValue());
        pagamento.setValor(Double.valueOf(txtValor.getText()));

        String erros = ValidationUtils.validarEntidade(pagamento);
        if (erros != null) {
            AlertUtils.showErrorAlert("Erro", erros);
            return;
        }

        try {
            Pagamento salvo = pagamentoService.insert(pagamento);
            listaPagamentos.add(salvo);

            AlertUtils.showSuccessAlert("Sucesso", "Pagamento registado com sucesso!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao registar pagamento", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    @FXML
    protected void onclickOnEditar() {
        if (editMode) {
            cancelarEdicao();
            return;
        }

        selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        comboBoxColaborador.setValue(selecionado.getColaborador());
        comboBoxPlataforma.setValue(selecionado.getPlataforma());
        comboBoxTipoPagamento.setValue(selecionado.getTipoPagamento());

        txtValor.setText(String.valueOf(selecionado.getValor()));
        datePickerData.setValue(selecionado.getData());

        editMode = true;
        btEditar.setText("Voltar");
        btGravar.setDisable(false);
        btRegistar.setDisable(true);
        btEliminar.setDisable(false);
    }

    @FXML
    protected void onclickOnGravar() {
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Editar Pagamento",
                "Deseja realmente editar este pagamento?"
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            selecionado.setColaborador(comboBoxColaborador.getValue());
            selecionado.setPlataforma(comboBoxPlataforma.getValue());
            selecionado.setTipoPagamento(comboBoxTipoPagamento.getValue());
            selecionado.setData(datePickerData.getValue());
            selecionado.setValor(Double.valueOf(txtValor.getText()));

            pagamentoService.update(selecionado.getId(), selecionado);
            FormUtils.atualizarListaNaTabela(listaPagamentos, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Pagamento atualizado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao atualizar pagamento", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    @FXML
    protected void onclickOnEliminar() {
        Pagamento selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Eliminar Pagamento",
                "Deseja realmente eliminar este pagamento?"
        );

        if (!confirmar) return;

        try {
            pagamentoService.delete(selecionado.getId());
            listaPagamentos.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Pagamento eliminado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao eliminar pagamento", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    private void cancelarEdicao() {
        selecionado = null;
        editMode = false;

        tableView.getSelectionModel().clearSelection();
        carregarPagamentos();
        tableView.refresh();

        resetarBotoes();
    }

    private void resetarBotoes() {
        btGravar.setDisable(true);
        btEliminar.setDisable(true);
        btRegistar.setDisable(true);

        btEditar.setDisable(true);
        btEditar.setText("Editar");

        editMode = false;

        FormUtils.limparFormulario(apPrincipal);

        verificarCamposPreenchidos();
    }

    private void configurarListenersDeValidacao() {
        txtValor.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        comboBoxColaborador.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        comboBoxPlataforma.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        comboBoxTipoPagamento.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        datePickerData.valueProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
    }

    private void verificarCamposPreenchidos() {
        boolean preenchido =
                comboBoxColaborador.getValue() != null &&
                        comboBoxPlataforma.getValue() != null &&
                        comboBoxTipoPagamento.getValue() != null &&
                        !txtValor.getText().isBlank() &&
                        datePickerData.getValue() != null;

        btRegistar.setDisable(!preenchido);

        if (editMode) {
            btGravar.setDisable(!preenchido);
        }
    }

    private Map<String, Control> entidade() {
        return Map.of(
                "valor", txtValor
        );
    }
}
