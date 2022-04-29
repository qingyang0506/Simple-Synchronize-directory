import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.LinkOption.*;

public class sync {

    // sync two directory dir1,dir2,the input is the directory name
    public static void sync(String dir11, String dir22) {

        File dir1 = new File(dir11);
        File dir2 = new File(dir22);

        // if the dir1 is not exist,create a new directory
        if (!dir1.exists()) {
            dir1.mkdirs();
            updateSyn(dir1.getAbsolutePath());
        }
        // if the dir2 is not exist,create a new directory
        if (!dir2.exists()) {
            dir2.mkdirs();
            updateSyn(dir2.getAbsolutePath());
        }

        // first to loop the dir1 files
        File[] files1 = dir1.listFiles();
        File[] files2 = dir2.listFiles();

        JsonParser parser = new JsonParser();

        for (int i = 0; i < files1.length; i++) {
            File f = files1[i];
            String name = f.getName();
            if (f.isHidden()) {
                continue;
            }

            if (f.isFile()) {

                File t = new File(dir2.getAbsolutePath() + "/" + f.getName());
                if (t.exists()) {

                    // compare their SHA-256

                    try {

                        JsonObject obj1 = (JsonObject) parser.parse(new FileReader(dir1.getAbsolutePath() +
                                "/" + ".sync"));
                        JsonObject obj2 = (JsonObject) parser.parse(new FileReader(dir2.getAbsolutePath() +
                                "/" + ".sync"));

                        JsonArray a1 = obj1.get(name).getAsJsonArray();
                        JsonArray a2 = obj2.get(name).getAsJsonArray();

                        String s1 = a1.get(0).getAsJsonArray().get(1).getAsString();
                        String s2 = a2.get(0).getAsJsonArray().get(1).getAsString();

                        if (s1.equals(s2)) {

                            Date d1 = stringtoDate(a1.get(0).getAsJsonArray().get(0).getAsString());
                            Date d2 = stringtoDate(a2.get(0).getAsJsonArray().get(0).getAsString());

                            if (d1.before(d2)) {
                                t.setLastModified(d1.getTime());
                            } else if (d2.before(d1)) {
                                f.setLastModified(d2.getTime());
                            }

                        } else {

                            // check whether one of SHA_256 value is the old version of another

                            Date d1 = stringtoDate(a1.get(0).getAsJsonArray().get(0).getAsString());
                            Date d2 = stringtoDate(a2.get(0).getAsJsonArray().get(0).getAsString());

                            JsonArray array1 = obj1.get(name).getAsJsonArray();
                            JsonArray array2 = obj2.get(name).getAsJsonArray();

                            if (checkExistSameSHA_256(array2, s1)) {
                                copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                                f.setLastModified(d2.getTime());
                            } else if (checkExistSameSHA_256(array1, s2)) {
                                copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                                t.setLastModified(d1.getTime());
                            } else {
                                if (d1.after(d2)) {
                                    copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                                    t.setLastModified(d1.getTime());
                                } else {
                                    copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                                    f.setLastModified(d2.getTime());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JsonObject obj2 = (JsonObject) parser.parse(new FileReader(dir2.getAbsolutePath() +
                                "/" + ".sync"));
                        if (obj2.get(name) == null) {
                            copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                        } else {

                            // check is deleted or readd to the directory

                            JsonObject obj1 = (JsonObject) parser.parse(new FileReader(dir1.getAbsolutePath() +
                                    "/" + ".sync"));
                            JsonArray a1 = obj1.get(name).getAsJsonArray();
                            JsonArray a2 = obj2.get(name).getAsJsonArray();

                            if (a1.size() > a2.size()) {
                                // directly add this file to another directory
                                copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                            } else {
                                f.delete();
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } else if (f.isDirectory()) {
                // recursive to achieve directory
                sync(dir1.getAbsolutePath() + "/" + f.getName(), dir2.getAbsolutePath() + "/" + f.getName());
            }

        }

        // loop the dir2 files
        for (int i = 0; i < files2.length; i++) {
            File f = files2[i];
            String name = f.getName();
            if (f.isHidden()) {
                continue;
            }

            if (f.isFile()) {

                File t = new File(dir1.getAbsolutePath() + "/" + f.getName());
                if (t.exists()) {

                    // compare their SHA-256

                    try {

                        JsonObject obj1 = (JsonObject) parser.parse(new FileReader(dir1.getAbsolutePath() +
                                "/" + ".sync"));
                        JsonObject obj2 = (JsonObject) parser.parse(new FileReader(dir2.getAbsolutePath() +
                                "/" + ".sync"));

                        JsonArray a1 = obj1.get(name).getAsJsonArray();
                        JsonArray a2 = obj2.get(name).getAsJsonArray();

                        String s1 = a1.get(0).getAsJsonArray().get(1).getAsString();
                        String s2 = a2.get(0).getAsJsonArray().get(1).getAsString();

                        if (s1.equals(s2)) {

                            Date d1 = stringtoDate(a1.get(0).getAsJsonArray().get(0).getAsString());
                            Date d2 = stringtoDate(a2.get(0).getAsJsonArray().get(0).getAsString());

                            if (d1.before(d2)) {
                                f.setLastModified(d1.getTime());
                            } else if (d2.before(d1)) {
                                t.setLastModified(d2.getTime());
                            }

                        } else {

                            // check whether one of SHA_256 value is the old version of another

                            Date d1 = stringtoDate(a1.get(0).getAsJsonArray().get(0).getAsString());
                            Date d2 = stringtoDate(a2.get(0).getAsJsonArray().get(0).getAsString());

                            JsonArray array1 = obj1.get(name).getAsJsonArray();
                            JsonArray array2 = obj2.get(name).getAsJsonArray();

                            if (checkExistSameSHA_256(array2, s1)) {
                                copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                                t.setLastModified(d2.getTime());
                            } else if (checkExistSameSHA_256(array1, s2)) {
                                copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                                f.setLastModified(d1.getTime());
                            } else {
                                if (d1.after(d2)) {
                                    copyFile(dir1.getAbsolutePath() + "/" + name, dir2.getAbsolutePath() + "/" + name);
                                    f.setLastModified(d1.getTime());
                                } else {
                                    copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                                    t.setLastModified(d2.getTime());
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    try {
                        JsonObject obj1 = (JsonObject) parser.parse(new FileReader(dir1.getAbsolutePath() +
                                "/" + ".sync"));
                        if (obj1.get(name) == null) {
                            copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                        } else {

                            // check is deleted or readd to the directory

                            JsonObject obj2 = (JsonObject) parser.parse(new FileReader(dir2.getAbsolutePath() +
                                    "/" + ".sync"));
                            JsonArray a2 = obj2.get(name).getAsJsonArray();

                            JsonArray a1 = obj1.get(name).getAsJsonArray();

                            if (a2.size() > a1.size()) {
                                // directly add this file to another directory
                                copyFile(dir2.getAbsolutePath() + "/" + name, dir1.getAbsolutePath() + "/" + name);
                            } else {
                                f.delete();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else if (f.isDirectory()) {

                sync(dir1.getAbsolutePath() + "/" + f.getName(), dir2.getAbsolutePath() + "/" + f.getName());
            }
        }
    }

    // copy file1 to file2 the input is full file path
    public static void copyFile(String fileName1, String fileName2) {
        try {
            Files.copy(new File(fileName1).toPath(),
                    new File(fileName2).toPath(),
                    REPLACE_EXISTING, COPY_ATTRIBUTES, NOFOLLOW_LINKS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // check another sync file's key whether have the same value of SHA-256
    public static boolean checkExistSameSHA_256(JsonArray jarray, String SHA_256) {

        for (int i = 0; i < jarray.size(); i++) {
            if (jarray.get(i).getAsJsonArray().get(1).getAsString().equals(SHA_256)) {
                return true;
            }
        }

        return false;
    }

    // update directory synchronic file
    public static void updateSyn(String pathname) {

        File file = new File(pathname);

        if (!file.exists()) {
            file.mkdirs();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File[] fileArr = file.listFiles();

        JsonObject jobject = new JsonObject();
        JsonParser parser = new JsonParser();

        try {

            File sync = new File(pathname + "/" + ".sync");
            if (!sync.exists()) {
                sync.createNewFile();
            }

            if (sync.length() == 0 && fileArr.length == 0) {

            } else if (sync.length() == 0 && fileArr.length != 0) {
                for (int i = 0; i < fileArr.length; i++) {
                    File f = fileArr[i];
                    if (f.isHidden()) {
                        continue;
                    } else if (f.isFile()) {
                        String name = f.getName();
                        JsonArray array = new JsonArray();
                        JsonArray a1 = new JsonArray();
                        a1.add(getLastModified(pathname + "/" + name));
                        a1.add(getSHA_256(pathname + "/" + name));
                        array.add(a1);
                        jobject.add(name, array);
                    } else {
                        updateSyn(pathname + "/" + f.getName());
                    }
                }
            } else if (sync.length() != 0) {

                JsonObject object = (JsonObject) parser.parse(new FileReader(pathname + "/" + ".sync"));

                Set<Map.Entry<String, JsonElement>> entries = object.entrySet();

                List<String> list = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : entries) {
                    list.add(entry.getKey());
                }

                for (int i = 0; i < fileArr.length; i++) {
                    File f = fileArr[i];
                    if (f.isHidden()) {
                        continue;
                    } else if (f.isFile()) {
                        String name = f.getName();
                        if (object.get(name) == null) {
                            JsonArray array = new JsonArray();
                            JsonArray a1 = new JsonArray();
                            a1.add(getLastModified(pathname + "/" + name));
                            a1.add(getSHA_256(pathname + "/" + name));
                            array.add(a1);
                            jobject.add(name, array);
                        } else {
                            JsonArray array = object.get(name).getAsJsonArray();
                            JsonArray a1 = new JsonArray();
                            String SHA_256 = array.get(0).getAsJsonArray().get(1).getAsString();
                            String current = getSHA_256(pathname + "/" + name);
                            if (!current.equals(SHA_256)) {
                                JsonArray first = new JsonArray();
                                first.add(getLastModified(pathname + "/" + name));
                                first.add(getSHA_256(pathname + "/" + name));
                                a1.add(first);
                            } else {

                                // the digest is the same then compare the modified time
                                Date curDate = stringtoDate(getLastModified(pathname + "/" + name));
                                Date fileDate = stringtoDate(
                                        array.get(0).getAsJsonArray().get(0).getAsString().substring(0, 19));

                                if (!curDate.equals(fileDate)) {
                                    File curFile = new File(pathname + "/" + name);
                                    curFile.setLastModified(fileDate.getTime());
                                }
                            }
                            for (int j = 0; j < array.size(); j++) {
                                a1.add(array.get(j));
                            }
                            jobject.add(name, a1);
                        }
                    } else if (f.isDirectory()) {
                        updateSyn(pathname + "/" + f.getName());
                    }
                }

                for (int i = 0; i < list.size(); i++) {
                    File f = new File(pathname + "/" + list.get(i));
                    if (!f.exists()) {
                        JsonObject object1 = (JsonObject) parser.parse(new FileReader(pathname + "/" + ".sync"));
                        JsonArray array1 = object1.get(list.get(i)).getAsJsonArray();
                        JsonArray array = new JsonArray();
                        if (!array1.get(0).getAsJsonArray().get(1).getAsString().equals("deleted")) {

                            JsonArray a1 = new JsonArray();
                            a1.add(dateToString(new Date()));
                            a1.add("deleted");
                            array.add(a1);
                        }

                        for (int j = 0; j < array1.size(); j++) {
                            array.add(array1.get(j));
                        }

                        jobject.add(list.get(i), array);
                    }

                }

            }

            FileWriter fw = new FileWriter(pathname + "/" + ".sync");
            String s = gson.toJson(jobject);
            fw.write(s);
            fw.flush();
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // get file's last modified time
    public static String getLastModified(String pathname) {
        File f = new File(pathname);

        long lastModified = f.lastModified();

        Date date = new Date(lastModified);

        return dateToString(date);
    }

    // get the current file's SHA-256 value
    public static String getSHA_256(String pathname) {

        File file = new File(pathname);

        String str = "";

        try {
            str = getHash(file, "SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    // helper function for getSHA-256
    public static String getHash(File file, String hashType) throws Exception {
        InputStream fis = new FileInputStream(file);
        byte buffer[] = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        for (int numRead = 0; (numRead = fis.read(buffer)) > 0;) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }

    // helper function for getSHA-256
    public static String toHexString(byte b[]) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            sb.append(Integer.toHexString(aB & 0xFF));
        }
        return sb.toString();
    }

    // let time convert to the string that format we want
    public static String dateToString(Date time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ans = format.format(time);
        return ans + " +1200";
    }

    // let string convert to the date
    public static Date stringtoDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(str);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    public static void main(String[] args) {

        // get the two file name from the command line
        String dirPath1 = args[0];
        String dirPath2 = args[1];

        File dir1 = new File(dirPath1);
        File dir2 = new File(dirPath2);

        if (!dir1.exists() && !dir2.exists()) {
            System.out.println("Error: \nThe dir1 and dir2 not exists");
        } else if (dir1.isFile() || dir2.isFile()) {
            System.out.println("Error: \nThe name passed on the command line are not a directories");
        } else {
            // if dir1 exist we will update the synchronic file of dir1
            if (dir1.exists()) {
                updateSyn(dir1.getAbsolutePath());
            }
            // if dir2 exist we will update the synchronic file of dir2
            if (dir2.exists()) {
                updateSyn(dir2.getAbsolutePath());
            }

            // achieve two directory synchronic
            sync(dir1.getAbsolutePath(), dir2.getAbsolutePath());

            // After synchronic file we will update the sync file
            updateSyn(dir1.getAbsolutePath());
            updateSyn(dir2.getAbsolutePath());
        }

    }
}