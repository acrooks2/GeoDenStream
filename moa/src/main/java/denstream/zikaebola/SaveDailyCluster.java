package denstream.zikaebola;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import moa.cluster.Clustering;

public class SaveDailyCluster {

	public static void writeDailyClusterInfo(
			String outfileName_DailyCluster,
			int iDay,
			Clustering daily_cluster)
	{
		Integer day_idx = iDay+1;
        String outfileName_cur_day_cluster = outfileName_DailyCluster + "ClusterInfo"+ day_idx.toString()+".csv";
    
        try {
			BufferedWriter outBufWriter =new BufferedWriter(new FileWriter(outfileName_cur_day_cluster));
			
			outBufWriter.write("cluster_id,center_x,center_y,weight\n");
			
			int cluster_count = daily_cluster.size();        
	        for (int iCluster = 0; iCluster<cluster_count; iCluster++)
			{
				moa.clusterers.macro.NonConvexCluster temp_cluster = 
					(moa.clusterers.macro.NonConvexCluster)daily_cluster.getClustering().get(iCluster);
				Integer cluster_id = (int) temp_cluster.getId();
				double[] center = temp_cluster.getCenter();
				
				Double weight = temp_cluster.getWeight();
				
				Double center_x = center[0];
				Double center_y = center[1];
				
				outBufWriter.write(cluster_id.toString());
				outBufWriter.write(",");
				outBufWriter.write(center_x.toString());
				outBufWriter.write(",");
				outBufWriter.write(center_y.toString());
				outBufWriter.write(",");
				outBufWriter.write(weight.toString());
				outBufWriter.write("\n");
			}
	        
			outBufWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
	}
}
