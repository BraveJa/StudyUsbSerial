

/**
 * Created by Administrator on 2018/5/17.
 */

public class Constants {
	//权限的广播
	public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	//通知ui获取到了权限
	public static final String ACTION_USB_PERMISSION_GRANTED = "com.zzw.usbservice.USB_PERMISSION_GRANTED";
	//usb权限被拒绝,发广播通知用户
	public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.zzw.usbservice.USB_PERMISSION_NOT_GRANTED";
	//连接上
	public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	//未连接上
	public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	//通知用户usb断开连接
	public static final String ACTION_USB_DISCONNECTED = "com.zzw.usbservice.USB_DISCONNECTED";
	//没有usb
	public static final String ACTION_NO_USB = "com.zzw.usbservice.NO_USB";



	public static final int CP210x_IFC_ENABLE = 0x00;
	public static final int CP210x_SET_BAUDDIV = 0x01;
	public static final int CP210x_SET_LINE_CTL = 0x03;
	public static final int CP210x_GET_LINE_CTL = 0x04;
	public static final int CP210x_SET_MHS = 0x07;
	public static final int CP210x_SET_BAUDRATE = 0x1E;
	public static final int CP210x_SET_FLOW = 0x13;
	public static final int CP210x_SET_XON = 0x09;
	public static final int CP210x_SET_XOFF = 0x0A;
	public static final int CP210x_SET_CHARS = 0x19;
	public static final int CP210x_GET_MDMSTS = 0x08;
	public static final int CP210x_GET_COMM_STATUS = 0x10;

	public static final int CP210x_REQTYPE_HOST2DEVICE = 0x41;
	public static final int CP210x_REQTYPE_DEVICE2HOST = 0xC1;

	public static final int CP210x_MHS_RTS_ON = 0x202;
	public static final int CP210x_MHS_RTS_OFF = 0x200;
	public static final int CP210x_MHS_DTR_ON = 0x101;
	public static final int CP210x_MHS_DTR_OFF = 0x100;

	/***
	 *  Default Serial Configuration
	 *  Baud rate: 9600
	 *  Data bits: 8
	 *  Stop bits: 1
	 *  Parity: None
	 *  Flow Control: Off
	 */
	public static final int CP210x_UART_ENABLE = 0x0001;
	public static final int CP210x_UART_DISABLE = 0x0000;
	public static final int CP210x_LINE_CTL_DEFAULT = 0x0800;
	public static final int CP210x_MHS_DEFAULT = 0x0000;
	public static final int CP210x_MHS_DTR = 0x0001;
	public static final int CP210x_MHS_RTS = 0x0010;
	public static final int CP210x_MHS_ALL = 0x0011;
	public static final int CP210x_XON = 0x0000;
	public static final int CP210x_XOFF = 0x0000;
	public static final int DEFAULT_BAUDRATE = 9600;



	public static int FLOW_CONTROL_OFF = 0;
	public static final int USB_TIMEOUT = 5000;
}
