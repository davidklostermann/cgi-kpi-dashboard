package com.cgi.kpi.dashboard.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectDataDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;
import com.cgi.kpi.dashboard.kpi.service.PortfolioKpiService;

class PortfolioKpiServiceContractTest {

    private static final String DOMAIN_PACKAGE = "com.cgi.kpi.dashboard.domain";

    @Test
    void readerInterfacesExist() throws ClassNotFoundException {
        assertTrue(Class.forName("com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader").isInterface());
        assertTrue(Class.forName("com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader").isInterface());
    }

    @Test
    void portfolioKpiServiceReturnsDtoNotDomainEntity() throws NoSuchMethodException {
        Method method = PortfolioKpiService.class.getMethod("getPortfolioSummary", PortfolioFilterCriteria.class);

        assertFalse(method.getReturnType().getName().startsWith(DOMAIN_PACKAGE));
        assertTrue(PortfolioKpiSummaryDto.class.isAssignableFrom(method.getReturnType()));
    }

    @Test
    void approvedProjectDataReaderReturnsDtoNotDomainEntity() throws NoSuchMethodException {
        Method method = ApprovedProjectDataReader.class.getMethod("readApprovedProjectData", UUID.class);

        assertTrue(Optional.class.isAssignableFrom(method.getReturnType()));
        assertFalse(method.getGenericReturnType().toString().contains(DOMAIN_PACKAGE));

        Type returnType = method.getGenericReturnType();
        assertInstanceOf(ParameterizedType.class, returnType);
        ParameterizedType parameterizedType = (ParameterizedType) returnType;
        assertEquals(Optional.class, parameterizedType.getRawType());
        assertEquals(ApprovedProjectDataDto.class, parameterizedType.getActualTypeArguments()[0]);
    }

    @Test
    void portfolioKpiServiceIsPublicApi() {
        assertTrue(Modifier.isPublic(PortfolioKpiService.class.getModifiers()));
        assertTrue(PortfolioKpiService.class.isInterface());
    }
}
