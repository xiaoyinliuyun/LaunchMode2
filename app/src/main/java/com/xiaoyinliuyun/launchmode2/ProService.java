package com.xiaoyinliuyun.launchmode2;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProService extends Service {
    private static final String TAG = "ProService";

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull @NotNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Bundle data = msg.getData();
                if (data == null) {
                    Log.i(TAG, "handleMessage: bundle is null");
                    return;
                }

//                Book book1 = data.getParcelable("book1");
//                if(book1 == null){
//                    Log.i(TAG, "handleMessage: book1 is null");
//                    return;
//                }
//                Log.i(TAG, "handleMessage: 服务端收到book1 : "+book1);

                String book = data.getString("book");
                if (book == null) {
                    Log.i(TAG, "handleMessage: book is null");
                    return;
                }
                Log.i(TAG, "handleMessage: 服务端收到book : " + book);

                Person person = (Person) data.getSerializable("person");
                if (person == null) {
                    Log.i(TAG, "handleMessage: person is null");
                    return;
                }
                Log.i(TAG, "handleMessage: 服务端收到person : " + person.name);

                Address address = data.getParcelable("address");
                if (address == null) {
                    Log.i(TAG, "handleMessage: address is null");
                    return;
                }
                Log.i(TAG, "handleMessage: 服务端收到address : " + address);

                try {
                    msg.replyTo.send(Message.obtain());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());


    // aidl方式的 Service组件
    Binder mBinderAidl = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            List<Book> list = new ArrayList<>();
            list.add(new Book(1, "Java大全"));
            list.add(new Book(2, "算法大全"));
            return list;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Log.i(TAG, "addBook: " + book.bookName);
        }
    };

    // 服务端创建的Binder，
    Binder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
