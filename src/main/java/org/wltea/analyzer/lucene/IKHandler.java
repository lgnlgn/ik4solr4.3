package org.wltea.analyzer.lucene;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.wltea.analyzer.dic.Dictionary;

/**
 * IK 的 solr handler，用于更新词库等。
 * 
 * @author xiong.xu 2013-04-12 上午10:53:38
 */
public class IKHandler extends RequestHandlerBase implements SolrCoreAware {

	// private File solrHome = null;
	private SolrResourceLoader loader = null;

	@Override
	public String getDescription() {

		return "";
	}

	@Override
	public String getSource() {

		return "$URL: http:// $";
	}

	public String getSourceId() {

		return "$Revision: 1$";
	}

	@Override
	public String getVersion() {

		return "4.1";
	}

	@Override
	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
			throws Exception {

		System.out.println("准备开始处理请求！");
		rsp.setHttpCaching(false);
		final SolrParams solrParams = req.getParams();

		NamedList<Object> result = new NamedList<Object>();
		result.add("dicPath", new String("测试"));

		/* the request included "dicPath" parameter */
		String dicPath = solrParams.get("dicpath"); // params : dicpath

		if (dicPath != null && !dicPath.trim().isEmpty()) {
			System.out.println("<handlerRequest> begin split dicPath: ");
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
			if (inputStreamList.size() > 0) {
				System.out.println("begin reconsitution _MainDict");
				Dictionary.addDic2MainDic(inputStreamList); // 重构MainDic字典
			}
		}

		/*
		 * the http request included "stopWordsPath" parameter String
		 * stopWordsDicPath = solrParams.get("stopwords"); //params : stopwords
		 * if(stopWordsDicPath != null && !stopWordsDicPath.trim().isEmpty()) {
		 * IKStopFilterFactory.addStopWord(loader, stopWordsDicPath); }
		 * 
		 * //loader 根据实例目录来进行切换？ the http request included "synwords" parameter
		 * String synonymWordsDic = solrParams.get("synonyms"); //params :
		 * synwords if(synonymWordsDic != null &&
		 * !synonymWordsDic.trim().isEmpty()) { //Properties property =
		 * loader.getCoreProperties();
		 * IKSynonymFilterFactory.getInstance().updateSolrSynonyms(loader); }
		 */

		/* the result */
		rsp.add("result", result); // used to return content of the result html
	}

	public void inform(SolrCore core) {
		loader = core.getResourceLoader();
		// solrHome = new File(loader.getInstanceDir());
	}
}
