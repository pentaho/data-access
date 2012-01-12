/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.repository.pmd.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

@SuppressWarnings("nls")
public class MetadataDatasourceServiceTest extends TestCase {

	// ~ Instance fields
	// =================================================================================================

	private MicroPlatform booter;

	private IUnifiedRepository repository;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		booter = new MicroPlatform("test-res");

		// Clear up the cache
		final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
		cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);

		// Define a repository for testing
		repository = new MockUnifiedRepository(new MockUserProvider());
		repository.createFolder(repository.getFile("/etc").getId(), new RepositoryFile.Builder("metadata").folder(true).build(), "initialization");

		booter.defineInstance(IUnifiedRepository.class, repository);
		booter.start();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testImportSchema() throws Exception {
		try {
			String localizeBundleEntries = "index_ja.properties=index_ja.properties;index_de.properties=index_de.properties";
			MetadataDatasourceService service = new MetadataDatasourceService();
			service.importMetadataDatasource(localizeBundleEntries, "steel-wheels", "metadata.xmi");
		} catch (Exception e) {
			final RepositoryFile etcMetadata = repository.getFile(PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath());
			assertNotNull(etcMetadata);
			assertTrue(etcMetadata.isFolder());
			final List<RepositoryFile> children = repository.getChildren(etcMetadata.getId());
			assertNotNull(children);
			assertEquals(3, children.size());
		}
	}

	private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
		@Override
		public String getUser() {
			return MockUnifiedRepository.root().getName();
		}

		@Override
		public List<String> getRoles() {
			return new ArrayList<String>();
		}
	}
}
