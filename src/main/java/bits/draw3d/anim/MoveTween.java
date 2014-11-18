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
public class MoveTween extends AbstractTween {

    private       Vec3  mFromRef;
    private final Vec3  mToRef;
    private final Actor mTarget;


    public MoveTween( Vec3 fromRef,
                      Vec3 toRef,
                      Actor target )
    {
        mFromRef = fromRef;
        mToRef = toRef;
        mTarget = target;
    }

    @Override
    public void init() {
        if(mFromRef == null) {
            mFromRef = new Vec3( mTarget.mPos );
        }
    }
    
    @Override
    public void update( float t ) {
        Vec.lerp( mFromRef, mToRef, (float)t, mTarget.mPos );
    }

}
