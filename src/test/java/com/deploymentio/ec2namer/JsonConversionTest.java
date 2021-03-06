/*
 * Copyright 2016 - Deployment IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deploymentio.ec2namer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class JsonConversionTest {

	private SampleFunction fn;
	private Context context;

	@Before
	public void setup() {

		fn = new SampleFunction();
		context = mock(Context.class);
		LambdaLogger logger = mock(LambdaLogger.class);
		when(context.getLogger()).thenReturn(logger);
	}

	static class SampleFunction extends JsonLambdaFunction<SampleInput, SampleOutput, String> {
		@Override
		public SampleOutput process(SampleInput req, LambdaContext context) throws IOException {
			return new SampleOutput().withValue("bar");
		}

		@Override
		public boolean validate(SampleInput req, LambdaContext context) {
			return "foo".equals(req.getName());
		}
		
		@Override
		public String error(LambdaContext context, String error) {
			return "error";
		}
	}

	@Test
	public void testInputOutputAllGood() throws Exception {
		InputStream inStream = new ByteArrayInputStream("{\"name\":\"foo\"}".getBytes(StandardCharsets.UTF_8));
		OutputStream outStream = new ByteArrayOutputStream();

		fn.handleRequest(inStream, outStream, context);
		assertEquals("{\"value\":\"bar\"}", outStream.toString());
	}
	
	@Test
	public void testInputValidationFails() throws Exception {
		InputStream inStream = new ByteArrayInputStream("{\"name\":\"not-foo\"}".getBytes(StandardCharsets.UTF_8));
		OutputStream outStream = new ByteArrayOutputStream();

		fn.handleRequest(inStream, outStream, context);
		assertEquals("\"error\"", outStream.toString());
	}
	
	@Test
	public void testInputJsonConversionFails() throws Exception {
		InputStream inStream = new ByteArrayInputStream("{\"notName\":\"foo\"}".getBytes(StandardCharsets.UTF_8));
		OutputStream outStream = new ByteArrayOutputStream();

		fn.handleRequest(inStream, outStream, context);
		assertEquals("\"error\"", outStream.toString());
	}
}
