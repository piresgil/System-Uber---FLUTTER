import 'package:flutter/material.dart';
import '../models/pagamento.dart';
import '../services/pagamento_service.dart';

class ListagemPagamentosView extends StatefulWidget {
  const ListagemPagamentosView({super.key});

  @override
  State<ListagemPagamentosView> createState() => _ListagemPagamentosViewState();
}

class _ListagemPagamentosViewState extends State<ListagemPagamentosView> {
  final PagamentoService _pagamentoService = PagamentoService();

  final TextEditingController _txtPesquisaController = TextEditingController();
  final TextEditingController _dpInicioController = TextEditingController();
  final TextEditingController _dpFimController = TextEditingController();

  List<Pagamento> _todosPagamentos = [];
  List<Pagamento> _pagamentosFiltrados = [];
  bool _isLoading = true;

  DateTime? _dataInicio;
  DateTime? _dataFim;
  String? _selectedPlataforma;
  String? _selectedTipo;

  final List<String> _plataformas = ['Todas', 'Uber', 'Bolt', 'Geral', 'Outra'];
  final List<String> _tipos = ['Todos', 'Transferência', 'Dinheiro', 'Cartão', 'MBWay'];

  @override
  void initState() {
    super.initState();
    _carregarPagamentos();
    _txtPesquisaController.addListener(_aplicarFiltros);
  }

  @override
  void dispose() {
    _txtPesquisaController.dispose();
    _dpInicioController.dispose();
    _dpFimController.dispose();
    super.dispose();
  }

