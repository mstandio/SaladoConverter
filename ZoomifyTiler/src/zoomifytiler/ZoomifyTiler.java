package zoomifytiler;

/*
Copyright 2011 Marek Standio
This file is part of ZoomifyTiler.java.
ZoomifyTiler is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with EquirectangulartoCubic. If not, see http://www.gnu.org/licenses/.
This code is modified from DeepJZoom, courtesy of Glenn Lawrence, which is also licensed under the GPL. Thank you!
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.imageio.*;
import javax.imageio.stream.*;
import com.sun.media.jai.codec.FileSeekableStream;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * @author Glenn Lawrence
 */
public class ZoomifyTiler {

    static final String help = "\nZoomifyTiler v1.0 \n\n Usage: \n\n" +
            "java [-java_options] -jar path/to/ZoomifyTiler.jar [-options] [args...]\n" +
            "For a list of java options try: java -help or java -X for a list of less\n" +
            "common options. Loading large images for conversion takes a lot of RAM so\n" +
            "you will find the -Xmx option useful to raise Java's maximum heap size.\n" +
            "The -Xmx command is followed immediately by an integer specifying RAM size\n" +
            "and a unit indicator. For example, -Xmx1024m means to use 1024 megabytes.\n" +
            "If you see an error about heap size, then you will need to increase this \n" +
            "value.\n\n" + " Basic usage example for the jar file:\n\n" +
            "java -Xmx1024m -jar path/to/ZoomifyTiler.jar path/to/directory/of/images/\n" +
            "This will generate a folder of tiles beside the input directory or file \n" +
            "with 'tiles_' prepended onto the name. So in the basic example above, \n" +
            "the output files would be in path/to/directory/of/tiles_images/.\n" +
            "\n" +
            " Options:\n\n" +
            "-quality: output JPEG compression. Value must be between 0.0 and 1.0.\n" +
            "\t0.0 is maximum compression, lowest quality, smallest file.\n" +
            "\t1.0 is least compression, highest quality, tlargest file.\n" +
            "\tDefault is 0.8.\n\n" +
            "-tilesize: target pixel tile size. Tiling starts at the top left \n" +
            "\tof an image, so tiles at the right and bottom to the image\n" +
            "\tmay not be this width or height, respectively, unless the\n" +
            "\tinput image's size is divisible by the tileSize. The tileSize\n" +
            "\tdoes NOT include the overlap. Overlap pixels are add to the\n" +
            "\tdimensions of the tile.\n" +
            "\tDefault is 512.\n\n" +
            "-outputdir or -o: the output directory for the converted images. It\n" +
            "\tneed not exist. Default is a folder next to the input folder\n" +
            "\tor file, with 'tiles_' prepended to the name of the input\n" +
            "\t(input files will have the extension removed). \n\n" +
            "-verbose or -v: makes the utility more 'chatty' during processing. \n\n" +
            "-debug: print various debugging messages during processing. \n\n" +
            " Arguments:\n\n" + "The arguments following any options are the input images or folders.\n" +
            "(or both) If there are multiple input folders or images, each should\n" +
            "be separated by a space. Input folders will not be NOT be recursed.\n" +
            "Only images immediately inside the folder will be processed.\n" +
            "All inputs will be processed into the one output directory,\n" +
            "so general usage is to process one folder containing multiples images\n" +
            "or to process one singe image file. \n";

    private enum CmdParseState {

        DEFAULT, OUTPUTDIR, TILESIZE, QUALITY, INPUTFILE
    }
    // The following can be overriden/set by the indicated command line arguments
    static boolean showHelp = false;               // -help | -h
    static int tileSize = 512;                     // -tilesize
    static float quality = 0.8f;	           // -quality (0.0 to 1.0)
    static File outputDir = null;                  // -outputdir | -o
    static boolean verboseMode = false;            // -verbose
    static boolean debugMode = false;              // -debug
    static ArrayList<File> inputFiles = new ArrayList<File>(); // must follow all other args
    static ArrayList<File> outputFiles = new ArrayList<File>();

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
                //when output file is given, output are folders with names of input file in that directory
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

