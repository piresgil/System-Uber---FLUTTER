import 'package:flutter/material.dart';
import '../models/carro.dart';
import '../services/carro_service.dart';

class CarroView extends StatefulWidget {
  const CarroView({super.key});

  @override
  State<CarroView> createState() => _CarroViewState();
}

class _CarroViewState extends State<CarroView> {
  final CarroService _carroService = CarroService();

  final TextEditingController _marcaController = TextEditingController();
  final TextEditingController _modeloController = TextEditingController();
  final TextEditingController _matriculaController = TextEditingController();
  final TextEditingController _kilometragemController = TextEditingController();

  bool _isAtivo = false;
  bool _isLoading = true;
  Carro? _selectedCarro;
  List<Carro> _carros = [];

  @override
  void initState() {
    super.initState();
    _carregarCarros();
  }

  @override
  void dispose() {
    _marcaController.dispose();
    _modeloController.dispose();
    _matriculaController.dispose();
    _kilometragemController.dispose();
    super.dispose();
  }

  Future<void> _carregarCarros() async {
    setState(() => _isLoading = true);
    try {
      final dados = await _carroService.getAll();
      setState(() {
        _carros = dados;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao carregar carros: $e');
    }
  }

  void _limparFormulario() {
    setState(() {
      _selectedCarro = null;
      _marcaController.clear();
      _modeloController.clear();
      _matriculaController.clear();
      _kilometragemController.clear();
      _isAtivo = false;
    });
  }

  void _selecionarCarro(Carro carro) {
    setState(() {
      _selectedCarro = carro;
      _matriculaController.text = carro.matricula;
      final partes = carro.marcaModelo.split(' ');
      _marcaController.text = partes.isNotEmpty ? partes.first : '';
      _modeloController.text = partes.length > 1 ? partes.sublist(1).join(' ') : '';
    });
  }

  Future<void> _registarOuGravarCarro() async {
    if (_matriculaController.text.isEmpty) {
      _mostrarMensagem('Por favor, introduza a matrícula.');
      return;
    }

    final marcaModelo = "${_marcaController.text} ${_modeloController.text}".trim();
    final novoCarro = Carro(
      id: _selectedCarro?.id,
      matricula: _matriculaController.text,
      marcaModelo: marcaModelo,
    );

    try {
      if (_selectedCarro == null) {
        await _carroService.create(novoCarro);
        _mostrarMensagem('Carro registado com sucesso!');
      } else {
        // Se a API suportar update no futuro, faz a chamada aqui; por agora recria/atualiza
        await _carroService.create(novoCarro);
        _mostrarMensagem('Carro atualizado com sucesso!');
      }
      _limparFormulario();
      _carregarCarros();
    } catch (e) {
      _mostrarMensagem('Erro ao guardar carro: $e');
    }
  }

  Future<void> _eliminarCarro() async {
    if (_selectedCarro == null || _selectedCarro!.id == null) {
      _mostrarMensagem('Selecione um carro para eliminar.');
      return;
    }

    try {
      await _carroService.delete(_selectedCarro!.id!);
      _mostrarMensagem('Carro eliminado com sucesso!');
      _limparFormulario();
      _carregarCarros();
    } catch (e) {
      _mostrarMensagem('Erro ao eliminar carro: $e');
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
        title: const Text('Carros'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregarCarros,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  // Formulário de Entrada
                  TextField(
                    controller: _marcaController,
                    decoration: const InputDecoration(
                      labelText: 'Marca',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _modeloController,
                    decoration: const InputDecoration(
                      labelText: 'Modelo',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _matriculaController,
                          decoration: const InputDecoration(
                            labelText: 'Matrícula',
                            border: OutlineInputBorder(),
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Row(
                        children: [
                          Checkbox(
                            value: _isAtivo,
                            onChanged: (bool? value) {
                              setState(() {
                                _isAtivo = value ?? false;
                              });
                            },
                          ),
                          const Text('Ativo'),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _kilometragemController,
                    decoration: const InputDecoration(
                      labelText: 'Kilometragem',
                      border: OutlineInputBorder(),
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),

                  // Botões de Ação
                  Wrap(
                    spacing: 8.0,
                    runSpacing: 8.0,
                    children: [
                      ElevatedButton(
                        onPressed: _selectedCarro != null ? _registarOuGravarCarro : null,
                        child: const Text('Editar'),
                      ),
                      ElevatedButton(
                        onPressed: _selectedCarro != null ? _eliminarCarro : null,
                        child: const Text('Eliminar'),
                      ),
                      ElevatedButton(
                        onPressed: _registarOuGravarCarro,
                        child: const Text('Gravar'),
                      ),
                      ElevatedButton(
                        onPressed: () {
                          _limparFormulario();
                        },
                        child: const Text('Registar'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // Tabela equivalente à TableView do JavaFX
                  Expanded(
                    child: SingleChildScrollView(
                      scrollDirection: Axis.vertical,
                      child: SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: DataTable(
                          showCheckboxColumn: false,
                          columns: const [
                            DataColumn(label: Text('Matrícula')),
                            DataColumn(label: Text('Marca / Modelo')),
                            DataColumn(label: Text('Ativo')),
                          ],
                          rows: _carros.map((carro) {
                            final isSelected = _selectedCarro?.id == carro.id;
                            return DataRow(
                              selected: isSelected,
                              onSelectChanged: (_) => _selecionarCarro(carro),
                              cells: [
                                DataCell(Text(carro.matricula)),
                                DataCell(Text(carro.marcaModelo)),
                                DataCell(Text(_isAtivo ? 'Sim' : 'Não')),
                              ],
                            );
                          }).toList(),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}