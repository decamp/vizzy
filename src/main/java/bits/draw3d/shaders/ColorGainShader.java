/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import java.io.IOException;

import bits.draw3d.*;
import bits.draw3d.shader.Program;
import bits.draw3d.shader.Shader;
import bits.util.Resources;
import static javax.media.opengl.GL2ES2.*;


/**
 * @author decamp
 */
public class ColorGainShader implements DrawUnit {

    private static final String PACKAGE     = ColorGainShader.class.getPackage().getName().replace( ".", "/" ) + "/";
    private static final String SOURCE_VERT = PACKAGE + "BaseTransform.vs";
    private static final String SOURCE_FRAG = PACKAGE + "ColorGain.fs";

    private final Program mProgram;

    private int   mTex  = 0;
    private float mGain = 1.0f;

    private boolean mInitialized = false;
    private int     mTexParam    = 0;
    private int     mGainParam   = 0;


    public ColorGainShader() {
        mProgram = new Program();
        try {
            String vertText = Resources.readString( SOURCE_VERT );
            String fragText = Resources.readString( SOURCE_FRAG );
            mProgram.addShader( new Shader( GL_VERTEX_SHADER, vertText ) );
            mProgram.addShader( new Shader( GL_FRAGMENT_SHADER, fragText ) );
        }catch( IOException ex ) {
            Error e = new UnsatisfiedLinkError( "Shader failed." );
            e.initCause( ex );
            throw e;
        }
    }
    


    public void textureUnit( int unit ) {
        mTex = unit;
    }
    
    
    public void gain( float gain ) {
        mGain = gain;
    }


    public void init( DrawEnv gl ) {
        if( !mInitialized ) {
            doInit( gl );
        }
    }


    public void bind( DrawEnv gl ) {
        if( !mInitialized ) {
            doInit( gl );
        }
        mProgram.bind( gl );
        updateParameters( gl );
    }
    
    
    public void unbind( DrawEnv gl ) {
        mProgram.unbind( gl );
    }

    
    public void dispose( DrawEnv gld ) {
        if( !mInitialized ) {
            return;
        }
        
        mInitialized = false;
        mTexParam    = 0;
        mGainParam   = 0;
    }


    
    private void updateParameters( DrawEnv gl ) {
        gl.mGl.glUniform1i( mTexParam,  mTex );
        gl.mGl.glUniform1f( mGainParam, mGain );
    }
    
    
    private void doInit( DrawEnv d ) {
        d.checkErr();
        
        mInitialized = true;
        mProgram.init( d );
        mProgram.bind( d );
        
        mTexParam  = d.mGl.glGetUniformLocation( mProgram.id(), "TEX_UNIT" );
        mGainParam = d.mGl.glGetUniformLocation( mProgram.id(), "GAIN" );
        
        d.checkErr();
    }
    
}
