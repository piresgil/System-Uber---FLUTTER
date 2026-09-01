package application.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ValidationUtils {

    /**
     * Verifica se o e-mail existe em qualquer serviço que implementa a lógica de verificação.
     *
     * @param email         E-mail a ser verificado.
     * @param existsByEmail Função que verifica se o e-mail existe no serviço.
     * @return true se o e-mail já estiver em uso, false caso contrário.
     */
    public static boolean isEmailDuplicado(String email, Function<String, Boolean> existsByEmail) {
        return existsByEmail.apply(email);
    }

    /**
     * Verifica se o e-mail existe, ignorando um item específico (para edição).
     *
     * @param email                 E-mail a ser verificado.
     * @param id                    ID do item que está sendo editado.
     * @param existsByEmailAndIdNot Função que verifica se o e-mail existe, ignorando um id específico.
     * @return true se o e-mail já estiver em uso, false caso contrário.
     */
    public static boolean isEmailDuplicado(String email, Long id, BiFunction<String, Long, Boolean> existsByEmailAndIdNot) {
        return existsByEmailAndIdNot.apply(email, id);
    }

    public static <T> String validarEntidade(T entity) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            StringBuilder erros = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                erros.append(violation.getMessage()).append("\n");
            }
            return erros.toString();
        }
        return null; // Validação bem-sucedida
    }
}
