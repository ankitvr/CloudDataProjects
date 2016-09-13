package iw.ie.reporting.reports.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
  /**
   * Zip a list of file into one zip file.
   * 
   * @param files
   *          files to zip
   * @param targetZipFile
   *          target zip file
   * @throws IOException
   *           IO error exception can be thrown when copying ...
   */
  public static void zipFile(final List<File> files, final File targetZipFile) throws IOException {
    try {
      FileOutputStream   fos = new FileOutputStream(targetZipFile);
      ZipOutputStream zos = new ZipOutputStream(fos);
      byte[] buffer = new byte[128];
      for (File currentFile: files) {
        if (!currentFile.isDirectory()) {
          ZipEntry entry = new ZipEntry(currentFile.getName());
          FileInputStream fis = new FileInputStream(currentFile);
          zos.putNextEntry(entry);
          int read = 0;
          while ((read = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, read);
          }
          zos.closeEntry();
          fis.close();
        }
      }
      zos.close();
      fos.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found : " + e);
    }

  }
}