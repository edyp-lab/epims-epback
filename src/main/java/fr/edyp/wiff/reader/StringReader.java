/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.wiff.reader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.LittleEndian;

public class StringReader {

	public static String convert(String str) {
		char[] array = str.toCharArray();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < array.length; i += 2) {
			buffer.append(array[i]);
		}
		return buffer.toString();
	}

	public static String readLine(int length, DataInputStream dis) {
		String s = null;
		try {
			byte[] b = new byte[length];
			dis.read(b);
			s = new String(b);
			s = convert(s);
			System.out.println(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}


	public static String readLine(InputStream dis) {
		short length;
		String s = null;
		try {
			byte[] w = new byte[2];
			dis.read(w);
//			System.out.print(BinaryUtils.toHex(w));
			length = LittleEndian.getShort(w);
//			length = LittleEndianUtils.readShort(w);
//			System.out.print(" ("+length+" bytes) : ");			
			//		         length = (short)((w[0] << 8) | (w[1] & 0xff));
			byte[] b = new byte[length];
			dis.read(b);
			s = new String(b);
			s = convert(s);
//			System.out.println(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}
}
