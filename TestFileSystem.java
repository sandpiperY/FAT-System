package fileSys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import fileSys.FileAccessTable;
 
public class TestFileSystem {
	public static void main(String[] args) {
		try{
		FileAccessTable manager = new FileAccessTable();
		meun(manager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	public static void meun(FileAccessTable manager) {
		Scanner s = new Scanner(System.in);
		String str = null;
		System.out.println("***********************************" + "��ӭʹ��ģ��FAT�ļ�ϵͳ" + "***********************************");
		System.out.println();
		System.out.println("����ʣ��ռ�:" + manager.empty + "            " + "������ʾ������������  �˳�ϵͳ������exit  �鿴��������������help");
		System.out.println();
		System.out.print(manager.getNowCatalog()+">");
		while ((str = s.nextLine()) != null) {
			if (str.equals("exit")) {
				System.out.println("���˳�ϵͳ����л����ʹ��");
				break;
			}
 
			String[] strs = editStr(str);
			switch (strs[0]) {
			case "new"://�����ļ�
				if (strs.length < 2) {
					System.out.println("���������������������");
				} else {
					String[] roadName = strs[1].split("/");
					if(roadName.length==1) {
						String[] split = strs[1].split("\\."); // ʹ��ת���ַ�"\\."
						
						if(strs.length==2) {
							manager.createFile(split[0], split[1],1);
						}else {
							manager.createFile(split[0], split[1],1);
							String input="";
							for(int i=2;i<strs.length;i++) {
								input=input+" "+strs[i];
							}
							manager.editFile(split[0], input);
						}

					}else {
						if(manager.searchRoad(roadName)) {
							String[] split = roadName[roadName.length-1].split("\\."); // ʹ��ת���ַ�"\\."
							if(strs.length==2) {
								manager.createFile(split[0], split[1],1);
							}else {
								manager.createFile(split[0], split[1],1);
								String input="";
								for(int i=2;i<strs.length;i++) {
									input=input+" "+strs[i];
								}
								manager.editFile(split[0], input);
							}
						}
					}	
				}
				
				break;
			case "md"://����Ŀ¼
				if (strs.length < 2) {
					System.out.println("��������������������飡");
				} else {
					String[] roadName = strs[1].split("/");
					if(roadName.length==1) {
						manager.createCatolog(strs[1]);
					}else {
						if(manager.searchRoad(roadName)) {
							manager.createCatolog(roadName[roadName.length-1]);
						}
					}
				}
				break;

			case "cd":
				if (strs.length < 2) {
					System.out.println("��������������������飡");
				} else {
					String[] roadName = strs[1].split("/");
					manager.changeDir(roadName);
				}
				break;
			case "cd ..":
				manager.backFile();
				break;
			case "dir":
				if (strs.length == 1) {
					manager.showFile();
				} else {
					String[] roadName = strs[1].split("/");
					FileModel theCatalog=manager.nowCatalog;//���öϵ��¼��ǰλ��
					manager.changeDir(roadName);
					manager.showFile();
					manager.nowCatalog=theCatalog;
				}
				break;
			case "rd":
				if (strs.length < 2) {
					System.out.println("��������������������飡");
				} else {
					String[] roadName = strs[1].split("/");
					FileModel theCatalog=manager.nowCatalog;
					manager.deleteDir(roadName);
					manager.showFile();
					manager.nowCatalog=theCatalog;
				}
				break;
			case "del":
				if (strs.length < 2) {
					System.out.println("��������������������飡");
				} else {
					String[] roadName = strs[1].split("/");
					String[] split = roadName[roadName.length-1].split("\\.");
					roadName[roadName.length-1]=split[0];
					FileModel theCatalog=manager.nowCatalog;
					manager.deleteFile(roadName);
					manager.showFile();
					manager.nowCatalog=theCatalog;
				}
				break;
			case "edit":
				if(strs.length<3) {
					System.out.println("��������������������飡");
				}else {
					String[] roadName = strs[1].split("/");
					String[] split = roadName[roadName.length-1].split("\\.");
					roadName[roadName.length-1]=split[0];
					FileModel theCatalog=manager.nowCatalog;
					if(manager.searchFile(roadName)) {
						manager.nowCatalog=manager.nowCatalog.getFather();
						String input="";
						for(int i=2;i<strs.length;i++) {
							input=input+" "+strs[i];
						}
						System.out.println(input);
						manager.editFile(split[0],input);
					}
					manager.nowCatalog=theCatalog;
				}
				break;
			case "type":
				if(strs.length<2) {
					System.out.println("��������������������飡");
				}else {
					String[] roadName = strs[1].split("/");
					String[] split = roadName[roadName.length-1].split("\\.");
					roadName[roadName.length-1]=split[0];
					FileModel theCatalog=manager.nowCatalog;
					if(roadName.length==1) {
						manager.readFile(split[0]);
					}
					else if(manager.searchFile(roadName)) {
						manager.nowCatalog=manager.nowCatalog.getFather();
						manager.readFile(split[0]);
					}
					manager.nowCatalog=theCatalog;
				}
				break;
			case "copy":
				if(strs.length<3) {
					System.out.println("��������������������飡");
				}else {
					String[] roadNameCopy=strs[1].split("/");
					String[] roadNamePaste=strs[2].split("/");
					FileModel theCatalog=manager.nowCatalog;
					manager.copyFile(roadNameCopy, roadNamePaste);
					manager.nowCatalog=theCatalog;
				}
				break;
			case "attr":
				if(strs.length<3) {
					System.out.println("��������������������飡");
				}else {
					String[] roadName=strs[1].split("/");
					FileModel theCatalog=manager.nowCatalog;
					manager.attr(roadName, strs);
					manager.nowCatalog=theCatalog;
				}
				break;
			case "help": {
				System.out.println("�������£��ո���ʡ�ԣ���������Ĭ��֧�����·���;���·��Ѱַ��");
				System.out.println("md RoadName/catalogName");
				System.out.println("<������ǰĿ¼�µ���Ŀ¼ �磺md users>");
				System.out.println("<�����·���´���Ŀ¼ �磺md ./users/chen Ҳ��д�� md users/chen>");
				System.out.println("<�ھ���·���´���Ŀ¼ �磺md root/users/chen>");
				System.out.println();
				System.out.println("new RoadName/fileName.fileType");
				System.out.println("<����һ�����ļ� �磺new a.txt>");
				System.out.println();
				System.out.println("new RoadName/fileName.fileType FileContent");
				System.out.println("<�����ļ��������ļ����� �磺new a.txt aaaaaaa>");
				System.out.println();
				System.out.println("cd RoadName/catalogName");
				System.out.println("<�л�Ŀ¼ �磺 cd users>");
				System.out.println("<�л����ϼ�Ŀ¼�� cd..>");
				System.out.println();
				System.out.println("rd RoadName/catalogName");
				System.out.println("<ɾ��Ŀ¼ �磺 rd users>");
				System.out.println();
				System.out.println("dir RoadName/catalogName");
				System.out.println("<���ļ�Ŀ¼ �磺 dir users>");
				System.out.println("<�е�ǰĿ¼�µ��ļ�Ŀ¼�� dir>");
				System.out.println();
				System.out.println("del RoadName/filegName.fileType");
				System.out.println("<ɾ���ļ� �磺 del a.txt>");
				System.out.println();
				System.out.println("type RoadName/fileName.fileType");
				System.out.println("<���ļ� �磺type a.txt>");
				System.out.println();
				System.out.println("edit RoadName/fileName.fileType FileContent");
				System.out.println("<�༭�ļ� �磺edit a.txt aaaaaaaa>");
				System.out.println();
				System.out.println("copy RoadNameCopy/fileName.fileType RoadNamePaste/fileName.fileType");
				System.out.println("<�༭�ļ� �磺copy a.txt users/b.txt>");
				System.out.println();
				System.out.println("attr RoadNameCopy/fileName.fileType (+h/-h/+r/-r)");
				System.out.println("<�����ļ����� �磺attr a.txt +h ���ļ����� ");
				System.out.println("                 attr a.txt -h ȡ���ļ����� ");
				System.out.println("                 attr a.txt +r ���ļ�����Ϊֻ���ļ� ");
				System.out.println("                 attr a.txt -r ȡ���ļ���ֻ������ ");
				System.out.println("                 attr a.txt +r +h ���ļ�����Ϊֻ���ļ�������>");
				System.out.println();
				break;
			}
			default:
				System.out.println("��"+strs[0]+"��������ȷ������");
			}
			System.out.print(manager.getNowCatalog()+">");
		}	
		
	}
 
	public static String[] editStr(String str) {
		String[] strs = str.split("\\s+");
//		for(String ss : strs)
//		    System.out.println(ss);
		return strs;
	}
	
}