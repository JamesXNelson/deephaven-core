/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit TestCharPrimitives and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.libs.primitives;

import io.deephaven.base.testing.BaseArrayTestCase;
import io.deephaven.db.tables.dbarrays.DbShortArray;
import io.deephaven.db.tables.dbarrays.DbShortArrayDirect;

import static io.deephaven.libs.primitives.ShortPrimitives.*;
import static io.deephaven.util.QueryConstants.NULL_SHORT;
import static io.deephaven.util.QueryConstants.NULL_LONG;

public class TestShortPrimitives extends BaseArrayTestCase {

    public void testUnbox(){
        assertNull(unbox((Short[])null));
        assertEquals(new short[]{1, NULL_SHORT, 3, NULL_SHORT}, unbox((short)1, null, (short)3, NULL_SHORT));
    }

    public void testIsNull(){
        assertFalse(isNull((short)3));
        assertTrue(isNull(NULL_SHORT));
    }

    public void testNullToValueScalar() {
        assertEquals((short) 3, nullToValue((short) 3, (short) 7));
        assertEquals((short) 7, nullToValue(NULL_SHORT, (short) 7));
    }

    public void testNullToValueArray() {
        assertEquals(new short[]{(short) 3, (short) 7, (short) 11}, nullToValue(new DbShortArrayDirect(new short[]{(short) 3, NULL_SHORT, (short) 11}), (short) 7));

        assertEquals(new short[]{(short) 3, (short) 7, (short) 11}, nullToValue(new short[]{(short) 3, NULL_SHORT, (short) 11}, (short) 7));
    }

    public void testCount(){
        assertEquals(0, count((DbShortArray)null));
        assertEquals(3,count(new DbShortArrayDirect(new short[]{40,50,60})));
        assertEquals(0,count(new DbShortArrayDirect()));
        assertEquals(0,count(new DbShortArrayDirect(NULL_SHORT)));
        assertEquals(2,count(new DbShortArrayDirect(new short[]{5,NULL_SHORT,15})));
    }

    public void testLast(){
        assertTrue(Math.abs(60-last(new DbShortArrayDirect(new short[]{40,50,60})))==0.0);
        assertEquals(NULL_SHORT,last(new DbShortArrayDirect()));
        assertEquals(NULL_SHORT,last(new DbShortArrayDirect(NULL_SHORT)));
        assertTrue(Math.abs(15-last(new DbShortArrayDirect(new short[]{5,NULL_SHORT,15})))==0.0);
        assertTrue(Math.abs(40-last(new DbShortArrayDirect((short)40)))==0.0);

        assertTrue(Math.abs(60-last(new short[]{40,50,60}))==0.0);
        assertEquals(NULL_SHORT,last(new short[]{}));
        assertEquals(NULL_SHORT,last(new short[]{NULL_SHORT}));
        assertTrue(Math.abs(15-last(new short[]{5,NULL_SHORT,15}))==0.0);
        assertTrue(Math.abs(40-last(new short[]{(short)40}))==0.0);
    }

    public void testFirst(){
        assertTrue(Math.abs(40-first(new DbShortArrayDirect(new short[]{40,50,60})))==0.0);
        assertEquals(NULL_SHORT,first(new DbShortArrayDirect()));
        assertEquals(NULL_SHORT,first(new DbShortArrayDirect(NULL_SHORT)));
        assertTrue(Math.abs(5-first(new DbShortArrayDirect(new short[]{5,NULL_SHORT,15})))==0.0);
        assertTrue(Math.abs(40-first(new DbShortArrayDirect((short)40)))==0.0);

        assertTrue(Math.abs(40-first(new short[]{40,50,60}))==0.0);
        assertEquals(NULL_SHORT,first(new short[]{}));
        assertEquals(NULL_SHORT,first(new short[]{NULL_SHORT}));
        assertTrue(Math.abs(5-first(new short[]{5,NULL_SHORT,15}))==0.0);
        assertTrue(Math.abs(40-first(new short[]{(short)40}))==0.0);
    }

    public void testNth(){
        assertEquals(NULL_SHORT, nth(-1,new DbShortArrayDirect(new short[]{40,50,60})));
        assertEquals((short)40, nth(0,new DbShortArrayDirect(new short[]{40,50,60})));
        assertEquals((short)50, nth(1,new DbShortArrayDirect(new short[]{40,50,60})));
        assertEquals((short)60, nth(2,new DbShortArrayDirect(new short[]{40,50,60})));
        assertEquals(NULL_SHORT, nth(10,new DbShortArrayDirect(new short[]{40,50,60})));

        assertEquals(NULL_SHORT, nth(-1,new short[]{40,50,60}));
        assertEquals((short)40, nth(0,new short[]{40,50,60}));
        assertEquals((short)50, nth(1,new short[]{40,50,60}));
        assertEquals((short)60, nth(2,new short[]{40,50,60}));
        assertEquals(NULL_SHORT, nth(10,new short[]{40,50,60}));
    }

