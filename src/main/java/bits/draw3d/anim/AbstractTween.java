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
public abstract class AbstractTween implements Tween {
    public void init() {}
    public void update( float t ) {}
    public void finish() {}
    public void cancel() {}
}
