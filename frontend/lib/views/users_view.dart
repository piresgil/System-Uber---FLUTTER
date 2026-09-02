import 'package:flutter/material.dart';
import '../models/user.dart'; // Ajusta o import conforme a tua estrutura
import '../services/user_service.dart';

class UsersView extends StatefulWidget {
  const UsersView({super.key});

  @override
  State<UsersView> createState() => _UsersViewState();
}

class _UsersViewState extends State<UsersView> {
  final _formKey = GlobalKey<FormState>();
  final UserService _userService = UserService();

  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();

  String _selectedRole = 'USER';
  String? _selectedUserId;
  bool _isLoading = false;

  List<User> _utilizadores = [];
  final List<String> _roles = ['ADMIN', 'MANAGER', 'USER'];

  @override
  void initState() {
    super.initState();
    _carregarUtilizadores();
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _carregarUtilizadores() async {
    setState(() => _isLoading = true);
    try {
      final dados = await _userService.getAll();
      setState(() {
        _utilizadores = dados;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarSnackBar('Erro ao carregar utilizadores: $e');
    }
  }

  void _limparFormulario() {
    setState(() {
      _selectedUserId = null;
      _selectedRole = 'USER';
      _usernameController.clear();
      _emailController.clear();
    });
    _formKey.currentState?.reset();
  }

  void _selecionarParaEdicao(User user) {
    setState(() {
      _selectedUserId = user.id;
      _usernameController.text = user.username;
      _emailController.text = user.email;
      _selectedRole = _roles.contains(user.role) ? user.role : 'USER';
    });
  }

  Future<void> _salvarOuAtualizar() async {
    if (!_formKey.currentState!.validate()) return;

    final novoUsuario = User(
      id: _selectedUserId,
      username: _usernameController.text.trim(),
      email: _emailController.text.trim(),
      role: _selectedRole,
    );

    setState(() => _isLoading = true);
    try {
      if (_selectedUserId == null) {
        await _userService.create(novoUsuario);
        _mostrarSnackBar('Utilizador registado com sucesso!');
      } else {
        await _userService.update(_selectedUserId!, novoUsuario);
        _mostrarSnackBar('Utilizador atualizado com sucesso!');
      }
      _limparFormulario();
      await _carregarUtilizadores();
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarSnackBar('Erro ao guardar: $e');
    }
  }

  Future<void> _eliminar() async {
    if (_selectedUserId == null) {
      _mostrarSnackBar('Selecione um utilizador na tabela para eliminar.');
      return;
    }

    setState(() => _isLoading = true);
    try {
      await _userService.delete(_selectedUserId!);
      _mostrarSnackBar('Utilizador eliminado com sucesso!');
      _limparFormulario();
      await _carregarUtilizadores();
    } catch (e) {
      setState(() => _isLoading = false);
      _mostrarSnackBar('Erro ao eliminar: $e');
    }
  }

  void _mostrarSnackBar(String mensagem) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mensagem)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Gestão de Utilizadores'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregarUtilizadores,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(16.0),
              child: Form(
                key: _formKey,
                child: Column(
                  children: [
                    Expanded(
                      child: SingleChildScrollView(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            // Username
                            TextFormField(
                              controller: _usernameController,
                              decoration: const InputDecoration(
                                labelText: 'Username',
                                border: OutlineInputBorder(),
                                prefixIcon: Icon(Icons.person),
                              ),
                              validator: (v) =>
                                  v == null || v.trim().isEmpty ? 'Introduza o username' : null,
                            ),
                            const SizedBox(height: 12),

                            // E-mail
                            TextFormField(
                              controller: _emailController,
                              decoration: const InputDecoration(
                                labelText: 'E-mail',
                                border: OutlineInputBorder(),
                                prefixIcon: Icon(Icons.email),
                              ),
                              keyboardType: TextInputType.emailAddress,
                              validator: (v) {
                                if (v == null || v.trim().isEmpty) return 'Introduza o e-mail';
                                if (!v.contains('@') || !v.contains('.')) return 'E-mail inválido';
                                return null;
                              },
                            ),
                            const SizedBox(height: 12),

                            // Role
                            DropdownButtonFormField<String>(
                              initialValue: _selectedRole,
                              decoration: const InputDecoration(
                                labelText: 'Perfil (Role)',
                                border: OutlineInputBorder(),
                                prefixIcon: Icon(Icons.security),
                              ),
                              items: _roles.map((String role) {
                                return DropdownMenuItem<String>(
                                  value: role,
                                  child: Text(role),
                                );
                              }).toList(),
                              onChanged: (value) {
                                if (value != null) {
                                  setState(() => _selectedRole = value);
                                }
                              },
                            ),
                            const SizedBox(height: 16),

                            // Ações
                            Wrap(
                              alignment: WrapAlignment.center,
                              spacing: 8.0,
                              runSpacing: 8.0,
                              children: [
                                ElevatedButton.icon(
                                  onPressed: _salvarOuAtualizar,
                                  icon: Icon(_selectedUserId == null ? Icons.person_add : Icons.save),
                                  label: Text(_selectedUserId == null ? 'Registar' : 'Gravar'),
                                ),
                                ElevatedButton.icon(
                                  onPressed: _selectedUserId != null ? _eliminar : null,
                                  icon: const Icon(Icons.delete),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: Colors.red.shade100,
                                  ),
                                  label: const Text('Eliminar'),
                                ),
                                OutlinedButton.icon(
                                  onPressed: _limparFormulario,
                                  icon: const Icon(Icons.clear),
                                  label: const Text('Limpar'),
                                ),
                              ],
                            ),
                            const SizedBox(height: 20),

                            // Tabela
                            SizedBox(
                              width: double.infinity,
                              child: SingleChildScrollView(
                                scrollDirection: Axis.horizontal,
                                child: DataTable(
                                  showCheckboxColumn: false,
                                  columns: const [
                                    DataColumn(label: Text('Username')),
                                    DataColumn(label: Text('E-mail')),
                                    DataColumn(label: Text('Role')),
                                  ],
                                  rows: _utilizadores.map((user) {
                                    final isSelected = _selectedUserId == user.id;

                                    return DataRow(
                                      selected: isSelected,
                                      onSelectChanged: (_) => _selecionarParaEdicao(user),
                                      cells: [
                                        DataCell(Text(user.username)),
                                        DataCell(Text(user.email)),
                                        DataCell(Text(user.role)),
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
            ),
    );
  }
}