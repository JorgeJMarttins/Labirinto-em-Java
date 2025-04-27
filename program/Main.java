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
            Labirinto lab = new Labirinto("teste1.txt");

            lab.imprimirLabirinto();
            System.out.println(lab.encontrarEntrada());
        } 
        catch (Exception e) {
            System.err.println("Erro! " + e.getMessage());
        }
    }
}
