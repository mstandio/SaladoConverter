/*
Copyright 2009 Zephyr Renner
This file is part of EquirectangulartoCubic.java.
EquirectangulartoCubic is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
EquirectangulartoCubic is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with EquirectangulartoCubic. If not, see http://www.gnu.org/licenses/.

The Equi2Rect.java library is modified from PTViewer 2.8 licenced under the GPL courtesy of Fulvio Senore, originally developed by Helmut Dersch.  Thank you both!

Some basic structural of this code are influenced by / modified from DeepJZoom, Glenn Lawrence, which is also licensed under the GPL.
 */

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.Vector;
import java.util.Iterator;

/**
 *
 * @author Zephyr Renner
 */
public class EquirectangularToCubic {

    static final String version = "\nEquirectangularToCubic v1.2";
    static final String help =
            "\n\tApplication converts equirectangular *.jpg panoramas into cubie walls \n\n" +
            "\tInput (file or directory) must follow all other arguments\n\n" +
            "\t-h | -help | -? \n\t\t show help and exit \n\n" +
            "\t-v | version \n\t\t show version and exit \n\n" +
            "\t-interpolation \n\t\t lanczos2 | bilinear | nearest-neighbor default:lanczos2\n\n" +
            "\t-overlap \n\t\t overcome display bug, default:1\n\n" +
            "\t-quality \n\t\t quality of jpg files (0.0 to 1.0), default:0.8\n\n" +
            "\t-outputdir | -o \n\t\t output directory, default:parent directory\n\n" +
            "\t-simpleoutput | -s \n\t\t dont create output subdirectory\n\n" +
            "\t-verbose \n\t\t enable verbose output \n\n" +
            "\t-debug \n\t\t show detailed debug information\n\n";
    static final String LANCZOS2 = "lanczos2";
    static final String BILINEAR = "bilinear";
    static final String NEAREST_NEIGHBOUR = "nearest-neighbor";

