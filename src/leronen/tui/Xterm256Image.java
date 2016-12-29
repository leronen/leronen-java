package leronen.tui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;

import javax.imageio.ImageIO;

import util.dbg.Logger;

public class Xterm256Image {
    private int height;
    private int width;
    private int data[]; // actually should be bytes, as we have 256 palette, but avoiding this now due to signedness horrors
    private Xterm256Palette palette = new Xterm256Palette();
    
    public Xterm256Image(int height, int width) {
        this.height = height;
        this.width = width;
        this.data = new int[width * height];
    }
    
    public Xterm256Image(BufferedImage sourceImage, int rows, int columns) {
        this.height = rows;
        this.width = columns;
        this.data = new int[columns * rows];
        dbg("Created data ärräy with " + rows * columns + " elements");
        
        dbg("scaling image to " + rows + " rows, " + columns + " columns");
        
        BufferedImage scaledImage = new BufferedImage(columns,
            rows, sourceImage.getType());

        // create new image, scaled to desired number of rows / columns
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(sourceImage, 0, 0, columns, rows, null);
        g2d.dispose();
        
        // grab pixel values
        int[] pixels = new int[columns * rows];
        PixelGrabber pg = new PixelGrabber(scaledImage, 0, 0, columns, rows, pixels, 0, columns);
        
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return;
        }
        
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                handlesinglepixel(column, row, pixels[row * columns + column]);
            }
        }
                
    }
    
    public void handlesinglepixel(int column, int row, int pixel) {
//        int alpha = (pixel >> 24) & 0xff;
        int red   = (pixel >> 16) & 0xff;
        int green = (pixel >>  8) & 0xff;
        int blue  = (pixel      ) & 0xff;
        
        try {
            int colorIndex = palette.getClosestMatch(red, green, blue);
            setPixel(row, column, colorIndex);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            dbg("handlesinglepixel failed @ col " + column + ", row " + row);
            throw e;
        }
        
    }
        
//    public void handlepixels(Image img, int w, int h) {
//        int[] pixels = new int[w * h];
//        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
//        try {
//            pg.grabPixels();
//        } catch (InterruptedException e) {
//            System.err.println("interrupted waiting for pixels!");
//            return;
//        }
//        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
//            System.err.println("image fetch aborted or errored");
//            return;
//        }
//        for (int j = 0; j < h; j++) {
//            for (int i = 0; i < w; i++) {
//                handlesinglepixel(i, j, pixels[j * w + i]);
//            }
//        }
//    }
    
    public void setPixel(int y, int x, int value) {
        try { 
            data[width * y + x]  = value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            dbg("Ärräy out of bounds @ y = " + y + ", x=" + x);
            throw e;
        }
    }
    
    public int getPixel(int y, int x) {
        return data[width * y + x];
    }
    
    /** Render to stdout, quite simply */
    public void render() {
        for (int y=0; y < height; y++) {
            for (int x=0; x<width; x++) {
                int color = getPixel(y, x);             
                System.out.print(TerminalUtils.getXtermBgColor(color) + " ");
            }
            System.out.println(TerminalUtils.ANSI_ESCAPE_RESET);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Logger.enableLogging();
        File infile = new File(args[0]);
//        int rows = Integer.parseInt(args[1]);
        int columns = Integer.parseInt(args[1]);        
        BufferedImage inputImage = ImageIO.read(infile);
        double aspectRatio = ((double)inputImage.getWidth()) / inputImage.getHeight();
        dbg("aspect ratio :" + aspectRatio);
        int rows = (int) (((double)columns) / aspectRatio);
        Xterm256Image xtermImage = new Xterm256Image(inputImage, rows, columns);
        xtermImage.render();
    }
    
    private static void dbg(String msg) {
        System.err.println(msg);
    }
}
