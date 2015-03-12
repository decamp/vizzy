/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy;

import bits.draw3d.*;
import bits.draw3d.scene.SceneGraph;
import bits.math3d.Box3;
import bits.vizzy.input.NavigationController;

import javax.swing.*;


/**
 * @author Philip DeCamp
 */
public class TestVizzy {

    public static void main( String[] args ) throws Exception {
        launchApp();
    }

    static void launchApp() throws Exception {
        ClockNode clock = ClockNode.create( false, 0, 0 );
        SimpleEngine eng = SimpleEngine.create( clock.playCont().masterClock(), clock.tweenExec(), 8 );
        eng.setModelBounds( new Box3( -20, -20, -20, 20, 20, 20 ) );
        eng.navigationCont().mouseMode( NavigationController.MouseMode.MOVE );

        SceneGraph model = new SceneGraph();
        model.add( clock );
        model.connectLast( new DrawNodeAdapter() {
            @Override
            public void pushDraw( DrawEnv d ) {
                d.mCullFace.apply( false );
                d.mView.push();
                //d.mView.identity();
                d.mProj.push();
                d.mLineWidth.apply( 5f );

                //d.mProj.identity();
                //d.mProj.setFrustum( -0.75f, 0.75f, -1f, 1f, 0.25f, 50.f );

                DrawStream s = d.drawStream();
                s.config( true, false, false );
                s.beginLines();
                s.color( 1,  0,   0 );
                s.vert(  0,  0, -10 );
                s.vert( 10,  0, -10 );

                s.color( 0,  1,   0 );
                s.vert(  0,  0, -10 );
                s.vert(  0, 10, -10 );

                s.color( 0,  0,  1 );
                s.vert(  0,  0,-10 );
                s.vert(  0,  0,  0 );
                s.end();

                s.color( 0.5f, 0.5f, 0.5f );
                s.beginTris();
                s.vert( 1, 0, -10 );
                s.vert( 0, 1, -10 );
                s.vert( 0, 0,  -9 );
                s.end();

                d.mView.pop();
                d.mProj.pop();
            }
        } );
        eng.setModelGraph( model );

        JFrame frame = eng.buildFrame( 1204, 768 );
        frame.setVisible( true );
        eng.start( 60.0 );
    }

}
