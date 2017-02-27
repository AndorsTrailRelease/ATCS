package com.gpl.rpg.atcontentstudio.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

	public static void deleteDir(File dir) {
		if (dir.exists()) {
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					deleteDir(f);
				} else {
					f.delete();
				}
			}
			dir.delete();
		}
	}
	
	public static void copyFile(File sourceLocation , File targetLocation) {
		try {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			try {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
		}
	}
	
	private static final int BUFFER = 2048;
	public static void writeToZip(File folder, File target) {
		try {
	        FileOutputStream dest = new FileOutputStream(target);
	        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	        zipDir(folder, "", out);	        
	        out.flush();
	        out.close();
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }

	}
	
	private static void zipDir(File dir, String prefix, ZipOutputStream zos) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				zipDir(f, prefix+File.separator+f.getName(), zos);
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(f);
					BufferedInputStream origin = new BufferedInputStream(fis, BUFFER);
					ZipEntry entry = new ZipEntry(prefix+File.separator+f.getName());
					try {
						zos.putNextEntry(entry);
						int count;
						byte data[] = new byte[BUFFER];
						while ((count = origin.read(data, 0, BUFFER)) != -1) {
							zos.write(data, 0, count);
							zos.flush();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							origin.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean makeSymlink(File targetFile, File linkFile) {
		Path target = Paths.get(targetFile.getAbsolutePath());
		Path link = Paths.get(linkFile.getAbsolutePath());
		if (!Files.exists(link)) {
			try {
				Files.createSymbolicLink(link, target);
			} catch (Exception e) {
				System.err.println("Failed to create symbolic link to target \""+targetFile.getAbsolutePath()+"\" as \""+linkFile.getAbsolutePath()+"\" the java.nio way:");
				e.printStackTrace();
				switch (DesktopIntegration.detectedOS) {
				case Windows:
					System.err.println("Trying the Windows way with mklink");
					try {
						Runtime.getRuntime().exec("mklink "+(targetFile.isDirectory() ? "/J " : "")+linkFile.getAbsolutePath()+" "+targetFile.getAbsolutePath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				case MacOS:
				case NIX:
				case Other:
					System.err.println("Trying the unix way with ln -s");
					try {
						Runtime.getRuntime().exec("ln -s "+targetFile.getAbsolutePath()+" "+linkFile.getAbsolutePath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				default:
					System.out.println("Unrecognized OS. Please contact ATCS dev.");
					break;
					
				}
			}
		}
		if (!Files.exists(link)) {
			System.err.println("Failed to create link \""+linkFile.getAbsolutePath()+"\" targetting \""+targetFile.getAbsolutePath()+"\"");
			System.err.println("You can try running ATCS with administrative privileges once, or create the symbolic link manually.");
		}
		return true;
	}
	
}
