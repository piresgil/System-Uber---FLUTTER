class Carro {
  final String? id;
  final String matricula;
  final String marcaModelo;

  Carro({
    this.id,
    required this.matricula,
    required this.marcaModelo,
  });

  factory Carro.fromJson(Map<String, dynamic> json) {
    return Carro(
      id: json['id']?.toString(),
      matricula: json['matricula'] ?? '',
      marcaModelo: json['marcaModelo'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'matricula': matricula,
      'marcaModelo': marcaModelo,
    };
  }
}