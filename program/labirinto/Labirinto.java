package program.labirinto;

import program.coordenada.Coordenada;
import program.pilha.Pilha;
import program.fila.Fila;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Labirinto 
{
    private char[][] labirinto;
    private int linha, coluna;
    private Pilha<Coordenada> caminho;
    private Pilha<Fila<Coordenada>> possibilidades;
    private Coordenada atual;

    public Labirinto (String arq) throws Exception
    {
        caminho = new Pilha<Coordenada> (45);
        possibilidades = new Pilha<Fila<Coordenada>> (45); 
        leitura(arq);
    }

    public void leitura (String arq)
    {
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader("testes/" + arq));

            linha = Integer.parseInt(br.readLine());
            coluna = Integer.parseInt(br.readLine());

            labirinto = new char[linha][coluna];

            for (int i = 0; i < linha; i++) 
            {
                String linhaTexto = br.readLine();

                for (int j=0; j<coluna; j++) 
                    labirinto[i][j] = linhaTexto.charAt(j);
            }

            br.close();
        } 
        catch (IOException e) 
        {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public void imprimirLabirinto ()
    {
        for (int i=0; i<linha; i++)
        {
            for (int j=0; j<coluna; j++)
                System.out.print(labirinto[i][j]);

            System.out.println();
        }
    }

    public boolean encontrarEntrada() throws Exception
    {
        for (int j=0; j<coluna; j++)
        {
            if (labirinto[0][j] == 'E')
            {
                atual = new Coordenada(0, j);
                return true;
            }

            if (labirinto[linha - 1][j] == 'E')
            {
                atual = new Coordenada(linha - 1, j);
                return true;
            }
        }

        for (int i=0; i<linha; i++)
        {
            if (labirinto[i][0] == 'E')
            {
                atual = new Coordenada(i, 0);
                return true;
            }

            if (labirinto[i][coluna - 1] == 'E')
            {
                atual = new Coordenada(i, coluna - 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() 
    {
        StringBuilder st = new StringBuilder();

        for (int i=0; i<linha; i++) 
        {
            for (int j=0; j<coluna; j++) 
                st.append(labirinto[i][j]);  

            st.append('\n');
        }

        return st.toString();  
    }


    @Override
    public int hashCode() 
    {
        int ret=1;

        ret *= 7 + ((Integer)(this.linha  )).hashCode();
        ret *= 7 + ((Integer)(this.coluna )).hashCode();
    
        for (int i=0; i<linha; i++) 
        {
            for (int j=0; j<coluna; j++) 
                ret *= 7 + ((Character)(this.labirinto[i][j])).hashCode();  
        }
        
        if (ret<0) 
            return ret=-ret;

        return ret;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this==obj) return true;
        if (obj==null) return false;
        if (this.getClass()!=obj.getClass()) return false;

        Labirinto lab = (Labirinto) obj;
        
        if (this.linha!=lab.linha) return false;
        if (this.coluna!=lab.coluna) return false;

        for (int i=0; i<linha; i++) 
        {
            for (int j=0; j<coluna; j++) 
            {
                if (this.labirinto[i][j] != lab.labirinto[i][j])
                    return false;
            }
        }

        return true;
    }
}
