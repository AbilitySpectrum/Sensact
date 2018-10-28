package lyricom.sensactConfig.model;

/**
 * A singleton class for transferring mouse speed data
 * to and from the Sensact.
 * @author Andrew
 */
public class MouseSpeedTransfer {
    private static MouseSpeedTransfer instance = null;
    
    public static MouseSpeedTransfer getInstance() {
        if (instance == null) {
            instance = new MouseSpeedTransfer();
        }
        return instance;
    }
    
    private Converter converter = new Converter();
    private MouseSpeedTransferInterface transfer;
    
    private MouseSpeedTransfer() {
        
    }
    
    // Called by the UI component to register its location.
    public void registerUIComponent(MouseSpeedTransferInterface msti) {
        transfer = msti;
    }
    
    public void toStream(OutStream os) {
        int[] values = transfer.getSpeeds();
        
        os.putChar((byte)'\n');
        os.putChar(Model.MOUSE_SPEED);
        os.putNum(20, 2);
        
        converter.convertToOutput(values[0]);
        os.putID(converter.delay, 2);
        os.putID(converter.jump, 2);
        converter.convertToOutput(values[1]);
        os.putID(converter.delay, 2);
        os.putID(converter.jump, 2);
        converter.convertToOutput(values[2]);
        os.putID(converter.delay, 2);
        os.putID(converter.jump, 2);
        os.putNum(values[3], 2);
        os.putNum(values[4], 2);
    }
    
    public void fromStream(InStream is) throws IOError {
        int[] values = new int[5];
        
        int num = is.getNum(2);
        if (num != 20) {
            // Some unknown extension?
            for(int i=0; i<num; i++) {
                is.getChar();
            }
            return;
        }
        int delay;
        int jump;
        delay = is.getID(2);
        jump = is.getID(2);
        values[0] = converter.convertToInput(delay, jump);
        delay = is.getID(2);
        jump = is.getID(2);
        values[1] = converter.convertToInput(delay, jump);
        delay = is.getID(2);
        jump = is.getID(2);
        values[2] = converter.convertToInput(delay, jump);
        values[3] = is.getNum(2);
        values[4] = is.getNum(2);
        transfer.setSpeeds(values);
    }
    
    static class Converter {
        /*
         delay is the # of milliseconds between mouse moves
         jump is the # of pixels the mouse will go in a single move
        */
        int delay;
        int jump;
        
        /*
	 Convert the logarithmic value for pixels/second from the Widget
	 to number-of-milliseconds between mouse moves
	 and size of mouse jump.
	 Aim for a speed with a mouse jump of less than 10,
	 but for the highest speeds we get a delay of 24 
	 and a jump of 15.
        */
        void convertToOutput(int logSpeed) {
            double speed = Math.exp(logSpeed/100.0);
            for(delay = 60; delay > 23; delay -= 12) {
                jump = (int) ((speed * delay) / 1000.0 + 0.5);
                if (jump < 10) {
                    break;
                }
            }
            /*
             Reduce the delay by one.
             The actual delay will be the first 'tick' where
             the elapsed time is greater than the delay.
             We reduce delay by one to ensure we don't overshoot the 'tick'.
            */
            delay -= 1;
        }
        
        int convertToInput(int delay, int jump) {
            delay += 1;
            double speed = (jump * 1000.0) / delay;
            return (int) (Math.log(speed) * 100);
        }
    }
}
