
/**  
 * @ClassName     Task.java  
 * @Description   task数据类
 * @author        yuanshengtao 
 * @version       V1.0    
 * @Date          2015-11-02  
 */ 

package ymlfileparser;

class Task {
	private String executable;     //executable 属性
	private boolean failonerror;   //failonerror 属性
	private String type;	       //type属性
	
	public void setExecutable(String ConfigExecutable){
		executable = ConfigExecutable;
	}
	
	public void setFailonerror(boolean ConfigFailOnError){
		failonerror = ConfigFailOnError;
	}
	
	public void setType(String ConfigType){
		type = ConfigType;
	}
	
	public String getExecutable(){
		return executable;
	}
	
	public boolean getFailonerror(){
		return failonerror;
	}
	
	public String getType(){
		return type;
	}
	
}
