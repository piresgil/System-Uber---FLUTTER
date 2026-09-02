import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/carro.dart';
import 'api_config.dart';

class CarroService {
  final String _endpoint = '${ApiConfig.baseUrl}/carros';

  Future<List<Carro>> getAll() async {
    final response = await http.get(Uri.parse(_endpoint));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((json) => Carro.fromJson(json)).toList();
    } else {
      throw Exception('Falha ao carregar carros');
    }
  }

  Future<Carro> create(Carro carro) async {
    final response = await http.post(
      Uri.parse(_endpoint),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(carro.toJson()),
    );
    if (response.statusCode == 201 || response.statusCode == 200) {
      return Carro.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao registar carro');
    }
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$_endpoint/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Falha ao eliminar carro');
    }
  }
}