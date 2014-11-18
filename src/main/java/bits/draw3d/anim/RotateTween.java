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
public class RotateTween extends AbstractTween {

    private final Actor mTarget;

    private boolean mNeedInit    = true;
    private final Vec4 mStartRot = new Vec4();
    private final Vec4 mStopRot  = new Vec4();
    private final Vec4 mWork     = new Vec4();


    public RotateTween( Mat3 stopRot, Actor target ) {
        this( null, stopRot, target );
    }


    public RotateTween( Mat3 startRot, Mat3 stopRot, Actor target ) {
        mTarget = target;
        if( startRot != null ) {
            mNeedInit = false;
            Quat.matToQuat( startRot, mStartRot );
        }
        Quat.matToQuat( stopRot, mStopRot );
    }


    @Override
    public void init() {
        if( mNeedInit ) {
            Quat.matToQuat( mTarget.mRot, mStartRot );
        }
    }

    @Override
    public void update( float t ) {
        Quat.slerp( mStartRot, mStopRot, t, mWork );
        Quat.quatToMat( mWork, mTarget.mRot );
    }

}
