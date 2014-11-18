/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import javax.media.opengl.GL;

import bits.draw3d.DrawEnv;
import bits.math3d.func.Function11;



/**
 * @author decamp
 */
public class TweenAction implements ScriptAction {
    
    private final long mStartMicros;
    private final long mStopMicros;
    private final Tween mTween;
    private final Function11 mTransFunc;
    
    private boolean mNeedInit = true;
    private boolean mComplete = false;
    private boolean mCancel   = false;
    
    
    public TweenAction( long t0, 
                        long t1, 
                        Tween tween,
                        Function11 transFunc) 
    {        
        mStartMicros = t0;
        mStopMicros  = t1;
        mTween       = tween;
        mTransFunc   = transFunc;
    }

    
    public long startMicros() {
        return mStartMicros;
    }
    
    public long stopMicros() {
        return mStopMicros;
    }
    
    public boolean update( DrawEnv d, long t ) {
        if( mComplete ) {
            if( mCancel ) {
                mTween.cancel();
                mCancel = false;
            }
            
            return true;
        }
        
        if( t < mStartMicros )
            return false;
        
        if( mNeedInit ) {
            mNeedInit = false;
            mTween.init();
        }
        
        float p;
        if( mStopMicros <= mStartMicros || t >= mStopMicros ) {
            p = 1f;
        } else {
            p = (float)( t - mStartMicros ) / ( mStopMicros - mStartMicros );
        }
        
        if( mTransFunc != null ) {
            p = (float)mTransFunc.apply( p );
        }
        
        mTween.update( p );
        if( t < mStopMicros ) {
            return false;
        }
        mComplete = true;
        mTween.finish();
        return true;
        
    }
    
    public void cancel() {
        if( mComplete ) {
            return;
        }
        mCancel   = true;
        mComplete = true;
    }
    
    public boolean isComplete() {
        return mComplete;
    }
    
}
