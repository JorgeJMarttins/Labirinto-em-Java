package program;

import program.pilha.Pilha;
import program.fila.Fila;
import program.coordenada.Coordenada;
import program.labirinto.Labirinto;

public class Main 
{
    public static void main(String[] args)
    {
        try 
        {
            Labirinto lab = new Labirinto("teste3.txt");

            // Mostra o labirinto original
            System.out.println("Labirinto original:");
            lab.imprimirLabirinto();

            // Resolve o labirinto
            lab.resolverLabirinto("teste3.txt");

            // Mostra o labirinto após resolver
            System.out.println("\nLabirinto resolvido:");
            lab.imprimirLabirinto();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
