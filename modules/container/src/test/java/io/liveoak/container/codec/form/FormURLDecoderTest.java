/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.form;

import java.net.URLEncoder;
import java.nio.charset.Charset;

import io.liveoak.common.codec.form.FormURLDecoder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aslak Knutsen
 */
public class FormURLDecoderTest {

    protected ResourceState decode(String data) throws Exception {

        FormURLDecoder decoder = new FormURLDecoder();
        ByteBuf buffer = Unpooled.wrappedBuffer(data.getBytes(Charset.defaultCharset()));
        return decoder.decode(buffer);
    }

    @Test
    public void testEmptyObject() throws Exception {
        ResourceState state = decode("");

        Assert.assertEquals(0, state.getPropertyNames().size());
    }

    @Test
    public void testSingleValue() throws Exception {
        ResourceState state = decode("a=x");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals("x", state.getProperty("a"));
    }

    @Test
    public void testMultipleValues() throws Exception {
        ResourceState state = decode("a=x&b=y");

        Assert.assertEquals(2, state.getPropertyNames().size());
        Assert.assertEquals("x", state.getProperty("a"));
        Assert.assertEquals("y", state.getProperty("b"));
    }

    @Test
    public void testEncodedKey() throws Exception {
        ResourceState state = decode(URLEncoder.encode("a=x", "UTF-8") + "=y");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals("y", state.getProperty("a=x"));
    }

    @Test
    public void testEncodedValue() throws Exception {
        ResourceState state = decode("a=" + URLEncoder.encode("a&b=1", "UTF-8"));

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals("a&b=1", state.getProperty("a"));
    }

    @Test
    public void testIntegerTypedValue() throws Exception {
        ResourceState state = decode("a=1");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Integer);
        Assert.assertEquals(1, state.getProperty("a"));
    }

    @Test
    public void testDoubleTypedValue() throws Exception {
        ResourceState state = decode("a=1.0");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Double);
        Assert.assertEquals(1.0, state.getProperty("a"));
    }

    @Test
    public void testStringTypedValue() throws Exception {
        ResourceState state = decode("a=x");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof String);
        Assert.assertEquals("x", state.getProperty("a"));
    }

    @Test
    public void testBooleanTypedTrueUpperValue() throws Exception {
        ResourceState state = decode("a=TRUE");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Boolean);
        Assert.assertEquals(true, state.getProperty("a"));
    }

    @Test
    public void testBooleanTypedTrueLowerValue() throws Exception {
        ResourceState state = decode("a=true");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Boolean);
        Assert.assertEquals(true, state.getProperty("a"));
    }

    @Test
    public void testBooleanTypedFalseUpperValue() throws Exception {
        ResourceState state = decode("a=FALSE");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Boolean);
        Assert.assertEquals(false, state.getProperty("a"));
    }

    @Test
    public void testBooleanTypedFalseLowerValue() throws Exception {
        ResourceState state = decode("a=false");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertTrue(state.getProperty("a") instanceof Boolean);
        Assert.assertEquals(false, state.getProperty("a"));
    }

    @Test
    public void testNullTypedLowerValue() throws Exception {
        ResourceState state = decode("a=null");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals(null, state.getProperty("a"));
    }

    @Test
    public void testNullTypedUpperValue() throws Exception {
        ResourceState state = decode("a=NULL");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals(null, state.getProperty("a"));
    }

    @Test
    public void testLastOfSameKeyOverrides() throws Exception {
        ResourceState state = decode("a=x&a=y");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals("y", state.getProperty("a"));
    }

    @Test
    public void testProcentEncoding() throws Exception {
        ResourceState state = decode("a=x%20y");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals("x y", state.getProperty("a"));
    }

    @Test
    public void testNullValue() throws Exception {
        ResourceState state = decode("a=");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals(null, state.getProperty("a"));
    }

    @Test
    public void testNullOnKeyOnly() throws Exception {
        ResourceState state = decode("a");

        Assert.assertEquals(1, state.getPropertyNames().size());
        Assert.assertEquals(null, state.getProperty("a"));
    }
}
