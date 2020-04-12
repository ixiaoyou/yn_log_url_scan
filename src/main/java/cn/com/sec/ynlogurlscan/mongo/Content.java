package cn.com.sec.ynlogurlscan.mongo;

import cn.com.sec.ynlogurlscan.utils.DFA.DFAUtils;
import cn.com.sec.ynlogurlscan.utils.UrlInfo;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.mining.word.WordInfo;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  获取页面并分析
 */
public class Content {


    /**
     * 主要分析函数
     * @param dfaUtils
     * @param doc
     */
    public static void analysis(DFAUtils dfaUtils, Document doc){
        Map<String, Object> map = new HashMap<>();
        //爬取
        map = UrlInfo.getContent(doc.getString("url"));
        //获取内容
        String htmlCode = map.get("htmlCode").toString();
        if (StringUtils.isNotBlank(htmlCode)) {
            cn.com.sec.ynlogurlscan.utils.DFA.Tuple.T2<String, String> res = dfaUtils.handleContent(htmlCode);
            String type = res.getFirst();
            String keyWord = res.getSecond();
            if (StringUtils.isNotBlank(keyWord)) {
                doc.append("dataType", "5");
                if (StringUtils.isNotBlank(type)) {
                    doc.append("type", type);// 类型
                }
                doc.append("word", keyWord);// 匹配的敏感词
                doc.append("htmlTitle", map.get("title"));
                doc.append("htmlMeta", map.get("meta"));
                List<WordInfo> newWords = HanLP.extractWords(htmlCode,5, true);
                doc.append("newWords", getNewWords(newWords));
                List<WordInfo> newForWords = HanLP.extractWords(htmlCode,5, false);
                doc.append("newForWords", getNewWords(newForWords));
                doc.append("htmlContent",htmlCode);

            }else {
                doc.append("dataType", "6"); //6代表没有关键词匹配中
                doc.append("type", "正常");
            }
        }else{
            doc.append("dataType", "4");//4代表获取到页面的内容是空的
            doc.put("type","无法打开");
        }
    }

    /**
     * 提取新词
     *
     * @param list
     * @return
     */
    public static List<String> getNewWords(List<WordInfo> list){
        List<String> strList = new ArrayList<String>();
        for(WordInfo w:list){
            strList.add(w.text);
        }
        return strList;
    }
}
