import com.esotericsoftware.reflectasm.MethodAccess;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

public class ImageProcess {
    public static void main(String[] args) throws Exception {
 /*
        HashMap<String,Object> map=new HashMap<>(); map.put("type","Full");
        double[][][] outtest=new double[16][5][5];
        long now=System.currentTimeMillis();
        for (int n=0;n<100000 ;n++ ) {
        //    outtest=(double[][][])convolution(new double[6][14][14],new Object[]{new double[16][6][5][5],new double[16]},map);
            outtest=C3(outtest,new double[6][14][14],new double[16][6][5][5],new double[16],new int[16][6]);
        }
        System.out.println((System.currentTimeMillis()-now)+" ms");
        // origin: 23143 ms
        //now:    6820 ms
*/
        JSONObject network=LeNet_5_network("D:\\MNIST\\RBFparameter","D:\\MNIST\\network\\LeNet-5.txt");
        num2arr_training(network,"D:\\MNIST\\train-images.idx3-ubyte","D:\\MNIST\\train-labels.idx1-ubyte");
    }

    static double[][][][] test = new double[120][16][5][5];
    public static double[][][][] test(double[][][][] origin) {

        //Object testobj =
        //test = (double[][][][]) testobj;
        for (int out = 0; out < 120; out++) {
            for (int in = 0; in < 16; in++) {
                for (int x = 0; x < 5; x++) {
                    for (int y = 0; y < 5; y++) {
                        test[out][in][x][y] = (origin[out][in][x][y] + 1.45342) * 1.716;
                    }
                }
            }
        }

        return test;
    }







    public static JSONObject LeNet_5_network(String RBFparameterdir,String savingpath) throws Exception {
        File file=new File(savingpath);
        if(file.exists()){
            return Format.txtpath_json(savingpath);
        }

        JSONObject network=new JSONObject();
        JSONArray function=new JSONArray();
        network.put("function",function);
        function.put("convolution");
        function.put("sampling");
        function.put("convolution");
        function.put("sampling");
        function.put("convolution"); // Full connection
        function.put("convolution"); // Full connection
        function.put("outputlayer");

        JSONArray conf=new JSONArray();
        network.put("configuration",conf);
        // C1
        JSONObject layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        // S2
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        layerconf.put("step",2);
        // C3
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        layerconf.put("type",new JSONArray(new int[][]{ {0,1,2}, {1,2,3}, {2,3,4},
                {3,4,5}, {4,5,0}, {5,0,1},// 一定要按照顺序吗？？？
                {0,1,2,3}, {1,2,3,4}, {2,3,4,5},
                {3,4,5,0}, {4,5,0,1}, {5,0,1,2},
                {0,1,3,4}, {1,2,4,5}, {0,2,3,5},
                {0,1,2,3,4,5}}));
        // S4
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        layerconf.put("step",2);
        // C5, full connection
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        layerconf.put("type","Full");
        // F6, full connection
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("Activation","tanh");
        layerconf.put("type","Full");
        // output, RBF
        layerconf=new JSONObject();
        conf.put(layerconf);
        layerconf.put("type","RBF_Euclid");
        JSONArray RBFparameter=new JSONArray();
        layerconf.put("data",RBFparameter); // RBF 的数组data还需要补充！
        String RBFparameterpath=RBFparameterdir; // 读取 RBF的parameter即weight
        for (int n=0;n<10;n++ ) {
            int[][][] bmpcolor=path_colorarr(RBFparameterpath+"\\"+n+".bmp");
            int index=0;
            int[] RBFweight=new int[bmpcolor.length*bmpcolor[0].length];
            for (int i=0,il=bmpcolor.length;i<il;i++ )
                for (int j=0,jl=bmpcolor[i].length;j<jl ;j++ ) {
                    if(bmpcolor[i][j][0]==0){
                        RBFweight[index]=-1;
                    }else if(bmpcolor[i][j][0]==255){
                        RBFweight[index]=1;
                    }else{
                        throw new Exception("data format wrong !!!");
                    }
                    index++;
                }
            RBFparameter.put(new JSONArray(RBFweight));
        }
        //System.out.println(RBFparameter);



        // weight
        Object[][] weight=new Object[][]{
                {// C1
                        // weight
                        new double[6][5][5],
                        // bias
                        new double[6]
                }, {// S2
                // weight
                new double[6],
                // bias
                new double[6]
        }, {// C3
                // weight;  convolution: output * input * format(x * y)
                new double[][][][]{ //这种结构没有问题，但前面括号中不能有数组，否则后面不能加{}
                        new double[3][5][5], new double[3][5][5], new double[3][5][5],
                        new double[3][5][5], new double[3][5][5], new double[3][5][5],
                        new double[4][5][5], new double[4][5][5], new double[4][5][5],
                        new double[4][5][5], new double[4][5][5], new double[4][5][5],
                        new double[4][5][5], new double[4][5][5], new double[4][5][5],
                        new double[6][5][5]},
                // bias
                new double[16]
        }, {// S4
                // weight;
                new double[16],
                // bias
                new double[16]
        }, {// C5, Full connection  convolution: output * input
                // weight
                new double[120][16][5][5],
                // bias
                new double[120]
        }, {// C6, Full connection
                // weight
                new double[84][120][1][1],
                // bias
                new double[84]
        }, {// Output, RBF
                // RBF 没有weight
                /*new double[1][1],
                new double[1]*/
        }};
        for (int i=0,l=weight.length;i<l ;i++ ) { // weight value initialization
            //System.out
            if ((weight[i] != null)&&(weight[i].length>0))
                weight_initialize(weight[i][0], 2.4);
        }
        network.put("weight",new JSONArray(weight));



        Format.json_txtpath(network,savingpath);
        return network;
    }

    // iterate every elements , initialize the weight
    public static Object weight_initialize(Object sumelement,double sum) throws Exception {
        if (sumelement==null) {
            return null;
        }
        Object result=null;
        String type=sumelement.getClass().getTypeName();
        // 先判断是否找到了
        //System.out.println(type);
        if(type.equals("double[]")){
            double[] array=(double[]) sumelement;
            double unitdouble=sum / array.length;
            for (int i=0,l=array.length;i<l ;i++ ) {
                array[i] = (Math.random()*2-1)*unitdouble;
                //System.out.println(array[i]);
            }
            result=array;
        }else{
            int bracketindex=type.indexOf("[]");
            if (bracketindex!=-1) {// 可以拆分--->子元素中寻找
                Object[] array=(Object[]) sumelement; result=array;
                //Object[] resultarray=new Object[array.length]; result=resultarray;
                double unitdouble=sum / array.length;
                for (int i=0,l=array.length;i<l ;i++ )
                    weight_initialize(array[i],unitdouble);
            } else {// 不可拆分
                result=null;
                throw new Exception("wrong type !!!");
            }
        }
        return result;
    }




    // deep learning !!!!!
    //public static long testtime=0;
    public static MethodAccess thismethod=MethodAccess.get(ImageProcess.class);
    public static Object[] num2arr_training(JSONObject network,String imagepath,String labelpath) throws Exception {
        /*
        // read the network
        JSONArray layers=network.getJSONArray("layers");
        JSONObject thislayer=null;
        JSONArray thisweight=null;
        JSONArray weightline=null;
        Method[] layermethod=new Method[layers.length()];
        Object[][] layerweight=new Object[layers.length()][];
        Object[] sumconf=null;
        for (int m=0,ml=layers.length();m<ml ;m++ ) {
            thislayer=layers.getJSONObject(m);
            // read Function name
            layermethod[m]=Class.forName("ImageProcess").getDeclaredMethod(thislayer.getString("Function"));
            // read Function configuration

            // read Function parameter/weight
            thisweight=thislayer.getJSONArray("Weight");
            if(thisweight.get(0).getClass().getTypeName().equals("org.json.JSONArray")){
                double[][] thislayerweight=new double[thisweight.length()][];
                layerweight[m]=thislayerweight;

                for (int i=0,il=thisweight.length();i<il ;i++ ) {
                    weightline=thisweight.getJSONArray(i);
                    thislayerweight[i]=new double[weightline.length()];
                    for (int j=0,jl=weightline.length();j<jl ;j++ )
                        thislayerweight[i][j]=weightline.getDouble(j);
                }
            } else{
                double[] thislayerweight=new double[thisweight.length()];
                layerweight[m][0]=thislayerweight;

                for (int i=0,il=thisweight.length();i<il ;i++ )
                    thislayerweight[i]=thisweight.getDouble(i);
            }
        }
        */

        HashMap<String,Object> map=(HashMap<String,Object>)Format.json_variable(network);
        HashMap<String,Object>[] sumconf=(HashMap<String,Object>[]) map.get("configuration");
        Object[][] weight=(Object[][]) map.get("weight");
        String[] function=(String[]) map.get("function");
        //Method[] layermethod=new Method[function.length];
        //MethodAccess[] layermethod=new MethodAccess[function.length];
        int[] methodindex=new int[function.length];
        for (int i=0,il=function.length;i<il ;i++ ) {
            //Object inputobj,Object[] weightobj,HashMap<String,Object> confobj
            // Object[].class; //也可以直接这么表示！！！！
            //layermethod[i]=Class.forName("ImageProcess").getDeclaredMethod(function[i],Object.class,Object[].class,HashMap.class);
            //layermethod[i]=MethodAccess.get(ImageProcess.class); // 直接.class就行了
            methodindex[i]=thismethod.getIndex(function[i]);
        }
        //MethodAccess method=MethodAccess.get(ImageProcess.class);


        // read the data
        Object[] data=MNISTpath_whitearr(imagepath,labelpath);
        int[][][] colordata=(int[][][])data[0];
        int[] actualres=(int[])data[1];
        //System.out.println(Format.variable_json(colordata[7]));
        colorarr_path(whitearr_color(colordata[7]),"C:\\Users\\HP\\Desktop\\1.png");


        //double[] res6=num2arr_forwardresult(layermethod,weight,sumconf,colordata[7]);
        //System.out.println("res6___");
        //System.out.println(Format.variable_json(res6));
        Format.json_txtpath((JSONObject) Format.variable_json(map),"D:\\MNIST\\network\\LeNet-5_1.txt");


        int DataSum=colordata.length;
        int index=0;
        long now = System.currentTimeMillis();
        for (int n=0;n<200 ;n++ ) {
            index=(int)(Math.random()*DataSum);//7;//
            //index=33681;
            //index=7; // 盯着一个反复学。。。没效率，学个2、3轮，就定着不动了
            System.out.println("Random Number___"+index+"__"+actualres[index]);
            //System.out.println(actualres[7]);
            int[][][] part_data=new int[][][]{colordata[index]};
            int[] part_actualres=new int[]{actualres[index]};
            //System.out.println("weight_matrix____");
            //System.out.println(Format.variable_json(gradient));
            // gradient
            Object[][] gradient=num2arr_gradient_average(methodindex,weight,sumconf,0.0001,part_data,part_actualres);
            System.out.println("gradient_matrix_____");
            System.out.println(Format.variable_json(gradient));
            // Before___ result, loss
            double[][] originres=num2arr_forwardresult(methodindex,weight,sumconf,part_data);
            double originloss=num2arr_loss_average(originres,part_actualres,"LeNet-5");
            System.out.println("_____Now result is__");
            System.out.println(Format.variable_json(originres));
            System.out.println("_____Now  loss is__"+originloss);
            // After___ weight, result, loss
            System.out.println("weight_matrix_after learning____");
            weight_backpropagation(weight,gradient,0.0001); // 直接导致了biasg过大完全saturated了。。。dat与times匹配时，最能实现loss的下降
            System.out.println(Format.variable_json(weight));
            double[][] thenres=num2arr_forwardresult(methodindex,weight,sumconf,part_data);// 前期weight都是0，导致各个图的结果result都是相同的
            double thenloss=num2arr_loss_average(thenres,part_actualres,"LeNet-5");
            System.out.println("_____Then result is__");
            System.out.println(Format.variable_json(thenres));
            System.out.println("_____Then loss is__"+thenloss);
            // saving
            Format.json_txtpath((JSONObject) Format.variable_json(map),"D:\\MNIST\\network\\LeNet-5_1.txt");
            System.out.println("耗时___"+(System.currentTimeMillis() - now) + " ms");//，和是" +sum);
            now=System.currentTimeMillis();
            //System.out.println("The step sum time___ "+testtime+" ms"); testtime=0;
            System.out.println();
            System.out.println();
        }
        // new 方法，所需时间：。。。已经采用了MethodAccess反射法
        // 89532 ms
        // 90908 ms


        /*
        double[][][] res0=(double[][][]) convolution(colordata[7],weight[0],sumconf[0]);
        System.out.println("res0___");
        System.out.println(Format.variable_json(res0));
        double[][][] res1=(double[][][]) sampling(res0,weight[1],sumconf[1]);
        System.out.println("res1___");
        System.out.println(Format.variable_json(res1));
        double[][][] res2=(double[][][]) convolution(res1,weight[2],sumconf[2]);
        System.out.println("res2___");
        System.out.println(Format.variable_json(res2));
        double[][][] res3=(double[][][]) sampling(res2,weight[3],sumconf[3]);
        System.out.println("res3___");
        System.out.println(Format.variable_json(res3));
        double[][][] res4=(double[][][]) convolution(res3,weight[4],sumconf[4]);
        System.out.println("res4___");
        System.out.println(Format.variable_json(res4));
        double[][][] res5=(double[][][]) convolution(res4,weight[5],sumconf[5]);
        System.out.println("res5___");
        System.out.println(Format.variable_json(res5));
        double[] res6=(double[]) outputlayer(res5,weight[6],sumconf[6]);
        System.out.println("res6___");
        System.out.println(Format.variable_json(res6));*/

        //return num2arr_training(layermethod,weight,sumconf,colordata,actualres);
        return null;
    }


    public static Object[][] num2arr_training(int[] layermethod,Object[][] sumweight,Object[] sumconf,int[][][] colordata,int[] actualres) throws Exception {
        Object[][] sumgradient=null;
        Object[][] weightupdated=sumweight;

        for (int iterator=0;iterator<100 ;iterator++ ) {
            sumgradient= num2arr_gradient_average(layermethod,weightupdated,sumconf,0.01,colordata,actualres);
            weightupdated=weight_backpropagation(sumweight,sumgradient,0.1);
        }

        return weightupdated;
    }

    // backward propagation, according to the gradient, -gradient*times
    public static Object[][] weight_backpropagation(Object[][] sumweight,Object[][] sumgradient,double times) throws Exception {
        // update the weight, according to the gradient
        // the term "weight" here, also including bias
        for (int m=0,ml=sumweight.length;m<ml;m++) {
            for (int th=0,thl=sumweight[m].length;th<thl ;th++ ) {
                switch (sumweight[m][th].getClass().getTypeName()) {
                    case "double[]": { // double[]
                        double[] weight = (double[]) sumweight[m][th];
                        double[] gradient = (double[]) sumgradient[m][th];
                        for (int w = 0, wl = weight.length; w < wl; w++)
                            weight[w] -= (gradient[w] * times);
                    }
                    break;
                    case "double[][]": { // double[][]
                        double[][] weight = (double[][]) sumweight[m][th];
                        double[][] gradient = (double[][]) sumgradient[m][th];
                        for (int x = 0, xl = weight.length; x < xl; x++)
                            for (int y = 0, yl = weight[x].length; y < yl; y++)
                                weight[x][y] -= (gradient[x][y] * times);
                    }
                    break;
                    case "double[][][]": { // double[][]
                        double[][][] weight = (double[][][]) sumweight[m][th];
                        double[][][] gradient = (double[][][]) sumgradient[m][th];
                        for (int x = 0, xl = weight.length; x < xl; x++)
                            for (int y = 0, yl = weight[x].length; y < yl; y++)
                                for (int z = 0, zl = weight[x][y].length; z < zl; z++)
                                    weight[x][y][z] -= (gradient[x][y][z] * times);
                    }
                    break;
                    case "double[][][][]":{
                        double[][][][] weight = (double[][][][]) sumweight[m][th];
                        double[][][][] gradient = (double[][][][]) sumgradient[m][th];
                        for (int x = 0, xl = weight.length; x < xl; x++)
                            for (int y = 0, yl = weight[x].length; y < yl; y++)
                                for (int z = 0, zl = weight[x][y].length; z < zl; z++)
                                    for (int w=0,wl=weight[x][y][z].length;w<wl ;w++ ) {
                                        weight[x][y][z][w] -= (gradient[x][y][z][w] * times);
                                    }
                    }
                    break;
                    default:{
                        throw new Exception("wrong type! not complete !____"+sumweight[m][th].getClass().getTypeName());
                    }
                }
            }
        }

        return sumweight;
    }

