package mindthegap.model;

import java.io.File;
import java.io.IOException;

import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.DatabaseLoader;
import weka.core.converters.LibSVMLoader;


import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Model {
	
	 
	
	
	Bagging bagging;
	
	public Model(){	
		bagging = new Bagging();			
		bagging.setNumExecutionSlots(12);
		//bagging.setBagSizePercent(90);
					
	}


	public void train(Instances trainData) {
		try {
			bagging.buildClassifier(trainData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Double> classify(Instances testData) {
		
		List<Double> classes = new ArrayList<>(); 		
		try {
			for (Instance instance : testData) {
				classes.add(bagging.classifyInstance(instance));
			}			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classes;
	}

}

