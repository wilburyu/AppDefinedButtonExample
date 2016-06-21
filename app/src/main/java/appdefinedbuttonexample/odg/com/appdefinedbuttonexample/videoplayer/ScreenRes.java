package appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * User: kevin.moore
 * Static methods for calculating new width and height's based on a design size vs the current screen size.
 */
public class ScreenRes {
	private static String TAG = "ScreenRes";
	private static int DESIGN_WIDTH = 1280;
	private static int DESIGN_HEIGHT = 720;
	private static int displayWidth, displayHeight;
	private static float designRatio, displayRatio, widthRatio, heightRatio, largestRatio;
	private static float density;
	private static int designWidth = DESIGN_WIDTH, designHeight = DESIGN_HEIGHT;
	private static boolean debugging = false;
	private static Context context;

	/**
	 * Initialize static class. Needs to be called only 1x
	 * @param context
	 */
	public static void init(Context context) {
		ScreenRes.context = context;
		if (displayHeight == 0) {
			configurationChanged();
		}
	}

	public static void configurationChanged() {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		density = outMetrics.density;
		Point display_size = new Point();
		display.getSize(display_size);
		displayWidth = display_size.x;
		displayHeight = display_size.y;
		calcRatios();
		if (debugging)
			Log.d(TAG, "Display density: " + density + " DensityDpi: " + outMetrics.densityDpi + " width: " + displayWidth + " Height: " + displayHeight);

	}

	/**
	 * Calculate the ratios of the design and screen sizes
	 */
	private static void calcRatios() {
		designRatio = (float)DESIGN_HEIGHT/(float)DESIGN_WIDTH;
		if (displayHeight > displayWidth) {
			displayRatio = (float)displayWidth/(float)displayHeight;
		} else {
			displayRatio = (float)displayHeight/(float)displayWidth;
		}
		if (debugging)
			Log.d(TAG, "designRatio:  " + designRatio + " displayRatio: " + displayRatio);
		float ratioDiff = Math.abs(designRatio - displayRatio);
		if (displayHeight > displayWidth) {
			widthRatio = (float)displayWidth / (float)designHeight + ratioDiff;
			heightRatio = (float)displayHeight / (float)designWidth + ratioDiff;
		} else {
			widthRatio = (float)displayWidth / (float)designWidth + ratioDiff;
			heightRatio = (float)displayHeight / (float)designHeight + ratioDiff;
		}
		largestRatio = Math.max(widthRatio, heightRatio);
		if (debugging)
			Log.d(TAG, "Width Ratio = " + widthRatio + " heightRatio= " + heightRatio + " ratioDiff:" + ratioDiff);
	}

	/**
	 * Are we in landscape orientation
	 * @return true if in landscape
	 */
	public static boolean isLandscape() {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        }
        if (displayHeight == 0) {
            configurationChanged();
        }
		return (displayWidth  > displayHeight);
	}

	/**
	 * Get the display height.
	 * @return height
	 */
	public static int getDisplayHeight() {
		return displayHeight;
	}

	/**
	 * Get the display width.
	 * @return width
	 */
	public static int getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * Set the design height.
	 * @param designHeight
	 */
	public static void setDesignHeight(int designHeight) {
		ScreenRes.designHeight = designHeight;
		calcRatios();
	}

	/**
	 * Set the design widthy.
	 * @param designWidth
	 */
	public static void setDesignWidth(int designWidth) {
		ScreenRes.designWidth = designWidth;
		calcRatios();
	}

	/**
	 * Calculate the display width of the given design width.
	 * @param width
	 * @return new width
	 */
	public static int calcWidth(int width) {
		if (debugging)
			Log.d(TAG, "calcWidth: Pre-width = " + width);
//		width = (int) (widthRatio * (float)width + 0.5f);
		width = (int) (largestRatio * (float)width + 0.5f);
		if (debugging)
			Log.d(TAG, "calcWidth: Post-width = " + width);
		return width;
	}

	/**
	 * Calculate the display height of the given design height.
	 * @param height
	 * @return new height
	 */
	public static int calcHeight(int height) {
		if (debugging)
			Log.d(TAG, "calcHeight: Pre-height = " + height);
//		height = (int) (heightRatio * (float)height + 0.5f);
		height = (int) (largestRatio * (float)height + 0.5f);
		if (debugging)
			Log.d(TAG, "calcHeight: Post-height = " + height);
		return height;
	}

}
