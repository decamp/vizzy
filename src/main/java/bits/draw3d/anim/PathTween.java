/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.actors.Actor;
import bits.math3d.func.Func1v3;


/**
 * @author decamp
 */
public class PathTween extends AbstractTween {

    private final Func1v3 mPath;
    private final Actor   mTarget;


    public PathTween( Func1v3 path, Actor target ) {
        mPath = path;
        mTarget = target;
    }


    public void update( float t ) {
        mPath.apply( t, mTarget.mPos );
    }

}
