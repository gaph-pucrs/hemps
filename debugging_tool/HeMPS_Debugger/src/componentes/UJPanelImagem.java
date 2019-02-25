/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package componentes;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Marcelo
 */
public class UJPanelImagem extends JPanel {

    private Image imagem = null;

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(imagem, 0, 0, this.getWidth(), this.getHeight(), this);
    }

    public void setImagem(String path) {
        try {
            this.imagem = ImageIO.read(this.getClass().getResource(path));
        } catch (IOException ex) {
            Logger.getLogger(UJPanelImagem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
