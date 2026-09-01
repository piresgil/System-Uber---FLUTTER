package application.services;

// Importações das classes do projeto

import application.model.User;
import application.repositories.UserRepository;
import application.services.exceptions.DatabaseException;
import application.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe de serviço responsável por operações relacionadas aos usuários.
 */
@Service // Define esta classe como um serviço gerenciado pelo Spring.
public class UserService {

    private final UserRepository repository; // Repositório para acessar os usuários no banco de dados.
    private final PasswordEncoder passwordEncoder; // Encoder para criptografar senhas.
    private static final Logger logger = LoggerFactory.getLogger(UserService.class); // Logger para registrar eventos.
    private final Map<Long, User> cache = new HashMap<>();

    public void limparCache() {
        cache.clear();
    }

    /**
     * Construtor com injeção de dependências.
     */
    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retorna todos os usuários cadastrados no banco de dados.
     * O uso de @Transactional(readOnly = true) melhora a performance, evitando locks desnecessários.
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        if (!cache.isEmpty()) {
            return new ArrayList<>(cache.values());// Retorna os valores já no cache
        }
        List<User> users = repository.findAll();
        users.forEach(user -> cache.put(user.getId(), user)); // Armazena no cache
        return users;
    }

    /**
     * Busca um usuário pelo ID.
     * Se não for encontrado, lança ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Usuário com ID {} não encontrado!", id);
                    return new ResourceNotFoundException(id);
                });
    }

    /**
     * Insere um novo usuário no banco de dados.
     * Antes de salvar, criptografa a senha usando o PasswordEncoder.
     */
    @Transactional
    public User insert(@Valid User obj) {
        try {
            // REMOVER A ENCRIPTAÇÃO PARA TESTE
            // obj.setPassword(passwordEncoder.encode(obj.getPassword()));

            User savedUser = repository.save(obj); // Salva no banco de dados
            cache.put(savedUser.getId(), savedUser); // Atualiza o cache
            logger.info("Usuário com ID {} cadastrado com sucesso.", savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao salvar usuário: {}", e.getMessage());
            throw new DatabaseException("Erro ao salvar o usuário: violação de integridade.");
        } catch (RuntimeException e) {
            logger.error("Erro inesperado ao salvar usuário: {}", e.getMessage());
            throw new DatabaseException("Erro inesperado ao salvar o usuário.");
        }
    }

    /**
     * Atualiza os dados de um usuário existente.
     * Se o usuário não for encontrado, lança ResourceNotFoundException.
     */
    @Transactional
    public User update(Long id, @Valid User obj) {
        try {
            return repository.findById(id)
                    .map(entity -> {
                        // Atualiza os campos
                        entity.setNome(obj.getNome());
                        entity.setEmail(obj.getEmail());
                        entity.setPassword(obj.getPassword());

                        // Salva e retorna o user atualizado
                        User updatedUser = repository.save(entity);
                        logger.info("User com ID {} atualizado com sucesso.", id);
                        return updatedUser;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(id));
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao atualizar carro ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro ao atualizar o User: violação de integridade.");
        }
    }

    /**
     * Exclui um usuário pelo ID.
     * Se o usuário não existir, lança ResourceNotFoundException.
     */
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            cache.remove(id); // remove do cache
            logger.info("Usuário com ID {} deletado com sucesso.", id);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("Tentativa de deletar usuário inexistente: ID {}", id);
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro ao excluir usuário ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Não foi possível eliminar o usuário. Verifique dependências.");
        }
    }

    public Boolean existsByEmail(String email) {
        return repository.existsByEmail(email); // Chama o método do repositório para verificar duplicação de e-mail.
    }

    public Boolean existsByEmailAndIdNot(String email, Long id) {
        return repository.existsByEmailAndIdNot(email, id);
    }
}
