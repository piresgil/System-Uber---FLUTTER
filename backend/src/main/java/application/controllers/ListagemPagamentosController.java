package application.controllers;

import application.model.Colaborador;
import application.model.Pagamento;
import application.model.Plataforma;
import application.model.TipoPagamento;
import application.services.ColaboradorService;
import application.services.PagamentoService;
import application.util.AlertUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class ListagemPagamentosController {

    @FXML private TableView<Pagamento> tableView;
    @FXML private TableColumn<Pagamento, String> colColaborador;
    @FXML private TableColumn<Pagamento, String> colPlataforma;
    @FXML private TableColumn<Pagamento, String> colTipo;
    @FXML private TableColumn<Pagamento, String> colData;
    @FXML private TableColumn<Pagamento, String> colValor;

    @FXML private TextField txtPesquisa;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private ComboBox<Plataforma> comboPlataforma;
    @FXML private ComboBox<TipoPagamento> comboTipo;

    private final ObservableList<Pagamento> listaOriginal = FXCollections.observableArrayList();
    private final ObservableList<Pagamento> listaFiltrada = FXCollections.observableArrayList();

    @FXML private Label lblTotal;
    @FXML private Label lblTotalPlataforma;
    @FXML private Label lblTotalTipo;

    @Autowired
    private ColaboradorService colaboradorService;
    @Autowired
    private PagamentoService pagamentoService;

    @FXML
    public void initialize() {
        configurarTabela();
        carregarFiltros();
        carregarDados();
        configurarListeners();
    }

    private void configurarTabela() {
        colColaborador.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getColaborador().getNome()));

        colPlataforma.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getPlataforma().name()));

        colTipo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getTipoPagamento().name()));

        colData.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getData().toString()));

        colValor.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.format("%.2f €", c.getValue().getValor())));

        tableView.setItems(listaFiltrada);
    }

    private void carregarFiltros() {
        comboPlataforma.setItems(FXCollections.observableArrayList(Plataforma.values()));
        comboTipo.setItems(FXCollections.observableArrayList(TipoPagamento.values()));
    }

    private void carregarDados() {
        listaOriginal.setAll(pagamentoService.findAll());
        listaFiltrada.setAll(listaOriginal);
        atualizarTotais();
    }

    private void configurarListeners() {
        txtPesquisa.textProperty().addListener((obs, o, n) -> filtrar());
        dpInicio.valueProperty().addListener((obs, o, n) -> filtrar());
        dpFim.valueProperty().addListener((obs, o, n) -> filtrar());
        comboPlataforma.valueProperty().addListener((obs, o, n) -> filtrar());
        comboTipo.valueProperty().addListener((obs, o, n) -> filtrar());
    }

    private void filtrar() {
        listaFiltrada.setAll(
                listaOriginal.stream()
                        .filter(this::filtroTexto)
                        .filter(this::filtroData)
                        .filter(this::filtroPlataforma)
                        .filter(this::filtroTipo)
                        .collect(Collectors.toList())
        );

        atualizarTotais();
    }

    private boolean filtroTexto(Pagamento p) {
        String txt = txtPesquisa.getText();
        if (txt == null || txt.isBlank()) return true;

        txt = txt.toLowerCase();

        return p.getColaborador().getNome().toLowerCase().contains(txt)
                || p.getPlataforma().name().toLowerCase().contains(txt)
                || p.getTipoPagamento().name().toLowerCase().contains(txt)
                || String.valueOf(p.getValor()).contains(txt);
    }

    private boolean filtroData(Pagamento p) {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fim = dpFim.getValue();

        if (inicio == null && fim == null) return true;

        if (inicio != null && p.getData().isBefore(inicio)) return false;
        if (fim != null && p.getData().isAfter(fim)) return false;

        return true;
    }

    private boolean filtroPlataforma(Pagamento p) {
        return comboPlataforma.getValue() == null ||
                p.getPlataforma() == comboPlataforma.getValue();
    }

    private boolean filtroTipo(Pagamento p) {
        return comboTipo.getValue() == null ||
                p.getTipoPagamento() == comboTipo.getValue();
    }

    @FXML
    private void onclickLimpar() {
        txtPesquisa.clear();
        dpInicio.setValue(null);
        dpFim.setValue(null);
        comboPlataforma.setValue(null);
        comboTipo.setValue(null);
        listaFiltrada.setAll(listaOriginal);
    }

    private void atualizarTotais() {

        // Total geral
        double totalGeral = listaFiltrada.stream()
                .mapToDouble(Pagamento::getValor)
                .sum();

        lblTotal.setText(String.format("Total: %.2f €", totalGeral));

        // Total por plataforma
        if (comboPlataforma.getValue() != null) {
            Plataforma plataforma = comboPlataforma.getValue();

            double totalPlataforma = listaFiltrada.stream()
                    .filter(p -> p.getPlataforma() == plataforma)
                    .mapToDouble(Pagamento::getValor)
                    .sum();

            lblTotalPlataforma.setText(
                    String.format("Plataforma (%s): %.2f €", plataforma.name(), totalPlataforma)
            );
        } else {
            lblTotalPlataforma.setText("Plataforma: --");
        }

        // Total por tipo
        if (comboTipo.getValue() != null) {
            TipoPagamento tipo = comboTipo.getValue();

            double totalTipo = listaFiltrada.stream()
                    .filter(p -> p.getTipoPagamento() == tipo)
                    .mapToDouble(Pagamento::getValor)
                    .sum();

            lblTotalTipo.setText(
                    String.format("Tipo (%s): %.2f €", tipo.name(), totalTipo)
            );
        } else {
            lblTotalTipo.setText("Tipo: --");
        }
    }

    @FXML
    private void onclickExportarCSV() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Colaborador;Plataforma;Tipo;Data;Valor\n");

            for (Pagamento p : listaFiltrada) {
                sb.append(p.getColaborador().getNome()).append(";")
                        .append(p.getPlataforma().name()).append(";")
                        .append(p.getTipoPagamento().name()).append(";")
                        .append(p.getData()).append(";")
                        .append(p.getValor()).append("\n");
            }

            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("pagamentos_export.csv"),
                    sb.toString()
            );

            AlertUtils.showSuccessAlert("Exportado", "CSV criado com sucesso!");

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erro", "Falha ao exportar CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onclickExportarExcel() {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Pagamentos");

            // Cabeçalho
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Colaborador");
            header.createCell(1).setCellValue("Plataforma");
            header.createCell(2).setCellValue("Tipo");
            header.createCell(3).setCellValue("Data");
            header.createCell(4).setCellValue("Valor");

            // Dados
            int rowIndex = 1;
            for (Pagamento p : listaFiltrada) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(p.getColaborador().getNome());
                row.createCell(1).setCellValue(p.getPlataforma().name());
                row.createCell(2).setCellValue(p.getTipoPagamento().name());
                row.createCell(3).setCellValue(p.getData().toString());
                row.createCell(4).setCellValue(p.getValor());
            }

            // Auto-ajustar colunas
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar ficheiro
            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream("pagamentos_export.xlsx")) {
                workbook.write(fileOut);
            }

            AlertUtils.showSuccessAlert("Exportado", "Excel criado com sucesso!");

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erro", "Falha ao exportar Excel: " + e.getMessage());
        }
    }


    private void importarExcel(java.io.File file) {
        try (org.apache.poi.ss.usermodel.Workbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.FileInputStream(file))) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String colaboradorNome = row.getCell(0).getStringCellValue().trim();
                    String plataformaStr = row.getCell(1).getStringCellValue().trim();
                    String tipoStr = row.getCell(2).getStringCellValue().trim();
                    String dataStr = row.getCell(3).getStringCellValue().trim();
                    double valor = row.getCell(4).getNumericCellValue();

                    // Procurar colaborador pelo nome
                    Colaborador col = colaboradorService.findAll().stream()
                            .filter(c -> c.getNome().equalsIgnoreCase(colaboradorNome))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Colaborador não encontrado: " + colaboradorNome));

                    Plataforma plataforma = Plataforma.valueOf(plataformaStr.toUpperCase());
                    TipoPagamento tipo = TipoPagamento.valueOf(tipoStr.toUpperCase());
                    LocalDate data = LocalDate.parse(dataStr);

                    Pagamento pagamento = new Pagamento();
                    pagamento.setColaborador(col);
                    pagamento.setPlataforma(plataforma);
                    pagamento.setTipoPagamento(tipo);
                    pagamento.setData(data);
                    pagamento.setValor(valor);

                    pagamentoService.insert(pagamento);

                    listaOriginal.add(pagamento);
                    listaFiltrada.add(pagamento);

                } catch (Exception e) {
                    System.err.println("Erro na linha " + (i + 1) + ": " + e.getMessage());
                }
            }

            tableView.refresh();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler Excel: " + e.getMessage(), e);
        }
    }



    @FXML
    private void onclickImportarExcel() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Selecionar ficheiro Excel");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Ficheiros Excel (*.xlsx)", "*.xlsx")
            );

            java.io.File file = fileChooser.showOpenDialog(null);
            if (file == null) return;

            importarExcel(file);

            AlertUtils.showSuccessAlert("Importado", "Excel importado com sucesso!");

        } catch (Exception e) {
            AlertUtils.showErrorAlert("Erro", "Falha ao importar Excel: " + e.getMessage());
        }
    }
}
