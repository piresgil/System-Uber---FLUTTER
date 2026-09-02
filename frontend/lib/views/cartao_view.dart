import 'package:flutter/material.dart';
import '../models/cartao.dart';
import '../models/carro.dart';
import '../services/cartao_service.dart';
import '../services/carro_service.dart';

class CartaoView extends StatefulWidget {
  const CartaoView({super.key});

  @override
  State<CartaoView> createState() => _CartaoViewState();
}

class _CartaoViewState extends State<CartaoView> {
  final CartaoService _cartaoService = CartaoService();
  final CarroService _carroService = CarroService();

  final TextEditingController _cartaoNrController = TextEditingController();
  final TextEditingController _contratoNrController = TextEditingController();
  final TextEditingController _nomeController = TextEditingController();

  String? _selectedTipo;
  Carro? _selectedCarro;
  Cartao? _selectedCartao;

  bool _isLoading = true;

  final List<String> _tipos = ['Frota', 'Combustível', 'Portagem', 'Outro'];
  List<Carro> _carros = [];
  List<Cartao> _cartoes = [];

  @override
  void initState() {
    super.initState();
    _carregarDados();
  }

  @override
  void dispose() {
    _cartaoNrController.dispose();
    _contratoNrController.dispose();
    _nomeController.dispose();
    super.dispose();
  }

  Future<void> _carregarDados() async {
    setState(() => _isLoading = true);
    try {
      final resultados = await Future.wait([
        _cartaoService.getAll(),
        _carroService.getAll(),
      ]);

      setState(() {
        _cartoes = resultados[0] as List<Cartao>;
        _carros = resultados[1] as List<Carro>;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao carregar dados: $e');
    }
  }

  void _limparFormulario() {
    setState(() {
      _selectedCartao = null;
      _selectedTipo = null;
      _selectedCarro = null;
      _cartaoNrController.clear();
      _contratoNrController.clear();
      _nomeController.clear();
    });
  }

  void _selecionarCartao(Cartao cartao) {
    setState(() {
      _selectedCartao = cartao;
      _selectedTipo = _tipos.contains(cartao.tipo) ? cartao.tipo : null;
      _cartaoNrController.text = cartao.numeroCartao;
      _contratoNrController.text = cartao.contrato ?? '';
      _nomeController.text = cartao.nomeCliente ?? '';
      
      // Tenta encontrar o carro correspondente pelo ID ou pela matrícula
      try {
        _selectedCarro = _carros.firstWhere((c) =>
            (cartao.carroId != null && c.id == cartao.carroId) ||
            (cartao.carroMatricula != null && c.matricula == cartao.carroMatricula));
      } catch (_) {
        _selectedCarro = null;
      }
    });
  }

  Future<void> _registarOuGravarCartao() async {
    if (_cartaoNrController.text.isEmpty) {
      _mostrarMensagem('Por favor, introduza o número do cartão.');
      return;
    }

    final novoCartao = Cartao(
      id: _selectedCartao?.id,
      numeroCartao: _cartaoNrController.text,
      tipo: _selectedTipo ?? 'Outro',
      contrato: _contratoNrController.text.isNotEmpty ? _contratoNrController.text : null,
      nomeCliente: _nomeController.text.isNotEmpty ? _nomeController.text : null,
      carroId: _selectedCarro?.id,
      carroMatricula: _selectedCarro?.matricula,
    );

    try {
      if (_selectedCartao == null) {
        await _cartaoService.create(novoCartao);
        _mostrarMensagem('Cartão registado com sucesso!');
      } else {
        await _cartaoService.create(novoCartao);
        _mostrarMensagem('Cartão atualizado com sucesso!');
      }
      _limparFormulario();
      _carregarDados();
    } catch (e) {
      _mostrarMensagem('Erro ao guardar cartão: $e');
    }
  }

  Future<void> _eliminarCartao() async {
    if (_selectedCartao == null || _selectedCartao!.id == null) {
      _mostrarMensagem('Selecione um cartão para eliminar.');
      return;
    }

    try {
      await _cartaoService.delete(_selectedCartao!.id!);
      _mostrarMensagem('Cartão eliminado com sucesso!');
      _limparFormulario();
      _carregarDados();
    } catch (e) {
      _mostrarMensagem('Erro ao eliminar cartão: $e');
    }
  }

  void _mostrarMensagem(String mensagem) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mensagem)));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cartão'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregarDados,
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
                        children: [
                          // Dropdown Tipo
                          DropdownButtonFormField<String>(
                            initialValue: _selectedTipo,
                            decoration: const InputDecoration(
                              labelText: 'Tipo',
                              border: OutlineInputBorder(),
                            ),
                            items: _tipos.map((String tipo) {
                              return DropdownMenuItem<String>(
                                value: tipo,
                                child: Text(tipo),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() {
                                _selectedTipo = value;
                              });
                            },
                          ),
                          const SizedBox(height: 12),

                          // Campos de Texto
                          TextField(
                            controller: _cartaoNrController,
                            decoration: const InputDecoration(
                              labelText: 'Nr Cartão',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _contratoNrController,
                            decoration: const InputDecoration(
                              labelText: 'Contrato',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _nomeController,
                            decoration: const InputDecoration(
                              labelText: 'Nome',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),

                          // Dropdown Carro
                          DropdownButtonFormField<Carro>(
                            initialValue: _selectedCarro,
                            decoration: const InputDecoration(
                              labelText: 'Carro',
                              border: OutlineInputBorder(),
                            ),
                            items: _carros.map((Carro carro) {
                              return DropdownMenuItem<Carro>(
                                value: carro,
                                child: Text('${carro.matricula} (${carro.marcaModelo})'),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() {
                                _selectedCarro = value;
                              });
                            },
                          ),
                          const SizedBox(height: 16),

                          // Botões de Ação
                          Wrap(
                            spacing: 8.0,
                            runSpacing: 8.0,
                            children: [
                              ElevatedButton(
                                onPressed: _selectedCartao != null ? _registarOuGravarCartao : null,
                                child: const Text('Editar'),
                              ),
                              ElevatedButton(
                                onPressed: _selectedCartao != null ? _eliminarCartao : null,
                                child: const Text('Eliminar'),
                              ),
                              ElevatedButton(
                                onPressed: _registarOuGravarCartao,
                                child: const Text('Gravar'),
                              ),
                              ElevatedButton(
                                onPressed: _limparFormulario,
                                child: const Text('Registar'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 20),

                          // TableView equivalente
                          SizedBox(
                            width: double.infinity,
                            child: SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: DataTable(
                                showCheckboxColumn: false,
                                columns: const [
                                  DataColumn(label: Text('Tipo')),
                                  DataColumn(label: Text('Número')),
                                  DataColumn(label: Text('Contrato')),
                                  DataColumn(label: Text('Nome')),
                                  DataColumn(label: Text('Carro')),
                                ],
                                rows: _cartoes.map((cartao) {
                                  final isSelected = _selectedCartao?.id == cartao.id;
                                  return DataRow(
                                    selected: isSelected,
                                    onSelectChanged: (_) => _selecionarCartao(cartao),
                                    cells: [
                                      DataCell(Text(cartao.tipo)),
                                      DataCell(Text(cartao.numeroCartao)),
                                      DataCell(Text(cartao.contrato ?? '')),
                                      DataCell(Text(cartao.nomeCliente ?? '')),
                                      DataCell(Text(cartao.carroMatricula ?? '')),
                                    ],
                                  );
                                }).toList(),
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