package com.lanahra.whitelist.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lanahra.whitelist.entity.ClientExpression;
import com.lanahra.whitelist.entity.ClientWhitelistRepository;
import com.lanahra.whitelist.entity.Expression;
import com.lanahra.whitelist.entity.GlobalExpression;
import com.lanahra.whitelist.entity.GlobalWhitelistRepository;
import java.util.List;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {

    @InjectMocks
    private Service service;

    @Mock
    private GlobalWhitelistRepository globalWhitelistRepository;

    @Mock
    private ClientWhitelistRepository clientWhitelistRepository;

    @Test
    public void testInsertion_globalException() {
        GlobalExpression expression = new GlobalExpression();

        when(globalWhitelistRepository.save(any(GlobalExpression.class)))
            .thenThrow(new DataIntegrityViolationException("Null expression"){});

        Expression result = service.processExpressionInsertion((Expression) expression);

        assertThat(result).isNull();
    }

    @Test
    public void testInsertion_globalExpressions() {
        GlobalExpression expression = new GlobalExpression();
        expression.setRegex("expression");

        when(globalWhitelistRepository.save(any(GlobalExpression.class)))
            .thenReturn(expression);

        Expression result = service.processExpressionInsertion((Expression) expression);

        assertThat(result.getRegex()).isEqualTo(expression.getRegex());
    }

    @Test
    public void testInsertion_clientException() {
        ClientExpression expression = new ClientExpression();
        expression.setClient("client");

        when(clientWhitelistRepository.save(any(ClientExpression.class)))
            .thenThrow(new DataIntegrityViolationException("Null expression"){});

        Expression result = service.processExpressionInsertion((Expression) expression);

        assertThat(result).isNull();
    }

    @Test
    public void testInsertion_clientExpressions() {
        ClientExpression expression = new ClientExpression();
        expression.setClient("client");
        expression.setRegex("expression");

        when(clientWhitelistRepository.save(any(ClientExpression.class)))
            .thenReturn(expression);

        Expression result = service.processExpressionInsertion((Expression) expression);

        assertThat(result.getClient()).isEqualTo(expression.getClient());
        assertThat(result.getRegex()).isEqualTo(expression.getRegex());
    }

    @Test
    public void testValidation_emptyDatasource() {
        Page<Expression> expressions = new PageImpl<>(new ArrayList<>());

        when(globalWhitelistRepository.findAll(any(Pageable.class)))
            .thenReturn(expressions);

        when(clientWhitelistRepository.findByClient(any(String.class), any(Pageable.class)))
            .thenReturn(expressions);


        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");
        request.setCorrelationId(0);

        ValidationResponse response = service.processExpressionValidation(request);

        assertThat(response.getMatch()).isFalse();
        assertThat(response.getRegex()).isNull();
        assertThat(response.getCorrelationId()).isEqualTo(request.getCorrelationId());
    }

    @Test
    public void testValidation_noMatchDatasource() {
        Expression globalExpression = new Expression();
        globalExpression.setRegex("nomatch");

        Expression clientExpression = new Expression();
        clientExpression.setClient("client");
        clientExpression.setRegex("nomatch");

        List<Expression> globalList = new ArrayList<>();
        globalList.add(globalExpression);

        List<Expression> clientList = new ArrayList<>();
        clientList.add(clientExpression);

        Page<Expression> globalExpressions = new PageImpl<>(globalList);

        when(globalWhitelistRepository.findAll(any(Pageable.class)))
            .thenReturn(globalExpressions);

        Page<Expression> clientExpressions = new PageImpl<>(clientList);

        when(clientWhitelistRepository.findByClient(any(String.class), any(Pageable.class)))
            .thenReturn(clientExpressions);


        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");
        request.setCorrelationId(0);

        ValidationResponse response = service.processExpressionValidation(request);

        assertThat(response.getMatch()).isFalse();
        assertThat(response.getRegex()).isNull();
        assertThat(response.getCorrelationId()).isEqualTo(request.getCorrelationId());
    }

    @Test
    public void testValidation_matchGlobalDatasource() {
        Expression globalExpression = new Expression();
        globalExpression.setRegex("url");

        Expression clientExpression = new Expression();
        clientExpression.setClient("client");
        clientExpression.setRegex("nomatch");

        List<Expression> globalList = new ArrayList<>();
        globalList.add(globalExpression);

        List<Expression> clientList = new ArrayList<>();
        clientList.add(clientExpression);

        Page<Expression> globalExpressions = new PageImpl<>(globalList);

        when(globalWhitelistRepository.findAll(any(Pageable.class)))
            .thenReturn(globalExpressions);

        Page<Expression> clientExpressions = new PageImpl<>(clientList);

        when(clientWhitelistRepository.findByClient(any(String.class), any(Pageable.class)))
            .thenReturn(clientExpressions);

        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");
        request.setCorrelationId(0);

        ValidationResponse response = service.processExpressionValidation(request);

        assertThat(response.getMatch()).isTrue();
        assertThat(response.getRegex()).isEqualTo(globalExpression.getRegex());
        assertThat(response.getCorrelationId()).isEqualTo(request.getCorrelationId());
    }

    @Test
    public void testValidation_matchClientDatasource() {
        Expression globalExpression = new Expression();
        globalExpression.setRegex("nomatch");

        Expression clientExpression = new Expression();
        clientExpression.setClient("client");
        clientExpression.setRegex("url");

        List<Expression> globalList = new ArrayList<>();
        globalList.add(globalExpression);

        List<Expression> clientList = new ArrayList<>();
        clientList.add(clientExpression);

        Page<Expression> globalExpressions = new PageImpl<>(globalList);

        when(globalWhitelistRepository.findAll(any(Pageable.class)))
            .thenReturn(globalExpressions);

        Page<Expression> clientExpressions = new PageImpl<>(clientList);

        when(clientWhitelistRepository.findByClient(any(String.class), any(Pageable.class)))
            .thenReturn(clientExpressions);


        ValidationRequest request = new ValidationRequest();
        request.setClient("client");
        request.setUrl("url");
        request.setCorrelationId(0);

        ValidationResponse response = service.processExpressionValidation(request);

        assertThat(response.getMatch()).isTrue();
        assertThat(response.getRegex()).isEqualTo(clientExpression.getRegex());
        assertThat(response.getCorrelationId()).isEqualTo(request.getCorrelationId());
    }
}
