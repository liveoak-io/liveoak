/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.traversal;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class TraversingResponder extends BaseResponder {

    public TraversingResponder(Executor executor, GlobalContext globalContext, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(inReplyTo, ctx);
        this.executor = executor;
        this.currentResource = globalContext;
        this.plan = new TraversalPlan(inReplyTo.requestType(), inReplyTo.resourcePath());
    }

    @Override
    public void resourceRead(Resource resource) {
        if (isLastStep()) {
            super.resourceRead(resource);
            return;
        }
        this.currentResource = resource;
        doNextStep(resource);
    }

    public void doNextStep(Resource resource) {
        ++this.stepNumber;
        TraversalPlan.Step step = this.plan.steps().get(this.stepNumber);
        doStep(step, resource);
    }

    protected void doStep(TraversalPlan.Step step, Resource resource) {
        Responder nextResponder = nextResponder();

        AtomicReference<Runnable> ref = new AtomicReference<>();

        TraversalPlan.StepContext stepContext = new TraversalPlan.StepContext() {
            @Override
            public RequestContext requestContext() {
                return inReplyTo().requestContext();
            }

            @Override
            public ResourceState state() {
                return inReplyTo().state();
            }

            @Override
            public Responder responder() {
                return nextResponder;
            }

            @Override
            public Runnable invocation() {
                return ref.get();
            }
        };

        Runnable stepRunner = () -> {
            if (resource instanceof BlockingResource) {
                this.executor.execute(() -> {
                    try {
                        step.execute(stepContext, resource);
                    } catch (Throwable t) {
                        internalError(t);
                    }
                });
            } else {
                try {
                    step.execute(stepContext, resource);
                } catch (Throwable t) {
                    internalError(t);
                }
            }
        };
        ref.set(stepRunner);

        stepRunner.run();
    }

    @Override
    public void noSuchResource(String id) {
        if (isLastStep()) {
            if (currentResource != null) {
                int lastDotLoc = id.lastIndexOf('.');
                if (lastDotLoc >= 0) {
                    String idWithoutExtension = id.substring(0, lastDotLoc);
                    doStep(new ReadStep(idWithoutExtension), currentResource);
                    return;
                }
            }
        }

        super.noSuchResource(id);
    }

    protected Resource currentResource() {
        return this.currentResource;
    }

    protected TraversalPlan.Step currentStep() {
        return this.plan.steps().get(this.stepNumber);
    }

    protected TraversalPlan.Step nextStep() {
        if (isLastStep() ) {
            return null;
        }
        return this.plan.steps().get( this.stepNumber + 1 );
    }

    protected Responder nextResponder() {
        TraversalPlan.Step nextStep = nextStep();
        if ( nextStep == null ) {
            return this;
        }
        return nextStep.createResponder( this );
    }

    protected boolean isLastStep() {
        return this.stepNumber == this.plan.steps().size() - 1;
    }

    void replaceStep(TraversalPlan.Step oldStep, TraversalPlan.Step newStep) {
        this.plan.replace( oldStep, newStep );

    }

    private TraversalPlan plan;
    private int stepNumber = -1;

    private Executor executor;

    private Resource currentResource;
}
