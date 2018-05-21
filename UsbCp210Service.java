

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/17.
 */

public class UsbCp210Service extends Service {


	private UsbManager usbManager;
	private UsbDevice usbDevice;

	private UsbDeviceConnection connection;
	private boolean serialPortConnected;//是否连接上
	private Handler handler;
	private BroadcastReceiver usbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(Constants.ACTION_USB_PERMISSION)) {
				boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
				if (granted) {// 用户同意了usb使用权限,开始打开串口通信
					Intent intent = new Intent(Constants.ACTION_USB_PERMISSION_GRANTED);
					arg0.sendBroadcast(intent);
					connection = usbManager.openDevice(usbDevice);
					new ConnectionThread().start();
				} else { // 用户拒绝了usb使用权限
					Intent intent = new Intent(Constants.ACTION_USB_PERMISSION_NOT_GRANTED);
					arg0.sendBroadcast(intent);
				}
			} else if (arg1.getAction().equals(Constants.ACTION_USB_ATTACHED)) {
				//usb连接上
				if (!serialPortConnected) {
					findUsbCp210Device();
				}
			} else if (arg1.getAction().equals(Constants.ACTION_USB_DETACHED)) {
				//usb连接断开了
				Intent intent = new Intent(Constants.ACTION_USB_DISCONNECTED);
				arg0.sendBroadcast(intent);
				if (serialPortConnected) {
					close();
				}
				serialPortConnected = false;
			}
		}
	};
	private UsbInterface usbInterface;
	private UsbEndpoint inEndpoint;
	private UsbEndpoint outEndpoint;
	private boolean isOpenSuccess;//串口通信是否打开成功

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return new UsbBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerUsbBoradCast();
		findUsbCp210Device();
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 注册usb的广播,监听连接断开事件
	 */
	private void registerUsbBoradCast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_USB_PERMISSION);
		filter.addAction(Constants.ACTION_USB_DETACHED);
		filter.addAction(Constants.ACTION_USB_ATTACHED);
		registerReceiver(usbReceiver, filter);
	}

	/**
	 * 找到cp210设备
	 */
	private void findUsbCp210Device() {
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
		if (!usbDevices.isEmpty()) {
			boolean keep = true;
			for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
				usbDevice = entry.getValue();
				int deviceVID = usbDevice.getVendorId();
				int devicePID = usbDevice.getProductId();

				// 7531   1478  36940
				if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003) && deviceVID != 0x5c6 && devicePID != 0x904c) {

					// 有个设备连接到我们的anroid机器 ,连接上端口
					requestUserPermission();
					keep = false;
				} else {
					connection = null;
					usbDevice = null;
				}

				if (!keep)
					break;
			}
			if (!keep) {
				/**
				 * 没有连接usb 发送广播到 {@link MainActivity }
				 */
				Intent intent = new Intent(Constants.ACTION_NO_USB);
				sendBroadcast(intent);
			}
		} else {
			/**
			 * 没有连接usb 发送广播到 {@link MainActivity }
			 */
			Intent intent = new Intent(Constants.ACTION_NO_USB);
			sendBroadcast(intent);
		}
	}

	/*
		 * 发送广播请求权限
		 */
	private void requestUserPermission() {
		PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
		usbManager.requestPermission(usbDevice, mPendingIntent);
	}

	/**
	 * 打开连接,通信在子线程,不阻塞
	 */
	private class ConnectionThread extends Thread {
		@Override
		public void run() {
			super.run();
			usbInterface = usbDevice.getInterface(0);
			isOpenSuccess = openCP210X();
		}
	}

	// 打开声明连接
	private boolean openCP210X() {
		if (connection.claimInterface(usbInterface, true)) {
			Log.i("UsbCp210Service", "Interface succesfully claimed");
		} else {
			Log.i("UsbCp210Service", "Interface could not be claimed");
			return false;
		}

		//获取端口个数,找到输入和输出端口
		int numberEndpoints = usbInterface.getEndpointCount();
		for (int i = 0; i <= numberEndpoints - 1; i++) {
			UsbEndpoint endpoint = usbInterface.getEndpoint(i);
			if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
					&& endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
				inEndpoint = endpoint;
			} else {
				outEndpoint = endpoint;
			}
		}


		// 设置控制命令
		if (setControlCommand(Constants.CP210x_IFC_ENABLE, Constants.CP210x_UART_ENABLE, null) < 0) {

			return false;
		}

		//设置波特率 9600
		setBaudRate(Constants.DEFAULT_BAUDRATE);
		if (setControlCommand(Constants.CP210x_SET_LINE_CTL, Constants.CP210x_LINE_CTL_DEFAULT, null) < 0) {
			return false;
		}
		//设置跟随控制
		setFlowControl();

		return setControlCommand(Constants.CP210x_SET_MHS, Constants.CP210x_MHS_DEFAULT, null) >= 0;
	}

	/**
	 * 设置控制命令
	 *
	 * @param request
	 * @param value
	 * @param data
	 * @return
	 */
	private int setControlCommand(int request, int value, byte[] data) {
		int dataLength = 0;
		if (data != null) {
			dataLength = data.length;
		}
		int response = connection.controlTransfer(Constants.CP210x_REQTYPE_HOST2DEVICE, request, value, usbInterface.getId(), data, dataLength, Constants.USB_TIMEOUT);
		Log.i("UsbCp210Service", "Control Transfer Response: " + String.valueOf(response));
		return response;
	}

	public void close() {
		setControlCommand(Constants.CP210x_IFC_ENABLE, Constants.CP210x_UART_DISABLE, null);
		connection.releaseInterface(usbInterface);
	}

	public void setBaudRate(int baudRate) {
		byte[] data = new byte[]{
				(byte) (baudRate & 0xff),
				(byte) (baudRate >> 8 & 0xff),
				(byte) (baudRate >> 16 & 0xff),
				(byte) (baudRate >> 24 & 0xff)
		};
		setControlCommand(Constants.CP210x_SET_BAUDRATE, 0, data);
	}

	public void setFlowControl() {
		byte[] dataOff = new byte[]{
				(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00
		};
		setControlCommand(Constants.CP210x_SET_FLOW, 0, dataOff);
	}

	public class UsbBinder extends Binder {

		private ReadThread readThread;

		public UsbCp210Service getService() {
			return UsbCp210Service.this;
		}

		public int write(byte[] buffer, int timeout) {
			if (readThread == null) {
				readThread = new ReadThread();
			}
			if (!readThread.isAlive()) {
				readThread.start();
			}
			int bulkTransfer = connection.bulkTransfer(outEndpoint, buffer, buffer.length, timeout);
			return bulkTransfer;
		}

		public int callback(byte[] buffer, int timeout) {
			return connection.bulkTransfer(inEndpoint, buffer, buffer.length, timeout);
		}
	}

	private class ReadThread extends Thread {
		@Override
		public void run() {
			String result = "";
			while (true) {
				byte[] buffer = new byte[1];
				int n = connection.bulkTransfer(inEndpoint, buffer, buffer.length, 0);
				if (n > 0) {
					String hexToString = HexData.hexToString(buffer);
					if ("0x20 ".equals(hexToString)) {
						result = "";
					}
					result = result + hexToString;
					System.out.println("收到的消息 ---------:  " + result);
					if ("0x03 ".equals(hexToString)) {
						if (handler != null) {
							handler.obtainMessage(1, result).sendToTarget();
						}
					}

				}
			}
		}
	}
}
