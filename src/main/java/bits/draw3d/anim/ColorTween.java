/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.math3d.Vec;
import bits.math3d.Vec4;


/**
 * @author decamp
 */
public class ColorTween extends AbstractTween {

    private       Vec4        mFromRef;
    private final Vec4        mToRef;
    private final ColorObject mTarget;
    private final Vec4 mWork = new Vec4();


    public ColorTween( Vec4 fromRef,
                       Vec4 toRef,
                       ColorObject target )
    {
        mFromRef = fromRef;
        mToRef = toRef;
        mTarget  = target;
    }

    @Override
    public void init() {
        if( mFromRef == null ) {
            mFromRef = new Vec4( mTarget.color() );
        }
    }

    @Override
    public void update( float t ) {
        Vec.lerp( mFromRef, mToRef, t, mWork );
        mTarget.color( mWork );
    }

    @Override
    public void finish() {
        mTarget.color( mToRef );
    }

}
