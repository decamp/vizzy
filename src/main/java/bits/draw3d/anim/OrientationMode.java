/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

public enum OrientationMode {
    /** No translation, rotation, or scaling for this actor. **/
    NONE,
    /** Full translation, rotation and scaling. **/
    FULL,
    /** Actor always faces camera ( rotation determined by camera). Normal translation and scaling. **/
    CAMERA,
    /** Actor is in Normalized Device Coordinates and both matrix stacks should be set to identity. **/
    NORM,
    /**
     * Actor is in aspect-corrected normalized device coordinates
     * As if NDC bounds were actually [-aspect,-1] to [aspect,1]. */
    NORM_ASPECT,
    /** Actor has a modelview position but is otherwise in aspect-corrected normalize device coordinates. **/
    NORM_ASPECT_MODEL_POS,
    /** Actor is in pixel coordinates. **/
    PIXEL,
    /**
     * Not fully defined yet.
     */
    POLE,

    /** Scaling and translation, but no rotation. **/
    @Deprecated FLAT

}
