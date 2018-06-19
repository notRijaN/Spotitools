package exceptions;

@SuppressWarnings("serial")
public class ConnectionException extends Exception {
	
	private int errorCode;
	
	public ConnectionException(int errorCode) {
		super(String.valueOf(errorCode));
	}
	
	public int getErrorCode() {
		return errorCode;
	}

}
