ik4solr4.3(不再维护，未来可能删除：请去https://github.com/mlcsdev/mlcsseg)
==========

solr4.3的ik分词器（改了一些smart分词算法。使用maven编译），隔壁有我写的[ansj的solr接口](https://github.com/lgnlgn/ansj4solr)包



- 支持从solr自己的环境中获取自定义词典（使用solr的ResourceLoader, 只需要把字典文件放到对应索引的conf目录里）
- 增加一个定时更新类
- 在IK中不建议用smart分词，因为它并不怎么准，ansj的分词效果明显更好。

**这是一次新的改变，如果你还用旧的，请注意更新了，由于曾经疏忽，旧版本并发下有问题**

**推荐可以结合另外一个动态[停用词分词器](https://github.com/lgnlgn/stop4solr4.x)使用**

----------

======以下是详细说明，不想了解太细的从第`五`点开始看，注意看完所有说明======

----------


	6.1新增功能说明
	①、由分级判断改为权重判断，增加了单字权重判断。 
		在org.wltea.analyzer.core包中LexemePath.java：compareTo(LexemePath)函数

	②、增加了单字字频字典，除去了停止字典(交给stopFilter处理)
	在org.wltea.analyzer.dic包中Dictionary.java：
	 /* 单字带词频词典 */
	private DictCharNode _CharFreqDict;
	
	在DictCharNode.java中：
	用HashMap实现了单字和字频的存储。


	
	二、IKTokenizerFactory.java
	IKTokenizer.java：用于生成IK分词器实例对象。
		注意事项：IK共用一个字典文件，主字典和扩展字典都加载在_MainDict中。
		使用方法：在schema.xml中，增加dicpath配置项，每次生成对象时，都会加载。
				  字典重建已封装在Dictionary类中，addDic2MainDic()函数


	四、UpdateKeeper.java：
		定时扫描配置文件，并执行更新
	
	五、schema.xml示例( md格式用得不熟，格式可能需要调整，比如小于号和文字原来是连着的)




    <fieldType name="text_cn" class="solr.TextField" positionIncrementGap="100" >  
      <analyzer type="index" >   
      <tokenizer class="org.wltea.analyzer.lucene.IKTokenizerFactory" useSmart="false" conf="ik.conf"/>
       <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" />
       <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>

      <analyzer type="query">
       <tokenizer class="org.wltea.analyzer.lucene.IKTokenizerFactory" useSmart="false" conf="ik.conf"/>
      </analyzer>
     </fieldType>

useSmart表示是否使用智能分词，建议false

conf执行配置文件路径，是个Properties格式的文本：

     lastupdate=123
     files=extDic.txt,aaa.txt

  其中lastupdate 是一个数字，只要这次比上一次大就会触发更新操作，可以用时间戳
files是用户词库，以**英文逗号**隔开




## 特别说明： ##
1. IK分词的词库是单例，因此在整个collection中，所有地方都共享一个词库。
那么单例会不会跨collection，根据我的实验，如果schema的名字不一样就可以避免，如果schema名字一样，还是会使用单例。
2. 定时器也是单例，但根据一些GC工具查看，定时器线程貌似不会在collection卸载以后被回收，多个collection使用定时器对象数量也没增加。这里我并不是很确定，如果你使用了IK分词器，并在你的SOLR里进行了多次索引创建卸载的操作，建议做一下重启
