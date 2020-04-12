package cn.com.sec.ynlogurlscan.utils.DFA;

import java.io.Serializable;
import java.util.*;

/**
 * @author mocong
 * @date 2017年5月22日 下午2:52:30
 * 
 */
public class SensitiveWordInit implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String            ENCODING         = "GBK"; //字符编码
    @SuppressWarnings("rawtypes")
    public HashMap            sensitiveWordMap;

    public SensitiveWordInit() {
        super();
    }

    @SuppressWarnings("rawtypes")
    public Map initKeyWord() {
        try {
            //读取词库    //此处从redis获取
            // Set<String> keyWordSet = backConfigRaoImpl
            //    .getBackConfigAll(BackConfigTypeEnum.SENSITIVEWORD);//readSensitiveWordFile();
            //将词库加入到HashMap中
            //addSensitiveWordToHashMap(keyWordSet);

            //spring获取application，然后
            //webApplicationContext.getServletContext().setAttribute("sensitiveWordMap", sensitiveWordMap);

            Set<String> keyWordSet = new HashSet<String>();
            keyWordSet.add("moc.qq");
            keyWordSet.add("moc.udiab");
            //将词库加入到HashMap中
            addSensitiveWordToHashMap(keyWordSet);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    public Map initKeyWord(Set words) {
        try {
            //将词库加入到HashMap中
            addSensitiveWordToHashMap(words);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    /**
     * 读取词库，将词放入HashSet中，构建一个DFA算法模型：<br>
     * 中 = {
     *      isEnd = 0
     *      国 = {<br>
     *           isEnd = 1
     *           人 = {isEnd = 0
     *                民 = {isEnd = 1}
     *                }
     *           男  = {
     *                 isEnd = 0
     *                  人 = {
     *                       isEnd = 1
     *                      }
     *              }
     *           }
     *      }
     *  五 = {
     *      isEnd = 0
     *      星 = {
     *          isEnd = 0
     *          红 = {
     *              isEnd = 0
     *              旗 = {
     *                   isEnd = 1
     *                  }
     *              }
     *          }
     *      }
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        sensitiveWordMap = new HashMap(keyWordSet.size()); //初始化词容器，减少扩容操作
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next(); //关键字
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                char keyChar = key.charAt(i); //转换成char型
                Object wordMap = nowMap.get(keyChar); //获取

                if (wordMap != null) { //如果存在该key，直接赋值
                    nowMap = (Map) wordMap;
                } else { //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<String, String>();
                    newWorMap.put("isEnd", "0"); //不是最后一个
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1"); //最后一个
                }
            }
        }
    }

    /**
     * 读取词库中的内容，将内容添加到set集合中
     * @throws Exception 
     */
    @SuppressWarnings("resource")
    private Set<String> readSensitiveWordFile() throws Exception {
        Set<String> set = null;

        /* File file = new File("D:\\CensorWords.txt"); //读取文件
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), ENCODING);
        try {
            if (file.isFile() && file.exists()) { //文件流是否存在
                set = new HashSet<String>();
                BufferedReader bufferedReader = new BufferedReader(read);
                String txt = null;
                while ((txt = bufferedReader.readLine()) != null) { //读取文件，将文件内容放入到set中
                    set.add(txt);
                }
            } else { //不存在抛出异常信息
                throw new Exception("词库文件不存在");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            read.close(); //关闭文件流
        }*/

        set = new HashSet<String>();
        set.add("法轮功");
        set.add("自杀");

        return set;
    }
}
