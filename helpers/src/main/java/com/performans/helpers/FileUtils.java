package com.performans.helpers;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final long CHUNK_SIZE = 128;


    public static void writeFileOnInternalStorage(Context context, String fileName, File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            int c;
            String temp = "";
            while ((c = in.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
            writeFileOnInternalStorage(context, fileName, temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody) {
        File file = new File(mcoContext.getFilesDir(), "rsa");
        if (!file.exists()) {
            file.mkdir();
        }

        try {
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public static int split(String filename) throws FileNotFoundException, IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
        int partCount = 0;

        File f = new File(filename);
        long fileSize = f.length();

        int subfile;
        for (subfile = 0; subfile < fileSize / CHUNK_SIZE; subfile++) {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));
            for (int currentByte = 0; currentByte < CHUNK_SIZE; currentByte++) {
                out.write(in.read());
            }
            out.close();
            partCount++;
        }
        if (fileSize != CHUNK_SIZE * (subfile - 1)) {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            out.close();
            partCount++;
        }
        in.close();
        return partCount;
    }

    public static void join(String baseFilename) throws IOException {
        int numberParts = getNumberParts(baseFilename);

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(baseFilename));
        for (int part = 0; part < numberParts; part++) {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(baseFilename + "." + part));

            int b;
            while ((b = in.read()) != -1)
                out.write(b);

            in.close();
        }
        out.close();
    }

    public static int getNumberParts(String baseFilename) throws IOException {
        File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
        final String justFilename = new File(baseFilename).getName();
        assert directory != null;
        String[] matchingFiles = directory.list((dir, name) -> name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.\\d+$"));
        assert matchingFiles != null;
        return matchingFiles.length;
    }
}