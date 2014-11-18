/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.actors.Actor;
import bits.math3d.Vec;
import bits.math3d.Vec3;


/**
 * @author decamp
 */
public class ScaleTween extends AbstractTween {

    private       Vec3  mStart;
    private final Vec3  mStop;
    private final Actor mTarget;


    public ScaleTween( Vec3 fromRef,
                       Vec3 toRef,
                       Actor target )
    {
        mStart = fromRef;
        mStop = toRef;
        mTarget = target;
    }


    @Override
    public void init() {
        if( mStart == null ) {
            mStart = new Vec3( mTarget.mScale );
        }
    }

    @Override
    public void update( float t ) {
        Vec.lerp( mStart, mStop, t, mTarget.mScale );
    }

}
