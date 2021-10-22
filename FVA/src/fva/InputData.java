package fva;


import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class InputData {
    public final int AreasCounter;
    public final int TotalVolume;
    public final int[] DemandsSet;
    public final double[][] ServiceQuality;
    public final int FleetsCounter;
    public final double[] SharedVolumeRatio;
    private final int[] MaxCapacity;
    private final int[] MaxContractedVolume;
    public final int[] MinVolume;
    public final int[] MaxVolume;
    private final int[] GreenCapacity;
    public final double[] Costs;
    public final double[][] Productivity;
    
    InputData(){
        // Input files reader
        //Demand.csv
        String FileName="Demand.csv";
        Scanner scanner=null;
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        String line=scanner.nextLine();
        StringTokenizer stk;
        Vector<Integer> v=new Vector<>();
        while(scanner.hasNext()){
            line=scanner.nextLine();
            stk=new StringTokenizer(line,",");
            stk.nextToken();
            v.addElement(Integer.valueOf(stk.nextToken()));
        }
        this.AreasCounter=v.size();
        this.DemandsSet=v.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        this.TotalVolume=IntStream.of(this.DemandsSet).sum();
        scanner.close();
        
        //Fleets.csv
        FileName="Fleets.csv";
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        scanner.nextLine();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        v.removeAllElements();
        while(stk.hasMoreElements())
            v.addElement(Integer.valueOf(new StringTokenizer(stk.nextToken(),"%").nextToken()));
        this.FleetsCounter=v.size();
        this.SharedVolumeRatio=v.stream().flatMapToDouble(x->DoubleStream.of(x/100d)).toArray();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        v.removeAllElements();
        while(stk.hasMoreElements())
            v.addElement(Integer.valueOf(stk.nextToken()));
        this.MaxCapacity=v.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        v.removeAllElements();
        while(stk.hasMoreElements())
            v.addElement(Integer.valueOf(stk.nextToken()));
        this.MaxContractedVolume=v.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        v.removeAllElements();
        while(stk.hasMoreElements())
            v.addElement(Integer.valueOf(stk.nextToken()));
        this.MinVolume=v.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        v.removeAllElements();
        while(stk.hasMoreElements())
            v.addElement(Integer.valueOf(stk.nextToken()));
        this.GreenCapacity=v.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        line=scanner.nextLine();
        stk=new StringTokenizer(line,",");
        stk.nextToken();
        Vector<Double> vect=new Vector<>();
        while(stk.hasMoreElements())
            vect.addElement(Double.valueOf(stk.nextToken()));
        this.Costs=vect.stream().flatMapToDouble(x->DoubleStream.of(x)).toArray();
        scanner.close();
        this.ServiceQuality=new double[this.AreasCounter][this.FleetsCounter];
        
        //FleetAreaConstraints.csv
        FileName="FleetAreaConstraints.csv";
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        scanner.nextLine();
        for(int i=0;i<this.AreasCounter;i++){
            line=scanner.nextLine();
            stk=new StringTokenizer(line,",");
            stk.nextToken();
            for(int j=0;j<this.FleetsCounter;j++)
                this.ServiceQuality[i][j]=("yes".equals(stk.nextToken()))?1d:0d;
        }
        scanner.close();
        
        //DSR.csv
        FileName="DSR.csv";
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        scanner.nextLine();
        for(int i=0;i<this.AreasCounter;i++){
            line=scanner.nextLine();
            stk=new StringTokenizer(line,",");
            stk.nextToken();
            for(int j=0;j<this.FleetsCounter;j++)
                if(this.ServiceQuality[i][j]>0d)
                    this.ServiceQuality[i][j]*=Double.valueOf(new StringTokenizer(stk.nextToken(),"%").nextToken())/100d;
                else
                    stk.nextToken();
        }
        scanner.close();
        
        //Delayed.csv
        FileName="Delayed.csv";
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        scanner.nextLine();
        for(int i=0;i<this.AreasCounter;i++){
            line=scanner.nextLine();
            stk=new StringTokenizer(line,",");
            stk.nextToken();
            for(int j=0;j<this.FleetsCounter;j++)
                if(this.ServiceQuality[i][j]>0d)
                    this.ServiceQuality[i][j]*=1-Double.valueOf(new StringTokenizer(stk.nextToken(),"%").nextToken())/100d;
                else
                    stk.nextToken();
        }
        scanner.close();
        for(int i=0;i<this.AreasCounter;i++)
            for(int j=0;j<this.FleetsCounter;j++)
                if(this.ServiceQuality[i][j]>0d)
                    this.ServiceQuality[i][j]/=(this.MaxContractedVolume[j]-this.GreenCapacity[j])/(double)this.MaxContractedVolume[j];

        //ParcelsPerH.csv
        FileName="ParcelsPerH.csv";
        try {
            scanner=new Scanner(new FileInputStream(FileName));
        }catch(FileNotFoundException e){
            System.out.println(FileName+" file is missing");
            System.exit(0); 
        }
        scanner.nextLine();
        this.Productivity=new double[this.AreasCounter][this.FleetsCounter];
        for(int i=0;i<this.AreasCounter;i++){
            line=scanner.nextLine();
            stk=new StringTokenizer(line,",");
            stk.nextToken();
            for(int j=0;j<this.FleetsCounter;j++)
                this.Productivity[i][j]=Double.valueOf(stk.nextToken());
        }
        scanner.close();
        this.MaxVolume=new int[this.FleetsCounter];
        for(int j=0;j<this.FleetsCounter;j++){
            this.MaxVolume[j]=(int)Math.min(Math.ceil(this.SharedVolumeRatio[j]*this.TotalVolume),this.MaxContractedVolume[j]);
            this.MaxVolume[j]=(int)Math.min(this.MaxVolume[j],this.MaxCapacity[j]);
        }
    }
    
    
    void ServiceQualtiyFile() throws IOException{
        //Service Qualtiy indicator file
        String FileName="Service Qualtiy indicators.csv";
        try(BufferedWriter OutputFile=new BufferedWriter(new FileWriter(FileName))){
            String str="Postcode,";
            for(int i=0;i<this.FleetsCounter;i++)
                str+=(i+1<this.FleetsCounter)?"Fleet"+(i+1)+",":"Fleet"+(i+1);
            OutputFile.write(str);
            OutputFile.newLine();
            for(int i=0;i<this.AreasCounter;i++){
                str=(i+1)+",";
                for(int j=0;j<this.FleetsCounter;j++)
                    str+=(j+1<this.FleetsCounter)?this.ServiceQuality[i][j]+",":this.ServiceQuality[i][j];
                OutputFile.write(str);
                OutputFile.newLine();
            }
            str="Max Volume,";
            for(int j=0;j<this.FleetsCounter;j++)
                str+=(j+1<this.FleetsCounter)?this.MaxVolume[j]+",":this.MaxVolume[j];
            OutputFile.write(str);
            OutputFile.newLine();
        }
    }
}
