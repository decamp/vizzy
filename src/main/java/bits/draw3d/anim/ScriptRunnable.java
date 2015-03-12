/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.DrawEnv;

/**
 * @author decamp
 */
public class ScriptRunnable implements ScriptAction {
    
    private final long mMicros;
    private final Runnable mRun;
    private boolean mComplete = false;
    
    public ScriptRunnable( long micros ) {
        this( micros, null );
    }


    public ScriptRunnable( long micros, Runnable r ) {
        mMicros = micros;
        mRun    = r;
    }
    

    @Override
    public long startMicros() {
        return mMicros;
    }

    @Override
    public long stopMicros() {
        return mMicros;
    }

    @Override
    public boolean update( long micros ) {
        if( !mComplete ) {
            run();
            mComplete = true;
        }
        return true;
    }

    @Override
    public void cancel() {
        mComplete = true;
    }

    @Override
    public boolean isComplete() {
        return mComplete;
    }

    
    public void run() {
        if( mRun != null ) {
            mRun.run();
        }
    }
}
