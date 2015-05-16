/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package horarioescolar;

/**
 *
 * @author Gabriel
 */
public class HorarioEscolar {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Modelo modelo = new Modelo();
        if(modelo.readFile("entrada.txt")){
            modelo.modelo();
        } else {
            System.out.println("Não foi possível ler o arquivo.");
        }
    }
    
}
