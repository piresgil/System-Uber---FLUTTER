import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../models/despesa.dart';
import '../models/cartao.dart';
import '../models/carro.dart';
import '../models/colaborador.dart';
import '../services/despesa_service.dart';
import '../services/cartao_service.dart';
import '../services/carro_service.dart';
import '../services/colaborador_service.dart';

class DespesaView extends StatefulWidget {
  const DespesaView({super.key});

  @override
  State<DespesaView> createState() => _DespesaViewState();
}

class _DespesaViewState extends State<DespesaView> {
  final DespesaService _despesaService = DespesaService();
  final CartaoService _cartaoService = CartaoService();
  final CarroService _carroService = CarroService();
  final ColaboradorService _colaboradorService = ColaboradorService();

  final TextEditingController _nomeController = TextEditingController();
  final TextEditingController _descricaoController = TextEditingController();
  final TextEditingController _dataController = TextEditingController();
  final TextEditingController _valorController = TextEditingController();
  final TextEditingController _quantidadeController = TextEditingController();
  final TextEditingController _unidadeController = TextEditingController();

  Cartao? _selectedCartao;
  Carro? _selectedCarro;
  Colaborador? _selectedMotorista;
  DateTime? _selectedDate;

  // Variáveis para guardar o ficheiro selecionado com o image_picker
  String? _faturaPathSelecionado;
  String? _nomeFaturaSelecionada;

  bool _isLoading = true;
  Despesa? _selectedDespesa;

  List<Cartao> _cartoes = [];
  List<Carro> _carros = [];
  List<Colaborador> _motoristas = [];
  List<Despesa> _despesas = [];

  @override
  void initState() {
    super.initState();
    _carregarDados();
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _descricaoController.dispose();
    _dataController.dispose();
    _valorController.dispose();
    _quantidadeController.dispose();
    _unidadeController.dispose();
    super.dispose();
  }

