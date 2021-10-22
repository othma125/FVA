/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fva;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 *
 * @author Othmane
 */
public class HeuristicSolution {
    private boolean[] Chromosome;
    private int[][] ParcelsAssignment;
    private int[] SumParcelsPerFleet;
    private double[] SumCostPerFleet;
    private double[] SumCostPerArea;
    private double[] SumProductivityPerFleet;
    private BiObjectiveFunction Fitness;
    private boolean IsFeasible=true;
    
    static Vector<HeuristicSolution> GeneticAlgorithm(InputData data,double RunTime/*in Seconds*/) throws InterruptedException, Throwable{
        long StartTime=System.currentTimeMillis();
        int PopulationSize=20;
        HeuristicSolution[] population=HeuristicSolution.InitialPopulation(data,PopulationSize);
        System.out.println(population[0].toString()+" after "+(System.currentTimeMillis()-StartTime)+" ms of running time");
        HeuristicSolution newSolution;
        long t=(long)(RunTime*1000l);
        while(System.currentTimeMillis()-StartTime<=t){
            if(Math.random()<0.8d)
                HeuristicSolution.Crossover(data,StartTime,population);
            else{
                do{
                    newSolution=new HeuristicSolution(data);
                }while(!newSolution.IsFeasible);
                newSolution.LocalSearch(data);
                HeuristicSolution.UpdatePopulation(population,newSolution,StartTime);
            }
        }
        return HeuristicSolution.ParetoSet(population);
    }
    
    public static Vector<HeuristicSolution> ParetoSet(HeuristicSolution[] population){
        Vector<HeuristicSolution> ParetoSet=new Vector<>();
        double min=0d;
        for(HeuristicSolution s:population){
            if(s.getTotalProductivity()>min){
                min=s.getTotalProductivity();
                ParetoSet.addElement(s);
            }
            else
                return ParetoSet;
        }
        return null;
    }
    
    static HeuristicSolution[] InitialPopulation(InputData data,int PopulationSize) throws InterruptedException, Throwable{
        //Random created population
        HeuristicSolution[] population=new HeuristicSolution[PopulationSize];
        int i=0;
        System.out.println("Initial population");
        while(i<PopulationSize){
            do{
                population[i]=new HeuristicSolution(data);
            }while(!population[i].IsFeasible);
            population[i].ShowSolution();
            i++;
        }
        Arrays.sort(population,(s1,s2)->s1.Compare(s2));
        System.out.println();
        return population;
    }
    
    static void UpdatePopulation(HeuristicSolution[] population,HeuristicSolution newSolution,long StartTime) throws Throwable{
        // Add new individual to population
        if(newSolution.IsFeasible && (newSolution.getTotalCost()<population[population.length-1].getTotalCost()
                                        || newSolution.getTotalProductivity()<population[population.length-1].getTotalProductivity())){
            int half=population.length/2;
            int i=half+(int)(Math.random()*(population.length-half));
            population[i].finalize();
            population[i]=newSolution;
            if(newSolution.Improve(population[0]))
                System.out.println(newSolution.toString()+" after "+(System.currentTimeMillis()-StartTime)+" ms of running time");
            Arrays.sort(population,(s1,s2)->s1.Compare(s2));
        }
        else
            newSolution.finalize();
    }
    
    private static void Crossover(InputData data,long StartTime,HeuristicSolution ... population) throws Throwable{
        //Parents selection
        int half=population.length/2,i=(int)(Math.random()*half),j;
        if(Math.random()<0.7d)
            do{
                j=(int)(Math.random()*half);
            }while(i==j);
        else
            j=half+(int)(Math.random()*(population.length-half));
        //Crossover
        HeuristicSolution.Crossover(data,StartTime,population,population[i],population[j]);
    }
    
