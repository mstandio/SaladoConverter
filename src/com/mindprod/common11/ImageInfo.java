/*
 * @(#)ImageInfo.java
 *
 * Summary: Rapidly determine the gif or jpg or png image width and height without loading the image.
 *
 * Copyright: (c) 2003-2010 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.1+
 *
 * Created with: IntelliJ IDEA IDE.
 *
 * Version History:
 *  1.1 2006-03-04
 */
package com.mindprod.common11;

import com.mindprod.ledatastream.LEDataInputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Rapidly determine the gif or jpg or png image width and height without loading the image.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.1 2006-03-04
 * @since 2003-05-15
 */
public final class ImageInfo
    {
    // -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * Summary: Rapidly calculate image size without loading the image.
     * <p/>
     * get the height and width of a gif or jpg image without having to read the entire Image into RAM. This works only
     * with local images, not ones out on the web accessible by URL.
     * Works with fully qualified name, not necessarily anything to do with the htmlmacros package.
     *
     * @param imageFilename filename. Must end in .jpg or .gif
     *
     * @return length-2 array of two numbers, width and height of the image, or 0,0 if it could not be found. We don't
     *         return a Dimension object because it provides doubles, not ints.
     * @noinspection WeakerAccess
     * @see "com.mindprod.ledatastream.LEDataStream"
     */
    public static int[] getImageDimensions( String imageFilename )
        {
        int width = 0;
        int height = 0;
        LEDataInputStream inle = null;
        DataInputStream inbe = null;
        final int place = imageFilename.lastIndexOf( '.' );
        if ( place < 0 )
            {
            return new int[] { 0, 0 };
            }
        final String ext = imageFilename.substring( place + 1 ).toLowerCase();
        try
            {
            try
                {
                if ( ext.equals( "gif" ) )
                    {
                    // signature GIF89a i.e. 0x474946383961
                    // or GIF87a
                    // just check first 4 chars
                    // width at offset 0x06 and height at 0x08 16-bit little
                    // endian
                    inle = new LEDataInputStream( new FileInputStream(
                            imageFilename ) );
                    int signature4 = inle.readInt();
                    if ( signature4 != 0x38464947/* reversed */ )
                        {
                        throw new IOException( "not a valid gif" );
                        }
                    inle.skipBytes( 2 );
                    width = inle.readShort();
                    height = inle.readShort();
                    inle.close();
                    }
                else if ( ext.equals( "jpg" ) || ext.equals( "jpeg" ) )
                    {
                    // ffd8
                    // in variable location: height, then width, big endian.
                    inbe = new DataInputStream( new FileInputStream(
                            imageFilename ) );

                    if ( inbe.readUnsignedByte() != 0xff )
                        {
                        throw new IOException( "not a valid jpg" );
                        }
                    if ( inbe.readUnsignedByte() != 0xd8 )
                        {
                        throw new IOException( "not a valid jpg" );
                        }
                    while ( true )
                        {
                        int p1 = inbe.readUnsignedByte();
                        int p2 = inbe.readUnsignedByte();
                        if ( p1 == 0xff && 0xc0 <= p2 && p2 <= 0xc3 )
                            {
                            inbe.skipBytes( 3 );
                            height = inbe.readShort();
                            width = inbe.readShort();
                            break;
                            }
                        else
                            {
                            // bypass this marker
                            int length = inbe.readShort();
                            inbe.skipBytes( length - 2 );
                            }
                        }// end while
                    inbe.close();
                    }// end else
                if ( ext.equals( "png" ) )
                    {
                    // see http://mindprod.com/jgloss/png.html
                    // The PNG file header looks like this:
                    // signature \211PNG\r\n\032\n 8-bytes
                    // ie. in hex 89504e470d0a1a0a
                    // chunksize 4 bytes 0x0000000D
                    // chunkid 4 bytes "IHDR" 0x49484452
                    // width 4 bytes big-endian binary
                    // height 4 bytes big-endian binary
                    inbe = new DataInputStream( new FileInputStream(
                            imageFilename ) );
                    long signature = inbe.readLong();
                    if ( signature != 0x89504e470d0a1a0aL )
                        {
                        throw new IOException( "not a valid png file" );
                        }
                    inbe.skipBytes( 4 + 4 );

                    width = inbe.readInt();
                    height = inbe.readInt();
                    inbe.close();
                    }
                // other file types will default to 0,0
                }// end try
            catch ( IOException e )
                {
                if ( inle != null )
                    {
                    inle.close();
                    }
                if ( inbe != null )
                    {
                    inbe.close();
                    }
                width = 0;
                height = 0;
                }
            }
        catch ( Exception e )
            {
            width = 0;
            height = 0;
            }
        return new int[] { width, height };
        }// end getImageDimensions

    // --------------------------- main() method ---------------------------

    /**
     * Test driver to find size of an image mentioned on the command line.
     *
     * @param args name of a *.gif or *.jpg or *.png image file to test. Should print out its width and height.
     */
    public static void main( String[] args )
        {
        if ( args.length != 1 )
            {
            System.out.println( "Need exactly one image filename on the command line." );
            }
        String imageFilename = args[ 0 ];
        int[] d = getImageDimensions( imageFilename );
        System.out.println( imageFilename
                            + " width:"
                            + d[ 0 ]
                            + " height:"
                            + d[ 1 ] );
        }
    }