    // get the gradient of the network at some points... gradient--Object[] , consistent with layerweight
    // this is average gradient (average of all the colordata), when nl=1, this is for single colorimage
    // 通过控制 colordata的规模nl来实现
    public static Object[][] num2arr_gradient_average(int[] layermethod,Object[][] sumweight,Object[] sumconf,double dat,int[][][] colordata,int[] actualres) throws Exception {
        //ArrayList<Double> gradientlist=new ArrayList<>();
        Object[][] layergradient=new Object[sumweight.length][];
        double[][] originres=num2arr_forwardresult(layermethod,sumweight,sumconf,colordata);
        double originloss=num2arr_loss_average(originres,actualres,"LeNet-5");
        double[][] datres=null;
        double datloss=0;
        //Object[][] stack=new Object[20][];
        for (int m=0,ml=sumweight.length;m<ml;m++){ // different layer
            layergradient[m]=new Object[sumweight[m].length];
            for (int th=0,thl=sumweight[m].length;th<thl ;th++ ) { // in each layer, differenct format/operation of weight(including bias)
                //System.out.println("_____"+m+"_"+th+"_"+sumweight[m][th].getClass().getTypeName());
                switch (sumweight[m][th].getClass().getTypeName()) {
                    case "double[]": { // double[]
                        double[] weight = (double[]) sumweight[m][th];
                        double[] gradient = new double[weight.length];
                        layergradient[m][th] = gradient;
                        for (int w = 0, wl = weight.length; w < wl; w++) {
                            // dat change for caculating the gradient
                            weight[w] += dat;
                            datres = num2arr_forwardresult(layermethod, sumweight,sumconf, colordata);
                            datloss=num2arr_loss_average(datres,actualres,"LeNet-5");
                            gradient[w] = (datloss - originloss) / dat;
                            //if(datloss - originloss!=0)
                            //System.out.println((datloss - originloss)+"___"+gradient[w]);
                            // recovery after calculation
                            weight[w] -= dat;
                        }
                    }
                    break;
                    case "double[][]": { // double[][]
                        double[][] weight = (double[][]) sumweight[m][th];
                        double[][] gradient = new double[weight.length][];
                        layergradient[m][th] = gradient;
                        for (int x = 0, xl = weight.length; x < xl; x++) {
                            gradient[x] = new double[weight[x].length];
                            for (int y = 0, yl = weight[x].length; y < yl; y++) {
                                // dat change for caculating the gradient
                                weight[x][y] += dat;
                                datres = num2arr_forwardresult(layermethod, sumweight,sumconf, colordata);
                                datloss=num2arr_loss_average(datres,actualres,"LeNet-5");
                                gradient[x][y] = (datloss - originloss) / dat;
                                //if(datloss - originloss!=0)
                                //System.out.println((datloss - originloss)+"___"+gradient[x][y]);
                                // recovery after calculation
                                weight[x][y] -= dat;
                            }
                        }
                    }
                    break;
                    case "double[][][]": { // double[][][]
                        double[][][] weight = (double[][][]) sumweight[m][th];
                        double[][][] gradient = new double[weight.length][][];
                        layergradient[m][th] = gradient;
                        for (int x = 0, xl = weight.length; x < xl; x++) {
                            gradient[x] = new double[weight[x].length][];
                            for (int y = 0, yl = weight[x].length; y < yl; y++) {
                                gradient[x][y]=new double[weight[x][y].length];
                                for (int z=0,zl=weight[x][y].length;z<zl ;z++ ) {
                                    // dat change for caculating the gradient
                                    weight[x][y][z] += dat;
                                    datres = num2arr_forwardresult(layermethod, sumweight,sumconf, colordata);
                                    datloss=num2arr_loss_average(datres,actualres,"LeNet-5");
                                    gradient[x][y][z] = (datloss - originloss) / dat;
                                    //if(datloss - originloss!=0)
                                    //System.out.println((datloss - originloss)+"___"+gradient[x][y][z]);
                                    // recovery after calculation
                                    weight[x][y][z] -= dat;
                                }
                            }
                        }
                    }
                    break;
                    case "double[][][][]": { // double[][][][]
                        double[][][][] weight = (double[][][][]) sumweight[m][th];
                        double[][][][] gradient = new double[weight.length][][][];
                        double[] thisweightline=null;
                        layergradient[m][th] = gradient;
                        //System.out.println("Sum___"+weight.length+"   ");
                        for (int x = 0, xl = weight.length; x < xl; x++) {
                            //System.out.print("_");
                            gradient[x] = new double[weight[x].length][][];
                            for (int y = 0, yl = weight[x].length; y < yl; y++) {
                                gradient[x][y]=new double[weight[x][y].length][];
                                for (int z=0,zl=weight[x][y].length;z<zl ;z++ ) {
                                    gradient[x][y][z]=new double[weight[x][y][z].length];
                                    thisweightline=weight[x][y][z];
                                    for (int w=0,wl=weight[x][y][z].length;w<wl ;w++ ) {
                                        // dat change for caculating the gradient
                                        weight[x][y][z][w] += dat;
                                        //thisweightline[w]+=dat; // 索引不是主要原因
                                        datres = num2arr_forwardresult(layermethod, sumweight,sumconf, colordata);
                                        datloss=num2arr_loss_average(datres,actualres,"LeNet-5");
                                        gradient[x][y][z][w] = (datloss - originloss) / dat;
                                        //if(datloss - originloss!=0)
                                        //System.out.println((datloss - originloss)+"___"+gradient[x][y][z][w]);
                                        // recovery after calculation
                                        weight[x][y][z][w] -= dat;
                                        //thisweightline[w]-=dat;
                                    }
                                }
                            }
                        }
                        //System.out.println();
                    }
                    break;
                    default:{
                        throw new Exception("wrong type! not complete !____"+sumweight[m][th].getClass().getTypeName());
                    }
                }
            }
        }
        //System.out.println("_____Now loss is___"+originloss);

        /*
        double[] gradient=new double[gradientlist.size()];
        for (int i=0,il=gradient.length;i<il ;i++ )
            gradient[i]=gradientlist.get(i);*/
        return layergradient;
    }

    // iterate every elements , select the elements of specific type and take some operation
    // save the result as the same format as the sumelement structure
    public static Object iterate_operation(Object sumelement, String targettype, Method method, Object[] parameter) throws InvocationTargetException, IllegalAccessException {
        if (sumelement==null) {
            return null;
        }
        Object result=null;
        String type=sumelement.getClass().getTypeName();
        // 先判断是否找到了
        if(type.equals(targettype)){
            result=method.invoke(null,parameter);
        }else{
            int bracketindex=type.indexOf("[]");
            if (bracketindex!=-1) {// 可以拆分--->子元素中寻找
                Object[] array=(Object[]) sumelement;
                Object[] resultarray=new Object[array.length]; result=resultarray;
                for (int i=0,l=array.length;i<l ;i++ )
                    resultarray[i]=iterate_operation(array[i],targettype,method,parameter);
            } else {// 不可拆分
                result=null;
            }
        }
        return result;
    }



    // get the error/loss function of the network
    public static double num2arr_loss_average(double[][] y,int[] actualindex,String type) throws Exception {
        double[] Ew2arr=new double[y.length];
        double sum=0;
        for (int i=0,l=y.length;i<l ;i++ ) {
            Ew2arr[i]=numarr_loss(y[i],actualindex[i],type);
            sum+=Ew2arr[i];
        }
        // average
        return sum/y.length; // double/int ?????????
    }
    public static double numarr_loss(double[] y,int actualindex,String type) throws Exception {
        int l=y.length;
        double Ew=0;
        //System.out.println(type);
        switch(type){
            case "LeNet-5":{
                double j=50; // constant j ??????????????????????
                // j决定，近到什么程度，才会有处罚，否则一定距离之外，没有处罚（即只有y<j,y才有效果）
                // j need careful choosing to prevent undistinguished,
                // j is small enough to make unright option effective
                // j is big enough to prevent the result is to (-)big
                double logsum=Math.exp(-j);
                for (int i=0;i<l ;i++ )
                    if (i==actualindex) {
                        Ew+=y[i];
                    }else{
                        logsum+=Math.exp(-y[i]);
                    }
                Ew+=Math.log(logsum); // log==ln ???
            }
            break;
            default:{
                throw new Exception("wrong loss Type"); //防止类型写错了
            }
        }
        return Ew;
    }
    /*
    public static double num2arr_loss(Method[] layermethod,Object[] sumweight,Object[] sumconf,int[][][] colordata,int[] actualres) throws Exception {
        int datal=actualres.length;
        int[] forwardres=num2arr_forwardresult(layermethod,sumweight,sumconf,colordata);
        long MSE=0;
        int thisdata;
        for (int n=0;n<datal ;n++ ) {
            //thisdata=actualres[n]-forwardres[n]; // !!!!!!!!!!!!!!! 不应该是相见， 4，5之间显然没有任何关系，英国直接用0-1法
            //thisdata*=thisdata;
            //MSE+=thisdata;
            if(actualres[n]!=forwardres[n])
                MSE++;
        }
        double averageMSE=((double)MSE)/((double)datal*2.0);
        return averageMSE;
    }*/

    // get the result of the network.... basics for the training
    // weight is included in the layerparameters
    public static double[][] num2arr_forwardresult(int[] layermethod,Object[][] sumweight,Object[] sumconf,int[][][] colordata)  //  using black and white
            throws Exception {
        int datal = colordata.length;
        double[][] result = new double[datal][];
        for (int n = 0; n < datal; n++) {
            result[n] = num2arr_forwardresult(layermethod,sumweight,sumconf,colordata[n]);
        }

        return result;
    }
    public static double[] num2arr_forwardresult(int[] layermethod,Object[][] sumweight,Object[] sumconf,int[][] singlecolor)  throws Exception {
        //  using black and white
        int[][] white = singlecolor;
        int ml = layermethod.length;


        double[][] initial = new double[white.length][white[0].length];
        Object nowresult = initial;


        // initial....直接带入？？？？
        for (int i = 0, il = white.length, jl = white[0].length; i < il; i++)
            for (int j = 0; j < jl; j++)
                initial[i][j] = white[i][j];
/**/
        /*
        long now=0;
        C1(sumoutput[0],singlecolor,(double[][][])sumweight[0][0],(double[])sumweight[0][1]);
        S2(sumoutput[1],sumoutput[0],(double[])sumweight[1][0],(double[])sumweight[1][1]); // 修改到此： 耗时___87646 ms； 耗时___89559 ms； 耗时___88404 ms； 耗时___94655 ms
        //long now=System.currentTimeMillis();
        now = System.currentTimeMillis();
        for (int n = 0; n < 100000; n++)
        C3(sumoutput[2],sumoutput[1],(double[][][][])sumweight[2][0],(double[])sumweight[2][1],(int[][])(((HashMap<String,Object>)sumconf[2]).get("type"))); //33681__7: 85052 ms(before)  VS 87596 ms
        System.out.println("total time____ " + (System.currentTimeMillis() - now));

        //testtime+=(System.currentTimeMillis()-now);
        // 13595 ms(85847 ms) VS 12061 ms(87960 ms)
        Object nowresult=sumoutput[2];
        // layer to layer
        */

        for (int m = 0; m < ml; m++) {
            /*if (m == 2) { 20957ms VS 19565ms VS 21598ms(反复调顺序)。。。。有时不明显啊。。。operation的循环需要4阶，new才3阶，new的时间损耗不明显
                // 20957ms VS 21598ms(反复调用) VS 19565ms(避免反复调用) VS 11496 ms(1个循环), 3296 ms(无tanh)
                now = System.currentTimeMillis();
                for (int n = 0; n < 100000; n++) {
                    // regular operation
                    Object tempresult = thismethod.invoke(null, layermethod[m], nowresult, sumweight[m], (HashMap<String, Object>) sumconf[m]);
                    // invoke中第一个Object参数为该方法类的实例instance
                    // Activation
                    tempresult = Activation(tempresult, (HashMap<String, Object>) sumconf[m]);
                }
                System.out.println("total time____ " + (System.currentTimeMillis() - now));
            }*/
            // regular operation
            nowresult = thismethod.invoke(null, layermethod[m], nowresult, sumweight[m], (HashMap<String, Object>) sumconf[m]);
            // invoke中第一个Object参数为该方法类的实例instance
            // Activation
            nowresult = Activation(nowresult, (HashMap<String, Object>) sumconf[m]);

        }

        return (double[])nowresult;


        /*
        // 不采用反射，直接运行，看看效果
        double[][][] res0=(double[][][]) convolution(singlecolor,sumweight[0],(HashMap<String, Object>)sumconf[0]);
        //System.out.println("res0___");
        //System.out.println(Format.variable_json(res0));
        double[][][] res1=(double[][][]) sampling(res0,sumweight[1],(HashMap<String, Object>)sumconf[1]);
        //System.out.println("res1___");
        //System.out.println(Format.variable_json(res1));
        double[][][] res2=(double[][][]) convolution(res1,sumweight[2],(HashMap<String, Object>)sumconf[2]);
        //System.out.println("res2___");
        //System.out.println(Format.variable_json(res2));
        double[][][] res3=(double[][][]) sampling(res2,sumweight[3],(HashMap<String, Object>)sumconf[3]);
        //System.out.println("res3___");
        //System.out.println(Format.variable_json(res3));
        double[][][] res4=(double[][][]) convolution(res3,sumweight[4],(HashMap<String, Object>)sumconf[4]);
        //System.out.println("res4___");
        //System.out.println(Format.variable_json(res4));
        double[][][] res5=(double[][][]) convolution(res4,sumweight[5],(HashMap<String, Object>)sumconf[5]);
        //System.out.println("res5___");
        //System.out.println(Format.variable_json(res5));
        double[] res6=(double[]) outputlayer(res5,sumweight[6],(HashMap<String, Object>)sumconf[6]);
        //System.out.println("res6___");
        //System.out.println(Format.variable_json(res6));
        return res6;*/

    }

    // raw various types of functions:
    public static double[][][][] sumoutput=new double[][][][]{
            new double[6][28][28],
            new double[6][14][14],
            new double[16][10][10],
            new double[16][5][5],
            new double[120][1][1],
            new double[84][1][1]
    };
    public static double[][][] C1(double[][][] output,int[][] input,double[][][] weight,double[] bias){
        double sum=0;
        double[][] thisplaneweight=null;
        for (int out=0,ol=weight.length;out<ol;out++) {
            thisplaneweight=weight[out];
            for (int i = 0,il=output[out].length; i <il;i++ ) {
                for (int j=0,jl=output[out][i].length;j<jl ;j++ ) {
                    sum = 0;
                    for (int x=0,xl=weight[out].length;x<xl ;x++ ) {
                        for (int y=0,yl=weight[out][x].length;y<yl ;y++ ) {
                            sum +=input[i+x][j+y]*thisplaneweight[x][y];
                        }
                    }
                    sum+=bias[out];
                    sum=1.7159*Math.tanh(0.6667 * sum);
                    output[out][i][j]=sum;
                }
            }
        }
        return output;
    }
    public static double[][][] S2(double[][][] output,double[][][] input,double[] weight,double[] bias){
        double sum=0;
        double[][] thisinputplane=null;
        for (int out=0,ol=output.length;out<ol ;out++ ) {
            thisinputplane=input[out];
            for (int x=0,xl=output[out].length;x<xl ;x++ ) {
                for (int y=0,yl=output[out][x].length;y<yl ;y++ ) {
                    sum=0;
                    for (int datx=0;datx<2 ;datx++ ) {
                        for (int daty=0;daty<2 ;daty++ ) {
                            sum +=thisinputplane[2*x+datx][2*y+daty];
                        }
                    }
                    sum *=weight[out];
                    sum +=bias[out];
                    output[out][x][y]=sum;
                }
            }
        }
        return output;
    }
    public static double[][][] C3(double[][][] output,double[][][] input,double[][][][] weight,double[] bias,int[][] conf){
        /*double sum=0;
        double[][] filter=null;
        double[][] thisinput=null;
        for (int out=0,ol=output.length;out<ol ;out++ ) {
            for (int i = 0,il=output[out].length; i <il;i++ ) {
                for (int j = 0, jl = output[out][i].length; j < jl; j++) {
                    sum=0;
                    for (int th=0,thl=weight[out].length;th<thl ;th++ ) { // 不能把 input[inputindex]防止内层，否则检索消耗时间很多
                        filter=weight[out][th];
                        //System.out.println(filter.);
                        thisinput=input[conf[out][th]];
                        for (int x=0,xl=filter.length;x<xl ;x++ ) {
                            for (int y=0,yl=filter[x].length;y<yl ;y++ ) {
                                //System.out.println(inputindex+"___"+x+"___"+y+"___"+(i+x)+"___"+(j+y));
                                sum +=thisinput[i+x][j+y]*filter[x][y];
                            }
                        }
                    }
                    sum+=bias[out];
                    sum=1.7159*Math.tanh(0.6667 * sum);
                    output[out][i][j]=sum;
                }
            }
        }*/



        // 0 --->sum, add
        double[][] thisoutput=null;
        double[][] thisinput=null;
        double[][] thisweight=null;
        double sum=0;
        for (int out=0,ol=output.length;out<ol ;out++ ) { // different output
            thisoutput=output[out];
            for (int i=0,il=thisoutput.length;i<il ;i++ ) {
                for (int j=0,jl=thisoutput[i].length;j<jl ;j++ ) {
                    thisoutput[i][j]=0;
                }
            }
            for (int th=0,thl=conf[out].length;th<1 ;th++ ) { // different input
                thisinput=input[conf[out][th]];
                thisweight=weight[out][th];
                // convolution
                for (int i=0,il=thisoutput.length;i<il ;i++ ) {
                    for (int j=0,jl=thisoutput[i].length;j<jl ;j++ ) {
                        sum=0;
                        for (int x=0,xl=thisweight.length;x<xl ;x++ ) {
                            for (int y=0,yl=thisweight[x].length;y<yl ;y++ ) {
                                sum +=thisinput[i+x][j+y]*thisweight[x][y];
                            }
                        }
                        thisoutput[i][j] += sum;
                    }
                }
            }
            for (int i=0,il=thisoutput.length;i<il ;i++ ) {
                for (int j=0,jl=thisoutput[i].length;j<jl ;j++ ) {
                    thisoutput[i][j]=1.7159*Math.tanh(0.6667 *(thisoutput[i][j]+bias[out]));
                }
            }
        }

        return output;
    }








    // various types of functions

    // RBF(Radial Based Function), for output layer, discret classification
    public static Object outputlayer(Object inputobj, Object[] weightobj, HashMap<String,Object> confobj){
        String type=(String)confobj.get("type");
        Object res=null;
        switch(type){
            case "RBF_Euclid":{
                int[][] weight=(int[][])confobj.get("data");
                double[] input=num3arrtonum1arr((double[][][])inputobj);// convert input from 3-D to 1-D firstly
                //double[][] weight=(double[][]) weightobj;
                double[] output=new double[weight.length]; res=output;
                double sum=0.0;
                double dat=0.0;
                for (int n=0,nl=output.length;n<nl ;n++ ){
                    sum=0;
                    for (int ini=0,inl=input.length;ini<inl ;ini++ ) {
                        dat=input[ini]-weight[n][ini];
                        sum+=dat*dat;
                    }
                    output[n]=sum;
                }
            }
            break;
        }
        return res;
    }
    //volume data(3-D) to line data(1-D), simply take the [0][0]
    public static double[] num3arrtonum1arr(double[][][] input){
        int l=input.length;
        double[] output=new double[l];
        for (int i=0; i<l ;i++ )
            output[i]=input[i][0][0];

        return output;
    }


