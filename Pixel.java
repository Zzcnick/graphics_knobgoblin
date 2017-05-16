import java.io.*;
import java.util.*;

// ===================================================
// Pixel Class - Pixel Information
// ===================================================
public class Pixel {
    private int R;
    private int G;
    private int B;

    // Constructors
    public Pixel() { // White Pixel
	R = 255;
	G = 255;
	B = 255;
    }
    public Pixel(int R, int G, int B) {
	this.R = R;
	this.G = G;
	this.B = B;
    }
    public Pixel(int[] RGB) {
	this(RGB[0], RGB[1], RGB[2]);
    }
    public Pixel(Pixel p) {
	int[] rgb = p.getRGB();
	R = rgb[0];
	G = rgb[1];
	B = rgb[2];
    }
    public Pixel(int flag) {
	if (flag == 1) { // Random
	    R = (int)(Math.random() * 256);
	    G = (int)(Math.random() * 256);
	    B = (int)(Math.random() * 256);
	}
    }
    
    // Accessors and Mutators
    public int[] getRGB() {
	return new int[]{R, G, B};
    }
    public Pixel copy() {
	return new Pixel(R, G, B);
    }

    // Methods
    public int[] adjust(int dR, int dG, int dB) {
	int[] old = getRGB();
	R += dR; G += dG; B += dB;
	return old;
    }

    // ToString Utility
    public String toString() {
	return "" + R + " " + G + " " + B + "\n";
    }
}
