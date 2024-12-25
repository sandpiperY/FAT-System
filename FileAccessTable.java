package fileSys;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.Map;

import java.util.Map.Entry;

public class FileAccessTable {
	public Map<String,FileModel> all=new HashMap<String,FileModel>();
	private int[] fat = new int[128];//����fat��
	private FileModel root = new FileModel("root",2);//��Ŀ¼ ʹ�����ݿ�2
	FileModel nowCatalog=root;
	public int empty=125;
	byte[] data=new byte[131072];//�������ݿ�
	
	public FileAccessTable() {
		//fat��ʼ�����ڶ���Ϊ��Ŀ¼
		for(int i=0; i<fat.length ; i++ ) {
			fat[i] = 0;
		}
		for(int i=0; i<data.length ; i++ ) {
			data[i] = 0;
		}//���ݿ��ʼ��
		fat[0]=0xF8FFFF0F;//fat����ʼ��־
		fat[1]=0xFFFFFFFF;
		fat[2]=0x0FFFFFFF;//����2д��������
		root.setFather(root);//root�ĸ�Ŀ¼Ϊ����
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
		//Ϊ�ļ�������̿ռ�
		int[] startNum=new int[128];
		int i = 2;//��¼fatѭ����λ˳��
		for(int j=0;j<size;i++) {
			if(fat[i]==0) {
				startNum[j]=i;//��¼�ļ����̿�����
				if(j>0) {
					fat[startNum[j-1]]=i;//��һ�̿�ָ����һ�̿�
				}
				j++;
			}
		}
		fat[i-1] = 0x0FFFFFFF;//д��������
		return startNum[0];
		
	}
	
