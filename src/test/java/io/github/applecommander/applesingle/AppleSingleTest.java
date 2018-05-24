package io.github.applecommander.applesingle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class AppleSingleTest {
	private static final String AS_HELLO_BIN = "/hello.applesingle.bin";
	
	@Test
	public void testSampleFromCc65() throws IOException {
		AppleSingle as = AppleSingle.read(getClass().getResourceAsStream(AS_HELLO_BIN)); 
		
		assertNull(as.getRealName());
		assertNull(as.getResourceFork());
		assertNotNull(as.getDataFork());
		assertNotNull(as.getProdosFileInfo());
		
		ProdosFileInfo info = as.getProdosFileInfo();
		assertEquals(0xc3, info.getAccess());
		assertEquals(0x06, info.getFileType());
		assertEquals(0x0803, info.getAuxType());
	}

	@Test
	public void testCreateAndReadAppleSingle() throws IOException {
		final byte[] dataFork = "testing testing 1-2-3".getBytes();
		final String realName = "test.as";
		
		// Using default ProDOS info and skipping resource fork
		AppleSingle createdAS = AppleSingle.builder()
				.dataFork(dataFork)
				.realName(realName)
				.build();
		assertNotNull(createdAS);
		assertEquals(realName, createdAS.getRealName());
		assertArrayEquals(dataFork, createdAS.getDataFork());
		assertNull(createdAS.getResourceFork());
		assertNotNull(createdAS.getProdosFileInfo());
		
		ByteArrayOutputStream actualBytes = new ByteArrayOutputStream();
		createdAS.save(actualBytes);
		assertNotNull(actualBytes);
		
		AppleSingle readAS = AppleSingle.read(actualBytes.toByteArray());
		assertNotNull(readAS);
		assertEquals(realName, readAS.getRealName());
		assertArrayEquals(dataFork, readAS.getDataFork());
		assertNull(readAS.getResourceFork());
		assertNotNull(readAS.getProdosFileInfo());
	}
}
