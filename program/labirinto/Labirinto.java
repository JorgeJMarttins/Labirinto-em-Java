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

    public void leitura(String arq) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("testes/" + arq))) {
            String linhaStr = br.readLine();
            String colunaStr = br.readLine();

            if (linhaStr == null || colunaStr == null || linhaStr.isEmpty() || colunaStr.isEmpty())
                throw new Exception("O labirinto deve ter linhas e colunas declaradas");

            try {
                this.linha = Integer.parseInt(linhaStr);
                this.coluna = Integer.parseInt(colunaStr);
            } catch (NumberFormatException e) {
                throw new Exception("Linhas e colunas devem ser números inteiros");
            }

            if (this.linha < 4)
                throw new Exception("O labirinto é pequeno demais (menos de 4 linhas).");
            if (this.coluna < 4)
                throw new Exception("O labirinto é pequeno demais (menos de 4 colunas).");

            this.labirinto = new char[this.linha][this.coluna];
            for (int i = 0; i < this.linha; i++) {
                String linhaTexto = br.readLine();
                if (linhaTexto == null || linhaTexto.length() != this.coluna)
                    throw new Exception("Linhas do labirinto incompatíveis com dimensões declaradas.");
                for (int j = 0; j < this.coluna; j++) {
                    this.labirinto[i][j] = linhaTexto.charAt(j);
                }
            }

            this.caminho = new Pilha<>(this.linha * this.coluna);
            this.possibilidades = new Pilha<>(this.linha * this.coluna);

            verificarIntegridadeBasica();
            verificarEstruturaDeLabirinto();
        } catch (IOException e) {
            throw new Exception("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private void verificarIntegridadeBasica() throws Exception {
        int entrada = 0, saida = 0;
        int eLinha = -1, eColuna = -1, sLinha = -1, sColuna = -1;

        for (int i = 0; i < this.linha; i++) {
            for (int j = 0; j < this.coluna; j++) {
                char c = this.labirinto[i][j];
                if (c == 'E') {
                    entrada++;
                    eLinha = i; eColuna = j;
                }
                if (c == 'S') {
                    saida++;
                    sLinha = i; sColuna = j;
                }
            }
        }

        if (entrada != 1 || saida != 1) {
            throw new Exception("Labirinto deve conter exatamente uma entrada 'E' e uma saída 'S'.");
        }

        boolean eNaBorda = (eLinha == 0 || eLinha == this.linha - 1
                         || eColuna == 0 || eColuna == this.coluna - 1);
        if (!eNaBorda) {
            throw new Exception("Entrada 'E' deve estar em alguma borda do labirinto.");
        }

        boolean sNaBorda = (sLinha == 0 || sLinha == this.linha - 1
                         || sColuna == 0 || sColuna == this.coluna - 1);
        if (!sNaBorda) {
            throw new Exception("Saída 'S' deve estar em alguma borda do labirinto.");
        }

        if (!podeAcessar(eLinha, eColuna)) {
            throw new Exception("Entrada 'E' sem caminho livre ao redor.");
        }

        if (!podeAcessar(sLinha, sColuna)) {
            throw new Exception("Saída 'S' sem caminho livre ao redor.");
        }

        for (int j = 0; j < this.coluna; j++) {
            char sup = this.labirinto[0][j];
            char inf = this.labirinto[this.linha - 1][j];
            if ((sup != '#' && sup != 'E' && sup != 'S')
             || (inf != '#' && inf != 'E' && inf != 'S')) {
                throw new Exception("Borda superior/inferior deve ser composta por '#' (ou E/S).");
            }
        }
        for (int i = 0; i < this.linha; i++) {
            char esq = this.labirinto[i][0];
            char dir = this.labirinto[i][this.coluna - 1];
            if ((esq != '#' && esq != 'E' && esq != 'S')
             || (dir != '#' && dir != 'E' && dir != 'S')) {
                throw new Exception("Borda esquerda/direita deve ser composta por '#' (ou E/S).");
            }
        }
    }

    private boolean podeAcessar(int i, int j) {
        int[][] direcoes = {{0,1},{1,0},{0,-1},{-1,0}};
        for (int[] d : direcoes) {
            int ni = i + d[0], nj = j + d[1];
            if (ni >= 0 && ni < this.linha && nj >= 0 && nj < this.coluna) {
                char c = this.labirinto[ni][nj];
                if (c == ' ' || c == 'E' || c == 'S') {
                    return true;
                }
            }
        }
        return false;
    }

    private void verificarEstruturaDeLabirinto() throws Exception 
    {
        int paredesInternas = 0, bifurcacoes = 0;
        for (int i = 1; i < this.linha - 1; i++) 
        {
            for (int j = 1; j < this.coluna - 1; j++) 
            {
                if (this.labirinto[i][j] == '#') paredesInternas++;
                if (this.labirinto[i][j] == ' ') 
                {
                    int caminhos = 0;
                    if (this.labirinto[i-1][j] == ' ') caminhos++;
                    if (this.labirinto[i+1][j] == ' ') caminhos++;
                    if (this.labirinto[i][j-1] == ' ') caminhos++;
                    if (this.labirinto[i][j+1] == ' ') caminhos++;
                    if (caminhos >= 2) bifurcacoes++;
                }
            }
        }
        if (paredesInternas < 1)
            throw new Exception("Labirinto sem paredes internas suficientes.");
        int minimoBif = (this.linha >= 4 && this.coluna >= 4) ? 2 : 1;
        if (bifurcacoes < minimoBif)
            throw new Exception("Poucas bifurcações internas; não é um labirinto real.");
    }

    public boolean temEntradaESaida() 
    {
        try {
            verificarIntegridadeBasica();
            return true;
        } catch (Exception e) {
            return false;
        }
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

    private void limparCaminhosErrados() 
    {
        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
            {
                if (this.labirinto[i][j] == 'x') 
                    this.labirinto[i][j] = ' ';
            }
        }
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
            limparCaminhosErrados();
            escreverLabirinto(arquivoSaida);
            imprimirCaminho();
            imprimirLabirinto();
        } 
        else 
        {
            System.out.println("\nLabirinto sem acesso a saída!\n");
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
        sb.append("Labirinto (").append(this.linha).append("x").append(this.coluna).append("):\n");

        for (int i = 0; i < this.linha; i++) 
        {
            for (int j = 0; j < this.coluna; j++) 
                sb.append(this.labirinto[i][j]);
            sb.append('\n');
        }

        return sb.toString();
    }

    @Override
    public int hashCode() 
    {
        int ret = 17;

        ret = 31 * ret + linha;
        ret = 31 * ret + coluna;

        if (caminho != null)
            ret = 31 * ret + caminho.hashCode();

        if (possibilidades != null)
            ret = 31 * ret + possibilidades.hashCode();

        if (fila != null)
            ret = 31 * ret + fila.hashCode();

        if (atual != null)
            ret = 31 * ret + atual.hashCode();

        ret = 31 * ret + (encontrouSaida ? 1 : 0);

        for (int i = 0; i < linha; i++) 
            for (int j = 0; j < coluna; j++) 
                ret = 31 * ret + labirinto[i][j]; // char já tem valor numérico

        return ret;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Labirinto lab = (Labirinto) obj;

        if (linha != lab.linha || coluna != lab.coluna || encontrouSaida != lab.encontrouSaida)
            return false;

        if ((caminho != null && !caminho.equals(lab.caminho)) || (caminho == null && lab.caminho != null))
            return false;

        if ((possibilidades != null && !possibilidades.equals(lab.possibilidades)) || (possibilidades == null && lab.possibilidades != null))
            return false;

        if ((fila != null && !fila.equals(lab.fila)) || (fila == null && lab.fila != null))
            return false;

        if ((atual != null && !atual.equals(lab.atual)) || (atual == null && lab.atual != null))
            return false;

        for (int i = 0; i < linha; i++) 
            for (int j = 0; j < coluna; j++) 
                if (labirinto[i][j] != lab.labirinto[i][j])
                    return false;

        return true;
    }

    public Labirinto(Labirinto modelo) throws Exception 
    {
        if (modelo == null)
            throw new Exception("Labirinto não instanciado!");

        this.linha = modelo.linha;
        this.coluna = modelo.coluna;

        this.labirinto = new char[linha][coluna];
        for (int i = 0; i < linha; i++) 
            for (int j = 0; j < coluna; j++)
                this.labirinto[i][j] = modelo.labirinto[i][j];

        this.caminho = new Pilha<>(modelo.caminho);
        this.possibilidades = new Pilha<>(modelo.possibilidades);
        this.fila = new Fila<>(modelo.fila);
        this.atual = (modelo.atual != null) ? new Coordenada(modelo.atual) : null;
        this.encontrouSaida = modelo.encontrouSaida;
    }

    @Override
    public Object clone()
    {
        try 
        {
            return new Labirinto(this);
        } 
        catch (Exception e) 
        {
            return null;
        }
    }


}
