package uk.gov.hmcts.ccd.integrations;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.ccd.ApplicationParams;
import uk.gov.hmcts.ccd.data.definition.CachedCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.CaseTypeDefinitionVersion;
import uk.gov.hmcts.ccd.data.definition.DefaultCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.HttpUIDefinitionGateway;
import uk.gov.hmcts.ccd.data.definition.UIDefinitionRepository;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTabCollection;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.model.definition.SearchInputDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.SearchResult;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPage;
import uk.gov.hmcts.ccd.domain.model.definition.WorkbasketInputDefinition;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:integration_tests.properties")
public class DefinitionsCachingIT {

    static final Logger LOGGER = LoggerFactory.getLogger(DefinitionsCachingIT.class);

    private static final String ID_1 = "case1";
    private static final String ID_2 = "case11";
    private static final String ID_3 = "case111";
    private static final String EVENT_ID = "event1";
    private static final int VERSION_1 = 33;
    private static final int VERSION_2 = 3311;
    private static final int VERSION_3 = 331111;

    @SpyBean
    private DefaultCaseDefinitionRepository caseDefinitionRepository;

    @Autowired
    CachedCaseDefinitionRepository cachedCaseDefinitionRepository;

    @Autowired
    ApplicationParams applicationParams;

    @SpyBean
    UIDefinitionRepository uiDefinitionRepository;

    @SpyBean
    private HttpUIDefinitionGateway httpUIDefinitionGateway;

    @Mock
    CaseType mockCaseType;

    @Mock
    WorkbasketInputDefinition workbasketInputDefinition;

    @Mock
    SearchResult searchResult;

    @Mock
    CaseTabCollection caseTabCollection;

    @Mock
    SearchInputDefinition searchInputDefinition;

    List<WizardPage> wizardPageList = Collections.emptyList();

    @Before
    public void setup() {
        doReturn(aCaseTypeDefVersion(VERSION_1)).when(this.caseDefinitionRepository).doGetLatestVersion(ID_1);
        doReturn(aCaseTypeDefVersion(VERSION_2)).when(this.caseDefinitionRepository).doGetLatestVersion(ID_2);
        doReturn(aCaseTypeDefVersion(VERSION_3)).when(this.caseDefinitionRepository).doGetLatestVersion(ID_3);
        doReturn(mockCaseType).when(this.caseDefinitionRepository).getCaseType(ID_1);
    }

    @Test
    public void testCaseDefinitionLatestVersionsAreCached() {
        Assert.assertEquals(3, applicationParams.getLatestVersionTTLSecs());
        cachedCaseDefinitionRepository.getLatestVersion(ID_2);
        cachedCaseDefinitionRepository.getLatestVersion(ID_2);
        cachedCaseDefinitionRepository.getLatestVersion(ID_2);

        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_2);
    }

    @Test
    public void testTtlBasedEvictionOfCaseDefinitionLatestVersion() throws InterruptedException {
        verify(caseDefinitionRepository, times(0)).getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_3);

        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_3);

        TimeUnit.SECONDS.sleep(1);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_3);

        TimeUnit.SECONDS.sleep(1);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_3);

        TimeUnit.SECONDS.sleep(1);
        verify(caseDefinitionRepository, times(1)).getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(2)).getLatestVersion(ID_3);

        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        caseDefinitionRepository.getLatestVersion(ID_3);
        verify(caseDefinitionRepository, times(2)).getLatestVersion(ID_3);
    }

    @Test
    public void testCaseDefinitionAreCached() {

        cachedCaseDefinitionRepository.getCaseType(ID_1);
        cachedCaseDefinitionRepository.getCaseType(ID_1);
        cachedCaseDefinitionRepository.getCaseType(ID_1);

        verify(caseDefinitionRepository, times(1)).getCaseType(VERSION_1, ID_1);
        verify(caseDefinitionRepository, times(1)).getCaseType(ID_1);
    }

    @Test
    public void testWorkbasketInputDefinitionsAreCached() {

        doReturn(workbasketInputDefinition).when(this.httpUIDefinitionGateway).getWorkbasketInputDefinitions(VERSION_1, ID_1);

        uiDefinitionRepository.getWorkbasketInputDefinitions(ID_1);
        uiDefinitionRepository.getWorkbasketInputDefinitions(ID_1);
        uiDefinitionRepository.getWorkbasketInputDefinitions(ID_1);

        verify(httpUIDefinitionGateway, times(1)).getWorkbasketInputDefinitions(VERSION_1, ID_1);
    }

    @Test
    public void testWorkbasketResultAreCached() {

        doReturn(searchResult).when(this.httpUIDefinitionGateway).getWorkBasketResult(VERSION_1, ID_1);

        uiDefinitionRepository.getWorkBasketResult(ID_1);
        uiDefinitionRepository.getWorkBasketResult(ID_1);
        uiDefinitionRepository.getWorkBasketResult(ID_1);

        verify(httpUIDefinitionGateway, times(1)).getWorkBasketResult(VERSION_1, ID_1);
    }

    @Test
    public void testSearchResultAreCached() {

        doReturn(searchResult).when(this.httpUIDefinitionGateway).getSearchResult(VERSION_1, ID_1);

        uiDefinitionRepository.getSearchResult(ID_1);
        uiDefinitionRepository.getSearchResult(ID_1);
        uiDefinitionRepository.getSearchResult(ID_1);

        verify(httpUIDefinitionGateway, times(1)).getSearchResult(VERSION_1, ID_1);
    }

    @Test
    public void testCaseTabsAreCached() {

        doReturn(caseTabCollection).when(this.httpUIDefinitionGateway).getCaseTabCollection(VERSION_1, ID_1);

        uiDefinitionRepository.getCaseTabCollection(ID_1);
        uiDefinitionRepository.getCaseTabCollection(ID_1);
        uiDefinitionRepository.getCaseTabCollection(ID_1);

        verify(httpUIDefinitionGateway, times(1)).getCaseTabCollection(VERSION_1, ID_1);
    }

    @Test
    public void testSearchInputDefinitionsAreCached() {

        doReturn(searchInputDefinition).when(this.httpUIDefinitionGateway).getSearchInputDefinitions(VERSION_1, ID_1);

        uiDefinitionRepository.getSearchInputDefinitions(ID_1);
        uiDefinitionRepository.getSearchInputDefinitions(ID_1);
        uiDefinitionRepository.getSearchInputDefinitions(ID_1);

        verify(httpUIDefinitionGateway, times(1)).getSearchInputDefinitions(VERSION_1, ID_1);
    }

    @Test
    public void testWizardPageDefinitionsAreCached() {

        doReturn(wizardPageList).when(this.httpUIDefinitionGateway).getWizardPageCollection(VERSION_1, ID_1, EVENT_ID);

        uiDefinitionRepository.getWizardPageCollection(ID_1, EVENT_ID);
        uiDefinitionRepository.getWizardPageCollection(ID_1, EVENT_ID);
        uiDefinitionRepository.getWizardPageCollection(ID_1, EVENT_ID);

        verify(httpUIDefinitionGateway, times(1)).getWizardPageCollection(VERSION_1, ID_1, EVENT_ID);
    }

    protected CaseTypeDefinitionVersion aCaseTypeDefVersion(int version) {
        CaseTypeDefinitionVersion ctdv = new CaseTypeDefinitionVersion();
        ctdv.setVersion(version);
        return ctdv;
    }
}


