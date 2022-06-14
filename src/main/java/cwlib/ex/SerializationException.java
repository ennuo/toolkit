package cwlib.ex;

/**
 * Exception thrown when an error occurs during (de)serialization.
 */
public class SerializationException extends RuntimeException {
	/**
	 * Used for adding custom messages when throwing exceptions.
	 * @param message Message to display with exception
	 */
	public SerializationException(String message) { super(message); }

	/**
	 * Used for wrapping exception in catch block.
	 * @param cause The cause of the exception
	 */
	public SerializationException(Throwable cause) { super(cause); }

	/**
	 * Used when wrapping exception in catch block and adding custom messages.
	 * @param message Message to display with exception
	 * @param cause The cause of the exception
	 */
	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
