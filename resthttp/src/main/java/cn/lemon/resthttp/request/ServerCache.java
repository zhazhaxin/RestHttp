package cn.lemon.resthttp.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.lemon.resthttp.util.RestHttpLog;
import cn.lemon.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class ServerCache implements Cache {

    /**
     * 缓存的根目录
     */
    public static final File networkCacheRoot = Util.getDiskCacheDir("ServerCache");
    private List<File> cacheFiles;
    private static ServerCache instance;

    private ServerCache() {
        cacheFiles = new ArrayList<>();
        for (File file : networkCacheRoot.listFiles()) {
            cacheFiles.add(file);
        }
    }

    public static ServerCache getInstance() {
        if (instance == null) {
            instance = new ServerCache();
        }
        return instance;
    }

    @Override
    public Entry get(String key) {
        return (Entry) readObjectFromFile(getCacheFile(key));
    }

    @Override
    public void put(String key, Entry entry) {
        Entry cacheEntry = get(key);
        if (cacheEntry == null || cacheEntry.refreshNeeded() || cacheEntry.isExpired()) {
            File newFile = getCacheFile(key);
            if (writeObjectToFile(entry, newFile)) {
                cacheFiles.add(newFile);
            }
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void invalidate(String key, boolean fullExpire) {

    }

    @Override
    public void remove(String key) {
        File file = getCacheFile(key);
        if(file.delete()){
            cacheFiles.remove(file);
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
     * @param object
     * @param cache
     */
    private boolean writeObjectToFile(Object object, File cache) {
        if (!cache.exists()) {
            try {
                cache.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream(cache));
            os.writeObject(object);   // 将object对象写进文件
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 从一个文件中读取对象
     * @param cache
     */
    private <T> T readObjectFromFile(File cache) {
        if (!cache.exists()) {
            RestHttpLog.i("Cache file does not exist");
            return null;
        }
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(cache));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (is != null ) {
                T temp = (T) is.readObject();
                is.close();
                return temp;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断缓存是否存在
     * @param key
     */
    public boolean isExistsCache(String key) {
        return cacheFiles.contains(getCacheFile(key));
    }

    private File getCacheFile(String key) {
        return new File(networkCacheRoot, key);
    }

}
