/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizor;

import java.io.*;
import java.util.*;

import bits.draw3d.*;
import bits.draw3d.camera.CameraNode;
import bits.draw3d.util.*;
import bits.microtime.*;


/**
 * @author decamp
 */
public class ScreenshotController extends DrawNodeAdapter {

    public static final int ALPHA_COPY     = ScreenshotSaver.ALPHA_COPY;
    public static final int ALPHA_SATURATE = ScreenshotSaver.ALPHA_SATURATE;
    public static final int ALPHA_MULTIPLY = ScreenshotSaver.ALPHA_MULTIPLY;

    private final File mOutDir;

    private final PlayController mPlayCont;
    private final CameraNode     mCamera;

    private ScreenshotSaver mSaver = null;

    private boolean mTakeScreenshot = false;

    private boolean mTimeStopped = false;
    private boolean mTiledShot   = false;
    private int     mTileCols    = 0;
    private int     mTileRows    = 0;
    private int     mTilePos     = -1;
    private int     mAlphaOp     = 0;

    private File       mTileDir = null;
    private List<File> mTiles   = null;


    public ScreenshotController( File outDir, PlayController playCont, CameraNode camera ) {
        mOutDir = outDir;
        mPlayCont = playCont;
        mCamera = camera;
    }


    public void takeScreenshot() {
        takeScreenshot( 1, 1, ALPHA_COPY );
    }


    public void takeScreenshot( int cols, int rows ) {
        takeScreenshot( cols, rows, ALPHA_COPY );
    }
    
    
    public void takeScreenshot( int cols, int rows, int alphaOp ) {
        if( mTakeScreenshot == true )
            return;
        
        mTakeScreenshot = true;
        mAlphaOp        = alphaOp;
        mTileCols       = cols;
        mTileRows       = rows;
        mTiledShot      = cols >= 1 && rows >= 1 && ( cols > 1 || rows > 1 );
        mTilePos        = -1;
        
        if( mTiledShot ) {
            if( mPlayCont.clock().isPlaying() ) {
                mPlayCont.control().playStop();
                mTimeStopped = true;
            }
        }
        
        System.out.println( "Taking screenshot." );
    }
    
    
    
    @Override
    public void popDraw( DrawEnv d ) {
        if( !mTakeScreenshot ) 
            return;
        
        if( mSaver == null ) {
            mSaver = new ScreenshotSaver();
            mSaver.openOutputDir( mOutDir, true );
        }
        
        if( !mTiledShot ) {
            Rect box = mCamera.viewportRef();
            File file = mSaver.savePng( d, box.x0, box.y0, box.width(), box.height(), mAlphaOp );
            
            mTakeScreenshot = false;
            System.out.println( "Saved: " + file.getPath() );
            return;
        }
        
        
        if( mTilePos < 0 ) {
            try {
                mTileDir = File.createTempFile( "screenshot", null );
            } catch( IOException ex ) {
                ex.printStackTrace();
                mTakeScreenshot = false;
                return;
            }
                
            mTileDir.delete();
            mTileDir.mkdir();
            mTileDir.deleteOnExit();
            mTiles = new ArrayList<File>( mTileCols * mTileRows );
            
        } else {
            Rect box = mCamera.viewportRef();
            File file = new File( mTileDir, "screenshot_ " + mTilePos + ".png" );
            file.deleteOnExit();
            
            mSaver.savePng( d, box.x0, box.y0, box.width(), box.height(), mAlphaOp, file );
            mTiles.add( file );
        }
        
        mTilePos++;
        
        if( mTilePos >= mTileRows * mTileCols ) {
            // Screenshot has completed.
            mTakeScreenshot = false;
            mCamera.overrideTileViewport( null );
            
            // Resume clock.
            if( mTimeStopped ) {
                mPlayCont.control().playStart();
            }
            
            // Combine the tiles.
            try {
                File file = mSaver.nextPngFile();
                ImageCombiner.combine( mTiles, mTileCols, mTileRows, file );
                System.out.println( "Saved: " + file.getPath() );
            } catch( IOException ex ) {
                ex.printStackTrace();
            }
            
            for( File f: mTiles ) {
                f.delete();
            }
            
            mTileDir.delete();
            mTiles = null;
            mTileDir = null;
            return;
        }
        
        Rect viewport = mCamera.viewportRef();
        Rect tile     = sliceTile( viewport, mTileCols, mTileRows, mTilePos );
        mCamera.overrideTileViewport( tile );
    }

   
    static Rect sliceTile( Rect viewport, int cols, int rows, int pos ) {
        int s = pos % cols;
        int t = rows - 1 - pos / rows;
        
        int x0 = viewport.x0 + viewport.width()  * s / cols;
        int y0 = viewport.y0 + viewport.height() * t / rows;
        int x1 = viewport.x0 + viewport.width()  * ( s + 1 ) / cols;
        int y1 = viewport.y0 + viewport.height() * ( t + 1 ) / rows;
        
        return new Rect( x0, y0, x1, y1 );
    }
    
    
}
