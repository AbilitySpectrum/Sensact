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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Andrew
 */
public class TheFrame extends JFrame {
    private static TheFrame instance = null;
    
    public static TheFrame getInstance() {
        if (instance == null) {
            instance = new TheFrame();
        }
        return instance;
    }
        
    private JTextArea text;
    private MyProps props;
    
    private TheFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // setTitle("SensAct");
        setUndecorated(true);
        props = MyProps.getInstance();
        
        setLayout(new FlowLayout());
        text = new JTextArea(props.getRows(), props.getColumns());
        text.setEditable(false);
        text.setFont(new Font("Dialog", Font.PLAIN, props.getFontSize()));
        text.setBorder(new EmptyBorder(10,10,10,10));
        add(text);
        add(closeBox());
        
        newMessage("Starting ...");
        pack();
        
        Dimension dim = getPreferredSize();
        ScreenLocation l = props.getLocation();
        
        int x = 0, y = 0;
        switch(l) {
            case TOP_RIGHT:
                x = ScreenInfo.getRight() - dim.width;
                y = ScreenInfo.getTop();
                break;
                
            case TOP_LEFT:
                x = ScreenInfo.getLeft();
                y = ScreenInfo.getTop();
                break;
                
            case BOTTOM_LEFT:
                x = ScreenInfo.getLeft();
                y = ScreenInfo.getBottom() - dim.height;
                break;
                
            case BOTTOM_RIGHT:
                x = ScreenInfo.getRight() - dim.width;
                y = ScreenInfo.getBottom() - dim.height;
                break;
        }
        setLocation(x, y);
        
        setVisible(Boolean.TRUE);
    }

    public final void newMappingChar(String ch) {
        String str = props.getMapping(ch);
        if (str == null) {
            return;
        }
        newMessage(str);
    }
        
    public final void newMessage(String msg) {
        SwingUtilities.invokeLater( () -> {
            text.setText(msg);
            forceVisibility();
        });
    }
    
    public void forceVisibility() {
        setVisible(true);
        toFront();
        requestFocus();
        repaint();
    }
    
    public @Override void toFront() {
        int sta = super.getExtendedState() & ~JFrame.ICONIFIED;

        super.setExtendedState(sta);
        super.setAlwaysOnTop(true);
        super.toFront();
        super.requestFocus();
        super.setAlwaysOnTop(false);
    }
    
    private JComponent closeBox() {
        JButton btn = new JButton("X");
        btn.setFont(new Font("Courier", Font.BOLD, 10));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
//        btn.setVerticalAlignment(1);
        Dimension dim = new Dimension(20, 20);
        btn.setMaximumSize(dim);
        return btn;
    }
}
