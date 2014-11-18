/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import java.lang.reflect.*;

import bits.draw3d.anim.ScriptRunnable;


class InvokeMethodAction extends ScriptRunnable {

    private final Method mMethod;
    private final Object mTarget;
    private final Object[] mArgs;


    public InvokeMethodAction( long micros, 
                               Method method, 
                               Object target, 
                               Object[] args ) 
    {
        super( micros );
        mMethod = method;
        mTarget = target;
        mArgs   = args;
    }


    public void run() {
        try {
            mMethod.invoke( mTarget, mArgs );
        } catch( InvocationTargetException ex ) {
            ex.printStackTrace();
        } catch( IllegalAccessException ex ) {
            ex.printStackTrace();
        }
    }

}

