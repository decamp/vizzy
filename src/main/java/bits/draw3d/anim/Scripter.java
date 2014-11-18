/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import java.lang.reflect.*;
import java.util.*;

import bits.microtime.Clock;
import bits.util.reflect.Methods;


public class Scripter implements Clock {
    
    private final ScriptExecutor mExec;
    private final Clock mClock;
    private final List<ScriptAction> mActions;
    
    private long mTime = 0L;
    
    private boolean mHasPrevAction = false;
    private long mPrevActionEnd = 0L;
    
    
    public Scripter( ScriptExecutor exec ) {
        this( exec, false );
    }
    
    
    private Scripter( ScriptExecutor exec, boolean enableCancel ) {
        mExec    = exec;
        mClock   = exec.clock();
        mTime    = mClock.micros();
        mActions = enableCancel ? new ArrayList<ScriptAction>() : null;
    }
    
    
    
    public Clock clock() {
        return mExec.clock();
    }                      
    
    
    public ScriptExecutor exec() {
        return mExec;
    }
    
    
    /**
     * @return current time position of this Scripter in seconds. 
     *         Current time determines when any commands that are scheduled will be executed.
     */
    public double time() {
        return mTime / 1000000.0;
    }
    
    /**
     * @return current time of underlying clock.
     */
    public double now() {
        return mClock.micros() / 1000000.0;
    }

    /**
     * @return the time delay between this Scripter and its clock: <code>now() - time()</code>.
     */
    public double delay() {
        return ( mTime - mClock.micros() ) / 1000000.0;
    }
    
    /**
     * @param secs  This Tween will be set to this time position,
     *              which determines when added commands will be executed.
     * @return this
     */
    public Scripter seek( double secs ) {
        doSetTime( toMicros( secs ) );
        return this;
    }
    
    /**
     * Adds seconds to current time. Equivalent to calling: <br/><code>
     * this.secs( this.secs() + secs )
     * </code>
     * 
     * @param secs
     * @return this
     */
    public Scripter skip( double secs ) {
        doSetTime( mTime + toMicros( secs ) );
        return this;
    }
    
    /**
     * Sets time of this scripter to current time.
     * @return
     */
    public Scripter reset() {
        doSetTime( mExec.clock().micros() );
        return this;
    }
    
    /**
     * Sets the time of this Scripter to the end of the last action 
     * scheduled. Calling this method more than once or after time
     * has already been updated via seek() or skip() will have no effect.
     *  
     * @return this
     */
    public Scripter sync() {
        if( mHasPrevAction ) {
            doSetTime( mPrevActionEnd );
        }
        return this;
    }
    
    
    public Scripter exec( Runnable run ) {
        doAddAction( new ScriptRunnable( micros(), run ) );
        return this;
    }

    
    public Scripter exec( Tween tween, Ease trans ) {
        if( trans == null ) {
            ScriptAction action = new TweenAction( mTime, mTime, tween, null );
            doAddAction( action );
            
        } else {
            ScriptAction action = new TweenAction( mTime + trans.delay(),
                                                   mTime + trans.end(),
                                                   tween,
                                                   trans.func() );
            doAddAction( action );
        }
        
        return this;
    }
    
    
    public Scripter exec( Object target, String method, Object... args ) {
        if( args == null ) {
            args = new Object[]{ null };
        }
        
        Method meth = Methods.matchArgs( target, method, args );
        if( meth == null ) { 
            throw new RuntimeException( "Could not find matching method: " + method );
        }
        
        ScriptAction action = new InvokeMethodAction( micros(), meth, target, args );
        doAddAction( action );
        return this;
    }
    
    /**
     * Adds Tween to executor directly. The time position of this Scripter has
     * no effect on this call.
     * 
     * @param tween
     * @param trans
     * @return
     */
    public Scripter execNow( Tween tween, Ease trans ) {
        long t0 = mClock.micros();
        
        if( trans == null ) {
            doAddAction( new TweenAction( t0, t0, tween, null ) );
        } else {
            doAddAction( new TweenAction( t0 + trans.delay(), t0 + trans.end(), tween, trans.func() ) );
        }
        
        return this;
    }
    
    /**
     * Add ScriptAction to executor directly. The time position of this Scripter has
     * no effect on this call.
     * 
     * @param action
     * @return
     */
    public Scripter execNow( ScriptAction action ) {
        doAddAction( action );
        return this;
    }
    
    /**
     * @return true iff actions invoked on this Scripter may be cancelled.
     */
    public boolean canCancel() {
        return mActions != null;
    }
    
    /**
     * @return list of scheduled actions, or an empty list if this Scripter does not keep track.
     */
    public List<ScriptAction> actions() {
        if( mActions == null || mActions.size() == 0 ) {
            return Collections.<ScriptAction>emptyList();
        }
        
        return new ArrayList<ScriptAction>( mActions );
    }

    
    public void cancel() {
        if( mActions == null ) {
            return;
        }
        
        for( ScriptAction a: mActions ) {
            a.cancel();
        }
        
        mActions.clear();
    }
    
    

    
    /**
     * @return current time position of this Scripter in microseconds. 
     *         Current time determines when any commands that are scheduled will be executed.
     */
    public long micros() {
        return mTime;
    }
    
    /**
     * @return current time of underlying clock, in microseconds.
     */
    public long nowMicros() {
        return mClock.micros();
    }

    /**
     * @return the time delay between this Scripter and its clock: <code>nowMicros() - micros()</code>.
     */
    public long delayMicros() {
        return mTime - mClock.micros();
    }
    
    /**
     * @param secs  This Tween will be set to this time position,
     *              which determines when added commands will be executed.
     * @return this
     */
    public Scripter seekMicros( long micros ) {
        doSetTime( micros );
        return this;
    }
    
    /**
     * Adds seconds to current time. Equivalent to calling: <br/><code>
     * this.secs( this.secs() + secs )
     * </code>
     * 
     * @param secs
     * @return this
     */
    public Scripter skipMicros( long micros ) {
        doSetTime( mTime + micros );
        return this;
    }
        
    
    
    
    public static long toMicros( double secs ) {
        return (long)( secs * 1000000.0 + 0.5 );
    }
    

    public static double toSecs( long micros ) {
        return micros / 1000000.0;
    }
    
        
    
    private void doSetTime( long time ) {
        mTime = time;
        mHasPrevAction = false;
    }
    
    
    private void doAddAction( ScriptAction action ) {
        if( mActions != null ) {
            mActions.add( action );
        }
        
        long t = action.stopMicros();
        if( t < Long.MAX_VALUE ) {
            mHasPrevAction = true;
            mPrevActionEnd = t;
        }
        mExec.addAction( action );
    }
    
        
}
