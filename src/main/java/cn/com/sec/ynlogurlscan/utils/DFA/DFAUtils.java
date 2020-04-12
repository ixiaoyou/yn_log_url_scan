package cn.com.sec.ynlogurlscan.utils.DFA;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFAUtils {
    //没有将typeMap和sensitiveTreeMap定义为static，考虑到，可能这个类会被同时用于不同的关键词库抓取！
    //这可能会造成内存浪费，不过不会太多。
    private HashMap<String, String> typeMap = new HashMap<>();
    private Map sensitiveTreeMap;
    //分词器是static的，所有实例公用同一个分词器
    private Segment segment;


    public DFAUtils(FindIterable<Document> all) {
        initKey(all);
        initSensitiveTreeMap();
        initSeg();
    }

    //初始化分词器
    private void initSeg() {

//        segment = new DoubleArrayTrieSegment("C:\\Users\\Dell\\SVNCheckOut\\All\\ydjt-sca-bljc\\urlscan\\src\\main\\resources\\data\\dictionary\\CoreNatureDictionary.txt",
//                "C:\\Users\\Dell\\SVNCheckOut\\All\\ydjt-sca-bljc\\urlscan\\src\\main\\resources\\data\\dictionary\\CoreNatureDictionary.ngram.txt",
//                "C:\\Users\\Dell\\SVNCheckOut\\All\\ydjt-sca-bljc\\urlscan\\src\\main\\resources\\data\\dictionary\\stopwords.txt");
//        segment.enableCustomDictionary(false);
        HanLP.Config.IOAdapter = new cn.com.sec.ynlogurlscan.utils.FileIoAdapter();
        //换回以前的分词器，可以对英文进行分词
        segment = HanLP.newSegment().enablePartOfSpeechTagging(false);
//        segment = new DoubleArrayTrieSegment();
        segment.enableCustomDictionary(true);

//        segment.enableJapaneseNameRecognize(true);
        segment.enableMultithreading(false);
//        segment.enableMultithreading(10);
    }

    private void initSensitiveTreeMap() {
        SensitivewordFilter sensitiveWordInit = new SensitivewordFilter(typeMap.keySet());
        sensitiveTreeMap = sensitiveWordInit.getSensitiveWordMap();
    }

    //初始化关键字
    private void initKey(FindIterable<Document> all) {

//        File file = new File("C:\\Users\\Dell\\SVNCheckOut\\All\\xjfz\\xjfzurlscan\\src\\main\\resources\\data\\dictionary\\custom\\wordList.txt");
//        if (file.exists()) {
//            file.delete();
//        }
//        file.createNewFile();
//        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("utf-8")))) {
//            for (Document document : all) {
//                String type = document.getString("type");
//                //关键字的中间的空格将会被替换为+号
//                String word = document.getString("word").trim().toLowerCase().replace(" ", "+");
//                //多个关键词会被拆分。同时将全部组合加入到关键词Map，并加入到自定义词典。
//                String[] split = word.split("_");
//                if (split.length == 1) {
//                    typeMap.put(split[0], type);
//                } else if (split.length >= 2) {
//                    putMultiWord(split, type);
//                }
//                for (String wordOne : split) {
//                    printWriter.println(wordOne);
//                }
//            }
//            //如果数据库中的多关键字，按照 '_' 分隔
//            putMultiWord("哲学_鬼畜".split("_"), "色情");
//            printWriter.println("鬼畜");
//            printWriter.println("哲学");

        for (Document document : all) {
            String type = document.getString("type");
            //关键字的中间的空格将会被替换为+号
            String word = document.getString("word").trim().toLowerCase().replace(" ", "+");
            //多个关键词会被拆分。同时将全部组合加入到关键词Map，并加入到自定义词典。
            String[] split = word.split("_");
            if (split.length == 1) {
                typeMap.put(split[0], type);
            } else if (split.length >= 2) {
                putMultiWord(split, type);
            }
            //System.out.println("办理无抵押贷款====="+CustomDictionary.contains("办理无抵押贷款"));
//            for (String wordOne : split) {
//                CustomDictionary.add(wordOne);
//            }
        }
//        //如果数据库中的多关键字，按照 '_' 分隔
//        putMultiWord("哲学_鬼畜".split("_"), "色情");
//        CustomDictionary.add("哲学");
//        CustomDictionary.add("鬼畜");
//        putMultiWord("我的乖乖".split("_"), "色情");
//        CustomDictionary.add("我的乖乖");
    }

    private void putMultiWord(String[] split, String type) {
        permuteSequence(split, type, 0);
    }

    private void permuteSequence(String[] strArr, String type, int i) {
        String temp;
        if (strArr == null || i > strArr.length || i < 0) {
        } else if (i == strArr.length) {
            String join = String.join("_", strArr);
            typeMap.put(join, type);
        } else {
            for (int j = i; j < strArr.length; j++) {
                temp = strArr[j];//
                strArr[j] = strArr[i];
                strArr[i] = temp;
                permuteSequence(strArr, type, i + 1);
                temp = strArr[j];//
                strArr[j] = strArr[i];
                strArr[i] = temp;
            }
        }
    }

    public String findKeyFast(List<Term> html, Map keyMap) {
        return findKeyWithOrder(html, keyMap);
    }


    @Deprecated
    //有更好的实现方式了！
    //根据html的分词结果来匹配关键字
    public String findKey(List<Term> html, Map keyMap) {
        for (Term term : html) {
            String word = term.word;
            Map nowMap = keyMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (nowMap.containsKey(c)) {
                    nowMap = (Map) nowMap.get(c);
                    if ("1".equals(nowMap.get("isEnd"))) {
                        if (i == word.length() - 1) {
                            return word;
                        } else {
                            break;
                        }
                    } else if (nowMap.containsKey('_') && i == word.length() - 1) {
                        String res = findKey(html, (Map) nowMap.get('_'));
                        if (res != null) {
                            return word + "_" + res;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return null;
    }


    //匹配所有关键字
    public ArrayList<String> findAllKey(List<Term> html, Map keyMap) {
        ArrayList<String> resList = new ArrayList<>();
        for (Term term : html) {
            String word = term.word;
            Map nowMap = keyMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (nowMap.containsKey(c)) {
                    nowMap = (Map) nowMap.get(c);
                    if ("1".equals(nowMap.get("isEnd"))) {
                        if (i == word.length() - 1) {
                            resList.add(word);
                        } else {
                            break;
                        }
                    } else if (nowMap.containsKey('_') && i == word.length() - 1) {
                        String res = findKey(html, (Map) nowMap.get('_'));
                        if (res != null) {
                            resList.add(word + "_" + res);
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return resList;
    }


    //根据html的分词结果来匹配关键字，保持顺序！
    public String findKeyWithOrder(List<Term> html, Map keyMap) {
        for (Term term : html) {
            String word = term.word;
            Map nowMap = keyMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (nowMap.containsKey(c)) {
                    nowMap = (Map) nowMap.get(c);
                    if ("1".equals(nowMap.get("isEnd"))) {
                        if (i == word.length() - 1) {
                            return word;
                        } else {
                            break;
                        }
                    } else if (nowMap.containsKey('_') && i == word.length() - 1) {
                        String res = findKeyWithOrder(html.subList(html.indexOf(term) + 1, html.size()), (Map) nowMap.get('_'));
                        if (res != null) {
                            return word + "_" + res;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 该方法根据提供html文本内容，返回其中命中类型和关键字。<br>
     * 内部实现为多线程的，速度很快：3000Page/s。
     *
     * @param contentByHtml 从html页面提取出来的文本内容和
     * @return 返回命中类型和关键字的元组。
     */
    public synchronized Tuple.T2<String, String> handleContent(String contentByHtml) {

        //将空格替换为+，因为用户自定义词典中有带空格关键字，但是HanLP无法识别。
        String content = contentByHtml.toLowerCase().replace(' ', '+');
        List<Term> htmlSeg = segment.seg(content);
        String keyFast = findKeyFast(htmlSeg, sensitiveTreeMap);
        return new Tuple.T2<>(typeMap.get(keyFast), keyFast);
    }


    public static void main(String[] args) {
//        MongoDBClient client = new MongoDBClient("mongodb://192.168.0.9:27017/yn-yd-fqz", "wordList");
//        DFAUtils[] dfaUtils = new DFAUtils[10];
//        for (int i = 0; i < 10; i++) {
//            dfaUtils[i] = new DFAUtils(client.findAll());
//            System.out.println(DFAUtils.getTypeMap().size());
//            System.out.println(DFAUtils.getSensitiveTreeMap().size());
//        }
//
//
//
////        String url = "http://h3.cnmbtgf.info/pw/thread.php?fid=3";
////        String htmlCode = UrlInfo.getContent("", url);
////        String contentByHtml = ContentExtractor.getContentByHtml(htmlCode);
//
//        String contentByHtml = "我的乖乖是好事情";
//        long startTime = System.currentTimeMillis();
//        for (int i = 0; i < 10000; i++) {
//            Random rand = new Random();
//            new Thread(() -> {
//                int i1 = rand.nextInt(10);
//                Tuple.T2<String, String> res = dfaUtils[i1].handleContent(contentByHtml + UUID.randomUUID().toString());
////                System.out.println(res);
//            }).start();
//        }
//        long executeTime = System.currentTimeMillis() - startTime;
//        System.out.println(executeTime);


//        SensitivewordFilter sensitiveWordInit = new SensitivewordFilter(typeMap.keySet());
//
//        long startTime = System.currentTimeMillis();
//        String htmlUrl = "http://h3.cnmbtgf.info/pw/index.php";
//        String htmlCode = UrlInfo.getContent("", htmlUrl);
////        String htmlCode = "adult video哲学的每个鬼畜是好的事情";
////        String htmlCode = "adult video is not good.".replace(' ', '+');
//        long executeTime = System.currentTimeMillis() - startTime;
//        System.out.println("[INFO]获取url内容完成，html length：" + htmlCode.length() + "，耗时：" + executeTime + "ms");
//
//
//        startTime = System.currentTimeMillis();
//        String contentByHtml = ("哲学的每个鬼畜是好的事情" + ContentExtractor.getContentByHtml(htmlCode)).toLowerCase().replace(' ', '+');
//        System.out.println(contentByHtml);
//        executeTime = System.currentTimeMillis() - startTime;
//        System.out.println("提取html：" + executeTime);
//
//
////        startTime = System.currentTimeMillis();
////        Segment segment = HanLP.newSegment().enablePartOfSpeechTagging(false);
////        List<Term> list = segment.seg(contentByHtml);
////        System.out.println(list);
////        executeTime = System.currentTimeMillis() - startTime;
////        System.out.println("分词用时：" + executeTime);
//
//        //repeat 100次测试性能
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < 10; i++) {
//            stringBuilder.append(contentByHtml);
//        }
//
//
//        startTime = System.currentTimeMillis();
//        List<Term> list = segment.seg(stringBuilder.toString());
//        System.out.println(list);
//        executeTime = System.currentTimeMillis() - startTime;
//        System.out.println("分词用时：" + executeTime);
//
//        startTime = System.currentTimeMillis();
//        String key = findKey(list, sensitiveWordInit.getSensitiveWordMap());
//        String type = typeMap.get(key);
//        System.out.println(type + ":" + key);
//        executeTime = System.currentTimeMillis() - startTime;
//        System.out.println(executeTime);
//
//
//        startTime = System.currentTimeMillis();
//        String key1 = findKeyWithOrder(list, sensitiveWordInit.getSensitiveWordMap());
//        String type1 = typeMap.get(key);
//        System.out.println(type1 + ":" + key1);
//        executeTime = System.currentTimeMillis() - startTime;
//        System.out.println(executeTime);
//
//
//        startTime = System.currentTimeMillis();
//        ArrayList<String> allKey = findAllKey(list, sensitiveWordInit.getSensitiveWordMap());
//        for (String key2 : allKey) {
//            String type2 = typeMap.get(key);
//            System.out.println(type2 + ":" + key2);
//        }
//        executeTime = System.currentTimeMillis() - startTime;
//        System.out.println(executeTime);
    }

}
