import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/carro.dart';
import 'api_config.dart';

class CarroService {
  final String _endpoint = '${ApiConfig.baseUrl}/carros';

  // Obter todos os carros
  Future<List<Carro>> getAll() async {
    try {
      final response = await http.get(Uri.parse(_endpoint));
      if (response.statusCode == 200) {
        final String decodedBody = utf8.decode(response.bodyBytes);
        final List<dynamic> body = jsonDecode(decodedBody);
        return body.map((json) => Carro.fromJson(json)).toList();
      } else {
        throw Exception('Falha ao carregar carros (Código: ${response.statusCode})');
      }
    } catch (e) {
      throw Exception('Erro de ligação ao carregar carros: $e');
    }
  }

  // Obter carro por ID
  Future<Carro> getById(dynamic id) async {
    try {
      final response = await http.get(Uri.parse('$_endpoint/$id'));
      if (response.statusCode == 200) {
        final String decodedBody = utf8.decode(response.bodyBytes);
        return Carro.fromJson(jsonDecode(decodedBody));
      } else {
        throw Exception('Falha ao carregar detalhes do carro: ${response.body}');
      }
    } catch (e) {
      throw Exception('Erro de ligação ao carregar carro: $e');
    }
  }

  // Criar novo carro (envia todos os dados incluindo documento, seguro e inspeção)
  Future<Carro> create(Carro carro) async {
    try {
      final response = await http.post(
        Uri.parse(_endpoint),
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: jsonEncode(carro.toJson()),
      );
      if (response.statusCode == 201 || response.statusCode == 200) {
        final String decodedBody = utf8.decode(response.bodyBytes);
        return Carro.fromJson(jsonDecode(decodedBody));
      } else {
        throw Exception('Falha ao registar carro: ${response.body}');
      }
    } catch (e) {
      throw Exception('Erro de ligação ao criar carro: $e');
    }
  }

  // Atualizar carro existente
  Future<Carro> update(dynamic id, Carro carro) async {
    try {
      final response = await http.put(
        Uri.parse('$_endpoint/$id'),
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: jsonEncode(carro.toJson()),
      );
      if (response.statusCode == 200) {
        final String decodedBody = utf8.decode(response.bodyBytes);
        return Carro.fromJson(jsonDecode(decodedBody));
      } else {
        throw Exception('Falha ao atualizar carro: ${response.body}');
      }
    } catch (e) {
      throw Exception('Erro de ligação ao atualizar carro: $e');
    }
  }

  // Eliminar carro
  Future<void> delete(dynamic id) async {
    try {
      final response = await http.delete(Uri.parse('$_endpoint/$id'));
      if (response.statusCode != 200 && response.statusCode != 204) {
        throw Exception('Falha ao eliminar carro: ${response.body}');
      }
    } catch (e) {
      throw Exception('Erro de ligação ao eliminar carro: $e');
    }
  }
}