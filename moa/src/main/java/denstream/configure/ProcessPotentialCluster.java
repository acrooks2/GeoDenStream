package denstream.configure;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.yahoo.labs.samoa.instances.DenseInstance;

import moa.cluster.Clustering;
import moa.clusterers.denstream.MicroCluster;


public class ProcessPotentialCluster 
{
	/////////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	/////////////////////////////////////////////////////////////////////////////////
	public static ArrayList<Integer> fillPotentialCluster(moa.clusterers.denstream.WithDBSCAN den,
			int data_count, 
			double[] point_x_list, 
			double[] point_y_list,
			int[] point_id_list)
	{
		int potential_count = den.getMicroClusteringResult().size();
		ArrayList<Integer> all_involved_points = new ArrayList<Integer>();
		for (int iPC = 0; iPC < potential_count; iPC++) 
		{
			moa.clusterers.denstream.MicroCluster temp_cluster = 
					(moa.clusterers.denstream.MicroCluster)(den.getMicroClusteringResult().get(iPC));
			
			double[] cluster_center = temp_cluster.getCenter();
			int point_count = temp_cluster.related_point_idxs.size();
			for (int iPoint=0; iPoint<point_count; iPoint++)
			{
				double temp_x = point_x_list[temp_cluster.related_point_idxs.get(iPoint)];
				double temp_y = point_y_list[temp_cluster.related_point_idxs.get(iPoint)];
				int temp_point_idx = temp_cluster.related_point_idxs.get(iPoint);
				double temp_dist = (temp_x - cluster_center[0]) * (temp_x - cluster_center[0])
						+ (temp_y - cluster_center[1]) * (temp_y - cluster_center[1]);
				temp_cluster.related_points_dist.add(temp_dist);
				
				all_involved_points.add(temp_point_idx);
			}
		}

		///////////////////////////////////////////////////////////////////////////////////////////
		//
		//
		//
		///////////////////////////////////////////////////////////////////////////////////////////
		HashMap<Integer, Integer> overlap_point_dict=new HashMap<Integer, Integer>();
		ArrayList<ArrayList<Integer>> overlap_point_seq = new ArrayList<ArrayList<Integer>>();
		for (int iPC = 0; iPC < potential_count; iPC++) 
		{
			moa.clusterers.denstream.MicroCluster temp_cluster1 = 
					(moa.clusterers.denstream.MicroCluster)(den.getMicroClusteringResult().get(iPC));
			
			int point_count1 = temp_cluster1.related_point_idxs.size();
			for (int iPoint1=0; iPoint1<point_count1; iPoint1++)
			{
				double temp_x1 = point_x_list[temp_cluster1.related_point_idxs.get(iPoint1)];
				double temp_y1 = point_y_list[temp_cluster1.related_point_idxs.get(iPoint1)];
				int temp_point_idx1 = temp_cluster1.related_point_idxs.get(iPoint1);
				ArrayList<Integer> temp_seq = new ArrayList<Integer>();
				
				for (int jPC = iPC+1; jPC < potential_count; jPC++) 
				{
					moa.clusterers.denstream.MicroCluster temp_cluster2 = 
							(moa.clusterers.denstream.MicroCluster)(den.getMicroClusteringResult().get(jPC));

					int point_count2 = temp_cluster2.related_point_idxs.size();
					for (int iPoint2=0; iPoint2<point_count2; iPoint2++)
					{
						double temp_x2 = point_x_list[temp_cluster2.related_point_idxs.get(iPoint2)];
						double temp_y2 = point_y_list[temp_cluster2.related_point_idxs.get(iPoint2)];
						int temp_point_idx2 = temp_cluster2.related_point_idxs.get(iPoint2);
						
						if (Math.abs(temp_x1-temp_x2)<0.00001&&Math.abs(temp_y1-temp_y2)<0.00001)
						{
							if (overlap_point_dict.containsKey(temp_point_idx1)==false)
							{
								overlap_point_dict.put(temp_point_idx1, iPC);
								temp_seq.add(temp_point_idx1);
							}
							if (overlap_point_dict.containsKey(temp_point_idx2)==false)
							{
								overlap_point_dict.put(temp_point_idx2, jPC);
								temp_seq.add(temp_point_idx2);
							}
						}
					}
				}
				if (temp_seq.size()>0) overlap_point_seq.add(temp_seq);
			}
		}
		int overlap_count = 0;
		if (overlap_point_seq.isEmpty() == false)
			overlap_count = overlap_point_seq.size();
		
		for (int iOver=0; iOver<overlap_count; iOver++)
		{
			ArrayList<Integer> temp_list = overlap_point_seq.get(iOver);
			int cluster_count = temp_list.size();

			int p_idx = temp_list.get(0);
			int c_idx = overlap_point_dict.get(p_idx);
			moa.cluster.Cluster c = den.getMicroClusteringResult().get(c_idx);
			moa.clusterers.denstream.MicroCluster temp_cluster = (moa.clusterers.denstream.MicroCluster)c;
			
			int temp_point_idx_flag = temp_cluster.related_point_idxs.indexOf(p_idx);
			double min_dist = temp_cluster.related_points_dist.get(temp_point_idx_flag);
			int min_idx = c_idx;
			
			for (int iC=1; iC<cluster_count; iC++)
			{
				p_idx = temp_list.get(iC);
				c_idx = overlap_point_dict.get(p_idx);
				
				c = den.getMicroClusteringResult().get(c_idx);
				temp_cluster = (moa.clusterers.denstream.MicroCluster)c;

				temp_point_idx_flag = temp_cluster.related_point_idxs.indexOf(p_idx);
				double temp_dist = temp_cluster.related_points_dist.get(temp_point_idx_flag);
				if (temp_dist< min_dist)
				{
					temp_dist = min_dist;
					min_idx = c_idx;
				}
				temp_cluster.related_point_idxs.remove(temp_point_idx_flag);
				temp_cluster.related_points_dist.remove(temp_point_idx_flag);
			}

			c = den.getMicroClusteringResult().get(min_idx);
			temp_cluster = (moa.clusterers.denstream.MicroCluster)c;
			
			for (int iC=0; iC<cluster_count; iC++)
			{
				temp_cluster.related_point_idxs.add(temp_list.get(iC));				
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		//
		//
		//
		///////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<Integer> possible_point_idx_list = new ArrayList<Integer>();	
		int pruned_count = den.pruned_list.size();
		for (int iPruned=0; iPruned<pruned_count; iPruned++)
		{
			int temp_idx = den.pruned_list.get(iPruned);
			possible_point_idx_list.add(temp_idx);
		}
		den.pruned_list.clear();
		int outlier_count = den.o_micro_cluster.size();
		for (int iC=0; iC<outlier_count; iC++)
		{
			MicroCluster temp_c = (MicroCluster)(den.o_micro_cluster.get(iC));
			int temp_c_size = temp_c.related_point_idxs.size();
			for (int iPruned=0; iPruned<temp_c_size; iPruned++)
			{
				int temp_idx = temp_c.related_point_idxs.get(iPruned);
				possible_point_idx_list.add(temp_idx);
			}
		}
		Collections.sort(possible_point_idx_list); //should merge with arrival sequence

		ArrayList<Integer> remained_point_idx_list = new ArrayList<Integer>();
		double[] temp_coord = new double[2];
		for (int iPoint=0; iPoint<possible_point_idx_list.size(); iPoint++)
		{
			int temp_idx = possible_point_idx_list.get(iPoint);
			if (temp_idx<0 || temp_idx>=data_count) continue;
			double temp_x = point_x_list[temp_idx];
			double temp_y = point_y_list[temp_idx];
			temp_coord[0] = temp_x;
			temp_coord[1] = temp_y;
			DenseInstance temp_instance = new DenseInstance(1.0D, temp_coord);
			moa.clusterers.denstream.DenPoint temp_point = 
				new moa.clusterers.denstream.DenPoint(temp_instance, den.timestamp);
			
			MicroCluster x = den.nearestCluster(temp_point, den.p_micro_cluster);
			MicroCluster xCopy = x.copy();
			xCopy.tryInsert(temp_point, den.timestamp);
			if (xCopy.getRadius() <= den.epsilonOption.getValue()) 
			{
				x.related_point_idxs.add(point_id_list[temp_idx]);
				
				all_involved_points.add(point_id_list[temp_idx]);
			}
			else
			{
				remained_point_idx_list.add(point_id_list[temp_idx]);
			}
		}
		
		return remained_point_idx_list;
	}
	
	public static int getClusterIdWithCoord(double x, double y, 
			moa.clusterers.denstream.WithDBSCAN den,
			Clustering daily_cluster,
			double[] point_x_list, 
			double[] point_y_list)
	{
		int potential_count = den.getMicroClusteringResult().size();
		
		int nearest_cluster_idx = -1;
		double min_dist = 0;

		moa.cluster.Cluster c = null;
		moa.clusterers.denstream.MicroCluster temp_cluster = null;
		int start_idx=0;
		for (int iPC = 0; iPC < potential_count; iPC++)
		{
			c = den.getMicroClusteringResult().get(iPC);
			temp_cluster = (moa.clusterers.denstream.MicroCluster)c;
			if (temp_cluster.related_point_idxs.size()>0)
			{
				start_idx=iPC;
				break;
			}
		}
		double temp_x1 = point_x_list[temp_cluster.related_point_idxs.get(0)];
		double temp_y1 = point_y_list[temp_cluster.related_point_idxs.get(0)];
		min_dist = (x-temp_x1)*(x-temp_x1) + (y-temp_y1)*(y-temp_y1);
		nearest_cluster_idx = start_idx;
		
		for (int iPC = start_idx; iPC < potential_count; iPC++) 
		{
			if (min_dist<=0.000001)
			{
				nearest_cluster_idx = iPC;
				break;
			}
			c = den.getMicroClusteringResult().get(iPC);
			temp_cluster = (moa.clusterers.denstream.MicroCluster)c;
			if (temp_cluster.related_point_idxs.size() <= 0) continue;
			
			int point_count=temp_cluster.related_point_idxs.size();
			for (int iPP=1; iPP<point_count; iPP++)
			{
				temp_x1 = point_x_list[temp_cluster.related_point_idxs.get(iPP)];
				temp_y1 = point_y_list[temp_cluster.related_point_idxs.get(iPP)];
				double temp_dist = (x-temp_x1)*(x-temp_x1) + (y-temp_y1)*(y-temp_y1);
				if (temp_dist<=0.000001)
				{
					nearest_cluster_idx = iPC;
					min_dist = 0;
					break;
				}
				if (temp_dist<min_dist)
				{
					min_dist = temp_dist;
					nearest_cluster_idx = iPC;
				}
			}
			if (min_dist<=0.000001)
			{
				nearest_cluster_idx = iPC;
				break;
			}
		}
		moa.cluster.Cluster nearest_cluster = den.getMicroClusteringResult().get(nearest_cluster_idx);
		int nearest_id = -1;
		for (int iC=0; iC<daily_cluster.size(); iC++)
		{
			moa.clusterers.macro.NonConvexCluster temp_cluster1 = 
				(moa.clusterers.macro.NonConvexCluster)daily_cluster.getClustering().get(iC);
			int cluster_id = (int) temp_cluster1.getId();
			int micro_cluster_count = temp_cluster1.getMicroClusters().size();
			for (int iMicro = 0; iMicro < micro_cluster_count; iMicro++) 
			{
				moa.clusterers.denstream.MicroCluster temp_micro_cluster = 
					(moa.clusterers.denstream.MicroCluster)temp_cluster1.getMicroClusters().get(iMicro);

				if (temp_micro_cluster.equals(nearest_cluster))
				{
					nearest_id = cluster_id+1;
					break;
				}
			}
			if (nearest_id>0)
			{
				break;
			}
		}
		if (nearest_id <= 0)
		{
			int a = 0;
			int b = 1;
			int cc = a + b;
		}
		return nearest_id;
	}
	
	public static int getPointIdWithCoord(double x, double y, ArrayList<double[]> coordList)
	{
		int minIndex = 0;
		double minDist= (x-coordList.get(0)[0])*(x-coordList.get(0)[0])
				+(y-coordList.get(0)[1])*(y-coordList.get(0)[1]);
		
		for (int iPoint=1; iPoint<coordList.size(); iPoint++)
		{
			double temp_dist = (x-coordList.get(iPoint)[0])*(x-coordList.get(iPoint)[0])
					+(y-coordList.get(iPoint)[1])*(y-coordList.get(iPoint)[1]);
			if(temp_dist == 0)
			{
				return iPoint;
			}
			if (temp_dist<minDist)
			{
				minDist = temp_dist;
				minIndex = iPoint;
			}
		}
		
		return minIndex;
	}
}
