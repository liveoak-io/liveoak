package io.liveoak.security.policy.uri.complex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import io.liveoak.security.policy.uri.integration.URIPolicyConfig;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigRule;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.template.DataProviderCompiler;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyConfigurator {

    private static final Logger log = Logger.getLogger(URIPolicyConfigurator.class);

    private RuleBase ruleBase;
    private PackageBuilder packageBuilder;

    public void configure(URIPolicy policy, URIPolicyConfig uriPolicyConfig) {
        doInit();

        int counter = 0;
        for (URIPolicyConfigRule cfgRule : uriPolicyConfig.getRules()) {
            URIPolicyRule internalRule = URIPolicyRule.createRule(counter++, cfgRule.getPriority(), cfgRule.getUriPattern(),
                    cfgRule.getConditions(), cfgRule.getRequestType(), cfgRule.getAllowedRoles(), cfgRule.getDeniedRoles(),
                    cfgRule.getAllowedUsers(), cfgRule.getDeniedUsers());
            this.addURIPolicyRule(internalRule);
        }

        // Now whole package is configured. Creating RuleBase
        org.drools.rule.Package rulesPackage = packageBuilder.getPackage();
        this.ruleBase.addPackage(rulesPackage);

        policy.setRuleBase(this.ruleBase);
        log.info("Drools engine for URIPolicy initialized and configured successfully");
    }

    /**
     * Drools initialization
     */
    protected void doInit() {
        // Workaround for https://issues.jboss.org/browse/DROOLS-329 TODO: Remove when not needed or move to better place
        System.setProperty("drools.dialect.java.compiler", "JANINO");

        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration(URIPolicy.class.getClassLoader());
        this.ruleBase = RuleBaseFactory.newRuleBase(ruleBaseConfig);
        this.packageBuilder = new PackageBuilder(this.ruleBase);

        // Add DRL with functions
        InputStream functionsFileStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyFunctions.drl");
        Reader functionsFileReader = new InputStreamReader(functionsFileStream);
        addPackage(functionsFileReader);
    }

    protected void addURIPolicyRule(URIPolicyRule uriPolicyRule) {
        InputStream templateStream = URIPolicy.class.getClassLoader().getResourceAsStream("templates/URIPolicyTemplate.drl");
        URIPolicyTemplateDataProvider tdp = new URIPolicyTemplateDataProvider(uriPolicyRule);
        DataProviderCompiler converter = new DataProviderCompiler();
        String drl = converter.compile(tdp, templateStream);

        if (log.isDebugEnabled()) {
            log.debug("------------ ADDING NEW POLICY RULE INTO DROOLS ENGINE ----------------------");
            log.debug(drl);
            log.debug("------------ END NEW POLICY RULE ------------------");
        }

        // Then add it into drools RuleBase
        addPackage(drl);
    }

    protected void addPackage(String drl) {
        Reader uriPolicyEntriesReader = new StringReader(drl);
        addPackage(uriPolicyEntriesReader);
    }


    protected void addPackage(Reader packageReader) {
        try {
            this.packageBuilder.addPackageFromDrl(packageReader);
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
    }
}
