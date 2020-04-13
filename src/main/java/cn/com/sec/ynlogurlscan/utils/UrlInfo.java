package cn.com.sec.ynlogurlscan.utils;

import cn.com.sec.ynlogurlscan.commom.MongoDBClient;
import cn.com.sec.ynlogurlscan.exception.BusinessException;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UrlInfo {

    private static Logger log      = LoggerFactory.getLogger(UrlInfo.class);
    public static final String JHAPPKEY = "a209e2fe2f84d5ecc00cffcb42e21d13";

    public static String sendGet(String api, String domain) {
        String result = "";
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            /*String proxyHost = "10.209.204.73";
            String proxyPort = "3128";
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", proxyPort);*/
            URL realUrl = new URL(api + domain);
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", "");
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            connection.connect();

            //            System.out.println(connection.getResponseCode());
            if (connection.getResponseCode() >= 400) {
                return result;
            }
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            // System.out.println(new String(result.getBytes("GBK"), "UTF-8"));
        } catch (Exception e) {
            //e.printStackTrace();
            //log.warn("发送GET请求出现异常！" + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                //log.warn("URLConnection close error:" + e2.getMessage());
            }
        }
        return result;
    }

    public static byte[] sendGet(String api) {
        String result = "";
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            URL realUrl = new URL(api);
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", "");
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();

            //            System.out.println(connection.getResponseCode());
            if (connection.getResponseCode() >= 400) {
                return result.getBytes();
            }
            in = connection.getInputStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = 0;

            //防止出现下载文件，只获取html
            int countNum = 0;
            if (null != connection.getContentType()
                && connection.getContentType().indexOf("text/html") != -1) {
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                    countNum++;
                    if (countNum >= 350) {//读取次数过大说明为非正常网页 跳过
                        //System.out.println("读取次数为" + countNum);
                        //return out.toByteArray();
                        throw new BusinessException("url读取过大");
                    }
                }
            }

            return out.toByteArray();
        } catch (BusinessException e) {
            //log.warn("RuntimeException！" + api);
            throw new BusinessException("url读取过大");
        } catch (Exception e) {
           // e.printStackTrace();
            //log.warn("发送GET请求出现异常！" + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                //log.warn("URLConnection close error:" + e2.getMessage());
            }
        }
        return result.getBytes();
    }

    public static boolean getSEO(MongoDBClient client, String host) {
        String[] domain = host.split("\\.");
        String domainStr = domain[domain.length - 2] + "." + domain[domain.length - 1];
        String api = "http://www.sojson.com/api/beian/";
        String jsonStr = sendGet(api, domainStr);
        if (null == jsonStr || jsonStr.equals("")) {
            return false;
        }
        Document seo = Document.parse(jsonStr);
        Document site = new Document();
        boolean result = false;

        if (null == seo || (seo.containsKey("type") && seo.getInteger("type") != 200)) {
            site.append("domain", domainStr);
            site.append("malicetype", "blacklist");
        } else { //已备案
            site.append("siteDomain", domainStr);
            site.append("SEO", seo);
            result = true;
        }
        client.insert(site);
        return result;

    }

    public static Document getSEOByJH(MongoDBClient client, String host) {
        Document site = new Document();
        String[] domain = host.split("\\.");
        String domainStr;
        if (host.endsWith("com.cn") && domain.length >= 3) {
            domainStr = domain[domain.length - 3] + "." + domain[domain.length - 2] + "."
                        + domain[domain.length - 1];
        } else {
            domainStr = domain[domain.length - 2] + "." + domain[domain.length - 1];
        }

        String api = "http://api.juheapi.com/japi/beian?key=a209e2fe2f84d5ecc00cffcb42e21d13&type=1&keyword="
                     + domainStr + "&v=1.0";
        String jsonStr = sendGet(api, "");

        if (null == jsonStr || jsonStr.equals("")) {
            site.append("jsonStr", "接口返回空或超时");
            return site;
        }

        Document resultDoc = Document.parse(jsonStr);
        //		System.out.println("seo:"+resultDoc.toString());

        if (resultDoc.containsKey("error_code") && resultDoc.getInteger("error_code") == 0) {
            ArrayList<Document> seoList = (ArrayList<Document>) resultDoc.get("result");
            if (null != seoList && seoList.size() > 0) {
                site.append("SEO", seoList.get(0));
            }
        } else {
            site.append("jsonStr", jsonStr);
            site.append("resultDoc", resultDoc);
        }
        site.append("siteDomain", domainStr);
        return site;
    }

    /*    public static Map<String, String> getContent(String host, String url, String port,
                                                 SensitivewordFilter filter) {
        Map<String, String> map = new HashMap<String, String>();
        String htmlUrl = "";
        //		if(null==port||port.equals("")||port.equals("0")){
        //			map.put("result", "0");
        //			return map;
        //		}
        if (url.trim().startsWith("http")) {
            htmlUrl = url;
        } else if (null == port || port.equals("") || port.equals("0")) {
            map.put("result", "0");
            return map;
        } else if (port.equals("443")) {
            htmlUrl = "https://" + host + url;
        } else if (port.equals("80")) {
            htmlUrl = "http://" + host + url;
        } else {
            htmlUrl = "http://" + host + ":" + port + url;
        }
    
        //默认编码
        String charset = "UTF-8";
        String string = "";
        byte[] byteHtml = UrlInfo.sendGet(htmlUrl);
        try {
            string = new String(byteHtml, charset);
        } catch (UnsupportedEncodingException e1) {
            log.warn("Decode warn " + e1.getMessage());
        }
        org.jsoup.nodes.Document html = Jsoup.parse(string);
    
        Elements elements = html.select("meta");
    
        //判断编码
        for (Element e : elements) {
            String tempStr = e.attr("content").toString();
            tempStr.toLowerCase();
            if (tempStr.indexOf("gb2312") != -1 || tempStr.indexOf("gbk") != -1) {
                charset = "GBK";
            }
        }
    
        String meta = "";
    
        if (charset.equals("UTF-8")) {
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]", "") + ":" + e.attr("content") + ",";
            }
        } else {
            //再次转码
            string = "";
            try {
                string = new String(byteHtml, charset);
            } catch (UnsupportedEncodingException e1) {
                log.warn("again Decode warn " + e1.getMessage());
            }
            html = Jsoup.parse(string);
    
            elements = html.select("meta");
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]|,，", "") + ":" + e.attr("content")
                        + ",";
            }
        }
    
        map.put("title", html.title().replaceAll("[,|，]", "、"));
        map.put("meta", meta.replaceAll("[,|，]", "、"));
        String extisFirstWord = filter.getExtisFirstWord(html.title() + meta);
        map.put("result", extisFirstWord);
        return map;
    
    }*/

    
    public static String delHtmlTags(String htmlStr) {
        //定义script的正则表达式，去除js可以防止注入
        String scriptRegex="<script[^>]*?>[\\s\\S]*?<\\/script>";
        //定义style的正则表达式，去除style样式，防止css代码过多时只截取到css样式代码
        String styleRegex="<style[^>]*?>[\\s\\S]*?<\\/style>";
        //定义HTML标签的正则表达式，去除标签，只提取文字内容
        String htmlRegex="<[^>]+>";
        //定义空格,回车,换行符,制表符   --\\s*|
        String spaceRegex = "\t|\r|\n";
 
        // 过滤script标签
        htmlStr = htmlStr.replaceAll(scriptRegex, "");
        // 过滤style标签
        htmlStr = htmlStr.replaceAll(styleRegex, "");
        // 过滤html标签
        htmlStr = htmlStr.replaceAll(htmlRegex, "");
        // 过滤空格等
        htmlStr = htmlStr.replaceAll(spaceRegex, "");
        return htmlStr.trim(); // 返回文本字符串
    }
    /**
     * 获取HTML代码里的内容
     * @param htmlStr
     * @return
     */
    public static String getTextFromHtml(String htmlStr){
        //去除html标签
        htmlStr = delHtmlTags(htmlStr);
        //去除空格" "
        htmlStr = htmlStr.replaceAll(" ","");
        return htmlStr;
    }
    
	public static String getContent(String host, String uri) {
		String htmlUrl = host + uri;
		if (htmlUrl.trim().startsWith("http")) {
			htmlUrl = htmlUrl;
		} else {
			htmlUrl = "http://" + htmlUrl;
		}

		// 默认编码
		String charset = "UTF-8";
		String string = "";
		byte[] byteHtml = null;
		try {

			byteHtml = UrlInfo.sendGet(htmlUrl);
		} catch (BusinessException e) {
			return "";
		}
		try {
			string = new String(byteHtml, charset);
		} catch (UnsupportedEncodingException e1) {
			// log.warn("Decode warn " + e1.getMessage());
		}
		org.jsoup.nodes.Document html = Jsoup.parse(string);

		Elements elements = html.select("meta");
		// 判断编码
		for (Element e : elements) {
			String tempStr = e.attr("content").toString();
			if (tempStr.toLowerCase().contains("gb2312") || tempStr.toLowerCase().contains("gbk")) {
				charset = "GBK";
			}
		}

		if (charset.equals("GBK")) {
			string = "";
			try {
				string = new String(byteHtml, charset);
			} catch (UnsupportedEncodingException e1) {
				// log.warn("again Decode warn " + e1.getMessage());
			}
		}

		return string.replaceAll("[,|，]", "、");

	}   
    
    
    public static Map<String, Object> getContent(String url) {
        Map<String, Object> map = new HashMap<String, Object>();
        String htmlUrl = "";
        if (url.trim().startsWith("http")) {
            htmlUrl = url;
        } else {
            htmlUrl = "http://" + url;
        }

        //默认编码
        String charset = "UTF-8";//GBK UTF-8
        String string = "";
        //byte[] byteHtml = UrlInfo.sendGet(htmlUrl);
        byte[] byteHtml = null;
        try {

            byteHtml = UrlInfo.sendGet(htmlUrl);
        } catch (BusinessException e) {
            //e.printStackTrace();
            map.put("urlType", "dataError");
            map.put("title", "");
            map.put("meta", "");
            map.put("htmlCode", "");
            return map;
        }
        try {
            string = new String(byteHtml, charset);
        } catch (UnsupportedEncodingException e1) {
            //log.warn("Decode warn " + e1.getMessage());
        }
        org.jsoup.nodes.Document html = Jsoup.parse(string);

        Elements elements = html.select("meta");

        //判断编码
        for (Element e : elements) {
            String tempStr = e.attr("content").toString();
            if (tempStr.toLowerCase().contains("gb2312") || tempStr.toLowerCase().contains("gbk")) {
                charset = "GBK";
            }
        }

        String meta = "";
        if (charset.equals("UTF-8")) {
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]", "") + ":" + e.attr("content") + ",";
            }
        } else {
            //再次转码
            string = "";
            try {
                string = new String(byteHtml, charset);
            } catch (UnsupportedEncodingException e1) {
                //log.warn("again Decode warn " + e1.getMessage());
            }
            html = Jsoup.parse(string);

            elements = html.select("meta");
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]|,", "") + ":" + e.attr("content")
                        + ",";
            }
        }

        /*        List<String> titleList = new ArrayList<String>();
        elements = html.select("title");
        int num = 0;
        for (Element e : elements) {
            num++;
            titleList.add("Title" + ":" + e.ownText().replaceAll("[,|，]", "、"));
        }*/

        //map.put("urlType", "");
        map.put("title", html.title());
        map.put("meta", meta.replaceAll("[,|，]", "、"));
        map.put("htmlCode", getTextFromHtml(string.replaceAll("[,|，]", "、")));//string.replaceAll("[^\u4e00-\u9fa5]", "")过滤非中文字符
