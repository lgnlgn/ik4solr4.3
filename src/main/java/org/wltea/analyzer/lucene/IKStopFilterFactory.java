package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class IKStopFilterFactory extends TokenFilterFactory implements
		ResourceLoaderAware, TimelyThread.UpdateJob {

	public IKStopFilterFactory(Map<String, String> args) {
		super(args);
		// TODO Auto-generated constructor stub
		wordsPath = get(args, "words");
		ignoreCase = getBoolean(args, "ignoreCase", false);
		enablePositionIncrements = getBoolean(args, "enablePositionIncrements", false);
		isAutoUpdate = getBoolean(args, "autoupdate", false);
	}

	private boolean ignoreCase;
	private boolean enablePositionIncrements;
	private CharArraySet stopWords;
	private String wordsPath;
	private ResourceLoader stopWordsLoader;

	private boolean isAutoUpdate;
	
	public void inform(ResourceLoader loader) throws IOException {
		
//		wordsPath = args.get( "words");
//		ignoreCase = getBoolean( "ignoreCase", false);
//		enablePositionIncrements = getBoolean("enablePositionIncrements", false);
//		isAutoUpdate = getBoolean( "autoupdate", false);
		
		
		if(isAutoUpdate){
			stopWordsLoader = loader;
			TimelyThread.getInstance().register(this);
		}
		if (wordsPath != null && !wordsPath.trim().isEmpty()) {
			System.out.println("<IKStopFilterFactory>addStopWord!! ");
			stopWords = getWordSet(loader, wordsPath, ignoreCase);
		}
	}

	@Override
	public TokenStream create(TokenStream arg0) {

		IKStopFilter stopFilter = new IKStopFilter(true, arg0, stopWords);
		
		stopFilter.setEnablePositionIncrements(enablePositionIncrements);
		return stopFilter;
	}

	public void update() throws IOException {
		if (wordsPath != null && !wordsPath.trim().isEmpty()) {
			System.out.println("<IKStopFilterFactory>addStopWord!! ");
			stopWords = getWordSet(stopWordsLoader, wordsPath, ignoreCase);
		}

	}

}
