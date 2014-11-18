/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizor;

import bits.draw3d.*;
import bits.draw3d.scene.*;
import bits.glui.*;


/**
 * Not thread-safe.
 * 
 * @author decamp
 */
public class SceneGraphPanel extends GPanel {

    private GraphPath<DrawNode> mDrawList = null;

    private boolean mInit    = true;
    private boolean mReshape = true;
    private boolean mDispose = false;
    private boolean mExit    = false;


    public SceneGraphPanel() {
        setLayout( new Layout() );
    }


    public void setGraph( SceneGraph graph ) {
        if( graph == null ) {
            mDrawList = null;
            return;
        }
        GraphPath<Object> raw = graph.compilePath();
        GraphPath<DrawNode> path = SceneGraphs.modulePathToNodePath( raw, DrawNode.class );
        mDrawList = path;
        mInit     = true;
    }

    @Override
    public void paintComponent( DrawEnv g ) {
        GraphPath<DrawNode> path = mDrawList;
        if( path == null ) {
            return;
        }

        if( mInit ) {
            mInit = false;
            if( !mDispose ) {
                for( GraphStep<DrawNode> s: path ) {
                    if( s.type() == GraphActionType.PUSH ) {
                        s.target().init( g );
                    }
                }
            } else {
                for( GraphStep<DrawNode> s: path ) {
                    if( s.type() == GraphActionType.PUSH ) {
                        s.target().dispose( g );
                    }
                }
                
                if( mExit ) {
                    System.exit( 0 );
                }
            }
        }
        
        if( mReshape ) {
            mReshape    = false;
            final int w = width();
            final int h = height();
            
            for( GraphStep<DrawNode> s: path ) {
                if( s.type() == GraphActionType.PUSH ) {
                    if( s.target() instanceof ReshapeListener ) {
                        ((ReshapeListener)s.target()).reshape( g );
                    }
                }
            }
        }
        
        for( GraphStep<DrawNode> s: path ) {
            if( s.type() == GraphActionType.PUSH ) {
                DrawNode target = s.target();
                target.pushDraw( g );
            } else {
                DrawNode target = s.target();
                target.popDraw( g );
            }
        }
    }


    public void dispose( boolean exit ) {
        mInit    = true;
        mDispose = true;
        mExit |= exit;
    }
    
    
    private final class Layout implements GLayout {

        public void layoutPane( GComponent pane ) {
            GComponent parent = parent();
            if(parent == null)
                return;
            
            bounds( 0, 0, parent.width(), parent.height() );
            mReshape = true;
        }
    
    }
    
}
