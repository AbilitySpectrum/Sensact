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
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import lyricom.sensactConfig.ui.Utils;

/**
 *
 * @author Andrew
 */
public class W_TextField extends W_Base {
    
    private final int maxWidth;
    final protected JTextField field;
    public W_TextField(String label, int width) {
        super();
        
        this.maxWidth = width;
        field = new JTextField(width);
        field.setFont(Utils.MONO_FONT);
        
        add(new JLabel(label));
        add(field);
        
        AbstractDocument doc = (AbstractDocument) field.getDocument();
        doc.setDocumentFilter( new W_TextField.DocFilterx() );
                
        field.setInputVerifier(new W_TextField.W_InputVerifier());
        field.setHorizontalAlignment(JTextField.RIGHT);
        
        field.setBorder(BorderFactory.createCompoundBorder(
                field.getBorder(), 
                BorderFactory.createEmptyBorder(1, 2, 0, 2)));    
    }
    
    class W_InputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            // There is nothing to verify.
            // This is simply a way to know that focus is about 
            // to change.
            widgetChanged();
            return true;
        }  
    }
    
    class DocFilterx extends DocumentFilter {
        // InsertString never seems to get called ... ???
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, 
                String str, AttributeSet a) throws BadLocationException {
            super.insertString(fb, offset, str, a);
        }
        
        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int len, String str,
            AttributeSet attrs) throws BadLocationException {
            
            // Check final expected length
            // Note: len is # of chars that will be removed.
            int currentLen = fb.getDocument().getLength();
            int targetLen = currentLen + str.length() - len;
            if (targetLen > maxWidth) return;
            
            super.replace(fb, offset, len, str, attrs);
        }        
    }
}
