# Project MD2Lang帮助文档

欢迎访问Project MD2Lang。本项目旨在帮助您更快速地完成Markdown到语言文件的转换。

本项目的初衷是将Markdown语法编辑的文件转换到单行的lang-tag语法以便在AC等游戏中进行渲染。

Project MD2Lang完全由Java语言编写并减少所有硬编码和平台有关的路径格式。因此，您可以在任何JVM支持的操作系统平台上使用MD2Lang。我们已经测试过的平台有：Windows（Windows 10 x64 Pro）、Linux（Ubuntu 14.10 x64 Desktop）和Macintosh（OS X 10.11 x64 EI Captain），以上所有平台使用JRE x32 8u50运行时环境。在这些操作系统平台上，MD2Lang的功能十分完美，我们推荐您在转换时使用这些平台和版本以便保证得出的结果与我们的测试完全相同。

MD2Lang有两种运行模式：GUI模式和Console模式。您可以在两种运行方式下获得一样的运行结果。这两种运行模式已经在上述提及的所有操作系统平台下成功通过测试。

### MD2Lang GUI模式

GUI模式是一种简单的操作方式。进入GUI模式的最简单的方法是直接双击jar文件（在Java运行时环境配置完全的Windows和OS X下可用）。
另一种方法是通过命令行启动：

首先，您需要切换到md2lang.jar文件所在的目录，然后执行下列命令：<br/>
<code>java -jar md2lang.jar</code><br/>
接着，MD2LangGUI也会启动。

__特别说明：您可以在GUI中获得关于使用方法的信息，但这些信息以英文书写，如果您需要中文的提示信息，请联系Lambda Innovation。__

我们准备了两张屏幕快照，分辨向您展示了MD2Lang在Windows和Mac下的运行状况，如果您需要具体的信息，请访问我的Google+。
![OS X](https://lh5.googleusercontent.com/-KfsOrPSaA-s/VhEnXDiHpYI/AAAAAAAAABc/QNKt2zXkYa0/w495-h575-no/%25E5%25B1%258F%25E5%25B9%2595%25E5%25BF%25AB%25E7%2585%25A7%2B2015-10-04%2B21.19.11.png) 
![Windows](https://lh4.googleusercontent.com/-ViTBJX3NtbU/VhEokoGpfVI/AAAAAAAAABo/eRzT2JqfrRg/w469-h562-no/MD2LangWindowsScreenSnap.PNG)

### MD2Lang Command模式

Command模式也可以帮助您进行转换工作。

通过Command模式，您可以获得与GUI模式同样的结果。但在Command模式下，您可以要求MD2Lang输出Stacktrace以进行调试。

如果要使用Command模式，请定位到md2lang.jar所在的目录，然后执行下列命令：<br/>
<code>java -jar md2lang.jar --command <input_file> <output_file> [--stacktrace]</code><br/>
在这个命令中，截止到output file部分是必要的。你需要注意所有字符的大小写和空格的数量，并将input file和output file部分替换为您需要进行转换的文件的路径。
__特别说明：文件可以以windows格式书写，也可以以unix通用格式书写。__
最后的--stacktrace是可选的，如果您在命令中加入这个参数，则程序会输出所有调试信息以便调试。

我们也制作了一个截图展示了Command模式的用法，如果您需要具体的信息，请访问我的Google+。
![OS X Command Mode](https://lh5.googleusercontent.com/-v5o8JfQRWg4/VhEuZX6-p5I/AAAAAAAAADI/cSTMFAJocg0/w573-h480-no/%25E5%25B1%258F%25E5%25B9%2595%25E5%25BF%25AB%25E7%2585%25A7%2B2015-10-04%2B21.42.23.png)<br/>
__特别注意：这张截图是在OS X环境下进行的，如果您使用的是Windows或Linux平台。您可以使用同样的命令，但输出的内容可能有一些区别。__

### 可转换的内容

使用MD2Lang时，您能够在Markdown中书写的内容是有限制的。一般来说，MD2Lang Version 1能够转换以下Markdown语法：
<table>
    <th>
        <td>名称</td>
        <td>markdown格式</td>
        <td>lang格式</td>
    </th>
    <tr>
        <td>1</td>
        <td>标题1级</td>
        <td>
            <code># example</code> 或 <code>example</code>
            <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>-------</code>
        </td>
        <td><code>[h1]example[/h1]</code></td>
    </tr>
    <tr>
        <td>2</td>
        <td>标题2级</td>
        <td><code>## example<code></td>
        <td><code>[h2]example[/h2]</code></td>
    </tr>
    <tr>
        <td>3</td>
        <td><code>标题3级</code></td>
        <td><code>### example</code></td>
        <td><code>[h3]example[/h3]</code></td>
    </tr>
    <tr>
        <td>4</td>
        <td><code>标题4级</code></td>
        <td><code>#### example</code></td>
        <td><code>[h4]example[/h4]</code></td>
    </tr>
    <tr>
        <td>5</td>
        <td>标题5级</td>
        <td><code>##### example</code></td>
        <td><code>[h5]example[/h5]</code></td>
    </tr>
    <tr>
        <td>6</td>
        <td>标题6级</td>
        <td><code>###### example</code></td>
        <td><code>[h6]example[/h6]</code></td>
    </tr>
    <tr>
        <td>7</td>
        <td>删除线</td>
        <td><code>~~example~~</code></td>
        <td><code>[stth]exampl[/stth]</code></td>
    </tr>
    <tr>
        <td>8</td>
        <td>代码引用</td>
        <td><code>`example`</code> 或 <code>``example``</code><br/>或 <code>```example```</code></td>
        <td><code>[code]example[/code]</code></td>
    </tr>
    <tr>
        <td>8.5</td>
        <td colspan="3"><font color="red">
            语法注意：为了简单起见，对于[code]，我们只编写了一个、两个或三个<code>`</code>符号的情况。如果确实需要能够适应多个 <code>`</code>的情况，请发布一条Issue来通知我们，您的要求将被排入日程。
        </font></td>
    </tr>
    <tr>
        <td>9</td>
        <td>加粗字体</td>
        <td><code>__example__</code></td>
        <td><code>[bold]example[/bold]</code></td>
    </tr>
    <tr>
        <td>10</td>
        <td>高亮字体</td>
        <td><code>==example==</code></td>
        <td><code>[hili]example[/hili]</code></td>
    </tr>
    <!--{"&nbsp;", " "},
			{"-", ""},
			{"=", ""},
			{"&equa;", "="},
			{"&hyph;", "-"},
			{"\n\\*\n", "[npar]"},
			{"\t", "&tab;"},
			{"  \n", "[ln]"},
			{"\n\n", "[ln]"},
			{"\n", ""}-->
    <tr>
        <td>11</td>
        <td></td>
    </tr>
</table>
