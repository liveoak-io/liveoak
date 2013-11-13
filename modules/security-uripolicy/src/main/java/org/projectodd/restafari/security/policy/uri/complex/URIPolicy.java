package org.projectodd.restafari.security.policy.uri.complex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.template.DataProviderCompiler;
import org.projectodd.restafari.security.impl.SimpleLogger;
import org.projectodd.restafari.security.spi.AuthToken;
import org.projectodd.restafari.security.spi.AuthorizationDecision;
import org.projectodd.restafari.security.spi.AuthorizationPolicy;
import org.projectodd.restafari.security.spi.AuthorizationRequestContext;
import org.projectodd.restafari.spi.RequestContext;

/**
 * Complex URI policy based on drools engine
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicy implements AuthorizationPolicy {

    // TODO: Replace with real logging
    private static final SimpleLogger log = new SimpleLogger(URIPolicy.class);

    private RuleBase ruleBase;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void init() {
        // Execute initialization asynchronously
        Runnable initTask = () -> {
                doInit();
                latch.countDown();
        };
        executor.execute(initTask);
    }

    /**
     * Drools initialization performed in separate worker thread
     */
    protected void doInit() {
        // Workaround for https://issues.jboss.org/browse/DROOLS-329 TODO: Remove when not needed or move to better place
        System.setProperty("drools.dialect.java.compiler", "JANINO");

        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration(URIPolicy.class.getClassLoader(), AuthorizationPolicy.class.getClassLoader());
        ruleBase = RuleBaseFactory.newRuleBase(ruleBaseConfig);

        // Add DRL with functions
        InputStream functionsFileStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyFunctions.drl");
        Reader functionsFileReader = new InputStreamReader(functionsFileStream);
        addPackageToRuleBase(functionsFileReader);
    }


    public void addURIPolicyEntry(URIPolicyEntry uriPolicyEntry) {
        InputStream templateStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyTemplate.drl");
        URIPolicyTemplateDataProvider tdp = new URIPolicyTemplateDataProvider(uriPolicyEntry);
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


    @Override
    public AuthorizationDecision isAuthorized(AuthorizationRequestContext authRequestContext) {
        checkInitializationCompleted();

        RequestContext reqContext = authRequestContext.getRequestContext();
        AuthToken token = authRequestContext.getAuthToken();
        if (log.isTraceEnabled()) {
            log.debug("Start checking request: " + reqContext + ", token: " + token);
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

            // TODO: Verify if it's better to first insert request or token (Rules checking is triggered right after inserting, so it could affect performance)
            RequestContextDecorator reqContextDecorator = new RequestContextDecorator(reqContext);
            workingMemory.insert(reqContextDecorator);
            workingMemory.insert(token);

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


    protected void checkInitializationCompleted() {
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during wait for initialization");
        }
    }
}
