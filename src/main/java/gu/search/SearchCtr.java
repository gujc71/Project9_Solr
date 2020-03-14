package gu.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.noggit.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import gu.common.FullTextSearchVO;
import gu.common.Util4calen;

@Controller 
public class SearchCtr {

    static final Logger logger = LoggerFactory.getLogger(SearchCtr.class);
    static final Integer DISPLAY_COUNT = 5;
    static final String INDEX_NAME = "project9";
    static final String[] HIGHLIGHT_FIELDS = { "brdwriter", "brdtitle", "brdmemo" };
    static final String[] INCLUDE_FIELDS = new String[] {"brdno", "userno", "brddate", "brdtime", "brdtitle", "brdwriter", "brdmemo"}; // 값을 가지고 올 필드

    @RequestMapping(value = "/search")
    public String search(HttpServletRequest request, ModelMap modelMap) {
        String today = Util4calen.date2Str(Util4calen.getToday());
        
        modelMap.addAttribute("today", today);

        return "search/search";
    }

    /*
     * 검색
     * @param searchVO: 검색 조건.
     * @ajx return SearchResponse: ES가  전송한 검색 결과. Java에서는 받은 값을 그대로 client(ajax)에 전송. 대부분의 처리를 JS에서 진행
     */    
    @RequestMapping(value = "/search4Ajax")
    public void search4Ajax(HttpServletRequest request, HttpServletResponse response, FullTextSearchVO searchVO) {
        String searchKeyword = searchVO.getSearchKeyword();
    	if (searchKeyword==null || "".equals(searchKeyword)) return;
    	
    	String[] fields = searchVO.getSearchRange().split(",");	 			// 검색 대상 필드 - 작성자, 제목, 내용 등
    	String[] words  = searchKeyword.split(" ");
    	String queryStr = "";
    	
    	for (int i=0; i<words.length; i++) {
    		queryStr += makeQuery(words[i], fields);
    	}
    	
        String searchType = searchVO.getSearchType();
    	if (searchType!=null & !"".equals(searchType)) {
    		queryStr = "(" + queryStr + ") AND bgno:" + searchType;  
    	}
    	
    	if (!"a".equals(searchVO.getSearchTerm())) { // 기간 검색		
    		queryStr = "(" + queryStr + ") AND brddate:[" + searchVO.getSearchTerm1() + " TO " + searchVO.getSearchTerm2() + "]";  
    	}
    	
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(queryStr);
        solrQuery.setFields("");
        solrQuery.addSort("id", ORDER.desc);								// 정렬
        solrQuery.setStart( (searchVO.getPage()-1) * DISPLAY_COUNT);		// 페이징
		solrQuery.setRows(DISPLAY_COUNT); 
        solrQuery.setFacet(true);											// 합계
        solrQuery.addFacetField("bgno");
        solrQuery.setParam("hl.fl", "brdwriter, brdtitle, brdmemo");		// 하이라이팅
        solrQuery.setHighlight(true).setHighlightSnippets(1);
        logger.info(solrQuery.toString());
        
        // 실제 조회
        QueryResponse queryResponse = null;
        SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/project9").build();
        try {
            queryResponse = solrClient.query(solrQuery);
            solrClient.close(); 

            logger.info(queryResponse.toString());
        } catch (SolrServerException | IOException e) {
            logger.error("solrQuery error");
        }
        
        // 반환 처리
    	HashMap<String, Long> facetMap = new HashMap<String, Long>();
        List<FacetField> ffList = queryResponse.getFacetFields();
        for(FacetField ff : ffList){
            List<Count> counts = ff.getValues();
            for(Count c : counts){
                facetMap.put(c.getName(), c.getCount());
            }
        }
        
        //Map<String, Map<String, List<String>>> highlights = rsp1.getHighlighting();
    	HashMap<String, Object> resultMap = new HashMap<String, Object>();
    	resultMap.put("total", queryResponse.getResults().getNumFound());
    	resultMap.put("docs", JSONUtil.toJSON(queryResponse.getResults()).toString()) ;
    	resultMap.put("facet", facetMap) ;
    	resultMap.put("highlighting", queryResponse.getHighlighting()) ;
    	
        ObjectMapper mapper = new ObjectMapper();
		response.setContentType("application/json;charset=UTF-8");
		try {
			response.getWriter().print(mapper.writeValueAsString(resultMap));
		} catch (IOException e) {
			logger.error("response error"+e);
		}
    }
    /*
     * 검색식 작성
     * @param keyword: 검색 키워드.
     * @param keyword: 검색 대상 필드 - 작성자, 제목, 내용 등.
     * @return 검색식
     */   
    private String makeQuery(String keyword, String[] fields) {
    	String queryStr = "";
    	
        for (int i=0; i<fields.length; i++) {
            if (queryStr.length()>0) queryStr += " OR ";
        	if ("brdfiles".equals(fields[i]))
                queryStr += " {!parent which=\"brdtype:1\"}filememo:" + keyword;
        	else 
        	if ("brdreply".equals(fields[i]))
                 queryStr += " {!parent which=\"brdtype:1\"}rememo:" + keyword;
        	else queryStr += " " + fields[i] + ":" + keyword;
        }
        
        return queryStr;
    }
    // ---------------------------------------------------------------------------
}
