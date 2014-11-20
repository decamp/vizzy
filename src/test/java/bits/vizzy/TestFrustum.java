/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy;

import bits.math3d.*;


/**
 * @author Philip DeCamp
 */
public class TestFrustum {

    public static void main( String[] args ) throws Exception {
        test1();
    }


    static void test1() throws Exception {
        Mat4 frustum = new Mat4();
        float near = 1;
        float far  = 2;
        Mat.getFrustum( -1, 1, -1, 1, near, far, frustum );


        test( frustum, new Vec4( 0, 0,        -far, 1 ) );
        test( frustum, new Vec4( 0, 0,       -near, 1 ) );
        test( frustum, new Vec4( 0, 0, -0.01f*near, 1 ) );
        test( frustum, new Vec4( 0, 0,  0.01f*near, 1 ) );
        test( frustum, new Vec4( 0, 0,        near, 1 ) );
        test( frustum, new Vec4( 0, 0,         far, 1 ) );

        System.out.println();

        test( frustum, new Vec4( 1, 0,        -far, 1 ) );
        test( frustum, new Vec4( 1, 0,       -near, 1 ) );
        test( frustum, new Vec4( 1, 0, -0.01f*near, 1 ) );
        test( frustum, new Vec4( 1, 0,  0.01f*near, 1 ) );
        test( frustum, new Vec4( 1, 0,        near, 1 ) );
        test( frustum, new Vec4( 1, 0,         far, 1 ) );
    }



    static void test( Mat4 frustum, Vec4 vec ) {
        Vec4 a = new Vec4();
        Mat.mult( frustum, vec, a );
        Vec3 b = new Vec3( a.x / a.w, a.y / a.w, a.z / a.w );

        System.out.println( vec + " -> " + a + " (" + b + ")" );
    }

}
