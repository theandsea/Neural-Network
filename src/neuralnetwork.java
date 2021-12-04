import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class neuralnetwork {

    public static void main(String[] args) throws Exception {

        Object[] data= ImageProcess.MNISTpath_whitearr_padding("train-images.idx3-ubyte","train-labels.idx1-ubyte");
        colordata = (int[][][]) data[0];
        actualres = (int[]) data[1];/**/



        LeNet_5_network("RBFparameter",
               "train-images.idx3-ubyte","train-labels.idx1-ubyte");
        System.out.println();
        System.out.println("========================training================================");
        training_batch(colordata,actualres,0.01);
        //training_adapted();
        //training(colordata,actualres,0.01);
        save();



        read();
        int l=colordata.length;
        int index=0;
        wrongtime=0;
        System.out.println();
        System.out.println("========================test================================");
        int total=10000;
        for (int n=0;n<10000 ;n++ ) {
            index=(int)(l*Math.random());
            image_judge(index);
        }
        System.out.println("accuracy____"+(total-wrongtime)+"/"+total);
        String percent=(int)((total-wrongtime)/(double)total*10000)/100.0+"%";
        System.out.println("accuracy____"+percent);
        /* */
    }






    public static int[][][][] color_bynum=null;
    public static void training_adapted() throws Exception {
        // 总的来说，不如random法。。。870/10000, 1200/10000
        // 数据分类
        if (color_bynum==null) {
            // 先统计数量，便于new int[]
            int[] nums=new int[10];
            for (int i=0,l=colordata.length;i<l ;i++ )
                nums[actualres[i]]++;
            int[] index=new int[10];
            color_bynum=new int[10][][][];
            for (int i=0;i<10 ;i++ )
                color_bynum[i]=new int[nums[i]][][];
            int labelnum=0;
            for (int i=0,l=colordata.length;i<l ;i++ ){
                labelnum=actualres[i];
                color_bynum[labelnum][index[labelnum]]=colordata[i];
                index[labelnum]++;
            }
        }

        // 自适应的处理程序
        int imageindex=0;
        int imageclass=0;
        int[] num_class=new int[1000];
        double[] loss=new double[10];
        int[] times=new int[10];
        for (int i=0;i<10 ;i++ ) times[i]=1;
        double thisloss=0;
        for (int epoch=0;epoch<500 ;epoch++ ) {
            // 先确定比率数组，num_class
            for (int i=0;i<10 ;i++ ) // nonlinear
                loss[i] = Math.sqrt(loss[i] / (times[i]+1)) + 1; // 防止times[i]=0
            System.out.println("*********************************");
            System.out.println("*********************************");
            System.out.println("==============New epoch==========");
            System.out.println(Format.variable_json(loss));
            for (int i=1;i<10 ;i++ ) // sum up
                loss[i] += loss[i-1];
            for (int i=0;i<10 ;i++ ) {
                loss[i] = loss[i]*1000/loss[9];
            }
            int now=0;
            for (int i=0;i<1000 ;i++ ) {
                if (i>=loss[now]) // 超过上界就不再是了
                    now++;
                num_class[i]=now;
            }
            for (int i=0;i<10 ;i++ ) {
                times[i]=0;
                loss[i]=0;
            }
            System.out.println(Format.variable_json(num_class));

            // training
            for(int b=0;b<100;b++){
                // 有针对的选择进行训练
                imageclass=num_class[(int)(Math.random()*1000)];
                imageindex=(int)(Math.random()*color_bynum[imageclass].length);
                // before
                gradient_math(color_bynum[imageclass][imageindex],imageclass);
                System.out.println("Number is___"+imageclass);
                thisloss=loss(imageclass);
                System.out.println("Now loss___"+thisloss);
                loss[imageclass]+= thisloss;
                times[imageclass]++;
                System.out.println(Format.variable_json(layer_out));
                backpropagation_uniform(0.01);
                // after
                forward(color_bynum[imageclass][imageindex]);
                System.out.println("Then loss___"+loss(imageclass));
                System.out.println(Format.variable_json(layer_out));
                System.out.println("==========================================");
            }
        }
    }




    public static int wrongtime=0;
    public static int image_judge(int index) throws Exception {
        forward(colordata[index]);
        double min=tanh_A*(2*2)*84;
        int num=-1;
        for (int i=0,il=layer_out.length;i<il ;i++ ) {
            if(min>layer_out[i].value){
                min=layer_out[i].value;
                num=i;
            }
        }
        System.out.println("No. "+index+"   ___ "+actualres[index]);
        System.out.println("judge ___"+num+"   loss___"+loss(actualres[index]));
        if (num != actualres[index]) {
            //System.out.println("===================================================" + "====================================");
            System.out.println("error!");
            wrongtime++;
        }
        return num;
    }


    public static void training(int[][][] white,int[] actualres,double times) throws Exception {
        int index=0;
        int l=white.length;
        double originloss=0;
        double thisloss=0;

        for (int n=0;n<50000 ;n++ ) { // n 不能太大，过拟合的情况时有发生，indirect也有可能过拟合
            index = (int) (Math.random() * l);
            forward(white[index]);
            //thisloss = loss(actualres[index]);
            if (n % 100 == 0) {
                System.out.println(index + "__" + actualres[index] + "_____loss_" + loss(actualres[index]));
                System.out.println(Format.variable_json(layer_out));
            }
            //gradient_indirect(white[index],actualres[index],times);...非常靠谱的检验手段，但有点慢
            /*if (thisloss<3.9) { //并没有起到很好的作用，反而有点反作用
                System.out.println("very little, no need to modify");
            }else {*/
            gradient_math(white[index], actualres[index]);
            backpropagation_vanish(times);
            //}
            // after
            if (n % 100 == 0) {
                System.out.println("After____");
                forward(white[index]);
                System.out.println(Format.variable_json(layer_out));
                System.out.println("loss___" + loss(actualres[index]));
                System.out.println("=================================");
            }
        }
    }

    public static void training_batch(int[][][] white,int[] actualres,double times) throws Exception {
        int batch=50; // batch法之后，貌似backpropagation不需要非线性了，要降低其非线性
        int l=white.length;
        int index=0;
        double losssum=0;

        for (int n=0;n<2000;n++ ) {
            gradientaccum_reset();
            losssum = 0;
            for (int b = 0; b < batch; b++) {
                index = (int) (Math.random() * l);
                gradient_math(white[index], actualres[index]);
                gradientaccum_sumup();
                losssum += loss(actualres[index]);
            }
            if (losssum / batch < 3.9) {
                // 不必修正，以免起反作用。。。【no need to modify】非常成功！！！470-->252
                System.out.println("very little, no need to modify");
            } /*else if (losssum / batch < 10) { // 这个的作用没那么大
                System.out.println("little, reduced modification");
                gradientaccum_average(batch);
                backpropagation(0.5*times);
            } */else {
                gradientaccum_average(batch);
                backpropagation_uniform(times);  // times * batch ???
            }
            System.out.println("loss____" + losssum / batch);
        }
    }
    // by average gradient
    public static void gradientaccum_reset(){
        for (int n=1;n<=6 ;n++ ) {
            // weight
            for (int th = 0, thl = weightlist[n].length; th < thl; th++) {
                weightlist[n][th].gradient_accumu =0;
            }
            // bias
            for (int th = 0, thl = biaslist[n].length; th < thl; th++) {
                biaslist[n][th].gradient_accumu = 0;
            }
        }
    }
    public static void gradientaccum_sumup(){
        for (int n=1;n<=6 ;n++ ) {
            // weight
            for (int th = 0, thl = weightlist[n].length; th < thl; th++) {
                weightlist[n][th].gradient_accumu +=weightlist[n][th].gradient;
            }
            // bias
            for (int th = 0, thl = biaslist[n].length; th < thl; th++) {
                biaslist[n][th].gradient_accumu += biaslist[n][th].gradient;
            }
        }
    }
    public static void gradientaccum_average(int batch){
        for (int n=1;n<=6 ;n++ ) {
            // weight
            for (int th = 0, thl = weightlist[n].length; th < thl; th++) {
                weightlist[n][th].gradient =weightlist[n][th].gradient_accumu/batch;
            }
            // bias
            for (int th = 0, thl = biaslist[n].length; th < thl; th++) {
                biaslist[n][th].gradient = biaslist[n][th].gradient_accumu/batch;
            }
        }
    }

    // backpropagation:  weight, bias
    // gradient * times ---> value
    public static void backpropagation_uniform(double times) throws Exception {
        double[] timeslist=new double[weightlist.length];
        for (int i=1,l=weightlist.length;i<l;i++)
            timeslist[i]=times;
        backpropagation(timeslist);
    }
    public static void backpropagation_vanish(double times) throws Exception {
        double[] timeslist=new double[weightlist.length];
        // batch的average平均法，与此处的probability法，二者选一即可，不必全用，尤其大batch时，此处的非线性起反作用
        // 让下级先学，增强对data的敏感度。。。确实收敛速度大大提高了！！！
        for (int i=1,l=weightlist.length;i<l;i++) {
            timeslist[i] = (1.0 / (double) i) * (1.0 / (double) i) * times;//
            //System.out.println(timeslist[i]);
        }
        //System.out.println("length is___"+timeslist.length);
        backpropagation(timeslist);
    }
    public static void backpropagation(double[] times) throws Exception {
        double propobility=0;
        for (int n=1,l=weightlist.length;n<l ;n++ ) {
            // weight
            for (int th = 0, thl = weightlist[n].length; th < thl; th++) {
                weightlist[n][th].value -= weightlist[n][th].gradient * times[n];
            }
            // bias
            for (int th = 0, thl = biaslist[n].length; th < thl; th++) {
                biaslist[n][th].value -= biaslist[n][th].gradient * times[n]; //* 0.3; // 抑制bias, 强化对data敏感度
            }
        }
    }

    // math 法的gradient计算，简直太快了。。。而且非常准确，是以后把握的重点！！！
    public static void gradient_math(int[][] white,int acturalindex) throws Exception {
        // origin
        forward(white);
        double intergradient=0;

        // out ---> loss
        double publish=Math.exp(-mindistance);
        for (int i=0,il=layer_out.length;i<il ;i++ )
            if (i!=acturalindex) {
                publish +=Math.exp(-layer_out[i].value);
            }
        for (int i=0,il=layer_out.length;i<il ;i++ ) {
            if (i==acturalindex) {
                layer_out[i].gradient=1.0;
            }else{
                layer_out[i].gradient= -Math.exp(-layer_out[i].value)/publish;
                /*if(layer_out[i].gradient <1e-13)
                    layer_out[i].gradient=0; */
            }
        }

        // F6, RBF ---> out
        double gradient_sum=0;
        for (int i=0,il=layer_F6.length;i<il ;i++ ) {
            gradient_sum=0;
            for (int j=0,jl=layer_out.length;j<jl ;j++ ) {
                gradient_sum += 2*(layer_F6[i].value-RBF[j][i])*layer_out[j].gradient;
            }
            layer_F6[i].gradient =gradient_sum;
        }


        // layer by layer
        // C5,W6,B6 --> F6
        // S4,W5,B5 --> C5
        // C3,W4,B4 --> S4
        // S2,W3,B3 --> C3
        // C1,W2,B2 --> S2
        // in,W1,B1 --> C1 .... in not needed
        neuron[] input=null;
        node[] weight=null;
        neuron Neuron=null;
        for (int n=6;n>=1 ;n-- ) {
            // initialization !
            // B
            for (int th=0,thl=biaslist[n].length;th<thl ;th++ )
                biaslist[n][th].gradient = 0;
            // W
            for (int th=0,thl=weightlist[n].length;th<thl ;th++ )
                weightlist[n][th].gradient=0;
            // X
            for (int th=0,thl=layerlist[n-1].length;th<thl ;th++ )
                layerlist[n-1][th].gradient=0;

            // cumulate !
            for (int th=0,thl=layerlist[n].length;th<thl ;th++ ) {
                Neuron=layerlist[n][th];
                intergradient =(tanh_A* (1-(Neuron.value*Neuron.value)/(tanh_A*tanh_A))*tanh_S)*Neuron.gradient;
                // 求导公式反复出错。。。太可惜了。。。值得警惕
                // Bias
                Neuron.bias.gradient += intergradient;
                /*if (Neuron.bias==speicificbias) {
                    System.out.println(intergradient+"___"+(tanh_A* (1-(Neuron.value*Neuron.value)/(tanh_A*tanh_A))*tanh_S)+"___"+Neuron.gradient);
                    System.out.println(Neuron.bias.gradient+"___"+Neuron.value);
                }*/
                // input(X) & Weight .... n=0, layer_in 不必计算！！！
                weight=Neuron.weight;
                input=Neuron.input;
                for (int in=0,inl=input.length;in<inl ;in++ ) {
                    weight[in].gradient += intergradient * input[in].value;
                    input[in].gradient += intergradient * weight[in].value;
                }
            }
        }
    }


    // 实践证明，可以用于收敛！！！
    public static void gradient_indirect(int[][] white,int acturalindex,double dat) throws Exception {
        // origin
        forward(white);

        // out --> loss
        neuron Neuron=null;
        double originloss=loss(acturalindex); // for indirect, it is necessary
        double datloss=0.0;
        for (int th=0,thl=layer_out.length;th<thl ;th++ ) {
            Neuron=layer_out[th];
            // dat
            Neuron.value +=dat;
            datloss=loss(acturalindex);
            Neuron.gradient = (datloss-originloss)/dat;
            // recover
            Neuron.value -=dat;
        }

        // F6 --> out ; RBF
        double gradient_sum=0;
        //double gradient_unit=0;
        double tempres=0;
        double tempdat=0;
        for (int th=0,thl=layer_F6.length;th<thl ;th++ ) {
            Neuron=layer_F6[th];
            // dat
            Neuron.value +=dat;
            // dat --> out
            gradient_sum=0;
            for (int out=0,ol=RBF.length;out<ol;out++ ) {
                tempres=0;
                for (int in=0,inl=layer_F6.length;in<inl ;in++ ) {
                    tempdat =layer_F6[in].value-RBF[out][in];
                    tempres += tempdat*tempdat;
                }
                gradient_sum += ((tempres-layer_out[out].value)/dat) * layer_out[out].gradient;
            }
            Neuron.gradient=gradient_sum;
            // recover
            Neuron.value -=dat;
        }

        // layer by layer
        // C5,W6,B6 --> F6
        // S4,W5,B5 --> C5
        // C3,W4,B4 --> S4
        // S2,W3,B3 --> C3
        // C1,W2,B2 --> S2
        // in,W1,B1 --> C1 .... in not needed
        node Node=null;
        neuron outson=null;
        for (int n=6;n>=1 ;n-- ) { // n--layer number
            // Wn gradient  <-- Xn
            for (int th=0,thl=weightlist[n].length;th<thl ;th++ ) {
                Node=weightlist[n][th];
                // dat
                Node.value += dat;
                Node.gradient=gradient_onepoint(Node.out,dat);
                // recover
                Node.value -=dat;
            }

            // Bn gradient <-- Xn
            for (int th=0,thl=biaslist[n].length;th<thl ;th++ ) {
                Node=biaslist[n][th];
                // dat
                Node.value +=dat;
                Node.gradient=gradient_onepoint(Node.out,dat,Node);
                // recover
                Node.value -=dat;
            }

            // Xn-1 <-- Xn
            // each neuron in the next layer(n-1), calculate its gradient
            if (n-1>0) { // input gradient is not necessary
                for (int th = 0, thl = layerlist[n - 1].length; th < thl; th++) {
                    Neuron=layerlist[n-1][th];
                    // dat
                    Neuron.value += dat;
                    Neuron.gradient = gradient_onepoint(Neuron.out,dat);
                    // recover
                    Neuron.value -= dat;
                }
            }
        }
    }
    // recalculate each out neuron, get its gradient to input, mulitplied by its own gradient(out)
    // sum them up to get the input gradient
    public static double gradient_onepoint(ArrayList<neuron> list,double dat) throws Exception {
        return gradient_onepoint(list,dat,null);
    }

    public static node speicificbias=null; // 这种检验刺探法，十分值得学习！！！ 直接跟踪某个关键的指针
    // 【调试技巧--设点法（specificbias）】
    public static double gradient_onepoint(ArrayList<neuron> list,double dat,node thisnode) throws Exception {
        neuron outson=null;

        double gradient_sum=0;
        for (int out=0,ol=list.size();out<ol ;out++ ) { // each output neuron(Xn) involved
            outson=list.get(out);
            //if (Math.abs(outson.gradient) > 1e-7)
            gradient_sum += ((neuron_sum(outson)-outson.value)/dat * outson.gradient); // 重新计算，获取新的宿命结果
            /*if (speicificbias==thisnode) { // 这一调试技巧很值得深思。。。多用几种方法计算，便于对比！！！
                System.out.println(((neuron_sum(outson)-outson.value)/dat * outson.gradient)+"___"+((neuron_sum(outson)-outson.value)/dat)+"___"+outson.gradient);
                System.out.println(gradient_sum+"___"+outson.value);
            }*/
            // 还可以按照数学表达式，继续优化。。。tanh的优化表达法。。。。大大提升了效率
        }


        return gradient_sum;
    }


    // calculate the gradient directly,
    public static void gradient_direct(int[][] white,int acturalindex,double dat) throws Exception {
        // origin
        forward(white);
        double originloss=loss(acturalindex);
        double datloss=0.0;

        // 直接计算法，没有用 偏微风间接计算快（类似动态规划） 41660 ms,42211 ms,42541 ms(double[][]) VS 40938 ms(thisnode),45225 ms (node.gradient)
        // 不过这种偏微分计算，只有在的neuron的框架下，才可行啊。。。矩阵法时则非常复杂
        // 性能利用率不稳定（不通电时CPU利用率下降大约只有6%），也会随着CPU的使用率而改变
        // weight_gradient
        node thisnode=null; // 放在内部，反复重新申请变量，导致速度慢
        for (int n=1;n<=6 ;n++ ) {
            for (int th=0,thl=weightlist[n].length;th<thl ;th++ ) {
                thisnode=weightlist[n][th];
                // datloss
                thisnode.value +=dat;
                forward(white);
                datloss=loss(acturalindex);
                thisnode.gradient= (datloss-originloss)/dat;
                // recover
                thisnode.value -=dat;
            }
        }

        // bias_gradient
        for (int n=1;n<=6 ;n++ ) {
            for (int th=0,thl=biaslist[n].length;th<thl ;th++ ) {
                thisnode=biaslist[n][th];
                // datloss
                thisnode.value +=dat;
                forward(white);
                datloss=loss(acturalindex);
                thisnode.gradient= (datloss-originloss)/dat;
                // recover
                thisnode.value -=dat;
            }
        }
    }


    public static double mindistance=50;
    public static double loss(int actualindex){
        int l=layer_out.length;
        double Ew=mindistance;
        //double j=50; // j决定，近到什么程度，才会有处罚，否则一定距离之外，没有处罚（即只有y<j,y才有效果）
        double logsum=Math.exp(-mindistance);
        for (int i=0;i<l ;i++ ) {
            if (i == actualindex) {
                Ew += layer_out[i].value;
            } else {
                logsum += Math.exp(-layer_out[i].value);
            }
        }
        Ew+=Math.log(logsum); // 默认log==ln  , log10=lg , log1P=???

        return Ew;
    }

    // 不需要返回，直接再layer_out中有结果
    public static void forward(int[][] singlecolor) throws Exception {
        // colordata--->inputlayer
        for (int i=0,il=singlecolor.length;i<il;i++ ) {
            for (int j=0,jl=singlecolor[i].length;j<jl;j++) {
               // System.out.println(inputlayer[i][j]);
                layer_in[i][j].value=singlecolor[i][j];
            }
        }

        // layer to layer, C1,S2,C3,S4,C5,S6
        double tempsum=0;
        for (int n=1;n<=6;n++ ) {
            for (int th=0,thl=layerlist[n].length;th<thl ;th++ ) { // each neuron in each layer
                layerlist[n][th].value=neuron_sum(layerlist[n][th]);
            }
        }

        // F6 --> outputlayer
        neuron[] layer_F6=layerlist[6];
        neuron[] outlayer=layerlist[7];
        double dat=0;
        for (int i=0,il=outlayer.length;i<il ;i++ ) {
            tempsum=0;
            for (int j=0,jl=layer_F6.length;j<jl ;j++ ) {
                if (Math.abs(layer_F6[j].value)>2) {
                    throw new Exception("Something is wrong !");
                }
                dat=layer_F6[j].value-RBF[i][j];
                tempsum +=dat*dat;
            }
            outlayer[i].value=tempsum;
        }
    }

    public static double tanh_A=1.7159;
    public static double tanh_S=0.66667;
    // compute the result of one neuron
    public static double neuron_sum(neuron Neuron) throws Exception {
        neuron[] unitlist_input = null;
        node[] unitlist_weight = null;
        double tempsum = 0;

        unitlist_input = Neuron.input;
        unitlist_weight = Neuron.weight;
        // weight
        tempsum = 0;
        for (int j = 0, jl = unitlist_input.length; j < jl; j++) {
            tempsum += unitlist_input[j].value * unitlist_weight[j].value;
        }
        // bias
        tempsum += Neuron.bias.value;

        // Activation...sample层也要activation，防止范围溢出！！！
        //if (n!=2 && n!=4) {
        tempsum = tanh_A * Math.tanh(tanh_S * tempsum);
        //}

        return tempsum;
    }

















































































    // construct the network layer, layerlist
    // read the data
    public static int[][][] colordata =null;
    public static int[] actualres =null;
    // construct !!!!!
    public static void LeNet_5_network(String RBFparameterdir,String imagepath,String labelpath) throws Exception {

        // origin()
        // weight, bias, RBF
        // initial()
        // node--new,  weightnode, biasnode <--- weight, bias
        // interconnection
        // layer, neuron--interconnection, new neuron,  neuron.input(neuron), neuron.weight(node), neuron.bias(node)
        // arr_list
        // weightlist, biaslist, layerlist <--- weightnode, biasnode, layer


        // origin !!!
        double[][][] weight_C1 = new double[6][5][5];
        double[] bias_C1 = new double[6];
        node[][][] weightnode_C1 = new node[6][5][5];
        node[] biasnode_C1 = new node[6];
        double[] weight_S2 = new double[6];
        double[] bias_S2 = new double[6];
        node[] weightnode_S2 = new node[6];
        node[] biasnode_S2 = new node[6];
        double[][][][] weight_C3 = new double[][][][]{
                new double[3][5][5], new double[3][5][5], new double[3][5][5],
                new double[3][5][5], new double[3][5][5], new double[3][5][5],
                new double[4][5][5], new double[4][5][5], new double[4][5][5],
                new double[4][5][5], new double[4][5][5], new double[4][5][5],
                new double[4][5][5], new double[4][5][5], new double[4][5][5],
                new double[6][5][5]};
        node[][][][] weightnode_C3 = new node[][][][]{
                new node[3][5][5], new node[3][5][5], new node[3][5][5],
                new node[3][5][5], new node[3][5][5], new node[3][5][5],
                new node[4][5][5], new node[4][5][5], new node[4][5][5],
                new node[4][5][5], new node[4][5][5], new node[4][5][5],
                new node[4][5][5], new node[4][5][5], new node[4][5][5],
                new node[6][5][5]};
        int[][] conf_C3 = new int[][]{{0, 1, 2}, {1, 2, 3}, {2, 3, 4},
                {3, 4, 5}, {4, 5, 0}, {5, 0, 1},// 一定要按照顺序吗？？？
                {0, 1, 2, 3}, {1, 2, 3, 4}, {2, 3, 4, 5},
                {3, 4, 5, 0}, {4, 5, 0, 1}, {5, 0, 1, 2},
                {0, 1, 3, 4}, {1, 2, 4, 5}, {0, 2, 3, 5},
                {0, 1, 2, 3, 4, 5}};
        double[] bias_C3 = new double[16];
        node[] biasnode_C3 = new node[16];
        double[] weight_S4 = new double[16];
        double[] bias_S4 = new double[16];
        node[] weightnode_S4 = new node[16];
        node[] biasnode_S4 = new node[16];
        double[][][][] weight_C5 = new double[120][16][5][5];
        double[] bias_C5 = new double[120];
        node[][][][] weightnode_C5 = new node[120][16][5][5];
        node[] biasnode_C5 = new node[120];
        double[][] weight_F6 = new double[84][120];
        double[] bias_F6 = new double[84];
        node[][] weightnode_F6 = new node[84][120];
        node[] biasnode_F6 = new node[84];
        RBF = new int[10][];
        for (int n = 0; n < 10; n++) {
            int[][][] bmpcolor = ImageProcess.path_colorarr(RBFparameterdir + "\\" + n + ".bmp");
            int index = 0;
            int[] RBFweight = new int[bmpcolor.length * bmpcolor[0].length];
            RBF[n] = RBFweight;
            for (int i = 0, il = bmpcolor.length; i < il; i++)
                for (int j = 0, jl = bmpcolor[i].length; j < jl; j++) {
                    if (bmpcolor[i][j][0] == 0) {
                        RBFweight[index] = -1;
                    } else if (bmpcolor[i][j][0] == 255) {
                        RBFweight[index] = 1;
                    } else {
                        throw new Exception("data format wrong !!!");
                    }
                    index++;
                }
        }
        // weight random !!!
        Object[] weightsum = new Object[]{weight_C1, weight_S2, weight_C3, weight_S4, weight_C5, weight_F6};
        Object[] biassum = new Object[]{bias_C1, bias_S2, bias_C3, bias_S4, bias_C5, bias_F6};
        for (int i = 0, il = weightsum.length; i < il; i++) {
            ImageProcess.weight_initialize(weightsum[i], 2.4);
            ImageProcess.weight_initialize(biassum, 1.2);
        } // random 还是有问题，值太小了，导致不同的数据输入后，差异很小

        // initial()
        // weightnode, biasnode
        initial(weightnode_C1, weight_C1);
        initial(biasnode_C1, bias_C1);
        initial(weightnode_S2, weight_S2);
        initial(biasnode_S2, bias_S2);
        initial(weightnode_C3, weight_C3);
        initial(biasnode_C3, bias_C3);
        initial(weightnode_S4, weight_S4);
        initial(biasnode_S4, bias_S4);
        initial(weightnode_C5, weight_C5);
        initial(biasnode_C5, bias_C5);
        initial(weightnode_F6, weight_F6);
        initial(biasnode_F6, bias_F6);


        // interconnection
        // layer, neuron--interconnection, new neuron,  neuron.input(neuron), neuron.weight(node), neuron.bias(node)
        initial_neuron(layer_in);
        initial_neuron(layer_C1);
        initial_neuron(layer_S2);
        initial_neuron(layer_C3);
        initial_neuron(layer_S4);
        initial_neuron(layer_C5);
        initial_neuron(layer_F6);
        initial_neuron(layer_out); // 缺乏初始化
        // interconnection 的 new放入initial,以便于实现 out list的添加
        num2_num3_convolution(layer_in, layer_C1, weightnode_C1, biasnode_C1);
        num3_num3_sample(layer_C1, layer_S2, weightnode_S2, biasnode_S2);
        num3_num3_convolution_conf(layer_S2, layer_C3, weightnode_C3, biasnode_C3, conf_C3);
        num3_num3_sample(layer_C3, layer_S4, weightnode_S4, biasnode_S4);
        num3_num3_convolution_full(layer_S4, layer_C5, weightnode_C5, biasnode_C5);
        num3_num1_convolution_full(layer_C5, layer_F6, weightnode_F6, biasnode_F6); // C5 --> F6
        //System.out.println(layer_out[0]);


        // arr_list
        // weightlist, biaslist, layerlist <--- weightnode, biasnode, layer
        weightlist = new node[][]{null,
                (node[]) objNarr_objarr(weightnode_C1),
                (node[]) objNarr_objarr(weightnode_S2),
                (node[]) objNarr_objarr(weightnode_C3),
                (node[]) objNarr_objarr(weightnode_S4),
                (node[]) objNarr_objarr(weightnode_C5),
                (node[]) objNarr_objarr(weightnode_F6)
        };

        biaslist = new node[][]{null,
                biasnode_C1, biasnode_S2, biasnode_C3,
                biasnode_S4, biasnode_C5, biasnode_F6};

        layerlist = new neuron[][]{(neuron[]) objNarr_objarr(layer_in),
                (neuron[]) objNarr_objarr(layer_C1),
                (neuron[]) objNarr_objarr(layer_S2),
                (neuron[]) objNarr_objarr(layer_C3),
                (neuron[]) objNarr_objarr(layer_S4),
                (neuron[]) objNarr_objarr(layer_C5),
                (neuron[]) objNarr_objarr(layer_F6),
                (neuron[]) objNarr_objarr(layer_out)
        };
        parameter=new Object[][]{
                {layer_in},
                {layer_C1,weightnode_C1,biasnode_C1},
                {layer_S2,weightnode_S2,biasnode_S2},
                {layer_C3,weightnode_C3,biasnode_C3},
                {layer_S4,weightnode_S4,biasnode_S4},
                {layer_C5,weightnode_C5,biasnode_C5},
                {layer_F6,weightnode_F6,biasnode_F6},
                {layer_out}
        };

        // read the data
        // forward
        // loss
        // gradient
        // backpropagation
        // training
        // read & save model


        // read the data
        /*
        Object[] data = ImageProcess.MNISTpath_whitearr(imagepath, labelpath);
        colordata = (int[][][]) data[0];
        actualres = (int[]) data[1];*/

        // read model parameter
        //read();


        // forward
        //forward(colordata[7]);
        // loss
        //System.out.println(loss(actualres[7]));


        // 【调试技巧--对比法！！！】3种方法（方法2indirect已经发现了收敛现象），结果是否一致，最终的可靠性测试
        // small difference hard to determine
        // gradient
/*        speicificbias=((node[])parameter[2][2])[0];
        long now = System.currentTimeMillis();
        gradient_math(colordata[7], actualres[7]);
        System.out.println("time duraction___ " + (System.currentTimeMillis() - now) + " ms");
        System.out.println(Format.variable_json(parameter));

        now=System.currentTimeMillis();
        gradient_indirect(colordata[7], actualres[7], 1e-7);
        System.out.println("time duraction___ " + (System.currentTimeMillis() - now) + " ms");
        System.out.println(Format.variable_json(parameter));

        now=System.currentTimeMillis();
        gradient_direct(colordata[7], actualres[7], 1e-10);
        System.out.println("time duraction___ " + (System.currentTimeMillis() - now) + " ms");
        System.out.println(Format.variable_json(parameter));
        */


        /**/
        // backpropagation
        //backpropagation(0.0001);

        //read();
        // training
        //training(colordata,actualres,0.01);

        // save & read model... weight & bias
        //save();
    }




    // 多维化为 1维的list
    public static Object[] objNarr_objarr(Object obj) throws Exception {
        String typename=obj.getClass().getTypeName();
        int bracketindex=typename.indexOf("[]");
        if (bracketindex==-1) {
            throw new Exception("wrong, null");
        } else{
            int dimension=(typename.length()-bracketindex)/2;
            String basictype=typename.substring(0,bracketindex);
            if (dimension==1) { // do it。。。 array unit new
                return (Object[])obj;
            }else{
                Object[] father=(Object[]) obj;
                switch (basictype){
                    case "neuralnetwork$neuron":{ // 注意 static class貌似要用$
                        ArrayList<neuron> list=new ArrayList<>();
                        for (int i=0,l=father.length;i<l ;i++ ) {
                            list.addAll(Arrays.asList((neuron[])objNarr_objarr(father[i])));
                        }
                        return list.toArray(new neuron[list.size()]);
                    }
                    //break;
                    case "neuralnetwork$node":{
                        ArrayList<node> list=new ArrayList<>();
                        for (int i=0,l=father.length;i<l ;i++ ) {
                            list.addAll(Arrays.asList((node[])objNarr_objarr(father[i])));
                        }
                        return list.toArray(new node[list.size()]);
                    }
                    //break;
                    default:{
                        throw new Exception("type not complete");
                    }
                }

            }
        }
    }


    // F6 <-- C5
    public static void num3_num1_convolution_full(neuron[][][] input,neuron[] output,node[][] weightnode,node[] biasnode){
        neuron[] inputlist=null;
        node[] weight=null;
        int sum=input.length;
        int index=0;
        for (int out=0,ol=output.length; out<ol; out++) {
            //output[out] = new neuron();
            // bias
            output[out].bias = biasnode[out];
            biasnode[out].out.add(output[out]);
            // input & weight
            output[out].input = new neuron[sum];
            inputlist = output[out].input;
            output[out].weight = new node[sum];
            weight = output[out].weight;
            index = 0;
            for (int in = 0, inl = weightnode[out].length; in < inl; in++) {
                // input
                inputlist[index] = input[in][0][0];
                input[in][0][0].out.add(output[out]);
                // weight
                weight[index] = weightnode[out][in];
                weightnode[out][in].out.add(output[out]);
                index++;
            }
        }
    }

    // C5
    public static void num3_num3_convolution_full(neuron[][][] input,neuron[][][] output,node[][][][] weightnode,node[] biasnode){
        neuron[] inputlist=null;
        node[] weight=null;
        int sum=input.length*weightnode[0][0].length*weightnode[0][0][0].length;
        int index=0;
        for (int out=0,ol=output.length; out<ol; out++) {
            for (int i=0,il=output[out].length; i<il;i++ ) {
                for (int j=0,jl=output[out][i].length;j<jl ;j++ ) {
                    //output[out][i][j]=new neuron();
                    // bias
                    output[out][i][j].bias=biasnode[out];
                    biasnode[out].out.add(output[out][i][j]);
                    // input & weight
                    output[out][i][j].input=new neuron[sum];
                    inputlist=output[out][i][j].input;
                    output[out][i][j].weight=new node[sum];
                    weight=output[out][i][j].weight;
                    index=0;
                    for (int in=0,inl=weightnode[out].length;in<inl ;in++ ) {
                        for (int datx = 0, xl =weightnode[out][in].length; datx < xl; datx++) {
                            for (int daty = 0, yl = weightnode[out][in][datx].length; daty < yl; daty++) {
                                // input
                                inputlist[index] = input[in][i + datx][j + daty];
                                input[in][i + datx][j + daty].out.add(output[out][i][j]);
                                // weight
                                weight[index] = weightnode[out][in][datx][daty];
                                weightnode[out][in][datx][daty].out.add(output[out][i][j]);
                                // index
                                index++;
                            }
                        }
                    }
                }
            }
        }
    }

    // C3
    public static void num3_num3_convolution_conf(neuron[][][] input,neuron[][][] output,node[][][][] weightnode,node[] biasnode,int[][] conf){
        neuron[] inputlist=null;
        node[] weight=null;
        int sum=0;
        int index=0;
        for (int out=0,ol=output.length; out<ol; out++) {
            for (int i=0,il=output[out].length; i<il;i++ ) {
                for (int j=0,jl=output[out][i].length;j<jl ;j++ ) {
                    sum=weightnode[out].length*25; // 不是粗暴的150，而是有些150，75，100
                    //output[out][i][j]=new neuron();
                    // bias
                    output[out][i][j].bias=biasnode[out];
                    biasnode[out].out.add(output[out][i][j]);
                    // input & weight
                    output[out][i][j].input=new neuron[sum];
                    inputlist=output[out][i][j].input;
                    output[out][i][j].weight=new node[sum];
                    weight=output[out][i][j].weight;
                    index=0;
                    for (int th=0,thl=weightnode[out].length;th<thl ;th++ ) {
                        for (int datx = 0, xl =weightnode[out][th].length; datx < xl; datx++) {
                            for (int daty = 0, yl = weightnode[out][th][datx].length; daty < yl; daty++) {
                                // input
                                inputlist[index] = input[conf[out][th]][i + datx][j + daty];
                                input[conf[out][th]][i + datx][j + daty].out.add(output[out][i][j]);
                                // weight
                                weight[index] = weightnode[out][th][datx][daty];
                                weightnode[out][th][datx][daty].out.add(output[out][i][j]);
                                // index
                                index++;
                            }
                        }
                    }
                }
            }
        }
    }


    // S2, S4
    public static void num3_num3_sample(neuron[][][] input,neuron[][][] output,node[] weightnode,node[] biasnode){
        neuron[] inputlist=null;
        node[] weight=null;
        int sum=4;
        int index=0;
        for (int out=0,ol=output.length; out<ol; out++) {
            for (int i=0,il=output[out].length; i<il;i++ ) {
                for (int j=0,jl=output[out][i].length;j<jl ;j++ ) {
                    //output[out][i][j]=new neuron();
                    // bias
                    output[out][i][j].bias=biasnode[out];
                    biasnode[out].out.add(output[out][i][j]);
                    // input & weight
                    output[out][i][j].input=new neuron[sum];
                    inputlist=output[out][i][j].input;
                    output[out][i][j].weight=new node[sum];
                    weight=output[out][i][j].weight;
                    index=0;
                    for (int datx=0;datx<2 ;datx++ ) {
                        for (int daty=0;daty<2 ;daty++ ) {
                            // input
                            inputlist[index]=input[out][2*i+datx][2*j+daty];
                            input[out][2*i+datx][2*j+daty].out.add(output[out][i][j]);
                            /*inputlist[index]=input[out][i+datx][j+daty];
                            input[out][i+datx][j+daty].out.add(output[out][i][j]);*/
                            // weight
                            weight[index]=weightnode[out];
                            // index
                            index++;
                        }
                    }
                    // 注意多个相同的weight只保留一个，计算机只能计算常微分df/dx，不能计算偏微分pf/px
                    weightnode[out].out.add(output[out][i][j]);
                }
            }
        }
    }

    // C1
    public static void num2_num3_convolution(neuron[][] input,neuron[][][] output,node[][][] weightnode,node[] biasnode){
        neuron[] inputlist=null;
        node[] weight=null;
        int sum=25;
        int index=0;
        for (int out=0,ol=output.length; out<ol; out++) {
            for (int i=0,il=output[out].length; i<il;i++ ) {
                for (int j=0,jl=output[out][i].length;j<jl ;j++ ) {
                    //output[out][i][j]=new neuron();
                    // bias
                    output[out][i][j].bias=biasnode[out];
                    biasnode[out].out.add(output[out][i][j]);
                    // input & weight
                    output[out][i][j].input=new neuron[sum];
                    inputlist=output[out][i][j].input;
                    output[out][i][j].weight=new node[sum];
                    weight=output[out][i][j].weight;
                    index=0;
                    for (int datx=0,xl=weightnode[out].length;datx<xl ;datx++ ) {
                        for (int daty=0,yl=weightnode[out][datx].length;daty<yl ;daty++ ) {
                            // input
                            inputlist[index]=input[i+datx][j+daty];
                            input[i+datx][j+daty].out.add(output[out][i][j]);
                            // weight
                            weight[index]=weightnode[out][datx][daty];
                            weightnode[out][datx][daty].out.add(output[out][i][j]); // 不同的weight，当然每一次都要算
                            // index
                            index++;
                        }
                    }
                }
            }
        }
    }

    // 给所有neuron[][]中的unit new一个
    // 但不能自动合并总结，也不能new到各级，只能给基层unit，new一个instance
    public static void initial_neuron(Object obj) throws Exception {
        String typename=obj.getClass().getTypeName();
        int bracketindex=typename.indexOf("[]");
        if (bracketindex==-1) {
            throw new Exception("wrong, null");
        } else{
            int dimension=(typename.length()-bracketindex)/2;
            if (dimension==1) { // do it。。。 array unit new
                neuron[] father=(neuron[])obj;
                //Class c=Class.forName(typename.substring(0,bracketindex));
                for (int i=0,l=father.length;i<l ;i++ ) {
                    father[i]=new neuron();
                    father[i].out=new ArrayList<>();
                }
            }else{
                Object[] father=(Object[]) obj;
                for (int i=0,l=father.length;i<l ;i++ ) {
                    initial_neuron(father[i]);
                }
            }
        }
    }


    public static void initial(Object obj,Object num) throws Exception {
        String typename=obj.getClass().getTypeName();
        int bracketindex=typename.indexOf("[]");
        if (bracketindex==-1) {
            throw new Exception("wrong, null");
        } else{
            int dimension=(typename.length()-bracketindex)/2;
            if (dimension==1) { // do it。。。 array unit new
                node[] father=(node[])obj;
                double[] numvalue=(double[]) num;
                //Class c=Class.forName(typename.substring(0,bracketindex));
                for (int i=0,l=father.length;i<l ;i++ ) {
                    father[i]=new node();
                    father[i].out=new ArrayList<>();
                    father[i].value=numvalue[i];
                }
            }else{
                Object[] father=(Object[]) obj;
                Object[] numfather=(Object[])num;
                for (int i=0,l=father.length;i<l ;i++ ) {
                    initial(father[i],numfather[i]);
                }
            }
        }
    }



    //public static int[][] C3conf=null;
    public static int[][] RBF=null;

    //public static neuron[][][][] layerneuron=null;
    public static neuron[][] layerlist=null;
    public static neuron[][] layer_in=new neuron[32][32];
    public static neuron[][][] layer_C1=new neuron[6][28][28];
    public static neuron[][][] layer_S2=new neuron[6][14][14];
    public static neuron[][][] layer_C3=new neuron[16][10][10];
    public static neuron[][][] layer_S4=new neuron[16][5][5];
    public static neuron[][][] layer_C5=new neuron[120][1][1];
    public static neuron[] layer_F6=new neuron[84];
    public static neuron[] layer_out=new neuron[10];
    public static class neuron{
        public double value;
        public double gradient;
        public neuron[] input;
        public ArrayList<neuron> out;
        // out used for cacluating the gradient, namely which neuron will be affected by this neuron
        public node[] weight;
        public node bias;
    }


    //public static node[][][][][] weightnode=null;
    public static Object[][] parameter=null;
    public static node[][] weightlist=null;
    public static node[][] biaslist=null;
    public static class node{
        public double value;
        public double gradient;
        public double gradient_accumu;
        public ArrayList<neuron> out;// out used for cacluating the gradient,
        // namely which neuron will be affected by this node (weight, bias)
    }







































































































































    // transfer the value of obj to para , of the same format !
    public static void read() throws Exception {
        String path=modelpath.replace(".txt","__1.txt");
        //"GBK" 才能不乱码
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "GBK"));
        StringBuilder sb = new StringBuilder();
        String content;
        while ((content = br.readLine()) != null) {
            sb.append(content);
        }
        br.close();
        /* read的旧办法
        Object jsonarray=new JSONArray(sb.toString());
        read(jsonarray,parameter);*/

        // 自动化的新办法
        JSONObject jsonobj=new JSONObject(sb.toString());
        Format.json_variable_graph(jsonobj);
    }
    public static void read(Object json, Object para) throws Exception {
        String typename=json.getClass().getTypeName();
        switch (typename){
            /*case "org.json.JSONObject":{

            }break;*/
            case "org.json.JSONArray":{
                JSONArray datobj=(JSONArray) json;
                Object[] parasum=(Object[]) para;
                for (int i=0,il=datobj.length(); i<il;i++ ) {
                    read(datobj.get(i),parasum[i]);
                }
            }break;
            case "java.lang.Double":{
                switch (para.getClass().getTypeName()){
                    case "neuralnetwork$node":{
                        node Node=(node)para;
                        Node.value=(Double)json;
                    }break;
                    case "neuralnetwork$neuron":{
                        neuron Neuron=(neuron)para;
                        Neuron.value=(Double)json;
                    }break;
                    default:{
                        throw new Exception("wrong ! type__"+para.getClass().getTypeName());
                    }
                }
            }break;
            default:{
                throw new Exception("wrong ! type__"+typename);
            }
        }
    }



    public static String modelpath="D:\\MNIST\\network\\LeNet-5_Neuron.txt";
    public static void save() throws Exception {
        //Format.json_txtpath(save(parameter),modelpath.replace(".txt","__0.txt")); //默认存储parameter

        // 用于遍历
        /*
        HashMap<String,String[]> parameter_around=new HashMap<>();
        parameter_around.put("neuralnetwork$node",new String[]{"value"});
        parameter_around.put("neuralnetwork$neuron",new String[]{"value"});
        parameter_around.put("neuralnetwork",new String[]{"parameter","layer_in",
                "layer_C1","layer_S2","layer_C3","layer_S4","layer_C5","layer_F6","layer_out",
                "layerlist","biaslist","weightlist","RBF"});*/

        // 用于实际输出
        HashMap<String,String[]> parameter_conf=new HashMap<>();
        parameter_conf.put("neuralnetwork$node",new String[]{"value","out"});
        parameter_conf.put("neuralnetwork$neuron",new String[]{"value","out","input","weight","bias"});
        parameter_conf.put("neuralnetwork",new String[]{"parameter","layer_in",
                "layer_C1","layer_S2","layer_C3","layer_S4","layer_C5","layer_F6","layer_out",
                "layerlist","biaslist","weightlist","RBF"}); // 后面的这些property虽然已经再parameter中了，但为了后面的引用，必须也写入
        Object json=Format.variable_json_graph(new neuralnetwork(),parameter_conf,parameter_conf); // 其实都是static，形式需要new而已
        Format.json_txtpath(json,modelpath.replace(".txt","__1.txt"));//
    }
    /*
    Format中已经有了完全的办法
    public static Object save(Object variable) throws Exception {
        if (variable==null) {
            //return null;
            throw new Exception("Wrong!");
        };
        Object res=null;
        String typename=variable.getClass().getTypeName();
        switch (typename){
            case "neuralnetwork$node":{
                neuralnetwork.node Node= (neuralnetwork.node) variable;
                res=Node.value;
            }
            break;case "neuralnetwork$neuron":{
                neuralnetwork.neuron Neuron= (neuralnetwork.neuron) variable;
                res=Neuron.value;
            }
            break;
            default:{
                if(typename.contains("[]")) { // 数组。。。子元素有可能是HashMap,需要额外逐个判断啊
                    JSONArray jsonarray = new JSONArray();
                    res = jsonarray;
                    Object[] array = (Object[]) variable;
                    for (int i = 0, l = array.length; i < l; i++) {
                        Object son = save(array[i]);
                        jsonarray.put(son);
                    }
                }else{ // 其他引用类型，包括: String等
                    res=variable;
                }
            }
            break;
        }
        return res;
    }*/
}
