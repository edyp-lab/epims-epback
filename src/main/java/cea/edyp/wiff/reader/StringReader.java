package cea.edyp.wiff.reader;

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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
}
