/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.math3d.Vec4;


/**
 * @author decamp
 */
public interface ColorObject {
    public Vec4 color();
    public void color( Vec4 color );
}
