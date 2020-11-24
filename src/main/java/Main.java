import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the full path to the archive and click \"enter\"");
        String zipPath = scanner.nextLine().trim();
        System.out.println("Enter the full path where to unpack the archive, JSON files and press \"enter\"");
        String unzipDir = scanner.nextLine().trim();

        Main.unzipAndCreateJSON(zipPath, unzipDir);
    }

    public static void unzipAndCreateJSON (String zipPath, String unzipDir) {
        File zipDirFiles = new File(unzipDir);
        if (!zipDirFiles.exists()) {
            try {
                zipDirFiles.mkdir();
                log.info(unzipDir + " Dir created");
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
        unzipDir = unzipDir + "\\";
        // Создаём список csv файлов для распаковки
        ArrayList<String> csvFileList = new ArrayList<>();
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            String name;
            while((entry=zin.getNextEntry())!=null){

                name = entry.getName(); // получим название файла

                // распаковка
                FileOutputStream fout = new FileOutputStream(unzipDir + name);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
                csvFileList.add(unzipDir+name); // добавляем файл в список csv файлов
                log.info("In archive file added is cvs List files: " + unzipDir + name);
            }
        } catch(Exception ex){
            log.error("exception", ex);
        }

        HashMap<Mark, List<Integer>> mapMark = new HashMap<>();
        Mark[] marks = Mark.values();
        for (Mark m: marks) {
            mapMark.put(m, null);
        }

        for (String filePath : csvFileList) {
            try {
                Scanner csvScanner = new Scanner(new FileInputStream(filePath), "UTF-8");
                while (csvScanner.hasNextLine()) {
                    String markStr = csvScanner.nextLine().split("#")[0];
                    if(markStr.contains(",")) {
                        Mark markKey = Mark.valueOf(markStr.split(",")[0].toLowerCase());
                        int markValue = Integer.parseInt(markStr.split(",")[1]);
                        List<Integer> markValueList = new ArrayList<>();
                        markValueList.add(markValue);
                        mapMark.merge(markKey, markValueList, (o, n) -> {
                            o.add(n.get(0));
                            return o;
                        });
                    }
                }
            } catch (IOException e) {
                log.error("exception",e);
            }
        }
        TreeMap<String, Integer> mapMark1 = new TreeMap<>();
        TreeMap<String, Integer> mapMark2 = new TreeMap<>();
        TreeMap<String, List<Integer>> mapMark3 = new TreeMap<>();
        mapMark.forEach((k, v) -> {
            String keyNew = null;
            switch (k) {
                case markfv:
                    keyNew = "markFV";
                    break;
                case markfx:
                    keyNew = "markFX";
                    break;
                case markft:
                    keyNew = "markFT";
                    break;
                default:
                    keyNew = k.toString();
                    break;
            }
            if (v != null) {
                Collections.sort(v);
                Collections.reverse(v);
                int sum = 0;
                for(Integer i : v) {
                    sum = sum + i;
                }
                mapMark1.put(keyNew, sum);
                mapMark2.put(keyNew, sum);
                mapMark3.put(keyNew, v);
            } else {
                mapMark2.put(keyNew, null);
            }
        });

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(unzipDir + "1.json"), mapMark1);
            log.info(unzipDir + "1.json created");
            mapper.writeValue(new File(unzipDir + "2.json"), mapMark2);
            log.info(unzipDir + "2.json created");
            mapper.writeValue(new File(unzipDir + "3.json"), mapMark3);
            log.info(unzipDir + "3.json created");
        } catch (IOException e) {
            log.error("exception mapper", e);
        }
    }
}



