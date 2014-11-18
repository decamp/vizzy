/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import java.io.*;
import java.util.zip.*;

/**
 * @author decamp
 */
public class DepthFiles {
    
    public static void writeDepthFile( byte[] arr, int w, int h, File outFile ) throws IOException {
        GZIPOutputStream s   = new GZIPOutputStream( new FileOutputStream( outFile ), 8064 );
        DataOutputStream out = new DataOutputStream( s );
        
        out.writeInt( w );
        out.writeInt( h );
        out.write( arr, 0, w * h * 4 );
        out.flush();
        s.flush();
        s.finish();
        s.close();
    }

    
    public static void writeDepthFile( float[] arr, int w, int h, File outFile ) throws IOException {
        GZIPOutputStream s   = new GZIPOutputStream( new FileOutputStream( outFile ), 8064 );
        DataOutputStream out = new DataOutputStream( new BufferedOutputStream( s ) );

        out.writeInt( w );
        out.writeInt( h );
        for(int i = 0; i < w * h; i++) {
            out.writeFloat( arr[i] );
        }
        
        out.flush();
        s.flush();
        s.finish();
        s.close();
    }

    
    public static float[] readDepthFile( File file, int[] outSize, float[] outArr ) throws IOException {
        DataInputStream in = new DataInputStream( new GZIPInputStream( new FileInputStream( file ), 8064 ) );
        int w = in.readInt();
        int h = in.readInt();
        
        outSize[0] = w;
        outSize[1] = h;
        if(outArr == null || outArr.length < w * h) {
            outArr = new float[w * h];
        }
        
        for( int i = 0; i < w * h; i++ ) {
            outArr[i] = in.readFloat();
        }
        
        in.close();
        return outArr;
    }
    
}
