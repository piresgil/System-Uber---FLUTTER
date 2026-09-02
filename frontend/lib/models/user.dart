class User {
  final String? id;
  final String username;
  final String email;
  final String role; // Ex: 'ADMIN', 'USER', 'MANAGER'

  User({
    this.id,
    required this.username,
    required this.email,
    required this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString(),
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      role: json['role'] ?? 'USER',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'username': username,
      'email': email,
      'role': role,
    };
  }
}