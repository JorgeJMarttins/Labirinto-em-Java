package program.labirinto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Labirinto 
{
    private char[][] labirinto;
    private int linha, coluna;

    public Labirinto (String arq)
    {
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

@Override
public String toString() {
    StringBuilder st = new StringBuilder();
    for (int i = 0; i < linhas; i++) {
        for (int j = 0; j < colunas; j++) {
            st.append(labirinto[i][j]);  
        }
        st.append('\n');
    }
    return st.toString();  
}


@Override
public int hashCode() {
    int ret = 1;
    ret = 31 * ret + linhas;
    ret = 31 * ret + colunas;
   
    for (int i = 0; i < linhas; i++) {
        for (int j = 0; j < colunas; j++) {
            ret = 31 * ret + labirinto[i][j];  
        }
    }
    
    return ret < 0 ? -ret : ret; 
}


    @Override
    public boolean equals(Object obj)
    {
        if (this==obj) return true;
        if (obj==null) return false;
        if (this.getClass()!=obj.getClass()) return false;

        Labirinto lab = (Labirinto) obj;
        
        if (this.linhas != lab.linhas || this.colunas != lab.colunas) return false;
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                 if (this.labirinto[i][j] != outro.labirinto[i][j])
                return false;
        }
    }
    return true;
}
}
