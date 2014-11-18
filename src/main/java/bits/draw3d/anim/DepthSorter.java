/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import java.util.*;

import bits.draw3d.actors.DepthSortable;
import bits.draw3d.util.TimSort;
import bits.math3d.Mat4;
import bits.math3d.Vec3;
import bits.math3d.geom.*;


/**
 * @author decamp
 */
public class DepthSorter {


    public static DepthSorter createBackToFront( Volume optNormCullBounds ) {
        Comparator<DepthSortable> comp;
        if( optNormCullBounds == null ) {
            comp = DepthSortable.BACK_TO_FRONT_ORDER;
        } else {
            comp = new BackToFrontCullComp( optNormCullBounds );
        }
        return new DepthSorter( TimSort.defaultInstance(), comp, optNormCullBounds );
    }


    public static DepthSorter createFrontToBack( Volume optNormCullBounds ) {
        Comparator<DepthSortable> comp;
        if( optNormCullBounds == null ) {
            comp = DepthSortable.FRONT_TO_BACK_ORDER;
        } else {
            comp = new FrontToBackCullComp( optNormCullBounds );
        }
        return new DepthSorter( TimSort.defaultInstance(), comp, optNormCullBounds );
    }


    private final TimSort mSorter;
    private final Comparator<DepthSortable> mComp;

    private final Volume mCullBounds;

    private int mRenderStart = -1;
    private int mRenderStop  = -1;


    public DepthSorter( TimSort sorter, Comparator<DepthSortable> comp, Volume cullBounds ) {
        mSorter = sorter;
        mComp = comp;
        mCullBounds = cullBounds;
    }


    public Volume cullBounds() {
        return mCullBounds;
    }


    /**
     * Computes normalized positions of DepthSortable objects, then
     * sorts the objects based on their normalized position depth.
     * If this DepthSorter has a defined cull bounds, it will also partition
     * the array into culled and unculled objects.  The partion boundaries
     * can be retrieved by calling <tt>unculledStartIndex()</tt> and
     * <tt>unculledStopIndex</tt>.
     *
     * @param arr Array of DepthSortable objects to sort.
     * @param off Offset into array.
     * @param len Number of objects in array.
     */
    public void sort( Mat4 modelToNormMat, DepthSortable[] arr, int off, int len ) {
        for( int i = 0; i < len; i++ ) {
            arr[i + off].updateNormPos( modelToNormMat );
        }
        sort( arr, off, len );
    }

    /**
     * Sorts array of objects based on their normalized position depth.
     * If this DepthSorter has a defined cull bounds, it will also partition
     * the array into culled and unculled objects.  The partion boundaries
     * can be retrieved by calling <tt>unculledStartIndex()</tt> and
     * <tt>unculledStopIndex</tt>.
     *
     * @param arr Array of DepthSortable objects to sort.
     * @param off Offset into array.
     * @param len Number of objects in array.
     */
    public void sort( DepthSortable[] arr, int off, int len ) {
        mSorter.sort( arr, off, off + len, mComp );

        Volume cull = mCullBounds;
        if( cull == null ) {
            mRenderStart = off;
            mRenderStop = off + len;
            return;
        }

        //Binary search for last remark not culled.
        int i = off;
        int j = off + len;

        while( i < j ) {
            int k = (i + j) / 2;
            Vec3 pos = arr[k].normPosRef();
            if( cull.contains( pos.x, pos.y, pos.z ) ) {
                i = k + 1;
            } else {
                j = k;
            }
        }

        mRenderStart = off;
        mRenderStop = i;
    }

    /**
     * After sorting, calling this method will return
     * the index into the sorted array of the first
     * unculled DepthSortable object.  Before <tt>sort</tt>
     * is called, this method returns an undefined value.
     *
     * @return inclusive start position of unculled objects in sorted array
     */
    public int startIndex() {
        return mRenderStart;
    }

    /**
     * After sorting, calling this method will return
     * the index into the sorted array after the last
     * unculled DepthSortable object.  Before <tt>sort</tt>
     * is called, this method returns an undefined value.
     *
     * @return exclusive stop postion of unculled objects in sorted array.
     */
    public int stopIndex() {
        return mRenderStop;
    }


    private static class BackToFrontCullComp implements Comparator<DepthSortable> {

        private final Volume mBounds;

        public BackToFrontCullComp( Volume bounds ) {
            mBounds = bounds;
        }


        public int compare( DepthSortable a, DepthSortable b ) {
            Vec3 ap = a.normPosRef();
            if( !mBounds.contains( ap.x, ap.y, ap.z ) ) {
                return 1;
            }

            Vec3 bp = b.normPosRef();
            if( !mBounds.contains( bp.x, bp.y, bp.z ) ) {
                return -1;
            }

            return ap.z < bp.z ? 1 : -1;
        }

    }


    private static class FrontToBackCullComp implements Comparator<DepthSortable> {

        private final Volume mBounds;

        public FrontToBackCullComp( Volume bounds ) {
            mBounds = bounds;
        }


        public int compare( DepthSortable a, DepthSortable b ) {
            Vec3 ap = a.normPosRef();
            if( !mBounds.contains( ap.x, ap.y, ap.z ) ) {
                return 1;
            }

            Vec3 bp = b.normPosRef();
            if( !mBounds.contains( bp.x, bp.y, bp.z ) ) {
                return -1;
            }

            return ap.z > bp.z ? 1 : -1;
        }

    }



    @Deprecated public static DepthSorter newBackToFrontSorter( Volume normCullBounds ) {
        return createBackToFront( normCullBounds );
    }


    @Deprecated public static DepthSorter newFrontToBackSorter( Volume normCullBounds ) {
        return createFrontToBack( normCullBounds );
    }


    @Deprecated public int unculledStartIndex() {
        return mRenderStart;
    }


    @Deprecated public int unculledStopIndex() {
        return mRenderStop;
    }

}