//            super.onTransact(code, data, reply, flags);

            // 提供的服务
            byte[] result = new byte[1024];
            data.readByteArray(result);
            byte[] byteArray = data.createByteArray();
            for (byte b : byteArray) {
                Log.i(TAG, "i: " + b);
            }
            Book book = data.readParcelable(Book.class.getClassLoader());
            Log.i(TAG, "onTransact: book: " + book.bookName);

            ParcelFileDescriptor pfd = data.readParcelable(ParcelFileDescriptor.class.getClassLoader());
            FileDescriptor fd = pfd.getFileDescriptor();

            try {
                MemoryFile memoryFile = new MemoryFile("", 1024*1024);
                memoryFile.close();
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
                    Log.i(TAG, "进行共享内存的版本 <= 26: " + Build.VERSION.SDK_INT);

                    Class<MemoryFile> memoryFileClass = MemoryFile.class;
                    Field mFD = memoryFileClass.getDeclaredField("mFD");
                    mFD.setAccessible(true);
                    mFD.set(memoryFile, fd);

                    Field mLength = memoryFileClass.getDeclaredField("mLength");
                    mLength.setAccessible(true);
                    mLength.set(memoryFile, 1024 * 1024);

//                    long address = (long) InvokeUtil.invokeStaticMethod(c, "native_mmap", fd, length, mode);
                    Method[] declaredMethods = memoryFileClass.getDeclaredMethods();
                    if(declaredMethods.length <= 0){
                        Log.i(TAG, "没有方法");
                        return false;
                    }
                    Method native_mmap = null;
                    for (Method met :declaredMethods) {
                        if ("native_mmap".equals(met.getName())) {
                            Log.i(TAG, "找到native_mmap了 ");
                            native_mmap = met;
                            break;
                        }
                    }
                    if(native_mmap == null){
                        Log.i(TAG, "没找到native_mmap ");
                        return false;
                    }
                    native_mmap.setAccessible(true);
//                    Method native_mmap = memoryFileClass.getDeclaredMethod("native_mmap");
                    long address = (long)native_mmap.invoke(null, fd, 1024 * 1024, 0x1 | 0x2);

                    Field mAddress = memoryFileClass.getDeclaredField("mAddress");
                    mAddress.setAccessible(true);
                    mAddress.set(memoryFile, address);


                    // 完成重建MemoryFile
                    byte[] result1 = new byte[1024 * 1024];
                    memoryFile.readBytes(result1, 0, 0, 1024 * 1024);

                    byte[] rrrr = new byte[1024 * 1024];
                    rrrr[1000] = 55;
                    memoryFile.writeBytes(rrrr,0,0,1024 * 1024);

                    memoryFile.close();

                    Log.i(TAG, "共享内存服务端数据: " + result1[1024 * 1024 - 1]);

                }else {
                    Log.i(TAG, "进行共享内存的版本 > 26: " + Build.VERSION.SDK_INT);
                    Class<?> clazz = Class.forName("android.os.SharedMemory");
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    if (constructors.length > 0) {
                        constructors[0].setAccessible(true);
                        SharedMemory sharedMemory = (SharedMemory) constructors[0].newInstance(fd);

                        Class<? extends SharedMemory> smClass = sharedMemory.getClass();
                        Method mapReadWrite = smClass.getDeclaredMethod("mapReadWrite");
                        mapReadWrite.setAccessible(true);
                        ByteBuffer mapping = (ByteBuffer) mapReadWrite.invoke(sharedMemory);

                        // 给memoryFile设置SharedMemory和ByteBuffer
                        Class<? extends MemoryFile> mfClass = memoryFile.getClass();
                        Field field = mfClass.getDeclaredField("mSharedMemory");
                        field.setAccessible(true);
                        field.set(memoryFile, sharedMemory);

                        Field field1 = mfClass.getDeclaredField("mMapping");
                        field1.setAccessible(true);
                        field1.set(memoryFile, mapping);

                        // 完成重建MemoryFile
                        byte[] result1 = new byte[1024 * 1024];
                        memoryFile.readBytes(result1, 0, 0, 1024 * 1024);
                        memoryFile.close();

                        Log.i(TAG, "共享内存服务端数据: " + result1[1024 * 1024 - 1]);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

//            if(code > 10000){
//                return false;
//            }
            if (reply != null) {
                Log.i(TAG, "reply 不是 null ，正常写数据到reply");

                byte[] value = new byte[]{1,2,3,4,5};
                reply.writeByteArray(value);

                Book book1 = new Book(2, "Java 算法");
                reply.writeParcelable(book1, 0);
            }

            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            Log.i(TAG + " onTransact", "pid: " + pid + ", uid: " + uid);
            Log.i(TAG + " onTransact", "myPid: " + Process.myPid() + ", myUid: " + Process.myUid());

            Log.i(TAG, "onTransact: code: " + code);
            Log.i(TAG, "onTransact: data: " + data);
            Log.i(TAG, "onTransact: reply: " + reply);
            Log.i(TAG, "onTransact: flags: " + flags);
            return true;
        }
    };

    public ProService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(TAG, "onRebind: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        ;

        Log.i(TAG, "pid: " + pid + ", uid: " + uid);
        Log.i(TAG, "myPid: " + Process.myPid() + ", myUid: " + Process.myUid());
        int i = checkPermission("com.xiaoyinliuyun.launchmode2.pro", pid, uid);
        if (i == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "checkPermission -> PERMISSION_DENIED");
            return null;
        }
        Log.e(TAG, "checkPermission -> PERMISSION_GRANTED");

        int check = checkCallingOrSelfPermission("com.xiaoyinliuyun.launchmode2.pro");
        if (check == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "checkCallingOrSelfPermission -> PERMISSION_DENIED");
            return null;
        }
        Log.e(TAG, "checkCallingOrSelfPermission -> PERMISSION_GRANTED");

//        Log.i(TAG, "onBind: Messenger Binder " + mMessenger.getBinder()); // android.os.Handler$MessengerImpl@8d42c39
//        Log.i(TAG, "onBind: aidl Binder " + mBinderAidl); // com.xiaoyinliuyun.launchmode2.ProService$1@4e6627e
//        Log.i(TAG, "onBind: Binder " + mBinder); // com.xiaoyinliuyun.launchmode2.ProService$2@38e83df

        return mBinder;
    }
}