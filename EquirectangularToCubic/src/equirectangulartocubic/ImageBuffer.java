package equirectangulartocubic;

import com.sun.media.jai.codec.FileSeekableStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

public class ImageBuffer {

    private FileSeekableStream stream;
    private PlanarImage planarImage;
    private RectIter rectIter;
    private int[] pixel;
    private int currentRow;
    private HashMap<Integer, int[]> buffer;
    private int rowCounter;
    private int outCounter;
    private int rowLimit;
    private long startTime;
    private boolean verboseMode;
    private int wallCount;

    public ImageBuffer(File inFile, boolean verboseMode) throws IOException {
        this.verboseMode = verboseMode;
        try {
            stream = new FileSeekableStream(inFile);
            planarImage = JAI.create("stream", stream);
        } catch (Exception e) {
            if (stream != null) {
                stream.close();
            }
            throw new IOException("Cannot read image file: " + inFile);
        }
        //planarImage = JAI.create("fileload", inFile.getAbsolutePath());

        rectIter = RectIterFactory.create(planarImage, null);
        pixel = new int[planarImage.getSampleModel().getNumBands()];
        if (pixel.length < 3 || pixel.length > 4) {
            throw new IllegalArgumentException("Image color scheme is not suppported!");
        }
    }

    public void init(double yaw, double pitch) {
        // top, botttom
        if (pitch != 0) {
            rowLimit = (int) Math.ceil((double) planarImage.getHeight() * 0.300d);
            // front, back, left, right
        } else {
            rowLimit = (int) Math.ceil((double) planarImage.getHeight() * 0.5015d);
        }

        rectIter.startPixels();
        rectIter.startLines();
        currentRow = 0;
        rowCounter = 0;
        buffer = new HashMap<Integer, int[]>();
        startTime = System.currentTimeMillis();
        wallCount++;
        System.out.println("Reading wall " + wallCount + " of 6:");
        outCounter = 0;
    }

    public void reset() {
        buffer = null;
    }

    public void printStats() {
        if (verboseMode) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            String format = String.format("%%0%dd", 2);
            elapsedTime = elapsedTime / 1000;
            String seconds = String.format(format, elapsedTime % 60);
            String minutes = String.format(format, (elapsedTime % 3600) / 60);
            String hours = String.format(format, elapsedTime / 3600);
            if (hours.equals("00")) {
                if (minutes.equals("00")) {
                    if (seconds.equals("00")) {
                        System.out.println(" in " + (System.currentTimeMillis() - startTime) + "ms");
                    } else {
                        System.out.println(" in " + seconds + "s ");
                    }
                } else {
                    System.out.println(" in " + minutes + "m " + seconds + "s ");
                }
            } else {
                System.out.println(" in " + hours + "h " + minutes + "m " + seconds + "s");
            }
        }
    }

    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }

    public int[] getRow(int rowNumber) {
        if (buffer.containsKey(rowNumber)) {
            return buffer.get(rowNumber);
        } else {
            int[] result = new int[planarImage.getWidth()];
            rectIter.jumpLines(rowNumber - currentRow);
            currentRow = rowNumber;
            rectIter.startPixels();
            for (int i = 0; i < planarImage.getWidth(); i++) {
                rectIter.getPixel(pixel);
                result[i] = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
                rectIter.nextPixel();
            }
            buffer.put(rowNumber, result);
            if (verboseMode) {
                rowCounter++;
                if (rowCounter % 100 == 0) {
                    outCounter++;
                    System.out.print(((outCounter % 25 == 0) ? "\n" : "")
                            + (int) Math.ceil(((double) buffer.size() / (double) rowLimit) * 100d)
                            + "% ");
                }
            }
            return result;
        }
    }

    public int getPanoWidth() {
        return planarImage.getWidth();
    }

    public int getPanoHeight() {
        return planarImage.getHeight();
    }
}
