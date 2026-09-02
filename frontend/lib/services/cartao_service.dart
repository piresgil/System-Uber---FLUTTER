import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/cartao.dart';
import 'api_config.dart';

class CartaoService {
  final String _endpoint = '${ApiConfig.baseUrl}/cartoes';

  Future<List<Cartao>> getAll() async {
    final response = await http.get(Uri.parse(_endpoint));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((json) => Cartao.fromJson(json)).toList();
    } else {
      throw Exception('Falha ao carregar cartões');
    }
  }

  Future<Cartao> create(Cartao cartao) async {
    final response = await http.post(
      Uri.parse(_endpoint),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(cartao.toJson()),
    );
    if (response.statusCode == 201 || response.statusCode == 200) {
      return Cartao.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao registar cartão');
    }
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$_endpoint/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Falha ao eliminar cartão');
    }
  }
}