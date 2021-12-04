import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

// 找不到符号，可以编译后，再运行一下，或许就会找到了


// 数据结构的信息保存性（后续还要用）、扩展性（新需求下容易调整）、结构一致性（不同方法函数中代码结构趋于一致）非常重要，否则后续的修改会非常麻烦
// Json的扩展性非常好，适合作为数据结构；

public class Format {
    public static void main(String[] args) throws Exception {
        /*
        String str="01234567890";
        //System.out.println(str.substring(2,2).equals("daf\"fdafd")+"___"+"\0".equals(null));
        //System.out.println(str_same_pre(str,5,new String[]{"234"}));
        //System.out.println(str_same_after(str,5,new String[]{"9","8","7","7"}));
        str_indexof("equals(\"daf\\\"fda\"fd\")",0,"\"",new String[]{"\\"},new String[]{"f"}); // 平级的

        */

        //hrefurl_string_browser("https://cseweb.ucsd.edu//~dasgupta/250B/lectures.html");
        // "https://www.163.com/dy/article/GIRC34CO0512B07B.html"
        //

        press();
    }


















































































    //转换为%E4%BD%A0形式
    // 原本默认的
    public static String encode(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    static String errormessage=null;
    public static String error_string(Exception e){
        StringBuilder sb=new StringBuilder(e.toString()+"\r\n");
        StackTraceElement[] error= e.getStackTrace();
        String blankstr="    ";
        for(int i=0,l=error.length;i<l;i++) {
            sb.append(blankstr+"at "+error[i].toString());//(error[i].getMethodName()+"__"+error[i].getLineNumber()+"\r\n");
            blankstr+="    ";
        }
        errormessage=sb.toString();
        e.printStackTrace();
        //System.out.println(errormessage);
        return errormessage;
    }



    static String[] httptype=new String[]{"http","https"};
    public static boolean url_ifhttptype(String urladdress){
        boolean ifhttptype=true;  // 没有:，则默认是http！
        if(urladdress.contains(":")) {
            ifhttptype=false;
            String applicationtype=urladdress.substring(0,urladdress.indexOf(":"));
            for(int i=0,l=httptype.length;i<l;i++)
                if(applicationtype.equals(httptype[i])){
                    ifhttptype=true;
                    break;
                }
        }
        return ifhttptype;
    }

    // 获取网页对应的源码
    public static String hrefurl_string_browser(String urladdress) throws IOException, InterruptedException { // 并自动存储
        // 启动程序类，直接跳过
        if(!url_ifhttptype(urladdress)) return null;

        // 针对于//格式
        int httpindex=urladdress.indexOf("//");
        if(httpindex!=-1) urladdress="http:"+urladdress.substring(httpindex);

        String saveto = url_pcaddress(urladdress);// 获取pc中对应的地址
        File file = new File(saveto);
        if (file.exists()) {
            System.out.println("____alreadyexist___"+urladdress+"---------->"+saveto);
            try {
                String str = txtpath_str(saveto);
                //System.out.println("str__"+str+"__"+saveto);
                return str;
            } catch (IOException e) {
                System.out.println("ReadException____" + saveto + "___" + urladdress);
                return "";
            }
        } else {
            // 不管是否成功，先创建文件，以防同一文件反复连接不上，浪费时间
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("WriteException____" + saveto + "___" + urladdress);
                return "";
            }

            String str = url_string_browser(urladdress);

            str_txtpath(str, saveto); // 顺便存储，方便下次使用
            return str;
        }
    }


    public static void url_string_agenthttp(String url) throws IOException {
        CloseableHttpClient closeableHttpClient= HttpClients.createDefault(); //1、创建实例
        HttpGet httpGet=new HttpGet("http://www.tuicool.com"); //2、创建请求

        httpGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12A365 icroMessenger/5.4.1 NetType/WIFI");

        CloseableHttpResponse closeableHttpResponse=closeableHttpClient.execute(httpGet); //3、执行
        HttpEntity httpEntity=closeableHttpResponse.getEntity(); //4、获取实体
        System.out.println(EntityUtils.toString(httpEntity, "utf-8")); //5、获取网页内容，并且指定编码

        closeableHttpResponse.close();
        closeableHttpClient.close();
    }

    // 用默认的http连接方式，获取网页数据，不如firefox模拟法更加全面，但反映了具体情况，并且速度快！！！
    public static String url_string_http(String urladdress) throws IOException {
        // 启动程序类，不要动
        //if(urladdress.contains(":")) return null;

        URL url = new URL(urladdress);
        HttpURLConnection httpurl=(HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpurl.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line + "\r\n");
            //System.out.println("xxxx");
            line = reader.readLine();
        }
        reader.close();
        String str = sb.toString();

