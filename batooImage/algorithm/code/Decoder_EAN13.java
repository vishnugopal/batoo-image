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

package batooImage.algorithm.code;

import batooImage.algorithm.code.*;

//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
/** 
*  This class is used to recognize and decode EAN13 barcodes. 
* 
*  @author Robert Adelmann
*  @version 1.0 
*/
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
public class Decoder_EAN13 {

	//---------------------------------------------------------------------------------------
	// VARIABLES
	//---------------------------------------------------------------------------------------

	static int BOTH_TABLES = 0;
	static int EVEN_TABLE = 1;
	static int ODD_TABLE = 2;
	
	static int[][] code_odd = { { 30, 20, 10, 10 }, 
		                        { 20, 20, 20, 10 }, 
		                        { 20, 10, 20, 20 }, 
		                        { 10, 40, 10, 10 }, 
		                        { 10, 10, 30, 20 }, 
		                        { 10, 20, 30, 10 }, 
		                        { 10, 10, 10, 40 }, 
		                        { 10, 30, 10, 20 }, 
		                        { 10, 20, 10, 30 }, 
		                        { 30, 10, 10, 20 } };

	static int[][] code_even = { { 10, 10, 20, 30 }, 
		                         { 10, 20, 20, 20 }, 
		                         { 20, 20, 10, 20 }, 
		                         { 10, 10, 40, 10 }, 
		                         { 20, 30, 10, 10 }, 
		                         { 10, 30, 20, 10 }, 
		                         { 40, 10, 10, 10 }, 
		                         { 20, 10, 30, 10 }, 
		                         { 30, 10, 20, 10 }, 
		                         { 20, 10, 10, 30 } };


	static boolean parity_pattern_list[][] = { { false, false, false, false, false, false }, 
		                                       { false, false, true, false, true, true }, 
		                                       { false, false, true, true, false, true }, 
		                                       { false, false, true, true, true, false }, 
		                                       { false, true, false, false, true, true }, 
		                                       { false, true, true, false, false, true }, 
		                                       { false, true, true, true, false, false }, 
		                                       { false, true, false, true, false, true }, 
		                                       { false, true, false, true, true, false }, 
		                                       { false, true, true, false, true, false } };

	static boolean debug = false;

	//---------------------------------------------------------------------------------------
	// METHODS
	//---------------------------------------------------------------------------------------
  
	//---------------------------------------------------------------------------------------
	/** This method is called from the ScanlineControl class, if a recognition run should be 
	 *  performed along a path.
	 *  
	 *  @param fields contains information about a series of four alternating black and white 
	 *                fields.<br>
	 *                The second dimension has 2 entries: <br>
	 *                The first one (index 0) contains information about a fields color 
	 *                (0 for black and 255 for white) <br>
	 *                The second (index 1) contains the length of this field in pixels.
   *
   *   @return A barcode_EAN13 object containing the recogized barcode.
   *
	 *   @author Robert Adelmann 
	 */
	//---------------------------------------------------------------------------------------
	public static Barcode_EAN13 recognize(int[][] fields) {

		// try to extract the encoded information from the field series:
		int numbers[] = decode(fields, 0, fields.length);
		Barcode_EAN13 barcode = new Barcode_EAN13(numbers);

		// return the results:
		return barcode;

	}

