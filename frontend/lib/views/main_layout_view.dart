import 'package:flutter/material.dart';
import 'pagamento_view.dart';
import 'listagem_pagamentos_view.dart';
import 'users_view.dart';

class MainLayoutView extends StatefulWidget {
  const MainLayoutView({super.key});

  @override
  State<MainLayoutView> createState() => _MainLayoutViewState();
}

class _MainLayoutViewState extends State<MainLayoutView> {
  int _currentIndex = 0;

  final List<Widget> _views = const [
    PagamentoView(),
    ListagemPagamentosView(),
    UsersView(),
  ];

  final List<String> _titles = const [
    'Novo Pagamento',
    'Histórico de Pagamentos',
    'Gestão de Utilizadores',
  ];

  void _onSelectItem(int index) {
    setState(() {
      _currentIndex = index;
    });
    Navigator.of(context).pop(); // Fecha o Drawer após selecionar
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_currentIndex]),
        centerTitle: true,
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            // Cabeçalho do Menu
            UserAccountsDrawerHeader(
              accountName: const Text(
                'Sistema de Gestão',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              accountEmail: const Text('admin@empresa.com'),
              currentAccountPicture: CircleAvatar(
                backgroundColor: Theme.of(context).colorScheme.onPrimary,
                child: Icon(
                  Icons.store,
                  size: 36,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primary,
              ),
            ),

            // Item 1: Novo Pagamento
            ListTile(
              leading: const Icon(Icons.add_card),
              title: const Text('Registar Pagamento'),
              selected: _currentIndex == 0,
              onTap: () => _onSelectItem(0),
            ),

            // Item 2: Histórico de Pagamentos
            ListTile(
              leading: const Icon(Icons.receipt_long),
              title: const Text('Histórico / Relatórios'),
              selected: _currentIndex == 1,
              onTap: () => _onSelectItem(1),
            ),

            const Divider(),

            // Item 3: Gestão de Utilizadores
            ListTile(
              leading: const Icon(Icons.people),
              title: const Text('Utilizadores'),
              selected: _currentIndex == 2,
              onTap: () => _onSelectItem(2),
            ),

            const Divider(),

            // Opção de Terminar Sessão / Sair
            ListTile(
              leading: const Icon(Icons.logout, color: Colors.red),
              title: const Text(
                'Terminar Sessão',
                style: TextStyle(color: Colors.red),
              ),
              onTap: () {
                Navigator.of(context).pop();
                // Adicionar lógica de logout aqui (limpar tokens, voltar para LoginView, etc.)
              },
            ),
          ],
        ),
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: _views,
      ),
    );
  }
}