<font color="red"><b>This is Chinese Documentation. If you need English version, please send an issue to this repo. Then your request will be discharged into the schedule immediately.</b></font>

<h1>Project MD2Lang帮助文档</h1>

<p>欢迎访问Project MD2Lang。本项目旨在帮助您更快速地完成Markdown到语言文件的转换。</p>

<p>本项目的初衷是将Markdown语法编辑的文件转换到单行的lang-tag语法以便在AC等游戏中进行渲染。</p>

<p>Project MD2Lang完全由Java语言编写并减少所有硬编码和平台有关的路径格式。因此，您可以在任何JVM支持的操作系统平台上使用MD2Lang。我们已经测试过的平台有：Windows（Windows 10 x64 Pro）、Linux（Ubuntu 14.10 x64 Desktop）和Macintosh（OS X 10.11 x64 EI Captain），以上所有平台使用JRE x32 8u50运行时环境。在这些操作系统平台上，MD2Lang的功能十分完美，我们推荐您在转换时使用这些平台和版本以便保证得出的结果与我们的测试完全相同。</p>

<p>MD2Lang有两种运行模式：GUI模式和Console模式。您可以在两种运行方式下获得一样的运行结果。这两种运行模式已经在上述提及的所有操作系统平台下成功通过测试。</p>

<h3>MD2Lang GUI模式</h3>

<p>GUI模式是一种简单的操作方式。进入GUI模式的最简单的方法是直接双击jar文件（在Java运行时环境配置完全的Windows和OS X下可用）。
另一种方法是通过命令行启动：</p>

<p>首先，您需要切换到md2lang.jar文件所在的目录，然后执行下列命令：<br/>
<code>java -jar md2lang.jar</code><br/>
接着，MD2LangGUI也会启动。</p>

<p><b>特别说明：您可以在GUI中获得关于使用方法的信息，但这些信息以英文书写，如果您需要中文的提示信息，请联系Lambda Innovation。您的要求将被立刻排入日程。</b></p>

<p>我们准备了两张屏幕快照，分辨向您展示了MD2Lang在Windows和Mac下的运行状况，如果您需要具体的信息，请访问我的Google+。
<img src="https://lh5.googleusercontent.com/-KfsOrPSaA-s/VhEnXDiHpYI/AAAAAAAAABc/QNKt2zXkYa0/w495-h575-no/%25E5%25B1%258F%25E5%25B9%2595%25E5%25BF%25AB%25E7%2585%25A7%2B2015-10-04%2B21.19.11.png" />
<img src="https://lh4.googleusercontent.com/-ViTBJX3NtbU/VhEokoGpfVI/AAAAAAAAABo/eRzT2JqfrRg/w469-h562-no/MD2LangWindowsScreenSnap.PNG" /></p>

<h3>MD2Lang Command模式</h3>

<p>Command模式也可以帮助您进行转换工作。</p>

<p>通过Command模式，您可以获得与GUI模式同样的结果。但在Command模式下，您可以要求MD2Lang输出Stacktrace以进行调试。</p>

<p>如果要使用Command模式，请定位到md2lang.jar所在的目录，然后执行下列命令：<br/>
<code>java -jar md2lang.jar --command <input_file> <output_file> [--stacktrace]</code><br/>
在这个命令中，截止到output file部分是必要的。你需要注意所有字符的大小写和空格的数量，并将input file和output file部分替换为您需要进行转换的文件的路径。<br/>
<b>特别说明：文件可以以windows格式书写，也可以以unix通用格式书写。</b>
最后的--stacktrace是可选的，如果您在命令中加入这个参数，则程序会输出所有调试信息以便调试。</p>

<p>我们也制作了一个截图展示了Command模式的用法，如果您需要具体的信息，请访问我的Google+。
<img src="https://lh5.googleusercontent.com/-v5o8JfQRWg4/VhEuZX6-p5I/AAAAAAAAADI/cSTMFAJocg0/w573-h480-no/%25E5%25B1%258F%25E5%25B9%2595%25E5%25BF%25AB%25E7%2585%25A7%2B2015-10-04%2B21.42.23.png" /><br/>
<b>特别注意：这张截图是在OS X环境下进行的，如果您使用的是Windows或Linux平台。您可以使用同样的命令，但输出的内容可能有一些区别。</b></p>

<h3>可转换的内容</h3>

<p>使用MD2Lang时，您能够在Markdown中书写的内容是有限制的。一般来说，MD2Lang Version 1能够转换以下Markdown语法：</p>

