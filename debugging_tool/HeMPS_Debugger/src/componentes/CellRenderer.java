/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package componentes;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.table.DefaultTableCellRenderer;

public class CellRenderer extends DefaultTableCellRenderer {

    public CellRenderer() {
        super();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (column == 2) {
            this.setForeground(Color.white);
            if (value.equals("checked")) {
                this.setBackground(Color.blue);
            } else if (value.equals("pending")) {
                this.setBackground(Color.red);
            }
        } else {
            this.setBackground(Color.white);
            this.setForeground(Color.black);
        }
        
        if (column > 0){
            this.setHorizontalAlignment(CENTER);
        } else {
            this.setHorizontalAlignment(CENTER);
        }



        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}