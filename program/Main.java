package program;

import program.labirinto.Labirinto;

public class Main {
    public static void main(String[] args) 
    {
        try {
            Labirinto lab = new Labirinto("teste3.txt");

            System.out.println("Labirinto original:");
            lab.imprimirLabirinto();

            lab.resolverLabirinto("teste3_resolvido.txt"); // Salva em outro arquivo

            System.out.println("\nLabirinto resolvido:");
            lab.imprimirLabirinto();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}