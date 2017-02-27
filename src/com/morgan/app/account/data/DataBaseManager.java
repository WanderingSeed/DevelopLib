package com.morgan.app.account.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.morgan.app.account.model.TransferRecord;
import com.morgan.app.account.model.TransferType;
import com.morgan.lib.base.BaseApplication;
import com.morgan.lib.util.Logger;

/**
 * 数据库管理器，用于操作数据库中的所有数据
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-06-16
 */
public class DataBaseManager extends SQLiteOpenHelper {

    private static final String TAG = DataBaseManager.class.getName();
    /**
     * 数据库版本
     */
    public static final int DATABASE_VERSION = 1;
    /**
     * 数据库名称
     */
    public static final String DATABASE_NAME = "Account";
    private static Map<String, String> tables;
    private static DataBaseManager mInstance;
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS %1$s";

    interface TransferRecordTable {

        public static final String TABLE_NAME = "transfer_record";
        public static final String RECORD_ID = "record_id";
        public static final String TRANSFER_TYPE = "transfer_type";
        public static final String TRANSFER_VALUE = "transfer_value";
        public static final String TRANSFER_TIME = "transfer_time";
        public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS %1$S (" + RECORD_ID
                + " TEXT PRIMARY KEY, " + TRANSFER_TYPE + " TEXT ," + TRANSFER_VALUE + " REAL, " + TRANSFER_TIME
                + " TEXT)";
    }

    public static DataBaseManager getInstance() {
        if (null == mInstance) {
            mInstance = new DataBaseManager(BaseApplication.getContext());
        }
        if (tables == null) {
            tables = new HashMap<String, String>();
            tables.put(TransferRecordTable.TABLE_NAME, TransferRecordTable.CREATE_TABLE_SQL);
        }
        return mInstance;
    }

    private DataBaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db == null || tables == null) {
            return;
        }
        try {
            db.beginTransaction();
            for (String table : tables.keySet()) {
                db.execSQL(String.format(tables.get(table), table));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "database tables create error " , e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null) {
            return;
        }
        try {
            dropAllTables(db);
            onCreate(db);
        } catch (Exception e) {
            Logger.e(TAG, "upgrade database error " , e);
        } finally {
            // empty here.
        }
    }

