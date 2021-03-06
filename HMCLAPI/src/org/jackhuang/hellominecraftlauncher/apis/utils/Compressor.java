/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jackhuang.hellominecraftlauncher.apis.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 文件压缩/解压类
 *
 * @author hyh
 */
public class Compressor {

    public static void zip(String sourceDir, String zipFile) throws IOException {
        zip(new File(sourceDir), zipFile);
    }

    /**
     * 功能：把 sourceDir 目录下的所有文件进行 zip 格式的压缩，保存为指定 zip 文件
     *
     * @param sourceDir
     * @param zipFile
     */
    public static void zip(File sourceDir, String zipFile) throws IOException {
        FileOutputStream os;
        os = new FileOutputStream(zipFile);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        ZipOutputStream zos = new ZipOutputStream(bos);
        String basePath = null;
        if (sourceDir.isDirectory()) {
            basePath = sourceDir.getPath();
        } else {//直接压缩单个文件时，取父目录  
            basePath = sourceDir.getParent();
        }
        zipFile(sourceDir, basePath, zos);
        zos.closeEntry();
        zos.close();
    }

    /**
     * 将文件压缩成zip文件
     *
     * @param source zip文件路径
     * @param basePath 待压缩文件根目录
     * @param zos
     */
    private static void zipFile(File source, String basePath,
            ZipOutputStream zos) throws IOException {
        File[] files = new File[0];
        if (source.isDirectory()) {
            files = source.listFiles();
        } else {
            files = new File[1];
            files[0] = source;
        }
        String pathName;//存相对路径(相对于待压缩的根目录)  
        byte[] buf = new byte[1024];
        int length = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                pathName = file.getPath().substring(basePath.length() + 1)
                        + "/";
                if (file.getName().toLowerCase().contains("meta-inf")) {
                    continue;
                }
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {
                pathName = file.getPath().substring(basePath.length() + 1);
                InputStream is = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));
                while ((length = bis.read(buf)) > 0) {
                    zos.write(buf, 0, length);
                }
                is.close();
            }
        }
    }

    public static void unzip(String zipFileName, String extPlace) throws IOException {
        unzip(zipFileName, new File(extPlace));
    }

    /**
     * 将文件压缩成zip文件
     *
     * @param zipFileName zip文件路径
     * @param extPlace 待压缩文件根目录
     */
    public static void unzip(String zipFileName, File extPlace) throws IOException {
        extPlace.mkdirs();
        ZipFile zipFile = new ZipFile(zipFileName);
        if (new File(zipFileName).exists()) {
            String strPath, gbkPath, strtemp;
            strPath = extPlace.getAbsolutePath();
            java.util.Enumeration e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEnt = (ZipEntry) e.nextElement();
                gbkPath = zipEnt.getName();
                if (zipEnt.isDirectory()) {
                    strtemp = strPath + File.separator + gbkPath;
                    File dir = new File(strtemp);
                    dir.mkdirs();
                    continue;
                } else {
                    //读写文件   
                    InputStream is = zipFile.getInputStream(zipEnt);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    gbkPath = zipEnt.getName();
                    strtemp = strPath + File.separator + gbkPath;
                    //建目录   
                    String strsubdir = gbkPath;
                    for (int i = 0; i < strsubdir.length(); i++) {
                        if (strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {
                            String temp = strPath + File.separator + strsubdir.substring(0, i);
                            File subdir = new File(temp);
                            if (!subdir.exists()) {
                                subdir.mkdir();
                            }
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(strtemp);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int c;
                    while ((c = bis.read()) != -1) {
                        bos.write((byte) c);
                    }
                    bos.close();
                    fos.close();
                }
            }
        }
        zipFile.close();
    }

    /**
     * 将zip1合并到zip2里面，即保留zip2
     *
     * @param destFile zip1
     * @param srcFile zip2
     */
    public static void merge(String destFile, String srcFile) throws IOException {
        ZipOutputStream os = new ZipOutputStream(new FileOutputStream(new File(destFile)));
        if (new File(destFile).exists()) {
            String extPlace = IOUtils.addSeparator(IOUtils.currentDir()) + "HMCL-MERGE-TEMP";
            unzip(srcFile, extPlace);
            ZipFile zipFile = new ZipFile(srcFile);
            if (new File(srcFile).exists()) {
                String strPath, gbkPath, strtemp;
                File tempFile = new File(extPlace);
                strPath = tempFile.getAbsolutePath();
                java.util.Enumeration e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry zipEnt = (ZipEntry) e.nextElement();
                    gbkPath = zipEnt.getName();
                    if (zipEnt.isDirectory()) {
                        strtemp = strPath + File.separator + gbkPath;
                        continue;
                    } else {
                        gbkPath = zipEnt.getName();
                        strtemp = strPath + File.separator + gbkPath;
                        os.putNextEntry(zipEnt);
                        os.write(gbkPath.getBytes("UTF-8"));
                    }
                }
            }
        }
        os.closeEntry();
        os.close();
    }
}
