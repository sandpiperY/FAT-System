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
		System.out.println("***********************************" + "欢迎使用模拟FAT文件系统" + "***********************************");
		System.out.println();
		System.out.println("磁盘剩余空间:" + manager.empty + "            " + "请在提示符后输入命令  退出系统请输入exit  查看可用命令请输入help");
		System.out.println();
		System.out.print(manager.getNowCatalog()+">");
		while ((str = s.nextLine()) != null) {
			if (str.equals("exit")) {
				System.out.println("已退出系统，感谢您的使用");
				break;
			}
 
			String[] strs = editStr(str);
			switch (strs[0]) {
			case "new"://创建文件
				if (strs.length < 2) {
					System.out.println("您所输入的命令有误，请检查");
				} else {
					String[] roadName = strs[1].split("/");
					if(roadName.length==1) {
						String[] split = strs[1].split("\\."); // 使用转义字符"\\."
						
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
							String[] split = roadName[roadName.length-1].split("\\."); // 使用转义字符"\\."
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
			case "md"://创建目录
				if (strs.length < 2) {
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
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
					FileModel theCatalog=manager.nowCatalog;//设置断点记录当前位置
					manager.changeDir(roadName);
					manager.showFile();
					manager.nowCatalog=theCatalog;
				}
				break;
			case "rd":
				if (strs.length < 2) {
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
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
					System.out.println("您所输入的命令有误，请检查！");
				}else {
					String[] roadName=strs[1].split("/");
					FileModel theCatalog=manager.nowCatalog;
					manager.attr(roadName, strs);
					manager.nowCatalog=theCatalog;
				}
				break;
			case "help": {
				System.out.println("命令如下（空格不能省略）所有命令默认支持相对路径和绝对路径寻址：");
				System.out.println("md RoadName/catalogName");
				System.out.println("<创建当前目录下的子目录 如：md users>");
				System.out.println("<在相对路径下创建目录 如：md ./users/chen 也可写作 md users/chen>");
				System.out.println("<在绝对路径下创建目录 如：md root/users/chen>");
				System.out.println();
				System.out.println("new RoadName/fileName.fileType");
				System.out.println("<创建一个空文件 如：new a.txt>");
				System.out.println();
				System.out.println("new RoadName/fileName.fileType FileContent");
				System.out.println("<创建文件并输入文件内容 如：new a.txt aaaaaaa>");
				System.out.println();
				System.out.println("cd RoadName/catalogName");
				System.out.println("<切换目录 如： cd users>");
				System.out.println("<切换到上级目录： cd..>");
				System.out.println();
				System.out.println("rd RoadName/catalogName");
				System.out.println("<删除目录 如： rd users>");
				System.out.println();
				System.out.println("dir RoadName/catalogName");
				System.out.println("<列文件目录 如： dir users>");
				System.out.println("<列当前目录下的文件目录： dir>");
				System.out.println();
				System.out.println("del RoadName/filegName.fileType");
				System.out.println("<删除文件 如： del a.txt>");
				System.out.println();
				System.out.println("type RoadName/fileName.fileType");
				System.out.println("<打开文件 如：type a.txt>");
				System.out.println();
				System.out.println("edit RoadName/fileName.fileType FileContent");
				System.out.println("<编辑文件 如：edit a.txt aaaaaaaa>");
				System.out.println();
				System.out.println("copy RoadNameCopy/fileName.fileType RoadNamePaste/fileName.fileType");
				System.out.println("<编辑文件 如：copy a.txt users/b.txt>");
				System.out.println();
				System.out.println("attr RoadNameCopy/fileName.fileType (+h/-h/+r/-r)");
				System.out.println("<设置文件属性 如：attr a.txt +h 将文件隐藏 ");
				System.out.println("                 attr a.txt -h 取消文件隐藏 ");
				System.out.println("                 attr a.txt +r 将文件设置为只读文件 ");
				System.out.println("                 attr a.txt -r 取消文件的只读属性 ");
				System.out.println("                 attr a.txt +r +h 将文件设置为只读文件并隐藏>");
				System.out.println();
				break;
			}
			default:
				System.out.println("“"+strs[0]+"”不是正确的命令");
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