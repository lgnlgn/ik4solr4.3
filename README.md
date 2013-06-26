ik4solr4.3
==========

solr4.3的ik分词器（主要改动不是我完成的，只是指点。建议使用maven）


- 支持从solr自己的环境中获取自定义词典（使用solr的ResourceLoader, 只需要把字典文件放到conf目录里）

- 增加一个定时更新的停用词、同义词工厂类


----------

============我是分割线====以下是详细说明================

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

	③、实现solr的接口
	在org.wltea.analyzer.lucene包中。
	一、IKHandler.java：用于处理HTTP请求，现只实现主词典的动态请求加载。
		请求参数：http://xxx/ikupdate?dicpath=dic.txt
		注意事项：1、多个字典文件以逗号分隔。
				  2、在Zookeeper集群上时，得先上传修改后的字典文件，再更新。
		使用方法：需在solrconfig.xml中配置
	<requestHandler name="/ikupdate" class="org.wltea.analyzer.lucene.IKHandler">
     	<lst name="defaults">
     	</lst> 
  	</requestHandler>
	可以配置默认参数
	<str name=”dicpath”>dic.txt</str>
	
	二、IKTokenizerFactory.java
	IKTokenizer.java：用于生成IK分词器实例对象。
		注意事项：IK共用一个字典文件，主字典和扩展字典都加载在_MainDict中。
		使用方法：在schema.xml中，增加dicpath配置项，每次生成对象时，都会加载。
				  字典重建已封装在Dictionary类中，addDic2MainDic()函数

	三、IKStopFilter.java
	IKStopFilterFactory.java：停止词过滤
	IKSynonymFilterFactory.java：同义词过滤
		注意事项：1、字典的切换采用solr默认接口实现
				  2、2个过滤器在schema.xml均增加了autoupdate配置项，为true，则						会定时去更新字典文件。
		使用方法：在schema.xml中，添加自己的过滤器类，增加配置项。

	四、UpdateThread.java：
	停止词和同义词更新管理类，如果设置了autoupdate=true，则注册到此管理类中，由管理类定时去触发更新操作。
	
	五、schema.xml示例



      < fieldType name="text_cn" class="solr.TextField" positionIncrementGap="100" >        
   
      <analyzer type="index" >       
        < tokenizer class="org.wltea.analyzer.lucene.IKTokenizerFactory" useSmart="true" />
        < filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" />
       < filter class="solr.LowerCaseFilterFactory"/>
      < /analyzer>

      < analyzer type="query">
        <tokenizer class="org.wltea.analyzer.lucene.IKTokenizerFactory" useSmart="true" dicPath="extDic.txt,extDic1.txt"/>
        <filter class="org.wltea.analyzer.lucene.IKStopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" autoupdate="true"/>
		<filter class="org.wltea.analyzer.lucene.IKSynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true" autoupdate="true"/>
        <filter class="solr.LowerCaseFilterFactory"/>
      </analyzer>
      </fieldType>