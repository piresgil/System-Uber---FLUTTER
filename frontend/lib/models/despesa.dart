class Despesa {
  final String? id;
  final String nome;
  final double valor;
  final double? quantidade;
  final String? unidade;
  final DateTime? data;
  final String? descricao;
  final String? cartaoId;
  final String? carroId;
  final String? motoristaId;
  final String? faturaUrl; // <--- Adicionado aqui

  Despesa({
    this.id,
    required this.nome,
    required this.valor,
    this.quantidade,
    this.unidade,
    this.data,
    this.descricao,
    this.cartaoId,
    this.carroId,
    this.motoristaId,
    this.faturaUrl, // <--- Adicionado aqui
  });

  factory Despesa.fromJson(Map<String, dynamic> json) {
    return Despesa(
      id: json['id']?.toString(),
      nome: json['nome'] ?? '',
      valor: (json['valor'] as num?)?.toDouble() ?? 0.0,
      quantidade: (json['quantidade'] as num?)?.toDouble(),
      unidade: json['unidade'],
      data: json['data'] != null ? DateTime.tryParse(json['data'].toString()) : null,
      descricao: json['descricao'],
      cartaoId: json['cartaoId']?.toString(),
      carroId: json['carroId']?.toString(),
      motoristaId: json['motoristaId']?.toString(),
      faturaUrl: json['faturaUrl']?.toString(), // <--- Adicionado aqui
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'nome': nome,
      'valor': valor,
      'quantidade': quantidade,
      'unidade': unidade,
      'data': data?.toIso8601String(),
      'descricao': descricao,
      'cartaoId': cartaoId,
      'carroId': carroId,
      'motoristaId': motoristaId,
      'faturaUrl': faturaUrl, // <--- Adicionado aqui
    };
  }
}