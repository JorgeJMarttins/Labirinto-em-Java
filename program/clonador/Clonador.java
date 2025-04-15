package program.clonador;

import java.lang.reflect.*;

public class Clonador<X>
{
    public X clone (X x)
    {        
        Class<?> classe = x.getClass();
        
        Class<?>[] tpsParmsForms = null;
        
        Method metodo=null;
        try
        {
            metodo = classe.getMethod ("clone",tpsParmsForms);
        }
        catch (NoSuchMethodException erro)
        {}
        
        Object[] parmsReais = null;
                
        X ret=null;
        try
        {
            ret = (X)metodo.invoke(x,parmsReais);
        }
        catch (InvocationTargetException erro)
        {}
        catch (IllegalAccessException erro)
        {}

        return ret;
    }
}
