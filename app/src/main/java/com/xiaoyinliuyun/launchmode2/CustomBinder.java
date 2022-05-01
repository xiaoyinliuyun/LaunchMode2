package com.xiaoyinliuyun.launchmode2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

/**
 * @Author yangkunjian.
 * @Date 2022/4/28 15:17.
 * @Desc
 */

public class CustomBinder extends Binder implements IInterface {



    @Override
    public IBinder asBinder() {
        return this;
    }
}
