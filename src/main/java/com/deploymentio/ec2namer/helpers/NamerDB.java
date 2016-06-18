package com.deploymentio.ec2namer.helpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.UpdateCondition;
import com.amazonaws.util.DateUtils;
import com.deploymentio.ec2namer.DenameRequest;
import com.deploymentio.ec2namer.NamerRequest;
import com.deploymentio.ec2namer.RequestedName;

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
		
		String query = "select * from `" + dbDomain + "` where 	`env`='" + req.getEnvironment() + "' and `base-domain`='" + req.getBaseDomain() + "' and `group`='" + req.getGroup() + "'";
		String token = null;
		
		do {
			SelectResult result = sdb.select(new SelectRequest(query, true).withNextToken(token));
			for (Item item : result.getItems()) {
				Map<String, String> attributes = getItemAsMap(item);
				set.add(new IndexInUse(attributes.get("inst"), Integer.valueOf(attributes.get("idx"))));
			}
			
			token = result.getNextToken();
		} while(token != null);
		
		return set;
	}

	private Map<String, String> getItemAsMap(Item item) {
		HashMap<String, String> attributes = new HashMap<>(); 
		for (Attribute a : item.getAttributes()) {
			attributes.put (a.getName(), a.getValue());
		}
		return attributes;
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
		String fqdn = req.createFqdn(name.getHostname());
		
		try {
			
			sdb.putAttributes(new PutAttributesRequest()
				.withExpected(new UpdateCondition("fqdn", null, false))
				.withDomainName(dbDomain)
				.withItemName(fqdn)
				.withAttributes(
					new ReplaceableAttribute("fqdn", fqdn, true),
					new ReplaceableAttribute("base-domain", req.getBaseDomain(), true),
					new ReplaceableAttribute("env", req.getEnvironment(), true),
					new ReplaceableAttribute("group", req.getGroup(), true),
					new ReplaceableAttribute("idx", String.valueOf(name.getIndex()), true),
					new ReplaceableAttribute("inst-id", req.getInstanceId(), true),
					new ReplaceableAttribute("use-public-name", String.valueOf(req.isAlwaysUsePublicName()), true),
					new ReplaceableAttribute("names", getRequestedNamesAsString(req.getRequestedNames()), true),
					new ReplaceableAttribute("created", DateUtils.formatISO8601Date(new Date()), true)));
			
			return name;
			
		} catch (AmazonServiceException e) {
			if (!"ConditionalCheckFailed".equals(e.getErrorCode())) {
				throw e;
			}
		}
		
		return null;
	}
	
	public DenameRequest findReservedNameForDenaming(String instanceId) {
		
		SelectResult result = sdb.select(new SelectRequest("select * from `" + dbDomain + "` where `inst-id`='" + instanceId + "' limit 1"));
		if (!result.getItems().isEmpty()) {
			Map<String, String> attributes = getItemAsMap(result.getItems().get(0));
			DenameRequest req = new DenameRequest();
			req.setAlwaysUsePublicName("true".equals(attributes.get("use-public-name")));
			req.setBaseDomain(attributes.get("base-domain"));
			req.setEnvironment(attributes.get("env"));
			req.setInstanceId(instanceId);
			req.setReservedName(new ReservedName(attributes.get("group"), Integer.valueOf(attributes.get("idx"))));
			req.setRequestedNames(getRequestedNamesFromString(attributes.get("names")));
			return req;
		}
		
		return null;
	}
	
	public void unreserve(DenameRequest req) {
		String fqdn = req.createFqdn(req.getReservedName().getHostname());
		UpdateCondition expected = new UpdateCondition("inst-id", req.getInstanceId(), true);
		sdb.deleteAttributes(new DeleteAttributesRequest(dbDomain, fqdn).withExpected(expected));
	}
	
	protected List<String> getRequestedNamesFromString(String namesAsString) {
		if (":empty:".equals(namesAsString)) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(StringUtils.split(namesAsString, ','));
		}
	}
	
	protected String getRequestedNamesAsString(List<RequestedName> names) {
		if (names.isEmpty()) {
			return ":empty:";
		} else {
			StringBuilder b = new StringBuilder();
			for(RequestedName name : names) {
				if (b.length() > 0) {
					b.append(',');
				}
				b.append(name.getName());
			}
			return b.toString();
		}
	}
}
