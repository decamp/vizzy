/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.vizzy.input;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNodeAdapter;
import bits.draw3d.actors.*;
import bits.glui.event.*;
import bits.hidpunk.HidException;
import bits.hidpunk.deltamouse.*;
import bits.math3d.Vec3;
import bits.microtime.Clock;


public final class NavigationController extends DrawNodeAdapter implements GKeyListener {


    public static enum MouseMode {
        OFF,
        MOVE,
        DRAG_LEFT_BUTTON,
        DRAG_RIGHT_BUTTON
    }


    public static enum KeyboardMode {
        OFF,
        WASD
    }


    private static final int KEY_FORWARD  = GKeyEvent.VK_W;
    private static final int KEY_BACKWARD = GKeyEvent.VK_S;
    private static final int KEY_LEFT     = GKeyEvent.VK_A;
    private static final int KEY_RIGHT    = GKeyEvent.VK_D;
    private static final int KEY_UP       = GKeyEvent.VK_E;
    private static final int KEY_DOWN     = GKeyEvent.VK_Q;

    // Keys for dvorak. Dumb hack that I needed.
    // private static final char KEY_FORWARD = ',';
    // private static final char KEY_BACKWARD = 'o';
    // private static final char KEY_LEFT = 'a';
    // private static final char KEY_RIGHT = 'e';
    // private static final char KEY_UP = '.';
    // private static final char KEY_DOWN = '\'';

    private static final Vec3 PITCH = ActorCoords.newLeftAxis();
    private static final Vec3 YAW   = ActorCoords.newUpAxis();

    private static final int FORWARD  = 0;
    private static final int BACKWARD = 1;
    private static final int LEFT     = 2;
    private static final int RIGHT    = 3;
    private static final int UP       = 4;
    private static final int DOWN     = 5;

    @SuppressWarnings( "unused" )
    private final DeltaMouseGroup mMouseGroup;
    private final WalkingActor    mTarget;
    private final Clock           mClock;

    private long mPrevMicros = Long.MAX_VALUE;

    private MouseMode    mMouseMode = MouseMode.MOVE;
    private KeyboardMode mKeyMode   = KeyboardMode.WASD;

    private float mMoveSpeed = 10.0f / 8.0f;
    private float mRotSpeed  = (float)(0.25 * Math.PI / 300.0);

    // Forward, left, backward, right, up, down
    private boolean[] mForce = { false, false, false, false, false, false };

    private float mSpeedX = 0;
    private float mSpeedY = 0;
    private float mSpeedZ = 0;
    private float mRotX   = 0;
    private float mRotY   = 0;


    public NavigationController( Clock clock, WalkingActor target ) {
        mTarget = target;
        mClock = clock;
        DeltaMouseGroup g = null;

        try {
            g = DeltaMouseGroup.create();
            g.addListener( new MouseHandler() );
            g.start();
        } catch( HidException ignored ) {
        }

        mMouseGroup = g;
        mouseMode( MouseMode.OFF );
    }


    @Override
    public void pushDraw( DrawEnv d ) {
        update();
    }

    /**
     * Must be called on each frame to update target actor. pushDraw() will call
     * this method as well.
     */
    public void update() {
        long t = mClock.micros();

        if( t <= mPrevMicros ) {
            mPrevMicros = t;
            return;
        }

        synchronized( this ) {
            float dt = (float)( (t - mPrevMicros) / 1000000.0 );
            // System.out.println(mSpeedY * mMoveSpeed * dt + "\t" + mSpeedX *
            // mMoveSpeed * dt);

            if( mKeyMode != KeyboardMode.OFF ) {
                if( mSpeedX != 0.0 || mSpeedY != 0.0 || mSpeedZ != 0.0 ) {
                    mTarget.move( mSpeedX * mMoveSpeed * dt,
                                  mSpeedY * mMoveSpeed * dt,
                                  mSpeedZ * mMoveSpeed * dt );
                }
            }

            if( mRotX != 0.0 ) {
                mTarget.rotate( mRotX * mRotSpeed, YAW.x, YAW.y, YAW.z );
                mRotX = 0;
            }

            if( mRotY != 0.0 ) {
                mTarget.rotate( mRotY * mRotSpeed, PITCH.x, PITCH.y, PITCH.z );
                mRotY = 0;
            }
        }

        mPrevMicros = t;
    }



