package cn.com.sec.ynlogurlscan.mongo;

import cn.com.sec.ynlogurlscan.conf.Constants;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * 结果保存
 */
public class MongoInsert {

    /**
     * 保存结果
     */
    public static void save(MongoDatabase db, Document document){
        db.getCollection(Constants.DEST_TABLE_NAME).insertOne(document);
    }

    /**
     * 保存备份
     */
    public static void bakup(MongoDatabase db, Document document){
        db.getCollection(Constants.BAK_TABLE_NAME).insertOne(document);
    }
}
