package bits.vizzy;

import bits.util.gui.ImagePanel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 * @author Philip DeCamp
 */
public class GenWifi {


    public static void main( String[] args ) throws Exception {

        //final int dim = 192;
        //fianl int baseColor = 0xFF5ABEE4;

        final int dim = 154;
        final int baseColor = 0xFFF3A222;

        BufferedImage im = new BufferedImage( dim, dim, BufferedImage.TYPE_INT_ARGB );

        for( int y = 0; y < dim; y++ ) {
            for( int x = 0; x < dim; x++ ) {
                double dx = x - dim * 0.5;
                double dy = y - dim * 0.5;
                double off = Math.sqrt( dx * dx + dy * dy );
                double s = Math.cos( Math.max( 0.0, ( off - dim * 5.0 / 210.0 ) * ( dim / 500.0 ) ) ) * 0.6 + 0.6;
                s = Math.max( 0.0, Math.min( 1.0, s ) );

                double alpha = off < 0.49 * dim ? s : 0;

                int a   = (int)( alpha * 255.0 + 0.5 ) ;
                int val = mult( baseColor, a << 24 | 0x00FFFFFF );
                im.setRGB( x, y, val );
            }
        }

        ImagePanel.showImage( im );
        ImageIO.write( im, "png", new File( "/tmp/phone.png" ) );
    }



    public static int mult( int ca, int cb ) {
        int a = ( ca >>> 24        ) * ( cb >>> 24        ) / 0xFF;
        int r = ( ca >>  16 & 0xFF ) * ( cb >>  16 & 0xFF ) / 0xFF;
        int g = ( ca >>   8 & 0xFF ) * ( cb >>   8 & 0xFF ) / 0xFF;
        int b = ( ca        & 0xFF ) * ( cb        & 0xFF ) / 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }


}
