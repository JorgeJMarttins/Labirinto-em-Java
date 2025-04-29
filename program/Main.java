package program;

import program.labirinto.Labirinto;

public class Main {
    public static void main(String[] args) 
    {
        try 
        {
            System.out.println();

            Labirinto lab = new Labirinto("teste6.txt");

            System.out.println("Labirinto original:");
            lab.imprimirLabirinto();

            lab.resolverLabirinto("teste6_resolvido.txt"); // Salva em outro arquivo

        } 
        catch (Exception e) 
        {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}