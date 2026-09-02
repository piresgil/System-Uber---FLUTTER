import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/despesa.dart';
import 'api_config.dart';

class DespesaService {
  final String _endpoint = '${ApiConfig.baseUrl}/despesas';

  Future<List<Despesa>> getAll() async {
    final response = await http.get(Uri.parse(_endpoint));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((json) => Despesa.fromJson(json)).toList();
    } else {
      throw Exception('Falha ao carregar despesas');
    }
  }

  Future<Despesa> create(Despesa despesa) async {
    final response = await http.post(
      Uri.parse(_endpoint),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(despesa.toJson()),
    );
    if (response.statusCode == 201 || response.statusCode == 200) {
      return Despesa.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao registar despesa');
    }
  }

  Future<Despesa> update(String id, Despesa despesa) async {
    final response = await http.put(
      Uri.parse('$_endpoint/$id'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(despesa.toJson()),
    );
    if (response.statusCode == 200) {
      return Despesa.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao atualizar despesa');
    }
  }

  /// NOVO: Método para enviar o ficheiro/imagem da fatura para o backend
  Future<Despesa> uploadFatura(String id, String filePath) async {
    var uri = Uri.parse('$_endpoint/$id/fatura');
    var request = http.MultipartRequest('POST', uri);

    // Adiciona o ficheiro ao pedido multipart usando a chave 'file' (exigida pelo backend)
    request.files.add(await http.MultipartFile.fromPath('file', filePath));

    // Envia o pedido
    var streamedResponse = await request.send();
    var response = await http.Response.fromStream(streamedResponse);

    if (response.statusCode == 200) {
      return Despesa.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Falha ao carregar fatura: ${response.body}');
    }
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$_endpoint/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Falha ao eliminar despesa');
    }
  }
}