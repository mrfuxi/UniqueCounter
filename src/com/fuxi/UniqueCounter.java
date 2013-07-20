package com.fuxi;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.search.SyntaxError;

public class UniqueCounter extends SearchComponent {
    volatile long numRequests;
    volatile long numErrors;
    volatile long totalRequestsTime;
    volatile Long lastRequestTime;
    volatile Long minRequestTime;
    volatile Long maxRequestTime;
    volatile String maxRequestTimeDT;

    @Override
    @SuppressWarnings("rawtypes")
    public void init(NamedList args) {
        super.init(args);
    }
    
    private boolean hasValidParams(SolrParams params) throws IOException {
		if (!params.getBool(getName(), false)) {
			return false;
		}
		
		if (params.getParams(getName() + ".field") == null) {
			throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Specify at least one field to get unique count.");
		}
		
		return true;
    }
    
	@Override
	public void process(ResponseBuilder rb) throws IOException {
		long lstartTime = System.currentTimeMillis();
		SolrParams params = rb.req.getParams();
		
		if (!hasValidParams(params)) {
			return;
		}
		
		// Count only request that will be executed
		numRequests++;
		
		String[] field_names = params.getParams(getName() + ".field");
		Boolean missing = params.getBool(getName() + ".missing");
		String method = params.get(getName() + ".method");
		
		ModifiableSolrParams facet_params = new ModifiableSolrParams();
		for (String field : field_names) {
			facet_params.add(FacetParams.FACET_FIELD, field);
		}

		if (missing != null) {
			facet_params.set(FacetParams.FACET_MISSING, missing);
		}
		
		if (method != null) {
			facet_params.set(FacetParams.FACET_METHOD, method);
		}
		
		facet_params.set(FacetParams.FACET_LIMIT, -1);
		facet_params.set(FacetParams.FACET_MINCOUNT, 1);
		facet_params.set(FacetParams.FACET_OFFSET, 0);
		
        SimpleFacets f = new SimpleFacets(rb.req,
        								  rb.getResults().docSet,
        								  facet_params,
							              rb );
		
		NamedList<Integer> unique_counts = new SimpleOrderedMap<Integer>();
        
        try {
			NamedList<Object> f_counts = f.getFacetFieldCounts();
			
			Iterator<Entry<String, Object>> iterator = f_counts.iterator();
			
			while(iterator.hasNext()) {
				Entry<String, Object> field_data = iterator.next();
				String field_name = field_data.getKey();
				@SuppressWarnings("unchecked")
				NamedList<Object> field_facets = (NamedList<Object>) field_data.getValue();

				/*
				TODO: Handle missing with 0 results - do not count that
                */

				unique_counts.add(field_name, field_facets.size());
			}
		} catch (SyntaxError e) {
			numErrors++;
			e.printStackTrace();
			return;
		}
        
        rb.rsp.add(getName(), unique_counts);
        updateRequestStats(lstartTime);
	}

	private void updateRequestStats(long lstartTime) {
    	lastRequestTime = System.currentTimeMillis() - lstartTime;
        
        if (minRequestTime == null) {
        	minRequestTime = lastRequestTime;
        } else if (lastRequestTime.compareTo(minRequestTime) < 0) {
        	minRequestTime = lastRequestTime;
        }
        
        if (maxRequestTime == null) {
        	maxRequestTime = lastRequestTime;
        	maxRequestTimeDT = new Date().toString();
        } else if (lastRequestTime.compareTo(maxRequestTime) > 0) {
        	maxRequestTime = lastRequestTime;
        	maxRequestTimeDT = new Date().toString();
        }
        
        totalRequestsTime += lastRequestTime;
    }

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		if (hasValidParams(rb.req.getParams())) {
			rb.setNeedDocSet(true);
		}
	}

	@Override
	public String getDescription() {
		return "Unique value counter";
	}
    
	@Override
    public String getVersion() {
        return "0.1";
    }
    
	@Override
	public String getSource() {
		return "https://github.com/mrfuxi/UniqueCounter";
	}
	
    @Override
    public NamedList<Object> getStatistics() {
        NamedList<Object> stats = new SimpleOrderedMap<Object>();
        stats.add("requests", "" + numRequests);
        stats.add("errors", "" + numErrors);
        stats.add("totalRequestTime(ms)", "" + totalRequestsTime);    
        stats.add("last request(ms)", "" + lastRequestTime);
        stats.add("fastest request(ms)", "" + minRequestTime);
        stats.add("slowest request(ms)", "" + maxRequestTime + " (" + maxRequestTimeDT + ")");
        if (numRequests-numErrors != 0) {
	        stats.add("average time(ms)", "" + totalRequestsTime/(numRequests-numErrors));
        }
        
        return stats;
    }
}
