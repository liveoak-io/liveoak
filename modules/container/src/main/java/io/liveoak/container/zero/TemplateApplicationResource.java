package io.liveoak.container.zero;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.applications.templates.TemplateRegistry;
import io.liveoak.common.util.StringPropertyReplacer;
import io.liveoak.container.extension.system.service.ExtensionService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.git.GitHelper;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.spi.util.ObjectMapperFactory;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by mwringe on 11/02/15.
 */
public class TemplateApplicationResource implements RootResource, SynchronousResource {

    private Resource parent;
    private final InternalApplicationRegistry applicationRegistry;
    private final File appsDir;
    private final TemplateRegistry templateRegistry;

    // TODO: we should really define this at a more common location for all the *ApplicationResource files
    private static final String ID = "applications";
    private static final String APPLICATION_FILE_NAME = "application.json";
    private static final String NAME = "name";


    private static final String TOKENS = "tokens";
    private static final String TEMPLATE = "template";

    public TemplateApplicationResource(InternalApplicationRegistry applicationRegistry, TemplateRegistry templateRegistry, File appsDir) {
        this.applicationRegistry = applicationRegistry;
        this.appsDir = appsDir;
        this.templateRegistry = templateRegistry;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String id = state.id();

        // generate a default id if none is provided
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        String name = state.getProperty(NAME, false, String.class);
        if (name == null) {
            name = id;
        }

        String template = (String) state.getProperty(TEMPLATE);
        // if the template is null, fall back to the default 'module' template
        if (template == null) {
            template = ExtensionService.MODULE;
        }

        // get the template configuration
        ObjectNode templateConfig = templateRegistry.getTemplate(template);
        if (templateConfig == null) {
            responder.invalidRequest("Could not find the '" + template + "' template. Cannot create application.");
            return;
        }

        // setup the properties which will be used to fill in the templates
        Properties properties = new Properties();
        // The id and name are handled in a slightly different manner than the other normal tokens. Add them here
        properties.put("id", id);
        properties.put(NAME, name);

        // grab everything else from the 'token' object and add them to the
        ResourceState tokens = state.getPropertyAsResourceState(TOKENS);
        if (tokens != null) {
            for (String property : tokens.getPropertyNames()) {
                properties.put(property, tokens.getProperty(property));
            }
        }

        //We need to use #{ in the template, otherwise when the configuration file gets read it will be replaced
        //with system properties at runtime. We need to replace it here so the StringPropertyReplacer will work
        String templateString = templateConfig.toString().replace("#{", "${");

        // replace the properties in the json string
        String applicationJsonString = StringPropertyReplacer.replaceProperties(templateString, properties);
        applicationJsonString = applicationJsonString.replace("${", "#{");

        // convert back to a JSON object so that it can be written nicely to a file
        JsonNode node = ObjectMapperFactory.create().readTree(applicationJsonString);

        File directory = new File(this.appsDir, id);
        if (!directory.exists()) {
            directory.mkdirs();
        } else {
            responder.invalidRequest("The directory for the '" + id + "' application already exists.");
            return;
        }

        Git gitRepo = GitHelper.initRepo(directory);

        // Write the application.json file to the disk
        File applicationFile = new File(directory, APPLICATION_FILE_NAME);
        ObjectMapperFactory.createWriter().writeValue(applicationFile, node);

        final String templateName = template;
        try {
            InternalApplication app = this.applicationRegistry.createApplication(id, name, directory, d -> {
                try {
                    GitHelper.addAllAndCommit(gitRepo, ctx.securityContext().getUser(), "Initial creation of LiveOak application from the '" + templateName + "' template.");
                    gitRepo.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            responder.resourceCreated(app.resource());
        } catch (Exception e) {
            responder.internalError(e);
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.readNotSupported(this);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        responder.deleteNotSupported(this);
    }
}
