/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

import javax.media.opengl.GL;
import static javax.media.opengl.GL2ES3.*;

import bits.draw3d.DrawEnv;
import bits.png.*;
import bits.util.OutputFileNamer;


/**
 * @author Philip DeCamp
 */
public class ScreenshotSaver {

    public static final int COLOR_TYPE_GRAYSCALE       = PngBufferWriter.COLOR_TYPE_GRAYSCALE;
    public static final int COLOR_TYPE_RGB             = PngBufferWriter.COLOR_TYPE_RGB;
    public static final int COLOR_TYPE_GRAYSCALE_ALPHA = PngBufferWriter.COLOR_TYPE_GRAYSCALE_ALPHA;
    public static final int COLOR_TYPE_RGBA            = PngBufferWriter.COLOR_TYPE_RGBA;
    
    public static final int LEVEL_NO_COMPRESSION   = NativeZLib.Z_NO_COMPRESSION;
    public static final int LEVEL_BEST_SPEED       = NativeZLib.Z_BEST_SPEED;
    public static final int LEVEL_BEST_COMPRESSION = NativeZLib.Z_BEST_COMPRESSION;
    public static final int LEVEL_DEFAULT          = NativeZLib.Z_DEFAULT_COMPRESSION;
    
    public static final int ALPHA_COPY              = 0;
    public static final int ALPHA_SATURATE          = 1;
    public static final int ALPHA_MULTIPLY          = 2;
    public static final int ALPHA_MULTIPLY_SATURATE = 3;
    public static final int ALPHA_DIVIDE            = 4;
    
    
    private File             mOutDir        = null;
    private OutputFileNamer  mPngSequence   = null;
    private OutputFileNamer  mDepthSequence = null;
    private PngBufferWriter  mPngEncoder    = null;
    private DepthFileEncoder mDepthEncoder  = null;
    private ByteBuffer       mReadBuffer    = null;
    private ByteBuffer       mWriteBuffer   = null;
    

    
    public synchronized void openOutputDir( File outDir, boolean generateSubDir ) {
        if( !outDir.exists() ) {
            outDir.mkdirs();
        }
        
        if( generateSubDir ) {
            outDir = new OutputFileNamer( outDir, "", "", 3 ).next();
            outDir.mkdirs();
        }
        
        mOutDir        = outDir;
        mPngSequence   = new OutputFileNamer( outDir, "", ".png", 5 );
        mDepthSequence = new OutputFileNamer( outDir, "", ".png", 5 );
    }
    
    
    public synchronized void closeOutputDir() {
        mOutDir        = null;
        mPngSequence   = null;
        mDepthSequence = null;
    }

    
    public synchronized File outputDir() {
        return mOutDir;
    }
    
    
    public synchronized File nextPngFile() {
        return mPngSequence == null ? null : mPngSequence.next();
    }
    
    
    public synchronized File nextDepthFile() {
        return mDepthSequence == null ? null : mDepthSequence.next();
    }
    
   
    public ByteBuffer readRgba( DrawEnv d, int x, int y, int w, int h, int alphaOp, ByteBuffer out ) {
        final int len = w * h * 4;
        
        if( out == null || out.remaining() < len ) {
            out = ByteBuffer.allocateDirect( len );
        } 
        
        d.mGl.glReadPixels( x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, out );
        applyOp( alphaOp, out, len );
        out.position( out.position() + w * h * 4 );
        
        return out;
    }
    
    
    public ByteBuffer readDepth( GL gl, int x, int y, int w, int h, ByteBuffer out ) {
        final int len = w * h * 4;
        
        if( out == null || out.remaining() < len ) {
            out = ByteBuffer.allocateDirect( len );
        }
        
        gl.glReadPixels( x, y, w, h, GL_DEPTH_COMPONENT, GL_FLOAT, out );
        out.position( out.position() + len );
        
        return out;
    }
    
    
    public synchronized File savePng( DrawEnv d, int x, int y, int w, int h, int alphaOp ) {
        if( mPngSequence == null )
            return null;
        
        File ret = mPngSequence.next();
        savePng( d, x, y, w, h, alphaOp, ret );
        return ret;
    }
    
    
    public synchronized void savePng( DrawEnv d, int x, int y, int w, int h, int alphaOp, File outFile ) {
        try {
            if( mReadBuffer != null ) {
                mReadBuffer.clear();
            }
            mReadBuffer = readRgba( d, x, y, w, h, alphaOp, mReadBuffer );
            mReadBuffer.flip();
            
            if( mWriteBuffer != null ) {
                mWriteBuffer.clear();
            }
            mWriteBuffer = encodePng( mReadBuffer, w, h, COLOR_TYPE_RGBA, mWriteBuffer );
            mWriteBuffer.flip();
            
            saveBuffer( mWriteBuffer, outFile );
            
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }
    
    
    public synchronized File saveDepth( GL gl, int x, int y, int w, int h ) {
        if( mDepthSequence == null )
            return null;
        
        File ret = mDepthSequence.next();
        saveDepth( gl, x, y, w, h, ret );
        return ret;
    }
    
    
    public synchronized void saveDepth( GL gl, int x, int y, int w, int h, File outFile ) {
        try {
            if( mReadBuffer != null ) {
                mReadBuffer.clear();
            }
            mReadBuffer = readDepth( gl, x, y, w, h, mReadBuffer );
            mReadBuffer.flip();
            
            if( mWriteBuffer != null ) {
                mWriteBuffer.clear();
            }
            mWriteBuffer = encodeDepth( mReadBuffer, w, h, mWriteBuffer );
            mWriteBuffer.flip();
            
            saveBuffer( mWriteBuffer, outFile );
            
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }


    @SuppressWarnings( "resource" )
    public synchronized void saveBuffer( ByteBuffer buf, File out ) throws IOException {
        FileChannel chan = null;
        
        try {
            chan = new FileOutputStream( out ).getChannel();
            
            while( buf.remaining() > 0 ) {
                int n = chan.write( buf );
                if( n <= 0 ) {
                    throw new IOException( "Write failed." );
                }
            }
        } finally {
            if( chan != null ) {
                chan.close();
            }
        }
    }
    
    
    public synchronized ByteBuffer encodePng( ByteBuffer in, 
                                              int w, 
                                              int h, 
                                              int colorType, 
                                              ByteBuffer out ) 
                                              throws IOException
    {
        return encodePng( in, w, h, colorType, PngBufferWriter.LEVEL_DEFAULT, out );
    }
    
    
    public synchronized ByteBuffer encodePng( ByteBuffer in, 
                                              int w, 
                                              int h, 
                                              int colorType, 
                                              int compLevel, 
                                              ByteBuffer out )
                                              throws IOException 
    {
        if( mPngEncoder == null ) {
            mPngEncoder = new PngBufferWriter();
        }
        
        int cap = w * h * 4 + 1024;
        
        if( out == null || out.remaining() < cap ) {
            out = ByteBuffer.allocateDirect( cap );
        }
        
        mPngEncoder.open( out, w, h, colorType, 8, compLevel, null );
        
        int pos    = in.position();
        int stride = w * 4;
        in.limit( pos + stride * h );
        
        for( int i = 0; i < h; i++ ) {
            int p0 = pos + stride * ( h - i - 1);
            in.position( p0 ).limit( p0 + stride );
            mPngEncoder.writeData( in );
        }
            
        in.limit( pos + stride * h ).position( pos + stride * h );
        mPngEncoder.close();
        
        return out;
    }
    
    
    public synchronized ByteBuffer encodeDepth( ByteBuffer in,
                                                int w,
                                                int h,
                                                ByteBuffer out )
                                                throws IOException
    {
        return encodeDepth( in, w, h, LEVEL_DEFAULT, out );
    }
    
    
    public synchronized ByteBuffer encodeDepth( ByteBuffer in,
                                                int w,
                                                int h,
                                                int compressionLevel,
                                                ByteBuffer out )
                                                throws IOException
    {
        if( mDepthEncoder == null ) {
            mDepthEncoder = new DepthFileEncoder();
        }
        
        int cap = w * h * 4 + 1024;
        
        if( out == null || out.remaining() < cap ) {
            out = ByteBuffer.allocateDirect( cap );
        }
        
        mDepthEncoder.open( out, w, h, compressionLevel );
        mDepthEncoder.writeData( in );
        mDepthEncoder.close();
        
        return out;
    }
    

    private void applyOp( int op, ByteBuffer buf, int len ) {

        switch( op ) {
        case ALPHA_SATURATE: {
            final int pos = buf.position();
            
            for( int i = 3; i < len; i += 4 ) {
                buf.put( pos + i, (byte)0xFF );
            }
            break;
        }

        case ALPHA_MULTIPLY: {
            final int pos = buf.position();
            
            for( int i = 0; i < len; i += 4 ) {
                int r = buf.get( pos + i     ) & 0xFF;
                int g = buf.get( pos + i + 1 ) & 0xFF;
                int b = buf.get( pos + i + 2 ) & 0xFF;
                int a = buf.get( pos + i + 3 ) & 0xFF;
                
                buf.put( pos + i    , (byte)( r * a / 255 ) );
                buf.put( pos + i + 1, (byte)( g * a / 255 ) );
                buf.put( pos + i + 2, (byte)( b * a / 255 ) );
                buf.put( pos + i + 3, (byte)a );
            }
            break;
        }

        case ALPHA_MULTIPLY_SATURATE: {
            final int pos = buf.position();

            for( int i = 0; i < len; i += 4 ) {
                int r = buf.get( pos + i     ) & 0xFF;
                int g = buf.get( pos + i + 1 ) & 0xFF;
                int b = buf.get( pos + i + 2 ) & 0xFF;
                int a = buf.get( pos + i + 3 ) & 0xFF;

                buf.put( pos + i    , (byte)( r * a / 255 ) );
                buf.put( pos + i + 1, (byte)( g * a / 255 ) );
                buf.put( pos + i + 2, (byte)( b * a / 255 ) );
                buf.put( pos + i + 3, (byte)0xFF );
            }
            break;
        }

        case ALPHA_DIVIDE: {
            final int pos = buf.position();

            for( int i = 0; i < len; i += 4 ) {
                int r = buf.get( pos + i     ) & 0xFF;
                int g = buf.get( pos + i + 1 ) & 0xFF;
                int b = buf.get( pos + i + 2 ) & 0xFF;
                int a = buf.get( pos + i + 3 ) & 0xFF;

                if( a == 0 ) {
                    buf.put( pos + i    , (byte)0 );
                    buf.put( pos + i + 1, (byte)0 );
                    buf.put( pos + i + 2, (byte)0 );
                    buf.put( pos + i + 3, (byte)0 );
                } else {
                    buf.put( pos + i    , (byte)Math.min( 255, r * 255 / a ) );
                    buf.put( pos + i + 1, (byte)Math.min( 255, g * 255 / a ) );
                    buf.put( pos + i + 2, (byte)Math.min( 255, b * 255 / a ) );
                    buf.put( pos + i + 3, (byte)a );
                }
            }

            break;
        }

        default:
        }
    }

    
}
