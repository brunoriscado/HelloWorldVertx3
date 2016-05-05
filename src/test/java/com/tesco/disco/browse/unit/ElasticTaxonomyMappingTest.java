package com.tesco.disco.browse.unit;

import com.tesco.disco.browse.model.enumerations.ResponseTypesEnum;
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

				private static final String EXPECTED_PAYLOAD_RESULT = "{\"superDepartments\":[{\"name\":\"Health & Beauty\",\"departments\":" +
												"[{\"name\":\"Haircare\",\"aisles\":[{\"name\":\"Shampoo\",\"shelves\":" +
												"[{\"name\":\"Anti Dandruff Shampoo\"},{\"name\":\"Kids Shampoo\"}," +
												"{\"name\":\"Professional Shampoo\"}]},{\"name\":\"Colour Haircare\",\"shelves\":" +
												"[{\"name\":\"Blonde Shampoo & Conditioner\"}]},{\"name\":\"Conditioner\",\"shelves\":" +
												"[{\"name\":\"Colour Conditioner\"}]},{\"name\":\"Professional Haircare\",\"shelves\":" +
												"[{\"name\":\"Professional Styling\"}]}]},{\"name\":\"Shower, Bath & Soap\",\"aisles\":" +
												"[{\"name\":\"Gift Sets\",\"shelves\":[{\"name\":\"Womens Gift Sets\"}]}," +
												"{\"name\":\"Shower Gel\",\"shelves\":[{\"name\":\"Tesco Shower Gel\"}]}," +
												"{\"name\":\"Toiletries for Travel\",\"shelves\":[{\"name\":\"Travel Sizes\"}]}]}]}]}";

				@Test
				public void testMappingFromElasticResponseRX() {
								BrowseServiceImpl browseServiceImpl = Mockito.spy(BrowseServiceImpl.class);
								JsonObject result = browseServiceImpl.getBrowsingTaxonomy(new JsonObject(ELASTIC_PAYLOAD_MOCK), ResponseTypesEnum.TAXONOMY).toBlocking().single();
								Assert.assertEquals(EXPECTED_PAYLOAD_RESULT, result.encode());
				}
}
