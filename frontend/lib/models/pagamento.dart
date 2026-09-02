class Pagamento {
  final String? id;
  final String colaborador;
  final String? cartao;
  final String plataforma;
  final DateTime data;
  final double valor;
  final String tipo;

  Pagamento({
    this.id,
    required this.colaborador,
    this.cartao,
    required this.plataforma,
    required this.data,
    required this.valor,
    required this.tipo,
  });

  factory Pagamento.fromJson(Map<String, dynamic> json) {
    return Pagamento(
      id: json['id']?.toString(),
      colaborador: json['colaborador'] ?? '',
      cartao: json['cartao'],
      plataforma: json['plataforma'] ?? '',
      data: json['data'] != null ? DateTime.parse(json['data']) : DateTime.now(),
      valor: (json['valor'] as num?)?.toDouble() ?? 0.0,
      tipo: json['tipo'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'colaborador': colaborador,
      if (cartao != null) 'cartao': cartao,
      'plataforma': plataforma,
      'data': data.toIso8601String(),
      'valor': valor,
      'tipo': tipo,
    };
  }
}