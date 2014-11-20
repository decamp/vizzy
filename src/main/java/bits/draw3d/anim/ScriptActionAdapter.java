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
public class ScriptActionAdapter implements ScriptAction {

    private final long mStartMicros;
    private final long mStopMicros;
    
    
    public ScriptActionAdapter( long startMicros, long stopMicros ) {
        mStartMicros = startMicros;
        mStopMicros  = stopMicros;
    }
    
    
    public long startMicros() {
        return mStartMicros;
    }
    
    public long stopMicros() {
        return mStopMicros;
    }
    
    public boolean update( DrawEnv d, long t ) {
        return true;
    }
    
    public void cancel() {}
    
    public boolean isComplete() {
        return false;
    }
}