	//---------------------------------------------------------------------------------------
	/** Trys to detect an EAN13 barcode in the field data.
	 *  
	 *  @param fields contains information about a series of four alternating black and white 
	 *                fields.<br>
	 *                The second dimension has 2 entries: <br>
	 *                The first one (index 0) contains information about a fields color 
	 *                (0 for black and 255 for white) <br>
	 *                The second (index 1) contains the length of this field in pixels.
	 *  @param start_i represents the field index at which the search for a barcode should be 
	 *                 started.              
	 *  @param end_i represents the field index at which the search for a barcode should be 
	 *               stopped.
	 *                                
	 *  @return If a EAN 13 barcode (13 digits) is found, its digits are returned. 
	 *          If not, at least the digits of it that could be recognized will be returned. 
	 *          All other number fields will contin a -1 value.
	 *   
	 *  @author Robert Adelmann 
	 */
	//---------------------------------------------------------------------------------------
	private static int[] decode(int[][] fields, int start_i, int end_i) {

      // determine the length of the path in pixels
		int length = 0;
		for (int i = 0; i < fields.length; i++) length = length + fields[i][1];
		
      // set the parameters accordingly:
		int max_start_sentry_bar_differences;
		int max_unit_length;
		int min_unit_length;
		
		if (length <= 800) {
		   max_start_sentry_bar_differences = 6;
		   max_unit_length = 10;
		   min_unit_length = 1;
		} else {
		   max_start_sentry_bar_differences = 30;
		   max_unit_length = 50;
		   min_unit_length = 1;
		}
		
		// consistency checks:
		if (fields.length <= 0) return null;
		if (start_i > end_i - 3) return null;
		if (end_i - start_i < 30) return null; // (just a rough value)

		// relevant indexes: 
		int start_sentinel_i;
		int end_sentinel_i;
		int left_numbers_i;
		int middle_guard_i;
		int right_numbers_i;

		// relevant parameters:
		float unit_length;

		// results:
		int[] numbers = new int[13];

		// determine the relevant positions:

		// Try to detect the start sentinel (a small black-white-black serie):
		start_sentinel_i = -1;
		for (int i = start_i; i < end_i - 56; i++) {
			if (fields[i][0] == 0) {
				if ((fields[i][1] >= min_unit_length) && (fields[i][1] <= max_unit_length)) {
					if ((Math.abs(fields[i][1] - fields[i + 1][1]) <= max_start_sentry_bar_differences)
							&& (Math.abs(fields[i][1] - fields[i + 2][1]) <= max_start_sentry_bar_differences) && (fields[i + 3][1] < fields[i][1] << 3)) {
						start_sentinel_i = i;
						break;
					}
				}
			}
		}

		if (debug) {
			System.out.println("start_sentinal_index: " + start_sentinel_i);
		}

		if (start_sentinel_i < 0) return null;

		// calculate the other positions:
		left_numbers_i = start_sentinel_i + 3;
		middle_guard_i = left_numbers_i + 6 * 4;
		right_numbers_i = middle_guard_i + 5;
		end_sentinel_i = right_numbers_i + 6 * 4;

		//if (debug) System.out.println("end_sentinel " + end_sentinel_i + " end_i " + end_i);
		if (end_sentinel_i + 3 > end_i) return null;

		// calculate the average (pixel) length of a bar that is one unit wide:
		// (a complete  barcode consists out of 95 length units)
		int temp_length = 0;
		int field_amount = (end_sentinel_i - start_sentinel_i + 3);
		for (int i = start_sentinel_i; i < start_sentinel_i + field_amount; i++)
			temp_length = temp_length + fields[i][1];
		unit_length = (float) ((float) temp_length / (float) 95);

		// print out some debugging information:
		if (debug) {
			System.out.println("unit_width: " + unit_length);
		}
		int[][] current_number_field = new int[4][2];

		if (left_numbers_i + 1 > end_i) return null;



	
		// test the side from which we are reading the barcode:
		MatchMakerResult matchMakerResult;
		for (int j = 0; j < 4; j++) {
			current_number_field[j][0] = fields[left_numbers_i + j][0];
			current_number_field[j][1] = fields[left_numbers_i + j][1];
		}
		matchMakerResult = recognizeNumber(current_number_field, BOTH_TABLES);
		
		
		
		if (matchMakerResult.isEven()) {
			
			// we are reading the barcode from the back side:
			
			// use the already obtained information:
			numbers[12] = matchMakerResult.getDigit();
			
          // try to recognize the "right" numbers:
			int counter = 11;
			for (int i = left_numbers_i + 4; i < left_numbers_i + 24; i = i + 4) {
				for (int j = 0; j < 4; j++) {
					current_number_field[j][0] = fields[i + j][0];
					current_number_field[j][1] = fields[i + j][1];
				}
				matchMakerResult = recognizeNumber(current_number_field, EVEN_TABLE);
				numbers[counter] = matchMakerResult.getDigit();
				counter--;
			}
          	
			boolean[] parity_pattern = new boolean[6];  // true = even, false = odd
			
			//(counter has now the value 6)
			
			// try to recognize the "left" numbers:	
			for (int i = right_numbers_i; i < right_numbers_i + 24; i = i + 4) {
				for (int j = 0; j < 4; j++) {
					current_number_field[j][0] = fields[i + j][0];
					current_number_field[j][1] = fields[i + j][1];
				}
				matchMakerResult = recognizeNumber(current_number_field, BOTH_TABLES);
				numbers[counter] = matchMakerResult.getDigit();
				parity_pattern[counter-1] = !matchMakerResult.isEven();
				counter--;
			} 

			// try to determine the system code:
			matchMakerResult = recognizeSystemCode(parity_pattern);
			numbers[0] = matchMakerResult.getDigit();
				
			
		} else {
			
			// we are reading the abrcode from the "correct" side:
			
			boolean[] parity_pattern = new boolean[6];  // true = even, false = odd
			
			// use the already obtained information:
			numbers[1] = matchMakerResult.getDigit();
			parity_pattern[0] = matchMakerResult.isEven();
			
			// try to recognize the left numbers:
			int counter = 2;
			for (int i = left_numbers_i + 4; i < left_numbers_i + 24; i = i + 4) {
				for (int j = 0; j < 4; j++) {
					current_number_field[j][0] = fields[i + j][0];
					current_number_field[j][1] = fields[i + j][1];
				}
				matchMakerResult = recognizeNumber(current_number_field, BOTH_TABLES);
				numbers[counter] = matchMakerResult.getDigit();
				parity_pattern[counter-1] = matchMakerResult.isEven();
				counter++;
			}

			// try to determine the system code:
			matchMakerResult = recognizeSystemCode(parity_pattern);
			numbers[0] = matchMakerResult.getDigit();
			
			// try to recognize the right numbers:
			counter = 0;
			for (int i = right_numbers_i; i < right_numbers_i + 24; i = i + 4) {
				for (int j = 0; j < 4; j++) {
					current_number_field[j][0] = fields[i + j][0];
					current_number_field[j][1] = fields[i + j][1];
				}
				matchMakerResult = recognizeNumber(current_number_field, ODD_TABLE);
				numbers[counter + 7] = matchMakerResult.getDigit();
				counter++;
			}
			
		}
		
		return numbers;

	}

