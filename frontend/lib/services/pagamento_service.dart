import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/pagamento.dart';
import 'api_config.dart';

class PagamentoService {
  final String _endpoint = '${ApiConfig.baseUrl}/pagamentos';

  Future<List<Pagamento>> getAll() async {
    final response = await http.get(Uri.parse(_endpoint));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((json) => Pagamento.fromJson(json)).toList();
    } else {
      throw Exception('Falha ao carregar pagamentos');
    }
  }

  Future<Pagamento> create(Pagamento pagamento) async {
    final response = await http.post(
      Uri.parse(_endpoint),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(pagamento.toJson()),
    );
    if (response.statusCode == 201 || response.statusCode == 200) {
      return Pagamento.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao registar pagamento');
    }
  }

  Future<Pagamento> update(String id, Pagamento pagamento) async {
    final response = await http.put(
      Uri.parse('$_endpoint/$id'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(pagamento.toJson()),
    );
    if (response.statusCode == 200) {
      return Pagamento.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao atualizar pagamento');
    }
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$_endpoint/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Falha ao eliminar pagamento');
    }
  }
}