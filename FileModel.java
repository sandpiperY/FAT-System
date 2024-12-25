package fileSys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class FileModel {
	public Map<String,FileModel> subMap=new HashMap<String,FileModel>();//目录
	private String Filename;//文件名
	private String type;//后缀名
	private int readOnly;//是否只读 1是 0不是
	private int isDir;//是否目录 1是 0不是
	private int startNum;//首块号
	private int size;//文件大小
	private FileModel father=null;//上级目录
	private int hide;
	
	public FileModel(String name,String type,int startNum,int size) {
		//字符文件的构造函数
		this.Filename=name;
		this.type=type;
		this.isDir=0;
		this.readOnly=0;
		this.hide=0;
		this.size=size;
		this.startNum=startNum;
	}
	
	public FileModel(String name,int startNum) {
		//目录文件的构造函数
		this.Filename=name;
		this.isDir=1;
		this.readOnly=0;
		this.type=" ";
		this.startNum=startNum;
		this.size=1;
	}
	
	public String getName() {
		return Filename;
	}
	public void setName(String name) {
		this.Filename=name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type=type;
	}	
	public int getAttr() {
		return isDir;
	}
	public void setAttr(int isDir) {
		this.isDir=isDir;
	}
	public int getRO() {
		return readOnly;
	}
	public void setRO(int RO) {
		this.readOnly=RO;
	}
	public int getHide() {
		return hide;
	}
	public void setHide(int hide) {
		this.hide=hide;
	}
	public int getStartNum() {
		return startNum;
	}
	public void setStartNum(int startNum) {
		this.startNum = startNum;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
 
	public FileModel getFather() {
		return father;
	}
 
	public void setFather(FileModel father) {
		this.father = father;
	}
}
