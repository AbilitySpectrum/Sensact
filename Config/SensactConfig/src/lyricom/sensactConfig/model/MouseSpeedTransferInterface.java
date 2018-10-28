package lyricom.sensactConfig.model;

/**
 * Interface for transfer of mouse speed data.
 * Transfers consist of 5 integers - 3 speeds and 2 intervals.
 * @author Andrew
 */
public interface MouseSpeedTransferInterface {

    public int[] getSpeeds();
    public void setSpeeds(int[] values);
}
