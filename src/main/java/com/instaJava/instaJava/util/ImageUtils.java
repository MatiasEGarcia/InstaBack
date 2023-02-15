package com.instaJava.instaJava.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.instaJava.instaJava.exception.ImageException;

public class ImageUtils {

	public static byte[] compressImage(byte[] data){
		Deflater deflater = new Deflater();
		deflater.setLevel(Deflater.BEST_COMPRESSION);
		deflater.setInput(data);
		deflater.finish();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] tmp = new byte[4 * 1024];
		try {
			while (!deflater.finished()) {
				int size = deflater.deflate(tmp);
				outputStream.write(tmp, 0, size);
			}
			outputStream.close();
		} catch (IOException e) {
			throw new ImageException(e);
		}
		return outputStream.toByteArray();
	}

	public static byte[] decompressImage(byte[] data) {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] tmp = new byte[4 * 1024];
		try {
			while (!inflater.finished()) {
				int count = inflater.inflate(tmp);
				outputStream.write(tmp, 0, count);
			}
			outputStream.close();
		} catch (Exception e) {
			throw new ImageException(e);
		}
		return outputStream.toByteArray();
	}
}
