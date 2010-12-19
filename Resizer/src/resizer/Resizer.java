package resizer;

import com.sun.media.jai.codec.FileSeekableStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.InterpolationNearest;

/** *
 * @author Marek Standio
 */
public class Resizer {

    
    static final String help = "\nResizer v0.1 \n\n Usage: \n\n" + "java [-java_options] -jar path/to/Resizer.jar [-options] [args...]\n" + "For a list of java options try: java -help or java -X for a list of less\n" + "common options. Loading large images for conversion takes a lot of RAM so\n" + "you will find the -Xmx option useful to raise Java's maximum heap size.\n" + "The -Xmx command is followed immediately by an integer specifying RAM size\n" + "and a unit indicator. For example, -Xmx1024m means to use 1024 megabytes.\n" + "If you see an error about heap size, then you will need to increase this \n" + "value.\n\n" + " Basic usage example for the jar file:\n\n" + "java -Xmx1024m -jar path/to/Resizer.jar path/to/directory/of/images/\n" + "This will generate a folder of resized images beside the input directory\n" + "or file with 'resized_' prepended onto the name. So in the basic example\n" + "above, the output files would be in path/to/directory/of/tiles_images/.\n" + "\n" + " Options:\n\n" 
            + "-width: width of output image. If only width is set, aspect ratio\n"
            + "\tof result image will be preserved.\n"
            + "\tDefault is 0\n\n"
            + "-height: height of output image. If only height is set, aspect ratio\n"
            + "\tof result image will be preserved.\n\n"
            + "-outputtype or -t: format of output image (tif, jpg, bmp, gif, png)\n"
            + "\tCurrently only output format supported is tif.\n"
            + "\tDefault is extension of input image.\n\n"
            + "-outputdir or -o: the output directory for the converted images. It\n" 
            + "\tneed not exist. Default is a folder next to the input folder\n" 
            + "\tor file, with 'resized_' prepended to the name of the input\n" 
            + "\t(input files will have the extension removed). \n\n" 
            + "-simpleoutput or -s: '_resized' parent directory for output files is not\n" 
            + "\tcreated. Resized files are saved directly into output folder.\n\n"
            + "-verbose or -v: makes the utility more 'chatty' during processing. \n\n" 
            + "-debug: print various debugging messages during processing. \n\n" 
            + " Arguments:\n\n" + "The arguments following any options are the input images or folders.\n" + "(or both) If there are multiple input folders or images, each should\n" + "be separated by a space. Input folders will not be NOT be recursed.\n" + "Only images immediately inside the folder will be processed.\n" + "All inputs will be processed into the one output directory,\n" + "so general usage is to process one folder containing multiples images\n" + "or to process one singe image file. \n";
    private enum CmdParseState {

