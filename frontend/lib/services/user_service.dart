import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user.dart';

class UserService {
  final String baseUrl = 'http://localhost:8080/api/users';

  Future<List<User>> getAll() async {
    final response = await http.get(Uri.parse(baseUrl));
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((item) => User.fromJson(item)).toList();
    }
    throw Exception('Erro ao carregar utilizadores');
  }

  Future<User> create(User user) async {
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(user.toJson()),
    );
    if (response.statusCode == 200 || response.statusCode == 201) {
      return User.fromJson(jsonDecode(response.body));
    }
    throw Exception('Erro ao criar utilizador');
  }

  Future<User> update(String id, User user) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$id'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(user.toJson()),
    );
    if (response.statusCode == 200) {
      return User.fromJson(jsonDecode(response.body));
    }
    throw Exception('Erro ao atualizar utilizador');
  }

  Future<void> delete(String id) async {
    final response = await http.delete(Uri.parse('$baseUrl/$id'));
    if (response.statusCode != 200 && response.statusCode != 204) {
      throw Exception('Erro ao eliminar utilizador');
    }
  }
}