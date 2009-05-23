//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
/** 
 *  Copyright (C) 2006 Robert Adelmann
 *  Parts Copyright (C) 2009 Vishnu Gopal  
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 *  MA  02110-1301, USA  
 */
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

package batooImage.algorithm;

//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
/** 
 *  This class represents a dummy Image device. It provides access to all
 *  potentially image specific data and functionality.
 *
 *  @author Vishnu Gopal
 *  @version 1.0
 */
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

public class ImageDevice implements Device {

	//---------------------------------------------------------------------------------------
	// VARIABLES
	//---------------------------------------------------------------------------------------
	private byte pixel_data[];        // a byte array containing the image information
	private int image_width;          // the width of the image contained in the byte array
	private int image_height;         // the height of the image contained in the byte array
	
	//---------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//---------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------
	/** @param pixel_data contains the image data.
	 *  @param width contains the width of the image contained in the pixel_data byte array.
	 *  @param height contains the height of the image contained in the pixel_data byte array.
	 *  @param canvas specifies the cancas used for the displaying of information.
   */
	//---------------------------------------------------------------------------------------
	public ImageDevice(byte[] pixel_data, int width, int height) {
		this.pixel_data = pixel_data;
		this.image_height = height;
		this.image_width = width;	
	}

	//---------------------------------------------------------------------------------------
	// METHODS
	//---------------------------------------------------------------------------------------

	//---------------------------------------------------------------------------------------
	/** Is called by the ImageLoader class
   */
	//---------------------------------------------------------------------------------------
	public Barcode recognize() {
		Barcode code = BarcodeDecoder.recognizeBarcode(this);
		return code;
	}

	//---------------------------------------------------------------------------------------
	/** Extracts color values from the class's 
     *  pixel data array along a specified path.
     *
	 *  @param x1 x-Pos of path starting point
	 *  @param y1 y-Pos of path starting point
	 *  @param x2 x-Pos of path end point
	 *  @param y2 y-Pos of path end point
	 *  @param w  path width
	 *  
	 *  @return A two dim. array, containing the RGB values along the specified path.
	 *  <p>
	 *  Index 1 has a size that correlates to the length of the path and 
	 *  specifies the position along the path.<br> 
	 *  Index 2 has size three and specifies the color as RGB values. <br>
	 */
	//---------------------------------------------------------------------------------------
	public int[][] getPath(int x1, int y1, int x2, int y2, int w) {
		if (pixel_data == null) return null;
		return getPathFromBMPData(pixel_data, w, x1, y1, x2, y2);
	}

	//---------------------------------------------------------------------------------------
	/** Extracts color values from the image along a specified path.
	 * 
	 *  @param bmp_data array, containing the image data in the bmp format
	 *  @param x1 x-Pos of path starting point
	 *  @param y1 y-Pos of path starting point
	 *  @param x2 x-Pos of path end point
	 *  @param y2 y-Pos of path end point
	 *  @param width  image width
	 *  
	 *  @return A two three dim. array, containing the RGB values along the specified path.  
	 */
	//---------------------------------------------------------------------------------------
	private int[][] getPathFromBMPData(byte[] bmp_data, int width, int x1, int y1, int x2, int y2) {

		// all distances are measured in "pixels"
		float dx = Math.abs(x2 - x1);
		float dy = Math.abs(y2 - y1);

		int distance = new Float(Math.sqrt(dx * dx + dy * dy)).intValue();
		int[][] path = new int[distance][3];

		float factor, px, py;
		int px_i, py_i;
		int pos;

		// collect the color information:
		for (int i = 0; i < distance; i++) {
			factor = ((float) i / distance);
			px = x1 + dx * factor;
			py = y1 + dy * factor;
			px_i = new Float(px).intValue();
			py_i = new Float(py).intValue();

			pos = 54 + (py_i * width + px_i) * 3;
			path[i][2] = getIntValue(bmp_data[pos]);
			path[i][1] = getIntValue(bmp_data[pos + 1]);
			path[i][0] = getIntValue(bmp_data[pos + 2]);
		}
		return path;
	}

	//---------------------------------------------------------------------------------------
	/** Converts the given byte value representing a "color" into an int value.
	 *  
	 *  @param byte_value The color in the byte format (-128..127)
	 *  
	 *  @return Color as int value (0..255)
	 */
	//---------------------------------------------------------------------------------------
	private static int getIntValue(byte byte_value) {
		if (byte_value >= 0) {
			int value = byte_value;
			return value;
		} else {
			int value = 255 + byte_value;
			return value;
		}
	}


	//---------------------------------------------------------------------------------------
	// GET/SET METHODS
	//---------------------------------------------------------------------------------------
	
	//---------------------------------------------------------------------------------------
	/** @return the width of the image on which the recognition should be performed. */
	//---------------------------------------------------------------------------------------
	public int getImageWidth() {
		return image_width;
	}
	
	//---------------------------------------------------------------------------------------
	/** @return the height of the image on which the recognition should be performed. */
	//---------------------------------------------------------------------------------------
	public int getImageHeight() {
		return image_height;
	}

}
