package program.labirinto;

import program.coordenada.Coordenada;
import program.pilha.Pilha;
import program.fila.Fila;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Labirinto {
    private char[][] labirinto;
    private int linha, coluna;
    private Pilha<Coordenada> caminho;
    private Pilha<Fila<Coordenada>> possibilidades;
    private Coordenada atual;
    private Fila<Coordenada> fila;
    private boolean encontrouSaida;

    public Labirinto(String arq) throws Exception {
        caminho = new Pilha<>(100);
        possibilidades = new Pilha<>(100);
        leitura(arq);
    }

    public Labirinto() throws Exception {
        caminho = new Pilha<>(100);
        possibilidades = new Pilha<>(100);
        fila = new Fila<>(3);
    }

    public void leitura(String arq) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("testes/" + arq))) {
            linha = Integer.parseInt(br.readLine());
            coluna = Integer.parseInt(br.readLine());

            if (linha < 4) {
                throw new Exception("O labirinto é pequeno demais com menos de 4 linhas.");
            }

            if (coluna < 4) {
                throw new Exception("O labirinto é pequeno demais com menos de 4 colunas.");
            }

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
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private void verificarIntegridadeBasica() throws Exception {
        int entrada = 0, saida = 0;

        for (int i = 0; i < linha; i++)
            for (int j = 0; j < coluna; j++) {
                if (labirinto[i][j] == 'E') entrada++;
                if (labirinto[i][j] == 'S') saida++;
            }

        if (entrada != 1 || saida != 1)
            throw new Exception("Labirinto deve conter exatamente uma entrada 'E' e uma saída 'S'.");

        // Verifica se E ou S estão em cantos sem acesso válido ao interior
        Coordenada[] extremidades = {
            new Coordenada(0, 0), new Coordenada(0, coluna - 1),
            new Coordenada(linha - 1, 0), new Coordenada(linha - 1, coluna - 1)
        };

        for (Coordenada c : extremidades) {
            char atual = labirinto[c.getLinha()][c.getColuna()];
            if (atual == 'E' || atual == 'S')
                throw new Exception("Entrada ou saída está em uma posição sem acesso ao interior.");
        }

        // Verificar acesso lateral (bloqueio total por paredes)
        for (int i = 0; i < linha; i++) {
            for (int j = 0; j < coluna; j++) {
                if (labirinto[i][j] == 'E' || labirinto[i][j] == 'S') {
                    boolean bloqueado = true;
                    int[][] direcoes = {{0,1}, {1,0}, {0,-1}, {-1,0}};
                    for (int[] d : direcoes) {
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

        // Verifica paredes externas (borda)
        for (int j = 0; j < coluna; j++) {
            if (labirinto[0][j] != '#' && labirinto[0][j] != 'E' && labirinto[0][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (labirinto[linha - 1][j] != '#' && labirinto[linha - 1][j] != 'E' && labirinto[linha - 1][j] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }

        for (int i = 0; i < linha; i++) {
            if (labirinto[i][0] != '#' && labirinto[i][0] != 'E' && labirinto[i][0] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");

            if (labirinto[i][coluna - 1] != '#' && labirinto[i][coluna - 1] != 'E' && labirinto[i][coluna - 1] != 'S')
                throw new Exception("O labirinto deve conter paredes externas (#).");
        }
    }

    private void verificarEstruturaDeLabirinto() throws Exception {
        int paredesInternas = 0;
        int bifurcacoes = 0;

        for (int i = 1; i < linha - 1; i++) {
            for (int j = 1; j < coluna - 1; j++) {
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

        // Ajustando o critério de bifurcações para labirintos menores
        int minimoBifurcacoes = (linha >= 4 && coluna >= 4) ? 2 : 1;  // Menos bifurcações para labirintos menores
        if (bifurcacoes < minimoBifurcacoes)            throw new Exception("Labirinto com poucas bifurcações internas, não caracteriza um labirinto real.");
    }

    public void imprimirLabirinto() {
        for (int i = 0; i < linha; i++) {
            for (int j = 0; j < coluna; j++)
                System.out.print(labirinto[i][j]);
            System.out.println();
        }
    }

    public boolean encontrarEntrada() throws Exception {
        for (int j = 0; j < coluna; j++) {
            if (labirinto[0][j] == 'E') {
                atual = new Coordenada(0, j);
                return true;
            }
            if (labirinto[linha - 1][j] == 'E') {
                atual = new Coordenada(linha - 1, j);
                return true;
            }
        }

        for (int i = 0; i < linha; i++) {
            if (labirinto[i][0] == 'E') {
                atual = new Coordenada(i, 0);
                return true;
            }
            if (labirinto[i][coluna - 1] == 'E') {
                atual = new Coordenada(i, coluna - 1);
                return true;
            }
        }
        return false;
    }

    private void marcarCaminhoFinal() throws Exception 
    {
        Pilha<Coordenada> caminhoAuxiliar = new Pilha<>(100);
        boolean encontrouS = false;
    
        while (!caminho.isVazia()) {
            Coordenada c = caminho.recupereUmItem();
            caminhoAuxiliar.guardeUmItem(c);
            caminho.removaUmItem();
            
            if (labirinto[c.getLinha()][c.getColuna()] == 'S') {
                encontrouS = true;
            }
        }
    
        while (!caminhoAuxiliar.isVazia()) {
            Coordenada c = caminhoAuxiliar.recupereUmItem();
            caminho.guardeUmItem(c);
            
            if (labirinto[c.getLinha()][c.getColuna()] == 'S') {
                encontrouS = false;
            } else if (encontrouS && labirinto[c.getLinha()][c.getColuna()] == ' ') {
                labirinto[c.getLinha()][c.getColuna()] = '*';
            }
            
            caminhoAuxiliar.removaUmItem();
        }
    }

    public void resolverLabirinto(String arquivoSaida) throws Exception 
    {
        if (!encontrarEntrada()) {
            System.out.println("Erro: Não há entrada no labirinto.");
            return;
        }
    
        caminho.guardeUmItem((Coordenada) atual.clone());
        encontrouSaida = false;
    
        while (!encontrouSaida) {
            if (labirinto[atual.getLinha()][atual.getColuna()] == 'S') {
                encontrouSaida = true;
                if (!caminho.recupereUmItem().equals(atual)) {
                    caminho.guardeUmItem((Coordenada) atual.clone());
                }
                break;
            }
    
            if (labirinto[atual.getLinha()][atual.getColuna()] == ' ') {
                labirinto[atual.getLinha()][atual.getColuna()] = '*';
            }
    
            fila = new Fila<>(3);  
            verificarDirecao(atual.getLinha(), atual.getColuna() - 1); // Esquerda
            verificarDirecao(atual.getLinha(), atual.getColuna() + 1); // Direita
            verificarDirecao(atual.getLinha() - 1, atual.getColuna()); // Cima
            verificarDirecao(atual.getLinha() + 1, atual.getColuna()); // Baixo
    
            if (!fila.isVazia()) {
                possibilidades.guardeUmItem(fila);
                Coordenada proxima = fila.recupereUmItem();
                fila.removaUmItem();
    
                if (labirinto[proxima.getLinha()][proxima.getColuna()] == 'S') {
                    encontrouSaida = true;
                    caminho.guardeUmItem((Coordenada) proxima.clone());
                    break;
                }
    
                atual = proxima;
                caminho.guardeUmItem((Coordenada) atual.clone());
            } else {
                boolean achouAlternativa = false;
                while (!possibilidades.isVazia() && !achouAlternativa) {
                    fila = possibilidades.recupereUmItem();
                    possibilidades.removaUmItem();
    
                    if (!fila.isVazia()) {
                        Coordenada proxima = fila.recupereUmItem();
                        fila.removaUmItem();
    
                        if (labirinto[proxima.getLinha()][proxima.getColuna()] == 'S') {
                            encontrouSaida = true;
                            caminho.guardeUmItem((Coordenada) proxima.clone());
                            break;
                        }
    
                        atual = proxima;
                        caminho.guardeUmItem((Coordenada) atual.clone());
                        possibilidades.guardeUmItem(fila);
                        achouAlternativa = true;
                    } else {
                        if (!caminho.isVazia()) {
                            caminho.removaUmItem();
                            if (!caminho.isVazia()) {
                                atual = caminho.recupereUmItem();
                            }
                        }
                    }
                }
    
                if (!achouAlternativa && !encontrouSaida) {
                    System.out.println("Labirinto sem saída!");
                    escreverLabirinto(arquivoSaida);
                    return;
                }
            }
        }
    
        System.out.println("Labirinto resolvido!");
        marcarCaminhoFinal();
        escreverLabirinto(arquivoSaida);
        imprimirCaminho();
    }

    private void verificarDirecao(int linha, int coluna) throws Exception 
    {
        if (linha >= 0 && linha < this.linha && coluna >= 0 && coluna < this.coluna) 
        {
            char celula = labirinto[linha][coluna];
            
            if (!encontrouSaida && (celula == ' ' || celula == 'S')) 
            {
                fila.guardeUmItem(new Coordenada(linha, coluna));
                if (celula == 'S') 
                    encontrouSaida = true;
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
        } catch (IOException e) 
        {
            System.out.println("Erro ao escrever o arquivo: " + e.getMessage());
        }
    }

    private void imprimirCaminho() throws Exception 
    {
        Pilha<Coordenada> inverso = new Pilha<>(100);
        Pilha<Coordenada> backup = new Pilha<>(100);

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
