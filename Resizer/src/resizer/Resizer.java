package resizer;

import com.sun.media.jai.codec.FileSeekableStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.InterpolationNearest;

/** *
 * @author Marek Standio
 */
public class Resizer {

    static final String help = "\nResizer v1.1 \n\n Usage: \n\n"
            + "java [-java_options] -jar path/to/Resizer.jar [-options] [args...]\n\n"
            + "For a list of java options try: java -help or java -X for a list of less\n"
            + "common options. Loading large images for conversion takes a lot of RAM,\n"
            + "so you will find the -Xmx option useful to raise Java's maximum heap size.\n"
            + "The -Xmx command is followed immediately by an integer specifying RAM size\n"
            + "and a unit indicator. For example: -Xmx1024m means to use 1024 megabytes.\n"
            + "If you see an heap size error, then you will need to increase this value.\n\n"
            + " Basic usage example for the jar file:\n\n"
            + "java -Xmx1024m -jar path/to/Resizer.jar -width 300 path/to/directory/of/images/\n\n"
            + "This will generate a folder of resized images beside the input directory\n"
            + "or file with 'resized_' prepended onto the name. So in the basic example\n"
            + "above, the output files would be in path/to/directory/of/resized_images/.\n"
            + "\n"
            + " Options:\n\n"
            + "-width: width of output image. If only width is set, aspect ratio\n"
            + "\tof result image will be preserved.\n"
            + "\tDefault is 0\n\n"
            + "-height: height of output image. If only height is set, aspect ratio\n"
            + "\tof result image will be preserved.\n\n"
            + "-quality: output JPEG compression. Value must be between 0.0 and 1.0.\n"
            + "\t0.0 is maximum compression, lowest quality, smallest file.\n"
            + "\t1.0 is least compression, highest quality, largest file.\n"
            + "-outputformat: output format of converted images. Possible values\n"
            + "\tare: tif, jpg. Default is tif. \n\n"
            + "-outputdir or -o: the output directory for the converted images. It\n"
            + "\tneed not exist. Default is a folder next to the input folder\n"
            + "\tor file, with 'resized_' prepended to the name of the input.\n\n"
            + "-simpleoutput or -s: '_resized' parent directory for output files is not\n"
            + "\tcreated. Resized files are saved directly into output folder.\n\n"
            + " Arguments:\n\n"
            + "The argument following any options is input image or folder that contains\n"
            + "images. Input folder will not be NOT be recursed. Only images immediately\n"
            + "inside the folder will be processed.\n";
    static final String FORMAT_TIF = "tif";
    static final String FORMAT_JPG = "jpg";

    private enum CmdParseState {

        DEFAULT, OUTPUTDIR, INPUTFILE, WIDTH, HEIGHT, QUALITY, OUTPUTFORMAT
    }
    // The following can be overriden/set by the indicated command line arguments    
    static boolean showHelp = false;              // -help | -h
    static int width = 0;                         // -width
    static int height = 0;                        // -height
    static float quality = 0.8f;	          // -quality (0.0 to 1.0)
    static String outputFormat = FORMAT_TIF;      // -outputformat (tif or jpg)
    static File outputDir = null;                 // -outputdir | -o
    static boolean simpleOutput = false;          // -simpleoutput | -s
    static ArrayList<File> inputFiles = new ArrayList<File>();  // must follow all other args    

