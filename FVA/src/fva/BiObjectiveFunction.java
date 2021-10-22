/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fva;

/**
 *
 * @author Othmane
 */
public class BiObjectiveFunction{
    public double TotalCost;
    public double TotalProductivity;

//    BiObjectiveFunction(){
//        this(Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY);
//    }

    BiObjectiveFunction(double tc,double pr) {
        this.TotalCost=tc;
        this.TotalProductivity=pr;
    }
    
    @Override
    public String toString(){
        return "(Total Cost = "+this.getTotalCost()+" Euros, Total Productivity = "+this.getTotalProductivity()+" Per Hour)";
    }
    
    public static double OneDigitAfterFloatingPoint(double x){
        double y=10*x;
        return ((int)y)/10d;
    }
    
    int getTotalCost(){
        return (int)this.TotalCost;
    }
    
    double  getTotalProductivity(){
        return BiObjectiveFunction.OneDigitAfterFloatingPoint(this.TotalProductivity);
    }
    
    boolean Improve(BiObjectiveFunction F){
        return (this.getTotalCost()<F.getTotalCost() && this.getTotalProductivity()>F.getTotalProductivity())
                || (this.getTotalCost()==F.getTotalCost() && this.getTotalProductivity()>F.getTotalProductivity())
                || (this.getTotalCost()<F.getTotalCost() && this.getTotalProductivity()==F.getTotalProductivity());
    }
}