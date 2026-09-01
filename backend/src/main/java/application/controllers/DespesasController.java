package application.controllers;

import application.model.*;
import application.repositories.CarroRepository;
import application.repositories.CartaoRepository;
import application.repositories.ColaboradorRepository;
import application.services.DespesaService;
import application.services.exceptions.DatabaseException;
import application.util.AlertUtils;
import application.util.FormUtils;
import application.util.LogUtils;
import application.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Controller
public class DespesasController {

    @FXML
    private AnchorPane apPrincipal;

    @FXML
    private Button btRegistar, btEditar, btGravar, btEliminar;

    @FXML
    private ComboBox<Cartao> comboBoxCartao;

    @FXML
    private ComboBox<Carro> comboBoxCarro;

    @FXML
    private ComboBox<Colaborador> comboBoxMotorista;

    @FXML
    private TextField txtNome, txtDescricao, txtValor, txtQuantidade, txtUnidade;

    @FXML
    private DatePicker txtData;

    @FXML
    private AnchorPane paneAbastecimento;
    boolean mostrarPane;

    @FXML
    private TableView<Despesa> tableView;

    private final ObservableList<Despesa> listaDespesa = FXCollections.observableArrayList();
    private final ObservableList<Cartao> listaDeCartoes = FXCollections.observableArrayList();

    private Despesa selecionado;
    private boolean editMode;

    private final DespesaService despesaService;
    private final CarroRepository carroRepository;
    private final CartaoRepository cartaoRepository;
    private final ColaboradorRepository colaboradorRepository;

    @Autowired
    public DespesasController(DespesaService despesaService, CarroRepository carroRepository, CartaoRepository cartaoRepository, ColaboradorRepository colaboradorRepository) {
        this.despesaService = despesaService;
        this.carroRepository = carroRepository;
        this.cartaoRepository = cartaoRepository;
        this.colaboradorRepository = colaboradorRepository;
    }

    @FXML
    public void initialize() {
        carregarDadosIniciais();
        configurarComponentes();
        configurarListeners();
        resetarBotoes();
    }

    private void carregarDadosIniciais() {
        btRegistar.setDisable(true);
        btEditar.setDisable(true);
        editMode = false;

        paneAbastecimento.getChildren().forEach(node -> node.setVisible(mostrarPane));
        FormUtils.configurarListeners(() -> editMode, btRegistar,
                comboBoxCartao, txtNome, txtDescricao, comboBoxCarro, comboBoxMotorista, txtData, txtValor);
        carregarCartoes();
        carregarCarros();
        carregarMotorista();
        carregarDespesas();
    }

    private void configurarComponentes() {
        txtData.setValue(LocalDate.now());
        // Configura as colunas da tabela
        //DespesaUtil.configurarColunasTabela(cartaoColumn, cartaoTipoColumn, nomeColumn, descricaoColumn, carroColumn, motoristaColumn, dataColumn, valorColumn, quantidadeColumn, unidadeColumn);
        // Configuração das colunas da tabela
        FormUtils.configurarTabela(tableView, List.of("cartao", "nome", "descricao", "carro", "motorista", "data", "valor", "quantidade", "unidade"), Despesa.class);

        configurarComboboxes();
        tableView.setItems(listaDespesa);
    }

