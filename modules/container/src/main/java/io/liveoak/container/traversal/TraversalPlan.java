package io.liveoak.container.traversal;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class TraversalPlan {

    public TraversalPlan(RequestType requestType, ResourcePath path) {
        List<ResourcePath.Segment> segments = path.segments();

        for (ResourcePath.Segment segment : segments) {
            if (!segment.name().equals("")) {
                this.steps.add(new ReadStep(segment.name()));
            }
            if (segment.matrixParameters().containsKey("config")) {
                this.steps.add(new ConfigurationStep());
            }
        }
        switch (requestType) {
            case CREATE:
                this.steps.add(new CreateStep());
                break;
            case READ:
                // nothing
                break;
            case UPDATE:
                this.steps.add(new UpdateStep());
                break;
            case DELETE:
                this.steps.add(new DeleteStep());
                break;
        }
    }

    public List<Step> steps() {
        return this.steps;
    }

    public void replace(Step oldStep, Step newStep) {
        this.steps().replaceAll((in) -> {
            if (in == oldStep) {
                return newStep;
            }
            return in;
        });
    }

    public String toString() {
        return "[TraversalPlan: steps=" + this.steps() + "]";
    }

    private List<Step> steps = new ArrayList<>();

// ----------------------------------------------------------------------

    public static interface Step {
        void execute(StepContext context, Resource resource) throws Exception;

        default Responder createResponder(TraversingResponder responder) {
            return responder;
        }
    }

    public static interface StepContext {
        RequestContext requestContext();

        ResourceState state();

        Responder responder();

        Runnable invocation();
    }
}
