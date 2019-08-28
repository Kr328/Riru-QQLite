package com.github.kr328.qq.blocker.proxy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.util.Log;
import com.github.kr328.qq.blocker.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

class TransactCodeExporter {
    private IInterface proxyInterface;
    private int lastTransactCode;

    TransactCodeExporter(Class<? extends Binder> stubClass) throws ReflectiveOperationException {
        Method methodAsInterface = stubClass.getMethod("asInterface", IBinder.class);
        Binder TRANSACT_CODE_EXPORT_BINDER = new Binder() {
            @Override
            public String getInterfaceDescriptor() {
                return "";
            }

            @Override
            public IInterface queryLocalInterface(String descriptor) {
                return null;
            }

            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
                lastTransactCode = code;
                return true;
            }
        };
        proxyInterface = (IInterface) methodAsInterface.invoke(null, TRANSACT_CODE_EXPORT_BINDER);
    }

    int export(String name, Class<?>... argTypes) throws ReflectiveOperationException {
        Method m = proxyInterface.getClass().getMethod(name, argTypes);
        return export(m);
    }

    private int export(Method method) throws ReflectiveOperationException {
        Object[] args = buildDefaultArgs(method);

        lastTransactCode = -1;

        method.invoke(proxyInterface, args);

        return lastTransactCode;
    }

    private Object[] buildDefaultArgs(Method method) {
        ArrayList<Object> result = new ArrayList<>();

        for (Class<?> c : method.getParameterTypes()) {
            switch (c.getName()) {
                case "int":
                    result.add(0);
                    break;
                case "long":
                    result.add(0L);
                    break;
                case "float":
                    result.add(0.0f);
                    break;
                case "double":
                    result.add(0.0d);
                    break;
                case "char":
                    result.add((char) 0);
                    break;
                case "byte":
                    result.add((byte) 0);
                    break;
                default:
                    try {
                        result.add(c.getConstructor().newInstance());
                    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                        Log.w(Constants.TAG, "Create default object " + c + " failure");
                        result.add(null);
                    }
            }
        }

        return result.toArray();
    }
}
