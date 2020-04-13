package cn.com.sec.ynlogurlscan.mongo;

import cn.com.sec.ynlogurlscan.conf.Constants;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * mongo根据条件查询
 */
public class MongoFilter {

    /**
     * 常用白名单过滤
     * @return
     */
    public static void filterSensitive(MongoDatabase db, Document document,Document filter){
        Document first = db.getCollection(Constants.WHITE_SENSITIVE_TABLE_NAME).find(filter).first();
        if (null != first && !first.isEmpty()){
            document.put("dataType","1");
            document.put("urlResult","白名单");
        }
    }

    /**
     * 白名单过滤
     * @return
     */
    public static void filterWhite(MongoDatabase db, Document document,Document filter){
        Document first = db.getCollection(Constants.WHITE_LIST_TABLE_NAME).find(document).first();
        if (null != first && !first.isEmpty()){
            document.put("dataType","2");
            document.put("urlResult","白名单");
        }
    }

    /**
     * 黑名单过滤
     * @return
     */
    public static void filterBlack(MongoDatabase db, Document document,Document filter){
        Document first = db.getCollection(Constants.BLACK_LIST_TABLE_NAME).find(document).first();
        if (null != first && !first.isEmpty()){
            document.put("dataType","3");
            document.put("urlResult","黑名单");
            document.put("word",first.getString("word"));
        }
    }

}