<table>
    <th>
        <td>名称</td>
        <td>markdown格式</td>
        <td>lang格式</td>
    </th>
    <tr>
        <td>1</td>
        <td>标题：1级</td>
        <td>
            <code># example</code> 或 <code>example</code>
            <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>-------</code>
        </td>
        <td><code>[h1]example[/h1]</code></td>
    </tr>
    <tr>
        <td>2</td>
        <td>标题：2级</td>
        <td><code>## example</code></td>
        <td><code>[h2]example[/h2]</code></td>
    </tr>
    <tr>
        <td>3</td>
        <td>标题：3级</td>
        <td><code>### example</code></td>
        <td><code>[h3]example[/h3]</code></td>
    </tr>
    <tr>
        <td>4</td>
        <td>标题：4级</td>
        <td><code>#### example</code></td>
        <td><code>[h4]example[/h4]</code></td>
    </tr>
    <tr>
        <td>5</td>
        <td>标题：5级</td>
        <td><code>##### example</code></td>
        <td><code>[h5]example[/h5]</code></td>
    </tr>
    <tr>
        <td>6</td>
        <td>标题：6级</td>
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
        <td colspan="3"><font color="red">语法注意：为了简单起见，对于[code]，我们只编写了一个、两个或三个<code>`</code>符号的情况。如果确实需要能够适应多个 <code>`</code>的情况，请发布一条Issue来通知我们，您的要求将被立刻排入日程。</font></td>
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
    <tr>
        <td>11</td>
        <td>转义字符：空格</td>
        <td><code>&amp;nbsp;</code></td>
        <td><code>&nbsp;</code></td>
    </tr>
    <tr>
    	<td>12</td>
    	<td>转义字符：等于号</td>
    	<td><code>&amp;equa;</code></td>
    	<td><code>=</code></td>
    </tr>
    <tr>
    	<td>13</td>
    	<td>转义字符：连字符</td>
    	<td><code>&amp;hyph;</code></td>
    	<td><code>-</code></td>
    </tr>
    <tr>
    	<td>12-13</td>
    	<td colspan="3"><font color="red">语法注意：如果你需要在md文档中使用单独的（没有语法意义的）等于号或连字符，请使用上文提供的转义字符。否则将被视为语法符号并清除。</font></td>
    </tr>
    <tr>
    	<td>14</td>
    	<td>新段落</td>
    	<td><code>example<br/>***<br/>foo</code></td>
    	<td><code>example[npar]foo</code></td>
    </tr>
    <tr>
    	<td>15</td>
    	<td>Tab字符</td>
    	<td><code>foo&emsp;bar</code>(Tab, \t)</td>
    	<td>foo&tab;bar</td>
    </tr>
    	<td>16</td>
    	<td>新行</td>
    	<td><code>exmaple <br/>foo</code>或<code>example<br/><br/>foo</code></td>
    	<td><code>example[ln]foo</code></td>
    </tr>
    <tr>
    	<td>16.5</td>
    	<td colspan="3"><font color="red">语法注意：文档中所有换行都会被去除，除了上述格式中能够被转换为<code>[ln]</code>的。另外如果您需要换行，也可以直接在文档中编写<code>[ln]</code></font></td>
    </tr>
    <tr>
    	<td>17</td>
    	<td>引入图片</td>
    	<td><code>![http://cn.bing.com/logo.png](123, 456)</code></td>
    	<td><code>[img src="http://cn.bing.com/logo.png" width=123 height=456]</code></td>
    </tr>
</table>

<p>如果您需要能够转换更多语法的MD2Lang，请在本repo中发一条issue，您的要求将被立刻排入日程。</p>

<h3>版权信息和二次开发</h3>

<p>Project MD2Lang的作者是Lambda Innovation的GISDYT，项目发起者是LI的组长WeAthFolD。</p>

<p>本项目基于GPL3.0协议开源，关于此协议，您可在Google Developer上查询到全文，我们在这里说明比较重要的项目：</p>

<p>
1. 您可以自由地访问、复制、修改和分发本软件，但要保证所有代码都是完整的。
2. 您必须在分发时无偿提供源代码，不得将源代码进行任何形式的销售或变相捆绑销售。
3. 您在分发时，可以将源代码放入光盘、U盘等介质或打印出来从窗口扔出去。
4. 您可以去除所有我们的版权信息，但必须保持开源，并附带GPL协议。
5. 您不能将本软件与其它非自由软件合并。
6. 如果您违反上述任何规则，将收到国际版权保护发和美国开源软件保护法以及GNU Open Source Federation最严厉的惩罚。
</p>

<p>关于二次开发，如果您需要文档，请给本repo发一条issue，您的要求将被立刻排入日程。</p>


<p><h4>---- GISDYT 2015-10-5</h4><p>