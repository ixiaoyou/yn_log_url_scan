package cn.com.sec.ynlogurlscan.mongo;

import cn.com.sec.ynlogurlscan.conf.Constants;
import cn.com.sec.ynlogurlscan.utils.DFA.DFAUtils;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 *对每一条记录进行，白/黑名单，关键词匹配
 */
public class MongoConsumer implements Consumer<Document> {

    //设置日志
    private static Logger log = LoggerFactory.getLogger(MongoConsumer.class);

    //mongo数据库
    private MongoDatabase db;
    //分词工具
    private DFAUtils dfaUtils;
    //设置时间
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public MongoConsumer( MongoDatabase db){
        this.db=db;
        //获取所有的关键字
        FindIterable<Document> findAll = this.db.getCollection(Constants.KEY_WORD_TABLE_NAME).find();
        //初始化分词器
        dfaUtils = new DFAUtils(findAll);
        for (Document document : findAll) {
            //关键字的中间的空格将会被替换为+号
            String word = document.getString("word").trim().toLowerCase().replace(" ", "+");
            //多个关键词会被拆分。同时将全部组合加入到关键词Map，并加入到自定义词典。
            String[] split = word.split("_");
            for (String wordOne : split) {
                try{
                    CustomDictionary.add(wordOne);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 关键处理函数,处理流程如下
     * 1、常用白名单过滤 dataType=1
     * 2、白名单过滤 dataType=2
     * 3、黑名单过滤 dataType=3
     * 4、关键词匹配 dataType= 4,5,6 4=无法打开页面 5=匹配中关键词 6=没有匹配中关键词
     * @param document
     */
    @Override
    public void accept(Document document) {
        //获取原始
        String oracHost = document.getString("oracHost");
        String fileName = document.getString("fileName");
        log.info("-- Start Analysis Host:{} -- FileName:{} --",oracHost,fileName);
        //获取一级域名
        String host = document.getString("host");
        //根据一级域名过滤
        Document hostFilter = new Document();
        hostFilter.put("host",host);
        //过滤常用白名单
        if("0".equals(document.getString("dataType"))){
            MongoFilter.filterSensitive(db, document,hostFilter);
        }
        //过滤白名单
        if("0".equals(document.getString("dataType"))){
            MongoFilter.filterWhite(db,document,hostFilter);
        }
        //过滤黑名单
        if("0".equals(document.getString("dataType"))){
            hostFilter.remove("host");
            hostFilter.put("url",new Document("$regex",host));
            MongoFilter.filterWhite(db,document,hostFilter);
        }
        //抓取网址
        if("0".equals(document.getString("dataType"))){
            Content.analysis(dfaUtils,document);
        }
        //保存结果
        MongoInsert.save(db,document);
        //保存备份结果
        MongoInsert.bakup(db,document);

        log.info("-- Start Analysis Host:{} -- FileName:{} --",oracHost,fileName);
    }



}
