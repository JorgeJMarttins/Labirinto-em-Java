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
            String linhaStr = br.readLine();
            String colunaStr = br.readLine();

            if ((linhaStr == null || colunaStr == null) || (linhaStr == "" || colunaStr == ""))
                throw new Exception("O labirinto deve ter linhas e colunas declaradas");

            try {
                this.linha = Integer.parseInt(linhaStr);
                this.coluna = Integer.parseInt(colunaStr);
            } catch (NumberFormatException e) {
                throw new Exception("O labirinto deve ter linhas e colunas declaradas");
            }

            if (this.linha < 4) 
                throw new Exception("O labirinto é pequeno demais com menos de 4 linhas.");

            if (this.coluna < 4)
                throw new Exception("O labirinto é pequeno demais com menos de 4 colunas.");

            this.labirinto = new char[this.linha][this.coluna];

            for (int i = 0; i < this.linha; i++) {
                String linhaTexto = br.readLine();

                if (linhaTexto == null || linhaTexto.length() != this.coluna)
                    throw new Exception("Linhas do labirinto incompatíveis com dimensões declaradas.");

                for (int j = 0; j < this.coluna; j++)
                    this.labirinto[i][j] = linhaTexto.charAt(j);
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

        for (int i = 0; i < this.linha; i++)
            for (int j = 0; j < this.coluna; j++) 
            {
                if (this.labirinto[i][j] == 'E') entrada++;
                if (this.labirinto[i][j] == 'S') saida++;
            }

        if (entrada != 1 || saida != 1)
            throw new Exception("Labirinto deve conter exatamente uma entrada 'E' e uma saída 'S'.");

        Coordenada[] extremidades = {
            new Coordenada(0, 0), new Coordenada(0, this.coluna - 1),
            new Coordenada(this.linha - 1, 0), new Coordenada(this.linha - 1, this.coluna - 1)
        };

        for (Coordenada c : extremidades) 
        {
            char atual = this.labirinto[c.getLinha()][c.getColuna()];

            if (atual == 'E' || atual == 'S')
                throw new Exception("Entrada ou saída está em uma posição sem acesso ao interior.");
        }

        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
            {
                if (this.labirinto[i][j] == 'E') 
                {
                    boolean bloqueado = true;
                    int[][] direcoes = { {0,1}, {1,0}, {0,-1}, {-1,0} };

                    for (int[] d : direcoes) 
                    {
                        int ni = i + d[0], nj = j + d[1];

                        if (ni >= 0 && ni < this.linha && nj >= 0 && nj < this.coluna)
                            if (this.labirinto[ni][nj] == ' ')
                                bloqueado = false;
                    }

                    if (bloqueado)
                        throw new Exception("Entrada 'E' sem caminho livre ao redor.");
                }
            }
        }

        // Verifica se a saída tem pelo menos um caminho livre ao redor. ESTÁ DANDO ERRO
        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
            {
                if (this.labirinto[i][j] == 'S') 
                {
                    boolean bloqueado = true;
                    int[][] direcoes = { {0,1}, {1,0}, {0,-1}, {-1,0} };

                    for (int[] d : direcoes) 
                    {
                        int ni = i + d[0], nj = j + d[1];

                        if (ni >= 0 && ni < this.linha && nj >= 0 && nj < this.coluna)
                        {
                            char c = this.labirinto[ni][nj];
                            if (c == ' ' || c == 'E') {
                                bloqueado = false;
                                break;
                            }
                        }
                    }

                    if (bloqueado)
                        throw new Exception("A saída 'S' está inacessível (totalmente cercada).");
                }
            }
        }

        for (int j = 0; j < this.coluna; j++) 
        {
            if (this.labirinto[0][j] != '#' && this.labirinto[0][j] != 'E' && this.labirinto[0][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (this.labirinto[this.linha - 1][j] != '#' && this.labirinto[this.linha - 1][j] != 'E' && this.labirinto[this.linha - 1][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }

        for (int i = 0; i < this.linha; i++) 
        {
            if (this.labirinto[i][0] != '#' && this.labirinto[i][0] != 'E' && this.labirinto[i][0] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (this.labirinto[i][this.coluna - 1] != '#' && this.labirinto[i][this.coluna - 1] != 'E' && this.labirinto[i][this.coluna - 1] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }
    }

    private void verificarEstruturaDeLabirinto() throws Exception 
    {
        int paredesInternas = 0;
        int bifurcacoes = 0;

        for (int i = 1; i < this.linha - 1; i++) 
        {
            for (int j = 1; j < this.coluna - 1; j++) 
            {
                if (this.labirinto[i][j] == '#') paredesInternas++;

                if (this.labirinto[i][j] == ' ') {
                    int caminhos = 0;

                    if (this.labirinto[i - 1][j] == ' ') caminhos++;
                    if (this.labirinto[i + 1][j] == ' ') caminhos++;
                    if (this.labirinto[i][j - 1] == ' ') caminhos++;
                    if (this.labirinto[i][j + 1] == ' ') caminhos++;

                    if (caminhos >= 2) bifurcacoes++;
                }
            }
        }

        if (paredesInternas < 1)
            throw new Exception("O labirinto não possui paredes internas suficientes.");

        int minimoBifurcacoes = (this.linha >= 4 && this.coluna >= 4) ? 2 : 1;

        if (bifurcacoes < minimoBifurcacoes)
            throw new Exception("Labirinto com poucas bifurcações internas, não caracteriza um labirinto real.");
    }

    public boolean encontrarEntrada() throws Exception 
    {
        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
            {
                if (this.labirinto[i][j] == 'E') 
                {
                    this.atual = new Coordenada(i, j);
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

        this.encontrouSaida = false;
        this.caminho = new Pilha<Coordenada>(this.labirinto.length * this.labirinto[0].length);
        this.possibilidades = new Pilha<Fila<Coordenada>>(this.labirinto.length * this.labirinto[0].length);

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
        // Posição atual
        this.atual = new Coordenada(linhaAtual, colunaAtual);
    
        // Verifica se a posição está fora dos limites do labirinto
        if (linhaAtual < 0 || linhaAtual >= this.linha || colunaAtual < 0 || colunaAtual >= this.coluna)
            return false;
    
        // Verifica se a célula é uma parede, já visitada ou caminho morto
        char celula = this.labirinto[linhaAtual][colunaAtual];
        if (celula == '#' || celula == '*' || celula == 'x') 
            return false;
    
        // Adiciona a coordenada atual na pilha do caminho
        this.caminho.guardeUmItem(this.atual);
    
        // Se encontrou a saída, encerra a busca com sucesso
        if (celula == 'S') 
            return true;
    
        // Marca a célula como visitada (exceto se for a entrada)
        if (celula != 'E') 
            this.labirinto[linhaAtual][colunaAtual] = '*';
    
        // Cria uma fila para armazenar as coordenadas adjacentes válidas
        this.fila = new Fila<Coordenada>(3);
    
        // Direções possíveis: baixo, cima, direita, esquerda
        int[][] direcoes = { {1,0}, {-1,0}, {0,1}, {0,-1} };
    
        // Verifica cada direção possível
        for (int[] dir : direcoes) 
        {
            int ni = linhaAtual + dir[0];
            int nj = colunaAtual + dir[1];
    
            // Se estiver dentro dos limites do labirinto
            if (ni >= 0 && ni < this.linha && nj >= 0 && nj < this.coluna) 
            {
                char proximaCelula = this.labirinto[ni][nj];
    
                // Adiciona à fila apenas se for espaço livre ou saída
                if (proximaCelula == ' ' || proximaCelula == 'S') 
                    this.fila.guardeUmItem(new Coordenada(ni, nj));
            }
        }
    
        // Guarda essa fila de possibilidades na pilha
        this.possibilidades.guardeUmItem(this.fila);
    
        // Enquanto houver caminhos possíveis para explorar
        while (!this.possibilidades.isVazia()) 
        {
            // Recupera a fila do topo da pilha (últimas possibilidades)
            Fila<Coordenada> filaTopo = this.possibilidades.recupereUmItem();
    
            // Tenta cada coordenada da fila
            while (!filaTopo.isVazia()) 
            {
                Coordenada proxima = filaTopo.recupereUmItem();
                filaTopo.removaUmItem();
    
                // Chama recursivamente a função para a próxima coordenada
                if (resolverCaminho(proxima.getLinha(), proxima.getColuna()))
                    return true; // Se chegar à saída, retorna sucesso
            }
    
            // Se a fila de possibilidades se esgotou, remove-a da pilha
            this.possibilidades.removaUmItem();
    
            // Remove a última coordenada do caminho (backtracking)
            Coordenada ultima = this.caminho.recupereUmItem();
            this.caminho.removaUmItem();
    
            // Marca como caminho morto (exceto a entrada)
            if (this.labirinto[ultima.getLinha()][ultima.getColuna()] != 'E')
                this.labirinto[ultima.getLinha()][ultima.getColuna()] = 'x';
        }
    
        // Se nenhuma possibilidade levar à saída, retorna false
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
                    bw.write(this.labirinto[i][j]);
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
        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++)
                System.out.print(this.labirinto[i][j]);
            System.out.println();
        }
    }

    private void imprimirCaminho() throws Exception 
    {
        // Cria uma pilha para armazenar o caminho invertido
        Pilha<Coordenada> inverso = new Pilha<Coordenada>(this.labirinto.length * this.labirinto[0].length);

        // Transfere as coordenadas do caminho para a pilha inversa
        while (!this.caminho.isVazia()) 
        {
            Coordenada c = this.caminho.recupereUmItem();
            inverso.guardeUmItem(c);
            this.caminho.removaUmItem();
        }

        // Imprime o caminho da entrada até a saída
        System.out.println("Caminho da entrada até a saída:");

        // Imprime o caminho invertido
        while (!inverso.isVazia()) 
        {
            Coordenada c = inverso.recupereUmItem();
            System.out.print("(" + c.getLinha() + "," + c.getColuna() + ") ");
            inverso.removaUmItem();
        }

        // Finaliza a impressão com uma quebra de linha
        System.out.println();
    }

    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Labirinto (" + this.linha + "x" + this.coluna + "):\n");

        for (int i = 0; i < this.linha; i++)
        {
            for (int j = 0; j < this.coluna; j++) 
                sb.append(this.labirinto[i][j]);

            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int hashCode() 
    {
        int ret = 17;
        
        ret = 31 * ret + this.linha;
        ret = 31 * ret + this.coluna;
        ret = 31 * ret + (this.caminho != null ? this.caminho.hashCode() : 0);
        ret = 31 * ret + (this.possibilidades != null ? this.possibilidades.hashCode() : 0);
        ret = 31 * ret + (this.fila != null ? this.fila.hashCode() : 0);
        ret = 31 * ret + (this.atual != null ? this.atual.hashCode() : 0);
        ret = 31 * ret + (this.encontrouSaida ? 1 : 0);

        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
                ret = 31 * ret + Character.hashCode(this.labirinto[i][j]);
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Labirinto lab = (Labirinto) obj;

        if (this.linha != lab.linha || this.coluna != lab.coluna) return false;

        if (this.caminho != null ? !this.caminho.equals(lab.caminho) : lab.caminho != null) return false;
        if (this.possibilidades != null ? !possibilidades.equals(lab.possibilidades) : lab.possibilidades != null)
            return false;
        if (this.fila != null ? !this.fila.equals(lab.fila) : lab.fila != null) return false;
        if (this.atual != null ? !this.atual.equals(lab.atual) : lab.atual != null) return false;
        if (this.encontrouSaida != lab.encontrouSaida) return false;

        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
                if (this.labirinto[i][j] != lab.labirinto[i][j]) return false;
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
