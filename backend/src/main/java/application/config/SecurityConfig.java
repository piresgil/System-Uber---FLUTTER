// Define o pacote onde esta classe está localizada.
// Pode ser "application.config" ou "application.security", dependendo da estrutura do projeto.
package application.config;

// Importações necessárias para a configuração de segurança do Spring.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Classe de configuração para definir um bean de codificação de senha.
 * Isso permite que o Spring gerencie a instância de PasswordEncoder
 * e possibilita a sua injeção automática em outras classes.
 */
@Configuration // Indica que esta classe contém definições de configuração do Spring.
public class SecurityConfig {

    /**
     * Metodo que cria e disponibiliza um bean de PasswordEncoder no contexto do Spring.
     * O BCryptPasswordEncoder é uma implementação segura para criptografia de senhas.
     *
     * @return uma instância de BCryptPasswordEncoder para ser usada na aplicação.
     */
    @Bean // Define este método como um bean gerenciado pelo Spring.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Retorna uma instância de BCryptPasswordEncoder.
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite acesso a todas as páginas SEM autenticação
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) // Desativa CSRF para o H2
                .headers(headers -> headers.frameOptions().disable()) // Permite iframes para o H2
                .formLogin().disable()
                .httpBasic().disable();

        return http.build();
    }
}
