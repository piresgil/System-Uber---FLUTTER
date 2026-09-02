class Colaborador {
  final String? id;
  final String nome;
  final String? email;
  final String? telefone;
  final String? tipo;
  final String? documentoUrl;

  Colaborador({
    this.id,
    required this.nome,
    this.email,
    this.telefone,
    this.tipo,
    this.documentoUrl,
  });

  factory Colaborador.fromJson(Map<String, dynamic> json) {
    return Colaborador(
      id: json['id']?.toString(),
      nome: json['nome'] ?? '',
      email: json['email'],
      telefone: json['telefone'],
      tipo: json['tipo'],
      documentoUrl: json['documentoUrl'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'nome': nome,
      'email': email,
      'telefone': telefone,
      'tipo': tipo,
      'documentoUrl': documentoUrl,
    };
  }
}