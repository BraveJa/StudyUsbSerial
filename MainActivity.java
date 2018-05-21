import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzw.usbserial.R;
import com.zzw.usbserial.UsbCp210Service;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private Button btn_write, btn_cardNum, btn_read, btn_restore;
	private TextView tvData;
	private UsbCp210Service.UsbBinder usbBinder;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String result = (String) msg.obj;
			tvData.append(result + "\n");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, UsbCp210Service.class);
		bindService(intent, binderConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection binderConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			usbBinder = (UsbCp210Service.UsbBinder) service;
			UsbCp210Service usbService = usbBinder.getService();
			usbService.setHandler(handler);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//服务断开
			usbBinder = null;
		}
	};

	private void initView() {
		btn_write = findViewById(R.id.btn_write);
		btn_cardNum = findViewById(R.id.btn_card_num);
		btn_read = findViewById(R.id.btn_read);
		btn_restore = findViewById(R.id.btn_restore);
		btn_write.setOnClickListener(this);
		btn_cardNum.setOnClickListener(this);
		btn_read.setOnClickListener(this);
		btn_restore.setOnClickListener(this);
		tvData = findViewById(R.id.data);
		tvData.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_write) {

			usbBinder.write(write, 1000);
		} else if (id == R.id.btn_read) {

			usbBinder.write(query, 1000);
		} else if (id == R.id.btn_restore) {

		} else if (id == R.id.btn_card_num) {

			
			usbBinder.write(query, 1000);
		}
	}
}
