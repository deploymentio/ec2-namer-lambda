package com.deploymentio.ec2namer.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.deploymentio.ec2namer.LambdaContext;
import com.deploymentio.ec2namer.NamerRequest;

public class NameReserverTest {

	private NamerRequest goodRequest;
	private LambdaContext context;
	private NameReserver reserver;
	
	@Before
	public void setup() {
		
		goodRequest = new NamerRequest();
		goodRequest.setBaseDomain("base.com");
		goodRequest.setEnvironment("env");
		goodRequest.setInstanceId("i-some-id");
		goodRequest.setGroup("web");
		
		Context inner = mock(Context.class);
		LambdaLogger logger = mock(LambdaLogger.class);
		when(inner.getLogger()).thenReturn(logger);
		
		context = new LambdaContext(inner);

		reserver = new NameReserver();
		reserver.db = mock(NamerDB.class);
	}

	private TreeSet<IndexInUse> getMockIndexesInUseRange(int size) {
		int[] arr = new int[size];
		for(int i = 0; i < size; i++) {
			arr[i] = i+1;
		}
		return getMockIndexesInUse(arr);
	}
	
	private TreeSet<IndexInUse> getMockIndexesInUse(int...indexes) {
		TreeSet<IndexInUse> set = new TreeSet<>();
		for (int idx : indexes) {
			set.add(new IndexInUse("i-" + idx, idx));
		}
		return set;
	}

	@Test
	public void testValidateRequiredInfoMissing() {
		NamerRequest req = new NamerRequest();
		boolean validated = reserver.validate(req, context);
		assertFalse("Validation should fail", validated);
	}
	
	@Test
	public void testValidateAllOk() {
		boolean validated = reserver.validate(goodRequest, context);
		assertTrue("Validation should pass", validated);
	}
	
	@Test
	public void testReserveNameNoExistingReservations() throws IOException {
		assertReservedCorrectIndex(getMockIndexesInUse(), 1);
	}

	@Test
	public void testReserveNameWithExistingReservations() throws IOException {
		assertReservedCorrectIndex(getMockIndexesInUseRange(3), 4);
	}

	@Test
	public void testReserveNameWithGapInExistingReservations() throws IOException {
		assertReservedCorrectIndex(getMockIndexesInUse(1, 2, 3, 6), 4);
	}

	@Test
	public void testReserveNameWithInstanceIdInUse() throws IOException {
		goodRequest.setInstanceId("i-7");
		assertReservedCorrectIndex(getMockIndexesInUseRange(10), 7);
	}

	@Test
	public void testReserveNameWithInstanceIdInUseIgnoresGaps() throws IOException {
		goodRequest.setInstanceId("i-7");
		assertReservedCorrectIndex(getMockIndexesInUse(1, 4, 7, 8), 7);
	}
	
	private void assertReservedCorrectIndex(TreeSet<IndexInUse> inUse, int expectedIndexToReserve) throws IOException {
		when(reserver.db.getGroupIndxesInUse(any(NamerRequest.class))).thenReturn(inUse);
		reserver.validate(goodRequest, context);

		ReservedName name = reserver.reserve(goodRequest, context);
		assertNotNull(name);
		assertEquals(expectedIndexToReserve, name.getIndex());
		verify(reserver.db).reserveGroupIndex(goodRequest, expectedIndexToReserve);
	}

	@Test
	public void testReserveNameWithMaxExistingReservations() throws IOException {
		when(reserver.db.getGroupIndxesInUse(any(NamerRequest.class))).thenReturn(getMockIndexesInUseRange(999));
		reserver.validate(goodRequest, context);

		ReservedName name = reserver.reserve(goodRequest, context);
		assertNull(name);
		verify(reserver.db, never()).reserveGroupIndex(any(NamerRequest.class), Mockito.anyInt());
	}
}
