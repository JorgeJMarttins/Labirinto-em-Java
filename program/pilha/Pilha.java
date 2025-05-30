package program.pilha;

import program.clonador.Clonador;

public class Pilha <X> implements Cloneable
{
    private Object[]    elemento; 
    private final int   tamanhoInicial;
    private int         ultimo=-1; 
    private Clonador<X> clonador;

    public Pilha (int tamanho) throws Exception
    {
        if (tamanho<=0)
            throw new Exception ("Tamanho invalido");

        this.elemento       = new Object [tamanho]; 
        this.tamanhoInicial = tamanho;
        
        this.clonador = new Clonador<X> ();
    }
    
    public Pilha ()
    {
        this.elemento       = new Object [10]; 
        this.tamanhoInicial = 10;
        
        this.clonador = new Clonador<X> ();
    }
    
    private void redimensioneSe (float fator)
    {
        Object[] novo = new Object [(int)(this.elemento.length*fator)];
        
        for(int i=0; i<=this.ultimo; i++)
            novo[i] = this.elemento[i];

        this.elemento = novo;
    }

    public void guardeUmItem (X x) throws Exception 
    {
        if (x==null)
            throw new Exception ("Falta o que guardar");

        if (this.ultimo+1==this.elemento.length) 
            this.redimensioneSe (2.0F);

        this.ultimo++;

        if (x instanceof Cloneable)
            this.elemento[this.ultimo]=this.clonador.clone(x);
        else
            this.elemento[this.ultimo]=x;
    }

    public X recupereUmItem () throws Exception 
    {
        if (this.ultimo==-1) 
            throw new Exception ("Nada a recuperar");

        X ret=null;
        if (this.elemento[this.ultimo] instanceof Cloneable)
            ret = this.clonador.clone((X)this.elemento[this.ultimo]);
        else
            ret = (X)this.elemento[this.ultimo];

        return ret;
    }

    public void removaUmItem () throws Exception 
    {
        if (this.ultimo==-1) 
            throw new Exception ("Labirinto sem acesso a saida");

        this.elemento[this.ultimo] = null;
        this.ultimo--;

        if (this.elemento.length>this.tamanhoInicial &&
            this.ultimo+1<=(int)(this.elemento.length*0.25F))
            this.redimensioneSe (0.5F);
    }

    public boolean isCheia ()
    {
        if(this.ultimo+1==this.elemento.length)
            return true;

        return false;
    }

    public boolean isVazia ()
    {
        if(this.ultimo==-1)
            return true;

        return false;
    }

    @Override
    public String toString ()
    {
        String ret = (this.ultimo+1) + " elemento(s)";
        
        if (this.ultimo!=-1)
            ret += ", sendo o ultimo "+this.elemento[this.ultimo];
            
        return ret;
    }

    @Override
    public boolean equals (Object obj)
    {
        if(this==obj)
            return true;

        if(obj==null)
            return false;

        if(this.getClass()!=obj.getClass())
            return false;

        Pilha<X> pil = (Pilha<X>) obj;

        if(this.ultimo!=pil.ultimo)
            return false;
        
        for(int i=0 ; i<this.ultimo;i++)
            if(!this.elemento[i].equals(pil.elemento[i]))
                return false;

        return true;
    }

    @Override
    public int hashCode ()
    {
        int ret=1;

        ret = ret*7 + ((Integer)(this.ultimo        )).hashCode();

        for (int i=0; i<this.ultimo; i++)
            ret = ret*7 + this.elemento[i].hashCode();

        if (ret<0)
            ret=-ret;

        return ret;
    }

    public Pilha (Pilha<X> modelo) throws Exception
    {
        if(modelo == null)
            throw new Exception("Modelo ausente");

        this.tamanhoInicial = modelo.tamanhoInicial;
        this.ultimo         = modelo.ultimo;
        this.clonador       = modelo.clonador;

        
        this.elemento = new Object[modelo.elemento.length];

        for(int i=0 ; i<modelo.elemento.length ; i++)
            this.elemento[i] = modelo.elemento[i];
    }

    @Override
    public Object clone ()
    {
        Pilha<X> ret=null;

        try
        {
            ret = new Pilha<X>(this);
        }
        catch(Exception erro)
        {}

        return ret;
    }
}



