/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.actors.Actor;
import bits.math3d.*;


/**
 * @author decamp
 */
public class PovLerpTween implements Tween {

    private final Actor mTarget;

    private Vec3 mStartPos;
    private final Vec4 mStartQuat = new Vec4();
    private final Vec3 mStopPos;
    private final Vec4 mStopQuat  = new Vec4();
    private final Vec4 mWork      = new Vec4();


    public PovLerpTween( Trans3 stop, Actor target ) {
        this( null, stop, target );
    }


    public PovLerpTween( Trans3 optStart, Trans3 stopRef, Actor target ) {
        mTarget = target;
        if( optStart != null ) {
            mStartPos = optStart.mPos;
            Quat.matToQuat( optStart.mRot, mStartQuat );
        }
        mStopPos = stopRef.mPos;
        Quat.matToQuat( stopRef.mRot, mStopQuat );
    }

    @Override
    public void init() {
        if( mStartPos == null ) {
            mStartPos = new Vec3( mTarget.mPos );
            Quat.matToQuat( mTarget.mRot, mStartQuat );
        }
    }
    
    
    public void update( float t ) {
        Vec.lerp( mStartPos, mStopPos, t, mTarget.mPos );
        Quat.slerp( mStartQuat, mStopQuat, t, mWork );
        Quat.quatToMat( mWork, mTarget.mRot );
    }
    
    
    public void finish() {}
    
    
    public void cancel() {}

}
