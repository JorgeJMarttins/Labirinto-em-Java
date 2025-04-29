package program.labirinto;

import program.coordenada.Coordenada;
import program.pilha.Pilha;
import program.fila.Fila;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Labirinto implements Cloneable
{
    private char[][] labirinto;
    private int linha, coluna;
    private Pilha<Coordenada> caminho;
    private Pilha<Fila<Coordenada>> possibilidades;
    private Fila<Coordenada> fila;
    private Coordenada atual;
    private boolean encontrouSaida;

    public Labirinto(String arq) throws Exception 
    {
        leitura(arq);
    }

    public void leitura(String arq) throws Exception 
    {
        try (BufferedReader br = new BufferedReader(new FileReader("testes/" + arq))) 
        {
            linha = Integer.parseInt(br.readLine());
            coluna = Integer.parseInt(br.readLine());

            if (linha < 4) 
                throw new Exception("O labirinto é pequeno demais com menos de 4 linhas.");

            if (coluna < 4)
                throw new Exception("O labirinto é pequeno demais com menos de 4 colunas.");

            labirinto = new char[linha][coluna];

            for (int i = 0; i < linha; i++) {
                String linhaTexto = br.readLine();

                if (linhaTexto == null || linhaTexto.length() != coluna)
                    throw new Exception("Linhas do labirinto incompatíveis com dimensões declaradas.");

                for (int j = 0; j < coluna; j++)
                    labirinto[i][j] = linhaTexto.charAt(j);
            }

            verificarIntegridadeBasica();
            verificarEstruturaDeLabirinto();
        } 
        catch (IOException e) 
        {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private void verificarIntegridadeBasica() throws Exception 
    {
        int entrada = 0, saida = 0;

        for (int i = 0; i < linha; i++)
            for (int j = 0; j < coluna; j++) 
            {
                if (labirinto[i][j] == 'E') entrada++;
                if (labirinto[i][j] == 'S') saida++;
            }

        if (entrada != 1 || saida != 1)
            throw new Exception("Labirinto deve conter exatamente uma entrada 'E' e uma saída 'S'.");

        Coordenada[] extremidades = {
            new Coordenada(0, 0), new Coordenada(0, coluna - 1),
            new Coordenada(linha - 1, 0), new Coordenada(linha - 1, coluna - 1)
        };

        for (Coordenada c : extremidades) 
        {
            char atual = labirinto[c.getLinha()][c.getColuna()];

            if (atual == 'E' || atual == 'S')
                throw new Exception("Entrada ou saída está em uma posição sem acesso ao interior.");
        }

        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
            {
                if (labirinto[i][j] == 'E' || labirinto[i][j] == 'S') 
                {
                    boolean bloqueado = true;
                    int[][] direcoes = { {0,1}, {1,0}, {0,-1}, {-1,0} };

                    for (int[] d : direcoes) 
                    {
                        int ni = i + d[0], nj = j + d[1];

                        if (ni >= 0 && ni < linha && nj >= 0 && nj < coluna)
                            if (labirinto[ni][nj] == ' ')
                                bloqueado = false;
                    }

                    if (bloqueado)
                        throw new Exception("Entrada ou saída sem caminho livre ao redor.");
                }
            }
        }

        for (int j = 0; j < coluna; j++) 
        {
            if (labirinto[0][j] != '#' && labirinto[0][j] != 'E' && labirinto[0][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (labirinto[linha - 1][j] != '#' && labirinto[linha - 1][j] != 'E' && labirinto[linha - 1][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }

        for (int i = 0; i < linha; i++) 
        {
            if (labirinto[i][0] != '#' && labirinto[i][0] != 'E' && labirinto[i][0] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (labirinto[i][coluna - 1] != '#' && labirinto[i][coluna - 1] != 'E' && labirinto[i][coluna - 1] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }
    }

    private void verificarEstruturaDeLabirinto() throws Exception 
    {
        int paredesInternas = 0;
        int bifurcacoes = 0;

        for (int i = 1; i < linha - 1; i++) 
        {
            for (int j = 1; j < coluna - 1; j++) 
            {
                if (labirinto[i][j] == '#') paredesInternas++;

                if (labirinto[i][j] == ' ') {
                    int caminhos = 0;

                    if (labirinto[i - 1][j] == ' ') caminhos++;
                    if (labirinto[i + 1][j] == ' ') caminhos++;
                    if (labirinto[i][j - 1] == ' ') caminhos++;
                    if (labirinto[i][j + 1] == ' ') caminhos++;

                    if (caminhos >= 2) bifurcacoes++;
                }
            }
        }

        if (paredesInternas < 1)
            throw new Exception("O labirinto não possui paredes internas suficientes.");

        int minimoBifurcacoes = (linha >= 4 && coluna >= 4) ? 2 : 1;

        if (bifurcacoes < minimoBifurcacoes)
            throw new Exception("Labirinto com poucas bifurcações internas, não caracteriza um labirinto real.");
    }

    public boolean encontrarEntrada() throws Exception 
    {
        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
            {
                if (labirinto[i][j] == 'E') 
                {
                    atual = new Coordenada(i, j);
                    return true;
                }
            }
        }

        return false;
    }

    public void resolverLabirinto(String arquivoSaida) throws Exception 
    {
        if (!encontrarEntrada()) 
        {
            System.out.println("Erro: Não há entrada no labirinto.");
            return;
        }

        encontrouSaida = false;

        caminho = new Pilha<Coordenada> (this.labirinto.length*this.labirinto[0].length);
        possibilidades = new Pilha<Fila<Coordenada>> (this.labirinto.length*this.labirinto[0].length);
        fila = new Fila<Coordenada>(3);

        if (resolverCaminho(atual.getLinha(), atual.getColuna())) 
        {
            System.out.println("\nLabirinto resolvido!\n");
            escreverLabirinto(arquivoSaida);
            imprimirCaminho();
            imprimirLabirinto();
        } 
        else 
        {
            System.out.println("\nLabirinto sem saída!\n");
            escreverLabirinto(arquivoSaida);
        }
    }

    private boolean resolverCaminho(int linhaAtual, int colunaAtual) throws Exception 
    {
        if (linhaAtual < 0 || linhaAtual >= linha || colunaAtual < 0 || colunaAtual >= coluna)
            return false;

        char celula = labirinto[linhaAtual][colunaAtual];

        if (celula == '#' || celula == '*')
            return false;

        caminho.guardeUmItem(new Coordenada(linhaAtual, colunaAtual));

        if (celula == 'S') return true;

        if (celula != 'E') labirinto[linhaAtual][colunaAtual] = '*';

        Fila<Coordenada> novasPosicoes = new Fila<Coordenada>(3);
        if (linhaAtual + 1 < linha && labirinto[linhaAtual + 1][colunaAtual] != '#') novasPosicoes.guardeUmItem(new Coordenada(linhaAtual + 1, colunaAtual));
        if (linhaAtual - 1 >= 0 && labirinto[linhaAtual - 1][colunaAtual] != '#') novasPosicoes.guardeUmItem(new Coordenada(linhaAtual - 1, colunaAtual));
        if (colunaAtual + 1 < coluna && labirinto[linhaAtual][colunaAtual + 1] != '#') novasPosicoes.guardeUmItem(new Coordenada(linhaAtual, colunaAtual + 1));
        if (colunaAtual - 1 >= 0 && labirinto[linhaAtual][colunaAtual - 1] != '#') novasPosicoes.guardeUmItem(new Coordenada(linhaAtual, colunaAtual - 1));
        
        if (!novasPosicoes.isVazia()) 
            possibilidades.guardeUmItem(novasPosicoes);

        if (resolverCaminho(linhaAtual + 1, colunaAtual) ||
            resolverCaminho(linhaAtual - 1, colunaAtual) ||
            resolverCaminho(linhaAtual, colunaAtual + 1) ||
            resolverCaminho(linhaAtual, colunaAtual - 1)) 
        {
            return true;
        }

        if (labirinto[linhaAtual][colunaAtual] != 'E')
            labirinto[linhaAtual][colunaAtual] = ' ';

        caminho.removaUmItem();

        return false;
    }

    private void escreverLabirinto(String nomeArquivo) 
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("testes/" + nomeArquivo))) 
        {
            bw.write(this.linha + "\n");
            bw.write(this.coluna + "\n");

            for (int i = 0; i < this.linha; i++) {
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

    public void imprimirLabirinto() 
    {
        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++)
                System.out.print(labirinto[i][j]);
            System.out.println();
        }
    }

    private void imprimirCaminho() throws Exception 
    {
        Pilha<Coordenada> inverso = new Pilha<Coordenada>(this.labirinto.length*this.labirinto[0].length);
        Pilha<Coordenada> backup = new Pilha<Coordenada>(this.labirinto.length*this.labirinto[0].length);

        while (!caminho.isVazia()) 
        {
            Coordenada c = caminho.recupereUmItem();
            inverso.guardeUmItem(c);
            backup.guardeUmItem(c);
            caminho.removaUmItem();
        }

        while (!backup.isVazia()) 
        {
            caminho.guardeUmItem(backup.recupereUmItem());
            backup.removaUmItem();
        }

        System.out.println("Caminho da entrada até a saída:");

        while (!inverso.isVazia()) 
        {
            Coordenada c = inverso.recupereUmItem();
            System.out.print("(" + c.getLinha() + "," + c.getColuna() + ") ");
            inverso.removaUmItem();
        }

        System.out.println();
    }

    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Labirinto (" + linha + "x" + coluna + "):\n");

        for (int i = 0; i < linha; i++)
        {
            for (int j = 0; j < coluna; j++) 
                sb.append(labirinto[i][j]);

            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int hashCode() 
    {
        int ret = 17;
        
        ret = 31 * ret + linha;
        ret = 31 * ret + coluna;
        ret = 31 * ret + (caminho != null ? caminho.hashCode() : 0);
        ret = 31 * ret + (possibilidades != null ? possibilidades.hashCode() : 0);
        ret = 31 * ret + (fila != null ? fila.hashCode() : 0);
        ret = 31 * ret + (atual != null ? atual.hashCode() : 0);
        ret = 31 * ret + (encontrouSaida ? 1 : 0);

        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
                ret = 31 * ret + Character.hashCode(labirinto[i][j]);
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Labirinto lab = (Labirinto) obj;

        if (linha != lab.linha || coluna != lab.coluna) return false;

        if (caminho != null ? !caminho.equals(lab.caminho) : lab.caminho != null) return false;
        if (possibilidades != null ? !possibilidades.equals(lab.possibilidades) : lab.possibilidades != null)
            return false;
        if (fila != null ? !fila.equals(lab.fila) : lab.fila != null) return false;
        if (atual != null ? !atual.equals(lab.atual) : lab.atual != null) return false;
        if (encontrouSaida != lab.encontrouSaida) return false;

        for (int i = 0; i < linha; i++) 
        {
            for (int j = 0; j < coluna; j++) 
                if (labirinto[i][j] != lab.labirinto[i][j]) return false;
        }

        return true;
    }

    public Labirinto(Labirinto modelo) throws Exception 
    {
        if (modelo == null) 
            throw new Exception("Labirinto não instanciado!");
    
        this.linha = modelo.linha;
        this.coluna = modelo.coluna;
    
        this.labirinto = new char[modelo.linha][modelo.coluna];
        for (int i = 0; i < modelo.linha; i++) 
        {
            System.arraycopy(modelo.labirinto[i], 0, this.labirinto[i], 0, modelo.coluna);
        }
    
        this.caminho = new Pilha<Coordenada> (modelo.caminho);
        this.possibilidades = new Pilha<Fila<Coordenada>> (modelo.possibilidades);
        this.fila = new Fila<Coordenada> (modelo.fila);

        this.atual = (modelo.atual != null) ? new Coordenada(modelo.atual) : null;
    
        this.encontrouSaida = modelo.encontrouSaida;
    }
    
    @Override
    public Object clone()
    {
        Labirinto ret = null;

        try 
        {
            ret = new Labirinto(this);
        } 
        catch (Exception erro) 
        {}

        return ret;
    }

}
