package com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.aaaaahhhhhhh.bananapuncher714.minecraftvideo.internet.Video;

public class ResourcePackTest {

	public static void main(String[] args) throws IOException {

		File vid = new File(
				"C:\\Users\\Brandon Li\\Downloads\\MinecraftVideo2_Master\\MinecraftVideo2_Master\\MinecraftVideo2\\video\\video.mp4");
		File sound = new File(
				"C:\\Users\\Brandon Li\\Downloads\\MinecraftVideo2_Master\\MinecraftVideo2_Master\\MinecraftVideo2\\video\\audio.ogg");

		createEmptyZipFile(new Video(vid, sound));

	}
	
	public static void createEmptyZipFile(Video v) throws IOException {

		ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(new File(System.getProperty("user.dir") + "/resourcepack.zip")));

		byte[] mcmeta = ("{\r\n" + "	\"pack\": {\r\n" + "    \"pack_format\": 6,\r\n"
				+ "    \"description\": \"Custom Server Resourcepack for MinecraftVideo\"\r\n" + "  }\r\n" + "}")
						.getBytes();
		ZipEntry config = new ZipEntry("pack.mcmeta");
		out.putNextEntry(config);
		out.write(mcmeta);
		out.closeEntry();

		byte[] soundJSON = ("{\r\n" + "   \"minecraftvideo\":{\r\n" + "      \"sounds\":[\r\n"
				+ "         \"audio\"\r\n" + "      ]\r\n" + "   }\r\n" + "}").getBytes();
		ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
		out.putNextEntry(sound);
		out.write(soundJSON);
		out.closeEntry();

		ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
		out.putNextEntry(soundFile);
		out.write(Files.readAllBytes(Paths.get(v.sound.getAbsolutePath())));
		out.closeEntry();

		out.close();

	}
	
	@Deprecated // Not Ready
	public static void createServerZipFile(Video v, File file) throws IOException {
		
		boolean exists = false;
		
		ZipFile zipFile = new ZipFile(file.getAbsolutePath());
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("resourcepack.zip"));

		for (Enumeration<?> e = zipFile.entries(); e.hasMoreElements();) {

			ZipEntry next = (ZipEntry) e.nextElement();
			InputStream is = zipFile.getInputStream(next);
			
			if (next.getName().equals("sounds.json")) {
				
				zos.putNextEntry(new ZipEntry("sounds.json"));
				
				String contents = readFile(is).trim();
				
				if (contents.isEmpty()) {
					
					byte[] soundJSON = ("{\r\n" + "   \"minecraftvideo\":{\r\n" + "      \"sounds\":[\r\n"
							+ "         \"audio\"\r\n" + "      ]\r\n" + "   }\r\n" + "}").getBytes();
					zos.write(soundJSON);
					
				} else {
					
					String json = "	,\"minecraftvideo\": {\r\n" + "		\"sounds\": [\r\n" + "			\"audio\"\r\n"
							+ "		]\r\n" + "	}";

					StringBuffer sb = new StringBuffer(contents);
					sb.insert(sb.length() - 2, json);
					zos.write(sb.toString().getBytes());
					
				}
				
				exists = true;
				
			} else {
				
				zos.putNextEntry(next);
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
				exists = true;
				is.close();
				
			}
			
			if (next.isDirectory() && next.getName().equals("sounds")) {

				zos.putNextEntry(new ZipEntry("audio.ogg"));
				zos.write(Files.readAllBytes(v.sound.toPath()));

			}

			zos.closeEntry();
			
		}
		
		if (!exists) {
			
			byte[] soundJSON = ("{\r\n" + "   \"minecraftvideo\":{\r\n" + "      \"sounds\":[\r\n"
					+ "         \"audio\"\r\n" + "      ]\r\n" + "   }\r\n" + "}").getBytes();
			ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
			zos.putNextEntry(sound);
			zos.write(soundJSON);
			zos.closeEntry();
			
		}
		
		zos.close();
		zipFile.close();
		
	}

	public static String readFile(InputStream file) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(file));
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[8192];
		int read;
		while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
			builder.append(buffer, 0, read);
		}
		return builder.toString();

	}

}
