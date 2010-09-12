/*
Copyright 2009 Zephyr Renner
This file is part of EquirectangulartoCubic.java.
EquirectangulartoCubic is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
EquirectangulartoCubic is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with EquirectangulartoCubic. If not, see http://www.gnu.org/licenses/.

This code is modified from DeepJZoom, courtesy of Glenn Lawrence, which is also licensed under the GPL.  Thank you!
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.Vector;
import java.util.Iterator;

/**
 *
 * @author Glenn Lawrence
 */
public class DeepZoomTiler {

    static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    static final String schemaName = "http://schemas.microsoft.com/deepzoom/2009";
    static final String version = "\nDeepZoomTiler v1.2";
    static final String help =
            "\n\tApplication converts *.jpg images into DeepZoom format\n\n" +
            "\tInput (file or directory) must follow all other arguments\n\n" +
            "\t-h | -help | -? \n\t\t show help and exit \n\n" +
            "\t-v | version \n\t\t show version and exit \n\n" +
            "\t-tilesize \n\t\t maximum size of tiles, default:256\n\n" +
            "\t-overlap \n\t\t overcome display bug, default:1\n\n" +
            "\t-quality \n\t\t quality of jpg files (0.1 to 1.0), default:0.8\n\n" +
            "\t-outputdir | -o \n\t\t output directory, default:parent directory\n\n" +
            "\t-simpleoutput | -s \n\t\t don't create output subdirectories\n\n" +
            "\t-delsrc \n\t\t delete original files\n\n" +
            "\t-verbose \n\t\t enable verbose output \n\n" +
            "\t-debug \n\t\t show detailed debug information\n\n";
    
    private enum CmdParseState {

        DEFAULT, OUTPUTDIR, TILESIZE, OVERLAP, QUALITY, INPUTFILE
    };
    static Boolean deleteExisting = true;
    static String tileFormat = "jpg";
    // The following can be overriden/set by the indicated command line arguments
    static boolean showVersion = false;            // -v | -version
    static boolean showHelp = false;               // -h | -help | -?
    static int tileSize = 256;                     // -tilesize
    static int tileOverlap = 1;                    // -overlap
    static float quality = 0.8f;	           // -quality (0.1 to 1.0)
    static File outputDir = null;                  // -outputdir | -o
    static boolean createOutputSubDirs = true;     // -simpleoutput | -s
    static boolean deleteOriginalFiles = false;    // -delsrc
    static boolean verboseMode = false;            // -verbose
    static boolean debugMode = false;              // -debug
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

                Iterator<File> itr = inputFiles.iterator();

                while (itr.hasNext()) {
                    File inputFile = itr.next();
                    File outputFile;
                    String fileName = inputFile.getName();
                    String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                    if (outputDir == null) {
                        File parentFile = inputFile.getAbsoluteFile().getParentFile();
                        if (createOutputSubDirs){
                            outputFile = createDir(parentFile, "tiles_" + nameWithoutExtension);
                            outputFiles.add(outputFile);
                        }else{
                            outputFiles.add(parentFile);
                        }
                    } else {
                        if (createOutputSubDirs){
                            outputFile = createDir(outputDir, "tiles_" + nameWithoutExtension);
                        }else{
                            outputFiles.add(outputDir);
                        }
                    }
                }

