/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source;

import util.MPSoCConfig;

/**
 *
 * @author Marcelo
 */
public class RouterNeighbors {

    private int XDIMENSION;
    private int YDIMENSION;
    
    /*private int vizinho_cima;
    private int vizinho_baixo;
    private int vizinho_esquerda;
    private int vizinho_direita;
    */
    public RouterNeighbors(MPSoCConfig mPSoCConfig){
        this.XDIMENSION = mPSoCConfig.getX_dimension();
        this.YDIMENSION = mPSoCConfig.getY_dimension();
    }
    
    public RouterNeighbors(int XDIMENSION, int YDIMENSION){
        this.XDIMENSION = XDIMENSION;
        this.YDIMENSION = YDIMENSION;
    }

    public int xy_to_ham_addr(int x) {

        int y;
        x = x & 0xFFFF;//limpar o header
        y = x & 0xFF;//elimina o endereco x
        x = x >> 8;//elimina o endereco y

        if ((y % 2) == 1) {
            return ((y * XDIMENSION) + (XDIMENSION - x) - 1);
        }

        return ((y * XDIMENSION) + x);

    }
    
    public String XYAdressToXYLabel(int hamAddres){
        int x = ham_to_xy_addr(hamAddres);
        int y;
        x = x & 0xFFFF;//limpar o header
        y = x & 0xFF;//elimina o endereco x
        x = x >> 8;//elimina o endereco y
        
        return x+""+y;
    }

    public int ham_to_xy_addr(int addr) {

        int x = 0;
        int y = 0;

        addr = addr & 0xFF; //limpar o header

        while (addr - XDIMENSION >= 0) {
            addr -= XDIMENSION;
            y++;
        }

        if ((y % 2) == 1) {
            x = XDIMENSION - addr - 1;
        } else {
            x = addr;
        }

        return ((x << 8) | y);
    }

    public int getVizinho_cima(int router_address) {
        router_address = ham_to_xy_addr(router_address);
        
        int y = router_address & 0xFF;
        int x = router_address >> 8;
        
        y++;
        
        if (y < YDIMENSION)
            return xy_to_ham_addr((x << 8) | y);
        return -1;
    }

    public int getVizinho_baixo(int router_address) {
        router_address = ham_to_xy_addr(router_address);
        int y = router_address & 0xFF;
        int x = router_address >> 8;
        
        y--;
        
        if (y >= 0)
            return xy_to_ham_addr((x << 8) | y);
        return -1;
       
    }

    public int getVizinho_esquerda(int router_address) {
        router_address = ham_to_xy_addr(router_address);
        int y = router_address & 0xFF;
        int x = router_address >> 8;
        
        x--;
        
        if (x >= 0)
            return xy_to_ham_addr((x << 8) | y);
        return -1;
        
    }

    public int getVizinho_direita(int router_address) {
        router_address = ham_to_xy_addr(router_address);
        int y = router_address & 0xFF;
        int x = router_address >> 8;
        
        x++;
        
        if (x < XDIMENSION)
            return xy_to_ham_addr((x << 8) | y);
        return -1;
        
    }
    
    public int getXCoordinate(int ham_addr){
        return ham_to_xy_addr(ham_addr) >> 8;
    }
    
    public int getYCoordinate(int ham_addr){
        return ham_to_xy_addr(ham_addr) & 0xFF;
    }
}