                        File outputFile = createDir(outputDir, "tiles_" + nameWithoutExtension);
                        outputFiles.add(outputFile);
                    }
                }

                // default location for output files is folder beside input files with the name of the input file
                if (outputFiles.isEmpty()) {
                    Iterator<File> itr = inputFiles.iterator();
                    while (itr.hasNext()) {
                        File inputFile = itr.next();
                        File parentFile = inputFile.getAbsoluteFile().getParentFile();
                        String fileName = inputFile.getName();
                        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

                        File outputFile = createDir(parentFile, "tiles_" + nameWithoutExtension);
                        outputFiles.add(outputFile);
                    }
                }

                if (debugMode) {
                    System.out.printf("tileSize=%d ", tileSize);
                    System.out.printf("quality=%.2f ", quality);
                }
            } catch (Exception e) {
                System.out.println("Invalid command: " + e.getMessage());
                System.out.println("For avaible options type: -help or -h");
                return;
            }
            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
            // it probably can be problematic in non-admin accounts
            // java -D com.sun.media.jai.disableMediaLib=true YourApp
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
                    if (arg.equals("-h") || arg.equals("-help")) {
                        showHelp = true;
                        return;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                        debugMode = true;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-tilesize")) {
                        state = CmdParseState.TILESIZE;
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
                case TILESIZE:
                    tileSize = Integer.parseInt(args[count]);
                    state = CmdParseState.DEFAULT;
                    break;
                case QUALITY:
                    float qtmp = Float.parseFloat(args[count]);
                    if (qtmp < 0 || qtmp > 1) {
                        throw new Exception("-quality");
                    }
                    quality = qtmp;
                    state = CmdParseState.DEFAULT;
                    break;
            }
            if (state == CmdParseState.INPUTFILE) {
                File inputFile = new File(arg);
                if (!inputFile.exists()) {
                    throw new FileNotFoundException("Missing input: " + inputFile.getPath());
                }
                ArrayList<String> exts = new ArrayList<String>();
                exts.add("bmp");
                exts.add("jpg");
                exts.add("jpeg");
                exts.add("png");
                exts.add("gif");
                exts.add("tif");
                exts.add("tiff");
                if (inputFile.isDirectory()) {
                    FilenameFilter select = new FileListFilter(exts);
                    File[] files = inputFile.listFiles(select);
                    java.util.List fileList = java.util.Arrays.asList(files);
                    for (java.util.Iterator itr = fileList.iterator(); itr.hasNext();) {
                        File f = (File) itr.next();
                        inputFiles.add((File) f);
                    }
                } else {
                    String fExt = inputFile.getAbsolutePath().substring(inputFile.getAbsolutePath().lastIndexOf(".") + 1);
                    for (String ext : exts) {
                        if (ext.equals(fExt)) {
                            inputFiles.add(inputFile);
                            break;
                        }
                    }
                }
            }
        }
        if (inputFiles.isEmpty()) {
            throw new Exception("No input files given.");
        }
    }

    private static void processImageFile(File inFile, File outputDir) throws IOException {
        if (verboseMode) {
            System.out.printf("Processing image: %s\n", inFile);
        }

        BufferedImage image = loadImage(inFile);

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        int numTiers = numTiers(originalWidth, originalHeight);

        if (verboseMode) {
            System.out.printf("nTiers=%d\n", numTiers);
        }

        // Delete any existing output files and folders for this image
        File descriptor = new File(outputDir + File.separator + "ImageProperties.xml");
        if (descriptor.exists()) {
            if (verboseMode) {
                System.out.printf("Deleting descriptor: %s\n", descriptor);
            }
            deleteFile(descriptor);
        }

        File imgDir = null;
        for(int i=0; i<100; i++){
            imgDir = new File(outputDir + File.separator + "TileGroup"+i);
            if (imgDir.exists()) {
                if (verboseMode) {
                    System.out.printf("Deleting directory: %s\n", imgDir);
                }
                deleteDir(imgDir);
            }
        }

        imgDir = outputDir;

        System.out.printf("Writing into directory: %s\n", imgDir);

        double width = originalWidth;
        double height = originalHeight;

        final int totalTiles = numTilesTotal(originalWidth, originalHeight);
        int currentTileGroup = (int)Math.ceil((double)totalTiles/256d)-1;
        int totalTilesCounter = totalTiles;
        File dir = createDir(imgDir, "TileGroup"+Integer.toString(currentTileGroup));
        for (int currentTier = numTiers; currentTier >= 0; currentTier--) {
            int nCols = (int) Math.ceil(width / tileSize);
            int nRows = (int) Math.ceil(height / tileSize);
            if (debugMode) {
                System.out.printf("level=%d \t w/h=%.0f/%.0f \t cols/rows=%d/%d\n",
                        numTiers, width, height, nCols, nRows);
            }            
            for (int row = nRows-1; row >= 0; row--){
                for (int col = nCols-1; col >= 0; col--) {
                    saveImageAtQuality(getTile(image, row, col), dir + File.separator + currentTier + '-' +  col + '-' + row, quality);
                    totalTilesCounter--;
                    if(totalTilesCounter > 0 && totalTilesCounter % 256 == 0){
                        currentTileGroup--;
                        dir = createDir(imgDir, "TileGroup"+Integer.toString(currentTileGroup));
                    }
                }
            }

            // Scale down image for next level
            width = Math.floor(width / 2d);
            height = Math.floor(height / 2d);
            if (width > 10 && height > 10) {
                // resize in stages to improve quality
                image = resizeImage(image, width * 1.66d, height * 1.66d);
                image = resizeImage(image, width * 1.33d, height * 1.33d);
            }
            image = resizeImage(image, width, height);
        }

        saveImageDescriptor(originalWidth, originalHeight, totalTiles, descriptor);
    }

    private static int numTilesTotal(int originalWidth, int originalHeight) {
        int result = 0;
        int numTiers = numTiers(originalWidth, originalHeight);
        for(int i=numTiers; i>=0; i--){
            result += numTilesFromTier(originalWidth, originalHeight, i);
        }
        return result;
    }

    private static int numTilesFromTier(int originalWidth, int originalHeight, int tier) {
        Point2D sizeAtTier = sizeAtTier(originalWidth, originalHeight, tier);
        return ((int) Math.ceil(sizeAtTier.getX() / tileSize) * (int) Math.ceil(sizeAtTier.getY() / tileSize));
    }

    private static Point2D sizeAtTier(int originalWidth, int originalHeight, int tier) {
        double width = originalWidth;
        double height = originalHeight;
        int tiers = numTiers(originalWidth, originalHeight);
        for (int i = 0; i < (tiers - tier); i++) {
            width = Math.ceil(width / 2d);
            height = Math.ceil(height / 2d);
        }
        return new Point((int) width, (int) height);
    }

    /**
     * follows convention when "0" means 1 tier(0)
     * "1" means 2 tiers(0 and 1) and so on
     */
    private static int numTiers(int originalWidth, int originalHeight) {
        int result = 0;
        double size = Math.max(originalWidth, originalHeight);
        while (size > tileSize) {
            size = Math.ceil(size / 2d);
            result++;
        }
        return result;
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

    /**
     * Gets an image containing the tile at the given row and column
     * for the given image.
     * @param img - the input image from whihc the tile is taken
     * @param row - the tile's row (i.e. y) index
     * @param col - the tile's column (i.e. x) index
     */
    private static BufferedImage getTile(BufferedImage img, int row, int col) {
        int x = col * tileSize;
        int y = row * tileSize;
        int w = tileSize;
        int h = tileSize;
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
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
        //surprisingly this gives worse results
        //RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //BufferedImage result = JAI.create("SubsampleAverage", img, width / (double) img.getWidth(), height / (double) img.getHeight(), qualityHints).getAsBufferedImage();
        return result;
    }

    /**
     * Saves image to the given file
     * @param img the image to be saved
     * @param path the path of the file to which it is saved (less the extension)
     * @param quality the compression quality to use (0-1)
     */
    private static void saveImageAtQuality(BufferedImage img, String path, float quality) throws IOException {
        File outputFile = new File(path + ".jpg");
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
        } finally {
            if (writer != null) {
                writer.dispose();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * Write image descriptor XML file
     * @param width image width
     * @param height image height
     * @param file the file to which it is saved
     */
    private static void saveImageDescriptor(int width, int height, int numTiles, File file) throws IOException {
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(xmlHeader);
        lines.add("<IMAGE_PROPERTIES WIDTH=\"" + width + "\" HEIGHT=\"" + height + "\" NUMTILES=\"" + numTiles + "\" NUMIMAGES=\"1\" VERSION=\"1.8\" TILESIZE=\"" + tileSize + "\"/>");
        saveText(lines, file);
    }

    /**
     * Saves strings as text to the given file
     * @param lines the image to be saved
     * @param file the file to which it is saved
     */
    private static void saveText(ArrayList lines, File file) throws IOException {
        if (verboseMode) {
            System.out.printf("Writing file: %s\n", file);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            for (int i = 0; i < lines.size(); i++) {
                ps.println((String) lines.get(i));
            }
        } catch (IOException e) {
            throw new IOException("Unable to write to text file: " + file);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}

class FileListFilter implements FilenameFilter {

    private ArrayList<String> extensions;

    public FileListFilter(ArrayList<String> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean accept(File directory, String filename) {
        if (extensions != null) {
            Iterator<String> itr = extensions.iterator();
            while (itr.hasNext()) {
                String ext = (String) itr.next();
                if (filename.toLowerCase().endsWith('.' + ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
