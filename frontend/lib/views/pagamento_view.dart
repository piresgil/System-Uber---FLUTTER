import 'package:flutter/material.dart';
import '../models/pagamento.dart';
import '../services/pagamento_service.dart';

class PagamentoView extends StatefulWidget {
  const PagamentoView({super.key});

  @override
  State<PagamentoView> createState() => _PagamentoViewState();
}

class _PagamentoViewState extends State<PagamentoView> {
  final _formKey = GlobalKey<FormState>();
  final _pagamentoService = PagamentoService();

  String? _selectedPagamentoId;

  final TextEditingController _colaboradorController = TextEditingController();
  final TextEditingController _cartaoController = TextEditingController();
  final TextEditingController _plataformaController = TextEditingController();
  final TextEditingController _valorController = TextEditingController();
  final TextEditingController _tipoController = TextEditingController();

  DateTime _dataSelecionada = DateTime.now();

  @override
  void dispose() {
    _colaboradorController.dispose();
    _cartaoController.dispose();
    _plataformaController.dispose();
    _valorController.dispose();
    _tipoController.dispose();
    super.dispose();
  }

  void _carregarParaEdicao(Pagamento p) {
    setState(() {
      _selectedPagamentoId = p.id;
      _colaboradorController.text = p.colaborador;
      _cartaoController.text = p.cartao ?? '';
      _plataformaController.text = p.plataforma;
      _valorController.text = p.valor.toString();
      _tipoController.text = p.tipo;
      _dataSelecionada = p.data;
    });
  }

  void _limparFormulario() {
    setState(() {
      _selectedPagamentoId = null;
      _colaboradorController.clear();
      _cartaoController.clear();
      _plataformaController.clear();
      _valorController.clear();
      _tipoController.clear();
      _dataSelecionada = DateTime.now();
    });
  }

  Future<void> _salvar() async {
    if (!_formKey.currentState!.validate()) return;

    final novoPagamento = Pagamento(
      id: _selectedPagamentoId,
      colaborador: _colaboradorController.text,
      cartao: _cartaoController.text.isEmpty ? null : _cartaoController.text,
      plataforma: _plataformaController.text,
      data: _dataSelecionada,
      valor: double.tryParse(_valorController.text) ?? 0.0,
      tipo: _tipoController.text,
    );

    try {
      if (_selectedPagamentoId != null) {
        await _pagamentoService.update(_selectedPagamentoId!, novoPagamento);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Pagamento atualizado com sucesso!')),
          );
        }
      } else {
        // Alinhado com o método create do seu PagamentoService
        await _pagamentoService.create(novoPagamento);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Pagamento registado com sucesso!')),
          );
        }
      }
      _limparFormulario();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro ao salvar: $e')),
        );
      }
    }
  }

  Future<void> _eliminar() async {
    if (_selectedPagamentoId == null) return;

    try {
      await _pagamentoService.delete(_selectedPagamentoId!);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Pagamento eliminado com sucesso!')),
        );
      }
      _limparFormulario();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro ao eliminar: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              _selectedPagamentoId == null
                  ? 'Registar Novo Pagamento'
                  : 'Editar Pagamento',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _colaboradorController,
              decoration: const InputDecoration(
                labelText: 'Colaborador',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  v == null || v.isEmpty ? 'Informe o colaborador' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _cartaoController,
              decoration: const InputDecoration(
                labelText: 'Cartão (Opcional)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _plataformaController,
              decoration: const InputDecoration(
                labelText: 'Plataforma',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  v == null || v.isEmpty ? 'Informe a plataforma' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _valorController,
              keyboardType:
                  const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Valor (€)',
                border: OutlineInputBorder(),
              ),
              validator: (v) {
                if (v == null || v.isEmpty) return 'Informe o valor';
                if (double.tryParse(v) == null) return 'Valor inválido';
                return null;
              },
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _tipoController,
              decoration: const InputDecoration(
                labelText: 'Tipo de Pagamento',
                border: OutlineInputBorder(),
              ),
              validator: (v) =>
                  v == null || v.isEmpty ? 'Informe o tipo' : null,
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _salvar,
                    icon: const Icon(Icons.save),
                    label: Text(_selectedPagamentoId == null
                        ? 'Guardar'
                        : 'Atualizar'),
                  ),
                ),
                if (_selectedPagamentoId != null) ...[
                  const SizedBox(width: 8),
                  ElevatedButton.icon(
                    onPressed: _eliminar,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                    icon: const Icon(Icons.delete),
                    label: const Text('Eliminar'),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: _limparFormulario,
                    icon: const Icon(Icons.clear),
                    tooltip: 'Cancelar edição',
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }
}