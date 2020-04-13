package cn.com.sec.ynlogurlscan.mongo;

import cn.com.sec.ynlogurlscan.conf.Constants;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结果保存
 */
public class MongoInsert {


    private static Logger log = LoggerFactory.getLogger(MongoInsert.class);
    /**
     * 保存结果
     */
    public static void save(MongoDatabase db,Document updater, Document document){
        document.remove("_id");
        db.getCollection(Constants.DEST_TABLE_NAME).updateMany(updater,new Document("$set",document));
    }

    /**
     * 保存备份
     */
    public static void bakup(MongoDatabase db, Document document){
        document.remove("_id");
        db.getCollection(Constants.BAK_TABLE_NAME).insertOne(document);
    }
}
