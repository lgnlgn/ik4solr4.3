package org.wltea.analyzer.lucene;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.Version;

public class IKSynonymFilterFactory extends TokenFilterFactory implements
		ResourceLoaderAware, TimelyThread.UpdateJob {

	public IKSynonymFilterFactory(Map<String, String> args) throws IOException {
		super(args);
		
		expand = getBoolean(args, "expand", true);
		synonyms = get(args, "synonyms");
		ignoreCase = getBoolean(args, "ignoreCase", false);
		isAutoUpdate = getBoolean(args, "autoupdate", false);
	}

	private String synonyms;
	private SynonymMap map;
	private boolean ignoreCase;
	private boolean expand;
	private ResourceLoader loader = null;
	
	boolean isAutoUpdate;
	
	Analyzer analyzer = null;		//包访问权限，共用？

	public void inform(ResourceLoader loader) throws IOException {

//		expand = getBoolean( "expand", true);
//		synonyms = args.get( "synonyms");
//		ignoreCase = getBoolean( "ignoreCase", false);
//		isAutoUpdate = getBoolean( "autoupdate", false);
		
		final 

		//IKAnalyzer analyzer = new IKAnalyzer(); // max words
		Analyzer analyzer = new Analyzer() {
		      @Override
		      protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		        WhitespaceTokenizer tokenizer =  new WhitespaceTokenizer(Version.LUCENE_43, reader);
		        TokenStream stream = ignoreCase ? new LowerCaseFilter(Version.LUCENE_43, tokenizer) : tokenizer;
		        return new TokenStreamComponents(tokenizer, stream);
		      }
		    };

		System.out.println("<IKSynonymFilterFactory>inform---loadSolrSynonyms!");
		//analyzer = null;
		
		try {
			map = loadSolrSynonyms(loader, true, analyzer);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IOException("Exception thrown while loading synonyms", e);
		}
		
		if(isAutoUpdate && synonyms != null && !synonyms.trim().isEmpty())
		{
			this.loader = loader;
			this.analyzer = analyzer;
			TimelyThread.getInstance().register(this);
		}
	}

	private SynonymMap loadSolrSynonyms(ResourceLoader loader, boolean dedup,
			Analyzer analyzer) throws IOException, ParseException {

		if (synonyms == null)
			throw new IllegalArgumentException(
					"Missing required argument 'synonyms'.");

		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);

		SolrSynonymParser parser = new SolrSynonymParser(dedup, expand,
				analyzer);
		File synonymFile = new File(synonyms);
		if (loader != null){ //first call in constructor
			if (synonymFile.exists()) {
				decoder.reset();
				parser.add(new InputStreamReader(loader.openResource(synonyms),
						decoder));
			} else {
				List<String> files = splitFileNames(synonyms);
				for (String file : files) {
					decoder.reset();
					parser.add(new InputStreamReader(loader.openResource(file),
							decoder));
				}
			}
		}
		return parser.build();
	}

	@Override
	public TokenStream create(TokenStream input) {
		return map.fst == null ? input : new SynonymFilter(input, map,ignoreCase);
	}

	public void update() {
		System.out.println("<IKSynonymFilterFactory>updateSolrSynonyms!");
	/*	Analyzer analyzer = new Analyzer() {
		      @Override
		      protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		        WhitespaceTokenizer tokenizer =  new WhitespaceTokenizer(Version.LUCENE_40, reader);
		        TokenStream stream = ignoreCase ? new LowerCaseFilter(Version.LUCENE_40, tokenizer) : tokenizer;
		        return new TokenStreamComponents(tokenizer, stream);
		      }
		    };*/
		//IKAnalyzer analyzer = new IKAnalyzer(); // max words
		try {
			map = loadSolrSynonyms(loader, true, analyzer); // 内部已实现切换
		} catch (IOException e) {
			System.out.println("<IKSynonymFilterFactory> IOException!!");
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("<IKSynonymFilterFactory> ParseException!!");
			e.printStackTrace();
		}
	}

}
