package cn.alien95.resthttp.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Utils;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class NetworkCache implements Cache {

    /**
     * 缓存的根目录
     */
    public static final File networkCacheRoot = Utils.getDiskCacheDir("NetworkCache");
    private List<File> cacheFiles;
    private static NetworkCache instance;

    private NetworkCache() {
        cacheFiles = Arrays.asList(networkCacheRoot.listFiles());
    }

    public static NetworkCache getInstance(){
        if(instance == null){
            instance = new NetworkCache();
        }
        return instance;
    }

    @Override
    public Entry get(String key) {
        return (Entry) readObjectFromFile(getCacheFile(key));
    }

    @Override
    public void put(String key, Entry entry) {
        File newFile = new File(networkCacheRoot, getCacheFileName(key));
        writeObjectToFile(entry, newFile);
        cacheFiles.add(newFile);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void invalidate(String key, boolean fullExpire) {

    }

    @Override
    public void remove(String key) {
        String fileName = getCacheFileName(key);
        for (File file : cacheFiles) {
            if (file.getName().equals(fileName)) {
                file.delete();
            }
        }
    }

    @Override
    public void clear() {
        for (File file : cacheFiles) {
            file.delete();
        }
    }


    /**
     * 检查是否初始化缓存根目录
     */
    public static void checkCacheRoot() {
        if (!networkCacheRoot.exists()) {
            networkCacheRoot.mkdir();
        }
    }

    /**
     * 把一个对象写入文件
     *
     * @param object
     * @param cache
     */
    public void writeObjectToFile(Object object, File cache) {
        if (!cache.exists()) {
            try {
                cache.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream(cache));
            os.writeObject(object);   // 将object对象写进文件
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从一个文件中读取对象
     *
     * @param cache
     * @param <T>
     * @return
     */
    private  <T> T readObjectFromFile(File cache) {
        if (!cache.exists()) {
            RestHttpLog.i("File does not exist");
            return null;
        }
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(cache));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            T temp = (T) is.readObject();
            is.close();
            return temp;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 判断缓存是否存在
     *
     * @param key
     * @return
     */
    public boolean isExistsCache(String key) {
        String fileName = getCacheFileName(key);
        for (File file : cacheFiles) {
            if (file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private String getCacheFileName(String url) {
        return Utils.MD5(url);
    }

    public File getCacheFile(String url) {
        return new File(networkCacheRoot, getCacheFileName(url));
    }

}
