# FAT-System
## 程序说明
TestFileSystem是命令交互，编译三个文件之后运行TestFileSystem进行测试
这个代码是我三年之前写的了，很多细节都已记不太清，能够改进的地方也很多，如果要交作业建议在理解代码的基础上自己再改一改
## 代码结构
![image](https://github.com/user-attachments/assets/e03fd5da-f212-48a6-b859-7e2d0fa6ae7f)

## 使用说明
当前路径+>为命令提示符，在命令提示符后输入命令，按下回车命令会被执行。
进入文件系统后，自动创建根目录，并以根目录为当前目录，显示命令提示符形式为
root/>
当前路径改变后，命令提示符随之改变,如
root/user/administer>
所有命令支持绝对路径和相对路径的使用，绝对路径需以root/开头，如root/users/administer
命令格式如下（空格不可省略）
1.	列文件目录命令：dir
命令形式：dir，dir <路径名>
root/>dir     列当前目录（根目录）下目录和文件
root/>dir   root/user   当前目录是根，列root/user目录下的目录和文件

2.	创建目录命令：md
命令形式：md <目录名> ，md <路径/目录名>
root/user/administer>md data  在root/user/administer下创建子目录data
root/user/administer>md  ../file/data  在root/user/file下创建子目录data，创建目录后会自动跳转到新建目录所在的父目录。

3.	删除目录命令：rd
命令形式：rd <目录名>，rd<路径/目录名>
root/user/administer>rd data  删除/user/administer下的子目录data
root/user/administer>rd user/file/data  删除/user/file下的子目录data
该命令只能删除空目录，非空目录不允许删除。如目录非空，则提示目录非空，无法删除。

4.	修改当前目录：cd
命令形式： cd <目录名>，cd <路径/目录名>
root/user>cd  administer  改变当前目录为当前目录下的子目录/user/administer
root/user/administer>cd .. 改变当前目录为当前目录的父目录/user
root/user/administer>cd root/user/file  改变当前目录为/user/file

5.	创建文件命令：new
命令形式： new <文件名> (<文件内容>)，new <路径/文件名> (<文件内容>)
创建文件有两种方式，第一种，创建一个空文件。
root/user/administer>new hello.txt 创建当前目录下的空文件
root/user/administer>new data/hello.txt创建当前目录下一级目录data下的空文件
第二种，创建文件后立即输入文件内容。
root/user/administer>new root/user/file/hello.txt helloworld创建绝对路径下的新文件，并输入文件内容。

6.	删除文件命令：del
命令形式：del <文件名>，del <路径名/文件名>
root/user/administer>del hello.txt  删除当前目录中的文件
root/user/administer>del ../file/hello.txt  删除相对路径下的文件

7.	编辑文件命令：edit
命令形式：edit <文件名> <编辑内容>,edit <路径名/文件名> <编辑内容>
如文件内有内容，执行命令时会先清空内容，再将新的内容写入文件。
root/user/administer>edit hello.txt helloworld! 编辑当前目录中的文件
root/user/administer>edit /user/file/hello.txt helloworld!  编辑绝对路径下的文件

8.	查看文件命令：type
命令形式：type<文件名>,type<路径名/文件名>
输出文件内容。
root/user/administer>type hello.txt  查看当前目录中的文件
root/user/administer>type /user/file/hello.txt  查看绝对路径下的文件
9.	复制文件命令：copy
命令形式：copy <文件名> <文件名>
copy <路径名/文件名>  <文件名>
copy <文件名>  <路径名/文件名>
copy <路径名/文件名> <路径名/文件名>
root/user/administer>copy hello.txt hi.txt  复制当前目录下文件为新文件 
root/user/administer>copy /user/file/a.txt b.txt 复制某路径下的文件到当前目录并改名
root/user/administer>copy a.txt  /user/file/a.txt 复制当前目录文件到某路径
root/user/administer>copy data/a.txt  ../user/file/b.txt 复制一个路径下文件到另一路径

10.	设置文件属性命令：attr
命令形式 attr <文件名> <+r/-r/+h/-h> ，attr <路径名/文件名> <+r/-r/+h/-h>
+h 将文件隐藏，-h 取消文件隐藏，+r 将文件设置为只读文件，-r 取消文件的只读属性
root/user/administer>attr hello.txt +r -h  修改当前目录中的文件属性
root/user/administer>attr /user/file/hello.txt -r  修改绝对路径下的文件属性
被隐藏的文件在执行dir命令时不会被显示，只读属性文件不允许编辑和删除。

11.	退出系统命令：exit
命令形式 exit

12.	帮助命令：help
查看所有可用的命令和命令输入格式
命令形式 help