    private static void Crossover(InputData data,long StartTime,HeuristicSolution[] population,HeuristicSolution ... parents) throws InterruptedException, Throwable{
        HeuristicSolution SecondChild=null;
        boolean[] CutPoints=new boolean[data.AreasCounter*data.FleetsCounter];
        for(int i=0;i<CutPoints.length;i++)
            CutPoints[i]=Math.random()<0.5d;
        Thread t;
        t=new Thread(()->{
            HeuristicSolution FirstChild=null;
            boolean c=true;
            do{
                if(c)
                    FirstChild=parents[0].Crossover(data,parents[1],CutPoints);
                else{
                    try{
                        FirstChild.finalize();
                    }catch(Throwable ex){
                        Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    FirstChild=new HeuristicSolution(data);
                }
                FirstChild.LocalSearch(data);
                c=false;
            }while(!FirstChild.IsFeasible);
            try {
                HeuristicSolution.UpdatePopulation(population,FirstChild,StartTime);
            } catch (Throwable ex) {
                Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        t.start();
        boolean c=true;
        do{
            if(c)
                SecondChild=parents[1].Crossover(data,parents[0],CutPoints);
            else{
                SecondChild.finalize();
                SecondChild=new HeuristicSolution(data);
            }
            SecondChild.LocalSearch(data);
            c=false;
        }while(!SecondChild.IsFeasible);
        t.join();
        HeuristicSolution.UpdatePopulation(population,SecondChild,StartTime);
    }
    
    private HeuristicSolution Crossover(InputData data,HeuristicSolution Father,boolean ... cut_points){
        boolean[] chromosome=new boolean[Father.Chromosome.length];
        for(int i=0;i<chromosome.length;i++)
            chromosome[i]=(cut_points[i])?this.Chromosome[i]:Father.Chromosome[i];
        return new HeuristicSolution(data,chromosome);
    }

    HeuristicSolution(InputData data){
        //random solution
        this.Chromosome=new boolean[data.AreasCounter*data.FleetsCounter];
        this.ParcelsAssignment=new int[data.AreasCounter][data.FleetsCounter];
        this.SumParcelsPerFleet=new int[data.FleetsCounter];
        this.SumCostPerFleet=new double[data.FleetsCounter];
        this.SumCostPerArea=new double[data.AreasCounter];
        this.SumProductivityPerFleet=new double[data.FleetsCounter];
        int[] order=IntStream.range(0,data.AreasCounter).toArray();
        for(int i=0;i<data.AreasCounter;i++){
            int j=(int)(Math.random()*data.AreasCounter);
            int aux=order[i];
            order[i]=order[j];
            order[j]=aux;
        }
        for(int area:order){
            double[] Probabilities=IntStream.range(0,data.FleetsCounter).mapToDouble(fleet->{
                double probability;
                if(this.SumParcelsPerFleet[fleet]<data.MinVolume[fleet])
                    probability=1d-this.SumParcelsPerFleet[fleet]/(double)data.MinVolume[fleet];
                else
                    probability=(this.SumParcelsPerFleet[fleet]-data.MinVolume[fleet])/(double)(data.MaxVolume[fleet]-data.MinVolume[fleet]);
                return Math.max(0.1d,probability);
            }).toArray();
            int[] assigned_volumes;
            boolean[] SelectionConditions;
            int demand;
            int MinUsedFleets=1;
            do{
                demand=data.DemandsSet[area];
                int MaxAvailableFleets;
                int FleetsCounter;
                double SumWeights;
                do{
                    MaxAvailableFleets=0;
                    FleetsCounter=0;
                    SumWeights=0d;
                    SelectionConditions=new boolean[data.FleetsCounter];
                    for(int j=0;j<data.FleetsCounter;j++){
                        if(data.ServiceQuality[area][j]>0d && this.SumParcelsPerFleet[j]<data.MaxVolume[j])
                            MaxAvailableFleets++;
                        SelectionConditions[j]=data.ServiceQuality[area][j]>0d && this.SumParcelsPerFleet[j]<data.MaxVolume[j] && Math.random()<Probabilities[j];
                        if(SelectionConditions[j]){
                            SumWeights+=data.ServiceQuality[area][j];
                            FleetsCounter++;
                        }
                    }
                    if(MaxAvailableFleets==0){
                        this.IsFeasible=false;
                        return;
                    }
                }while(FleetsCounter<MinUsedFleets);
                assigned_volumes=new int[data.FleetsCounter];
                for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                    if(SelectionConditions[fleet]){
                        assigned_volumes[fleet]=(FleetsCounter==1)?demand:(int)((data.ServiceQuality[area][fleet]/SumWeights)*demand);
                        if(this.SumParcelsPerFleet[fleet]+assigned_volumes[fleet]>data.MaxVolume[fleet]){
                            if(MaxAvailableFleets==1){
                                this.IsFeasible=false;
                                return;
                            }
                            else{ 
                                MinUsedFleets++;
                                if(MinUsedFleets>MaxAvailableFleets){
                                    this.IsFeasible=false;
                                    return;
                                }
                                break;
                            }
                        }
                        else{
                            demand-=assigned_volumes[fleet];
                            SumWeights-=data.ServiceQuality[area][fleet];
                            FleetsCounter--;
                        }
                    } 
            }while(demand>0);
            for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                if(SelectionConditions[fleet] && assigned_volumes[fleet]>0){
                    this.Chromosome[area*data.FleetsCounter+fleet]=true;
                    this.ParcelsAssignment[area][fleet]=assigned_volumes[fleet];
                    this.SumParcelsPerFleet[fleet]+=this.ParcelsAssignment[area][fleet];
                    this.SumCostPerFleet[fleet]+=data.Costs[fleet]*this.ParcelsAssignment[area][fleet];
                    this.SumCostPerArea[area]+=data.Costs[fleet]*this.ParcelsAssignment[area][fleet];
                    this.SumProductivityPerFleet[fleet]+=data.Productivity[area][fleet]/this.ParcelsAssignment[area][fleet];
                }
        }
        this.setFitness();
    }

    HeuristicSolution(InputData data,boolean[] chr){
        this.Chromosome=chr;
        this.setVolumesAssignment(data);
    }

    private void setVolumesAssignment(InputData data){
        // Percels assignment calculation and chromosome feasibility test
        this.ParcelsAssignment=new int[data.AreasCounter][data.FleetsCounter];
        this.SumParcelsPerFleet=new int[data.FleetsCounter];
        this.SumCostPerFleet=new double[data.FleetsCounter];
        this.SumCostPerArea=new double[data.AreasCounter];
        this.SumProductivityPerFleet=new double[data.FleetsCounter];
        int area=-1;
        int demand=0;
        int FleetsCounter=0;
        double SumWeights=0d;
        for(int gene=0;gene<this.Chromosome.length;gene++){
            int fleet=gene%data.FleetsCounter;
            if(gene/data.FleetsCounter>area){
                area=gene/data.FleetsCounter;
                if(demand>0){
                    this.IsFeasible=false;
                    return;
                }
                demand=data.DemandsSet[area];
                SumWeights=0d;
                FleetsCounter=0;
                for(int j=0;j<data.FleetsCounter;j++)
                    if(this.Chromosome[j+data.FleetsCounter*area]){
                        if(data.ServiceQuality[area][j]>0d){
                            SumWeights+=data.ServiceQuality[area][j];
                            FleetsCounter++;
                        }
                        else
                            this.Chromosome[j+data.FleetsCounter*area]=false;
                    }
            }
            if(this.Chromosome[gene]){
                int volume=(FleetsCounter==1)?demand:(int)((data.ServiceQuality[area][fleet]/SumWeights)*demand);
                demand-=volume;
                FleetsCounter--;
                this.ParcelsAssignment[area][fleet]=volume;
                this.SumParcelsPerFleet[fleet]+=this.ParcelsAssignment[area][fleet];
                if(this.SumParcelsPerFleet[fleet]>data.MaxVolume[fleet]){
                    this.IsFeasible=false;
                    return;
                }
                this.SumCostPerFleet[fleet]+=data.Costs[fleet]*this.ParcelsAssignment[area][fleet];
                this.SumCostPerArea[area]+=data.Costs[fleet]*this.ParcelsAssignment[area][fleet];
                this.SumProductivityPerFleet[fleet]+=data.Productivity[area][fleet]/this.ParcelsAssignment[area][fleet];
            }
        }
        if(IntStream.range(0,data.FleetsCounter).anyMatch(j->this.SumParcelsPerFleet[j]<data.MaxVolume[j])){
            this.IsFeasible=false;
            return;
        }
        this.setFitness();
    }
    
    void LocalSearch(InputData data){
        // local search to improve the volume assignment of the current solution
        if(!this.IsFeasible)
            return;
        double cost=this.Fitness.TotalCost;
        int[] AreasSet=IntStream.range(0,data.AreasCounter).toArray();
        for(int i=0;i<data.AreasCounter;i++){
            int j=(int)(Math.random()*data.AreasCounter);
            int aux=AreasSet[i];
            AreasSet[i]=AreasSet[j];
            AreasSet[j]=aux;
        }
        for(int area:AreasSet){
            int[] order=this.getAssignmentOrder(data,area);
            for(int fleet1=0;fleet1<data.FleetsCounter;fleet1++)
                if(this.Chromosome[fleet1+area*data.FleetsCounter]){
                    int index1=HeuristicSolution.getIndex(order,fleet1);
                    int min1=(index1+1<order.length)?this.ParcelsAssignment[area][order[index1+1]]:0;
                    int max1=(index1>0)?this.ParcelsAssignment[area][order[index1-1]]:data.DemandsSet[area];
                    int exchanged_volume;
                    for(int fleet2=fleet1+1;fleet2<data.FleetsCounter;fleet2++)
                        if(this.Chromosome[fleet2+area*data.FleetsCounter]){
                            int index2=HeuristicSolution.getIndex(order,fleet2);
                            int min2=(index2+1<order.length)?this.ParcelsAssignment[area][order[index2+1]]:0;
                            int max2=(index2>0)?this.ParcelsAssignment[area][order[index2-1]]:data.DemandsSet[area];
                            if(index2>index1){
                                exchanged_volume=this.ParcelsAssignment[area][fleet2]-min2;
                                if(exchanged_volume+this.ParcelsAssignment[area][fleet1]<=max1
                                    && exchanged_volume+this.SumParcelsPerFleet[fleet1]<=data.MaxVolume[fleet1]
                                    && -exchanged_volume+this.SumParcelsPerFleet[fleet2]>=data.MinVolume[fleet2]
                                    && this.getCostGain(data,fleet1,fleet2,exchanged_volume)<0d){
                                    if(min2>0)
                                        this.VolumeExchange(data,area,fleet1,fleet2,exchanged_volume);
                                    else{
                                        this.Chromosome[fleet2+area*data.FleetsCounter]=false;
                                        this.SumCostPerArea[area]+=this.getCostGain(data,fleet1,fleet2,exchanged_volume);
                                        this.SumParcelsPerFleet[fleet1]+=exchanged_volume;
                                        this.SumCostPerFleet[fleet1]+=data.Costs[fleet1]*exchanged_volume;
                                        this.SumProductivityPerFleet[fleet1]-=(data.Productivity[area][fleet1]*exchanged_volume)/(this.ParcelsAssignment[area][fleet1]*(exchanged_volume+this.ParcelsAssignment[area][fleet1]));
                                        this.ParcelsAssignment[area][fleet1]+=exchanged_volume;
                                        this.SumParcelsPerFleet[fleet2]-=exchanged_volume;
                                        this.SumCostPerFleet[fleet2]-=data.Costs[fleet2]*exchanged_volume;
                                        this.SumProductivityPerFleet[fleet2]-=data.Productivity[area][fleet2]/exchanged_volume;
                                        this.ParcelsAssignment[area][fleet2]-=exchanged_volume;
                                    }
                                    continue;
                                }
                                if(index2>index1+1){
                                    exchanged_volume=this.ParcelsAssignment[area][fleet1]-min1;
                                    if(exchanged_volume+this.ParcelsAssignment[area][fleet2]<=max2
                                        && exchanged_volume+this.SumParcelsPerFleet[fleet2]<=data.MaxVolume[fleet2]
                                        && -exchanged_volume+this.SumParcelsPerFleet[fleet1]>=data.MinVolume[fleet1]
                                        && this.getCostGain(data,fleet1,fleet2,-exchanged_volume)<0d){
                                        this.VolumeExchange(data,area,fleet1,fleet2,-exchanged_volume);
                                        break;
                                    }
                                }
                                else{
                                    exchanged_volume=-this.ParcelsAssignment[area][fleet2]+this.ParcelsAssignment[area][fleet1];
                                    exchanged_volume=(int)(exchanged_volume/2d);
                                    if(exchanged_volume+this.SumParcelsPerFleet[fleet2]<=data.MaxVolume[fleet2]
                                        && -exchanged_volume+this.SumParcelsPerFleet[fleet1]>=data.MinVolume[fleet1]
                                        && this.getCostGain(data,fleet1,fleet2,-exchanged_volume)<0d){
                                        this.VolumeExchange(data,area,fleet1,fleet2,-exchanged_volume);
                                        break;
                                    }
                                }
                            }
                            else{
                                if(index2<index1-1){
                                    exchanged_volume=this.ParcelsAssignment[area][fleet2]-min2;
                                    if(exchanged_volume+this.ParcelsAssignment[area][fleet1]<=max1
                                        && exchanged_volume+this.SumParcelsPerFleet[fleet1]<=data.MaxVolume[fleet1]
                                        && -exchanged_volume+this.SumParcelsPerFleet[fleet2]>=data.MinVolume[fleet2]
                                        && this.getCostGain(data,fleet1,fleet2,exchanged_volume)<0d){
                                        this.VolumeExchange(data,area,fleet1,fleet2,exchanged_volume);
                                        order=this.getAssignmentOrder(data,area);
                                        index1=HeuristicSolution.getIndex(order,fleet1);
                                        min1=(index1+1<order.length)?this.ParcelsAssignment[area][order[index1+1]]:0;
                                        max1=(index1>0)?this.ParcelsAssignment[area][order[index1-1]]:data.DemandsSet[area];
                                        continue;
                                    }
                                }
                                else{
                                    exchanged_volume=this.ParcelsAssignment[area][fleet2]-this.ParcelsAssignment[area][fleet1];
                                    exchanged_volume=(int)(exchanged_volume/2d);
                                    if(exchanged_volume+this.SumParcelsPerFleet[fleet1]<=data.MaxVolume[fleet1]
                                        && -exchanged_volume+this.SumParcelsPerFleet[fleet2]>=data.MinVolume[fleet2]
                                        && this.getCostGain(data,fleet1,fleet2,exchanged_volume)<0d){
                                        this.VolumeExchange(data,area,fleet1,fleet2,exchanged_volume);
                                        continue;
                                    }
                                }
                                exchanged_volume=this.ParcelsAssignment[area][fleet1]-min1;
                                if(exchanged_volume+this.ParcelsAssignment[area][fleet2]<=max2
                                    && exchanged_volume+this.SumParcelsPerFleet[fleet2]<=data.MaxVolume[fleet2]
                                    && -exchanged_volume+this.SumParcelsPerFleet[fleet1]>=data.MinVolume[fleet1]
                                    && this.getCostGain(data,fleet1,fleet2,-exchanged_volume)<0d){
                                    if(min1>0)
                                        this.VolumeExchange(data,area,fleet1,fleet2,-exchanged_volume);
                                    else{
                                        this.Chromosome[fleet1+area*data.FleetsCounter]=false;
                                        this.SumCostPerArea[area]+=this.getCostGain(data,fleet1,fleet2,-exchanged_volume);
                                        this.SumParcelsPerFleet[fleet1]-=exchanged_volume;
                                        this.SumCostPerFleet[fleet1]-=data.Costs[fleet1]*exchanged_volume;
                                        this.SumProductivityPerFleet[fleet1]-=data.Productivity[area][fleet2]/exchanged_volume;
                                        this.ParcelsAssignment[area][fleet1]-=exchanged_volume;
                                        this.SumParcelsPerFleet[fleet2]+=exchanged_volume;
                                        this.SumCostPerFleet[fleet2]+=data.Costs[fleet2]*exchanged_volume;
                                        this.SumProductivityPerFleet[fleet2]-=(data.Productivity[area][fleet2]*exchanged_volume)/(this.ParcelsAssignment[area][fleet2]*(exchanged_volume+this.ParcelsAssignment[area][fleet2]));
                                        this.ParcelsAssignment[area][fleet2]+=exchanged_volume;
                                    }
                                    break;
                                }
                            }
                        }
                }
                else if(data.ServiceQuality[area][fleet1]>0d){
                    int FleetsCounter=0;
                    double SumWeights=0d;
                    for(int j=0;j<data.FleetsCounter;j++)
                        if(this.Chromosome[j+data.FleetsCounter*area] || j==fleet1){
                            FleetsCounter++;
                            SumWeights+=data.ServiceQuality[area][j];
                        }
                    int d=(int)((data.ServiceQuality[area][fleet1]/SumWeights)*data.DemandsSet[area]);
                    if(d>0d && d+this.SumParcelsPerFleet[fleet1]<=data.MaxVolume[fleet1]){
                        int[] exchanged_volumes=new int[data.FleetsCounter];
                        int demand=d;
                        double gain=data.Costs[fleet1]*d;
                        FleetsCounter--;
                        SumWeights-=data.ServiceQuality[area][fleet1];
                        for(int j=0;j<data.FleetsCounter;j++)
                            if(this.Chromosome[j+data.FleetsCounter*area]){
                                int volume=(FleetsCounter==1)?demand:(int)((data.ServiceQuality[area][j]/SumWeights)*demand);
                                if(this.SumParcelsPerFleet[j]-volume<data.MinVolume[j] || this.ParcelsAssignment[area][j]<=volume){
                                    gain=0d;
                                    break;
                                }
                                demand-=volume;
                                exchanged_volumes[j]=volume;
                                FleetsCounter--;
                                gain-=data.Costs[j]*volume;
                                SumWeights-=data.ServiceQuality[area][j];
                            }
                        if(gain<0d){
                            for(int j=0;j<data.FleetsCounter;j++)
                                if(this.Chromosome[j+data.FleetsCounter*area]){
                                    this.SumParcelsPerFleet[j]-=exchanged_volumes[j];
                                    this.SumCostPerFleet[j]-=data.Costs[j]*exchanged_volumes[j];
                                    this.SumProductivityPerFleet[j]+=(data.Productivity[area][j]*exchanged_volumes[j])/(this.ParcelsAssignment[area][j]*(-exchanged_volumes[j]+this.ParcelsAssignment[area][j]));
                                    this.ParcelsAssignment[area][j]-=exchanged_volumes[j];
                                }
                            this.SumParcelsPerFleet[fleet1]+=d;
                            this.SumCostPerFleet[fleet1]+=data.Costs[fleet1]*d;
                            this.SumProductivityPerFleet[fleet1]+=data.Productivity[area][fleet1]/d;
                            this.ParcelsAssignment[area][fleet1]=d;
                            this.SumCostPerArea[area]+=gain;
                            this.Chromosome[fleet1+area*data.FleetsCounter]=true;
                            order=this.getAssignmentOrder(data,area);
                        }
                    }
                }
        }
        this.setFitness();
        if(this.Fitness.TotalCost<cost)
            this.LocalSearch(data);
    }
    
    double getCostGain(InputData data,int fleet1,int fleet2,int exchanged_volume){
        return (data.Costs[fleet1]-data.Costs[fleet2])*exchanged_volume;
    }
    
    void VolumeExchange(InputData data,int area,int fleet1,int fleet2,int exchanged_volume){
        this.SumCostPerArea[area]+=this.getCostGain(data,fleet1,fleet2,exchanged_volume);
        this.SumParcelsPerFleet[fleet1]+=exchanged_volume;
        this.SumCostPerFleet[fleet1]+=data.Costs[fleet1]*exchanged_volume;
        this.SumProductivityPerFleet[fleet1]-=(data.Productivity[area][fleet1]*exchanged_volume)/(this.ParcelsAssignment[area][fleet1]*(exchanged_volume+this.ParcelsAssignment[area][fleet1]));
        this.ParcelsAssignment[area][fleet1]+=exchanged_volume;
        this.SumParcelsPerFleet[fleet2]-=exchanged_volume;
        this.SumCostPerFleet[fleet2]-=data.Costs[fleet2]*exchanged_volume;
        this.SumProductivityPerFleet[fleet2]+=(data.Productivity[area][fleet2]*exchanged_volume)/(this.ParcelsAssignment[area][fleet2]*(-exchanged_volume+this.ParcelsAssignment[area][fleet2]));
        this.ParcelsAssignment[area][fleet2]-=exchanged_volume;
    }
    
    int[] getAssignmentOrder(InputData data,int area){
        int[] order=IntStream.range(0,data.FleetsCounter).toArray();
        for(int i=0;i<order.length;i++)
            for(int j=i+1;j<order.length;j++)
                if(this.ParcelsAssignment[area][order[i]]<=this.ParcelsAssignment[area][order[j]]){
                    int aux=order[j];
                    order[j]=order[i];
                    order[i]=aux;
                }
        return order;
    }
    
    static int getIndex(int[] order,int f){
        int index=0;
        while(index<order.length){
            if(order[index]==f)
                return index;
            index++;
        }
        return 0;
    }
    
    void setOutputFile(InputData data) throws IOException{
        //Output file
        String FileName=this.Fitness.toString()+".csv";
        try(BufferedWriter OutputFile=new BufferedWriter(new FileWriter(FileName))){
            String str="PostCode,Parcels,";
            for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                str+="Fleet"+(fleet+1)+",";
            str+="Sum cost per area";
            OutputFile.write(str);
            OutputFile.newLine();
            for(int area=0;area<data.AreasCounter;area++){
                str=(area+1)+","+data.DemandsSet[area]+",";
                for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                    str+=this.ParcelsAssignment[area][fleet]+",";
                str+=this.SumCostPerArea[area];
                OutputFile.write(str);
                OutputFile.newLine();
            }
            str=",Volume Assigned per fleet,";
            for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                str+=(fleet+1<data.FleetsCounter)?this.SumParcelsPerFleet[fleet]+" Euros,":this.SumParcelsPerFleet[fleet]+" Euros";
            OutputFile.write(str);
            OutputFile.newLine();
            str=",Sum cost per fleet,";
            for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                str+=this.SumCostPerFleet[fleet]+" Euros,";
            str+=this.Fitness.getTotalCost()+" Euros";
            OutputFile.write(str);
            OutputFile.newLine();
            str=",Sum Productivity per fleet,";
            for(int fleet=0;fleet<data.FleetsCounter;fleet++)
                str+=this.SumProductivityPerFleet[fleet]+" per Hour,";
            str+=this.Fitness.getTotalProductivity()+" per Hour";
            OutputFile.write(str);
            OutputFile.newLine();
        }
    }
    
    int Compare(HeuristicSolution s){
        return (int)((this.getTotalCost()-s.getTotalCost())*10d);
    }
    
    @Override
    public String toString(){
        return this.Fitness.toString();
    }
    
    void ShowSolution(){
        System.out.println(this.Fitness.toString());
    }
    
    void setFitness(){
        this.Fitness=new BiObjectiveFunction(DoubleStream.of(this.SumCostPerArea).sum(),DoubleStream.of(this.SumProductivityPerFleet).sum());
    }
    
    boolean Improve(HeuristicSolution s){
        return this.Fitness.Improve(s.Fitness);
    }

    double getTotalCost() {
        return this.Fitness.getTotalCost();
    }

    double getTotalProductivity() {
        return this.Fitness.getTotalProductivity();
    }
}