        return str;
    }

    // sum！！！把图片、视频都行，但不能是网页，保存到对应目录，采用流直接传输！！！
    // 注意此时不能添加
    // 返回过程是否顺利
    public static boolean srcurl_path_http(String urladdressorigin) throws IOException {
        // 启动程序类，直接跳过
        if(!url_ifhttptype(urladdressorigin)) return true;

        String urladdress = urladdressorigin;
        // 针对于//格式
        int httpindex=urladdress.indexOf("//");
        if(httpindex!=-1) urladdress="http:"+urladdress.substring(httpindex);
        //String urladdress=urladdressorigin.replaceAll("https://","https://"); // https貌似好多无法兼容，例如百度搜索
        //?????????????????????????????????? 但貌似weixin只能用https，http则无法获取全面的信息？？？
        //百度那是自己跳转了！


        String saveto = url_pcaddress(urladdress);// 获取pc中对应的地址
        // 此时saveto的后缀.html不需要了
        if (saveto.lastIndexOf(".html") != -1)
            saveto = saveto.substring(0, saveto.lastIndexOf(".html"));

        File file = new File(saveto);
        if (file.exists()) { // 存在就不用执行了
            System.out.println("____alreadyexist___"+urladdress+"---------->"+saveto);
        } else {
            try {
                // 不管是否成功，先创建文件，以防同一文件反复连接不上，浪费时间
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("ReadException____" + saveto + "___" + urladdress);
                    return false;
                }

                URL url = new URL(urladdress);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true); // 接受数据。。。已经连接上了就不能设置属性了？？？
                connection.setDoInput(true); // 允许发送参数。。。post要用，实际上百度搜索也要用！！！
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(10000);

                double filesize = 0;//connection.getContentLength()/1024.0;
                // 这个不影响后面的结果的，https://blogs.oracle.com/js/stats.js错误恐怕有别的原因！！！
                // connection的各种get方法导致connected，出现already connected错误，故而必须在get方法前设置setdooutput等各种属性
                // disconnect而后再connect或openconnect都是没用的
                if (filesize <= 10240) { // 10M以内的文件才下载
                    System.out.println(filesize + "KB______" + urladdress);
                    InputStream in = connection.getInputStream(); // 确实有些是无法连接的，例如：https://assets.ucsd.edu/js/jquery.fancybox.js
                    OutputStream out = new FileOutputStream(saveto);
                    byte[] buffer = new byte[1024];
                    int bytelength = 0;
                    while ((bytelength = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytelength);
                    }
                    out.flush();
                    out.close();
                    in.close();
                }
            } catch (MalformedURLException e) {
                System.out.println(urladdress + "_____________Unable to connect");
                return false;
            } catch (IOException e) {
                System.out.println(urladdress + "_____________IOException when connecting to URL");
                return false;
            }
        }

        return true; // true表示过程顺利
    }

    // 根据url与父文件的pcaddress找到相对路径的表达式（便于迁移）
    // ?????有些原本就是相对路径的形式？？？
    public static String url_relativepcurl(String relativeurl, String fatherpc) throws IOException {
        //// 特例
        // 启动程序类，直接不动
        if(!url_ifhttptype(relativeurl)) return relativeurl;
        // 本来就是相对路径
        if ((relativeurl.length() >= 1) && (relativeurl.charAt(0) == '#')) return relativeurl;
        if ((relativeurl.length() >= 2) && relativeurl.substring(0, 2).equals("./")) return relativeurl;
        if ((relativeurl.length() >= 3) && relativeurl.substring(0, 3).equals("../")) return relativeurl;

        /*
        // 把书签#取下。。。最后再加上去。。。用url，则书签自动有了
        String mark=""; int markint=relativeurl.lastIndexOf("#");
        if(markint!=-1) mark=relativeurl.substring(markint);*/

        // 先找到父文件pcaddress对应的绝对url
        String fatherurl = pcaddress_url(fatherpc);


        // 根据relativeurl和fatherurl获取绝对url
        String url = url_relativeactual(relativeurl, fatherurl);
        // 获取绝对pc
        String pc = url_pcaddress(url);


        // 还要把绝对路径改为相对路径（../../法）；对比两个pc,fatherpc？？？
        // 。。。url的对比不影响相对路径，但按照pc否则有可能对应不上，例如：../be/java
        String[] fatherpcsplit = fatherpc.split("\\\\");
        String[] pcsplit = pc.split("\\\\"); //这里的\\\\要特别注意。。。split（\）要写成split（\\\\）
        int commoni = 0; //先找最大公共相等层级
        int leastl = Math.min(fatherpcsplit.length, pcsplit.length);//../../..//www.oracle.com???
        //System.out.println("leastl___"+leastl);
        for (int i = 0; i < leastl; i++) {
            commoni = i - 1; //最后一个相等的层级。。。防止一次也不触发break
            if (!pcsplit[i].equals(fatherpcsplit[i])) {
                break;
            }
        }
        //System.out.println("commoni___"+commoni);
        // father中剩余的项要向上返回，本级则不需要
        StringBuilder sb = new StringBuilder();
        for (int i = 0, remian = (fatherpcsplit.length - 2) - commoni; i < remian; i++) { //fatherpcsplit.l-2为默认层级
            sb.append("../");
        }
        // pc中剩余的项全部加上
        for (int i = commoni + 1, l = pcsplit.length; i < l; i++) {
            sb.append(pcsplit[i] + "/");
        }
        sb.deleteCharAt(sb.length() - 1);// 去除最后一个多加的/

        String relativepcurl = sb.toString();
        return relativepcurl;
    }


    // 获取网页的实际路径
    public static String url_relativeactual(String relativeurl, String originurl) {
        String actualurl = "";

        // 特例
        //String[] specialstr=new String[]{"","about:blank","tel:18006330738","javascript:sso_sign_out();"};
        // 启动程序类，直接不动
        if(!url_ifhttptype(relativeurl)) return relativeurl;
        // 空
        if (relativeurl.equals("")) return originurl;


        // 处理开头总的来说分为三种：http://(//), /（借助origin）, 书签直接作用于当前页（不以父目录为准）,以父目录为准的相对路径（借助origin）
        int testi = relativeurl.indexOf("//");
        if (testi != -1) { //已经完备。。。 也有可能已经有http了
            if (testi == 0)
                actualurl = "https://" + relativeurl.substring(testi + 2); // 特别注意纠正//img03.en25.com/i/elqCfg.min.js，为http
            else
                actualurl = relativeurl;
        } else if (relativeurl.charAt(0) == '/') { //如：/sites/default/files/images/ut-crest-on.png
            //先找出服务器的根目录。。。。。例子著名法，容易想象化，类似于做应用题打草稿
            String rooturl = null;
            int index = originurl.indexOf("/", originurl.indexOf("//") + 2);
            if (index != -1) { // 如：https://www.cs.utexas.edu/faq/graduate/， 需要裁剪成：https://www.cs.utexas.edu
                rooturl = originurl.substring(0, index);
            } else { // 如：https://www.baidu.com 不需要裁剪
                rooturl = originurl;
            }
            actualurl = rooturl + relativeurl;
        } else if (relativeurl.charAt(0) == '#') { //书签，以当前页为准：#skip2content-->https://docs.oracle.com/javase/8/docs/index.html#skip2content
            actualurl = originurl + relativeurl;
        } else { // 不完备的相对路径：default/files/images/ut-crest-on.png; ../files/images/ut-crest-on.png

            String thisdir = originurl;
            //去除http://
            thisdir = thisdir.substring(thisdir.indexOf("//") + 2);
            if (!thisdir.contains("/")) { // 独体，如：www.baidu.com本身就是当前目录
            } else { //非独体，
                testi=thisdir.lastIndexOf("/");
                if(thisdir.indexOf(".",testi)!=-1)
                    // 如：docs.oracle.com/javase/8/docs/index.html--->当前目录为：docs.oracle.com/javase/8/docs
                    thisdir = thisdir.substring(0, thisdir.lastIndexOf("/"));
                else { // 如：docs.oracle.com/javase/8/docs--->当前目录为：docs.oracle.com/javase/8/docs
                }
            }

            ArrayList<String> dirsplitlist = new ArrayList<String>(); //dirsplitlist.remove(0); //默认已经有一个了
            String[] dirsplit = thisdir.split("/");
            Collections.addAll(dirsplitlist, dirsplit);
            String[] relativesplit = relativeurl.split("/"); // 从当前目录出发，逐级分析相对路径

            for (int i = 0, l = relativesplit.length; i < l; i++) {
                if (relativesplit[i].equals(".")) { // .不计入
                } else if (relativesplit[i].equals("..")) { // ..向上，即删除最后一个
                    if (dirsplitlist.size() > 0)
                        dirsplitlist.remove(dirsplitlist.size() - 1);
                } else { //其他级直接加入
                    dirsplitlist.add(relativesplit[i]);
                }
            }
            actualurl = originurl.substring(0,originurl.indexOf("://")+3); // "https://"; // 逐级累加
            for (int i = 0, l = dirsplitlist.size(); i < l; i++) {
                actualurl += dirsplitlist.get(i) + "/";
            }
            actualurl = actualurl.substring(0, actualurl.length() - 1); //去除多出来的/
        }

        // 处理末尾，暂时不需要。。。以防添加了index.html影响传输啊

        return actualurl;
    }


    public static String pcaddress_url(String pc) { //不过貌似！！！！！无法完全对应，只能把头部借给relative用用
        // "C:\websitedata\docs.oracle.com\javase\8\docs\index.html"
        int start = pc.indexOf("."); // split中必须要\\.，其他则不需要了
        start = pc.lastIndexOf("\\", start); // .号前的\
        String url = "https://" + pc.substring(start + 1).replaceAll("\\\\", "/");
        url = url.replaceAll("？", "?");

        return url;
    }


    static JSONObject queryGUID=null;
    static String dataaddress = "D:/websitedata";
    static String querypcaddress=dataaddress+ "/？？？.txt"; // static 等中定义的必须要按顺序，否则无法识别
    static String pathquery_guid(String querystr,String fold) throws IOException {
        if (queryGUID == null) {
            File file = new File(querypcaddress);
            if (!file.exists()) { // 不存在则直接创建
                queryGUID = new JSONObject();
                // 并创造文件
                path_file(querypcaddress);
            }else
                queryGUID = txtpath_json(querypcaddress);
        }
        String guid = null;
        if (queryGUID.isNull(fold)) queryGUID.put(fold, new JSONObject());
        JSONObject thisfold=queryGUID.getJSONObject(fold);
        if (thisfold.isNull(querystr)) {
            UUID uuid = UUID.randomUUID();
            guid = uuid.toString();
            thisfold.put(querystr,guid);
        } else {
            guid = thisfold.getString(querystr);
        }
        return guid;
    }

    // 获取网址在电脑中的对应位置
    public static String url_pcaddress(String urladdress) throws IOException { // 连带创建文件夹
        //找出实际网址便于对应     注明例子法！！！

        // 首先要进行编码替换，把%E5%8D%A0%E4%BD%8D_%E9%80%8F%E6%98%8E.png 替换为：占位_透明.png，后者才是html的指定实际路径
        //String encodeStr = URLEncoder.encode("中国","utf-8"); 这个貌似有问题，用 encode方法吧！！！！
        //String decodeStr = URLDecoder.decode("img005.h5yo.cn/Upload/s1551086411000011/images/%E5%8D%A0%E4%BD%8D_%E9%80%8F%E6%98%8E.png", "utf-8");
        String decodeStr = URLDecoder.decode(urladdress, "utf-8");


        // 预处理
        // 符号替换空格处理
        String saveto = decodeStr.replaceAll(" ", "");


        // 处理开头。。。最后再添加表头，以防后面搜索时与前面矛盾
        // 去除http替换成指定目录
        if (saveto.indexOf("://") != -1) { // 如：https://docs.oracle.com--->docs.oracle.com
            saveto = saveto.substring(saveto.indexOf("://") + 3);
        }


        // 去除末尾
        // ? 处理 。。。 还是不要轻易处理为好，一面影响原本的层级，再次打开时relativepc<>relativeurl
        int queryi = saveto.indexOf("?");
        if (queryi != -1) {
            // ?????? 还要进一步处理？的情形，防止长度过长，用GUID码对应到对应名称！！！
            String querystr=saveto.substring(queryi+1);
            String fold=saveto.substring(0,queryi);
            //取出原本的后缀，没有则用html
            int lastslashi=saveto.lastIndexOf("/");
            int suffixi=saveto.indexOf(".",lastslashi);
            String suffix=".html";
            System.out.println(suffixi);
            if((suffixi!=-1)&&(suffixi<queryi)) suffix=saveto.substring(suffixi,queryi);

            //修改格式
            saveto=saveto.substring(0,queryi)+"？"+pathquery_guid(querystr,fold)+suffix;

           // saveto = saveto.replaceAll("\\?", "？"); //用中文符号替代之！以防目录无法实现
         /*   //竟然还会这种情况！！！导致转义根本无法使用 https://hdhhome.ucsd.edu/hdh-covid-19-faq.html?_ga=2.202928274.26508703.1604338815
            // 去除其.html,以防错误
            if (saveto.substring(testi - 5, testi).equals(".html")) {
                saveto = saveto.substring(0, testi - 5) + "_html" + saveto.substring(testi);
            }
            saveto = saveto.replaceAll("\\?", "/"); //+、*、|、\、?等在正则表达式中有特殊含义，需要转义
        */
        }


        // #书签处理。。。pc中可以使用，但httpurl中不允许
        if (saveto.lastIndexOf("#") != -1) saveto = saveto.substring(0, saveto.lastIndexOf("#"));
        // 添加后缀文件名，如：index.html   目标：// docs.oracle.com/javase/tutorial/reallybigindex.html
        int testi = saveto.lastIndexOf("/");
        if (testi == -1) { // docs.oracle.com
            saveto += "/index.html";
        } else if (saveto.length() - 1 == testi) { //最后一个 docs.oracle.com/javase/tutorial/
            saveto += "index.html";
        } else {
            testi = saveto.indexOf(".", testi);
            if (testi == -1) { // docs.oracle.com/javase/tutorial/reallybigindex
                saveto += "/index.html";
            } else { // docs.oracle.com/javase/tutorial/reallybigindex.html   不需要处理
            }
        }


        saveto = dataaddress + "/" + saveto;// 最后添加前面，否则会误把websitedata\www.oracle.com中的.com当作后缀

        // 替换，/与\，一般url本来不含空格。。。。其他#？？？？对应符号？？？
        saveto = saveto.replaceAll("%20", " ");
        saveto = saveto.replaceAll("/", "\\\\"); //此处的替换要非常小心


        //先把文件夹创建
        path_directory(saveto.substring(0, saveto.lastIndexOf("\\")));
        // 不能在此处创建新文件，以防覆盖旧文件
        return saveto;
    }

    // 根据电脑中的位置，反推网址
    public static String path_url(String path) {
        int start = path.indexOf(".");
        start = path.lastIndexOf("\\", start) + 1;
        String url = "https://" + path.substring(start).replaceAll("\\\\", "/");
        return url;
    }


    public static String strs_str(String[] str,int l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; i++) {
            sb.append(str[i] + "\r\n");
        }
        return sb.toString();
    }

    public static String strs_str(String[] str) {
        int l = str.length;
        return strs_str(str,l);
    }





















































































































    // 导入jar包时，selenium-server-standalone-3.9.1.jar要放到selenium-java-2.40.0.jar前面才能发挥作用，
    // 否则出错（org.openqa.selenium.firefox.NotConnectedException: Unable to connect to host 127.0.0.1，搞得好像版本过低）。
    // 额，真没想到啊！！！！
    public static WebDriver driver = null;

    public static void drivernew_firefox(){/*
        FirefoxOptions fo = new FirefoxOptions();
        fo.addPreference("browser.download.dir", "C:\\websitedata");
        //fo.addPreference();
        fo.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/rss+xml");//default
        fo.addPreference("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12A365 microMessenger/5.4.1 NetType/WIFI");
        */
        /*System.setProperty("webdriver.gecko.driver", seleniumpahth);
        driver = new FirefoxDriver(fo);
        */
    }

    public static void drivernew_chrome() throws InterruptedException {

        /*
        ChromeOptions option=new ChromeOptions();
        // option.addArguments("--headless");
        option.setExperimentalOption("excludeSwitches",new String[]{"enable-automation"});
        option.setExperimentalOption("useAutomationExtension", "False");
        // 设置user-agent
        option.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3756.400 QQBrowser/10.5.4039.400");
        */
        // 设置路径
        String seleniumpahth = System.getProperty("user.dir") + "\\src\\browser\\chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", seleniumpahth);
        driver = new ChromeDriver();//option
        finaldo();
    }

    public static void finaldo(){ // 不能反复调用，否则会出现“Hook previously registered”错误
        Runtime.getRuntime().addShutdownHook(finaldoing);
    }
    private static Thread finaldoing = new Thread(new Runnable() {
        @Override
        public void run() {
            // TODO 自动生成的方法存根
            try {
                if (queryGUID != null) {
                    System.out.println("更新query对应文件");
                    json_txtpath(queryGUID, querypcaddress);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(driver!=null) {
                System.out.println("关闭浏览器"); // 运行结束统一关闭浏览器避免重复打开
                driver.quit(); //close方法一般关闭一个tab，quit方法才是我们认为的完全关闭浏览器方法
            }

            if(errormessage!=null) {
                System.out.println("发送报告邮件");
                try {
                    str_email("program break", errormessage);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }

    });

    public static String[] url_string_browser(String[] urladdress) throws InterruptedException {
        if (driver == null) drivernew_chrome();

        String[] res = new String[urladdress.length];
        for (int i = 0, l = urladdress.length; i < l; i++) {
            res[i] = url_string_browser(urladdress[i], driver, 30);
        }

        return res;
    }

    // 总程序
    public static String url_string_browser(String urladdress) throws InterruptedException, IOException {
        // 特例
        // 启动程序类，直接跳过
        if(!url_ifhttptype(urladdress)) return null;

        if (driver == null) drivernew_chrome();

        System.out.println("chrome___" + urladdress);

        // 此处限制browser的执行时长
        String res = url_string_browser(urladdress, driver, 600); //总时长可以多等一会儿
        //driver.close();
        //driver.quit(); quit貌似与close矛盾？？？
        return res;
    }

    // 限定运行时间了
    public static String url_string_browser(String urladdress, org.openqa.selenium.WebDriver driver, int seconds) {
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws InterruptedException {
                // 这里写方法，必须实现抽象方法call，真正的任务在这里执行，这里的返回值类型为String，可以为任意类型
                return url_string_browser(urladdress, driver);
            }
        };

        return execute(call, seconds);

        // close thread pool
        //future.cancel(true);
        //
        // exec.shutdown(); // 无论哪种情况，最终都要关闭程序，否则一直无法退出来
    }

    // 限定连接时间
    public static String url_string_browser(String urladdress, org.openqa.selenium.WebDriver driver) throws InterruptedException {
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() {
                // 这里写方法，必须实现抽象方法call，真正的任务在这里执行，这里的返回值类型为String，可以为任意类型
                driver.get(urladdress);
                return "connected within time";
            }
        };
        String res = execute(call, 300); // 连接时间要严格限制，以防出错。。。
        // 有些网址的连接时间就需要花很长时间，非常麻烦
        System.out.println("Result___"+res);
        if (res.equals("")) {
            return ""; // 说明都没有连接上
        } else {
        }

        // 用js测试全部加载结束
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        int leng = 0, newleng = 0;
        for (boolean already = false; !already; ) { // false就一直执行
            if (((String) js.executeScript("return document.readyState;")).equals("loading")) {
                Thread.sleep(200);
                System.out.println((String) js.executeScript("return document.readyState;"));
            } else {
                js.executeScript("window.scrollTo(0,document.body.scrollHeight);");
                Thread.sleep(200); // 对于scrollTo这种执行语句，需要有个时间差让命令得到执行；测量语句则不需要了
                long x = (long) js.executeScript("return document.body.scrollHeight;");
                System.out.println(x);
                newleng = (int) x;
                if (newleng != leng) {
                    leng = newleng;
                    already = false;
                } else {
                    already = true;
                }
            }
        }

        // 此处的错误语句（其他程序中，不适用与此没有该元素），一旦错误，则影响后面语句的执行了！！！！！
        //js.executeScript("window.open(document.querySelectorAll(\"a.downloadNowButton\")[0].href);");

        //return
        String xxx = (String) js.executeScript("return document.documentElement.outerHTML;");

        return xxx;
    }

    // Executors.newCachedThreadPool()貌似不管用，还是得用这个，分开来写
    public static String execute(Callable<String> call, int seconds) {
        String res = "";//=execute(call,10);
        // 这个终止不了，还是要解决firefox的问题，否则后面浏览器无法加载了
        ExecutorService exec = Executors.newCachedThreadPool();
        Future<String> future = exec.submit(call);
        try {
            res = future.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            System.out.println("====================task time out===============" + seconds);
            //ex.printStackTrace();
            //exec.shutdown(); // 无论哪种情况，最终都要关闭程序，否则一直无法退出来
            //return " ";
            // 强制关闭浏览器,并重新生成，避免后面阻塞//无法关闭，无法操作，乃至于一旦close/quit碰到就陷入阻塞
            Runtime.getRuntime().exec("tskill firefox");
            Runtime.getRuntime().exec("tskill firefoxdriver");
            drivernew_chrome();
            //Thread.sleep(10*1000); //稍微等一下，防止来不及启动
        } catch (InterruptedException e) {
            //e.printStackTrace();
            //return " ";
        } catch (ExecutionException e) {
            //e.printStackTrace();
            //return " ";
        } finally {
            exec.shutdownNow();
            return res;
        }
    }











































































































    public static void path_changepath(String pathold,String pathnew){
        File file=new File(pathold);
        file.renameTo(new File(pathnew));
    }
    public static String path_changepathsubtle(String patholdfull, String pathnew){
        String path=patholdfull.substring(0,patholdfull.lastIndexOf("\\"));
        String pathnewfull=path+"\\"+pathnew;
        File file=new File(patholdfull);
        file.renameTo(new File(pathnewfull));
        return pathnewfull;
    }

    public static String[] path_sonpaths(String path, String suffix) throws IOException {
        List<String> sonpaths = new ArrayList<>();
        File dir = new File(path);
        File[] files = dir.listFiles(); // 该文件目录下文件(不包括下下级的文件)全部放入数组
        if (files == null)
            return null;
        else {
            int l = files.length;
            for (int i = 0; i < l; i++) {
                String filename = files[i].getCanonicalPath();//.getAbsolutePath()
                int index = filename.lastIndexOf('.');
                if (suffix == "")  //全部抓取
                    sonpaths.add(filename);
                else if (index != -1) {
                    if (filename.substring(index+1).equals(suffix)) {
                        sonpaths.add(filename);
                    }
                }
            }
        }

        return sonpaths.toArray(new String[sonpaths.size()]);
    }

    public static String txtpath_str(String path) throws IOException {
        //"GBK" 才能不乱码
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String content;
        while ((content = br.readLine()) != null) { // 这里已经默认把换行去除，要注意！！！！
            sb.append(content + "\r\n");
        }
        br.close();
        return sb.toString();

    }

    public static JSONObject txtpath_json(String path) throws IOException {
        //"GBK" 才能不乱码
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "GBK"));
        StringBuilder sb = new StringBuilder();
        String content;
        while ((content = br.readLine()) != null) {
            sb.append(content);
        }
        br.close();
        JSONObject res = new JSONObject(sb.toString());
        return res;

    }

    // 存储编码要与读取编码一致，才能读出
    public static void json_txtpath(Object res, String txtpath) throws Exception {
        /*PrintStream out = new PrintStream(new FileOutputStream(txtpath,"GBK"));
        out.println(res.toString(2));*/
        //写入文件（指定编码）。。。
        if(res==null) return;
        File file = new File(txtpath);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8")));
        //默认为false直接覆盖，true才能追加
        //out.write(res.toString(2).replace("\n", "\r\n")); // json用\n换行，txt用\r\n换行，需要对应转化
        out.write(json_str(res)); // 原装的res.toString()方法，把double的0，也当作了int的0，无法还原
        out.flush();
        out.close();
    }



    public static void str_txtpath(String str, String txtpath) throws UnsupportedEncodingException {
        // 创造文件夹
        /*if(txtpath.indexOf("?")!=-1) { // 这是http查询，无法解析
        }else*/
        try {
            path_directory(txtpath.substring(0, txtpath.lastIndexOf("\\")));
            // 创造文件
            File file = new File(txtpath);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"))); //false表示覆盖，true为追加，默认false
            out.write(str); // json用\n换行，txt用\r\n换行，需要对应转化
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("filenotfound_____" + txtpath);
        }
    }

    // 按照路径创造文件夹
    public static void path_directory(String dirpath) {
        File file = new File(dirpath);
        if (!file.exists()) file.mkdirs();
    }

    public static void path_file(String filepath) throws IOException {
        System.out.println(filepath);
        String dirpath=null;
        if(filepath.contains("\\"))
            dirpath=filepath.substring(0,filepath.lastIndexOf("\\"));
        else if(filepath.contains("/"))
            dirpath=filepath.substring(0,filepath.lastIndexOf("/")); //这种格式也可以接受
        path_directory(dirpath);
        File file=new File(filepath);
        file.createNewFile();
    }
























































































































