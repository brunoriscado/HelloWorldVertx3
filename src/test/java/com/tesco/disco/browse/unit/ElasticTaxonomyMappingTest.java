package com.tesco.disco.browse.unit;

import com.tesco.disco.browse.service.impl.BrowseServiceImpl;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
	* Created by bruno on 23/04/16.
	*/
public class ElasticTaxonomyMappingTest {
				private static final String ELASTIC_PAYLOAD_MOCK = "{\"took\":133,\"timed_out\":false,\"_shards\":" +
												"{\"total\":1,\"successful\":1,\"failed\":0},\"hits\":{\"total\":9,\"max_score\":0.0,\"hits\":[]}," +
												"\"aggregations\":{\"superDepartments\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Health & Beauty\",\"doc_count\":9,\"departments\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Haircare\",\"doc_count\":6,\"aisles\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Shampoo\",\"doc_count\":3,\"shelves\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Anti Dandruff Shampoo\",\"doc_count\":1},{\"key\":\"Kids Shampoo\",\"doc_count\":1},{\"key\":\"Professional Shampoo\",\"doc_count\":" +
												"1}]}},{\"key\":\"Colour Haircare\",\"doc_count\":1,\"shelves\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Blonde Shampoo & Conditioner\",\"doc_count\":1}]}},{\"key\":\"Conditioner\",\"doc_count\":1,\"shelves\":" +
												"{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":[{\"key\":\"Colour Conditioner\",\"doc_count\":1}]}}," +
												"{\"key\":\"Professional Haircare\",\"doc_count\":1,\"shelves\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Professional Styling\",\"doc_count\":1}]}}]}},{\"key\":\"Shower, Bath & Soap\",\"doc_count\":3,\"aisles\":" +
												"{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":[{\"key\":\"Gift Sets\",\"doc_count\":1,\"shelves\":" +
												"{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":[{\"key\":\"Womens Gift Sets\",\"doc_count\":1}]}}," +
												"{\"key\":\"Shower Gel\",\"doc_count\":1,\"shelves\":{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":" +
												"[{\"key\":\"Tesco Shower Gel\",\"doc_count\":1}]}},{\"key\":\"Toiletries for Travel\",\"doc_count\":1,\"shelves\":" +
												"{\"doc_count_error_upper_bound\":0,\"sum_other_doc_count\":0,\"buckets\":[{\"key\":\"Travel Sizes\",\"doc_count\":1}]}}]}}]}}]}}}";

				private static final String EXPECTED_PAYLOAD_RESULT = "{\"superDepartments\":[{\"name\":\"Health & Beauty\",\"total\":9,\"departments\":" +
												"[{\"name\":\"Haircare\",\"total\":6,\"aisles\":[{\"name\":\"Shampoo\",\"total\":3,\"shelves\":" +
												"[{\"name\":\"Anti Dandruff Shampoo\",\"total\":1},{\"name\":\"Kids Shampoo\",\"total\":1}," +
												"{\"name\":\"Professional Shampoo\",\"total\":1}]},{\"name\":\"Colour Haircare\",\"total\":1,\"shelves\":" +
												"[{\"name\":\"Blonde Shampoo & Conditioner\",\"total\":1}]},{\"name\":\"Conditioner\",\"total\":1,\"shelves\":" +
												"[{\"name\":\"Colour Conditioner\",\"total\":1}]},{\"name\":\"Professional Haircare\",\"total\":1,\"shelves\":" +
												"[{\"name\":\"Professional Styling\",\"total\":1}]}]},{\"name\":\"Shower, Bath & Soap\",\"total\":3,\"aisles\":" +
												"[{\"name\":\"Gift Sets\",\"total\":1,\"shelves\":[{\"name\":\"Womens Gift Sets\",\"total\":1}]}," +
												"{\"name\":\"Shower Gel\",\"total\":1,\"shelves\":[{\"name\":\"Tesco Shower Gel\",\"total\":1}]}," +
												"{\"name\":\"Toiletries for Travel\",\"total\":1,\"shelves\":[{\"name\":\"Travel Sizes\",\"total\":1}]}]}]}]}";

				@Test
				public void testMappingFromElasticResponseRX() {
								BrowseServiceImpl browseServiceImpl = Mockito.spy(BrowseServiceImpl.class);
								JsonObject result = browseServiceImpl.getBrowsingTaxonomyRx(new JsonObject(ELASTIC_PAYLOAD_MOCK)).toBlocking().single();
								Assert.assertEquals(EXPECTED_PAYLOAD_RESULT, result.encode());
				}
}
