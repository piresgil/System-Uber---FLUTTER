class Cartao {
  final String? id;
  final String tipo;
  final String numeroCartao;
  final String? contrato;
  final String? nomeCliente;
  final String? carroId;
  final String? carroMatricula;

  Cartao({
    this.id,
    required this.tipo,
    required this.numeroCartao,
    this.contrato,
    this.nomeCliente,
    this.carroId,
    this.carroMatricula,
  });

  factory Cartao.fromJson(Map<String, dynamic> json) {
    return Cartao(
      id: json['id']?.toString(),
      tipo: json['tipo'] ?? '',
      numeroCartao: json['numeroCartao'] ?? json['numero'] ?? '',
      contrato: json['contrato'],
      nomeCliente: json['nomeCliente'],
      carroId: json['carroId']?.toString(),
      carroMatricula: json['carroMatricula'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'tipo': tipo,
      'numeroCartao': numeroCartao,
      'contrato': contrato,
      'nomeCliente': nomeCliente,
      'carroId': carroId,
      'carroMatricula': carroMatricula,
    };
  }
}