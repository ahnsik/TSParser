package ts_parser;

import java.util.EventObject;

public class PayloadUnitComplete extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PayloadUnitComplete(Object source) {
        super(source);
    }
}
