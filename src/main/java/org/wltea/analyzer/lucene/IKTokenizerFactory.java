package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.wltea.analyzer.dic.Dictionary;

public class IKTokenizerFactory extends TokenizerFactory implements
		ResourceLoaderAware {

	public IKTokenizerFactory(Map<String, String> args) {
		super(args);
		assureMatchVersion();
		useSmart = getBoolean(args, "useSmart", false);
		dicPath = get(args, "dicPath");
	}
	
	
	

	private boolean useSmart = false;
	private Tokenizer _IKTokenizer = null;
	String dicPath = null;
	
	
	public boolean useSmart() {
		return useSmart;
	}

	
	// 通过这个实现，调用自身分词器
	public Tokenizer create(AttributeFactory attributeFactory, Reader in) { // 会多次被调用
		if(_IKTokenizer == null)
		{
			_IKTokenizer = new IKTokenizer(in, this.useSmart()); // 初始化词典，分词器，消歧器
		}
		else
		{
			try {
				_IKTokenizer.setReader(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return _IKTokenizer;
	}

//	public Tokenizer create(Reader in) { // 会多次被调用
//	if(_IKTokenizer == null)
//	{
//		_IKTokenizer = new IKTokenizer(in, this.useSmart()); // 初始化词典，分词器，消歧器
//	}
//	else
//	{
//		try {
//			_IKTokenizer.setReader(in);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	return _IKTokenizer;
//}
	
	/**
	 * just need once define "dicpath" in schema.xml,so defined in index or query
	 * 
	 */
	public void inform(ResourceLoader loader) throws IOException { // 在启动时初始化一次
			/* first: load main dic */
//		useSmart = getBoolean("useSmart", false);
//		dicPath = args.get("dicPath");
		
		if (dicPath != null && !dicPath.trim().isEmpty()) {
			System.out.println("get dicPath: " + dicPath);

			System.out.println("<IKTokenizerFactory>begin split dicPath: ");
			List<String> dicPaths = Util.SplitFileNames(dicPath);
			System.out.println(dicPaths);

			List<InputStream> inputStreamList = new ArrayList<InputStream>();
			for (String path : dicPaths) {
				if ((path != null && !path.isEmpty())) {
					InputStream is = loader.openResource(path);

					if (is != null) {
						inputStreamList.add(is);
					}
				}
			}

			if (!inputStreamList.isEmpty()) {
				Dictionary.addDic2MainDic(inputStreamList); // load dic to MainDic
			}
		}
		
	}
}
