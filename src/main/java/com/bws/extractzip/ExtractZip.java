/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bws.extractzip;

import java.io.*;
import java.util.zip.*;

/**
 *
 * @author adminapp
 */
public class ExtractZip {

    public static void main(String[] args) {

        File folder = new File("D:/testextract");
        File[] files = folder.listFiles();
        String destDirectory = "D:/extractedzip";
        String strucDirectory = "D:/structuredfolder";
        byte[] buffer = new byte[1024];

        if (folder.canRead() == false && folder.canWrite() == false) {
            folder.setReadable(true);
            folder.setWritable(true);
            System.out.println("can be read and written");
        } else if (folder.canRead() == false && folder.canWrite() == true) {
            folder.setReadable(true);
            System.out.println("can be read and written is denied");
        } else if (folder.canWrite() == false && folder.canRead() == true) {
            folder.setWritable(true);
            System.out.println("can be written and read is denied");
        } else {
            System.out.println("Folder is given full control access= " + folder.canRead());
            try {
                // Buat objek ZipInputStream untuk membaca file ZIP
                for (File file : files) {
                    if (file.isFile()) {
//                System.out.println("cek inside= "+file.getName());
                    }
                    File zipfile = new File("D:/testextract/" + file.getName());
                    FileInputStream fis = new FileInputStream(zipfile);
                    ZipInputStream zis = new ZipInputStream(fis);
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        // Dapatkan nama file ZIP entry
                        String fileName = zipEntry.getName();
                        File newFile = new File(destDirectory + File.separator + fileName);
                        // Buat folder jika tidak ada
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        // Baca data dari ZIP entry dan tulis ke file output
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        // Tutup file output stream
                        fos.close();
                        // Pindah ke ZIP entry berikutnya
                        zipEntry = zis.getNextEntry();
                    }
                    // Tutup ZipInputStream
                    zis.closeEntry();
                    zis.close();
                    fis.close();
                    System.out.println("File ZIP berhasil diekstrak!");
                    
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
