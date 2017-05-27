package research.bwsharingapp.iptables;

/**
 * Created by alex on 5/21/17.
 */

public class ExecFailedException extends Exception {
    private int retcode;

    public ExecFailedException() {
        super();
    }
    public ExecFailedException(int retcode) {
        super();
        this.retcode = retcode;
    }

    public ExecFailedException(String message) {
        super(message);
    }
    public ExecFailedException(String message, int retcode) {
        super(message);
        this.retcode = retcode;
    }

    public ExecFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public ExecFailedException(String message, Throwable cause, int retcode) {
        super(message, cause);
        this.retcode = retcode;
    }

    public ExecFailedException(Throwable cause) {
        super(cause);
    }
    public ExecFailedException(Throwable cause, int retcode) {
        super(cause);
        this.retcode = retcode;
    }

    public int getRetcode() {
        return retcode;
    }

}
