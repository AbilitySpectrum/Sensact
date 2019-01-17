/*
 * Main
 */
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * 
    This file is part of the Sensact Configuration software.

    Sensact Configuration software is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Sensact Configuration software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this Sensact Arduino software.  
    If not, see <https://www.gnu.org/licenses/>.   
 * * * * * * * * * * * * * * * * * * * * * * * * * * * */ 

package lyricom.sensactConfig;

import java.util.Locale;
import lyricom.sensactConfig.ui.MainFrame;
import javax.swing.SwingUtilities;
import lyricom.sensactConfig.comms.Connection;
import lyricom.sensactConfig.model.Model;
import lyricom.sensactConfig.solutions.SolutionRegister;

/**
 *
 * @author Andrew
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         
        String language;
        String country;
        Locale locale;
        
        if (args.length == 2) {
            language = args[0];
            country = args[1];
            locale = new Locale(language, country);
        } else if (args.length == 1) {
            language = args[0];
            locale = new Locale(language);
        } else {
            locale = Locale.getDefault();
        }
        Locale.setDefault(locale);
        
        Connection conn = Connection.getInstance();
        conn.establishConnection();
        
        Model.initModel(conn.getVersionID());
        SolutionRegister.init();
        
        SwingUtilities.invokeLater(() -> {
            new MainFrame(conn.getVersionString());
        });
    }

}
