package program.coordenada;
public class Coordenada implements Cloneable
{
    private int linha;
    private int coluna;

    public Coordenada(int linha, int coluna) throws Exception
    {
        if (linha<0 || coluna<0)
            throw new Exception("Erro! Digite números positivos");

        this.linha=linha;
        this.coluna=coluna;
    }

    public void setLinha(int linha) 
    {
        this.linha = linha;
    }

    public void setColuna(int coluna) 
    {
        this.coluna = coluna;
    }

    public int getColuna() 
    {
        return coluna;
    }

    public int getLinha() 
    {
        return linha;
    }

    @Override
    public String toString()
    {
        return "{ Linha: " + this.linha +
                " Coluna: " + this.coluna +
                " }";
    }

    @Override 
    public int hashCode()
    {
        int ret=1;

        ret *= 7 + ((Integer)(this.linha  )).hashCode();
        ret *= 7 + ((Integer)(this.coluna )).hashCode();

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

        Coordenada cord = (Coordenada) obj;
        
        if (this.linha!=cord.linha) return false;
        if (this.coluna!=cord.coluna) return false;
        
        return true;
    }

    public Coordenada(Coordenada modelo) throws Exception
    {
        if (modelo == null)
            throw new Exception("Modelo ausente");

        this.linha = modelo.linha;
        this.coluna = modelo.coluna;
    }

    @Override
    public Object clone()
    {
        Coordenada ret = null;

        try
        {
            ret = new Coordenada(this);
        }
        catch (Exception erro)
        {
            // Não vai acontecer, porque estamos clonando nós mesmos
        }

        return ret;
    }
}