
/**  
 * @ClassName     Steps.java  
 * @Description   step数据类
 * @author        yuanshengtao 
 * @version       V1.0    
 * @Date          2015-11-02  
 */ 

package ymlfileparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Steps {
	private String name;                                                    //step 名
	private boolean failoncontinue;			                        //failoncontinue 属性
	private List<String> depends;				                //depends 依赖项
	private Map<String, String> properties = new HashMap<String, String>(); //properties 属性
	private List<Task> task = new ArrayList<Task>();                        //task 列表
	 
	public void setName(String ConfigName){
		name = ConfigName;
	}
	
	public String getName(){
		return name;
	}
	
	public void setFail(boolean ConfigFailoncontinue){
		failoncontinue = ConfigFailoncontinue;
	}
	
	public boolean getFail(){
		return failoncontinue;
	}
	
	public void setDepend(List<String> ConfigDepend){
		depends = ConfigDepend;
	}
	
	public List<String> getDepends(){
		return depends;
	}
	
	public void addProperties(String key, String value){
		properties.put(key, value);
	}
	
	public Map<String, String> getProperties(){
		return properties;
	}
	
	public void addTask(Task tasks){
		task.add(tasks);
	}
	
	public List<Task> getTask(){
		return task;
	}
}
