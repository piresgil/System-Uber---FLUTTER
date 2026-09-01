/**
 * @author Daniel Gil
 */
package application.services.exceptions;

/**
 * Exceção Data Base Exception
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String msg) {
        super(msg);
    }
}
