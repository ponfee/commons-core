package code.ponfee.commons.base;

/**
 * Initialize resources
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface Initializable {

    NoArgMethodInvoker INITIATOR = new NoArgMethodInvoker("init", "initialize");

    void init();

    static void init(Object caller) {
        if (caller == null) {
            return;
        }

        if (caller instanceof Initializable) {
            ((Initializable) caller).init();
        } else {
            INITIATOR.invoke(caller);
        }
    }

}
