/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.misc;

 

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
 

/**
 *
 * @author THiNk
 */
public class MiscUtils {

    private String osName = "linux";

    public MiscUtils() {
        Properties props = System.getProperties(); //获得系统属性集    
        osName = props.getProperty("os.name"); //操作系统名称   
    }

     

    public void createDir(String dstPath) {

        Path newdir = FileSystems.getDefault().getPath(dstPath);
     
        boolean pathExists = Files.exists(newdir,
                new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (!pathExists) {
            Set<PosixFilePermission> perms = PosixFilePermissions
                    .fromString("rwxrwxrwx");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
                    .asFileAttribute(perms);
            try {
                if (osName.indexOf("Windows")==-1) {
                    Files.createDirectories(newdir, attr);
                } else {
                    Files.createDirectories(newdir);
                }
            } catch (Exception e) {
                System.err.println(e);

            }
        }
    }

    public static synchronized boolean moveFile(String src, String dst) {
        boolean ret = true;
        try {
            Path source = Paths.get(src);
            Path target = Paths.get(dst);
            Files.move(source, target, REPLACE_EXISTING, ATOMIC_MOVE);
            ret = true;
        } catch (IOException ex) {
            Logger.getLogger(MiscUtils.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        return ret;
    }

    public static long fileSize(String src) {
        long ret = 0L;
        try {
            Path source = Paths.get(src);

            ret = Files.size(source);

        } catch (IOException ex) {
            Logger.getLogger(MiscUtils.class.getName()).log(Level.SEVERE, null, ex);
            ret = 0L;
        }
        return ret;
    }

}
