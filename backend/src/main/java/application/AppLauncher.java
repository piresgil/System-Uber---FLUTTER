package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppLauncher {

    public static void main(String[] args) {
        System.out.println("\n...A iniciar o servidor Spring Boot...\n");
        SpringApplication.run(AppLauncher.class, args);
        System.out.println("\n...Servidor Spring Boot a funcionar como API REST!\n");
    }
}