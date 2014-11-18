/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.DrawEnv;

import javax.media.opengl.GL;


/**
 * @author decamp
 */
public interface Tween {
    /** Called before first update. */
    public void init();

    /**
     * @param t Number between 0 and 1, where 0 indicates start of Tween and 1 indicates end.
     */
    public void update( float t );

    /** Called when completing Tween prior to disposal. */
    public void finish();

    /** Called when cancelling Tween, meaning it will not be completed before disposal. */
    public void cancel();
}
