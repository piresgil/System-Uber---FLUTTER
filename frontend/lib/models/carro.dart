import 'despesa.dart';

class Carro {
  final dynamic id;
  final String marca;
  final String modelo;
  final String matricula;
  final bool alugado;
  final double kilometragem;
  final bool ativo;
  final String dataUltimoReinicio;
  
  // Os 3 campos específicos para as fotos individuais
  final String? documentoUrl;
  final String? seguroUrl;
  final String? inspecaoUrl;
  
  final List<Despesa> despesas;

  Carro({
    this.id,
    required this.marca,
    required this.modelo,
    required this.matricula,
    required this.alugado,
    required this.kilometragem,
    required this.ativo,
    required this.dataUltimoReinicio,
    this.documentoUrl,
    this.seguroUrl,
    this.inspecaoUrl,
    required this.despesas,
  });

  /// Getter de compatibilidade para views antigas
  String get marcaModelo => '$marca $modelo';

  factory Carro.fromJson(Map<String, dynamic> json) {
    var despesasFromJson = json['despesas'] as List? ?? [];
    List<Despesa> despesasLista =
        despesasFromJson.map((i) => Despesa.fromJson(i)).toList();

    return Carro(
      id: json['id'],
      marca: json['marca'] ?? '',
      modelo: json['modelo'] ?? '',
      matricula: json['matricula'] ?? '',
      alugado: json['alugado'] ?? false,
      kilometragem: (json['kilometragem'] as num?)?.toDouble() ?? 0.0,
      ativo: json['ativo'] ?? true,
      dataUltimoReinicio: json['dataUltimoReinicio'] ?? '',
      documentoUrl: json['documentoUrl'],
      seguroUrl: json['seguroUrl'],
      inspecaoUrl: json['inspecaoUrl'],
      despesas: despesasLista,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'marca': marca,
      'modelo': modelo,
      'matricula': matricula,
      'alugado': alugado,
      'kilometragem': kilometragem,
      'ativo': ativo,
      'dataUltimoReinicio': dataUltimoReinicio,
      'documentoUrl': documentoUrl,
      'seguroUrl': seguroUrl,
      'inspecaoUrl': inspecaoUrl,
      'despesas': despesas.map((d) => d.toJson()).toList(),
    };
  }
}