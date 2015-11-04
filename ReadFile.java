
/**  
 * @ClassName     ReadFile.java  
 * @Description   CI插件Yaml文件解析
 * @author        yuanshengtao 
 * @version       V1.0    
 * @Date          2015-11-02  
 */ 

package ymlfileparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ReadFile {
	private Map<String, Steps> stepMap = new HashMap<String, Steps>();               //Yaml文件解析出来的step列表
	private List<String> sortSteps = new ArrayList<String>();			 //排序之后的step列表，不包含step详细信息
	private Map<String, List<String>> relyMap = new HashMap<String, List<String>>(); //以step为key，依赖step的step list为value的map

	/**
	 * @param  args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args){
		String file = "abs.yml";
		String stepString = "a";
		
		ReadFile readFile = new ReadFile();
		try {
			readFile.readFileToList(file);
			if(!readFile.sortSteps(stepString)){
				return;
			}
			
			readFile.printSortListInfo();
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * @FunName         readFileToList
	 * @Description     读取Yaml文件，并且读取“build”节点
	 * @param  fileName Yaml文件名
	 * @throw           文件不存在异常
	 * @return          无
	 */
	@SuppressWarnings("unchecked")
	public void readFileToList(String fileName) throws FileNotFoundException{
		InputStream input = new FileInputStream(new File(fileName));
		Yaml yaml = new Yaml(new SafeConstructor());

		Map<String, Object> YamlMap = (Map<String, Object>) yaml.load(input);
		List<Object> build = (List<Object>) YamlMap.get("build");
		for (Object step : build) {
			
			Steps stepObj = new Steps();
			Map<String, Object> steps = (Map<String, Object>) (step);
			Set<String> set = steps.keySet();
			for (String key : set) {
				//获取step name
				if (key.equals("step")) {
					stepObj.setName((String)steps.get("step"));
				}
				
				//获取failoncontinue
				if (key.equals("failoncontinue")) {
					stepObj.setFail((Boolean)steps.get("failoncontinue"));
				}
				
				//获取 depends
				if (key.equals("depends")) {
					String dependsString = (String)steps.get("depends");
					String [] dependsArray = dependsString.split(",");
					stepObj.setDepend(Arrays.asList(dependsArray));
				}
				
				//获取properties
				if (key.equals("properties")) {
					Map<String, String> propertiesMap = (Map<String, String>)steps.get("properties");
					
					Set<String> properSet = propertiesMap.keySet();
					for(String properKey : properSet){
						stepObj.addProperties(properKey, propertiesMap.get(properKey));
					}
				}
				
				//获取task节点
				if (key.equals("tasks")) {
					List<Object> tasks = (List<Object>) steps.get("tasks");
					parserTask(stepObj, tasks);
				}
			}
			stepMap.put(stepObj.getName(), stepObj);
		}
	}
	

	/**
	 * @FunName       parserTask
	 * @Description   获取customtask列表
	 * @param steps   Step对象，保存task
	 * @param tasks   Yaml文件获取出来的task节点
	 * @return        无
	 */
	@SuppressWarnings("unchecked")
	private void parserTask(Steps steps, List<Object> tasks){
		for (Object ctask : tasks) {
			
			Map<String, Object> ctaskAttr = (Map<String, Object>) ctask;
			Set<String> ctaskAttrKeySet = ctaskAttr.keySet();
			for (String keyString : ctaskAttrKeySet) {
				
				Task taskObj = new Task();
				Map<String, Object> attrsMap = (Map<String, Object>) (ctaskAttr.get(keyString));
				Set<String> attrsKey = attrsMap.keySet();
				for (String attrKey : attrsKey) {
					if (attrKey.equals("failonerror")) {
						taskObj.setFailonerror((Boolean) attrsMap.get(attrKey));
					}
					
					if (attrKey.equals("executable")) {
						taskObj.setExecutable((String)attrsMap.get(attrKey));
					}
					
					if (attrKey.equals("type")) {
						taskObj.setType((String)attrsMap.get(attrKey));
					}
				}
				steps.addTask(taskObj);
			}
		}
	}
	
	/**
	 * @FunName         sortSteps
	 * @Description     执行步骤排序
	 * @param step      需要执行的步骤
	 * @return boolean  解析成功返回true，否则返回false
	 */
	public boolean sortSteps(String step){
		List<String> depends;
		
		if (!stepMap.containsKey(step)) {
			System.out.println("The Yaml file has no step:" + step + ".");
			return false;
		}
		
		if (!sortSteps.contains(step)) {
			sortSteps.add(0, step);
		}
		
		depends = stepMap.get(step).getDepends();
		if (depends != null) {
			insertDependsToSortMap(sortSteps.indexOf(step), depends);
			
			insertToRelyMap(depends, step);
			if (isCycle(depends, step)) {
				return false;
			}
			
			for(String dependStep : depends){
				if(!sortSteps(dependStep)){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @FunName         isCycle
	 * @Description     判断依赖项是否造成死循环
	 * @param depends   step依赖的step列表
	 * @param step      当前解析的step
	 * @return boolean  死循环返回true，否则返回false
	 */
	private boolean isCycle(List<String> depends, String step){
		//step还没被依赖，不存在死循环依赖
		if (!relyMap.containsKey(step)) {
			return false;
		}
		
		//被step依赖同时也依赖step造成死循环依赖
		for(String dependStep : depends){
			if(relyMap.get(step).contains(dependStep)){
				System.out.println("Dependency cause death cycle.");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @FunName        insertToRelyMap
	 * @Description    每个step所被依赖的step添加到map
	 * @param depends  step依赖的step列表
	 * @param step     当前解析的step
	 * @return         无    
	 */
	private void insertToRelyMap(List<String> depends, String step){
		for(String dependsStep : depends){
			if(relyMap.containsKey(dependsStep)){
				relyMap.get(dependsStep).add(step);
			}else {
				List<String> rely = new ArrayList<String>();
				rely.add(step);
				relyMap.put(dependsStep, rely);
			}
			
			//如果a依赖于b,则依赖a的全部都依赖b
			if (relyMap.containsKey(step)) {
				relyMap.get(dependsStep).addAll(relyMap.get(step));
			}
		}
	}
	
	/**
	 * @FunName         insertDependsToSortMap
	 * @Description     将step依赖项插入到step之前
	 * @param index		step索引位置
	 * @param depends   step依赖项
	 * @return          无
	 */
	private void insertDependsToSortMap(int index, List<String> depends){
		for(String dependString : depends){
			if ((sortSteps.contains(dependString)) && (index > sortSteps.indexOf(dependString))) {
				break;
			}
			sortSteps.remove(dependString);
			sortSteps.add(index, dependString);
			index++;
		}
	}
	
	/**
	 * @FunName      printSortListInfo
	 * @Description  打印排序信息
	 * @return       无
	 */
	public void printSortListInfo(){
		System.out.println("排序后的执行步骤是：");
		for(String stepString : sortSteps){
			System.out.print(stepString + "  ");
		}
	}
}
