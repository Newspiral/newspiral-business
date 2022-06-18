package com.jinninghui.newspiral.common.entity.sm9;

/**
 * String 与 byte数组之间的（十六进制）转换.
 * 
 * @author
 * 
 */
public class Hex {

	/**
	 * 编码.
	 *
	 * @param data 原始数据.
	 * @return 十六进制字符串.
	 */
	public static String encodeToString(byte[] data) {
		return encodeToString(data, false);
	}
	/**
	 * 编码.
	 *
	 * @param data 原始数据.
	 * @return 十六进制字符串.
	 */
	public static String encodeToString(byte[] data, boolean isUpperCase) {
		char[] digital = "0123456789abcdef".toCharArray();
		if(isUpperCase)
			digital = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer("");
		int bit;
		for (int i = 0; i < data.length; i++) {
			bit = (data[i] & 0xF0) >> 4;
			sb.append(digital[bit]);
			bit = data[i] & 0x0F;
			sb.append(digital[bit]);
		}
		return sb.toString();
	}

	/**
	 * 编码.
	 *
	 * @param data 原始数据.
	 * @return 十六进制数组.
	 */
	public static byte[] encode(byte[] data) {
		return encodeToString(data).getBytes();
	}

	/**
	 * 编码.
	 *
	 * @param data 原始数据.
	 * @return 十六进制数组.
	 */
	public static byte[] encode(byte[] data, boolean isUpperCase) {
		return encodeToString(data, isUpperCase).getBytes();
	}


	/**
	 * String 类型的十六进制字符串转换为 byte 内存数组
	 * 
	 * @param hex
	 *            String 类型的十六进制字符串.
	 * @return byte 内存数组.
	 */
	public static byte[] decode(String hex) {
		String digital = "0123456789abcdef";
		char[] hex2char = hex.toLowerCase().toCharArray();
		byte[] bytes = new byte[hex.length() / 2];
		int temp;
		for (int i = 0; i < bytes.length; i++) {
			temp = digital.indexOf(hex2char[2 * i]) << 4;
			temp += digital.indexOf(hex2char[2 * i + 1]);
			bytes[i] = (byte) (temp & 0xFF);
		}
		return bytes;
	}


}