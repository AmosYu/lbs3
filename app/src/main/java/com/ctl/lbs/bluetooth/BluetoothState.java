package com.ctl.lbs.bluetooth;




public enum BluetoothState implements IntegerValueEnum {

    DISCONNECTED(0), CONNECTED(1), CONNECT_FAILED(2),ABORTED(3),NODEV(4);

    private static String[] stringValues = { "未连接采集器", "已连接采集器", "连接采集器失败", "连接异常中断","未发现采集器","未与采集器配对"};

    private int value;
    BluetoothState(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
    @Override
    public String toString() {
        return BluetoothState.stringValues[value];
    }
}
