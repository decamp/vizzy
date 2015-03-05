/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy;

import bits.blob.Blob;
import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNodeAdapter;
import bits.draw3d.anim.ScriptExecutor;
import bits.microtime.PlayController;


/**
 * @author decamp
 */
public class ClockNode extends DrawNodeAdapter {


    public static ClockNode create( Blob config ) {
        Blob blob        = config.slice( "clock" );
        long start       = blob.getLong( "startMicros" );
        long step        = blob.getLong( "stepMicros" );
        boolean stepping = blob.tryGetBoolean( false, "stepping" );
        return create( stepping, start, step );
    }
    
    
    public static ClockNode create( boolean stepping,
                                    long startMicros,
                                    long stepMicros )
    {
        PlayController playCont;
        if( stepping ) {
            playCont = PlayController.createStepping( startMicros, stepMicros );
        } else {
            playCont = PlayController.createRealtime();
        }
        
        return new ClockNode( playCont );
    }
    
    
    private final PlayController mPlayCont;
    private final ScriptExecutor mTweenExec;
    
    
    public ClockNode( PlayController playCont ) {
        mPlayCont  = playCont;
        mTweenExec = ScriptExecutor.newInstance( playCont.clock() );
    }
            
    
    public PlayController playCont() {
        return mPlayCont;
    }
    
    
    public ScriptExecutor tweenExec() {
        return mTweenExec;
    }

    @Override
    public void pushDraw( DrawEnv d ) {
        mPlayCont.tick();
        mTweenExec.pushDraw( d );
    }

}
