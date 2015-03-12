/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import java.util.*;

import bits.data.segment.TimeRangedSet;
import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNodeAdapter;
import bits.math3d.func.Function11;
import bits.microtime.*;

/**
 * Executes scripts for a single clock.
 * 
 * @author decamp
 */
public class ScriptExecutor implements Ticker {

    private static final int UPDATE_ADD   = 0;
    //private static final int UPDATE_REMOVE = 1;
    private static final int UPDATE_CLEAR = 2;


    private final Clock mClock;
    private final TimeRangedSet<ScriptAction> mActions = new TimeRangedSet<ScriptAction>();
    private final Queue<Update> mUpdates = new LinkedList<Update>();


    public ScriptExecutor( Clock clock ) {
        mClock = clock;
    }



    public Clock clock() {
        return mClock;
    }


    public synchronized void addActions( Collection<? extends ScriptAction> actions ) {
        for( ScriptAction a : actions ) {
            mUpdates.offer( new Update( UPDATE_ADD, a ) );
        }
    }


    public synchronized void addAction( ScriptAction action ) {
        mUpdates.offer( new Update( UPDATE_ADD, action ) );
    }

    
    public synchronized void addTweens( Collection<? extends Tween> tweens, Ease trans ) {
        long t = mClock.micros();
        Function11 func = trans.func();
        
        for( Tween tween: tweens ) {
            addAction( new TweenAction( t + trans.delay(),
                                        t + trans.end(),
                                        tween,
                                        func ) );
        }
    }
    
    
    public synchronized ScriptAction addTween( Tween tween, Ease trans ) {
        long t = mClock.micros();
        ScriptAction ret = new TweenAction( t + trans.delay(),
                                            t + trans.end(),
                                            tween,
                                            trans.func() );
        
        addAction( ret );
        return ret;
    }
    
    
    public synchronized int actionCount() {
        return mActions.size();
    }
    
    
    public synchronized void cancelAll() {
        mUpdates.offer( new Update( UPDATE_CLEAR, null ) );
    }
    


    public void tick() {
        while( true ) {
            Update up = null;
            synchronized( this ) {
                up = mUpdates.poll();
            }

            if( up == null ) {
                break;
            }
            
            switch( up.mCode ) {
            case UPDATE_ADD:
                mActions.add( up.mAction );
                break;
            case UPDATE_CLEAR:
                long t = mClock.micros();
                for( ScriptAction a: mActions ) {
                    a.cancel();
                    // Give script a chance to release GL assets.
                    a.update( t );
                }
                mActions.clear();
                break;
            }
        }
        
        if( mActions.isEmpty() ) {
            return;
        }
        
        long t = mClock.micros();
        Iterator<ScriptAction> iter = mActions.intersectionSet( Long.MIN_VALUE, t ).iterator();

        while( iter.hasNext() ) {
            if( iter.next().update( t ) ) {
                iter.remove();
            }    
        }
    }

    
    
    public Object listAdd( Object opaque, Tween tween, Ease trans, boolean cancelPrev ) {
        ScriptAction act = addTween( tween, trans );
        return listInsert( opaque, act, cancelPrev ); 
    }
    
    
    public Object listAdd( Object opaque, ScriptAction act, boolean cancelPrev ) {
        addAction( act );
        return listInsert( opaque, act, cancelPrev );
    }
    
    
    public static Object listCancel( Object opaque ) {
        ScriptAction[] arr = (ScriptAction[])opaque;
        if( arr == null ) {
            return null;
        }
        
        for( int i = 0; i < arr.length; i++ ) {
            ScriptAction a = arr[i];
            if( a == null ) {
                break;
            }
            a.cancel();
            arr[i] = null;
        }
        
        return arr.length > 10 ? null : arr;
    }

    
    
    private static Object listInsert( Object opaque, ScriptAction newAct, boolean cancelPrev ) {
        ScriptAction[] acts = (ScriptAction[])opaque;
        if( acts == null ) {
            acts = new ScriptAction[]{ newAct };
            return acts;
        }
        
        if( cancelPrev ) {
            for( int i = 0; i < acts.length; i++ ) {
                ScriptAction a = acts[i];
                if( a == null ) {
                    break;
                }
                a.cancel();
                acts[i] = null;
            }
            
            acts[0] = newAct;
            return acts;
        }
        
        // Remove completed actions and find empty index.
        int n = listCullComplete( acts );
        if( n < acts.length ) {
            acts[n] = newAct;
            return acts;
        }
            
        ScriptAction[] rr = new ScriptAction[ acts.length + 2 ];
        System.arraycopy( acts, 0, rr, 0, acts.length );
        rr[n] = newAct;
        return rr;
    }
    
    
    private static int listCullComplete( ScriptAction[] arr ) {
        int tail = -1;
        
        for( int i = 0; i < arr.length; i++ ) { 
            ScriptAction a = arr[i];
            if( a == null ) {
                return i;
            }
            if( !a.isComplete() ) {
                continue;
            }
            
            // Action is complete and should be removed.
            // Find last entry.
            if( tail < 0 ) {
                tail = i + 1;
                while( tail < arr.length && arr[tail] != null ) tail++;
            }
            
            // Check if item being removed is last entry.
            if( i == tail - 1 ) {
                arr[i] = null;
                return i;
            }
            
            // Set current position to tail. 
            // Post-decremend index to check action now at that position.
            arr[i--]  = arr[--tail];
            arr[tail] = null;
        }
        
        return arr.length;
    }
    
    
    private final class Update {
        
        final int mCode;
        final ScriptAction mAction;
        
        Update(int code, ScriptAction action) {
            mCode   = code;
            mAction = action;
        }

    }
    
}
