import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:url_launcher/url_launcher.dart';
import 'dart:io';
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

  int? _selectedCartaoId;
  int? _selectedCarroId;
  int? _selectedMotoristaId;
  DateTime? _selectedDate;

  String? _faturaPathSelecionado;
  String? _nomeFaturaSelecionada;
  String? _faturaUrlExistente;

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
      
      setState(() => _isLoading = false);
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao carregar dados: $e');
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
      _selectedCartaoId = null;
      _selectedCarroId = null;
      _selectedMotoristaId = null;
      _selectedDate = null;
      _faturaPathSelecionado = null;
      _nomeFaturaSelecionada = null;
      _faturaUrlExistente = null;
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
      _faturaUrlExistente = despesa.faturaUrl;
      _nomeFaturaSelecionada = despesa.faturaUrl != null && despesa.faturaUrl!.isNotEmpty 
          ? despesa.faturaUrl!.split('/').last 
          : null;
          
      _nomeController.text = despesa.nome;
      _descricaoController.text = despesa.descricao ?? '';
      _valorController.text = despesa.valor.toString();
      _quantidadeController.text = despesa.quantidade?.toString() ?? '';
      _unidadeController.text = despesa.unidade?.toString() ?? '';

      _selectedCartaoId = despesa.cartaoId;
      _selectedCarroId = despesa.carroId;
      _selectedMotoristaId = despesa.motoristaId;

      if (despesa.data != null) {
        _selectedDate = despesa.data;
        _dataController.text =
            "${despesa.data!.day.toString().padLeft(2, '0')}/${despesa.data!.month.toString().padLeft(2, '0')}/${despesa.data!.year}";
      } else {
        _selectedDate = null;
        _dataController.clear();
      }
    });
  }

  Future<void> _procurarFatura(ImageSource source) async {
    try {
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(source: source);

      if (image != null) {
        setState(() {
          _faturaPathSelecionado = image.path;
          _nomeFaturaSelecionada = image.name;
          _faturaUrlExistente = null;
        });
        _mostrarMensagem('Fatura selecionada: $_nomeFaturaSelecionada');
      }
    } catch (e) {
      _mostrarMensagem('Erro ao obter imagem: $e');
    }
  }

  void _mostrarOpcoesFatura(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (BuildContext context) {
        return SafeArea(
          child: Wrap(
            children: <Widget>[
              ListTile(
                leading: const Icon(Icons.photo_camera, color: Colors.blue),
                title: const Text('Tirar Foto (Câmara)'),
                onTap: () {
                  Navigator.of(context).pop();
                  _procurarFatura(ImageSource.camera);
                },
              ),
              ListTile(
                leading: const Icon(Icons.photo_library, color: Colors.green),
                title: const Text('Escolher da Galeria'),
                onTap: () {
                  Navigator.of(context).pop();
                  _procurarFatura(ImageSource.gallery);
                },
              ),
            ],
          ),
        );
      },
    );
  }

  Future<void> _abrirFatura() async {
    try {
      Uri? uri;
      if (_faturaPathSelecionado != null && _faturaPathSelecionado!.isNotEmpty) {
        if (kIsWeb) {
          uri = Uri.tryParse(_faturaPathSelecionado!);
        } else {
          final file = File(_faturaPathSelecionado!);
          if (await file.exists()) uri = Uri.file(file.path);
        }
      } else if (_faturaUrlExistente != null && _faturaUrlExistente!.isNotEmpty) {
        String caminhoOuUrl = _faturaUrlExistente!.trim();
        if (!caminhoOuUrl.startsWith('http://') && !caminhoOuUrl.startsWith('https://') && !caminhoOuUrl.startsWith('blob:')) {
          const String baseUrl = 'http://localhost:8080/'; 
          if (caminhoOuUrl.startsWith('/')) caminhoOuUrl = caminhoOuUrl.substring(1);
          caminhoOuUrl = '$baseUrl$caminhoOuUrl';
        }
        uri = Uri.tryParse(caminhoOuUrl);
      }

      if (uri != null && await canLaunchUrl(uri)) {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
      } else {
        _mostrarMensagem('Não foi possível abrir o ficheiro da fatura.');
      }
    } catch (e) {
      _mostrarMensagem('Erro ao abrir fatura: $e');
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
      unidade: double.tryParse(_unidadeController.text),
      cartaoId: _selectedCartaoId,
      carroId: _selectedCarroId,
      motoristaId: _selectedMotoristaId,
      faturaUrl: _faturaUrlExistente,
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
    final bool temFatura = (_faturaPathSelecionado != null) || (_faturaUrlExistente != null && _faturaUrlExistente!.isNotEmpty);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Gestão de Despesas'),
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
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Center(
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 600),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          DropdownButtonFormField<int>(
                            value: _selectedCartaoId != null && _cartoes.any((c) => (c.id is int ? c.id : int.tryParse(c.id.toString())) == _selectedCartaoId) ? _selectedCartaoId : null,
                            decoration: const InputDecoration(
                              labelText: 'Cartão',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.credit_card),
                            ),
                            items: _cartoes.map((Cartao cartao) {
                              final idVal = cartao.id is int ? cartao.id as int : int.tryParse(cartao.id.toString()) ?? 0;
                              return DropdownMenuItem<int>(
                                value: idVal,
                                child: Text('${cartao.numeroCartao} (${cartao.tipo})'),
                              );
                            }).toList(),
                            onChanged: (value) => setState(() => _selectedCartaoId = value),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _nomeController,
                            decoration: const InputDecoration(
                              labelText: 'Nome da Despesa',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.label),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _descricaoController,
                            decoration: const InputDecoration(
                              labelText: 'Descrição',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.description),
                            ),
                          ),
                          const SizedBox(height: 12),
                          DropdownButtonFormField<int>(
                            value: _selectedCarroId != null && _carros.any((c) => (c.id is int ? c.id : int.tryParse(c.id.toString())) == _selectedCarroId) ? _selectedCarroId : null,
                            decoration: const InputDecoration(
                              labelText: 'Carro',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.directions_car),
                            ),
                            items: _carros.map((Carro carro) {
                              final idVal = carro.id is int ? carro.id as int : int.tryParse(carro.id.toString()) ?? 0;
                              return DropdownMenuItem<int>(
                                value: idVal,
                                child: Text('${carro.matricula} (${carro.marcaModelo})'),
                              );
                            }).toList(),
                            onChanged: (value) => setState(() => _selectedCarroId = value),
                          ),
                          const SizedBox(height: 12),
                          DropdownButtonFormField<int>(
                            value: _selectedMotoristaId != null && _motoristas.any((m) => (m.id is int ? m.id : int.tryParse(m.id.toString())) == _selectedMotoristaId) ? _selectedMotoristaId : null,
                            decoration: const InputDecoration(
                              labelText: 'Motorista',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.person),
                            ),
                            items: _motoristas.map((Colaborador colab) {
                              final idVal = colab.id is int ? colab.id as int : int.tryParse(colab.id.toString()) ?? 0;
                              return DropdownMenuItem<int>(
                                value: idVal,
                                child: Text(colab.nome),
                              );
                            }).toList(),
                            onChanged: (value) => setState(() => _selectedMotoristaId = value),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _dataController,
                            readOnly: true,
                            decoration: InputDecoration(
                              labelText: 'Data',
                              border: const OutlineInputBorder(),
                              prefixIcon: const Icon(Icons.calendar_today),
                              suffixIcon: IconButton(
                                icon: const Icon(Icons.edit_calendar),
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
                              prefixIcon: Icon(Icons.euro),
                            ),
                            keyboardType: const TextInputType.numberWithOptions(decimal: true),
                          ),
                          const SizedBox(height: 16),
                          Card(
                            elevation: 0,
                            shape: RoundedRectangleBorder(
                              side: BorderSide(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Padding(
                              padding: const EdgeInsets.all(12.0),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'Informação de Abastecimento (Opcional)',
                                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                                  ),
                                  const SizedBox(height: 12),
                                  TextField(
                                    controller: _quantidadeController,
                                    decoration: const InputDecoration(
                                      labelText: 'Quantidade',
                                      border: OutlineInputBorder(),
                                      prefixIcon: Icon(Icons.format_list_numbered),
                                    ),
                                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                                  ),
                                  const SizedBox(height: 12),
                                  TextField(
                                    controller: _unidadeController,
                                    decoration: const InputDecoration(
                                      labelText: 'Unidade (ex: Litros)',
                                      border: OutlineInputBorder(),
                                      prefixIcon: Icon(Icons.straighten),
                                    ),
                                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                                  ),
                                ],
                              ),
                            ),
                          ),
                          const SizedBox(height: 16),
                          Card(
                            elevation: 0,
                            shape: RoundedRectangleBorder(
                              side: BorderSide(color: Colors.grey.shade400),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Padding(
                              padding: const EdgeInsets.all(12.0),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  Row(
                                    children: [
                                      const Icon(Icons.receipt_long, color: Colors.blueGrey),
                                      const SizedBox(width: 12),
                                      Expanded(
                                        child: Text(
                                          _nomeFaturaSelecionada != null
                                              ? 'Fatura: $_nomeFaturaSelecionada'
                                              : (_faturaUrlExistente != null && _faturaUrlExistente!.isNotEmpty
                                                  ? 'Fatura guardada no sistema'
                                                  : 'Nenhuma fatura anexada'),
                                          overflow: TextOverflow.ellipsis,
                                          style: TextStyle(
                                            color: temFatura ? Colors.green.shade700 : Colors.grey.shade700,
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                      ),
                                      ElevatedButton.icon(
                                        onPressed: () => _mostrarOpcoesFatura(context),
                                        icon: const Icon(Icons.camera_alt),
                                        label: const Text('Anexar'),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: Colors.blue,
                                          foregroundColor: Colors.white,
                                        ),
                                      ),
                                    ],
                                  ),
                                  if (temFatura) ...[
                                    const SizedBox(height: 10),
                                    OutlinedButton.icon(
                                      onPressed: _abrirFatura,
                                      icon: const Icon(Icons.visibility, color: Colors.blue),
                                      label: const Text('Ver Fatura', style: TextStyle(color: Colors.blue)),
                                      style: OutlinedButton.styleFrom(
                                        side: const BorderSide(color: Colors.blue),
                                      ),
                                    ),
                                  ],
                                ],
                              ),
                            ),
                          ),
                          const SizedBox(height: 20),
                          Wrap(
                            spacing: 8.0,
                            runSpacing: 8.0,
                            alignment: WrapAlignment.center,
                            children: [
                              ElevatedButton.icon(
                                onPressed: _registarOuGravarDespesa,
                                icon: const Icon(Icons.save),
                                label: Text(_selectedDespesa == null ? 'Registar' : 'Atualizar'),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: Colors.green,
                                  foregroundColor: Colors.white,
                                ),
                              ),
                              ElevatedButton.icon(
                                onPressed: _selectedDespesa != null ? _eliminarDespesa : null,
                                icon: const Icon(Icons.delete),
                                label: const Text('Eliminar'),
                                style: ElevatedButton.styleFrom(
                                  backgroundColor: Colors.red,
                                  foregroundColor: Colors.white,
                                ),
                              ),
                              ElevatedButton.icon(
                                onPressed: _limparFormulario,
                                icon: const Icon(Icons.clear),
                                label: const Text('Limpar'),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 30),
                  Center(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Despesas Registadas',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 12),
                        _despesas.isEmpty
                            ? const Padding(
                                padding: EdgeInsets.all(20.0),
                                child: Text('Nenhuma despesa encontrada.'),
                              )
                            : Container(
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
                                          DataCell(Text(despesa.unidade?.toString() ?? '')),
                                          DataCell(
                                            Text(
                                              despesa.faturaUrl != null && despesa.faturaUrl!.isNotEmpty
                                                  ? 'Sim'
                                                  : 'Não',
                                            ),
                                          ),
                                        ],
                                      );
                                    }).toList(),
                                  ),
                                ),
                              ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}