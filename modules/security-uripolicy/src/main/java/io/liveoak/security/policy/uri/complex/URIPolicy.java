/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.template.DataProviderCompiler;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Policy for authorization of resources based on resource URI. Policy implementation is based on drools engine
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicy {

    private static final Logger log = Logger.getLogger(URIPolicy.class);

    private RuleBase ruleBase;

    public URIPolicy(InitializationWorker worker) {
        doInit();
        worker.run(this);
    }

    /**
     * Drools initialization performed in separate worker thread
     */
    protected void doInit() {
        // Workaround for https://issues.jboss.org/browse/DROOLS-329 TODO: Remove when not needed or move to better place
        System.setProperty("drools.dialect.java.compiler", "JANINO");

        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration(URIPolicy.class.getClassLoader());
        ruleBase = RuleBaseFactory.newRuleBase(ruleBaseConfig);

        // Add DRL with functions
        InputStream functionsFileStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyFunctions.drl");
        Reader functionsFileReader = new InputStreamReader(functionsFileStream);
        addPackageToRuleBase(functionsFileReader);
    }


    public void addURIPolicyRule(URIPolicyRule uriPolicyRule) {
        InputStream templateStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyTemplate.drl");
        URIPolicyTemplateDataProvider tdp = new URIPolicyTemplateDataProvider(uriPolicyRule);
        DataProviderCompiler converter = new DataProviderCompiler();
        String drl = converter.compile(tdp, templateStream);

        // TODO:Logging
        if (log.isDebugEnabled()) {
            log.debug("------------ ADDING NEW POLICY RULE INTO DROOLS ENGINE ----------------------");
            log.debug(drl);
            log.debug("------------ END NEW POLICY RULE ------------------");
        }

        // Then add it into drools RuleBase
        addPackageToRuleBase(drl);
    }

    public AuthzDecision isAuthorized(RequestContext reqContext) {
        return isAuthorized(reqContext, null);
    }

    public AuthzDecision isAuthorized(RequestContext reqContext, ResourceState reqResourceState) {
        if (log.isTraceEnabled()) {
            log.trace("Start checking request: " + reqContext);
        }

        WorkingMemory workingMemory = null;

        try {
            workingMemory = ruleBase.newStatefulSession();

            RulesProcessingResult rulesProcessingResult = new RulesProcessingResult();
            workingMemory.insert(rulesProcessingResult);

            EndSemaphore endSemaphore = new EndSemaphore();
            workingMemory.insert(endSemaphore);

            URIMatcherCache cache = new URIMatcherCache();
            workingMemory.insert(cache);

            // TODO: Verify if it's better to first insert request or securityContext (Rules checking is triggered right after inserting, so it could affect performance)
            RequestContextDecorator reqContextDecorator = new RequestContextDecorator(reqContext);
            workingMemory.insert(reqContextDecorator);
            workingMemory.insert(reqContextDecorator.securityContext());

            // TODO: this is temporary
            if (reqResourceState == null) {
                reqResourceState = new DefaultResourceState();
            }
            workingMemory.insert(reqResourceState);

            // Uncomment for drools debugging (TODO: should be somehow configurable...)
            //workingMemory.addEventListener(new DebugAgendaEventListener());
            //workingMemory.addEventListener( new DebugWorkingMemoryEventListener() );

            int numberOfFiredPolicies = workingMemory.fireAllRules();

            if (log.isTraceEnabled()) {
                log.trace("Overall result for request: " + reqContext + ". Count of fired policies=" + numberOfFiredPolicies + ", Result=" + rulesProcessingResult.getDecision());
            }
            return rulesProcessingResult.getDecision();
        } finally {
            if (workingMemory != null) {
                workingMemory.dispose();
            }
        }
    }


    protected void addPackageToRuleBase(String drl) {
        Reader uriPolicyEntriesReader = new StringReader(drl);
        addPackageToRuleBase(uriPolicyEntriesReader);
    }


    protected void addPackageToRuleBase(Reader packageReader) {
        PackageBuilder packageBuilder = new PackageBuilder(ruleBase);

        try {
            packageBuilder.addPackageFromDrl(packageReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PackageBuilderErrors errors = packageBuilder.getErrors();
        if (errors.getErrors().length > 0) {
            StringBuilder errorMessages = new StringBuilder();
            errorMessages.append("Found errors in package builder\n");
            for (int i = 0; i < errors.getErrors().length; i++) {
                DroolsError errorMessage = errors.getErrors()[i];
                errorMessages.append(errorMessage);
                errorMessages.append("\n");
            }
            errorMessages.append("Could not parse knowledge");

            throw new IllegalArgumentException(errorMessages.toString());
        }

        org.drools.rule.Package rulesPackage = packageBuilder.getPackage();
        ruleBase.addPackage(rulesPackage);
    }
}
