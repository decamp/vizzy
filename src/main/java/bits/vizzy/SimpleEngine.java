/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy;

import java.awt.*;
import javax.swing.JFrame;
import javax.media.opengl.*;
import static javax.media.opengl.GL.*;

import bits.draw3d.*;
import bits.draw3d.actors.Actor;
import bits.draw3d.camera.CameraNode;
import bits.draw3d.scene.SceneGraph;
import bits.draw3d.text.FontManager;
import bits.glui.*;
import bits.math3d.*;
import bits.microtime.Clock;
import bits.vizzy.input.*;


/**
 * @author decamp
 */
public class SimpleEngine {

    
    public static SimpleEngine create( Clock clock ) {
        return new SimpleEngine( clock, null );
    }
    
    
    public static SimpleEngine create( Clock clock, int numSamples ) {
        GLCapabilities caps = newDefaultCaps();
        caps.setNumSamples( numSamples );
        caps.setSampleBuffers( numSamples > 1 );
        return new SimpleEngine( clock, caps );
    }
    
    
    public static SimpleEngine create( Clock clock, GLCapabilities caps ) {
        if( caps == null ) {
            caps = newDefaultCaps();
        }
        return new SimpleEngine( clock, caps );
    }


    private final GRootController mRootController;
    private final SceneGraphPanel mRenderPane;

    private final WalkingActor         mCamera;
    private final CameraNode           mCameraNode;
    private final SpaceMouseController mSpaceMouseCont;
    private final NavigationController mNavCont;

    private Object mPreModelGraph  = null;
    private Object mLightGraph     = null;
    private Object mModelGraph     = null;
    private Object mPostModelGraph = null;

    private final Box3    mModelBounds         = new Box3( -100, -100, -100, 100, 100, 100 );
    private       boolean mAutoSetControlSpeed = true;
    private       float   mAutoSetControlScale = 1;
    private       float   mMaxFps              = 80;


    private SimpleEngine( Clock clock, GLCapabilities caps ) {
        mRootController = GRootController.create( caps );
        mRootController.setClearColor( 1, 1, 1, 1 );

        mRenderPane = new SceneGraphPanel();
        mRootController.rootPane().addChild( mRenderPane );

        mCamera = new WalkingActor();
        mSpaceMouseCont = new SpaceMouseController( clock, mCamera );
        mNavCont = new NavigationController( clock, mCamera );
        mCameraNode = new CameraNode( mCamera );

        mCamera.rotate( (float)(Math.PI * 0.5), 0, 1, 0 );
        mCamera.rotate( (float)(-Math.PI * 0.5), 1, 0, 0 );

        mRenderPane.addKeyListener( mNavCont );
    }


    public void setClearColor( double r, double g, double b, double a ) {
        mRootController.setClearColor( (float)r,
                                       (float)g,
                                       (float)b,
                                       (float)a );
    }


    public Component awtComponent() {
        return mRootController.component();
    }


    public JFrame buildFrame( int w, int h ) {
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.add( awtComponent() );
        frame.getContentPane().setPreferredSize( new Dimension( w, h ) );
        frame.pack();
        frame.setLocationRelativeTo( null );
        
        return frame;
    }
    
    
    
    public GRootController rootController() {
        return mRootController;
    }
    
    
    public GLayeredPanel rootPane() {
        return mRootController.rootPane();
    }
    
    
    public GPanel renderPane() {
        return mRenderPane;
    }
    
    
    public FontManager fontManager() {
        return mRootController.fontManager();
    }


