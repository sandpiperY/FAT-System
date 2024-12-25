package fileSys;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;

import java.util.Map.Entry;

public class FileAccessTable {
	public Map<String,FileModel> all=new HashMap<String,FileModel>();
	private int[] fat = new int[128];//定义fat表
	private FileModel root = new FileModel("root",2);//根目录 使用数据块2
	FileModel nowCatalog=root;
	public int empty=125;
	byte[] data=new byte[131072];//定义数据块
	
	public FileAccessTable() {
		//fat初始化，第二项为根目录
		for(int i=0; i<fat.length ; i++ ) {
			fat[i] = 0;
		}
		for(int i=0; i<data.length ; i++ ) {
			data[i] = 0;
		}//数据块初始化
		fat[0]=0xF8FFFF0F;//fat表起始标志
		fat[1]=0xFFFFFFFF;
		fat[2]=0x0FFFFFFF;//表项2写入结束标记
		root.setFather(root);//root的父目录为自身
		all.put("root", root);
	}
	public String getNowCatalog() {
		FileModel theCatalog=nowCatalog;
		String name=nowCatalog.getName();
		while(nowCatalog!=root) {
			nowCatalog=nowCatalog.getFather();
			name=nowCatalog.getName()+"/"+name;
		}
		nowCatalog=theCatalog;
		return name;
	}
	public int setFat(int size) {
		//为文件分配磁盘空间
		int[] startNum=new int[128];
		int i = 2;//记录fat循环定位顺序
		for(int j=0;j<size;i++) {
			if(fat[i]==0) {
				startNum[j]=i;//记录文件的盘块链表
				if(j>0) {
					fat[startNum[j-1]]=i;//上一盘块指向下一盘块
				}
				j++;
			}
		}
		fat[i-1] = 0x0FFFFFFF;//写入结束标记
		return startNum[0];
		
	}
	
