package application.util;

import application.model.Carro;
import application.model.Cartao;
import application.model.Colaborador;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FormUtils {

    /**
     * Preenche uma entidade genérica com base nos valores de um formulário JavaFX.
     *
     * @param <T>      Tipo da entidade.
     * @param entidade Instância da entidade a ser preenchida.
     * @param campos   Mapa dos nomes dos atributos e seus componentes FXML correspondentes.
     * @return A entidade preenchida ou null em caso de erro.
     */
    public static <T> T capturarEntidadeDoFormulario(T entidade, Map<String, Control> campos) {
        try {
            // Itera sobre os campos do mapa e preenche a entidade com os valores correspondentes.
            for (Map.Entry<String, Control> entry : campos.entrySet()) {
                String atributo = entry.getKey();
                Control componente = entry.getValue();

                // Acessa o campo da entidade pelo nome do atributo
                Field campoEntidade = entidade.getClass().getDeclaredField(atributo);
                campoEntidade.setAccessible(true);  // Torna o campo acessível

                // Obtém o valor do componente (campo FXML) e o converte para o tipo do atributo
                Object valor = obterValorDoComponente(componente, campoEntidade.getType(), atributo);

                // Só atualiza se o valor não for nulo ou vazio
                if (valor != null && !(valor instanceof String && ((String) valor).trim().isEmpty())) {
                    campoEntidade.set(entidade, valor);
                }
            }
            return entidade;
        } catch (NumberFormatException e) {
            AlertUtils.showErrorAlert("Erro", "Erro ao capturar os dados do formulário: " + e.getMessage());

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Captura erros e exibe um alerta de erro
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erro", "Erro ao capturar os dados do formulário: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtém o valor de um componente JavaFX e o converte para o tipo adequado.
     *
     * @param componente O componente JavaFX que contém o valor.
     * @param tipo       O tipo esperado do valor.
     * @param atributo   O nome do atributo para validação específica.
     * @return O valor convertido ou null se houver erro.
     */
    private static Object obterValorDoComponente(Control componente, Class<?> tipo, String atributo) {
        if (componente instanceof TextField) {
            String valor = ((TextField) componente).getText();
            if (tipo == Integer.class || tipo == int.class) {
                try {
                    int numero = Integer.parseInt(valor);
                    if (numero < 0) {
                        throw new NumberFormatException("A quilometragem deve ser um número inteiro positivo.");
                    }
                    return numero;
                } catch (NumberFormatException e) {
                    AlertUtils.showErrorAlert("Erro", "A quilometragem deve ser um número inteiro positivo.");
                    return null;
                }
            } else if (tipo == Double.class || tipo == double.class) {
                return Double.parseDouble(valor.isEmpty() ? "0" : valor);
            }
            return valor; // Retorna como String para atributos de texto
            // Adicione mais conversões conforme necessário
        } else if (componente instanceof ComboBox) {
            ComboBox<?> comboBox = (ComboBox<?>) componente;
            return comboBox.getValue();
        } else if (componente instanceof CheckBox) {
            return ((CheckBox) componente).isSelected();
        }
        return null;
    }

    /**
     * Limpa os campos de um formulário dentro de um contêiner (Pane).
     *
     * @param container O contêiner que contém os campos a serem limpos.
     */
    public static void limparFormulario(Pane container) {
        for (Node node : container.getChildren()) {
            // Verifica o tipo de cada componente e limpa seu valor
            if (node instanceof TextInputControl) {
                ((TextInputControl) node).clear();  // Limpa campos de texto
            } else if (node instanceof ComboBox) {
                ((ComboBox<?>) node).getSelectionModel().clearSelection();  // Limpa seleção de ComboBox
            } else if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false);  // Desmarca CheckBox
            } else if (node instanceof Pane) {
                limparFormulario((Pane) node);  // Limpeza recursiva se for um contêiner dentro de outro contêiner
            }
        }
    }

    /**
     * Atualiza um item na tabela.
     *
     * @param <T>               Tipo do objeto na lista.
     * @param lista             Lista observável associada à tabela.
     * @param objetoSelecionado Objeto a ser atualizado.
     */
    public static <T> void atualizarListaNaTabela(ObservableList<T> lista, T objetoSelecionado) {
        int index = lista.indexOf(objetoSelecionado);
        if (index != -1) {
            lista.set(index, objetoSelecionado);  // Atualiza a lista na tabela
        }
    }

    /**
     * Configura um listener para habilitar/desabilitar um botão baseado na seleção da tabela.
     *
     * @param <T>       Tipo dos itens da tabela.
     * @param tableView A tabela a ser monitorada.
     * @param botao     O botão a ser habilitado/desabilitado.
     */
    public static <T> void configurarListenerSelecao(TableView<T> tableView, Button botao) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Habilita o botão se um item for selecionado, desabilita caso contrário
            botao.setDisable(newSelection == null);
        });
    }

    /**
     * Verifica se os campos estão preenchidos e habilita/desabilita o botão.
     *
     * @param botao  O botão a ser controlado.
     * @param campos Os campos a serem verificados.
     */
    public static void seCamposPreenchidos(Supplier<Boolean> isEditModeSupplier, Button botao, Node... campos) {
        boolean camposPreenchidos = true;

        for (Node campo : campos) {
            if (campo instanceof TextField && ((TextField) campo).getText().trim().isEmpty()) {
                camposPreenchidos = false;
                break;
            } else if (campo instanceof ComboBox && ((ComboBox<?>) campo).getValue() == null) {
                camposPreenchidos = false;
                break;
            } else if (campo instanceof DatePicker && ((DatePicker) campo).getValue() == null) {
                camposPreenchidos = false;
                break;
            }
            // else if (campo instanceof CheckBox && !((CheckBox) campo).isSelected()) {
            //     camposPreenchidos = false;
            //     break;
            // }
        }

        // O botão só fica ativo se os campos estiverem preenchidos e NÃO estiver em modo de edição
        botao.setDisable(!(camposPreenchidos && !isEditModeSupplier.get()));
    }

    /**
     * Configura os listeners de eventos para os campos de um formulário, habilitando/desabilitando o botão.
     *
     * @param isEditModeSupplier Função que retorna se está em modo de edição.
     * @param botao              O botão a ser habilitado/desabilitado.
     * @param campos             Os campos a serem monitorados.
     */
    public static void configurarListeners(Supplier<Boolean> isEditModeSupplier, Button botao, Node... campos) {
        for (Node campo : campos) {

            if (campo instanceof TextField) {
                ((TextField) campo).textProperty().addListener((observable, oldValue, newValue) ->
                        seCamposPreenchidos(isEditModeSupplier, botao, campos)
                );
            } else if (campo instanceof ComboBox) {
                ((ComboBox<?>) campo).valueProperty().addListener((observable, oldValue, newValue) ->
                        seCamposPreenchidos(isEditModeSupplier, botao, campos)
                );
            } else if (campo instanceof DatePicker) {
                ((DatePicker) campo).valueProperty().addListener((observable, oldValue, newValue) ->
                        seCamposPreenchidos(isEditModeSupplier, botao, campos)
                );
            } else if (campo instanceof CheckBox) {
                ((CheckBox) campo).selectedProperty().addListener((observable, oldValue, newValue) ->
                        seCamposPreenchidos(isEditModeSupplier, botao, campos)
                );
            }
        }
    }

    /**
     * Configura a tabela para exibir os atributos das entidades com base nas listas de atributos.
     *
     * @param <T>       Tipo da entidade associada à tabela.
     * @param tableView A tabela a ser configurada.
     * @param atributos Lista de atributos a serem exibidos na tabela.
     * @param clazz     Classe da entidade.
     */
    public static <T> void configurarTabela(TableView<T> tableView, List<String> atributos, Class<T> clazz) {
        tableView.getColumns().clear();

        for (String atributo : atributos) {
            TableColumn<T, ?> coluna;
            try {
                Field campo = clazz.getDeclaredField(atributo);
                campo.setAccessible(true);  // Torna o campo acessível
                Class<?> tipo = campo.getType();

                // Gera a coluna com base no tipo do campo
                if (tipo.equals(String.class)) {
                    coluna = criarColunaString(campo, atributo);
                } else if (tipo.equals(Boolean.class) || tipo.equals(boolean.class)) {
                    coluna = criarColunaBoolean(campo, atributo);
                } else if (tipo.equals(Double.class) || tipo.equals(double.class)) {
                    coluna = criarColunaDouble(campo, atributo);
                } else if (tipo.equals(Integer.class) || tipo.equals(int.class)) {
                    coluna = criarColunaInteger(campo, atributo);
                } else if (tipo.equals(LocalDate.class)) {
                    coluna = criarColunaData(campo, atributo);
                } else if (tipo.equals(Instant.class)) {
                    coluna = criarColunaInstant(campo, atributo);
                } else if (campo.getType().equals(Carro.class)) {
                    coluna = criarColunaCarro(campo, atributo);
                } else if (campo.getType().equals(Colaborador.class)) {
                    coluna = criarColunaMotorista(campo, atributo);
                } else if (campo.getType().equals(Cartao.class)) {
                    coluna = criarColunaCartao(campo, atributo);
                } else {
                    coluna = criarColunaGenerica(campo, atributo);
                }

                // Adiciona a coluna à tabela
                if (coluna != null) {
                    tableView.getColumns().add(coluna);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cria uma coluna para campos do tipo String.
     */
    private static <T> TableColumn<T, String> criarColunaString(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Object valor = campo.get(cellData.getValue());
                return new SimpleStringProperty(valor != null ? valor.toString() : "");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        return coluna;
    }

    /**
     * Cria uma coluna para campos do tipo Double.
     */
    private static <T> TableColumn<T, Double> criarColunaDouble(Field campo, String atributo) {
        TableColumn<T, Double> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Double valor = (Double) campo.get(cellData.getValue());
                return new SimpleObjectProperty<>(valor); // Retorna null se o valor for null
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new SimpleObjectProperty<>(null); // Retorna null em caso de erro
            }
        });

        // Configura o CellFactory para exibir uma string vazia quando o valor for null
        coluna.setCellFactory(column -> new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(""); // Exibe uma string vazia para valores null
                } else {
                    setText(String.valueOf(item)); // Exibe o valor
                }
            }
        });

        return coluna;
    }

    /**
     * Cria uma coluna para campos do tipo Integer.
     */
    private static <T> TableColumn<T, Integer> criarColunaInteger(Field campo, String atributo) {
        TableColumn<T, Integer> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Integer valor = (Integer) campo.get(cellData.getValue());
                return new SimpleObjectProperty<>(valor); // Retorna null se o valor for null
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new SimpleObjectProperty<>(null); // Retorna null em caso de erro
            }
        });

        // Configura o CellFactory para exibir uma string vazia quando o valor for null
        coluna.setCellFactory(column -> new TableCell<T, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(""); // Exibe uma string vazia para valores null
                } else {
                    setText(String.valueOf(item)); // Exibe o valor
                }
            }
        });

        return coluna;
    }

    private static <T> TableColumn<T, Boolean> criarColunaBoolean(Field campo, String atributo) {
        TableColumn<T, Boolean> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                return new SimpleBooleanProperty((Boolean) campo.get(cellData.getValue())).asObject();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        coluna.setCellFactory(column -> new TableCell<T, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item ? "Sim" : "Não"));
            }
        });
        return coluna;
    }

    /**
     * Cria uma coluna para campos do tipo Instant.
     */
    private static <T> TableColumn<T, String> criarColunaInstant(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Instant valor = (Instant) campo.get(cellData.getValue());
                if (valor != null) {
                    // Converte Instant para LocalDate
                    LocalDate data = valor.atZone(ZoneId.systemDefault()).toLocalDate();
                    return new SimpleStringProperty(data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    return new SimpleStringProperty(""); // Exibe uma string vazia para valores null
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });

        return coluna;
    }

    /**
     * Cria uma coluna para campos do tipo LocalDate.
     */
    private static <T> TableColumn<T, LocalDate> criarColunaData(Field campo, String atributo) {
        TableColumn<T, LocalDate> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                return new SimpleObjectProperty<>((LocalDate) campo.get(cellData.getValue()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });

        // Configura o CellFactory para exibir a data no formato dd/MM/yyyy
        coluna.setCellFactory(column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(""); // Exibe uma string vazia para valores null
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // Formata a data
                }
            }
        });

        return coluna;
    }

    private static <T> TableColumn<T, String> criarColunaCarro(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Carro carro = (Carro) campo.get(cellData.getValue());
                return new SimpleStringProperty(carro != null ? carro.getMarca() + ", " + carro.getModelo() : "Sem carro");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        return coluna;
    }

    private static <T> TableColumn<T, String> criarColunaCartao(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Cartao cartao = (Cartao) campo.get(cellData.getValue());
                return new SimpleStringProperty(cartao != null ? cartao.getNumero() + ", " + cartao.getNome() : "Sem Cartao");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        return coluna;
    }

    private static <T> TableColumn<T, String> criarColunaMotorista(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Colaborador motorista = (Colaborador) campo.get(cellData.getValue());
                return new SimpleStringProperty(motorista != null ? motorista.getNome() : "Sem motorista");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        return coluna;
    }

    private static <T> TableColumn<T, String> criarColunaGenerica(Field campo, String atributo) {
        TableColumn<T, String> coluna = new TableColumn<>(capitalize(atributo));
        coluna.setCellValueFactory(cellData -> {
            try {
                Object valor = campo.get(cellData.getValue());
                return new SimpleStringProperty(valor != null ? valor.toString() : "");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        });
        return coluna;
    }

    /**
     * Capitaliza a primeira letra de um texto.
     *
     * @param texto O texto a ser capitalizado.
     * @return O texto com a primeira letra em maiúscula.
     */
    private static String capitalize(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    public static Double validarCampoDouble(String valor, String nomeCampo) {
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            AlertUtils.showErrorAlert("Erro", "O campo '" + nomeCampo + "' deve conter um número válido.");
            throw e; // Lança a exceção para interromper a execução
        }
    }

    public static Integer validarCampoInteger(String valor, String nomeCampo) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            AlertUtils.showErrorAlert("Erro", "O campo '" + nomeCampo + "' deve conter um número válido.");
            throw e; // Lança a exceção para interromper a execução
        }
    }

}
