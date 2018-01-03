package com.ctl.lbs.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.ctl.lbs.bluetooth.BluetoothState.CONNECTED;


public  class BluetoothConn extends Observable {

    private static final String TAG = "BluetoothConn";

    /**
     * 接收消息的输入流
     */
    private InputStream inputStream = null;

    /**
     * 通过输入流转换为Buffer 可以按行读数据
     */
    private BufferedReader msgReader = null;
    /**
     * 发送消息的输出流
     */
    private OutputStream msgWriter = null;

    /**
     * 通信Socket
     */
    private BluetoothSocket socket	= null;

    /**
     * 蓝牙适配
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * 连接的状态
     */
    private BluetoothState state = BluetoothState.DISCONNECTED;


    /**
     *手机蓝牙的串口服务的UUID，用其创建蓝牙串口的socket
     */
    private final String uuidStr = "00001101-0000-1000-8000-00805F9B34FB";

    private String devName = "";

    private int cycleConnect = 6000;

    /**
     *
     *
     * @param devName 蓝牙名字,可以是全名，也可以是名称的一部分例如 设备名是 LTE_001
     *                devName可以是LTE_001  也可以是 LTE_  程序会从配对表中选择包含LTE_001或LTE_
     *                的设备自动连接如果有多个相同或近似的设备，怎默认在表中选择第一个。
     * @param cycleConnect  设备未连接时，每隔cycleConnect 毫秒连接一次，最小6000ms 小于6000无效
     */
    public BluetoothConn(String devName, int cycleConnect)
    {

        if(cycleConnect<5999)
            this.cycleConnect = 6000;
        else
            this.cycleConnect = cycleConnect;
        this.devName = devName;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()) bluetoothAdapter.enable();
        startTimer();

    }


    private Timer timer = null;

    private TimerTask timerTask = null;

    /**
     * 连接蓝牙设备，连接过程比较耗时，不建议放在主线程中
     * @return 蓝牙连接状态
     */
    public BluetoothState connectDevice(){
        BluetoothDevice dev = getDevice();
        if(dev==null) return BluetoothState.NODEV;
        try {
            socket = dev.createRfcommSocketToServiceRecord(UUID.fromString(uuidStr));
            socket.connect();
            inputStream = socket.getInputStream();
            //将inputStream 的输入流包装称BufferRead 可以提供按行读的功能
            msgReader = new BufferedReader(new InputStreamReader(inputStream));
            msgWriter = socket.getOutputStream();
        }catch (IOException ex) {
            return BluetoothState.CONNECT_FAILED;
        }
        return CONNECTED;
    }
    /**
     * Get the target device
     * @return
     */
    private BluetoothDevice getDevice()
    {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if(devices.size()<1) {
            return null;
        }
        BluetoothDevice deviceGroup[] = new BluetoothDevice[devices.size()];
        devices.toArray(deviceGroup);
        for (int i = 0, size = deviceGroup.length; i < size; i++) {
            BluetoothDevice dev = deviceGroup[i];
            String devName = (dev.getName()).toString();
            if(devName.contains(this.devName)) {
                return dev;
            }
        }
        return null;
    }
    /**
     * 数据发送函数
     * @param buffer
     */
    public  void sendCmd(ByteBuffer buffer)
    {

        if (msgWriter != null&&buffer!=null)
        {
            try
            {
                msgWriter.write(buffer.array());
                msgWriter.flush();
//                MyLog.logInfo("发送",MyLog.getHexString(buffer.array()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送字符串并增加\r\n结尾
     * @param cmd
     */
    public  void sendCmd(String cmd)
    {

        if (msgWriter != null)
        {
            try
            {
                msgWriter.write((cmd+"\r\n").getBytes());
                msgWriter.flush();
                Log.i(TAG,"发送"+cmd);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void startTimer()
    {
        if(timer == null)
            timer = new Timer();
        if(timerTask == null)
        {
            timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    if(state != CONNECTED||socket==null||!socket.isConnected()){
                        BluetoothState connetState = connectDevice();
                        setState(connetState);
                    }
                }
            };
        }
        if(timer != null && timerTask != null )
            timer.schedule(timerTask,2000,6000);
    }

    /**
     * 状态变更，通知各个观察者
     *  状态变为CONNECTED应开启数据接收线程
     * @param state
     */
    public void setState(BluetoothState state) {
        if(state != this.state){
            setChanged();
            notifyObservers(state);
            Log.i(TAG,"连接状态："+state.toString());
        }
        this.state = state;
    }

    /**
     * 从输入流中按行读取数据，使用此方法接收数据时，应事先保证
     * 发送端按行发送或发送数据中包含 0D 0A （回车换行）
     * @return
     * @throws IOException
     */
    public String revMsg() throws IOException
    {
        return msgReader.readLine();
    }

    /**
     * 读取输入流中的数据，最多一次读1024字节
     * @return  实际读取到的数据
     * @throws IOException
     */
    public byte[] revBuffer() throws IOException
    {
        byte[] buf = new byte[1024];
        int length = inputStream.read(buf);

        return subBytes(buf,0,length);
    }
    /**
     * 从一个byte[]数组中截取一部分
     * @param src 数据源
     * @param begin 开始位置
     * @param count 截取长度
     * @return 返回截取的内容
     */
    public  byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }

    /**
     * 退出或连接中断时关闭资源
     */
    public void closeConn()
    {
        try
        {
            if(socket!=null)
                socket.close();
            if(inputStream!=null)
                inputStream.close();
            if(msgWriter!=null)
                msgWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 停止自动连接的Timer
     */
    private void stopTimer()
    {
        if(timer!=null)
        {
            timer.cancel();
            timer = null;
        }
        if(timerTask!= null)
        {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * 软件退出时调用，停止定时器，关闭连接资源
     */
    public void appExit(){
        stopTimer();
        closeConn();
    }

    /**
     * 判断是否被链接
     * @return
     */
    public boolean isConnected()
    {
        if(socket!=null){
            return socket.isConnected();
        }
        return false;
    }
}
