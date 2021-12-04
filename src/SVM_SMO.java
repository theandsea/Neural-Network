import java.io.IOException;

/*if(1.0067559976990332E-6>9.990857839938141E-7)
     System.out.println(true);*/

public class SVM_SMO {

    public static int xl,yl,T;
    public static int[][][] colordata=null;
    public static int[] actualres=null;
    public static void main(String[] args) throws Exception {

        MNISTdata();
        //testdata();
        T=colordata.length/100;//100;
        totalscale=T/6;
        xl=colordata[0].length;
        yl=colordata[0][0].length;
        System.out.println("total data number___"+T);
        System.out.println("total data scale___"+totalscale);
        colordata_colorgroup();


        w_sum=new double[10][10][][];
        b_sum=new double[10][10];
        double[][] acurrate=new double[10][10];
        double thisaccurate=0;
        for (int i=0;i<10;i++ ) {
            for (int j=i+1;j<10 ;j++ ) { // i<j
                thisaccurate=distinguish(i,j);
                acurrate[i][j]=(int)(thisaccurate*10000)/10000.0; ///10000.0 //
                acurrate[j][i]=acurrate[i][j];
                w_sum[i][j]=omiga.clone();
                b_sum[i][j]=bstar;
            }
        }

        // general situation
        System.out.println();
        System.out.println();
        System.out.println("=========================general situation============================");
        int total=10000;
        int correct=0;
        int[] index_total=new int[10];
        int[][] resultmatrix=new int[10][10];
        for (int t=0;t<total;t++ ) {
            int index=(int)(Math.random()*60000);
            index_total[actualres[index]]++;

            int[] judge_res=general_judge(index);
            correct += judge_res[0];
            resultmatrix[actualres[index]][judge_res[1]]++;
        }
        System.out.println("general situation accuracy___"+correct+"/"+total);
        System.out.println("general situation accuracy___"+double_percent((double)correct/total));

        System.out.print("accuracy matrix for training data___");
        array_print_double(acurrate);

        System.out.println();
        System.out.print("accuracy matrix for testing data___");
        double[][] test_accurate=new double[10][10];
        for (int i=0;i<10 ;i++ ) {
            for (int j=i;j<10 ;j++ ) {
                double rate=resultmatrix[i][j]/(double)index_total[i];
                rate=(int)(rate*10000)/10000.0;
                test_accurate[i][j]=rate;
                test_accurate[j][i]=test_accurate[i][j];
            }
        }
        array_print_double(test_accurate);
    }

    public static int[] general_judge(int index){ // true--1, false--0
        int[][] target=colordata[index];
        int res=actualres[index];
        int[] score=new int[10];
        for (int i=0;i<10;i++ ) {
            for (int j=i+1;j<10 ;j++ ) { // i<j
                int judge=wb_judge(target,w_sum[i][j],b_sum[i][j]);
                if(judge==1)
                    score[i]++;
                else
                    score[j]++;
            }
        }
        array_print(score);
        int max_score=score[0];
        int mostlikeli=0;
        for (int i=0;i<10 ;i++ ) {
            if (max_score < score[i]) {
                max_score = score[i];
                mostlikeli = i;
            }
        }
        System.out.println("most likely number___"+mostlikeli);
        System.out.println("actual result___"+res);
        if(mostlikeli==res)
            return new int[]{1,mostlikeli};
        else
            return new int[]{0,mostlikeli};
    }


    public static double[][][][] w_sum=null;
    public static double[][] b_sum=null;
    public static int totalscale=0;
    public static double distinguish(int num1,int num2) throws Exception {
        System.out.println();
        System.out.println("================================================================");
        System.out.println("distinguish between___"+num1+"_and_"+num2);
        set_colorpairy(num1,num2);
        set_yyxx();
        set_aC();
        //int[][] pair=new int[][]{{102,58},{60,115},{86,77},{68,94},{103,49}}; //for 1,2

        boolean valid=false;
        for (int t=0;t<totalscale*10;) { //pair.length
            int a1=(int)(Math.random()*cl); //pair[t][0];//
            int a2=(int)(Math.random()*cl); //pair[t][1];//
            valid=false;
            if(a1!=a2){
                if(t<2*totalscale){ // first 100 must be different side, second 100 can be the same side
                    if(y[a1]!=y[a2])
                        valid=true;
                }else
                    valid=true;
            }
            if(valid){
                SMO(a1,a2);
                t++;
                System.out.println("pair___"+a1+"__"+a2);
                System.out.println("L__"+a_L());
                //array_print(a);
            }
        }
        a_wb();

        System.out.println();
        System.out.println("================================================================");
        System.out.println("judge___"+num1+"_and_"+num2);
        int correct=0;
        for (int i=0;i<cl ;i++ ) {
            //array_print(colorpair[i]);
            int judge=wb_judge(colorpair[i],omiga,bstar);
            //System.out.println(judge+"___"+y[i]);
            if(judge==y[i]) {
                //System.out.println("correct");
                correct++;
            }else {
                //System.out.println("wrong");
            }
        }
        System.out.println("accuracy___"+correct+"/"+cl);
        System.out.println("accuracy___"+double_percent((double)correct/cl));
        return (double)correct/cl;
    }

