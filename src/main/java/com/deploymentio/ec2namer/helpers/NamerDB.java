package com.deploymentio.ec2namer.helpers;

import java.util.SortedSet;

import com.deploymentio.ec2namer.NamerRequest;

public class NamerDB {

	/**
	 * Gets a set of all reserved indexes for this group. The set is sorted.
	 * 
	 * @param req
	 *            the namer request
	 * @return a sorted of reserved indexes for this group
	 */
	public SortedSet<Integer> getGroupIndxesInUse(NamerRequest req) {
		// TODO: implement this later
		return null;
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
	public ReservedName reserveGroupIndex(NamerRequest req, int indexToReserve) {
		// TODO: implement this later
		return null;
	}
}
