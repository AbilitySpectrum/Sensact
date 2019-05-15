/*
 * This file is part of the Sensact software.
 *
 * Sensact software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sensact software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this Sensact Arduino software.  
 * If not, see <https://www.gnu.org/licenses/>.   
 */ 
package lyricom.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * @author Andrew
 */
public class MyProps {
    private static MyProps instance = null;
    
    static public MyProps getInstance() {
        if (instance == null) {
            instance = new MyProps();
        }
        return instance;
    }
    
    private Properties props;
    private String port;
    private int rows;
    private int columns;
    private int fontSize;
    private int displayTime;
    private ScreenLocation location;
    private boolean titleBar;
    
    private MyProps() {
    }
    
    public void load() throws IOException, URISyntaxException, NumberFormatException {
        File dir = new File(MyProps.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File base = dir.getParentFile();
        
        File target = new File(base, "config.txt");
//        System.out.println(target.toPath());
        if (! target.canRead()) {
//            System.out.println(" ... failed.  Trying ...");
            target = new File(base.getParentFile(), "config.txt");
//            System.out.println(target.toPath());
        }
        
        FileInputStream in = new FileInputStream(target);   
        props = new Properties();
        props.load(in);
        in.close();
        
        port = props.getProperty("Port", "Leonardo");
        
        rows = getNumberParameter(props, "Rows", "2");
        columns = getNumberParameter(props, "Columns", "40");
        fontSize = getNumberParameter(props, "FontSize", "14");
        displayTime = getNumberParameter(props, "DisplayTime", "0");
        
        String locationText = props.getProperty("Location");
        if (locationText == null) {
            location = ScreenLocation.TOP_RIGHT;
        } else {
            location = ScreenLocation.TOP_RIGHT;
            for(ScreenLocation l: ScreenLocation.values()) {
                if (l.matches(locationText)) {
                    location = l;
                }
            }
        }
        
        String tmp = props.getProperty("TitleBar");
         if ((tmp != null) && tmp.equalsIgnoreCase("yes")) {
            titleBar = true;
        } else {
            titleBar = false;
        }
    }
    
    private int getNumberParameter(Properties props, String name, String dft) 
                    throws NumberFormatException {
        String temp = props.getProperty(name, dft);
        int value;
        try {
            value = Integer.valueOf(temp);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Bad " + name + " value.");
        }
        return value;
    }

    public String getPort() {
        return port;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getFontSize() {
        return fontSize;
    }
    
    public int getDisplayTime() {
        return displayTime;
    }
    
    public String getMapping(String ch) {
        return props.getProperty(ch);
    }
    
    public ScreenLocation getLocation() {
        return location;
    }
    
    public boolean hasTitleBar() {
        return titleBar;
    }
}
