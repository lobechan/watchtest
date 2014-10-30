package com.fastfox.watchassistant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class StepFacade {

	public static int deleteAll(Context context){
		int res = context.getContentResolver().delete(StepProvider.CONTENT_STEPS_URI, "1", null);
		return res;
	}
	public static int addStep(Context context,
			long start_time,
			long end_time,
			int step_type,
			int steps){
		ContentValues values = new ContentValues();
		values.put(
				StepProvider.ColumnsSteps.START_TIME,
				start_time);
		values.put(
				StepProvider.ColumnsSteps.END_TIME,
				end_time);
		values.put(
				StepProvider.ColumnsSteps.STEP_TYPE,
				step_type);
		values.put(
				StepProvider.ColumnsSteps.STEPS,
				steps);
		values.put(StepProvider.ColumnsSteps.CREATE_TIME, new Date().getTime());
		
		context.getContentResolver().insert(StepProvider.CONTENT_STEPS_URI, values);
		return 1;
	}
	
	public static int getStepCount(Context context){
		int  count = 0;

		Cursor c = context.getContentResolver().query(
				StepProvider.CONTENT_STEPS_URI,
						new String[] {
						StepProvider.ColumnsSteps._ID
								 },
						null,
						null, null);
		if(c != null) {
			count = c.getCount();
		}
		c.close();
		return count;
	}
	
	public static ArrayList<Steps> getStepsList(Context context,
			long startTime, long endTime) {
		
		ArrayList<Steps> steps =  new ArrayList<Steps>();
		String id = null;
		
		Cursor c = context.getContentResolver().query(
				StepProvider.CONTENT_STEPS_URI,
						new String[] {
						StepProvider.ColumnsSteps._ID,
						StepProvider.ColumnsSteps.START_TIME,
						StepProvider.ColumnsSteps.END_TIME,
						StepProvider.ColumnsSteps.STEP_TYPE,
						StepProvider.ColumnsSteps.STEPS,
						StepProvider.ColumnsSteps.CREATE_TIME},
						StepProvider.ColumnsSteps.START_TIME + " >? and "+
								StepProvider.ColumnsSteps.END_TIME+ "<?",
						new String[] { Long.toString(startTime) ,Long.toString(endTime)}," end_time asc");
		if(c != null){
			while (c.moveToNext()) {
				Steps step = new Steps();
				step.setId(Long.parseLong(c.getString(0)));
				step.setStartTime(Long.parseLong(c.getString(1)));
				
				step.setEndTime(Long.parseLong(c.getString(2)));
				step.setStepType(Integer.parseInt(c.getString(3)));
				step.setSteps(Integer.parseInt(c.getString(4)));
				step.setCreateTime(Long.parseLong(c.getString(5)));
				
				steps.add(step);
			}
			c.close();
		}
		
		return steps;
	}
	public static ArrayList<Steps> getAllStepsList(Context context) {
		
		ArrayList<Steps> steps =  new ArrayList<Steps>();
		String id = null;
		
		Cursor c = context.getContentResolver().query(
				StepProvider.CONTENT_STEPS_URI,
						new String[] {
						StepProvider.ColumnsSteps._ID,
						StepProvider.ColumnsSteps.START_TIME,
						StepProvider.ColumnsSteps.END_TIME,
						StepProvider.ColumnsSteps.STEP_TYPE,
						StepProvider.ColumnsSteps.STEPS,
						StepProvider.ColumnsSteps.CREATE_TIME},null,
						null," _id desc");
		if(c != null){
			while (c.moveToNext()) {
				Steps step = new Steps();
				step.setId(Long.parseLong(c.getString(0)));
				step.setStartTime(Long.parseLong(c.getString(1)));
				
				step.setEndTime(Long.parseLong(c.getString(2)));
				step.setStepType(Integer.parseInt(c.getString(3)));
				step.setSteps(Integer.parseInt(c.getString(4)));
				step.setCreateTime(Long.parseLong(c.getString(5)));
				
				steps.add(step);
			}
			c.close();
		}
		
		return steps;
	}
}
