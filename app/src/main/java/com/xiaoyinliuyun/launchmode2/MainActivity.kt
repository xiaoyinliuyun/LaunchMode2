package com.xiaoyinliuyun.launchmode2

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.FileDescriptor

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.i(TAG, "binderDied: ");
            mClientService!!.unlinkToDeath(this, 0);
        }
    }

    private var mClientService: IBinder? = null;

    // 服务端Messenger代理
    private var mMessenger: Messenger? = null;

    private class MessengerHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i("MainActivity", "handleMessage: 客户端收到消息")

        }
    }

    // 客户端Messenger
    private var mGetReplyMessenger: Messenger = Messenger(MessengerHandler());

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // messenger
//            mMessenger = Messenger(service)
//
//            val message = Message.obtain();
            val book = Book(1, "Android book");
//            val address = Address(Locale.CHINA);
//            val person = Person("杨坤建", 33);
//            val bundle = Bundle();
//            // android.os.BadParcelableException: ClassNotFoundException when unmarshalling: com.xiaoyinliuyun.launchmode2.Book
////            bundle.putParcelable("book1", book);
//            bundle.putParcelable("address", address);
//            bundle.putSerializable("person", person);
//            bundle.putString("book", "Android str");
//            message.replyTo = mGetReplyMessenger;
//            message.data = bundle;
//            message.what = 1;
//            mMessenger!!.send(message);
//
//            Log.i(TAG, "mMessenger.binder: ${mMessenger!!.binder}")


            // aidl
//            val bookManager = IBookManager.Stub.asInterface(service);
//            bookManager.addBook(Book(1,"Android"));
//            bookManager.bookList.forEach(::println);
//
//            Log.i(TAG, "bookManager: $bookManager")
//            val asBinder = bookManager.asBinder();
//            Log.i(TAG, "asBinder: $asBinder")

            // binder
            mClientService = service;
            val data = Parcel.obtain();
            val reply = Parcel.obtain();
            val value = ByteArray(1024);
            data.writeByteArray(value)
            data.writeByteArray(byteArrayOf(1,5,3,2,5))
            data.writeParcelable(book,0);
            // 传输共享内存的文件描述符
            val memoryFile = MemoryFile("testAshmem", 1024*1024);
            val byteArray = ByteArray(1024 * 1024);
            byteArray[1024 * 1024 -1] = 99;
            memoryFile.writeBytes(byteArray, 0, 0, 1024 * 1024);

            val clazz: Class<out MemoryFile?> = memoryFile.javaClass
            val getFileDescriptor = clazz.getMethod("getFileDescriptor")
            val fd = getFileDescriptor.invoke(memoryFile) as FileDescriptor
            val pfd = ParcelFileDescriptor.dup(fd);
            data.writeParcelable(pfd,0);

            val pid = Binder.getCallingPid()
            val uid = Binder.getCallingUid()
            Log.i("$TAG", "pid: $pid, uid: $uid")
            Log.i("$TAG", "myPid: " + Process.myPid() + ", myUid: " + Process.myUid())

            Log.i(TAG, "onServiceConnected: ComponentName: $name , IBinder: $service")
            Log.i(TAG, "reply 响应前: ${reply.dataSize()}")
            val transact = mClientService!!.transact(10001, data, reply, 2);
            if (transact) {
                Log.i(TAG, "transact: 鉴权成功");
            } else {
                Log.i(TAG, "transact: 鉴权失败");
            };

            Log.i(TAG, "reply 响应后: ${reply.dataSize()}")
            // 为什么服务端写回来的数据，没法正常读取
            val result = reply.createByteArray();// 这么玩的
            // java.lang.RuntimeException: bad array lengths
//            reply.readByteArray(result);
            result!!.forEach { b ->
                Log.i(TAG, "result: $b")
            }

            val replyBook: Book? = reply.readParcelable(Book::class.java.classLoader)
            if(replyBook == null){
                Log.i(TAG, "replyBook is null")
                return;
            }
            Log.i(TAG, "replyBook: ${replyBook.bookName}")
            val rrrr = ByteArray(1024 * 1024);
            memoryFile.readBytes(rrrr, 0, 0, 1024 * 1024);
            Log.i(TAG, "memoryFile read: ${rrrr[1000]}")
            memoryFile.close();

            mClientService!!.linkToDeath(deathRecipient, 0);
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.i(TAG, "onServiceDisconnected: ComponentName: $name")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "onCreate: ")


        val pid = Binder.getCallingPid()
        val uid = Binder.getCallingUid()
        Log.i("$TAG onCreate", "pid: $pid, uid: $uid")
        Log.i("$TAG onCreate", "myPid: " + Process.myPid() + ", myUid: " + Process.myUid())
    }

    fun onFirst(view: android.view.View) {
        val byteArray = ByteArray(1024 * 1024);
        byteArray[0] = 1;
        byteArray[1] = 3;
        byteArray[2] = 5;
        byteArray[3] = 7;
        byteArray[4] = 9;
        byteArray[1024 * 1024 - 1] = 29;

        TestAshemeMemoryFile.getInstance().writeBytes(byteArray, 0, 0, 1024 * 1024);
        val intent = Intent(this, FirstActivity::class.java);
        startActivity(intent);
    }


    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
    }

    fun onStartService(view: View) {
        val intent = Intent(this, ProService::class.java);
        startService(intent);
    }

    fun onBindService(view: View) {
        val intent = Intent(this, ProService::class.java);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    fun onUnbindService(view: View) {
        unbindService(mServiceConnection);
    }

    fun onStopService(view: View) {
        val intent = Intent(this, ProService::class.java);
        stopService(intent);
    }


}