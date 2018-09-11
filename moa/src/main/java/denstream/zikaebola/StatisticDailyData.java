package denstream.zikaebola;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatisticDailyData 
{
	/////////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	/////////////////////////////////////////////////////////////////////////////////
	public static int getDateIndex(Date temp_date, Date[] start_list, Date[] end_list)
	{
		int idx = -1;
		if (temp_date.after(end_list[end_list.length-1]))
		{
			return -2;
		}

		for (int i=0; i<start_list.length; i++)
		{
			Date temp_date1 = start_list[i];
			Date temp_date2 = end_list[i];
			if (temp_date.equals(temp_date1))
			{
				idx = i;
				break;
			}
			else if (temp_date.after(temp_date1) && temp_date.before(temp_date2))
			{
				idx = i;
				break;
			}
		}
		return idx;
	}

	public static void getDateRange(String start_time_str,
			int date_count, 
			Date[] start_date_list,
			Date[] end_date_list)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start_date_val = null;
		try {
			start_date_val = sdf.parse(start_time_str);			
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		Calendar specialDate1 = Calendar.getInstance();
		specialDate1.setTime(start_date_val);
		Date temp_data_val1 = specialDate1.getTime();
		start_date_list[0] = (temp_data_val1);

		Calendar specialDate2 = Calendar.getInstance();
		specialDate2.setTime(start_date_val);
		specialDate2.add(Calendar.DAY_OF_MONTH, 1);
		Date temp_data_val2 = specialDate2.getTime();
		end_date_list[0] = (temp_data_val2);
		
		for (int i=1; i<date_count; i++)
		{
			specialDate1.add(Calendar.DAY_OF_MONTH, 1);
			temp_data_val1 = specialDate1.getTime();

			specialDate2.add(Calendar.DAY_OF_MONTH, 1);
			temp_data_val2 = specialDate2.getTime();
			
			start_date_list[i] = (temp_data_val1);
			end_date_list[i] = (temp_data_val2);
		}
		
		for (int i=0; i<date_count; i++)
		{
			System.out.print(sdf.format(start_date_list[i]));
			System.out.print(" - ");
			System.out.print(sdf.format(end_date_list[i]));
			System.out.println();
		}
	}
	
	public static String loadAndStatistic(String fileName,
			int date_count,
			Date[] start_date_list,
			Date[] end_date_list,
			int[] daily_count_list)
	{
		String header = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i=0; i<date_count; i++)
		{
			daily_count_list[i] = 0;
		}
		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			String str = "";
			fis = new FileInputStream(fileName);			
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			
			header = br.readLine();
			String[] temp_line_array;
			String time_str;
			Date temp_date = null;
			int flag = 0;
			int print_flag = 100000;
			while ((str = br.readLine()) != null) 
			{
				flag++;
				if (flag % print_flag == 0){
					System.out.println(flag);
				}
				temp_line_array = str.split(",");

				time_str = temp_line_array[0];

				try {
					temp_date = sdf.parse(time_str);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (temp_date.before(start_date_list[0]))
				{
					continue;
				}
				if (temp_date.after(end_date_list[date_count-1]))
				{
					break;
				}
				int date_idx = getDateIndex(temp_date, start_date_list, end_date_list);
				if (date_idx >= 0) daily_count_list[date_idx]++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("cannot find file");
		} catch (IOException e) {
			System.out.println("read file failed");
		} finally {
			try {
				br.close();
				isr.close();
				fis.close();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header;
	}
	
	
}
