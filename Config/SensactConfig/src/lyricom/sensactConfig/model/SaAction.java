/*
 * This file is part of the Sensact Configuration software.
 *
 * Sensact Configuration software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact Configuration software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.sensactConfig.model;

import lyricom.sensactConfig.ui.ActionUI;

/**
 * POJO defining an action.  Immutable.
 * Named SaAction to avoid conflict with java.swing.action.
 * @author Andrew
 */
public class SaAction {
    private final ActionType type;
    private final int defaultVal;
    private final ActionUI optionUI;
    private final ParameterCheck pCheck;

    public SaAction(ActionType type, int defaultVal, ActionUI optionUI, ParameterCheck pCheck) {
        this.type = type;
        this.defaultVal = defaultVal;
        this.optionUI = optionUI;
        this.pCheck = pCheck;
    }
    
    public boolean doParameterCheck(int p) {
        if (pCheck == null) {
            return true;
        } else {
            return pCheck.doCheck(p);
        }
    }
    
    public ActionUI getOptionUI() {
        return optionUI;
    }

    public int getId() {
        return type.getActionID();
    }

    public ActionType getType() {
        return type;
    }

    public int getDefaultVal() {
        return defaultVal;
    }
    
    public String toString() {
        return type.toString();
    }
}
