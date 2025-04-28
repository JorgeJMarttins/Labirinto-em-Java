package program.labirinto;

import program.coordenada.Coordenada;
import program.pilha.Pilha;
import program.fila.Fila;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Labirinto 
{
    private char[][] labirinto;
    private int linha, coluna;
    private Pilha<Coordenada> caminho;
    private Pilha<Fila<Coordenada>> possibilidades;
    private Coordenada atual;
    private Fila<Coordenada> fila;

    public Labirinto(String arq) throws Exception
    {
        caminho = new Pilha<>(100);
        possibilidades = new Pilha<>(100);
        leitura(arq);
    }

    public Labirinto() throws Exception
    {
        caminho = new Pilha<>(100);
        possibilidades = new Pilha<>(100);
        fila = new Fila<>(4);
    }

    public void leitura(String arq)
    {
        try (BufferedReader br = new BufferedReader(new FileReader("testes/" + arq))) 
        {
            linha = Integer.parseInt(br.readLine());
            coluna = Integer.parseInt(br.readLine());
            labirinto = new char[linha][coluna];

            for (int i = 0; i < linha; i++) 
            {
                String linhaTexto = br.readLine();
                for (int j = 0; j < coluna; j++) 
                    labirinto[i][j] = linhaTexto.charAt(j);
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public void imprimirLabirinto()
    {
        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
                System.out.print(labirinto[i][j]);
            System.out.println();
        }
    }

    public boolean encontrarEntrada() throws Exception
    {
        for (int j = 0; j < coluna; j++)
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

        for (int i = 0; i < linha; i++)
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

    private void marcarCaminhoFinal() throws Exception
    {
        Pilha<Coordenada> caminhoAuxiliar = new Pilha<>(100);

        while (!caminho.isVazia())
        {
            Coordenada c = caminho.recupereUmItem();
            caminhoAuxiliar.guardeUmItem(c);
            caminho.removaUmItem();
        }

        while (!caminhoAuxiliar.isVazia())
        {
            Coordenada c = caminhoAuxiliar.recupereUmItem();
            caminho.guardeUmItem(c);
            caminhoAuxiliar.removaUmItem();

            if (labirinto[c.getLinha()][c.getColuna()] == ' ')
                labirinto[c.getLinha()][c.getColuna()] = '*';
        }
    }

    public void resolverLabirinto(String arquivoSaida) throws Exception
    {
        if (!encontrarEntrada())
        {
            System.out.println("Erro: Não há entrada no labirinto.");
            return;
        }

        caminho.guardeUmItem(atual);

        while (true)
        {
            if (labirinto[atual.getLinha()][atual.getColuna()] == 'S') 
            {
                System.out.println("Labirinto resolvido!");
                marcarCaminhoFinal();
                escreverLabirinto(arquivoSaida);
                return;
            }

            if (labirinto[atual.getLinha()][atual.getColuna()] == ' ')
                labirinto[atual.getLinha()][atual.getColuna()] = '*';

            fila = new Fila<>(4);

            verificarDirecao(atual.getLinha(), atual.getColuna() - 1); // Esquerda
            verificarDirecao(atual.getLinha(), atual.getColuna() + 1); // Direita
            verificarDirecao(atual.getLinha() - 1, atual.getColuna()); // Cima
            verificarDirecao(atual.getLinha() + 1, atual.getColuna()); // Baixo

            if (!fila.isVazia())
            {
                possibilidades.guardeUmItem(fila);

                Coordenada proxima = fila.recupereUmItem();
                fila.removaUmItem();

                atual = proxima;
                caminho.guardeUmItem(atual);

                System.out.println(this.toString());
                Thread.sleep(50);
            }
            else
            {
                boolean achouAlternativa = false;

                while (!possibilidades.isVazia())
                {
                    fila = possibilidades.recupereUmItem();
                    possibilidades.removaUmItem();

                    if (!fila.isVazia())
                    {
                        Coordenada proxima = fila.recupereUmItem();
                        fila.removaUmItem();

                        atual = proxima;
                        caminho.guardeUmItem(atual);

                        possibilidades.guardeUmItem(fila);

                        System.out.println(this.toString());
                        Thread.sleep(50);
                        achouAlternativa = true;
                        break;
                    }
                    else
                    {
                        if (!caminho.isVazia())
                            caminho.removaUmItem();
                        if (!caminho.isVazia())
                            atual = caminho.recupereUmItem();
                    }
                }

                if (!achouAlternativa)
                {
                    System.out.println("Labirinto sem saída!");
                    escreverLabirinto(arquivoSaida);
                    return;
                }
            }
        }
    }


    private void verificarDirecao(int linha, int coluna)
    {
        if (linha >= 0 && linha < this.linha && coluna >= 0 && coluna < this.coluna) 
        {
            if (labirinto[linha][coluna] == ' ' || labirinto[linha][coluna] == 'S') 
            {
                try 
                {
                    fila.guardeUmItem(new Coordenada(linha, coluna));
                } 
                catch (Exception e) 
                {
                    System.out.println("Erro ao adicionar na fila: " + e.getMessage());
                }
            }
        }
    }

    private void escreverLabirinto(String nomeArquivo)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("testes/" + nomeArquivo))) 
        {
            bw.write(this.linha + "\n");
            bw.write(this.coluna + "\n");

            for (int i = 0; i < this.linha; i++) 
            {
                for (int j = 0; j < this.coluna; j++) 
                    bw.write(labirinto[i][j]);
                bw.newLine();
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }

    @Override
    public String toString()
    {
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
                st.append(labirinto[i][j]);
            st.append('\n');
        }
        return st.toString();
    }
}