	//---------------------------------------------------------------------------------------
	/** Rounds the given float value. It is used because the Java2 Micro Edition
	 *  provides no round method.
	 *  
	 *  @param f contains the float value that sould be rounded
	 *  
	 *  @return the rounded value
	 *  
	 *  @author Robert Adelmann 
	 */
	//---------------------------------------------------------------------------------------
	private static int round(double f) {
		int i = (int) f;
		f = f - i;
		if (f >= 0.5) i = i + 1;
		return i;
	}

	//---------------------------------------------------------------------------------------
	/** Recognizes the digit that is encoded by the given series of four black and white fields.
	 * 
	 *  @param fields contains information about a series of four alternating black and white 
	 *                fields.<br>
	 *                The second dimension has 2 entries: <br>
	 *                The first one (index 0) contains information about a fields color 
	 *                (0 for black and 255 for white) <br>
	 *                The second (index 1) contains the length of this field in pixels.
	 *  @param left_side specifies if these black and white fields are located on the
	 *                   left or right side of the barcode.
	 *                   
	 *  @return the digit encoded by the set of black and white fields as well as the parity of
	 *              this digit (even or odd). 
	 *              
	 *  @author Robert Adelmann 
	 */
	//---------------------------------------------------------------------------------------
	public static MatchMakerResult recognizeNumber(int[][] fields, int code_table_to_use) {

		// convert the pixel lenghts of the four black&white fields into 
		// normed values that have together a length of 70;
		int pixel_sum = fields[0][1] + fields[1][1] + fields[2][1] + fields[3][1];
		int b[] = new int[4];
		for (int i = 0; i < 4; i++) {
			b[i] = round((((float) fields[i][1]) / ((float) pixel_sum)) * 70);
		}

		// print some debugging information:	
		if (debug) {
			System.out.println("Recognize Number (code table to use: " + code_table_to_use + "):");
			System.out.println("lengths: " + fields[0][1] + " " + fields[1][1] + " " + fields[2][1] + " " + fields[3][1]);
			System.out.println("normed lengths: " + b[0] + " " + b[1] + " " + b[2] + " " + b[3]);
		}

		// try to detect the digit that is encoded by the set of four normed bar lenghts:	
		int max_difference_for_acceptance = 60;
		int temp;

		
		int even_min_difference = 100000;
		int even_min_difference_index = 0;
		int odd_min_difference = 100000;
		int odd_min_difference_index = 0;
		
		if ((code_table_to_use == BOTH_TABLES)||(code_table_to_use == EVEN_TABLE)) {
			int even_differences[] = new int[10];

			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 4; j++) {
					// calculate the differences in the even group:
					temp = b[j] - code_even[i][j];
					if (temp < 0) even_differences[i] = even_differences[i] + ((-temp) << 1);
					else even_differences[i] = even_differences[i] + (temp << 1);

				}
				if (even_differences[i] < even_min_difference) {
					even_min_difference = even_differences[i];
					even_min_difference_index = i;
				}
			}	
		}	
			
		if ((code_table_to_use == BOTH_TABLES)||(code_table_to_use == ODD_TABLE)) {
			int odd_differences[] = new int[10];

			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 4; j++) {
					// calculate the differences in the odd group:			
					temp = b[j] - code_odd[i][j];
					if (temp < 0) odd_differences[i] = odd_differences[i] + ((-temp) << 1);
					else odd_differences[i] = odd_differences[i] + (temp << 1);
				}
				if (odd_differences[i] < odd_min_difference) {
					odd_min_difference = odd_differences[i];
					odd_min_difference_index = i;
				}
			}
		}	
			
		
		// select the digit and parity with the lowest difference to the found pattern:
		if (even_min_difference <= odd_min_difference) {
			if (even_min_difference < max_difference_for_acceptance) return new MatchMakerResult(true, even_min_difference_index);
		} else {
			if (odd_min_difference < max_difference_for_acceptance) return new MatchMakerResult(false, odd_min_difference_index);
		}
		
		return new MatchMakerResult(false, -1);
	}

	//---------------------------------------------------------------------------------------
	/** This method takes a parity pattern and tries to detect the according system code.
	 *  
	 *  @param parity_pattern contains the parity pattern (six boolean values). <br>
	 *         True corresponds to even and false to odd.
	 * 
	 *  @return the system code that corresponds to the given parity pattern.
	 *  
	 *  @author Robert Adelmann 
	 */
	//---------------------------------------------------------------------------------------
	public static MatchMakerResult recognizeSystemCode(boolean[] parity_pattern) {

		// search for a fitting parity pattern:
		boolean fits = false;
		for (int i = 0; i < 10; i++) {
			fits = true;
			for (int j = 0; j < 6; j++) {
				if (parity_pattern_list[i][j] != parity_pattern[j]) {
					fits = false;
					break;
				}
			}
			if (fits) return new MatchMakerResult(false, i);
		}

		return new MatchMakerResult(false, -1);

	}

}

//---------------------------------------------------------------------------------------
//HELPER CLASS
//---------------------------------------------------------------------------------------

class MatchMakerResult {

	//---------------------------------------------------------------------------------------
	// VARIABLES
	//---------------------------------------------------------------------------------------
	boolean even;
	int digit;

	//---------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//---------------------------------------------------------------------------------------
	public MatchMakerResult(boolean even, int digit) {
		this.even = even;
		this.digit = digit;
	}

	//---------------------------------------------------------------------------------------
	// METHODS
	//---------------------------------------------------------------------------------------

	public int getDigit() {
		return digit;
	}

	public void setDigit(int digit) {
		this.digit = digit;
	}

	public boolean isEven() {
		return even;
	}

	public void setEven(boolean even) {
		this.even = even;
	}

}
