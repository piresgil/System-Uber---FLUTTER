class Despesa {
  final String? id;
  final String nome;
  final double valor;
  final double? quantidade;
  final double? unidade;
  final DateTime? data;
  final String? descricao;
  final int? cartaoId;      // Alterado para int? para coincidir com o backend
  final int? carroId;       // Alterado para int? para coincidir com o backend
  final int? motoristaId;   // Alterado para int? para coincidir com o backend
  final String? faturaUrl;

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
    this.faturaUrl,
  });

  factory Despesa.fromJson(Map<String, dynamic> json) {
    return Despesa(
      id: json['id']?.toString(),
      nome: json['nome'] ?? '',
      valor: (json['valor'] as num?)?.toDouble() ?? 0.0,
      quantidade: (json['quantidade'] as num?)?.toDouble(),
      unidade: (json['unidade'] as num?)?.toDouble(),
      data: json['data'] != null ? DateTime.tryParse(json['data'].toString()) : null,
      descricao: json['descricao'],
      cartaoId: json['cartaoId'] as int?,
      carroId: json['carroId'] as int?,
      motoristaId: json['motoristaId'] as int?,
      faturaUrl: json['faturaUrl']?.toString(),
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
      'faturaUrl': faturaUrl,
    };
  }
}