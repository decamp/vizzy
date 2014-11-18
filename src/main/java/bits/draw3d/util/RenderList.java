/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import java.util.*;
import bits.draw3d.DrawEnv;
import bits.draw3d.actors.DepthSortable;
import bits.math3d.*;
import bits.math3d.geom.*;


@SuppressWarnings( "unchecked" )
public class RenderList<T> {
    
    public static final boolean BACK_TO_FRONT = true;
    public static final boolean FRONT_TO_BACK = false;
    
    
    public static <T> RenderList<T> create( Class<T> clazz, int capacity ) {
        return create( clazz, capacity, BACK_TO_FRONT, null );
    }
    
    
    @SuppressWarnings( "rawtypes" )
    public static <T> RenderList<T> create( Class<T> clazz,
                                            int capacity,
                                            boolean sortOrder,
                                            Volume cullVolume )
    {
        Comparator comp  = sortOrder == BACK_TO_FRONT ?
                           DepthSortable.BACK_TO_FRONT_ORDER :
                           DepthSortable.FRONT_TO_BACK_ORDER;
        float cullValue = sortOrder == BACK_TO_FRONT ?
                          Float.NEGATIVE_INFINITY:
                          Float.POSITIVE_INFINITY;
        return new RenderList<T>( clazz, capacity, comp, cullVolume, cullValue );
    }
    
    
    public static <T> RenderList<T> create( Class<T> clazz,
                                            int capacity,
                                            Comparator<? super T> comp )
    {
        return new RenderList<T>( clazz, capacity, comp, null, Float.NaN );
    }
    
    
    public static <T> RenderList<T> create( Class<T> clazz,
                                            int capacity,
                                            Comparator<? super T> comp,
                                            Volume cullBounds,
                                            float cullValue )
    {
        return new RenderList<T>( clazz, capacity,comp, cullBounds, cullValue );
    }
    
    
    
    private static final TimSort SORTER = TimSort.defaultInstance();
    
    public T[] mArr;
    public int mSize;
    public int mRenderStart = -1;
    public int mRenderStop  = -1;
    
    public final Mat4 mModelToProjMat = new Mat4();
    public final Mat4 mModelToNormMat = new Mat4();
    private final Mat4 mWork = new Mat4();

    private final Class<T> mClazz;
    private final float mCulledValue;
    private final Volume mCullBounds;
    private final Comparator<? super T> mComp; 

    private final boolean mDepthSortable;
    
        
    private RenderList( Class<T> clazz,
                        int capacity,
                        Comparator<? super T> comp,
                        Volume cullBounds,
                        float cullValue )
    {
        mClazz         = clazz;
        mArr           = (T[])java.lang.reflect.Array.newInstance( clazz, capacity );
        mComp          = comp;
        mDepthSortable = comp != null && DepthSortable.class.isAssignableFrom( clazz );
        mCullBounds    = cullBounds;
        mCulledValue   = cullValue;
    }
    
    
    
    public void add( T t ) {
        if( mSize == mArr.length ) {
            realloc( mSize + 1 );
        }
        
        mArr[mSize++] = t;
    }
    
    
    public void add( T[] arr, int off, int len ) {
        if( mSize + len > mArr.length ) {
            realloc( mSize + len );
        }
        
        System.arraycopy( arr, off, mArr, mSize, len );
        mSize += len;
    }
    
    
    public void addAll( Collection<T> items ) {
        int len = items.size();
        if( mSize + len > mArr.length ) {
            realloc( mSize + len );
        }
        
        for( T item: items ) {
            mArr[mSize++] = item;
        }
    }
    
    
    public int size() {
        return mSize;
    }
    
    
    
    public boolean remove( T t ) {
        for( int i = 0; i < mSize; i++ ) {
            if( mArr[i] == t ) {
                mArr[i] = mArr[--mSize];
                return true;
            }
        }
        
        return false;
    }
    
    
    public void remove( int idx ) {
        if( idx >= mSize )
            throw new IndexOutOfBoundsException();
        
        mArr[idx] = mArr[--mSize];
    }
    
    
    public void clear() {
        mSize = 0;
    }
    

    public void update( DrawEnv d ) {
        updateOrientation( d );
        updateSort();
    }

    
    public void updateOrientation( DrawEnv d ) {
        Mat.put( d.mView.get(), mModelToProjMat );
        Mat.put( d.mProj.get(), mWork );
        Mat.mult( mWork, mModelToProjMat, mModelToNormMat );
    }
    
    
    public void updateSort() {
        mRenderStart = 0;
        mRenderStop  = mSize;

        if( !mDepthSortable ) {
            if( mComp == null ) {
                return;
            }
            SORTER.sort( mArr, 0, mSize, mComp );
            return;
        }

        DepthSortable[] darr = (DepthSortable[])mArr;
        updateSortPositions( darr, 0, mSize, mModelToNormMat, mCullBounds, mCulledValue );    
        SORTER.sort( mArr, 0, mSize, mComp );

        Volume cull = mCullBounds;
        if( cull == null ) {
            return;
        }
        
        //Binary search for last remark not culled.
        int i = 0;
        int j = mSize;
        
        while( i < j ) {
            int k = ( i + j ) / 2;
            Vec3 pos = darr[k].normPosRef();
            
            // Check if not culled.
            if( pos.z != mCulledValue ) {
                i = k + 1;
            } else {
                j = k;
            }
        }

        mRenderStop  = i;
    }
    
    
    /**
     * After sorting, calling this method will return
     * the index into the sorted array of the first
     * unculled DepthSortable object.  Before <tt>sort</tt>
     * is called, this method returns an undefined value.   
     * 
     * @return inclusive start position of unculled objects in sorted array
     */
    public int renderStartIndex() {
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
    public int renderStopIndex() {
        return mRenderStop;
    }

    /**
     * Computes normalized positions of DepthSortable objects. 
     * 
     * @param arr  Array of DepthSortable objects to sort.
     * @param off  Offset into array.
     * @param len  Number of objects in array.
     */
    public static void updateSortPositions( DepthSortable[] arr, int off, int len, Mat4 transformMat, Volume cullBounds, float cullValue ) {
        if( cullBounds == null ) {
            for( int i = 0; i < len; i++ ) {
                arr[i+off].updateNormPos( transformMat );
            }
        } else {
            // Check if each value is within cull bounds.
            for( int i = 0; i < len; i++ ) {
                DepthSortable d = arr[i+off];
                d.updateNormPos( transformMat );
                Vec3 pos = d.normPosRef();
                if( !cullBounds.contains( pos.x, pos.y, pos.z ) ) {
                    pos.z = cullValue;
                }
            }
        }
    }
    
    
    private void realloc( int min ) {
        int n = Pots.ceilPot( min );
        T[] arr = (T[])java.lang.reflect.Array.newInstance( mClazz, n );
        System.arraycopy( mArr, 0, arr, 0, mSize );
        mArr = arr;
    }
    
}

