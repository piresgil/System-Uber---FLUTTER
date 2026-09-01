package application.controllers;

import application.model.Carro;
import application.services.CarroService;
import application.util.AlertUtils;
import application.util.FormUtils;
import application.util.LogUtils;
import application.util.ValidationUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CarroController {

    @FXML
    private AnchorPane apPrincipal;

    @FXML
    private TextField txtMarca, txtModelo, txtMatricula, txtKilometragem;

    @FXML
    private Button btRegistar, btEditar, btGravar, btEliminar;

    @FXML
    private CheckBox cbAtivo;

    @FXML
    private TableView<Carro> tableView;

    @FXML
    private TableColumn<Carro, String> marcaColumn, modeloColumn, matriculaColumn;

    @FXML
    private TableColumn<Carro, Double> kilometragemColumn;

    @FXML
    private TableColumn<Carro, Boolean> ativoColumn;


    private final ObservableList<Carro> listaCarros = FXCollections.observableArrayList();

    private Carro selecionado;
    private boolean editMode;

    private final CarroService carroService;

    @Autowired
    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @FXML
    public void initialize() {
        configurarTabela();
        carregarCarros();
        resetarBotoes();
        configurarListenersDeValidacao();

        FormUtils.configurarListenerSelecao(tableView, btEditar);
    }

    private void configurarTabela() {

        marcaColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getMarca()));

        modeloColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getModelo()));

        matriculaColumn.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getMatricula()));

        kilometragemColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper(c.getValue().getKilometragem()));


        ativoColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().isAtivo()));

        tableView.setItems(listaCarros);

    }


    private void carregarCarros() {
        listaCarros.setAll(carroService.findAll());
    }

    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;

        Carro carro = FormUtils.capturarEntidadeDoFormulario(new Carro(), entidade());
        if (carro == null) return;


        String erros = ValidationUtils.validarEntidade(carro);
        if (erros != null) {
            AlertUtils.showErrorAlert("Erro", erros);
            return;
        }

        try {
            Carro salvo = carroService.insert(carro);
            listaCarros.add(salvo);

            AlertUtils.showSuccessAlert("Sucesso", "Carro registado com sucesso!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao registar carro", e);
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

        txtMarca.setText(selecionado.getMarca());
        txtModelo.setText(selecionado.getModelo());
        txtMatricula.setText(selecionado.getMatricula());
        txtKilometragem.setText(String.valueOf(selecionado.getKilometragem()));
        cbAtivo.setSelected(selecionado.isAtivo());

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
                "Editar Carro",
                "Deseja realmente editar este carro?"
        );

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {
            selecionado = FormUtils.capturarEntidadeDoFormulario(selecionado, entidade());
            selecionado.setAtivo(cbAtivo.isSelected());

            carroService.update(selecionado.getId(), selecionado);
            FormUtils.atualizarListaNaTabela(listaCarros, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Carro atualizado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao atualizar carro", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    @FXML
    protected void onclickOnEliminar() {
        Carro selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert(
                "Confirmação",
                "Eliminar Carro",
                "Deseja realmente eliminar este carro?"
        );

        if (!confirmar) return;

        try {
            carroService.delete(selecionado.getId());
            listaCarros.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Carro eliminado!");
            resetarBotoes();

        } catch (Exception e) {
            LogUtils.logError("Erro ao eliminar carro", e);
            AlertUtils.showErrorAlert("Erro", e.getMessage());
        }
    }

    private void cancelarEdicao() {
        selecionado = null;
        editMode = false;

        tableView.getSelectionModel().clearSelection();
        carregarCarros();
        tableView.refresh();

        resetarBotoes();
    }

    private void resetarBotoes() {
        btGravar.setDisable(true);
        btEliminar.setDisable(true);
        btRegistar.setDisable(true);

        // O botão Editar só deve ficar ativo quando houver seleção na tabela
        btEditar.setDisable(true);
        btEditar.setText("Editar");
        editMode = false;

        FormUtils.limparFormulario(apPrincipal);
        cbAtivo.setSelected(false);

        verificarCamposPreenchidos();
    }

    private void configurarListenersDeValidacao() {
        txtMarca.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtModelo.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtMatricula.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
        txtKilometragem.textProperty().addListener((obs, oldV, newV) -> verificarCamposPreenchidos());
    }

    private void verificarCamposPreenchidos() {
        boolean preenchido =
                !txtMarca.getText().isBlank() &&
                        !txtModelo.getText().isBlank() &&
                        !txtMatricula.getText().isBlank() &&
                        !txtKilometragem.getText().isBlank();

        btRegistar.setDisable(!preenchido);

        if (editMode) {
            btGravar.setDisable(!preenchido);
        }
    }

    private Map<String, Control> entidade() {
        return Map.of(
                "marca", txtMarca,
                "modelo", txtModelo,
                "matricula", txtMatricula,
                "kilometragem", txtKilometragem
        );
    }
}
