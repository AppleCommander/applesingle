package io.github.applecommander.applesingle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
		// Need to truncate to seconds as the AppleSingle format is only good to seconds!
		final Instant instant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		
		// Using default ProDOS info and skipping resource fork
		AppleSingle createdAS = AppleSingle.builder()
				.dataFork(dataFork)
				.realName(realName)
				.allDates(instant)
				.build();
		assertNotNull(createdAS);
		assertEquals(realName.toUpperCase(), createdAS.getRealName());
		assertArrayEquals(dataFork, createdAS.getDataFork());
		assertNull(createdAS.getResourceFork());
		assertNotNull(createdAS.getProdosFileInfo());
		
		ByteArrayOutputStream actualBytes = new ByteArrayOutputStream();
		createdAS.save(actualBytes);
		assertNotNull(actualBytes);
		
		AppleSingle readAS = AppleSingle.read(actualBytes.toByteArray());
		assertNotNull(readAS);
		assertEquals(realName.toUpperCase(), readAS.getRealName());
		assertArrayEquals(dataFork, readAS.getDataFork());
		assertNull(readAS.getResourceFork());
		assertNotNull(readAS.getProdosFileInfo());
		assertNotNull(readAS.getFileDatesInfo());
		assertEquals(instant, readAS.getFileDatesInfo().getCreationInstant());
		assertEquals(instant, readAS.getFileDatesInfo().getModificationInstant());
		assertEquals(instant, readAS.getFileDatesInfo().getAccessInstant());
		assertEquals(instant, readAS.getFileDatesInfo().getBackupInstant());
	}
	
	@Test
	public void testProdosFileNameLengthRequirements() {
		AppleSingle as = AppleSingle.builder().realName("superlongnamethatneedstobetruncated").build();
		assertEquals(15, as.getRealName().length());
	}
	
	@Test
	public void testProdosFileNameCharacterRequirements() {
		AppleSingle as = AppleSingle.builder().realName("bad-~@").build();
		assertEquals("BAD...", as.getRealName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testProdosFileNameFirstCharacter() {
		AppleSingle.builder().realName("1st-file").build();
	}
}
