/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.math3d.func.*;


public class Ease {

    public static Ease createLinear( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.LINEAR );
    }
    
    public static Ease createLinearSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.LINEAR );
    }

    public static Ease createSmooth( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTH );
    }
    
    public static Ease createSmoothSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTH );
    }

    public static Ease createSmoothIn( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTH_IN );
    }
    
    public static Ease createSmoothInSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTH_IN );
    }
    
    public static Ease createSmoothOut( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTH_OUT );
    }
    
    public static Ease createSmoothOutSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTH_OUT );
    }
    
    public static Ease creatSmoother( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTHER );
    }
    
    public static Ease createSmootherSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTHER );
    }

    public static Ease createSmootherIn( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTHER_IN );
    }
    
    public static Ease createSmootherInSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTHER_IN );
    }
    
    public static Ease createSmootherOut( long delayMicros, long durMicros ) {
        return new Ease( delayMicros, durMicros, EaseFuncs.SMOOTHER_OUT );
    }
    
    public static Ease createSmootherOutSecs( double delaySecs, double durSecs ) {
        return new Ease( delaySecs, durSecs, EaseFuncs.SMOOTHER_OUT );
    }

    
    private final long mDelayMicros;
    private final long mDurationMicros;
    private final Function11 mFunc;
    

    public Ease( long delayMicros, long durMicros ) {
        this( delayMicros, durMicros, null );
    }
    
    
    public Ease( double delay, double dur ) {
        this( (long)( delay * 1000000.0 ), (long)( dur * 1000000.0 ), null );
    }
    
    
    public Ease( long delayMicros, long durMicros, Function11 func ) {
        mDelayMicros    = delayMicros;
        mDurationMicros = durMicros;
        mFunc           = func == null ? EaseFuncs.LINEAR : func;
    }
    
    
    public Ease( double delaySecs, double durSecs, Function11 func ) {
        mDelayMicros    = (long)( delaySecs * 1000000.0 );
        mDurationMicros = (long)( durSecs * 1000000.0 );
        mFunc           = func;
    }
    

    
    public long delay() {
        return mDelayMicros;
    }
    
    public long duration() {
        return mDurationMicros;
    }
    
    public long end() {
        return mDelayMicros + mDurationMicros;
    }
    
    public double delaySecs() {
        return mDelayMicros / 1000000.0;
    }
    
    public double durationSecs() {
        return mDurationMicros / 1000000.0;
    }
    
    public double endSecs() {
        return ( mDelayMicros + mDurationMicros ) / 1000000.0;
    }

    public Function11 func() {
        return mFunc;
    }
    
}