    public static void MNISTdata() throws IOException {
        Object[] data = ImageProcess.MNISTpath_whitearr("train-images.idx3-ubyte","train-labels.idx1-ubyte");
        colordata = (int[][][]) data[0];
        actualres = (int[]) data[1];
    }

    public static void testdata(){ //{0,1},{1,1},{2,1},{0,3},{1,3},{2,3}
        int[][] point=new int[][]{{0,2},{0,3},{1,3},{0,4},{1,4},{1,2},{1,1},  {1,1},{2,1},{2,2},{1,0},{2,0},{1,2},{0,2}};
        int[] classifi=new int[]{0,0,0,0,0,0,0,   1,1,1,1,1,1,1};
        colordata=new int[point.length][][];
        for (int i=0,il=point.length;i<il ;i++ ) {
            colordata[i]=new int[][]{point[i]};
        }

        actualres=classifi;
    }


    public static int[][][] colorpair=null;
    public static int[] y=null;
    public static int cl=0;
    public static int SMO_l=0;
    public static void set_colorpairy(int num1,int num2){
        int[][][] color1=colorgroup[num1];
        int[][][] color2=colorgroup[num2];
        int cl_1=color1.length;
        int cl_2=color2.length;
        cl=cl_1+cl_2;

        colorpair=new int[cl_1+cl_2][][];
        y=new int[cl_1+cl_2];
        for (int i=0;i<cl_1 ;i++ ) {
            colorpair[i] = color1[i];
            y[i]=1;
        }for (int i=0;i<cl_2 ;i++ ) {
            colorpair[i + cl_1] = color2[i];
            y[i+cl_1]=-1;
        }
    }

    public static int[][] yyxx=null;
    public static void set_yyxx(){
        yyxx=new int[cl][cl];
        for (int i=0;i<cl ;i++ ) {
            for (int j=0;j<cl ;j++ ) {
                yyxx[i][j]=kernel(colorpair[i],colorpair[j])*y[i]*y[j];
            }
        }
        System.out.print("yyxx____");
        array_print(yyxx);
    }

    public static double[] a=null;
    public static double C=0;
    public static void set_aC(){
        C=10;
        a=new double[cl];
    }

    public static void SMO(int a1,int a2) throws Exception {
        // S, k, b
        double S=0;
        for (int i=0;i<cl ;i++ ) {
            if (i != a1 && i != a2)
                S += a[i] * y[i];
        }
        double k=-y[a1]*y[a2];
        double b=-y[a1]*S;

        // range
        double b_p=-y[a2]*S;
        //System.out.println(k+"___"+b+"___"+b_p);
        double kab1=b_p;
        double kab2=k*C+b_p; // should kab1<kab2
        double tempkab=0;
        if(kab1>kab2){
            tempkab=kab1;
            kab1=kab2;
            kab2=tempkab;
        }
        double L=Math.max(0,kab1);
        double H=Math.min(C,kab2);
        if(L>H){ // not exist
            //System.out.println("no range___"+a1+"___"+a2);
            return;
        }
        //System.out.println(L+"___"+H);

        // A1
        double A1=0;
        for (int i=0;i<cl ;i++ ) {
            if (i != a1 && i != a2)
                A1 +=a[i]*yyxx[a1][i];
        }

        // A2
        double A2=0;
        for (int i=0;i<cl ;i++ ) {
            if (i != a1 && i != a2)
                A2 +=a[i]*yyxx[a2][i];
        }

        // 2nd term coefficient...a
        double aaa=-0.5*(k*k*yyxx[a1][a1]+yyxx[a2][a2]+2*k*yyxx[a1][a2]);
        // 1st term coefficient...b
        double bbb=-0.5*(2*k*A1+2*A2+2*b*k*yyxx[a1][a1]+2*b*yyxx[a1][a2])+1+k;
                //k*(1-A1)+(1-A2)-yyxx[a1][a1]*b*k-yyxx[a1][a2]*b;

        // mid
        double mid=-0.5*bbb/aaa;



        // situation
        if(aaa<0) { //regular
            /*System.out.println("origin___"+a[a2]);
            a[a2] = L;
            a[a1] = k * a[a2] + b;
            System.out.println("L___"+L+"___"+a_L());
            a[a2] = H;
            a[a1] = k * a[a2] + b;
            System.out.println("H___"+H+"___"+a_L());*/

            if (L <= mid && mid <= H) {
                a[a2] = mid;
                //System.out.println("mid");
            }else if (mid < L) {
                a[a2] = L;
                //System.out.println("L");
            }else if (H < mid) {
                a[a2] = H;
                //System.out.println("H");
            }
        }else if(aaa>0){ // wired
            System.out.println("wired");
            if (L <= mid && mid <= H) { // compare
                if (mid - L <= H - mid)
                    a[a2] = H;
                else
                    a[a2] = L;
            }else if (mid < L)
                a[a2]=H;
            else if (H < mid)
                a[a2]=L;
        }else if(aaa==0){
            //System.out.println("error___a=0");
            //throw new Exception("error___a=0");
        }
        a[a1] = k * a[a2] + b;
    }