    public ShaderManager shaderManeger() {
        return mRootController.shaderManager();
    }

    
    public Actor camera() {
        return mCamera;
    }
    
    
    public CameraNode cameraNode() {
        return mCameraNode;
    }
    
    
    public SpaceMouseController spaceMouseCont() {
        return mSpaceMouseCont;
    }
    
    
    public NavigationController navigationCont() {
        return mNavCont;
    }
        
    
    public void setPreModelGraph( Object graph ) {
        mPreModelGraph = graph;
    }
    
    
    public void setLightGraph( Object graph ) {
        mLightGraph = graph;
    }
    
    
    public void setModelGraph( Object graph ) {
        mModelGraph = graph;
    }
    
    
    public void setPostModelGraph( Object graph ) {
        mPostModelGraph = graph;
    }
    
    
    public void setModelBounds( Box3 bounds ) {
        Box.put( bounds, mModelBounds );
    }
    
    
    public void setMaxFps( float fps ) {
        mMaxFps = fps;
    }
    
    
    public void autoSetControlSpeed( boolean autoSet, float scale ) {
        mAutoSetControlSpeed = autoSet;
        mAutoSetControlScale = scale;
    }
    
    
    public void start() {
        start(mMaxFps);
    }
    
    
    public void start( double maxFps ) {
        //Setup bounds.
        Box3 bounds = mModelBounds;
        float[] arr = { Box.centX( bounds ), Box.centY( bounds ), Box.centZ(  bounds ) };
        arr[2] += Box.spanZ( bounds );
        Vec.put( arr, mCamera.mPos );
        
        // Adjust projection depth to size of model bounds.
        float maxDim = Math.max( Math.max( bounds.span( 0 ), bounds.span( 1 ) ), bounds.span( 2 ) );

        mCameraNode.nearPlane( maxDim * 2.2f / 500.0f );
        mCameraNode.farPlane( maxDim * 2.2f );

        if( mAutoSetControlSpeed ) {
            mSpaceMouseCont.moveSpeed( maxDim * mAutoSetControlScale / 5f );
            mNavCont.moveSpeed( maxDim * mAutoSetControlScale / 10f );
        }     
        
        SceneGraph graph = new SceneGraph();
        graph.add( new InitNode() );

        if( mPreModelGraph != null ) {
            graph.connectLast( mPreModelGraph );
        }

        graph.connectLast( mCameraNode );
        graph.connectLast( mSpaceMouseCont );
        graph.connectLast( mNavCont );

        if( mLightGraph != null ) {
            graph.connectLast( mLightGraph );
        }
        if( mModelGraph != null ) {
            graph.connectLast( mModelGraph );
        }
        if( mPostModelGraph != null ) {
            graph.connectLast( mPostModelGraph );
        }

        mRenderPane.setGraph( graph );
        mRootController.startAnimator( maxFps );
    }


    public void stop( boolean exit ) {
        mRenderPane.dispose( exit );
    }
    
    
    
    private static final class InitNode extends DrawNodeAdapter {
        @Override
        public void pushDraw( DrawEnv d ) {
            d.mCullFace.set( true );
            d.mDepthTest.set( true, GL_LEQUAL );
        }
    }


    private static GLCapabilities newDefaultCaps() {
        GLProfile profile = GLProfile.get( GLProfile.GL3 );
        GLCapabilities caps = new GLCapabilities( profile );
        caps.setHardwareAccelerated( true );
        caps.setStencilBits( 8 );
        caps.setDepthBits( 24 );
        caps.setSampleBuffers( true );
        caps.setNumSamples( 4 );
        return caps;
    }

    
    
    /**
     * @deprecated use spaceMouseCont()
     */
    public SpaceMouseController spaceMouseController() {
        return mSpaceMouseCont;
    }
    
    /**
     * @deprecated use navigationCont
     */
    public NavigationController walkingController() {
        return mNavCont;
    }

    /**
     * Use <code>buildFrame()</code>, which more usefully sets
     * the size of the newly craeted frame to the desired size
     * of the content pane.
     */
    @Deprecated 
    public JFrame createFrame( int w, int h ) {
        JFrame frame = new JFrame();
        
        frame.setSize( w, h );
        frame.setLocationRelativeTo( null );
        frame.add( awtComponent() );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        
        return frame;
    }


}