                if (debugMode) {
                    System.out.printf("tileSize=%d ", tileSize);
                    System.out.printf("tileOverlap=%d ", tileOverlap);
                    System.out.printf("quality=%.2f ", quality);
                    if (outputDir != null) {
                        System.out.printf("outputDir=%s", outputDir.getPath());
                    }
                    System.out.print("\n");
                }
            } catch (Exception e) {
                System.out.println("Invalid command line: " + e.getMessage());
                return;
            }
            if(!verboseMode){
                System.out.println("Processing images...");
            }
            for (int i = 0; i < inputFiles.size(); i++) {
                processImageFile(inputFiles.get(i), outputFiles.get(i));
            }
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
                    if (arg.equals("-v") || arg.equals("-version")) {
                        showVersion = true;
                    } else if (arg.equals("-h") || arg.equals("-help") || arg.equals("-?")) {
                        showHelp = true;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                        debugMode = true;
                    } else if (arg.equals("-simpleoutput") || arg.equals("-s")) {
                        createOutputSubDirs = false;
                    } else if (arg.equals("-delsrc")) {
                        deleteOriginalFiles = true;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-tilesize")) {
                        state = CmdParseState.TILESIZE;
                    } else if (arg.equals("-overlap")) {
                        state = CmdParseState.OVERLAP;
                    } else if (arg.equals("-quality")) {
                        state = CmdParseState.QUALITY;
                    } else {
                        state = CmdParseState.INPUTFILE;
                    }
                    break;
                case OUTPUTDIR:
                    outputDir = new File(args[count]);

                    if (!(outputDir.exists() || outputDir.mkdir())) {
                        outputDir = null;
                        throw new Exception("-outputdir");
                    }
                    state = CmdParseState.DEFAULT;
                    break;
                case TILESIZE:
                    tileSize = Integer.parseInt(args[count]);
                    state = CmdParseState.DEFAULT;
                    break;
                case OVERLAP:
                    tileOverlap = Integer.parseInt(args[count]);
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
                    throw new FileNotFoundException("Missing input file: " + inputFile.getPath());
                }
                //check if file is folder.
                if (inputFile.isDirectory()) {
                    //findImagesRecurisve(inputFile);
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
/*
    private static void findImagesRecurisve(File parent) {
        if (parent.isDirectory()) {
            Vector<String> exts = new Vector<String>();
            exts.add("jpg");
            exts.add("jpeg");
            exts.add("JPG");
            exts.add("JPEG");
            FileListFilter fileFilter = new FileListFilter(exts);
            File[] files = parent.listFiles(fileFilter);
            java.util.List fileList = java.util.Arrays.asList(files);
            for (java.util.Iterator itr = fileList.iterator(); itr.hasNext();) {
                File f = (File) itr.next();
                inputFiles.add((File) f);
            }

            DirectoryFilter dirFilter = new DirectoryFilter();
            files = parent.listFiles(dirFilter);
            fileList = java.util.Arrays.asList(files);
            for (java.util.Iterator itr = fileList.iterator(); itr.hasNext();) {
                File f = (File) itr.next();
                findImagesRecurisve(f);
            }
        }
    }
*/
    /**
     * Process the given image file, producing its Deep Zoom output files
     * in a subdirectory of the given output directory.
     * @param inFile the file containing the image
     * @param outputDir the output directory
     */
    private static void processImageFile(File inFile, File outputDir) throws IOException {
        if (verboseMode) {
            System.out.printf("Processing image: %s\n", inFile);
        }

        String fileName = inFile.getName();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        String pathWithoutExtension = outputDir + File.separator + nameWithoutExtension;

        BufferedImage image = loadImage(inFile);

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        double maxDim = Math.max(originalWidth, originalHeight);

        int nLevels = (int) Math.ceil(Math.log(maxDim) / Math.log(2));

        if (verboseMode) {
            System.out.printf("nLevels=%d\n", nLevels);
        }

        // Delete any existing output files and folders for this image

        File descriptor = new File(pathWithoutExtension + ".xml");
        if (descriptor.exists()) {
            if (deleteExisting) {
                if (verboseMode) {
                    System.out.printf("Deleting descriptor: %s\n", descriptor);
                }
                deleteFile(descriptor);
            } else {
                throw new IOException("File already exists in output dir: " + descriptor);
            }
        }

        File imgDir = new File(pathWithoutExtension);
        if (imgDir.exists()) {
            if (deleteExisting) {
                if (verboseMode) {
                    System.out.printf("Deleting directory: %s\n", imgDir);
                }
                deleteDir(imgDir);
            } else {
                throw new IOException("Image directory already exists in output dir: " + imgDir);
            }
        }

        imgDir = createDir(outputDir, nameWithoutExtension);

        System.out.printf("Writing into directory: %s\n", imgDir);

        double width = originalWidth;
        double height = originalHeight;

        for (int level = nLevels; level >= 0; level--) {
            int nCols = (int) Math.ceil(width / tileSize);
            int nRows = (int) Math.ceil(height / tileSize);
            if (debugMode) {
                System.out.printf("level=%d \t w/h=%.0f/%.0f \t cols/rows=%d/%d\n",
                        level, width, height, nCols, nRows);
            }

            File dir = createDir(imgDir, Integer.toString(level));
            for (int col = 0; col < nCols; col++) {
                for (int row = 0; row < nRows; row++) {
                    BufferedImage tile = getTile(image, row, col);
                    saveImageAtQuality(tile, dir + File.separator + col + '_' + row, quality);
                }
            }

            // Scale down image for next level
            width = Math.ceil(width / 2);
            height = Math.ceil(height / 2);
            if (width > 10 && height > 10) {
                // resize in stages to improve quality
                image = resizeImage(image, width * 1.66, height * 1.66);
                image = resizeImage(image, width * 1.33, height * 1.33);
            }
            image = resizeImage(image, width, height);
        }

        if(deleteOriginalFiles){
            try{
                if (verboseMode) {
                    System.out.printf("Deleting file: %s\n", inFile);
                }
                deleteFile(inFile);
            }catch(IOException ex){
                if (verboseMode) {
                    System.out.printf("Failed to delete file: %s\n", inFile);
                }
            }
        }

        saveImageDescriptor(originalWidth, originalHeight, descriptor);
    }

    /**
     * Delete a file
     * @param path the path of the directory to be deleted
     */
    private static void deleteFile(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    /**
     * Recursively deletes a directory
     * @param path the path of the directory to be deleted
     */
    private static void deleteDir(File dir) throws IOException {
        if (!dir.isDirectory()) {
            deleteFile(dir);
        } else {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    deleteFile(file);
                }
            }
            if (!dir.delete()) {
                throw new IOException("Failed to delete directory: " + dir);
            }
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
        if (!(result.exists() || result.mkdir())) {
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
     * Gets an image containing the tile at the given row and column
     * for the given image.
     * @param img - the input image from whihc the tile is taken
     * @param row - the tile's row (i.e. y) index
     * @param col - the tile's column (i.e. x) index
     */
    private static BufferedImage getTile(BufferedImage img, int row, int col) {
        int x = col * tileSize - (col == 0 ? 0 : tileOverlap);
        int y = row * tileSize - (row == 0 ? 0 : tileOverlap);
        int w = tileSize + (col == 0 ? 1 : 2) * tileOverlap;
        int h = tileSize + (row == 0 ? 1 : 2) * tileOverlap;

        if (x + w > img.getWidth()) {
            w = img.getWidth() - x;
        }
        if (y + h > img.getHeight()) {
            h = img.getHeight() - y;
        }

        if (debugMode) {
            System.out.printf("getTile: row=%d, col=%d, x=%d, y=%d, w=%d, h=%d\n",
                    row, col, x, y, w, h);
        }

        assert (w > 0);
        assert (h > 0);

        BufferedImage result = new BufferedImage(w, h, img.getType());
        Graphics2D g = result.createGraphics();
        g.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, null);

        return result;
    }

    /**
     * Returns resized image
     * NB - useful reference on high quality image resizing can be found here:
     *   http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     * @param width the required width
     * @param height the frequired height
     * @param img the image to be resized
     */
    private static BufferedImage resizeImage(BufferedImage img, double width, double height) {
        int w = (int) width;
        int h = (int) height;
        BufferedImage result = new BufferedImage(w, h, img.getType());
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
        return result;
    }

    /**
     * Saves image to the given file
     * @param img the image to be saved
     * @param path the path of the file to which it is saved (less the extension)
     * @param quality the compression quality to use (0-1)
     */
    private static void saveImageAtQuality(BufferedImage img, String path, float quality) throws IOException {
        File outputFile = new File(path + "." + tileFormat);
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

    /**
     * Write image descriptor XML file
     * @param width image width
     * @param height image height
     * @param file the file to which it is saved
     */
    private static void saveImageDescriptor(int width, int height, File file) throws IOException {
        Vector<String> lines = new Vector<String>();
        lines.add(xmlHeader);
        lines.add("<Image TileSize=\"" + tileSize + "\" Overlap=\"" + tileOverlap +
                "\" Format=\"" + tileFormat + "\" ServerFormat=\"Default\" xmnls=\"" +
                schemaName + "\">");
        lines.add("<Size Width=\"" + width + "\" Height=\"" + height + "\" />");
        lines.add("</Image>");
        saveText(lines, file);
    }

    /**
     * Saves strings as text to the given file
     * @param lines the image to be saved
     * @param file the file to which it is saved
     */
    private static void saveText(Vector lines, File file) throws IOException {
        if(verboseMode){
            System.out.printf("Writing file: %s\n", file);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            for (int i = 0; i < lines.size(); i++) {
                ps.println((String) lines.elementAt(i));
            }
        } catch (IOException e) {
            throw new IOException("Unable to write to text file: " + file);
        }
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

class DirectoryFilter implements FilenameFilter {

    public DirectoryFilter() {
    }

    public boolean accept(File directory, String filename) {
        if (directory.isDirectory()) {
            return true;
        }
        return false;
    }
}