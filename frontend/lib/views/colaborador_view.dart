import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../models/colaborador.dart';
import '../services/colaborador_service.dart';

class ColaboradorView extends StatefulWidget {
  const ColaboradorView({super.key});

  @override
  State<ColaboradorView> createState() => _ColaboradorViewState();
}

class _ColaboradorViewState extends State<ColaboradorView> {
  final ColaboradorService _colaboradorService = ColaboradorService();

  final TextEditingController _nomeController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _telefoneController = TextEditingController();

  String? _selectedTipo;
  XFile? _documentImage;
  final ImagePicker _picker = ImagePicker();

  bool _isLoading = true;
  Colaborador? _selectedColaborador;
  List<Colaborador> _colaboradores = [];

  final List<String> _tipos = ['Motorista', 'Gestor', 'Suporte', 'Outro'];

  @override
  void initState() {
    super.initState();
    _carregarColaboradores();
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _emailController.dispose();
    _telefoneController.dispose();
    super.dispose();
  }

  Future<void> _carregarColaboradores() async {
    setState(() => _isLoading = true);
    try {
      final dados = await _colaboradorService.getAll();
      setState(() {
        _colaboradores = dados;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarMensagem('Erro ao carregar colaboradores: $e');
    }
  }

  Future<void> _pickImage(ImageSource source) async {
    try {
      final XFile? pickedFile = await _picker.pickImage(
        source: source,
        maxWidth: 1920,
        maxHeight: 1080,
        imageQuality: 85,
      );
      if (pickedFile != null) {
        setState(() {
          _documentImage = pickedFile;
        });
      }
    } catch (e) {
      _mostrarMensagem('Erro ao selecionar imagem: $e');
    }
  }

  void _limparFormulario() {
    setState(() {
      _selectedColaborador = null;
      _nomeController.clear();
      _emailController.clear();
      _telefoneController.clear();
      _selectedTipo = null;
      _documentImage = null;
    });
  }

  void _selecionarColaborador(Colaborador colab) {
    setState(() {
      _selectedColaborador = colab;
      _nomeController.text = colab.nome;
      _emailController.text = colab.email ?? '';
      _telefoneController.text = colab.telefone ?? '';
      _selectedTipo = _tipos.contains(colab.tipo) ? colab.tipo : null;
      _documentImage = null;
    });
  }

  Future<void> _registarOuGravarColaborador() async {
    if (_nomeController.text.isEmpty) {
      _mostrarMensagem('Por favor, introduza o nome do colaborador.');
      return;
    }

    final novoColaborador = Colaborador(
      id: _selectedColaborador?.id,
      nome: _nomeController.text,
      email: _emailController.text.isNotEmpty ? _emailController.text : null,
      telefone: _telefoneController.text.isNotEmpty ? _telefoneController.text : null,
      tipo: _selectedTipo,
      documentoUrl: _documentImage?.path,
    );

    try {
      if (_selectedColaborador == null) {
        await _colaboradorService.create(novoColaborador);
        _mostrarMensagem('Colaborador registado com sucesso!');
      } else {
        await _colaboradorService.create(novoColaborador);
        _mostrarMensagem('Colaborador atualizado com sucesso!');
      }
      _limparFormulario();
      _carregarColaboradores();
    } catch (e) {
      _mostrarMensagem('Erro ao guardar colaborador: $e');
    }
  }

  Future<void> _eliminarColaborador() async {
    if (_selectedColaborador == null || _selectedColaborador!.id == null) {
      _mostrarMensagem('Selecione um colaborador para eliminar.');
      return;
    }

    try {
      await _colaboradorService.delete(_selectedColaborador!.id!);
      _mostrarMensagem('Colaborador eliminado com sucesso!');
      _limparFormulario();
      _carregarColaboradores();
    } catch (e) {
      _mostrarMensagem('Erro ao eliminar colaborador: $e');
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
        title: const Text('Colaboradores'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregarColaboradores,
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
                          TextField(
                            controller: _nomeController,
                            decoration: const InputDecoration(
                              labelText: 'Nome',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _emailController,
                            decoration: const InputDecoration(
                              labelText: 'E-mail',
                              border: OutlineInputBorder(),
                            ),
                            keyboardType: TextInputType.emailAddress,
                          ),
                          const SizedBox(height: 12),
                          TextField(
                            controller: _telefoneController,
                            decoration: const InputDecoration(
                              labelText: 'Telefone',
                              border: OutlineInputBorder(),
                            ),
                            keyboardType: TextInputType.phone,
                          ),
                          const SizedBox(height: 12),
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
                          const SizedBox(height: 16),

                          // Área de Carregamento do Documento/Foto
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              border: Border.all(color: Colors.grey),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              children: [
                                const Text(
                                  'Documento do Colaborador (CC / Carta de Condução)',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                const SizedBox(height: 8),
                                if (_documentImage != null)
                                  Container(
                                    height: 150,
                                    margin: const EdgeInsets.only(bottom: 8),
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(8),
                                      image: DecorationImage(
                                        image: kIsWeb
                                            ? NetworkImage(_documentImage!.path)
                                            : FileImage(File(_documentImage!.path)) as ImageProvider,
                                        fit: BoxFit.cover,
                                      ),
                                    ),
                                  ),
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                  children: [
                                    ElevatedButton.icon(
                                      onPressed: () => _pickImage(ImageSource.gallery),
                                      icon: const Icon(Icons.photo_library),
                                      label: const Text('Galeria/Ficheiro'),
                                    ),
                                    ElevatedButton.icon(
                                      onPressed: () => _pickImage(ImageSource.camera),
                                      icon: const Icon(Icons.camera_alt),
                                      label: const Text('Câmara'),
                                    ),
                                  ],
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
                                onPressed: _selectedColaborador != null ? _registarOuGravarColaborador : null,
                                child: const Text('Editar'),
                              ),
                              ElevatedButton(
                                onPressed: _selectedColaborador != null ? _eliminarColaborador : null,
                                child: const Text('Eliminar'),
                              ),
                              ElevatedButton(
                                onPressed: _registarOuGravarColaborador,
                                child: const Text('Gravar'),
                              ),
                              ElevatedButton(
                                onPressed: _limparFormulario,
                                child: const Text('Registar'),
                              ),
                            ],
                          ),
                          const SizedBox(height: 20),

                          // TableView Equivalente
                          SizedBox(
                            width: double.infinity,
                            child: SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: DataTable(
                                showCheckboxColumn: false,
                                columns: const [
                                  DataColumn(label: Text('Nome')),
                                  DataColumn(label: Text('Email')),
                                  DataColumn(label: Text('Telefone')),
                                  DataColumn(label: Text('Tipo')),
                                ],
                                rows: _colaboradores.map((colab) {
                                  final isSelected = _selectedColaborador?.id == colab.id;
                                  return DataRow(
                                    selected: isSelected,
                                    onSelectChanged: (_) => _selecionarColaborador(colab),
                                    cells: [
                                      DataCell(Text(colab.nome)),
                                      DataCell(Text(colab.email ?? '')),
                                      DataCell(Text(colab.telefone ?? '')),
                                      DataCell(Text(colab.tipo ?? '')),
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