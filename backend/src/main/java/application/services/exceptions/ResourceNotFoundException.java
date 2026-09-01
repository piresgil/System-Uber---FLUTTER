/**
 * @author Daniel Gil
 */
package application.services.exceptions;


/**
 * Exceção Resource Not Found Exception
 * <p>
 * Exceção Find By Id
 */

public class ResourceNotFoundException extends RuntimeException {


    public ResourceNotFoundException(Object id) {
        super("Resource not found. Id " + id);
    }


}