    public void testCountDistinct() {
        assertEquals(NULL_LONG, countDistinct((DbShortArrayDirect)null));
        assertEquals(NULL_LONG, countDistinct((DbShortArrayDirect)null,true));
        assertEquals(0, countDistinct(new DbShortArrayDirect(new short[]{})));
        assertEquals(0, countDistinct(new DbShortArrayDirect(new short[]{NULL_SHORT})));
        assertEquals(1, countDistinct(new DbShortArrayDirect(new short[]{1})));
        assertEquals(2, countDistinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT})));
        assertEquals(2, countDistinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}), false));
        assertEquals(3, countDistinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}), true));

        assertEquals(NULL_LONG, countDistinct((short[])null));
        assertEquals(NULL_LONG, countDistinct((short[])null,true));
        assertEquals(0, countDistinct(new short[]{}));
        assertEquals(0, countDistinct(new short[]{NULL_SHORT}));
        assertEquals(1, countDistinct(new short[]{1}));
        assertEquals(2, countDistinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}));
        assertEquals(2, countDistinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}, false));
        assertEquals(3, countDistinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}, true));
    }

    public void testDistinct() {
        assertEquals(null, distinct((DbShortArrayDirect)null));
        assertEquals(null, distinct((DbShortArrayDirect)null, true, true));
        assertEquals(new DbShortArrayDirect(), distinct(new DbShortArrayDirect(new short[]{})));
        assertEquals(new DbShortArrayDirect(), distinct(new DbShortArrayDirect(new short[]{NULL_SHORT})));
        assertEquals(new DbShortArrayDirect(new short[]{1}), distinct(new DbShortArrayDirect(new short[]{1})));
        assertEquals(new DbShortArrayDirect(new short[]{1,2}), distinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT})));
        assertEquals(new DbShortArrayDirect(new short[]{1,2}), distinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}), false, false));
        assertEquals(new DbShortArrayDirect(new short[]{1,2,NULL_SHORT}), distinct(new DbShortArrayDirect(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}), true, false));
        assertEquals(new DbShortArrayDirect(new short[]{1,2,3}), distinct(new DbShortArrayDirect(new short[]{3,1,2,1,NULL_SHORT,NULL_SHORT}), false, true));
        assertEquals(new DbShortArrayDirect(new short[]{1,2,3,4}), distinct(new DbShortArrayDirect(new short[]{3,1,2,4,1,NULL_SHORT,NULL_SHORT}), false, true));
        assertEquals(new DbShortArrayDirect(new short[]{NULL_SHORT,1,2,3,4}), distinct(new DbShortArrayDirect(new short[]{3,1,2,4,1,NULL_SHORT,NULL_SHORT}), true, true));

        assertEquals(null, distinct((short[])null));
        assertEquals(null, distinct((short[])null, true, true));
        assertEquals(new short[]{}, distinct(new short[]{}));
        assertEquals(new short[]{}, distinct(new short[]{NULL_SHORT}));
        assertEquals(new short[]{1}, distinct(new short[]{1}));
        assertEquals(new short[]{1,2}, distinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}));
        assertEquals(new short[]{1,2}, distinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}, false, false));
        assertEquals(new short[]{1,2,NULL_SHORT}, distinct(new short[]{1,2,1,NULL_SHORT,NULL_SHORT}, true, false));
        assertEquals(new short[]{1,2,3}, distinct(new short[]{3,1,2,1,NULL_SHORT,NULL_SHORT}, false, true));
        assertEquals(new short[]{1,2,3,4}, distinct(new short[]{3,1,2,4,1,NULL_SHORT,NULL_SHORT}, false, true));
        assertEquals(new short[]{NULL_SHORT,1,2,3,4}, distinct(new short[]{3,1,2,4,1,NULL_SHORT,NULL_SHORT}, true, true));
    }

    public void testVec(){
        assertEquals(new short[]{(short)1,(short)3,(short)5}, vec(new DbShortArrayDirect((short)1,(short)3,(short)5)));
    }

    public void testArray(){
        assertEquals(new DbShortArrayDirect((short)1,(short)3,(short)5), array(new short[]{(short)1,(short)3,(short)5}));
    }

    public void testIn(){
        assertTrue(in((short)1,(short)1,(short)2,(short)3));
        assertFalse(in((short)5,(short)1,(short)2,(short)3));
        assertFalse(in(NULL_SHORT,(short)1,(short)2,(short)3));
        assertTrue(in(NULL_SHORT,(short)1,(short)2,NULL_SHORT,(short)3));
    }

    public void testInRange(){
        assertTrue(inRange((short)2,(short)1,(short)3));
        assertTrue(inRange((short)1,(short)1,(short)3));
        assertFalse(inRange(NULL_SHORT,(short)1,(short)3));
        assertTrue(inRange((short)3,(short)1,(short)3));
        assertFalse(inRange((short)4,(short)1,(short)3));
    }

    public void testRepeat() {
        assertEquals(new short[]{5,5,5}, repeat((short) 5, 3));
        assertEquals(new short[]{}, repeat((short) 5, -3));
    }

    public void testEnlist() {
        assertEquals(new short[]{1, 11, 6}, enlist((short)1, (short)11, (short)6));
        assertEquals(new short[]{}, enlist((short[])(null)));
    }

    public void testConcat() {
        assertEquals(new short[]{}, concat((short[][])null));
        assertEquals(new short[]{1,2,3,4,5,6}, concat(new short[]{1,2}, new short[]{3}, new short[]{4,5,6}));
        assertEquals(new short[]{}, concat((short[])(null)));

        assertEquals(new short[]{}, concat((DbShortArray[])null));
        assertEquals(new short[]{1,2,3,4,5,6}, concat(new DbShortArrayDirect(new short[]{1,2}), new DbShortArrayDirect(new short[]{3}), new DbShortArrayDirect(new short[]{4,5,6})));
        assertEquals(new short[]{}, concat((DbShortArray) (null)));
    }

    public void testReverse() {
        assertEquals(new short[]{3,2,1}, reverse((short)1,(short)2,(short)3));
        assertEquals(null, reverse((short[])(null)));

        assertEquals(new short[]{3,2,1}, reverse(new DbShortArrayDirect(new short[]{1,2,3})));
        assertEquals(null, reverse((DbShortArray) (null)));
    }
}

