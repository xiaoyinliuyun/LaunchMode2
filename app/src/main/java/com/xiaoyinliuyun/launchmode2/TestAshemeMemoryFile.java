package com.xiaoyinliuyun.launchmode2;

import android.os.MemoryFile;
import android.os.Process;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author yangkunjian.
 * @Date 2022/4/30 12:49.
 * @Desc
 */

public class TestAshemeMemoryFile {
    private static final String TAG = "TestAshemeMemoryFile";

    private MemoryFile mMemoryFile;

    private static volatile TestAshemeMemoryFile mInstance;

    public static TestAshemeMemoryFile getInstance(){
        if(mInstance == null){
            synchronized (TestAshemeMemoryFile.class){
                if(mInstance == null){
                    try {
                        Log.i(TAG, "getInstance: pid -> " + Process.myPid());
                        mInstance = new TestAshemeMemoryFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mInstance;
    }


    private TestAshemeMemoryFile() throws IOException {
        mMemoryFile = new MemoryFile("testAshmem", 1024 * 1024);

        try {
            Class<? extends MemoryFile> clazz = mMemoryFile.getClass();
            Method getFileDescriptor = clazz.getMethod("getFileDescriptor");
            FileDescriptor fd = (FileDescriptor) getFileDescriptor.invoke(mMemoryFile);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public int readBytes(byte[] buffer, int srcOffset, int destOffset, int count){
        try {
            return mMemoryFile.readBytes(buffer, srcOffset, destOffset, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void writeBytes(byte[] buffer, int srcOffset, int destOffset, int count){
        try {
            mMemoryFile.writeBytes(buffer, srcOffset, destOffset, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
