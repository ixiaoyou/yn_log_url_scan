package cn.com.sec.ynlogurlscan;

import cn.com.sec.ynlogurlscan.conf.Constants;
import cn.com.sec.ynlogurlscan.mongo.MongoConsumer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * url扫描入口
 */
public class UrlScan {

    //设置日志类别
    private static Logger log = LoggerFactory.getLogger(UrlScan.class);


    public static void main(String[] args) throws InterruptedException {
        Boolean flag = true;
        while (flag){
            //获取mongo客户端
            MongoClient client=getClient();
            //设置客户端数据库
            MongoDatabase db = client.getDatabase(Constants.MONGO_DB_NAME);
            //获取未扫描的数据
            FindIterable<Document> dongruans = getSources(db.getCollection(Constants.SRC_TABLE_NAME));
            //判断是否有数据
            if(null != dongruans){
                //获取Mongo消费者
                MongoConsumer consumer = new MongoConsumer(db);
                //如果获取到数据就开始消费
                dongruans.forEach(consumer);
            }else{
                //没有日志
                log.info("----There are no sites to analyze---------");
            }
            //关闭客户端
            close(client);
            Thread.sleep(1000*60);
        }
    }


    /**
     * 获取mongodb客户端
     * @return
     */
    private static MongoClient getClient(){
        MongoClientURI uri = new MongoClientURI(Constants.MONGO_URI);
        return new MongoClient(uri);
    }


    /**
     * 关闭mongodb客户端
     * @param client mongodb客户端
     */
    private static  void close(MongoClient client){
        if(null != client){
            client.close();
        }
    }


    /**
     * 获取所有没有扫描的记录
     * @param collection
     * @return
     */
    private static FindIterable<Document> getSources(MongoCollection<Document> collection){
        Document document = new Document();
        document.put("state","0");
         return collection.find(document);
    }
}
