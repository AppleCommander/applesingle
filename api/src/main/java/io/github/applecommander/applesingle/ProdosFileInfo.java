package io.github.applecommander.applesingle;

import java.nio.ByteBuffer;

import io.github.applecommander.applesingle.AppleSingle.Builder;

/**
 * A simple ProDOS File Info wrapper class.
 * <p>
 * Note 1: {@link #standardBIN()} can be used to generate sensible defaults.<br/>
 * Note 2: Fields are package-private to allow {@link Builder} to have direct access.<br/>
 */
public class ProdosFileInfo {
	int access;
	int fileType;
	int auxType;
	
	public static ProdosFileInfo standardBIN() {
		return new ProdosFileInfo(0xc3, 0x06, 0x0000);
	}
	public static ProdosFileInfo fromEntry(Entry entry) {
		ByteBuffer infoData = entry.getBuffer();
		int access = infoData.getShort();
		int fileType = infoData.getShort();
		int auxType = infoData.getInt();
		return new ProdosFileInfo(access, fileType, auxType);
	}
	
	public ProdosFileInfo(int access, int fileType, int auxType) {
		this.access = access;
		this.fileType = fileType;
		this.auxType = auxType;
	}
	
	public int getAccess() {
		return access;
	}
	public int getFileType() {
		return fileType;
	}
	public int getAuxType() {
		return auxType;
	}
}