/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.DrawEnv;
import bits.draw3d.actors.Actor;
import bits.math3d.*;
import bits.math3d.func.*;


/**
 * @author decamp
 */
public class TweenActor extends Actor implements ColorObject {
    
    public static final int ACTION_MOVE   = 0;
    public static final int ACTION_SCALE  = 1;
    public static final int ACTION_COLOR  = 2;
    public static final int ACTION_ROTATE = 3;
    public static final int ACTION_MAX    = 4;
    
    
    public Vec4   mColor    = new Vec4( 1, 1, 1, 1 );
    public Object mUserData = null;
    
    private final ScriptExecutor mExec;
    private final Object[] mActions;
    
    
    public TweenActor( ScriptExecutor exec ) {
        this( exec, ACTION_MAX );
    }
    
    
    public TweenActor( ScriptExecutor exec, int actionCount ) {
        mExec    = exec;
        mActions = new ScriptAction[actionCount][];
    }
    
    
    
    public ScriptExecutor executor() {
        return mExec;
    }
    
    
    
    public Vec4 color() {
        return mColor;
    }


    public void color( Vec4 color ) {
        Vec.put( color, mColor );
    }


    public void position( Vec3 pos ) {
        Vec.put( pos, mPos );
    }
    
    
    public void rotation( Mat3 rot ) {
        Mat.put( rot, mRot );
    }
    
    
    public void scale( Vec3 scale ) {
        Vec.put( scale, mScale );
    }
    
    
    public synchronized void cancelTweens() {
        for( int i = 0; i < mActions.length; i++ ) {
            mActions[i] = mExec.listCancel( mActions[i] ); 
        }
    }
    
    
    public synchronized void tweenPosition( Vec3 destRef, Ease ease, boolean cancelPrev ) {
        if( ease == null || ease.end() <= 0L ) {
            if( cancelPrev ) {
                cancelActions( ACTION_MOVE );
            }
            position( destRef );
            return;
        }
        Tween tween = new MoveTween( null, destRef, this );
        addAction( ACTION_MOVE, tween, ease, cancelPrev );
    }
    
    
    public synchronized void tweenPosition( ScriptAction action, boolean cancelPrev ) {
        addAction( ACTION_MOVE, action, cancelPrev );
    }
    
    
    public synchronized void tweenPath( Func1v3 func, Ease ease, boolean cancelPrev ) {
        if( ease == null || ease.end() <= 0L ) {
            if( cancelPrev ) {
                cancelActions( ACTION_MOVE );
            }
            func.apply( 1, mPos );
            return;
        }
        Tween tween = new PathTween( func, this );
        addAction( ACTION_MOVE, tween, ease, cancelPrev );
    }
    
    
    public synchronized void tweenRotation( Mat3 destRef, Ease trans, boolean cancelPrev ) {
        if( trans == null || trans.end() <= 0L ) {
            if( cancelPrev ) {
                cancelActions( ACTION_ROTATE );
            }
            rotation( destRef );
            return;
        }
        Tween tween = new RotateTween( null, destRef, this );
        addAction( ACTION_ROTATE, tween, trans, cancelPrev );
    }

    
    public synchronized void tweenRotation( ScriptAction action, boolean cancelPrev ) {
        addAction( ACTION_ROTATE, action, cancelPrev );
    }
    
    
    public synchronized void tweenScale( Vec3 destRef, Ease trans, boolean cancelPrev ) {
        if( trans == null || trans.end() <= 0L ) {
            if( cancelPrev ) {
                cancelActions( ACTION_SCALE );
            }
            scale( destRef );
            return;
        }
        Tween tween = new ScaleTween( null, destRef, this );
        addAction( ACTION_SCALE, tween, trans, cancelPrev );
    }
        
    
    public synchronized void tweenScale( ScriptAction action, boolean cancelPrev ) {
        addAction( ACTION_SCALE, action, cancelPrev );
    }
    
    
    public synchronized void tweenColor( Vec4 destRef, Ease trans, boolean cancelPrev ) {
        if( trans == null || trans.end() <= 0L ) {
            if( cancelPrev ) {
                cancelActions( ACTION_COLOR );
            }
            color( destRef );
            return;
        }
        Tween tween = new ColorTween( null, destRef, this );
        addAction( ACTION_COLOR, tween, trans, cancelPrev );
    }


    public synchronized void tweenColor( ScriptAction action, boolean cancelPrev ) {
        addAction( ACTION_COLOR, action, cancelPrev );
    }
    
    
    public void render( DrawEnv d ) {}

    
    public void pushTransformer( DrawEnv d ) {
        computeTransform( mWorkMat );
        d.mView.push();
        d.mView.mult( mWorkMat );
    }
    
    
    public void popTransformer( DrawEnv d ) {
        d.mView.pop();
    }
    

    
    protected void cancelActions( int action ) {
        mActions[ action ] = mExec.listCancel( mActions[ action ] );
    }
    
    
    protected void addAction( int action, Tween tween, Ease trans, boolean cancelPrev ) {
        mActions[ action ] = mExec.listAdd( mActions[ action ], tween, trans, cancelPrev );
    }
    
    
    protected void addAction( int action, ScriptAction newAct, boolean cancelPrev ) {
        mActions[ action ] = mExec.listAdd( mActions[ action ], newAct, cancelPrev );
    }
        
}