    // sampling
    public static Object sampling(Object inputobj,Object[] weightobj,HashMap<String,Object> confobj){
        Object res=null;
        switch (inputobj.getClass().getTypeName()){
            case "double[][][]":{
                double[] weight=(double[]) weightobj[0];    // 0---weight;
                double[] bias=(double[]) weightobj[1];      // 1---bias
                double[][][] input=(double[][][]) inputobj;
                int step=(int) confobj.get("step");
                double[][][] output=new double[input.length][][]; res=output;
                for (int n=0,nl=input.length;n<nl;n++ ) {
                    output[n]=sampling_basic(input[n],weight[n],bias[n],step);
                }
            }
            break;
        }
        return res;
    }
    public static double[][] sampling_basic(double[][] input,double weight,double bias,int step){
        int lx=input.length;
        int ly=input[0].length;
        int outx=lx/step;
        int outy=ly/step;
        double[][] output=new double[outx][outy];
        double sum=0.0;
        for (int i=0;i<outx ;i++ )
            for (int j=0;j<outy ;j++ ) {
                sum = 0.0;
                for (int datx = 0; datx < step; datx++)
                    for (int daty = 0; daty < step; daty++)
                        sum += input[i + datx][j + daty];
                sum *= weight;
                sum += bias;
                output[i][j] = sum;
            }
        return output;
    }


    // differenct type of convolution
    public static Object convolution(Object inputobj, Object[] weightobj,HashMap<String,Object> confobj){ //+constant ???
        double[][][] output=null;
        switch (inputobj.getClass().getTypeName()){
            case "int[][]": { // int[][] 强制转化为 double[][] ???????????????????????
                int[][] inputint=(int[][]) inputobj;
                double[][] input=new double[inputint.length][inputint[0].length];
                for (int i=0,il=input.length;i<il;i++ )
                    for (int j=0,jl=input[i].length;j<jl;j++ )
                        input[i][j]=inputint[i][j];
                inputobj=input;
            }
            case "double[][]":{
                double[][] input=(double[][]) inputobj; // ????? int[][]--->double[][]
                double[][][] weight=(double[][][]) weightobj[0]; // 0---weight, 1---bias
                double[] bias=(double[]) weightobj[1];
                output=new double[weight.length][][];
                for (int n=0,nl=weight.length;n<nl;n++ ) {
                    output[n]=convolution_basic(input,weight[n]);
                    output[n]=conaddto(output[n],bias[n]);
                }
            }
            break;
            case "double[][][]":{
                double[][][] input=(double[][][]) inputobj;
                double[][][][] weight=(double[][][][]) weightobj[0];
                double[] bias=(double[]) weightobj[1];
                output=new double[weight.length][][];
                //System.out.println(confobj.get("type").getClass().getTypeName());
                switch (confobj.get("type").getClass().getTypeName()){
                    case "int[][]": {
                        int[][] conf = (int[][]) confobj.get("type"); // which input the weight corresponds to ???
                        for (int n = 0, nl = weight.length; n < nl; n++)
                            for (int c = 0, cl = conf[n].length; c < cl; c++) {
                                output[n] = addto(output[n], convolution_basic(input[conf[n][c]], weight[n][c])); // warning ????
                                output[n]=conaddto(output[n],bias[n]);
                            }
                    }
                    break;
                    case "java.lang.String":{ // 要写全，不能单写String
                        String conf=(String) confobj.get("type");
                        //System.out.println(conf);
                        switch (conf){
                            case "Full":{
                                for (int n=0,nl=weight.length;n<nl ;n++ )
                                    for (int ini=0,inl=input.length;ini<inl ;ini++ ) {
                                        output[n] = addto(output[n], convolution_basic(input[ini], weight[n][ini]));
                                        output[n]=conaddto(output[n],bias[n]);
                                    }
                            }
                            break;
                        }
                    }
                    break;
                }

            }
            break;
        }
        return output;
    }
    public static double[][] convolution_basic(double[][] input,double[][] filter){
        int inx=input.length;
        int iny=input[0].length;
        int fx=filter.length;
        int fy=filter[0].length;
        int outx=inx-fx+1;
        int outy=iny-fy+1;
        double[][] output=new double[outx][outy];
        double sum=0.0;
        for (int i=0; i<outx; i++)
            for (int j=0; j<outy; j++) {
                sum = 0;
                for (int datx = 0; datx < fx; datx++)
                    for (int daty = 0; daty < fy; daty++)
                        sum += (filter[datx][daty] * input[i + datx][j + daty]);
                output[i][j] = sum;
            }
        return output;
    }

    ////// Activation function !!!!
    public static Object Activation(Object inputobj,HashMap<String,Object> conf) throws Exception {
        // 先统一化为2-dimenison
        Object inputobj2D=Format.numNarr_num2arr_All(inputobj);
        // int[][] ---> double[][] 怎么办 ？？？
        double[][] input=(double[][]) inputobj2D;

        // 再做处理
        if (!conf.containsKey("Activation")) {
            return inputobj;
        }
        switch((String)conf.get("Activation")){
            case "tanh":{
                double tanh_A=1.7159;//(double)conf.get("A");
                double tanh_S=0.6667;//(double)conf.get("S");
                for (int i=0,il=input.length; i<il;i++ ) {
                    for (int j = 0, jl = input[i].length; j < jl; j++) {
                        input[i][j]=tanh_A*Math.tanh(tanh_S * input[i][j]);
                        if (input[i][j]>tanh_A) {
                            throw new Exception("something is wrong");
                        }
                    }
                }
            }
            break;
            case "sigmoid":{
                for (int i=0,il=input.length; i<il;i++ ) {
                    for (int j = 0, jl = input[i].length; j < jl; j++) {
                        input[i][j]=1.0/(1+Math.exp(-input[i][j]));
                    }
                }
            }
            break;
            case "Relu":{
                for (int i=0,il=input.length; i<il;i++ ) {
                    for (int j = 0, jl = input[i].length; j < jl; j++) {
                        if(input[i][j]<0)
                            input[i][j]=0;
                    }
                }
            }
            break;
            case "softmax":{
                double denominator=0;
                for (int i=0,il=input.length; i<il;i++ )
                    for (int j = 0, jl = input[i].length; j < jl; j++) {
                        input[i][j]=Math.exp(input[i][j]);
                        denominator+=input[i][j];
                    }
                for (int i=0,il=input.length; i<il;i++ )
                    for (int j = 0, jl = input[i].length; j < jl; j++)
                        input[i][j]/=denominator;
            }
            break;
        }

        return inputobj;
    }





    ////// assistant function
    // matrix a + matrix b--->matrix a; if a==null, a=b;
    public static double[][] addto(double[][] a,double[][] b) {
        if(a==null)
            return b;
        int lx = a.length;
        int ly = a[0].length;
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                a[i][j] += b[i][j];
        return a;
    }
    // matrix a + bias---> matrix a
    public static double[][] conaddto(double[][] a, double con) {
        int lx = a.length;
        int ly = a[0].length;
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                a[i][j] += con;
        return a;
    }





    public static Object[] MNISTpath_whitearr_padding(String imagepath,String labelpath) throws IOException {
        // label first to rename the image file
        byte[] bytenum=new byte[4];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(labelpath));
        in.read(bytenum,0,4);
        in.read(bytenum,0,4);
        int datal=bytearr_int(bytenum);
        int[] num=new int[datal];
        for (int n=0;n<datal ;n++ ) {
            num[n]=in.read();
        }
        //System.out.println(Format.variable_json(num));


        in = new BufferedInputStream(new FileInputStream(imagepath));
        in.read(bytenum,0,4);
        in.read(bytenum,0,4);
        datal=bytearr_int(bytenum);
        in.read(bytenum,0,4);
        int ly=bytearr_int(bytenum);
        in.read(bytenum,0,4);
        int lx=bytearr_int(bytenum);
        //System.out.println(datal+"___rows_"+ly+"___columns_"+lx);
        int[][][] white=new int[datal][][];
        int[][] thiswhite=null;
        for (int n=0;n<datal ;n++ ) {
            //System.out.println("Now___"+n);
            thiswhite=new int[lx][ly];
            for (int j=0;j<ly ;j++ ) {
                for (int i=0;i<lx ;i++ ) {
                    thiswhite[i][j]=in.read();
                    /*if (thiswhite[i][j]>0) { 醒目的输出0-1矩阵，看出图案pattern
                        thiswhite[i][j]=8;
                    }*/
                }
            }
            white[n]=padding(thiswhite,0,2,2,2,2);
            //colorarr_path(whitearr_color(thiswhite),"D:\\MNIST\\training\\"+n+"__"+num[n]+".png");
        }

        return new Object[]{white,num};
    }
        // read data from MNIST Data Set
    public static Object[] MNISTpath_whitearr(String imagepath,String labelpath) throws IOException {
        // label first to rename the image file
        byte[] bytenum=new byte[4];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(labelpath));
        in.read(bytenum,0,4);
        in.read(bytenum,0,4);
        int datal=bytearr_int(bytenum);
        int[] num=new int[datal];
        for (int n=0;n<datal ;n++ ) {
            num[n]=in.read();
        }
        //System.out.println(Format.variable_json(num));


        in = new BufferedInputStream(new FileInputStream(imagepath));
        in.read(bytenum,0,4);
        in.read(bytenum,0,4);
        datal=bytearr_int(bytenum);
        in.read(bytenum,0,4);
        int ly=bytearr_int(bytenum);
        in.read(bytenum,0,4);
        int lx=bytearr_int(bytenum);
        //System.out.println(datal+"___rows_"+ly+"___columns_"+lx);
        int[][][] white=new int[datal][][];
        int[][] thiswhite=null;
        for (int n=0;n<datal ;n++ ) {
            //System.out.println("Now___"+n);
            thiswhite=new int[lx][ly];
            for (int j=0;j<ly ;j++ ) {
                for (int i=0;i<lx ;i++ ) {
                    thiswhite[i][j]=in.read();
                    /*if (thiswhite[i][j]>0) { 醒目的输出0-1矩阵，看出图案pattern
                        thiswhite[i][j]=8;
                    }*/
                }
            }
            white[n]=thiswhite;
            //colorarr_path(whitearr_color(thiswhite),"D:\\MNIST\\training\\"+n+"__"+num[n]+".png");
        }

        return new Object[]{white,num};
    }
    public static int bytearr_int(byte[] b){
        int l=b.length;
        int num=0;
        for (int i=0;i<l ;i++ ) {
            num*=256;
            num+=(b[i]&0xFF); // &0xFF to make it unsigned ???
        }
        return num;
    }
    public static int[][] padding(int[][] input,int value,int up,int down,int left,int right){
        int lx=input.length;
        int ly=input[0].length;
        int[][] output=new int[left+lx+right][up+ly+down];
        // remain the same
        for (int i=0;i<lx ;i++ )
            for (int j=0;j<ly ;j++ )
                output[i+left][j+up]=input[i][j];
        // padding
        // left
        int nx=output.length;
        int ny=output[0].length;
        for (int i=0;i<left ;i++ ) {
            for (int j = 0; j < ny; j++) {
                output[i][j] = value;
            }
        }
        // right
        for (int i=left+lx-1;i<nx ;i++ ) {
            for (int j = 0; j < ny; j++) {
                output[i][j] = value;
            }
        }
        // up
        for (int i=0;i<nx ;i++ ) {
            for (int j=0;j<up ;j++ ) {
                output[i][j]=value;
            }
        }
        // down
        for (int i=0;i<nx ;i++ ) {
            for (int j=up+ly-1;j<ny ;j++ ) {
                output[i][j]=value;
            }
        }
        return output;
    }



















































































    public static JSONObject numarr_situation(int[] num){
        /*
        // ranking it
        int[] rankingnum=new int[l];
        int[] rankingindex=new int[l];
        for (int i=0;i<l ;i++ ) {
            rankingnum[i]=num[i];
            rankingindex[i]=i;
        }
        int internum=0; int interindex=0;
        for (int i=0;i<l;i++ ) {
            for (int j=i+1;j<l ;j++ ) {
                if (rankingnum[i]<rankingnum[j]) { // from maximum to minimum
                    internum=rankingnum[i];
                    rankingnum[i]=rankingnum[j];
                    rankingnum[j]=internum;

                    interindex=rankingindex[i];
                    rankingindex[i]=rankingindex[j];
                    rankingindex[j]=interindex;
                }
            }
        }*/

        /*
        //calculate the diff
        int[] diff=new int[l+1];
        diff[0]=num[0];
        diff[l]= -num[l-1];
        int prenum=num[0];
        for (int i=1;i<l;i++ ) {
            diff[i]=num[i]-prenum;
            prenum=num[i];
        }*/

        // calculate the integral, not include the end
        int l=num.length;
        int[] integral=new int[l+1];
        int thissum=0;
        for (int i=0;i<l ;i++ ) {
            integral[i]=thissum;
            thissum+=num[i];
        }
        integral[l]=thissum; // last one

        // analysis the situation
        JSONObject situation=new JSONObject();
        // dominant several colors:
        double concentrateratio=0.05;
        double validratio=0.2;
        int width=(int)(l*concentrateratio);
        int validsum=(int)(integral[l-1]*validratio);
        double times=2;
        // 可以作为dominant的判断方法， 却不能用于判断一般的ridge！！！！！！！！！！！！
        // times ????   subtracted the main ridge ??????????????
        int validnum=((int)(integral[l]*times))/l; System.out.println(validnum);
        //integral[concentratewidth]>validsum
        //thissum>validsum
        JSONArray colorlist=new JSONArray();
        JSONObject newcolor;
        int pre=-1;
        int after;
        boolean exist=false;
        boolean dominant=false;
        for (int i=0;i<l ;i++ ) {
            if(num[i]>validnum){// a dominant color
                if (!exist) {
                    pre=i;
                    exist=true;
                }
            }else if(exist){
                newcolor=new JSONObject();colorlist.put(newcolor);
                newcolor.put("s",pre);
                newcolor.put("e",i); // e not included
                newcolor.put("value",integral[i]-integral[pre]);
                //System.out.println(pre+"    "+i);
                exist=false;
            }
        }
        // last one
        if (exist) {
            newcolor=new JSONObject();colorlist.put(newcolor);
            newcolor.put("s",pre);
            newcolor.put("e",l); // l not included
            newcolor.put("value",integral[l]-integral[pre]);
            //System.out.println(pre+"    "+(l-1));
            exist=false;
        }
        if(colorlist.length()>0){ // there is at least one dominat color
            situation.put("type","dominant");
            situation.put("colorlist",colorlist);
        }else{
            situation.put("type","undefined");
        }

        return situation;
    }






    // not equal to exactly lx*ly, because of the averaging
    public static int[] colorarr_spectrum1(int[][][] color,int which,int div,boolean[][] meet){
        int[] spectrum=new int[256/div];
        int lx=color.length;
        int ly=color[0].length;
        int[] thiscolor;
        int difference=0;
        for (int i=0;i<lx;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                if(meet[i][j]) {
                    thiscolor = color[i][j];
                    //System.out.println(spectrum.length+"____"+thiscolor[which]+"___"+which);
                    spectrum[thiscolor[which] / div]++;
                }
            }
        }
        int l=spectrum.length;
        // averaging to cover some strange points
        int[] averagespectrum=new int[l];
        int averagel=0; int thisindex=0; int averagesum=0;int averagenum=averagel*2+1;
        for (int i=0;i<l;i++ ) {
            averagesum=0;
            for (int j=i-averagel,je=i+averagel;j<=je ;j++ ) {
                thisindex=j;
                if (j<0)
                    thisindex=0;
                if (j>=l)
                    thisindex=l-1;
                averagesum+=spectrum[thisindex];
            }
            averagespectrum[i]=averagesum/averagenum;
        }

        for (int i=0;i<l ;i++ ) {
            System.out.println(averagespectrum[i]);
        }
        System.out.println();
        System.out.println();
