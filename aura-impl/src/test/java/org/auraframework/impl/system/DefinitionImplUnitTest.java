/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.system;

import java.util.Map;

import org.auraframework.Aura;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.Definition;
import org.auraframework.def.DefinitionAccess;
import org.auraframework.impl.DefinitionAccessImpl;
import org.auraframework.impl.system.DefinitionImpl.RefBuilderImpl;
import org.auraframework.system.AuraContext;
import org.auraframework.system.AuraContext.Authentication;
import org.auraframework.system.AuraContext.Format;
import org.auraframework.system.AuraContext.Mode;
import org.auraframework.system.Location;
import org.auraframework.system.SubDefDescriptor;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.auraframework.util.test.util.UnitTestCase;
import org.auraframework.util.text.Hash;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

public abstract class DefinitionImplUnitTest<I extends DefinitionImpl<D>, D extends Definition, R extends Definition, B extends RefBuilderImpl<D, R>>
extends UnitTestCase {

    protected String descriptorName;
    protected String qualifiedDescriptorName;
    @Mock
    protected DefDescriptor<D> descriptor;
    @Mock
    protected Location location;
    protected Map<SubDefDescriptor<?, D>, Definition> subDefs;
    protected String description;
    protected DefinitionAccess access = null;
    @Mock
    protected Hash sourceHash;
    protected String ownHash;

    protected AuraContext testAuraContext;

    @Test
    public void testGetDescription() throws Exception {
        this.description = "this is a test Definition";
        String actual = buildDefinition().getDescription();
        assertEquals(this.description, actual);
    }

    @Test
    public void testGetDescriptor() throws Exception {
        Object actual = buildDefinition().getDescriptor();
        assertEquals(this.descriptor, actual);
    }

    @Test
    public void testGetLocation() throws Exception {
        Location actual = buildDefinition().getLocation();
        assertEquals(this.location, actual);
    }

    @Test
    public void testGetName() throws Exception {
        String actual = buildDefinition().getName();
        assertEquals(this.descriptorName, actual);
    }

    @Test
    public void testGetNameNullDescriptor() throws Exception {
        this.descriptor = null;
        R instance = buildDefinition();
        String expected = instance.getClass().getName();
        String actual = buildDefinition().getName();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetOwnHashWithSourceHash() throws Exception {
        Mockito.doReturn("myhash").when(this.sourceHash).toString();
        Mockito.doReturn(true).when(this.sourceHash).isSet();
        String actual = buildDefinition().getOwnHash();
        assertEquals("myhash", actual);
    }

    @Test
    public void testGetOwnHashWithOwnHashAndNoSourceHash() throws Exception {
        this.sourceHash = null;
        this.ownHash = "ownhash";
        String actual = buildDefinition().getOwnHash();
        assertEquals(this.ownHash, actual);
    }

    @Test
    public void testGetSubDefinition() throws Exception {
        @SuppressWarnings("unchecked")
        SubDefDescriptor<?, D> subdesc = Mockito.mock(SubDefDescriptor.class);
        Definition def = Mockito.mock(Definition.class);
        this.subDefs = ImmutableMap.<SubDefDescriptor<?, D>, Definition> of(subdesc, def);
        Object actual = buildDefinition().getSubDefinition(subdesc);
        assertEquals(def, actual);
    }

    @Test
    public void testGetSubDefinitionNull() throws Exception {
        this.subDefs = ImmutableMap.of();
        Definition actual = buildDefinition().getSubDefinition(null);
        assertNull(actual);
    }

    @Test
    public void testGetSubDefinitionWithoutSubDefinitions() throws Exception {
        SubDefDescriptor<?, ?> subdesc = Mockito.mock(SubDefDescriptor.class);
        this.subDefs = null;
        Object actual = buildDefinition().getSubDefinition(subdesc);
        assertNull(actual);
    }

    @Test
    public void testGetSubDefinitionNotFound() throws Exception {
        SubDefDescriptor<?, ?> subdesc = Mockito.mock(SubDefDescriptor.class);
        this.subDefs = ImmutableMap.of();
        Object actual = buildDefinition().getSubDefinition(subdesc);
        assertNull(actual);
    }

    @Test
    public void testAccessGlobal() throws Exception {
        this.access = new DefinitionAccessImpl(null, "global");
        DefinitionAccess actual = buildDefinition().getAccess();
        assertTrue(actual.isGlobal());
    }

    @Test
    public void testAccessGlobalDynamic() throws Exception {
        this.access = new DefinitionAccessImpl(null, "org.auraframework.impl.test.util.TestAccessMethods.allowGlobal");
        DefinitionAccess actual = buildDefinition().getAccess();
        assertTrue(actual.isGlobal());
    }

    @Test
    public void testAccessDefault() throws Exception {
        this.access = DefinitionAccessImpl.defaultAccess(null);
        DefinitionAccess actual = buildDefinition().getAccess();
        assertTrue(actual.isPublic());
    }


    @Test
    public void testIsValid() throws Exception {
        boolean actual = buildDefinition().isValid();
        assertFalse(actual);
    }

    @Test
    public void testMarkValid() throws Exception {
        R def = buildDefinition();
        def.markValid();
        boolean actual = def.isValid();
        assertTrue(actual);
    }

    @Test
    public void testValidateDefinition() throws Exception {
        if (testAuraContext != null) {
            Aura.getContextService().endContext();
        }

        testAuraContext = Aura.getContextService().startContext(Mode.PROD, Format.JS, Authentication.AUTHENTICATED);

        buildDefinition().validateDefinition();
    }

    @Test
    public void testValidateDefinitionNullDescriptor() throws Exception {
        this.descriptor = null;
        try {
            buildDefinition().validateDefinition();
            fail("Expected an exception for null descriptor");
        } catch (Exception e) {
            assertExceptionMessage(e, InvalidDefinitionException.class, "No descriptor");
        }
    }

    // used to setup references to be validated by subclasses
    @Test
    public void testValidateReferences() throws Exception {
        if (testAuraContext != null) {
            Aura.getContextService().endContext();
        }

        testAuraContext = Aura.getContextService().startContext(Mode.PROD, Format.JS, Authentication.AUTHENTICATED);

        setupValidateReferences();
        buildDefinition().validateReferences();
    }

    @Override
    public void tearDown() throws Exception {
        if (testAuraContext != null) {
            Aura.getContextService().endContext();
        }
    }

    protected void setupValidateReferences() throws Exception {
    }

    /**
     * Get a Builder instance to use for building test instances
     */
    protected abstract B getBuilder();

    /**
     * Get a DefinitionImpl instance to test
     */
    protected R buildDefinition() throws Exception {
        return buildDefinition(getBuilder());
    }

    protected R buildDefinition(B builder) throws Exception {
        if (this.qualifiedDescriptorName != null && this.descriptor != null) {
            Mockito.doReturn(this.descriptorName).when(this.descriptor).getName();
            Mockito.doReturn(this.qualifiedDescriptorName).when(this.descriptor).getQualifiedName();
        }
        builder.setDescriptor(this.descriptor);
        builder.setLocation(this.location);
        builder.subDefs = this.subDefs;
        builder.setDescription(this.description);
        builder.hash = this.sourceHash;
        builder.ownHash = this.ownHash;
        builder.setAccess(this.access);
        return builder.build();
    }

	protected void setupValidateDefinitions() throws Exception {
		
	}
}
