package lyricom.sensactConfig.widgets;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for several widgets.
 * 
 * @author Andrew
 */
public class W_Composite extends W_Base {
    
    private final List<W_Base> subParts = new ArrayList<>();
    
    public W_Composite() {
        super();
    }
    
    public void addPart(W_Base part) {
        subParts.add(part);
        add(part);  // Add to JPanel
    }
    
    @Override
    public void update() {
        subParts.forEach((p) -> {
            p.update();
        });
    }
    
}