        DEFAULT, OUTPUTDIR, OUTPUTTYPE, JPGQUALITY, INPUTFILE, WIDTH, HEIGHT
    }
    // The following can be overriden/set by the indicated command line arguments    
    static boolean showHelp = false;              // -help | -h
    static int width = 0;                         // -width
    static int height = 0;                        // -height
    static File outputDir = null;                 // -outputdir | -o
    static String outputType = null;              // -outputtype | -t
    static boolean simpleOutput = false;          // -simpleoutput | -s
    static float jpgQuality = 0.8f;	          // -jpgQuality (0.1 to 1.0)    
    static boolean verboseMode = false;           // -verbose
    static boolean debugMode = false;             // -debug
    static ArrayList<File> inputFiles = new ArrayList<File>();  // must follow all other args
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
                        if (simpleOutput) {
                            outputFiles.add(outputDir);
                        } else {
                            File outputFile = createDir(outputDir, "resized_" + nameWithoutExtension);
                            outputFiles.add(outputFile);
                        }
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
                        if (simpleOutput) {
                            outputFiles.add(parentFile);
                        } else {
                            File outputFile = createDir(parentFile, "tiles_" + nameWithoutExtension);
                            outputFiles.add(outputFile);
                        }
                    }
                }

                if (debugMode) {
                    if (outputDir != null) {
                        System.out.printf("outputDir=%s", outputDir.getPath());
                    }
                    System.out.print("\n");
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
                    if (arg.equals("-help") || arg.equals("-h")) {
                        showHelp = true;
                        return;
                    } else if (arg.equals("-verbose")) {
                        verboseMode = true;
                    } else if (arg.equals("-debug")) {
                        verboseMode = true;
                    } else if (arg.equals("-simpleoutput") || arg.equals("-s")) {
                        simpleOutput = true;
                    } else if (arg.equals("-outputdir") || arg.equals("-o")) {
                        state = CmdParseState.OUTPUTDIR;
                    } else if (arg.equals("-outputtype") || arg.equals("-t")) {
                        state = CmdParseState.OUTPUTTYPE;
                    } else if (arg.equals("-quality")) {
                        state = CmdParseState.JPGQUALITY;
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
                    if (wtmp < 0) {
                        throw new Exception("-width");
                    }
                    width = wtmp;
                    state = CmdParseState.DEFAULT;
                    break;
                case HEIGHT:
                    int htmp = Integer.parseInt(args[count]);
                    if (htmp < 0) {
                        throw new Exception("-height");
                    }
                    height = htmp;
                    state = CmdParseState.DEFAULT;
                    break;
                case OUTPUTDIR:
                    outputFiles.add(new File(args[count]));
                    state = CmdParseState.DEFAULT;
                    break;
                case OUTPUTTYPE:
                    String ttmp = args[count].toLowerCase();
                    if (ttmp.equals("jpg") || ttmp.equals("jpeg") || ttmp.equals("tif") || ttmp.equals("tiff") || ttmp.equals("bmp") || ttmp.equals("png") || ttmp.equals("bmp") || ttmp.equals("gif")) {
                        outputType = ttmp;
                    } else {
                        throw new Exception("-outputtype");
                    }
                    state = CmdParseState.DEFAULT;
                    break;
                case JPGQUALITY:
                    float qtmp = Float.parseFloat(args[count]);
                    if (qtmp < 0.1 || qtmp > 1) {
                        throw new Exception("-quality");
                    }
                    jpgQuality = qtmp;
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
        if (inputFiles.isEmpty() && showHelp) {
            throw new Exception("No input files given");
        }
        if (width == 0 && height == 0) {
            throw new Exception("No resize dimensions given");
        }
    }

    private static void processImageFile(File inFile, File outputDir) throws IOException {
        if (verboseMode) {
            System.out.printf("Resizing image: %s\n", inFile);
        }

        if (outputType == null) {
            outputType = inFile.getAbsolutePath().substring(inFile.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
        }

        FileSeekableStream stream = null;

        try {

            stream = new FileSeekableStream(inFile);
            PlanarImage planarImage = JAI.create("stream", stream);
            if (width == 0) {
                width = (height * planarImage.getWidth() / planarImage.getHeight());
            } else if (height == 0) {
                height = (width * planarImage.getHeight() / planarImage.getWidth());
            }

            ParameterBlock paramBlock = new ParameterBlock();
            paramBlock.addSource(planarImage); // The source image
            paramBlock.add((float) width / (float) planarImage.getWidth()); // The xScale
            paramBlock.add((float) height / (float) planarImage.getHeight()); // The yScale
            paramBlock.add(0.0f); // The x translation
            paramBlock.add(0.0f); // The y translation
            paramBlock.add(new InterpolationNearest());
            planarImage = JAI.create("scale", paramBlock);

            String outputFileName = outputDir.getAbsolutePath() + File.separator + inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));

            if (outputType.equals("tif") || outputType.equals("tiff")) {
                JAI.create("filestore", planarImage, outputFileName + ".tif", "TIFF");
            } else if (outputType.equals("jpg") || outputType.equals("jpeg")) {
                throw new UnsupportedOperationException("cant save as jpg yet");
            } else if (outputType.equals("png")) {
                throw new UnsupportedOperationException("cant save as png yet");
            } else if (outputType.equals("bmp")) {
                throw new UnsupportedOperationException("cant save as bmp yet");
            } else if (outputType.equals("gif")) {
                throw new UnsupportedOperationException("cant save as gif yet");
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
