/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import java.nio.*;
import static javax.media.opengl.GL2ES3.*;

import bits.draw3d.tex.Texture1;
import bits.draw3d.tex.Texture2;
import bits.math3d.func.*;


public final class KernelTexs {
    
    
    public static Texture1 createDashedLine( int w, float segments, float dutyCycle ) {
        ByteBuffer buf = ByteBuffer.allocateDirect( w );
        buf.order( ByteOrder.nativeOrder() );
        
        final float blankStart = dutyCycle * 0.5f;
        final float blankEnd   = 1f - dutyCycle * 0.5f;
        
        for( int i = 0; i < w; i++ ) {
            float seg = i / ( w - 1.0f ) * segments;
            float t   = seg % 1.0f;
            if( t <= blankStart || t >= blankEnd ) {
                buf.put( (byte)255 );
            } else {
                buf.put( (byte)0 );
            }
        }
        
        buf.flip();
        Texture1 ret = new Texture1();
        ret.buffer( buf, GL_ALPHA, GL_ALPHA, GL_UNSIGNED_BYTE, w );
        ret.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        return ret;
    }
    
    
    public static Texture1 createGauss1( int w ) {
        return createGauss1( w, 2.0 );
    }
    
    
    public static Texture1 createGauss1( int w, double sigs ) {
        ByteBuffer buf = ByteBuffer.allocateDirect( w + 2 );
        buf.order( ByteOrder.BIG_ENDIAN );
        int hw = w / 2;
        
        Gaussian1 g  = Gaussian1.fromSigma( hw / sigs );
        buf.put( (byte)0 );
        double sum   = 0.0;
        
        for( int x = 0; x < w; x++) {
            sum += g.apply( x - hw );
        }
        
        
        for( int x = 0; x < w; x++ ) {
            double v = g.apply( x - hw ) / sum;
            //buf.putInt( (int)( v * 0x7FFFFFFF ) );
            buf.put( (byte)( v * 0xFF ) );
        }
        
        buf.put( (byte)0 );
        buf.flip();

        Texture1 node = new Texture1();
        node.buffer( buf, GL_ALPHA, GL_ALPHA, GL_UNSIGNED_BYTE, w + 2 );
        node.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        return node;
    }

    
    public static Texture2 createGauss2( int w, int h ) {
        ByteBuffer buf = ByteBuffer.allocateDirect( w * h * 4 );
        buf.order( ByteOrder.BIG_ENDIAN );
        int hw = w / 2;
        int hh = h / 2;
        
        Gaussian2 g  = Gaussian2.fromSigma( hw * 0.35, hh * 0.35, 0.0 );
        double scale = 1.0 / g.apply( 0.0, 0.0 );
        
        for( int y = 0; y < h; y++ ) {
            for( int x = 0; x < w; x++ ) {
                double v = g.apply( x - hw, y - hh ) * scale;
                buf.putInt( (int)( v * 0x7FFFFFFF ) );
            }
        }
        
        buf.flip();

        Texture2 node = new Texture2();
        node.buffer( buf, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE, w, h );
        node.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        node.param( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        return node;
    }

    
    public static Texture1 createWashbin( int w, double min, double max ) {
        ByteBuffer buf = ByteBuffer.allocateDirect( w * 4 );
        buf.order( ByteOrder.BIG_ENDIAN );
        
        for( int x = 0; x < w; x++ ) {
            double s = x / ( w - 1.0 );
            
            if( s < min || s > max ) {
                buf.putInt( 0x00FF0000 );
            }else{
                buf.putInt( 0xFFFF0000 );
            }
        }
        
        buf.flip();

        Texture1 node = new Texture1();
        node.buffer( buf, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE, w );
        node.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        return node;
    }


    private KernelTexs() {}

}
