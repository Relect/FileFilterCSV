import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class MainTest {
    private final String zipPath = "source_archive.zip";
    private final String unzipDir = "C:\\temp";
    private final String file1Csv = "1.csv";
    private final String file2Csv = "2.csv";

    @Test
    void unzipAndCreateJSON() {
        Main.unzipAndCreateJSON( zipPath, unzipDir);
    }

    @Before
    void createArchivCsv() {
        try (
                BufferedWriter writer1 = Files.newBufferedWriter(Paths.get(file1Csv));
                CSVPrinter csvPrinter1 = new CSVPrinter(writer1, CSVFormat.DEFAULT);
                BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(file2Csv));
                CSVPrinter csvPrinter2 = new CSVPrinter(writer2, CSVFormat.DEFAULT)
        ) {
            csvPrinter1.printRecord("mark01", "1");
            csvPrinter1.printRecord("markFX", "5");

            csvPrinter2.printRecord("mark01", "9");
            csvPrinter2.printRecord("mark17", "17");
            csvPrinter2.printRecord("markFX", "50");

            csvPrinter1.flush();
            csvPrinter2.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (
                ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipPath));
                FileInputStream fis1= new FileInputStream(file1Csv);
                FileInputStream fis2= new FileInputStream(file2Csv)
        ){
            ZipEntry entry1 =new ZipEntry(file1Csv);
            zout.putNextEntry(entry1);
            byte[] buffer1 = new byte[fis1.available()];
            fis1.read(buffer1);
            // добавляем содержимое к архиву
            zout.write(buffer1);
            // закрываем текущую запись для новой записи
            zout.closeEntry();

            ZipEntry entry2=new ZipEntry(file2Csv);
            zout.putNextEntry(entry2);
            byte[] buffer2 = new byte[fis2.available()];
            fis2.read(buffer2);
            zout.write(buffer2);
            zout.closeEntry();

        } catch (IOException ex) {
           ex.printStackTrace();
        }
    }
}