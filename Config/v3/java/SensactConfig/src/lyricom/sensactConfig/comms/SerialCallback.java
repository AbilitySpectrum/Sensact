package lyricom.sensactConfig.comms;

import java.util.List;

/**
 * Callbacks from the serial connection.
 * @author Andrew
 */
public interface SerialCallback {
    public void dispatchData(List<Byte> bytes);
    public void connectionLost();
}
