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

import batooImage.algorithm.Barcode;

//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
/** 
 *  This class represents an EAN13 barcode. 
 * 
 *  @author Robert Adelmann
 *  @version 1.0 
 */
//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
public class Barcode_EAN13 implements Barcode {

	//---------------------------------------------------------------------------------------
	// VARIABLES
	//---------------------------------------------------------------------------------------

	private int numbers[] = new int[13];

	//---------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//---------------------------------------------------------------------------------------
	public Barcode_EAN13(int numbers[]) {
		for (int i = 0; i < 13; i++) this.numbers[i] = -1;
		if (numbers == null) return;
		if (numbers.length != 13) return;
		for (int i = 0; i < 13; i++) this.numbers[i] = numbers[i];
	}

	public Barcode_EAN13(String code) {
		for (int i = 0; i < 13; i++) this.numbers[i] = -1;
		if (code.length() != 13) return;	
		char[] code_chars = code.toCharArray();	
		for (int i = 0; i < 13; i++) this.numbers[i] = Integer.valueOf(""+code_chars[i]).intValue();
	}

	//---------------------------------------------------------------------------------------
	/** Converts this Barcode_EAN13 object to a String. 
	 * 
	 *  @return this Code as a String.*/
	//---------------------------------------------------------------------------------------
	public String toString() {
		String s = "";
		for (int i = 0; i < numbers.length; i++) {
			if (numbers[i] >= 0) s = s + numbers[i] + ""; else s = s + "?";
		}
		return s;
	}

	//---------------------------------------------------------------------------------------
	/** Checks if the contained barcode is a valid EAN13 barcode.
	 *  (So far, the checksum is not tested.)
	 *  
	 *  @return true, if the code is a valid EAN13 code. */
	//---------------------------------------------------------------------------------------
	public boolean isValid() {
		for (int i = 0; i < numbers.length; i++) {
			if ((numbers[i] < 0) || (numbers[i] > 9)) return false;
		}
		
		// calculate the checksum of the barcode:
		int sum1 = numbers[0] + numbers[2] + numbers[4] + numbers[6] + numbers[8] + numbers[10];
		int sum2 = 3 * (numbers[1] + numbers[3] + numbers[5] + numbers[7] + numbers[9] + numbers[11]);
		int checksum_value = sum1 + sum2;
		int checksum_digit = 10 - (checksum_value % 10);
		if (checksum_digit == 10) checksum_digit = 0;

		return (numbers[12] == checksum_digit);
	}

    //---------------------------------------------------------------------------------------
	// GET METHODS
    //---------------------------------------------------------------------------------------

	public int[] getNumbers() {
		return numbers;
	}

	public int getSystemCode() {
		return numbers[0];
	}

	public int getNumber(int index) {
		if ((index < 0) || (index > 12)) return -1;
		return numbers[index];
	}

}