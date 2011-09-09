package equirectangularfiller;

import com.sun.media.jai.codec.FileSeekableStream;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

/**
 * @author Marek Standio
 */
public class EquirectangularFiller {

    static final String help = "\nEquirectangularFiller v1.0 \n\n Usage: \n\n"
            + "java [-java_options] -jar path/to/EquirectangularFiller.jar [-options] [args...]\n"
            + "For a list of java options try: java -help or java -X for a list of less common\n"
            + "options. Loading large images for conversion takes a lot of RAM so you will\n"
            + "find the -Xmx option useful to raise Java's maximum heap size. The -Xmx command\n"
            + "is followed immediately by an integer specifying RAM size and a unit indicator.\n"
            + "For example, -Xmx1024m means to use 1024 megabytes. If you see an error about\n"
            + "heap size, then you will need to increase this value.\n\n"
            + " Basic usage example for the jar file:\n\n"
            + "java -Xmx1024m -jar path/to/EquirectangularFiller.jar -fov 120 path/to/image\n"
            + "This will generate file with 'filled_' prepended onto its name. This file will have\n"
            + "proportions width:height as 2:1 and will represent full equirectangular image.\n"
            + "\n"
            + " Options:\n\n"
            + "-fov: horizontal field of view (in degrees) represented in input image.\n"
            + "\tDefault is 360.\n\n"            
            + "-offset: vertical offset (in pixels) of image. Usually means how far middle \n"
            + "\tof image is away from horizon.\n"
            + "\tDefault is 0\n\n"
            + "-outputdir or -o: the output directory for the converted image. It\n"
            + "\tneed not exist. Default is the input folder.\n\n"
            + "-simpleoutput or -s: output file will not have 'filled_' prepended to its name.\n\n"
            + "-verbose or -v: makes the utility more 'chatty' during processing. \n\n"
            + " Arguments:\n\n"
            + "The argument following any options is single input partial equirectangular image that\n"
            + "will be filled with black background to match proportions of full equirectangular image.\n";

    private enum CmdParseState {

        DEFAULT, FOV, OFFSET, OUTPUTDIR, INPUTFILE
    }
    // The following can be overriden/set by the indicated command line arguments
    static boolean showHelp = false;              // -help | -h
    static int fov = 360;                         // -fov
    static int offset = 0;                        // -offset
    static File outputDir = null;                 // -outputdir | -o
    static boolean simpleOutput = false;          // -simpleoutput | -s
    static boolean verboseMode = false;           // -verbose
    static File inputFile = null;                 // must follow all other args

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            try {
                parseCommandLine(args);
                if (showHelp) {
                    System.out.println(help);
                    return;
                }
                if (outputDir != null) {
                    if (!outputDir.exists() || !outputDir.isDirectory()) {
                        if (!outputDir.mkdir()) {
                            throw new IOException("Unable to create directory: " + outputDir);
                        }
                    }
                } else {
                    File parentFile = inputFile.getAbsoluteFile().getParentFile();
                    outputDir = parentFile;
                }
            } catch (Exception e) {
                System.out.println("Invalid command: " + e.getMessage());
                System.out.println("type -h to get list of supported commands");
                return;
            }
            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
            // can be problematic in non-admin accounts
            // java -Dcom.sun.media.jai.disableMediaLib=true YourApp
            processImageFile(inputFile, outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the command line arguments
     * @param args the command line arguments
     */
    private static void parseCommandLine(String[] args) throws Exception {
        CmdParseState state = CmdParseState.DEFAULT;
        for (int count = 0; count < args.length; count++) {
            String arg = args[count];
            switch (state) {
                case DEFAULT:
                    if (arg.equals("-help") || arg.equals("-h")) {
                        showHelp = true;
                        return;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-fov")) {
                        state = CmdParseState.FOV;
                    } else if (arg.equals("-offset")) {
                        state = CmdParseState.OFFSET;
                    } else {
                        state = CmdParseState.INPUTFILE;
                    }
                    break;
                case FOV:
                    int tmp = Integer.parseInt(args[count]);
                    if (tmp < 1 || tmp > 360) {
                        throw new Exception("-fov");
                    }
                    fov = tmp;
                    state = CmdParseState.DEFAULT;
                    break;
                case OFFSET:
                    offset = Integer.parseInt(args[count]);
                    state = CmdParseState.DEFAULT;
                    break;
                case OUTPUTDIR:
                    outputDir = (new File(args[count]));
                    state = CmdParseState.DEFAULT;
                    break;
            }
            if (state == CmdParseState.INPUTFILE) {
                inputFile = new File(arg);
                if (!inputFile.exists()) {
                    throw new FileNotFoundException("Missing input file: " + inputFile.getPath());
                }
                if (inputFile.isDirectory()) {
                    throw new FileNotFoundException("Directory is not accepted as input: " + inputFile.getPath());
                }
                ArrayList<String> exts = new ArrayList<String>();
                exts.add("bmp");
                exts.add("jpg");
                exts.add("jpeg");
                exts.add("png");
                exts.add("gif");
                exts.add("tif");
                exts.add("tiff");
                String fExt = inputFile.getAbsolutePath().substring(inputFile.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
                for (String ext : exts) {
                    if (ext.equals(fExt)) {
                        return;
                    }
                }
                throw new IllegalArgumentException("Inrecognized input file extension: " + fExt);
            }
        }
    }

    private static void processImageFile(File inFile, File outputDir) throws IOException {
        if (verboseMode) {
            System.out.printf("filling image: %s\n", inFile);
        }
        String outputName = (simpleOutput ? "" : "fiiled_") + inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")) + ".tif";
        BufferedImage input = loadImage(inputFile);
        Point outputDimension = getOutputDimension(input);
        BufferedImage output = new BufferedImage(outputDimension.x, outputDimension.y, BufferedImage.TYPE_INT_RGB);        
        Graphics graphics = output.getGraphics();
        graphics.drawImage(input, (int) Math.floor((double) (outputDimension.x - input.getWidth()) * 0.5d),
                (int) Math.floor((double) (outputDimension.y - input.getHeight()) * 0.5d + offset), Color.BLACK, null);
        if (verboseMode) {
            System.out.printf("Writing to directory: %s\n", outputDir.getAbsolutePath() + File.separator + outputName);
        }
        JAI.create("filestore", output, outputName, "TIFF");
        graphics.dispose();
    }

    /**
     * Loads image from file
     * @param file the file containing the image
     */
    private static BufferedImage loadImage(File file) throws IOException {
        FileSeekableStream stream = null;
        BufferedImage result = null;
        try {
            stream = new FileSeekableStream(file);
            PlanarImage planarImage = JAI.create("stream", stream);
            //PlanarImage planarImage = JAI.create("fileload", file.getAbsolutePath());
            result = planarImage.getAsBufferedImage();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Cannot read image file: " + file.getAbsolutePath());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return result;
    }

    private static Point getOutputDimension(BufferedImage input) {
        double width = 0;
        double height = 0;
        if (fov == 360) {
            width = input.getWidth();
        } else {
            width = Math.floor(360d * (double) input.getWidth() / (double) fov);
        }
        if (width % 2 == 0) {
            height = width / 2d;
        } else {
            width -= 1;
            height = width / 2d;
        }
        return new Point((int) width, (int) height);
    }
}
