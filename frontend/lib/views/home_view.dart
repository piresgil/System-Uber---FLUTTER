import 'package:flutter/material.dart';

// Imports exatos da tua pasta views
import 'carro_view.dart';
import 'cartao_view.dart';
import 'colaborador_view.dart';
import 'despesa_view.dart';
import 'pagamento_view.dart';
import 'listagem_pagamentos_view.dart';
import 'users_view.dart';

class HomeView extends StatefulWidget {
  const HomeView({super.key});

  @override
  State<HomeView> createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  int _currentIndex = 0;

  final List<String> _titles = const [
    'Dashboard',
    'Gestão de Carros',
    'Gestão de Colaboradores',
    'Gestão de Cartões',
    'Gestão de Despesas',
    'Registar Pagamento',
    'Histórico de Pagamentos',
    'Gestão de Utilizadores',
  ];

  void _onSelectItem(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  void _onSelectDrawerItem(int index) {
    _onSelectItem(index);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    // Mapeamento direto para as tuas classes existentes
    final List<Widget> views = [
      _buildDashboardView(context), // Index 0
      const CarroView(),            // Index 1
      const ColaboradorView(),      // Index 2
      const CartaoView(),           // Index 3
      const DespesaView(),          // Index 4
      const PagamentoView(),        // Index 5
      const ListagemPagamentosView(),// Index 6
      const UsersView(),            // Index 7
    ];

    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_currentIndex]),
        centerTitle: true,
      ),
      drawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            UserAccountsDrawerHeader(
              accountName: const Text(
                'Gestão de Frota',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              accountEmail: const Text('admin@empresa.com'),
              currentAccountPicture: CircleAvatar(
                backgroundColor: Theme.of(context).colorScheme.onPrimary,
                child: Icon(
                  Icons.directions_car,
                  size: 36,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primary,
              ),
            ),
            ListTile(
              leading: const Icon(Icons.dashboard),
              title: const Text('Dashboard'),
              selected: _currentIndex == 0,
              onTap: () => _onSelectDrawerItem(0),
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.directions_car),
              title: const Text('Carros / Frota'),
              selected: _currentIndex == 1,
              onTap: () => _onSelectDrawerItem(1),
            ),
            ListTile(
              leading: const Icon(Icons.badge),
              title: const Text('Colaboradores'),
              selected: _currentIndex == 2,
              onTap: () => _onSelectDrawerItem(2),
            ),
            ListTile(
              leading: const Icon(Icons.credit_card),
              title: const Text('Cartões'),
              selected: _currentIndex == 3,
              onTap: () => _onSelectDrawerItem(3),
            ),
            ListTile(
              leading: const Icon(Icons.receipt_long),
              title: const Text('Despesas'),
              selected: _currentIndex == 4,
              onTap: () => _onSelectDrawerItem(4),
            ),
            ListTile(
              leading: const Icon(Icons.payment),
              title: const Text('Registar Pagamento'),
              selected: _currentIndex == 5,
              onTap: () => _onSelectDrawerItem(5),
            ),
            ListTile(
              leading: const Icon(Icons.history),
              title: const Text('Histórico Pagamentos'),
              selected: _currentIndex == 6,
              onTap: () => _onSelectDrawerItem(6),
            ),
            ListTile(
              leading: const Icon(Icons.people),
              title: const Text('Utilizadores'),
              selected: _currentIndex == 7,
              onTap: () => _onSelectDrawerItem(7),
            ),
          ],
        ),
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: views,
      ),
    );
  }

  Widget _buildDashboardView(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(12.0),
      child: GridView.count(
        crossAxisCount: MediaQuery.of(context).size.width > 600 ? 3 : 2,
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
        childAspectRatio: 1.3,
        children: [
          _buildDashboardCard(
            title: 'Carros',
            icon: Icons.directions_car,
            color: Colors.blue,
            indexTarget: 1,
          ),
          _buildDashboardCard(
            title: 'Colaboradores',
            icon: Icons.badge,
            color: Colors.indigo,
            indexTarget: 2,
          ),
          _buildDashboardCard(
            title: 'Cartões',
            icon: Icons.credit_card,
            color: Colors.orange,
            indexTarget: 3,
          ),
          _buildDashboardCard(
            title: 'Despesas',
            icon: Icons.receipt_long,
            color: Colors.redAccent,
            indexTarget: 4,
          ),
          _buildDashboardCard(
            title: 'Novo Pagamento',
            icon: Icons.add_card,
            color: Colors.green,
            indexTarget: 5,
          ),
          _buildDashboardCard(
            title: 'Histórico',
            icon: Icons.history,
            color: Colors.purple,
            indexTarget: 6,
          ),
          _buildDashboardCard(
            title: 'Utilizadores',
            icon: Icons.people,
            color: Colors.teal,
            indexTarget: 7,
          ),
        ],
      ),
    );
  }

  Widget _buildDashboardCard({
    required String title,
    required IconData icon,
    required Color color,
    required int indexTarget,
  }) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: InkWell(
        onTap: () => _onSelectItem(indexTarget),
        borderRadius: BorderRadius.circular(10),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircleAvatar(
              radius: 22,
              backgroundColor: color.withValues(alpha: 0.15),
              child: Icon(icon, size: 24, color: color),
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
          ],
        ),
      ),
    );
  }
}