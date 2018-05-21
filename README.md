# StudyUsbSerial
基于cp210x串口通信demo , 把这几个类放在你的工程里就可以了
### 此次模块使用cp210x芯片
- usb通信关键几个类
    -  UsbManager
    -  UsbDevice
    -  UsbInterface
    -  UsbRequest
    -  UsbDeviceConnection


#### 操作步骤
1. 获取usbmanager
2. 找到设备芯片 , [设备的pid和vid值不一样](https://blog.csdn.net/u010661782/article/details/50749271)
3. 判断是否有usb使用权限,没有发送指令获取权限
4. 打开usbConnection连接
5. 找到interface.一般都是用第一个
6. 判断是否成功打开连接
7. 找到 输入 输出 端口
8. 设置控制命令 CP210x_UART_ENABLE
9. 设置波特率 这里用9600
10. 设置 控制命令 CP210x_LINE_CTL_DEFAULT
11. 设置 FLOW_CONTROL_OFF
12. 设置CP210x_MHS_DEFAULT
13. 写数据到模块
14. 获取模块返回的数据
#### 获取系统usbManager

```
 UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
```

#### 找到设备

```
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
```

#### 判断是否有权限,没有就申请权限

```
/*
		 * 发送广播请求权限
		 */
	private void requestUserPermission() {
		PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
		usbManager.requestPermission(usbDevice, mPendingIntent);
	}
```
#### 打开usbConnection连接

```
connection = usbManager.openDevice(usbDevice);
```
#### 找到interface.一般都是用第一个

```
usbInterface = usbDevice.getInterface(0);
```
#### 判断是否成功打开连接

```
connection.claimInterface(usbInterface, true)
```
#### 找到 输入 输出 端口

```
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
```

#### 设置方法

```
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
```

#### 设置控制命令 CP210x_UART_ENABLE

```
//小于0 设置失败
setControlCommand(Constants.CP210x_IFC_ENABLE, Constants.CP210x_UART_ENABLE, null)
```
#### 设置波特率 这里用9600

```
public void setBaudRate(int baudRate) {
		byte[] data = new byte[]{
				(byte) (baudRate & 0xff),
				(byte) (baudRate >> 8 & 0xff),
				(byte) (baudRate >> 16 & 0xff),
				(byte) (baudRate >> 24 & 0xff)
		};
		setControlCommand(Constants.CP210x_SET_BAUDRATE, 0, data);
	}
```
#### 设置 控制命令 CP210x_LINE_CTL_DEFAULT

```
//小于0 设置失败
setControlCommand(Constants.CP210x_SET_LINE_CTL, Constants.CP210x_LINE_CTL_DEFAULT, null)
```
#### 设置 FLOW_CONTROL_OFF
```
setFlowControl(Constants.FLOW_CONTROL_OFF)
```
#### 设置CP210x_MHS_DEFAULT

```
setControlCommand(Constants.CP210x_SET_MHS, Constants.CP210x_MHS_DEFAULT, null)
```
#### 写数据到模块

```
int bulkTransfer = connection.bulkTransfer(outEndpoint, buffer, buffer.length, timeout);
```
#### 获取模块返回的数据

```
private class ReadThread extends Thread {
		@Override
		public void run() {
			while (true) {
		
				byte[] buffer = new byte[1];
				int n = connection.bulkTransfer(inEndpoint, buffer, buffer.length, 0);
				//System.out.println("收到的消息结果-------" +n);
				if (n > 0) {
					String hexToString = HexData.hexToString(buffer);
					if ("0x20".equals(hexToString)){
						byteStr.clear();
					}
					byteStr.add(hexToString);
					if ("0x03".equals(hexToString)){
						for (String s: byteStr) {
							System.out.println("收到的消息 ---------:  " + s);
						}
					}
				}
			}
		}
	}
```
#### 读写数据的时候,根据公司自己定义的指定开始位 ,和停止位 来控制一帧数据的完整
#### 参考资料
[https://github.com/felHR85/UsbSerial](https://github.com/felHR85/UsbSerial)
#### 其他
[https://blog.csdn.net/qq_16064871/article/details/77987681](https://blog.csdn.net/qq_16064871/article/details/77987681)
