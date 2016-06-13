package com.deploymentio.ec2namer.helpers;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.UpdateCondition;
import com.amazonaws.util.DateUtils;
import com.deploymentio.ec2namer.NamerRequest;

public class NamerDB {

	protected AmazonSimpleDB sdb = new AmazonSimpleDBClient();
	protected String dbDomain = "namer";
	
	/**
	 * Gets a set of all reserved indexes for this group. The set is sorted.
	 * 
	 * @param req
	 *            the namer request
	 * @return a sorted of reserved indexes for this group
	 */
	public SortedSet<IndexInUse> getGroupIndxesInUse(NamerRequest req) {
		
		TreeSet<IndexInUse> set = new TreeSet<>();
		
		String query = "select * from `" + dbDomain + "` where 	`domain`='" + req.getEnvironment() + "." + req.getBaseDomain() + "' and `group`='" + req.getGroup() + "'";
		String token = null;
		
		do {
			SelectResult result = sdb.select(new SelectRequest(query, true).withNextToken(token));
			for (Item item : result.getItems()) {
				HashMap<String, String> attributes = new HashMap<>(); 
				for (Attribute a : item.getAttributes()) {
					attributes.put (a.getName(), a.getValue());
				}
				set.add(new IndexInUse(attributes.get("inst"), Integer.valueOf(attributes.get("idx"))));
			}
			
			token = result.getNextToken();
		} while(token != null);
		
		return set;
	}
	
	/**
	 * Reserves the given index for this request
	 * 
	 * @param req
	 *            the namer request
	 * @param indexToReserve
	 *            index to reserve
	 * @return the reserved name if successful or <code>null</code> otherwise
	 */
	public ReservedName reserveGroupIndex(NamerRequest req, int indexToReserve) throws IOException {
		
		ReservedName name = new ReservedName(req.getGroup(), indexToReserve);
		String domain = req.getEnvironment() + "." + req.getBaseDomain();
		String fqdn = name + "." + domain;
		
		try {
			
			sdb.putAttributes(new PutAttributesRequest()
				.withExpected(new UpdateCondition("fqdn", null, false))
				.withDomainName(dbDomain)
				.withItemName(fqdn)
				.withAttributes(
					new ReplaceableAttribute("fqdn", fqdn, true),
					new ReplaceableAttribute("domain", domain, true),
					new ReplaceableAttribute("group", req.getGroup(), true),
					new ReplaceableAttribute("idx", String.valueOf(name.getIndex()), true),
					new ReplaceableAttribute("inst", req.getInstanceId(), true),
					new ReplaceableAttribute("created", DateUtils.formatISO8601Date(new Date()), true)));
			
			return name;
			
		} catch (AmazonServiceException e) {
			if (!"ConditionalCheckFailed".equals(e.getErrorCode())) {
				throw e;
			}
		}
		
		return null;
	}
}
