package org.blinksd.utils.dictionary;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.io.*;
import java.util.*;
import org.blinksd.*;

public class DictionaryDB extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "dicts.db";
	private static final int DATABASE_VERSION = 1;
	public boolean isReady = false;
	
	public DictionaryDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase p1){
		List<String> types = SuperBoardApplication.getLanguageTypes();
		StringBuilder sb = new StringBuilder();
		for(String type : types){
			sb.append("CREATE TABLE IF NOT EXISTS LANG_")
				.append(escapeString(type))
				.append(" (id INTEGER PRIMARY KEY AUTOINCREMENT,")
				.append(" word TEXT)");
			p1.execSQL(sb.toString());
			sb.setLength(0);
		}
	}

/*
	public void fillDB(){
		File dictDir = getDictionaryDir();
		
		if(!dictDir.exists()) {
			dictDir.mkdirs();
			return;
		}

		File[] files = dictDir.listFiles();
		if(files == null || files.length < 1)
			return;
		
		for(File file : files){
			String name = file.getName();
			name = name.substring(0, name.lastIndexOf("."));
			if(getQuery(name, null, true).size() < 1) {
				saveToDB(name, file);
			}
		}
		
		isReady = true;
	}
*/

	public void saveToDB(String lang, File file, OnSaveProgressListener listener){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			saveToDB(lang, reader, listener);
		} catch(Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void saveToDB(String lang, InputStream fd, OnSaveProgressListener listener){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fd));
			saveToDB(lang, reader, listener);
		} catch(Throwable ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void saveToDB(String lang, BufferedReader reader, OnSaveProgressListener listener) throws IOException{
		String table = "LANG_" + escapeString(lang);
		StringBuilder sb = new StringBuilder();
		SQLiteDatabase db = getWritableDatabase();
		
		int count = 0;
		for (String line; (line = reader.readLine()) != null; ) {
			line = line.trim().toLowerCase();
			if(line.length() > 1 && !line.contains(" ") && !line.contains("/")) {
				sb.append("INSERT OR IGNORE INTO ")
					.append(table)
					.append("(word)")
					.append(" VALUES ('")
					.append(escapeString(line))
					.append("')");
				db.execSQL(sb.toString());
				sb.setLength(0);
				
				if(listener != null)
					listener.onProgress(++count, OnSaveProgressListener.STATE_IMPORT);
			}
		}
		
		if(listener != null)
			listener.onProgress(count, OnSaveProgressListener.STATE_DELETE_DUPLICATES);

		sb.setLength(0);
		sb.append("DELETE FROM ")
			.append(table)
			.append(" WHERE id NOT IN (SELECT min(id) FROM ")
			.append(table)
			.append(" GROUP BY word)");
		db.execSQL(sb.toString());

		reader.close();
		db.close();

		isReady = true;
	}
	
	public List<String> getQuery(String lang, String prefix){
		return getQuery(lang, prefix, false);
	}
	
	
	private List<String> getQuery(String lang, String prefix, boolean internalCheck){
		List<String> out = new ArrayList<String>();
		
		if(!isReady && !internalCheck) return out;
		
		try {
			SQLiteDatabase db = getReadableDatabase();

			if(lang.contains("_")){
				lang = lang.split("_")[0].toLowerCase();
			}

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM LANG_")
				.append(escapeString(lang.trim().toLowerCase()));

			if(!internalCheck) {
				if(prefix == null || prefix.length() < 1)
					return out;
				
				sb.append(" WHERE word")
					.append(" LIKE ")
					.append("'")
					.append(escapeString(prefix))
					.append("%'");
			}

			sb.append(" LIMIT 20");

			Cursor cursor = db.rawQuery(sb.toString(), null);

			if(cursor.moveToFirst()){
				do {
					String word = cursor.getString(1);
					out.add(word);
				} while (cursor.moveToNext());
			}

			db.close();
		} catch(Throwable t){}
		
		return out;
	}
	
	public static String escapeString(String str){
		if (str != null && str.length() > 0) {
			str = str.replace("\\", "\\\\");
			str = str.replace("'", "\\'");
			str = str.replace("\0", "\\0");
			str = str.replace("\n", "\\n");
			str = str.replace("\r", "\\r");
			str = str.replace("\"", "\\\"");
			str = str.replace("\\x1a", "\\Z");
		}
		return str;
	}
	
	public static File getDictionaryDir(){
		return new File(SuperBoardApplication.getApplication().getDataDir() + "/dictionaries");
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3){
		// TODO: If you want to upgrade
	}
	
	public interface OnSaveProgressListener {
		public static final int STATE_IMPORT = 0;
		public static final int STATE_DELETE_DUPLICATES = 1;
		
		void onProgress(int current, int state);
	}
}
