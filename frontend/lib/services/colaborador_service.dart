import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/colaborador.dart';
import 'api_config.dart';

class ColaboradorService {
  final String _endpoint = '${ApiConfig.baseUrl}/colaboradores';

  Future<List<Colaborador>> getAll() async {
    final response = await http.get(Uri.parse(_endpoint));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((json) => Colaborador.fromJson(json)).toList();
    } else {
      throw Exception('Falha ao carregar colaboradores');
    }
  }

  Future<Colaborador> create(Colaborador colaborador) async {
    final response = await http.post(
      Uri.parse(_endpoint),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(colaborador.toJson()),
    );
    if (response.statusCode == 201 || response.statusCode == 200) {
      return Colaborador.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao registar colaborador');
    }
  }

  Future<Colaborador> update(String id, Colaborador colaborador) async {
    final response = await http.put(
      Uri.parse('$_endpoint/$id'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(colaborador.toJson()),
    );
    if (response.statusCode == 200) {
      return Colaborador.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao atualizar colaborador');
    }
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$_endpoint/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Falha ao eliminar colaborador');
    }
  }
}