/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.anim;

import bits.draw3d.DrawEnv;
import bits.microtime.TimeRanged;

/**
 * @author decamp
 */
public interface ScriptAction extends TimeRanged {
    
    /**
     * @param micros current time
     * @return true iff script is complete
     */
    public boolean update( DrawEnv d, long micros );
    
    /**
     * Tells the action that it has been cancelled.
     */
    public void cancel();
    
    /**
     * @return true iff ScriptAction has completed and no longer requires updates.
     */
    public boolean isComplete();
    
}