//        String extisFirstWord = filter.getExtisFirstWord(string.replaceAll("[,|，]", "、"));
//        map.put("word", extisFirstWord);
        return map;

    }

    /* public static Map<String, String> getContent(String host, String url, String port,
                                                 SensitivewordFilter filter) {
        Map<String, String> map = new HashMap<String, String>();
        String htmlUrl = "";
        //      if(null==port||port.equals("")||port.equals("0")){
        //          map.put("result", "0");
        //          return map;
        //      }
        if (host.trim().startsWith("http")) {
            htmlUrl = url;
        } else if (null == port || port.equals("") || port.equals("0")) {
            map.put("result", "0");
            return map;
        } else if (port.equals("443")) {
            htmlUrl = "https://" + host + url;
        } else if (port.equals("80")) {
            htmlUrl = "http://" + host + url;
        } else {
            htmlUrl = "http://" + host + ":" + port + url;
        }
    
        //默认编码
        String charset = "UTF-8";
        String string = "";
        byte[] byteHtml = UrlInfo.sendGet(htmlUrl);
        try {
            string = new String(byteHtml, charset);
        } catch (UnsupportedEncodingException e1) {
            log.warn("Decode warn " + e1.getMessage());
        }
        org.jsoup.nodes.Document html = Jsoup.parse(string);
    
        Elements elements = html.select("meta");
    
        //判断编码
        for (Element e : elements) {
            String tempStr = e.attr("content").toString();
            tempStr.toLowerCase();
            if (tempStr.indexOf("gb2312") != -1 || tempStr.indexOf("gbk") != -1) {
                charset = "GBK";
            }
        }
    
        String meta = "";
    
        if (charset.equals("UTF-8")) {
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]", "") + ":" + e.attr("content") + ",";
            }
        } else {
            //再次转码
            string = "";
            try {
                string = new String(byteHtml, charset);
            } catch (UnsupportedEncodingException e1) {
                log.warn("again Decode warn " + e1.getMessage());
            }
            html = Jsoup.parse(string);
    
            elements = html.select("meta");
            for (Element e : elements) {
                meta += e.attr("name").replaceAll("[^a-zA-Z]|,，", "") + ":" + e.attr("content")
                        + ",";
            }
        }
    
        map.put("title", html.title().replaceAll("[,|，]", "、"));
        map.put("meta", meta.replaceAll("[,|，]", "、"));
        String extisFirstWord = filter.getExtisFirstWord(html.title() + meta);
        map.put("result", extisFirstWord);
        return map;
    
    }*/

    public static void getWhois(MongoDBClient client, String domain) {

    }

    public static void main(String[] args) {
        //String sendGet = UrlInfo.sendGet("http://www.sina.com", "");
        //System.out.println(sendGet);
        //urlContext:msg.71.am  /v5/alt/act?&rseat=full_ply_pmwxhdyl&block=bofangqi2&rpage=full_ply&t=20&p1=2_22_222&u=867376021556564&v=8.5.0&de=m3pp0tlz30ixqz7s&hu=-1&mkey=b398b8ccbaeacca840073a7ee9b7e7e6&mod=cn_s&qyidv2=37F83A87286C039F29087704FD6FD0E9&aqyid=867376021556564_9c6a7a1847a278a4_C4Z07Z2FZ95Z7DZ5F&pps=0&pu=1349451365&cupid_uid=867376021556564&secure_p=GPhone&gps=;&lang=zh_CN&app_lm=cn&req_times=0&req_sn=1495858100260  3
//        Map<String, Object> content = UrlInfo.getContent("002002z.com", "", null);
//        System.out.println(content.get("title"));
//        System.out.println(content.get("meta"));

    }
}
