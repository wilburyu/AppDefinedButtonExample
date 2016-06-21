package appdefinedbuttonexample.odg.com.appdefinedbuttonexample;

import android.app.Application;

public class TheApp extends Application {

	private static TheApp instance;

	public TheApp() {
		instance = this;
	}

	public static TheApp getInstance() {
		return instance;
	}
	
}
