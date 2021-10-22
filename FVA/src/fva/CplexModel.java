//package fva;
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//
//import ilog.concert.IloException;
//import ilog.concert.IloIntVar;
//import ilog.concert.IloLinearIntExpr;
//import ilog.concert.IloNumExpr;
//import ilog.cplex.IloCplex;
//
///**
// *
// * @author Othmane
// */
//public class CplexModel {
//    private final IloCplex cplex;
//    private IloIntVar N[][];
//    private final int[][][] X;
//    private final int[][] Y;
//    
//    CplexModel(InputData data,double TargetedProductivity) throws IloException{
//        this.cplex=new IloCplex();
//        for(int i=0;i<data.AreasCounter;i++)
//            for(int j=0;j<data.FleetsCounter;j++)
//                this.N[i][j]=this.cplex.intVar(0,data.DemandsSet[i]);
//        this.X=new int[data.AreasCounter][data.FleetsCounter][data.FleetsCounter];
//        for(int i=0;i<data.AreasCounter;i++)
//            for(int j=0;j<data.FleetsCounter;j++)
//                for(int k=j;k<data.FleetsCounter;k++){
//                    this.X[i][j][k]=(data.ServiceQuality[i][j]>=data.ServiceQuality[i][k])?1:0;
//                    this.X[i][k][j]=(data.ServiceQuality[i][j]>data.ServiceQuality[i][k])?1-this.X[i][j][k]:this.X[i][j][k];
//                }
//        this.Y=new int[data.AreasCounter][data.FleetsCounter];
//        for(int i=0;i<data.AreasCounter;i++)
//            for(int j=0;j<data.FleetsCounter;j++)
//                this.Y[i][j]=(data.ServiceQuality[i][j]>0)?1:0;
//        this.ObjectiveFunctions(data);
//        this.TargetedProductivityConstraint(data,TargetedProductivity);
//        this.Constaint1(data);
//        this.Constaints2and3(data);
//        this.Constaint4(data);
//        this.Constaint5(data);
//    }
//    public void Solve() throws IloException{
//        if(this.cplex.solve()){
//            System.out.println("Solution status = "+this.cplex.getStatus());
//            System.out.println("Solution value = "+this.cplex.getObjValue());
//        }
//        this.cplex.end();
//    }
//    
////    private void TargetedProductivityConstraint(InputData data,double TargetedProductivity) throws IloException{
////        IloNumExpr exp=this.cplex.numExpr();
////        for(int i=0;i<data.AreasCounter;i++)
////            for(int j=0;j<data.FleetsCounter;j++)
////                exp=this.cplex.sum(exp,this.cplex.prod(data.Costs[j],this.N[i][j]));
////        this.cplex.addLe(exp,TargetedProductivity);
////    }
//    
//    private void ObjectiveFunctions(InputData data) throws IloException{
//        IloNumExpr obj=this.cplex.numExpr();
//        for(int i=0;i<data.AreasCounter;i++)
//            for(int j=0;j<data.FleetsCounter;j++)
//                obj=this.cplex.sum(obj,this.cplex.prod(this.N[i][j],data.Costs[j]));
//        this.cplex.addMinimize(obj);
//    }
//    
//    private void Constaint1(InputData data) throws IloException{
//        for(int i=0;i<data.AreasCounter;i++){
//            IloLinearIntExpr exp=this.cplex.linearIntExpr();
//            for(int j=0;j<data.FleetsCounter;j++)
//                exp.addTerm(this.N[i][j],1);
//            this.cplex.addEq(exp,data.DemandsSet[i]);
//        }
//    }
//    
//    private void Constaints2and3(InputData data) throws IloException{
//        for(int j=0;j<data.FleetsCounter;j++){
//            IloLinearIntExpr exp=this.cplex.linearIntExpr();
//            for(int i=0;i<data.AreasCounter;i++)
//                exp.addTerm(this.N[i][j],1);
//            this.cplex.addGe(exp,data.MinVolume[j]);
//            this.cplex.addLe(exp,data.MaxVolume[j]);
//        }
//    }
//    
//    private void Constaint4(InputData data) throws IloException{
//        for(int i=0;i<this.N.length;i++)
//            for(int j=0;j<this.N[0].length;j++)
//                for(int k=0;k<this.N[0].length;k++)
//                    this.cplex.addLe(this.N[i][k],this.cplex.sum(this.N[i][j],data.DemandsSet[i]*(1-this.X[i][j][k])));
//    }
//    
//    private void Constaint5(InputData data) throws IloException{
//        for(int i=0;i<this.N.length;i++)
//            for(int j=0;j<this.N[0].length;j++)
//                this.cplex.addLe(this.N[i][j],data.DemandsSet[i]*this.Y[i][j]);
//    }
//}