	public void delFat(int startNum) {
		//删除文件时释放FAT表项
		int nextPoint = fat[startNum];
		int nowPoint = startNum;
		int count=0;
		while(fat[nowPoint]!=0) {
			//判断是否有结束标记，如果为结束标记，则结束循环
			nextPoint=fat[nowPoint];
			if(nextPoint == 0x0FFFFFFF) {
				fat[nowPoint]=0;//清空FAT表项
				for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
					data[i]=0;
				}
				count++;
				break;
			}else {
				fat[nowPoint]=0;
				for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
					data[i]=0;
				}
				count++;
				nowPoint=nextPoint;
			}
		}
		empty+=count;
	}
	
	public void addEntry(String name,int startNum_F,int startNum,FileModel fatherCatalog) {
		//添加目录项的方法(目录)
		int size=fatherCatalog.subMap.size();//父目录元素数，设定每个目录项长度为32
		int entry=startNum_F*1024+(size-1)*32;//找到写入目录项的起始位置
		byte[] namebyte=name.getBytes(StandardCharsets.UTF_8); 
		for(int i=0;i<namebyte.length;i++) {//写入目录名称
			data[entry+i]=namebyte[i];
		}
		data[entry+26]=0;//26位为是否只读，27位为是否目录
		data[entry+27]=1;
		byte[] startNumByte=int2byte(startNum);//写入首块号
		for(int i=0;i<startNumByte.length;i++) {
			data[entry+28+i]=startNumByte[i];
		}
		
	}
	public void addEntryforFile(String name,String type,int startNum_F,int startNum,FileModel fatherCatalog) {
		//添加目录项的方法(文件)
		int size=fatherCatalog.subMap.size();//父目录元素数，设定每个目录项长度为32
		int entry=startNum_F*1024+(size-1)*32;//找到目录项起始位置
		byte[] namebyte=name.getBytes(StandardCharsets.UTF_8); 
		for(int i=0;i<namebyte.length;i++) {//写入文件名称
			data[entry+i]=namebyte[i];
		}
		byte[] typebyte=type.getBytes(StandardCharsets.UTF_8);
		for(int i=0;i<typebyte.length;i++) {//写入文件类型
			data[entry+20+i]=typebyte[i];
		}
		data[entry+26]=0;//26位为是否只读，27位为是否目录
		data[entry+27]=0;
		byte[] startNumByte=int2byte(startNum);//写入首块号
		for(int i=0;i<startNumByte.length;i++) {
			data[entry+28+i]=startNumByte[i];
		}
		
	}
	
	public void createFile(String name,String type,int size) {
		//创建文件的方法
		if(empty>=size) {//判断磁盘剩余空间是否足够
			FileModel value=nowCatalog.subMap.get(name);
			if(value!=null) {//判断文件是否存在
				System.out.println("创建失败，已存在同名文件或目录！");				
			}
			else if(value==null) {//若无同名文件或文件夹，继续创建文件
				int startNum=setFat(size);
				FileModel file = new FileModel(name,type,startNum,size);
				file.setFather(nowCatalog);
				nowCatalog.subMap.put(file.getName(), file);//在父目录里添加文件
				all.put(file.getName(), file);
				empty-=size;
				System.out.println("创建文件成功！");
				showFile();
				//在父目录块中添加新的目录项
				int start_Num=nowCatalog.getStartNum();//找到父目录的起始块
				addEntryforFile(name,type,start_Num, startNum,nowCatalog);
			}	
		}else {
				System.out.println("创建文件失败，磁盘空间不足");
			}
	}
	
	public void createCatolog(String name) {
		//创建目录的方法
		if(empty>=1) {//判断磁盘剩余空间是否足够
			FileModel value=nowCatalog.subMap.get(name);//判断该目录下是否存在同名目录或文件
			if(value!=null) {
				System.out.println("创建目录失败，已存在同名文件或目录！");			
			}
			else if(value==null) {
				int startNum=setFat(1);
				FileModel catalog = new FileModel(name,startNum);
				catalog.setFather(nowCatalog);
				nowCatalog.subMap.put(name, catalog);
				empty--;
				all.put(catalog.getName(), catalog);
				System.out.println("创建目录成功！");
				showFile();
				
				//在父目录块中添加新的目录项
				int start_Num=nowCatalog.getStartNum();//找到父目录的起始块
				addEntry(name, start_Num, startNum,nowCatalog);
				
				//在新建目录块下添加 . 和 ..目录项
				addEntry(".",startNum,startNum,catalog);
				addEntry("..",startNum,start_Num,catalog);
				
			}			
		}
		
		else {
			System.out.println("创建目录失败，磁盘空间不足！");
		}
		
	}
	

	public void showFile() {//列文件目录的方法
		System.out.println( nowCatalog.getName() );
       
		if(!nowCatalog.subMap.isEmpty()) {
			for(FileModel value : nowCatalog.subMap.values()) {
				if(value.getAttr() == 1) { //目录文件
					System.out.println("文件名:" + value.getName()+" | 文件类型:目录 | 起始盘块:" + value.getStartNum()
										+" | 大小:" + value.getSize()+" | 是否只读:"+value.getRO());
				}
				else if(value.getAttr() == 0) {
					if(value.getHide()==0) {
						System.out.println("文件名:" + value.getName() + "." + value.getType()
						+" | 文件类型:字符文件 | 起始盘块:" 
						+ value.getStartNum()+" | 大小:" + value.getSize()+" | 是否只读:"+value.getRO());
					}
				}
			}
		}
		System.out.println();
		System.out.println("磁盘剩余空间:" + empty + "            " + "退出系统请输入exit");
		System.out.println();
	}
	

	public void deleteDir(String[] roadName) {
		//删除目录的方法
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==1) {
				if(!nowCatalog.subMap.isEmpty()) {
					System.out.println("该目录非空，无法删除！");
				}else {
					FileModel value=nowCatalog;
					nowCatalog=nowCatalog.getFather();
					nowCatalog.subMap.remove(roadName[roadName.length-1]);
					delFat(value.getStartNum());
					System.out.println("删除成功");
				}
			}else if (nowCatalog.getAttr()==0) {
				System.out.println("找不到该目录，请检查路径是否正确");
			}
		}
	}
	
	public void deleteFile(String[] roadName) {//删除文件的方法
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==0) {
				if(nowCatalog.getRO()==0) {
					FileModel value=nowCatalog;
					nowCatalog=nowCatalog.getFather();
					nowCatalog.subMap.remove(roadName[roadName.length-1]);
					delFat(value.getStartNum());
					System.out.println("删除成功");
				}else {
					System.out.println("该文件为只读文件，不允许删除！");
				}	
			}else if (nowCatalog.getAttr()==1) {
				System.out.println("找不到该文件，请检查路径是否正确");
			}
		}
	}
	
	public void changeDir(String[] roadName) {//改变当前目录
		FileModel theCatalog=nowCatalog;
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr() == 1) {
				System.out.println();
			} else if(nowCatalog.getAttr() == 0) {
				System.out.println("找不到该目录，请检查路径是否正确");
				nowCatalog=theCatalog;
			}
		}
	}

	public void editFile(String name, String input) {
			//编辑文件
		FileModel value = nowCatalog.subMap.get(name);
		if(value.getAttr() == 0) {
				if(value.getRO()==0) {
					byte[] b=input.getBytes(StandardCharsets.UTF_8); 
					int addSize=b.length/1024+1;
					value.setSize(addSize);
					delFat(value.getStartNum());
					int startNum=setFat(addSize);
					value.setStartNum(startNum);
					for(int i=0;i<b.length;i++) {
						data[startNum*1024+i]=b[i];
					}
					empty-=addSize;
				}else {
					System.out.println("该文件为只读文件，不允许编辑！");
				}	
		} else{
			System.out.println("编辑失败，请确认文件名是否正确输入");					
		}
	} 
	
	
	public boolean searchRoad(String[] roadName) {//识别命令中的绝对路径和相对路径，用于在绝对路径或相对路径下新建文件或目录
		
		FileModel theCatalog = nowCatalog; //设置断点纪录当前目录
		
		if(all.containsKey(roadName[roadName.length-2])|roadName[0].equals(".")|roadName[0].equals("..")) { //检查所有文件中有无该文件
			
			if(roadName[0].equals(".")) {//在当前目录下查找
				int i;
				for(i=1; i<roadName.length-1; i++) {//查找到上一级为止
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
 
					} else {
						System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
						nowCatalog = theCatalog;
//						showFile();
						break;
					}
				}
				if(i==roadName.length-1) {
					return true;	
				}else {
					return false;
				}
			}
			
			else if(roadName[0].equals("..")) {//上一级目录
				nowCatalog=nowCatalog.getFather();
				int i;
				for(i=1; i<roadName.length-1; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
					} 
					else if(roadName[i].equals("..")) {
						nowCatalog=nowCatalog.getFather();
					}
					else {
						System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
						nowCatalog = theCatalog;
//						showFile();
						break;
					}
				}
				if(i==roadName.length-1) {
					return true;	
				}else {
					return false;
				}
			}
			else{
				if("root".equals(roadName[0])) {	//判断输入路径的首目录是否root
					nowCatalog = root;
					int i;
					for(i=1; i<roadName.length-1; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
	 
						} else {
							System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
							nowCatalog = theCatalog;
//							showFile();
							break;
						}
					}

					if(i==roadName.length-1) {
						return true;	
					}else {
						return false;
					}
				}else if(nowCatalog.subMap.containsKey(roadName[0])) {//查找当前目录下是否包含该目录
					nowCatalog = nowCatalog.subMap.get(roadName[0]);
					int i;
					for(i=1; i<roadName.length-1; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
	 
						} else {
							System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
							nowCatalog = theCatalog;
							break;
						}
					}
					if(i==roadName.length-1) {
						return true;	
					}else {
						return false;
					}
				}else {
					nowCatalog = theCatalog;
					System.out.println("请输入正确的绝对路径,绝对路径需以root/开头！");
					return false;
				}
			} 
		}else {
			System.out.println("该文件或目录不存在，请输入正确的路径");
//			showFile();
			return false;
		}
		
	}
	
	public void readFile(String name) {//查看文件
		FileModel value = nowCatalog.subMap.get(name);
		if(value.getAttr()==1) {
			System.out.println("文件不存在！");
		} else {
			int startNum=value.getStartNum();
			int nextPoint = fat[startNum];
			int nowPoint = startNum;
			int count=0;
			while(fat[nowPoint]!=0) {
				//判断是否有结束标记，如果为结束标记，则结束循环
				nextPoint=fat[nowPoint];
				if(nextPoint == 0x0FFFFFFF) {
					for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
						if(data[i]==0) {
							break;
						}else {
							byte[] b= {data[i],data[++i]};
							String s=new String(b,StandardCharsets.UTF_8);
							System.out.print(s);
						}			
					}
					break;
				}else {
					for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
						if(data[i]==0) {
							break;
						}else {
							byte[] b= {data[i],data[i++]};
							String s=new String(b,StandardCharsets.UTF_8);
							System.out.print(s);
						}
					}
					nowPoint=nextPoint;
				}
			}
		}
		System.out.println();
	}

	public void copyFile(String[] roadNameCopy,String[] roadNamePaste) {//复制文件
		String[] split = roadNameCopy[roadNameCopy.length-1].split("\\.");
		roadNameCopy[roadNameCopy.length-1]=split[0];
		FileModel theCatalog=nowCatalog;
		if(searchFile(roadNameCopy)) {
			nowCatalog=nowCatalog.getFather();
			FileModel value=nowCatalog.subMap.get(split[0]);
			if(value.getAttr()==1) {
				System.out.println("被复制的文件不存在！");
			}else if(value.getAttr()==0) {
				String[] split1 = roadNamePaste[roadNamePaste.length-1].split("\\.");
				roadNamePaste[roadNamePaste.length-1]=split1[0];
				nowCatalog=theCatalog;
				if(roadNamePaste.length==1 || searchRoad(roadNamePaste)) {
					createFile(split1[0], split1[1], 1);
					int startNum=value.getStartNum();
					int nextPoint = fat[startNum];
					int nowPoint = startNum;
					int count=0;
					String input="";
					while(fat[nowPoint]!=0) {
						nextPoint=fat[nowPoint];
						if(nextPoint == 0x0FFFFFFF) {
							for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
								if(data[i]==0) {
									break;
								}else {
									byte[] b= {data[i],data[++i]};
									String s=new String(b,StandardCharsets.UTF_8);
									input=input+s;
								}			
							}
							break;
						}else {
							for(int i=nowPoint*1024;i<(nowPoint+1)*1024-1;i++) {
								if(data[i]==0) {
									break;
								}else {
									byte[] b= {data[i],data[i++]};
									String s=new String(b,StandardCharsets.UTF_8);
									input=input+s;
								}
							}
							nowPoint=nextPoint;
						}
					}
					editFile(split1[0],input);
					System.out.println("复制成功");
				}
			}
		}
	}
	
	public void attr(String[] roadName,String[] strs) {//修改文件属性
		String[] split=roadName[roadName.length-1].split("\\.");
		roadName[roadName.length-1]=split[0];
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==1) {
				System.out.println("该命令只能对文件操作！");
			}else if(nowCatalog.getAttr()==0) {
				for(int i=2;i<strs.length;i++) {
					switch(strs[i]) {
					case "+h"://设置隐藏文件属性
						if(nowCatalog.getHide()==0) {
							nowCatalog.setHide(1);
						}else {
							System.out.println("文件已被隐藏，无法将文件隐藏！");
						}
						break;
					case "-h":
						if(nowCatalog.getHide()==1) {
							nowCatalog.setHide(0);
						}else {
							System.out.println("文件未被隐藏，无法对文件解除隐藏！");
						}
						break;
					case "+r"://设置只读属性
						if(nowCatalog.getRO()==0) {
							nowCatalog.setRO(1);
						}else {
							System.out.println("文件已被设置为只读，无法设置为只读文件！");
						}
						break;
					case "-r" :
						if(nowCatalog.getRO()==1) {
							nowCatalog.setRO(0);
						}else {
							System.out.println("文件未被设置为只读，无法解除只读属性！");
						}
						break;
					default:
						System.out.println("命令输入有误,请输入 +h/-h/+r/-r");
						System.out.println(strs[i]);
						break;
					}
				}
			}
		}
	}
	public void backFile() {
		if(nowCatalog.getFather() == null) {
			System.out.println("该文件没有上级目录");
		} else {
			nowCatalog = nowCatalog.getFather();
			showFile();
		}
	}

	public static byte[] int2byte(int n) {  
		  byte[] b = new byte[4];  
		  b[0] = (byte) (n & 0xff);  
		  b[1] = (byte) (n >> 8 & 0xff);  
		  b[2] = (byte) (n >> 16 & 0xff);  
		  b[3] = (byte) (n >> 24 & 0xff);  
		  return b;  
	}
	
	public boolean searchFile(String[] roadName) {//根据绝对路径和相对路径寻找文件,用于对已经存在的文件或目录执行打开、删除等操作
		// TODO 自动生成的方法存根
		
		FileModel theCatalog = nowCatalog; //设置断点纪录当前目录
		
		if(all.containsKey(roadName[roadName.length-1])|roadName[0].equals(".")|roadName[0].equals("..")) { //检查所有文件中有无该文件
			
			if(roadName[0].equals(".")) {//在当前目录下查找
				int i;
				for(i=1; i<roadName.length; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
 
					} else {
						System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
						nowCatalog = theCatalog;
//						showFile();
						break;
					}
				}
				if(i==roadName.length) {
					return true;	
				}else {
					return false;
				}
			}
			
			else if(roadName[0].equals("..")) {//上一级目录
				nowCatalog=nowCatalog.getFather();
				int i;
				for(i=1; i<roadName.length; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
					} 
					else if(roadName[i].equals("..")) {
						nowCatalog=nowCatalog.getFather();
					}
					else {
						System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
						nowCatalog = theCatalog;
						break;
					}
				}
				if(i==roadName.length) {
					return true;	
				}else {
					return false;
				}
			}
			else{
				if("root".equals(roadName[0])) {	//判断输入路径的首目录是否root
					nowCatalog = root;
					int i;
					for(i=1; i<roadName.length; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
	 
						} else {
							System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
							nowCatalog = theCatalog;
							break;
						}
					}
					if(i==roadName.length) {
						return true;	
					}else {
						return false;
					}
				}else if(nowCatalog.subMap.containsKey(roadName[0])) {//查找当前目录下是否包含该目录
					nowCatalog = nowCatalog.subMap.get(roadName[0]);
					int i;
					for(i=1; i<roadName.length; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //一级一级往下查
	 
						} else {
							System.out.println("找不到该路径下的文件或目录，请检查路径是否正确");
							nowCatalog = theCatalog;
							break;
						}
					}
					if(i==roadName.length) {
						return true;	
					}else {
						return false;
					}
				}else {
					nowCatalog = theCatalog;
					System.out.println("请输入正确的绝对路径,绝对路径需以root/开头！");
					return false;
				}
			} 
		}else {
			System.out.println("该文件或目录不存在，请输入正确的路径！");
			return false;
		}
	}

}