	public void delFat(int startNum) {
		//ɾ���ļ�ʱ�ͷ�FAT����
		int nextPoint = fat[startNum];
		int nowPoint = startNum;
		int count=0;
		while(fat[nowPoint]!=0) {
			//�ж��Ƿ��н�����ǣ����Ϊ������ǣ������ѭ��
			nextPoint=fat[nowPoint];
			if(nextPoint == 0x0FFFFFFF) {
				fat[nowPoint]=0;//���FAT����
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
		//���Ŀ¼��ķ���(Ŀ¼)
		int size=fatherCatalog.subMap.size();//��Ŀ¼Ԫ�������趨ÿ��Ŀ¼���Ϊ32
		int entry=startNum_F*1024+(size-1)*32;//�ҵ�д��Ŀ¼�����ʼλ��
		byte[] namebyte=name.getBytes(StandardCharsets.UTF_8); 
		for(int i=0;i<namebyte.length;i++) {//д��Ŀ¼����
			data[entry+i]=namebyte[i];
		}
		data[entry+26]=0;//26λΪ�Ƿ�ֻ����27λΪ�Ƿ�Ŀ¼
		data[entry+27]=1;
		byte[] startNumByte=int2byte(startNum);//д���׿��
		for(int i=0;i<startNumByte.length;i++) {
			data[entry+28+i]=startNumByte[i];
		}
		
	}
	public void addEntryforFile(String name,String type,int startNum_F,int startNum,FileModel fatherCatalog) {
		//���Ŀ¼��ķ���(�ļ�)
		int size=fatherCatalog.subMap.size();//��Ŀ¼Ԫ�������趨ÿ��Ŀ¼���Ϊ32
		int entry=startNum_F*1024+(size-1)*32;//�ҵ�Ŀ¼����ʼλ��
		byte[] namebyte=name.getBytes(StandardCharsets.UTF_8); 
		for(int i=0;i<namebyte.length;i++) {//д���ļ�����
			data[entry+i]=namebyte[i];
		}
		byte[] typebyte=type.getBytes(StandardCharsets.UTF_8);
		for(int i=0;i<typebyte.length;i++) {//д���ļ�����
			data[entry+20+i]=typebyte[i];
		}
		data[entry+26]=0;//26λΪ�Ƿ�ֻ����27λΪ�Ƿ�Ŀ¼
		data[entry+27]=0;
		byte[] startNumByte=int2byte(startNum);//д���׿��
		for(int i=0;i<startNumByte.length;i++) {
			data[entry+28+i]=startNumByte[i];
		}
		
	}
	
	public void createFile(String name,String type,int size) {
		//�����ļ��ķ���
		if(empty>=size) {//�жϴ���ʣ��ռ��Ƿ��㹻
			FileModel value=nowCatalog.subMap.get(name);
			if(value!=null) {//�ж��ļ��Ƿ����
				System.out.println("����ʧ�ܣ��Ѵ���ͬ���ļ���Ŀ¼��");				
			}
			else if(value==null) {//����ͬ���ļ����ļ��У����������ļ�
				int startNum=setFat(size);
				FileModel file = new FileModel(name,type,startNum,size);
				file.setFather(nowCatalog);
				nowCatalog.subMap.put(file.getName(), file);//�ڸ�Ŀ¼������ļ�
				all.put(file.getName(), file);
				empty-=size;
				System.out.println("�����ļ��ɹ���");
				showFile();
				//�ڸ�Ŀ¼��������µ�Ŀ¼��
				int start_Num=nowCatalog.getStartNum();//�ҵ���Ŀ¼����ʼ��
				addEntryforFile(name,type,start_Num, startNum,nowCatalog);
			}	
		}else {
				System.out.println("�����ļ�ʧ�ܣ����̿ռ䲻��");
			}
	}
	
	public void createCatolog(String name) {
		//����Ŀ¼�ķ���
		if(empty>=1) {//�жϴ���ʣ��ռ��Ƿ��㹻
			FileModel value=nowCatalog.subMap.get(name);//�жϸ�Ŀ¼���Ƿ����ͬ��Ŀ¼���ļ�
			if(value!=null) {
				System.out.println("����Ŀ¼ʧ�ܣ��Ѵ���ͬ���ļ���Ŀ¼��");			
			}
			else if(value==null) {
				int startNum=setFat(1);
				FileModel catalog = new FileModel(name,startNum);
				catalog.setFather(nowCatalog);
				nowCatalog.subMap.put(name, catalog);
				empty--;
				all.put(catalog.getName(), catalog);
				System.out.println("����Ŀ¼�ɹ���");
				showFile();
				
				//�ڸ�Ŀ¼��������µ�Ŀ¼��
				int start_Num=nowCatalog.getStartNum();//�ҵ���Ŀ¼����ʼ��
				addEntry(name, start_Num, startNum,nowCatalog);
				
				//���½�Ŀ¼������� . �� ..Ŀ¼��
				addEntry(".",startNum,startNum,catalog);
				addEntry("..",startNum,start_Num,catalog);
				
			}			
		}
		
		else {
			System.out.println("����Ŀ¼ʧ�ܣ����̿ռ䲻�㣡");
		}
		
	}
	

	public void showFile() {//���ļ�Ŀ¼�ķ���
		System.out.println( nowCatalog.getName() );
       
		if(!nowCatalog.subMap.isEmpty()) {
			for(FileModel value : nowCatalog.subMap.values()) {
				if(value.getAttr() == 1) { //Ŀ¼�ļ�
					System.out.println("�ļ���:" + value.getName()+" | �ļ�����:Ŀ¼ | ��ʼ�̿�:" + value.getStartNum()
										+" | ��С:" + value.getSize()+" | �Ƿ�ֻ��:"+value.getRO());
				}
				else if(value.getAttr() == 0) {
					if(value.getHide()==0) {
						System.out.println("�ļ���:" + value.getName() + "." + value.getType()
						+" | �ļ�����:�ַ��ļ� | ��ʼ�̿�:" 
						+ value.getStartNum()+" | ��С:" + value.getSize()+" | �Ƿ�ֻ��:"+value.getRO());
					}
				}
			}
		}
		System.out.println();
		System.out.println("����ʣ��ռ�:" + empty + "            " + "�˳�ϵͳ������exit");
		System.out.println();
	}
	

	public void deleteDir(String[] roadName) {
		//ɾ��Ŀ¼�ķ���
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==1) {
				if(!nowCatalog.subMap.isEmpty()) {
					System.out.println("��Ŀ¼�ǿգ��޷�ɾ����");
				}else {
					FileModel value=nowCatalog;
					nowCatalog=nowCatalog.getFather();
					nowCatalog.subMap.remove(roadName[roadName.length-1]);
					delFat(value.getStartNum());
					System.out.println("ɾ���ɹ�");
				}
			}else if (nowCatalog.getAttr()==0) {
				System.out.println("�Ҳ�����Ŀ¼������·���Ƿ���ȷ");
			}
		}
	}
	
