package cn.com.sec.ynlogurlscan.conf;

public class Constants {

    //mongo地址
    public static final String MONGO_URI="mongodb://root:hhjx%401908@fqz18:27017,fqz19:27017,fqz20:27017/yn-yd-fqz?authSource=admin";
    //数据库名称
    public static final String MONGO_DB_NAME="yn-yd-fqz";
    //查询数据表名
    public static final String SRC_TABLE_NAME="dongRuan";
    //结果数据数表名
    public static final String DEST_TABLE_NAME=SRC_TABLE_NAME;
    //备份数据库表名
    public static final String BAK_TABLE_NAME="dongRuanOwner";
    //常用数据表名
    public static final String WHITE_SENSITIVE_TABLE_NAME="white_sensitive";
    //白名单数据表名
    public static final String WHITE_LIST_TABLE_NAME="whiteList";
    //黑名单数据表名
    public static final String BLACK_LIST_TABLE_NAME="blackList";
    //关键词数据表名
    public static final String KEY_WORD_TABLE_NAME="wordList";


}
