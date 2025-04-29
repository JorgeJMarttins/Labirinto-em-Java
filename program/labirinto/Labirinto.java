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
            labirinto = new char[linha][coluna];
    
            int linhasLidas = 0;
            int entradaCount = 0;
            int saidaCount = 0;
            int paredesInternas = 0;
    
            for (int i = 0; i < linha; i++) {
                String linhaTexto = br.readLine();
    
                if (linhaTexto == null || linhaTexto.length() != coluna)
                    throw new Exception("Erro: Linha " + (i + 1) + " não possui " + coluna + " colunas.");
    
                for (int j = 0; j < coluna; j++) {
                    char c = linhaTexto.charAt(j);
                    labirinto[i][j] = c;
    
                    if (c == 'E') {
                        entradaCount++;
                        if ((i == 0 && j == 0) || (i == 0 && j == coluna - 1) ||
                            (i == linha - 1 && j == 0) || (i == linha - 1 && j == coluna - 1))
                            throw new Exception("Erro: Entrada 'E' não pode estar no canto (" + i + "," + j + ").");
                    }
    
                    if (c == 'S') {
                        saidaCount++;
                        if ((i == 0 && j == 0) || (i == 0 && j == coluna - 1) ||
                            (i == linha - 1 && j == 0) || (i == linha - 1 && j == coluna - 1))
                            throw new Exception("Erro: Saída 'S' não pode estar no canto (" + i + "," + j + ").");
                    }
    
                    // Verifica paredes internas
                    if (c == '#' && !(i == 0 || i == linha - 1 || j == 0 || j == coluna - 1)) {
                        paredesInternas++;
                    }
                }
    
                linhasLidas++;
            }
    
            if (linhasLidas != linha)
                throw new Exception(" Número de linhas lidas (" + linhasLidas + ") não corresponde ao esperado (" + linha + ").");
    
            if (entradaCount != 1)
                throw new Exception(" Deve haver exatamente uma entrada 'E'. Encontrado: " + entradaCount);
    
            if (saidaCount != 1)
                throw new Exception("Deve haver exatamente uma saída 'S'. Encontrado: " + saidaCount);
    
            if (br.readLine() != null)
                throw new Exception(" Arquivo possui mais linhas do que o esperado (" + linha + ").");
    
            if (paredesInternas == 0)
                throw new Exception(" Labirinto inválido! Não há paredes internas (não é um labirinto, é uma sala).");
    
        } catch (IOException e) {
            throw new Exception("Erro ao ler o arquivo: " + e.getMessage());
        }
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