    private void configurarComboboxes() {
        // Configuração do ComboBox de Cartões
        comboBoxCartao.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cartao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNumero() + ", " + item.getNome()); // Exibe o número do cartão
                }
            }
        });

        comboBoxCartao.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cartao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNumero() + ", " + item.getNome()); // Exibe o número do cartão
                }
            }
        });

        // Teste: Verifique se os itens estão sendo carregados
        comboBoxCartao.setItems(FXCollections.observableArrayList(listaDeCartoes));
        System.out.println("Itens no ComboBox de Cartões: " + comboBoxCartao.getItems().size());


        // Configuração do ComboBox de Carros
        comboBoxCarro.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Carro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMarca() + " " + item.getModelo()); // Exibe marca e modelo
                }
            }
        });

        comboBoxCarro.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Carro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMarca() + " " + item.getModelo()); // Exibe marca e modelo
                }
            }
        });

        // Configuração do ComboBox de Motoristas
        comboBoxMotorista.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Colaborador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome()); // Exibe o nome do motorista
                }
            }
        });

        comboBoxMotorista.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Colaborador item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome()); // Exibe o nome do motorista
                }
            }
        });

    }

    private void configurarListeners() {

        FormUtils.configurarListenerSelecao(tableView, btEditar);


        comboBoxCartao.valueProperty().addListener((observable, oldValue, newValue) -> {

            System.out.println("Tipo do cartão selecionado: " + (newValue != null ? newValue.getTipo() : "Nulo"));

            System.out.println("Valor selecionado no ComboBox de Cartões: " + (newValue != null ? newValue.getNumero() : "Nulo"));
            System.out.println("Tipo do cartão selecionado: " + (newValue != null ? newValue.getTipo() : "Nulo"));

            if (newValue != null) {
                selecionarCarroPorCartao(newValue);

                if (txtQuantidade != null && txtUnidade != null) {
                    mostrarPane = newValue.getTipo() == TipoCartao.ABASTECIMENTO;

                    // Abordagem 1: Ocultar/mostrar o Pane inteiro
                    // paneAbastecimento.setVisible(!mostrarPane);
                    paneAbastecimento.getChildren().forEach(node -> node.setVisible(mostrarPane));


                    System.out.println("Visibilidade dos campos: Quantidade = " + mostrarPane + ", Unidade = " + mostrarPane);


                    txtQuantidade.requestLayout(); // Força a atualização do layout
                    txtUnidade.requestLayout();   // Força a atualização do layout
                } else {
                    System.out.println("Erro: txtQuantidade ou txtUnidade não foram inicializados.");
                }
            } else {
                System.out.println("Nenhum cartão válido selecionado.");
            }
            System.out.println("Listener configurado para comboBoxCartao.");
        });

        txtNome.textProperty().addListener((observable, oldValue, newValue) -> verificarCamposPreenchidos());
        txtDescricao.textProperty().addListener((observable, oldValue, newValue) -> verificarCamposPreenchidos());

        comboBoxCarro.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Carro selecionado: " + newValue.getMarca() + " " + newValue.getModelo());

                // Obtém a lista de cartões associados ao carro
                List<Cartao> cartoesAssociados = newValue.getCartoes();

                if (cartoesAssociados != null && !cartoesAssociados.isEmpty()) {
                    // Define o primeiro cartão da lista na ComboBox de cartões
                    comboBoxCartao.setValue(cartoesAssociados.get(0));
                    System.out.println("Cartão associado ao carro: " + cartoesAssociados.get(0).getNumero());
                } else {
                    System.out.println("Nenhum cartão associado ao carro selecionado.");
                    comboBoxCartao.setValue(null);
                }
            } else {
                System.out.println("Nenhum carro selecionado.");
                comboBoxCartao.setValue(null);
            }
        });

        txtData.valueProperty().addListener((observable, oldValue, newValue) -> verificarCamposPreenchidos());
        txtValor.textProperty().addListener((observable, oldValue, newValue) -> verificarCamposPreenchidos());

    }

    private void verificarCamposPreenchidos() {
        boolean camposPreenchidos = !txtNome.getText().isEmpty() && !txtDescricao.getText().isEmpty() && comboBoxCarro.getValue() != null && comboBoxCartao.getValue() != null && txtData.getValue() != null && !txtValor.getText().isEmpty();
        btRegistar.setDisable(!camposPreenchidos);
    }

    private void carregarCartoes() {
        List<Cartao> cartoes = cartaoRepository.findAll();
        if (cartoes.isEmpty()) {
            System.out.println("Nenhum cartão encontrado no repositório.");
        } else {
            System.out.println("Cartões carregados: " + cartoes.size());
            for (Cartao cartao : cartoes) {
                System.out.println("Cartão: " + cartao.getNumero());
            }
        }
        listaDeCartoes.setAll(cartoes);
        comboBoxCartao.setItems(FXCollections.observableArrayList(listaDeCartoes));
        System.out.println("Itens no ComboBox de Cartões: " + comboBoxCartao.getItems().size());
    }

    private void carregarCarros() {
        List<Carro> carros = carroRepository.findAll();

        if (carros.isEmpty()) {
            System.out.println("Nenhum carro encontrado no repositório.");
        } else {
            System.out.println("Carros carregados: " + carros.size());
        }

        comboBoxCarro.setItems(FXCollections.observableArrayList(carros));
    }

    private void carregarMotorista() {
        List<Colaborador> motoristas = colaboradorRepository.findAll();
        comboBoxMotorista.setItems(FXCollections.observableArrayList(motoristas));
    }

    private void carregarDespesas() {
        List<Despesa> despesas = despesaService.findAll();
        if (despesas.isEmpty()) {
            System.out.println("Nenhuma despesa encontrada no repositório.");
        } else {
            System.out.println("Despesas carregadas: " + despesas.size());
        }
        listaDespesa.setAll(despesas);
    }

    private void selecionarCarroPorCartao(Cartao cartao) {
        if (cartao != null && cartao.getCarro() != null) {
            if (!comboBoxCarro.getItems().isEmpty()) {
                comboBoxCarro.setValue(cartao.getCarro());
            } else {
                System.out.println("Nenhum carro disponível na lista.");
            }
        } else {
            System.out.println("Nenhum carro associado ao cartão selecionado.");
        }
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

        despesaService.limparCache();

    }

    @FXML
    protected void onclickOnRegistar() {
        selecionado = null;

        try {
            Cartao cartao = comboBoxCartao.getValue();
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();
            Carro carro = comboBoxCarro.getValue();
            Colaborador motorista = comboBoxMotorista.getValue();
            Instant data = txtData.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Validação dos campos numéricos
            Double valor = FormUtils.validarCampoDouble(txtValor.getText().trim(), "Valor");

            if (cartao.getTipo() == TipoCartao.ABASTECIMENTO) {
                Integer quantidade = FormUtils.validarCampoInteger(txtQuantidade.getText().trim(), "Quantidade");
                Double unidade = FormUtils.validarCampoDouble(txtUnidade.getText().trim(), "Unidade");
                selecionado = new Despesa(null, cartao, nome, descricao, carro, motorista, data, valor, quantidade, unidade);
            } else {
                selecionado = new Despesa(null, cartao, nome, descricao, carro, motorista, data, valor);
            }

            // Validação usando Bean Validation
            String errosValidacao = ValidationUtils.validarEntidade(selecionado);
            if (errosValidacao != null) {
                AlertUtils.showErrorAlert("Erro", errosValidacao);
                return;
            }

            // Persistência da despesa
            Despesa despesaSalva = despesaService.insert(selecionado);
            listaDespesa.add(despesaSalva);

            AlertUtils.showSuccessAlert("Sucesso", "Despesa registada com sucesso!");
            resetarBotoes();

        } catch (NumberFormatException e) {
            // A mensagem de erro já foi exibida pelos métodos de validação
        } catch (DataIntegrityViolationException e) {
            AlertUtils.showErrorAlert("Erro ao registar", "Já existe uma Despesa registada na base de dados.");
        } catch (DatabaseException e) {
            LogUtils.logError("Erro ao atualizar despesa ", e);
            AlertUtils.showErrorAlert("Erro", "Esta Despesa já está em uso na base de dados.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erro ao registar", e.getMessage());
        }
    }

    @FXML
    protected void onclickOnEditar() {
        if (editMode) {
            cancelarEdicao(); // Se já está editando, cancela a edição
            btEditar.setText("Editar"); // Volta ao estado original
        } else {
            selecionado = tableView.getSelectionModel().getSelectedItem();

            if (selecionado != null) {
                comboBoxCartao.setValue(selecionado.getCartao());
                txtNome.setText(selecionado.getNome());
                txtDescricao.setText(selecionado.getDescricao());
                comboBoxCarro.setValue(selecionado.getCarro());
                comboBoxMotorista.setValue(selecionado.getMotorista());
                txtData.setValue(selecionado.getData().atZone(ZoneId.systemDefault()).toLocalDate());
                txtValor.setText(String.valueOf(selecionado.getValor()));


                txtQuantidade.setText(String.valueOf(selecionado.getQuantidade()));
                txtUnidade.setText(String.valueOf(selecionado.getUnidade()));


                editMode = true;
                btEditar.setText("Voltar"); // Altera para cancelar
                btGravar.setDisable(false);
                btRegistar.setDisable(true); // Desativa Registar em modo de edição
                btEliminar.setDisable(false);

                FormUtils.configurarListeners(() -> editMode, btRegistar,
                        comboBoxCartao, txtNome, txtDescricao, comboBoxCarro, comboBoxMotorista, txtData, txtValor);
            }
        }
    }

    @FXML
    protected void onclickOnGravar() {
        if (!editMode) cancelarEdicao();

        if (selecionado == null) return;

        boolean confirmar = AlertUtils.showConfirmationAlert("Confirmação", "Editar Despesa", "Tem certeza que deseja editar esta despesa?");

        if (!confirmar) {
            cancelarEdicao();
            return;
        }

        try {

            Cartao cartao = comboBoxCartao.getValue();
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();
            Carro carro = comboBoxCarro.getValue();
            Colaborador motorista = comboBoxMotorista.getValue();
            Instant data = txtData.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();

            // Validação dos campos numéricos
            Double valor = FormUtils.validarCampoDouble(txtValor.getText().trim(), "Valor");

            if (cartao.getTipo() == TipoCartao.ABASTECIMENTO) {
                Integer quantidade = FormUtils.validarCampoInteger(txtQuantidade.getText().trim(), "Quantidade");
                Double unidade = FormUtils.validarCampoDouble(txtUnidade.getText().trim(), "Unidade");
                selecionado = new Despesa(selecionado.getId(), cartao, nome, descricao, carro, motorista, data, valor, quantidade, unidade);
            } else {
                selecionado = new Despesa(selecionado.getId(), cartao, nome, descricao, carro, motorista, data, valor);
            }

            // Validação da entidade Despesa
            String errosValidacao = ValidationUtils.validarEntidade(selecionado);
            if (errosValidacao != null) {
                AlertUtils.showErrorAlert("Erro de Validação", errosValidacao);
                return;
            }

            despesaService.update(selecionado.getId(), selecionado);

         //  if (listaDespesa.contains(selecionado)) {
         //      listaDespesa.set(listaDespesa.indexOf(selecionado), selecionado);
         //  } else {
         //      System.out.println("Despesa selecionada não encontrada na lista.");
         //  }

            FormUtils.atualizarListaNaTabela(listaDespesa, selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Despesa atualizada com sucesso!");

            resetarBotoes();

        } catch (DataIntegrityViolationException e) {
            LogUtils.logError("Erro ao atualizar Despesa (ID duplicado)", e); // Registra a exceção
            AlertUtils.showErrorAlert("Erro", "Este ID já está em uso.");
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erro ao atualizar", e.getMessage());
        }
    }

    @FXML
    protected void onclickOnEliminar() {
        Despesa selecionado = tableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }

        boolean confirmar = AlertUtils.showConfirmationAlert("Confirmação", "Eliminar Despesa", "Tem certeza que deseja eliminar esta despesa?");

        if (!confirmar) {
            return;
        }

        try {
            despesaService.delete(selecionado.getId());
            listaDespesa.remove(selecionado);

            AlertUtils.showSuccessAlert("Sucesso", "Despesa eliminada com sucesso!");

            resetarBotoes();

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erro ao eliminar", e.getMessage());
        }
    }

    /**
     * Cancela a edição do Colaborador, limpando a seleção e resetando os campos.
     * Também altera o botão Editar para o estado original.
     */
    private void cancelarEdicao() {
        selecionado = null; // Remove o colaborador em edição
        editMode = false; // Sai do modo de edição

        tableView.getSelectionModel().clearSelection(); // Limpa a seleção da tabela
        carregarCarros(); // 🔥 Garante que os dados são sincronizados com o repositório
        tableView.refresh(); // Atualiza a tabela

        resetarBotoes(); // Chama a função para restaurar o estado inicial
    }

    private Map<String, Control> entidade() {
        return Map.of(
                "cartao", comboBoxCartao,
                "nome", txtNome,
                "descricao", txtDescricao,
                "data", txtData,
                "carro", comboBoxCarro,
                "motorista", comboBoxMotorista,
                "quantidade", txtQuantidade,
                "unidade", txtUnidade
        );
    }
}