//        System.out.println();
        return averagespectrum;
    }
    public static int[] colorarr_spectrum1(int[][][] color,int which,int div){
        int lx=color.length;
        int ly=color[0].length;
        boolean[][] meet=new boolean[lx][ly];
        for (int i=0;i<lx ;i++ ) {
            for (int j=0;j<ly;j++ ) {
                meet[i][j]=true;
            }
        }
        return colorarr_spectrum1(color,which,div,meet);
    }
    public static int[] colorarr_spectrum1(int[][][] color,int which){
        return colorarr_spectrum1(color,which,1);
    }


    public static JSONObject situation_combinechunk(JSONObject situation, int limit) {
        // combine some situation, if the gap is less than limit....
        JSONArray array = situation.getJSONArray("colorlist");
        int[] rangechunk = new int[256 + 1];
        //int[] chunkvalue=new int[256+1]; // 暂不处理，等以后需要了，再说
        for (int i = 0, l = array.length(); i < l; i++) {
            rangechunk[array.getJSONObject(i).getInt("s")] = 1;
            rangechunk[array.getJSONObject(i).getInt("e")] = -1;
            //chunkvalue[array.getJSONObject(i).getInt("s")]=i;
        }
        // combine
        boolean ifcombined = false;
        int preend = -1;
        for (int i = 0, l = rangechunk.length; i < l; i++)
            if (rangechunk[i] != 0) {
                if ((rangechunk[i] == -1) && (preend == -1)) { //find the end && not already an end
                    preend = i;
                } else if ((rangechunk[i] == 1) && (preend != -1)) {
                    // find the start && already an end---try to combine
                    if (i - preend <= limit) { // combine, remove the end and the start
                        rangechunk[preend] = 0;
                        rangechunk[i] = 0;
                        ifcombined = true;
                    }
                    preend = -1;
                }
            }

        // if combination happened, reconstruct the situation
        if (ifcombined) {
            JSONArray newcolorlist = new JSONArray();
            JSONObject colorobj = null;
            int prestart = -1;
            for (int i = 0, l = rangechunk.length; i < l; i++)
                if ((prestart == -1) && (rangechunk[i] == 1)) //-1 no start--->to find a start
                    prestart = i;
                else if ((prestart != -1) && (rangechunk[i] == -1)) { // find an end--->new color added
                    colorobj = new JSONObject();
                    newcolorlist.put(colorobj);
                    colorobj.put("s", prestart);
                    colorobj.put("e", i);
                    prestart=-1; //become no start
                }
            situation.put("colorlist", newcolorlist);
        }

        return situation;
    }

    public static int[][] colorarr_colorindex(int[][][] color,int which){
        int[] spectrum=colorarr_spectrum1(color,which);
        JSONObject situation=numarr_situation(spectrum);
        situation=situation_combinechunk(situation,5);
        System.out.println(situation.toString(2));
        return colorarr_colorindex(color,which,situation);
    }


    // get the colorindex(from situation colorlist) of each color unit
    public static int[][] colorarr_colorindex(int[][][] color,int which,JSONObject situation){
        int lx=color.length;
        int ly=color[0].length;

        int[][] colorindex=new int[lx][ly];
        for (int i=0;i<lx ;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                colorindex[i][j]=-1;
            }
        }

        if (!situation.getString("type").equals("dominant")) {
            return colorindex; // no need to consider, return
        }

        JSONArray colorlist=situation.getJSONArray("colorlist");
        int l=colorlist.length();
        int range1=0, range2=0;
        for (int k=0;k<l ;k++ ) {
            // the tolerance should be bigger....
            // the differentiation between white and other color should be accomplished by saturation
            // but also should be adjustable !!! for example :  20 difference is also recognizable with scrutiny
            range1=colorlist.getJSONObject(k).getInt("s")-30;
            range2=colorlist.getJSONObject(k).getInt("e")+30;
            for (int i=0;i<lx ;i++ )
                for (int j=0;j<ly ;j++ )
                    if(colorindex[i][j]==-1) // not allocated
                        if((range1<=color[i][j][which])&&(color[i][j][which]<range2))
                            colorindex[i][j]=k;
        }

        return colorindex;
    }


    // colorarr's  color spectrum classification analysis
    public static JSONObject colorarr_colorsituation(int[][][] color){
        int lx=color.length;
        int ly=color[0].length;
        int dimension=3; // note: do not use =color[0][0].length; it is actually 4, including the transparency

        int[] spectrum=null;
        JSONObject thissituation=null;
        JSONObject root=null;

        int[][][] chunkindex=new int[4][lx][ly]; int[][] thischunkindex=new int[lx][ly];
        for (int k=0;k<3;k++)
            for (int i=0;i<lx ;i++ )
                for (int j=0;j<ly;j++)
                    chunkindex[k][i][j]=-1; //  -1 .... not allocated

        JSONArray colorlist=null;
        JSONObject[] situation=new JSONObject[1000]; situation[0]=new JSONObject();
        int[][] indexlist=new int[1000][10];
        int[] level=new int[1000]; level[0]=0; int thislevel=0;
        boolean[][] meet=new boolean[lx][ly];
        int s=1; // s--stacknum
        while (s>0) {
            s--;
            // 选取meet
            thislevel=level[s];
            for (int i=0;i<lx ;i++ )
                for (int j=0;j<ly ;j++ )
                    meet[i][j]=true;
            if(thislevel>0){ // with pre
                for (int k=0;k<thislevel;k++ ) {
                    thischunkindex=chunkindex[k];
                    for (int i = 0; i < lx; i++)
                        for (int j = 0; j < ly; j++)
                            if (meet[i][j])
                                if (thischunkindex[i][j]!=indexlist[s][k])
                                    meet[i][j]=false;
                }
            }
            // spectrum
            spectrum=colorarr_spectrum1(color,thislevel,1,meet);

            // 归类处理situation
            thissituation=numarr_situation(spectrum);
            //... add to the main object
            situation[s].put("type",thissituation.getString("type"));
            if(thissituation.getString("type").equals("dominant"))
                situation[s].put("colorlist",thissituation.getJSONArray("colorlist"));
            if(thislevel==0)
                root=thissituation;

            // 可以处理，有结论dominant
            if(thissituation.getString("type").equals("dominant")&&(thislevel+1<dimension)) {//thislevel: 0, 1
                colorlist=thissituation.getJSONArray("colorlist");
                // 分组chunkindex....meet继续使用
                thischunkindex=chunkindex[thislevel]; // +1 --- overflow ????
                for (int listi=0,l=colorlist.length();listi<l ;listi++ ) {
                    int range1=colorlist.getJSONObject(listi).getInt("s");
                    int range2=colorlist.getJSONObject(listi).getInt("e");
                    for (int i = 0; i < lx; i++)
                        for (int j = 0; j < ly; j++)
                            if (meet[i][j]) { //meet previous ones
                                if (thischunkindex[i][j] == -1) // not allocated
                                    if ((range1 <= color[i][j][thislevel]) && (color[i][j][thislevel] < range2))
                                        thischunkindex[i][j]=listi;
                            }
                }
                //num2arr_print(thischunkindex);
                // dominant 展开son added to stack.... situation, level, indexlist
                int olds=s;
                int oldlevel=thislevel;
                int[] oldindexlist=new int[1000];
                for (int th=0;th<thislevel ;th++ ) // copy the old
                    oldindexlist[th]=indexlist[olds][th];
                for (int listi=0,l=colorlist.length();listi<l ;listi++ ) {
                    // situation
                    situation[s]=colorlist.getJSONObject(listi);
                    // level
                    level[s]=oldlevel+1;
                    // indexlist
                    for (int th=0;th<oldlevel ;th++ )
                        indexlist[s][th]=oldindexlist[th];
                    indexlist[s][oldlevel]=listi;
                    s++;
                }
            }
        }

        System.out.println(root.toString(2));
        return root;



        /*
        boolean[][] meet=null;
        if (colorsituation.getString("type").equals("dominant")) {
            int chunknum=colorsituation.getJSONArray("colorlist").length();
            JSONArray thisarray=colorsituation.getJSONArray("colorlist");
            JSONObject thiscolor=null;
            JSONObject thissituation=null;
            for (int i=0;i<chunknum ;i++ ) {
                thiscolor=thisarray.getJSONObject(i);
                meet=range_boolarr(new int[][]{{thiscolor.getInt("s"),thiscolor.getInt("e")}},color);
                spectrum=colorarr_spectrum1(color,1,1,meet);
                thissituation=numarr_situation(spectrum);
                thiscolor.put("type",thissituation.getString("type"));
                thiscolor.put("colorlist",thissituation.getJSONArray("colorlist"));

                if (thissituation.getString("type").equals("dominant")) {
                    int chunknum2=colorsituation.getJSONArray("colorlist");
                    JSONArray thisarray2=thiscolor.getJSONArray("colorlist");
                    JSONObject thiscolor2=null;
                    JSONObject thissituation2=null;
                    for (int j=0;j< ; ) {

                    }
                }
            }
        }*/
    }

    public static void num2arr_print(int[][] num){
        int lx=num.length;
        int ly=num[0].length;
        for (int j=0;j<ly ;j++ ) {
            System.out.println();
            for (int i=0;i<lx ;i++ )
                System.out.print(num[i][j]+"\t");
        }
        System.out.println();
    }


    // select the color unit that belongs to the specific colorlist index
    public static int[][][] colorarr_show_indexcolor(int[][][] color,int[][] indexcolor,int index) throws IOException {
        int lx=color.length;
        int ly=color[0].length;
        int[][][] newcolor=new int[lx][ly][3];
        for (int i=0;i<lx ;i++ )
            for(int j=0;j<ly;j++)
                if(indexcolor[i][j]==index)
                    for (int k=0;k<3 ;k++ ) {
                        newcolor[i][j][k]=color[i][j][k];
                    }

        colorarr_show(newcolor);
        return newcolor;
    }

    public static int[][][] colorarr_show_indexcolorarr(int[][][] color,int[][][] indexcolor,int index[]) throws IOException {
        int lx = color.length;
        int ly = color[0].length;
        int indexl = indexcolor.length;

        // select the selected color
        boolean[][] meet = new boolean[lx][ly];
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                meet[i][j] = true;
        int[][] thisindexcolor = null;
        int thisindex = -1;
        for (int indexi = 0; indexi < indexl; indexi++) {
            thisindexcolor = indexcolor[indexi];
            thisindex = index[indexi];
            for (int i = 0; i < lx; i++)
                for (int j = 0; j < ly; j++)
                    if ((meet[i][j]) && (thisindexcolor[i][j] != thisindex))
                        meet[i][j] = false;
        }

        // construct the newcolor according to the meet[][]
        int[][][] newcolor = new int[lx][ly][3];
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                if (meet[i][j])
                    for (int k = 0; k < 3; k++)
                        newcolor[i][j][k] = color[i][j][k];

        colorarr_show(newcolor);
        return newcolor;
    }

    /*
    public static boolean[][] range_boolarr(int[][] range,int[][][] color){
        int lx=color.length;
        int ly=color[0].length;
        int rangel=range.length;
        boolean[][] meet=new boolean[lx][ly];

        boolean thismeet=true;
        for (int x=0;x<lx ;x++ ) {
            for (int y=0;y<ly ;y++ ) {
                thismeet=true;// meet---true
                for (int k=0;k<rangel ;k++ ) {
                    if(!((range[k][0]<=color[x][y][0])&&(color[x][y][0]<range[k][1]))){
                        thismeet=false;
                        break;
                    }
                }
                meet[x][y]=thismeet;
            }
        }

        return meet;
    }*/







    // get the color spectrum of the image
    public static int[][][] colorarr_spectrum3(int[][][] color,int div){
        int[][][] spectrum=new int[256/div][256/div][256/div];
        int lx=color.length;
        int ly=color[0].length;
        int[] thiscolor;
        int difference=0;
        for (int i=0;i<lx;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                thiscolor=color[i][j];
                spectrum[thiscolor[0]/div][thiscolor[1]/div][thiscolor[2]/div]++;
            }
        }

        return spectrum;
    }
    public static int[][][] colorarr_spectrum3(int[][][] color){
        return colorarr_spectrum3(color,2);
    }



























    public static int[] color_bground_chunkextension(int[][][] color,int step,int limit){
        int lx=color.length;
        int ly=color[0].length;
        int newx=lx/step;
        int newy=ly/step;
        int[][] colorindex=new int[newx][newy];
        for (int i=0;i<newx ;i++ ) {
            for (int j=0;j<newy ;j++ ) {
            }
        }

        return new int[3];
    }



    public static int[] color_most_trysome(int[][][] color,int limit,double validratio){
        int lx=color.length; int ly=color[0].length;

        ArrayList<int[]> colorlist=new ArrayList<>();
        ArrayList<Integer> colornum=new ArrayList<>();
        int difference=0;
        boolean ifbelong=false;
        int[] thisrefer;
        int[] thiscolor;
        for (int i=lx;i<lx ;i++ ) {
            for (int j=ly;j<ly ;j++ ) {
                thiscolor=color[i][j];
                // compared with each existed color
                ifbelong=false;
                for (int colori=0,colorl=colorlist.size();colori<colorl;colori++ ) {
                    thisrefer=colorlist.get(colori);
                    difference=0;
                    for (int k=0;k<3 ;k++ )
                        difference+=Math.abs(thiscolor[k]-thisrefer[k]);
                    if(limit>difference){// belong to some color
                        ifbelong=true;
                        //colornum.set(colori,colornum.get(colori)+1);
                        break;
                    }/*else if(limit>difference) { // not belong to some color, but can be counted as some color
                        numforcolor.set(colori,numforcolor.get(colori)+1);
                    }*/
                }
                // find new color... not belong to any color
                if(!ifbelong){
                    colorlist.add(thiscolor);
                    //colornum.add(1);
                }
            }
        }

        return new int[3];
    }