    /**
     * 删除数据库中所有的表
     * 
     * @param db
     */
    private void dropAllTables(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            for (String table : tables.keySet()) {
                db.execSQL(String.format(DROP_TABLE_SQL, table));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(TAG, "drop table error " , e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 添加转账记录
     * 
     * @param record
     * @return 是否添加成功
     */
    public boolean addTransferRecord(TransferRecord record) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues value = new ContentValues();
            value.put(TransferRecordTable.RECORD_ID, record.getId());
            value.put(TransferRecordTable.TRANSFER_TIME, record.getTime());
            value.put(TransferRecordTable.TRANSFER_TYPE, record.getType().name());
            value.put(TransferRecordTable.TRANSFER_VALUE, record.getMoney());
            db.insertOrThrow(TransferRecordTable.TABLE_NAME, null, value);
            StockDataManager.getInstance().onAddTransferRecord(record);
        } catch (Exception e) {
            Logger.e(TAG, "add transfer record into database error ", e);
            return false;
        } finally {
            // empty here.
        }
        return true;
    }

    /**
     * 获取转帐记录列表
     * 
     * @return
     */
    public List<TransferRecord> getTransferRecordList() {
        List<TransferRecord> list = new ArrayList<TransferRecord>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(TransferRecordTable.TABLE_NAME, null, null, null, null, null,
                    TransferRecordTable.TRANSFER_TIME);
            if (cursor.moveToFirst()) {
                do {
                    TransferRecord record = new TransferRecord();
                    record.setId(cursor.getString(cursor.getColumnIndex(TransferRecordTable.RECORD_ID)));
                    record.setTime(cursor.getString(cursor.getColumnIndex(TransferRecordTable.TRANSFER_TIME)));
                    record.setType(TransferType.valueOf(cursor.getString(cursor
                            .getColumnIndex(TransferRecordTable.TRANSFER_TYPE))));
                    record.setMoney(cursor.getFloat(cursor.getColumnIndex(TransferRecordTable.TRANSFER_VALUE)));
                    list.add(0, record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Logger.e(TAG, "get transfer record list error " , e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 获取转入资产列表
     * 
     * @return
     */
    public List<TransferRecord> getTransferInRecordList() {
        List<TransferRecord> list = new ArrayList<TransferRecord>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(TransferRecordTable.TABLE_NAME, null, TransferRecordTable.TRANSFER_TYPE + " = ? ",
                    new String[] { TransferType.IN.name() }, null, null, TransferRecordTable.TRANSFER_TIME);
            if (cursor.moveToFirst()) {
                do {
                    TransferRecord record = new TransferRecord();
                    record.setId(cursor.getString(cursor.getColumnIndex(TransferRecordTable.RECORD_ID)));
                    record.setTime(cursor.getString(cursor.getColumnIndex(TransferRecordTable.TRANSFER_TIME)));
                    record.setType(TransferType.valueOf(cursor.getString(cursor
                            .getColumnIndex(TransferRecordTable.TRANSFER_TYPE))));
                    record.setMoney(cursor.getFloat(cursor.getColumnIndex(TransferRecordTable.TRANSFER_VALUE)));
                    list.add(0, record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Logger.e(TAG, "get transfer in record list error " , e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 获取转出资产列表
     * 
     * @return
     */
    public List<TransferRecord> getTransferOutRecordList() {
        List<TransferRecord> list = new ArrayList<TransferRecord>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(TransferRecordTable.TABLE_NAME, null, TransferRecordTable.TRANSFER_TYPE + " = ? ",
                    new String[] { TransferType.OUT.name() }, null, null, TransferRecordTable.TRANSFER_TIME);
            if (cursor.moveToFirst()) {
                do {
                    TransferRecord record = new TransferRecord();
                    record.setId(cursor.getString(cursor.getColumnIndex(TransferRecordTable.RECORD_ID)));
                    record.setTime(cursor.getString(cursor.getColumnIndex(TransferRecordTable.TRANSFER_TIME)));
                    record.setType(TransferType.valueOf(cursor.getString(cursor
                            .getColumnIndex(TransferRecordTable.TRANSFER_TYPE))));
                    record.setMoney(cursor.getFloat(cursor.getColumnIndex(TransferRecordTable.TRANSFER_VALUE)));
                    list.add(0, record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Logger.e(TAG, "get transfer out record list error " , e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 导出数据库数据到SD卡目录下的{@link Constants#EXPORT_OR_IMPORT_FILE_NAME}文件内
     * 
     * @return
     */
    public boolean exportData() {
        FileWriter writer = null;
        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator
                    + Constants.EXPORT_OR_IMPORT_FILE_NAME;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            String newLine = "\r";
            String spliter = ",";
            if (file.createNewFile()) {
                writer = new FileWriter(file);
                List<TransferRecord> transferRecords = getTransferRecordList();
                writer.write(transferRecords.size() + newLine);
                // 导出时时间正序排序
                for (int i = transferRecords.size() - 1; i >= 0; i--) {
                    TransferRecord transferRecord = transferRecords.get(i);
                    String context = transferRecord.getId() + spliter + transferRecord.getMoney() + spliter
                            + transferRecord.getTime() + spliter + transferRecord.getType().getValue();
                    writer.write(context + newLine);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 把SD卡目录下的stockaccount.db文件内的数据导入到数据库
     * 
     * @return
     */
    public boolean importData() {
        BufferedReader reader = null;
        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator
                    + Constants.EXPORT_OR_IMPORT_FILE_NAME;
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
            String spliter = ",";
            String line;
            int number;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            List<TransferRecord> transferRecords = getTransferRecordList();
            line = reader.readLine();
            if (line != null) {
                number = Integer.valueOf(line);
                for (int i = 0; i < number; i++) {
                    line = reader.readLine();
                    String values[] = line.split(spliter);
                    TransferRecord transferRecord = new TransferRecord();
                    transferRecord.setId(values[0]);
                    transferRecord.setMoney(Float.valueOf(values[1]));
                    transferRecord.setTime(values[2]);
                    transferRecord.setType(TransferType.valueOf(Integer.valueOf(values[3])));
                    transferRecords.add(transferRecord);
                }
            }
            for (int i = 0; i < transferRecords.size(); i++) {
                if (!addTransferRecord(transferRecords.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
