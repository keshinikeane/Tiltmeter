package lab10;

import java.awt.Color;
import java.util.*;
import java.io.*;

import javax.swing.*;

import edu.mines.jtk.dsp.Sampling; // see documentation online
import edu.mines.jtk.mosaic.*;

/**
 * Convenient class for tiltmeter data; for internal use only (not public).
 */
class TiltData {
  Sampling t; // uniform time sampling, in hours since 2004.09.01
  float[] x;  // measured tilts, in urad (microradians!)
  TiltData(Sampling t, float[] x) {
    this.t = t;
    this.x = x;
  }
}

/**
 * Reads and writes tiltmeter data.
 * Data were extracted from Matlab files provided by 
 * Professor Hartmut Spetzler, University of Colorado.
 * <pre>
 * Text file format: 
 * line 1: number of samples
 * line 2: time sampling interval, in hours since 2004.09.01
 * line 3: time of first sample, in hours since 2004.09.01
 * all remaining lines contain tilts, in microradians
 * </pre>
 * <pre>
 * Binary file format: 
 * int: number of samples
 * float: time sampling interval, in hours since 2004.09.01
 * float: time of first sample, in hours since 2004.09.01
 * all remaining floats contain tilts, in microradians
 * </pre>
 */
public class TiltDemo {

  /**
   * Main program. (No args required.)
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        main(); // will be called on the Swing thread
      }
    });
  }
  private static void main() {
    TiltData td;

    // Text file I/O; when done, tilt.txt and tilt2.txt should be identical.
    td = readText("lab10/tilt.txt");
    plot(td);
    writeText(td,"lab10/tilt2.txt");
    td = readText("lab10/tilt2.txt");
    plot(td);

    // Binary file I/O; when done, tilt.dat and tilt2.dat should be identical.
    td = readBinary("lab10/tilt.dat");
    plot(td);
    writeBinary(td,"lab10/tilt2.dat");
    td = readBinary("lab10/tilt2.dat");
    plot(td);

    // Some useful processing.
    plot(td,despike(td));
  }

  /**
   * Reads tiltmeter data from text file.
   * @param fileName name of text file.
   * @return tiltmeter data; null, if I/O exception is thrown.
   */
  static TiltData readText(String fileName) {
    TiltData td = null;
    try {
		FileInputStream fis = new FileInputStream(fileName);
		Scanner ss = new Scanner(fis);
		int count = ss.nextInt();
		float delta = ss.nextFloat();
		float first = ss.nextFloat();
		float[] x = new float[count];
		for (int i=0; i<count; ++i) {
			x[i] = ss.nextFloat();
		}
		ss.close();
	    td = new TiltData(new Sampling(count,delta,first),x);
    } catch (IOException e) {
      System.out.println(e);
    }
    return td;
  }

  /**
   * Writes tiltmeter data to text file.
   * @param td tiltmeter data.
   * @param fileName name of text file.
   */
  static void writeText(TiltData td, String fileName) {
    try {
    	FileOutputStream fos = new FileOutputStream(fileName);
		PrintWriter pw = new PrintWriter(fos);
		int count = td.t.getCount();
		float delta = (float)td.t.getDelta();
		float first = (float)td.t.getFirst();
		pw.println(count);
		pw.println(delta);
		pw.println(first);;
		for (int i=0; i<count; ++i) {
			pw.println(td.x[i]);
		}
		pw.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /**
   * Reads tiltmeter data from binary file.
   * @param fileName name of binary file.
   * @return tiltmeter data; null, if I/O exception is thrown.
   */
  static TiltData readBinary(String fileName) {
    TiltData td = null;
    try {
    	FileInputStream fis = new FileInputStream(fileName);
    	DataInputStream dis = new DataInputStream(fis);
    	int count = dis.readInt();
    	float delta = dis.readFloat();
    	float first = dis.readFloat();
    	float[] x = new float[count];
    	for (int i=0; i<count; ++i) {
    		x[i] = dis.readFloat();
    	}
    	dis.close();
    	td = new TiltData(new Sampling(count,delta,first),x);
    } catch (IOException e) {
      System.out.println(e);
    }
    return td;
  }

  /**
   * Writes tiltmeter data to binary file.
   * @param td tiltmeter data.
   * @param fileName name of binary file.
   */
  static void writeBinary(TiltData td, String fileName) {
    try {
    	FileOutputStream fos = new FileOutputStream(fileName);
		DataOutputStream dos = new DataOutputStream(fos);
		int count = td.t.getCount();
		float delta = (float)td.t.getDelta();
		float first = (float)td.t.getFirst();
		dos.writeInt(count);
		dos.writeFloat(delta);
		dos.writeFloat(first);
		for (int i=0; i<count; ++i) {
			dos.writeFloat(td.x[i]);
		}
		dos.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /**
   * Removes noise spikes from tiltmeter data.
   * For all except the first and last samples, the output value at
   * index i equals the median of the input values at indices i-1,
   * i and i+1. The first and last output values equal input values.
   * @param td input tiltmeter data; not modified
   * @return output despiked tiltmeter data
   */
  static TiltData despike(TiltData td) {
	  int count = td.t.getCount();
	  float[] x2 = new float[count];
	  for (int i=0; i<count; ++i) {
		  if (i==0 || i==count-1) {
			  x2[i] = td.x[i];
		  } else {
			  if ((td.x[i-1] <= td.x[i] && td.x[i-1] >= td.x[i+1]) || (td.x[i-1] >= td.x[i] && td.x[i-1] <= td.x[i+1]))
				  x2[i] = td.x[i-1];
			  else if ((td.x[i+1] <= td.x[i] && td.x[i+1] >= td.x[i-1]) || (td.x[i+1] >= td.x[i] && td.x[i+1] <= td.x[i-1]))
				  x2[i] = td.x[i+1];
			  else 
				  x2[i] = td.x[i];
		  }
	  }
	  return new TiltData(td.t,x2);
  }

  /**
   * Plots tiltmeter data.
   * @param td tiltmeter data
   */
  static void plot(TiltData td) {
    SimplePlot sp = SimplePlot.asPoints(td.t,td.x);
    sp.setTitle("Tiltmeter data");
    sp.setHLabel("time (hours)");
    sp.setVLabel("tilt (urad)");
  }

  /**
   * Plots two sequences of tiltmeter data.
   * @param td1 tiltmeter data
   * @param td2 tiltmeter data
   */
  static void plot(TiltData td1, TiltData td2) {
    SimplePlot sp = new SimplePlot();
    PointsView pv1 = sp.addPoints(td1.t,td1.x);
    pv1.setLineColor(Color.RED);
    PointsView pv2 = sp.addPoints(td2.t,td2.x);
    pv2.setLineColor(Color.BLUE);
    sp.setTitle("Tiltmeter data");
    sp.setHLabel("time (hours)");
    sp.setVLabel("tilt (urad)");
  }
}