    public void mouseMode( MouseMode mode ) {
        mMouseMode = mode;

        if( mode != MouseMode.OFF ) {
            mTarget.rollLock( true );
            mTarget.moveMode( MoveMode.WALK );
        } else {
            mTarget.rollLock( false );
            mTarget.moveMode( MoveMode.FLY );
        }
    }


    public MouseMode mouseMode() {
        return mMouseMode;
    }


    public void mouseSpeed( float radsPerPixel ) {
        mRotSpeed = radsPerPixel;
    }


    public float mouseSpeed() {
        return mRotSpeed;
    }


    public void keyboardMode( KeyboardMode mode ) {
        mKeyMode = mode;
    }


    public KeyboardMode keyboardMode() {
        return mKeyMode;
    }


    public void moveSpeed( float speed ) {
        mMoveSpeed = speed;
    }


    public float moveSpeed() {
        return mMoveSpeed;
    }



    public void goForward( boolean forward ) {
        mForce[FORWARD] = forward;
        updateMoveSpeed();
    }


    public boolean isGoingForward() {
        return mForce[FORWARD];
    }


    public void goBackward( boolean backward ) {
        mForce[BACKWARD] = backward;
        updateMoveSpeed();
    }


    public boolean isGoingBackward() {
        return mForce[BACKWARD];
    }


    public void goLeft( boolean left ) {
        mForce[LEFT] = left;
        updateMoveSpeed();
    }


    public boolean isGoingLeft() {
        return mForce[LEFT];
    }


    public void goRight( boolean right ) {
        mForce[RIGHT] = right;
        updateMoveSpeed();
    }


    public boolean isGoingRight() {
        return mForce[RIGHT];
    }


    public void goUp( boolean up ) {
        mForce[UP] = up;
        updateMoveSpeed();
    }


    public boolean isGoingUp() {
        return mForce[UP];
    }


    public void goDown( boolean down ) {
        mForce[DOWN] = down;
        updateMoveSpeed();
    }


    public boolean isGoingDown() {
        return mForce[DOWN];
    }



    public void keyPressed( GKeyEvent e ) {
        handleKey( e, true );
    }


    public void keyReleased( GKeyEvent e ) {
        handleKey( e, false );
    }


    public void keyTyped( GKeyEvent e ) {}


    private void handleKey( GKeyEvent e, boolean press ) {
        switch( e.getKeyCode() ) {
        case KEY_LEFT:
            goLeft( press );
            break;
        case KEY_BACKWARD:
            goBackward( press );
            break;
        case KEY_RIGHT:
            goRight( press );
            break;
        case KEY_FORWARD:
            goForward( press );
            break;
        case KEY_UP:
            goUp( press );
            break;
        case KEY_DOWN:
            goDown( press );
            break;
        }
    }



    private void updateMoveSpeed() {
        float mx = 0;
        float my = 0;
        float mz = 0;

        if( mForce[RIGHT] ) {
            my -= 1.0;
        }
        if( mForce[LEFT] ) {
            my += 1.0;
        }
        if( mForce[FORWARD] ) {
            mx += 1.0;
        }
        if( mForce[BACKWARD] ) {
            mx -= 1.0;
        }
        if( mForce[UP] ) {
            mz += 1.0;
        }
        if( mForce[DOWN] ) {
            mz -= 1.0;
        }

        mSpeedX = mx;
        mSpeedY = my;
        mSpeedZ = mz;

        mTarget.moveMode( MoveMode.WALK );
        mTarget.rollLock( true );
    }


    private final class MouseHandler implements DeltaMouseListener {

        private boolean mLeftDown = false;
        private boolean mRightDown = false;

        public void mouseMoved( long timeMicros, int dx, int dy ) {
            switch( mMouseMode ) {
            case OFF:
                return;

            case DRAG_LEFT_BUTTON:
                if( mLeftDown )
                    break;

                return;
            case DRAG_RIGHT_BUTTON:
                if( mRightDown )
                    break;

                return;
            default:
            }

            synchronized( NavigationController.this ) {
                mRotX -= dx;
                mRotY += dy;
            }
        }

        public void mouseButtonPressed( long timeMicros, int button ) {
            if( button == 1 ) {
                mLeftDown = true;
            } else if( button == 2 ) {
                mRightDown = true;
            }
        }

        public void mouseButtonReleased( long timeMicros, int button ) {
            if( button == 1 ) {
                mLeftDown = false;
            } else if( button == 2 ) {
                mRightDown = false;
            }
        }

    }

}
