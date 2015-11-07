/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;


/**
 * This is a class that I should one day implement. The current FrameController is pretty annoying to use.
 * @author decamp
 */
public interface FrameControllerDream {
    
    public static final int BUF_WINDOW      = 1 << 0;
    public static final int BUF_COLOR       = 1 << 1;
    public static final int BUF_DEPTH       = 1 << 2;
    public static final int BUF_STENCIL     = 1 << 3;
    
    public static final int BUF_HALF_SIZE   = 1 << 8;
    public static final int BUF_DOUBLE_SIZE = 1 << 9;
    
    
    public int bufferCount( int buf );
    public Texture buffer( int buf, int index );
    public Texture drawBuffer( int buf, int age );
    public Texture readBuffer( int buf, int age );
    public int currentDrawBuffers();
    public int currentReadBuffers();
    
    public void drawTo( GL gl, int drawTargets );
    public void swapTo( GL gl, int drawTargets );
    public void readFrom( GL gl, int readTargets );
    
    public Texture bindDraw( GL gl, int target, int age );
    public Texture bindRead( GL gl, int target, int age );
    
    public boolean blit( GL gl );
    public boolean blend( GL gl, boolean multiplyAlpha );
 
}
