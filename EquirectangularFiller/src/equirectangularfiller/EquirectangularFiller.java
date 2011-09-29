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
            + "java [-java_options] -jar path/to/EquirectangularFiller.jar [-options] [args...]\n\n"
            + "For a list of java options try: java -help or java -X for a list of less\n"
            + "common options. Loading large images for conversion takes a lot of RAM,\n"
            + "so you will find the -Xmx option useful to raise Java's maximum heap size.\n"
            + "The -Xmx command is followed immediately by an integer specifying RAM size\n"
            + "and a unit indicator. For example: -Xmx1024m means to use 1024 megabytes.\n"
            + "If you see an heap size error, then you will need to increase this value.\n\n"
            + " Basic usage example for the jar file:\n\n"
            + "java -Xmx1024m -jar path/to/EquirectangularFiller.jar -fov 120 path/to/image\n\n"
            + "This will generate file with 'filled_' prepended onto its name. So in the\n"
            + "basic example above, the output file would be in path/to/filled_image.\n"
            + "This file will have proportions width:height as 2:1 and will represent\n"
            + "full equirectangular image.\n\n"
            + " Options:\n\n"
            + "-fov: horizontal field of view (in degrees) of input image. Default is 360.\n\n"
            + "-offset: vertical offset (in pixels) of input image. Usually means how far\n"
            + "\tmiddle of image is away from horizon. Default is 0.\n\n"
            + "-outputdir or -o: the output directory for the converted image. It need not\n"
            + "\texist. Default is the input folder.\n\n"
            + "-simpleoutput or -s: output will not have 'filled_' prepended to its name.\n\n"
            + " Arguments:\n\n"
            + "The argument following any options is single input partial equirectangular\n"
            + "image that will be filled with black background to match proportions of\n"
            + "full equirectangular image.";

    private enum CmdParseState {

        DEFAULT, FOV, OFFSET, OUTPUTDIR, INPUTFILE
    }
    // The following can be overriden/set by the indicated command line arguments
    static boolean showHelp = false;              // -help | -h
    static int fov = 360;                         // -fov
    static int offset = 0;                        // -offset
    static File outputDir = null;                 // -outputdir | -o
    static boolean simpleOutput = false;          // -simpleoutput | -s    
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
                    outputDir = inputFile.getAbsoluteFile().getParentFile();
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
                    } else if (arg.equals("-simpleoutput") || arg.equals("-s")) {
                        simpleOutput = true;
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

        String outputName = outputDir.getAbsolutePath()
                + File.separator
                + (simpleOutput ? "" : "filled_")
                + inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."))
                + ".tif";
        BufferedImage input = loadImage(inputFile);
        Point outputDimension = getOutputDimension(input);
        BufferedImage output = new BufferedImage(outputDimension.x, outputDimension.y, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = output.getGraphics();
        graphics.drawImage(input, (int) Math.floor((double) (outputDimension.x - input.getWidth()) * 0.5d),
                (int) Math.floor((double) (outputDimension.y - input.getHeight()) * 0.5d + offset), Color.BLACK, null);
        input.flush();

        System.out.println("Filling image: " + inFile.getAbsolutePath() + " to: " + outputName);

        JAI.create("filestore", output, outputName, "TIFF");
        graphics.dispose();
        output.flush();
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
