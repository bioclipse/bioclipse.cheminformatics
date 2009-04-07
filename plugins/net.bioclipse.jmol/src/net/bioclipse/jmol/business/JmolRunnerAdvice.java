package net.bioclipse.jmol.business;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


public class JmolRunnerAdvice implements MethodInterceptor {

    public Object invoke( MethodInvocation invocation ) throws Throwable {

        if ( "run".equals( invocation.getMethod().getName() ) ) {
            return invocation.getThis().getClass().getMethod( 
                "run", 
                String.class, 
                boolean.class ).invoke( 
                    invocation.getThis(), 
                    invocation.getArguments()[0], 
                    true );
        }
        else {
            return invocation.proceed();
        }
    }
}