    public static double a_L() {
        // first term
        double first = 0;
        for (int i = 0; i < cl; i++) {
            for (int j = 0; j < cl; j++)
                first += a[i] * a[j] * yyxx[i][j];
        }
        // second term
        double second = 0;
        for (int i = 0; i < cl; i++)
            second += a[i];

        double sum = -0.5 * first + second;
        return sum;
    }

    // omiga
    public static double[][] omiga=null;
    public static double bstar;
    public static double[][] a_wb(){
        double[][] w=new double[xl][yl];
        for (int t=0;t<cl;t++ ) {
            int[][] color=colorpair[t];
            for (int i=0;i<xl ;i++ ) {
                for (int j=0;j<yl ;j++ ) {
                    w[i][j] += a[t]*y[t]*color[i][j];
                }
            }
        }
        omiga=w;
        System.out.print("omiga___");
        array_print_double(w);

        int k=0;
        double max=Math.abs(a[0]);
        for (int i=0;i<cl ;i++ ) { // is this necessary ; or should find the maximum ???
            if (max<Math.abs(a[i])) { //Math.abs(a[i]) > 0.0000001
                k = i;
                max=Math.abs(a[i]);
                break;
            }
        }
        bstar=y[k];
        for (int i=0;i<cl;i++ )
            bstar -=a[i]*y[k]*yyxx[i][k];
        System.out.println("b__"+bstar);

        return w;
    }

    public static int wb_judge(int[][] u,double[][] w,double b){
        double res=kernel(w,u)+b;
        if(res>0)
            return 1;
        else
            return -1;
    }







    public static int[][][][] colorgroup=null;
    public static void colordata_colorgroup(){
        int[] num=new int[10];
        int[] index=new int[10];
        for (int i=0;i<10 ;i++ ) {
            num[i] = 0;
            index[i] =0;
        }
        for (int i=0;i<T ;i++ )
            num[actualres[i]]++;

        // create
        colorgroup=new int[10][][][];
        for (int i=0;i<10 ;i++ ){
            System.out.println(i+"___"+num[i]);
            colorgroup[i]=new int[num[i]][][];
        }
        // classify
        int group=0;
        for (int i=0;i<T ;i++ ) {
            group=actualres[i];
            colorgroup[group][index[group]]=colordata[i];
            index[group]++;
        }
    }

    public static int kernel(int[][] g1,int[][] g2){
        int sum=0;
        for (int x=0;x<xl;x++ ) {
            for (int y=0;y<yl ;y++ ) {
                sum += g1[x][y] * g2[x][y];
            }
        }
        return sum;
    }
    public static double kernel(double[][] w,int[][] g2){
        double sum=0;
        for (int x=0;x<xl;x++ ) {
            for (int y=0;y<yl ;y++ ) {
                sum += w[x][y] * g2[x][y];
            }
        }
        return sum;
    }



    public static void array_print(int[][] array){
        System.out.println("array___");
        for (int i=0,il=array.length;i<il;i++ ) {
            for (int j=0,jl=array[i].length;j<jl ;j++ ) {
                System.out.print(array[i][j]+"\t");
            }
            System.out.println();
        }
    }
    public static void array_print_double(double[][] array){
        System.out.println("array___");
        for (int i=0,il=array.length;i<il;i++ ) {
            for (int j=0,jl=array[i].length;j<jl ;j++ ) {
                System.out.print(array[i][j]+"\t");
            }
            System.out.println();
        }
    }
    public static void array_print(double[] array) {
        System.out.println("array___");
        for (int i = 0, il = array.length; i < il; i++)
            System.out.print(array[i] + "\t");
        System.out.println();
    }
    public static void array_print(int[] array) {
        System.out.println("array___");
        for (int i = 0, il = array.length; i < il; i++)
            System.out.print(array[i] + "\t");
        System.out.println();
    }
    public static String double_percent(double doub){
        return(((double)(int)((doub*10000))/100+"%"));
    }
}
