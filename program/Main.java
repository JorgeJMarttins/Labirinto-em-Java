package program;

import program.pilha.*;
import program.coordenada.*;
import program.labirinto.Labirinto;

public class Main 
{
    public static void main(String[] args)
    {
        try 
        {
            Labirinto lab = new Labirinto("teste1.txt");

            lab.imprimirLabirinto();
        } 
        catch (Exception e) {
            System.err.println("Erro!" + e.getMessage());
        }
    }
}
