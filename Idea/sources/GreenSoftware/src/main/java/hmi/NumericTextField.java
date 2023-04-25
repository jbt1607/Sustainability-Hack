/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hmi;

import javax.swing.JTextField;
import javax.swing.text.*;

public class NumericTextField extends JTextField {
   private boolean decimalpoint = false;
    public NumericTextField() {
        super();
        setDocument(new NumericDocument());

    }

    public double getDouble() {
        if (this.getText().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(this.getText());
    }

    private class NumericDocument extends PlainDocument {

        @Override
        public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
            if (s == null) {
                return;
            }
            try {
                if(s.equals(".")== false || decimalpoint == true)
                    Integer.valueOf(s); // try to parse the input as an integer
                else decimalpoint = true;
                
                 super.insertString(offset, s, attributeSet);
            } catch (NumberFormatException e) {
                // ignore the input if it's not a valid integer
            }
        }
    }
}