    private enum CmdParseState {
        DEFAULT, OUTPUTDIR, OVERLAP, INPUTFILE, INTERPOLATION, QUALITY
    };
    static String format = "jpg";
    // The following can be overriden/set by the indicated command line arguments
    static boolean showVersion = false;         // -v | -version
    static boolean showHelp = false;            // -h | -help | -?
    static int overlap = 1;           	        // -overlap
    static String interpolation = LANCZOS2;     // -interpolation (lanczos2, bilinear, nearest-neighbor)
    static float quality = 0.8f;                // -quality (0.0 to 1.0)
    static File outputDir = null;               // -outputdir | -o
    static boolean createOutputSubDir = true;     // -simpleoutput | -s
    static boolean verboseMode = false;         // -verbose
    static boolean debugMode = false;           // -debug
    static Vector<File> inputFiles = new Vector<File>();  // must follow all other args
    static Vector<File> outputFiles = new Vector<File>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            try {
                parseCommandLine(args);
                if (showVersion) {
                    System.out.println(version);
                    return;
                }
                if (showHelp) {
                    System.out.println(help);
                    return;
                }
                //when one output file is given, output are folders with names of input file in that directory
                if (outputFiles.size() == 1) {
                    outputDir = outputFiles.get(0);
                    outputFiles.clear();

                    if (!outputDir.exists() || !outputDir.isDirectory()) {                         
                        if (!outputDir.mkdir()) {
                            throw new IOException("Unable to create directory: " + outputDir);
                        }
                    }

                    Iterator<File> itr = inputFiles.iterator();
                    while (itr.hasNext()) {
                        File inputFile = itr.next();
                        String fileName = inputFile.getName();
                        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                        if(createOutputSubDir){
                            File outputFile = createDir(outputDir, "cubic_" + nameWithoutExtension);
                            outputFiles.add(outputFile);
                        }else{
                            outputFiles.add(outputDir);
                        }
                    }

                }
                // default location for output files is folder beside input files with the name of the input file
                if (outputFiles.size() == 0) {
                    Iterator<File> itr = inputFiles.iterator();
                    while (itr.hasNext()) {
                        File inputFile = itr.next();
                        File parentFile = inputFile.getAbsoluteFile().getParentFile();
                        String fileName = inputFile.getName();
                        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                        if(createOutputSubDir){
                            File outputFile = createDir(parentFile, "cubic_" + nameWithoutExtension);
                            outputFiles.add(outputFile);
                        }else{
                            outputFiles.add(parentFile);
                        }
                    }
                }
                if (debugMode) {
                    System.out.printf("overlap=%d ", overlap);
                    System.out.printf("interpolation=%s ", interpolation);
                    System.out.printf("quality=%.2f ", quality);
                }

            } catch (Exception e) {
                System.out.println("Invalid command: " + e.getMessage());
                return;
            }
            if(!verboseMode){
                System.out.println("Processing images...");
            }
            Equi2Rect.init();
            for (int i = 0; i < inputFiles.size(); i++) {
                processImageFile(inputFiles.get(i), outputFiles.get(i));
            }        
        }catch (Exception e){
            e.printStackTrace(System.out);
        }
    }

    /**
     * Process the command line arguments
     * @param args the command line arguments
     */
    private static void parseCommandLine(String[] args) throws Exception {
        CmdParseState state = CmdParseState.DEFAULT;
        for (int count = 0; count <
                args.length; count++) {
            String arg = args[count];
            switch (state) {
                case DEFAULT:
                    if (arg.equals("-v") || arg.equals("-version")) {
                        showVersion = true;
                    } else if (arg.equals("-h") || arg.equals("-help") || arg.equals("-?")) {
                        showHelp = true;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                        debugMode = true;
                    }else if (arg.equals("-simpleoutput") || arg.equals("-s")){
                        createOutputSubDir = false;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-overlap")) {
                        state = CmdParseState.OVERLAP;
                    } else if (arg.equals("-interpolation")) {
                        state = CmdParseState.INTERPOLATION;
                    } else if (arg.equals("-quality")) {
                        state = CmdParseState.QUALITY;
                    } else {
                        state = CmdParseState.INPUTFILE;
                    }

                    break;
                case OUTPUTDIR:
                    outputFiles.add(new File(args[count]));
                    state = CmdParseState.DEFAULT;
                    break;

                case OVERLAP:
                    overlap = Integer.parseInt(args[count]);
                    state = CmdParseState.DEFAULT;
                    break;

                case INTERPOLATION:
                    interpolation = args[count];
                    state = CmdParseState.DEFAULT;
                    break;

                case QUALITY:
                    float qtmp = Float.parseFloat(args[count]);
                    if(qtmp < 0.1 || qtmp > 1){
                        throw new Exception("-quality");
                    }
                    quality = qtmp;
                    state = CmdParseState.DEFAULT;
                    break;
            }


            if (state == CmdParseState.INPUTFILE) {
                File inputFile = new File(arg);
                if (!inputFile.exists()) {
                    throw new FileNotFoundException("input file not found: " + inputFile.getPath());
                }
                //check if file is folder.

                if (inputFile.isDirectory()) {
                    Vector<String> exts = new Vector<String>();
                    exts.add("jpg");
                    exts.add("jpeg");
                    exts.add("JPG");
                    exts.add("JPEG");
                    FilenameFilter select = new FileListFilter(exts);
                    File[] files = inputFile.listFiles(select);
                    java.util.List fileList = java.util.Arrays.asList(files);
                    for (java.util.Iterator itr = fileList.iterator(); itr.hasNext();) {
                        File f = (File) itr.next();
                        inputFiles.add((File) f);
                    }
                } else {
                    inputFiles.add(inputFile);
                }

            }
        }
        if (inputFiles.size() == 0 && !(showHelp || showVersion)) {
            throw new Exception("No input files given");
        }

    }

    /**
     * Process the given image file, producing its Deep Zoom output files
     * in a subdirectory of the given output directory.
     * @param inFile the file containing the image
     * @param outputDir the output directory
     */
    private static void processImageFile(File inFile, File outputDir) throws IOException {
        if (verboseMode) {
            System.out.printf("Processing image file: %s\n", inFile);
        }

        String fileName = inFile.getName();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        //String pathWithoutExtension = outputDir + File.separator + nameWithoutExtension;

        BufferedImage equi = loadImage(inFile);

        int equiWidth = equi.getWidth();
        int equiHeight = equi.getHeight();

        if (equiWidth != equiHeight * 2) {
            if (verboseMode) {
                System.out.println("Image is not equirectangular (" + equiWidth + " x " + equiHeight + "), skipping...");
            }

            return;
        }

        int equiData[][] = new int[equiHeight][equiWidth];
        new ImageTo2DIntArrayExtractor(equiData, (Image) equi).doit();

        double fov; // horizontal field of view
        // peri = 2PIr
        double r = equiWidth / (2D * Math.PI);
        double y = (Math.tan(Math.PI / 4D) * r + overlap);
        fov =
                Math.atan(y / r) * 180 / Math.PI * 2;
        int rectWidth = (int) (y * 2);
        int rectHeight = rectWidth;

        Boolean bilinear;

        Boolean lanczos2;

        if (interpolation.equals(LANCZOS2)) {
            lanczos2 = true;
            bilinear = false;
        } else if (interpolation.equals(BILINEAR)) {
            lanczos2 = false;
            bilinear = true;
        } else if (interpolation.equals(NEAREST_NEIGHBOUR)) {
            lanczos2 = false;
            bilinear = false;
        } else {
            lanczos2 = true;
            bilinear = false;
        } // lanczos2 is default for junk values.

        int rectData[];
        double pitch;
        double yaw;
        BufferedImage output;

        Equi2Rect.initForIntArray2D(equiData);

        yaw = 0;
        pitch = 0;
        rectData =  Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output =  new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        //setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_f", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_f.jpg");
        }

        yaw = 90;
        pitch = 0;
        rectData =  Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_r", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_r.jpg");
        }

        yaw = 180;
        pitch = 0;
        rectData =  Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output =  new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_b", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_b.jpg");
        }

        yaw = 270;
        pitch = 0;
        rectData = Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_l", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_l.jpg");
        }

        yaw = 0;
        pitch = 90;
        rectData =  Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_u", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_u.jpg");
        }

        yaw = 0;
        pitch = -90;
        rectData = Equi2Rect.extractRectilinear(yaw, pitch, fov, equiData, rectWidth, rectHeight, equiWidth, bilinear, lanczos2);
        output = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        output.setRGB(0, 0, rectWidth, rectHeight, rectData, 0, rectWidth);
        saveImageAtQuality(output, outputDir + File.separator + nameWithoutExtension + "_d", quality);
        if (verboseMode) {
            System.out.println("created: " + outputDir + File.separator + nameWithoutExtension + "_d.jpg");
        }

    }

    /**
     * Creates a directory
     * @param parent the parent directory for the new directory
     * @param name the new directory name
     */
    private static File createDir(File parent, String name) throws IOException {
        assert (parent.isDirectory());
        File result = new File(parent + File.separator + name);
        if (result.exists()) {
            return result;
        }

        if (!result.mkdir()) {
            throw new IOException("Unable to create directory: " + result);
        }

        return result;
    }

    /**
     * Loads image from file
     * @param file the file containing the image
     */
    private static BufferedImage loadImage(File file) throws IOException {
        BufferedImage result = null;
        try {
            result = ImageIO.read(file);
        } catch (Exception e) {
            throw new IOException("Cannot read image file: " + file);
        }

        return result;
    }

    /**
     * Saves image to the given file
     * @param img the image to be saved
     * @param path the path of the file to which it is saved (less the extension)
     * @param quality the compression quality to use (0-1)
     */
    private static void saveImageAtQuality(BufferedImage img, String path, float quality) throws IOException {
        File outputFile = new File(path + "." + format);
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);
        FileImageOutputStream output = new FileImageOutputStream(outputFile);
        writer.setOutput(output);
        IIOImage image = new IIOImage(img, null, null);
        try {
            writer.write(null, image, iwp);
        } catch (IOException e) {
            throw new IOException("Unable to save image file: " + outputFile);
        }        
        writer.dispose();        
    }
}

class FileListFilter implements FilenameFilter {

    private Vector<String> extensions;

    public FileListFilter(Vector<String> extensions) {
        this.extensions = extensions;
    }

    public boolean accept(File directory, String filename) {
        if (extensions != null) {
            Iterator<String> itr = extensions.iterator();
            while (itr.hasNext()) {
                String ext = (String) itr.next();
                if (filename.endsWith('.' + ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
