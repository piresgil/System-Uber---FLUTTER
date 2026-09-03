import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/carro.dart';
import '../models/despesa.dart';
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
  final TextEditingController _dataCompraController = TextEditingController();

  bool _isAlugado = false;
  bool _isAtivo = true;
  bool _isLoading = true;
  Carro? _selectedCarro;
  List<Carro> _carros = [];

  String? _documentoUrl;
  String? _seguroUrl;
  String? _inspecaoUrl;

  Uint8List? _documentoBytes;
  Uint8List? _seguroBytes;
  Uint8List? _inspecaoBytes;

  List<Despesa> _despesasTemporarias = [];

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
    _dataCompraController.dispose();
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
      _dataCompraController.clear();
      _isAlugado = false;
      _isAtivo = true;
      _documentoUrl = null;
      _seguroUrl = null;
      _inspecaoUrl = null;
      _documentoBytes = null;
      _seguroBytes = null;
      _inspecaoBytes = null;
      _despesasTemporarias = [];
    });
  }

  void _selecionarCarro(Carro carro) {
    setState(() {
      _selectedCarro = carro;
      _marcaController.text = carro.marca;
      _modeloController.text = carro.modelo;
      _matriculaController.text = carro.matricula;
      _kilometragemController.text = carro.kilometragem?.toString() ?? '';
      _dataCompraController.text = carro.dataUltimoReinicio;
      _isAlugado = carro.alugado;
      _isAtivo = carro.ativo;
      _documentoUrl = carro.documentoUrl;
      _seguroUrl = carro.seguroUrl;
      _inspecaoUrl = carro.inspecaoUrl;
      _documentoBytes = null;
      _seguroBytes = null;
      _inspecaoBytes = null;
      _despesasTemporarias = List.from(carro.despesas);
    });
  }

  Future<void> _escolherImagem(String tipo) async {
    try {
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(source: ImageSource.gallery, imageQuality: 70);
      if (image != null) {
        final bytes = await image.readAsBytes();
        setState(() {
          if (tipo == 'documento') {
            _documentoBytes = bytes;
            _documentoUrl = image.name;
          } else if (tipo == 'seguro') {
            _seguroBytes = bytes;
            _seguroUrl = image.name;
          } else if (tipo == 'inspecao') {
            _inspecaoBytes = bytes;
            _inspecaoUrl = image.name;
          }
        });
      }
    } catch (e) {
      _mostrarMensagem('Erro ao carregar imagem: $e');
    }
  }

  Future<void> _selecionarData() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        _dataCompraController.text = "${picked.year}-${picked.month.toString().padLeft(2, '0')}-${picked.day.toString().padLeft(2, '0')}";
      });
    }
  }

  void _adicionarDespesaModal() {
    final descricaoController = TextEditingController();
    final valorController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Adicionar Despesa', style: TextStyle(fontSize: 14)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: descricaoController, decoration: const InputDecoration(labelText: 'Descrição / Nome', isDense: true)),
            const SizedBox(height: 8),
            TextField(controller: valorController, decoration: const InputDecoration(labelText: 'Valor (€)', isDense: true), keyboardType: TextInputType.number),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () {
              final val = double.tryParse(valorController.text);
              if (descricaoController.text.isNotEmpty && val != null) {
                setState(() {
                  _despesasTemporarias.add(Despesa(
                    nome: descricaoController.text,
                    descricao: descricaoController.text,
                    valor: val,
                  ));
                });
                Navigator.pop(context);
              }
            },
            child: const Text('Adicionar'),
          ),
        ],
      ),
    );
  }

  Future<void> _registarOuGravarCarro() async {
    if (_marcaController.text.isEmpty ||
        _modeloController.text.isEmpty ||
        _matriculaController.text.isEmpty ||
        _kilometragemController.text.isEmpty) {
      _mostrarMensagem('Preencha os campos obrigatórios.');
      return;
    }

    final double? kilometragem = double.tryParse(_kilometragemController.text);
    if (kilometragem == null) {
      _mostrarMensagem('Quilometragem inválida.');
      return;
    }

    final carro = Carro(
      id: _selectedCarro?.id,
      marca: _marcaController.text,
      modelo: _modeloController.text,
      matricula: _matriculaController.text,
      alugado: _isAlugado,
      kilometragem: kilometragem,
      ativo: _isAtivo,
      dataUltimoReinicio: _dataCompraController.text,
      documentoUrl: _documentoUrl,
      seguroUrl: _seguroUrl,
      inspecaoUrl: _inspecaoUrl,
      despesas: _despesasTemporarias,
    );

    try {
      setState(() => _isLoading = true);
      if (_selectedCarro == null) {
        await _carroService.create(carro);
        _mostrarMensagem('Carro registado com sucesso!');
      } else {
        await _carroService.update(_selectedCarro!.id, carro);
        _mostrarMensagem('Carro atualizado com sucesso!');
      }
      _limparFormulario();
      await _carregarCarros();
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao gravar: $e');
    }
  }

  Future<void> _eliminarCarro() async {
    if (_selectedCarro == null || _selectedCarro!.id == null) return;
    try {
      setState(() => _isLoading = true);
      await _carroService.delete(_selectedCarro!.id);
      _mostrarMensagem('Carro eliminado com sucesso!');
      _limparFormulario();
      await _carregarCarros();
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao eliminar: $e');
    }
  }

  void _mostrarMensagem(String mensagem) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mensagem), duration: const Duration(seconds: 2)));
    }
  }

  String _resolverUrl(String? url) {
    if (url == null || url.isEmpty) return '';
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) {
      return Uri.encodeFull(url);
    }
    String caminhoFinal = url;
    if (!caminhoFinal.startsWith('/')) {
      caminhoFinal = '/$caminhoFinal';
    }
    return Uri.encodeFull('http://localhost:8080$caminhoFinal');
  }

  Future<void> _abrirFicheiroNoBrowser(String? url) async {
    final resolvedUrl = _resolverUrl(url);
    if (resolvedUrl.isEmpty) {
      _mostrarMensagem('Nenhum documento anexado.');
      return;
    }
    final uri = Uri.parse(resolvedUrl);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      await launchUrl(uri, mode: LaunchMode.platformDefault);
    }
  }

  Widget _buildFileRow(String titulo, String? url, VoidCallback onSelected, VoidCallback onRemoved) {
    bool temFicheiro = url != null && url.isNotEmpty;

    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        border: Border.all(color: Colors.grey.shade300),
        borderRadius: BorderRadius.circular(6),
        color: Colors.grey.shade50,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(
                  temFicheiro ? '$titulo: ${url.split('/').last}' : '$titulo: (Vazio)',
                  style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  InkWell(onTap: onSelected, child: const Text('Anexar', style: TextStyle(fontSize: 10, color: Colors.blue, fontWeight: FontWeight.w600))),
                  if (temFicheiro) ...[
                    const SizedBox(width: 8),
                    InkWell(onTap: onRemoved, child: const Text('Remover', style: TextStyle(fontSize: 10, color: Colors.red, fontWeight: FontWeight.w600))),
                  ]
                ],
              )
            ],
          ),
          if (temFicheiro) ...[
            const SizedBox(height: 6),
            SizedBox(
              height: 26,
              child: OutlinedButton.icon(
                onPressed: () => _abrirFicheiroNoBrowser(url),
                icon: const Icon(Icons.visibility, size: 12),
                label: Text('Ver $titulo', style: const TextStyle(fontSize: 10)),
                style: OutlinedButton.styleFrom(
                  padding: EdgeInsets.zero,
                  side: const BorderSide(color: Colors.blue),
                  foregroundColor: Colors.blue,
                ),
              ),
            ),
          ]
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Gestão de Carros', style: TextStyle(fontSize: 16)),
        centerTitle: true,
        toolbarHeight: 40,
        actions: [IconButton(icon: const Icon(Icons.refresh, size: 18), onPressed: _carregarCarros)],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : LayoutBuilder(
              builder: (context, constraints) {
                bool isNarrow = constraints.maxWidth < 900;

                return SingleChildScrollView(
                  padding: const EdgeInsets.all(12.0),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(minHeight: constraints.maxHeight - 24),
                    child: isNarrow
                        ? Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              _buildFormularioCard(),
                              const SizedBox(height: 12),
                              SizedBox(
                                height: 500,
                                child: _buildTabelaCard(),
                              ),
                            ],
                          )
                        : SizedBox(
                            height: 680,
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              children: [
                                Expanded(flex: 3, child: _buildFormularioCard()),
                                const SizedBox(width: 12),
                                Expanded(flex: 3, child: _buildTabelaCard()),
                              ],
                            ),
                          ),
                  ),
                );
              },
            ),
    );
  }

  Widget _buildFormularioCard() {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text('Dados do Carro', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.blueGrey)),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(child: TextField(controller: _marcaController, decoration: const InputDecoration(labelText: 'Marca', isDense: true, border: OutlineInputBorder()))),
                  const SizedBox(width: 10),
                  Expanded(child: TextField(controller: _modeloController, decoration: const InputDecoration(labelText: 'Modelo', isDense: true, border: OutlineInputBorder()))),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(child: TextField(controller: _matriculaController, decoration: const InputDecoration(labelText: 'Matrícula', isDense: true, border: OutlineInputBorder()))),
                  const SizedBox(width: 10),
                  Expanded(child: TextField(controller: _kilometragemController, decoration: const InputDecoration(labelText: 'Quilometragem', isDense: true, border: OutlineInputBorder()), keyboardType: TextInputType.number)),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    flex: 2,
                    child: TextField(
                      controller: _dataCompraController,
                      readOnly: true,
                      onTap: _selecionarData,
                      decoration: const InputDecoration(
                        labelText: 'Data de Compra',
                        isDense: true,
                        border: OutlineInputBorder(),
                        suffixIcon: Icon(Icons.calendar_today, size: 16),
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    flex: 1,
                    child: CheckboxListTile(
                      title: const Text('Alugado', style: TextStyle(fontSize: 11)),
                      value: _isAlugado,
                      onChanged: (v) => setState(() => _isAlugado = v ?? false),
                      controlAffinity: ListTileControlAffinity.leading,
                      contentPadding: EdgeInsets.zero,
                      dense: true,
                    ),
                  ),
                  Expanded(
                    flex: 1,
                    child: CheckboxListTile(
                      title: const Text('Ativo', style: TextStyle(fontSize: 11)),
                      value: _isAtivo,
                      onChanged: (v) => setState(() => _isAtivo = v ?? true),
                      controlAffinity: ListTileControlAffinity.leading,
                      contentPadding: EdgeInsets.zero,
                      dense: true,
                    ),
                  ),
                ],
              ),
              const Divider(height: 24),
              const Text('Documentos:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, color: Colors.blueGrey)),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(child: _buildFileRow('Documento', _documentoUrl, () => _escolherImagem('documento'), () => setState(() { _documentoBytes = null; _documentoUrl = null; }))),
                  const SizedBox(width: 8),
                  Expanded(child: _buildFileRow('Seguro', _seguroUrl, () => _escolherImagem('seguro'), () => setState(() { _seguroBytes = null; _seguroUrl = null; }))),
                  const SizedBox(width: 8),
                  Expanded(child: _buildFileRow('Inspeção', _inspecaoUrl, () => _escolherImagem('inspecao'), () => setState(() { _inspecaoBytes = null; _inspecaoUrl = null; }))),
                ],
              ),
              const Divider(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Despesas do Carro:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, color: Colors.blueGrey)),
                  TextButton.icon(
                    onPressed: _adicionarDespesaModal,
                    icon: const Icon(Icons.add, size: 14),
                    label: const Text('Adicionar', style: TextStyle(fontSize: 10)),
                    style: TextButton.styleFrom(padding: EdgeInsets.zero, minimumSize: const Size(50, 20)),
                  ),
                ],
              ),
              Container(
                height: 100,
                decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade200), borderRadius: BorderRadius.circular(4)),
                child: _despesasTemporarias.isEmpty
                    ? const Center(child: Text('Sem despesas registadas.', style: TextStyle(fontSize: 10, color: Colors.grey)))
                    : ListView.builder(
                        itemCount: _despesasTemporarias.length,
                        itemBuilder: (context, index) {
                          final d = _despesasTemporarias[index];
                          return ListTile(
                            dense: true,
                            contentPadding: const EdgeInsets.symmetric(horizontal: 8),
                            title: Text(d.descricao ?? '', style: const TextStyle(fontSize: 11)),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text('${d.valor} €', style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                                IconButton(
                                  icon: const Icon(Icons.delete, size: 14, color: Colors.red),
                                  onPressed: () => setState(() => _despesasTemporarias.removeAt(index)),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                alignment: WrapAlignment.center,
                children: [
                  ElevatedButton.icon(
                    onPressed: _registarOuGravarCarro,
                    icon: const Icon(Icons.save, size: 14),
                    label: Text(_selectedCarro == null ? 'Registar' : 'Gravar', style: const TextStyle(fontSize: 11)),
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.green.shade600, foregroundColor: Colors.white),
                  ),
                  ElevatedButton.icon(
                    onPressed: _selectedCarro != null ? _eliminarCarro : null,
                    icon: const Icon(Icons.delete, size: 14),
                    label: const Text('Eliminar', style: TextStyle(fontSize: 11)),
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.grey.shade300, foregroundColor: Colors.black87),
                  ),
                  OutlinedButton.icon(
                    onPressed: _limparFormulario,
                    icon: const Icon(Icons.clear, size: 14),
                    label: const Text('Limpar', style: TextStyle(fontSize: 11)),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

 Widget _buildTabelaCard() {
    return Card(
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('Lista de Carros', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.blueGrey)),
            ),
            Expanded(
              child: _carros.isEmpty
                  ? const Center(child: Text('Nenhum carro registado.', style: TextStyle(fontSize: 11, color: Colors.grey)))
                  : Align(
                      alignment: Alignment.topCenter, // Mantém no topo na vertical e centrado na horizontal
                      child: SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: SingleChildScrollView(
                          child: DataTable(
                            showCheckboxColumn: false,
                            headingRowHeight: 40,
                            dataRowMinHeight: 36,
                            dataRowMaxHeight: 40,
                            columnSpacing: 16,
                            columns: const [
                              DataColumn(label: Text('Matrícula', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Marca', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Modelo', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Quilometragem', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Data Compra', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Alugado', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                              DataColumn(label: Text('Ativo', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold))),
                            ],
                            rows: _carros.map((carro) {
                              final isSelected = _selectedCarro?.id == carro.id;
                              return DataRow(
                                selected: isSelected,
                                onSelectChanged: (_) => _selecionarCarro(carro),
                                cells: [
                                  DataCell(Text(carro.matricula, style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.marca, style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.modelo, style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.kilometragem?.toString() ?? '', style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.dataUltimoReinicio, style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.alugado ? 'Sim' : 'Não', style: const TextStyle(fontSize: 13))),
                                  DataCell(Text(carro.ativo ? 'Sim' : 'Não', style: const TextStyle(fontSize: 13))),
                                ],
                              );
                            }).toList(),
                          ),
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