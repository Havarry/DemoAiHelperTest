package com.gzln.goba.aihelp;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/*
 * 用于将assets下的文件按目录结构拷贝到sdcard中
 */

public class AssetsCopyer {

	private static final String ASSET_LIST_FILENAME = "assets.lst";

	private final Context mContext;
	private final AssetManager mAssetManager;
	private File mAppDirectory;

	public AssetsCopyer(Context context) {
		mContext = context;
		mAssetManager = context.getAssets();
	}



	/*
	 * 获取需要拷贝的文件列表
	 */
	protected List<String> getAssetsList() throws IOException{

		List<String> files = new ArrayList<String>();

		InputStream listFile = mAssetManager.open(new File(ASSET_LIST_FILENAME).getPath());
		BufferedReader br = new BufferedReader(new InputStreamReader(listFile));
		String path;
		while ( ( path = br.readLine() ) != null ) {
			files.add(path);
		}
		return files;
	}

	/*
	 * 将assets目录下制定的文件拷贝到sdcard中
	 */
	public boolean copy() throws IOException{

		List<String> srcFiles = new ArrayList<String>();

		//获取系统在sdcard中为app分配的目录，如：/sdcard/Android/data/app's package
		mAppDirectory = mContext.getExternalFilesDir(null);

		if ( null == mAppDirectory ){
			return false;
		}

		//读取assets目录下的asset.lst文件，得到需要拷贝的文件列表
		List<String> assets = getAssetsList();

		for ( String asset : assets ){
			//如果不存在，则添加到copy列表
			if ( ! new File(mAppDirectory, asset).exists() ){
				srcFiles.add(asset);
			}
		}

		//依次拷贝到app的安装目录下
		for ( String file : srcFiles ){
			Log.i("COPY FILE ", file);
			copy(file);
		}

		//通知MainActivity文件拷贝完毕
		//CacheData.getInstance().getMain().onCompletedCopy(0x100);

		return true;
	}


	/*
	 * 执行拷贝任务
	 */
	protected File copy(String asset) throws IOException {

		InputStream source = mAssetManager.open(new File(asset).getPath());
		File destinationFile = new File(mAppDirectory, asset);
		destinationFile.getParentFile().mkdirs();
		OutputStream destination = new FileOutputStream(destinationFile);
		byte[] buffer = new byte[1024];
		int nread;

		while ((nread = source.read(buffer)) != -1) {
			if (nread == 0) {
				nread = source.read();
				if (nread < 0)
					break;
				destination.write(nread);
				continue;
			}
			destination.write(buffer, 0, nread);
		}
		destination.close();

		return destinationFile;
	}

	public static String getAppDir(){
			//return "file:///" + CacheData.getInstance().getMain().getExternalFilesDir(null).toString();
		return "";
	}

	public static String getAppDir2(){

			//return CacheData.getInstance().getMain().getExternalFilesDir(null).toString();
		return "";

	}




















}
