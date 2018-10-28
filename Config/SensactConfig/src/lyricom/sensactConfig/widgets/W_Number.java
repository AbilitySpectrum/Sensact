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
package lyricom.sensactConfig.widgets;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import lyricom.sensactConfig.ui.MainFrame;
import lyricom.sensactConfig.ui.Utils;

/**
 *
 * @author Andrew
 */
public class W_Number extends W_Base {

    protected final JTextField field;
    private final int maxWidth;
    private final int minValue;
    private final int maxValue;
    private final String fldName;
    public W_Number(String label, String fname, int width, int min, int max) {
        super();
        maxWidth = width;
        minValue = min;
        maxValue = max;
        fldName = fname;
        
        field = new JTextField(width);
        field.setFont(Utils.MONO_FONT);
        
        add(new JLabel(label));
        add(field);
        
        AbstractDocument doc = (AbstractDocument) field.getDocument();
        doc.setDocumentFilter( new DocFilterx() );
                
        field.setInputVerifier(new W_InputVerifier());
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                field.getBorder(), 
                BorderFactory.createEmptyBorder(1, 2, 0, 2)));    
    }
    
    public void setValue(int value) {
        field.setText(Integer.toString(value));
    }
    
    public int getValue() {
        String txt = field.getText();
        return new Integer(txt);
    }    
    
    class W_InputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            if (getValue() < minValue) {
                JOptionPane.showMessageDialog(MainFrame.TheFrame,
                        fldName + " may be not less than " + Integer.toString(minValue),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                setValue(minValue);  
                return false;
            } else {
                widgetChanged();
                return true;
            }
        }  
    }
    
    class DocFilterx extends DocumentFilter {
        // InsertString never seems to get called ... ???
        @Override
        public void insertString(FilterBypass fb, int offset, 
                String str, AttributeSet a) throws BadLocationException {
            super.insertString(fb, offset, str, a);
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int len, String str,
            AttributeSet attrs) throws BadLocationException {
            
            // Check final expected length
            // Note: len is # of chars that will be removed.
            int currentLen = fb.getDocument().getLength();
            int targetLen = currentLen + str.length() - len;
            if (targetLen > maxWidth) return;
            
            // Check for invalid characters
            for(int i=0; i<str.length(); i++) {
                char ch = str.charAt(i);
                if (ch < '0' || ch > '9') return;
            }
            super.replace(fb, offset, len, str, attrs);
            if (getValue() > maxValue) {
                JOptionPane.showMessageDialog(MainFrame.TheFrame,
                        fldName + " may be not more than " + Integer.toString(maxValue),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                setValue(maxValue);
            }
        }        
    }
}
