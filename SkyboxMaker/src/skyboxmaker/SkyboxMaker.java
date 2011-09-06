package skyboxmaker;

import java.io.File;
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
import java.awt.Graphics;

public class SkyboxMaker {

    static final String help = "\nSkyboxMaker v1.0 \n\n Usage: \n\n"
            + "java [-java_options] -jar path/to/SkyboxMaker.jar [-options] [args...]\n"
            + "For a list of java options try: java -help or java -X for a list of less\n"
            + "common options. Loading large images for conversion takes a lot of RAM so\n"
            + "you will find the -Xmx option useful to raise Java's maximum heap size.\n"
            + "The -Xmx command is followed immediately by an integer specifying RAM size\n"
            + "and a unit indicator. For example, -Xmx1024m means to use 1024 megabytes.\n"
            + "If you see an error about heap size, then you will need to increase this \n"
            + "value.\n\n" + " Basic usage example for the jar file:\n\n"
            + "java -Xmx1024m -jar path/to/SkyboxMaker.jar path/to/directory/of/images/\n"
            + "This will generate a folder beside the input directory or file \n"
            + "with 'skybox_' prepended onto the name. So in the basic example above, \n"
            + "the output files would be in path/to/directory/of/tiles_images/.\n"
            + "\n"
            + " Options:\n\n"
            + "-quality: output JPEG compression. Value must be between 0.0 and 1.0.\n"
            + "\t0.0 is maximum compression, lowest quality, smallest file.\n"
            + "\t1.0 is least compression, highest quality, largest file.\n"
            + "\tDefault is 0.8.\n\n"
            + "-previewSize: width of low resolution skybox image outputed along\n"
            + "\twith main outputed image, with 'preview_' prefix.\n"
            + "\tDefault is 200.\n\n"
            + "-previewOnly: only preview image is outputed.\n\n"
            + "-outputdir or -o: the output directory for the converted images.\n"
            + "\tIt need not exist. Default is a folder next to the input folder\n"
            + "\tor file, with 'skybox_' prepended to the name of the input\n"
            + "\t(input files will have the extension removed). \n\n"
            + "-verbose or -v: makes the utility more 'chatty' during processing. \n\n"
            + "-debug: print various debugging messages during processing. \n\n"
            + " Arguments:\n\n"
            + "The arguments following any options are the input images or folders.\n"
            + "If there are multiple input folders or images, each should\n"
            + "be separated by a space. Input folders will not be NOT be recursed.\n"
            + "Only images immediately inside the folder will be processed.\n"
            + "All inputs will be processed into the one output directory,\n"
            + "so general usage is to process one folder containing multiples images\n"
            + "or to process one singe image file. Application has to be able \n"
            + "to recognize six cube walls, which need to be square, have exactly same\n"
            + "size and their naming has to follow one of common conventions.\n";

    private enum CmdParseState {

        DEFAULT, OUTPUTDIR, preview, QUALITY, PREVIEWSIZE, INPUTFILE
    }
    // The following can be overriden/set by the indicated command line arguments
    static boolean showHelp = false;               // -help | -h
    static float quality = 0.8f;	           // -quality (0.0 to 1.0)
    static int previewSize = 200;	           // -previewSize
    static boolean previewOnly = false;	           // -previewOnly
    static File outputDir = null;                  // -outputdir | -o
    static boolean verboseMode = false;            // -verbose
    static boolean debugMode = false;              // -debug
    static ArrayList<File> inputFiles = new ArrayList<File>(); // must follow all other args

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
                if (outputDir != null) {
                    if (!outputDir.exists() || !outputDir.isDirectory()) {
                        if (!outputDir.mkdir()) {
                            throw new IOException("Unable to create directory: " + outputDir);
                        }
                    }
                } else {
                    outputDir = inputFiles.get(0).getAbsoluteFile().getParentFile();
                }

                if (debugMode) {
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
            processImageFiles();
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
                    } else if (arg.equals("-previewOnly")) {
                        previewOnly = true;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                        debugMode = true;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-quality")) {
                        state = CmdParseState.QUALITY;
                    } else if (arg.equals("-previewSize")) {
                        state = CmdParseState.PREVIEWSIZE;
                    } else {
                        state = CmdParseState.INPUTFILE;
                    }
                    break;
                case OUTPUTDIR:
                    outputDir = (new File(args[count]));
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
                case PREVIEWSIZE:
                    int size = Integer.parseInt(args[count]);
                    if (size <= 0) {
                        throw new Exception("-previewSize");
                    }
                    previewSize = size;
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
        } else if (inputFiles.size() != 6) {
            throw new Exception("Wrong number of input files given (" + inputFiles.size() + ")");
        }
    }

    private static void processImageFiles() throws IOException {
        String nameWithoutDescription = inputFiles.get(0).getName().substring(0, inputFiles.get(0).getName().lastIndexOf("_"));
        BufferedImage input = loadImage(inputFiles.get(0));
        int inputSize = input.getWidth();

        BufferedImage output = new BufferedImage(inputSize, inputSize * 6, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = output.getGraphics();
        graphics.drawImage(input, 0, getImagePosition(inputFiles.get(0).getName()) * inputSize, null);
        for (int i = 1; i < 6; i++) {
            input = loadImage(inputFiles.get(i));
            graphics.drawImage(input, 0, getImagePosition(inputFiles.get(i).getName()) * inputSize, null);
        }
        if (!previewOnly) {
            saveImageAtQuality(output, outputDir.getAbsolutePath() + File.separator + nameWithoutDescription, quality);
        }

        output = resizeImage(output, previewSize, previewSize * 6);
        saveImageAtQuality(output, outputDir.getAbsolutePath() + File.separator + "preview_" + nameWithoutDescription, quality);

        graphics.dispose();
    }

    private static int getImagePosition(String name) {
        String nameWithoutExtension = name.substring(name.lastIndexOf(File.separator) + 1, name.lastIndexOf('.'));
        if (nameWithoutExtension.matches("^.+(_f|_0|_11|_front)$")) {
            return 0;
        }
        if (nameWithoutExtension.matches("^.+(_r|_1|_22|_right)$")){
            return 1;
        }
        if (nameWithoutExtension.matches("^.+(_b|_2|_33|_back)$")){
            return 2;
        }
        if (nameWithoutExtension.matches("^.+(_l|_3|_44|_left)$")){
            return 3;
        }
        if (nameWithoutExtension.matches("^.+(_u|_4|_55|_up)$")){
            return 4;
        }
        if (nameWithoutExtension.matches("^.+(_d|_5|_66|_down)$")){
            return 5;
        }
        throw new IllegalArgumentException("Cannot resolve name: " + name);
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
}

class FileListFilter implements FilenameFilter {

    private ArrayList<String> extensions;

    public FileListFilter(ArrayList<String> extensions) {
        this.extensions = extensions;
    }

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
