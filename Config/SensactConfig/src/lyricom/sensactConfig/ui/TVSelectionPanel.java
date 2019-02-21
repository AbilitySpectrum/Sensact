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
package lyricom.sensactConfig.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import lyricom.sensactConfig.model.TVInfo;
import lyricom.sensactConfig.model.TVType;
import lyricom.sensactConfig.model.TVTypeUI;

/**
 * TVSelection Panel.
 * @author Andrew
 */
public class TVSelectionPanel extends JPanel implements TVTypeUI {
    private static final ResourceBundle RES = ResourceBundle.getBundle("strings");
    
    private JComboBox volumeControlSelectionBox;
    private JComboBox channelControlSelectionBox;
    private final TVInfo tvinfo;
    
    public TVSelectionPanel() {
        setLayout(new BorderLayout());
        
        tvinfo = TVInfo.getInstance();
        
        Box vbox = Box.createVerticalBox();
        vbox.add(Box.createVerticalStrut(20));
        vbox.add(textBox());
        vbox.add(Box.createVerticalStrut(20));
        vbox.add(volumeCombo());
        vbox.add(Box.createVerticalStrut(20));
        vbox.add(channelCombo());
        
        add(vbox, BorderLayout.NORTH);
    }
    
    private JComponent textBox() {
        JTextPane text = new JTextPane();
        text.setEditable(false);
        text.setText(RES.getString("TV_INSTRUCTIONS"));
        text.setFont(Utils.STATE_FONT);
        
        // All this just to center the text??  This is a real 'pane'.
        /*
        StyledDocument doc = text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        */
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        text.setBorder(new CompoundBorder(
                new LineBorder(Color.DARK_GRAY),
                new EmptyBorder(10, 20, 10 ,20) )
        );
         p.add(text);
        return p;
    }
    
    private JComponent volumeCombo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel l = new JLabel(RES.getString("TV_VOLUME_CONTROL") + ": ");
        p.add(l);
        volumeControlSelectionBox = new JComboBox();
        for(TVType t: tvinfo.getTVTypes()) {
            if (t.canControlVolume()) {
                volumeControlSelectionBox.addItem(t);
            }
        }
        volumeControlSelectionBox.addActionListener(e -> volumeSelectionChange());
        volumeControlSelectionBox.setSelectedItem(tvinfo.getTVTypes().get(0));
        p.add(volumeControlSelectionBox);
        
        return p;
    }
    
    private JComponent channelCombo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel l = new JLabel(RES.getString("TV_CHANNEL_CONTROL") + ": ");
        p.add(l);
        channelControlSelectionBox = new JComboBox();
        for(TVType t: tvinfo.getTVTypes()) {
            channelControlSelectionBox.addItem(t);
        }
        channelControlSelectionBox.addActionListener(e -> channelSelectionChange());
        channelControlSelectionBox.setSelectedItem(tvinfo.getTVTypes().get(0));
        p.add(channelControlSelectionBox);
        
        return p;
    }
    
    private void volumeSelectionChange() {
        TVType t = (TVType) volumeControlSelectionBox.getSelectedItem();
        tvinfo.setVolumeControl(t, this);
    }
    
    private void channelSelectionChange() {
        TVType t = (TVType) channelControlSelectionBox.getSelectedItem();
        tvinfo.setChannelControl(t, this);
    }
    
    // These methods are called from the model when loading a configuration.
    // They inform the UI of the tv type used in the configuration.
    @Override
    public void changeVolumeControl(TVType type) {
        SwingUtilities.invokeLater(() -> {
            volumeControlSelectionBox.setSelectedItem(type);
        });
    }

    @Override
    public void changeChannelControl(TVType type) {
       SwingUtilities.invokeLater(() -> {
           channelControlSelectionBox.setSelectedItem(type);
       });
   }
}