    public static void main(String[] args) {
        try {
            try {
                parseCommandLine(args);
                if (showHelp) {
                    System.out.println(help);
                    return;
                }
                File inputFile = inputFiles.get(0);
                File parentFolder = inputFile.getAbsoluteFile().getParentFile();
                if (outputDir != null) {
                    if (!outputDir.exists() || !outputDir.isDirectory()) {
                        if (!outputDir.mkdir()) {
                            throw new IOException("Unable to create directory: " + outputDir);
                        }
                    }
                    if (!simpleOutput) {
                        outputDir = createDir(outputDir.getAbsoluteFile(), "resized_" + parentFolder.getName());
                    }
                } else {
                    if (simpleOutput) {
                        outputDir = parentFolder.getParentFile();
                    } else {
                        outputDir = createDir(parentFolder.getParentFile(), "resized_" + parentFolder.getName());
                    }
                }

            } catch (Exception e) {
                System.out.println("Invalid command: " + e.getMessage());
                System.out.println("type -h to get list of supported commands");
                return;
            }
            System.setProperty("com.sun.media.jai.disableMediaLib", "true");
            // can be problematic in non-admin accounts
            // java -Dcom.sun.media.jai.disableMediaLib=true YourApp
            for (int i = 0; i < inputFiles.size(); i++) {
                processImageFile(inputFiles.get(i), outputDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                    } else if (arg.equals("-outputformat")) {
                        state = CmdParseState.OUTPUTFORMAT;
                    } else if (arg.equals("-quality")) {
                        state = CmdParseState.QUALITY;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-width")) {
                        state = CmdParseState.WIDTH;
                    } else if (arg.equals("-height")) {
                        state = CmdParseState.HEIGHT;
                    } else {
                        state = CmdParseState.INPUTFILE;
                    }
                    break;
                case WIDTH:
                    int wtmp = Integer.parseInt(args[count]);
                    if (wtmp <= 0) {
                        throw new Exception("-width");
                    }
                    width = wtmp;
                    state = CmdParseState.DEFAULT;
                    break;
                case HEIGHT:
                    int htmp = Integer.parseInt(args[count]);
                    if (htmp <= 0) {
                        throw new Exception("-height");
                    }
                    height = htmp;
                    state = CmdParseState.DEFAULT;
                    break;
                case OUTPUTDIR:
                    outputDir = new File(args[count]);
                    state = CmdParseState.DEFAULT;
                    break;
                case OUTPUTFORMAT:
                    if (args[count].toLowerCase().equals(FORMAT_JPG) || args[count].toLowerCase().equals(FORMAT_TIF)) {
                        outputFormat = args[count].toLowerCase();
                    }
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
                    throw new FileNotFoundException("Missing input file: " + inputFile.getPath());
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
                        inputFiles.add((File) itr.next());
                    }
                } else {
                    String fExt = inputFile.getAbsolutePath().substring(inputFile.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
                    for (String ext : exts) {
                        if (ext.equals(fExt)) {
                            inputFiles.add(inputFile);
                            break;
                        }
                    }
                }
                break;
            }
        }
        if (inputFiles.isEmpty() && showHelp) {
            throw new Exception("No input files given");
        }
        if (width == 0 && height == 0) {
            throw new Exception("No resize dimensions given");
        }
    }

    private static void processImageFile(File inFile, File outputDir) throws IOException {

        FileSeekableStream stream = null;

        try {

            double resultWidth = width;
            double resultHeight = height;

            stream = new FileSeekableStream(inFile);
            PlanarImage planarImage = JAI.create("stream", stream);
            if (width == 0) {
                resultWidth = Math.floor((double) (height * planarImage.getWidth()) / (double) planarImage.getHeight());
            } else if (height == 0) {
                resultHeight = Math.floor((double) (width * planarImage.getHeight()) / (double) planarImage.getWidth());
            }

            ParameterBlock paramBlock = new ParameterBlock();
            paramBlock.addSource(planarImage); // The source image
            paramBlock.add((float) resultWidth / (float) planarImage.getWidth()); // The xScale
            paramBlock.add((float) resultHeight / (float) planarImage.getHeight()); // The yScale
            paramBlock.add(0.0f); // The x translation
            paramBlock.add(0.0f); // The y translation
            paramBlock.add(new InterpolationNearest());
            planarImage = JAI.create("scale", paramBlock);

            String outputFileName = outputDir.getAbsolutePath()
                    + File.separator
                    + inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));

            System.out.println("Resizing image to: " + outputDir.getAbsolutePath());

            if (outputFormat.equals(FORMAT_TIF)) {
                JAI.create("filestore", planarImage, outputFileName + ".tif", "TIFF");
            } else if (outputFormat.equals(FORMAT_JPG)) {
                saveImageAtQuality(planarImage.getAsBufferedImage(), outputFileName, quality);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Cannot read image file: " + inFile);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

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

    private static File createDir(File parent, String name) throws IOException {
        assert (parent.isDirectory());
        File result = new File(parent + File.separator + name);
        if (!(result.exists() || result.mkdir())) {
            throw new IOException("Unable to create directory: " + result);
        }
        return result;
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
