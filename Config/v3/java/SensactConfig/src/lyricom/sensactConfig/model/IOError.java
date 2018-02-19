/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lyricom.sensactConfig.model;

/**
 *
 * @author Andrew
 */
public class IOError extends Exception {

    /**
     * Creates a new instance of <code>IOError</code> without detail message.
     */
    public IOError() {
    }

    /**
     * Constructs an instance of <code>IOError</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public IOError(String msg) {
        super(msg);
    }
}
