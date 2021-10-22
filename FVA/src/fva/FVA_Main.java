/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fva;

import ilog.concert.IloException;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Othmane
 */
public class FVA_Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IloException, Throwable {
        // TODO code application logic here
        System.out.println();
        InputData data=new InputData();
        data.ServiceQualtiyFile();
//        new CplexModel(data).Solve();
        Vector<HeuristicSolution> ParetoSet=HeuristicSolution.GeneticAlgorithm(data, 10d/*running time in seconds*/);
        System.out.println("Pareto Set:");
        ParetoSet.forEach(s->{
            s.ShowSolution();
//            try {
//                s.setOutputFile(data);
//            } catch (IOException ex) {
//                Logger.getLogger(FVA_Main.class.getName()).log(Level.SEVERE, null, ex);
//            }
        });
    }
}