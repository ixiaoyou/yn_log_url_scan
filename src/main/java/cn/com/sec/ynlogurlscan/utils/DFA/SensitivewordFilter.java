package cn.com.sec.ynlogurlscan.utils.DFA;

import java.io.Serializable;
import java.util.*;

/**
 * @date 2017-10-12
 * @author 
 */
public class SensitivewordFilter implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("rawtypes")
    private Map               sensitiveWordMap = null;
    public static int         minMatchTYpe     = 1;   //最小匹配规则
    public static int         maxMatchType     = 2;   //最大匹配规则

    SensitiveWordInit         sensitiveWordInit;

    /**
     * 构造函数，初始化词库
     */
    private SensitivewordFilter() {
        sensitiveWordMap = new SensitiveWordInit().initKeyWord();
    }

    public SensitivewordFilter(Set words) {
        sensitiveWordMap = new SensitiveWordInit().initKeyWord(words);
    }

    /**
     * 手动调用，初始化词库
     */
    private void init() {
        sensitiveWordMap = sensitiveWordInit.initKeyWord();
    }
    
    public Map getSensitiveWordMap(){
    	return this.sensitiveWordMap;
    }

    /**
     * 判断文字是否包含字符
     * @param txt  文字
     * @param matchType  匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     * @return 若包含返回true，否则返回false
     * @version 1.0
     */
    public boolean isContaintSensitiveWord(String txt, int matchType) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = this.checkSensitiveWord(txt, i, matchType); //判断是否包含字符
            if (matchFlag > 0) { //大于0存在，返回true
                flag = true;
            }
        }
        return flag;
    }

    public boolean getContaintSensitiveWord(String txt, int matchType) {
        boolean flag = false;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = this.checkSensitiveWord(txt, i, matchType); //判断是否包含字符
            if (matchFlag > 0) { //大于0存在，返回true

                System.out.println(txt.substring(i, matchFlag + i));
                flag = true;
                return true;
            }
        }
        return flag;
    }

    /**
     * 获取文字中的词
     * @param txt 文字
     * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     * @return
     * @version 1.0
     */
    public Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<String>();

        for (int i = 0; i < txt.length(); i++) {
            int length = checkSensitiveWord(txt, i, matchType); //判断是否包含字符
            //System.out.println(length);
            if (length > 0) { //存在,加入list中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1; //减1的原因，是因为for会自增
            }
        }

        return sensitiveWordList;
    }

    /**
     * 替换字字符
     * @param txt
     * @param matchType
     * @param replaceChar 替换字符，默认*
     * @version 1.0
     */
    public String replaceSensitiveWord(String txt, int matchType, String replaceChar) {
        String resultTxt = txt;
        if (sensitiveWordMap != null) {
            Set<String> set = getSensitiveWord(txt, matchType); //获取所有的词
            Iterator<String> iterator = set.iterator();
            String word = null;
            String replaceString = null;
            while (iterator.hasNext()) {
                word = iterator.next();
                replaceString = getReplaceChars(replaceChar, word.length());
                resultTxt = resultTxt.replaceAll(word, replaceString);
            }
        }

        return resultTxt;
    }

    /**
     * 获取替换字符串
     * @param replaceChar
     * @param length
     * @return
     * @version 1.0
     */
    private String getReplaceChars(String replaceChar, int length) {
        String resultReplace = replaceChar;
        for (int i = 1; i < length; i++) {
            resultReplace += replaceChar;
        }

        return resultReplace;
    }

    /**
     * 检查文字中是否包含字符，检查规则如下：<br>
     * @param txt
     * @param beginIndex
     * @param matchType
     * @return，如果存在，则返回词字符的长度，不存在返回0
     * @version 1.0
     */
    @SuppressWarnings({ "rawtypes" })
    public int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        boolean flag = false; //词结束标识位：用于词只有1位的情况
        int matchFlag = 0; //匹配标识数默认为0
        char word = 0;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            nowMap = (Map) nowMap.get(word); //获取指定key
            if (nowMap != null) { //存在，则判断是否为最后一个
                matchFlag++; //找到相应key，匹配标识+1 
                if ("1".equals(nowMap.get("isEnd"))) { //如果为最后一个匹配规则,结束循环，返回匹配标识数
                    flag = true; //结束标志位为true   
                    if (SensitivewordFilter.minMatchTYpe == matchType) { //最小规则，直接返回,最大规则还需继续查找
                        break;
                    }
                }
            } else { //不存在，直接返回
                break;
            }
        }
        if (matchFlag < 2 || !flag) { //长度必须大于等于1，为词 
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * 验证是否存敏感词  存在返回true 不存在返回false
     * 以开头为必须匹配，开头匹配不上，之后内容不做匹配
     * @param str 输入字符串
     * @return Boolean
     * @Data 17-10-12
     */
    public Boolean checkIsExtisWord(String str) {
        /*        for (int i = 0; i < str.length(); i++) {
            int length = checkSensitiveWord(str, i, 1); //判断是否包含字符
            //System.out.println(length);
            if(length!=0){
            	return true;
            }
        }*/

        int length = checkSensitiveWord(str, 0, 1); //判断是否包含字符
        //System.out.println(length);
        if (length != 0) {
            return true;
        }

        return false;
    }

    /**
     * 获取匹配到的第一个敏感词，匹配到返回敏感词，没匹配到返回null
     * 17-11-06
     * @param txt
     * @return
     */
    public String getExtisFirstWord(String txt) {
        String word = null;
        for (int i = 0; i < txt.length(); i++) {
            int matchFlag = this.checkSensitiveWord(txt, i, 1); //判断是否包含字符
            if (matchFlag > 0) { //大于0存在，返回
                return txt.substring(i, matchFlag + i);
            }
        }
        return word;
    }
    

    public static List<Object> CompareMapgt1(Map src,Map desc){
    	List<Object> result = new ArrayList<Object>();

        for(Iterator it = src.keySet().iterator(); it.hasNext();){
        	Object s =  it.next();
        	if(s.toString().equals("isEnd")){
        		continue;
        	}
        	if(!desc.getOrDefault(s, "").equals("")){
        		result.add(s);
        		result.add(src.get(s));
        		result.add(desc.get(s));
        	}else{
        		 result.add("false");
        		 break;
        	}
        }	    	
      
    	return result;
    }
    
    public static boolean CompareMapeq1(Map src,Map desc){
        
        if("1".equals(src.get("isEnd"))){
        	return true;
        }
        return false;

    }	    

    public static String CompareMap(Map src,Map desc) {
    	StringBuffer keywordResult = new StringBuffer();

        List<Object> result_temp;
        Map keyWord_temp_map;
        Map htmlWord_temp_map;
        
        for(Iterator it = src.keySet().iterator(); it.hasNext();){
        	Object s =  it.next();

        	if(!desc.getOrDefault(s, "").equals("")){
        		keywordResult.append(s);
        		keyWord_temp_map = (Map) src.get(s); 
        		htmlWord_temp_map = (Map) desc.get(s);
        		
        		while(keyWord_temp_map.size()>1&&htmlWord_temp_map.size()>1){
        			result_temp = CompareMapgt1(keyWord_temp_map,htmlWord_temp_map);
        			if(result_temp.size()==1){
        				keywordResult.setLength(0);
        				break;
        			}
        			keywordResult.append(result_temp.get(0));
        			keyWord_temp_map = (Map)result_temp.get(1);
        			htmlWord_temp_map = (Map)result_temp.get(2);
        		}
        		
        		if(keyWord_temp_map.size()==1||htmlWord_temp_map.size()==1){
        			if(CompareMapeq1(keyWord_temp_map,htmlWord_temp_map)){
        				return keywordResult.toString();
        			}else{
        				keywordResult.setLength(0);
        			}
        		}
        	}
        }

        return "";
    }

    public static void main(String[] args) {
        /*  SensitivewordFilter filter = new SensitivewordFilter();
        System.out.println("词的数量：" + filter.sensitiveWordMap.size());
        String string = "baidu.com太多的loc.map.baidu.com";
        System.out.println("待检测语句字数：" + string.length());
        long beginTime = System.currentTimeMillis();
        Set<String> set = filter.getSensitiveWord(string, 1);
        
        long endTime = System.currentTimeMillis();
        System.out.println("语句中包含词的个数为：" + set.size() + "。包含：" + set);
        System.out.println("总共消耗时间为：" + (endTime - beginTime));
        
        String replaceSensitiveWord = filter.replaceSensitiveWord(string, 1, "*");
        System.out.println("替换后的内容为：" + replaceSensitiveWord);*/

        SensitivewordFilter filter = new SensitivewordFilter();
        System.out.println("词的数量：" + filter.sensitiveWordMap.size());
        String string = "问哦qq.com";
        /*       System.out.println("待检测语句字数：" + string.length());
        long beginTime = System.currentTimeMillis();
        Boolean checkIsExtisWord = filter.checkIsExtisWord(string); //判断是否包含字符
        System.out.println(checkIsExtisWord);
        long endTime = System.currentTimeMillis();
        System.out.println("总共消耗时间为：" + (endTime - beginTime));*/

        filter.getContaintSensitiveWord(string, 0);

        //String replaceSensitiveWord = filter.replaceSensitiveWord(string, 1, "*");
        // System.out.println("字符匹配开始下标为：" + checkSensitiveWord);
    }
}
