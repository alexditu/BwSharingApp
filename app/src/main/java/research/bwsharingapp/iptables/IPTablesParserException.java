package research.bwsharingapp.iptables;

/**
 * Created by alex on 5/21/17.
 */

public class IPTablesParserException extends Exception {
    private int retcode;

    public IPTablesParserException() {
        super();
    }
    public IPTablesParserException(int retcode) {
        super();
        this.retcode = retcode;
    }

    public IPTablesParserException(String message) {
        super(message);
    }
    public IPTablesParserException(String message, int retcode) {
        super(message);
        this.retcode = retcode;
    }

    public IPTablesParserException(String message, Throwable cause) {
        super(message, cause);
    }
    public IPTablesParserException(String message, Throwable cause, int retcode) {
        super(message, cause);
        this.retcode = retcode;
    }

    public IPTablesParserException(Throwable cause) {
        super(cause);
    }
    public IPTablesParserException(Throwable cause, int retcode) {
        super(cause);
        this.retcode = retcode;
    }

    public int getRetcode() {
        return retcode;
    }
}
