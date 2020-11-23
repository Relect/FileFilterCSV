import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        // Создаём список csv файлов после распаковки
        ArrayList<String> csvFileList = new ArrayList<>();
        
        // считываем данные архива и пути распаковки
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите полный путь архива и нажмите enter");
        String zipPath = scanner.nextLine();
        zipPath = zipPath.trim();
        System.out.println("Введите полный путь куда распаковать архив, файлы JSON и нажмите enter");
        String unzipDir = scanner.nextLine() + "\\";
        unzipDir = unzipDir.trim();

        File createUnzipDir = new File(unzipDir);
        if (!createUnzipDir.exists()) {
            boolean result = false;
            try {
                createUnzipDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                se.printStackTrace();
            }
            if(result) {
                log.info(unzipDir + " Dir created");
            }
        }

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
                csvFileList.add(unzipDir+name); // добавляем файл в список файлов
                log.info("In archive file added is cvs List: " + unzipDir + name);
            }
        } catch(Exception ex){
            log.error("exception", ex);
        }

        HashMap<String, List<Integer>> mapMark = new HashMap<>();
        mapMark.put("mark01", null);
        mapMark.put("mark17", null);
        mapMark.put("mark23", null);
        mapMark.put("mark35", null);
        mapMark.put("markfv", null);
        mapMark.put("markfx", null);
        mapMark.put("markft", null);

        for (String filePath : csvFileList) {
            try {
                Scanner csvScanner = new Scanner(new FileInputStream(filePath), "UTF-8");
                while (csvScanner.hasNextLine()) {
                    String mark = csvScanner.nextLine().split("#")[0];
                    String markKey = mark.split(",")[0].toLowerCase();
                    if(mark.contains(",")) {
                        int markValue = Integer.parseInt(mark.split(",")[1]);
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
                case "markfv": keyNew = "markFV"; break;
                case "markfx": keyNew = "markFX"; break;
                case "markft": keyNew = "markFT"; break;
                default: keyNew = k; break;
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

