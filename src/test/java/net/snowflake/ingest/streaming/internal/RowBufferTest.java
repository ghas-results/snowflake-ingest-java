package net.snowflake.ingest.streaming.internal;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.snowflake.ingest.utils.ErrorCode;
import net.snowflake.ingest.utils.SFException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowBufferTest {
  private ArrowRowBuffer rowBuffer;

  @Before
  public void setupRowBuffer() {
    // Create row buffer
    this.rowBuffer = new ArrowRowBuffer(null);

    ColumnMetadata colTinyIntCase = new ColumnMetadata();
    colTinyIntCase.setName("colTinyInt");
    colTinyIntCase.setPhysicalType("SB1");
    colTinyIntCase.setNullable(false);
    colTinyIntCase.setLogicalType("FIXED");
    colTinyIntCase.setScale(0);

    ColumnMetadata colTinyInt = new ColumnMetadata();
    colTinyInt.setName("COLTINYINT");
    colTinyInt.setPhysicalType("SB1");
    colTinyInt.setNullable(false);
    colTinyInt.setLogicalType("FIXED");
    colTinyInt.setScale(0);

    ColumnMetadata colSmallInt = new ColumnMetadata();
    colSmallInt.setName("COLSMALLINT");
    colSmallInt.setPhysicalType("SB2");
    colSmallInt.setNullable(false);
    colSmallInt.setLogicalType("FIXED");
    colSmallInt.setScale(0);

    ColumnMetadata colInt = new ColumnMetadata();
    colInt.setName("COLINT");
    colInt.setPhysicalType("SB4");
    colInt.setNullable(false);
    colInt.setLogicalType("FIXED");
    colInt.setScale(0);

    ColumnMetadata colBigInt = new ColumnMetadata();
    colBigInt.setName("COLBIGINT");
    colBigInt.setPhysicalType("SB8");
    colBigInt.setNullable(false);
    colBigInt.setLogicalType("FIXED");
    colBigInt.setScale(0);

    ColumnMetadata colDecimal = new ColumnMetadata();
    colDecimal.setName("COLDECIMAL");
    colDecimal.setPhysicalType("SB16");
    colDecimal.setNullable(false);
    colDecimal.setLogicalType("FIXED");
    colDecimal.setPrecision(38);
    colDecimal.setScale(2);

    ColumnMetadata colChar = new ColumnMetadata();
    colChar.setName("COLCHAR");
    colChar.setPhysicalType("LOB");
    colChar.setNullable(true);
    colChar.setLogicalType("TEXT");
    colChar.setByteLength(14);
    colChar.setLength(11);
    colChar.setScale(0);

    // Setup column fields and vectors
    this.rowBuffer.setupSchema(
        Arrays.asList(
            colTinyIntCase, colTinyInt, colSmallInt, colInt, colBigInt, colDecimal, colChar));
  }

  @Test
  public void testReset() throws Exception {
    RowBufferStats stats = this.rowBuffer.statsMap.get("COLCHAR");
    stats.addIntValue(BigInteger.valueOf(1));
    Assert.assertEquals(BigInteger.valueOf(1), stats.getCurrentMaxIntValue());
    this.rowBuffer.reset();
    RowBufferStats resetStats = this.rowBuffer.statsMap.get("COLCHAR");
    Assert.assertNotNull(resetStats);
    Assert.assertNull(resetStats.getCurrentMaxIntValue());
  }

  @Test
  public void testInvalidLogicalType() throws Exception {
    ColumnMetadata colInvalidLogical = new ColumnMetadata();
    colInvalidLogical.setName("COLINVALIDLOGICAL");
    colInvalidLogical.setPhysicalType("SB1");
    colInvalidLogical.setNullable(false);
    colInvalidLogical.setLogicalType("INVALID");
    colInvalidLogical.setByteLength(14);
    colInvalidLogical.setLength(11);
    colInvalidLogical.setScale(0);

    try {
      this.rowBuffer.setupSchema(Arrays.asList(colInvalidLogical));
      Assert.fail("Setup should fail if invalid column metadata is provided");
    } catch (IllegalArgumentException e) {
      // Do nothing
    }
  }

  @Test
  public void testInvalidPhysicalType() throws Exception {
    ColumnMetadata colInvalidPhysical = new ColumnMetadata();
    colInvalidPhysical.setName("COLINVALIDPHYSICAL");
    colInvalidPhysical.setPhysicalType("INVALID");
    colInvalidPhysical.setNullable(false);
    colInvalidPhysical.setLogicalType("FIXED");
    colInvalidPhysical.setByteLength(14);
    colInvalidPhysical.setLength(11);
    colInvalidPhysical.setScale(0);

    try {
      this.rowBuffer.setupSchema(Arrays.asList(colInvalidPhysical));
      Assert.fail("Setup should fail if invalid column metadata is provided");
    } catch (IllegalArgumentException e) {
      // Do nothing
    }
  }

  @Test
  public void testInsertNullToNotNullColumn() throws Exception {
    ColumnMetadata colNotNull = new ColumnMetadata();
    colNotNull.setName("COLNOTNULL");
    colNotNull.setPhysicalType("SB16");
    colNotNull.setNullable(false);
    colNotNull.setLogicalType("FIXED");
    colNotNull.setPrecision(38);
    colNotNull.setScale(0);

    this.rowBuffer.setupSchema(Arrays.asList(colNotNull));

    try {
      Map<String, Object> row = new HashMap<>();
      row.put("colInt", null);
      row.put("colDecimal", null);
      row.put("colChar", null);
      row.put("colNotNull", null);
      this.rowBuffer.insertRows(Collections.singletonList(row), null);
      Assert.fail("Insert null to non-nullable column should fail");
    } catch (SFException e) {
      Assert.assertEquals(ErrorCode.INVALID_ROW.getMessageCode(), e.getVendorCode());
    }
  }

  @Test
  public void testInsertRow() throws Exception {
    Map<String, Object> row = new HashMap<>();
    row.put("colTinyInt", (byte) 1);
    row.put("colSmallInt", (short) 2);
    row.put("colInt", 3);
    row.put("colBigInt", 4L);
    row.put("colDecimal", 1.23);
    row.put("colChar", "2");

    try {
      this.rowBuffer.insertRows(Collections.singletonList(row), null);
    } catch (Exception e) {
      Assert.fail("Row buffer insert row failed");
    }
  }

  @Test
  public void testInsertRows() throws Exception {
    Map<String, Object> row1 = new HashMap<>();
    row1.put("colTinyInt", (byte) 1);
    row1.put("colSmallInt", (short) 2);
    row1.put("colInt", 3);
    row1.put("colBigInt", 4L);
    row1.put("colDecimal", 1.23);
    row1.put("colChar", "2");

    Map<String, Object> row2 = new HashMap<>();
    row2.put("colTinyInt", (byte) 1);
    row2.put("colSmallInt", (short) 2);
    row2.put("colInt", 3);
    row2.put("colBigInt", 4L);
    row2.put("colDecimal", 2.34);
    row2.put("colChar", "3");

    try {
      this.rowBuffer.insertRows(Arrays.asList(row1, row2), null);
    } catch (Exception e) {
      Assert.fail("Row buffer insert rows failed");
    }
  }

  @Test
  public void testInsertInvalidRow() throws Exception {
    Map<String, Object> row = new HashMap<>();
    row.put("colTinyInt", null);
    row.put("colSmallInt", null);
    row.put("colInt", null);
    row.put("colBigInt", null);
    row.put("colDecimal", 1.23456);
    row.put("colChar", null);

    try {
      this.rowBuffer.insertRows(Collections.singletonList(row), null);
      Assert.fail("Row buffer insert row should fail");
    } catch (SFException e) {
      Assert.assertEquals(ErrorCode.INVALID_ROW.getMessageCode(), e.getVendorCode());
    }
  }

  @Test
  public void testClose() throws Exception {
    this.rowBuffer.close();
    Map<String, Object> row = new HashMap<>();
    row.put("colTinyInt", (byte) 1);
    row.put("colSmallInt", (short) 2);
    row.put("colInt", 3);
    row.put("colBigInt", 4L);
    row.put("colDecimal", 1.23);
    row.put("colChar", "2");
    try {
      this.rowBuffer.insertRows(Collections.singletonList(row), null);
      Assert.fail("Insert should fail after buffer is closed");
    } catch (SFException e) {
      Assert.assertEquals(ErrorCode.INVALID_ROW.getMessageCode(), e.getVendorCode());
    }
  }

  @Test
  public void testFlush() throws Exception {
    Map<String, Object> row1 = new HashMap<>();
    row1.put("colTinyInt", (byte) 1);
    row1.put("colSmallInt", (short) 2);
    row1.put("colInt", 3);
    row1.put("colBigInt", 4L);
    row1.put("colDecimal", 1.23);
    row1.put("colChar", "2");

    Map<String, Object> row2 = new HashMap<>();
    row2.put("colTinyInt", (byte) 1);
    row2.put("colSmallInt", (short) 2);
    row2.put("colInt", 3);
    row2.put("colBigInt", 4L);
    row2.put("colDecimal", 2.34);
    row2.put("colChar", "3");

    this.rowBuffer.insertRows(Arrays.asList(row1, row2), "1");
    float bufferSize = this.rowBuffer.getSize();

    ChannelData data = this.rowBuffer.flush();
    Assert.assertEquals(2, data.getRowCount());
    Assert.assertEquals((Long) 0L, data.getRowSequencer());
    Assert.assertEquals(6, data.getVectors().size());
    Assert.assertEquals("1", data.getOffsetToken());
    Assert.assertEquals(bufferSize, data.getBufferSize(), 0);
  }

  @Test
  public void testDoubleQuotesColumnName() throws Exception {
    ColumnMetadata colDoubleQuotes = new ColumnMetadata();
    colDoubleQuotes.setName("colDoubleQuotes");
    colDoubleQuotes.setPhysicalType("SB16");
    colDoubleQuotes.setNullable(false);
    colDoubleQuotes.setLogicalType("FIXED");
    colDoubleQuotes.setPrecision(38);
    colDoubleQuotes.setScale(0);

    this.rowBuffer.setupSchema(Arrays.asList(colDoubleQuotes));

    Map<String, Object> row = new HashMap<>();
    row.put("colTinyInt", (byte) 1);
    row.put("colSmallInt", (short) 2);
    row.put("colInt", 3);
    row.put("colBigInt", 4L);
    row.put("colDecimal", 1.23);
    row.put("colChar", "2");
    row.put("\"colDoubleQuotes\"", 1);

    try {
      this.rowBuffer.insertRows(Collections.singletonList(row), null);
    } catch (Exception e) {
      Assert.fail("Row buffer insert row failed");
    }
  }

  @Test
  public void testBuildEpInfoFromStats() throws Exception {
    Map<String, RowBufferStats> colStats = new HashMap<>();

    RowBufferStats stats1 = new RowBufferStats();
    stats1.addIntValue(BigInteger.valueOf(2));
    stats1.addIntValue(BigInteger.valueOf(10));
    stats1.addIntValue(BigInteger.valueOf(1));

    RowBufferStats stats2 = new RowBufferStats();
    stats2.addStrValue("alice");
    stats2.addStrValue("bob");
    stats2.incCurrentNullCount();

    colStats.put("intColumn", stats1);
    colStats.put("strColumn", stats2);

    EpInfo result = ArrowRowBuffer.buildEpInfoFromStats(2, colStats);
    Map<String, FileColumnProperties> columnResults = result.getColumnEps();
    Assert.assertEquals(2, columnResults.keySet().size());

    FileColumnProperties strColumnResult = columnResults.get("strColumn");
    Assert.assertEquals(2, strColumnResult.getDistinctValues());
    Assert.assertEquals("alice", strColumnResult.getMinStrValue());
    Assert.assertEquals("bob", strColumnResult.getMaxStrValue());
    Assert.assertEquals(1, strColumnResult.getNullCount());

    FileColumnProperties intColumnResult = columnResults.get("intColumn");
    Assert.assertEquals(3, intColumnResult.getDistinctValues());
    Assert.assertEquals(BigInteger.valueOf(1), intColumnResult.getMinIntValue());
    Assert.assertEquals(BigInteger.valueOf(10), intColumnResult.getMaxIntValue());
    Assert.assertEquals(0, intColumnResult.getNullCount());
  }

  @Test
  public void testStatsE2E() throws Exception {
    Map<String, Object> row1 = new HashMap<>();
    row1.put("\"colTinyInt\"", (byte) 10);
    row1.put("colTinyInt", (byte) 1);
    row1.put("colSmallInt", (short) 2);
    row1.put("colInt", 3);
    row1.put("colBigInt", 4L);
    row1.put("colChar", "2");

    Map<String, Object> row2 = new HashMap<>();
    row2.put("\"colTinyInt\"", (byte) 11);
    row2.put("colTinyInt", (byte) 1);
    row2.put("colSmallInt", (short) 3);
    row2.put("colInt", null);
    row2.put("colBigInt", 40L);
    row2.put("colChar", "alice");

    this.rowBuffer.insertRows(Arrays.asList(row1, row2), null);
    ChannelData result = this.rowBuffer.flush();
    EpInfo resultInfo = result.getEpInfo();
    Assert.assertEquals(2, resultInfo.getRowCount());

    Assert.assertEquals(
        BigInteger.valueOf(11), resultInfo.getColumnEps().get("colTinyInt").getMaxIntValue());
    Assert.assertEquals(
        BigInteger.valueOf(10), resultInfo.getColumnEps().get("colTinyInt").getMinIntValue());
    Assert.assertEquals(0, resultInfo.getColumnEps().get("colTinyInt").getNullCount());
    Assert.assertEquals(2L, resultInfo.getColumnEps().get("colTinyInt").getDistinctValues());

    Assert.assertEquals(
        BigInteger.valueOf(1), resultInfo.getColumnEps().get("COLTINYINT").getMaxIntValue());
    Assert.assertEquals(
        BigInteger.valueOf(1), resultInfo.getColumnEps().get("COLTINYINT").getMinIntValue());
    Assert.assertEquals(0, resultInfo.getColumnEps().get("COLTINYINT").getNullCount());
    Assert.assertEquals(2L, resultInfo.getColumnEps().get("COLTINYINT").getDistinctValues());

    Assert.assertEquals(
        BigInteger.valueOf(3), resultInfo.getColumnEps().get("COLSMALLINT").getMaxIntValue());
    Assert.assertEquals(
        BigInteger.valueOf(2), resultInfo.getColumnEps().get("COLSMALLINT").getMinIntValue());
    Assert.assertEquals(0, resultInfo.getColumnEps().get("COLSMALLINT").getNullCount());
    Assert.assertEquals(2L, resultInfo.getColumnEps().get("COLSMALLINT").getDistinctValues());

    Assert.assertEquals(
        BigInteger.valueOf(3), resultInfo.getColumnEps().get("COLINT").getMaxIntValue());
    Assert.assertEquals(
        BigInteger.valueOf(3), resultInfo.getColumnEps().get("COLINT").getMinIntValue());
    Assert.assertEquals(1L, resultInfo.getColumnEps().get("COLINT").getNullCount());
    Assert.assertEquals(1L, resultInfo.getColumnEps().get("COLINT").getDistinctValues());

    Assert.assertEquals(
        BigInteger.valueOf(40), resultInfo.getColumnEps().get("COLBIGINT").getMaxIntValue());
    Assert.assertEquals(
        BigInteger.valueOf(4), resultInfo.getColumnEps().get("COLBIGINT").getMinIntValue());
    Assert.assertEquals(0, resultInfo.getColumnEps().get("COLBIGINT").getNullCount());
    Assert.assertEquals(2L, resultInfo.getColumnEps().get("COLBIGINT").getDistinctValues());

    Assert.assertEquals("alice", resultInfo.getColumnEps().get("COLCHAR").getMaxStrValue());
    Assert.assertEquals("2", resultInfo.getColumnEps().get("COLCHAR").getMinStrValue());
    Assert.assertEquals(0, resultInfo.getColumnEps().get("COLCHAR").getNullCount());
    Assert.assertEquals(2L, resultInfo.getColumnEps().get("COLCHAR").getDistinctValues());

    // Confirm we reset
    ChannelData resetResults = this.rowBuffer.flush();
    Assert.assertNull(resetResults);
  }
}
