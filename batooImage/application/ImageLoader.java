
/**
 * Loads a bitmap image and converts it to raw image data.
 */

package batooImage.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import batooImage.algorithm.*;
import batooImage.algorithm.code.*;

class ImageLoader {
	public static void main(String[] args) throws IOException {
		
		String imageFileName = null;
		int imageWidth = 0;
		int imageHeight = 0;
		
		try {
			imageFileName = args[0];	
			imageWidth = java.lang.Integer.parseInt(args[1]);
			imageHeight = java.lang.Integer.parseInt(args[2]);
		} catch(java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("Must give path to a bitmap file as input & dimensions.");
			return;
		}
		
		File imageFile = null;
		FileInputStream imageFileStream = null;
		byte[] imageRawData = null;
		
		try {
			imageFile = new File(imageFileName);
			long imageFileSize = imageFile.length();
			imageFileStream = new FileInputStream(imageFileName);		
			imageRawData = new byte[(int) imageFileSize];
			
			int offset = 0;
      int numRead = 0;
      while (offset < imageRawData.length && 
				(numRead = imageFileStream.read(imageRawData, offset, 
					imageRawData.length - offset)) >= 0) {
      			offset += numRead;
      }

			// Ensure all the bytes have been read in
      if (offset < imageRawData.length) {
          throw new IOException("Could not completely read file " + imageFileName);
      }
		} catch(java.io.FileNotFoundException e) {
			System.out.println("Must give a valid path to a bitmap file as input & dimensions.");
			return;
		} finally {
			if (imageFileStream != null) {
				imageFileStream.close();
			}
		}
		
		ImageDevice imageDevice = new ImageDevice(imageRawData, imageWidth, imageHeight);
		Barcode recognizedBarcode = imageDevice.recognize();
		
		System.out.println("The recognized barcode is: " + recognizedBarcode.toString());
		
  }
}