  Future<void> _carregarDados() async {
    setState(() => _isLoading = true);
    try {
      _despesas = await _despesaService.getAll();

      try { _cartoes = await _cartaoService.getAll(); } catch (_) {}
      try { _carros = await _carroService.getAll(); } catch (_) {}
      try { _motoristas = await _colaboradorService.getAll(); } catch (_) {}

      setState(() {
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao carregar despesas: $e');
    }
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
        _dataController.text =
            "${picked.day.toString().padLeft(2, '0')}/${picked.month.toString().padLeft(2, '0')}/${picked.year}";
      });
    }
  }

  void _limparFormulario() {
    setState(() {
      _selectedDespesa = null;
      _selectedCartao = null;
      _selectedCarro = null;
      _selectedMotorista = null;
      _selectedDate = null;
      _faturaPathSelecionado = null;
      _nomeFaturaSelecionada = null;
      _nomeController.clear();
      _descricaoController.clear();
      _dataController.clear();
      _valorController.clear();
      _quantidadeController.clear();
      _unidadeController.clear();
    });
  }

  void _selecionarDespesa(Despesa despesa) {
    setState(() {
      _selectedDespesa = despesa;
      _faturaPathSelecionado = null;
      _nomeFaturaSelecionada = despesa.faturaUrl;
      _nomeController.text = despesa.nome;
      _descricaoController.text = despesa.descricao ?? '';
      _valorController.text = despesa.valor.toString();
      _quantidadeController.text = despesa.quantidade?.toString() ?? '';
      _unidadeController.text = despesa.unidade ?? '';

      if (despesa.data != null) {
        _selectedDate = despesa.data;
        _dataController.text =
            "${despesa.data!.day.toString().padLeft(2, '0')}/${despesa.data!.month.toString().padLeft(2, '0')}/${despesa.data!.year}";
      } else {
        _selectedDate = null;
        _dataController.clear();
      }

      try {
        _selectedCartao = _cartoes.firstWhere((c) => c.id == despesa.cartaoId);
      } catch (_) {
        _selectedCartao = null;
      }

      try {
        _selectedCarro = _carros.firstWhere((c) => c.id == despesa.carroId);
      } catch (_) {
        _selectedCarro = null;
      }

      try {
        _selectedMotorista = _motoristas.firstWhere((m) => m.id == despesa.motoristaId);
      } catch (_) {
        _selectedMotorista = null;
      }
    });
  }

  Future<void> _procurarFatura() async {
    try {
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(source: ImageSource.gallery);

      if (image != null) {
        setState(() {
          _faturaPathSelecionado = image.path;
          _nomeFaturaSelecionada = image.name;
        });
        _mostrarMensagem('Fatura selecionada: $_nomeFaturaSelecionada');
      }
    } catch (e) {
      _mostrarMensagem('Erro ao selecionar imagem: $e');
    }
  }

  Future<void> _registarOuGravarDespesa() async {
    if (_nomeController.text.isEmpty) {
      _mostrarMensagem('Por favor, introduza o nome da despesa.');
      return;
    }

    final double? valor = double.tryParse(_valorController.text);
    if (valor == null) {
      _mostrarMensagem('Por favor, introduza um valor válido.');
      return;
    }

    final novaDespesa = Despesa(
      id: _selectedDespesa?.id,
      nome: _nomeController.text,
      descricao: _descricaoController.text.isNotEmpty ? _descricaoController.text : null,
      valor: valor,
      data: _selectedDate,
      quantidade: double.tryParse(_quantidadeController.text),
      unidade: _unidadeController.text.isNotEmpty ? _unidadeController.text : null,
      cartaoId: _selectedCartao?.id,
      carroId: _selectedCarro?.id,
      motoristaId: _selectedMotorista?.id,
    );

    try {
      Despesa despesaSalva;
      if (_selectedDespesa == null) {
        despesaSalva = await _despesaService.create(novaDespesa);
        _mostrarMensagem('Despesa registada com sucesso!');
      } else {
        despesaSalva = await _despesaService.update(_selectedDespesa!.id!, novaDespesa);
        _mostrarMensagem('Despesa atualizada com sucesso!');
      }

      if (_faturaPathSelecionado != null && despesaSalva.id != null) {
        try {
          await _despesaService.uploadFatura(despesaSalva.id!, _faturaPathSelecionado!);
        } catch (uploadError) {
          _mostrarMensagem('Despesa guardada, mas erro no upload da fatura: $uploadError');
        }
      }

      _limparFormulario();
      _carregarDados();
    } catch (e) {
      _mostrarMensagem('Erro ao guardar despesa: $e');
    }
  }

  Future<void> _eliminarDespesa() async {
    if (_selectedDespesa == null || _selectedDespesa!.id == null) {
      _mostrarMensagem('Selecione uma despesa para eliminar.');
      return;
    }

    try {
      await _despesaService.delete(_selectedDespesa!.id!);
      _mostrarMensagem('Despesa eliminada com sucesso!');
      _limparFormulario();
      _carregarDados();
    } catch (e) {
      _mostrarMensagem('Erro ao eliminar despesa: $e');
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
        title: const Text('Despesas'),
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
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Dropdown Cartão
                          DropdownButtonFormField<Cartao>(
                            value: _selectedCartao,
                            decoration: const InputDecoration(
                              labelText: 'Cartão',
                              border: OutlineInputBorder(),
                            ),
                            items: _cartoes.map((Cartao cartao) {
                              return DropdownMenuItem<Cartao>(
                                value: cartao,
                                child: Text('${cartao.numeroCartao} (${cartao.tipo})'),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() {
                                _selectedCartao = value;
                              });
                            },
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

                          TextField(
                            controller: _descricaoController,
                            decoration: const InputDecoration(
                              labelText: 'Descrição',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),

                          // Dropdown Carro
                          DropdownButtonFormField<Carro>(
                            value: _selectedCarro,
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
                          const SizedBox(height: 12),

                          // Dropdown Motorista
                          DropdownButtonFormField<Colaborador>(
                            value: _selectedMotorista,
                            decoration: const InputDecoration(
                              labelText: 'Motorista',
                              border: OutlineInputBorder(),
                            ),
                            items: _motoristas.map((Colaborador colab) {
                              return DropdownMenuItem<Colaborador>(
                                value: colab,
                                child: Text(colab.nome),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() {
                                _selectedMotorista = value;
                              });
                            },
                          ),
                          const SizedBox(height: 12),

                          // DatePicker Data
                          TextField(
                            controller: _dataController,
                            readOnly: true,
                            decoration: InputDecoration(
                              labelText: 'Data',
                              border: const OutlineInputBorder(),
                              suffixIcon: IconButton(
                                icon: const Icon(Icons.calendar_today),
                                onPressed: () => _selectDate(context),
                              ),
                            ),
                            onTap: () => _selectDate(context),
                          ),
                          const SizedBox(height: 12),

                          TextField(
                            controller: _valorController,
                            decoration: const InputDecoration(
                              labelText: 'Valor (€)',
                              border: OutlineInputBorder(),
                            ),
                            keyboardType: const TextInputType.numberWithOptions(decimal: true),
                          ),
                          const SizedBox(height: 16),

                          // Secção Abastecimento
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              border: Border.all(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'Informação de Abastecimento',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 12),
                                TextField(
                                  controller: _quantidadeController,
                                  decoration: const InputDecoration(
                                    labelText: 'Quantidade',
                                    border: OutlineInputBorder(),
                                  ),
                                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                                ),
                                const SizedBox(height: 12),
                                TextField(
                                  controller: _unidadeController,
                                  decoration: const InputDecoration(
                                    labelText: 'Unidade (ex: Litros)',
                                    border: OutlineInputBorder(),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 16),

                          // Botão Fatura com image_picker
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              border: Border.all(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Text(
                                    _nomeFaturaSelecionada != null
                                        ? 'Fatura: $_nomeFaturaSelecionada'
                                        : 'Nenhuma fatura selecionada',
                                    overflow: TextOverflow.ellipsis,
                                    style: TextStyle(
                                      color: _nomeFaturaSelecionada != null ? Colors.green.shade700 : Colors.grey.shade700,
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                ),
                                ElevatedButton.icon(
                                  onPressed: _procurarFatura,
                                  icon: const Icon(Icons.attach_file),
                                  label: const Text('Procurar Fatura'),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 16),

                          // Botões de Ação
                          Wrap(
                            spacing: 8.0,
                            runSpacing: 8.0,
                            children: [
                              ElevatedButton(
                                onPressed: _selectedDespesa != null ? _registarOuGravarDespesa : null,
                                child: const Text('Editar'),
                              ),
                              ElevatedButton(
                                onPressed: _selectedDespesa != null ? _eliminarDespesa : null,
                                child: const Text('Eliminar'),
                              ),
                              ElevatedButton(
                                onPressed: _registarOuGravarDespesa,
                                child: const Text('Gravar'),
                              ),
                              ElevatedButton(
                                onPressed: _limparFormulario,
                                child: const Text('Registar'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 24),

                          // Título da Listagem
                          const Text(
                            'Despesas Registadas',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 12),

                          // Tabela / Listagem de Despesas
                          _despesas.isEmpty
                              ? const Padding(
                                  padding: EdgeInsets.symmetric(vertical: 12.0),
                                  child: Text('Nenhuma despesa encontrada.'),
                                )
                              : Container(
                                  width: double.infinity,
                                  decoration: BoxDecoration(
                                    border: Border.all(color: Colors.grey.shade300),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: SingleChildScrollView(
                                    scrollDirection: Axis.horizontal,
                                    child: DataTable(
                                      showCheckboxColumn: false,
                                      columns: const [
                                        DataColumn(label: Text('Nome')),
                                        DataColumn(label: Text('Descrição')),
                                        DataColumn(label: Text('Data')),
                                        DataColumn(label: Text('Valor')),
                                        DataColumn(label: Text('Quantidade')),
                                        DataColumn(label: Text('Unidade')),
                                        DataColumn(label: Text('Fatura')),
                                      ],
                                      rows: _despesas.map((despesa) {
                                        final isSelected = _selectedDespesa?.id == despesa.id;
                                        final dataFormatada = despesa.data != null
                                            ? "${despesa.data!.day.toString().padLeft(2, '0')}/${despesa.data!.month.toString().padLeft(2, '0')}/${despesa.data!.year}"
                                            : '';
                                        return DataRow(
                                          selected: isSelected,
                                          onSelectChanged: (_) => _selecionarDespesa(despesa),
                                          cells: [
                                            DataCell(Text(despesa.nome)),
                                            DataCell(Text(despesa.descricao ?? '')),
                                            DataCell(Text(dataFormatada)),
                                            DataCell(Text('${despesa.valor.toStringAsFixed(2)} €')),
                                            DataCell(Text(despesa.quantidade?.toString() ?? '')),
                                            DataCell(Text(despesa.unidade ?? '')),
                                            DataCell(Text(despesa.faturaUrl != null && despesa.faturaUrl!.isNotEmpty ? 'Sim' : 'Não')),
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