	public void deleteFile(String[] roadName) {//ɾ���ļ��ķ���
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==0) {
				if(nowCatalog.getRO()==0) {
					FileModel value=nowCatalog;
					nowCatalog=nowCatalog.getFather();
					nowCatalog.subMap.remove(roadName[roadName.length-1]);
					delFat(value.getStartNum());
					System.out.println("ɾ���ɹ�");
				}else {
					System.out.println("���ļ�Ϊֻ���ļ���������ɾ����");
				}	
			}else if (nowCatalog.getAttr()==1) {
				System.out.println("�Ҳ������ļ�������·���Ƿ���ȷ");
			}
		}
	}
	
	public void changeDir(String[] roadName) {//�ı䵱ǰĿ¼
		FileModel theCatalog=nowCatalog;
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr() == 1) {
				System.out.println();
			} else if(nowCatalog.getAttr() == 0) {
				System.out.println("�Ҳ�����Ŀ¼������·���Ƿ���ȷ");
				nowCatalog=theCatalog;
			}
		}
	}

	public void editFile(String name, String input) {
			//�༭�ļ�
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
					System.out.println("���ļ�Ϊֻ���ļ���������༭��");
				}	
		} else{
			System.out.println("�༭ʧ�ܣ���ȷ���ļ����Ƿ���ȷ����");					
		}
	} 
	
	
	public boolean searchRoad(String[] roadName) {//ʶ�������еľ���·�������·���������ھ���·�������·�����½��ļ���Ŀ¼
		
		FileModel theCatalog = nowCatalog; //���öϵ��¼��ǰĿ¼
		
		if(all.containsKey(roadName[roadName.length-2])|roadName[0].equals(".")|roadName[0].equals("..")) { //��������ļ������޸��ļ�
			
			if(roadName[0].equals(".")) {//�ڵ�ǰĿ¼�²���
				int i;
				for(i=1; i<roadName.length-1; i++) {//���ҵ���һ��Ϊֹ
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
 
					} else {
						System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
			
			else if(roadName[0].equals("..")) {//��һ��Ŀ¼
				nowCatalog=nowCatalog.getFather();
				int i;
				for(i=1; i<roadName.length-1; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
					} 
					else if(roadName[i].equals("..")) {
						nowCatalog=nowCatalog.getFather();
					}
					else {
						System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
				if("root".equals(roadName[0])) {	//�ж�����·������Ŀ¼�Ƿ�root
					nowCatalog = root;
					int i;
					for(i=1; i<roadName.length-1; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
	 
						} else {
							System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
				}else if(nowCatalog.subMap.containsKey(roadName[0])) {//���ҵ�ǰĿ¼���Ƿ������Ŀ¼
					nowCatalog = nowCatalog.subMap.get(roadName[0]);
					int i;
					for(i=1; i<roadName.length-1; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
	 
						} else {
							System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
					System.out.println("��������ȷ�ľ���·��,����·������root/��ͷ��");
					return false;
				}
			} 
		}else {
			System.out.println("���ļ���Ŀ¼�����ڣ���������ȷ��·��");
//			showFile();
			return false;
		}
		
	}
	
	public void readFile(String name) {//�鿴�ļ�
		FileModel value = nowCatalog.subMap.get(name);
		if(value.getAttr()==1) {
			System.out.println("�ļ������ڣ�");
		} else {
			int startNum=value.getStartNum();
			int nextPoint = fat[startNum];
			int nowPoint = startNum;
			int count=0;
			while(fat[nowPoint]!=0) {
				//�ж��Ƿ��н�����ǣ����Ϊ������ǣ������ѭ��
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

	public void copyFile(String[] roadNameCopy,String[] roadNamePaste) {//�����ļ�
		String[] split = roadNameCopy[roadNameCopy.length-1].split("\\.");
		roadNameCopy[roadNameCopy.length-1]=split[0];
		FileModel theCatalog=nowCatalog;
		if(searchFile(roadNameCopy)) {
			nowCatalog=nowCatalog.getFather();
			FileModel value=nowCatalog.subMap.get(split[0]);
			if(value.getAttr()==1) {
				System.out.println("�����Ƶ��ļ������ڣ�");
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
					System.out.println("���Ƴɹ�");
				}
			}
		}
	}
	
	public void attr(String[] roadName,String[] strs) {//�޸��ļ�����
		String[] split=roadName[roadName.length-1].split("\\.");
		roadName[roadName.length-1]=split[0];
		if(searchFile(roadName)) {
			if(nowCatalog.getAttr()==1) {
				System.out.println("������ֻ�ܶ��ļ�������");
			}else if(nowCatalog.getAttr()==0) {
				for(int i=2;i<strs.length;i++) {
					switch(strs[i]) {
					case "+h"://���������ļ�����
						if(nowCatalog.getHide()==0) {
							nowCatalog.setHide(1);
						}else {
							System.out.println("�ļ��ѱ����أ��޷����ļ����أ�");
						}
						break;
					case "-h":
						if(nowCatalog.getHide()==1) {
							nowCatalog.setHide(0);
						}else {
							System.out.println("�ļ�δ�����أ��޷����ļ�������أ�");
						}
						break;
					case "+r"://����ֻ������
						if(nowCatalog.getRO()==0) {
							nowCatalog.setRO(1);
						}else {
							System.out.println("�ļ��ѱ�����Ϊֻ�����޷�����Ϊֻ���ļ���");
						}
						break;
					case "-r" :
						if(nowCatalog.getRO()==1) {
							nowCatalog.setRO(0);
						}else {
							System.out.println("�ļ�δ������Ϊֻ�����޷����ֻ�����ԣ�");
						}
						break;
					default:
						System.out.println("������������,������ +h/-h/+r/-r");
						System.out.println(strs[i]);
						break;
					}
				}
			}
		}
	}
	public void backFile() {
		if(nowCatalog.getFather() == null) {
			System.out.println("���ļ�û���ϼ�Ŀ¼");
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
	
	public boolean searchFile(String[] roadName) {//���ݾ���·�������·��Ѱ���ļ�,���ڶ��Ѿ����ڵ��ļ���Ŀ¼ִ�д򿪡�ɾ���Ȳ���
		// TODO �Զ����ɵķ������
		
		FileModel theCatalog = nowCatalog; //���öϵ��¼��ǰĿ¼
		
		if(all.containsKey(roadName[roadName.length-1])|roadName[0].equals(".")|roadName[0].equals("..")) { //��������ļ������޸��ļ�
			
			if(roadName[0].equals(".")) {//�ڵ�ǰĿ¼�²���
				int i;
				for(i=1; i<roadName.length; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
 
					} else {
						System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
			
			else if(roadName[0].equals("..")) {//��һ��Ŀ¼
				nowCatalog=nowCatalog.getFather();
				int i;
				for(i=1; i<roadName.length; i++) {
					if(nowCatalog.subMap.containsKey(roadName[i])) {
						nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
					} 
					else if(roadName[i].equals("..")) {
						nowCatalog=nowCatalog.getFather();
					}
					else {
						System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
				if("root".equals(roadName[0])) {	//�ж�����·������Ŀ¼�Ƿ�root
					nowCatalog = root;
					int i;
					for(i=1; i<roadName.length; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
	 
						} else {
							System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
							nowCatalog = theCatalog;
							break;
						}
					}
					if(i==roadName.length) {
						return true;	
					}else {
						return false;
					}
				}else if(nowCatalog.subMap.containsKey(roadName[0])) {//���ҵ�ǰĿ¼���Ƿ������Ŀ¼
					nowCatalog = nowCatalog.subMap.get(roadName[0]);
					int i;
					for(i=1; i<roadName.length; i++) {
						if(nowCatalog.subMap.containsKey(roadName[i])) {
							nowCatalog = nowCatalog.subMap.get(roadName[i]); //һ��һ�����²�
	 
						} else {
							System.out.println("�Ҳ�����·���µ��ļ���Ŀ¼������·���Ƿ���ȷ");
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
					System.out.println("��������ȷ�ľ���·��,����·������root/��ͷ��");
					return false;
				}
			} 
		}else {
			System.out.println("���ļ���Ŀ¼�����ڣ���������ȷ��·����");
			return false;
		}
	}

}
