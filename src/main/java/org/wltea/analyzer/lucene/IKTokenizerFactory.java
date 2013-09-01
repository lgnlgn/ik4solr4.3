package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.wltea.analyzer.dic.Dictionary;

public class IKTokenizerFactory extends TokenizerFactory implements
		ResourceLoaderAware , UpdateKeeper.UpdateJob{

	public IKTokenizerFactory(Map<String, String> args) {
		super(args);
		assureMatchVersion();
		useSmart = getBoolean(args, "useSmart", false);
		conf = get(args, "conf");
		System.out.println(":::ik:construction::::::::::::::::::::::::::" + conf);
	}
	private boolean useSmart = false;
	private ResourceLoader loader; 
	
	private long lastUpdateTime = -1;
	private String conf = null;
	
	
	private boolean useSmart() {
		return useSmart;
	}

	
	// 通过这个实现，调用自身分词器
	public Tokenizer create(AttributeFactory attributeFactory, Reader in) { // 会多次被调用
		return new IKTokenizer(in, this.useSmart()); // 初始化词典，分词器，消歧器
	}

	public void inform(ResourceLoader loader) throws IOException { // 在启动时初始化一次
		System.out.println(":::ik:::inform::::::::::::::::::::::::" + conf);
		this.loader = loader;
		this.update();
		if(conf != null && !conf.trim().isEmpty())
		{
			UpdateKeeper.getInstance().register(this);
		}

	}
	
	public static List<String> SplitFileNames(String fileNames) {
		if (fileNames == null)
			return Collections.<String> emptyList();

		List<String> result = new ArrayList<String>();
		for (String file : fileNames.split("[,\\s]+")) {
			result.add(file);
		}

		return result;
	}


	public void update() throws IOException {
		
		Properties p = canUpdate();
		if (p != null){
			List<String> dicPaths = SplitFileNames(p.getProperty("files"));
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
	
	private Properties canUpdate() {

		try{
			if (conf == null)
				return null;
			Properties p = new Properties();
			InputStream confStream = loader.openResource(conf);
			p.load(confStream);
			confStream.close();
			String lastupdate = p.getProperty("lastupdate", "0");
			Long t = new Long(lastupdate);
			
			if (t > this.lastUpdateTime){
				this.lastUpdateTime = t.longValue();
				String paths = p.getProperty("files");
				if (paths==null || paths.trim().isEmpty()) // 必须有地址
					return null;
				System.out.println("loading conf");
				return p;
			}else{
				this.lastUpdateTime = t.longValue();
				return null;
			}
		}catch(Exception e){
			System.err.println("IK parsing conf NullPointerException~~~~~" + e.getMessage());
			return null;
		}
	}
	
}
