/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extractzip;

import Koneksi.Koneksi;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adminapp
 */
public class ExtractZip {

    static Connection connServerLama = new Koneksi().getConnectionServerLama();
    static Connection connServerBaru = new Koneksi().getConnectionServerBaru();

    public static void main(String[] args) throws SQLException {

        Date date = new Date();
        SimpleDateFormat formatteryear = new SimpleDateFormat("yyyy");
        LocalDate tanggal = LocalDate.now();
        DateTimeFormatter pola = DateTimeFormatter.ofPattern("MMMM", new Locale("id"));
//        String namaBulan = tanggal.format(pola);
//        String year = formatteryear.format(date);
        String namaBulan = "";
        String year = "";
        File srcZipFolder = new File("D:/dokumenEHR/Slip_Payrol/");
        File[] files = srcZipFolder.listFiles();
        String extractedFileDir = "D:/extractedZIP";
        String dokumenSlipDir = "D:/dokumenslip";
        String namazip = "";

        byte[] buffer = new byte[1024];

        if (srcZipFolder.canRead() == false && srcZipFolder.canWrite() == false) {
            srcZipFolder.setReadable(true);
            srcZipFolder.setWritable(true);
            writeLog("Folder " + srcZipFolder + " not accessible at: ");
            System.out.println("can be read and written");
        } else if (srcZipFolder.canRead() == false && srcZipFolder.canWrite() == true) {
            srcZipFolder.setReadable(true);
            writeLog("Folder " + srcZipFolder + " not accessible at: ");
            System.out.println("can be read and written is denied");
        } else if (srcZipFolder.canWrite() == false && srcZipFolder.canRead() == true) {
            srcZipFolder.setWritable(true);
            writeLog("Folder " + srcZipFolder + " not accessible at: ");
            System.out.println("can be written and read is denied");
        } else {
            writeLog("Folder " + srcZipFolder + " accessible at: ");
            System.out.println("Folder is given full control access= " + srcZipFolder.canRead());
            try {
                // Buat objek ZipInputStream untuk membaca file ZIP\

                int flag_process_copy;
                for (File file : files) {
                    if (file.isFile()) {
                    }
                    System.out.println("file " + file);
                    File zipfile = new File(srcZipFolder + File.separator + file.getName());
                    FileInputStream fis = new FileInputStream(zipfile);
                    ZipInputStream zis = new ZipInputStream(fis);
                    ZipEntry zipEntry = zis.getNextEntry();
                    System.out.println("zipEntry " + file.getName());

                    while (zipEntry != null) {
                        writeLog("Start extracting file " + zipfile + " at: ");
                        //  nama file ZIP entry
                        String fileName = zipEntry.getName();
                        File newFile = new File(extractedFileDir + File.separator + fileName);
                        // Buat srcZipFolder jika tidak ada
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        // Baca data dari ZIP entry dan tulis ke file output
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        PreparedStatement prep12 = connServerBaru.prepareStatement("SELECT count(namazipfile) as jmldata FROM fileslipgajizip where namazipfile = '" + file.getName() + "'and flag_process_upload = 0");
                        int hasilsqlCarix = 0;
                        int jmldatacarix = 0;
                        ResultSet rse12 = prep12.executeQuery();
                        while (rse12.next()) {
                            jmldatacarix = rse12.getInt(1);
                        }

                        rse12.close();
                        prep12.close();

                        if (jmldatacarix != 0) {
                            PreparedStatement sqlCarix = connServerBaru.prepareStatement("SELECT * FROM fileslipgajizip where namazipfile = '" + file.getName() + "' and flag_process_upload = 0");

                            ResultSet rsec2 = sqlCarix.executeQuery();
                            while (rsec2.next()) {
                                namazip = rsec2.getString("namazipfile");
                                namaBulan = rsec2.getString("bulan");
                                year = rsec2.getString("tahun");
                            }

                            rsec2.close();
                            sqlCarix.close();
                            hasilsqlCarix = 0;
                        }

                        flag_process_copy = 1;
                        // Tutup file output stream
                        String found = zipEntry.getName();
                        int idxfound = found.indexOf("-");

                        System.out.println("Zip File Source " + found);
                        System.out.println("idxfound " + idxfound);
                        String nik = found.substring(0, 8);
                        String dest = extractedFileDir + "/" + found;
//                        String newStructured = dokumenSlipDir + "/" + nik + "/slipgaji" + "/" + year + "/" + namaBulan;
                        String newStructured = dokumenSlipDir + "/" + nik + "/slipgaji" + "/" + year;
                        String slipGajiFolder = dokumenSlipDir + "/" + nik + "/slipgaji";
                        File distributedFolder = new File(newStructured);
                        File slipGajiDirectory = new File(slipGajiFolder);
                        File sourceDirectory = new File(extractedFileDir + "/" + found);
                        System.out.println("source directory " + sourceDirectory);
                        System.out.println("destination directory " + distributedFolder);
                        File source = new File(extractedFileDir, found);
                        File newDest = new File(newStructured, source.getName());

                        if (!distributedFolder.exists()) {
                            int flag_distribusi;
                            String status = "";
                            String caused_by = "";
                            boolean success = distributedFolder.mkdirs();

                            if (success) {
                                writeLog("Start creating distributed srcZipFolder by the name of " + newStructured + " at: ");
                                System.out.println("New Distributed Folder " + distributedFolder + " created");
                                try {
                                    FileInputStream input = new FileInputStream(source);
                                    FileOutputStream output = new FileOutputStream(newDest);
                                    byte[] buffere = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = input.read(buffere)) != -1) {
                                        output.write(buffere, 0, bytesRead);
                                    }

                                    System.out.println(source.getName() + " copied to " + distributedFolder.getPath());

                                    input.close();
                                    output.close();
                                    flag_distribusi = 1;
                                    status = "Success";
                                    caused_by = "";
                                    System.out.println("TAHUN = " + year);
                                    System.out.println("BULAN = " + namaBulan);

                                    PreparedStatement prep0 = connServerBaru.prepareStatement("SELECT count(nik) as jmldata FROM fileslipgaji "
                                            + "where nik='" + nik + "' and gajiperiodetahun='" + year + "' and gajiperiodebulan = '" + namaBulan + "'");
//                                    PreparedStatement prep0 = connServerBaru.prepareStatement("SELECT count(namazipfile) as jmldata FROM fileslipgajizip where namazipfile = '" + file.getName() + "'and flag_process_upload = 0");
                                    int hasilsqlInsert = 0;
                                    int hasilsqlActivity = 0;
                                    int hasilsqlUpdateFlag = 0;
                                    int jmldatacari = 0;
                                    ResultSet rse0 = prep0.executeQuery();
                                    while (rse0.next()) {
                                        jmldatacari = rse0.getInt(1);
                                    }
                                    System.out.println("jumlahdatacari out " + jmldatacari);
                                    rse0.close();
                                    prep0.close();

                                    if (jmldatacari == 0) {
                                        PreparedStatement sqlInsert = connServerBaru.prepareStatement("INSERT INTO fileslipgaji(nik, gajiperiodetahun, gajiperiodebulan, "
                                                + "namafolder, location, files, flag_process_copy, flag_distribusi, created_at, updated_at)"
                                                + " values ('" + nik + "','" + year + "','" + namaBulan + "','" + dest + "','" + newStructured + "','" + fileName + "',"
                                                + "'" + flag_process_copy + "','" + flag_distribusi + "', now(), now())");
                                        hasilsqlInsert = sqlInsert.executeUpdate();
                                        sqlInsert.close();
                                        hasilsqlInsert = 0;
                                        writeLog("Write distribution process to database . Started at: ");

                                        PreparedStatement sqlActivity = connServerBaru.prepareStatement("INSERT INTO t_log_aktifitas(nik, action, status, "
                                                + "caused_by, created_at, updated_at)"
                                                + " values ('" + nik + "','distributing files slip gaji milik " + nik + " periode " + namaBulan + " / " + year + " ', '" + status + "','" + caused_by + "', now(), now())");
                                        hasilsqlActivity = sqlActivity.executeUpdate();
                                        sqlActivity.close();
                                        hasilsqlActivity = 0;

                                        PreparedStatement sqlUpdateFlagUpload = connServerBaru.prepareStatement("UPDATE fileslipgajizip SET flag_process_upload = 1 where namazipfile = '" + file.getName() + "'");
                                        hasilsqlUpdateFlag = sqlUpdateFlagUpload.executeUpdate();
                                        sqlUpdateFlagUpload.close();
                                        hasilsqlUpdateFlag = 0;
                                        writeLog("Update flag upload . Started at: ");

//                                        try {
//                                            SendtoNotifyService(nik);
//                                            writeLog("Send notification email to " + nik + " . Started at: ");
//                                        } catch (InterruptedException ex) {
//                                            Logger.getLogger(ExtractZip.class.getName()).log(Level.SEVERE, null, ex);
//                                        }

                                    } else {
                                        System.out.println("jumlahdatacari else " + jmldatacari);
//                                        PreparedStatement sqUpdate = connServerBaru.prepareStatement("UPDATE fileslipgaji SET nik = '" + nik + "', gajiperiodetahun = '" + year + "',"
//                                                + "gajiperiodebulan = '" + namaBulan + "', namafolder = '" + dest + "', location = '" + newStructured + "', files = '" + fileName + "',"
//                                                + " flag_process_copy = '" + flag_process_copy + "', flag_distribusi = '" + flag_distribusi + "', updated_at = now()");
//                                        hasilsqlInsert = sqUpdate.executeUpdate();
//                                        sqUpdate.close();
//                                        hasilsqlInsert = 0;
//                                        writeLog("Write distribution update process to database . Started at: ");

                                        PreparedStatement sqlInsert = connServerBaru.prepareStatement("INSERT INTO fileslipgaji(nik, gajiperiodetahun, gajiperiodebulan, "
                                                + "namafolder, location, files, flag_process_copy, flag_distribusi, created_at, updated_at)"
                                                + " values ('" + nik + "','" + year + "','" + namaBulan + "','" + dest + "','" + newStructured + "','" + fileName + "',"
                                                + "'" + flag_process_copy + "','" + flag_distribusi + "', now(), now())");
                                        hasilsqlInsert = sqlInsert.executeUpdate();
                                        sqlInsert.close();
                                        hasilsqlInsert = 0;
                                        writeLog("Write distribution process to database . Started at: ");

                                        PreparedStatement sqlActivity = connServerBaru.prepareStatement("INSERT INTO t_log_aktifitas(nik, action, status, "
                                                + "caused_by, created_at, updated_at)"
                                                + " values ('" + nik + "','distributing files slip gaji milik " + nik + " periode " + namaBulan + " / " + year + " ', '" + status + "','" + caused_by + "', now(), now())");
                                        hasilsqlActivity = sqlActivity.executeUpdate();
                                        sqlActivity.close();
                                        hasilsqlActivity = 0;

                                        PreparedStatement sqlUpdateFlagUpload = connServerBaru.prepareStatement("UPDATE fileslipgajizip SET flag_process_upload = 1 where namazipfile = '" + file.getName() + "'");
                                        hasilsqlUpdateFlag = sqlUpdateFlagUpload.executeUpdate();
                                        sqlUpdateFlagUpload.close();
                                        hasilsqlUpdateFlag = 0;
                                        writeLog("Update flag upload . Started at: ");
                                    }
                                } catch (IOException e) {
                                    flag_distribusi = 0;
                                    status = "Failed";
                                    caused_by = e.toString();
                                    e.printStackTrace();
                                }

                            } else {
                                System.out.println("Failed to created distributed srcZipFolder");
                                writeLog("Failed to created distributed srcZipFolder by the name of " + newStructured + " at: ");
                            }
                        } else {

                            System.out.println("Distributed srcZipFolder already exist");
                            writeLog("Distributed srcZipFolder " + newStructured + " already exist. at: ");
                            int flag_distribusi;
                            String status = "";
                            String caused_by = "";
//                            try {
//                                writeLog("Start distributing file " + source.getName() + " to " + distributedFolder.getPath() + " at: ");
//                                FileInputStream input = new FileInputStream(source);
//                                FileOutputStream output = new FileOutputStream(newDest);
//                                byte[] buffere = new byte[1024];
//                                int bytesRead;
//                                while ((bytesRead = input.read(buffere)) != -1) {
//                                    output.write(buffere, 0, bytesRead);
//                                }

                            flag_distribusi = 1;
                            status = "Success";
                            caused_by = "";
                            writeLog("End the file distributing process at: ");

                            System.out.println("bulan dibuai awan = " + namaBulan + "/" + year);

                            PreparedStatement prep0 = connServerBaru.prepareStatement("SELECT count(nik) as jmldata FROM fileslipgaji "
                                    + "where nik='" + nik + "' and gajiperiodetahun='" + year + "' and gajiperiodebulan = '" + namaBulan + "'");
//                                    PreparedStatement prep0 = connServerBaru.prepareStatement("SELECT count(namazipfile) as jmldata FROM fileslipgajizip where namazipfile = '" + file.getName() + "'and flag_process_upload = 0");
                            int hasilsqlInsert = 0;
                            int hasilsqlActivity = 0;
                            int hasilsqlUpdateFlag = 0;
                            int jmldatacari = 0;
                            ResultSet rse0 = prep0.executeQuery();
                            while (rse0.next()) {
                                jmldatacari = rse0.getInt(1);
                            }
                            System.out.println("jumlahdatacari out " + jmldatacari);
                            rse0.close();
                            prep0.close();

                            if (jmldatacari == 0) {
                                PreparedStatement sqlInsert = connServerBaru.prepareStatement("INSERT INTO fileslipgaji(nik, gajiperiodetahun, gajiperiodebulan, "
                                        + "namafolder, location, files, flag_process_copy, flag_distribusi, created_at, updated_at)"
                                        + " values ('" + nik + "','" + year + "','" + namaBulan + "','" + dest + "','" + newStructured + "','" + fileName + "',"
                                        + "'" + flag_process_copy + "','" + flag_distribusi + "', now(), now())");
                                hasilsqlInsert = sqlInsert.executeUpdate();
                                sqlInsert.close();
                                hasilsqlInsert = 0;
                                writeLog("Write distribution process to database . Started at: ");

                                PreparedStatement sqlActivity = connServerBaru.prepareStatement("INSERT INTO t_log_aktifitas(nik, action, status, "
                                        + "caused_by, created_at, updated_at)"
                                        + " values ('" + nik + "','distributing files slip gaji milik " + nik + " periode " + namaBulan + " / " + year + " ', '" + status + "','" + caused_by + "', now(), now())");
                                hasilsqlActivity = sqlActivity.executeUpdate();
                                sqlActivity.close();
                                hasilsqlActivity = 0;

                                PreparedStatement sqlUpdateFlagUpload = connServerBaru.prepareStatement("UPDATE fileslipgajizip SET flag_process_upload = 1 where namazipfile = '" + file.getName() + "'");
                                hasilsqlUpdateFlag = sqlUpdateFlagUpload.executeUpdate();
                                sqlUpdateFlagUpload.close();
                                hasilsqlUpdateFlag = 0;
                                writeLog("Update flag upload . Started at: ");

//                                    try {
//                                        SendtoNotifyService(nik);
//                                        writeLog("Send notification email to " + nik + " . Started at: ");
//                                    } catch (InterruptedException ex) {
//                                        Logger.getLogger(ExtractZip.class.getName()).log(Level.SEVERE, null, ex);
//                                    }
                            } else {
                                PreparedStatement sqlInsert = connServerBaru.prepareStatement("INSERT INTO fileslipgaji(nik, gajiperiodetahun, gajiperiodebulan, "
                                        + "namafolder, location, files, flag_process_copy, flag_distribusi, created_at, updated_at)"
                                        + " values ('" + nik + "','" + year + "','" + namaBulan + "','" + dest + "','" + newStructured + "','" + fileName + "',"
                                        + "'" + flag_process_copy + "','" + flag_distribusi + "', now(), now())");
                                hasilsqlInsert = sqlInsert.executeUpdate();
                                sqlInsert.close();
                                hasilsqlInsert = 0;
                                writeLog("Write distribution process to database . Started at: ");

                                PreparedStatement sqlActivity = connServerBaru.prepareStatement("INSERT INTO t_log_aktifitas(nik, action, status, "
                                        + "caused_by, created_at, updated_at)"
                                        + " values ('" + nik + "','distributing files slip gaji milik " + nik + " periode " + namaBulan + " / " + year + " ', '" + status + "','" + caused_by + "', now(), now())");
                                hasilsqlActivity = sqlActivity.executeUpdate();
                                sqlActivity.close();
                                hasilsqlActivity = 0;

                                PreparedStatement sqlUpdateFlagUpload = connServerBaru.prepareStatement("UPDATE fileslipgajizip SET flag_process_upload = 1 where namazipfile = '" + file.getName() + "'");
                                hasilsqlUpdateFlag = sqlUpdateFlagUpload.executeUpdate();
                                sqlUpdateFlagUpload.close();
                                hasilsqlUpdateFlag = 0;
                                writeLog("Update flag upload . Started at: ");
                                writeLog("Record by NIK " + nik + " has been found . Started at: ");
                            }
//                                input.close();
//                                output.close();

                            System.out.println(source.getName() + " copied to " + distributedFolder.getPath());

//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }

                        fos.close();
                        // Pindah ke ZIP entry berikutnya
                        zipEntry = zis.getNextEntry();
                        flag_process_copy = 0;

                        writeLog("End the file extracting process at: ");
                        writeLogUnderline("_______________________________");
                    }

                    // Tutup ZipInputStream
                    zis.closeEntry();
                    zis.close();
                    fis.close();

                    String sourceFilePath = "D:/dokumenEHR/Slip_Payrol/" + file.getName();
                    String destinationFilePath = "D:/dokumenEHRProcessed/" + file.getName();
                    Path sourcePath = Paths.get(sourceFilePath);
                    Path destinationPath = Paths.get(destinationFilePath);
                    
                    System.out.println("namazipmove = "+file.getName());
                    System.out.println("destinationFilePathwww= "+ destinationFilePath);

                    try {
                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("File moved successfully.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("File ZIP berhasil diekstrak ke folder " + extractedFileDir);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    private static void writeLog(String ket) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String tgl = formatter.format(date);
//        System.out.println(tgl);
        try {
            FileWriter log = new FileWriter("D:/logslipgaji/logWholeProcessSlip.txt", true);
            BufferedWriter bw = new BufferedWriter(log);
            bw.newLine();
            bw.write(ket + " " + tgl);
            bw.close();
        } catch (IOException e) {
            System.out.println("Failed to Create Log");
        }
    }

    private static void writeLogUnderline(String ket) {
        try {
            FileWriter log = new FileWriter("D:/logslipgaji/logWholeProcessSlip.txt", true);
            BufferedWriter bw = new BufferedWriter(log);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println("Failed to Create Log");
        }
    }

    private static HttpURLConnection con;

    public static void SendtoNotifyService(String nik) throws IOException, InterruptedException {
        String url = "http://172.28.97.10:8081/E-HR.v.02/notification/notification_gajian/" + nik;
        String urlParameters = "";
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {

                wr.write(postData);
                wr.flush();
            }

            StringBuilder content;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

//            System.out.println(content.toString());
        } finally {

            con.disconnect();
        }
//        return "OK";
    }
}
