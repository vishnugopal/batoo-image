//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
/** 
 *  Copyright (C) 2006 Robert Adelmann
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
 *  This interface represents a specific hardware device. It provides access to all
 *  potentially device specific data and functionality.
 *  
 *  @author Robert Adelmann
 *  @version 1.0
 */
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

public interface Device {

	//---------------------------------------------------------------------------------------
	/** Is called from application.RecognitionThread if a recognition run should be performed.
	 *  
	 *  @param profile The Profile object, containing the settings that 
	 *         should be used during this recognition run.
     */
	//---------------------------------------------------------------------------------------
	public Barcode recognize();

	//---------------------------------------------------------------------------------------
	/** Returns the width of the image on which the recognition should be performed.
	 *  
	 *  @return the width of the image on which the recognition should be performed.
     */
	//---------------------------------------------------------------------------------------
	public int getImageWidth();
	
	//---------------------------------------------------------------------------------------
	/** Returns the height of the image on which the recognition should be performed.
	 *  
	 *  @return the height of the image on which the recognition should be performed.
     */
	//---------------------------------------------------------------------------------------
	public int getImageHeight();
	
	//---------------------------------------------------------------------------------------
	/** Extracts color values from the class's pixel data array along a specified path.
     *
	 *  @param x1 x-Pos of path starting point
	 *  @param y1 y-Pos of path starting point
	 *  @param x2 x-Pos of path end point
	 *  @param y2 y-Pos of path end point
	 *  <p>
	 *  @param image width
	 *  
	 *  @return A two three dim. array, containing the RGB values along the specified path.<br>        
	 */
	//---------------------------------------------------------------------------------------
	public int[][] getPath(int x1, int y1, int x2, int y2, int image_w);
		
}