/*
    // the points with the color near the reference color, select and collect these point,
    // then calculate the average color of thse points to make more accurate the reference color
    public static int[] color_most_nearrefer(int[][][] color, int[] refercolor, int limit,double validratio){
        int lx=color.length;
        int ly=color[0].length;
        int[] thiscolor;
        int[] sectionnum=new int[9];
        int[] differsingle=new int[3];
        int[] uplow=new int[3];
        int difference=0;
        for (int i=0;i<lx;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                thiscolor=color[i][j];
                difference=0;
                for (int k=0;k<3 ;k++ ) {
                    differsingle[k]=thiscolor[k]-refercolor[k];
                    if (differsingle[k]<0) {
                        difference-=differsingle[k];
                        uplow[k]=0;
                    } else{
                        difference+=differsingle[k];
                        uplow[k]=1;
                    }
                }
                if(difference<limit){
                    sectionnum[uplow[0]*4+uplow[1]*2+uplow[2]]++;
                }
                if (difference<(limit/2)) {
                    sectionnum[8]++;
                }
            }
        }


        int sum=lx*ly;
        JSONObject analysisresult=numarr_situation(sectionnum,sum,validratio);
        switch (analysisresult.getString("type")){
            case "dominant":
                int index=analysisresult.getInt("index");
                ///////????????
                break;
            case "comparable":
                break;
            case "maximum":
                break;
            case "blending":
                break;
        }

        return new int[3];
    }

 */

    // if the number of the points of some color exceed the ratio(eg. 0.5), consider it as the most color
    // statis may meet some problem, because some grid may divide some color
    public static int[] color_most_statis(int[][][] color, double ratio){
        int lx=color.length;
        int ly=color[0].length;
        int[][][] colornum=new int[32][32][32];
        int[] thiscolor;
        int x,y,z;
        for (int i=0;i<lx;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                thiscolor=color[i][j];
                colornum[thiscolor[0]/8][thiscolor[1]/8][thiscolor[2]/8]++;
            }
        }

        int sum=lx*ly;
        int num=(int)(sum*ratio);

        return new int[3];
    }


    public static int[][][] colorarr_conspic_diffbackg(int[][][] color,int[] background, int threshold){
        int lx=color.length;
        int ly=color[0].length;
        int[][][] newcolor=new int[lx][ly][3];

        return newcolor;
    }























    /*
    // in fact, the color division is not by spectrum analyzing, but by color chunk
    // for example, the blending color or strip or patten is not regarded as the background in our mind
    public static JSONObject numarr_situation(int[] num,int sum, double validratio){
        // the maximum
        int max=0; int maxindex=-1;
        for (int i=0,l=num.length;i<l;i++ ) {
            if(max<num[i]) {
                max = num[i];
                maxindex=i;
            }
        }

        // analyzing the result !!!
        JSONObject result=new JSONObject();
        double dominantratio=0.5;
        int propernum=(int)(sum*dominantratio);
        // dominant single point....including the center one
        if(max>propernum){
            result.put("type","dominant");
            result.put("index",maxindex);
            result.put("value",max);
            return result;
        } else if(max>((int)(sum*validratio))){ // if it is valid, not only the blending, no real color ??
            // no one dominant
            double ratiowithmax=0.5;
            propernum=(int)(max*ratiowithmax);
            int comparablenum=0; // subtracted itself
            JSONArray comparableindex=new JSONArray();
            JSONArray comparablevalue=new JSONArray();
            for (int i=0,l=num.length;i<l ;i++ ) {
                if((i!=maxindex)&&(num[i]>propernum)) {
                    comparableindex.put(i);
                    comparablevalue.put(num[i]);
                    propernum++;
                }
            }
            // several comparable
            if (comparablenum>0) {
                result.put("type","comparable");
                result.put("index",comparableindex);
                result.put("value",comparablevalue);
            }else{// no comparable
                result.put("type","maximum");
                result.put("index",maxindex);
                result.put("value",max);
            }
            return result;
        } else{ // no valid color, only blending color
            result.put("type","blending");
            return result;
        }
    }
    */























    public static int[][][] experiment_color(int div) {
        int[][][] color = new int[300][300][];
        for (int i = 0; i < color.length; i++)
            for (int j = 0; j < color[i].length; j++) {
                switch ((i / div + j / div) % 3) {
                    case 0:
                        color[i][j] = new int[]{255, 0, 0};
                        break;
                    case 1:
                        color[i][j] = new int[]{0, 255, 0};
                        break;
                    case 2:
                        color[i][j] = new int[]{0, 0, 255};
                        break;
                }
            }
        colorarr_path(color,"D:\\computer data\\experiment"+div+".png");
        return color;
    }

    public static int[][][] blendexperiment_color() {
        int[][][] color = new int[300][300][];
        for (int i = 0; i < color.length; i++)
            for (int j = 0; j < color[i].length; j++) {
                color[i][j] = new int[]{85, 85, 85};
            }
        return color;
    }


    // setting some basic constant of the image processing
    static String imagepath = null;   // the path of an image
    static String imagedir = "D:\\test";    // the directory name of an image
    static String imagename = null;   // the name of an image
    static String imageexten = null;  // the extension name of an image
    static int[][][] colorimage = null;

    public static void filedeal(String path) {
        imagepath = path;
        File file = new File(path);
        imagedir = file.getParent();
        imagename = file.getName();
        imageexten = imagename.substring(imagename.lastIndexOf(".") + 1);
        imagename = imagename.substring(0, imagename.lastIndexOf("."));
        System.out.println(imagepath);
        System.out.println(imagedir);
        System.out.println(imagename);
        System.out.println(imageexten);
    }


    // transform:

    public static double[][][] singlearr_magnanglearr(int[][] single) throws IOException {
        double[][][] derivxy = singlearr_derivxyarr(single);
        double[][][] magangle = derivxyarr_magnanglearr(derivxy);
        double[][][] magnangleresult = new double[][][]{magangle[0], magangle[1], derivxy[0], derivxy[1]};

        double[][] magnitude = magnangleresult[0];
        double[][] angle = magnangleresult[1];
        double[][] deriv_x = magnangleresult[2];
        double[][] deriv_y = magnangleresult[3];
        matcharr_show(deriv_x, -255.0 / 2.0, 255.0 / 2.0);
        matcharr_show(deriv_y, -255.0 / 2.0, 255.0 / 2.0);
        matcharr_show(magnitude, 0, 255 / 4.0 * Math.sqrt(2));
        matcharr_show(angle, -180.0, 180.0);

        return magnangleresult;
    }

    public static double[][][] derivxyarr_magnanglearr(double[][][] devxy) {
        double[][] devx = devxy[0];
        double[][] devy = devxy[1];
        int lx = devx.length;
        int ly = devx[0].length;
        double[][] magnitude = new double[lx][ly];
        double[][] angle = new double[lx][ly];
        double thisx = 0;
        double thisy = 0;
        double pi = Math.PI;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                thisx = devx[i][j];
                thisy = devy[i][j];
                magnitude[i][j] = Math.sqrt(thisx * thisx + thisy * thisy);
                angle[i][j] = Math.atan2(thisy, thisx) / pi * 180.0;
            }
        }

        return new double[][][]{magnitude, angle};
    }

    public static double[][][] singlearr_derivxyarr(int[][] single) {
        int lx = single.length;
        int ly = single[0].length;
        double[][] sobel_x = filter_sobel_x();
        double[][] sobel_y = filter_revert(sobel_x);
        double[][] devx = singlearr_filter_notedge(single, sobel_x);
        double[][] devy = singlearr_filter_notedge(single, sobel_y);

        return new double[][][]{devx, devy};
    }

    public static double[][] filter_sobel_x() {
        double[][] filter = new double[][]{{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        double[][] newfilter = filter_multi(filter, 0.125);
        return newfilter;
    }

    public static double[][] filter_prewitt_x() { //??? multiplied by???
        double[][] filter = new double[][]{{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};
        double[][] newfilter = filter_multi(filter, 1 / 6.0);
        return newfilter;
    }

    public static double[][] filter_robert_x() { // ??? multiplied by???
        double[][] filter = new double[][]{{0, -1}, {1, 0}};
        double[][] newfilter = filter_multi(filter, 1 / 2.0);
        return newfilter;
    }

    public static double[][] singlearr_filter_notedge(int[][] single, double[][] filter) {
        int lx = single.length;
        int ly = single[0].length;
        int fx = filter.length;
        int fy = filter[0].length;
        int rx = fx / 2;
        int ry = fy / 2;
        //int filtersum=fx*fy;
        //double[][][] result=new double[lx][ly][]; double red,green,blue; int[] thiscolor=null;
        double[][] result = new double[lx][ly];
        double thissingle = 0;
        double singlesum = 0;
        double filtervalue = 0;
        for (int i = rx, il = lx - fx; i < il; i++)
            for (int j = ry, jl = ly - fy; j < jl; j++) {
                //red=0;green=0;blue=0;
                singlesum = 0.0;
                for (int ti = -rx; ti <= rx; ti++)
                    for (int tj = -ry; tj <= ry; tj++) {
                        filtervalue = filter[ti + rx][tj + ry];
                        thissingle = single[i + ti][j + tj];
                        singlesum += filtervalue * thissingle;
                        // thiscolor=single[i+ti][j+tj]; red+=filtervalue*thiscolor[0]; green+=filtervalue*thiscolor[1]; blue+=filtervalue*thiscolor[2];
                    }
                //result[i][j]=new double[]{red/filtersum,green/filtersum,blue/filtersum};
                result[i][j] = singlesum;///filtersum; // averaging is redundant
            }
        return result;
    }

    public static double[][] filter_multi(double[][] filter, double multi) {
        int lx = filter.length;
        int ly = filter[0].length;
        double[][] newfilter = new double[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                newfilter[i][j] = multi * filter[i][j];
            }
        }
        return newfilter;
    }

    public static double[][] filter_revert(double[][] filter) {
        int lx = filter.length;
        int ly = filter[0].length;
        double[][] revert = new double[ly][lx];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                revert[j][i] = filter[i][j];
            }
        }
        return revert;
    }


    // find the max value of the matcharr
    public static int[] matcharr_max(double[][] match) {
        int lx = match.length;
        int ly = match[0].length;
        double max = -2;
        int x = -1;
        int y = -1;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                //System.out.print(match[i][j]+"\t");
                if (max < match[i][j]) {
                    max = match[i][j];
                    x = i;
                    y = j;
                }
                if (max < -match[i][j]) {
                    max = -match[i][j];
                    x = i;
                    y = j;
                }
            }
            //System.out.println();
        }
        //System.out.println(lx + "___" + ly + "___:");
        //System.out.println("trymatch___"+x + "___" + y + "___" + max);
        return new int[]{x, y, (int) (max * 10000)};
    }

    // find the identically matched picture;  search the rectangular region(x1,y1)--(x2,y2)
    // maybe[i][j]==false-->skip it, not search !!!
    public static double[][] colorarr_matcharr(int[][][] color, int[][][] template, int x1, int y1, int x2, int y2, boolean[][] maybe) { // matcharr range: [-1,1]
        // https://blog.csdn.net/weixin_42104289/article/details/83088043
        //int lx=color.length;
        //int ly=color[0].length;
        int tx = template.length;
        int ty = template[0].length;

        // deviation, mean of template
        double tem_mean = colorarr_mean(template);
        double tem_dev = colorarr_dev(template, tem_mean);

        double[][] match = new double[color.length - template.length][color[0].length - template[0].length];
        double sumcorrelate = 0.0; // 这里特别要注意，设为int不会报错，但会自动近似，导致结果非常不准确
        int[] thiscolor = null;
        int[] thistemplate = null;
        double this_mean = 0;
        double this_dev = 0;
        for (int i = x1, ix = x2; i < ix; i++) {
            //System.out.println(i);
            for (int j = y1, jy = y2; j < jy; j++)
                if (maybe[i][j]) {
                    sumcorrelate = 0.0;
                    this_mean = colorarr_mean(color, i, i + tx, j, j + ty);
                    this_dev = colorarr_dev(color, this_mean, i, i + tx, j, j + ty);
                    for (int ti = 0; ti < tx; ti++)
                        for (int tj = 0; tj < ty; tj++) {
                            thistemplate = template[ti][tj];
                            thiscolor = color[i + ti][j + tj];
                            for (int k = 0; k < 3; k++) {
                                sumcorrelate += (thiscolor[k] - this_mean) * (thistemplate[k] - tem_mean);
                            }
                        }
                    match[i][j] = sumcorrelate / (tx * ty * 3.0 * this_dev * tem_dev); // match must be divided by tx*ty*3; corresponding to the averaging in convolution
                    if ((match[i][j] > 1.05) || (match[i][j] < -1.05)) {
                        //sumcorrelate<0
                        System.out.println(tem_mean + "___" + tem_dev);
                        System.out.println(this_mean + "__" + this_dev);
                        System.out.println(tx * ty * this_dev * tem_dev);
                        System.out.println(sumcorrelate);
                        System.out.println("---> " + match[i][j]);
                    }
                }
        }
        return match;
    }

    public static double[][] colorarr_matcharr(int[][][] color, int[][][] template, int x1, int y1, int x2, int y2) {
        boolean[][] maybeall = new boolean[x2][y2];
        for (int i = x1; i < x2; i++)
            for (int j = y1; j < y2; j++)
                maybeall[i][j] = true;
        return colorarr_matcharr(color, template, x1, y1, x2, y2, maybeall);
    }

    // search for match near (x,y)
    public static double[][] colorarr_matcharr(int[][][] color, int[][][] template, int x, int y) { // matcharr range: [-1,1]
        int x1 = x - template.length / 2;
        if (x1 < 0) x1 = 0;
        int y1 = y - template[0].length / 2;
        if (y1 < 0) y1 = 0;
        int x2 = x + template.length / 2;
        if (x2 > color.length) x2 = color.length;
        int y2 = y + template[0].length / 2;
        if (y2 > color[0].length) y2 = color[0].length;
        return colorarr_matcharr(color, template, x1, y1, x2, y2);
    }

    // search the entire image
    public static double[][] colorarr_matcharr(int[][][] color, int[][][] template) { // matcharr range: [-1,1]
        int x2 = color.length - template.length;
        int y2 = color[0].length - template[0].length;
        return colorarr_matcharr(color, template, 0, 0, x2, y2);
    }

    public static double[][] colorarr_matcharr(int[][][] color, int[][][] template, boolean[][] maybe) {
        int x2 = color.length - template.length;
        int y2 = color[0].length - template[0].length;
        return colorarr_matcharr(color, template, 0, 0, x2, y2, maybe);
    }

    public static double[][] colorarr_matcharr_part(int[][][] color, int[][][] template,int x1,int y1,int x2,int y2){
        boolean[][] originrange=new boolean[color.length][color[0].length];
        for (int i=x1;i<x2 ;i++ ) {
            for (int j=y1;j<y2 ;j++ ) {
                originrange[i][j]=true;
            }
        }
        return colorarr_matcharr_part(color,template,originrange);
    }
    public static double[][] colorarr_matcharr_part(int[][][] color, int[][][] template){
        boolean[][] originrange=new boolean[color.length][color[0].length];
        for (int i=0,il=originrange.length;i<il ;i++ ) {
            for (int j=0,jl=originrange[0].length;j<jl ;j++ ) {
                originrange[i][j]=true;
            }
        }
        return colorarr_matcharr_part(color,template,originrange);
    }
    // sometimes, the software update which makes the icon not the same any more.
    // like android emulator updatad .... very annoying, bothering
    // when it comes to the character recognition, it go wrong
    public static double[][] colorarr_matcharr_part(int[][][] color, int[][][] template, boolean[][] originrange) {
        // find the gradual-level feature pattern of template
        int sidel = 2;
        int tx = template.length;
        int ty = template[0].length;
        int tl = Math.min(tx, ty);
        double max = 0;
        double dev = 0, mean = 0;
        int thei = -1, thej = -1;
        int[] tempii = new int[20];
        int[] tempjj = new int[20];
        int[][][][] piece = new int[20][][][];
        int times = 0;
        while (sidel < tl) {
            max = 0;
            for (int i = 0, il = tx - sidel; i < il; i++)
                for (int j = 0, jl = ty - sidel; j < jl; j++) {
                    mean = colorarr_mean(template, i, i + sidel, j, j + sidel);
                    dev = colorarr_dev(template, mean, i, i + sidel, j, j + sidel);
                    if ((dev > 0) && (max < dev)) { // max<-dev ?????
                        max = dev;
                        thei = i;
                        thej = j;
                    } else if ((dev < 0) && (max < -dev)) {
                        max = -dev;
                        thei = i;
                        thej = j;
                    }
                }
            // get teh piece of template
            piece[times] = new int[sidel][sidel][];
            for (int i = 0; i < sidel; i++)
                for (int j = 0; j < sidel; j++)
                    piece[times][i][j] = template[thei + i][thej + j];
            tempii[times] = thei;
            tempjj[times] = thej;
            sidel *= 2;
            times++;
        }
        // put the whole as the last musk
        piece[times] = template;
        tempii[times] = 0;
        tempjj[times] = 0;
        times++;

        // gradually match the colorarr with piece of template
        boolean[][][] maybe = new boolean[times + 2][][];
        double[][] match = null;
        for (int t = 0; t < times; t++) {
            maybe[t] = new boolean[color.length - piece[t].length+1][color[0].length - piece[t][0].length+1];
            if (t == 0) { // the first maybe... all are true, possible
                boolean[][] thismaybe = maybe[0];
                for (int i = 0, il = thismaybe.length; i < il; i++)
                    for (int j = 0, jl = thismaybe[0].length; j < jl; j++) {
                        if (originrange[i][j])
                            thismaybe[i][j] = true;
                    }
            } else { // maybe[t]<---maybe[t-1]
                boolean[][] thismaybe = maybe[t];
                boolean[][] premaybe = maybe[t - 1];
                int dati = tempii[t - 1] - tempii[t];
                int datj = tempjj[t - 1] - tempjj[t];
                for (int i = 0, il = thismaybe.length, pil = premaybe.length; i < il; i++)
                    for (int j = 0, jl = thismaybe[0].length, pjl = premaybe[0].length; j < jl; j++) {
                        if ((i + dati >= 0) && (i + dati < pil) && (j + datj >= 0) && (j + datj < pjl))
                            if (premaybe[i + dati][j + datj])
                                thismaybe[i][j] = true;
                    }
            }
            match = colorarr_matcharr(color, piece[t], maybe[t]);
            maybe[t] = doublarr_boolarr(match, 0.98, maybe[t]);
            /*
            System.out.println(t+"____");
            colorarr_path(piece[t],"D:\\computer data\\piece_"+t+".png");
            int[] result=matcharr_max(match);
            System.out.println((result[0]+tempii[t])+"____"+(result[1]+tempjj[t])+"___"+result[2]);
            */

            if (t == -1) {
                System.out.println(tempii[t] + "___" + tempjj[t]);
                boolean[][] thismaybe = maybe[t];
                for (int j = 0, jl = thismaybe[0].length; j < jl; j++) {
                    for (int i = 0, il = thismaybe.length; i < il; i++)
                        System.out.print(thismaybe[i][j] + "\t");
                    System.out.println();
                }
                System.out.println();
            }
        }
        return match;
    }

    // search the match gradually, binarily
    public static double[][] colorarr_matcharr_binary(int[][][] color, int[][][] template) {
        int lx = color.length;
        int ly = color[0].length;
        int tx = template.length;
        int ty = template[0].length;
        int time_x = int_times(tx);
        int time_y = int_times(ty);
        System.out.println(time_x + "__" + time_y);
        int[][][][] colorgrad = colorarr_small_squreaverage_gradual(color, time_x, time_y);
        int[][][][] tempgrad = colorarr_small_squreaverage_gradual(template, time_x, time_y);

        /*
        for (int i=0,l=Math.max(time_x,time_y)+1;i<l ;i++ ) {
            colorarr_path(colorgrad[i],"D:\\computer data\\color_"+i+".png");
            colorarr_path(tempgrad[i],"D:\\computer data\\template2_"+i+".png");
        }*/

        // the corresponding x,y times for each i
        int times_l = Math.max(time_x, time_y) + 1;
        int[] i_x_times = new int[times_l];
        i_x_times[0] = 1;
        int[] i_y_times = new int[times_l];
        i_y_times[0] = 1;
        for (int i = 1; i < times_l; i++) {
            if (i > time_x)
                i_x_times[i] = i_x_times[i - 1];
            else i_x_times[i] = i_x_times[i - 1] * 2;
            if (i > time_y)
                i_y_times[i] = i_y_times[i - 1];
            else i_y_times[i] = i_y_times[i - 1] * 2;
        }
        // search for the pattern matching gradually !!!
        boolean[][][] maybe = new boolean[times_l][][]; // maybe[x][y] means maybe (x,y) match....to match the template gradually
        maybe[times_l - 1] = new boolean[colorgrad[times_l - 1].length][colorgrad[times_l - 1][0].length];
        for (int i = 0, il = colorgrad[times_l - 1].length; i < il; i++)
            for (int j = 0, jl = colorgrad[times_l - 1][0].length; j < jl; j++)
                maybe[times_l - 1][i][j] = true; // the first one assume all the point are possible
        double threshold = 0.5;  // getting the threshold value by comparision with self !!!!
        double[][] match = null;
        for (int i = times_l - 1; i >= 4; i--) {
            threshold = timesxy_threshold(time_x, time_y, i);
            if (i < times_l - 1) // not the first one.... not both x and y are enlarged ???
                maybe[i] = boolarr_boolarr_bigger(maybe[i + 1], colorgrad[i].length, colorgrad[i][0].length);
            match = colorarr_matcharr(colorgrad[i], tempgrad[i], maybe[i]); // only maybe[i][j]=true, compute the match, otherwise match==0;

            maybe[i] = doublarr_boolarr(match, threshold, maybe[i]); //only maybe[i][j] will be testify

            /*
            if (i == 4) {
                for (int x = 0, llx = match.length; x < llx; x++) {
                    for (int y = 0, lly = match[0].length; y < lly; y++)
                        System.out.print(match[x][y] + "\t");
                    System.out.println();
                }
                for (int x = 0, llx = maybe[i].length; x < llx; x++) {
                    for (int y = 0, lly = maybe[i][0].length; y < lly; y++)
                        System.out.print(maybe[i][x][y] + "\t");
                    System.out.println();
                }
                System.out.println(threshold);
            }*/
        }
        return match;
    }

    /*
    public static double[] colorarr_threshold(int[][][] template,int times){
        int[][][][][] tempsituat=new int[times][][][][];
        tempsituat[0][0]=template;
        int x=;
    }*/

    public static double timesxy_threshold(int timex, int timey, int i) {
        int m = 0;
        int n = 0;
        if (i >= timex)
            m = 0;
        else m = timex - i;
        if (i >= timey)
            n = 0;
        else n = timey - i;
        double partial = (1.0 - 1.0 / (double) (Math.pow(2, m))) * (1.0 - 1.0 / (double) (Math.pow(2, n)));
        double threshold = 2 * partial - 1;
        //if (threshold>0.92) threshold=0.92; // error is too big because double have instinct error
        //threshold-=0.01;
        return threshold;
    }

    // correspond the smaller boolarr to bigger boolarr...
    // !!! not both x,y are resized, enlarged !
    public static boolean[][] boolarr_boolarr_bigger(boolean[][] bool, int bx, int by) {
        int lx = bool.length;
        int ly = bool[0].length;
        boolean[][] boolbig = new boolean[bx][by]; // *2+1 ??? coherent with the origin image
        /*int bx=boolbig.length;
        int by=boolbig[0].length;*/
        boolean xstill = bool.length == bx; // if x remain
        boolean ystill = bool[0].length == by; // if y remain
        System.out.println(xstill + "___" + ystill);
        int[] xnum;
        int[] ynum;
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                if (bool[i][j]) { //(i*2-1, i*2+2)*(j*2-1, j*2+2)...
                    if (xstill) xnum = new int[]{i};
                    else xnum = new int[]{i * 2 - 1, i * 2, i * 2 + 1, i * 2 + 2};
                    if (ystill) ynum = new int[]{j};
                    else ynum = new int[]{j * 2 - 1, j * 2, j * 2 + 1, j * 2 + 2};
                    for (int x = 0, xll = xnum.length; x < xll; x++)
                        if ((xnum[x] >= 0) && (xnum[x] < bx)) {
                            for (int y = 0, yll = ynum.length; y < yll; y++)
                                if ((ynum[y] >= 0) && (ynum[y] < by))
                                    boolbig[xnum[x]][ynum[y]] = true; // false is by default, needless
                        }

                    /*
                    for (int x = i * 2 - 1, xend = i * 2 + 2; x <= xend; x++)
                        if ((x >= 0) && (x < bx)) {
                            for (int y = j * 2 - 1, yend = j * 2 + 2; y <= yend; y++)
                                if ((y >= 0) && (y < by))
                                    boolbig[x][y] = true; // false is by default, needless
                        }
                    */
                }
        return boolbig;
    }

    // over the threshold ---> true;  considering maybe
    public static boolean[][] doublarr_boolarr(double[][] value, double threshol, boolean[][] maybe) {
        boolean[][] boolarr = new boolean[value.length][value[0].length];
        double threshold = threshol - 0.001;
        if (threshold > 0) {
            for (int i = 0, il = value.length; i < il; i++)
                for (int j = 0, jl = value[0].length; j < jl; j++)
                    if ((maybe[i][j]) && (value[i][j] > threshold) || (value[i][j] < -threshold))
                        boolarr[i][j] = true;
                    else boolarr[i][j] = false;
        } else { // since threshold <0, mean range: (-x,1), it will actually not exclude any points out. like
            for (int i = 0, il = value.length; i < il; i++)
                for (int j = 0, jl = value[0].length; j < jl; j++)
                    boolarr[i][j] = true;
        }
        return boolarr;
    }

    public static int int_times(int s) {
        int times = 0;
        int trys = s;
        while (trys != 0) {
            times++;
            trys /= 2;
        }
        times--;
        return times;
    }


    // meandev
    public static double[] colorarr_meandev(int[][][] color) {
        int lx = color.length;
        int ly = color[0].length;
        return colorarr_meandev(color, 0, lx, 0, ly);
    }

    public static double[] colorarr_meandev(int[][][] color, int xs, int xe, int ys, int ye) {
        double mean = colorarr_mean(color, xs, xe, ys, ye);
        double dev = colorarr_dev(color, mean, xs, xe, ys, ye);
        return new double[]{mean, dev};
    }

    // the standard deviation of the matrix
    public static double colorarr_dev(int[][][] color, double mean, int x1, int x2, int y1, int y2) {
        double devsum = 0;
        double thisdev = 0;
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                for (int k = 0; k < 3; k++) {
                    thisdev = (color[i][j][k] - mean);
                    devsum += thisdev * thisdev;
                }
            }
        }

        double dev = Math.sqrt(devsum / ((double) (x2 - x1) * (y2 - y1) * 3));
        return dev;
    }

    public static double colorarr_dev(int[][][] color, double mean) {
        int lx = color.length;
        int ly = color[0].length;
        return colorarr_dev(color, mean, 0, lx, 0, ly);
    }

    // the average of the matrix
    public static double colorarr_mean(int[][][] color, int x1, int x2, int y1, int y2) {
        double mean = 0;
        double sum = 0;
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                for (int k = 0; k < 3; k++) {
                    sum += color[i][j][k];
                }
            }
        }
        mean = sum / ((double) (x2 - x1) * (y2 - y1) * 3);

        return mean;
    }

    public static double colorarr_mean(int[][][] color) {
        int lx = color.length;
        int ly = color[0].length;
        return colorarr_mean(color, 0, lx, 0, ly);
    }


    // find the media of the array ????
    public static int[][] whitearr_median(int[][] white, int r) {
        int lx = white.length;
        int ly = white[0].length;
        int[][] newwhite = new int[lx][ly];
        //int[] array=null;
        int middlenum = (2 * r + 1) * (2 * r + 1) / 2;
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = r; i < lx - r; i++) { // r=1
            for (int j = r; j < ly - r; j++) { //
                list.clear();
                for (int tx = -r; tx <= r; tx++) { //
                    for (int ty = -r; ty <= r; ty++) { //
                        list.add(white[i + tx][j + ty]);
                    }
                }
                Collections.sort(list);
                newwhite[i][j] = list.get(middlenum); //
            }
        }

        return newwhite;
    }


    public static JSONArray boolarr_strockarr(boolean[][] show, int step, int near) {
        // 尝试用笔画的观点，解析痕迹，解析出笔画
        JSONArray strock = null;

        // get the total number
        int sum = 0;
        int lx = show.length;
        int ly = show[0].length;
        boolean[][] already = new boolean[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (show[i][j]) {
                    already[i][j] = true;
                    sum++;
                }
            }
        }

        int alreadysum = 0;
        boolean complete = false;
        do {
            // find the next start point
            int[] startpoint = boolarr_findone(already);
            int x = startpoint[0];
            int y = startpoint[1];

            // if get a start, start one strock
            //ArrayList<int[]> pointlist=new ArrayList<>();
            int prex = -1;// -900 means none
            int prey = -1;
            int predatx = -1;
            int predaty = -1;

            while ((x != -1) && (y != -1)) {
                // find the next
                if (prex != -1) { // limit by the former one
                    predatx = x - prex;
                    predaty = x - prey;
                } else {
                    predatx = 1;
                    predaty = 0;
                }

                // go around all the point near it, find the minimum angel point
                int[] next = boolarr_nextpoint(already, x, y, step, near, predatx, predaty);
                int nextx = next[0];
                int nexty = next[1];

                // add nearby points
                alreadysum += next[2];

                // update
                prex = x;
                prey = y;
                x = nextx;
                y = nexty;
            }

            if (Math.abs(alreadysum - sum) < (sum / 100)) ;
        } while (!complete);

        return strock;
    }

    public static void boolarr_strock(boolean[][] already, int x, int y, int step, int near) {
        int predatx = 0;
        int predaty = 1;
        int[] next = null;
        int thisx = x;
        int thisy = y;
        do {
            next = boolarr_nextpoint(already, thisx, thisy, step, near, predatx, predaty);
            predatx = next[0] - thisx;
            predaty = next[1] - thisy;
            thisx = next[0];
            thisy = next[1];
            String filepath = imagedir + "\\" + imagename + "_" + thisx + "___" + thisy + "." + imageexten;
            colorarr_path(colorimage, already, filepath);
            System.out.println(thisx + "___" + thisy);
        } while (thisx != -1);
    }

    public static int[] boolarr_nextpoint(boolean[][] already, int x, int y, int step, int near, int predatx, int predaty) {
        int lx = already.length;
        int ly = already[0].length;

        int nextx = -1;
        int nexty = -1;
        double maxdirectdat = -20;
        for (int addi = Math.max(0, x - step), addil = Math.min(lx - 1, x + step); addi < addil; addi++)
            for (int addj = Math.max(0, y - step), addjl = Math.min(ly - 1, y + step); addj < addjl; addj++)
                if (already[addi][addj]) { // 可跳
                    if (!((Math.abs(addi - x) < near) && (Math.abs(addj - y) < near))) { // 范围内
                        double thiscosdat = points_datcos(predatx, predaty, addi - x, addj - y);
                        if (maxdirectdat < thiscosdat) {
                            maxdirectdat = thiscosdat;
                            nextx = addi;
                            nexty = addj;
                            //System.out.println(nextx+"__"+nexty);
                        }
                    }
                }
        int alreadysum = 0;
        for (int addi = Math.max(0, x - step), addil = Math.min(lx - 1, x + step); addi < addil; addi++) {
            for (int addj = Math.max(0, y - step), addjl = Math.min(ly - 1, y + step); addj < addjl; addj++) {
                if (already[addi][addj]) {
                    already[addi][addj] = false;
                    alreadysum++;
                }
            }
        }

        return new int[]{nextx, nexty, alreadysum};
    }

    public static double points_datcos(int datx1, int daty1, int datx2, int daty2) {
        double dist1 = Math.sqrt(datx1 * datx1 + daty1 * daty1);
        double dist2 = Math.sqrt(datx2 * datx2 + daty2 * daty2);
        double cos = (datx1 * datx2 + daty1 * daty2) / (dist1 * dist2);
        //cos=cos*cos;

        return cos;
    }/*
    public static int points_direct(int x1,int y1,int x2,int y2){
        double datx=x2-x1;
        double daty=y2-y1;
        double distance=Math.sqrt(datx*datx+daty*daty);
        double cos=(x2-x1)/distance;
        int degree=(int)(Math.acos(cos)/2/3.14159*180.0);
        return degree;
    }
    public static int boolarr_onestrock(boolean[][] already,boolean[][] origin,JSONObject strock){
        int sum=0;

        return sum;
    }*/

    public static int[] boolarr_findone(boolean[][] already) {
        int lx = already.length;
        int ly = already[0].length;
        int x = -1;
        int y = -1;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (already[i][j]) {
                    x = i;
                    y = j;
                    break;
                }
            }
        }
        return new int[]{x, y};
    }


    // conspicuous-- very different from nearby point , making it conspicuous;
    // this explain why images formed by dash lines(cartoon books) can also be recognized, just as the ones formed by solid line
    public static boolean[][] colorarr_dat_nearby(int[][][] color, int threshold, int r) {
        int lx = color.length;
        int ly = color[0].length;
        boolean[][] show = new boolean[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                show[i][j] = false;
                int[] thiscolor = color[i][j];
                // get the max difference; if the difference > threshold, it is valid
                // max,min the limit the range and make it valid; [i-r,i]*[j-r,j] is the same as[i-r,i+r]*[j-r,j+r]
                for (int testi = Math.max(0, i - r); testi <= i; testi++) { // 这个方法好简洁
                    for (int testj = Math.max(0, j - r), testjl = Math.min(ly - 1, j + r); testj <= testjl; testj++) { // ly-1!!!
                        int[] comparecolor = color[testi][testj];
                        if (color_absdistance(thiscolor, comparecolor) > threshold) {
                            show[i][j] = true;
                            show[testi][testj] = true;
                            //break;
                        }
                    }
                    //if(show[i][j]) break; // inherited from j loop
                }
            }
        }

        // show=boolarr_enough(show,r);

        return show;
    }

    public static boolean[][] boolarr_enough(boolean[][] show, int r) {
        // select the one with enough intensity
        int lx = show.length;
        int ly = show[0].length;
        boolean[][] enough = new boolean[lx][ly];
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                if (show[i][j]) {
                    int enoughsum = 0;
                    for (int testi = Math.max(0, i - r), testil = Math.min(lx - 1, i + r); testi <= testil; testi++) { // 这个方法好简洁
                        for (int testj = Math.max(0, j - r), testjl = Math.min(ly - 1, j + r); testj <= testjl; testj++) { // ly-1!!!
                            if (show[testi][testj])
                                enoughsum++;
                        }
                    }
                    if (enoughsum * 2 > r * r)
                        enough[i][j] = true;
                }

        return enough;
    }


    public static int[][] pixelarr_small_maxdat_upleft(int[][] pixel, double[][] matrix) {
        // select the one that has the max difference with upper and left
        int lx = pixel.length;
        int ly = pixel[0].length;
        // skip r sites(range:2*r+1) to get sharpness, so the range is
        int r = matrix.length / 2;
        int dat = r;
        int range = r / 2;
        // maximum difference in vertical or horizontal direction
        int[][] sharpness = new int[lx / dat][ly / dat];
        for (int i = r, ii = 0; i < lx; i += dat, ii++) { // to put it in the center, adjust it!
            for (int j = r, jj = 0; j < ly; j += dat, jj++) { // start by r
                int pixel_origin = pixel[i][j];
                int max = 0;
                // compare to get the sharpness
                int compare_i = ii - 1;
                int i_pixel = -1;
                if (compare_i >= 0)
                    i_pixel = pixel[compare_i][jj]; // ii-1,jj
                int compare_j = jj - 1;
                int j_pixel = -1;
                if (compare_j >= 0)
                    j_pixel = pixel[ii][compare_j]; // ii,jj-1

                // compae (-range,range) to get the max
                if (compare_i >= 0 || compare_j >= 0)
                    for (int datx = -range; datx <= range; datx++) {
                        for (int daty = -range; daty <= range; daty++) {
                            // make sure pixel[index_i][index_j] in the range, compare index_i,index_j with pixel_origin
                            int index_i = i + datx;
                            if (index_i < 0 || index_i >= lx) break;
                            int index_j = j + daty;
                            if (index_j < 0 || index_j >= ly) continue;
                            int pixel_select = pixel[index_i][index_j];

                            // select the one that has the max difference with upper and left
                            if (i_pixel > -1) {
                                int difference = pixel_absdistances(i_pixel, pixel_select);
                                if (difference > max) {
                                    max = difference;
                                    pixel_origin = pixel_select;
                                }
                            }
                            if (j_pixel > -1) {
                                int difference = pixel_absdistances(j_pixel, pixel_select);
                                if (difference > max) {
                                    max = difference;
                                    pixel_origin = pixel_select;
                                }
                            }
                        }
                    }
                sharpness[ii][jj] = pixel_origin;
            }
        }

        return sharpness;
    }

    public static int[][] pixelarr_small_sample(int[][] pixel) {
        int lx = pixel.length;
        int ly = pixel[0].length;
        double dat = 1;
        int l_max = Math.max(lx, ly);
        if ((l_max > 256.0) || (l_max < 128.0))
            dat = l_max / 256.0;
        return pixelarr_small_sample(pixel, dat);
    }

    public static int[][] pixelarr_small_sample(int[][] pixel, double dat) {
        int lx = pixel.length;
        int ly = pixel[0].length;
        int[][] result = new int[(int) (lx / dat) + 1][(int) (ly / dat) + 1];
        double x = 0;
        for (int i = 0, dati = 0; i < lx; x += dat, i = (int) x, dati++) {
            double y = 0;
            for (int j = 0, datj = 0; j < ly; y += dat, j = (int) y, datj++)
                try {
                    result[dati][datj] = pixel[i][j];
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(i + "   " + j + "   " + x + "   " + y);
                    System.out.println(
                            dati + "   " + datj + "   " + result.length + "   " + result[0].length);
                }
        }

        //arr_path(result, "C:\\Users\\PC\\Desktop\\standard" + x + ".jpg");
        return result;
    }

    // make the image smaller by blending the pixel
    public static int[][][] colorarr_small_squreaverage(int[][][] color, int datx, int daty) {
        int[][][] smallaverage = new int[color.length / datx][color[0].length / daty][3];
        double sum = 0;
        double totalnumber = datx * daty;
        for (int i = 0; i < smallaverage.length; i++)
            for (int j = 0; j < smallaverage[0].length; j++)
                for (int k = 0; k < 3; k++) {
                    sum = 0.0;
                    for (int x = i * datx; x < (i + 1) * datx; x++)
                        for (int y = j * daty; y < (j + 1) * daty; y++)
                            sum += color[x][y][k];
                    sum /= totalnumber;
                    smallaverage[i][j][k] = (int) (sum + 0.2);
                }
        return smallaverage;
    }

    public static int[][][][] colorarr_small_squreaverage_gradual(int[][][] color, int time_x, int time_y) {
        int timex = time_x;
        int timey = time_y;
        int max = Math.max(timex, timey);
        int[][][][] colorgradual = new int[max + 1][][][];
        colorgradual[0] = color;
        int th = 1;
        while (!((timex == 0) && (timey == 0))) {
            if ((timex != 0) && (timey != 0)) {
                timex--;
                timey--;
                colorgradual[th] = colorarr_small_squreaverage(colorgradual[th - 1], 2, 2);
                th++;
            } else if (timex != 0) {
                timex--;
                colorgradual[th] = colorarr_small_squreaverage(colorgradual[th - 1], 2, 1);
                th++;
            } else if (timey != 0) {
                timey--;
                colorgradual[th] = colorarr_small_squreaverage(colorgradual[th - 1], 1, 2);
                th++;
            }
        }

        return colorgradual;
    }


    // make the image blur by blending the pixel, but not smaller
    public static int[][][] colorarr_convolution_squreaverage(int[][][] color, int r) {
        int l = 2 * r + 1;
        /*double[][] matrix=new double[l][l];
        for(int i=0;i<l;i++){
            for (int j=0;j<l ;j++ ) {
                matrix[i][j]=1;
            }
        }*/
        double[][] matrix_x = new double[l][1];
        double[][] matrix_y = new double[1][l];
        for (int i = 0; i < l; i++) {
            matrix_x[i][0] = 1;
            matrix_y[0][i] = 1;
        }

        int[][][] result1 = colorarr_small_convolution(color, matrix_x, 1);
        int[][][] result2 = colorarr_small_convolution(result1, matrix_y, 1);

        return result2;
    }


    public static int[][][] colorarr_convolution_guass(int[][][] color, int r) {
        double[][] matrix_x = guassmatrix_1d_x(r);
        double[][] matrix_y = guassmatrix_1d_y(r);
        int[][][] colorresult1 = colorarr_small_convolution(color, matrix_x, 1);
        int[][][] colorresult2 = colorarr_small_convolution(colorresult1, matrix_y, 1);
        return colorresult2;
    }

    // since the image after convolution become blurred
    // we should compress the image to reduce the number of pixels
    // or maybe we should do that when covoluting it which means skip the some number as r
    public static int[][][] colorarr_small_convolution_guass(int[][][] color, int r) {
        double[][] matrix_x = guassmatrix_1d_x(r);
        double[][] matrix_y = guassmatrix_1d_y(r);
        int[][][] colorresult1 = colorarr_small_convolution(color, matrix_x, r, 1);
        int[][][] colorresult2 = colorarr_small_convolution(colorresult1, matrix_y, 1, r);
        return colorresult2;
/*
        // dat???---> datx, daty???
        double[][] matrix = guassmatrix_2d(r);
        return colorarr_small_convolution(color, matrix, r);*/
    }

    // complete convolution
    public static int[][][] colorarr_small_convolution(int[][][] color, double[][] matrix, int dat) {
        return colorarr_small_convolution(color, matrix, dat, dat);
    }

    public static int[][][] colorarr_small_convolution(int[][][] color, double[][] matrix, int datx, int daty) {
        int lx = color.length;
        int ly = color[0].length;
        //int lm = matrix.length;
        //int r = lm / 2;
        int rx = matrix.length / 2;
        int ry = matrix[0].length / 2;
        //int dat = r;
        int[][][] result = new int[(lx - 1) / datx + 1][(ly - 1) / daty + 1][];
        //System.out.println(result.length+"___"+result[0].length);
        // i,j--index in origin; ii,jj--index in the new!
        for (int i = 0, ii = 0; i < lx; i += datx, ii++) {// dat/2
            // to put it in the center, adjust it!
            for (int j = 0, jj = 0; j < ly; j += daty, jj++) { // start by r
                //System.out.println(ii+"__"+jj+"__");
                double red = 0;
                double green = 0;
                double blue = 0;
                double sum = 0;
                for (int testi = Math.max(i - rx, 0), testil = Math.min(i + rx, lx - 1); testi <= testil; testi++) {
                    for (int testj = Math.max(j - ry, 0), testjl = Math.min(j + ry, ly - 1); testj <= testjl; testj++) {
                        int[] thiscolor = color[testi][testj];
                        double matrixvalue = matrix[testi - i + rx][testj - j + ry];
                        sum += matrixvalue;
                        //System.out.println(thiscolor[0]);
                        red += thiscolor[0] * matrixvalue;
                        green += thiscolor[1] * matrixvalue;
                        blue += thiscolor[2] * matrixvalue;
                    }
                }
                /*
                for (int datx = -r; datx <= r; datx++) {
                    for (int daty = -r; daty <= r; daty++) {
                        int index_i = i + datx;
                        int index_j = j + daty;
                        if (index_i < 0) index_i = 0;
                        if (index_i >= lx) index_i = lx - 1;
                        if (index_j < 0) index_j = 0;
                        if (index_j >= ly) index_j = ly - 1;
                        int[] thiscolor=color[index_i][index_j];
                        //Color c = new Color(pixel[index_i][index_j]);
                        red += thiscolor[0] * matrix[r + datx][r + daty];
                        green += thiscolor[1] * matrix[r + datx][r + daty];
                        blue += thiscolor[2] * matrix[r + datx][r + daty];
                        // it need implementing respectively
                    }
                    // pixel:i-r--i+r; matrix:0--2*r
                }
                */
                //System.out.println(sum+"__"+red+"__"+green+"__"+blue);
                result[ii][jj] = new int[]{(int) (red / sum), (int) (green / sum), (int) (blue / sum)};
                //System.out.println(convolution_result[ii][jj]);
            }
        }

        return result;
    }

    public static int[][][] colorarr_small_sample(int[][][] color, int dat) {
        int[][][] sample = colorarr_small_convolution(color, new double[][]{{1.0}}, dat);

        return sample;
    }

    public static double[][] guassmatrix_2d(int r) {
        double[] x = guassmatrix_1d(r);
        int ll = x.length;
        double[][] guass_xy = new double[ll][ll];

        for (int i = 0; i < ll; i++) {
            for (int j = 0; j < ll; j++)
                guass_xy[i][j] = x[i] * x[j];
        }
        return guass_xy;
    }

    public static double[] guassmatrix_1d(int r) {
        int ll = 2 * r + 1;
        double siga = r / 3.0; // 3siga to determinde siga
        double a = Math.exp(1.0 / 2.0 / siga / siga); // 3siga to determine a
        double[] x = new double[ll];
        x[0] = 1.0;
        x[2 * r] = 1.0;
        for (int i = 1; i <= r; i++) {
            x[i] = x[i - 1] * a;
            x[2 * r - i] = x[i];
        }
        // Naturalization
        double sum = 0.0;
        for (int i = 0; i < ll; i++)
            sum += x[i];
        for (int i = 0; i < ll; i++)
            x[i] /= sum;

        double[] guass_x = x;

        return guass_x;
    }

    public static double[][] guassmatrix_1d_x(int r) {
        double[] guass_x = guassmatrix_1d(r);
        int l = guass_x.length;
        double[][] guass_y = new double[l][1];
        for (int i = 0; i < l; i++) {
            guass_y[i][0] = guass_x[i];
        }
        return guass_y;
    }

    public static double[][] guassmatrix_1d_y(int r) {
        double[] guass_x = guassmatrix_1d(r);
        return new double[][]{guass_x};
    }


    // not very reliable
    public static int[][][] pixelarr_outwhite(int[][][] colorarr) {
        // minu the white light(r=g=b)
        // not really! this will change the image!!!
        int lx = colorarr.length;
        int ly = colorarr[0].length;
        int[][][] nolight = new int[lx][ly][4];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                int mincolor = colorarr[i][j][0];
                for (int k = 0; k < 3; k++)
                    if (mincolor > colorarr[i][j][k]) {
                        mincolor = colorarr[i][j][k];
                    }
                for (int k = 0; k < 3; k++)
                    nolight[i][j][k] = colorarr[i][j][k] - mincolor;
            }
        }

        return nolight;
    }


    // overflood , select the point that has the difference with center < threshold; nearby points
    public static boolean[][] pixelarr_overfloodcenter_nearby(int[][] pixel, int x, int y, int threshold) {
        // actually it is not overflood because leaking is also included
        int lx = pixel.length;
        int ly = pixel[0].length;
        // ??? actually, the boundary is recognized by the difference with nearby points, not difference from one point

        // overflood
        boolean[][] ring = new boolean[lx][ly];
        int[][] que = new int[100000][2];
        que[0] = new int[]{x, y};
        for (int head = 0, rear = 0; head <= rear; head++) {
            int thex = que[head][0];
            int they = que[head][1];
            ring[thex][they] = true;

            // compare 4 point next to [thex,they], (-1,0),(1,0),(0,-1),(0,1) with center
            int pixel_center = pixel[thex][they];
            if ((thex - 1 >= 0) && (!ring[thex - 1][they])) {
                if (pixel_absdistances(pixel_center, pixel[thex - 1][they]) <= threshold) {
                    rear++;
                    que[rear][0] = thex - 1;
                    que[rear][1] = they;
                    ring[thex - 1][they] = true;
                }
            }
            if ((thex + 1 < lx) && (!ring[thex + 1][they])) {
                if (pixel_absdistances(pixel_center, pixel[thex + 1][they]) <= threshold) {
                    rear++;
                    que[rear][0] = thex + 1;
                    que[rear][1] = they;
                    ring[thex + 1][they] = true;
                }
            }
            if ((they - 1 >= 0) && (!ring[thex][they - 1])) {
                if (pixel_absdistances(pixel_center, pixel[thex][they - 1]) <= threshold) {
                    rear++;
                    que[rear][0] = thex;
                    que[rear][1] = they - 1;
                    ring[thex][they - 1] = true;
                }
            }
            if ((they + 1 < ly) && (!ring[thex][they + 1])) {
                if (pixel_absdistances(pixel_center, pixel[thex][they + 1]) <= threshold) {
                    rear++;
                    que[rear][0] = thex;
                    que[rear][1] = they + 1;
                    ring[thex][they + 1] = true;
                }
            }
        }

        return ring;
    }

    // overflood , select the point that has the difference with center < threshold; column points
    public static int[][] pixelarr_overfloodcenter_column(int[][] pixel, int x, int y, int threshold) {
        int lx = pixel.length;
        int ly = pixel[0].length;

        // overflood
        int[][] yminmax = new int[lx][2];
        int pixel_center = pixel[x][y];
        yminmax[x] = new int[]{y, y};

        // compare the point in column with pixel_center
        for (int i = 0; x + i < lx; i++) { // columnindex!+, x, x+1...lx-1
            int pixel_compare = pixel[x + i][y];
            if (pixel_absdistances(pixel_center, pixel_compare) <= threshold) {
                yminmax[x + i][0] = y; // 0 lower boundary
                yminmax[x + i][1] = y; // 1 upper boundary
                for (int j = 1; y + j < ly; j++) { // in the column
                    pixel_compare = pixel[x + i][y + j];
                    if (pixel_absdistances(pixel_center, pixel_compare) > threshold) {
                        yminmax[x + i][1] = y + (j - 1);
                        break;
                    }
                }
                for (int j = 1; y - j >= 0; j++) { // in the column
                    pixel_compare = pixel[x + i][y - j];
                    if (pixel_absdistances(pixel_center, pixel_compare) > threshold) {
                        yminmax[x + i][0] = y - (j - 1);
                        break;
                    }
                }
            } else
                break;
        }
        for (int i = 1; x - i >= 0; i++) { // columnindex!-, x-1, x-2...0
            int pixel_compare = pixel[x - i][y];
            if (pixel_absdistances(pixel_center, pixel_compare) <= threshold) {
                yminmax[x - i][0] = y; // 0 lower boundary
                yminmax[x - i][1] = y; // 1 upper boundary
                for (int j = 1; y + j < ly; j++) { // in the column
                    pixel_compare = pixel[x - i][y + j];
                    if (pixel_absdistances(pixel_center, pixel_compare) > threshold) {
                        yminmax[x - i][1] = y + (j - 1);
                        break;
                    }
                }
                for (int j = 1; y - j >= 0; j++) { // in the column
                    pixel_compare = pixel[x - i][y - j];
                    if (pixel_absdistances(pixel_center, pixel_compare) > threshold) {
                        yminmax[x - i][0] = y - (j - 1);
                        break;
                    }
                }
            } else
                break;
        }

        return yminmax;
    }


    // processing
    public static int[][][] whitearr_color(int[][] white) {
        int lx = white.length;
        int ly = white[0].length;
        int[][][] color = new int[lx][ly][];
        int thiswhite = 0;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                thiswhite = white[i][j];
                color[i][j] = new int[]{thiswhite, thiswhite, thiswhite};
            }
        }
        return color;
    }

    public static int[][] colorarr_whitearr(int[][][] color) {
        int lx = color.length;
        int ly = color[0].length;
        int[][] white = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                white[i][j] = (color[i][j][0] + color[i][j][1] + color[i][j][2]) / 3;
            }
        }
        return white;
    }

    public static boolean[][] hsvarr_same_boolarr(int[][][] hsvarr, int[] hsvsingle) {
        return hsvarr_same_boolarr(hsvarr, hsvsingle, 15); // 15-20 is by default
    }

    public static boolean[][] hsvarr_same_boolarr(int[][][] hsvarr, int[] hsvsingle, double maxdiff) {
        int lx = hsvarr.length;
        int ly = hsvarr[0].length;
        boolean[][] yes = new boolean[lx][ly];
        double maxdiffs = maxdiff * maxdiff;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (hsv_distances(hsvarr[i][j], hsvsingle) > maxdiffs)
                    yes[i][j] = true;
                else yes[i][j] = false;
            }
        }

        return yes;
    }

    // make the image smaller by sampling
    // there was no single color, every single color is based on comparison
    public static boolean[][] colorarr_same_boolarr(int[][][] colorarr, int[] targetcolor, int maxdifference) {
        int lx = colorarr.length;
        int ly = colorarr[0].length;
        boolean[][] yes = new boolean[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                //int thiscolor = colorarr[i][j][2]; //b
                int difference = color_absdistance(colorarr[i][j], targetcolor);
                if (difference <= maxdifference) {
                    yes[i][j] = true;
                    /*for (int k = 0; k < 3; k++)
                        singlearr[i][j][k] = colorarr[i][j][k];*/
                } else yes[i][j] = false;
            }
        }

        return yes;
    }

    public static int[][][] pixelarr_hsvarr(int[][][] pixelarr) {
        int lx = pixelarr.length;
        int ly = pixelarr[0].length;
        int[][][] hsvarr = new int[lx][ly][3];
        float[] hsv = new float[3];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                hsvarr[i][j] = color_hsv(pixelarr[i][j]);
                /*
                Color.RGBtoHSB(pixelarr[i][j][0], pixelarr[i][j][1], pixelarr[i][j][2],hsv);
                hsvarr[i][j][0]=(int)(hsv[0]*240+0.5);
                hsvarr[i][j][1]=(int)(hsv[1]*240+0.5);
                hsvarr[i][j][2]=(int)(hsv[2]*240+0.5);*/
            }
        }

        return hsvarr;
    }


    // sinlge handling

    public static int[] pixel_color(int pixelsingle) {
        Color c = new Color(pixelsingle);
        int[] colorint = new int[3];
        colorint[0] = c.getRed();
        colorint[1] = c.getGreen();
        colorint[2] = c.getBlue();
        return colorint;
    }

    public static int pixel_absdistances(int pixel1, int pixel2) {
        Color c1 = new Color(pixel1);
        Color c2 = new Color(pixel2);
        int red1 = c1.getRed();
        int green1 = c1.getGreen();
        int blue1 = c1.getBlue();
        int red2 = c2.getRed();
        int green2 = c2.getGreen();
        int blue2 = c2.getBlue();
        int difference = Math.abs(red1 - red2) + Math.abs(green1 - green2) + Math
                .abs(blue1 - blue2);
        // actually it still needs researching how the eye exactly sense the difference
        // this equation is not strict
        return difference;
    }

    public static int color_absdistance(int[] color1, int[] color2) {
        int sum = 0;
        for (int i = 0, l = color1.length; i < l; i++) {
            sum += Math.abs(color1[i] - color2[i]);
        }
        return sum;
    }

    public static int color_squredistance(int[] color1, int[] color2) {
        int sum = 0;
        for (int i = 0, l = color1.length; i < l; i++) {
            sum += (color1[i] - color2[i]) ^ 2;
        }
        return sum;
    }

    public static double pixel_hsvdistances(int[] a, int[] b) {
        int[] hsva = color_hsv(a);
        int[] hsvb = color_hsv(b);
        return hsv_distances(hsva, hsvb);
    }

    public static double hsv_distances(int[] a, int[] b) { //b is base
        int h = Math.abs(a[0] - b[0]); //0-h,theta
        double theta = Math.min(h, 1 - h) * 2 * Math.PI;
        theta = theta / 240;
        double r1 = a[1] * Math.min(a[2], 240 - a[2]) * 2;
        r1 = r1 / 240;
        double r2 = b[1] * Math.min(b[2], 240 - b[2]) * 2;
        r2 = r2 / 240;
        double rs = r1 * r1 + r2 * r2 - 2 * r1 * r2 * Math.cos(theta);// 1-s-r
        //System.out.println(r1+"   "+r2);
        double vsqrt = a[2] - b[2]; // nonlinear lightness
        int vs = (int) (vsqrt * vsqrt); // 2-v
        //if (vsqrt>-15) vs=0;
//light difference becomes less obvious as the light get denser.
        return (0.64 * rs + vs);// saturation is not so important
    }

    // consistent with windows!!
    //R, G, B: 0-255, H, L,S: 0-240
    public static int[] color_hsv(int[] color) {
        double r = ((double) color[0]) / 255;
        double g = ((double) color[1]) / 255;
        double b = ((double) color[2]) / 255;
        double MaxValue = Math.max(Math.max(r, g), b);
        double MinValue = Math.min(Math.min(r, g), b);
        double diff = MaxValue - MinValue;
        double l = (MaxValue + MinValue) / 2;
        double s, h, Bdist, Gdist, Rdist;
        if (Math.abs(diff) < 1E-6) {
            s = 0;
            h = 0;
        } else {
            if (l <= 0.5)
                s = diff / (MaxValue + MinValue);
            else
                s = diff / (2 - MaxValue - MinValue);
            Rdist = (MaxValue - r) / diff;
            Gdist = (MaxValue - g) / diff;
            Bdist = (MaxValue - b) / diff;
            h = 0;
            if (r == MaxValue)
                h = Bdist - Gdist;
            else if (g == MaxValue)
                h = 2 + Rdist - Bdist;
            else if (b == MaxValue)
                h = 4 + Gdist - Rdist;
            if (h < 0)
                h = h + 6;
        }
        int hh = (int) (h * 40 + 0.5);
        int ll = (int) (l * 240 + 0.5);
        int ss = (int) (s * 240 + 0.5);
        return (new int[]{hh, ss, ll});
    }

    public static int[][][] colorarr_hsvarr(int[][][] color){
        int lx=color.length;
        int ly=color[0].length;
        int[][][] hsv=new int[lx][ly][];
        for (int i=0;i<lx ;i++ ) {
            for (int j=0;j<ly ;j++ ) {
                hsv[i][j]=color_hsv(color[i][j]);
            }
        }
        return hsv;
    }

    // color vector model!!!!---using xyz to indicate color difference.... more accurate
    // R--->x
    // seem not very much feasible.... because we are more hue sensitive...such the face has the same hue, but xyz vary very rapid
    public static double sqrt1div3=Math.sqrt(1.0/3);
    public static double sqrt1div2=Math.sqrt(1.0/2);
    public static double sqrt1div6=Math.sqrt(1.0/6);
    public static double[][] maxmin={{-2*sqrt1div6*255.0,1.0*255.0},
            {-sqrt1div2*255.0,sqrt1div2*255.0},
            {0,3*sqrt1div3*255.0}};
    public static double[] maxminrange={1.0*255.0+2*sqrt1div6*255.0,
            sqrt1div2*255.0+sqrt1div2*255.0,
            3*sqrt1div3*255.0};
    public static int[] color_xyz(int[] color){
        int[] xyz=new int[3];
        double[] xyzdouble=new double[3];
        xyzdouble[0]=1.0*color[0]-sqrt1div6*color[1]-sqrt1div6*color[2]; //x
        xyzdouble[1]=sqrt1div2*color[1]-sqrt1div2*color[2]; //y
        xyzdouble[2]=sqrt1div3*(color[0]+color[1]+color[2]); //z
        for (int i=0;i<3 ;i++ )
            xyz[i]=(int)((xyzdouble[i]-maxmin[i][0])/maxminrange[i]*256.0);

        return xyz;
    }
    public static int[][][] colorarr_xyzarr(int[][][] color) {
        int lx = color.length;
        int ly = color[0].length;
        int[][][] xyz = new int[lx][ly][];
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++)
                xyz[i][j] = color_xyz(color[i][j]);
        return xyz;
    }


    /*
    public static int[] pixel_hsv(int[] pixel) {
        float[] hsv = Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], null);
        // this calculus is based on cone, not twin cone, which is used by Microsoft
        int[] hsvint = new int[3];
        hsvint[0] = (int) (hsv[0] * 240 + 0.5);
        hsvint[1] = (int) (hsv[1] * 240 + 0.5);
        hsvint[2] = (int) (hsv[2] * 240 + 0.5);
        return hsvint;
    }*/


    // tools & I/O
    public static int[][] pixelarr_cut_column(int[][] pixelarr, int[] start, int[] end) {
        int lx = pixelarr.length;
        int ly = pixelarr[0].length;
        int[][] cutpixel = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            //if (!(cutrange[i][0] == 0 && cutrange[i][1] == 0)) {
            for (int j = start[i]; j <= end[i]; j++) {
                cutpixel[i][j] = pixelarr[i][j];
            }
            // }
        }
        return (cutpixel);
    }

    public static int[][] pixelarr_cut_show(int[][] pixel, boolean[][] show) {
        int lx = pixel.length;
        int ly = pixel[0].length;
        int[][] cutpixel = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (show[i][j])
                    cutpixel[i][j] = pixel[i][j];
            }
        }
        return cutpixel;
    }

    public static int[][][] colorarr_cut_show(int[][][] color, boolean[][] show) {
        int lx = color.length;
        int ly = color[0].length;
        int[][][] cutcolor = new int[lx][ly][];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (show[i][j])
                    cutcolor[i][j] = color[i][j];
                else
                    cutcolor[i][j] = new int[]{0, 0, 0};
            }
        }
        return cutcolor;
    }


    // copy
    public static int[][][] colorarr_copy(int[][][] color) {
        int[][][] colorcopy = new int[color.length][color[0].length][color[0][0].length];
        for (int i = 0; i < color.length; i++)
            for (int j = 0; j < color[0].length; j++)
                for (int k = 0; k < color[0][0].length; k++) {
                    colorcopy[i][j][k] = color[i][j][k];
                }
        return colorcopy;
    }

    // draw a line(x1,y1, x2,y2)
    public static int[][][] colorarr_draw_line(int[][][] color, int x1, int y1, int x2, int y2, int[] drawcolor) {
        int[][][] colorcopy = colorarr_copy(color);
        if (x1 == x2) {
            for (int y = y1; y <= y2; y++)
                colorcopy[x1][y] = drawcolor;
        } else {
            double slope = ((double) (y2 - y1)) / ((double) (x2 - x1));
            for (int x = x1; x <= x2; x++) {
                colorcopy[x][(int) ((x - x1) * slope + y1 + 0.2)] = drawcolor; //+0.2 to avoid 0.9999->1.0
            }
        }
        return colorcopy;
    }

    public static int[][][] colorarr_draw_line(int[][][] color, int x1, int y1, int x2, int y2) {
        int[] drawcolor = new int[]{255, 0, 0};
        return colorarr_draw_line(color, x1, y1, x2, y2, drawcolor);
    }

    // draw a rectangle
    public static int[][][] colorarr_draw_rectangle(int[][][] color, int x1, int y1, int x2, int y2, int[] drawcolor) {
        int[][][] colorcopy = colorarr_copy(color);
        // x
        for (int x = x1; x <= x2; x++) {
            colorcopy[x][y1] = drawcolor;
            colorcopy[x][y2] = drawcolor;
        }
        // y
        for (int y = y1; y <= y2; y++) {
            colorcopy[x1][y] = drawcolor;
            colorcopy[x2][y] = drawcolor;
        }

        return colorcopy;
    }

    public static int[][][] colorarr_draw_rectangle(int[][][] color, int x1, int y1, int x2, int y2) {
        int[] drawcolor = new int[]{255, 0, 0};
        return colorarr_draw_rectangle(color, x1, y1, x2, y2, drawcolor);
    }


    // input
    public static int[][][] path_colorarr(String imagepath) {
        int[][] pixel = path_pixelarr(imagepath);
        return pixelarr_colorarr(pixel);
    }

    public static int[][] path_pixelarr(String imagepath){
        BufferedImage bi = path_image(imagepath);
        return bufferimage_pixelarr(bi);
    }

    public static int[][] bufferimage_pixelarr(BufferedImage bi) {
        int[] rgb = new int[3];
        int width = bi.getWidth();
        int height = bi.getHeight();
        int minX = bi.getMinX();
        int minY = bi.getMinY();
        int[][] pixelarr = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelarr[x][y] = bi.getRGB(x, y);
                /* rgb[0] = (pixel & 0xff0000) >> 16; // r
                rgb[1] = (pixel & 0xff00) >> 8; // g
                rgb[2] = (pixel & 0xff); // b
                System.out.print("(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")");*/
            }
            // System.out.println();
        }
        return pixelarr;
    }

    private static BufferedImage path_image(String imagepath) {
        File imagefile = new File(imagepath);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(imagefile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (bi);
    }

    public static int[][][] pixelarr_colorarr(int[][] pixel) {
        int lx = pixel.length;
        int ly = pixel[0].length;
        int[][][] pixelarr = new int[lx][ly][4];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                pixelarr[i][j][0] = (pixel[i][j] & 0xff0000) >> 16; // red
                pixelarr[i][j][1] = (pixel[i][j] & 0xff00) >> 8; // green
                pixelarr[i][j][2] = (pixel[i][j] & 0xff); // blue
                pixelarr[i][j][3] = (pixel[i][j] & 0xff000000) >> 24; //a
            }
        }

        return pixelarr;
    }


    // output
    // output--image
    public static void colorarr_path(int[][][] colorarr, String path) {
        int[][] pixel = colorarr_pixelarr(colorarr);
        pixelarr_path(pixel, path);
    }

    public static void colorarr_path(int[][][] colorarr, boolean[][] show, String path) {
        int[][] pixel = colorarr_pixelarr(colorarr);
        pixelarr_path(pixel, show, path);
    }

    public static int[][] colorarr_pixelarr(int[][][] color) {
        int lx = color.length;
        int ly = color[0].length;
        int[][] pixel = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                //System.out.println(i+"__"+j+"__"+color[i][j][0]+"__"+color[i][j][1]+"__"+color[i][j][2]);
                Color c = new Color(color[i][j][0], color[i][j][1], color[i][j][2]);
                pixel[i][j] = c.getRGB();
            }
        }

        return pixel;
    }

    public static void pixelarr_path(int[][] pixel, String path) {
        int lx = pixel.length;
        int ly = pixel[0].length;
        BufferedImage img = new BufferedImage(lx, ly, BufferedImage.TYPE_INT_BGR);

        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++) {
                img.setRGB(i, j, pixel[i][j]);
            }

        try {
            ImageIO.write(img, "bmp", new File(path));// jpeg may lose some information; bmp
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pixelarr_path(int[][] pixel, boolean[][] show, String path) {
        int[][] pixelshow = pixelarr_cut_show(pixel, show);
        pixelarr_path(pixelshow, path);
    }


    // output--numarr
    public static void pixelarr_path_numarr(int[][][] pixelarr, String path) {
        int lx = pixelarr.length;
        int ly = pixelarr[0].length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                sb.append(i).append("\t");
                sb.append(j).append("\t");
                sb.append(pixelarr[i][j][0]).append("\t");
                sb.append(pixelarr[i][j][1]).append("\t");
                sb.append(pixelarr[i][j][2]).append("\t");
                //sb.append(pixelarr[i][j][3]).append("\t");
                sb.append("\r\n");
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // show the color distribution in 255*255, to know which color appears most frequently in the image
    public static void colorarr_colordistribution_path(int[][][] colorarr, String path) {
        boolean[][] boolarr = colorarr_colordistribution_boolarr(colorarr);
        int[][] pixel = boolarr_pixelarr(boolarr);
        pixelarr_path(pixel, path);
    }

    public static boolean[][] colorarr_colordistribution_boolarr(int[][][] colorarr) {
        boolean[][] boolarr = new boolean[256][256];
        int lx = colorarr.length;
        int ly = colorarr[0].length;
        int r = 0, g = 0, b = 0;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                r = colorarr[i][j][0];
                g = colorarr[i][j][1];
                b = colorarr[i][j][2];
                boolarr[r][b] = true;
            }
        }

        return (boolarr);
    }

    public static int[][] boolarr_pixelarr(boolean[][] boolarr) {
        // show the distribution with black and white!!!!
        int lx = boolarr.length;
        int ly = boolarr[0].length;
        int[][] pixel = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                if (boolarr[i][j]) {
                    pixel[i][j] = 0xffffff;
                }
            }
        }

        // Mark the direction
        int[][] xy = new int[][]{{0, 255}, {255, 0}, {255, 255}};
        int[] colorff = new int[]{0xff0000, 0x00ff00, 0x0000ff};
        int k = 4;
        for (int times = 0; times < 3; times++) {
            int x = xy[times][0];
            int y = xy[times][1];
            for (int i = -k; i <= k; i++)
                if ((x + i <= 255) && (x + i >= 0)) {
                    for (int j = -k; j <= k; j++)
                        if ((y + j <= 255) && (y + j >= 0))
                            pixel[x + i][y + j] = colorff[times];
                }
        }
        pixel[0][255] = 0xff0000;
        pixel[255][0] = 0x00ff00;
        pixel[255][255] = 0x0000ff;

        return pixel;
    }

    public static int[][][] colorarr_cut(int[][][] color, int x1, int y1, int x2, int y2) throws IOException {
        int[][][] colorcut = new int[x2 - x1][y2 - y1][];
        for (int i = 0, li = x2 - x1; i < li; i++) {
            for (int j = 0, lj = y2 - y1; j < lj; j++) {
                colorcut[i][j] = color[i + x1][j + y1];
            }
        }

        //pixelarr_show(colorarr_pixelarr(colorcut));

        return colorcut;
    }

    public static int[][] colorarr_singlearr(int[][][] color, int whichcolor) {
        int lx = color.length;
        int ly = color[0].length;
        int[][] single = new int[lx][ly];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                single[i][j] = color[i][j][whichcolor];
            }
        }
        return single;
    }

    // get the single color part (red, green, blue) of the image color
    public static int[][][] colorarr_singlecolor(int[][][] color, int whichcolor) throws IOException {
        int lx = color.length;
        int ly = color[0].length;
        int[][][] singlecolor = new int[lx][ly][];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                singlecolor[i][j] = new int[]{0, 0, 0};
                singlecolor[i][j][whichcolor] = color[i][j][whichcolor];
            }
        }
        pixelarr_show(colorarr_pixelarr(singlecolor));

        return singlecolor;
    }


    // some operations !!!!!

    // colortimes=color*times ---time the oldcolor intensity to get the new color
    public static int[][][] colorarr_times(int[][][] color, double times) {
        int lx = color.length;
        int ly = color[0].length;
        int[][][] colortimes = new int[lx][ly][3];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                for (int k = 0; k < 3; k++) {
                    colortimes[i][j][j] = (int) (color[i][j][k] * times);
                }
            }
        }
        return colortimes;
    }

    // coloradd=color1*times+color*(1-times).....the blend of the two colors
    public static int[][][] colorarr_timesadd(int[][][] color1, int[][][] color2, double times) {
        int lx = Math.max(color1.length, color2.length);
        int ly = Math.max(color1[0].length, color2[0].length);
        int[][][] coloradd = new int[lx][ly][];
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                int[] colorunit = new int[3];
                coloradd[i][j] = colorunit;
                for (int k = 0; k < 3; k++) {
                    colorunit[k] = (int) (color1[i][j][k] * times + color2[i][j][k] * (1 - times));
                }
            }
        }
        return coloradd;
    }

    // coloradd=color1+color2;  cut the part beyond the range
    public static int[][][] colorarr_add(int[][][] color1, int[][][] color2) {
        int lx = Math.max(color1.length, color2.length);
        int ly = Math.max(color1[0].length, color2[0].length);
        int[][][] coloradd = new int[lx][ly][];
        int value = 0;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                int[] colorunit = new int[3];
                coloradd[i][j] = colorunit;
                for (int k = 0; k < 3; k++) {
                    value = color1[i][j][k] + color2[i][j][k];
                    if (value > 255) value = 255;
                    if (value < 0) value = 0;
                    colorunit[k] = value;
                }
            }
        }
        return coloradd;
    }


    // show the specific line with x,y
    public static void colorarr_line_path(int[][][] color, int x, int y, int whichcolor) throws IOException {
        // colorline
        int[][] colorline;
        if (x != -1) { // superior .... x=-1 means draw the line from y ; otherwise draw the line from x
            colorline = color[x];
        } else {
            int lx = color.length;
            colorline = new int[lx][];
            for (int i = 0; i < lx; i++) {
                colorline[i] = color[i][y];
            }
        }
        // colorlinesingle,
        // max is not necessary, range in 0-255
        int l = colorline.length;
        int[] colorlinesingle = new int[l];
        //int max=0;
        int thisnum = 0;
        for (int i = 0; i < l; i++) {
            thisnum = colorline[i][whichcolor];
            colorlinesingle[i] = thisnum;
            //if(max<thisnum) max=thisnum;
        }
        //max+=(max/4);

        // plot
        line_show(colorlinesingle);
    }

    public static void line_show(int[] line) throws IOException {
        int max = 255 + 50; //+50 in the top
        int l = line.length;
        boolean[][] plot = new boolean[l][max + 50]; //+50 in the bottom
        int maxvalue=0;
        for(int i=0;i<l;i++){
            if(maxvalue<line[i])
                maxvalue=line[i];
        }
        int convertednum=0;
        for (int i = 0; i < l; i++) {
            // convert line[i] to 0--255
            convertednum=line[i]*256/maxvalue;
            plot[i][max -convertednum] = true;
        }

        int[][] pixel = boolarr_pixelarr(plot);
        pixelarr_show(pixel);
    }

    public static void pixelarr_show(int[][] pixel) throws IOException {
        UUID uuid = UUID.randomUUID();
        String plotpath = imagedir + "\\" + uuid.toString() + ".jpg";
        pixelarr_path(pixel, plotpath);
        Format.path_open(plotpath);
    }

    public static void colorarr_show(int[][][] color) throws IOException {
        pixelarr_show(colorarr_pixelarr(color));
    }


    public static int[][][] matcharr_colorarr(double[][] match) {
        return matcharr_colorarr(match, 0, 1);
    }

    public static int[][][] matcharr_colorarr(double[][] match, double min, double max) { // match--correlation: [-1,1]
        int lx = match.length;
        int ly = match[0].length;
        double scale = max - min;
        int[][][] color = new int[lx][ly][3];
        int value = 0;
        int[] thiscolor = null;
        for (int i = 0; i < lx; i++) {
            for (int j = 0; j < ly; j++) {
                value = (int) ((match[i][j] - min) / scale * 255); // (int)((match[i][j]+1)/2*255);
                if ((value > 260) || (value < -3)) System.out.println(match[i][j] + "__" + value);
                if (value > 255) value = 255;
                if (value < 0) value = 0;
                //System.out.println(value+"__"+match[i][j]);
                thiscolor = color[i][j];
                for (int k = 0; k < 3; k++) {
                    thiscolor[k] = value;
                }
            }
        }
        return color;
    }

    public static void matcharr_show(double[][] match, double min, double max) throws IOException {
        colorarr_show(matcharr_colorarr(match, min, max));
    }


    // tools
    public static int[][][] guassianarr_colorarr(int lx, int ly, double sigma) {
        int[][][] color = new int[lx][ly][3];
        Random r = new Random();
        int value = 0;
        for (int i = 0; i < lx; i++)
            for (int j = 0; j < ly; j++) {
                value = (int) (r.nextGaussian() * sigma + 0.5) + 128;
                if (value > 255) value = 255;
                if (value < 0) value = 0;
                for (int k = 0; k < 3; k++) {
                    color[i][j][k] = value;
                }
            }
        return color;
    }
































































































    public static int[][] boolarr_squarerange(boolean[][] maybe){
        List<int[]> squarerange=new ArrayList<int[]>(); // x1,y1,x2,y2, w, h
        int xl=maybe.length;
        int yl=maybe[0].length;
        boolean[][] detected=new boolean[xl][yl];
        boolean still=true;
        int thisx=-1; int thisy=-1;
        int size=0; boolean canexpand=true; int datnum=0; int datyes=0; double threshold=0.75; double lenthreshold=0.3; double ratio=0.0;

        while(still) {
            // found one not detected
            still = false;
            for (int i = 0; i < xl; i++)
                for (int j = 0; j < yl; j++)
                    if ((!detected[i][j]) && (maybe[i][j])) { // not detected & maybe
                        still = true;
                        thisx = i;
                        thisy = j;
                    }
            if (!still) break; // if there is no point left, break;

            // try to find size
            size = 1;
            canexpand = true;
            while (canexpand) {
                size++;
                if ((thisx + size > xl) && (thisy + size > yl)) {
                    canexpand = false;
                    break;
                }
                // count the ratio.... thisx+size-1, thisy+size-1
                datyes = 0;
                for (int i = thisx, il = thisx + size; i < il; i++)
                    if (maybe[i][thisy + size - 1])
                        datyes++;
                for (int j = thisy, jl = thisy + size; j < jl; j++)
                    if (maybe[thisx + size - 1][j])
                        datyes++;
                datnum = 2 * size - 1;
                ratio = ((double) datyes) / ((double) datnum);
                if (ratio > threshold)
                    canexpand = true;
                else
                    canexpand = false;
            }
            size--; // return to the size the canexapnd=true;

            // enlengate is more tolerant.... threshold is lower;
            // tolerance is related to length
            int nowx = thisx;
            int nowy = thisy;
            datyes = 0;
            datnum = size * size;
            for (int i = nowx, il = nowx + size; i < il; i++)
                for (int j = nowy, jl = nowy + size; j < jl; j++)
                    if (maybe[i][j])
                        datyes++;
            ratio = ((double) datyes) / ((double) datnum);
            if (ratio < lenthreshold) {

            }
        }

        return squarerange.toArray(new int[squarerange.size()][]);
    }

    // not !!!!!!!!!!!!!!!!1  blur fuzzy--->interesting(further detail ?? the border??)
    // --->range --- usual shape(chunk area(2 dimension) ??? 套用rectangle等， range逐步扩大试探检测
    // --> line(1 dimension) )--->need further analysis
    // determine where the image contains more information needing further analysis...such as containing character that need further analysis
    // square shape.... have been resized to normalization
    // many but not all  ??? or  dev ???
    // 渐进色变的忽略 ？？？？
    public static boolean[][] colorarr_further(int[][][] color,int dat,double threshol){
        int il=color.length/dat;
        int jl=color[0].length/dat;
        double threshold=threshol*128.0;
        int x1,y1,x2,y2;
        double this_mean=0.0;
        double this_dev=0.0;
        boolean[][] need=new boolean[il][jl];
        boolean[][] newmaybe=new boolean[color.length][color[0].length]; // normalized need
        for (int i=0;i<il ;i++ ) {
            x1=i*dat;
            x2=i*dat+dat;
            for (int j=0;j<jl ;j++ ) {
                // here to heritage from old maybe
                y1=j*dat;
                y2=j*dat+dat;
                this_mean=colorarr_mean(color,x1,x2,y1,y2);
                this_dev=colorarr_dev(color,this_mean,x1,x2,y1,y2);
                if(this_dev>threshold) {
                    need[i][j] = true;
                    //normalized need
                    for (int x=x1;x<x2 ;x++ )
                        for (int y=y1;y<y2 ;y++ ) {
                            newmaybe[x][y]=true;
                        }
                }
            }
        }

        return need;
    }

}