/*    static Thread report = new Thread(new Runnable() {
        @Override
        public void run() {
            // TODO 自动生成的方法存根
            System.out.println("yes");
            try {
                str_email("program break", "break");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    });*/

    public static void str_email(String title, String content) throws MessagingException {
        String MAIL_SERVER_HOST = "mail.ustc.edu.cn";
        String USER = "seaspace@mail.ustc.edu.cn";
        String PASSWORD = "226354";
        String MAIL_FROM = "seaspace@mail.ustc.edu.cn";
        String MAIL_TO = "the_and_sea@163.com";
        String MAIL_CC = null;
        String MAIL_BCC = null;

        Properties prop = new Properties();
        prop.setProperty("mail.debug", "true");
        prop.setProperty("mail.host", MAIL_SERVER_HOST);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        // 1、创建session
        Session session = Session.getInstance(prop);
        Transport ts = null;
        // 2、通过session得到transport对象
        ts = session.getTransport();
        // 3、连上邮件服务器
        ts.connect(MAIL_SERVER_HOST, USER, PASSWORD);
        // 4、创建邮件
        MimeMessage message = new MimeMessage(session);
        // 邮件消息头
        message.setFrom(new InternetAddress(MAIL_FROM)); // 邮件的发件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(MAIL_TO)); // 邮件的收件人
        //message.setRecipient(Message.RecipientType.CC, new InternetAddress(MAIL_CC)); // 邮件的抄送人
        //message.setRecipient(Message.RecipientType.BCC, new InternetAddress(MAIL_BCC)); // 邮件的密送人
        message.setSubject(title); // 邮件的标题
        // 邮件消息体
        message.setText(content);
        // 5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }





































































































    // convert jsonarray * jsonobject ---> jsonobject * jsonarray
    public static JSONObject jsonarray_jsonobj(JSONArray jsonarray) throws Exception {
        // no need to qualify, do it directly
        JSONObject jsonobj=new JSONObject();
        for (int i=0,l=jsonarray.length();i<l;i++ ) {
            JSONObject son=jsonarray.getJSONObject(i);
            JSONObject xx=new JSONObject();
            for (String key : son.keySet()) {
                // 补足属性数组property array
                JSONArray propertyarray=null;
                if (jsonobj.isNull(key)) {
                    propertyarray=new JSONArray();
                    jsonobj.put(key, propertyarray);
                }else
                    propertyarray=jsonobj.getJSONArray(key);
                // 补足index位置
                int length=propertyarray.length();
                if (i<=length) { // ==时恰好不用添加直接put
                    for (int j=0;j<(length-i);j++ )
                        propertyarray.put((Collection<?>) null);
                    propertyarray.put(son.get(key));
                } else{
                    throw new Exception("wrong szie !");
                }
            }
        }
        return jsonobj;
    }

    // convert jsonobject * jsonarray ---> jsonarray * jsonobject
    public static JSONArray jsonobj_jsonarray(JSONObject jsonobj){
        // qualification ! every property is an jsonarray of the same size
        int number=-1;
        boolean samesize=true;
        for (String key : jsonobj.keySet() ) {
            JSONArray son=jsonobj.getJSONArray(key);
            if(son !=null) {
                int thislength = son.length();
                if (number == -1)
                    number=thislength;
                else if(number !=thislength){
                    samesize=false;
                    break;
                }
            }
        }
        if (samesize) {// convert
            JSONArray jsonarray=new JSONArray();
            for (int i=0;i<number;i++ ) {
                JSONObject son=new JSONObject(); jsonarray.put(son);
                for (String key: jsonobj.keySet()) {
                    son.put(key,jsonobj.getJSONArray(key).get(i));
                }
            }
            return jsonarray;
        } else{
            return null; // not constant size
        }
    }




    // 序列化存储，conf用于写入配置文件
    private static HashMap<String,String[]> variable_conf=null;
    private static HashMap<String,ArrayList<Object>> type_variable=null;
    private static JSONObject type_json=null;
    public static JSONObject variable_json_graph(Object variable,HashMap<String,String[]> aroundjsonobj,HashMap<String,String[]> confjsonobj) throws Exception {
        type_variable =new HashMap<>();
        type_json=new JSONObject();
        variable_index=new HashMap<>();
        // 初始化list

        String[] basicclasstype=new String[]{"java.lang.String","java.util.HashMap","java.util.ArrayList"}; // 默认一定要的class类型
        for (String key:basicclasstype ) {
            type_variable.put(key,new ArrayList<>());
            //type_json.put(key,new JSONArray());
        }

        // 先遍历，再逐个处理、完善
        variable_conf=confjsonobj;
        for (String key:variable_conf.keySet() ) {
            type_variable.put(key,new ArrayList<>());
            //type_json.put(key,new JSONArray());
        }
        long milli=System.currentTimeMillis();
        goaround(variable);
        System.out.println((System.currentTimeMillis()-milli)+"  ms");
        milli=System.currentTimeMillis();

        variable_conf=confjsonobj;
        // type_variable ---> type_json
        JSONObject json=new JSONObject();
        json.put("content",type_json);
        for (String key:type_variable.keySet()){
            //System.out.println(key);
            ArrayList<Object> type_variablelist=type_variable.get(key);
            JSONArray type_jsonlist=new JSONArray();type_json.put(key,type_jsonlist);
            for (int i=0,l=type_variablelist.size();i<l ;i++ ) {
                type_jsonlist.put(variable_json_graph(type_variablelist.get(i)));
            }
        }

        json.put("main",typevariable_index(variable));

        System.out.println((System.currentTimeMillis()-milli)+"  ms");
        milli=System.currentTimeMillis();


        // variable_conf
        JSONObject conf=new JSONObject();json.put("variable_conf",conf);
        for (String key:variable_conf.keySet() ) {
            JSONArray array=new JSONArray(); conf.put(key,array);
            String[] properties=variable_conf.get(key);
            for (int i=0,l=properties.length;i<l ;i++ )
                array.put(properties[i]);
        }
        return json;
    }


    // !!!!!!!!!!!!!!!!!!!!!！！！！！！！！！！！！！！！！！！！
    //！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！ 这个方法好
    // 将遍历--完善（导致递归太长），引用； 分开(遍历过程可以额外选择属性组合！！！)  遍历HashMap(type_variable)，完善
    //public static ArrayList<Object> goque=null;
    public static Object[] goque=null;
    public static int go_s=0;
    public static int go_end=0;
    public static void goaround(Object variable) throws Exception {
        /*goque=new ArrayList<>();
        goque.add(variable);
        while (goque.size()>0) {
            variable_typevariable(goque.get(0));
            goque.remove(0); 这个ArrayList的remove确实慢太多了
        }*/
        goque=new Object[Integer.MAX_VALUE/1000]; go_s=0; go_end=0;
        goque_add(variable);
        do{
            variable_typevariable(goque[go_end]);
            go_end++;
            if (go_end==goque.length)
                go_end=0;
            if (go_end==go_s)
                break;
        }while (go_s!=go_end);
    }
    public static void goque_add(Object variable) throws Exception {
        goque[go_s]=variable;
        go_s++;
        if (go_s==goque.length)  //循环
            go_s=0;
        if(go_s==go_end)
            throw new Exception("Queue overflow!");
    }
    public static HashMap<Object,Integer> variable_index=null;
    public static JSONObject typevariable_index(Object variable) throws Exception {
        String typename=variable.getClass().getTypeName();
        int index = -1;

        switch(typename){
            case "byte":
            case "short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "boolean":
            case "char":{
                throw new Exception("wrong serach type!    "+typename);
            }
            case "java.util.HashMap":
            case "java.util.ArrayList":
            case "java.lang.String":{  // 必须要进行寻找，equal已经重写了
                if (type_variable.containsKey(typename)) { // 有键
                    ArrayList<Object> variablelist = type_variable.get(typename);
                    for (int i = 0, l = variablelist.size(); i < l; i++) {
                        if (variablelist.get(i) == variable) {
                            index = i;
                            break;
                        }
                    }
                } else // 没有键
                    type_variable.put(typename, new ArrayList<>());
            } break; // 不需要加break，如果没有找到，则也需要继续寻找法

            default:{
                if(variable_index.containsKey(variable)){ //HashMap确实能大大提高速度！！！
                    index=variable_index.get(variable);
                } else if (type_variable.containsKey(typename)) { // 有键
                    ArrayList<Object> variablelist = type_variable.get(typename);
                    for (int i = 0, l = variablelist.size(); i < l; i++)
                        if (variablelist.get(i) == variable) {
                            index = i;
                            variable_index.put(variable, index);// 找到了还要添加上去
                            break;
                        }
                } else { // 没有键
                    //System.out.println("add type__"+typename);
                    type_variable.put(typename, new ArrayList<>());
                }
            }
        }


        JSONObject link=new JSONObject();
        link.put("class_type",typename);
        link.put("obj_index",index);
        return link;
    }

    public static void variable_typevariable(Object variable) throws Exception {
        if (variable == null) {
            return ;
        }
        String typename = variable.getClass().getTypeName();
        switch (typename) {
            // 基本类型: byte,short,int,long,float,double,boolean,char...直接写入即可
            // 递归后，variable中的double等会自动转化为java.lang.Double等
            // 注意这限制了使用条件，即Double不可使用，要额外封装成node,neuron等！！！
            case "byte":
            case "short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "boolean":
            case "char":
            case "java.lang.String": {
            }
            break;
            // 引用类型要归入type_variables, type_json数组
            default: {
                // 先判断有无
                int index=typevariable_index(variable).getInt("obj_index");
                if (index == -1) {// 无-- add type_variable; 展开son添加到queu中
                    // add type_variable
                    ArrayList<Object> variablelist = type_variable.get(typename);
                    variablelist.add(variable);

                    // 展开son添加到queu中
                    if (typename.contains("[]")) {
                        for (int i = 0, l = Array.getLength(variable); i < l; i++)
                            goque_add(Array.get(variable, i));
                    } else if (typename.equals("java.util.ArrayList")) { // Arraylist
                        ArrayList list = (ArrayList) variable;
                        for (int i = 0, l = list.size(); i < l; i++)
                            goque_add(list.get(i));
                    }
                    // 非数组
                    else {
                        switch (typename) {
                            case "java.util.HashMap": {
                                HashMap<String, Object> map = (HashMap<String, Object>) variable;
                                for (String key : map.keySet())
                                    goque_add(map.get(key));
                            }
                            break;
                            default: {// 其他普遍的引用类型，这些引用类型则需要相应的conf配置
                                String[] properties = variable_conf.get(typename);
                                // 【环】已经存在的obj，如何直接连接过去，分配额外的index，直接检索
                                Class c = Class.forName(typename);
                                for (String key : properties)
                                    goque_add(c.getField(key).get(variable));
                            }
                        }
                    }
                }
            }
        }
    }

    public static Object variable_linkorvalue(Object variable) throws Exception {
        if (variable == null) {
            return null;
        }
        String typename=variable.getClass().getTypeName();
        switch (typename) {
            case "byte":
            case "short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "boolean":
            case "char": {
                return variable;
            } //break;
            default:{
                return typevariable_index(variable);
            }
        }
    }
    // with ring,overlap ---> graph ---> class, not class ---> special
    // 以addproperty的形式进行，属性的添加分步进行，避免过多的递归调用（6万！！！）
    // 递归还是要少用啊，很容易溢出
    public static Object variable_json_graph(Object variable) throws Exception { // 仅返回index，具体内容存储于type_json
        if (variable == null) {
            return null;
        }
        Object res=null;
        String typename = variable.getClass().getTypeName();
        switch (typename) {
            // 基本类型: byte,short,int,long,float,double,boolean,char...直接写入即可
            // 递归后，variable中的double等会自动转化为java.lang.Double等
            // 注意这限制了使用条件，即Double不可使用，要额外封装成node,neuron等！！！
            case "byte":
            case "short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "boolean":
            case "char":
            case "java.lang.String":{
                res = variable;
            }
            break;
            // 引用类型要归入type_variables, type_json数组
            default: {
                // 先判断有无---不需要判断有无，直接顺序全部遍历
                //JSONArray jsonlist = type_json.getJSONArray(typename); // 同步add，以防未注册的重复

                // create json
                if (typename.contains("[]") || (typename.equals("java.util.ArrayList"))) { // 数组
                    // 需要公用分段seg
                    JSONObject jsonObj = new JSONObject(); res=jsonObj; //jsonlist.put(jsonObj); // 直接先添加上去，确保顺序
                    Object[] sonlist = null;


                    if (typename.contains("[]")) {
                        jsonObj.put("type", "[]");
                        sonlist = new Object[Array.getLength(variable)];
                        for (int i = 0, l = sonlist.length; i < l; i++) {
                            sonlist[i] = variable_linkorvalue(Array.get(variable, i));
                        }
                    } else if (typename.equals("java.util.ArrayList")) { // Arraylist
                        jsonObj.put("type", "java.util.ArrayList");
                        ArrayList list = (ArrayList) variable;
                        sonlist = new Object[list.size()];
                        for (int i = 0, l = sonlist.length; i < l; i++) {
                            sonlist[i] = variable_linkorvalue(list.get(i));
                        }
                    }
                    jsonObj.put("L", sonlist.length);

                    // 分个段（合并同类），避免冗杂
                    JSONArray jsonarray = new JSONArray(); jsonObj.put("seg", jsonarray);
                    JSONObject son_seg = null;
                    JSONArray son_index = null;
                    String pretype = "";
                    String nowtype = "";
                    boolean ifclass = false;
                    for (int i = 0, l = sonlist.length; i < l; i++) {
                        //System.out.println(sonlist[i]);
                        if (sonlist[i] == null) { // type讨论
                            nowtype = "null";
                            pretype = "null";
                            jsonarray.put((Map<?, ?>) null); // ！！！JSONarray.put(null),只能{}或[],无法真的null！！！
                            continue;
                        } else {
                            nowtype = sonlist[i].getClass().getTypeName();
                            if (nowtype.equals("org.json.JSONObject")) {
                                nowtype = ((JSONObject) sonlist[i]).getString("class_type");
                                ifclass = true;
                            } else {
                                nowtype = sonlist[i].getClass().getTypeName();
                            }
                        }

                        if (nowtype.equals(pretype)) { // 相同--追加
                            if (ifclass) { // list , first one
                                son_index.put(((JSONObject) sonlist[i]).getInt("obj_index"));
                            } else {
                                son_index.put(sonlist[i]);
                            }
                        } else { // 不同--new
                            //System.out.print(pretype+"___"+nowtype+"__");
                            son_seg = new JSONObject();
                            jsonarray.put(son_seg);
                            son_seg.put("class_type", nowtype);
                            if (ifclass) { // list , first one
                                son_index = new JSONArray();
                                son_seg.put("obj_index", son_index);
                                son_index.put(((JSONObject) sonlist[i]).getInt("obj_index"));
                            } else {
                                son_index = new JSONArray();
                                son_seg.put("value", son_index);
                                son_index.put(sonlist[i]);
                            }
                            pretype = nowtype;
                        }
                    }

                }
                // 非数组
                else {
                    switch (typename) {
                        // 特殊的非[]的class,需要特别列出: String, HashMap
                        case "java.lang.String": {
                            res=variable;
                            //jsonlist.put(variable);
                        }
                        break;
                        case "java.util.HashMap": {
                            JSONObject jsonObj = new JSONObject(); res=jsonObj;
                            //jsonlist.put(jsonObj);
                            HashMap<String, Object> map = (HashMap<String, Object>) variable;
                            for (String key : map.keySet()) {
                                jsonObj.put(key, variable_linkorvalue(map.get(key)));
                            }
                        }
                        break;
                        default: {// 其他普遍的引用类型，这些引用类型则需要相应的conf配置
                            String[] properties = variable_conf.get(typename);
                            // 【环】已经存在的obj，如何直接连接过去，分配额外的index，直接检索
                            JSONObject jsonObj = new JSONObject(); res=jsonObj;
                            //jsonlist.put(obj);
                            Class c = Class.forName(typename);
                            for (String key : properties) {
                                jsonObj.put(key, variable_linkorvalue(c.getField(key).get(variable)));
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
    // json ---> variable
    // with ring,overlap ---> graph ---> class, not class ---> special
    public static Object json_variable_graph(JSONObject json) throws Exception { // 仅返回index，具体内容存储于type_json
        Object main_json=json.get("main");
        // 涉及class链接
        if(main_json.getClass().getTypeName().equals("org.json.JSONObject")){
            // conf转化为variable_conf
            JSONObject conf=json.getJSONObject("variable_conf");
            variable_conf=new HashMap<>();
            for (String key:conf.keySet()){
                JSONArray jsonstrarr=conf.getJSONArray(key);
                String[] obj_conf=new String[jsonstrarr.length()];
                for (int i=0,l=jsonstrarr.length();i<l;i++ ) {
                    obj_conf[i]=jsonstrarr.getString(i);
                }
                variable_conf.put(key,obj_conf);
            }

            // 解析content（type_json）转化为type_variable
            type_json=json.getJSONObject("content"); // 本身就是type_json
            type_variable=new HashMap<>();
            for (String key:type_json.keySet()){
                JSONArray variable_jsonlist=type_json.getJSONArray(key);
                ArrayList<Object> variable_list=new ArrayList<>();
                type_variable.put(key,variable_list);
                for (int i=0,l=variable_jsonlist.length();i<l ;i++ ) {
                    variable_list.add(json_variable_graph_create(variable_jsonlist.get(i),key));
                }
            }

            // main
            JSONObject main_obj=(JSONObject) main_json;
            Object main_variable=json_variable_graph_create(main_obj,""); // main要么引用，要么基本数据

            // linking & value !!! main , type_variable
            json_variable_graph_value(main_json,main_variable);
            for (String key:type_json.keySet()){
                JSONArray variable_jsonlist=type_json.getJSONArray(key);
                ArrayList<Object> varialbe_list=type_variable.get(key);
                for (int i=0,l=variable_jsonlist.length();i<l ;i++ )
                    json_variable_graph_value(variable_jsonlist.get(i),varialbe_list.get(i));
            }

            return main_variable;
        }else{
            Object main_variable=json_variable_graph_value(main_json,"");
            //json_variable_graph_value(); 非引用类型，不需要new
            return main_variable;
        }
    }
    // 不需要递归到下级的class，即不需要递归了
    public static Object json_variable_graph_create(Object json,String objtype) throws Exception { // 仅返回index，具体内容存储于type_json
        String typename=json.getClass().getTypeName();
        Object res=null;
        switch (typename){
            case "org.json.JSONObject":{
                // 此处的单个unit，不会涉及引用（obj_index, class_type）;只有引用才会有typename，但也不需要处理了
                JSONObject jsonobj=(JSONObject)json;
                if((!jsonobj.isNull("class_type"))&&(!jsonobj.isNull("obj_index"))) { // 引用
                    // 引用。。。有typename，但无用了...直接返回null，防止重复new
                    // obj_index multi型
                    // obj_index sigle型
                    res=null;
                } else if(objtype.contains("[]")) { // 数组!jsonobj.isNull("L")
                    // 按照objtype创建instance
                    int dimension=(objtype.length()-objtype.indexOf("[]"))/2;
                    String basic=objtype.substring(0,objtype.indexOf("[]"));
                    Class bc=null;
                    boolean ifbasic=false;
                    switch (basic) {
                        case "byte":bc=byte.class;ifbasic=true;break;
                        case "short":bc=short.class;ifbasic=true;break;
                        case "int":bc=int.class;ifbasic=true;break;
                        case "long":bc=long.class;ifbasic=true;break;
                        case "float":bc=float.class;ifbasic=true;break;
                        case "double":bc=double.class;ifbasic=true;break;
                        case "boolean":bc=boolean.class;ifbasic=true;break;
                        case "char":bc=char.class;ifbasic=true;break;
                        default: bc=Class.forName(basic);break;
                    }
                    // create
                    int[] arraylength=new int[dimension]; arraylength[0]=jsonobj.getInt("L"); // 这个length有问题！！不是那个啊
                    Object obj=Array.newInstance(bc,arraylength); res=obj;
                } /*else if(jsonobj.length()==0) { // {}==null
                    // null 不需要做任何
                }*/else { // 非引用，普通对象object， property
                    Class c=Class.forName(objtype);
                    Object obj=c.newInstance();
                    res=obj;
                }
            }break;
            default:{
                throw new Exception(typename);
            }
        }
        return res;
    }
    // 全部遍历了一遍接通了一下，也不需要递归了
    public static Object json_variable_graph_value(Object json,Object variable) throws Exception {
        String typename=json.getClass().getTypeName();
        Object res=null;
        if (variable==null) {
            return null;
        }
        switch (typename){
            case "org.json.JSONObject":{
                // 此处的单个unit，不会涉及引用（obj_index, class_type）;只有引用才会有typename，但也不需要处理了
                JSONObject jsonobj=(JSONObject)json;
                if(!jsonobj.isNull("L")) { // 数组型
                    // 逐个seg，赋值到总表中
                    if (jsonobj.isNull("seg")) {
                        System.out.println(json);
                    }
                    ArrayList list=null; // arraylist !!!
                    if(jsonobj.getString("type").equals("java.util.ArrayList")) {
                        list = (ArrayList) variable;
                    }

                    JSONArray seg=jsonobj.getJSONArray("seg");
                    for (int i=0,sumindex=0,l=seg.length();i<l; i++) {
                        JSONObject seg_unit=seg.getJSONObject(i);
                        if (!seg_unit.isNull("obj_index")) { // index引用型
                            String class_type=seg_unit.getString("class_type");
                            JSONArray obj_index_arr=seg_unit.getJSONArray("obj_index");
                            for (int j=0,jl=obj_index_arr.length();j<jl ;j++,sumindex++ ) {
                                if (list != null)
                                    list.add(type_variable.get(class_type).get(obj_index_arr.getInt(j)));
                                else
                                    Array.set(variable, sumindex, type_variable.get(class_type).get(obj_index_arr.getInt(j))); // type_variable中的引用
                            }
                        } else if(!seg_unit.isNull("value")){ // 数值型
                            JSONArray value_arr=seg_unit.getJSONArray("value");
                            for (int j=0,jl=value_arr.length();j<jl ;j++,sumindex++ ) {
                                if (list != null)
                                    list.add(value_arr.get(j));
                                else
                                    Array.set(variable, sumindex, value_arr.get(j)); // 直接数组中的数值即可
                            }
                        }else if(seg_unit.length()==0) { // null型
                            if (list != null)
                                list.add(null);
                            else
                                Array.set(variable, sumindex, null);
                            sumindex++;
                        } else{
                            throw new Exception("type wrong!");
                        }
                    }
                } else { // 普通对象型object， property
                    // 引用。。。返回该值...递归截至了...variable=null空缺也行了
                    Class c=variable.getClass();
                    // property赋值
                    String objtype=variable.getClass().getTypeName();
                    String[] properties=variable_conf.get(objtype);
                    /*System.out.println(objtype);
                    System.out.println(jsonobj);*/
                    //Set<String> properties=jsonobj.keySet(); // 直接用jsonobj，以防某些属性为null，导致没有这个属性
                    for (String key:properties){
                        if (jsonobj.isNull(key)) {// 最好还是用强制null，以防new的初始值部位null！！
                            c.getField(key).set(variable,null);
                        }else if(jsonobj.get(key).getClass().getTypeName().equals("org.json.JSONObject")){ // 引用型的property
                            JSONObject property_link=jsonobj.getJSONObject(key);
                            String class_type=property_link.getString("class_type");
                            int obj_index=property_link.getInt("obj_index");
                            c.getField(key).set(variable,type_variable.get(class_type).get(obj_index));
                        }else{ // 数值型的property，直接用
                            c.getField(key).set(variable,jsonobj.get(key));
                        }
                    }
                }
                res=variable;
            }break;
            default:{
                throw new Exception(typename);
            }
        }
        return res;
    }

    // no ring, no overlap ---> tree ---> 不需要重复性判断
    public static Object variable_json_tree(Object variable,HashMap<String,String[]> confjsonobj) throws Exception {
        variable_conf=confjsonobj;

        JSONObject json=new JSONObject();
        json.put("variable_conf",variable_json_tree(variable_conf));
        json.put("main",variable_json_tree(variable));
        return json;
    }
    // with ring ---> graph ---> class, not class ---> special
    // 还是要把类型type添加上，不然不便于恢复read，要么要有现成的frame
    public static Object variable_json_tree(Object variable) throws Exception { // 仅返回index，具体内容存储于type_json
        if (variable == null) {
            return null;
        }
        Object res = null;
        String typename = variable.getClass().getTypeName();
        switch (typename) {
            // 基本类型+特殊class：byte,short,int,long,float,double,boolean,char...直接写入即可
            // String
            // 递归后，variable中的double等会自动转化为java.lang.Double等
            // 注意这限制了使用条件，即Double不可使用，要额外封装成node,neuron等！！！
            case "byte":
            case "short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "boolean":
            case "char":
            case "java.lang.String": {
                res = variable;
            }
            break;
            // 一维数组[]
            case "byte[]":
            case "short[]":
            case "int[]":
            case "long[]":
            case "float[]":
            case "double[]":
            case "boolean[]":
            case "char[]": {
                JSONArray jsonArray = new JSONArray(variable);
                res = jsonArray;
            }
            break;
            // 特殊默认的class类型: HashMap
            case "java.util.HashMap": {
                JSONObject obj = new JSONObject();
                res = obj;
                HashMap<String, Object> map = (HashMap<String, Object>) variable;
                for (String key : map.keySet()) {
                    Object son = variable_json_tree(map.get(key));
                    obj.put(key, son);
                }
            }
            break;
            // 一般class
            default: {
                // 数组
                if (typename.contains("[]")) {
                    JSONArray jsonarray = new JSONArray(); res = jsonarray;
                    Object[] array = (Object[]) variable;
                    for (int i = 0, l = array.length; i < l; i++) {
                        Object son = variable_json_tree(array[i]);
                        jsonarray.put(son);
                    }
                }
                // 非数组
                else {
                    // 一般引用类型，这些引用类型则需要相应的conf配置
                    String[] obj_conf = variable_conf.get(typename);
                    JSONObject obj = new JSONObject(); res=obj;
                    Class c = Class.forName(typename);
                    if(obj_conf.length>1) {
                        for (String key : obj_conf) {
                            Object son = variable_json_tree(c.getField(key).get(variable));
                            obj.put(key, son);
                        }
                    }else{ // 仅一个属性，直接省去不写
                        res = variable_json_tree(c.getField(obj_conf[0]).get(variable));
                    }
                }
            }
        }
        return res;
    }


     //已经有了完备的方案graph, tree
    // 序列化存储variable参数
    public static Object variable_json(Object variable) throws Exception {
        if (variable==null) {
            //return null;
            throw new Exception("Wrong!");
        };
        Object res=null;
        String typename=variable.getClass().getTypeName();
        switch (typename){
            case "java.util.HashMap":{
                JSONObject obj=new JSONObject(); res=obj;
                HashMap<String,Object> map=(HashMap<String,Object>) variable;
                for (String key: map.keySet() ) {
                    Object son=variable_json(map.get(key));
                    obj.put(key,son);
                }
            }
            break;
            case "int[]"://但一维基本类型的数组，也要直接运算，否则不可用为Object[]
            case "long[]":
            case "double[]":
            case "boolean[]":{
                JSONArray jsonArray=new JSONArray(variable); res=jsonArray;
            }
            break;/*
            case "neuralnetwork$node":{
                neuralnetwork.node Node= (neuralnetwork.node) variable;
                res=Node.value;
            }
            break;case "neuralnetwork$neuron":{
                neuralnetwork.neuron Neuron= (neuralnetwork.neuron) variable;
                res=Neuron.value;
            }
            break;*/
            default:{
                if(typename.contains("[]")) { // 数组。。。子元素有可能是HashMap,需要额外逐个判断啊
                    JSONArray jsonarray = new JSONArray();
                    res = jsonarray;
                    Object[] array = (Object[]) variable;
                    for (int i = 0, l = array.length; i < l; i++) {
                        Object son = variable_json(array[i]);
                        jsonarray.put(son);
                    }
                }else{ // 其他引用类型，包括: String等
                    res=variable;
                }
            }
            break;
        }
        return res;
    }

    // 反序列化恢复记录
    // convert JSON to standard variable____network reading function;
    public static Object json_variable(Object json) throws ClassNotFoundException {
        if(json==null) return null;
        Object res=null;
        switch(json.getClass().getTypeName()){ // switch 中 equal可以省略
            // 不会自动追认类型，结果还是显示为java.lang.Object[]，除非一开始就用new double[]
            case "org.json.JSONObject":{
                JSONObject jsonobj=(JSONObject) json;
                HashMap<String,Object> map=new HashMap<String,Object>(); res=map;
                for(String key:jsonobj.keySet()){
                    Object son=json_variable(jsonobj.get(key));
                    map.put(key,son);
                }
            }
            break;
            case "org.json.JSONArray":{
                JSONArray jsonarr=(JSONArray) json;
                Object[] array=new Object[jsonarr.length()]; res=array;
                for (int i=0,l=jsonarr.length();i<l ;i++ ) {
                    Object son = json_variable(jsonarr.get(i));
                    array[i] = son;
                }
                // 总结类型(consistant type)，合并数组
                String sontype=null;
                boolean ifconstant=true;
                for (int i=0,l=array.length;i<l ;i++ ) // Object虽然泛，但不能忽略，否则会出错 {Object[],double[],double[]}
                    //&&(!array[i].getClass().getTypeName().contains("java.lang.Object"))
                    if((array[i]!=null)){ // 这个不算
                        if (sontype==null) {
                            sontype=array[i].getClass().getTypeName();
                        }else if(!sontype.equals(array[i].getClass().getTypeName())) {
                            ifconstant=false;
                            break;
                        }
                    }
                if(ifconstant && sontype!=null){ // 合并
                    switch(sontype){
                        case "java.lang.Double":{
                            double[] newarray=new double[array.length]; res=newarray;
                            for (int i=0,l=array.length;i<l ;i++ )
                                newarray[i]= (Double) array[i];
                        }
                        break;
                        case "java.lang.Integer": {
                            int[] newarray = new int[array.length]; res=newarray;
                            for (int i = 0, l = array.length; i < l; i++)
                                newarray[i] = (Integer) array[i];
                        }
                        break;
                        case "java.lang.Long": {
                            long[] newarray = new long[array.length]; res=newarray;
                            for (int i = 0, l = array.length; i < l; i++)
                                newarray[i] = (Long) array[i];
                        }
                        break;
                        /*
                        case "java.lang.String":{
                            String[] newarray = new String[array.length]; res=newarray;
                            for (int i = 0, l = array.length; i < l; i++)
                                newarray[i] = (String) array[i];
                        }
                        break;*/
                        default:{ // all other 引用类型
                            // upgrade the dimension !!!
                            //Class sonc=Class.forName(sontype);
                            //System.out.println(sontype);
                            int bracketindex=sontype.indexOf("[]");
                            String basictype=null;
                            if(bracketindex!=-1) basictype=sontype.substring(0,bracketindex);
                            else basictype=sontype;
                            Class c=null;
                            switch (basictype) {
                                case "int":
                                    c = int.class;
                                    break;
                                case "double":
                                    c = double.class;
                                    break;
                                case "boolean":
                                    c = boolean.class;
                                    break;
                                default:
                                    c=Class.forName(basictype);/*
                                case "java.lang.String":
                                    c = Class.forName("java.lang.String");*/
                                    break;
                            }
                            int dimension=(sontype.length()-basictype.length())/2+1; // 括号数量
                            int[] paramter=new int[dimension]; paramter[0]=array.length; // 其余先写作0，类似于[1][0][0]
                            Object[] newarray=(Object[]) Array.newInstance(c,paramter); res=newarray;
                            //System.out.println(newarray.getClass().getTypeName()+"____"+array.getClass().getTypeName());
                            for (int i=0,l=array.length;i<l ;i++ ) {
                                //System.out.println(array[i].getClass().getTypeName());
                                newarray[i] = array[i];
                            }
                        }
                    }
                } else if(!ifconstant){
                    // Object 比较特殊，有些总结起来了，有些没有总结起来，结果都能算作Object ！！！！
                    // 直接统计least阶数
                    int least=1000;
                    int dimension=0;
                    int bracketindex=0;
                    boolean ifallnull=true;
                    String typename=null;
                    for (int i=0,l=array.length;i<l;i++ ) {
                        if(array[i]!=null) {
                            typename = array[i].getClass().getTypeName();
                            // 基本类型不能用作 Object[] objx=new double[10]; 要高一维
                            typename=typename.replace("double[]","").replace("int[]","").replace("boolean[]","");
                            bracketindex = typename.indexOf("[]");
                            if (bracketindex != -1) {
                                dimension = (typename.length() - bracketindex) / 2;
                                if (least > dimension)
                                    least = dimension;
                            } else{ // 没有[]，则作为least=0；
                                least=0;
                                break;
                            }
                            ifallnull=false;
                        }
                    }
                    if(ifallnull)
                        least=0; // all null ---> least dimension=0;
                    //System.out.println("least is___"+(least+1));
                    Class c=Class.forName("java.lang.Object");
                    int[] paramter=new int[least+1]; paramter[0]=array.length;
                    Object[] newarray=(Object[])Array.newInstance(c,paramter); res=newarray;
                    //System.out.println(newarray.getClass().getTypeName()+"____"+array.getClass().getTypeName());
                    for (int i=0,l=array.length;i<l ;i++ ) {
                        //System.out.println(array[i].getClass().getTypeName());
                        newarray[i] = array[i];
                    }
                }
            }
            break;
            default:{
                res=json;
            }
        }
        return res;
    }

    public static String json_str(Object json) throws Exception {
        return json_sb(json).toString();
    }
    // 修正已有的json_str的缺陷，把无.的double错误转化为了int
    // long也需要补0修正！！！ long/int/short(json中没有)区分
    // float(json中没有)/double 区分
    // json中的其他类型，如bigdecimal，bigint等
    public static StringBuilder json_sb(Object json) throws Exception {
        StringBuilder sb=new StringBuilder();
        String typename=json.getClass().getTypeName();
        switch (typename){
            case "org.json.JSONObject":{
                JSONObject obj=(JSONObject) json;
                if (obj.length()==0) {
                    sb.append("{}");
                }else {
                    sb.append("{"); // 如果一个都没有也有可能会把"{"给删除了
                    for (String key : obj.keySet())
                        sb.append("\"" + key + "\":" + json_str(obj.get(key)) + ",");
                    sb.delete(sb.length() - 1, sb.length()); // remove the last ,
                    sb.append("}");
                }
            }
            break;
            case "org.json.JSONArray":{
                JSONArray array=(JSONArray) json;
                if(array.length()==0){ // 如果一个都没有也有可能会把"["给删除了
                    sb.append("[]");
                }else {
                    sb.append("[");/*
                    System.out.println(array.length());
                    System.out.println(array.toString());*/
                    for (int i = 0, l = array.length(); i < l; i++) {
                        if (array.isNull(i)) // null会导致get无法使用！！！
                            sb.append("null,");
                        else
                            sb.append(json_str(array.get(i)) + ",");
                    }
                    sb.delete(sb.length() - 1, sb.length()); // remove the last ,
                    sb.append("]");
                }
            }
            break;
            case "java.lang.Double":{
                Double d=(Double) json;
                String res=d.toString();
                if (!res.contains(".")) {
                    res+=".0";
                }
                sb.append(res);
            }
            break;
            case "java.lang.Integer":{
                Integer I=(Integer) json;
                String res=I.toString();
                sb.append(res);
            }
            break;
            case "java.lang.String":{
                String str=(String) json;
                sb.append("\""+str+"\"");
            }
            break;
            case "java.lang.Boolean":{
                Boolean B=(Boolean) json;
                String res=B.toString();
                sb.append(res);
            }
            break;
            default:{
                throw new Exception(typename+"____type not completed !");
            }
        }
        return sb;
    }





































































































    // Array operation !!!
    // convert N-dimensional array to 2-dimensional array
    public static Object numNarr_num2arr_All(Object num) throws Exception {
        String typename=num.getClass().getTypeName();
        Object res=null;
        int bracketindex=typename.indexOf("[]");
        int dimension=0;
        String basictype=null;
        if(bracketindex!=-1){
            dimension=(typename.length()-bracketindex)/2;
            basictype=typename.substring(0,bracketindex);
        }else{
            dimension=0;
            basictype=typename;
        }

        Class c=name_class(basictype);
        // 符号要求的2D数组
        Object[] num2arr=null;
        switch (dimension) {
            case 0: {
            /*num2arr= (Object[])Array.newInstance(c,new int[]{1,1});
             single=Array.newInstance(c,1);
            num2arr[0]*/
                throw new Exception("wrong size"); // 直接用new int[]{}就行了
            }
            case 1: {
                num2arr = (Object[]) Array.newInstance(c, new int[]{1, 0}); // 1+1=2 阶
                num2arr[0] =num;
            } break;
            case 2:{
                num2arr=(Object[])num;
            } break;
            default:{
                // >=3
                ArrayList<Object> num2list=new ArrayList<>(); // 1+1=2 阶
                // 没必要用stack栈实现。。。等以后优化再说
                Object[][] origin=(Object[][]) num; // 2+1=3 阶 ---> 1+1=2 阶
                for (int i=0,il=origin.length;i<il ;i++ ) {
                    Object[] son=(Object[])numNarr_num2arr_All(origin[i]);
                    //先把每一个son都转化为1+1=2 阶， 直接合并到其中
                    for (int j = 0, jl = son.length; j < jl; j++) {
                        num2list.add(son[j]);
                    }
                }
                num2arr=(Object[]) Array.newInstance(c,new int[]{num2list.size(),0}); // 1+1=2
                for (int i=0,il=num2list.size();i<il ;i++ )
                    num2arr[i]=num2list.get(i);
            } break;
        }


        return num2arr;
    }

    public static Class name_class(String typename) throws ClassNotFoundException {
        Class c=null;
        switch (typename){
            case "int": c=int.class;
                break;
            case "double": c=double.class;
                break;
            case "boolean": c=boolean.class;
                break;
            default:{
                c=Class.forName(typename);
            }
        }
        return c;
    }






































































































































    public static Robot robot=null;
    public static void press() throws InterruptedException, AWTException {
        if (robot==null) {
            robot=new Robot();
        }

        while(1==1){//j'j'j'j'j'j'
            TimeUnit.MILLISECONDS.sleep(300); //  延迟等待
            robot.keyPress(KeyEvent.VK_J);
        }

    }












































































































































 /*
    // JAVE for video & music ... but only convert format!
    // JAVE http://www.sauronsoftware.it/projects/jave/manual.php
    // JAVE2 https://github.com/a-schild/jave2        https://github.com/a-schild/jave2/wiki/Examples
    // combine!!! https://stackoverflow.com/questions/19812480/java-xuggler-combine-an-mp3-audio-file-and-a-mp4-movie
    // https://blog.csdn.net/dnc8371/article/details/106707867
    public static void videovoice_video(String videopath, String voicepath, String output) throws EncoderException {
        String inputVideoFilePath = "in.mp4";
        String inputAudioFilePath = "in.mp3";
        String outputVideoFilePath = "out.mp4";

        IMediaWriter mWriter = ToolFactory.makeWriter(outputVideoFilePath);

        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        // check files are readable
        if (containerVideo.open(inputVideoFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputVideoFilePath);
        if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputAudioFilePath);

        // read video file and create stream
        IStreamCoder coderVideo = containerVideo.getStream(0).getStreamCoder();
        if (coderVideo.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");
        IPacket packetvideo = IPacket.make();
        int width = coderVideo.getWidth();
        int height = coderVideo.getHeight();

        // read audio file and create stream
        IStreamCoder coderAudio = containerAudio.getStream(0).getStreamCoder();
        if (coderAudio.open(null, null) < 0)
            throw new RuntimeException("Cant open audio coder");
        IPacket packetaudio = IPacket.make();

        mWriter.addAudioStream(1, 0, coderAudio.getChannels(), coderAudio.getSampleRate());
        mWriter.addVideoStream(0, 0, width, height);

        while (containerVideo.readNextPacket(packetvideo) >= 0) {

            containerAudio.readNextPacket(packetaudio);

            // video packet
            IVideoPicture picture = IVideoPicture.make(coderVideo.getPixelType(), width, height);
            coderVideo.decodeVideo(picture, packetvideo, 0);
            if (picture.isComplete())
                mWriter.encodeVideo(0, picture);

            // audio packet
            IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            coderAudio.decodeAudio(samples, packetaudio, 0);
            if (samples.isComplete())
                mWriter.encodeAudio(1, samples);

        }


//<added_code> This is Eli Sokal's code tweaked to work with gilad s' code

        IAudioSamples samples;
        do {
            samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            containerAudio.readNextPacket(packetaudio);
            coderAudio.decodeAudio(samples, packetaudio, 0);
            mWriter.encodeAudio(1, samples);
        }while (samples.isComplete() );

//</added_code>


        coderAudio.close();
        coderVideo.close();
        containerAudio.close();
        containerVideo.close();
        mWriter.close();
    }

*/


























































    public static void path_open(String path) throws IOException {
        Runtime.getRuntime().exec("rundll32 url.dll FileProtocolHandler "  +  path);
    }


    public static String[] strs_printarray(JSONObject body) {
        ArrayList<String> stringarray = new ArrayList<>();
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                if (!contentarray.getJSONObject(i).isNull("select"))
                    if (contentarray.getJSONObject(i).getBoolean("select")) {
                        stringarray.add(contentarray.getJSONObject(i).getString("content"));
                    }
            }
        } else if (body.get("content") instanceof String) {
            if (!body.isNull("select"))
                if (body.getBoolean("select")) {
                    stringarray.add(body.getString("content"));
                }
        }
        return stringarray.toArray(new String[stringarray.size()]);
    }


    public static String strs_print(JSONObject body) {
        StringBuilder sb = new StringBuilder();
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                if (!contentarray.getJSONObject(i).isNull("select"))
                    if (contentarray.getJSONObject(i).getBoolean("select")) {
                        sb.append(contentarray.getJSONObject(i).getString("content") + " ");
                    }
            }
        } else if (body.get("content") instanceof String) {
            if (!body.isNull("select"))
                if (body.getBoolean("select")) {
                    sb.append(body.getString("content") + " ");
                }
        }
        String result = sb.toString();
        return result;
    }

    public static JSONObject strs_selectrecovery(JSONObject body) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                contentarray.getJSONObject(i).put("select", true);
            }
        } else if (body.get("content") instanceof String) {
            body.put("select", true);
        }
        return body;
    }

    public static JSONObject str_select(JSONObject body, String[] property, Object[] value) {
        if ((!body.isNull("select")) && (!body.getBoolean("select"))) // 存在and未选中
            return body;

        boolean select = true;
        for (int i = 0, l = property.length; i < l; i++) {
            if ((body.isNull(property[i])) || (!body.get(property[i]).equals(value[i]))) { // 不存在or不相等
                select = false;
                break;
            }
        }
        // 直接put就可以覆盖原来的值
        body.put("select", select); // 此处可见，没有添加select的和选中的，都会进行这一步false
        /*if(body.get("select")==null){
            body.put("select",select);
        }else {
            body.get("select")= select;
        }*/

        return body;
    }


    public static JSONObject strs_select(JSONObject body, String[] property, Object[] value) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                str_select(contentarray.getJSONObject(i), property, value);
            }
        } else if (body.get("content") instanceof String) {
            str_select(body, property, value);
        }

        return body;
    }


    public static JSONObject strs_sum(JSONObject body, String propertyname) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0, add = 0; i < l; i++) {
                if (!contentarray.getJSONObject(i).isNull(propertyname))
                    if (contentarray.getJSONObject(i).get(propertyname) instanceof Integer) {
                        add += contentarray.getJSONObject(i).getInt(propertyname);
                    }
                contentarray.getJSONObject(i).put("sum", add);
            }
        } else if (body.get("content") instanceof String) {
            int add = 0;
            if (!body.isNull(propertyname))
                if (body.get(propertyname) instanceof Integer) {
                    add += body.getInt(propertyname);
                }
            body.put("sum", add);
        }

        return body;
    }


    public static JSONObject str_changeto(JSONObject unit, String propertyname, Object[] oringvalue, String newpropertyname, Object[] newvalue) {
        if ((!unit.isNull("select")) && (!unit.getBoolean("select"))) // 不存在or未选中
            return unit;

        if (unit.isNull(propertyname)) return unit;
        Object obj = unit.get(propertyname);
        for (int i = 0, l = oringvalue.length; i < l; i++) {
            if (obj.equals(oringvalue[i])) {
                unit.put(newpropertyname, newvalue[i]);
                return unit;
            }
        }

        return unit;
    }

    public static JSONObject str_changeto(JSONObject unit, String propertyname, Object[] oringvalue, Object[] newvalue) {
        return str_changeto(unit, propertyname, oringvalue, propertyname, newvalue);
    }

    public static JSONObject strs_changeto(JSONObject body, String propertyname, Object[] oringvalue, String newpropertyname, Object[] newvalue) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                str_changeto(contentarray.getJSONObject(i), propertyname, oringvalue, newpropertyname, newvalue);
            }
        } else if (body.get("content") instanceof String) {
            str_changeto(body, propertyname, oringvalue, newpropertyname, newvalue);
        }

        return body;
    }

    public static JSONObject strs_changeto(JSONObject body, String propertyname, Object[] oringvalue, Object[] newvalue) {
        return strs_changeto(body, propertyname, oringvalue, propertyname, newvalue);
    }


    public static JSONObject str_beginwith(JSONObject body, String[] target) {
        if ((!body.isNull("select")) && (!body.getBoolean("select"))) // 不存在or未选中
            return body;

        String str = body.getString("content");
        for (int i = 0, l = target.length; i < l; i++) {
            if (str.length() >= target[i].length())
                if (str.substring(0, target[i].length()).equals(target[i])) { //beginwith
                    body.put("beginwith", target[i]);
                    return body;
                }
        }

        String targetnull = null;
        body.put("beginwith", ""); // 如果是null 不能采用targetnull，否则null默认不执行put

        return body;
    }

    public static JSONObject strs_beginwith(JSONObject body, String[] target) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                str_beginwith(contentarray.getJSONObject(i), target);
            }
        } else if (body.get("content") instanceof String) {
            str_beginwith(body, target);
        }

        return body;
    }

    public static JSONObject str_replacemany(JSONObject body, String[] from, String[] to) {
        if ((!body.isNull("select")) && (!body.getBoolean("select"))) // 不存在or未选中
            return body;

        String str = body.getString("content");
        for (int i = 0, l = from.length; i < l; i++) {
            str = str.replaceAll(from[i], to[i]);
        }
        body.put("content", str); // 直接put就可以覆盖原来的值

        return body;
    }

    public static JSONObject strs_replacemany(JSONObject body, String[] from, String[] to) {
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                str_replacemany(contentarray.getJSONObject(i), from, to);
            }
        } else if (body.get("content") instanceof String) {
            str_replacemany(body, from, to);
        }

        return body;
    }




    // 按照pre，after寻找body中的组分，并用select标记之
    // 逐级寻找target序列
    static JSONObject[] JSONstack_select(String[][] target,JSONObject body) throws JSONException {
        ArrayList<JSONObject> outobj=new ArrayList<>();
        // 直接用栈写
        int tl=target.length;
        JSONObject[] stackobj=new JSONObject[10000];
        boolean[][] ifnowto=new boolean[10000][tl]; // 接下来验证哪个了（0始终有）

        int stackl=1; JSONObject thisobj;
        stackobj[0]=body;
        for (int i=0;i<tl ;i++ ) {ifnowto[0][i]=false;} ifnowto[0][0]=true;

        while(stackl>0) {
            thisobj = stackobj[stackl - 1];
            boolean[] thisifnowto=ifnowto[stackl-1];
            stackl--; // 出栈

            // 自身验证
            boolean[] thisifnext=new boolean[tl];
            for(int i=0;i<tl;i++){
                // 验证
                if(thisifnowto[i]){ // 上级
                    // 本级
                    boolean fit=true;
                    String[] propertyname=new String[]{"pre","after","content"};
                    for(int k=0,ktestl=target[i].length;k<ktestl;k++){
                        if(target[i][k]!=null) {
                            if (thisobj.isNull(propertyname[k])) {
                                fit=false;
                                break;
                            }else if(!thisobj.getString(propertyname[k]).equals(target[i][k])){
                                fit=false;
                                break;
                            }
                        }
                    }
/*
                    if (thisobj.isNull("pre")) fit=false;
                    else if(!thisobj.getString("pre").equals(target[i][0])) fit=false;
                    if (thisobj.isNull("after")) fit=false;
                    else if(!thisobj.getString("after").equals(target[i][1])) fit=false;
                    */

                    // 不完备则入栈，完备则输出
                    if(i+1<tl) {
                        thisifnext[i + 1] = fit; // 准备验证
                    } else if(fit){ //超出了且完备了
                        outobj.add(thisobj);
                    }
                }
            }

            // 展开 // content，有下级则入栈。。。不管如何都要展开，因为有0的true
            if(thisobj.get("content") instanceof JSONArray) {
                JSONArray contentarray = thisobj.getJSONArray("content");
                for (int i = 0, l = contentarray.length(); i < l; i++) {
                    stackobj[stackl] = contentarray.getJSONObject(i);
                    for (int j = 0; j < tl; j++) {
                        ifnowto[stackl][j] = thisifnext[j];
                    }
                    ifnowto[stackl][0] = true;
                    // 栈
                    stackl++;
                }
            }
        }

        return outobj.toArray(new JSONObject[outobj.size()]);
        /*
        // 先遍历; 每一个都有两种可能性
        JSONObject[] stackobj=new JSONObject[2000];
        byte[] nowto=new byte[2000];
        byte[] anothernowto=new byte[2000];

        int stackl=1; JSONObject thisobj;
        stackobj[0]=body; nowto[0]=-1; anothernowto[0]=-1;

        while(stackl>0){
            thisobj=stackobj[stackl-1]; //当前操作的对象
            // 自身验证
            int nowtonum=nowto[stackl-1]+1; boolean nowtoyes=false;
            if((!thisobj.isNull("pre"))&&(thisobj.getString("pre").equals(target[nowtonum][0]))) {
                if ((!thisobj.isNull("after")) && (thisobj.getString("after").equals(target[nowtonum][1]))) {
                    nowtoyes=true;
                }
            }
            int anothernowtonum=anothernowto[stackl-1]+1; boolean anotheryes=false;
            if(anothernowtonum!=nowtonum){
                if((!thisobj.isNull("pre"))&&(thisobj.getString("pre").equals(target[nowtonum][0]))) {
                    if ((!thisobj.isNull("after")) && (thisobj.getString("after").equals(target[nowtonum][1]))) {
                        anotheryes=true;
                    }
                }
            }

            // 依据自身结果，统一处理son
            int[] nextnum;
            if(nowtoyes){
                if(anotheryes){
                    nextnum=new int[]{nowtonum,anothernowtonum};
                }else{
                    nextnum=new int[]{nowtonum,-1};
                }
            }else{
                if(anotheryes){
                    nextnum=new int[]{-1,anothernowtonum};
                }else{
                    nextnum=new int[]{-1,-1};
                }
            }


            if(fold[stackl-1]) { //顶上的未展开，展开之
                fold[stackl-1]=false;
                // pre

                // content
                if (thisobj.get("content") instanceof JSONArray) { //逐个入栈
                    JSONArray contentarray = thisobj.getJSONArray("content");
                    for (int i = contentarray.length()-1; i>= 0; i--) {
                        stackobj[stackl]=contentarray.getJSONObject(i);
                        fold[stackl]=true;
                        stackl++;
                    }
                } else if (thisobj.get("content") instanceof String) {
                    sb.append(thisobj.getString("content"));
                }
            }else{ // 加上after，出栈
                if(!thisobj.isNull("after")) sb.append(thisobj.getString("after"));
                stackl--;
            }
        }
        */
    }


    static String JSONstack_str(JSONObject body){
        StringBuilder sb=new StringBuilder();
        // 直接用栈写
        JSONObject[] stackobj=new JSONObject[10000];
        boolean[] fold=new boolean[10000];

        int stackl=1; JSONObject thisobj;
        stackobj[0]=body; fold[0]=true;

        while(stackl>0){
            thisobj=stackobj[stackl-1];
            if(fold[stackl-1]) { //顶上的未展开，展开之
                fold[stackl-1]=false;
                // pre
                if(!thisobj.isNull("pre")) sb.append(thisobj.getString("pre"));
                // content
                if (thisobj.get("content") instanceof JSONArray) { //逐个入栈
                    JSONArray contentarray = thisobj.getJSONArray("content");
                    for (int i = contentarray.length()-1; i>= 0; i--) {
                        stackobj[stackl]=contentarray.getJSONObject(i);
                        fold[stackl]=true;
                        stackl++;
                    }
                } else if (thisobj.get("content") instanceof String) {
                    sb.append(thisobj.getString("content"));
                }
            }else{ // 加上after，出栈
                if(!thisobj.isNull("after")) sb.append(thisobj.getString("after"));
                stackl--;
            }
        }

        /*
        // pre
        if(!body.isNull("pre")) sb.append(body.getString("pre"));
        // content
        if (body.get("content") instanceof JSONArray) {
            JSONArray contentarray = body.getJSONArray("content");
            int l = contentarray.length();
            for (int i = 0; i < l; i++) {
                sb.append(JSONstack_str(contentarray.getJSONObject(i)));
            }
        } else if (body.get("content") instanceof String) {
            sb.append(body.getString("content"));
        }
        // after
        if(!body.isNull("after")) sb.append(body.getString("after"));
        */

        return sb.toString();
    }




    static int stack_condition(String[] stack,int stackl, String[][] condition){
        if(stackl==0) return 0; // 0表示任意情形，没有限制
        for(int i=1,conl=condition.length;i<conl;i++){ // 找到合适的情形, 情形0留出来给空栈
            boolean match=true;
            if(stackl<condition[i].length){ // 不够了
                match=false;
            } else { // 验证
                for (int stackj = stackl-1, condj = condition[i].length-1; stackj >= 0 && condj >= 0; stackj--, condj--) {
                    if(!stack[stackj].equals(condition[i][condj])){
                        match=false;
                        break;
                    }
                }
            }
            // 找到了，优先前面的
            if(match){
                return i;
            }
        }
        return -1; // 匹配不到！！！说明文档有错误
    }

    static boolean str_same_after(String content,int index,String[] notafter){
        if(notafter!=null)
            for(int i=0,notl=notafter.length;i<notl;i++) {
                int ilength = notafter[i].length();
                if (index + ilength < content.length()) { // 过长必然没用, index为i+1实际就是length
                    if (content.substring(index + 1, index + 1 + ilength).equals(notafter[i]))
                        return true;
                }
            }
        return false;
    }

    static boolean str_same_pre(String content,int index,String[] notpre){
        if(notpre!=null)
            for(int i=0,notl=notpre.length;i<notl;i++) {
                int ilength = notpre[i].length();
                if (ilength <= index) { // 过长必然没用, index为i+1实际就是length
                    if (content.substring(index - ilength, index).equals(notpre[i]))
                        return true;
                }
            }
        return false;
    }

    static HashMap<String,int[]> alreadyindex = new HashMap<String,int[]>();
    static int str_indexof(String content,int start,String target,String[] notpre,String[] notafter){
        if(alreadyindex.containsKey(target)){
            return alreadyindex.get(target)[start];
        }else{
            // 初始化
            int l=content.length();
            int[] indexarray=new int[l];
            for(int i=0;i<l;i++) indexarray[i]=-1;

            // 求解所有取值可能
            int index=0;
            while (index<l){
                index=content.indexOf(target,index);
                if(index!=-1){
                    boolean valid=true;
                    // notpre 检验
                    if(notpre!=null)
                        if(str_same_pre(content,index,notpre)) valid=false;
                    // notafter 检验
                    if(valid&&(notafter!=null))
                        if(str_same_after(content,index,notafter)) valid=false;

                    if(valid) indexarray[index]=index;

                    index++;
                }else break;
            }

            // 求解每个点的可能性
            for (int i=l-1,nearest=-1;i>=0 ;i-- ) {
                if (indexarray[i] != -1)
                    nearest = indexarray[i];
                else
                    indexarray[i]=nearest;
            }

            /*
            System.out.println(content);
            for(int i=0;i<l;i++) {
                System.out.print(indexarray[i]);
                if(indexarray[i]!=-1)
                    System.out.println("___"+content.substring(i,indexarray[i]+1));
            }*/

            // 保存
            alreadyindex.put(target,indexarray);
            return indexarray[start];
        }
    }

    static int[] str_minindex(String content,int start,String[] target,String[][] notpre,String[][] notafter){
        int min=-1, targetindex=-1;
        String[] thisnotpre=null; String[] thisnotafter=null;
        for(int i=0,tl=target.length;i<tl;i++){
            if(notpre==null) thisnotpre=null; else thisnotpre=notpre[i];
            if(notafter==null) thisnotafter=null; else thisnotafter=notafter[i];
            int test=str_indexof(content,start,target[i],thisnotpre,thisnotafter);
            if(test!=-1){
                if(min==-1) {
                    min = test;
                    targetindex=i;
                }
                else if(min>test) { //覆盖？？？优先前面的
                    min=test;
                    targetindex=i;
                }
            }
        }
        return new int[]{min,targetindex};
    }


    public static JSONObject str_stack_notpre(JSONObject unit,String[][] wait_con,String[][] wait,String[][] com_con,String[][][] notpre) throws Exception { //第一个放第一组，完整了便于验证结束
        return str_stack(unit,wait_con,wait,com_con,notpre,null);
    }

    public static JSONObject str_stack(JSONObject unit,String[][] wait_con,String[][] wait,String[][] com_con) throws Exception { //第一个放第一组，完整了便于验证结束
        return str_stack(unit,wait_con,wait,com_con,null,null);
    }

        // 先到先匹配原则，匹配括号（也可以单个啊）（屏蔽作用），ifand表示是否连同(注释则不连同，引号则连同)，并切割成string[]，
    // 此时linelevel的level用于存储type，1为连同，0为非连同，以备后用
    // unit必然是String的最基层
    public static JSONObject str_stack(JSONObject unit,String[][] wait_con,String[][] wait,String[][] com_con,String[][][] notpre,String[][][] notafter) throws Exception { //第一个放第一组，完整了便于验证结束
        // ifand 是否连同，0不连同（//，/**/），1连同（“”）
        // bracket: start, array, end;
        if ((!unit.isNull("select")) && (!unit.getBoolean("select"))) {// 存在and未选中
            //return unit;
        }

        // 清空已有的记录！！！
        alreadyindex.clear();


        // 验证合法性，避免空括号 ifand不全，仅能用一次

        JSONArray newcontent=new JSONArray();

        String content = unit.getString("content");
        int l = content.length(); int stackl=0;
        String[] stack=new String[1000];
        int[] stackstart=new int[1000];
        JSONObject[] stackobj=new JSONObject[1000];
        int index=0;
        String[][] thisnotpre=null; String[][] thisnotafter=null;
        while(index<l){
            int wai_con_num=stack_condition(stack,stackl,wait_con);
            String[] waitlist=wait[wai_con_num];
            // 依照情形，尝试找合适的下一个
            // 考虑notpre，notafter
            if(notpre==null) thisnotpre=null; else thisnotpre=notpre[wai_con_num];
            if(notafter==null) thisnotafter=null; else thisnotafter=notafter[wai_con_num];
            int[] test=str_minindex(content,index,waitlist,thisnotpre,thisnotafter);

            int min=test[0], thiswaitnum=test[1];
            if(min!=-1){ // 找到了，则入栈
                stack[stackl]=waitlist[thiswaitnum];
                stackstart[stackl]=min;
                stackl++;
                // 看看是否完备了
                int com_con_num=stack_condition(stack,stackl,com_con); // 一般完备性检验，长度为2
                //System.out.println(stackl+"___"+com_con_num);
                if((com_con_num!=-1)&&(com_con_num!=0)) { // 完备了则出栈(统一结算，加入到中)，0为空栈要排除
                    // 出栈
                    int coml = com_con[com_con_num].length; // stackl-1不必new和pre

                    if (coml > 1) { // 完备则直接添加after，content即可。。。相当于没有stackobj[stackl-1]了
                        //System.out.println(com_con_num+"__"+stackl);
                        stackobj[stackl - 2].put("after", stack[stackl - 1]);
                        if (stackobj[stackl - 2].isNull("content")) //已经有内容（已经有过出栈），则需要额外隔离
                            stackobj[stackl - 2].put("content", content.substring(index, min));
                        else {
                            JSONObject thisobj = new JSONObject();
                            thisobj.put("content", content.substring(index, min));
                            stackobj[stackl - 2].getJSONArray("content").put(thisobj);
                        }
                        // -2到-coml统一添加到-coml-1上面，超出则添加到father上面
                        JSONArray father = null;
                        if (stackl - coml - 1 < 0) {
                            father = newcontent;
                        } else {
                            if (stackobj[stackl - coml - 1].isNull("content"))
                                stackobj[stackl - coml - 1].put("content", new JSONArray());
                            father = stackobj[stackl - coml - 1].getJSONArray("content"); //已经有内容了
                        }
                        for (int i = coml; i >= 2; i--) father.put(stackobj[stackl - i]);
                        // 特别添加数目，便于以后寻找
                        if (coml - 1 > 1) {
                            stackobj[stackl - coml].put("total", coml - 1); //总共几段
                        }
                    }
                    else { // 总共也就一个single-complete，如；分号;, 按照不complete处理，并把pre合并到after上面，不用new thisobject
                        JSONObject beforeobj = new JSONObject();
                        beforeobj.put("content", content.substring(index, min));
                        beforeobj.put("after", stack[stackl - 1]);
                        if (stackl >= 2) {//才有上段
                            if (stackobj[stackl - 2].isNull("content"))
                                stackobj[stackl - 2].put("content", new JSONArray());
                            stackobj[stackl - 2].getJSONArray("content").put(beforeobj);
                        } else { //仅一个直接添加到newcontent中
                            newcontent.put(beforeobj);
                        }
                    }
                    stackl -= coml;
                } else{ //没有完备，则添加JSON
                    //上段结算。。。完备则直接content中，不完备需要宁外写到object中
                    JSONObject beforeobj=new JSONObject(); beforeobj.put("content",content.substring(index,min));
                    if(stackl>=2) {//才有上段
                        if(stackobj[stackl - 2].isNull("content"))
                            stackobj[stackl - 2].put("content",new JSONArray());
                        stackobj[stackl - 2].getJSONArray("content").put(beforeobj);
                    }else{ //仅一个直接添加到newcontent中
                        newcontent.put(beforeobj);
                    }
                    // 本段
                    stackobj[stackl-1]=new JSONObject(); stackobj[stackl-1].put("pre",stack[stackl-1]);
                }
                // 下移
                index=min+waitlist[thiswaitnum].length();
            } else { // 没找到
                if (stackl != 0) {// 栈还没空。。。说明有匹配错误
                    System.out.println(stackl);
                    System.out.println(strs_str(stack,stackl));
                    System.out.println("____" + content.substring(index-40));
                    throw new Exception("cannot find__" + wai_con_num + "___" + index);
                } else { // 栈也空了
                    break;
                }
            }
            //System.out.println(stackl);
        }
        //System.out.println("father____"+newcontent.toString(2));

        // 最后一段处理。。。视为完备了
        JSONObject thisobj;
        if(stackl==0){
            thisobj=new JSONObject();newcontent.put(thisobj);
            thisobj.put("content",content.substring(index));
        } else{ // ""引号到最后
            thisobj=stackobj[stackl-1];
            thisobj.put("pre",stack[stackl-1]);
            thisobj.put("after",stack[stackl-1]);
        }

        unit.put("content",newcontent);
        return unit;
    }

}
