package application.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Pasta raiz definida na propriedade (ou "uploads" por defeito)
    private final Path rootLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            // Garante que a pasta raiz principal existe
            Files.createDirectories(this.rootLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório raiz de uploads.", ex);
        }
    }

    /**
     * Guarda um ficheiro numa subpasta específica (ex: "faturas", "documentos", "seguros")
     */
    public String storeFile(MultipartFile file, String subfolder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("O ficheiro está vazio.");
            }

            // Define o destino final (ex: uploads/faturas ou uploads/documentos)
            Path targetDir = this.rootLocation;
            if (subfolder != null && !subfolder.isBlank()) {
                targetDir = this.rootLocation.resolve(subfolder);
                Files.createDirectories(targetDir);
            }

            // Limpa e gera um nome único para evitar colisões
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = targetDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retorna o URL/caminho relativo que será guardado na Base de Dados
            // Ex: /uploads/faturas/uuid-aqui.png ou /uploads/documentos/uuid-aqui.pdf
            if (subfolder != null && !subfolder.isBlank()) {
                return "/uploads/" + subfolder + "/" + uniqueFileName;
            }
            return "/uploads/" + uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Falha ao armazenar o ficheiro. Tente novamente!", ex);
        }
    }
}