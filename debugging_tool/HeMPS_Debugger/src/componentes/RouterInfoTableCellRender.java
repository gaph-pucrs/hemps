/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package componentes;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author mruaro
 */
public class RouterInfoTableCellRender extends DefaultTableCellRenderer {

    
    public RouterInfoTableCellRender() {
        super();
       
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value.toString().contains("*")){
             this.setForeground(Color.white);
             this.setBackground(Color.blue);
           
        } else {
            this.setBackground(Color.white);
            this.setForeground(Color.black);
        }
        
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}