  Future<void> _carregarPagamentos() async {
    setState(() => _isLoading = true);
    try {
      final dados = await _pagamentoService.getAll();
      setState(() {
        _todosPagamentos = dados;
        _isLoading = false;
      });
      _aplicarFiltros();
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro ao carregar pagamentos: $e')),
        );
      }
    }
  }

  void _aplicarFiltros() {
    final query = _txtPesquisaController.text.toLowerCase().trim();

    setState(() {
      _pagamentosFiltrados = _todosPagamentos.where((p) {
        final colab = p.colaborador.toLowerCase();
        final plat = p.plataforma.toLowerCase();
        final tipo = p.tipo.toLowerCase();
        final cartao = p.cartao?.toLowerCase() ?? '';

        // Filtro de Texto Global
        final matchesQuery = query.isEmpty ||
            colab.contains(query) ||
            plat.contains(query) ||
            tipo.contains(query) ||
            cartao.contains(query);

        // Filtro de Plataforma
        final matchesPlataforma = _selectedPlataforma == null ||
            _selectedPlataforma == 'Todas' ||
            plat == _selectedPlataforma!.toLowerCase();

        // Filtro de Tipo de Pagamento
        final matchesTipo = _selectedTipo == null ||
            _selectedTipo == 'Todos' ||
            tipo == _selectedTipo!.toLowerCase();

        // Filtro por Intervalo de Datas
        bool matchesData = true;
        if (_dataInicio != null) {
          matchesData = matchesData && p.data.isAfter(_dataInicio!.subtract(const Duration(seconds: 1)));
        }
        if (_dataFim != null) {
          matchesData = matchesData && p.data.isBefore(_dataFim!.add(const Duration(days: 1)));
        }

        return matchesQuery && matchesPlataforma && matchesTipo && matchesData;
      }).toList();
    });
  }

  void _limparFiltros() {
    setState(() {
      _txtPesquisaController.clear();
      _dpInicioController.clear();
      _dpFimController.clear();
      _dataInicio = null;
      _dataFim = null;
      _selectedPlataforma = null;
      _selectedTipo = null;
      _pagamentosFiltrados = List.from(_todosPagamentos);
    });
  }

  Future<void> _selectDate(BuildContext context, bool isInicio) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isInicio ? (_dataInicio ?? DateTime.now()) : (_dataFim ?? DateTime.now()),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        final formattedDate =
            "${picked.day.toString().padLeft(2, '0')}/${picked.month.toString().padLeft(2, '0')}/${picked.year}";
        if (isInicio) {
          _dataInicio = picked;
          _dpInicioController.text = formattedDate;
        } else {
          _dataFim = picked;
          _dpFimController.text = formattedDate;
        }
      });
      _aplicarFiltros();
    }
  }

  double get _totalGeral =>
      _pagamentosFiltrados.fold(0.0, (sum, item) => sum + item.valor);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Listagem de Pagamentos'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregarPagamentos,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Expanded(
                    child: SingleChildScrollView(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          // Campo de Pesquisa Geral
                          TextField(
                            controller: _txtPesquisaController,
                            decoration: InputDecoration(
                              labelText: 'Pesquisar',
                              hintText: 'Pesquisar por colaborador, plataforma...',
                              prefixIcon: const Icon(Icons.search),
                              suffixIcon: _txtPesquisaController.text.isNotEmpty
                                  ? IconButton(
                                      icon: const Icon(Icons.clear),
                                      onPressed: () {
                                        _txtPesquisaController.clear();
                                        _aplicarFiltros();
                                      },
                                    )
                                  : null,
                              border: const OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),

                          // Filtros de Data
                          Row(
                            children: [
                              Expanded(
                                child: TextField(
                                  controller: _dpInicioController,
                                  readOnly: true,
                                  decoration: InputDecoration(
                                    labelText: 'Data início',
                                    border: const OutlineInputBorder(),
                                    suffixIcon: _dataInicio != null
                                        ? IconButton(
                                            icon: const Icon(Icons.clear),
                                            onPressed: () {
                                              setState(() {
                                                _dataInicio = null;
                                                _dpInicioController.clear();
                                              });
                                              _aplicarFiltros();
                                            },
                                          )
                                        : IconButton(
                                            icon: const Icon(Icons.calendar_today),
                                            onPressed: () => _selectDate(context, true),
                                          ),
                                  ),
                                  onTap: () => _selectDate(context, true),
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: TextField(
                                  controller: _dpFimController,
                                  readOnly: true,
                                  decoration: InputDecoration(
                                    labelText: 'Data fim',
                                    border: const OutlineInputBorder(),
                                    suffixIcon: _dataFim != null
                                        ? IconButton(
                                            icon: const Icon(Icons.clear),
                                            onPressed: () {
                                              setState(() {
                                                _dataFim = null;
                                                _dpFimController.clear();
                                              });
                                              _aplicarFiltros();
                                            },
                                          )
                                        : IconButton(
                                            icon: const Icon(Icons.calendar_today),
                                            onPressed: () => _selectDate(context, false),
                                          ),
                                  ),
                                  onTap: () => _selectDate(context, false),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),

                          // Filtros Dropdown
                          DropdownButtonFormField<String>(
                            initialValue: _selectedPlataforma,
                            decoration: const InputDecoration(
                              labelText: 'Plataforma',
                              border: OutlineInputBorder(),
                            ),
                            items: _plataformas.map((String plat) {
                              return DropdownMenuItem<String>(
                                value: plat,
                                child: Text(plat),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() => _selectedPlataforma = value);
                              _aplicarFiltros();
                            },
                          ),
                          const SizedBox(height: 12),

                          DropdownButtonFormField<String>(
                            initialValue: _selectedTipo,
                            decoration: const InputDecoration(
                              labelText: 'Tipo de Pagamento',
                              border: OutlineInputBorder(),
                            ),
                            items: _tipos.map((String tipo) {
                              return DropdownMenuItem<String>(
                                value: tipo,
                                child: Text(tipo),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() => _selectedTipo = value);
                              _aplicarFiltros();
                            },
                          ),
                          const SizedBox(height: 12),

                          OutlinedButton.icon(
                            onPressed: _limparFiltros,
                            icon: const Icon(Icons.clear_all),
                            label: const Text('Limpar Filtros'),
                          ),
                          const SizedBox(height: 16),

                          // Ações de Exportação
                          Wrap(
                            spacing: 8.0,
                            runSpacing: 8.0,
                            alignment: WrapAlignment.center,
                            children: [
                              ElevatedButton.icon(
                                onPressed: () {
                                  // Lógica futura: exportação CSV
                                },
                                icon: const Icon(Icons.file_download),
                                label: const Text('Exportar CSV'),
                              ),
                              ElevatedButton.icon(
                                onPressed: () {
                                  // Lógica futura: exportação Excel
                                },
                                icon: const Icon(Icons.table_chart),
                                label: const Text('Exportar Excel'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 20),

                          // Tabela de Dados
                          SizedBox(
                            width: double.infinity,
                            child: SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: DataTable(
                                columns: const [
                                  DataColumn(label: Text('Colaborador')),
                                  DataColumn(label: Text('Plataforma')),
                                  DataColumn(label: Text('Tipo')),
                                  DataColumn(label: Text('Data')),
                                  DataColumn(label: Text('Valor')),
                                ],
                                rows: _pagamentosFiltrados.map((p) {
                                  final dataFormatada =
                                      "${p.data.day.toString().padLeft(2, '0')}/${p.data.month.toString().padLeft(2, '0')}/${p.data.year}";
                                  return DataRow(cells: [
                                    DataCell(Text(p.colaborador)),
                                    DataCell(Text(p.plataforma)),
                                    DataCell(Text(p.tipo)),
                                    DataCell(Text(dataFormatada)),
                                    DataCell(Text('${p.valor.toStringAsFixed(2)} €')),
                                  ]);
                                }).toList(),
                              ),
                            ),
                          ),
                          const SizedBox(height: 20),

                          // Resumo Financeiro
                          Card(
                            elevation: 2,
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Total Filtrado: ${_totalGeral.toStringAsFixed(2)} €',
                                    style: const TextStyle(
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  const SizedBox(height: 4),
                                  Text('Registos apresentados: ${_pagamentosFiltrados.length}'),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}